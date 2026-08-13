package com.matnokh.customer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matnokh.customer.R

/* شاشة فرعية بسيطة لعناصر «حسابي»: عناويني / وسائل الدفع / المفضّلة / المساعدة */
@Composable
fun AccountScreen(kind: String, onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val (title, icon, empty, cta) = when (kind) {
        "addresses" -> Quad("عناويني", R.drawable.ic_pin, "لا توجد عناوين محفوظة بعد", "إضافة عنوان جديد")
        "paymethods" -> Quad("وسائل الدفع", R.drawable.ic_card, "أضف وسيلة دفع لتسريع الطلبات", "إضافة وسيلة دفع")
        "favorites" -> Quad("المفضّلة", R.drawable.ic_heart, "لم تُضِف أي متجر أو منتج للمفضّلة بعد", "تصفّح المتاجر")
        else -> Quad("مركز المساعدة", R.drawable.ic_msg, "فريق الدعم متاح على مدار الساعة", "تواصل مع الدعم")
    }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(title, onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(40.dp))
            Box(Modifier.size(88.dp).clip(CircleShape).background(C.pillLive), contentAlignment = Alignment.Center) { Ic(icon, 42.dp, C.greenD) }
            Spacer(Modifier.height(18.dp))
            T(title, 18, FontWeight.Black, C.head)
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.Text(empty, fontFamily = Cairo, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = C.muted, textAlign = TextAlign.Center, lineHeight = 22.sp, modifier = Modifier.widthIn(max = 260.dp))
            Spacer(Modifier.height(24.dp))
            Row(Modifier.clip(RoundedCornerShape(15.dp)).background(Grad.green).clickable { toast("$cta — قريباً") }.padding(horizontal = 20.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                Ic(R.drawable.ic_plus, 16.dp, Color.White); Spacer(Modifier.width(8.dp)); T(cta, 13, FontWeight.ExtraBold, Color.White)
            }
        }
    }
}

private data class Quad(val a: String, val b: Int, val c: String, val d: String)
