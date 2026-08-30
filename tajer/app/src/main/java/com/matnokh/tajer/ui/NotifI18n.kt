package com.matnokh.tajer.ui

/**
 * Runtime translation of backend-stored Arabic dynamic text (wallet transaction
 * titles) when the app language is English. Backend stores Arabic only, so we
 * translate the known fixed phrases and keep the variable order number intact.
 */
fun trData(text: String?): String {
    val t = text?.trim() ?: return ""
    if (Lang.isAr) return t
    // "أرباح طلب #<order_no>"
    if (t.startsWith("أرباح طلب #")) return "Order earnings #" + t.removePrefix("أرباح طلب #")
    // "دفع مباشر (تحويل) · طلب #<order_no>"
    if (t.startsWith("دفع مباشر (تحويل) · طلب #")) return "Direct payment (transfer) · Order #" + t.removePrefix("دفع مباشر (تحويل) · طلب #")
    if (t.startsWith("طلب · دفع مباشر (تحويل) #")) return "Order · direct payment (transfer) #" + t.removePrefix("طلب · دفع مباشر (تحويل) #")
    return t
}
