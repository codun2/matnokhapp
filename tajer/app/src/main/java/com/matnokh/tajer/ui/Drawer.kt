package com.matnokh.tajer.ui

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
import com.matnokh.tajer.R
import com.matnokh.tajer.net.Session

// عنصر قائمة
private sealed interface MenuEntry
private data class Group(val title: String) : MenuEntry
private data class Item(val name: String, val iconId: Int, val view: String?, val action: String?, val badge: String? = null, val out: Boolean = false) : MenuEntry

private val MENU = listOf(
    Group(tr("المتجر", "Store")),
    Item(tr("لوحة المتجر", "Store dashboard"), R.drawable.ic_chart, "dash", null),
    Item(tr("المنتجات", "Products"), R.drawable.ic_box, "products", null),
    Item(tr("منتج جديد", "New product"), R.drawable.ic_plus, "newproduct", null),
    Item(tr("الطلبات", "Orders"), R.drawable.ic_list, "orders", null),
    Item(tr("الفروع", "Branches"), R.drawable.ic_pin, "branches", null),
    Item(tr("أقسام المتجر", "Store sections"), R.drawable.ic_list, "sections", null),
    Group(tr("المالية", "Finance")),
    Item(tr("المحفظة", "Wallet"), R.drawable.ic_cash, "wallet", null),
    Item(tr("باقات الاشتراك", "Subscription packages"), R.drawable.ic_card, "packages", null),
    Item(tr("بوابات الدفع", "Payment gateways"), R.drawable.ic_card, "payments", null),
    Item(tr("التقارير", "Reports"), R.drawable.ic_chart, "reports", null),
    Group(tr("الإدارة", "Admin")),
    Item(tr("إعدادات المتجر", "Store settings"), R.drawable.ic_cog, "store", null),
    Item(tr("بيانات المتجر", "Store details"), R.drawable.ic_shop, "storedata", null),
    Item(tr("العروض والخصومات", "Offers & discounts"), R.drawable.ic_zap, "offers", null),
    Item(tr("الوثائق", "Documents"), R.drawable.ic_doc, "documents", null),
    Item(tr("الإشعارات", "Notifications"), R.drawable.ic_bell, "notifications", null),
    Group(tr("التطبيق", "App")),
    Item(tr("اللغة", "Language"), R.drawable.ic_globe, null, tr("اللغة الحالية: العربية", "Current language: English")),
    Item(tr("الدعم الفني", "Technical support"), R.drawable.ic_msg, null, tr("الدعم الفني — تواصل معنا على مدار الساعة", "Technical support — contact us around the clock")),
    Item(tr("حول التطبيق", "About the app"), R.drawable.ic_info, null, tr("مطنوخ تاجر — الإصدار 1.0", "Matnokh Merchant — version 1.0")),
    Item(tr("مشاركة المتجر", "Share the store"), R.drawable.ic_share, null, tr("تم نسخ رابط متجرك ✓", "Your store link was copied ✓")),
    Item(tr("تسجيل الخروج", "Log out"), R.drawable.ic_out, "splash", null, out = true),
)

@Composable
fun DrawerOverlay(open: Boolean, current: String, onClose: () -> Unit, onNavigate: (String) -> Unit, toast: (String) -> Unit) {
    val anim by animateFloatAsState(if (open) 1f else 0f, tween(300), label = "drawer")
    if (anim <= 0.001f) return

    val width = (LocalConfiguration.current.screenWidthDp * 0.8f).coerceAtMost(302f).dp
    Box(Modifier.fillMaxSize()) {
        // scrim
        Box(Modifier.fillMaxSize().graphicsLayer { alpha = anim }
            .background(Color(0x6B2D3A34))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose))
        // الدرج (من اليمين في RTL)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(width)
                .graphicsLayer { translationX = (1f - anim) * size.width }
                .background(C.bg),
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(Modifier.fillMaxSize()) {
                // الرأس
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)).background(Grad.green)
                        .statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.size(60.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = .2f)), contentAlignment = Alignment.Center) {
                        Ic(R.drawable.ic_shop, 30.dp, Color.White)
                    }
                    Spacer(Modifier.height(10.dp))
                    T(tr("مطنوخ تاجر", "Matnokh Merchant"), 22, FontWeight.Black, Color.White)
                    T(tr("متجرك يوصل لكل بيت.", "Your store reaches every home."), 11, FontWeight.Normal, Color.White.copy(alpha = .85f))
                }
                // المستخدم
                Row(Modifier.fillMaxWidth().background(C.bg).padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    StoreLogoBox(48.dp, 16.dp, 20)
                    Spacer(Modifier.width(12.dp))
                    Column { T(Session.storeName ?: tr("متجري", "My store"), 14, FontWeight.Bold, C.head, maxLines = 1); T(tr("تطبيق التاجر — مطنوخ", "Merchant app — Matnokh"), 10, FontWeight.Normal, C.muted) }
                }
                Line()
                Row(Modifier.fillMaxWidth().clickable { Lang.toggle() }.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    T("🌐", 16, FontWeight.Bold, C.head)
                    Spacer(Modifier.width(10.dp))
                    T(if (Lang.isAr) "English" else "العربية", 13, FontWeight.Bold, C.greenD)
                }
                // القائمة
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 6.dp, bottom = 22.dp)) {
                    items2(MENU) { e ->
                        when (e) {
                            is Group -> Text(e.title, fontFamily = Cairo, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB4BCB2),
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp))
                            is Item -> DrawerItem(e, e.view == current) {
                                onClose()
                                when {
                                    e.view != null -> onNavigate(e.view)
                                    e.action != null -> toast(e.action)
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
    }
}

@Composable
private fun DrawerItem(item: Item, active: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).clip(CircleShape)
                .background(if (active) Grad.green else if (item.out) solid(C.redBg) else solid(Color(0xFFEEF4EF))),
            contentAlignment = Alignment.Center,
        ) {
            Ic(item.iconId, 18.dp, if (active) Color.White else if (item.out) C.redText else C.greenD)
        }
        Spacer(Modifier.width(13.dp))
        T(item.name, 13, FontWeight.Bold, if (item.out) C.redText else if (active) C.greenD else C.head, Modifier.weight(1f))
        if (item.badge != null) {
            Box(Modifier.clip(CircleShape).background(Grad.terra).padding(horizontal = 9.dp, vertical = 3.dp)) {
                T(item.badge, 9, FontWeight.Black, Color.White)
            }
            Spacer(Modifier.width(8.dp))
        }
        Ic(R.drawable.ic_back, 15.dp, Color(0xFFCDD4CB))
    }
}

@Composable
private fun Line() {
    androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(Color(0xFFE8E3D9), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 1f)
    }
}

// امتداد صغير لإضافة عناصر بقائمة كسولة
private fun androidx.compose.foundation.lazy.LazyListScope.items2(list: List<MenuEntry>, item: @Composable (MenuEntry) -> Unit) {
    items(list.size) { i -> item(list[i]) }
}
