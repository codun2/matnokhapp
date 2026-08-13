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
                call({ Net.api.subscribe(SubscribeBody(p.id)) }, toast)?.let { toast(it.message ?: "تم إرسال الطلب"); load() }
                busy = false
            }
        }
    }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("باقات الاشتراك", onBack, onMenu)
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
                            if (u != null) runCatching { ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(u)).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) }.onFailure { toast("تعذّر فتح صفحة الدفع") }
                            busy = false
                        }
                    }.padding(vertical = 15.dp), contentAlignment = Alignment.Center) {
                        T(if (busy) "جارٍ فتح صفحة الدفع…" else "ادفع الاشتراك · ﷼${money(sub?.price ?: 0.0)}", 15, FontWeight.ExtraBold, androidx.compose.ui.graphics.Color.White)
                    }
                    Spacer(Modifier.height(6.dp))
                    T("بعد إتمام الدفع، عُد واضغط «تحديث حالة الاشتراك».", 11, FontWeight.Normal, C.muted)
                }
                Spacer(Modifier.height(18.dp))
                val list = plans ?: emptyList()
                val regular = list.filter { it.type != "marketing" }
                val marketing = list.filter { it.type == "marketing" }
                if (regular.isEmpty() && marketing.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) { T("لا توجد باقات متاحة حالياً", 13, FontWeight.Bold, C.muted) }
                }
                if (regular.isNotEmpty()) {
                    SecTitle("الباقات العادية")
                    regular.forEach { PlanCard(it, canSubscribe && !busy, doSubscribe) }
                }
                if (marketing.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    SecTitle("الباقات التسويقية 📣")
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
        "active" -> Triple("اشتراكك فعّال ✓", "باقة ${sub?.plan_name ?: ""} — متبقٍ ${sub?.days_left ?: 0} يوم" + (sub?.ends_at?.let { " (ينتهي $it)" } ?: ""), C.greenD)
        "pending" -> Triple("طلبك قيد المراجعة ⏳", "باقة ${sub?.plan_name ?: ""} — بانتظار موافقة الإدارة، سنبلّغك فور التفعيل", Color(0xFFB45309))
        "rejected" -> Triple("لم يُعتمد طلبك السابق", "اختر باقة من الأسفل لإعادة الطلب", C.terra)
        "expired" -> Triple("انتهى اشتراكك", "اختر باقة لتجديد اشتراكك", C.terra)
        else -> Triple("اختر باقتك للبدء", "كل الباقات تمنحك طلبات ومنتجات بلا حدود — الفرق في المدة والنوع فقط", C.head)
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
            T(if (p.price <= 0.0) "مجاناً" else "﷼${money(p.price)}", 18, FontWeight.ExtraBold, C.greenD)
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
            T(if (enabled) "اشترك الآن" else "غير متاح", 13, FontWeight.ExtraBold, if (enabled) Color.White else C.muted)
        }
    }
}

private fun durationLabel(d: Int): String = when (d) {
    30 -> "شهر واحد"
    90 -> "3 أشهر"
    180 -> "6 أشهر"
    365 -> "سنة كاملة"
    else -> "$d يوم"
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
            "pending" -> { title = "اشتراكك قيد المراجعة \u23F3"; body = "باقة " + (sub?.plan_name ?: "") + " — بانتظار تفعيل الإدارة. لا يمكنك استخدام التطبيق حتى يُفعَّل اشتراكك." }
            "rejected" -> { title = "لم يُعتمد اشتراكك"; body = "تواصل مع الدعم، أو سجّل من جديد واختر باقة." }
            "expired" -> { title = "انتهى اشتراكك"; body = "بانتظار تفعيل الإدارة لتجديد باقتك." }
            else -> { title = "لا يوجد اشتراك فعّال"; body = "بانتظار تفعيل الإدارة لباقتك حتى تتمكن من استخدام التطبيق." }
        }
        T(title, 19, FontWeight.ExtraBold, C.head)
        Spacer(Modifier.height(10.dp))
        T(body, 13, FontWeight.Normal, C.muted, lineHeight = 22)
        Spacer(Modifier.height(26.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(C.green).clickable(onClick = onRefresh).padding(vertical = 14.dp), contentAlignment = Alignment.Center) { T("تحديث حالة الاشتراك", 14, FontWeight.ExtraBold, androidx.compose.ui.graphics.Color.White) }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).border(1.dp, C.line, RoundedCornerShape(15.dp)).clickable(onClick = onLogout).padding(vertical = 14.dp), contentAlignment = Alignment.Center) { T("تسجيل الخروج", 14, FontWeight.ExtraBold, C.muted) }
    }
}
