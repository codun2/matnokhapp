package com.matnokh.customer.ui

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/** يعيد الموقع الحالي (lat,lng) أو null. يتطلّب إذن الموقع مُمنوحاً مسبقاً. */
@SuppressLint("MissingPermission")
suspend fun currentLatLng(ctx: Context): Pair<Double, Double>? = suspendCancellableCoroutine { cont ->
    var done = false
    fun finish(v: Pair<Double, Double>?) { if (!done) { done = true; cont.resume(v) } }
    try {
        val client = LocationServices.getFusedLocationProviderClient(ctx)
        client.lastLocation
            .addOnSuccessListener { loc ->
                if (loc != null) finish(loc.latitude to loc.longitude)
                else client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { l -> finish(l?.let { it.latitude to it.longitude }) }
                    .addOnFailureListener { finish(null) }
            }
            .addOnFailureListener { finish(null) }
    } catch (e: Exception) { finish(null) }
}


private val geoClient = OkHttpClient()
/** يحوّل الإحداثيات إلى اسم مكان مختصر بالعربية عبر OpenStreetMap Nominatim. */
suspend fun reverseName(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
    try {
        val url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&zoom=18&lat=$lat&lon=$lng&accept-language=ar"
        val req = Request.Builder().url(url).header("User-Agent", "MatnokhCustomer/1.0 (Android)").build()
        geoClient.newCall(req).execute().use { r ->
            val body = r.body?.string() ?: return@withContext null
            val o = JSONObject(body)
            val a = o.optJSONObject("address")
            fun pick(vararg keys: String): String? { if (a == null) return null; for (k in keys) { val v = a.optString(k); if (v.isNotBlank()) return v }; return null }
            val near = pick("neighbourhood", "suburb", "quarter", "road", "pedestrian", "village")
            val city = pick("city", "town", "municipality", "state_district", "state")
            val parts = listOfNotNull(near, city)
            if (parts.isNotEmpty()) parts.joinToString(tr("، ", ", ")) else o.optString("display_name").split(",").take(2).joinToString(tr("،", ",")).trim().ifBlank { null }
        }
    } catch (e: Exception) { null }
}

/** المسافة بالكيلومترات بين نقطتين (haversine). */
fun distanceKm(a: com.google.android.gms.maps.model.LatLng, b: com.google.android.gms.maps.model.LatLng): Double {
    val r = 6371.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLng = Math.toRadians(b.longitude - a.longitude)
    val h = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(a.latitude)) * Math.cos(Math.toRadians(b.latitude)) * Math.sin(dLng / 2) * Math.sin(dLng / 2)
    return r * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h))
}
