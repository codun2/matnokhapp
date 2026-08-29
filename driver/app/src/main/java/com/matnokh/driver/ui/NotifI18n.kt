package com.matnokh.driver.ui

/**
 * Runtime translation of backend-stored Arabic text (notifications + dynamic
 * wallet/order titles) when the app language is English. Backend stores Arabic
 * only, so we map the known fixed phrases here and keep variable data
 * (store names, real addresses, order numbers) intact.
 */

private val NOTIF_MAP: Map<String, String> = mapOf(
    "طلب توصيل جديد 🛍️" to "New delivery order 🛍️",
    "طلب جديد 🚚" to "New order 🚚",
    "طلب جديد 🛒" to "New order 🛒",
    "طلب جديد" to "New order",
    "عرض توصيل جديد 💰" to "New delivery offer 💰",
    "عرض سعر جديد 💰" to "New price offer 💰",
    "عرض سعر جديد" to "New price offer",
    "تم اختيارك ✅" to "You've been selected ✅",
    "تم قبول عرضك ✅" to "Your offer was accepted ✅",
    "تم قبول عرضك" to "Your offer was accepted",
    "أُلغي الطلب" to "Order cancelled",
    "تم إلغاء الطلب" to "Order cancelled",
    "إلغاء الطلب" to "Order cancelled",
    "تحديث الطلب" to "Order update",
    "تم تسليم الطلب ✅" to "Order delivered ✅",
    "تم تسليم الطلب" to "Order delivered",
    "رسالة جديدة" to "New message",
    "رسالة جديدة 💬" to "New message 💬",
    "تم تأكيد الدفع ✅" to "Payment confirmed ✅",
    "تم تأكيد الدفع" to "Payment confirmed",
    "تم تحويل الأموال 💰" to "Funds transferred 💰",
    "تم تحويل أرباحك 💰" to "Your earnings were transferred 💰",
    "تم تحويل أرباحك" to "Your earnings were transferred",
)

private val NOTIF_PATTERNS: List<Pair<Regex, (MatchResult) -> String>> = listOf(
    Regex("^وصلك عرض توصيل لطلبك\\s*#?(.+)$") to { m -> "You received a delivery offer for order #${m.groupValues[1].trim()}" },
    Regex("^اختارك الزبون لتوصيل الطلب\\s*#?(\\S+).*$") to { m -> "The customer chose you to deliver order #${m.groupValues[1]} — head to pickup" },
    Regex("^ألغى الزبون الطلب\\s*#?(\\S+).*$") to { m -> "The customer cancelled order #${m.groupValues[1]}" },
    Regex("^لديك طلب (?:توصيل )?جديد\\s*#?(\\S+).*$") to { m -> "You have a new order #${m.groupValues[1]}" },
    Regex("^وصلك عرض سعر.*$") to { _ -> "You received a price offer" },
)

fun trNotif(text: String?): String {
    val t = text?.trim() ?: return ""
    if (Lang.isAr) return t
    NOTIF_MAP[t]?.let { return it }
    for ((re, f) in NOTIF_PATTERNS) re.find(t)?.let { return f(it) }
    return t
}

private val DATA_MAP: Map<String, String> = mapOf(
    "توصيل طلبات" to "Order delivery",
    "نقل مياه" to "Water transport",
    "توصيل مياه" to "Water delivery",
    "تسوّق نيابة عنك" to "Shop on your behalf",
    "تسوق نيابة عنك" to "Shop on your behalf",
    "نقل رمل" to "Sand transport",
    "متاجر قريبة" to "Nearby stores",
    "متاجر مطنوخ" to "Matnokh stores",
    "طلب نقل" to "Transport order",
    "موقعي الحالي" to "My current location",
    "الموقع الحالي" to "Current location",
    "موقع الاستلام" to "Pickup location",
    "موقع التسليم" to "Drop-off location",
    "متجر" to "store",
)

/**
 * Translate a backend dynamic title/address when in English mode. Handles the
 * "طلب من <store>" prefix and the "<service> — #<id>" wallet-title shape,
 * translating only the fixed part and preserving store names / order numbers.
 */
fun trData(text: String?): String {
    val t = text?.trim() ?: return ""
    if (Lang.isAr) return t
    DATA_MAP[t]?.let { return it }
    if (t.startsWith("طلب من ")) return "Order from " + trData(t.removePrefix("طلب من "))
    val sep = t.indexOf(" — #")
    if (sep > 0) {
        val head = t.substring(0, sep)
        return (DATA_MAP[head] ?: head) + t.substring(sep)
    }
    return t
}
