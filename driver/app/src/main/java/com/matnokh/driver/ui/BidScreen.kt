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
import androidx.compose.ui.unit.sp
import com.matnokh.driver.R

@Composable
fun BidScreen(job: Job, onBack: () -> Unit, onMenu: () -> Unit, onSend: (Int) -> Unit) {
    var bid by remember(job.id) { mutableStateOf(job.price) }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("تقديم عرض سعر", "Submit a price offer"), onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                OcTitle(job.iconId, "${job.svc} — #${job.id}")
                RouteBox(job.from, tr("${job.to} (${job.km} كم)", "${job.to} (${job.km} km)"))
                job.note?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFEEF4EF)).border(1.dp, C.line, RoundedCornerShape(14.dp)).padding(12.dp)) {
                        Ic(R.drawable.ic_msg, 16.dp, C.green); Spacer(Modifier.width(9.dp))
                        Column { T(tr("طلب الزبون / ملاحظات", "Customer request / notes"), 10, FontWeight.ExtraBold, C.green); Spacer(Modifier.height(3.dp)); T(it, 12, FontWeight.Medium, C.head, lineHeight = 19) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CargoCell(tr("الزبون", "Customer"), R.drawable.ic_user, job.cust, Modifier.weight(1f))
                    CargoCell(tr("الدفع", "Payment"), R.drawable.ic_card, tr("بطاقة", "Card"), Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(12.dp))
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                OcTitle(R.drawable.ic_cash, tr("عرضك للأجرة الكاملة ($RY)", "Your full-fare offer ($RY)"))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    BStep("−") { bid = maxOf(20, bid - 5) }
                    Box(Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(17.dp)).background(Color.White).border(1.5.dp, Color(0xFFCFE0D4), RoundedCornerShape(17.dp)), contentAlignment = Alignment.Center) { T("$RY$bid", 24, FontWeight.Black, C.greenD) }
                    BStep("+") { bid += 5 }
                }
                Spacer(Modifier.height(10.dp))
                T(tr("السعر المقترح من المنصّة: $RY${job.price} (سعر الكيلو × المسافة + الإضافات). عرضك يصل الزبون فوراً — وقد يزايد سائقون آخرون.", "Platform-suggested price: $RY${job.price} (per-km price × distance + extras). Your offer reaches the customer instantly — other drivers may bid too."), 10, FontWeight.Medium, C.muted, lineHeight = 17)
            }
            Spacer(Modifier.height(16.dp))
            WideButton(tr("إرسال العرض للزبون", "Send offer to customer"), R.drawable.ic_check, modifier = Modifier.padding(horizontal = 22.dp)) { onSend(bid) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CargoCell(label: String, iconId: Int, value: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFFFAF8F4)).border(1.dp, C.line, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 11.dp)) {
        T(label, 10, FontWeight.Normal, C.muted); Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { Ic(iconId, 15.dp, C.blue); Spacer(Modifier.width(7.dp)); T(value, 12, FontWeight.Bold, C.head, maxLines = 1) }
    }
}

@Composable
private fun BStep(label: String, onClick: () -> Unit) {
    Box(Modifier.size(52.dp).clip(RoundedCornerShape(17.dp)).background(Color(0xFFFAF8F4)).border(1.dp, C.line, RoundedCornerShape(17.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        androidx.compose.material3.Text(label, fontFamily = Cairo, fontSize = 24.sp, fontWeight = FontWeight.Black, color = C.greenD)
    }
}
