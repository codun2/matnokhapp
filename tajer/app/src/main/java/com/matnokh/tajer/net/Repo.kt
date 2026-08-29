package com.matnokh.tajer.net
import com.matnokh.tajer.ui.tr

import retrofit2.HttpException

// غلاف موحّد: ينفّذ نداءً ويعيد الرسالة عند الخطأ عبر onError
suspend fun <T> call(block: suspend () -> T, onError: (String) -> Unit): T? = try {
    block()
} catch (e: HttpException) {
    onError(errorMessage(e) ?: tr("تعذّر تنفيذ العملية", "Couldn't complete the operation"))
    null
} catch (e: Exception) {
    onError(tr("تعذّر الاتصال بالخادم", "Couldn't reach the server"))
    null
}
