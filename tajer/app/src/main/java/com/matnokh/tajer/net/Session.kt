package com.matnokh.tajer.net

import android.content.Context

// تخزين بسيط للتوكن + بيانات المتجر
object Session {
    private const val PREF = "matnokh"
    private lateinit var appCtx: Context

    fun init(ctx: Context) { appCtx = ctx.applicationContext }

    private fun sp() = appCtx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    var token: String?
        get() = sp().getString("token", null)
        set(v) = sp().edit().putString("token", v).apply()

    var storeName: String?
        get() = sp().getString("store", null)
        set(v) = sp().edit().putString("store", v).apply()

    var logo: String?
        get() = sp().getString("logo", null)
        set(v) = sp().edit().putString("logo", v).apply()

    fun bearer(): String = "Bearer ${token ?: ""}"
    fun isLoggedIn(): Boolean = !token.isNullOrBlank()

    fun save(resp: LoginResp) {
        token = resp.token
        storeName = resp.merchant?.store_name
        logo = resp.merchant?.logo
    }

    fun clear() { sp().edit().clear().apply() }
}
