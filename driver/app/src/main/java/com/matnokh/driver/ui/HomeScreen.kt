package com.matnokh.driver.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.driver.R
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onMenu: () -> Unit, onNotifications: () -> Unit, onBid: (Job) -> Unit, onAcceptDirect: (Job) -> Unit, onStoreReject: (Job) -> Unit, toast: (String) -> Unit, onExpandMap: () -> Unit) {
    val scope = rememberCoroutineScope()
    val avail = Drv.available.value
    var headsUp by remember { mutableStateOf<Job?>(null) }
    var lastTop by remember { mutableStateOf<Int?>(null) }
    var shiftMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { repoMe(toast) }
    LaunchedEffect(Unit) { repoShiftToday(toast) }
    // استطلاع دوري: الطلبات القريبة + اللوحة + الطلب النشط
    LaunchedEffect(Unit) {
        while (true) {
            if (Drv.available.value) { Drv.received.clear(); repoNearby(toast); repoStoreOrders(toast) } else Drv.received.clear()
            repoDash(toast); repoNow(toast)
            kotlinx.coroutines.delay(5000)
        }
    }
    // إشعار منبثق عند وصول طلب جديد لأعلى القائمة
    LaunchedEffect(Drv.received.firstOrNull()?.oid) {
        val top = Drv.received.firstOrNull()
        if (top != null && top.oid != lastTop) { lastTop = top.oid; if (lastTop != null) headsUp = top }
    }
    LaunchedEffect(headsUp) { if (headsUp != null) { kotlinx.coroutines.delay(9000); headsUp = null } }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(C.bg).verticalScroll(rememberScrollState())) {
            Row(Modifier.fillMaxWidth().background(C.bg).statusBarsPadding().padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(46.dp).clip(RoundedCornerShape(16.dp)).background(Grad.sand), contentAlignment = Alignment.Center) { T(Drv.avatar.value, 15, FontWeight.ExtraBold, Color(0xFF6B5335)) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { T(tr("أهلاً كابتن 🚚", "Hello captain 🚚"), 11, FontWeight.Normal, C.muted); T(Drv.name.value, 15, FontWeight.Bold, C.head, maxLines = 1) }
                HeaderBtn(R.drawable.ic_menu, onClick = onMenu); Spacer(Modifier.width(9.dp)); HeaderBtn(R.drawable.ic_bell, badge = true, onClick = onNotifications)
            }
            Drv.shiftToday.value?.shift?.let { sh ->
                val st = Drv.shiftToday.value
                if (st?.status == "not_started") {
                    Row(Modifier.padding(horizontal = 22.dp).padding(top = 10.dp)) {
                        Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Grad.green).clickable { scope.launch { repoShiftCheckIn(toast) } }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            T("\u25B6  \u0627\u0628\u062F\u0623 \u0648\u0631\u062F\u064A\u0629 ${sh.name}", 12, FontWeight.ExtraBold, Color.White)
                        }
                    }
                } else if (st != null && (st.status == "present" || st.status == "late") && st.check_out == null) {
                    Column(Modifier.padding(horizontal = 22.dp).padding(top = 10.dp)) {
                        Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFFE1F5EE)).border(1.dp, Color(0xFFBFE6D5), RoundedCornerShape(20.dp)).clickable { shiftMenu = !shiftMenu }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(7.dp).clip(CircleShape).background(C.green))
                                Spacer(Modifier.width(6.dp))
                                T("\u23F0 ${sh.name} \u00B7 \u062C\u0627\u0631\u064A\u0629", 12, FontWeight.ExtraBold, C.greenD)
                            }
                        }
                        if (shiftMenu) {
                            Spacer(Modifier.height(8.dp))
                            Box(Modifier.clip(RoundedCornerShape(14.dp)).background(Grad.terra).clickable { shiftMenu = false; scope.launch { repoShiftCheckOut(toast) } }.padding(horizontal = 16.dp, vertical = 9.dp)) {
                                T("\u23F9  \u0625\u0646\u0647\u0627\u0621 \u0627\u0644\u0648\u0631\u062F\u064A\u0629", 13, FontWeight.ExtraBold, Color.White)
                            }
                        }
                    }
                }
            }
            RadarBox(avail, onExpandMap)
            Row(
                Modifier.padding(horizontal = 22.dp).padding(top = 16.dp).fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(C.card)
                    .border(1.5.dp, if (avail) Color(0xFFCFE0D4) else C.line, RoundedCornerShape(26.dp)).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(50.dp).clip(RoundedCornerShape(17.dp)).background(if (avail) Grad.green else Grad.sand), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_nav, 24.dp, Color.White) }
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    T(if (avail) tr("متاح لاستقبال الطلبات", "Available to receive orders") else tr("غير متاح حالياً", "Currently unavailable"), 14, FontWeight.Bold, C.head)
                    T(if (avail) tr("يجري تحديث موقعك دورياً · ${Drv.vehicle.value}", "Your location is updated periodically · ${Drv.vehicle.value}") else tr("لن تصلك طلبات جديدة", "You won't receive new orders"), 11, FontWeight.Normal, C.muted, maxLines = 1)
                }
                Sw(avail) { scope.launch { repoSetAvailable(!avail, toast); if (Drv.available.value) { Drv.received.clear(); repoNearby(toast); repoStoreOrders(toast) } } }
            }
            Row(Modifier.padding(horizontal = 22.dp).padding(top = 14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Kpi("${Drv.tripsToday.value}", tr("رحلات اليوم", "Today's trips"), C.greenD, Modifier.weight(1f))
                Kpi("﷼${Drv.earningsToday.value}", tr("أرباح اليوم", "Today's earnings"), C.blueText, Modifier.weight(1f))
                Kpi("${Drv.rating.value} ★", tr("تقييمي", "My rating"), C.terraText, Modifier.weight(1f))
            }
            SecTitle(tr("الطلبات الواردة", "Incoming orders")) { if (avail) Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(9.dp).clip(CircleShape).background(C.green)); Spacer(Modifier.width(5.dp)); T(tr("بثّ مباشر", "Live broadcast"), 11, FontWeight.ExtraBold, C.greenD) } }
            if (!avail) CenterNote(tr("أنت غير متاح حالياً — فعّل التوفّر ليصلك بثّ الطلبات القريبة", "You're currently unavailable — turn on availability to receive nearby order broadcasts"))
            else if (Drv.received.isEmpty()) CenterNote(tr("بانتظار وصول أول طلب…", "Waiting for the first order…"))
            else Drv.received.forEach { job -> JobCard(job, onBid = { onBid(job) }, onAccept = { onAcceptDirect(job) }, onReject = { if (job.isStore) onStoreReject(job) else { Drv.hidden.add(job.oid); Drv.received.removeAll { it.oid == job.oid } } }) }
            Spacer(Modifier.height(120.dp))
        }

        AnimatedVisibility(
            visible = headsUp != null && avail,
            enter = fadeIn() + slideInVertically { -it }, exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 8.dp, start = 12.dp, end = 12.dp),
        ) {
            headsUp?.let { job -> HeadsUpCard(job, onGo = { headsUp = null; if (job.bid) onBid(job) else onAcceptDirect(job) }, onIgnore = { if (job.isStore) onStoreReject(job) else Drv.hidden.add(job.oid); headsUp = null }) }
        }
    }
}

