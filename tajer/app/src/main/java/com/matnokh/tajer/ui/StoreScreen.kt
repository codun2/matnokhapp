package com.matnokh.tajer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matnokh.tajer.R
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.StoreData
import com.matnokh.tajer.net.StoreUpdate
import com.matnokh.tajer.net.call
import kotlinx.coroutines.launch

@Composable
fun StoreScreen(
    onBack: () -> Unit, onMenu: () -> Unit,
    onBranches: () -> Unit, onSections: () -> Unit, onDocuments: () -> Unit, onStoreData: () -> Unit, onLogout: () -> Unit, toast: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var store by remember { mutableStateOf<StoreData?>(null) }

    suspend fun load() { call({ Net.api.store() }, toast)?.let { store = it.store; needsPrep = it.store.prep_mode } }
    LaunchedEffect(Unit) { load() }

    fun patch(u: StoreUpdate, done: (StoreData) -> Unit = {}) = scope.launch {
        call({ Net.api.updateStore(u) }, toast)?.let { store = it.store; done(it.store) }
    }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("إعدادات المتجر", onBack, onMenu)
        val s = store
        if (s == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
            return@Column
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // بطاقة المتجر
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StoreLogoBox(56.dp, 19.dp, 24)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        T(s.store_name ?: "متجري", 16, FontWeight.Bold, C.head)
                        T(listOfNotNull(s.owner_name, s.city).joinToString(" · "), 11, FontWeight.Normal, C.muted)
                    }
                    StatusChip(s.status)
                }
            }
            Spacer(Modifier.height(14.dp))
            ToggleCard(R.drawable.ic_shop, C.pillLive, C.greenD, "حالة المتجر الآن",
                if (s.is_open) "متاح — يستقبل الطلبات" else "مغلق مؤقتاً — لا يظهر للزبائن", s.is_open) {
                patch(StoreUpdate(is_open = !s.is_open)) { toast(if (it.is_open) "متجرك متاح الآن ✓" else "أُغلق المتجر مؤقتاً") }
            }
            Spacer(Modifier.height(14.dp))
            ToggleCard(R.drawable.ic_clock, Color(0xFFF6ECE4), Color(0xFFB5794F), "طلباتي تحتاج قبول وتجهيز",
                if (s.prep_mode) "مناسب للمطاعم والمخابز — تقبل الطلب وتجهّزه قبل وصول المندوب"
                else "مناسب للصيدليات والبقالات — تُقبل الطلبات تلقائياً وتُبثّ للمناديب فوراً", s.prep_mode) {
                patch(StoreUpdate(prep_mode = !s.prep_mode)) { needsPrep = it.prep_mode; toast(if (it.prep_mode) "فُعّل وضع التجهيز" else "فُعّل القبول التلقائي") }
            }
            Spacer(Modifier.height(14.dp))
            PList {
                PRow(Grad.green, R.drawable.ic_shop, "بيانات المتجر", "الاسم · الشعار · العنوان · المدينة", onClick = onStoreData)
                PRow(Grad.blue, R.drawable.ic_pin, "الفروع", "${s.branches_count} فروع", onClick = onBranches)
                PRow(Grad.terra, R.drawable.ic_list, "أقسام المتجر", "${s.sections_count} أقسام", onClick = onSections)
                PRow(Grad.sand, R.drawable.ic_doc, "الوثائق", "سجل تجاري · الهوية · الحساب البنكي", last = true, onClick = onDocuments)
            }
            Spacer(Modifier.height(14.dp))
            PList {
                PRow(Grad.blue, R.drawable.ic_msg, "الدعم الفني", null) { toast("الدعم الفني — على مدار الساعة") }
                PRowLogout(onLogout)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusChip(status: String?) {
    val (label, kind) = when (status) {
        "approved" -> "معتمد" to PillKind.Live
        "pending" -> "قيد المراجعة" to PillKind.Wait
        "rejected" -> "مرفوض" to PillKind.Rj
        else -> "موقوف" to PillKind.Off
    }
    Row(Modifier.clip(RoundedCornerShape(50.dp)).background(when (kind) { PillKind.Live -> C.pillLive; PillKind.Wait -> C.pillWait; PillKind.Rj -> C.redBg; else -> C.pillOff })
        .padding(horizontal = 11.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        if (status == "approved") { Ic(R.drawable.ic_shield, 13.dp, C.greenD); Spacer(Modifier.width(4.dp)) }
        T(label, 10, FontWeight.ExtraBold, when (kind) { PillKind.Live -> C.greenD; PillKind.Wait -> C.terraText; PillKind.Rj -> C.redText; else -> Color(0xFF9AA198) })
    }
}

@Composable
private fun ToggleCard(iconId: Int, iconBg: Color, iconColor: Color, title: String, sub: String, on: Boolean, onToggle: () -> Unit) {
    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(iconBg), contentAlignment = Alignment.Center) { Ic(iconId, 20.dp, iconColor) }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                T(title, 13, FontWeight.Bold, C.head)
                Spacer(Modifier.height(2.dp))
                T(sub, 10, FontWeight.Normal, C.muted, lineHeight = 17)
            }
            Spacer(Modifier.width(10.dp))
            Sw(on, onToggle)
        }
    }
}

@Composable
private fun PList(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card)
        .border(1.dp, C.line, RoundedCornerShape(22.dp)), content = content)
}

@Composable
private fun PRow(brush: Brush, iconId: Int, title: String, sub: String?, last: Boolean = false, onClick: () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(brush), contentAlignment = Alignment.Center) { Ic(iconId, 17.dp, Color.White) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                T(title, 13, FontWeight.Bold, C.head)
                if (sub != null) { Spacer(Modifier.height(1.dp)); T(sub, 10, FontWeight.Medium, C.muted, maxLines = 1) }
            }
            Ic(R.drawable.ic_back, 17.dp, Color(0xFFC3C9C0))
        }
        if (!last) StoreDivider()
    }
}

@Composable
private fun PRowLogout(onLogout: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onLogout).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(13.dp)).background(C.redBg), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_out, 17.dp, C.redText) }
        Spacer(Modifier.width(12.dp))
        T("تسجيل الخروج", 13, FontWeight.Bold, C.redText, Modifier.weight(1f))
    }
}

@Composable
private fun StoreDivider() {
    androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(Color(0xFFF0ECE3), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 1f)
    }
}
