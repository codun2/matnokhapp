@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.matnokh.customer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.matnokh.customer.R
import com.matnokh.customer.net.call
import com.matnokh.customer.net.Session
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

private val areas = listOf("حي النرجس", "شارع الملك فهد", "الملقا", "العليا", "الياسمين", "حي الورود")

/* ── إنشاء طلب نقل ── */
@Composable
fun OrderScreen(onBack: () -> Unit, onMenu: () -> Unit, onCreated: (Int) -> Unit, toast: (String) -> Unit) {
    val svc = Sel.svc ?: run { onBack(); return }
    val hasDropoff = svc.point_type != "pickup_only"
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var target by remember { mutableStateOf("a") }
    var note by remember { mutableStateOf("") }
    var locA by remember { mutableStateOf<LatLng?>(null) }
    var locB by remember { mutableStateOf<LatLng?>(null) }
    var areaA by remember { mutableStateOf<String?>(null) }
    var areaB by remember { mutableStateOf<String?>(null) }
    var mapFull by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(24.7136, 46.6753), 12f) }
    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        currentLatLng(ctx)?.let { camera.position = CameraPosition.fromLatLngZoom(LatLng(it.first, it.second), 14f) }
    }
    val handleClick: (LatLng) -> Unit = { ll ->
        val isA = !hasDropoff || target == "a"
        if (isA) { locA = ll; areaA = "جارٍ تحديد الاسم…"; if (hasDropoff) target = "b" } else { locB = ll; areaB = "جارٍ تحديد الاسم…"; target = "a" }
        scope.launch { val nm = reverseName(ll.latitude, ll.longitude) ?: "موقع على الخريطة"; if (isA) areaA = nm else areaB = nm }
    }
    val hasDist = hasDropoff && locA != null && locB != null
    val distExtraMin = if (hasDist) Math.round(distanceKm(locA!!, locB!!) * com.matnokh.customer.net.Repo.kmMin).toInt() else 0
    val distExtraMax = if (hasDist) Math.round(distanceKm(locA!!, locB!!) * com.matnokh.customer.net.Repo.kmMax).toInt() else 0
    val base = svc.base_price.toInt()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(C.bg)) {
            ScreenHeader("طلب — ${svc.name}", onBack, onMenu)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                    Row(Modifier.clip(RoundedCornerShape(18.dp)).background(Color(0xFFEEF4EF)).border(1.5.dp, Color(0xFFCFE0D4), RoundedCornerShape(18.dp)).padding(horizontal = 15.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(Grad.green), contentAlignment = Alignment.Center) { val si = svc.icon; when { si != null && si.startsWith("http") -> coil.compose.AsyncImage(model = si, contentDescription = null, contentScale = androidx.compose.ui.layout.ContentScale.Crop, modifier = Modifier.size(46.dp).clip(RoundedCornerShape(15.dp))); !si.isNullOrBlank() -> T(si, 22, FontWeight.Bold, Color.White); else -> Ic(R.drawable.ic_van, 22.dp, Color.White) } }
                        Spacer(Modifier.width(12.dp))
                        Column { T("خدمة: ${svc.name}", 13, FontWeight.Bold, C.head); Spacer(Modifier.height(2.dp)); T(if (hasDropoff) "حدّد نقطة الاستلام والتسليم — يصل طلبك لسائقي هذه الخدمة فقط" else "حدّد نقطة الاستلام فقط — يصل لسائقي هذه الخدمة", 10, FontWeight.Normal, C.muted, lineHeight = 16) }
                    }
                }
                Spacer(Modifier.height(14.dp))
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                    OcTitle(R.drawable.ic_pin, if (hasDropoff) "حدّد الاستلام والتسليم على الخريطة" else "حدّد نقطة الاستلام على الخريطة")
                    if (hasDropoff) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PMode("نقطة الاستلام", R.drawable.ic_pin, target == "a", C.green, Modifier.weight(1f)) { target = "a" }
                            PMode("نقطة التسليم", R.drawable.ic_flag, target == "b", C.terra, Modifier.weight(1f)) { target = "b" }
                        }
                    }
                    SavedAddrStrip(if (!hasDropoff || target == "a") "pickup" else "receive") { a ->
                        val ll = LatLng(a.lat ?: 24.7136, a.lng ?: 46.6753)
                        if (!hasDropoff || target == "a") { locA = ll; areaA = a.label; if (hasDropoff) target = "b" } else { locB = ll; areaB = a.label; target = "a" }
                        camera.position = CameraPosition.fromLatLngZoom(ll, 15f)
                    }
                    Spacer(Modifier.height(11.dp))
                    Box(Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(18.dp)).border(1.dp, C.line, RoundedCornerShape(18.dp))) {
                        if (!mapFull) {
                            OrderPickMap(camera, granted, locA, locB, handleClick, Modifier.fillMaxSize())
                            Box(Modifier.align(Alignment.BottomStart).padding(10.dp).clip(RoundedCornerShape(50.dp)).background(Color(0xF2FFFFFF)).border(1.dp, C.line, RoundedCornerShape(50.dp)).padding(horizontal = 13.dp, vertical = 7.dp)) {
                                T(if (!hasDropoff || target == "a") "اضغط لتحديد الاستلام" else "اضغط لتحديد التسليم", 10, FontWeight.ExtraBold, if (!hasDropoff || target == "a") C.greenD else C.terraText)
                            }
                            Box(Modifier.align(Alignment.TopEnd).padding(10.dp).clip(RoundedCornerShape(50.dp)).background(Color(0xF2FFFFFF)).border(1.dp, C.line, RoundedCornerShape(50.dp)).clickable { mapFull = true }.padding(horizontal = 12.dp, vertical = 8.dp)) { T("🗺️ ملء الشاشة", 11, FontWeight.ExtraBold, C.greenD) }
                        } else {
                            Box(Modifier.fillMaxSize().background(C.card2), contentAlignment = Alignment.Center) { T("الخريطة مفتوحة بملء الشاشة …", 12, FontWeight.ExtraBold, C.muted) }
                        }
                    }
                    Spacer(Modifier.height(11.dp))
                    LocBox(R.drawable.ic_pin, C.green, "الاستلام", areaA)
                    if (hasDropoff) { Spacer(Modifier.height(9.dp)); LocBox(R.drawable.ic_flag, C.terra, "التسليم", areaB) }
                }
                Spacer(Modifier.height(14.dp))
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                    OcTitle(R.drawable.ic_msg, "ملاحظات الطلب")
                    FinField(note, { note = it }, "تفاصيل الحمولة أو ملاحظات للمندوب (النوع، الوزن، طابق، وقت مناسب…)", singleLine = false, minHeight = 84.dp)
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.padding(horizontal = 22.dp, vertical = 12.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.5.dp, Color(0xFFCFE0D4), RoundedCornerShape(22.dp)).padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        T("التكلفة التقديرية", 11, FontWeight.Normal, C.muted)
                        if (hasDist) { T("﷼${base + distExtraMin} – ﷼${base + distExtraMax}", 20, FontWeight.Black, C.greenD); T("~${Math.round(distanceKm(locA!!, locB!!))} كم × سعر الكيلو", 10, FontWeight.Medium, C.muted) }
                        else if (!hasDropoff) T(if (base > 0) "﷼$base" else "حسب عرض السائق", 18, FontWeight.Black, C.greenD)
                        else T("حدّد النقطتين للتقدير", 15, FontWeight.Bold, C.muted)
                    }
                    T("تقديري — السعر النهائي\nحسب عرض السائق الفائز", 10, FontWeight.Normal, C.muted, lineHeight = 16)
                }
                Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Grad.green).clickable {
                    if (sending) return@clickable
                    if (!Session.isLoggedIn()) { toast("سجّل الدخول أولاً"); return@clickable }
                    if (locA == null) { toast("حدّد نقطة الاستلام على الخريطة"); return@clickable }
                    if (hasDropoff && locB == null) { toast("حدّد نقطة التسليم على الخريطة"); return@clickable }
                    sending = true
                    scope.launch {
                        val price = (if (distExtraMax > 0) base + distExtraMax else base).toDouble()
                        val body = com.matnokh.customer.net.TransportBody(svc.key, svc.name, areaA ?: "موقع الاستلام", if (hasDropoff) (areaB ?: "موقع التسليم") else null, locA!!.latitude, locA!!.longitude, note.ifBlank { null }, "bid", price, "cash", if (hasDropoff) locB?.latitude else null, if (hasDropoff) locB?.longitude else null)
                        val r = call({ com.matnokh.customer.net.Net.api.createTransport(body) }, toast)
                        sending = false
                        if (r?.order_id != null) { Sel.svcName = svc.name; toast(r.message ?: "أُرسل طلبك للسائقين ✓"); onCreated(r.order_id!!) }
                    }
                }.padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    T(if (sending) "جارٍ الإرسال…" else "تأكيد الطلب واستقبال العروض", 14, FontWeight.ExtraBold, Color.White); Spacer(Modifier.width(8.dp)); Ic(R.drawable.ic_check, 16.dp, Color.White)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
        if (mapFull) {
            Box(Modifier.fillMaxSize().background(C.bg)) {
                OrderPickMap(camera, granted, locA, locB, handleClick, Modifier.fillMaxSize())
                Box(Modifier.align(Alignment.TopStart).statusBarsPadding().padding(14.dp).size(46.dp).clip(RoundedCornerShape(16.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(16.dp)).clickable { mapFull = false }, contentAlignment = Alignment.Center) { Ic(R.drawable.ic_back, 20.dp, C.head) }
                Box(Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(14.dp).clip(RoundedCornerShape(50.dp)).background(Color(0xF2FFFFFF)).border(1.dp, C.line, RoundedCornerShape(50.dp)).padding(horizontal = 16.dp, vertical = 10.dp)) { T(if (!hasDropoff || target == "a") "اضغط لتحديد الاستلام" else "اضغط لتحديد التسليم", 12, FontWeight.ExtraBold, if (!hasDropoff || target == "a") C.greenD else C.terraText) }
                if (hasDropoff) Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PMode("نقطة الاستلام", R.drawable.ic_pin, target == "a", C.green, Modifier.weight(1f)) { target = "a" }
                    PMode("نقطة التسليم", R.drawable.ic_flag, target == "b", C.terra, Modifier.weight(1f)) { target = "b" }
                }
            }
        }
    }
}