@Composable
private fun RadarBox(avail: Boolean, onExpand: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var granted by remember { mutableStateOf(androidx.core.content.ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) }
    val locLauncher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted = it }
    var here by remember { mutableStateOf(Drv.driverLat.value?.let { la -> Drv.driverLng.value?.let { ln -> com.google.android.gms.maps.model.LatLng(la, ln) } }) }
    val camera = com.google.maps.android.compose.rememberCameraPositionState { position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(here ?: com.google.android.gms.maps.model.LatLng(24.7136, 46.6753), 13f) }
    LaunchedEffect(Unit) {
        if (!granted) locLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        currentLatLng(ctx)?.let { here = com.google.android.gms.maps.model.LatLng(it.first, it.second); Drv.driverLat.value = it.first; Drv.driverLng.value = it.second; camera.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(here!!, 15f) }
    }
    Box(Modifier.fillMaxWidth().height(230.dp)) {
        com.google.maps.android.compose.GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = com.google.maps.android.compose.MapProperties(isMyLocationEnabled = granted),
            uiSettings = com.google.maps.android.compose.MapUiSettings(myLocationButtonEnabled = granted, zoomControlsEnabled = false, mapToolbarEnabled = false),
        ) {
            OsmTiles()
            here?.let { com.google.maps.android.compose.Marker(state = com.google.maps.android.compose.MarkerState(it), title = tr("موقعي", "My location")) }
            Drv.received.forEach { j ->
                val la = j.fromLat; val ln = j.fromLng
                if (la != null && ln != null) com.google.maps.android.compose.Marker(state = com.google.maps.android.compose.MarkerState(com.google.android.gms.maps.model.LatLng(la, ln)), title = j.svc, snippet = "\u00B7 " + j.price + tr(" ريال", " SAR"))
            }
        }
        Row(Modifier.align(Alignment.TopStart).padding(14.dp).clip(RoundedCornerShape(15.dp)).background(Color(0xE6FFFFFF)).padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(if (avail) C.green else Color(0xFF9AA198))); Spacer(Modifier.width(7.dp)); T(if (avail) tr("بانتظار الطلبات القريبة…", "Waiting for nearby orders…") else tr("التوفّر متوقّف", "Availability off"), 12, FontWeight.ExtraBold, Color(0xFF4B5A51))
        }
        Box(Modifier.align(Alignment.TopEnd).padding(14.dp).clip(RoundedCornerShape(50.dp)).background(Color(0xF2FFFFFF)).clickable(onClick = onExpand).padding(horizontal = 12.dp, vertical = 8.dp)) { T(tr("🗺️ ملء الشاشة", "🗺️ Fullscreen"), 11, FontWeight.ExtraBold, C.greenD) }
    }
}

