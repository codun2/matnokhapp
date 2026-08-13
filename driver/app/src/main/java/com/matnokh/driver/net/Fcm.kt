package com.matnokh.driver.net

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.matnokh.driver.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Fcm {
    fun isConfigured(ctx: Context): Boolean {
        val a = ctx.getString(R.string.fcm_application_id); val k = ctx.getString(R.string.fcm_api_key)
        return !a.startsWith("YOUR_") && !k.startsWith("YOUR_") && a.isNotBlank() && k.isNotBlank()
    }
    fun init(ctx: Context): Boolean {
        if (!isConfigured(ctx)) return false
        return try {
            if (FirebaseApp.getApps(ctx).isEmpty()) FirebaseApp.initializeApp(ctx, FirebaseOptions.Builder()
                .setApplicationId(ctx.getString(R.string.fcm_application_id))
                .setApiKey(ctx.getString(R.string.fcm_api_key))
                .setProjectId(ctx.getString(R.string.fcm_project_id))
                .setGcmSenderId(ctx.getString(R.string.fcm_sender_id)).build())
            true
        } catch (e: Exception) { Log.w("Fcm", "init: ${e.message}"); false }
    }
    fun registerToken(ctx: Context) {
        if (!init(ctx) || !Session.isLoggedIn()) return
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { t ->
                if (!t.isSuccessful) return@addOnCompleteListener
                val token = t.result ?: return@addOnCompleteListener
                CoroutineScope(Dispatchers.IO).launch { runCatching { Net.api.registerDeviceToken(mapOf("token" to token, "platform" to "android")) } }
            }
        } catch (_: Exception) {}
    }
}
