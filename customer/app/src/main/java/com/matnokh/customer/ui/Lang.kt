package com.matnokh.customer.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** حالة اللغة العامة (عربي/إنجليزي) مع الحفظ. */
object Lang {
    var code by mutableStateOf("ar")
        private set
    val isAr get() = code == "ar"
    private var prefs: android.content.SharedPreferences? = null
    fun init(ctx: Context) {
        val p = ctx.getSharedPreferences("matnokh_customer", Context.MODE_PRIVATE)
        prefs = p
        code = p.getString("lang", "ar") ?: "ar"
    }
    fun set(c: String) { code = c; prefs?.edit()?.putString("lang", c)?.apply() }
    fun toggle() = set(if (isAr) "en" else "ar")
}

/** يختار النص حسب اللغة الحالية. القراءة داخل Compose تُعيد التركيب عند التبديل. */
fun tr(ar: String, en: String): String = if (Lang.isAr) ar else en