@Composable
private fun JobCard(job: Job, onBid: () -> Unit, onAccept: () -> Unit, onReject: () -> Unit) {
    Column(Modifier.padding(horizontal = 22.dp).padding(bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GradBadge(job.iconId, jobGradients[job.gradient])
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) { T(job.svc, 13, FontWeight.Bold, C.head, maxLines = 1); T(tr("#${job.id} · ${job.cust} · ${job.km} كم عنك", "#${job.id} · ${job.cust} · ${job.km} km from you"), 10, FontWeight.Normal, C.muted, maxLines = 1) }
            StatusPill(if (job.bid) tr("مزايدة", "Bidding") else if (job.companyFixed) tr("سعر الشركة", "Company price") else tr("قبول مباشر", "Direct accept"), if (job.bid) PillKind.Wait else PillKind.Live)
        }
        Spacer(Modifier.height(11.dp)); RouteBox(job.from, job.to); Spacer(Modifier.height(11.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { T(tr("${if (job.bid) "سعر مقترح" else "الأجرة"}: ﷼${job.price}", "${if (job.bid) "Suggested price" else "Fare"}: ﷼${job.price}"), 11, FontWeight.Bold, C.head, maxLines = 1); T(job.opts, 10, FontWeight.Normal, C.muted, maxLines = 1) }
            if (job.isStore || job.companyFixed) {
                Box(Modifier.clip(RoundedCornerShape(13.dp)).background(Color(0xFFFAF8F4)).border(1.dp, C.line, RoundedCornerShape(13.dp)).clickable(onClick = onReject).padding(horizontal = 15.dp, vertical = 10.dp), contentAlignment = Alignment.Center) { T(tr("رفض", "Reject"), 12, FontWeight.ExtraBold, C.muted) }
                Spacer(Modifier.width(8.dp))
            }
            Row(
                Modifier.clip(RoundedCornerShape(13.dp)).background(if (job.bid) Grad.green else Grad.terra).clickable(onClick = if (job.bid) onBid else onAccept).padding(horizontal = 15.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) { Ic(if (job.bid) R.drawable.ic_cash else R.drawable.ic_check, 15.dp, Color.White); Spacer(Modifier.width(6.dp)); T(if (job.bid) tr("قدّم عرضك", "Submit your offer") else tr("قبول", "Accept"), 12, FontWeight.ExtraBold, Color.White) }
        }
    }
}

@Composable
private fun HeadsUpCard(job: Job, onGo: () -> Unit, onIgnore: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xF7FFFFFF)).border(1.dp, Color(0xFFCFE0D4), RoundedCornerShape(20.dp)).padding(13.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GradBadge(job.iconId, jobGradients[job.gradient], 40.dp, 14.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) { T(tr("طلب جديد من ${job.cust}", "New order from ${job.cust}"), 13, FontWeight.Bold, C.head, maxLines = 1); T(tr("${job.svc} · ${job.km} كم عنك", "${job.svc} · ${job.km} km from you"), 10, FontWeight.Normal, C.muted, maxLines = 1) }
            Column(Modifier.clip(RoundedCornerShape(13.dp)).background(C.pillLive).padding(horizontal = 11.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) { T("﷼${job.price}", 14, FontWeight.Black, C.greenD); T(tr("السعر المقترح", "Suggested price"), 8, FontWeight.Normal, C.muted) }
        }
        Spacer(Modifier.height(9.dp)); RouteBox(job.from, job.to)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(Grad.green).clickable(onClick = onGo).padding(vertical = 10.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Ic(if (job.bid) R.drawable.ic_cash else R.drawable.ic_check, 14.dp, Color.White); Spacer(Modifier.width(6.dp)); T(if (job.bid) tr("اكتب سعرك", "Enter your price") else tr("قبول المشوار", "Accept the trip"), 12, FontWeight.ExtraBold, Color.White)
            }
            Box(Modifier.width(84.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFFFAF8F4)).border(1.dp, C.line, RoundedCornerShape(13.dp)).clickable(onClick = onIgnore).padding(vertical = 10.dp), contentAlignment = Alignment.Center) { T(tr("تجاهل", "Dismiss"), 12, FontWeight.ExtraBold, C.muted) }
        }
    }
}

@Composable
private fun CenterNote(text: String) {
    Box(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(30.dp), contentAlignment = Alignment.Center) { T(text, 12, FontWeight.Medium, C.muted) }
}
