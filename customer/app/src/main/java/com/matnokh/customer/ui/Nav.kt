package com.matnokh.customer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.customer.R
import com.matnokh.customer.net.*

// حالة الاختيار المشتركة
object Sel {
    var store by mutableStateOf<UiStore?>(null)
    var place by mutableStateOf<com.matnokh.customer.net.UiPlace?>(null)
    var destLabel by mutableStateOf("موقعي الحالي")
    var destAddr by mutableStateOf<String?>(null)
    var destLat by mutableStateOf<Double?>(null)
    var destLng by mutableStateOf<Double?>(null)
    var destBack by mutableStateOf("cart")
    var branchIdx by mutableStateOf(0)
    var sectionIdx by mutableStateOf(0)
    var sectionStoreId by mutableStateOf<Int?>(null)
    var product by mutableStateOf<UiProduct?>(null)
    var storeBack by mutableStateOf("home")
    var prodBack by mutableStateOf("store")
    var serviceId by mutableStateOf("fast")
    var svc by mutableStateOf<com.matnokh.customer.net.SvcDto?>(null)
    var transportId by mutableStateOf<Int?>(null)
    var detailOrderId by mutableStateOf<Int?>(null)
    var detailIsTransport by mutableStateOf(false)
    var drvName by mutableStateOf("أبو أحمد النجار")
    var drvAvatar by mutableStateOf("أب")
    var drvMeta by mutableStateOf("4.9 · فان توصيل")
    var payAmount by mutableStateOf(95)
    var svcName by mutableStateOf("نقل أثاث")
    var trackStep by mutableStateOf(3)
    var deeplink by mutableStateOf<String?>(null)
    var deeplinkOrderId by mutableStateOf<Int?>(null)
    var chatKind by mutableStateOf("store")
    var chatId by mutableStateOf<Int?>(null)
    var chatType by mutableStateOf("driver")
    var chatTitle by mutableStateOf("محادثة")
    var chatBack by mutableStateOf("orders")
    val service get() = CData.services.firstOrNull { it.id == serviceId } ?: CData.services[0]
}

@Composable
fun CustBackHeader(title: String, onBack: () -> Unit, onCart: () -> Unit, onMenu: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().background(C.bg.copy(alpha = .96f)).statusBarsPadding().padding(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        HeaderIcon(R.drawable.ic_back, onBack); Spacer(Modifier.width(10.dp))
        T(title, 18, FontWeight.ExtraBold, C.head, Modifier.weight(1f), maxLines = 1)
        if (trailing != null) { trailing(); Spacer(Modifier.width(9.dp)) }
        CartButton(Cart.count(), onCart); Spacer(Modifier.width(9.dp)); HeaderIcon(R.drawable.ic_menu, onMenu)
    }
}
