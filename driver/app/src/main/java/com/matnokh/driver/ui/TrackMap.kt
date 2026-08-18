package com.matnokh.driver.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AColor
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

/** أيقونة سيارة صغيرة (بلا دائرة) خضراء اللون. */
private var carCache: BitmapDescriptor? = null
fun carMarker(ctx: Context): BitmapDescriptor {
    carCache?.let { return it }
    val size = (32 * ctx.resources.displayMetrics.density).toInt()
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val d = ContextCompat.getDrawable(ctx, com.matnokh.driver.R.drawable.ic_van)!!
    d.setBounds(0, 0, size, size)
    d.setTint(AColor.parseColor("#1d9e75"))
    d.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bmp).also { carCache = it }
}

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
    if (km < 0.12) return "وصلت تقريباً"
    val mins = max(1, (km / 25.0 * 60.0).roundToInt())
    val dist = if (km < 1.0) "${(km * 1000).toInt()} م" else String.format("%.1f كم", km)
    return "$mins دقيقة · $dist"
}

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
