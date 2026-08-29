package com.matnokh.tajer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.PlanDto
import com.matnokh.tajer.net.SubDto
import com.matnokh.tajer.net.SubscribeBody
import com.matnokh.tajer.net.call
import kotlinx.coroutines.launch

@Composable
fun PackagesScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var plans by remember { mutableStateOf<List<PlanDto>?>(null) }
    var sub by remember { mutableStateOf<SubDto?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    suspend fun load() {
        call({ Net.api.plans() }, toast)?.let { plans = it.plans }
        sub = call({ Net.api.subscription() }, toast)?.subscription
        loaded = true
    }
    LaunchedEffect(Unit) { load() }

    val canSubscribe = sub?.status != "active" && sub?.status != "pending"
    val doSubscribe: (PlanDto) -> Unit = { p ->
        if (canSubscribe && !busy) {
            busy = true
            scope.launch {
                call({ Net.api.subscribe(SubscribeBody(p.id)) }, toast)?.let { toast(it.message ?: tr("تم إرسال الطلب", "Request sent")); load() }
                busy = false
            }
        }
    }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("باقات الاشتراك", "Subscription packages"), onBack, onMenu)
        if (!loaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {
                SubBanner(sub)
                if (sub?.status == "pending" && (sub?.price ?: 0.0) > 0.0) {
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(C.green).clickable(enabled = !busy) {
                        busy = true
                        scope.launch {
                            val r = call({ Net.api.paySubscription() }, toast)
                            val u = r?.payment_url
                            if (u != null) runCatching { ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(u)).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) }.onFailure { toast(tr("تعذّر فتح صفحة الدفع", "Couldn't open the payment page")) }
                            busy = false
                        }
                    }.padding(vertical = 15.dp), contentAlignment = Alignment.Center) {
                        T(if (busy) tr("جارٍ فتح صفحة الدفع…", "Opening the payment page…") else tr("ادفع الاشتراك · ﷼${money(sub?.price ?: 0.0)}", "Pay subscription · ﷼${money(sub?.price ?: 0.0)}"), 15, FontWeight.ExtraBold, androidx.compose.ui.graphics.Color.White)
                    }
                    Spacer(Modifier.height(6.dp))
                    T(tr("بعد إتمام الدفع، عُد واضغط «تحديث حالة الاشتراك».", "After completing payment, come back and tap «Refresh subscription status»."), 11, FontWeight.Normal, C.muted)
                }
                Spacer(Modifier.height(18.dp))
                val list = plans ?: emptyList()
                val regular = list.filter { it.type != "marketing" }
                val marketing = list.filter { it.type == "marketing" }
                if (regular.isEmpty() && marketing.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) { T(tr("لا توجد باقات متاحة حالياً", "No packages available currently"), 13, FontWeight.Bold, C.muted) }
                }
                if (regular.isNotEmpty()) {
                    SecTitle(tr("الباقات العادية", "Regular packages"))
                    regular.forEach { PlanCard(it, canSubscribe && !busy, doSubscribe) }
                }
                if (marketing.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    SecTitle(tr("الباقات التسويقية 📣", "Marketing packages 📣"))
                    marketing.forEach { PlanCard(it, canSubscribe && !busy, doSubscribe) }
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun SubBanner(sub: SubDto?) {
    val (title, body, tint) = when (sub?.status) {
        "active" -> Triple(tr("اشتراكك فعّال ✓", "Your subscription is active ✓"), tr("باقة ${sub?.plan_name ?: ""} — متبقٍ ${sub?.days_left ?: 0} يوم", "Package ${sub?.plan_name ?: ""} — ${sub?.days_left ?: 0} days left") + (sub?.ends_at?.let { tr(" (ينتهي $it)", " (ends $it)") } ?: ""), C.greenD)
        "pending" -> Triple(tr("طلبك قيد المراجعة ⏳", "Your request is under review ⏳"), tr("باقة ${sub?.plan_name ?: ""} — بانتظار موافقة الإدارة، سنبلّغك فور التفعيل", "Package ${sub?.plan_name ?: ""} — awaiting admin approval; we'll notify you once activated"), Color(0xFFB45309))
        "rejected" -> Triple(tr("لم يُعتمد طلبك السابق", "Your previous request wasn't approved"), tr("اختر باقة من الأسفل لإعادة الطلب", "Choose a package below to reorder"), C.terra)
        "expired" -> Triple(tr("انتهى اشتراكك", "Your subscription has expired"), tr("اختر باقة لتجديد اشتراكك", "Choose a package to renew your subscription"), C.terra)
        else -> Triple(tr("اختر باقتك للبدء", "Choose your package to start"), tr("كل الباقات تمنحك طلبات ومنتجات بلا حدود — الفرق في المدة والنوع فقط", "All packages give you unlimited orders and products — they differ only in duration and type"), C.head)
    }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(20.dp)).padding(16.dp)) {
        T(title, 15, FontWeight.ExtraBold, tint)
        Spacer(Modifier.height(5.dp)); T(body, 12, FontWeight.Normal, C.muted, lineHeight = 19)
    }
}

