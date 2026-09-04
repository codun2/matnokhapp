package com.matnokh.customer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.customer.R
import com.matnokh.customer.net.*
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun OrdersScreen(onBack: () -> Unit, onMenu: () -> Unit, onTrack: () -> Unit, toast: (String) -> Unit, activeOnly: Boolean = false, onTransport: (Int, String) -> Unit = { _, _ -> }, onOpenDetails: (Int, Boolean) -> Unit = { _, _ -> }) {
    var bidsOrder by remember { mutableStateOf<OrderRowDto?>(null) }
    LaunchedEffect(Unit) { Sel.deeplinkOrderId?.let { bidsOrder = OrderRowDto(it, null, "", 0.0, "", null); Sel.deeplinkOrderId = null } }
    val bo = bidsOrder
    if (bo != null) { OrderBidsScreen(bo, { bidsOrder = null }, onMenu, toast); return }
    var orders by remember { mutableStateOf<List<OrderRowDto>?>(null) }
    var torders by remember { mutableStateOf<List<TOrder>>(emptyList()) }
    LaunchedEffect(RefreshBus.tick) { if (Session.isLoggedIn()) { val o = call({ Net.api.orders() }, toast)?.orders ?: emptyList(); val t = runCatching { Net.api.transportOrders().orders }.getOrNull() ?: emptyList(); torders = t; orders = o } else { orders = emptyList(); torders = emptyList() } }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(if (activeOnly) tr("العروض الجارية", "Active offers") else tr("طلباتي", "My orders"), onBack, onMenu)
        val list = orders?.let { if (activeOnly) it.filter { o -> o.status in listOf("ready", "accepted", "picked_up", "on_the_way") } else it }
        val tlist = if (activeOnly) torders.filter { t -> t.status in listOf("broadcasting", "assigned", "loaded", "on_the_way") } else torders
        // قائمة كسولة: تركيب البطاقات المرئية فقط (كانت تُركَّب كلها دفعة واحدة = تعليق مع كثرة الطلبات)
        val merged = remember(orders, torders, activeOnly) {
            val l = orders?.let { if (activeOnly) it.filter { o -> o.status in listOf("ready", "accepted", "picked_up", "on_the_way") } else it } ?: emptyList()
            val tl = if (activeOnly) torders.filter { t -> t.status in listOf("broadcasting", "assigned", "loaded", "on_the_way") } else torders
            (l.map { it as Any } + tl.map { it as Any }).sortedByDescending { when (it) { is OrderRowDto -> it.ts; is TOrder -> it.ts; else -> 0L } }
        }
        androidx.compose.foundation.lazy.LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 20.dp)) {
            if (list == null) { item { Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) } } }
            else {
                if (!activeOnly) item {
                    Column {
                        val done = list.count { it.status in listOf("delivered", "done") } + tlist.count { it.status == "delivered" }
                        val active = list.count { it.status in listOf("pending", "accepted", "ready", "picked_up", "on_the_way") } + tlist.count { it.status in listOf("broadcasting", "assigned", "loaded", "on_the_way") }
                        val spend = list.sumOf { it.total } + tlist.sumOf { it.final_fare ?: it.proposed_price }
                        Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Kpi("$done", tr("طلب مكتمل", "Completed order"), C.greenD, Modifier.weight(1f)); Kpi("$active", tr("طلب نشط", "Active order"), C.blueText, Modifier.weight(1f)); Kpi("$RY${money(spend)}", tr("إجمالي الإنفاق", "Total spending"), C.terraText, Modifier.weight(1f))
                        }
                        SecTitle(tr("الأحدث", "Newest"))
                    }
                }
                if (list.isEmpty() && tlist.isEmpty()) item { CenterHint(if (!Session.isLoggedIn()) tr("سجّل الدخول لعرض طلباتك", "Log in to view your orders") else if (activeOnly) tr("لا توجد عروض جارية حالياً", "No active offers currently") else tr("لا توجد طلبات بعد", "No orders yet")) }
                items(merged, key = { x -> when (x) { is OrderRowDto -> "o${x.id}"; is TOrder -> "t${x.id}"; else -> x.hashCode() } }) { item ->
                    if (item is OrderRowDto) {
                        val o = item
                        val (lbl, kind) = orderStatus(o.status)
                        Row(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).clickable { if (activeOnly || o.status in listOf("pending", "accepted", "ready", "picked_up", "on_the_way")) bidsOrder = o else onOpenDetails(o.id, false) }.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(C.pillLive), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_box, 22.dp, C.greenD) }
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) { T(tr("طلب من ${trd(o.store, o.store_en)}", "Order from ${trd(o.store, o.store_en)}"), 13, FontWeight.Bold, C.head, maxLines = 1); Spacer(Modifier.height(2.dp)); T(tr("${o.dt ?: ""} · $RY${money(o.total)} · رقم ${(o.order_no ?: o.id.toString()).substringAfterLast("-")}", "${o.dt ?: ""} · $RY${money(o.total)} · No. ${(o.order_no ?: o.id.toString()).substringAfterLast("-")}"), 11, FontWeight.Normal, C.muted, maxLines = 1) }
                            StatusPill(lbl, kind)
                        }
                    } else if (item is TOrder) {
                        val t = item
                        val (tl, tk) = orderStatus(t.status)
                        Row(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).clickable { if (activeOnly) onTransport(t.id, t.status) else onOpenDetails(t.id, true) }.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(C.pillOk), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_truck, 22.dp, C.blueText) }
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) { T(t.service_name?.let { trd(it, t.service_name_en) } ?: tr("خدمة نقل", "Transport service"), 13, FontWeight.Bold, C.head, maxLines = 1); Spacer(Modifier.height(2.dp)); T(tr("${t.from ?: ""} ← ${t.to ?: "-"} · $RY${money(t.final_fare ?: t.proposed_price)} · رقم ${t.order_no.substringAfterLast("-")}", "${t.from ?: ""} ← ${t.to ?: "-"} · $RY${money(t.final_fare ?: t.proposed_price)} · No. ${t.order_no.substringAfterLast("-")}"), 11, FontWeight.Normal, C.muted, maxLines = 1) }
                            StatusPill(tl, tk)
                        }
                    }
                }
            }
        }
    }
}

