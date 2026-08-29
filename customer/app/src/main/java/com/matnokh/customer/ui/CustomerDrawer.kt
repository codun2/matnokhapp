package com.matnokh.customer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matnokh.customer.R

private sealed interface Entry
private data class Grp(val title: String) : Entry
private data class Itm(val name: String, val icon: Int, val view: String?, val toast: String?, val badge: String? = null, val out: Boolean = false) : Entry

@Composable
fun CustomerDrawer(open: Boolean, current: String, onClose: () -> Unit, onNavigate: (String) -> Unit, toast: (String) -> Unit) {
    var visible by remember { mutableStateOf(open) }
    LaunchedEffect(open) { if (open) visible = true }
    val anim by animateFloatAsState(if (open) 1f else 0f, tween(300), label = "d", finishedListener = { if (!open) visible = false })
    if (!visible) return
    val menu = remember(com.matnokh.customer.net.Repo.offers.size, Cart.count()) { listOf(
        Grp(tr("التصفّح", "Browse")),
        Itm(tr("الرئيسية", "Home"), R.drawable.ic_home, "home", null),
        Itm(tr("كل المتاجر", "All stores"), R.drawable.ic_shop, "stores", null),
        Itm(tr("متاجر قريبة", "Nearby stores"), R.drawable.ic_pin, "nearby", null),
        Itm(tr("العروض والخصومات", "Offers & discounts"), R.drawable.ic_zap, "offersall", null, badge = "${com.matnokh.customer.net.Repo.offers.size}"),
        Itm(tr("خدمات النقل", "Transport services"), R.drawable.ic_truck, "services", null),
        Grp(tr("طلباتي", "My orders")),
        Itm(tr("سلتي", "My cart"), R.drawable.ic_cart, "cart", null, badge = if (Cart.count() > 0) "${Cart.count()}" else null),
        Itm(tr("طلباتي السابقة", "My past orders"), R.drawable.ic_list, "orders", null),
        Itm(tr("تتبّع الطلب الحالي", "Track current order"), R.drawable.ic_nav, "track", null),
        Itm(tr("المفضّلة", "Favorites"), R.drawable.ic_heart, "favorites", null),
        Grp(tr("حسابي", "My account")),
        Itm(tr("الملف الشخصي", "Profile"), R.drawable.ic_user, "profile", null),
        Itm(tr("عناويني", "My addresses"), R.drawable.ic_pin, "addresses", null),
        Itm(tr("وسائل الدفع", "Payment methods"), R.drawable.ic_card, "paymethods", null),
        Itm(tr("الإشعارات", "Notifications"), R.drawable.ic_bell, "notifications", null),
        Grp(tr("التطبيق", "App")),
        Itm(tr("الإعدادات", "Settings"), R.drawable.ic_cog, "settings", null),
        Itm(tr("اللغة", "Language"), R.drawable.ic_globe, null, tr("اللغة الحالية: العربية", "Current language: English")),
        Itm(tr("مركز المساعدة", "Help center"), R.drawable.ic_msg, null, tr("مركز المساعدة — أسئلة شائعة ودعم", "Help center — FAQ & support")),
        Itm(tr("حول التطبيق", "About the app"), R.drawable.ic_info, null, tr("مطنوخ — الإصدار 1.1", "Matnokh — version 1.1")),
        Itm(tr("مشاركة التطبيق", "Share the app"), R.drawable.ic_share, null, tr("تم نسخ رابط التطبيق ✓", "App link copied ✓")),
        Itm(tr("تسجيل الخروج", "Log out"), R.drawable.ic_out, "splash", null, out = true),
    ) }
    val width = (LocalConfiguration.current.screenWidthDp * 0.8f).coerceAtMost(302f).dp
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().graphicsLayer { alpha = anim }.background(Color(0x6B2D3A34)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose))
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(Modifier.fillMaxSize()) {
        Box(Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(width).graphicsLayer { translationX = (1f - anim) * size.width }.background(C.bg)) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)).background(Grad.green).statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(60.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = .2f)), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_truck, 30.dp, Color.White) }
                    Spacer(Modifier.height(10.dp)); T(tr("مطنوخ", "Matnokh"), 22, FontWeight.Black, Color.White); T(tr("كل شي بوصل. بهدوء وأمان.", "Everything arrives. Calmly and safely."), 11, FontWeight.Normal, Color.White.copy(alpha = .85f))
                }
                Row(Modifier.fillMaxWidth().background(C.bg).padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    MiniAvatar(48.dp, 16.dp)
                    Spacer(Modifier.width(12.dp))
                    Column { T(tr("مرحباً، ", "Hello, ") + (com.matnokh.customer.net.Session.name ?: tr("زائر", "Guest")), 14, FontWeight.Bold, C.head); T(com.matnokh.customer.net.Session.phone ?: "", 10, FontWeight.Normal, C.muted) }
                }
                Row(Modifier.fillMaxWidth().clickable { Lang.toggle() }.background(C.bg).padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    T("🌐", 16, FontWeight.Bold, C.head)
                    Spacer(Modifier.width(10.dp))
                    T(if (Lang.isAr) "English" else "العربية", 13, FontWeight.Bold, C.greenD)
                }
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 6.dp, bottom = 22.dp)) {
                    items(menu.size) { i ->
                        when (val e = menu[i]) {
                            is Grp -> Text(e.title, fontFamily = Cairo, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB4BCB2), modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp))
                            is Itm -> DrawerItem(e, e.view == current) { onClose(); if (e.view != null) onNavigate(e.view) else e.toast?.let { toast(it) } }
                        }
                    }
                }
            }
            }
        }
        }
        }
    }
}

@Composable
private fun DrawerItem(item: Itm, active: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(CircleShape).then(if (active) Modifier.background(Grad.green) else Modifier.background(if (item.out) C.redBg else Color(0xFFEEF4EF))), contentAlignment = Alignment.Center) {
            Ic(item.icon, 18.dp, if (active) Color.White else if (item.out) C.redText else C.greenD)
        }
        Spacer(Modifier.width(13.dp))
        T(item.name, 13, FontWeight.Bold, if (item.out) C.redText else if (active) C.greenD else C.head, Modifier.weight(1f))
        if (item.badge != null) { Box(Modifier.clip(CircleShape).background(Grad.terra).padding(horizontal = 9.dp, vertical = 3.dp)) { T(item.badge, 9, FontWeight.Black, Color.White) }; Spacer(Modifier.width(8.dp)) }
        Ic(R.drawable.ic_back, 15.dp, Color(0xFFCDD4CB))
    }
}