@Composable
private fun OrderPickMap(camera: CameraPositionState, granted: Boolean, locA: LatLng?, locB: LatLng?, onClick: (LatLng) -> Unit, modifier: Modifier) {
    GoogleMap(
        modifier = modifier,
        cameraPositionState = camera,
        properties = MapProperties(isMyLocationEnabled = granted),
        uiSettings = MapUiSettings(myLocationButtonEnabled = granted, zoomControlsEnabled = false, mapToolbarEnabled = false, compassEnabled = false),
        onMapClick = onClick,
    ) {
        OsmTiles()
        locA?.let { Marker(state = MarkerState(it), title = "الاستلام", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) }
        locB?.let { Marker(state = MarkerState(it), title = "التسليم", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)) }
    }
}


@Composable
private fun BoxScope.Pin(off: Offset, density: androidx.compose.ui.unit.Density, w: androidx.compose.ui.unit.Dp, h: androidx.compose.ui.unit.Dp, brush: androidx.compose.ui.graphics.Brush, icon: Int) {
    val x = with(density) { off.x.toDp() }; val y = with(density) { off.y.toDp() }
    Box(Modifier.offset(x = x - 17.dp, y = y - 17.dp).size(34.dp).clip(RoundedCornerShape(12.dp)).background(brush), contentAlignment = Alignment.Center) { Ic(icon, 17.dp, Color.White) }
}

