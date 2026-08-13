package com.matnokh.customer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.matnokh.customer.R
import com.matnokh.customer.net.UiStore

// شعار متجر (صورة أو أيقونة بديلة)
@Composable
fun StoreLogo(logo: String?, size: Dp, corner: Dp, category: String? = null) {
    if (logo.isNullOrBlank()) Box(Modifier.size(size).clip(RoundedCornerShape(corner)).background(C.card2), contentAlignment = Alignment.Center) { Text(catEmoji(category), fontSize = (size.value * 0.45f).sp) }
    else AsyncImage(model = logo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(size).clip(RoundedCornerShape(corner)))
}

// أيقونة افتراضية حسب نوع المتجر (تُستبدل بشعار المتجر إن رفعه التاجر)
fun catEmoji(cat: String?): String = when {
    cat == null -> "\uD83C\uDFEA"
    "صيدل" in cat -> "\uD83D\uDC8A"
    "مطعم" in cat || "مطاعم" in cat || "مأكولات" in cat -> "\uD83C\uDF7D\uFE0F"
    "سوبر" in cat || "بقالة" in cat || "ماركت" in cat || "تسوق" in cat -> "\uD83D\uDED2"
    "حلوي" in cat || "حلا" in cat -> "\uD83C\uDF70"
    "قهوة" in cat || "كافيه" in cat || "مشروب" in cat -> "\u2615"
    "خضار" in cat || "فواكه" in cat -> "\uD83E\uDD6C"
    "لحوم" in cat || "دجاج" in cat -> "\uD83C\uDF57"
    "إلكترون" in cat || "الكترون" in cat || "جوال" in cat -> "\uD83D\uDCF1"
    "ملابس" in cat || "أزياء" in cat -> "\uD83D\uDC55"
    "ورد" in cat || "هدايا" in cat -> "\uD83C\uDF81"
    "مخبز" in cat || "خبز" in cat -> "\uD83E\uDD56"
    else -> "\uD83C\uDFEA"
}

@Composable
fun ProductImg(url: String?, height: Dp, corner: Dp, emojiFallback: String = "🍽️") {
    if (url.isNullOrBlank()) Box(Modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(corner)).background(C.card2), contentAlignment = Alignment.Center) { Text(emojiFallback, fontSize = (height.value * 0.42f).sp) }
    else AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(corner)))
}

@Composable
fun StoreRow(s: UiStore, onClick: () -> Unit) {
    Row(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 11.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).clickable(onClick = onClick).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
        StoreLogo(s.logo, 52.dp, 16.dp, s.categoryName)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            T(s.name, 13, FontWeight.Bold, C.head, maxLines = 1)
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Text("★", color = Color(0xFFD9A441), fontSize = 11.sp); Spacer(Modifier.width(4.dp)); T("${s.rating} · ${s.categoryName}", 11, FontWeight.Normal, C.muted, maxLines = 1) }
        }
        com.matnokh.customer.net.Repo.here?.let { h ->
            if (s.lat != null && s.lng != null) {
                val d = distanceKm(com.google.android.gms.maps.model.LatLng(h.first, h.second), com.google.android.gms.maps.model.LatLng(s.lat, s.lng))
                Box(Modifier.clip(CircleShape).background(Color(0xFFE9F0F4)).padding(horizontal = 9.dp, vertical = 3.dp)) { T("${"%.1f".format(d)} كم", 10, FontWeight.ExtraBold, C.blueText) }
                Spacer(Modifier.width(6.dp))
            }
        }
        Spacer(Modifier.width(6.dp))
        StorePill(if (s.isOpen) "متاح" else "مغلق", if (s.isOpen) C.pillLive else C.pillOff, if (s.isOpen) C.greenD else Color(0xFF9AA198))
        Spacer(Modifier.width(6.dp)); FavHeart(s.id)
    }
}

@Composable
fun StorePill(label: String, bg: Color, fg: Color) {
    Box(Modifier.clip(CircleShape).background(bg).padding(horizontal = 11.dp, vertical = 5.dp)) { T(label, 10, FontWeight.ExtraBold, fg) }
}

