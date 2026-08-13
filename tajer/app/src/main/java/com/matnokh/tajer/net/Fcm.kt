package com.matnokh.tajer.net

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.matnokh.tajer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// تهيئة Firebase يدوياً من موارد السلاسل (بدون google-services.json).
object Fcm {
    private const val TAG = "Fcm"

    fun isConfigured(ctx: Context): Boolean {
        val appId = ctx.getString(R.string.fcm_application_id)
        val apiKey = ctx.getString(R.string.fcm_api_key)
        return !appId.startsWith("YOUR_") && !apiKey.startsWith("YOUR_") && appId.isNotBlank() && apiKey.isNotBlank()
    }

    fun init(ctx: Context): Boolean {
        if (!isConfigured(ctx)) return false
        return try {
            if (FirebaseApp.getApps(ctx).isEmpty()) {
                val opts = FirebaseOptions.Builder()
                    .setApplicationId(ctx.getString(R.string.fcm_application_id))
                    .setApiKey(ctx.getString(R.string.fcm_api_key))
                    .setProjectId(ctx.getString(R.string.fcm_project_id))
                    .setGcmSenderId(ctx.getString(R.string.fcm_sender_id))
                    .build()
                FirebaseApp.initializeApp(ctx, opts)
            }
            true
        } catch (e: Exception) { Log.w(TAG, "init failed: ${e.message}"); false }
    }

    /** يجلب التوكن ويسجّله في الباك-إند (عند وجود جلسة). */
    fun registerToken(ctx: Context) {
        if (!init(ctx) || !Session.isLoggedIn()) return
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) { Log.w(TAG, "token failed", task.exception); return@addOnCompleteListener }
                val token = task.result ?: return@addOnCompleteListener
                CoroutineScope(Dispatchers.IO).launch {
                    runCatching { Net.api.registerDeviceToken(mapOf("token" to token, "platform" to "android")) }
                }
            }
        } catch (e: Exception) { Log.w(TAG, "register failed: ${e.message}") }
    }
}
