package com.matnokh.customer.ui

/** ترجمة نصوص الإشعارات (المخزّنة بالعربية) للإنجليزية عند العرض. */
private val NOTIF_EXACT: Map<String, String> = mapOf(
    "تحديث الطلب" to "Order update",
    "استلم المندوب طلبك" to "The courier picked up your order",
    "طلبك في الطريق إليك" to "Your order is on the way",
    "تم تسليم طلبك ✓" to "Your order has been delivered ✓",
    "تم اختيارك ✅" to "You've been selected ✅",
    "تم إسناد مندوب ✓" to "A courier has been assigned ✓",
    "تم قبول طلبك ✓" to "Your order was accepted ✓",
    "تم تأكيد تحويلك ✓" to "Your transfer was confirmed ✓",
    "طلبك جاهز 🎉" to "Your order is ready 🎉",
    "طلبك قيد الإسناد 🎉" to "Your order is being assigned 🎉",
    "تعذّر توصيل طلبك" to "Couldn't deliver your order",
    "تعذّر قبول طلبك" to "Couldn't accept your order",
    "تعذّر تأكيد التحويل" to "Couldn't confirm the transfer",
    "تم استرجاع مبلغك ✓" to "Your refund was processed ✓",
    "أُلغي الطلب" to "Order canceled",
    "طلب جديد 🛒" to "New order 🛒",
    "طلب توصيل جديد 🛍️" to "New delivery order 🛍️",
    "طلب جديد 🚚" to "New order 🚚",
    "عرض سعر جديد" to "New price offer",
    "عرض سعر جديد 💰" to "New price offer 💰",
    "عرض توصيل جديد 💰" to "New delivery offer 💰",
    "نبحث عن مندوب" to "Looking for a courier",
    "طلب بلا مندوب — تسوية" to "Order without a courier — settlement",
    "أموال مرجعة — خُصمت من رصيدك" to "Refund — deducted from your balance",
    "تحديث" to "Update",
)

private val NOTIF_RX: List<Pair<Regex, String>> = listOf(
    Regex("^طلبك من (.+) يُسنَد لأقرب مندوب الآن$") to "Your order from {1} is being assigned to the nearest courier now",
    Regex("^تعذّر توصيل طلبك #(\\S+) — أُعيد المبلغ إلى بطاقتك خلال أيام قليلة$") to "Couldn't deliver your order #{1} — the amount was refunded to your card within a few days",
    Regex("^أُعيد مبلغ طلبك #(\\S+).*بطاقتك.*$") to "Your order #{1} amount was refunded to your card within a few days",
    Regex("^حُوّل مبلغ طلبك #(\\S+) إلى حسابك البنكي$") to "Your order #{1} refund was transferred to your bank account",
    Regex("^أُعيد مبلغ طلبك #(\\S+) — .*$") to "Your order #{1} was refunded — you'll receive it via the store/support",
    Regex("^تعذّر توصيل طلبك #(\\S+) — تواصل مع الدعم.*$") to "Couldn't deliver your order #{1} — contact support to get your refund",
    Regex("^تعذّر توصيل طلبك #(\\S+) — تواصل مع المتجر.*$") to "Couldn't deliver your order #{1} — contact the store for a refund or to collect it",
    Regex("^المندوب في طريقه لاستلام طلبك من (.+)$") to "The courier is on the way to pick up your order from {1}",
    Regex("^لم يتأكد المتجر من إيصال التحويل.*$") to "The store hasn't confirmed the transfer receipt — contact the store or reorder",
)

fun trNotif(text: String?): String {
    if (text == null) return ""
    if (Lang.isAr) return text
    NOTIF_EXACT[text]?.let { return it }
    for ((rx, en) in NOTIF_RX) {
        val m = rx.find(text)
        if (m != null) {
            var out = en
            m.groupValues.forEachIndexed { i, g -> if (i > 0) out = out.replace("{$i}", g) }
            return out
        }
    }
    return text
}
