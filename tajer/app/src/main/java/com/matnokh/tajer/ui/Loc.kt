package com.matnokh.tajer.ui

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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
