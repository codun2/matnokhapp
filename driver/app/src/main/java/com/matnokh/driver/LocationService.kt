package com.matnokh.driver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.matnokh.driver.net.LocBody
import com.matnokh.driver.net.Net
import com.matnokh.driver.net.Session
import com.matnokh.driver.ui.Drv
import com.matnokh.driver.ui.currentLatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * خدمة أمامية تبثّ موقع المندوب بشكل مستقلّ عن الواجهة (تستمر حتى مع قفل الشاشة).
 * التردّد متكيّف: كل 15 ثانية أثناء طلب نشط، وكل 75 ثانية إذا كان متاحاً بلا طلب.
 * تتوقّف تلقائياً عند تسجيل الخروج أو إذا صار غير متاح ولا يوجد طلب نشط.
 */
class LocationService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var loop: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runCatching { goForeground() }
        if (loop?.isActive != true) {
            loop = scope.launch {
                while (isActive) {
                    if (!Session.isLoggedIn()) break
                    if (!Drv.available.value && Drv.nowOrders.isEmpty()) break
                    runCatching {
                        currentLatLng(applicationContext)?.let { p ->
                            Net.api.location(LocBody(p.first, p.second))
                            Drv.driverLat.value = p.first
                            Drv.driverLng.value = p.second
                        }
                    }
                    delay(if (Drv.nowOrders.isNotEmpty()) 15_000L else 75_000L)
                }
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun goForeground() {
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL, "تتبّع الموقع", NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false) }
            )
        }
        val n: Notification = NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle("مطنوخ")
            .setContentText("يشارك موقعك أثناء الدوام لاستقبال الطلبات وتتبّع التوصيل")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIF_ID, n)
        }
    }

    override fun onDestroy() {
        loop?.cancel(); scope.cancel(); super.onDestroy()
    }

    companion object {
        const val CHANNEL = "loc_tracking"
        const val NOTIF_ID = 4711

        /** يشغّل/يوقف الخدمة حسب حالة المندوب (يُنادى من الواجهة عند أي تغيّر). */
        fun sync(ctx: Context) {
            val run = Session.isLoggedIn() && (Drv.available.value || Drv.nowOrders.isNotEmpty())
            val hasLoc = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val i = Intent(ctx, LocationService::class.java)
            if (run && hasLoc) ContextCompat.startForegroundService(ctx, i) else ctx.stopService(i)
        }
    }
}
