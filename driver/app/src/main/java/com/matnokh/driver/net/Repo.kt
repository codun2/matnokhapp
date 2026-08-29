package com.matnokh.driver.net
import com.matnokh.driver.ui.tr

import retrofit2.HttpException

suspend fun <T> call(block: suspend () -> T, onError: (String) -> Unit): T? = try {
    block()
} catch (e: HttpException) {
    onError(errorMessage(e) ?: tr("تعذّر تنفيذ العملية", "Couldn't complete the operation"))
    null
} catch (e: Exception) {
    onError(tr("تعذّر الاتصال بالخادم", "Couldn't reach the server"))
    null
}
