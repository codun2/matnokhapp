package com.matnokh.customer.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AColor
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/** سيارة صغيرة مصمتة (منظر علوي) مقدّمتها للأعلى (شمال عند الدوران 0). */
private var carCache: BitmapDescriptor? = null
fun carMarker(ctx: Context): BitmapDescriptor {
    carCache?.let { return it }
    val s = (26 * ctx.resources.displayMetrics.density).toInt()
    val bmp = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AColor.parseColor("#1d9e75") }
    val glass = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AColor.parseColor("#CFEADE") }
    val w = s * 0.50f; val h = s * 0.86f
    val l = (s - w) / 2f; val t = (s - h) / 2f
    c.drawRoundRect(RectF(l, t, l + w, t + h), w * 0.36f, w * 0.36f, body)
    val gw = w * 0.64f; val gl = (s - gw) / 2f
    c.drawRoundRect(RectF(gl, t + h * 0.12f, gl + gw, t + h * 0.32f), gw * 0.25f, gw * 0.25f, glass) // زجاج أمامي (المقدّمة)
    c.drawRoundRect(RectF(gl, t + h * 0.60f, gl + gw, t + h * 0.80f), gw * 0.25f, gw * 0.25f, glass) // زجاج خلفي
    return BitmapDescriptorFactory.fromBitmap(bmp).also { carCache = it }
}

/** زاوية الاتجاه من a إلى b (بوصلة، 0=شمال، باتجاه عقارب الساعة). */
fun bearingBetween(a: LatLng, b: LatLng): Float {
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val la1 = Math.toRadians(a.latitude); val la2 = Math.toRadians(b.latitude)
    val y = sin(dLon) * cos(la2)
    val x = cos(la1) * sin(la2) - sin(la1) * cos(la2) * cos(dLon)
    return ((Math.toDegrees(atan2(y, x)) + 360.0) % 360.0).toFloat()
}

fun haversineKm(a: LatLng, b: LatLng): Double {
    val R = 6371.0
    val dLa = Math.toRadians(b.latitude - a.latitude)
    val dLo = Math.toRadians(b.longitude - a.longitude)
    val h = sin(dLa / 2).pow(2) + cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLo / 2).pow(2)
    return R * 2 * atan2(sqrt(h), sqrt(1 - h))
}

fun etaText(km: Double): String {
    if (km < 0.12) return "وصل تقريباً"
    val mins = max(1, (km / 25.0 * 60.0).roundToInt())
    val dist = if (km < 1.0) "${(km * 1000).toInt()} م" else String.format("%.1f كم", km)
    return "يصل خلال $mins دقيقة · $dist"
}

/** علامة سيارة تنزلق بنعومة، ومقدّمتها تتّجه نحو الوجهة (toward) إن وُجدت وإلا اتجاه الحركة. */
@Composable
fun AnimatedCarMarker(target: LatLng, title: String, ctx: Context, toward: LatLng? = null) {
    val state = remember { MarkerState(target) }
    var rot by remember { mutableStateOf(if (toward != null) bearingBetween(target, toward) else 0f) }
    LaunchedEffect(target.latitude, target.longitude, toward?.latitude, toward?.longitude) {
        val start = state.position
        rot = when {
            toward != null -> bearingBetween(target, toward)
            start.latitude != target.latitude || start.longitude != target.longitude -> bearingBetween(start, target)
            else -> rot
        }
        if (start.latitude == target.latitude && start.longitude == target.longitude) return@LaunchedEffect
        val frames = 25
        repeat(frames) { i ->
            val tt = (i + 1) / frames.toFloat()
            state.position = LatLng(
                start.latitude + (target.latitude - start.latitude) * tt,
                start.longitude + (target.longitude - start.longitude) * tt,
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
