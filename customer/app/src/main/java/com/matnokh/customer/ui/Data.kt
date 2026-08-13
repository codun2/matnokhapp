package com.matnokh.customer.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.matnokh.customer.R

// ══ بيانات تجريبية (طبق الأصل من customer.html) ══

data class Cat(val id: String, val name: String, val emoji: String)
data class Store(
    val name: String, val cat: String, val catName: String, val emoji: String,
    val rating: String, val status: String, val statusLabel: String, val isNew: Boolean,
    val dist: String, val branches: List<String>, val mapX: Int, val mapY: Int,
)
data class Addon(val name: String, val emoji: String, val price: Int)
data class Product(
    val name: String, val emoji: String, val images: List<String>, val price: Int,
    val oldPrice: Int = 0, val desc: String = "", val outBranches: List<Int> = emptyList(),
)
data class Section(val name: String, val items: List<Product>)
data class Service(val id: String, val name: String, val desc: String, val iconId: Int, val gradient: Int, val vehicle: String, val base: Int, val tag: String? = null, val tagTerra: Boolean = false)
data class DriverBid(val name: String, val avatar: String, val rating: String, val eta: String, val priceDelta: Int)

object CData {
    val cats = listOf(
        Cat("all", "الكل", "🏬"), Cat("rest", "مطاعم", "🍽"), Cat("bake", "مخابز", "🥖"),
        Cat("groc", "بقالة", "🛒"), Cat("phar", "صيدليات", "💊"), Cat("cafe", "مقاهي", "☕️"),
    )
    val stores = listOf(
        Store("مخبز الأفراح", "bake", "مخابز", "🥖", "4.6", "live", "متاح", true, "0.8", listOf("الرئيسي — الرياض", "فرع النرجس"), 30, 28),
        Store("مطعم شام الأصيل", "rest", "مطاعم", "🍗", "4.8", "live", "متاح", true, "1.2", listOf("الرئيسي — الرياض", "فرع الملقا", "فرع العليا"), 62, 22),
        Store("سوبرماركت السلام", "groc", "بقالة وتموينات", "🛒", "4.5", "live", "متاح", true, "0.5", listOf("الرئيسي — الرياض", "فرع النرجس", "فرع الياسمين"), 44, 44),
        Store("مطعم القدس", "rest", "مطاعم", "🥙", "4.7", "wait", "مشغول", false, "2.1", listOf("الرئيسي — الرياض"), 24, 58),
        Store("مخبز الطازج", "bake", "مخابز", "🫓", "4.9", "live", "متاح", false, "3.4", listOf("الرئيسي — الرياض"), 72, 52),
        Store("صيدلية الشفاء", "phar", "صيدليات", "💊", "4.9", "live", "متاح", false, "1.6", listOf("الرئيسي — الرياض", "فرع الملقا"), 54, 68),
        Store("محمصة بن جبل النار", "cafe", "مقاهي", "☕️", "4.7", "ok", "يفتح 4 م", false, "2.8", listOf("الرئيسي — الرياض"), 80, 34),
    )
    val adds = mapOf(
        "rest" to listOf(Addon("سلطة", "🥗", 5), Addon("بطاطا", "🍟", 7), Addon("صوص ثوم", "🧄", 3)),
        "bake" to listOf(Addon("زعتر إضافي", "🌿", 2), Addon("جبنة", "🧀", 6), Addon("زيتون", "🫒", 4)),
        "groc" to emptyList(), "phar" to emptyList(),
        "cafe" to listOf(Addon("شوت إضافي", "☕️", 4), Addon("حليب لوز", "🥛", 5), Addon("كراميل", "🍮", 3)),
    )
    val menus: Map<String, List<Section>> = mapOf(
        "rest" to listOf(
            Section("الوجبات", listOf(
                Product("مشاوي مشكلة", "🍢", listOf("🍢", "🥩", "🍽"), 55, 65, "كباب وشقف وجناح دجاج مع مقبلات وخبز", listOf(2)),
                Product("نص فروج مشوي", "🍗", listOf("🍗", "🔥", "🍽"), 35, desc = "مع بطاطا وصوص الثوم"),
                Product("مندي لحم", "🍛", listOf("🍛", "🥘"), 48, 55, "لحم مع أرز بخاري"))),
            Section("سندويشات", listOf(
                Product("شاورما عربي", "🌯", listOf("🌯", "🥙", "🍽"), 18, 22, "لحم متبّل مع صوص السمسم وخضار"),
                Product("برجر لحم أنغوس", "🍔", listOf("🍔", "🧀", "🍟"), 25, desc = "مع جبنة شيدر وصوص خاص", outBranches = listOf(1)),
                Product("فلافل سبيشل", "🧆", listOf("🧆", "🥗"), 8, desc = "مع حمص وسلطة"))),
            Section("مشروبات", listOf(
                Product("عصير برتقال طازج", "🍊", listOf("🍊", "🥤"), 10, desc = "معصور لحظياً"),
                Product("ليمون بالنعنع", "🍋", listOf("🍋", "🌿", "🥤"), 9, desc = "منعش ومثلّج")))),
        "bake" to listOf(
            Section("مخبوزات", listOf(
                Product("مناقيش زعتر", "🫓", listOf("🫓", "🌿", "🔥"), 4, desc = "زعتر بزيت زيتون"),
                Product("كعك بسمسم", "🥯", listOf("🥯", "🥚"), 5, desc = "مع بيضة وزعتر"),
                Product("خبز تنّور", "🍞", listOf("🍞", "🔥"), 6, desc = "ربطة طازجة"))),
            Section("حلويات", listOf(
                Product("كنافة", "🧡", listOf("🧡", "🍯", "🍽"), 15, 18, "جبنة وقطر طازج"),
                Product("معمول تمر", "🍪", listOf("🍪", "🌴"), 12, desc = "علبة نص كيلو")))),
        "groc" to listOf(
            Section("خضار وفواكه", listOf(
                Product("طماطم — كغم", "🍅", listOf("🍅", "🧺"), 5),
                Product("خيار — كغم", "🥒", listOf("🥒", "🧺"), 4),
                Product("موز — كغم", "🍌", listOf("🍌", "🧺"), 8, 10))),
            Section("ألبان وأجبان", listOf(
                Product("لبنة", "🥣", listOf("🥣", "🫒"), 12, desc = "علبة كيلو"),
                Product("جبنة", "🧀", listOf("🧀", "🥛", "🍽"), 38, 42, "كيلو")))),
        "phar" to listOf(
            Section("العناية والصحة", listOf(
                Product("فيتامين C فوّار", "💊", listOf("💊", "🍊"), 25, desc = "20 قرص"),
                Product("شامبو طبي", "🧴", listOf("🧴", "💧"), 32, 38)))),
        "cafe" to listOf(
            Section("قهوة", listOf(
                Product("لاتيه", "☕️", listOf("☕️", "🥛", "🎨"), 12, desc = "حجم وسط"),
                Product("قهوة تركية", "🫖", listOf("🫖", "☕️"), 8, desc = "مع هيل"))),
            Section("حلويات", listOf(
                Product("كوكيز شوكولاتة", "🍪", listOf("🍪", "🍫"), 9)))),
    )
    val services = listOf(
        Service("fast", "توصيل سريع", "طرود ومستندات · حتى 15 كغم", R.drawable.ic_bike, 0, "دراجة نارية", 25, "الأسرع"),
        Service("goods", "مشتريات وبضائع", "من المتاجر والسوق · حتى 1 طن", R.drawable.ic_van, 1, "فان توصيل", 80),
        Service("furn", "نقل أثاث", "عفش ومكاتب · مع عمّال تحميل", R.drawable.ic_furn, 2, "شاحنة + عمّال", 160),
        Service("cold", "نقل مبرّد", "أغذية وأدوية · تحكّم بالحرارة", R.drawable.ic_cold, 1, "فان مبرّد", 140),
        Service("sand", "نقل رمل وحصمة", "مواد بناء", R.drawable.ic_heavy, 3, "قلّاب", 220),
        Service("crane", "نقل ثقيل", "معدات ورافعات", R.drawable.ic_crane, 2, "لوبد + رافعة", 650, "ثقيل", true),
    )
    val driverPool = emptyList<DriverBid>()

