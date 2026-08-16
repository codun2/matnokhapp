package com.matnokh.driver.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.driver.R
import com.matnokh.driver.net.CompanyAccountResp
import com.matnokh.driver.net.Net
import com.matnokh.driver.net.call

/** شاشة «شركتي» — حصراً للمندوب التابع لشركة: اسم الشركة + الرصيد الحالي + سجل السحوبات والوصولات. */
@Composable
fun CompanyScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val ctx = LocalContext.current
    var data by remember { mutableStateOf<CompanyAccountResp?>(null) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        data = call({ Net.api.companyAccount() }, toast)
        loading = false
    }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("شركتي", onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(6.dp))
            val d = data
            val company = d?.company
            when {
                loading -> T("جارٍ التحميل…", 13, FontWeight.Medium, C.muted, Modifier.padding(vertical = 30.dp))
                company == null -> T("لست تابعاً لأي شركة توصيل.", 13, FontWeight.Medium, C.muted, Modifier.padding(vertical = 30.dp))
                else -> {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(16.dp)) {
                        T("الشركة التابع لها", 11, FontWeight.Medium, C.muted)
                        Spacer(Modifier.height(4.dp))
                        T(company.name, 18, FontWeight.ExtraBold, C.head)
                        val phone = company.phone
                        if (!phone.isNullOrBlank()) { Spacer(Modifier.height(2.dp)); T(phone, 12, FontWeight.Medium, C.muted) }
                    }
                    Spacer(Modifier.height(12.dp))
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(16.dp)) {
                        T("رصيدك الحالي لدى الشركة", 11, FontWeight.Medium, C.muted)
                        Spacer(Modifier.height(4.dp))
                        T("﷼${(d?.balance ?: 0.0).toInt()}", 22, FontWeight.ExtraBold, C.greenD)
                        Spacer(Modifier.height(2.dp))
                        T("يُصفّى عند استلامك الدفعة من الشركة.", 10, FontWeight.Normal, C.muted)
                    }
                    Spacer(Modifier.height(16.dp))
                    T("سجلّ السحوبات والوصولات", 14, FontWeight.Bold, C.head)
                    Spacer(Modifier.height(8.dp))
                    val settlements = d?.settlements ?: emptyList()
                    if (settlements.isEmpty()) {
                        T("لا توجد عمليات سحب بعد.", 12, FontWeight.Medium, C.muted, Modifier.padding(vertical = 14.dp))
                    } else {
                        settlements.forEach { s ->
                            Column(Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(18.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(18.dp)).padding(14.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    T("﷼${s.amount.toInt()}", 16, FontWeight.ExtraBold, C.greenD, Modifier.weight(1f))
                                    T(s.settled_at ?: "", 11, FontWeight.Medium, C.muted)
                                }
                                Spacer(Modifier.height(4.dp))
                                val extra = s.reference?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""
                                T("${s.orders_count} طلب · ${s.method ?: "—"}$extra", 11, FontWeight.Medium, C.muted)
                                val proof = s.payment_proof
                                if (!proof.isNullOrBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(Modifier.clip(RoundedCornerShape(10.dp)).background(C.pillLive).clickable { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(proof))) } }.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Ic(R.drawable.ic_doc, 13.dp, C.greenD); Spacer(Modifier.width(5.dp)); T("عرض إشعار الدفع", 11, FontWeight.ExtraBold, C.greenD)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(110.dp))
        }
    }
}
