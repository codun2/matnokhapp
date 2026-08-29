package com.matnokh.customer.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matnokh.customer.R
import com.matnokh.customer.net.UiStore

/* ── تأكيد إرسال الطلب للمتجر ── */
@Composable
fun OrderSentScreen(storeName: String, onTrack: () -> Unit, onPayment: () -> Unit, onOrders: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().background(C.bg).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(C.pillLive), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_check, 52.dp, C.greenD) }
        Spacer(Modifier.height(22.dp))
        T(tr("تم إرسال طلبك ✓", "Your order was sent ✓"), 24, FontWeight.Black, C.head)
        Spacer(Modifier.height(12.dp))
        androidx.compose.material3.Text(
            tr("ذهب طلبك إلى «$storeName» — انتظر قليلاً حتى يجهّزه المتجر. وحين يُنتهى من تجهيز طلبك، سيتواصل معك أقرب مندوب لك لتوصيله.", "Your order went to «$storeName» — please wait while the store prepares it. Once ready, the nearest courier will contact you to deliver it."),
            fontFamily = Cairo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = C.muted, textAlign = TextAlign.Center, lineHeight = 26.sp, modifier = Modifier.widthIn(max = 320.dp))
        Spacer(Modifier.height(28.dp))
        WideButton(tr("تتبّع الطلب", "Track order"), R.drawable.ic_pin, modifier = Modifier.widthIn(max = 300.dp), onClick = onTrack)
        Spacer(Modifier.height(14.dp))
        T(tr("متابعة طلباتي", "Track my orders"), 13, FontWeight.Bold, C.muted, Modifier.clickable(onClick = onOrders))
        Spacer(Modifier.height(10.dp))
        T(tr("العودة للرئيسية", "Back to home"), 13, FontWeight.Bold, C.muted, Modifier.clickable(onClick = onHome))
    }
}

/* ── طلب عبر مندوب من متجر قريب (Errand) ── */
@Composable
fun ErrandScreen(store: UiStore, onBack: () -> Unit, onMenu: () -> Unit, onSend: (String) -> Unit, toast: (String) -> Unit) {
    var msg by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("طلب من ${store.name}", "Order from ${store.name}"), onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StoreLogo(store.logo, 50.dp, 16.dp, store.categoryName)
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) { T(store.name, 14, FontWeight.Bold, C.head); Spacer(Modifier.height(2.dp)); T(tr("متجر قريب · اطلب عبر مندوب", "Nearby store · order via courier"), 11, FontWeight.Normal, C.muted) }
                    Box(Modifier.clip(CircleShape).background(Color(0xFFE9F0F4)).padding(horizontal = 9.dp, vertical = 3.dp)) { T(tr("${store.dist} كم", "${store.dist} km"), 10, FontWeight.ExtraBold, C.blueText) }
                }
            }
            Spacer(Modifier.height(12.dp))
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                OcTitle(R.drawable.ic_msg, tr("اكتب ما تريد شراءه", "Write what you want to buy"))
                FinField(msg, { msg = it }, tr("مثال: كيلو طماطم · ربطة خبز · علبة حليب · كرتونة بيض…", "e.g. 1 kg tomatoes · a bunch of bread · a milk carton · a tray of eggs…"), singleLine = false, minHeight = 110.dp)
                Spacer(Modifier.height(6.dp))
                T(tr("يشتري المندوب هذه المشتريات نيابةً عنك من المتجر ويوصّلها إليك.", "The courier buys these items on your behalf from the store and delivers them to you."), 10, FontWeight.Medium, C.muted, lineHeight = 16)
            }
            Spacer(Modifier.height(12.dp))
            // الدفع
            Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFFEEF4EF)).border(1.dp, Color(0xFFCFE0D4), RoundedCornerShape(18.dp)).padding(horizontal = 15.dp, vertical = 13.dp), verticalAlignment = Alignment.Top) {
                Ic(R.drawable.ic_card, 17.dp, C.greenD, Modifier.padding(top = 1.dp))
                Spacer(Modifier.width(9.dp))
                T(tr("الدفع نقداً للمندوب عند الاستلام — تدفع قيمة المشتريات الفعلية + أجرة التوصيل التي تتفق عليها مع المندوب.", "Pay the courier in cash on delivery — the actual items value + the delivery fee you agree on with the courier."), 11, FontWeight.Medium, C.greenD, lineHeight = 19)
            }
            Spacer(Modifier.height(16.dp))
            WideButton(tr("أرسل الطلب لأقرب مندوب", "Send order to nearest courier"), R.drawable.ic_nav, modifier = Modifier.padding(horizontal = 22.dp)) {
                if (msg.isBlank()) { toast(tr("اكتب ما تريد شراءه أولاً", "Write what you want to buy first")); return@WideButton }
                onSend(msg.trim())
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