fun orderStatus(st: String): Pair<String, PillKind> = when (st) {
    "pending" -> tr("قيد المراجعة", "Under review") to PillKind.Wait
    "awaiting_payment" -> tr("بانتظار تأكيد الدفع", "Awaiting payment confirmation") to PillKind.Wait
    "accepted" -> tr("قيد التجهيز", "Preparing") to PillKind.Live
    "ready" -> tr("جاهز", "Ready") to PillKind.Live
    "broadcasting" -> tr("بانتظار عروض", "Awaiting offers") to PillKind.Wait
    "assigned" -> tr("تم إسناد مندوب", "Courier assigned") to PillKind.Ok
    "loaded", "picked_up" -> tr("تم الاستلام", "Picked up") to PillKind.Live
    "on_the_way" -> tr("في الطريق", "On the way") to PillKind.Live
    "delivered", "done" -> tr("تم التسليم", "Delivered") to PillKind.Ok
    "expired" -> tr("منتهٍ", "Expired") to PillKind.Off
    else -> tr("ملغى", "Canceled") to PillKind.Rj
}

@Composable
private fun Kpi(value: String, label: String, color: Color, modifier: Modifier) {
    OCard(modifier, PaddingValues(vertical = 14.dp, horizontal = 10.dp)) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { T(value, 20, FontWeight.Black, color) }
        Spacer(Modifier.height(2.dp)); Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { T(label, 10, FontWeight.Normal, C.muted) }
    }
}