@Composable
private fun PMode(label: String, icon: Int, on: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Row(modifier.clip(RoundedCornerShape(14.dp)).then(if (on) Modifier.background(if (color == C.terra) Color(0xFFF6ECE4) else Color(0xFFEEF4EF)).border(1.5.dp, color, RoundedCornerShape(14.dp)) else Modifier.background(Color(0xFFFAF8F4)).border(1.5.dp, C.line, RoundedCornerShape(14.dp))).clickable(onClick = onClick).padding(vertical = 9.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Ic(icon, 14.dp, if (on) (if (color == C.terra) C.terraText else C.greenD) else C.muted); Spacer(Modifier.width(6.dp)); T(label, 11, FontWeight.ExtraBold, if (on) (if (color == C.terra) C.terraText else C.greenD) else C.muted)
    }
}

@Composable
private fun LocBox(icon: Int, color: Color, label: String, area: String?) {
    val set = area != null
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).then(if (set) Modifier.background(Color(0xFFF2F8F3)).border(1.dp, Color(0xFFCFE0D4), RoundedCornerShape(14.dp)) else Modifier.background(Color(0xFFFAF8F4)).border(1.dp, C.line, RoundedCornerShape(14.dp))).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Ic(icon, 15.dp, color); Spacer(Modifier.width(9.dp))
        if (set) T("$label: $area ✓", 12, FontWeight.Bold, C.head, maxLines = 1) else T("$label: اضغط على الخريطة لتحديدها", 11, FontWeight.Medium, C.muted, maxLines = 1)
    }
}

