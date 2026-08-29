package com.matnokh.tajer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.tajer.R
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.Period
import com.matnokh.tajer.net.ReportsResp
import com.matnokh.tajer.net.call

@Composable
fun ReportsScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    var rep by remember { mutableStateOf<ReportsResp?>(null) }
    LaunchedEffect(Unit) { call({ Net.api.reports() }, toast)?.let { rep = it } }
    val r = rep

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("التقارير", "Reports"), onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            Box(Modifier.padding(horizontal = 22.dp)) {
                T(tr("تقارير المبيعات — مبنيّة على الطلبات المسلّمة فعلياً", "Sales reports — based on actually delivered orders"), 11, FontWeight.Medium, C.muted)
            }
            Spacer(Modifier.height(12.dp))
            ReportCard(tr("اليوم", "Today"), r?.today, C.greenD, Grad.green)
            ReportCard(tr("هذا الأسبوع", "This week"), r?.week, C.blueText, Grad.blue)
            ReportCard(tr("هذا الشهر", "This month"), r?.month, C.terraText, Grad.terra)
            ReportCard(tr("هذا العام", "This year"), r?.year, C.greenD, Grad.green)
            ReportCard(tr("الإجمالي منذ البداية", "Total since the start"), r?.all, C.blueText, Grad.blue)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReportCard(label: String, p: Period?, color: Color, grad: Brush) {
    OCard(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth(), PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(grad), contentAlignment = Alignment.Center) {
                Ic(R.drawable.ic_chart, 22.dp, Color.White)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                T(label, 14, FontWeight.Bold, C.head)
                Spacer(Modifier.height(2.dp))
                T(tr("${p?.orders ?: 0} طلباً مسلّماً", "${p?.orders ?: 0} orders delivered"), 11, FontWeight.Normal, C.muted)
            }
            Spacer(Modifier.width(8.dp))
            T("﷼" + money(p?.sales ?: 0.0), 18, FontWeight.Black, color)
        }
    }
}
