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

private data class PayGate(val name: String, val desc: String, val iconId: Int)

private val GATES = listOf(
    PayGate("مدى", "بطاقة مدى — الأكثر استخداماً في السعودية", R.drawable.ic_card),
    PayGate("STC Pay", "محفظة STC Pay — تحويل فوري", R.drawable.ic_cash),
    PayGate("Apple Pay", "الدفع عبر Apple Pay", R.drawable.ic_card),
    PayGate("urpay", "محفظة urpay الرقمية", R.drawable.ic_cash),
    PayGate("بطاقة ائتمانية", "Visa / Mastercard", R.drawable.ic_card),
    PayGate("تحويل بنكي (IBAN)", "استقبل أجرتك على حسابك البنكي مباشرة", R.drawable.ic_cash),
    PayGate("نقداً عند التسليم", "يدفع الزبون نقداً عند استلام الطلب", R.drawable.ic_cash),
)

@Composable
fun PaymentsScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val linked = remember { mutableStateListOf(false, false, false, false, false, false, true) }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("بوابات الدفع", onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // شرح
            Row(Modifier.padding(horizontal = 22.dp).padding(top = 6.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFFEEF4EF)).border(1.dp, C.sage, RoundedCornerShape(18.dp)).padding(14.dp), verticalAlignment = Alignment.Top) {
                Ic(R.drawable.ic_card, 20.dp, C.greenD, Modifier.padding(top = 1.dp))
                Spacer(Modifier.width(11.dp))
                Column {
                    T("اربط وسيلة دفع ليدفع لك الزبون إلكترونياً", 12, FontWeight.Bold, C.greenD)
                    Spacer(Modifier.height(3.dp)); T("عند إتمام الطلب يختار الزبون كيف يحاسبك — نقداً أو عبر إحدى الوسائل المربوطة أدناه. تُتاح البوابات حسب تفعيل الإدارة.", 10, FontWeight.Medium, C.greenD, lineHeight = 17)
                }
            }
            SecTitle("وسائل استلام الأجرة")
            GATES.forEachIndexed { i, g ->
                Row(Modifier.padding(horizontal = 22.dp).padding(bottom = 11.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(C.card).border(1.dp, if (linked[i]) C.sage else C.line, RoundedCornerShape(20.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(if (linked[i]) C.pillLive else C.card2), contentAlignment = Alignment.Center) { Ic(g.iconId, 22.dp, if (linked[i]) C.greenD else Color(0xFF9AA198)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { T(g.name, 13, FontWeight.Bold, C.head); Spacer(Modifier.height(2.dp)); T(g.desc, 10, FontWeight.Normal, C.muted, maxLines = 2, lineHeight = 15) }
                    Box(
                        Modifier.clip(RoundedCornerShape(50.dp)).background(if (linked[i]) C.pillLive else Color(0xFFFAF8F4)).border(1.dp, if (linked[i]) C.sage else C.line, RoundedCornerShape(50.dp))
                            .clickable { linked[i] = !linked[i]; toast(if (linked[i]) "تم ربط ${g.name} ✓" else "أُلغي ربط ${g.name}") }.padding(horizontal = 13.dp, vertical = 7.dp),
                    ) { T(if (linked[i]) "مربوط ✓" else "ربط", 11, FontWeight.ExtraBold, if (linked[i]) C.greenD else C.muted) }
                }
            }
            Spacer(Modifier.height(110.dp))
        }
    }
}
