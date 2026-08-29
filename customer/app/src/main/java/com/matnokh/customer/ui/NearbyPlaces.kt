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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.matnokh.customer.R
import com.matnokh.customer.net.Net
import com.matnokh.customer.net.PLACE_CATS
import com.matnokh.customer.net.Repo
import com.matnokh.customer.net.TransportBody
import com.matnokh.customer.net.UiPlace
import com.matnokh.customer.net.call
import kotlinx.coroutines.launch

private const val RIYADH_LAT = 24.7136
private const val RIYADH_LNG = 46.6753

/* ── متاجر/خدمات قريبة من خرائط جوجل ── */
@Composable
fun NearbyScreen(onBack: () -> Unit, onCart: () -> Unit, onMenu: () -> Unit, onExpand: () -> Unit, onPlace: (UiPlace) -> Unit) {
    val ctx = LocalContext.current
    var catIdx by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var here by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
    LaunchedEffect(granted, catIdx, RefreshBus.tick) {
        loading = true
        if (here == null && granted) here = currentLatLng(ctx)
        val loc = here ?: (RIYADH_LAT to RIYADH_LNG)
        runCatching { Repo.loadPlaces(loc.first, loc.second, PLACE_CATS[catIdx].gtype) }
        loading = false
    }
    val center = LatLng(here?.first ?: RIYADH_LAT, here?.second ?: RIYADH_LNG)
    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(center, 13f) }
    LaunchedEffect(here) { here?.let { camera.position = CameraPosition.fromLatLngZoom(LatLng(it.first, it.second), 14f) } }

    val shown = if (query.isBlank()) Repo.places else Repo.places.filter { it.name.contains(query.trim(), true) || it.address.contains(query.trim(), true) }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        CustBackHeader(tr("متاجر قريبة", "Nearby stores"), onBack, onCart, onMenu)
        Box(Modifier.fillMaxWidth().height(300.dp)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(), cameraPositionState = camera,
                properties = MapProperties(isMyLocationEnabled = granted),
                uiSettings = MapUiSettings(myLocationButtonEnabled = granted, zoomControlsEnabled = false, mapToolbarEnabled = false),
            ) {
                OsmTiles()
                shown.forEach { p ->
                    Marker(state = MarkerState(LatLng(p.lat, p.lng)), title = p.name, snippet = p.address,
                        icon = BitmapDescriptorFactory.defaultMarker(PLACE_CATS[catIdx].hue), onClick = { onPlace(p); true })
                }
            }
            Box(Modifier.align(Alignment.TopEnd).padding(12.dp).clip(RoundedCornerShape(50.dp)).background(Color(0xF2FFFFFF)).border(1.dp, C.line, RoundedCornerShape(50.dp)).clickable(onClick = onExpand).padding(horizontal = 12.dp, vertical = 8.dp)) { T(tr("🗺️ ملء الشاشة", "🗺️ Fullscreen"), 11, FontWeight.ExtraBold, C.greenD) }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PLACE_CATS.forEachIndexed { i, c ->
                val on = catIdx == i
                Box(Modifier.clip(CircleShape).then(if (on) Modifier.background(Grad.green) else Modifier.background(C.card)).border(1.dp, if (on) Color.Transparent else C.line, CircleShape).clickable { catIdx = i }.padding(horizontal = 14.dp, vertical = 9.dp)) { T("${c.emoji} ${c.label}", 11, FontWeight.ExtraBold, if (on) Color.White else Color(0xFF4B5A51)) }
            }
        }
        FinField(query, { query = it }, placeholder = tr("ابحث باسم المكان أو العنوان…", "Search by place or address…"), modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 6.dp))
        Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            T(tr("${PLACE_CATS[catIdx].label} قريبة منك", "${PLACE_CATS[catIdx].label} near you"), 15, FontWeight.ExtraBold, C.head, Modifier.weight(1f))
            StorePill("${shown.size}", C.pillLive, C.greenD)
        }
        if (loading) Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
        else LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(shown) { p -> PlaceRow(p, PLACE_CATS[catIdx].emoji) { onPlace(p) } }
            if (shown.isEmpty()) item { CenterHint(if (query.isNotBlank()) tr("لا نتائج مطابقة للبحث", "No results match your search") else tr("لا توجد أماكن قريبة في هذا النوع", "No nearby places of this type")) }
        }
    }
}

@Composable
private fun PlaceRow(p: UiPlace, emoji: String, onClick: () -> Unit) {
    Row(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 11.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).clickable(onClick = onClick).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(C.card2), contentAlignment = Alignment.Center) { androidx.compose.material3.Text(emoji, fontSize = 22.sp) }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            T(p.name, 13, FontWeight.Bold, C.head, maxLines = 1)
            Spacer(Modifier.height(3.dp)); T(p.address.ifBlank { tr("مكان قريب", "Nearby place") }, 10, FontWeight.Normal, C.muted, maxLines = 2, lineHeight = 15)
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.clip(CircleShape).background(C.pillLive).padding(horizontal = 10.dp, vertical = 5.dp)) { T(tr("اطلب عبر مندوب", "Order via courier"), 9, FontWeight.ExtraBold, C.greenD) }
    }
}