@Composable
fun NearCard(brush: Brush, iconId: Int, title: String, sub: String, borderColor: Color = Color(0xFFCFE0D4), onClick: () -> Unit) {
    Row(Modifier.padding(horizontal = 22.dp).padding(top = 12.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.5.dp, borderColor, RoundedCornerShape(22.dp)).clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(50.dp).clip(RoundedCornerShape(17.dp)).background(brush), contentAlignment = Alignment.Center) { Ic(iconId, 24.dp, Color.White) }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) { T(title, 14, FontWeight.Bold, C.head); Spacer(Modifier.height(2.dp)); T(sub, 11, FontWeight.Normal, C.muted, lineHeight = 17) }
        Ic(R.drawable.ic_back, 17.dp, Color(0xFFC3C9C0))
    }
}

@Composable
fun SecTitle(title: String, link: String? = null, onLink: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        T(title, 16, FontWeight.ExtraBold, C.head, Modifier.weight(1f))
        if (link != null) Row(Modifier.clickable(onClick = onLink), verticalAlignment = Alignment.CenterVertically) { T(link, 12, FontWeight.Bold, C.green); Spacer(Modifier.width(4.dp)); Ic(R.drawable.ic_back, 14.dp, C.green) }
    }
}

@Composable
fun CustomerHeader(onMenu: () -> Unit, onCart: () -> Unit, cartCount: Int, onBell: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(C.bg.copy(alpha = .96f)).statusBarsPadding().padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MiniAvatar(46.dp, 16.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { T("مساء الخير 👋", 11, FontWeight.Normal, C.muted); T(com.matnokh.customer.net.Session.name ?: "زائر", 15, FontWeight.Bold, C.text, maxLines = 1) }
            HeaderIcon(R.drawable.ic_menu, onClick = onMenu); Spacer(Modifier.width(9.dp)); CartButton(cartCount, onCart); Spacer(Modifier.width(9.dp))
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(15.dp)).clickable(onClick = onBell), contentAlignment = Alignment.Center) {
                Ic(R.drawable.ic_bell, 17.dp, Color(0xFF5D6B62)); Box(Modifier.align(Alignment.TopEnd).padding(top = 10.dp, end = 11.dp).size(8.dp).clip(CircleShape).background(C.terra))
            }
        }
    }
}

@Composable
fun HeaderIcon(iconId: Int, onClick: () -> Unit) {
    Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(15.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) { Ic(iconId, 17.dp, Color(0xFF5D6B62)) }
}

@Composable
fun CartButton(count: Int, onClick: () -> Unit) {
    Box(Modifier.size(44.dp)) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(15.dp)).background(C.card).border(1.dp, if (count > 0) Color(0xFFCFE0D4) else C.line, RoundedCornerShape(15.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_cart, 17.dp, if (count > 0) C.greenD else Color(0xFF5D6B62)) }
        if (count > 0) Box(Modifier.align(Alignment.TopStart).offset(x = (-6).dp, y = (-6).dp).defaultMinSize(minWidth = 21.dp, minHeight = 21.dp).clip(CircleShape).background(Grad.terra).padding(horizontal = 5.dp), contentAlignment = Alignment.Center) { T("$count", 10, FontWeight.Black, Color.White) }
    }
}

@androidx.compose.runtime.Composable
fun FavHeart(merchantId: Int) {
    if (!com.matnokh.customer.net.Session.isLoggedIn()) return
    val scope = rememberCoroutineScope()
    val fav = com.matnokh.customer.net.Repo.favIds.contains(merchantId)
    Box(Modifier.size(34.dp).clip(CircleShape).background(if (fav) C.redBg else Color(0xFFF1EEE8)).clickable {
        scope.launch {
            val res = runCatching { com.matnokh.customer.net.Net.api.toggleFavorite(merchantId) }.getOrNull()
            if (res != null) { if (res.favorite) { if (!com.matnokh.customer.net.Repo.favIds.contains(merchantId)) com.matnokh.customer.net.Repo.favIds.add(merchantId) } else com.matnokh.customer.net.Repo.favIds.remove(merchantId) }
        }
    }, contentAlignment = Alignment.Center) { Ic(R.drawable.ic_heart, 17.dp, if (fav) C.redText else Color(0xFFB8BFB6)) }
}

fun money(v: Double): String {
    val r = Math.round(v * 100.0) / 100.0
    return if (r % 1.0 == 0.0) r.toLong().toString() else r.toString().trimEnd('0').trimEnd('.')
}
