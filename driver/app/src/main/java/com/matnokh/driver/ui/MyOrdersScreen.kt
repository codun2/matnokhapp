package com.matnokh.driver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.driver.R

@Composable
fun MyOrdersScreen(onBack: () -> Unit, onMenu: () -> Unit, onOpenActive: () -> Unit, toast: (String) -> Unit) {
    var tab by remember { mutableStateOf("now") }
    val stepNames = listOf("—", tr("تم الإسناد", "Assigned"), tr("تم التحميل", "Loaded"), tr("في الطريق", "On the way"), tr("بانتظار التأكيد", "Awaiting confirmation"))
    LaunchedEffect(Unit) { repoNow(toast); repoPast(toast) }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("طلباتي", "My orders"), onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = 22.dp).padding(top = 14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Kpi("${338 + Drv.pastDone.size}", tr("رحلة مكتملة", "Completed trip"), C.greenD, Modifier.weight(1f))
                Kpi("${Drv.nowOrders.size}", tr("قيد التنفيذ", "In progress"), C.blueText, Modifier.weight(1f))
                Kpi("﷼${Drv.pastDone.sumOf { it.fare }}", tr("أرباح الطلبات", "Order earnings"), C.terraText, Modifier.weight(1f))
            }
            // تبويبات
            Row(Modifier.padding(horizontal = 22.dp).padding(top = 14.dp).fillMaxWidth().clip(RoundedCornerShape(50.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(50.dp)).padding(5.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SegBtn(tr("الحالية", "Current"), tab == "now", Modifier.weight(1f)) { tab = "now" }
                SegBtn(tr("السابقة", "Past"), tab == "past", Modifier.weight(1f)) { tab = "past" }
            }
            Spacer(Modifier.height(14.dp))
            if (tab == "now") {
                if (Drv.nowOrders.isEmpty()) NoteRow(tr("لا يوجد طلب قيد التنفيذ الآن", "No order in progress now"))
                else Drv.nowOrders.forEach { j ->
                    OrderRow(j.iconId, j.gradient, trData(j.svc), "#${j.id} · ${j.cust}", trData(j.from), trData(j.to), Drv.fare.value, stepNames.getOrElse(Drv.activeStep.value) { tr("قيد التنفيذ", "In progress") }, PillKind.Live, rating = null, onClick = onOpenActive)
                }
            } else {
                if (Drv.pastDone.isEmpty()) NoteRow(tr("لا توجد طلبات سابقة", "No past orders"))
                else Drv.pastDone.forEach { o ->
                    OrderRow(o.iconId, o.gradient, trData(o.svc), "#${o.id} · ${o.cust} · ${o.dt}", trData(o.from), trData(o.to), o.fare, tr("مكتمل", "Completed"), PillKind.Ok, rating = o.rating, onClick = null)
                }
            }
            Spacer(Modifier.height(110.dp))
        }
    }
}

@Composable
private fun SegBtn(label: String, on: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(50.dp)).background(if (on) C.green else Color.Transparent).clickable(onClick = onClick).padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
        T(label, 12, FontWeight.ExtraBold, if (on) Color.White else C.muted)
    }
}

@Composable
private fun OrderRow(iconId: Int, gradient: Int, title: String, sub: String, from: String, to: String, fare: Int, status: String, kind: PillKind, rating: String?, onClick: (() -> Unit)?) {
    Column(
        Modifier.padding(horizontal = 22.dp).padding(bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GradBadge(iconId, jobGradients[gradient])
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) { T(title, 13, FontWeight.Bold, C.head, maxLines = 1); T(sub, 10, FontWeight.Normal, C.muted, maxLines = 1) }
            StatusPill(status, kind)
        }
        Spacer(Modifier.height(11.dp)); RouteBox(from, to); Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { if (rating != null) T(tr("تقييم الزبون: ★ $rating", "Customer rating: ★ $rating"), 11, FontWeight.Bold, C.terraText) else T(tr("أجرتك", "Your fare"), 11, FontWeight.Bold, C.muted) }
            T("﷼$fare", 15, FontWeight.Black, C.greenD)
        }
    }
}

@Composable
private fun NoteRow(text: String) {
    Box(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(30.dp), contentAlignment = Alignment.Center) { T(text, 12, FontWeight.Medium, C.muted) }
}
