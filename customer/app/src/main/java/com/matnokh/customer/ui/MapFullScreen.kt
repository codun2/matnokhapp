package com.matnokh.customer.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.matnokh.customer.R
import com.matnokh.customer.net.Repo
import com.matnokh.customer.net.UiStore

/* خريطة المتاجر بملء الشاشة + أزرار تحكّم (موقعي · تقريب · إغلاق) */
@Composable
fun StoresMapFull(onBack: () -> Unit, onStore: (UiStore) -> Unit) {
    val ctx = LocalContext.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }

    var cat by remember { mutableStateOf<Int?>(null) }
    val all = Repo.stores.filter { it.lat != null && it.lng != null }
    val stores = if (cat == null) all else all.filter { s -> Repo.categories.firstOrNull { it.id == cat }?.let { s.categoryName == it.name } ?: true }
    val center = all.firstOrNull()?.let { LatLng(it.lat!!, it.lng!!) } ?: LatLng(24.7136, 46.6753)
    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(center, 12f) }

    Box(Modifier.fillMaxSize().background(C.bg)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = MapProperties(isMyLocationEnabled = granted),
            uiSettings = MapUiSettings(myLocationButtonEnabled = granted, zoomControlsEnabled = true, mapToolbarEnabled = true, compassEnabled = true),
        ) {
            OsmTiles()
            stores.forEach { s ->
                Marker(state = MarkerState(LatLng(s.lat!!, s.lng!!)), title = s.name, snippet = trd(s.categoryName, s.categoryNameEn), onClick = { onStore(s); true })
            }
        }
        // زر الإغلاق
        Box(Modifier.align(Alignment.TopStart).statusBarsPadding().padding(14.dp).size(46.dp).clip(RoundedCornerShape(16.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(16.dp)).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
            Ic(R.drawable.ic_back, 20.dp, C.head)
        }
        Box(Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(14.dp).clip(RoundedCornerShape(50.dp)).background(Color(0xF2FFFFFF)).border(1.dp, C.line, RoundedCornerShape(50.dp)).padding(horizontal = 16.dp, vertical = 10.dp)) {
            T(tr("${stores.size} خدمة قريبة", "${stores.size} nearby services"), 13, FontWeight.ExtraBold, C.head)
        }
        // فلتر الأنواع
        Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().horizontalScroll(rememberScrollState()).padding(14.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(tr("الكل", "All"), cat == null) { cat = null }
            Repo.categories.forEach { c -> FilterChip(catText(c.icon, trd(c.name, c.name_en)), cat == c.id) { cat = c.id } }
        }
    }
}

@Composable
private fun FilterChip(label: String, on: Boolean, onClick: () -> Unit) {
    Box(Modifier.clip(CircleShape).then(if (on) Modifier.background(Grad.green) else Modifier.background(Color(0xF2FFFFFF))).border(1.dp, if (on) Color.Transparent else C.line, CircleShape).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 9.dp)) {
        T(label, 11, FontWeight.ExtraBold, if (on) Color.White else Color(0xFF4B5A51))
    }
}
