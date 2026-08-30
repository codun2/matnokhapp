@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.matnokh.customer.ui

import androidx.compose.animation.core.*
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.matnokh.customer.R

@Composable
fun SplashScreen(onStart: () -> Unit) {
    val float by rememberInfiniteTransition(label = "f").animateFloat(
        0f, -10f, infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float")
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF3F6F2), C.bg)))) {
        Box(Modifier.align(Alignment.TopEnd).offset(x = 130.dp, y = (-140).dp).size(420.dp).clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color(0x59A8BFAE), Color(0x00A8BFAE)))))
        Box(Modifier.align(Alignment.BottomStart).offset(x = (-100).dp, y = 110.dp).size(360.dp).clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color(0x52D9C8A9), Color(0x00D9C8A9)))))
        // اختيار اللغة قبل الدخول — يظهر أعلى الشاشة ويقلب التطبيق كاملاً فوراً
        Row(Modifier.align(Alignment.TopCenter).safeDrawingPadding().padding(top = 12.dp).clip(CircleShape).background(Color.White).border(1.dp, C.line, CircleShape).padding(4.dp).zIndex(2f), verticalAlignment = Alignment.CenterVertically) {
            LangChip("العربية", Lang.isAr) { Lang.set("ar") }
            LangChip("English", !Lang.isAr) { Lang.set("en") }
        }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val minH = maxHeight
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).safeDrawingPadding().heightIn(min = minH)
                .padding(horizontal = 30.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(Modifier.offset(y = float.dp).size(110.dp).clip(RoundedCornerShape(32.dp)).background(Grad.green), contentAlignment = Alignment.Center) {
                    Ic(R.drawable.ic_truck, 56.dp, Color.White)
                }
                Spacer(Modifier.height(28.dp))
                T(tr("مطنوخ", "Matnokh"), 42, FontWeight.Black, Color(0xFF33463C))
                Spacer(Modifier.height(6.dp))
                T(tr("كل شي بوصل. بهدوء وأمان.", "Everything arrives. Calmly and safely."), 15, FontWeight.Bold, C.green)
                Spacer(Modifier.height(14.dp))
                Text(tr("منصة واحدة لكل احتياجات النقل والتسوّق — اطلب من متاجرك المفضّلة أو انقل أي حمولة، وتتبّع بلحظتها.", "One platform for all your transport & shopping needs — order from your favorite stores or move any load, and track it in real time."),
                    fontFamily = Cairo, fontSize = 14.sp, color = C.muted, textAlign = TextAlign.Center, lineHeight = 28.sp, modifier = Modifier.widthIn(max = 290.dp))
                Spacer(Modifier.height(26.dp))
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Feat(R.drawable.ic_zap, tr("توصيل سريع", "Fast delivery")); Feat(R.drawable.ic_shield, tr("شحنات مؤمّنة", "Insured shipments")); Feat(R.drawable.ic_nav, tr("تتبّع مباشر", "Live tracking"))
                }
                Spacer(Modifier.height(34.dp))
                Row(Modifier.widthIn(max = 280.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Grad.green).clickable(onClick = onStart).padding(vertical = 17.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    T(tr("ابدأ الآن", "Get started"), 16, FontWeight.ExtraBold, Color.White); Spacer(Modifier.width(9.dp)); Ic(R.drawable.ic_back, 17.dp, Color.White)
                }
                Spacer(Modifier.height(12.dp)); T(tr("لديّ حساب — تسجيل الدخول", "I have an account — log in"), 13, FontWeight.Bold, C.muted, maxLines = 1)
            }
        }
    }
}

@Composable
private fun Feat(iconId: Int, label: String) {
    Row(Modifier.clip(CircleShape).background(Color.White).border(1.dp, C.line, CircleShape).padding(horizontal = 15.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Ic(iconId, 15.dp, C.green); Spacer(Modifier.width(6.dp)); T(label, 12, FontWeight.Bold, Color(0xFF5D6B62), maxLines = 1)
    }
}

@Composable
private fun LangChip(label: String, on: Boolean, onClick: () -> Unit) {
    Box(Modifier.clip(CircleShape).then(if (on) Modifier.background(Grad.green) else Modifier).clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 8.dp)) {
        T(label, 13, FontWeight.ExtraBold, if (on) Color.White else C.muted, maxLines = 1)
    }
}
