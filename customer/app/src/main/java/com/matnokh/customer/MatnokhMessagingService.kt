package com.matnokh.customer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.matnokh.customer.net.Net
import com.matnokh.customer.net.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MatnokhMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        if (Session.isLoggedIn()) CoroutineScope(Dispatchers.IO).launch { runCatching { Net.api.registerDeviceToken(mapOf("token" to token, "platform" to "android")) } }
    }
    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data["type"] == "chat") {
            val k = (message.data["kind"] ?: "") + ":" + (message.data["order_id"] ?: "") + ":" + (message.data["chat_type"] ?: "")
            if (com.matnokh.customer.ui.ChatOpen.key == k) return
        }
        val title = message.notification?.title ?: message.data["title"] ?: "مطنوخ"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) nm.createNotificationChannel(NotificationChannel("matnokh_default", "إشعارات مطنوخ", NotificationManager.IMPORTANCE_HIGH))
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP; putExtra("open", message.data["open"] ?: "orders"); putExtra("order_id", message.data["order_id"]); putExtra("kind", message.data["kind"]); putExtra("chat_type", message.data["chat_type"]) }
        val pi = PendingIntent.getActivity(this, 0, intent ?: Intent(), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        nm.notify(System.currentTimeMillis().toInt(), NotificationCompat.Builder(this, "matnokh_default")
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title).setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body)).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(pi).build())
    }
}
