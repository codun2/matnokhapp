package com.matnokh.customer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.customer.R

@Composable
fun BottomNav(current: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().background(C.card.copy(alpha = .96f)).border(1.dp, C.line, RoundedCornerShape(0.dp)).navigationBarsPadding().padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.Top) {
        Tab("home", R.drawable.ic_home, "الرئيسية", current, onSelect, Modifier.weight(1f))
        Tab("stores", R.drawable.ic_shop, "المتاجر", current, onSelect, Modifier.weight(1f))
        Tab("orders", R.drawable.ic_list, "طلباتي", current, onSelect, Modifier.weight(1f))
        Tab("profile", R.drawable.ic_user, "حسابي", current, onSelect, Modifier.weight(1f))
    }
}

@Composable
private fun Tab(key: String, iconId: Int, label: String, current: String, onSelect: (String) -> Unit, modifier: Modifier) {
    val on = current == key
    Column(modifier.clip(RoundedCornerShape(12.dp)).clickable { onSelect(key) }, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(44.dp, 30.dp).clip(RoundedCornerShape(12.dp)).background(if (on) C.pillLive else Color.Transparent), contentAlignment = Alignment.Center) {
            Ic(iconId, 22.dp, if (on) C.greenD else Color(0xFFA3ACA2))
        }
        Spacer(Modifier.height(4.dp)); T(label, 10, FontWeight.Bold, if (on) C.greenD else Color(0xFFA3ACA2))
    }
}

// تدرّجات الخدمات
val svcGradients = listOf(Grad.green, Grad.blue, Grad.terra, Grad.sand)