    // كل المنتجات المخفّضة عبر المتاجر
    fun allOffers(): List<OfferRow> {
        val out = mutableListOf<OfferRow>()
        stores.forEachIndexed { si, s ->
            (menus[s.cat] ?: emptyList()).forEach { sec ->
                sec.items.forEach { p ->
                    if (p.oldPrice > 0) out.add(OfferRow(s, si, p, sec.name, ((1 - p.price.toFloat() / p.oldPrice) * 100).toInt()))
                }
            }
        }
        return out.sortedByDescending { it.off }
    }
}

data class OfferRow(val store: Store, val storeIdx: Int, val product: Product, val section: String, val off: Int)

// السلة
data class CartLine(val productId: Int?, val name: String, val image: String?, val qty: Int, val addons: List<String>, val price: Double, val oldPrice: Double = 0.0)
object Cart {
    val lines = mutableStateListOf<CartLine>()
    var merchantId: Int? = null
    var branchId: Int? = null
    var storeName by androidx.compose.runtime.mutableStateOf("")
    fun count() = lines.sumOf { it.qty }
    fun total() = lines.sumOf { it.price }
    fun clear() { lines.clear(); merchantId = null; branchId = null; storeName = "" }
}

enum class PillKind { Live, Ok, Wait, Off, Rj }
