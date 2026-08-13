package com.matnokh.driver.net

import android.content.Context

object Session {
    private const val PREF = "matnokh_driver"
    private lateinit var appCtx: Context
    fun init(ctx: Context) { appCtx = ctx.applicationContext }
    private fun sp() = appCtx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    var token: String?
        get() = sp().getString("token", null)
        set(v) = sp().edit().putString("token", v).apply()
    var name: String?
        get() = sp().getString("name", null)
        set(v) = sp().edit().putString("name", v).apply()

    fun isLoggedIn(): Boolean = !token.isNullOrBlank()
    fun save(r: LoginResp) { token = r.token; name = r.driver?.name }
    fun clear() { sp().edit().clear().apply() }
}
