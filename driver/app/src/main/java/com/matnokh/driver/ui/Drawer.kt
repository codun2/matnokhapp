package com.matnokh.driver.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.matnokh.driver.R

private sealed interface MenuEntry
private data class Group(val title: String) : MenuEntry
private data class Item(val name: String, val iconId: Int, val view: String?, val action: String?, val badge: String? = null, val out: Boolean = false) : MenuEntry

private val MENU get() = listOf(
    Group(tr("العمل", "Work")),
    Item(tr("شاشة الاستقبال", "Receiving screen"), R.drawable.ic_nav, "home", null),
    Item(tr("طلباتي", "My orders"), R.drawable.ic_list, "myorders", null),
    Item(tr("الطلب النشط", "Active order"), R.drawable.ic_van, "active", null),
    Item(tr("عروضي المقدَّمة", "My submitted offers"), R.drawable.ic_cash, "myoffers", null),
    Group(tr("المالية", "Finance")),
    Item(tr("الأرباح والمحفظة", "Earnings & wallet"), R.drawable.ic_card, "earn", null),
    Item(tr("بوابات الدفع", "Payment gateways"), R.drawable.ic_card, "payments", null),
    Item(tr("اشتراكاتي / باقاتي", "My subscriptions / packages"), R.drawable.ic_star, "mypackages", null),
    Item(tr("طلب سحب", "Withdrawal request"), R.drawable.ic_cash, null, tr("سحب الأرباح — ضمن الخطة القادمة", "Withdraw earnings — in the upcoming plan")),
    Group(tr("حسابي", "My account")),
    Item(tr("الملف الشخصي", "Profile"), R.drawable.ic_user, "profile", null),
    Item(tr("مركبتي", "My vehicle"), R.drawable.ic_van, "vehicle", null),
    Item(tr("مستنداتي", "My documents"), R.drawable.ic_doc, "documents", null),
    Item(tr("خدماتي المفعّلة", "My enabled services"), R.drawable.ic_zap, "myservices", null),
    Item(tr("الإشعارات", "Notifications"), R.drawable.ic_bell, "notifications", null, badge = "5"),
    Group(tr("التطبيق", "App")),
    Item(tr("الإعدادات", "Settings"), R.drawable.ic_cog, null, tr("الإعدادات — الحساب والخصوصية", "Settings — account & privacy")),
    Item(tr("اللغة", "Language"), R.drawable.ic_globe, null, tr("اللغة الحالية: العربية", "Current language: English")),
    Item(tr("الدعم الفني", "Technical support"), R.drawable.ic_msg, null, tr("الدعم الفني — على مدار الساعة", "Technical support — 24/7")),
    Item(tr("حول التطبيق", "About the app"), R.drawable.ic_info, null, tr("مطنوخ كابتن — الإصدار 1.0", "Matnokh Captain — version 1.0")),
    Item(tr("مشاركة التطبيق", "Share the app"), R.drawable.ic_share, null, tr("تم نسخ رابط التطبيق ✓", "App link copied ✓")),
    Item(tr("تسجيل الخروج", "Log out"), R.drawable.ic_out, "splash", null, out = true),
)

@Composable
fun DrawerOverlay(open: Boolean, current: String, onClose: () -> Unit, onNavigate: (String) -> Unit, toast: (String) -> Unit) {
    val anim by animateFloatAsState(if (open) 1f else 0f, tween(300), label = "drawer")
    if (anim <= 0.001f) return
    val width = (LocalConfiguration.current.screenWidthDp * 0.82f).coerceAtMost(302f).dp
    Box(Modifier.fillMaxSize()) {
        // خلفية معتمة
        Box(
            Modifier.fillMaxSize().graphicsLayer { alpha = anim }.background(Color(0x6B2D3A34))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose),
        )
        // نُثبّت الدرج على اليمين فيزيائياً بفرض LTR للموضع فقط (المحتوى يبقى RTL)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(width)
                        .graphicsLayer { translationX = (1f - anim) * size.width }.background(C.bg),
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Column(Modifier.fillMaxSize()) {
                            Column(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)).background(Grad.green).statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(Modifier.size(60.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = .2f)), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_van, 30.dp, Color.White) }
                                Spacer(Modifier.height(10.dp)); T(tr("مطنوخ كابتن", "Matnokh Captain"), 22, FontWeight.Black, Color.White); T(tr("مركبتك مصدر دخلك.", "Your vehicle is your income source."), 11, FontWeight.Normal, Color.White.copy(alpha = .85f))
                            }
                            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                MiniAvatar(Drv.avatarUrl.value, Drv.avatar.value, 48.dp, 16.dp)
                                Spacer(Modifier.width(12.dp))
                                Column { T(Drv.name.value, 14, FontWeight.Bold, C.head, maxLines = 1); T("${Drv.vehicle.value} · ${Drv.city.value}", 10, FontWeight.Normal, C.muted) }
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8E3D9)))
                            Row(Modifier.fillMaxWidth().clickable { Lang.toggle() }.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                T("🌐", 16, FontWeight.Bold, C.head)
                                Spacer(Modifier.width(10.dp))
                                T(if (Lang.isAr) "English" else "العربية", 13, FontWeight.Bold, C.greenD)
                            }
                            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 6.dp, bottom = 22.dp)) {
                                items(MENU.size) { idx ->
                                    when (val e = MENU[idx]) {
                                        is Group -> T(e.title, 10, FontWeight.ExtraBold, Color(0xFFB4BCB2), Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp))
                                        is Item -> DrawerItem(e, e.view == current) {
                                            onClose()
                                            when { e.view != null -> onNavigate(e.view); e.action != null -> toast(e.action) }
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
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(if (active) Grad.green else solid(if (item.out) C.redBg else Color(0xFFEEF4EF))), contentAlignment = Alignment.Center) {
            Ic(item.iconId, 18.dp, if (active) Color.White else if (item.out) C.redText else C.greenD)
        }
        Spacer(Modifier.width(13.dp))
        T(item.name, 13, FontWeight.Bold, if (item.out) C.redText else if (active) C.greenD else C.head, Modifier.weight(1f))
        if (item.badge != null) { Box(Modifier.clip(CircleShape).background(Grad.terra).padding(horizontal = 9.dp, vertical = 3.dp)) { T(item.badge, 9, FontWeight.Black, Color.White) }; Spacer(Modifier.width(8.dp)) }
        Ic(R.drawable.ic_back, 15.dp, C.chev)
    }
}
