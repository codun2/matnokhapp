package com.matnokh.driver.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.driver.R
import kotlinx.coroutines.launch

@Composable
fun ActiveScreen(job: Job?, fare: Int, onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit, onStatus: (String) -> Unit, onFinish: () -> Unit, onExpand: () -> Unit, onChat: () -> Unit = {}) {
    if (job == null) {
        Column(Modifier.fillMaxSize().background(C.bg)) {
            ScreenHeader("الطلب النشط", onBack, onMenu)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { T("لا يوجد طلب نشط الآن", 13, FontWeight.Medium, C.muted) }
        }
        return
    }
    val step = Drv.activeStep.value
    val scope = rememberCoroutineScope()
    var showGiveUp by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("الطلب النشط", onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // خريطة محاكاة
            Box(Modifier.fillMaxWidth().height(260.dp)) {
                val ctx = androidx.compose.ui.platform.LocalContext.current
                val fLat = job.fromLat; val fLng = job.fromLng; val tLat = job.toLat; val tLng = job.toLng
                val meLat = Drv.driverLat.value; val meLng = Drv.driverLng.value
                // موقع المندوب الحيّ يتحدّث كل 12ث ليتحرّك المؤشّر كما يرى الزبون
                androidx.compose.runtime.LaunchedEffect(Unit) { while (true) { runCatching { currentLatLng(ctx)?.let { Drv.driverLat.value = it.first; Drv.driverLng.value = it.second } }; kotlinx.coroutines.delay(12000) } }
                val from = if (fLat != null && fLng != null) com.google.android.gms.maps.model.LatLng(fLat, fLng) else null
                val to = if (tLat != null && tLng != null) com.google.android.gms.maps.model.LatLng(tLat, tLng) else null
                val me = if (meLat != null && meLng != null) com.google.android.gms.maps.model.LatLng(meLat, meLng) else null
                val target = (if (step >= 2) to else from) ?: to ?: from
                val focus = me ?: from ?: to ?: com.google.android.gms.maps.model.LatLng(24.7136, 46.6753)
                val camera = com.google.maps.android.compose.rememberCameraPositionState { position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(focus, 13f) }
                val pts = listOfNotNull(from, to, me)
                androidx.compose.runtime.LaunchedEffect(pts.size, meLat, meLng, fLat, tLat) {
                    if (pts.size >= 2) {
                        val b = com.google.android.gms.maps.model.LatLngBounds.builder(); pts.forEach { b.include(it) }
                        runCatching { camera.animate(com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(b.build(), 120)) }
                    } else if (pts.size == 1) { camera.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(pts.first(), 15f) }
                }
                com.google.maps.android.compose.GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = camera, uiSettings = com.google.maps.android.compose.MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false)) {
                    OsmTiles()
                    from?.let { com.google.maps.android.compose.Marker(state = com.google.maps.android.compose.MarkerState(it), title = "الاستلام — " + job.cust) }
                    to?.let { com.google.maps.android.compose.Marker(state = com.google.maps.android.compose.MarkerState(it), title = "التسليم") }
                    if (me != null && target != null) com.google.maps.android.compose.Polyline(points = listOf(me, target), color = C.green, width = 9f)
                    me?.let { AnimatedCarMarker(it, "أنا", ctx, target) }
                }
                Row(Modifier.align(Alignment.TopStart).padding(14.dp).clip(RoundedCornerShape(15.dp)).background(Color(0xE6FFFFFF)).padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Ic(R.drawable.ic_nav, 15.dp, C.green); Spacer(Modifier.width(7.dp)); T("#" + job.id, 12, FontWeight.ExtraBold, Color(0xFF4B5A51))
                }
                Box(Modifier.align(Alignment.TopEnd).padding(14.dp).clip(RoundedCornerShape(50.dp)).background(Color(0xF2FFFFFF)).clickable(onClick = onExpand).padding(horizontal = 12.dp, vertical = 8.dp)) { T("🗺️ ملء الشاشة", 11, FontWeight.ExtraBold, C.greenD) }
                if (me != null && target != null && step < 4) Row(Modifier.align(Alignment.BottomStart).padding(14.dp).clip(RoundedCornerShape(15.dp)).background(Color(0xE6FFFFFF)).padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) { Ic(R.drawable.ic_clock, 14.dp, C.green); Spacer(Modifier.width(6.dp)); T((if (step >= 2) "للتسليم: " else "للاستلام: ") + etaText(haversineKm(me, target)), 12, FontWeight.ExtraBold, C.greenD) }
            }
            // اللوحة السفلية
            Column(Modifier.fillMaxWidth().offset(y = (-24).dp).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(C.bg).padding(horizontal = 22.dp, vertical = 14.dp)) {
                Box(Modifier.align(Alignment.CenterHorizontally).width(44.dp).height(5.dp).clip(RoundedCornerShape(5.dp)).background(C.trackOff)); Spacer(Modifier.height(16.dp))
                // مؤشر المراحل
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    DSTEPS.forEachIndexed { i, s ->
                        val n = i + 1
                        val done = n < step; val now = n == step
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
                            Box(Modifier.size(26.dp).clip(CircleShape).background(if (done) C.green else Color.White).border(2.dp, if (done) C.green else if (now) C.green else C.trackOff, CircleShape), contentAlignment = Alignment.Center) {
                                if (n <= step) Ic(if (done) R.drawable.ic_check else s.second, 13.dp, if (done) Color.White else C.green)
                            }
                            Spacer(Modifier.height(6.dp)); T(s.first, 10, if (done || now) FontWeight.Bold else FontWeight.Normal, if (done || now) Color(0xFF4B5A51) else Color(0xFFA3ACA2), maxLines = 1)
                        }
                        if (i < DSTEPS.size - 1) Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(3.dp)).background(if (n < step) C.green else C.trackOff))
                    }
                }
                Spacer(Modifier.height(18.dp))
                // بطاقة الزبون
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(17.dp)).background(Grad.terra), contentAlignment = Alignment.Center) { T(job.av, 16, FontWeight.ExtraBold, Color.White) }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) { T(job.cust, 14, FontWeight.Bold, C.head); Spacer(Modifier.height(2.dp)); T("${job.to} · أجرتك ﷼$fare", 11, FontWeight.Normal, C.muted, maxLines = 1) }
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(C.card2).clickable { onChat() }, contentAlignment = Alignment.Center) { Ic(R.drawable.ic_msg, 17.dp, Color(0xFF5D6B62)) }
                    Spacer(Modifier.width(9.dp))
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(Grad.green), contentAlignment = Alignment.Center) { Box(Modifier.clickable { toast("اتصال بالزبون") }, contentAlignment = Alignment.Center) { Ic(R.drawable.ic_phone, 17.dp, Color.White) } }
                }
                Spacer(Modifier.height(14.dp))
                // زر المرحلة
                when (step) {
                    1 -> Column {
                        WideButton("حمّلت الشحنة — انطلق", R.drawable.ic_check) { onStatus("loaded") }
                        Spacer(Modifier.height(10.dp))
                        if (!showGiveUp) {
                            WideButton("تخلّي عن الطلب", ghost = true) { showGiveUp = true }
                        } else {
                            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(18.dp)).padding(14.dp)) {
                                T("التخلّي عن الطلب سيُسنده لمندوب آخر ويُسجّل عليك. متأكد؟", 12, FontWeight.Bold, C.head)
                                Spacer(Modifier.height(10.dp))
                                Row {
                                    Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Grad.terra).clickable { showGiveUp = false; scope.launch { val ok = if (job.isStore) repoStoreRelinquish(job.oid, toast) else repoTransportRelinquish(job.oid, toast); if (ok) onFinish() } }.padding(vertical = 11.dp), contentAlignment = Alignment.Center) { T("نعم، تخلّيت", 13, FontWeight.ExtraBold, Color.White) }
                                    Spacer(Modifier.width(10.dp))
                                    Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(C.card2).clickable { showGiveUp = false }.padding(vertical = 11.dp), contentAlignment = Alignment.Center) { T("تراجع", 13, FontWeight.ExtraBold, C.muted) }
                                }
                            }
                        }
                    }
                    2 -> WideButton("بدأت الرحلة — في الطريق", R.drawable.ic_nav) { onStatus("on_the_way") }
                    3 -> WideButton("تم التسليم للزبون", R.drawable.ic_flag) { onStatus("delivered") }
                    else -> Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        T("أنجزت الرحلة 🎉  +﷼$fare", 15, FontWeight.Black, C.greenD)
                        Spacer(Modifier.height(8.dp)); T("انتقل الطلب إلى «طلباتي — السابقة» وعدت متاحاً", 11, FontWeight.Normal, C.muted)
                        Spacer(Modifier.height(14.dp)); WideButton("العودة للاستقبال", R.drawable.ic_nav, onClick = onFinish)
                    }
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}
