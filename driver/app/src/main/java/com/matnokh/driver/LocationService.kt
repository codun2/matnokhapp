package com.matnokh.driver
import com.matnokh.driver.ui.tr

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
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.matnokh.driver.net.LocBody
import com.matnokh.driver.net.Net
import com.matnokh.driver.net.Session
import com.matnokh.driver.ui.Drv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * خدمة أمامية تبثّ موقع المندوب باستمرار (عبر requestLocationUpdates) فتستمر حتى مع قفل الشاشة.
 * كل تحديث موقع يُرسَل للسيرفر (يحدّث lat/lng + last_seen_at → يبقى «متصلاً»).
 * تتوقّف تلقائياً عند تسجيل الخروج، أو إذا صار غير متاح ولا يوجد طلب نشط، أو عند إغلاق التطبيق (السحب).
 */
class LocationService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var fused: FusedLocationProviderClient? = null
    private var cb: LocationCallback? = null
    private var wakeLock: android.os.PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runCatching { goForeground() }
        runCatching { acquireWakeLock() }
        startUpdates()
        return START_STICKY
    }

    private fun startUpdates() {
        if (cb != null) return
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf(); return
        }
        fused = LocationServices.getFusedLocationProviderClient(this)
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(3_000L)
            .build()
        cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (!Session.isLoggedIn() || (!Drv.available.value && Drv.nowOrders.isEmpty())) { stopSelf(); return }
                val loc = result.lastLocation ?: return
                Drv.driverLat.value = loc.latitude
                Drv.driverLng.value = loc.longitude
                scope.launch { runCatching { Net.api.location(LocBody(loc.latitude, loc.longitude)) } }
            }
        }
        runCatching { fused?.requestLocationUpdates(req, cb!!, Looper.getMainLooper()) }
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = pm.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "matnokh:loc")
        wakeLock?.setReferenceCounted(false)
        runCatching { wakeLock?.acquire() }
    }

    private fun goForeground() {
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL, tr("تتبّع الموقع", "Location tracking"), NotificationManager.IMPORTANCE_LOW).apply { setShowBadge(false) }
            )
        }
        val n: Notification = NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle(tr("مطنوخ", "Matnokh"))
            .setContentText(tr("يشارك موقعك أثناء الدوام لاستقبال الطلبات وتتبّع التوصيل", "Your location is shared during your shift to receive orders and track delivery"))
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        cb?.let { c -> runCatching { fused?.removeLocationUpdates(c) } }
        cb = null
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
        scope.cancel()
        runCatching { stopForeground(Service.STOP_FOREGROUND_REMOVE) }
        super.onDestroy()
    }

    companion object {
        const val CHANNEL = "loc_tracking"
        const val NOTIF_ID = 4711

        fun sync(ctx: Context) {
            val run = Session.isLoggedIn() && (Drv.available.value || Drv.nowOrders.isNotEmpty())
            val hasLoc = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val i = Intent(ctx, LocationService::class.java)
            if (run && hasLoc) ContextCompat.startForegroundService(ctx, i) else ctx.stopService(i)
        }
    }
}