@Composable
fun ProfileScreen(onBack: () -> Unit, onMenu: () -> Unit, onLogout: () -> Unit, toast: (String) -> Unit, onNav: (String) -> Unit = {}) {
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("حسابي", "My account"), onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(toast)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) { T(Session.name ?: tr("زائر", "Guest"), 16, FontWeight.Bold, C.head); T(if (Session.isLoggedIn()) tr("حساب مطنوخ", "Matnokh account") else tr("تصفّح كزائر", "Browse as guest"), 12, FontWeight.Normal, C.muted) }
                    if (Session.isLoggedIn()) Row(Modifier.clip(CircleShape).background(C.pillLive).padding(horizontal = 11.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) { Ic(R.drawable.ic_shield, 13.dp, C.greenD); Spacer(Modifier.width(4.dp)); T(tr("موثّق", "Verified"), 10, FontWeight.ExtraBold, C.greenD) }
                }
            }
            Spacer(Modifier.height(14.dp))
            if (Session.isLoggedIn()) { RadiusCard(toast); Spacer(Modifier.height(14.dp)) }
            PList {
                PRow(Grad.green, R.drawable.ic_pin, tr("عناويني", "My addresses"), tr("أضف عناوينك", "Add your addresses")) { onNav("addresses") }
                PRow(Grad.blue, R.drawable.ic_card, tr("وسائل الدفع", "Payment methods"), tr("مدى · STC Pay · Apple Pay", "Mada · STC Pay · Apple Pay")) { onNav("paymethods") }
                PRow(Grad.terra, R.drawable.ic_heart, tr("المفضّلة", "Favorites"), tr("متاجرك المفضّلة", "Your favorite stores")) { onNav("favorites") }
                PRow(Grad.sand, R.drawable.ic_bell, tr("الإشعارات", "Notifications"), tr("آخر التنبيهات", "Latest alerts"), last = true) { onNav("notifications") }
            }
            Spacer(Modifier.height(14.dp))
            PList {
                PRow(Grad.blue, R.drawable.ic_msg, tr("مركز المساعدة", "Help center"), null) { onNav("help") }
                Row(Modifier.fillMaxWidth().clickable(onClick = onLogout).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(C.redBg), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_out, 17.dp, C.redText) }
                    Spacer(Modifier.width(12.dp)); T(if (Session.isLoggedIn()) tr("تسجيل الخروج", "Log out") else tr("تسجيل الدخول", "Log in"), 13, FontWeight.Bold, C.redText, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PList(content: @Composable ColumnScope.() -> Unit) { Column(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)), content = content) }

@Composable
private fun PRow(brush: Brush, icon: Int, title: String, sub: String?, last: Boolean = false, onClick: () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(brush), contentAlignment = Alignment.Center) { Ic(icon, 17.dp, Color.White) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { T(title, 13, FontWeight.Bold, C.head); if (sub != null) { Spacer(Modifier.height(1.dp)); T(sub, 10, FontWeight.Medium, C.muted, maxLines = 1) } }
            Ic(R.drawable.ic_back, 17.dp, Color(0xFFC3C9C0))
        }
        if (!last) androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(1.dp)) { drawLine(Color(0xFFF0ECE3), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 1f) }
    }
}

