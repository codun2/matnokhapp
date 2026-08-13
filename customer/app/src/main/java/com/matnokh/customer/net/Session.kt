package com.matnokh.customer.net

import android.content.Context

object Session {
    private lateinit var ctx: Context
    fun init(c: Context) { ctx = c.applicationContext }
    private fun sp() = ctx.getSharedPreferences("matnokh_customer", Context.MODE_PRIVATE)
    var token: String?
        get() = sp().getString("token", null)
        set(v) = sp().edit().putString("token", v).apply()
    var name: String?
        get() = sp().getString("name", null)
        set(v) = sp().edit().putString("name", v).apply()
    var avatar: String?
        get() = sp().getString("avatar", null)
        set(v) = sp().edit().putString("avatar", v).apply()
    var phone: String?
        get() = sp().getString("phone", null)
        set(v) = sp().edit().putString("phone", v).apply()
    var radius: Float
        get() = sp().getFloat("radius", 0f)
        set(v) = sp().edit().putFloat("radius", v).apply()
    fun isLoggedIn() = !token.isNullOrBlank()
    fun clear() = sp().edit().clear().apply()
}
