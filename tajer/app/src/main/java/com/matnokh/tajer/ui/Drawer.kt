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
    Group("المتجر"),
    Item("لوحة المتجر", R.drawable.ic_chart, "dash", null),
    Item("المنتجات", R.drawable.ic_box, "products", null),
    Item("منتج جديد", R.drawable.ic_plus, "newproduct", null),
    Item("الطلبات", R.drawable.ic_list, "orders", null),
    Item("الفروع", R.drawable.ic_pin, "branches", null),
    Item("أقسام المتجر", R.drawable.ic_list, "sections", null),
    Group("المالية"),
    Item("المحفظة", R.drawable.ic_cash, "wallet", null),
    Item("باقات الاشتراك", R.drawable.ic_card, "packages", null),
    Item("بوابات الدفع", R.drawable.ic_card, "payments", null),
    Item("التقارير", R.drawable.ic_chart, "reports", null),
    Group("الإدارة"),
    Item("إعدادات المتجر", R.drawable.ic_cog, "store", null),
    Item("بيانات المتجر", R.drawable.ic_shop, "storedata", null),
    Item("العروض والخصومات", R.drawable.ic_zap, "offers", null),
    Item("الوثائق", R.drawable.ic_doc, "documents", null),
    Item("الإشعارات", R.drawable.ic_bell, "notifications", null),
    Group("التطبيق"),
    Item("اللغة", R.drawable.ic_globe, null, "اللغة الحالية: العربية"),
    Item("الدعم الفني", R.drawable.ic_msg, null, "الدعم الفني — تواصل معنا على مدار الساعة"),
    Item("حول التطبيق", R.drawable.ic_info, null, "مطنوخ تاجر — الإصدار 1.0"),
    Item("مشاركة المتجر", R.drawable.ic_share, null, "تم نسخ رابط متجرك ✓"),
    Item("تسجيل الخروج", R.drawable.ic_out, "splash", null, out = true),
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
                    T("مطنوخ تاجر", 22, FontWeight.Black, Color.White)
                    T("متجرك يوصل لكل بيت.", 11, FontWeight.Normal, Color.White.copy(alpha = .85f))
                }
                // المستخدم
                Row(Modifier.fillMaxWidth().background(C.bg).padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    StoreLogoBox(48.dp, 16.dp, 20)
                    Spacer(Modifier.width(12.dp))
                    Column { T(Session.storeName ?: "متجري", 14, FontWeight.Bold, C.head, maxLines = 1); T("تطبيق التاجر — مطنوخ", 10, FontWeight.Normal, C.muted) }
                }
                Line()
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
