package com.matnokh.tajer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matnokh.tajer.R
import com.matnokh.tajer.net.DashOrder
import com.matnokh.tajer.net.DashResp
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.call

@Composable
fun DashScreen(onMenu: () -> Unit, onOpenOrders: () -> Unit, onNotifications: () -> Unit, toast: (String) -> Unit) {
    var dash by remember { mutableStateOf<DashResp?>(null) }
    LaunchedEffect(Unit) { call({ Net.api.dashboard() }, toast)?.let { dash = it } }
    val d = dash

    Column(Modifier.fillMaxSize().background(C.bg)) {
        // ── الترويسة المثبّتة ──
        Column(
            Modifier.fillMaxWidth().background(C.bg.copy(alpha = .96f)).statusBarsPadding()
                .padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StoreLogoBox(46.dp, 16.dp, 19)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    T("صباح الخير 👋", 11, FontWeight.Normal, C.muted)
                    T(com.matnokh.tajer.net.Session.storeName ?: "متجري", 15, FontWeight.Bold, C.text)
                }
                HeaderBtn(R.drawable.ic_menu, onClick = onMenu)
                Spacer(Modifier.width(9.dp))
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(C.card)
                        .border(1.dp, C.line, RoundedCornerShape(15.dp)).clickable(onClick = onNotifications),
                    contentAlignment = Alignment.Center,
                ) {
                    Ic(R.drawable.ic_bell, 17.dp, Color(0xFF5D6B62))
                    Box(Modifier.align(Alignment.TopEnd).padding(top = 10.dp, end = 11.dp)
                        .size(8.dp).clip(CircleShape).background(C.terra))
                }
            }
        }

        // ── المحتوى القابل للتمرير ──
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // hero — مبيعات هذا الشهر (حقيقي: مجموع الطلبات المسلّمة هذا الشهر)
            Box(
                Modifier.padding(start = 22.dp, end = 22.dp, top = 16.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp)).background(Grad.green).padding(20.dp)
            ) {
                Column {
                    T("مبيعات هذا الشهر", 12, FontWeight.Normal, Color.White.copy(alpha = .85f))
                    Spacer(Modifier.height(4.dp))
                    T("﷼" + money(d?.sales_month ?: 0.0), 32, FontWeight.Black, Color.White)
                    Spacer(Modifier.height(10.dp))
                    val g = d?.growth_pct
                    val trend = when {
                        g == null -> ""
                        g >= 0 -> "▲ $g٪ عن الشهر الماضي · "
                        else -> "▼ ${-g}٪ عن الشهر الماضي · "
                    }
                    Box(Modifier.clip(CircleShape).background(Color.White.copy(alpha = .22f))
                        .padding(horizontal = 14.dp, vertical = 5.dp)) {
                        T(trend + "${d?.orders_month ?: 0} طلباً", 11, FontWeight.Bold, Color.White)
                    }
                }
            }

            // kpis (حقيقي)
            Row(
                Modifier.padding(start = 22.dp, end = 22.dp, top = 14.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Kpi("${d?.branches ?: 0}", "فروع", C.greenD, Modifier.weight(1f))
                Kpi("${d?.products ?: 0}", "منتجاً", C.blueText, Modifier.weight(1f))
                val rt = d?.rating ?: 0.0
                Kpi(if (rt > 0) money(rt) else "—", "تقييم المتجر", C.terraText, Modifier.weight(1f), star = rt > 0)
            }

            SecTitle("مبيعات آخر 7 أيام")

            // مخطط الأعمدة (حقيقي: مبيعات كل يوم من آخر 7 أيام)
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(0.dp)) {
                val week = d?.week ?: emptyList()
                val maxV = (week.maxOfOrNull { it.total } ?: 0.0).takeIf { it > 0 } ?: 1.0
                Row(
                    Modifier.fillMaxWidth().height(110.dp).padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom,
                ) {
                    week.forEachIndexed { i, day ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                            T(money(day.total), 9, FontWeight.ExtraBold, C.greenD)
                            Spacer(Modifier.height(2.dp))
                            Box(
                                Modifier.fillMaxWidth().fillMaxHeight((day.total / maxV).toFloat().coerceIn(0.03f, 1f))
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                    .background(if (i == week.lastIndex) Grad.green else Brush.linearGradient(listOf(Color(0xFFDFE9E2), Color(0xFFDFE9E2))))
                            )
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    week.forEach { day ->
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { T(day.label, 9, FontWeight.Bold, C.muted) }
                    }
                }
            }

            SecTitle("آخر الطلبات الواردة", "عرض الكل", onLink = onOpenOrders)

            val recent = d?.recent ?: emptyList()
            if (d != null && recent.isEmpty()) {
                OCard(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth(), PaddingValues(18.dp)) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { T("لا توجد طلبات بعد", 12, FontWeight.Normal, C.muted) }
                }
            } else recent.forEach { o -> RecentOrdRow(o) }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RecentOrdRow(o: DashOrder) {
    val (lbl, kind) = orderStatus(o.status)
    val done = o.status == "done"
    val shortNo = (o.order_no?.substringAfterLast("-")?.trimStart('0')?.ifEmpty { "0" }) ?: o.id.toString()
    val what = o.items ?: (if (o.items_count == 1) "صنف واحد" else "${o.items_count} أصناف")
    val sub = buildString {
        append("$what · ﷼${money(o.total)} · طلب #$shortNo")
        o.driver?.let { append(" · مندوب: $it") }
    }
    OrdRow(
        if (done) R.drawable.ic_check else R.drawable.ic_box,
        if (done) C.pillOk else C.pillLive,
        if (done) C.blueText else C.greenD,
        o.customer,
        sub, lbl, kind,
    )
}

@Composable
private fun HeaderBtn(iconId: Int, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(C.card)
            .border(1.dp, C.line, RoundedCornerShape(15.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Ic(iconId, 17.dp, Color(0xFF5D6B62)) }
}

@Composable
private fun Kpi(value: String, label: String, color: Color, modifier: Modifier, star: Boolean = false) {
    OCard(modifier, PaddingValues(vertical = 14.dp, horizontal = 10.dp)) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (star) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    T("$value ", 20, FontWeight.Black, color)
                    Text("★", fontSize = 20.sp, color = Color(0xFFD9A441))
                }
            } else T(value, 20, FontWeight.Black, color)
        }
        Spacer(Modifier.height(2.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { T(label, 10, FontWeight.Normal, C.muted) }
    }
}

@Composable
fun SecTitle(title: String, link: String? = null, onLink: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        T(title, 16, FontWeight.ExtraBold, C.head, Modifier.weight(1f))
        if (link != null) T(link, 12, FontWeight.Bold, C.green, Modifier.clickable(onClick = onLink))
    }
}

@Composable
private fun OrdRow(iconId: Int, iconBg: Color, iconColor: Color, title: String, sub: String, pill: String, kind: PillKind) {
    OCard(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth(), PaddingValues(15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(iconBg), contentAlignment = Alignment.Center) {
                Ic(iconId, 22.dp, iconColor)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                T(title, 13, FontWeight.Bold, C.text, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                T(sub, 11, FontWeight.Normal, C.muted, maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            StatusPill(pill, kind)
        }
    }
}
