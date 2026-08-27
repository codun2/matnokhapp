package com.matnokh.tajer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.PayoutAccountDto
import com.matnokh.tajer.net.RequestWithdrawBody
import com.matnokh.tajer.net.WalletResp
import com.matnokh.tajer.net.WalletSummaryResp
import com.matnokh.tajer.net.WithdrawRow
import com.matnokh.tajer.net.call
import kotlinx.coroutines.launch

@Composable
fun WalletScreen(onBack: () -> Unit, onMenu: () -> Unit, onPayments: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var wallet by remember { mutableStateOf<WalletResp?>(null) }
    var summary by remember { mutableStateOf<WalletSummaryResp?>(null) }
    var accounts by remember { mutableStateOf<List<PayoutAccountDto>>(emptyList()) }
    var withdraws by remember { mutableStateOf<List<WithdrawRow>>(emptyList()) }
    var showWithdraw by remember { mutableStateOf(false) }

    suspend fun loadAll() {
        call({ Net.api.wallet() }, toast)?.let { wallet = it }
        call({ Net.api.walletSummary() }, toast)?.let { summary = it }
        call({ Net.api.payoutAccounts() }, toast)?.let { accounts = it.accounts }
        call({ Net.api.withdraws() }, toast)?.let { withdraws = it.withdraws }
    }
    LaunchedEffect(Unit) { loadAll() }
    val w = wallet
    val s = summary

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("المحفظة", onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // hero — الرصيد المتاح للسحب
            Box(
                Modifier.padding(start = 22.dp, end = 22.dp).fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Grad.green).padding(20.dp)
            ) {
                Column {
                    T("الرصيد المتاح للسحب", 12, FontWeight.Normal, Color.White.copy(alpha = .85f))
                    Spacer(Modifier.height(4.dp))
                    T("﷼" + money(s?.available ?: w?.balance ?: 0.0), 32, FontWeight.Black, Color.White)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        HeroStat("المتراكم", s?.accrued ?: 0.0, Modifier.weight(1f))
                        HeroStat("المسحوب", s?.withdrawn ?: 0.0, Modifier.weight(1f))
                        HeroStat("قيد الطلب", s?.pending ?: 0.0, Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Box(Modifier.padding(horizontal = 22.dp)) {
                WideButton("طلب سحب", R.drawable.ic_card) {
                    if (accounts.isEmpty()) { toast("أضِف حساب استلام أولاً"); onPayments() } else showWithdraw = true
                }
            }

            // حساب استلام الأرباح
            OCard(Modifier.padding(start = 22.dp, end = 22.dp, top = 14.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onPayments)) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Grad.green), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_card, 22.dp, Color.White) }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        T("حساب استلام الأرباح", 13, FontWeight.Bold, C.head)
                        Spacer(Modifier.height(2.dp))
                        T(if (accounts.isEmpty()) "أضِف آيبان بنكي أو STC Pay لاستلام تحويلاتك" else "مسجّل: " + accounts.size + " حساب — يحوّل إليه مطنوخ", 10, FontWeight.Normal, C.muted, lineHeight = 16)
                    }
                    Ic(R.drawable.ic_back, 17.dp, Color(0xFFC3C9C0))
                }
            }

            // سجل طلبات السحب
            if (withdraws.isNotEmpty()) {
                SecTitle("سجل طلبات السحب")
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(vertical = 4.dp)) {
                    withdraws.forEachIndexed { i, x ->
                        WithdrawRowUi(x, last = i == withdraws.lastIndex)
                    }
                }
            }

            SecTitle("سجل المعاملات")
            val txs = w?.transactions ?: emptyList()
            if (w != null && txs.isEmpty()) {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(20.dp)) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { T("لا توجد معاملات بعد", 12, FontWeight.Normal, C.muted) }
                }
            } else {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(vertical = 4.dp)) {
                    txs.forEachIndexed { i, t ->
                        Tx(R.drawable.ic_box, C.pillLive, C.greenD, t.title, t.dt ?: "", "+ ﷼" + money(t.amount), C.greenD, last = i == txs.lastIndex)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showWithdraw) WithdrawDialog(
        available = s?.available ?: 0.0,
        min = s?.min_withdraw ?: 0.0,
        accounts = accounts,
        onClose = { showWithdraw = false },
        onSubmit = { amt, acc -> scope.launch { call({ Net.api.requestWithdraw(RequestWithdrawBody(amt, acc)) }, toast)?.let { toast(it.message ?: "تم إرسال الطلب"); showWithdraw = false; loadAll() } } }
    )
}

