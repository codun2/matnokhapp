package com.matnokh.tajer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.tajer.R
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.OrderDetailResp
import com.matnokh.tajer.net.call
import kotlinx.coroutines.launch

/** بطاقة «طلب جديد» تظهر داخل التطبيق: تفاصيل الطلب + قبول/رفض في نفس المكان. */
@Composable
fun IncomingOrderCard(
    orderId: Int?,
    fallbackTitle: String,
    fallbackBody: String,
    onClose: () -> Unit,
    onOpen: () -> Unit,
    toast: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var d by remember(orderId) { mutableStateOf<OrderDetailResp?>(null) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) { if (orderId != null) call({ Net.api.orderDetail(orderId) }, toast)?.let { d = it } }

    fun act(block: suspend () -> Unit) { if (busy) return; busy = true; scope.launch { block(); busy = false; onClose() } }

    Box(Modifier.fillMaxSize().background(Color(0x88253A34)).clickable(onClick = onClose), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.statusBarsPadding().padding(14.dp).fillMaxWidth().widthIn(max = 400.dp)
                .clip(RoundedCornerShape(24.dp)).background(C.bg).clickable(enabled = false) {},
        ) {
            // رأس أخضر
            Row(Modifier.fillMaxWidth().background(Grad.green).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = .22f)), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_box, 24.dp, Color.White) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    T("طلب جديد 🛒", 15, FontWeight.Black, Color.White)
                    val o = d?.order
                    T(if (o != null) "#${o.order_no ?: o.id} — ${o.customer}" else fallbackTitle, 12, FontWeight.Bold, Color.White.copy(alpha = .92f), maxLines = 1)
                }
                Box(Modifier.size(30.dp).clip(CircleShape).background(Color.White.copy(alpha = .2f)).clickable(onClick = onClose), contentAlignment = Alignment.Center) { T("×", 16, FontWeight.Black, Color.White) }
            }

            val det = d
            if (orderId != null && det == null) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green, strokeWidth = 2.dp, modifier = Modifier.size(26.dp)) }
            } else if (det != null) {
                val o = det.order
                Column(Modifier.padding(16.dp)) {
                    // معلومات
                    InfoChip(R.drawable.ic_pin, o.branch ?: "—", R.drawable.ic_cash, payLabelPublic(o.payment_method))
                    Spacer(Modifier.height(10.dp))
                    // أول عناصر الطلب
                    det.items.take(3).forEach { it2 ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(26.dp).clip(RoundedCornerShape(8.dp)).background(C.pillLive), contentAlignment = Alignment.Center) { T("${it2.qty}×", 10, FontWeight.Black, C.greenD) }
                            Spacer(Modifier.width(9.dp))
                            T(it2.name, 12, FontWeight.Bold, C.head, Modifier.weight(1f), maxLines = 1)
                            T("﷼${it2.line_total.toInt()}", 12, FontWeight.Black, C.greenD)
                        }
                    }
                    if (det.items.size > 3) T("+ ${det.items.size - 3} أصناف أخرى", 10, FontWeight.Medium, C.muted)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) { T("الإجمالي (${det.items.size} أصناف)", 13, FontWeight.Bold, C.head, Modifier.weight(1f)); T("﷼${o.total.toInt()}", 15, FontWeight.Black, C.greenD) }

                    Spacer(Modifier.height(14.dp))
                    // الأزرار
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CardBtn("قبول وتجهيز", R.drawable.ic_check, true, Modifier.weight(1f)) { act { call({ Net.api.acceptOrder(o.id) }, toast)?.let { toast(it.message ?: "قُبل الطلب") } } }
                        CardBtn("رفض", R.drawable.ic_x, false, Modifier.weight(1f)) { act { call({ Net.api.rejectOrder(o.id) }, toast)?.let { toast(it.message ?: "رُفض الطلب") } } }
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().clickable { onClose(); onOpen() }.padding(8.dp), contentAlignment = Alignment.Center) { T("عرض في الطلبات", 12, FontWeight.ExtraBold, C.greenD) }
                }
            } else {
                // إشعار عام بلا طلب
                Column(Modifier.padding(16.dp)) {
                    T(fallbackBody, 12, FontWeight.Medium, C.text, lineHeight = 19)
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Grad.green).clickable(onClick = onClose).padding(12.dp), contentAlignment = Alignment.Center) { T("حسناً", 13, FontWeight.ExtraBold, Color.White) }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(i1: Int, t1: String, i2: Int, t2: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Ic(i1, 14.dp, C.green); Spacer(Modifier.width(5.dp)); T(t1, 11, FontWeight.Bold, C.head, maxLines = 1)
        Spacer(Modifier.width(14.dp))
        Ic(i2, 14.dp, C.green); Spacer(Modifier.width(5.dp)); T(t2, 11, FontWeight.Bold, C.head)
    }
}

@Composable
private fun CardBtn(label: String, iconId: Int, ok: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bg = if (ok) Grad.green else null
    Row(
        modifier.clip(RoundedCornerShape(14.dp)).then(if (ok) Modifier.background(Grad.green) else Modifier.background(C.redBg))
            .clickable(onClick = onClick).padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
    ) {
        Ic(iconId, 15.dp, if (ok) Color.White else C.redText); Spacer(Modifier.width(6.dp))
        T(label, 12, FontWeight.ExtraBold, if (ok) Color.White else C.redText)
    }
}

// نسخة عامة من payLabel (الأصلية private داخل OrdersScreen)
fun payLabelPublic(m: String?): String = when (m) { "card", "tap" -> "بطاقة"; "cash" -> "نقداً"; else -> m ?: "—" }