@Composable
private fun SecTitle(t: String) { T(t, 14, FontWeight.ExtraBold, C.head, Modifier.padding(bottom = 10.dp, top = 4.dp)) }

@Composable
private fun PlanCard(p: PlanDto, enabled: Boolean, onSubscribe: (PlanDto) -> Unit) {
    Column(Modifier.padding(bottom = 14.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(20.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                T(p.name, 15, FontWeight.ExtraBold, C.head)
                Spacer(Modifier.height(3.dp))
                T(durationLabel(p.duration_days), 11, FontWeight.Medium, C.muted)
            }
            T(if (p.price <= 0.0) tr("مجاناً", "Free") else "﷼${money(p.price)}", 18, FontWeight.ExtraBold, C.greenD)
        }
        if (p.features.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            p.features.forEach { f ->
                Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    T("✓", 12, FontWeight.Bold, C.green); Spacer(Modifier.width(7.dp)); T(f, 11, FontWeight.Normal, C.head)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (enabled) C.green else C.line)
            .then(if (enabled) Modifier.clickable { onSubscribe(p) } else Modifier).padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
            T(if (enabled) tr("اشترك الآن", "Subscribe now") else tr("غير متاح", "Unavailable"), 13, FontWeight.ExtraBold, if (enabled) Color.White else C.muted)
        }
    }
}

private fun durationLabel(d: Int): String = when (d) {
    30 -> tr("شهر واحد", "One month")
    90 -> tr("3 أشهر", "3 months")
    180 -> tr("6 أشهر", "6 months")
    365 -> tr("سنة كاملة", "Full year")
    else -> tr("$d يوم", "$d days")
}

@androidx.compose.runtime.Composable
fun SubscriptionGateScreen(sub: SubDto?, onRefresh: () -> Unit, onLogout: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(C.bg).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Box(Modifier.size(88.dp).clip(RoundedCornerShape(28.dp)).background(C.pillLive), contentAlignment = Alignment.Center) { T("\uD83D\uDD12", 40, FontWeight.Bold, C.greenD) }
        Spacer(Modifier.height(22.dp))
        val title: String; val body: String
        when (sub?.status) {
            "pending" -> { title = tr("اشتراكك قيد المراجعة \u23F3", "Your subscription is under review \u23F3"); body = tr("باقة ", "Package ") + (sub?.plan_name ?: "") + tr(" — بانتظار تفعيل الإدارة. لا يمكنك استخدام التطبيق حتى يُفعَّل اشتراكك.", " — awaiting admin activation. You can't use the app until your subscription is activated.") }
            "rejected" -> { title = tr("لم يُعتمد اشتراكك", "Your subscription wasn't approved"); body = tr("تواصل مع الدعم، أو سجّل من جديد واختر باقة.", "Contact support, or register again and choose a package.") }
            "expired" -> { title = tr("انتهى اشتراكك", "Your subscription has expired"); body = tr("بانتظار تفعيل الإدارة لتجديد باقتك.", "Awaiting admin activation of your package renewal.") }
            else -> { title = tr("لا يوجد اشتراك فعّال", "No active subscription"); body = tr("بانتظار تفعيل الإدارة لباقتك حتى تتمكن من استخدام التطبيق.", "Awaiting admin activation of your package so you can use the app.") }
        }
        T(title, 19, FontWeight.ExtraBold, C.head)
        Spacer(Modifier.height(10.dp))
        T(body, 13, FontWeight.Normal, C.muted, lineHeight = 22)
        Spacer(Modifier.height(26.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(C.green).clickable(onClick = onRefresh).padding(vertical = 14.dp), contentAlignment = Alignment.Center) { T(tr("تحديث حالة الاشتراك", "Refresh subscription status"), 14, FontWeight.ExtraBold, androidx.compose.ui.graphics.Color.White) }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).border(1.dp, C.line, RoundedCornerShape(15.dp)).clickable(onClick = onLogout).padding(vertical = 14.dp), contentAlignment = Alignment.Center) { T(tr("تسجيل الخروج", "Log out"), 14, FontWeight.ExtraBold, C.muted) }
    }
}