@Composable
private fun DimField(label: String, value: String, modifier: Modifier) {
    var v by remember { mutableStateOf(value) }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        T(label, 10, FontWeight.Bold, C.muted); Spacer(Modifier.height(5.dp))
        BasicTextField(v, { v = it }, singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Cairo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = C.text, textAlign = TextAlign.Center), cursorBrush = SolidColor(C.green),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(Color(0xFFFAF8F4)).border(1.dp, C.line, RoundedCornerShape(13.dp)).padding(vertical = 11.dp))
    }
}

@Composable
private fun OptChip(label: String, icon: Int, on: Boolean, onClick: () -> Unit) {
    Row(Modifier.clip(CircleShape).then(if (on) Modifier.background(Color(0xFFEEF4EF)).border(1.dp, C.green, CircleShape) else Modifier.background(Color(0xFFFAF8F4)).border(1.dp, C.line, CircleShape)).clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Ic(icon, 14.dp, if (on) C.greenD else C.muted); Spacer(Modifier.width(6.dp)); T(label, 11, FontWeight.Bold, if (on) C.greenD else C.muted)
    }
}

/* ── عروض السائقين (المزايدة) ── */
@Composable
fun OffersScreen(onBack: () -> Unit, onMenu: () -> Unit, onPick: (String, String, String, Int) -> Unit) {
    val svc = Sel.service
    val base = svc.base + 15
    val shown = remember { mutableStateListOf<DriverBid>() }
    LaunchedEffect(Unit) {
        shown.clear()
        CData.driverPool.forEachIndexed { i, d -> delay(if (i == 0) 900 else 1500); shown.add(d) }
    }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("عروض السائقين", onBack, onMenu)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(9.dp).clip(CircleShape).background(C.green))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) { T("تم بثّ طلبك ✓", 13, FontWeight.Bold, C.head); Spacer(Modifier.height(2.dp)); T("أُرسل لسائقي «${svc.name}» الأقرب إليك — العروض تصلك أولاً بأول", 10, FontWeight.Normal, C.muted, lineHeight = 16) }
                        Column(Modifier.clip(RoundedCornerShape(14.dp)).background(Color(0xFFEEF4EF)).padding(horizontal = 13.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) { T("${shown.size}", 17, FontWeight.Black, C.greenD); T("عروض", 9, FontWeight.Normal, C.muted) }
                    }
                }
                SecTitle("العروض المستلمة", "الأقل سعراً")
            }
            if (shown.isEmpty()) item { OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(26.dp)) { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { T("بانتظار أول عرض…", 12, FontWeight.Normal, C.muted) } } }
            items(shown.size) { i ->
                val d = shown[i]; val price = base + d.priceDelta
                Row(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Grad.sand), contentAlignment = Alignment.Center) { T(d.avatar, 14, FontWeight.ExtraBold, Color(0xFF6B5335)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { T(d.name, 13, FontWeight.Bold, C.head, maxLines = 1); Spacer(Modifier.height(2.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Text("★", color = Color(0xFFD9A441), fontSize = 11.sp); Spacer(Modifier.width(4.dp)); T("${d.rating} · ${svc.vehicle} · ${d.eta}", 10, FontWeight.Normal, C.muted, maxLines = 1) } }
                    Column(Modifier.clip(RoundedCornerShape(14.dp)).background(Color(0xFFEEF4EF)).padding(horizontal = 12.dp, vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) { T("﷼$price", 15, FontWeight.Black, C.greenD); T("عرض السائق", 9, FontWeight.Normal, C.muted) }
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.clip(RoundedCornerShape(13.dp)).background(Grad.green).clickable { onPick(d.name, d.avatar, "${d.rating} · ${svc.vehicle}", price) }.padding(horizontal = 14.dp, vertical = 10.dp)) { T("اختيار", 11, FontWeight.ExtraBold, Color.White) }
                }
            }
        }
    }
}

