package com.matnokh.driver.ui

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.driver.R

@Composable
fun ProfileScreen(onBack: () -> Unit, onMenu: () -> Unit, onLogout: () -> Unit, toast: (String) -> Unit, onNav: (String) -> Unit = {}) {
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("حسابي", onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(toast)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) { T(Drv.name.value, 16, FontWeight.Bold, C.head); T("مندوب مطنوخ · ${Drv.city.value}", 11, FontWeight.Normal, C.muted) }
                    Row(Modifier.clip(CircleShape).background(C.pillLive).padding(horizontal = 11.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) { Ic(R.drawable.ic_shield, 13.dp, C.greenD); Spacer(Modifier.width(4.dp)); T("موثّق", 10, FontWeight.ExtraBold, C.greenD) }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Kpi("${Drv.tripsToday.value}", "رحلات اليوم", C.greenD, Modifier.weight(1f))
                Kpi("${Drv.rating.value} ★", "التقييم", C.blueText, Modifier.weight(1f))
                Kpi("﷼${Drv.balance.value}", "الرصيد", C.terraText, Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            Column(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp))) {
                PRow(Grad.green, R.drawable.ic_van, "مركبتي", Drv.vehicle.value + (Drv.plate.value?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: "")) { onNav("vehicle") }
                PRow(Grad.blue, R.drawable.ic_doc, "مستنداتي", "الهوية ورخصة القيادة") { onNav("documents") }
                PRow(Grad.terra, R.drawable.ic_zap, "خدماتي المفعّلة", "${Drv.services.size} خدمة مفعّلة") { onNav("myservices") }
                PRow(Grad.sand, R.drawable.ic_bell, "الإشعارات", "التنبيهات والتحديثات", last = true) { onNav("notifications") }
            }
            Spacer(Modifier.height(14.dp))
            Column(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp))) {
                PRow(Grad.blue, R.drawable.ic_msg, "الدعم الفني", "على مدار الساعة") { onNav("support") }
                Row(Modifier.fillMaxWidth().clickable(onClick = onLogout).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(C.redBg), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_out, 17.dp, C.redText) }
                    Spacer(Modifier.width(12.dp)); T("تسجيل الخروج", 13, FontWeight.Bold, C.redText, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(110.dp))
        }
    }
}

@Composable
private fun PRow(brush: Brush, iconId: Int, title: String, sub: String?, last: Boolean = false, onClick: () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(brush), contentAlignment = Alignment.Center) { Ic(iconId, 17.dp, Color.White) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { T(title, 13, FontWeight.Bold, C.head); if (sub != null) { Spacer(Modifier.height(1.dp)); T(sub, 10, FontWeight.Medium, C.muted, maxLines = 1) } }
            Ic(R.drawable.ic_back, 17.dp, C.chev)
        }
        if (!last) Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(1.dp).background(Color(0xFFF0ECE3)))
    }
}
