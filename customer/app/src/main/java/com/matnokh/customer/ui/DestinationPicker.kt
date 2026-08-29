package com.matnokh.customer.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.matnokh.customer.R
import kotlinx.coroutines.launch

/* صف عنوان التوصيل المدمج في السلة/طلب المكان — يعرض الوجهة الحالية مع زر تغيير */
@Composable
fun DestRow(onChange: () -> Unit) {
    val ctx = LocalContext.current
    // أول مرة: عبّئ الوجهة تلقائياً من الموقع الحالي إن لم تُحدَّد بعد
    LaunchedEffect(Unit) {
        if (Sel.destLat == null) currentLatLng(ctx)?.let { Sel.destLat = it.first; Sel.destLng = it.second }
    }
    Row(
        Modifier.padding(horizontal = 22.dp, vertical = 4.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(C.card).border(1.dp, Color(0xFFCFE0D4), RoundedCornerShape(18.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(C.pillLive), contentAlignment = Alignment.Center) {
            Ic(R.drawable.ic_pin, 22.dp, C.greenD)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            T(tr("عنوان التوصيل", "Delivery address"), 10, FontWeight.Normal, C.muted)
            T(Sel.destLabel, 13, FontWeight.Bold, C.head, maxLines = 1)
            Sel.destAddr?.takeIf { it.isNotBlank() }?.let { T(it, 10, FontWeight.Normal, C.muted, maxLines = 1) }
        }
        Box(
            Modifier.clip(RoundedCornerShape(12.dp)).background(C.pillLive).clickable(onClick = onChange)
                .padding(horizontal = 15.dp, vertical = 9.dp),
        ) { T(tr("تغيير", "Change"), 11, FontWeight.ExtraBold, C.greenD) }
    }
}

/* شاشة اختيار الوجهة على الخريطة: حرّك الخريطة → «تأكيد هذا الموقع» أو «موقعي الحالي» */
@Composable
fun DestinationScreen(onBack: () -> Unit, toast: (String) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    val start = LatLng(Sel.destLat ?: 24.7136, Sel.destLng ?: 46.6753)
    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(start, 15f) }

    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        // الخريطة تفتح مباشرة على الموقع الحالي إن لم تكن هناك وجهة محفوظة
        if (Sel.destLat == null) currentLatLng(ctx)?.let {
            camera.position = CameraPosition.fromLatLngZoom(LatLng(it.first, it.second), 16f)
        }
    }

    Box(Modifier.fillMaxSize().background(C.bg)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = MapProperties(isMyLocationEnabled = granted),
            uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false, compassEnabled = true, mapToolbarEnabled = false),
        ) { OsmTiles() }
        // دبوس ثابت في منتصف الشاشة — يمثّل الوجهة
        Box(Modifier.align(Alignment.Center).padding(bottom = 44.dp), contentAlignment = Alignment.Center) {
            Ic(R.drawable.ic_pin, 48.dp, C.greenD)
        }
        // زر الرجوع
        Box(
            Modifier.align(Alignment.TopStart).statusBarsPadding().padding(14.dp).size(46.dp).clip(RoundedCornerShape(16.dp))
                .background(C.card).border(1.dp, C.line, RoundedCornerShape(16.dp)).clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) { Ic(R.drawable.ic_back, 20.dp, C.head) }
        // شريط الإرشاد
        Box(
            Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(14.dp).clip(RoundedCornerShape(50.dp))
                .background(Color(0xF2FFFFFF)).border(1.dp, C.line, RoundedCornerShape(50.dp)).padding(horizontal = 16.dp, vertical = 10.dp),
        ) { T(tr("حرّك الخريطة لتحديد وجهتك", "Move the map to set your destination"), 12, FontWeight.ExtraBold, C.head) }
        // زر موقعي الحالي (إعادة التمركز)
        Box(
            Modifier.align(Alignment.CenterEnd).padding(16.dp).size(48.dp).clip(CircleShape).background(C.card)
                .border(1.dp, C.line, CircleShape).clickable {
                    scope.launch {
                        currentLatLng(ctx)?.let { camera.position = CameraPosition.fromLatLngZoom(LatLng(it.first, it.second), 16f) }
                            ?: toast(tr("تعذّر تحديد موقعك", "Couldn't determine your location"))
                    }
                },
            contentAlignment = Alignment.Center,
        ) { Ic(R.drawable.ic_nav, 22.dp, C.greenD) }
        // أزرار التأكيد
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(18.dp)) {
            SavedAddrStrip("receive") { a -> Sel.destLat = a.lat; Sel.destLng = a.lng; Sel.destLabel = a.label; Sel.destAddr = a.address; toast(tr("الوجهة: ${a.label} ✓", "Destination: ${a.label} ✓")); onBack() }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Grad.green).clickable {
                    val t = camera.position.target
                    Sel.destLat = t.latitude; Sel.destLng = t.longitude
                    Sel.destLabel = tr("موقع محدد على الخريطة", "A set location on the map"); Sel.destAddr = null
                    toast(tr("تم تحديد الوجهة ✓", "Destination set ✓")); onBack()
                }.padding(vertical = 15.dp),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
            ) {
                T(tr("تأكيد هذا الموقع", "Confirm this location"), 14, FontWeight.ExtraBold, Color.White); Spacer(Modifier.width(8.dp)); Ic(R.drawable.ic_check, 16.dp, Color.White)
            }
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(15.dp))
                    .clickable {
                        scope.launch {
                            val loc = currentLatLng(ctx)
                            if (loc == null) { toast(tr("تعذّر تحديد موقعك", "Couldn't determine your location")); return@launch }
                            Sel.destLat = loc.first; Sel.destLng = loc.second
                            Sel.destLabel = tr("موقعي الحالي", "My current location"); Sel.destAddr = null
                            toast(tr("سيتم التوصيل إلى موقعك الحالي ✓", "Delivery will be to your current location ✓")); onBack()
                        }
                    }.padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) { T(tr("استخدم موقعي الحالي", "Use my current location"), 13, FontWeight.ExtraBold, C.greenD) }
        }
    }
}