/* ── التتبّع ── */
private val steps = listOf("تم القبول" to R.drawable.ic_check, "تم التحميل" to R.drawable.ic_box, "في الطريق" to R.drawable.ic_van, "التسليم" to R.drawable.ic_flag)

@Composable
fun TrackScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit, onChat: () -> Unit = {}) {
    var step by remember { mutableStateOf(Sel.trackStep) }
    var rated by remember { mutableStateOf(0) }
    LaunchedEffect(rated) { if (rated > 0) runCatching { com.matnokh.customer.net.Net.api.rate(com.matnokh.customer.net.RateBody(Sel.transportId ?: 0, true, rated)) } }
    var ord by remember { mutableStateOf<com.matnokh.customer.net.TOrder?>(null) }
    LaunchedEffect(Unit) { while (true) { val o = runCatching { com.matnokh.customer.net.Net.api.transportOrders().orders.firstOrNull { it.id == Sel.transportId } }.getOrNull(); ord = o; if (o != null) step = when (o.status) { "assigned" -> 1; "loaded" -> 2; "on_the_way" -> 3; "delivered" -> 4; else -> step }; delay(4000) } }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("تتبّع الطلب", onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // خريطة حقيقية
            val trkCtx = LocalContext.current
            var trkHere by remember { mutableStateOf<Pair<Double, Double>?>(null) }
            val trkCam = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(24.7136, 46.6753), 13f) }
            LaunchedEffect(Unit) { currentLatLng(trkCtx)?.let { trkHere = it; trkCam.position = CameraPosition.fromLatLngZoom(LatLng(it.first, it.second), 14f) } }
            LaunchedEffect(ord?.driver?.lat, ord?.driver?.lng) { val dd = ord?.driver; if (dd?.lat != null && dd.lng != null) trkCam.position = CameraPosition.fromLatLngZoom(LatLng(dd.lat, dd.lng), 15f) }
            Box(Modifier.fillMaxWidth().height(300.dp)) {
                GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = trkCam, uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false, compassEnabled = false)) {
                    trkHere?.let { Marker(state = MarkerState(LatLng(it.first, it.second)), title = "موقعك", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) }
                    val oo = ord
                    val dTo = if (oo?.to_lat != null && oo.to_lng != null) LatLng(oo.to_lat, oo.to_lng) else null
                    dTo?.let { Marker(state = MarkerState(it), title = "الوجهة", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)) }
                    oo?.driver?.let { dd -> if (dd.lat != null && dd.lng != null) {
                        if (dTo != null) Polyline(points = listOf(LatLng(dd.lat, dd.lng), dTo), color = C.green, width = 9f)
                        AnimatedCarMarker(LatLng(dd.lat, dd.lng), dd.name ?: "المندوب", trkCtx)
                    } }
                }
                Box(Modifier.align(Alignment.TopStart).padding(14.dp).clip(RoundedCornerShape(15.dp)).background(Color.White.copy(alpha = .9f)).padding(horizontal = 15.dp, vertical = 9.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Ic(R.drawable.ic_nav, 15.dp, C.green); Spacer(Modifier.width(7.dp)); T("مباشر", 12, FontWeight.ExtraBold, Color(0xFF4B5A51)) } }
            }
            // الورقة السفلية
            Column(Modifier.offset(y = (-26).dp).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(C.bg).padding(horizontal = 22.dp, vertical = 10.dp)) {
                Box(Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp, bottom = 16.dp).width(44.dp).height(5.dp).clip(CircleShape).background(Color(0xFFDDD6C9)))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { T(if (step >= 4) "وصلت شحنتك بأمان ✓" else "سائقك في الطريق إليك", 17, FontWeight.Bold, C.head); T("طلب #" + (ord?.order_no ?: "") + " · " + (ord?.service_name ?: Sel.svcName), 12, FontWeight.Normal, C.muted); run { val oo = ord; val dd = oo?.driver; val tlat = oo?.to_lat; val tlng = oo?.to_lng; if (step < 4 && dd?.lat != null && dd.lng != null && tlat != null && tlng != null) { Spacer(Modifier.height(5.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Ic(R.drawable.ic_clock, 13.dp, C.green); Spacer(Modifier.width(5.dp)); T(etaText(haversineKm(LatLng(dd.lat, dd.lng), LatLng(tlat, tlng))), 12, FontWeight.ExtraBold, C.green) } } } }
                    Column(Modifier.clip(RoundedCornerShape(16.dp)).background(C.pillLive).padding(horizontal = 16.dp, vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) { T("18 د", 20, FontWeight.Black, C.greenD); T("وقت الوصول", 10, FontWeight.Normal, C.muted) }
                }
                Spacer(Modifier.height(16.dp))
                // المراحل
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    steps.forEachIndexed { i, st ->
                        val n = i + 1; val done = n < step; val now = n == step
                        Column(Modifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(26.dp).clip(CircleShape).then(if (done) Modifier.background(Grad.green) else Modifier.background(C.card).border(2.dp, if (now) C.green else Color(0xFFE0DBD0), CircleShape)), contentAlignment = Alignment.Center) {
                                if (n <= step) Ic(if (done) R.drawable.ic_check else st.second, 13.dp, if (done) Color.White else C.green)
                            }
                            Spacer(Modifier.height(6.dp)); T(st.first, 10, if (done || now) FontWeight.Bold else FontWeight.Normal, if (done || now) Color(0xFF4B5A51) else Color(0xFFA3ACA2))
                        }
                        if (i < 3) Box(Modifier.weight(1f).padding(top = 12.dp).height(3.dp).clip(CircleShape).then(if (n < step) Modifier.background(C.green) else Modifier.background(Color(0xFFE0DBD0))))
                    }
                }
                Spacer(Modifier.height(20.dp))
                // السائق
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(17.dp)).background(Grad.sand), contentAlignment = Alignment.Center) { T(ord?.driver?.name?.take(2) ?: "؟", 16, FontWeight.ExtraBold, Color(0xFF6B5335)) }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) { T(ord?.driver?.name ?: "بانتظار تعيين السائق", 14, FontWeight.Bold, C.head); Spacer(Modifier.height(2.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Text("★", color = Color(0xFFD9A441), fontSize = 11.sp); Spacer(Modifier.width(4.dp)); T(ord?.driver?.let { String.format("%.1f", it.rating) + " · " + (it.vehicle_type ?: "") } ?: "—", 11, FontWeight.Normal, C.muted, maxLines = 1) } }
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(Color(0xFFF2EFE9)).border(1.dp, C.line, RoundedCornerShape(15.dp)).clickable { onChat() }, contentAlignment = Alignment.Center) { Ic(R.drawable.ic_msg, 17.dp, Color(0xFF5D6B62)) }
                    Spacer(Modifier.width(9.dp))
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(Grad.green).clickable { toast("الاتصال بالسائق") }, contentAlignment = Alignment.Center) { Ic(R.drawable.ic_phone, 17.dp, Color.White) }
                }
                Spacer(Modifier.height(12.dp))
                // معلومات الحمولة
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CargoBox("الحمولة", R.drawable.ic_box, ord?.service_name ?: Sel.svcName, Modifier.weight(1f)); CargoBox("الدفع", R.drawable.ic_card, "بطاقة · ﷼${Sel.payAmount}", Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CargoBox("من", R.drawable.ic_pin, ord?.from ?: "نقطة الاستلام", Modifier.weight(1f)); CargoBox("إلى", R.drawable.ic_flag, ord?.to ?: "نقطة التسليم", Modifier.weight(1f))
                }
                if (step >= 4) {
                    Spacer(Modifier.height(14.dp))
                    OCard(Modifier.fillMaxWidth()) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { T("وصلت شحنتك 🎉 — قيّم تجربتك", 14, FontWeight.Bold, C.head) }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            (1..5).forEach { n -> Text("★", fontSize = 30.sp, color = if (rated >= n) Color(0xFFD9A441) else Color(0xFFDDD6C9), modifier = Modifier.padding(horizontal = 4.dp).clickable { rated = n }) }
                        }
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { T(if (rated > 0) "شكراً لاستخدامك تطبيق مطنوخ 💚 نتمنى أن تكون تجربتك رائعة!" else "من 1 إلى 5 نجوم", 11, FontWeight.Normal, C.muted) }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CargoBox(label: String, icon: Int, value: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 12.dp)) {
        T(label, 10, FontWeight.Normal, C.muted); Spacer(Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { Ic(icon, 15.dp, C.blue); Spacer(Modifier.width(7.dp)); T(value, 12, FontWeight.Bold, C.head, maxLines = 1) }
    }
}

