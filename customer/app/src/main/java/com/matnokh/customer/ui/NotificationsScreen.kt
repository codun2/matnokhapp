package com.matnokh.customer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.customer.R
import com.matnokh.customer.net.*

@Composable
fun NotificationsScreen(onBack: () -> Unit, onMenu: () -> Unit, onOpen: (String?) -> Unit, toast: (String) -> Unit) {
    var items by remember { mutableStateOf<List<NotifItem>?>(null) }
    LaunchedEffect(RefreshBus.tick) { if (Session.isLoggedIn()) call({ Net.api.notifications() }, toast)?.let { items = it.notifications } else items = emptyList() }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("الإشعارات", onBack, onMenu)
        val list = items
        when {
            list == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
            list.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Ic(R.drawable.ic_bell, 40.dp, C.sage); Spacer(Modifier.height(10.dp)); T("لا توجد إشعارات بعد", 13, FontWeight.Bold, C.muted) } }
            else -> LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)) {
                items(list) { n ->
                    Row(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(20.dp)).clickable { onOpen(n.type) }.padding(15.dp)) {
                        Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(C.pillLive), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_bell, 20.dp, C.greenD) }
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) { Row(verticalAlignment = Alignment.CenterVertically) { T(n.title, 13, FontWeight.Bold, C.head, Modifier.weight(1f), maxLines = 1); n.dt?.let { T(it, 9, FontWeight.Medium, C.muted) } }; Spacer(Modifier.height(3.dp)); T(n.body, 11, FontWeight.Normal, C.muted, lineHeight = 18) }
                    }
                }
            }
        }
    }
}
