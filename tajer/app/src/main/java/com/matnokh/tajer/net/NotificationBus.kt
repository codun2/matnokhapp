package com.matnokh.tajer.net

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class InAppMsg(val title: String, val body: String, val type: String?, val orderId: Int?)

// جسر بين خدمة FCM وواجهة Compose: بطاقة داخل التطبيق + نقطة الجرس + توجيه عند الضغط.
object NotificationBus {
    var incoming by mutableStateOf<InAppMsg?>(null)   // بطاقة تظهر داخل التطبيق (foreground)
        private set
    var hasNew by mutableStateOf(false)               // مؤشّر الجرس

    // توجيه عند الضغط على الإشعار (من الخلفية أو من النظام)
    var pendingScreen by mutableStateOf<String?>(null)
    var pendingOrderId by mutableStateOf<Int?>(null)
    var pendingChatOrderId by mutableStateOf<Int?>(null)

    fun push(title: String, body: String, type: String?, orderId: Int?) {
        incoming = InAppMsg(title, body, type, orderId)
        hasNew = true
    }
    fun dismiss() { incoming = null }
    fun markSeen() { hasNew = false }

    /** يُستدعى عند الضغط على إشعار: يحدّد الوجهة. */
    fun routeFrom(type: String?, orderId: Int?) {
        if (type == "chat") { if (orderId != null) pendingChatOrderId = orderId; pendingScreen = "orders"; return }
        if (orderId != null) pendingOrderId = orderId
        pendingScreen = when {
            orderId != null || type == "new_order" || type == "order_update" -> "orders"
            type != null -> "notifications"
            else -> pendingScreen
        }
    }
}