@Composable
private fun RadiusCard(toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var km by remember { mutableStateOf(Session.radius) }
    fun save(v: Float) { Session.radius = v; scope.launch { runCatching { Net.api.updateProfile(ProfileBody(search_radius_km = v.toDouble())) }; toast(if (v <= 0f) tr("نطاق البحث: كل المناديب", "Search range: all couriers") else tr("نطاق البحث: ${v.toInt()} كم", "Search range: ${v.toInt()} km")) } }
    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(Grad.green), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_pin, 17.dp, Color.White) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { T(tr("نطاق البحث عن مندوب", "Courier search range"), 13, FontWeight.Bold, C.head); Spacer(Modifier.height(1.dp)); T(if (km <= 0f) tr("بدون حد \u2014 كل المناديب", "No limit \u2014 all couriers") else tr("ضمن ${km.toInt()} كم من موقعك", "Within ${km.toInt()} km of your location"), 10, FontWeight.Medium, C.muted) }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RStep("\u2212") { if (km > 0f) { km = (km - 1f).coerceAtLeast(0f); save(km) } }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { T(if (km <= 0f) tr("الكل", "All") else tr("${km.toInt()} كم", "${km.toInt()} km"), 19, FontWeight.Black, C.greenD) }
            RStep("+") { if (km < 20f) { km += 1f; save(km) } }
        }
    }
}
@Composable
private fun RStep(ch: String, onClick: () -> Unit) { Box(Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(Color(0xFFFAF8F4)).border(1.dp, C.line, RoundedCornerShape(15.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) { T(ch, 20, FontWeight.Black, C.greenD) } }

@Composable
fun OrderBidsScreen(order: OrderRowDto, onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    var resp by remember { mutableStateOf<OrderBidsResp?>(null) }
    var rated by remember { mutableStateOf(0) }
    LaunchedEffect(rated) { if (rated > 0) runCatching { Net.api.rate(com.matnokh.customer.net.RateBody(order.id, false, rated)) } }
    LaunchedEffect(Unit) { while (true) { runCatching { resp = Net.api.orderBids(order.id) }; kotlinx.coroutines.delay(3000) } }
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("تتبّع الطلب — رقم ", "Track order — No. ") + (order.order_no ?: order.id.toString()).substringAfterLast("-"), onBack, onMenu)
        val r = resp
        when {
            r == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
            r.driver_id == null && r.status != "delivered" -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 8.dp)) {
                Box(Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
                CenterHint(when (r.status) {
                    "pending" -> tr("بانتظار قبول المتجر لطلبك…", "Awaiting the store's acceptance of your order…")
                    "awaiting_payment" -> tr("بانتظار تأكيد المتجر لتحويلك البنكي…", "Awaiting the store's confirmation of your bank transfer…")
                    "accepted" -> tr("يجهّز المتجر طلبك الآن…\nوبعد التجهيز يُسنَد لأقرب مندوب لتوصيله.", "The store is preparing your order now…\nOnce ready it's assigned to the nearest courier to deliver.")
                    else -> tr("جارٍ إسناد طلبك لأقرب مندوب…\nسنُعلمك فور قبوله واستلامه طلبك.", "Assigning your order to the nearest courier…\nWe'll notify you once they accept and pick it up.")
                })
            }
            else -> {
                val step = if (r.step > 0) r.step else 1
                val steps = listOf(tr("تم القبول", "Accepted") to R.drawable.ic_check, tr("استلام الطلب", "Order pickup") to R.drawable.ic_box, tr("في الطريق", "On the way") to R.drawable.ic_van, tr("تم التسليم", "Delivered") to R.drawable.ic_flag)
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    val cam = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(r.pickup?.lat ?: 24.7136, r.pickup?.lng ?: 46.6753), 13f) }
                    var here by remember { mutableStateOf<Pair<Double, Double>?>(null) }
                    LaunchedEffect(Unit) { currentLatLng(ctx)?.let { here = it } }
                    LaunchedEffect(r.driver?.lat, r.driver?.lng) { val d = r.driver; if (d?.lat != null && d.lng != null) cam.position = CameraPosition.fromLatLngZoom(LatLng(d.lat, d.lng), 15f) }
                    Box(Modifier.fillMaxWidth().height(300.dp)) {
                        GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cam, uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false, compassEnabled = false)) {
                            r.pickup?.let { if (it.lat != null && it.lng != null) Marker(state = MarkerState(LatLng(it.lat, it.lng)), title = r.store?.let { trd(it, r.store_en) } ?: tr("المتجر", "Store"), icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)) }
                            r.drop?.let { if (it.lat != null && it.lng != null) Marker(state = MarkerState(LatLng(it.lat, it.lng)), title = tr("التسليم", "Delivery"), icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) }
                            r.driver?.let { d -> if (d.lat != null && d.lng != null) {
                                val dr = r.drop
                                if (dr?.lat != null && dr.lng != null) Polyline(points = listOf(LatLng(d.lat, d.lng), LatLng(dr.lat, dr.lng)), color = C.green, width = 9f)
                                AnimatedCarMarker(LatLng(d.lat, d.lng), d.name ?: tr("المندوب", "Courier"), ctx, if (dr?.lat != null && dr.lng != null) LatLng(dr.lat, dr.lng) else null)
                            } }
                        }
                        Box(Modifier.align(Alignment.TopStart).padding(14.dp).clip(RoundedCornerShape(15.dp)).background(Color.White.copy(alpha = .9f)).padding(horizontal = 15.dp, vertical = 9.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Ic(R.drawable.ic_nav, 15.dp, C.green); Spacer(Modifier.width(7.dp)); T(tr("مباشر", "Live"), 12, FontWeight.ExtraBold, Color(0xFF4B5A51)) } }
                    }
                    Column(Modifier.offset(y = (-26).dp).fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(C.bg).padding(horizontal = 22.dp, vertical = 10.dp)) {
                        Box(Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp, bottom = 16.dp).width(44.dp).height(5.dp).clip(CircleShape).background(Color(0xFFDDD6C9)))
                        T(if (step >= 4) tr("تم توصيل طلبك بنجاح ✓", "Your order was delivered successfully ✓") else tr("مندوبك في الطريق لإتمام طلبك", "Your courier is on the way to complete your order"), 17, FontWeight.Bold, C.head)
                        T(tr("طلب #", "Order #") + (r.order_no ?: "") + " · " + trd(r.store ?: "", r.store_en), 12, FontWeight.Normal, C.muted)
                        run {
                            val dv = r.driver; val dp = r.drop
                            if (step < 4 && dv?.lat != null && dv.lng != null && dp?.lat != null && dp.lng != null) {
                                Spacer(Modifier.height(7.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Ic(R.drawable.ic_clock, 14.dp, C.green); Spacer(Modifier.width(6.dp))
                                    T(etaText(haversineKm(LatLng(dv.lat, dv.lng), LatLng(dp.lat, dp.lng))), 13, FontWeight.ExtraBold, C.green)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
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
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(52.dp).clip(RoundedCornerShape(17.dp)).background(Grad.sand), contentAlignment = Alignment.Center) { T(r.driver?.name?.take(2) ?: tr("؟", "?"), 16, FontWeight.ExtraBold, Color(0xFF6B5335)) }
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) { T(r.driver?.name ?: tr("المندوب", "Courier"), 14, FontWeight.Bold, C.head); Spacer(Modifier.height(2.dp)); T("★ " + String.format("%.1f", r.driver?.rating ?: 5.0) + (r.driver?.vehicle_type?.let { " · " + it } ?: ""), 11, FontWeight.Normal, C.muted, maxLines = 1) }
                            r.driver?.phone?.let { ph -> val dialCtx = androidx.compose.ui.platform.LocalContext.current; Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(Grad.green).clickable { runCatching { dialCtx.startActivity(android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:" + ph)).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) }.onFailure { toast(tr("رقم المندوب: ", "Courier number: ") + ph) } }, contentAlignment = Alignment.Center) { Ic(R.drawable.ic_phone, 17.dp, Color.White) } }
                        }
                        if (step >= 4) {
                            Spacer(Modifier.height(16.dp))
                            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(vertical = 18.dp, horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                T(if (rated > 0) tr("شكراً لاستخدامك تطبيق مطنوخ 💚", "Thanks for using Matnokh 💚") else tr("وصل طلبك 🎉 — قيّم تجربتك مع المندوب", "Your order arrived 🎉 — rate your experience with the courier"), 14, FontWeight.Bold, C.head)
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.Center) { (1..5).forEach { n -> T("★", 30, FontWeight.Bold, if (rated >= n) Color(0xFFD9A441) else Color(0xFFDDD6C9), Modifier.padding(horizontal = 5.dp).clickable { rated = n }) } }
                                Spacer(Modifier.height(8.dp))
                                T(if (rated > 0) tr("نتمنى أن تكون تجربتك رائعة! ✓", "We hope you have a great experience! ✓") else tr("من 1 إلى 5 نجوم", "From 1 to 5 stars"), 11, FontWeight.Normal, C.muted)
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun SettingsScreen(onBack: () -> Unit, onMenu: () -> Unit, onNav: (String) -> Unit, onLogout: () -> Unit, toast: (String) -> Unit) {
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("الإعدادات", "Settings"), onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(12.dp))
            SecTitle(tr("الحساب", "Account"))
            PList {
                PRow(Grad.green, R.drawable.ic_user, tr("الملف الشخصي", "Profile"), tr("بياناتك الشخصية", "Your personal details")) { onNav("profile") }
                PRow(Grad.blue, R.drawable.ic_pin, tr("عناويني", "My addresses"), tr("عناوين التوصيل", "Delivery addresses")) { onNav("addresses") }
                PRow(Grad.terra, R.drawable.ic_card, tr("وسائل الدفع", "Payment methods"), tr("مدى · STC Pay · Apple Pay", "Mada · STC Pay · Apple Pay"), last = true) { onNav("paymethods") }
            }
            Spacer(Modifier.height(14.dp))
            SecTitle(tr("التفضيلات", "Preferences"))
            PList {
                PRow(Grad.sand, R.drawable.ic_bell, tr("الإشعارات", "Notifications"), tr("إدارة التنبيهات", "Manage alerts")) { onNav("notifications") }
                PRow(Grad.blue, R.drawable.ic_globe, tr("اللغة", "Language"), tr("العربية", "Arabic"), last = true) { toast(tr("اللغة الحالية: العربية", "Current language: English")) }
            }
            Spacer(Modifier.height(14.dp))
            SecTitle(tr("عن التطبيق", "About the app"))
            PList {
                PRow(Grad.blue, R.drawable.ic_msg, tr("مركز المساعدة", "Help center"), tr("أسئلة شائعة ودعم", "FAQ & support")) { onNav("help") }
                PRow(Grad.green, R.drawable.ic_info, tr("حول التطبيق", "About the app"), tr("مطنوخ — الإصدار 1.1", "Matnokh — version 1.1")) { toast(tr("مطنوخ — الإصدار 1.1", "Matnokh — version 1.1")) }
                Row(Modifier.fillMaxWidth().clickable(onClick = onLogout).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(C.redBg), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_out, 17.dp, C.redText) }
                    Spacer(Modifier.width(12.dp)); T(if (Session.isLoggedIn()) tr("تسجيل الخروج", "Log out") else tr("تسجيل الدخول", "Log in"), 13, FontWeight.Bold, C.redText, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}


@Composable
fun OrderDetailsScreen(onBack: () -> Unit, onMenu: () -> Unit, onAction: (String, Int) -> Unit, toast: (String) -> Unit) {
    val id = Sel.detailOrderId
    val isT = Sel.detailIsTransport
    var store by remember(id) { mutableStateOf<OrderDetail?>(null) }
    var trans by remember(id) { mutableStateOf<TOrder?>(null) }
    var loading by remember(id) { mutableStateOf(true) }
    LaunchedEffect(id) {
        if (id == null) { loading = false; return@LaunchedEffect }
        if (isT) runCatching { trans = Net.api.transportOrders().orders.firstOrNull { it.id == id } }
        else runCatching { store = Net.api.orderDetail(id) }
        loading = false
    }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("تفاصيل الطلب", "Order details"), onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(10.dp))
            when {
                loading -> Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
                isT && trans != null -> TransportDetail(trans!!, onAction)
                !isT && store != null -> StoreDetail(store!!, onAction)
                else -> CenterHint(tr("تعذّر تحميل تفاصيل الطلب", "Couldn't load order details"))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun vehLabel(v: String?): String = when (v) { "small" -> tr("مركبة صغيرة", "Small vehicle"); "medium" -> tr("مركبة متوسطة", "Medium vehicle"); "large" -> tr("مركبة كبيرة", "Large vehicle"); "motorcycle" -> tr("دراجة نارية", "Motorcycle"); else -> v ?: "" }

@Composable
private fun DetailRow(label: String, value: String, strong: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        T(label, 12, FontWeight.Medium, C.muted, Modifier.weight(1f))
        T(value, if (strong) 15 else 12, if (strong) FontWeight.Black else FontWeight.Bold, if (strong) C.greenD else C.head)
    }
}

@Composable
private fun DriverCard(name: String?, rating: Double, vehicle: String?, phone: String?, onChat: (() -> Unit)? = null) {
    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(CircleShape).background(Grad.sand), contentAlignment = Alignment.Center) { T((name ?: "?").take(2), 15, FontWeight.ExtraBold, Color(0xFF6B5335)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                T(name ?: tr("مندوب", "Courier"), 13, FontWeight.Bold, C.head)
                Spacer(Modifier.height(2.dp))
                T("\u2605 " + String.format("%.1f", rating) + (if (!vehicle.isNullOrBlank()) " \u00b7 " + vehLabel(vehicle) else ""), 11, FontWeight.Normal, C.muted)
                if (!phone.isNullOrBlank()) { val dialCtx = androidx.compose.ui.platform.LocalContext.current; val ph = phone; T(ph, 11, FontWeight.Medium, C.blueText, Modifier.clickable { runCatching { dialCtx.startActivity(android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:" + ph)).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) } }) }
            }
            if (onChat != null) {
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Grad.green).clickable { onChat() }, contentAlignment = Alignment.Center) { Ic(R.drawable.ic_msg, 16.dp, Color.White) }
            }
        }
    }
}

@Composable
private fun StoreDetail(o: OrderDetail, onAction: (String, Int) -> Unit) {
    val (lbl, kind) = orderStatus(o.status)
    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StoreLogo(o.store_logo, 46.dp, 15.dp, null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { T(tr("طلب من ${trd(o.store, o.store_en)}", "Order from ${trd(o.store, o.store_en)}"), 14, FontWeight.Bold, C.head, maxLines = 1); Spacer(Modifier.height(2.dp)); T(tr("رقم ${(o.order_no ?: o.id.toString()).substringAfterLast("-")} \u00b7 ${o.dt ?: ""}", "No. ${(o.order_no ?: o.id.toString()).substringAfterLast("-")} \u00b7 ${o.dt ?: ""}"), 10, FontWeight.Medium, C.muted) }
            StatusPill(lbl, kind)
        }
    }
    Spacer(Modifier.height(12.dp))
    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(12.dp)) {
        Row(Modifier.clickable { onAction("chatM", o.id) }, verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Grad.green), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_msg, 16.dp, Color.White) }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) { T(tr("محادثة المتجر", "Store chat"), 13, FontWeight.Bold, C.head); T(tr("راسل ${trd(o.store, o.store_en)} حول طلبك", "Message ${trd(o.store, o.store_en)} about your order"), 10, FontWeight.Normal, C.muted) }
        }
    }
    Spacer(Modifier.height(12.dp))
    if (o.driver != null) {
        DriverCard(o.driver.name, o.driver.rating, o.driver.vehicle_type, o.driver.phone); Spacer(Modifier.height(12.dp))
        OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(12.dp)) {
            Row(Modifier.clickable { onAction("chatD", o.id) }, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Grad.green), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_msg, 16.dp, Color.White) }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) { T(tr("محادثة المندوب", "Courier chat"), 13, FontWeight.Bold, C.head); T(tr("اعرض محادثتك مع ", "View your chat with ") + (o.driver.name ?: tr("المندوب", "Courier")), 10, FontWeight.Normal, C.muted) }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
    if (o.items.isNotEmpty()) {
        OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(14.dp)) {
            T(tr("المنتجات", "Products"), 12, FontWeight.ExtraBold, C.head); Spacer(Modifier.height(8.dp))
            o.items.forEach { it2 ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { T("${it2.name} \u00d7${it2.qty}", 12, FontWeight.Bold, C.head); if (it2.addons.isNotEmpty()) T(tr("إضافات: ${it2.addons.joinToString("\u060c ")}", "Add-ons: ${it2.addons.joinToString("\u060c ")}"), 10, FontWeight.Normal, C.muted) }
                    T("\uFDFC${money(it2.price * it2.qty)}", 12, FontWeight.Black, C.greenD)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(14.dp)) {
        DetailRow(tr("مجموع المنتجات", "Products total"), "\uFDFC${money(o.items_total)}")
        DetailRow(tr("أجرة التوصيل", "Delivery fee"), if (o.delivery_fee > 0) "\uFDFC${money(o.delivery_fee)}" else tr("تُحدَّد بالعرض", "Set by offer"))
        DetailRow(tr("الإجمالي", "Total"), "\uFDFC${money(o.total)}", strong = true)
        if (!o.payment_method.isNullOrBlank()) DetailRow(tr("طريقة الدفع", "Payment method"), if (o.payment_method == "cash") tr("نقداً", "Cash") else o.payment_method!!)
        if (!o.drop_address.isNullOrBlank()) DetailRow(tr("عنوان التوصيل", "Delivery address"), o.drop_address!!)
    }
    if (o.status == "ready") {
        Spacer(Modifier.height(14.dp))
        Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Grad.green).clickable { onAction("pickStore", o.id) }.padding(vertical = 15.dp), horizontalArrangement = Arrangement.Center) { T(tr("متابعة حالة الطلب", "Track order status"), 14, FontWeight.ExtraBold, Color.White) }
    }
    if (o.status == "pending" || o.status == "accepted" || o.status == "ready") {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Color(0xFFFDEEE9)).clickable { onAction("cancel", o.id) }.padding(vertical = 14.dp), horizontalArrangement = Arrangement.Center) { T(tr("إلغاء الطلب", "Cancel order"), 14, FontWeight.ExtraBold, Color(0xFFC0533B)) }
    }
}

