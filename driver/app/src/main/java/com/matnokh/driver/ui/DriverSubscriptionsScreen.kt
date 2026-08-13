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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.driver.net.DrvSub
import com.matnokh.driver.net.Net
import com.matnokh.driver.net.PaySubBody
import com.matnokh.driver.net.call
import kotlinx.coroutines.launch

private fun subStatusLabel(s: String): String = when (s) {
    "active" -> "فعّال ✓"
    "pending" -> "بانتظار الدفع/الموافقة"
    "suspended" -> "موقوف مؤقتاً"
    "rejected" -> "مرفوض"
    "expired" -> "منتهٍ"
    else -> s
}

@Composable
fun DriverSubscriptionsScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var subs by remember { mutableStateOf<List<DrvSub>?>(null) }
    var payingId by remember { mutableStateOf<Int?>(null) }
    suspend fun load() { call({ Net.api.driverSubscriptions() }, toast)?.let { subs = it.subscriptions } }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("اشتراكاتي", onBack, onMenu)
        val list = subs
        when {
            list == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
            list.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { T("لا توجد اشتراكات — سجّل بباقة أولاً", 13, FontWeight.Bold, C.muted) }
            else -> LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)) {
                items(list) { s ->
                    Column(Modifier.padding(horizontal = 22.dp).padding(bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(18.dp)).padding(15.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                T(s.service ?: "خدمة", 14, FontWeight.Bold, C.head)
                                Spacer(Modifier.height(3.dp))
                                T(subStatusLabel(s.status) + (s.days_left?.let { " · متبقٍ $it يوم" } ?: ""), 11, FontWeight.Medium, C.muted)
                            }
                            T(if (s.price <= 0.0) "مجاناً" else "﷼${s.price.toInt()}", 14, FontWeight.ExtraBold, C.greenD)
                        }
                        if (s.status == "pending" && s.price > 0.0) {
                            Spacer(Modifier.height(12.dp))
                            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(C.green).clickable(enabled = payingId == null) {
                                payingId = s.id
                                scope.launch {
                                    val r = call({ Net.api.driverPaySub(PaySubBody(s.id)) }, toast)
                                    val u = r?.payment_url
                                    if (u != null) runCatching { ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(u)).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) }.onFailure { toast("تعذّر فتح صفحة الدفع") }
                                    payingId = null
                                }
                            }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                T(if (payingId == s.id) "جارٍ فتح الدفع…" else "ادفع الآن · ﷼${s.price.toInt()}", 13, FontWeight.ExtraBold, Color.White)
                            }
                            Spacer(Modifier.height(5.dp))
                            T("بعد الدفع عُد وحدّث الشاشة.", 10, FontWeight.Normal, C.muted)
                        }
                    }
                }
            }
        }
    }
}
