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
        Cat("all", tr("الكل", "All"), "🏬"), Cat("rest", tr("مطاعم", "Restaurants"), "🍽"), Cat("bake", tr("مخابز", "Bakeries"), "🥖"),
        Cat("groc", tr("بقالة", "Grocery"), "🛒"), Cat("phar", tr("صيدليات", "Pharmacies"), "💊"), Cat("cafe", tr("مقاهي", "Cafés"), "☕️"),
    )
    val stores = listOf(
        Store(tr("مخبز الأفراح", "Al-Afrah Bakery"), "bake", tr("مخابز", "Bakeries"), "🥖", "4.6", "live", tr("متاح", "Available"), true, "0.8", listOf(tr("الرئيسي — الرياض", "Main — Riyadh"), tr("فرع النرجس", "Al-Narjis branch")), 30, 28),
        Store(tr("مطعم شام الأصيل", "Sham Al-Aseel Restaurant"), "rest", tr("مطاعم", "Restaurants"), "🍗", "4.8", "live", tr("متاح", "Available"), true, "1.2", listOf(tr("الرئيسي — الرياض", "Main — Riyadh"), tr("فرع الملقا", "Al-Malqa branch"), tr("فرع العليا", "Al-Olaya branch")), 62, 22),
        Store(tr("سوبرماركت السلام", "Al-Salam Supermarket"), "groc", tr("بقالة وتموينات", "Grocery & supplies"), "🛒", "4.5", "live", tr("متاح", "Available"), true, "0.5", listOf(tr("الرئيسي — الرياض", "Main — Riyadh"), tr("فرع النرجس", "Al-Narjis branch"), tr("فرع الياسمين", "Al-Yasmin branch")), 44, 44),
        Store(tr("مطعم القدس", "Al-Quds Restaurant"), "rest", tr("مطاعم", "Restaurants"), "🥙", "4.7", "wait", tr("مشغول", "Busy"), false, "2.1", listOf(tr("الرئيسي — الرياض", "Main — Riyadh")), 24, 58),
        Store(tr("مخبز الطازج", "Al-Tazaj Bakery"), "bake", tr("مخابز", "Bakeries"), "🫓", "4.9", "live", tr("متاح", "Available"), false, "3.4", listOf(tr("الرئيسي — الرياض", "Main — Riyadh")), 72, 52),
        Store(tr("صيدلية الشفاء", "Al-Shifa Pharmacy"), "phar", tr("صيدليات", "Pharmacies"), "💊", "4.9", "live", tr("متاح", "Available"), false, "1.6", listOf(tr("الرئيسي — الرياض", "Main — Riyadh"), tr("فرع الملقا", "Al-Malqa branch")), 54, 68),
        Store(tr("محمصة بن جبل النار", "Jabal Al-Nar Roastery"), "cafe", tr("مقاهي", "Cafés"), "☕️", "4.7", "ok", tr("يفتح 4 م", "Opens 4 PM"), false, "2.8", listOf(tr("الرئيسي — الرياض", "Main — Riyadh")), 80, 34),
    )
    val adds = mapOf(
        "rest" to listOf(Addon(tr("سلطة", "Salad"), "🥗", 5), Addon(tr("بطاطا", "Potato"), "🍟", 7), Addon(tr("صوص ثوم", "Garlic sauce"), "🧄", 3)),
        "bake" to listOf(Addon(tr("زعتر إضافي", "Extra zaatar"), "🌿", 2), Addon(tr("جبنة", "Cheese"), "🧀", 6), Addon(tr("زيتون", "Olives"), "🫒", 4)),
        "groc" to emptyList(), "phar" to emptyList(),
        "cafe" to listOf(Addon(tr("شوت إضافي", "Extra shot"), "☕️", 4), Addon(tr("حليب لوز", "Almond milk"), "🥛", 5), Addon(tr("كراميل", "Caramel"), "🍮", 3)),
    )
    val menus: Map<String, List<Section>> = mapOf(
        "rest" to listOf(
            Section(tr("الوجبات", "Meals"), listOf(
                Product(tr("مشاوي مشكلة", "Mixed grill"), "🍢", listOf("🍢", "🥩", "🍽"), 55, 65, tr("كباب وشقف وجناح دجاج مع مقبلات وخبز", "Kebab, chops & chicken wings with sides and bread"), listOf(2)),
                Product(tr("نص فروج مشوي", "Half grilled chicken"), "🍗", listOf("🍗", "🔥", "🍽"), 35, desc = tr("مع بطاطا وصوص الثوم", "With fries and garlic sauce")),
                Product(tr("مندي لحم", "Meat mandi"), "🍛", listOf("🍛", "🥘"), 48, 55, tr("لحم مع أرز بخاري", "Meat with Bukhari rice")))),
            Section(tr("سندويشات", "Sandwiches"), listOf(
                Product(tr("شاورما عربي", "Arabic shawarma"), "🌯", listOf("🌯", "🥙", "🍽"), 18, 22, tr("لحم متبّل مع صوص السمسم وخضار", "Marinated meat with sesame sauce and veggies")),
                Product(tr("برجر لحم أنغوس", "Angus beef burger"), "🍔", listOf("🍔", "🧀", "🍟"), 25, desc = tr("مع جبنة شيدر وصوص خاص", "With cheddar and special sauce"), outBranches = listOf(1)),
                Product(tr("فلافل سبيشل", "Falafel special"), "🧆", listOf("🧆", "🥗"), 8, desc = tr("مع حمص وسلطة", "With hummus and salad")))),
            Section(tr("مشروبات", "Drinks"), listOf(
                Product(tr("عصير برتقال طازج", "Fresh orange juice"), "🍊", listOf("🍊", "🥤"), 10, desc = tr("معصور لحظياً", "Freshly squeezed")),
                Product(tr("ليمون بالنعنع", "Lemon with mint"), "🍋", listOf("🍋", "🌿", "🥤"), 9, desc = tr("منعش ومثلّج", "Refreshing & iced"))))),
        "bake" to listOf(
            Section(tr("مخبوزات", "Baked goods"), listOf(
                Product(tr("مناقيش زعتر", "Zaatar manakish"), "🫓", listOf("🫓", "🌿", "🔥"), 4, desc = tr("زعتر بزيت زيتون", "Zaatar with olive oil")),
                Product(tr("كعك بسمسم", "Sesame cake"), "🥯", listOf("🥯", "🥚"), 5, desc = tr("مع بيضة وزعتر", "With egg and zaatar")),
                Product(tr("خبز تنّور", "Tannour bread"), "🍞", listOf("🍞", "🔥"), 6, desc = tr("ربطة طازجة", "Fresh bunch")))),
            Section(tr("حلويات", "Sweets"), listOf(
                Product(tr("كنافة", "Kunafa"), "🧡", listOf("🧡", "🍯", "🍽"), 15, 18, tr("جبنة وقطر طازج", "Fresh cheese & syrup")),
                Product(tr("معمول تمر", "Date maamoul"), "🍪", listOf("🍪", "🌴"), 12, desc = tr("علبة نص كيلو", "½ kg box"))))),
        "groc" to listOf(
            Section(tr("خضار وفواكه", "Vegetables & fruits"), listOf(
                Product(tr("طماطم — كغم", "Tomatoes — kg"), "🍅", listOf("🍅", "🧺"), 5),
                Product(tr("خيار — كغم", "Cucumber — kg"), "🥒", listOf("🥒", "🧺"), 4),
                Product(tr("موز — كغم", "Bananas — kg"), "🍌", listOf("🍌", "🧺"), 8, 10))),
            Section(tr("ألبان وأجبان", "Dairy & cheese"), listOf(
                Product(tr("لبنة", "Labneh"), "🥣", listOf("🥣", "🫒"), 12, desc = tr("علبة كيلو", "1 kg box")),
                Product(tr("جبنة", "Cheese"), "🧀", listOf("🧀", "🥛", "🍽"), 38, 42, tr("كيلو", "kg"))))),
        "phar" to listOf(
            Section(tr("العناية والصحة", "Care & health"), listOf(
                Product(tr("فيتامين C فوّار", "Vitamin C effervescent"), "💊", listOf("💊", "🍊"), 25, desc = tr("20 قرص", "20 tablets")),
                Product(tr("شامبو طبي", "Medical shampoo"), "🧴", listOf("🧴", "💧"), 32, 38)))),
        "cafe" to listOf(
            Section(tr("قهوة", "Coffee"), listOf(
                Product(tr("لاتيه", "Latte"), "☕️", listOf("☕️", "🥛", "🎨"), 12, desc = tr("حجم وسط", "Medium size")),
                Product(tr("قهوة تركية", "Turkish coffee"), "🫖", listOf("🫖", "☕️"), 8, desc = tr("مع هيل", "With cardamom")))),
            Section(tr("حلويات", "Sweets"), listOf(
                Product(tr("كوكيز شوكولاتة", "Chocolate cookies"), "🍪", listOf("🍪", "🍫"), 9)))),
    )
    val services = listOf(
        Service("fast", tr("توصيل سريع", "Fast delivery"), tr("طرود ومستندات · حتى 15 كغم", "Parcels & documents · up to 15 kg"), R.drawable.ic_bike, 0, tr("دراجة نارية", "Motorcycle"), 25, tr("الأسرع", "Fastest")),
        Service("goods", tr("مشتريات وبضائع", "Purchases & goods"), tr("من المتاجر والسوق · حتى 1 طن", "From stores & the market · up to 1 ton"), R.drawable.ic_van, 1, tr("فان توصيل", "Delivery van"), 80),
        Service("furn", tr("نقل أثاث", "Furniture moving"), tr("عفش ومكاتب · مع عمّال تحميل", "Furniture & offices · with loading workers"), R.drawable.ic_furn, 2, tr("شاحنة + عمّال", "Truck + workers"), 160),
        Service("cold", tr("نقل مبرّد", "Refrigerated transport"), tr("أغذية وأدوية · تحكّم بالحرارة", "Food & medicine · temperature-controlled"), R.drawable.ic_cold, 1, tr("فان مبرّد", "Refrigerated van"), 140),
        Service("sand", tr("نقل رمل وحصمة", "Sand & gravel transport"), tr("مواد بناء", "Building materials"), R.drawable.ic_heavy, 3, tr("قلّاب", "Tipper"), 220),
        Service("crane", tr("نقل ثقيل", "Heavy transport"), tr("معدات ورافعات", "Equipment & cranes"), R.drawable.ic_crane, 2, tr("لوبد + رافعة", "Lowbed + crane"), 650, tr("ثقيل", "Heavy"), true),
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
