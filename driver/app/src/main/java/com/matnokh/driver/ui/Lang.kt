package com.matnokh.driver.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Lang {
    var code by mutableStateOf("ar")
        private set
    val isAr get() = code == "ar"
    private var prefs: android.content.SharedPreferences? = null
    fun init(ctx: Context) {
        val p = ctx.getSharedPreferences("matnokh_driver", Context.MODE_PRIVATE)
        prefs = p
        code = p.getString("lang", "ar") ?: "ar"
    }
    fun set(c: String) { code = c; prefs?.edit()?.putString("lang", c)?.apply() }
    fun toggle() = set(if (isAr) "en" else "ar")
}

fun tr(ar: String, en: String): String = if (Lang.isAr) ar else en

fun trd(ar: String?, en: String?): String = if (Lang.isAr) (ar ?: "") else (en?.takeIf { it.isNotBlank() } ?: ar ?: "")

/** رمز العملة حسب اللغة: ﷼ بالعربية و SAR بالإنجليزية. */
val RY: String get() = if (Lang.isAr) "﷼" else "SAR "
