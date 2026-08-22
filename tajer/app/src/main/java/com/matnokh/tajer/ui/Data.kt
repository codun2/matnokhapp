package com.matnokh.tajer.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

// ══ بيانات تجريبية ثابتة (طبق الأصل من merchant.html) — بدون باك-إند ══

data class Section(val name: String, val emoji: String)
data class Branch(val name: String, val city: String, val phone: String, val hours: String, var on: Boolean)
data class Addon(var emoji: String, var name: String, var price: Int)

class Product(
    val name: String,
    val images: List<String>,
    val section: String,
    val price: Int,
    val oldPrice: Int,          // 0 = لا يوجد عرض
    val addons: List<Addon>,
    status: String,             // active | draft | archived
    qty: List<Int>,
) {
    var status = mutableStateOf(status)
    val qty: SnapshotStateList<Int> = mutableStateListOf(*qty.toTypedArray())
}

class Order(
    val id: String,
    val customer: String,
    val branch: Int,
    val items: Int,
    val total: Int,
    status: String,             // new | prep | ready | withdriver | done | rejected
    val dt: String,
    val driver: String? = null,
) {
    var status = mutableStateOf(status)
}

object Demo {
    val sections = mutableStateListOf(
        Section("مشاوي", "🍖"),
        Section("وجبات رئيسية", "🍚"),
        Section("مشروبات", "🥤"),
        Section("حلويات", "🍰"),
    )

    val branches = mutableStateListOf(
        Branch("فرع العليا", "الرياض", "+966 11 200 1122", "12:00 – 02:00", true),
        Branch("فرع النخيل", "الرياض", "+966 11 240 5566", "12:00 – 02:00", true),
        Branch("فرع الروضة", "جدة", "+966 12 291 7788", "13:00 – 01:00", true),
    )

    val products = mutableStateListOf(
        Product("كبسة لحم — طبق كبير", listOf("🍚", "🍖"), "وجبات رئيسية", 45, 55, emptyList(), "active", listOf(40, 25, 18)),
        Product("مندي دجاج — نص", listOf("🍗"), "وجبات رئيسية", 32, 0, emptyList(), "active", listOf(12, 8, 0)),
        Product("مشاوي مشكّل — للأسرة", listOf("🍖"), "مشاوي", 120, 140, emptyList(), "active", listOf(20, 15, 10)),
        Product("شاورما عربي — لحم", listOf("🥙"), "وجبات رئيسية", 15, 0, emptyList(), "draft", listOf(0, 0, 0)),
        Product("عصير برتقال طازج", listOf("🥤"), "مشروبات", 12, 0, emptyList(), "archived", listOf(0, 0, 0)),
    )

    val orders = mutableStateListOf(
        Order("MT-4809", "عبدالله الحربي", 0, 12, 213, "new", "قبل 4 دقائق"),
        Order("MT-4812", "سارة الزهراني", 1, 5, 78, "new", "قبل 9 دقائق"),
        Order("MT-4805", "محمد القحطاني", 0, 7, 134, "withdriver", "اليوم 2:36 م", "أبو فهد"),
        Order("MT-4801", "نورة العتيبي", 2, 8, 96, "done", "أمس 6:20 م", "خالد الشمري"),
        Order("MT-4788", "شركة نجد", 0, 23, 412, "done", "20 يوليو", "ماجد الدوسري"),
    )

    val cities = listOf("الرياض", "جدة", "مكة المكرمة", "المدينة المنورة", "الدمام", "الخبر")
    val emojis = listOf("🍖", "🍗", "🍚", "🥙", "🥤", "🍰", "🍔", "🍟", "🥗", "🌮", "🧆", "🍕", "☕️", "🧃", "🍩", "🥛")
}

// حالة النشر → (التسمية، نوع الشارة)
enum class PillKind { Live, Ok, Wait, Off, Rj }
fun statusLabel(st: String): Pair<String, PillKind> = when (st) {
    "active" -> "نشط" to PillKind.Live
    "draft" -> "مسودة" to PillKind.Wait
    else -> "مؤرشف" to PillKind.Off
}

// حالة الطلب → (التسمية، نوع الشارة)
fun orderStatus(st: String): Pair<String, PillKind> = when (st) {
    "await_pay" -> "بانتظار تأكيد التحويل" to PillKind.Wait
    "new" -> "جديد" to PillKind.Wait
    "prep" -> "قيد التجهيز" to PillKind.Live
    "ready" -> "جاهز — بانتظار مندوب" to PillKind.Live
    "withdriver" -> "مع المندوب" to PillKind.Ok
    "done" -> "مكتمل" to PillKind.Ok
    else -> "مرفوض" to PillKind.Rj
}