@Composable
private fun TransportDetail(t: TOrder, onAction: (String, Int) -> Unit) {
    val (lbl, kind) = orderStatus(t.status)
    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(C.pillOk), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_truck, 22.dp, C.blueText) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { T(t.service_name?.let { trd(it, t.service_name_en) } ?: tr("خدمة نقل", "Transport service"), 14, FontWeight.Bold, C.head, maxLines = 1); Spacer(Modifier.height(2.dp)); T(tr("رقم ${t.order_no.substringAfterLast("-")}", "No. ${t.order_no.substringAfterLast("-")}"), 10, FontWeight.Medium, C.muted) }
            StatusPill(lbl, kind)
        }
    }
    Spacer(Modifier.height(12.dp))
    if (t.driver != null) {
        DriverCard(t.driver.name, t.driver.rating, t.driver.vehicle_type, t.driver.phone); Spacer(Modifier.height(12.dp))
        OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(12.dp)) {
            Row(Modifier.clickable { onAction("chatD", t.id) }, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Grad.green), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_msg, 16.dp, Color.White) }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) { T(tr("محادثة المندوب", "Courier chat"), 13, FontWeight.Bold, C.head); T(tr("اعرض محادثتك مع ", "View your chat with ") + (t.driver.name ?: tr("المندوب", "Courier")), 10, FontWeight.Normal, C.muted) }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(14.dp)) {
        if (!t.from.isNullOrBlank()) DetailRow(tr("من", "From"), t.from!!)
        if (!t.to.isNullOrBlank()) DetailRow(tr("إلى", "To"), t.to!!)
        DetailRow(tr("السعر", "Price"), "\uFDFC${money(t.final_fare ?: t.proposed_price)}", strong = true)
    }
    if (t.status == "broadcasting" || t.status == "assigned") {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Color(0xFFFDEEE9)).clickable { onAction("cancelT", t.id) }.padding(vertical = 14.dp), horizontalArrangement = Arrangement.Center) { T(tr("إلغاء الطلب", "Cancel order"), 14, FontWeight.ExtraBold, Color(0xFFC0533B)) }
    }
    when (t.status) {
        "broadcasting" -> { Spacer(Modifier.height(14.dp)); Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Grad.green).clickable { onAction("bidsT", t.id) }.padding(vertical = 15.dp), horizontalArrangement = Arrangement.Center) { T(tr("عرض العروض واختيار مندوب", "View offers & choose a courier"), 14, FontWeight.ExtraBold, Color.White) } }
        "assigned", "loaded", "on_the_way" -> { Spacer(Modifier.height(14.dp)); Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Grad.green).clickable { onAction("trackT", t.id) }.padding(vertical = 15.dp), horizontalArrangement = Arrangement.Center) { T(tr("تتبّع الطلب على الخريطة", "Track order on map"), 14, FontWeight.ExtraBold, Color.White) } }
        "expired" -> { Spacer(Modifier.height(14.dp)); Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Grad.green).clickable { onAction("rebroadcastT", t.id) }.padding(vertical = 15.dp), horizontalArrangement = Arrangement.Center) { Ic(R.drawable.ic_nav, 17.dp, Color.White); Spacer(Modifier.width(8.dp)); T(tr("أعد بثّ الطلب", "Rebroadcast order"), 14, FontWeight.ExtraBold, Color.White) } }
    }
}
