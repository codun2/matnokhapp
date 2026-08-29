package com.matnokh.driver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.driver.R
import com.matnokh.driver.net.EarnResp
import com.matnokh.driver.net.Net

@Composable
fun EarnScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    var data by remember { mutableStateOf<EarnResp?>(null) }
    LaunchedEffect(Unit) { data = runCatching { Net.api.earnings() }.getOrDefault(EarnResp()) }
    val d = data ?: EarnResp()
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("الأرباح والمحفظة", "Earnings & wallet"), onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // بطاقة الأرباح — حقيقية
            Column(Modifier.padding(horizontal = 22.dp).padding(top = 4.dp).fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Grad.green).padding(22.dp)) {
                T(tr("أرباح آخر 7 أيام", "Last 7 days' earnings"), 12, FontWeight.Normal, Color.White.copy(alpha = .85f))
                Spacer(Modifier.height(4.dp)); T("﷼${d.week.toInt()}", 34, FontWeight.Black, Color.White)
                Spacer(Modifier.height(10.dp))
                Box(Modifier.clip(RoundedCornerShape(50.dp)).background(Color.White.copy(alpha = .22f)).padding(horizontal = 14.dp, vertical = 5.dp)) { T(tr("${d.trips_week} رحلة · الرصيد ﷼${d.balance.toInt()}", "${d.trips_week} trips · balance ﷼${d.balance.toInt()}"), 11, FontWeight.Bold, Color.White) }
            }
            Spacer(Modifier.height(14.dp))
            // مخطط 7 أيام — حقيقي
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                OcTitle(R.drawable.ic_chart, tr("أرباح آخر 7 أيام", "Last 7 days' earnings"))
                val days = d.days
                val maxV = (days.maxOfOrNull { it.amount } ?: 0.0).coerceAtLeast(1.0)
                Row(Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                    days.forEachIndexed { i, day ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                            T("${day.amount.toInt()}", 9, FontWeight.ExtraBold, C.greenD); Spacer(Modifier.height(3.dp))
                            Box(Modifier.fillMaxWidth().fillMaxHeight((day.amount / maxV).toFloat().coerceIn(0.03f, 1f)).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(if (i == days.lastIndex) Grad.green else Brush.linearGradient(listOf(Color(0xFFDFE9E2), Color(0xFFDFE9E2)))))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    days.forEach { Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { T(it.label, 9, FontWeight.Bold, C.muted, maxLines = 1) } }
                }
            }
            Spacer(Modifier.height(14.dp))
            Column(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFFFDF6EC)).border(1.dp, Color(0xFFECDCC3), RoundedCornerShape(18.dp)).padding(14.dp)) {
                T(tr("⚠ المحفظة داخل التطبيق [قيد التطوير]", "⚠ In-app wallet [under development]"), 12, FontWeight.ExtraBold, Color(0xFF75552E))
                Spacer(Modifier.height(4.dp)); T(tr("حالياً تُدفع أرباحك خارج النظام (تحويل مباشر). تمويل المحفظة والسحب الذاتي ضمن الخطة القادمة مع الباقات.", "Currently your earnings are paid outside the system (direct transfer). In-app wallet funding and self-withdrawal are in the upcoming plan with packages."), 11, FontWeight.Medium, Color(0xFF8A6A3F), lineHeight = 18)
            }
            SecTitle(tr("آخر العمليات", "Latest transactions"))
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(vertical = 4.dp)) {
                val ops = d.operations
                if (data == null) Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { T(tr("جارٍ التحميل…", "Loading…"), 11, FontWeight.Medium, C.muted) }
                else if (ops.isEmpty()) Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { T(tr("لا توجد عمليات بعد", "No transactions yet"), 12, FontWeight.Medium, C.muted) }
                else ops.forEach { op -> Tx(svcIcon(op.key), C.pillLive, C.greenD, trData(op.title), op.dt, "+ ﷼${op.amount.toInt()}", C.greenD) }
            }
            Spacer(Modifier.height(110.dp))
        }
    }
}

@Composable
private fun Tx(iconId: Int, bg: Color, fg: Color, title: String, sub: String, amount: String, amtColor: Color) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(bg), contentAlignment = Alignment.Center) { Ic(iconId, 17.dp, fg) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { T(title, 12, FontWeight.Bold, C.head, maxLines = 1); T(sub, 10, FontWeight.Normal, C.muted, maxLines = 1) }
        Spacer(Modifier.width(8.dp)); T(amount, 13, FontWeight.Black, amtColor, maxLines = 1)
    }
}
