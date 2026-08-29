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
import androidx.compose.ui.unit.dp
import com.matnokh.customer.R

private val PAY_OPTIONS = listOf(tr("مدى", "Mada"), "STC Pay", "Apple Pay", tr("بطاقة ائتمانية", "Credit card"), tr("نقداً عند الاستلام", "Cash on delivery"))

/** طرق الدفع التي تظهر للزبون عند تأكيد الطلب — للمتجر (المشتريات) وللمندوب (التوصيل). */
@Composable
fun PaymentMethodsScreen(storeName: String, onDone: () -> Unit, onMenu: () -> Unit) {
    var storePay by remember { mutableStateOf(0) }
    var driverPay by remember { mutableStateOf(4) }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("طرق الدفع", "Payment methods"), onDone, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = 22.dp).padding(top = 6.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFFEEF4EF)).border(1.dp, Color(0xFFCFE0D4), RoundedCornerShape(18.dp)).padding(14.dp), verticalAlignment = Alignment.Top) {
                Ic(R.drawable.ic_card, 20.dp, C.greenD, Modifier.padding(top = 1.dp))
                Spacer(Modifier.width(11.dp))
                Column {
                    T(tr("اختر كيف تحبّ الدفع", "Choose how you'd like to pay"), 12, FontWeight.Bold, C.greenD)
                    Spacer(Modifier.height(3.dp)); T(tr("تدفع قيمة المشتريات للمتجر، وأجرة التوصيل للمندوب — كل واحدة على حدة، إلكترونياً أو نقداً.", "You pay the items value to the store, and the delivery fee to the courier — each separately, online or in cash."), 10, FontWeight.Medium, C.greenD, lineHeight = 17)
                }
            }
            PaySection(tr("قيمة المشتريات — تُدفع لـ «$storeName»", "Items value — paid to «$storeName»"), storePay) { storePay = it }
            PaySection(tr("أجرة التوصيل — تُدفع للمندوب", "Delivery fee — paid to the courier"), driverPay) { driverPay = it }
            Spacer(Modifier.height(8.dp))
            WideButton(tr("تأكيد الدفع", "Confirm payment"), R.drawable.ic_check, modifier = Modifier.padding(horizontal = 22.dp)) { onDone() }
            Spacer(Modifier.height(10.dp))
            T(tr("العودة للرئيسية", "Back to home"), 13, FontWeight.Bold, C.muted, Modifier.padding(bottom = 24.dp).fillMaxWidth().clickable(onClick = onDone).padding(4.dp))
        }
    }
}

@Composable
private fun PaySection(title: String, selected: Int, onSelect: (Int) -> Unit) {
    OcTitleLike(title)
    PAY_OPTIONS.forEachIndexed { i, opt ->
        val on = selected == i
        Row(
            Modifier.padding(horizontal = 22.dp).padding(bottom = 9.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp))
                .background(if (on) C.pillLive else C.card).border(if (on) 1.5.dp else 1.dp, if (on) C.green else C.line, RoundedCornerShape(18.dp))
                .clickable { onSelect(i) }.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(if (on) C.green else C.card2), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_card, 20.dp, if (on) Color.White else Color(0xFF9AA198)) }
            Spacer(Modifier.width(12.dp))
            T(opt, 13, FontWeight.Bold, C.head, Modifier.weight(1f))
            Box(Modifier.size(22.dp).clip(CircleShape).background(if (on) C.green else Color.Transparent).border(if (on) 0.dp else 2.dp, if (on) C.green else C.line, CircleShape), contentAlignment = Alignment.Center) { if (on) Ic(R.drawable.ic_check, 13.dp, Color.White) }
        }
    }
}

@Composable
private fun OcTitleLike(title: String) {
    Box(Modifier.padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 10.dp)) { T(title, 13, FontWeight.ExtraBold, C.head) }
}
