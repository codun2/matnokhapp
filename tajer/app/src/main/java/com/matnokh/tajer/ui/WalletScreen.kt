package com.matnokh.tajer.ui

import androidx.compose.foundation.background
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
import com.matnokh.tajer.R
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.WalletResp
import com.matnokh.tajer.net.call

@Composable
fun WalletScreen(onBack: () -> Unit, onMenu: () -> Unit, onPayments: () -> Unit, toast: (String) -> Unit) {
    var wallet by remember { mutableStateOf<WalletResp?>(null) }
    LaunchedEffect(Unit) { call({ Net.api.wallet() }, toast)?.let { wallet = it } }
    val w = wallet

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("المحفظة", onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // hero الرصيد (حقيقي: مجموع أرباح الطلبات المسلّمة)
            Box(
                Modifier.padding(start = 22.dp, end = 22.dp).fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Grad.green).padding(20.dp)
            ) {
                Column {
                    T("رصيد أرباح المتجر", 12, FontWeight.Normal, Color.White.copy(alpha = .85f))
                    Spacer(Modifier.height(4.dp))
                    T("﷼" + money(w?.balance ?: 0.0), 32, FontWeight.Black, Color.White)
                    Spacer(Modifier.height(6.dp))
                    T("إجمالي أرباح طلباتك المسلّمة", 11, FontWeight.Normal, Color.White.copy(alpha = .8f))
                }
            }

            Spacer(Modifier.height(14.dp))
            // تسوية تلقائية عبر بوابة الدفع
            Box(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFEEF4EF)).padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Ic(R.drawable.ic_card, 17.dp, C.greenD, Modifier.padding(top = 1.dp))
                    Spacer(Modifier.width(9.dp))
                    T("تُحوَّل أرباحك تلقائياً إلى حسابك في بوابة الدفع المربوطة — لا حاجة لطلب سحب. اربط أو غيّر البوابة من الأسفل.",
                        11, FontWeight.Medium, C.greenD, lineHeight = 19)
                }
            }

            // بوابات الدفع
            OCard(Modifier.padding(start = 22.dp, end = 22.dp, top = 14.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onPayments)) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Grad.blue), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_card, 22.dp, Color.White) }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        T("بوابات الدفع وتحويل الأرباح", 13, FontWeight.Bold, C.head)
                        Spacer(Modifier.height(2.dp))
                        T("اربط حسابك في ميسر · تاب · هايبرباي وغيرها لاستلام أرباحك", 10, FontWeight.Normal, C.muted, lineHeight = 16)
                    }
                    Ic(R.drawable.ic_back, 17.dp, Color(0xFFC3C9C0))
                }
            }

            SecTitle("سجل المعاملات")
            val txs = w?.transactions ?: emptyList()
            if (w != null && txs.isEmpty()) {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(20.dp)) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { T("لا توجد معاملات بعد", 12, FontWeight.Normal, C.muted) }
                }
            } else {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(vertical = 4.dp)) {
                    txs.forEachIndexed { i, t ->
                        Tx(R.drawable.ic_box, C.pillLive, C.greenD, t.title, t.dt ?: "", "+ ﷼" + money(t.amount), C.greenD, last = i == txs.lastIndex)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Tx(iconId: Int, iconBg: Color, iconColor: Color, title: String, sub: String, amount: String, amountColor: Color, last: Boolean = false) {
    Column {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(iconBg), contentAlignment = Alignment.Center) {
                Ic(iconId, 17.dp, iconColor)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                T(title, 12, FontWeight.Bold, C.head, maxLines = 1)
                Spacer(Modifier.height(1.dp))
                T(sub, 10, FontWeight.Medium, C.muted, lineHeight = 16)
            }
            Spacer(Modifier.width(8.dp))
            T(amount, 13, FontWeight.Black, amountColor, maxLines = 1)
        }
        if (!last) androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 16.dp)) {
            drawLine(Color(0xFFF0ECE3), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 1f)
        }
    }
}
