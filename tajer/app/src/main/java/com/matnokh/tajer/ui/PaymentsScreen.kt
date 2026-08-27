package com.matnokh.tajer.ui

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.tajer.R
import com.matnokh.tajer.net.AddPayoutBody
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.PayoutAccountDto
import com.matnokh.tajer.net.call
import kotlinx.coroutines.launch

// شاشة «حساب استلام الأرباح» — الحساب البنكي/STC Pay الذي يحوِّل إليه مطنوخ عند طلب السحب.
@Composable
fun PaymentsScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var accounts by remember { mutableStateOf<List<PayoutAccountDto>?>(null) }
    var adding by remember { mutableStateOf(false) }

    suspend fun load() { call({ Net.api.payoutAccounts() }, toast)?.let { accounts = it.accounts } }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("حساب استلام الأرباح", onBack, onMenu)
        val list = accounts
        if (list == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }; return@Column }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)) {
            item {
                Box(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEEF4EF)).padding(horizontal = 14.dp, vertical = 12.dp)) {
                    T("أضِف حساب استلام أرباحك (آيبان بنكي أو STC Pay). عند طلب السحب من محفظتك نحوّل المبلغ إلى هذا الحساب.", 11, FontWeight.Medium, C.greenD, lineHeight = 19)
                }
            }
            items(list) { a ->
                PayoutCard(a, onDelete = { scope.launch { call({ Net.api.deletePayoutAccount(a.id) }, toast)?.let { toast(it.message ?: "حُذفت"); load() } } })
            }
            if (list.isEmpty()) item {
                Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { T("لم تُضِف حساب استلام بعد", 12, FontWeight.Medium, C.muted) }
            }
            item {
                Spacer(Modifier.height(6.dp))
                Box(Modifier.padding(horizontal = 22.dp)) {
                    WideButton("إضافة حساب استلام", R.drawable.ic_check) { adding = true }
                }
            }
        }
    }

    if (adding) AddPayoutDialog(
        isFirst = (accounts?.isEmpty() ?: true),
        onClose = { adding = false },
        onSave = { body -> scope.launch { call({ Net.api.addPayoutAccount(body) }, toast)?.let { toast(it.message ?: "أُضيف"); adding = false; load() } } }
    )
}

@Composable
private fun PayoutCard(a: PayoutAccountDto, onDelete: () -> Unit) {
    OCard(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(C.pillLive), contentAlignment = Alignment.Center) {
                Ic(R.drawable.ic_card, 22.dp, C.greenD)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    T(if (a.method == "stcpay") "STC Pay" else (a.bank_name ?: "حساب بنكي"), 13, FontWeight.Bold, C.head)
                    if (a.is_default) { Spacer(Modifier.width(6.dp)); StatusPill("افتراضي", PillKind.Ok) }
                }
                Spacer(Modifier.height(2.dp))
                T((a.account_name ?: "") + (a.account_number?.let { " · $it" } ?: ""), 10, FontWeight.Medium, C.muted)
            }
            Box(Modifier.clip(RoundedCornerShape(12.dp)).background(C.redBg).clickable(onClick = onDelete).padding(horizontal = 12.dp, vertical = 8.dp)) {
                T("حذف", 11, FontWeight.ExtraBold, C.redText)
            }
        }
    }
}

@Composable
private fun AddPayoutDialog(isFirst: Boolean, onClose: () -> Unit, onSave: (AddPayoutBody) -> Unit) {
    var method by remember { mutableStateOf("bank") }
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var def by remember { mutableStateOf(isFirst) }
    androidx.compose.ui.window.Dialog(onDismissRequest = onClose) {
        Column(Modifier.clip(RoundedCornerShape(22.dp)).background(C.bg).padding(20.dp).fillMaxWidth()) {
            T("إضافة حساب استلام", 16, FontWeight.ExtraBold, C.head)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(C.card2).padding(4.dp)) {
                SegBtn("حساب بنكي", method == "bank", Modifier.weight(1f)) { method = "bank" }
                SegBtn("STC Pay", method == "stcpay", Modifier.weight(1f)) { method = "stcpay" }
            }
            Spacer(Modifier.height(12.dp))
            FieldLabel("اسم صاحب الحساب", required = true)
            FinField(name, { name = it }, placeholder = "الاسم الكامل")
            Spacer(Modifier.height(10.dp))
            FieldLabel(if (method == "stcpay") "رقم STC Pay" else "رقم الآيبان (IBAN)", required = true)
            FinField(number, { number = it }, placeholder = if (method == "stcpay") "05xxxxxxxx" else "SA...", align = TextAlign.Left)
            if (method == "bank") {
                Spacer(Modifier.height(10.dp))
                FieldLabel("اسم البنك")
                FinField(bank, { bank = it }, placeholder = "مثال: الراجحي")
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Sw(def) { def = !def }
                Spacer(Modifier.width(10.dp))
                T("اجعله الحساب الافتراضي", 12, FontWeight.Bold, C.head)
            }
            Spacer(Modifier.height(16.dp))
            WideButton("حفظ", R.drawable.ic_check) {
                if (name.isBlank() || number.isBlank()) return@WideButton
                onSave(AddPayoutBody(method, name.trim(), number.trim(), if (method == "bank") bank.trim().ifBlank { null } else null, def))
            }
            Spacer(Modifier.height(8.dp))
            WideButton("إلغاء", ghost = true) { onClose() }
        }
    }
}

@Composable
private fun SegBtn(label: String, on: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(11.dp)).background(if (on) Color.White else Color.Transparent).clickable(onClick = onClick).padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
        T(label, 12, if (on) FontWeight.ExtraBold else FontWeight.Medium, if (on) C.greenD else C.muted)
    }
}
