package com.matnokh.customer.net

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// نماذج واجهة مبنيّة من الـAPI
data class UiStore(val id: Int, val name: String, val categoryName: String, val logo: String?, val rating: String, val isOpen: Boolean, val branchesCount: Int, val dist: String, val lat: Double? = null, val lng: Double? = null)
data class UiAddon(val name: String, val price: Double)
data class UiProduct(val id: Int, val name: String, val desc: String, val price: Double, val oldPrice: Double, val images: List<String>, val addons: List<UiAddon>, val outBranches: List<Int> = emptyList())
data class UiSection(val id: Int, val name: String, val items: List<UiProduct>)
data class UiBranch(val id: Int, val name: String)
data class StoreDetailUi(val store: UiStore, val branches: List<UiBranch>, val sections: List<UiSection>)
data class UiOffer(val product: UiProduct, val storeId: Int, val storeName: String, val storeLogo: String?, val storeCategory: String, val off: Int)
data class UiPlace(val id: String, val name: String, val lat: Double, val lng: Double, val address: String, val type: String)
data class PlaceCat(val label: String, val gtype: String, val emoji: String, val hue: Float)
val PLACE_CATS = listOf(
    PlaceCat("مطاعم", "restaurant", "\uD83C\uDF7D\uFE0F", 0f),
    PlaceCat("وجبات سريعة", "meal_takeaway", "\uD83C\uDF54", 30f),
    PlaceCat("قهوة وحلويات", "cafe", "\u2615", 45f),
    PlaceCat("سوبرماركت", "supermarket", "\uD83D\uDED2", 120f),
    PlaceCat("بقالة", "convenience_store", "\uD83C\uDFEA", 200f),
    PlaceCat("صيدليات", "pharmacy", "\uD83D\uDC8A", 210f),
    PlaceCat("مخابز", "bakery", "\uD83E\uDD56", 60f),
    PlaceCat("ورد وهدايا", "florist", "\uD83C\uDF81", 300f),
    PlaceCat("إلكترونيات", "electronics_store", "\uD83D\uDCF1", 270f),
)

object Repo {
    var categories by mutableStateOf<List<CatDto>>(emptyList())
    var stores by mutableStateOf<List<UiStore>>(emptyList())
    var offers by mutableStateOf<List<UiOffer>>(emptyList())
    var detail by mutableStateOf<StoreDetailUi?>(null)
    var loaded by mutableStateOf(false)
    var places by mutableStateOf<List<UiPlace>>(emptyList())
    var services by mutableStateOf<List<SvcDto>>(emptyList())
    var kmMin by mutableStateOf(1.0)
    var kmMax by mutableStateOf(3.0)
    val favIds = mutableStateListOf<Int>()
    var here by mutableStateOf<Pair<Double, Double>?>(null)

    private fun StoreDto.toUi() = UiStore(id, store_name, category_name ?: "متجر", logo, String.format("%.1f", rating.coerceAtLeast(0.0)).let { if (rating <= 0) "جديد" else it }, is_open, branches_count, "%.1f".format((id % 4 + 5) / 10.0 + id % 3), lat, lng)
    fun toUiStores(list: List<StoreDto>): List<UiStore> = list.map { it.toUi() }
    private fun ProdDto.toUi(outB: List<Int> = emptyList()) = UiProduct(id, name, description ?: "", price, price_before, images, addons.map { UiAddon(it.name, it.price) }, outB)

    suspend fun loadHome() {
        categories = Net.api.categories().categories
        stores = Net.api.stores().stores.map { it.toUi() }
        offers = Net.api.offers().offers.map { o ->
            UiOffer(UiProduct(o.id, o.name, o.description ?: "", o.price, o.price_before, o.images, o.addons.map { UiAddon(it.name, it.price) }), o.store_id, o.store_name, o.store_logo, o.store_category ?: "", o.off)
        }
        runCatching { val p = Net.api.pricing(); kmMin = p.km_price_min; kmMax = p.km_price_max }
        runCatching { services = Net.api.services().services }
        if (Session.isLoggedIn()) runCatching { favIds.clear(); favIds.addAll(Net.api.favoriteIds().ids) }
        loaded = true
    }
    suspend fun favoriteStores(): List<UiStore> = runCatching { Net.api.favorites().stores.map { it.toUi() } }.getOrDefault(emptyList())
    suspend fun reloadStores() { stores = Net.api.stores().stores.map { it.toUi() } }
    suspend fun loadPlaces(lat: Double, lng: Double, type: String) { places = Net.api.nearbyPlaces(lat, lng, type).places.filter { it.lat != null && it.lng != null }.map { UiPlace(it.id ?: "", it.name, it.lat!!, it.lng!!, it.address, it.type) } }
    suspend fun loadStore(id: Int) {
        detail = null
        val r = Net.api.storeDetail(id)
        detail = StoreDetailUi(r.store.toUi(), r.branches.map { UiBranch(it.id, it.name) }, r.sections.map { sec ->
            UiSection(sec.id, sec.name, sec.items.map { p -> p.toUi(p.stock.filter { it.in_stock <= 0 }.map { s -> r.branches.indexOfFirst { it.id == s.branch_id } }.filter { it >= 0 }) })
        })
    }
}

suspend fun <T> call(block: suspend () -> T, toast: (String) -> Unit): T? = try { block() }
catch (e: retrofit2.HttpException) { toast(errorMessage(e) ?: "خطأ في الخادم"); null }
catch (e: Exception) { toast("تعذّر الاتصال بالخادم"); null }
