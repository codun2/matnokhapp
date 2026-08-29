package com.matnokh.driver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.driver.R
import com.matnokh.driver.net.Net
import com.matnokh.driver.net.NotifItem

@Composable
fun NotificationsScreen(onBack: () -> Unit, onMenu: () -> Unit, onOpen: (String?) -> Unit) {
    var items by remember { mutableStateOf<List<NotifItem>?>(null) }
    LaunchedEffect(Unit) { items = runCatching { Net.api.notifications().notifications }.getOrDefault(emptyList()) }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("الإشعارات", "Notifications"), onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(6.dp))
            val list = items
            when {
                list == null -> Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
                list.isEmpty() -> Box(Modifier.fillMaxWidth().padding(top = 70.dp), contentAlignment = Alignment.Center) { T(tr("لا توجد إشعارات بعد", "No notifications yet"), 13, FontWeight.Medium, C.muted) }
                else -> list.forEach { n ->
                    val (icon, grad) = notifStyle(n.type)
                    NotifRow(icon, grad, n.title, n.body, n.dt ?: "", onClick = { onOpen(n.type) })
                }
            }
            Spacer(Modifier.height(110.dp))
        }
    }
}

private fun notifStyle(type: String?): Pair<Int, Brush> = when (type) {
    "store_offer" -> R.drawable.ic_nav to Grad.terra
    "store_assigned" -> R.drawable.ic_check to Grad.green
    "new_transport" -> R.drawable.ic_bike to Grad.blue
    "broadcast" -> R.drawable.ic_bell to Grad.sand
    else -> R.drawable.ic_bell to Grad.green
}

@Composable
private fun NotifRow(iconId: Int, gradient: Brush, title: String, body: String, time: String, unread: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        Modifier.padding(horizontal = 22.dp).padding(bottom = 11.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(if (unread) C.pillLive else C.card).border(1.dp, if (unread) C.sage else C.line, RoundedCornerShape(20.dp)).clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        GradBadge(iconId, gradient, 42.dp, 14.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            T(title, 13, FontWeight.Bold, C.head, maxLines = 2)
            Spacer(Modifier.height(3.dp)); T(body, 11, FontWeight.Normal, C.muted, lineHeight = 18)
            Spacer(Modifier.height(5.dp)); T(time, 10, FontWeight.Medium, C.sage)
        }
    }
}