@Composable
fun TransportBidsScreen(onBack: () -> Unit, onMenu: () -> Unit, onTrack: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val id = Sel.transportId
    var order by remember { mutableStateOf<com.matnokh.customer.net.TOrder?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0) }
    val startedAt = remember { System.currentTimeMillis() }
    LaunchedEffect(tick) {
        runCatching { order = com.matnokh.customer.net.Net.api.transportOrders().orders.firstOrNull { it.id == id } }
        loaded = true
        kotlinx.coroutines.delay(4000); tick++
    }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("عروض السائقين", onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 8.dp)) {
            val o = order
            when {
                !loaded -> Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { androidx.compose.material3.CircularProgressIndicator(color = C.green) }
                o == null -> CenterHint("لم يُعثر على الطلب — تحقّق من «العروض الجارية»")
                o.status != "broadcasting" -> {
                    CenterHint("تم إسناد طلبك لسائق ✓ — جارٍ التنفيذ")
                    Row(Modifier.padding(22.dp).fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Grad.green).clickable(onClick = onTrack).padding(vertical = 15.dp), horizontalArrangement = Arrangement.Center) { T("تتبّع الطلب", 14, FontWeight.ExtraBold, Color.White) }
                }
                o.bids.isEmpty() && System.currentTimeMillis() - startedAt > 180_000L -> {
                    CenterHint("عذراً، لا يوجد مناديب متاحون الآن.\nيمكنك المحاولة بعد قليل.")
                    Row(Modifier.padding(22.dp).fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Grad.green).clickable(onClick = onBack).padding(vertical = 15.dp), horizontalArrangement = Arrangement.Center) { T("العودة", 14, FontWeight.ExtraBold, Color.White) }
                }
                o.bids.isEmpty() -> CenterHint("بانتظار عروض السائقين القريبين…\nستظهر العروض هنا فور وصولها.")
                else -> {
                    T("اختر عرضاً لبدء التنفيذ", 12, FontWeight.Medium, C.muted, Modifier.padding(start = 22.dp, bottom = 8.dp))
                    o.bids.forEach { b ->
                        Row(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(20.dp)).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(46.dp).clip(CircleShape).background(Grad.sand), contentAlignment = Alignment.Center) { T(b.driver.name.take(2), 15, FontWeight.ExtraBold, Color(0xFF6B5335)) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) { T(b.driver.name, 13, FontWeight.Bold, C.head); Spacer(Modifier.height(2.dp)); T("★ " + String.format("%.1f", b.driver.rating), 11, FontWeight.Normal, C.muted) }
                            T("﷼" + money(b.amount), 16, FontWeight.Black, C.greenD); Spacer(Modifier.width(10.dp))
                            Box(Modifier.clip(RoundedCornerShape(13.dp)).background(Grad.green).clickable { scope.launch { val r = call({ com.matnokh.customer.net.Net.api.pickTransport(o.id, com.matnokh.customer.net.PickBidBody(b.id)) }, toast); if (r != null) { toast(r.message ?: "تم الاختيار ✓"); onTrack() } } }.padding(horizontal = 15.dp, vertical = 9.dp)) { T("قبول", 12, FontWeight.ExtraBold, Color.White) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
