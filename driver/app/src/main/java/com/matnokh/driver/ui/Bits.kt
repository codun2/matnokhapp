package com.matnokh.driver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.matnokh.driver.R

/* تدرّجات بطاقات الطلبات (0=green 1=terra 2=blue 3=sand) */
val jobGradients: List<Brush> = listOf(Grad.green, Grad.terra, Grad.blue, Grad.sand)

@Composable
fun SecTitle(title: String, trailing: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        T(title, 16, FontWeight.ExtraBold, C.head, Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
fun HeaderBtn(iconId: Int, badge: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(C.card)
            .border(1.dp, C.line, RoundedCornerShape(15.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Ic(iconId, 17.dp, Color(0xFF5D6B62))
        if (badge) Box(Modifier.align(Alignment.TopEnd).padding(top = 10.dp, end = 11.dp).size(8.dp).clip(CircleShape).background(C.terra))
    }
}

@Composable
fun Kpi(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(18.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(18.dp)).padding(vertical = 14.dp, horizontal = 8.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            T(value, 20, FontWeight.Black, color)
            Spacer(Modifier.height(2.dp)); T(label, 10, FontWeight.Normal, C.muted, maxLines = 1)
        }
    }
}

/* مسار الطلب: من → إلى */
@Composable
fun RouteBox(from: String, to: String) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color(0xFFFAF8F4))
            .border(1.dp, C.line, RoundedCornerShape(15.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) { Ic(R.drawable.ic_pin, 14.dp, C.green); Spacer(Modifier.width(8.dp)); T(tr("استلام", "Pickup"), 9, FontWeight.Bold, C.green); Spacer(Modifier.width(6.dp)); T(from, 11, FontWeight.Bold, C.head, maxLines = 1) }
        Row(verticalAlignment = Alignment.CenterVertically) { Ic(R.drawable.ic_flag, 14.dp, C.terra); Spacer(Modifier.width(8.dp)); T(tr("تسليم", "Deliver"), 9, FontWeight.Bold, C.terra); Spacer(Modifier.width(6.dp)); T(to, 11, FontWeight.Bold, C.head, maxLines = 1) }
    }
}

@Composable
fun GradBadge(iconId: Int, gradient: Brush, size: Dp = 44.dp, corner: Dp = 15.dp) {
    Box(Modifier.size(size).clip(RoundedCornerShape(corner)).background(gradient), contentAlignment = Alignment.Center) {
        Ic(iconId, size * 0.5f, Color.White)
    }
}

/* حالة الشارة */
enum class PillKind { Live, Ok, Wait, Off, Rj }
