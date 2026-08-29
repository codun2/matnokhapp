package com.matnokh.driver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.matnokh.driver.net.DrvSub
import com.matnokh.driver.net.ManualPayBody
import com.matnokh.driver.net.Net
import com.matnokh.driver.net.PayInfoResp
import com.matnokh.driver.net.PaySubBody
import com.matnokh.driver.net.call
import kotlinx.coroutines.launch

private fun subStatusLabel(s: String): String = when (s) {
    "active" -> tr("فعّال ✓", "Active ✓")
    "pending" -> tr("بانتظار الدفع/الموافقة", "Awaiting payment/approval")
    "suspended" -> tr("موقوف مؤقتاً", "Temporarily suspended")
    "rejected" -> tr("مرفوض", "Rejected")
    "expired" -> tr("منتهٍ", "Expired")
    else -> s
}

@Composable
private fun PayOption(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(13.dp))
            .border(1.dp, C.line, RoundedCornerShape(13.dp)).clickable { onClick() }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            T(title, 14, FontWeight.Bold, C.head)
            if (subtitle.isNotBlank()) { Spacer(Modifier.height(2.dp)); T(subtitle, 11, FontWeight.Medium, C.muted) }
        }
    }
}

@Composable
fun DriverSubscriptionsScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val clipboard = LocalClipboardManager.current
    var subs by remember { mutableStateOf<List<DrvSub>?>(null) }
    var payInfo by remember { mutableStateOf<PayInfoResp?>(null) }
    var chooserFor by remember { mutableStateOf<DrvSub?>(null) }
    var bankFor by remember { mutableStateOf<DrvSub?>(null) }
    var busyId by remember { mutableStateOf<Int?>(null) }

    suspend fun load() { call({ Net.api.driverSubscriptions() }, toast)?.let { subs = it.subscriptions } }
    LaunchedEffect(Unit) { load(); payInfo = call({ Net.api.driverPayInfo() }, toast) }

    fun payTap(s: DrvSub) {
        busyId = s.id
        scope.launch {
            val r = call({ Net.api.driverPaySub(PaySubBody(s.id)) }, toast)
            val u = r?.payment_url
            if (u != null) runCatching {
                ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(u)).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
            }.onFailure { toast(tr("تعذّر فتح صفحة الدفع", "Couldn't open the payment page")) }
            busyId = null
        }
    }
    fun payCash(s: DrvSub) {
        busyId = s.id
        scope.launch {
            val r = call({ Net.api.driverManualPay(ManualPayBody(s.id, "cash")) }, toast)
            if (r != null) { toast(r.message ?: tr("تم إرسال الطلب", "Request sent")); load() }
            busyId = null
        }
    }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("اشتراكاتي", "My subscriptions"), onBack, onMenu)
        val list = subs
        when {
            list == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
            list.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { T(tr("لا توجد اشتراكات — سجّل بباقة أولاً", "No subscriptions — register with a package first"), 13, FontWeight.Bold, C.muted) }
            else -> LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)) {
                items(list) { s ->
                    Column(Modifier.padding(horizontal = 22.dp).padding(bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(18.dp)).padding(15.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                T(s.service ?: tr("خدمة", "Service"), 14, FontWeight.Bold, C.head)
                                Spacer(Modifier.height(3.dp))
                                T(subStatusLabel(s.status) + (s.days_left?.let { tr(" · متبقٍ $it يوم", " · $it days left") } ?: ""), 11, FontWeight.Medium, C.muted)
                            }
                            T(if (s.price <= 0.0) tr("مجاناً", "Free") else "﷼${s.price.toInt()}", 14, FontWeight.ExtraBold, C.greenD)
                        }
                        if (s.status == "pending" && s.price > 0.0) {
                            Spacer(Modifier.height(12.dp))
                            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(C.green).clickable(enabled = busyId == null) { chooserFor = s }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                T(if (busyId == s.id) tr("جارٍ المعالجة…", "Processing…") else tr("ادفع الآن · ﷼${s.price.toInt()}", "Pay now · ﷼${s.price.toInt()}"), 13, FontWeight.ExtraBold, Color.White)
                            }
                            Spacer(Modifier.height(5.dp))
                            T(tr("بعد الدفع عُد وحدّث الشاشة.", "After paying, come back and refresh the screen."), 10, FontWeight.Normal, C.muted)
                        }
                    }
                }
            }
        }
    }

    // ==== اختيار طريقة الدفع ====
    chooserFor?.let { s ->
        Dialog(onDismissRequest = { chooserFor = null }) {
            Box(Modifier.fillMaxWidth().padding(24.dp)) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(C.card).padding(20.dp)) {
                    T(tr("اختر طريقة الدفع", "Choose a payment method"), 17, FontWeight.Black, C.head)
                    Spacer(Modifier.height(4.dp))
                    T(tr("﷼${s.price.toInt()} · ${s.service ?: "خدمة"}", "﷼${s.price.toInt()} · ${s.service ?: "Service"}"), 12, FontWeight.Medium, C.muted)
                    val eOn = payInfo?.tap_enabled == true
                    val bOn = payInfo?.bank_enabled != false
                    val cOn = payInfo?.cash_enabled != false
                    if (eOn) { PayOption(tr("دفع إلكتروني (بطاقة)", "Online payment (card)"), tr("تفعيل فوري بعد الدفع", "Instant activation after payment")) { chooserFor = null; payTap(s) } }
                    if (bOn) { PayOption(tr("تحويل بنكي", "Bank transfer"), tr("حوّل على الآيبان وارفع صورة الإشعار", "Transfer to the IBAN and upload the notice screenshot")) { bankFor = s; chooserFor = null } }
                    if (cOn) { PayOption(tr("كاش", "Cash"), tr("تفعيل خلال 24 ساعة بعد التأكيد", "Activates within 24 hours after confirmation")) { chooserFor = null; payCash(s) } }
                    if (!eOn && !bOn && !cOn) { Spacer(Modifier.height(12.dp)); T(tr("لا توجد طريقة دفع متاحة حالياً — تواصل مع الإدارة", "No payment method available currently — contact admin"), 12, FontWeight.Medium, C.muted) }
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().clickable { chooserFor = null }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        T(tr("إلغاء", "Cancel"), 13, FontWeight.Bold, C.muted)
                    }
                }
            }
        }
    }

    // ==== حوار التحويل البنكي ====
    bankFor?.let { s ->
        var receipt by remember { mutableStateOf<String?>(null) }
        var sending by remember { mutableStateOf(false) }
        Dialog(onDismissRequest = { bankFor = null }) {
            Box(Modifier.fillMaxWidth().padding(20.dp)) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(C.card).padding(20.dp)) {
                    T(tr("التحويل البنكي", "Bank transfer"), 17, FontWeight.Black, C.head)
                    Spacer(Modifier.height(10.dp))
                    val iban = payInfo?.bank_iban ?: ""
                    if (iban.isBlank()) {
                        T(tr("لم تُضف بيانات الحساب البنكي بعد. تواصل مع الإدارة.", "Bank account details haven't been added yet. Contact admin."), 12, FontWeight.Medium, C.muted)
                    } else {
                        if (!payInfo?.bank_name.isNullOrBlank()) {
                            T(tr("البنك: ${payInfo?.bank_name}", "Bank: ${payInfo?.bank_name}"), 13, FontWeight.Bold, C.head)
                            Spacer(Modifier.height(8.dp))
                        }
                        T(tr("الآيبان (IBAN)", "IBAN"), 11, FontWeight.Medium, C.muted)
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(C.bg).border(1.dp, C.line, RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            T(iban, 13, FontWeight.ExtraBold, C.head, modifier = Modifier.weight(1f))
                            Box(Modifier.clip(RoundedCornerShape(9.dp)).background(C.green).clickable { clipboard.setText(AnnotatedString(iban)); toast(tr("تم نسخ الآيبان", "IBAN copied")) }.padding(horizontal = 14.dp, vertical = 7.dp)) {
                                T(tr("نسخ", "Copy"), 12, FontWeight.ExtraBold, Color.White)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        T(tr("بعد التحويل ارفع صورة إشعار العملية:", "After transferring, upload a screenshot of the transaction notice:"), 12, FontWeight.Medium, C.head)
                        Spacer(Modifier.height(6.dp))
                        DocUpload(tr("إشعار التحويل", "Transfer notice"), true, { receipt = it }, toast)
                        Spacer(Modifier.height(16.dp))
                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
                                .background(if (receipt != null && !sending) C.green else C.line)
                                .clickable(enabled = receipt != null && !sending) {
                                    sending = true
                                    scope.launch {
                                        val r = call({ Net.api.driverManualPay(ManualPayBody(s.id, "bank", receipt)) }, toast)
                                        sending = false
                                        if (r != null) { toast(r.message ?: tr("تم إرسال الطلب للإدارة", "The request was sent to admin")); load(); bankFor = null }
                                    }
                                }.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) { T(if (sending) tr("جارٍ الإرسال…", "Sending…") else tr("إرسال للإدارة", "Send to admin"), 13, FontWeight.ExtraBold, Color.White) }
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth().clickable { bankFor = null }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        T(tr("إغلاق", "Close"), 13, FontWeight.Bold, C.muted)
                    }
                }
            }
        }
    }
}
