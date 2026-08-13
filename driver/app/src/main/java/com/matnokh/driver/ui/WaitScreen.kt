package com.matnokh.driver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matnokh.driver.R

@Composable
fun WaitScreen(custName: String, sentAmt: Int, onBack: () -> Unit, onMenu: () -> Unit) {
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("بانتظار اختيار الزبون", onBack, onMenu)
        Column(Modifier.weight(1f).padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(90.dp).clip(RoundedCornerShape(28.dp)).background(C.pillLive), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_clock, 42.dp, C.greenD) }
            Spacer(Modifier.height(20.dp))
            T("تم إرسال عرضك: ﷼$sentAmt", 17, FontWeight.Black, C.head)
            Spacer(Modifier.height(10.dp))
            androidx.compose.material3.Text(
                "وصل عرضك إلى «$custName» كإشعار فوري. قد يستقبل عروضاً من سائقين آخرين — إذا اختارك سيُسند الطلب لك تلقائياً.",
                fontFamily = Cairo, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = C.muted,
                textAlign = TextAlign.Center, lineHeight = 23.sp, modifier = Modifier.widthIn(max = 270.dp),
            )
            Spacer(Modifier.height(24.dp))
            WideButton("العودة لشاشة الاستقبال", ghost = true, modifier = Modifier.widthIn(max = 260.dp), onClick = onBack)
        }
    }
}