@Composable
private fun HeroStat(label: String, value: Double, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(13.dp)).background(Color.White.copy(alpha = .16f)).padding(vertical = 9.dp, horizontal = 8.dp)) {
        T(label, 9, FontWeight.Normal, Color.White.copy(alpha = .8f))
        Spacer(Modifier.height(2.dp))
        T("﷼" + money(value), 12, FontWeight.Bold, Color.White, maxLines = 1)
    }
}

private fun wdStatusAr(s: String) = when (s) { "pending" -> "بانتظار المعالجة"; "recorded" -> "مسجّل"; "paid" -> "مدفوع"; "rejected" -> "مرفوض"; else -> s }
private fun wdKind(s: String) = when (s) { "paid", "recorded" -> PillKind.Ok; "rejected" -> PillKind.Rj; else -> PillKind.Wait }

@Composable
private fun WithdrawRowUi(x: WithdrawRow, last: Boolean) {
    Column {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(C.card2), contentAlignment = Alignment.Center) {
                Ic(R.drawable.ic_card, 17.dp, C.greenD)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                T("سحب " + (if (x.method == "stcpay") "STC Pay" else "بنكي") + (x.account?.let { " · $it" } ?: ""), 12, FontWeight.Bold, C.head, maxLines = 1)
                Spacer(Modifier.height(1.dp))
                T(x.dt ?: "", 10, FontWeight.Medium, C.muted, lineHeight = 16)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                T("﷼" + money(x.amount), 13, FontWeight.Black, C.head, maxLines = 1)
                Spacer(Modifier.height(3.dp))
                StatusPill(wdStatusAr(x.status), wdKind(x.status))
            }
        }
        if (!last) androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 16.dp)) {
            drawLine(Color(0xFFF0ECE3), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 1f)
        }
    }
}

@Composable
private fun WithdrawDialog(available: Double, min: Double, accounts: List<PayoutAccountDto>, onClose: () -> Unit, onSubmit: (Double, Int) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var sel by remember { mutableStateOf(accounts.firstOrNull { it.is_default }?.id ?: accounts.firstOrNull()?.id) }
    androidx.compose.ui.window.Dialog(onDismissRequest = onClose) {
        Column(Modifier.clip(RoundedCornerShape(22.dp)).background(C.bg).padding(20.dp).fillMaxWidth()) {
            T("طلب سحب", 16, FontWeight.ExtraBold, C.head)
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(C.pillLive).padding(12.dp)) {
                T("الرصيد المتاح: ﷼" + money(available) + (if (min > 0) "   ·   أقل مبلغ ﷼" + money(min) else ""), 11, FontWeight.Bold, C.greenD)
            }
            Spacer(Modifier.height(12.dp))
            FieldLabel("المبلغ", required = true)
            FinField(amount, { v -> amount = v.filter { it.isDigit() || it == '.' } }, placeholder = "﷼", keyboard = KeyboardType.Number, align = TextAlign.Left)
            Spacer(Modifier.height(12.dp))
            FieldLabel("طريقة السحب")
            accounts.forEach { a ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp)).background(if (sel == a.id) C.pillLive else C.card2).clickable { sel = a.id }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(16.dp).clip(CircleShape).background(if (sel == a.id) C.green else Color(0xFFCED4CC)))
                    Spacer(Modifier.width(10.dp))
                    T((if (a.method == "stcpay") "STC Pay" else (a.bank_name ?: "بنكي")) + (a.account_number?.let { " · $it" } ?: ""), 12, FontWeight.Bold, C.head, maxLines = 1)
                }
            }
            Spacer(Modifier.height(16.dp))
            WideButton("إرسال الطلب", R.drawable.ic_check) {
                val amt = amount.toDoubleOrNull() ?: 0.0
                val acc = sel
                if (amt <= 0.0 || acc == null) return@WideButton
                onSubmit(amt, acc)
            }
            Spacer(Modifier.height(8.dp))
            WideButton("إلغاء", ghost = true) { onClose() }
        }
    }
}

@Composable
private fun Tx(iconId: Int, iconBg: Color, iconColor: Color, title: String, sub: String, amount: String, amountColor: Color, last: Boolean = false) {
    Column {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(iconBg), contentAlignment = Alignment.Center) {
                Ic(iconId, 17.dp, iconColor)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                T(title, 12, FontWeight.Bold, C.head, maxLines = 1)
                Spacer(Modifier.height(1.dp))
                T(sub, 10, FontWeight.Medium, C.muted, lineHeight = 16)
            }
            Spacer(Modifier.width(8.dp))
            T(amount, 13, FontWeight.Black, amountColor, maxLines = 1)
        }
        if (!last) androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 16.dp)) {
            drawLine(Color(0xFFF0ECE3), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 1f)
        }
    }
}