/* ── طلب من مكان عبر مندوب (يُبثّ للمناديب) ── */
@Composable
fun PlaceErrandScreen(place: UiPlace, onBack: () -> Unit, onMenu: () -> Unit, onSent: (Int) -> Unit, onDest: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var msg by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("طلب من ${place.name}", "Order from ${place.name}"), onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 6.dp).fillMaxWidth(), PaddingValues(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(C.pillLive), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_pin, 24.dp, C.greenD) }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) { T(place.name, 14, FontWeight.Bold, C.head, maxLines = 1); Spacer(Modifier.height(2.dp)); T(place.address.ifBlank { tr("مكان قريب", "Nearby place") }, 10, FontWeight.Normal, C.muted, maxLines = 2, lineHeight = 15) }
                }
            }
            Spacer(Modifier.height(12.dp))
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                OcTitle(R.drawable.ic_msg, tr("اكتب ما تريد شراءه من هذا المكان", "Write what you want to buy from this place"))
                FinField(msg, { msg = it }, tr("مثال: وجبة برجر · عصير · دواء بنادول…", "e.g. a burger meal · juice · Panadol…"), singleLine = false, minHeight = 110.dp)
                Spacer(Modifier.height(6.dp))
                T(tr("يشتري المندوب طلبك من هذا المكان ويوصّله إليك.", "The courier buys your order from this place and delivers it to you."), 10, FontWeight.Medium, C.muted, lineHeight = 16)
            }
            Spacer(Modifier.height(12.dp))
            DestRow(onDest)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFFEEF4EF)).border(1.dp, Color(0xFFCFE0D4), RoundedCornerShape(18.dp)).padding(horizontal = 15.dp, vertical = 13.dp), verticalAlignment = Alignment.Top) {
                Ic(R.drawable.ic_card, 17.dp, C.greenD, Modifier.padding(top = 1.dp)); Spacer(Modifier.width(9.dp))
                T(tr("الدفع نقداً للمندوب عند الاستلام — قيمة المشتريات + أجرة التوصيل التي تتفق عليها معه.", "Pay the courier in cash on delivery — items value + the delivery fee you agree on with them."), 11, FontWeight.Medium, C.greenD, lineHeight = 19)
            }
            Spacer(Modifier.height(16.dp))
            WideButton(if (sending) tr("جارٍ الإرسال…", "Sending…") else tr("أرسل الطلب لأقرب مندوب", "Send order to nearest courier"), R.drawable.ic_nav, modifier = Modifier.padding(horizontal = 22.dp)) {
                if (sending) return@WideButton
                if (msg.isBlank()) { toast(tr("اكتب ما تريد شراءه أولاً", "Write what you want to buy first")); return@WideButton }
                sending = true
                scope.launch {
                    val body = TransportBody("errand", tr("طلب من ${place.name}", "Order from ${place.name}"), place.address.ifBlank { place.name }, Sel.destAddr ?: Sel.destLabel, place.lat, place.lng, msg.trim(), "bid", 20.0, "cash", Sel.destLat, Sel.destLng)
                    val r = call({ Net.api.createTransport(body) }, toast)
                    sending = false
                    if (r?.order_id != null) { toast(r.message ?: tr("أُرسل طلبك للمناديب القريبين ✓", "Your request was sent to nearby couriers ✓")); onSent(r.order_id!!) }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ── الخريطة الكاملة (أماكن جوجل + فلتر) ── */
@Composable
fun PlacesMapFull(onBack: () -> Unit, onPlace: (UiPlace) -> Unit) {
    val ctx = LocalContext.current
    var catIdx by remember { mutableStateOf(0) }
    var here by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
    LaunchedEffect(granted, catIdx, RefreshBus.tick) {
        if (here == null && granted) here = currentLatLng(ctx)
        val loc = here ?: (RIYADH_LAT to RIYADH_LNG)
        runCatching { Repo.loadPlaces(loc.first, loc.second, PLACE_CATS[catIdx].gtype) }
    }
    val center = LatLng(here?.first ?: RIYADH_LAT, here?.second ?: RIYADH_LNG)
    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(center, 14f) }
    LaunchedEffect(here) { here?.let { camera.position = CameraPosition.fromLatLngZoom(LatLng(it.first, it.second), 15f) } }
    Box(Modifier.fillMaxSize().background(C.bg)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(), cameraPositionState = camera,
            properties = MapProperties(isMyLocationEnabled = granted),
            uiSettings = MapUiSettings(myLocationButtonEnabled = granted, zoomControlsEnabled = true, mapToolbarEnabled = true, compassEnabled = true),
        ) {
            OsmTiles()
            Repo.places.forEach { p -> Marker(state = MarkerState(LatLng(p.lat, p.lng)), title = p.name, snippet = p.address, icon = BitmapDescriptorFactory.defaultMarker(PLACE_CATS[catIdx].hue), onClick = { onPlace(p); true }) }
        }
        Box(Modifier.align(Alignment.TopStart).statusBarsPadding().padding(14.dp).size(46.dp).clip(RoundedCornerShape(16.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(16.dp)).clickable(onClick = onBack), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_back, 20.dp, C.head) }
        Box(Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(14.dp).clip(RoundedCornerShape(50.dp)).background(Color(0xF2FFFFFF)).border(1.dp, C.line, RoundedCornerShape(50.dp)).padding(horizontal = 16.dp, vertical = 10.dp)) { T(tr("${Repo.places.size} ${PLACE_CATS[catIdx].label} قريبة", "${Repo.places.size} ${PLACE_CATS[catIdx].label} nearby"), 13, FontWeight.ExtraBold, C.head) }
        Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().horizontalScroll(rememberScrollState()).padding(14.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PLACE_CATS.forEachIndexed { i, c ->
                val on = catIdx == i
                Box(Modifier.clip(CircleShape).then(if (on) Modifier.background(Grad.green) else Modifier.background(Color(0xF2FFFFFF))).border(1.dp, if (on) Color.Transparent else C.line, CircleShape).clickable { catIdx = i }.padding(horizontal = 14.dp, vertical = 9.dp)) { T("${c.emoji} ${c.label}", 11, FontWeight.ExtraBold, if (on) Color.White else Color(0xFF4B5A51)) }
            }
        }
    }
}
