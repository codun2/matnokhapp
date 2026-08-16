package com.matnokh.tajer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.NotificationBus
import com.matnokh.tajer.net.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MatnokhMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        if (Session.isLoggedIn()) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { Net.api.registerDeviceToken(mapOf("token" to token, "platform" to "android")) }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data["type"] == "chat") {
            val k = (message.data["kind"] ?: "") + ":" + (message.data["order_id"] ?: "") + ":" + (message.data["chat_type"] ?: "")
            if (com.matnokh.tajer.ui.ChatOpen.key == k) return
        }
        val title = message.notification?.title ?: message.data["title"] ?: "مطنوخ"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = message.data["type"]
        val orderId = message.data["order_id"]?.toIntOrNull()
        showNotification(title, body, type, orderId)
        // بثّ للواجهة لعرض بطاقة داخل التطبيق (عند فتحه)
        Handler(Looper.getMainLooper()).post { NotificationBus.push(title, body, type, orderId) }
    }

    private fun showNotification(title: String, body: String, type: String? = null, orderId: Int? = null) {
        val channelId = "matnokh_default"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(channelId, "إشعارات مطنوخ", NotificationManager.IMPORTANCE_HIGH))
        }
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("type", type)
            orderId?.let { putExtra("order_id", it.toString()) }
        }
        val pi = PendingIntent.getActivity(this, 0, intent ?: Intent(), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .build()
        nm.notify(System.currentTimeMillis().toInt(), notif)
    }
}
