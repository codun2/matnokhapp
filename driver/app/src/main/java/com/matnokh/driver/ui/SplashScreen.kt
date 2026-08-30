@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.matnokh.driver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matnokh.driver.R

@Composable
fun SplashScreen(onStart: () -> Unit, onRegister: () -> Unit = {}) {
    Column(
        Modifier.fillMaxSize().background(C.bg).padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // اختيار اللغة قبل الدخول
        Row(Modifier.clip(CircleShape).background(Color.White).border(1.dp, C.line, CircleShape).padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            LangChip("العربية", Lang.isAr) { Lang.set("ar") }
            LangChip("English", !Lang.isAr) { Lang.set("en") }
        }
        Spacer(Modifier.height(24.dp))
        Box(Modifier.size(110.dp).clip(RoundedCornerShape(32.dp)).background(Grad.green), contentAlignment = Alignment.Center) {
            Ic(R.drawable.ic_van, 56.dp, Color.White)
        }
        Spacer(Modifier.height(26.dp))
        T(tr("مطنوخ كابتن", "Matnokh Captain"), 34, FontWeight.Black, Color(0xFF33463C))
        Spacer(Modifier.height(6.dp))
        T(tr("مركبتك مصدر دخلك.", "Your vehicle is your income source."), 15, FontWeight.Bold, C.green)
        Spacer(Modifier.height(14.dp))
        androidx.compose.material3.Text(
            tr("انضم لأسطول مطنوخ — استقبل الطلبات القريبة منك، قدّم سعرك بنفسك، ونفّذ بمرونة كاملة.", "Join Matnokh's fleet — receive nearby orders, set your own price, and work with full flexibility."),
            fontFamily = Cairo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = C.muted,
            textAlign = TextAlign.Center, lineHeight = 26.sp, modifier = Modifier.widthIn(max = 300.dp),
        )
        Spacer(Modifier.height(24.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Feat(R.drawable.ic_cash, tr("سعرك بيدك", "Your price, your call"))
            Feat(R.drawable.ic_clock, tr("دوام مرن", "Flexible hours"))
            Feat(R.drawable.ic_shield, tr("دفع مضمون", "Guaranteed payment"))
        }
        Spacer(Modifier.height(34.dp))
        WideButton(tr("ابدأ الاستقبال", "Start receiving"), R.drawable.ic_back, modifier = Modifier.widthIn(max = 300.dp), onClick = onStart)
        Spacer(Modifier.height(12.dp))
        T(tr("التسجيل كسائق جديد", "Register as a new driver"), 13, FontWeight.Bold, C.greenD, modifier = Modifier.clickable { onRegister() }.padding(8.dp))
        Spacer(Modifier.height(10.dp))
        T(tr("الإصدار 3.3", "Version 3.3"), 11, FontWeight.Normal, C.muted)
    }
}

@Composable
private fun Feat(iconId: Int, label: String) {
    Row(
        Modifier.clip(RoundedCornerShape(50.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(50.dp)).padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ic(iconId, 14.dp, C.green); Spacer(Modifier.width(6.dp)); T(label, 11, FontWeight.Bold, Color(0xFF5D6B62))
    }
}

@Composable
private fun LangChip(label: String, on: Boolean, onClick: () -> Unit) {
    Box(Modifier.clip(CircleShape).then(if (on) Modifier.background(Grad.green) else Modifier).clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 8.dp)) {
        T(label, 13, FontWeight.ExtraBold, if (on) Color.White else C.muted, maxLines = 1)
    }
}
