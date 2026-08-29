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
        Section(tr("مشاوي", "Grills"), "🍖"),
        Section(tr("وجبات رئيسية", "Main dishes"), "🍚"),
        Section(tr("مشروبات", "Drinks"), "🥤"),
        Section(tr("حلويات", "Sweets"), "🍰"),
    )

    val branches = mutableStateListOf(
        Branch(tr("فرع العليا", "Al-Olaya branch"), tr("الرياض", "Riyadh"), "+966 11 200 1122", "12:00 – 02:00", true),
        Branch(tr("فرع النخيل", "Al-Nakheel branch"), tr("الرياض", "Riyadh"), "+966 11 240 5566", "12:00 – 02:00", true),
        Branch(tr("فرع الروضة", "Al-Rawdah branch"), tr("جدة", "Jeddah"), "+966 12 291 7788", "13:00 – 01:00", true),
    )

    val products = mutableStateListOf(
        Product(tr("كبسة لحم — طبق كبير", "Meat Kabsa — large plate"), listOf("🍚", "🍖"), tr("وجبات رئيسية", "Main dishes"), 45, 55, emptyList(), "active", listOf(40, 25, 18)),
        Product(tr("مندي دجاج — نص", "Chicken mandi — half"), listOf("🍗"), tr("وجبات رئيسية", "Main dishes"), 32, 0, emptyList(), "active", listOf(12, 8, 0)),
        Product(tr("مشاوي مشكّل — للأسرة", "Mixed grill — family size"), listOf("🍖"), tr("مشاوي", "Grills"), 120, 140, emptyList(), "active", listOf(20, 15, 10)),
        Product(tr("شاورما عربي — لحم", "Arabic shawarma — meat"), listOf("🥙"), tr("وجبات رئيسية", "Main dishes"), 15, 0, emptyList(), "draft", listOf(0, 0, 0)),
        Product(tr("عصير برتقال طازج", "Fresh orange juice"), listOf("🥤"), tr("مشروبات", "Drinks"), 12, 0, emptyList(), "archived", listOf(0, 0, 0)),
    )

    val orders = mutableStateListOf(
        Order("MT-4809", tr("عبدالله الحربي", "Abdullah Al-Harbi"), 0, 12, 213, "new", tr("قبل 4 دقائق", "4 minutes ago")),
        Order("MT-4812", tr("سارة الزهراني", "Sara Al-Zahrani"), 1, 5, 78, "new", tr("قبل 9 دقائق", "9 minutes ago")),
        Order("MT-4805", tr("محمد القحطاني", "Mohammed Al-Qahtani"), 0, 7, 134, "withdriver", tr("اليوم 2:36 م", "Today 2:36 PM"), tr("أبو فهد", "Abu Fahd")),
        Order("MT-4801", tr("نورة العتيبي", "Noura Al-Otaibi"), 2, 8, 96, "done", tr("أمس 6:20 م", "Yesterday 6:20 PM"), tr("خالد الشمري", "Khalid Al-Shammari")),
        Order("MT-4788", tr("شركة نجد", "Najd Company"), 0, 23, 412, "done", tr("20 يوليو", "20 July"), tr("ماجد الدوسري", "Majed Al-Dosari")),
    )

    val cities = listOf(tr("الرياض", "Riyadh"), tr("جدة", "Jeddah"), tr("مكة المكرمة", "Makkah"), tr("المدينة المنورة", "Madinah"), tr("الدمام", "Dammam"), tr("الخبر", "Al-Khobar"))
    val emojis = listOf("🍖", "🍗", "🍚", "🥙", "🥤", "🍰", "🍔", "🍟", "🥗", "🌮", "🧆", "🍕", "☕️", "🧃", "🍩", "🥛")
}

// حالة النشر → (التسمية، نوع الشارة)
enum class PillKind { Live, Ok, Wait, Off, Rj }
fun statusLabel(st: String): Pair<String, PillKind> = when (st) {
    "active" -> tr("نشط", "Active") to PillKind.Live
    "draft" -> tr("مسودة", "Draft") to PillKind.Wait
    else -> tr("مؤرشف", "Archived") to PillKind.Off
}

// حالة الطلب → (التسمية، نوع الشارة)
fun orderStatus(st: String): Pair<String, PillKind> = when (st) {
    "await_pay" -> tr("بانتظار تأكيد التحويل", "Awaiting transfer confirmation") to PillKind.Wait
    "new" -> tr("جديد", "New") to PillKind.Wait
    "prep" -> tr("قيد التجهيز", "Preparing") to PillKind.Live
    "ready" -> tr("جاهز — بانتظار مندوب", "Ready — awaiting a courier") to PillKind.Live
    "withdriver" -> tr("مع المندوب", "With the courier") to PillKind.Ok
    "done" -> tr("مكتمل", "Completed") to PillKind.Ok
    else -> tr("مرفوض", "Rejected") to PillKind.Rj
}
