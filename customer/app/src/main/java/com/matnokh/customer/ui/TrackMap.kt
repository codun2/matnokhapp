package com.matnokh.customer.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AColor
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** أيقونة السيارة (شعار فان أبيض على دائرة خضراء) كعلامة على الخريطة — مُخزّنة بعد أول بناء. */
private var carCache: BitmapDescriptor? = null
fun carMarker(ctx: Context): BitmapDescriptor {
    carCache?.let { return it }
    val size = (46 * ctx.resources.displayMetrics.density).toInt()
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AColor.argb(60, 0, 0, 0) }
    canvas.drawCircle(size / 2f, size / 2f + 2f, size / 2f - 3f, shadow)
    val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AColor.parseColor("#1d9e75") }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, bg)
    val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AColor.WHITE; style = Paint.Style.STROKE; strokeWidth = size * 0.05f }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, ring)
    val d = ContextCompat.getDrawable(ctx, com.matnokh.customer.R.drawable.ic_van)!!
    val pad = (size * 0.26f).toInt()
    d.setBounds(pad, pad, size - pad, size - pad)
    d.setTint(AColor.WHITE)
    d.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bmp).also { carCache = it }
}

/** زاوية اتجاه الحركة من a إلى b (لتدوير السيارة). */
fun bearingBetween(a: LatLng, b: LatLng): Float {
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val la1 = Math.toRadians(a.latitude); val la2 = Math.toRadians(b.latitude)
    val y = sin(dLon) * cos(la2)
    val x = cos(la1) * sin(la2) - sin(la1) * cos(la2) * cos(dLon)
    return ((Math.toDegrees(atan2(y, x)) + 360.0) % 360.0).toFloat()
}

/** المسافة بالكيلومترات بين نقطتين (هافرسين). */
fun haversineKm(a: LatLng, b: LatLng): Double {
    val R = 6371.0
    val dLa = Math.toRadians(b.latitude - a.latitude)
    val dLo = Math.toRadians(b.longitude - a.longitude)
    val h = sin(dLa / 2).pow(2) + cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLo / 2).pow(2)
    return R * 2 * atan2(sqrt(h), sqrt(1 - h))
}

/** نص الوقت المتوقّع للوصول من المسافة (متوسط 25 كم/س داخل المدينة). */
fun etaText(km: Double): String {
    if (km < 0.12) return "وصل تقريباً"
    val mins = max(1, (km / 25.0 * 60.0).roundToInt())
    val dist = if (km < 1.0) "${(km * 1000).toInt()} م" else String.format("%.1f كم", km)
    return "يصل خلال $mins دقيقة · $dist"
}

/** علامة سيارة تنزلق بنعومة من موقعها الحالي إلى الموقع الجديد وتدور نحو الاتجاه. */
@Composable
fun AnimatedCarMarker(target: LatLng, title: String, ctx: Context) {
    val state = remember { MarkerState(target) }
    var rot by remember { mutableStateOf(0f) }
    LaunchedEffect(target.latitude, target.longitude) {
        val start = state.position
        if (start.latitude == target.latitude && start.longitude == target.longitude) return@LaunchedEffect
        rot = bearingBetween(start, target)
        val frames = 25
        repeat(frames) { i ->
            val t = (i + 1) / frames.toFloat()
            state.position = LatLng(
                start.latitude + (target.latitude - start.latitude) * t,
                start.longitude + (target.longitude - start.longitude) * t,
            )
            delay(48)
        }
        state.position = target
    }
    Marker(
        state = state, title = title, icon = carMarker(ctx),
        rotation = rot, flat = true,
        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
    )
}
