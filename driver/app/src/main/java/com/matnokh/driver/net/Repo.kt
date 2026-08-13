package com.matnokh.driver.net

import retrofit2.HttpException

suspend fun <T> call(block: suspend () -> T, onError: (String) -> Unit): T? = try {
    block()
} catch (e: HttpException) {
    onError(errorMessage(e) ?: "تعذّر تنفيذ العملية")
    null
} catch (e: Exception) {
    onError("تعذّر الاتصال بالخادم")
    null
}
