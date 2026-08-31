package com.matnokh.driver.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Lang {
    var code by mutableStateOf("ar")
        private set
    val isAr get() = code == "ar"
    private var prefs: android.content.SharedPreferences? = null
    fun init(ctx: Context) {
        val p = ctx.getSharedPreferences("matnokh_driver", Context.MODE_PRIVATE)
        prefs = p
        code = p.getString("lang", "ar") ?: "ar"
        syncToServer()
    }
    fun set(c: String) { code = c; prefs?.edit()?.putString("lang", c)?.apply(); syncToServer() }
    /** مزامنة اللغة للخادم — إشعارات Push تصل بلغة المستخدم المختارة. يتجاهل الفشل بصمت (قبل الدخول مثلاً). */
    fun syncToServer() {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { com.matnokh.driver.net.Net.api.setLanguage(com.matnokh.driver.net.LangBody(code)) }
        }
    }
    fun toggle() = set(if (isAr) "en" else "ar")
}

fun tr(ar: String, en: String): String = if (Lang.isAr) ar else en

fun trd(ar: String?, en: String?): String = if (Lang.isAr) (ar ?: "") else (en?.takeIf { it.isNotBlank() } ?: ar ?: "")

/** رمز العملة حسب اللغة: ﷼ بالعربية و SAR بالإنجليزية. */
val RY: String get() = if (Lang.isAr) "﷼" else "SAR "
