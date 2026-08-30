package com.matnokh.tajer.ui

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
import com.matnokh.tajer.R

private data class NavItem(val key: String, val iconId: Int, val label: String)

private val NAV get() = listOf(
    NavItem("dash", R.drawable.ic_chart, tr("الرئيسية", "Home")),
    NavItem("products", R.drawable.ic_box, tr("المنتجات", "Products")),
    NavItem("orders", R.drawable.ic_list, tr("الطلبات", "Orders")),
    NavItem("wallet", R.drawable.ic_cash, tr("المحفظة", "Wallet")),
    NavItem("store", R.drawable.ic_cog, tr("المتجر", "Store")),
)

@Composable
fun BottomNav(current: String, onSelect: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(C.card.copy(alpha = .96f))
            .border(width = 1.dp, color = C.line, shape = RoundedCornerShape(0.dp))
            .navigationBarsPadding()
            .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
    ) {
        NAV.forEach { item ->
            val on = current == item.key
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { onSelect(item.key) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.size(44.dp, 30.dp).clip(RoundedCornerShape(12.dp))
                        .background(if (on) C.pillLive else Color.Transparent),
                    contentAlignment = Alignment.Center,
                ) { Ic(item.iconId, 22.dp, if (on) C.greenD else Color(0xFFA3ACA2)) }
                Spacer(Modifier.height(4.dp))
                T(item.label, 10, FontWeight.Bold, if (on) C.greenD else Color(0xFFA3ACA2))
            }
        }
    }
}
