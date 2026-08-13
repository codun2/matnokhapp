package com.matnokh.tajer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.tajer.R
import com.matnokh.tajer.net.LinkPayBody
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.PayProvider
import com.matnokh.tajer.net.TogglePayBody
import com.matnokh.tajer.net.call
import kotlinx.coroutines.launch

@Composable
fun PaymentsScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var providers by remember { mutableStateOf<List<PayProvider>?>(null) }
    var openKey by remember { mutableStateOf<String?>(null) }

    suspend fun load() { call({ Net.api.payments() }, toast)?.let { providers = it.providers } }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("بوابات الدفع", onBack, onMenu)
        val list = providers
        if (list == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }; return@Column }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)) {
            item {
                Box(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEEF4EF)).padding(horizontal = 14.dp, vertical = 12.dp)) {
                    T("اربط حسابك في بوابة الدفع لاستقبال مدفوعات الزبائن مباشرة. البوابات المتاحة يحدّدها مشرف النظام.", 11, FontWeight.Medium, C.greenD, lineHeight = 19)
                }
            }
            items(list) { p ->
                ProviderCard(p, expanded = openKey == p.key,
                    onExpand = { openKey = if (openKey == p.key) null else p.key },
                    onLink = { creds -> scope.launch { call({ Net.api.linkPayment(LinkPayBody(p.key, creds)) }, toast)?.let { toast(it.message ?: "تم الربط"); openKey = null; load() } } },
                    onToggle = { en -> scope.launch { call({ Net.api.togglePayment(TogglePayBody(p.key, en)) }, toast)?.let { toast(it.message ?: ""); load() } } },
                    onUnlink = { scope.launch { call({ Net.api.unlinkPayment(p.key) }, toast)?.let { toast(it.message ?: "أُلغي الربط"); load() } } })
            }
        }
    }
}

@Composable
private fun ProviderCard(p: PayProvider, expanded: Boolean, onExpand: () -> Unit, onLink: (Map<String, String>) -> Unit, onToggle: (Boolean) -> Unit, onUnlink: () -> Unit) {
    val creds = remember(p.key) { mutableStateMapOf<String, String>() }
    OCard(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onExpand)) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(if (p.linked) C.pillLive else C.card2), contentAlignment = Alignment.Center) {
                Ic(R.drawable.ic_card, 22.dp, if (p.linked) C.greenD else C.muted)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                T(p.name, 13, FontWeight.Bold, C.head)
                Spacer(Modifier.height(2.dp))
                T(if (p.linked) (if (p.enabled) "مربوطة ومفعّلة" else "مربوطة — معطّلة") else "غير مربوطة", 10, FontWeight.Bold, if (p.linked) C.greenD else C.muted)
            }
            if (p.linked) Sw(p.enabled) { onToggle(!p.enabled) }
            else StatusPill("ربط", PillKind.Wait)
        }
        AnimatedVisibility(expanded) {
            Column {
                Spacer(Modifier.height(10.dp))
                if (p.fields.isEmpty()) {
                    T("لا تحتاج هذه الطريقة بيانات — فعّلها فقط.", 11, FontWeight.Medium, C.muted)
                } else {
                    p.fields.forEach { f ->
                        FieldLabel(f.label, required = true)
                        val masked = p.masked?.get(f.key)
                        FinField(creds[f.key] ?: "", { creds[f.key] = it },
                            placeholder = if (!masked.blankOrNull()) "الحالي: $masked" else if (f.secret) "••••••••" else "",
                            keyboard = if (f.secret) KeyboardType.Password else KeyboardType.Text)
                    }
                }
                Spacer(Modifier.height(12.dp))
                WideButton(if (p.linked) "تحديث الربط" else "ربط الحساب", R.drawable.ic_check) { onLink(creds.toMap()) }
                if (p.linked) {
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(C.redBg).clickable(onClick = onUnlink).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        T("إلغاء الربط", 12, FontWeight.ExtraBold, C.redText)
                    }
                }
            }
        }
    }
}

private fun String?.blankOrNull() = this == null || this.isBlank()
