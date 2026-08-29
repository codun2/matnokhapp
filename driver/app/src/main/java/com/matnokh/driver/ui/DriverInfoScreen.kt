package com.matnokh.driver.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.matnokh.driver.R
import com.matnokh.driver.net.Net

fun svcLabel(k: String): String = when (k) {
    "errand" -> tr("متاجر قريبة", "Nearby stores")
    "store" -> tr("متاجر مطنوخ", "Matnokh stores")
    "fast" -> tr("توصيل سريع", "Fast delivery")
    "furniture", "furn" -> tr("نقل أثاث", "Furniture moving")
    "cold" -> tr("نقل مبرّد", "Refrigerated transport")
    "heavy", "sand", "crane" -> tr("نقل ثقيل", "Heavy transport")
    "water" -> tr("توصيل مياه", "Water delivery")
    "goods" -> tr("مشتريات وبضائع", "Purchases & goods")
    "transport" -> tr("نقل عام", "General transport")
    else -> tr("خدمة نقل", "Transport service")
}

@Composable
fun DriverInfoScreen(kind: String, onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val ctx = LocalContext.current
    var svcNames by remember { mutableStateOf<Map<String, com.matnokh.driver.net.SvcLite>>(emptyMap()) }
    LaunchedEffect(kind) { if (kind == "myservices") runCatching { svcNames = Net.api.driverServices().services.associate { it.key to it } } }
    val title = when (kind) {
        "vehicle" -> tr("مركبتي", "My vehicle"); "documents" -> tr("مستنداتي", "My documents"); "myservices" -> tr("خدماتي المفعّلة", "My enabled services"); else -> tr("الدعم الفني", "Technical support")
    }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(title, onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
            when (kind) {
                "vehicle" -> {
                    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(Grad.green), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_van, 24.dp, Color.White) }
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) {
                                T(Drv.vehicle.value, 15, FontWeight.Bold, C.head)
                                T(Drv.plate.value?.takeIf { it.isNotBlank() }?.let { tr("اللوحة: $it", "Plate: $it") } ?: tr("بدون لوحة", "No plate"), 11, FontWeight.Normal, C.muted)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                        T(tr("صورة المركبة", "Vehicle photo"), 12, FontWeight.ExtraBold, C.head)
                        Spacer(Modifier.height(10.dp))
                        val ph = Drv.vehiclePhoto.value
                        if (!ph.isNullOrBlank()) AsyncImage(model = ph, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)))
                        else Box(Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)).background(C.card2), contentAlignment = Alignment.Center) { T(tr("لا توجد صورة للمركبة", "No vehicle photo"), 12, FontWeight.Medium, C.muted) }
                    }
                }
                "documents" -> {
                    InfoCard(tr("رقم الهوية الوطنية", "National ID number"), Drv.nationalId.value?.takeIf { it.isNotBlank() } ?: tr("غير مُدخل", "Not entered"))
                    Spacer(Modifier.height(10.dp))
                    InfoCard(tr("رقم رخصة القيادة", "Driver's license number"), Drv.license.value?.takeIf { it.isNotBlank() } ?: tr("غير مُدخل", "Not entered"))
                    Spacer(Modifier.height(10.dp))
                    listOf(tr("رخصة القيادة", "Driver's license") to Drv.licensePhoto.value, tr("صورة الهوية", "ID photo") to Drv.idPhoto.value, tr("جواز السفر", "Passport") to Drv.passportPhoto.value).forEach { (lbl, u) ->
                        if (!u.isNullOrBlank()) { Spacer(Modifier.height(10.dp)); OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) { T(lbl, 11, FontWeight.Normal, C.muted); Spacer(Modifier.height(6.dp)); AsyncImage(model = u, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(14.dp))) } }
                    }
                    Spacer(Modifier.height(10.dp))
                    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Ic(R.drawable.ic_shield, 16.dp, C.greenD); Spacer(Modifier.width(8.dp)); T(tr("حالة الحساب: ${if (Drv.name.value.isNotBlank()) "موثّق" else "قيد المراجعة"}", "Account status: ${if (Drv.name.value.isNotBlank()) "Verified" else "Under review"}"), 12, FontWeight.Bold, C.head) }
                    }
                }
                "myservices" -> {
                    if (Drv.services.isEmpty()) Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { T(tr("لا توجد خدمات مفعّلة", "No enabled services"), 13, FontWeight.Bold, C.muted) }
                    else Column(Modifier.padding(horizontal = 22.dp)) {
                        T(tr("تصلك طلبات هذه الخدمات فقط:", "You'll only receive orders for these services:"), 11, FontWeight.Medium, C.muted)
                        Spacer(Modifier.height(10.dp))
                        Drv.services.forEach { k ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(15.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(15.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Grad.green), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_zap, 17.dp, Color.White) }
                                Spacer(Modifier.width(12.dp)); T(svcNames[k]?.let { trd(it.name, it.name_en) } ?: svcLabel(k), 13, FontWeight.Bold, C.head)
                            }
                        }
                    }
                }
                else -> { // support
                    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                        T(tr("فريق الدعم متاح على مدار الساعة لمساعدتك.", "The support team is available around the clock to help you."), 12, FontWeight.Medium, C.muted, lineHeight = 18)
                    }
                    Spacer(Modifier.height(12.dp))
                    SupportBtn(R.drawable.ic_phone, tr("اتصال بالدعم", "Call support")) { runCatching { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:920000000"))) }.onFailure { toast(tr("تعذّر فتح الاتصال", "Couldn't open the call")) } }
                    Spacer(Modifier.height(10.dp))
                    SupportBtn(R.drawable.ic_msg, tr("واتساب الدعم", "Support WhatsApp")) { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/920000000"))) }.onFailure { toast(tr("تعذّر فتح واتساب", "Couldn't open WhatsApp")) } }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
        T(label, 11, FontWeight.Normal, C.muted); Spacer(Modifier.height(4.dp)); T(value, 15, FontWeight.Bold, C.head)
    }
}

@Composable
private fun SupportBtn(icon: Int, label: String, onClick: () -> Unit) {
    Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(15.dp)).clickable(onClick = onClick).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Grad.green), contentAlignment = Alignment.Center) { Ic(icon, 17.dp, Color.White) }
        Spacer(Modifier.width(12.dp)); T(label, 13, FontWeight.Bold, C.head, Modifier.weight(1f)); Ic(R.drawable.ic_back, 16.dp, C.chev)
    }
}
