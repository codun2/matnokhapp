package com.matnokh.driver.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.matnokh.driver.R
import com.matnokh.driver.net.AvailBody
import com.matnokh.driver.net.StoreOrderDto
import com.matnokh.driver.net.BidBody
import com.matnokh.driver.net.JobDto
import com.matnokh.driver.net.LocBody
import com.matnokh.driver.net.Net
import com.matnokh.driver.net.Session
import com.matnokh.driver.net.StatusBody
import com.matnokh.driver.net.call

/* ── نماذج واجهة ── */
data class Job(
    val oid: Int, val id: String, val cust: String, val av: String, val svc: String, val iconId: Int,
    val gradient: Int, val from: String, val to: String, val km: String, val weight: String,
    val opts: String, val price: Int, val bid: Boolean, val status: String = "broadcasting", val note: String? = null,
    val fromLat: Double? = null, val fromLng: Double? = null, val toLat: Double? = null, val toLng: Double? = null, val isStore: Boolean = false, val companyFixed: Boolean = false,
)

data class DoneOrder(
    val id: String, val cust: String, val svc: String, val iconId: Int, val gradient: Int,
    val from: String, val to: String, val fare: Int, val dt: String, val rating: String? = null,
)

fun svcIcon(key: String?): Int = when (key) {
    "furniture" -> R.drawable.ic_furn
    "fast" -> R.drawable.ic_bike
    "cold" -> R.drawable.ic_cold
    "heavy" -> R.drawable.ic_heavy
    "errand" -> R.drawable.ic_box
    else -> R.drawable.ic_van
}
fun svcGrad(key: String?): Int = when (key) {
    "furniture" -> 1
    "cold", "errand" -> 2
    "heavy" -> 3
    else -> 0
}
fun statusToStep(s: String?): Int = when (s) {
    "assigned" -> 1; "ready" -> 1; "accepted" -> 1; "loaded" -> 2; "picked_up" -> 2; "on_the_way" -> 3; "delivered" -> 4; else -> 1
}

val DSTEPS = listOf(
    tr("تم الإسناد", "Assigned") to R.drawable.ic_check,
    tr("تم التحميل", "Loaded") to R.drawable.ic_box,
    tr("في الطريق", "On the way") to R.drawable.ic_van,
    tr("التسليم", "Delivery") to R.drawable.ic_flag,
)

private fun JobDto.toJob() = Job(
    oid = id, id = order_no ?: "WS-$id", cust = customer ?: tr("زبون", "Customer"),
    av = (customer ?: tr("زب", "Cu")).take(2), svc = service_name,
    iconId = svcIcon(service_key), gradient = svcGrad(service_key),
    from = from ?: "-", to = to ?: "-",
    km = km?.let { String.format("%.1f", it) } ?: tr("؟", "?"),
    weight = weight ?: "-", opts = options?.ifBlank { "—" } ?: "—", note = note,
    price = ((offer_price ?: final_fare ?: price)).toInt(), bid = if (company_fixed == true) false else (mode != "direct"), companyFixed = (company_fixed == true),
    status = status ?: "broadcasting",
    fromLat = from_lat, fromLng = from_lng, toLat = to_lat, toLng = to_lng,
)
private fun StoreOrderDto.toJob() = Job(
    oid = id, id = order_no ?: "WS-$id", cust = store ?: tr("متجر", "Store"), av = (store ?: tr("مت", "Ma")).take(2),
    svc = tr("توصيل: ", "Delivery: ") + (store ?: tr("متجر", "Store")), iconId = R.drawable.ic_box, gradient = 2,
    from = from ?: "-", to = to ?: "-", km = km?.let { String.format("%.1f", it) } ?: tr("؟", "?"),
    weight = "-", opts = "—", price = (offer_price ?: fee).toInt(), bid = false, companyFixed = (company_fixed == true), status = status ?: "ready",
    fromLat = from_lat, fromLng = from_lng, toLat = to_lat, toLng = to_lng, isStore = true,
)

private fun JobDto.toDone() = DoneOrder(
    id = order_no ?: "WS-$id", cust = customer ?: tr("زبون", "Customer"), svc = service_name,
    iconId = svcIcon(service_key), gradient = svcGrad(service_key),
    from = from ?: "-", to = to ?: "-", fare = ((final_fare ?: price)).toInt(),
    dt = dt ?: "", rating = "—",
)

/* ── الحالة العامة (مربوطة بالـ API) ── */
object Drv {
    val received = mutableStateListOf<Job>()
    val hidden = mutableStateListOf<Int>()
    val nowOrders = mutableStateListOf<Job>()
    val pastDone = mutableStateListOf<DoneOrder>()
    var available = mutableStateOf(false)
    var current = mutableStateOf<Job?>(null)
    var fare = mutableStateOf(0)
    var activeStep = mutableStateOf(1)
    var trackIntervalMin = mutableStateOf(5)

    // KPIs + الملف
    var tripsToday = mutableStateOf(0)
    var earningsToday = mutableStateOf(0)
    var rating = mutableStateOf("5.0")
    var balance = mutableStateOf(0)
    var name = mutableStateOf(tr("كابتن", "Captain"))
    var avatar = mutableStateOf(tr("كب", "Ca"))
    var avatarUrl = mutableStateOf<String?>(null)
    var plate = mutableStateOf<String?>(null)
    var vehiclePhoto = mutableStateOf<String?>(null)
    var nationalId = mutableStateOf<String?>(null)
    var license = mutableStateOf<String?>(null)
    val services = mutableStateListOf<String>()
    var licensePhoto = mutableStateOf<String?>(null)
    var idPhoto = mutableStateOf<String?>(null)
    var passportPhoto = mutableStateOf<String?>(null)
    var vehicle = mutableStateOf(tr("مركبتي", "My vehicle"))
    var city = mutableStateOf("—")
    var companyId = mutableStateOf<Int?>(null)
    var companyName = mutableStateOf("")
    var driverLat = mutableStateOf<Double?>(null)
    var driverLng = mutableStateOf<Double?>(null)
    var shiftToday = mutableStateOf<com.matnokh.driver.net.ShiftTodayResp?>(null)

    fun reset() {
        received.clear(); nowOrders.clear(); pastDone.clear(); hidden.clear()
        available.value = false; current.value = null; fare.value = 0; activeStep.value = 1
    }
}

/* ── دوال المستودع (Repo) ── */
suspend fun repoMe(toast: (String) -> Unit) {
    call({ Net.api.me() }, toast)?.driver?.let { d ->
        Drv.name.value = d.name; Drv.avatar.value = d.name.take(2); Drv.avatarUrl.value = d.avatar
        Drv.available.value = d.is_available; Drv.rating.value = String.format("%.1f", d.rating)
        Drv.balance.value = d.balance.toInt(); Drv.driverLat.value = d.lat; Drv.driverLng.value = d.lng
        Drv.vehicle.value = when (d.vehicle_type) {
            "small" -> tr("مركبة صغيرة", "Small vehicle"); "medium" -> tr("مركبة متوسطة", "Medium vehicle"); "large" -> tr("مركبة كبيرة", "Large vehicle"); else -> tr("مركبتي", "My vehicle")
        }
        Drv.plate.value = d.vehicle_plate; Drv.vehiclePhoto.value = d.vehicle_photo
        Drv.nationalId.value = d.national_id; Drv.license.value = d.license_number
        Drv.services.clear(); d.services?.let { Drv.services.addAll(it) }
        Drv.licensePhoto.value = d.license_photo; Drv.idPhoto.value = d.national_id_photo; Drv.passportPhoto.value = d.passport_photo
        Drv.companyId.value = d.delivery_company_id; Drv.companyName.value = d.company_name ?: ""
    }
}
suspend fun repoMyOffers(toast: (String) -> Unit): List<Job> =
    call({ Net.api.myOffers() }, toast)?.orders?.map { it.toJob() } ?: emptyList()

suspend fun repoNearby(toast: (String) -> Unit) {
    call({ Net.api.nearby() }, toast)?.let { r ->
        Drv.received.addAll(r.orders.map { it.toJob() }.filter { it.oid !in Drv.hidden })
    }
}
suspend fun repoStoreOrders(toast: (String) -> Unit) {
    call({ Net.api.storeOrders() }, toast)?.let { r -> Drv.received.addAll(r.orders.map { it.toJob() }) }
}
suspend fun repoStoreActive(toast: (String) -> Unit) {
    call({ Net.api.storeActive() }, toast)?.let { r ->
        Drv.nowOrders.addAll(r.orders.map { it.toJob() })
        val cur = Drv.nowOrders.firstOrNull()
        Drv.current.value = cur
        if (cur != null) { Drv.fare.value = cur.price; Drv.activeStep.value = statusToStep(cur.status) }
    }
}
suspend fun repoNow(toast: (String) -> Unit) {
    call({ Net.api.myOrders("now") }, toast)?.let { r ->
        Drv.nowOrders.clear(); Drv.nowOrders.addAll(r.orders.map { it.toJob() })
        val cur = Drv.nowOrders.firstOrNull()
        Drv.current.value = cur
        if (cur != null) { Drv.fare.value = cur.price; Drv.activeStep.value = statusToStep(cur.status) }
    }
}
// يحمّل الطلبات النشطة (نقل + متاجر مطنوخ) معاً بمسح واحد — يمنع محو أحدهما للآخر عند البدء/التحديث
suspend fun repoActive(toast: (String) -> Unit) {
    val list = mutableListOf<Job>()
    call({ Net.api.myOrders("now") }, toast)?.let { r -> list.addAll(r.orders.map { it.toJob() }) }
    call({ Net.api.storeActive() }, toast)?.let { r -> list.addAll(r.orders.map { it.toJob() }) }
    Drv.nowOrders.clear(); Drv.nowOrders.addAll(list)
    val cur = Drv.nowOrders.firstOrNull()
    Drv.current.value = cur
    if (cur != null) { Drv.fare.value = cur.price; Drv.activeStep.value = statusToStep(cur.status) }
}
suspend fun repoPast(toast: (String) -> Unit) {
    call({ Net.api.myOrders("past") }, toast)?.let { r ->
        Drv.pastDone.clear(); Drv.pastDone.addAll(r.orders.map { it.toDoneUi() })
    }
}
private fun JobDto.toDoneUi() = this.let {
    DoneOrder(order_no ?: "WS-$id", customer ?: tr("زبون", "Customer"), service_name, svcIcon(service_key), svcGrad(service_key), from ?: "-", to ?: "-", ((final_fare ?: price)).toInt(), dt ?: "", driver_rating?.let { String.format("%.1f", it) })
}
suspend fun repoDash(toast: (String) -> Unit) {
    call({ Net.api.dashboard() }, toast)?.let { d ->
        Drv.tripsToday.value = d.trips_today; Drv.earningsToday.value = d.earnings_today.toInt()
        Drv.rating.value = String.format("%.1f", d.rating); Drv.balance.value = d.balance.toInt()
    }
}
suspend fun repoSetAvailable(v: Boolean, toast: (String) -> Unit) {
    call({ Net.api.availability(AvailBody(v)) }, toast)?.let { Drv.available.value = it.is_available; toast(if (it.is_available) tr("أصبحت متاحًا لاستقبال الطلبات", "You're now available to receive orders") else tr("أصبحت غير متاح حاليًا", "You're now unavailable")) }
}
suspend fun repoBid(oid: Int, amount: Int, toast: (String) -> Unit): Boolean =
    call({ Net.api.bid(oid, BidBody(amount.toDouble())) }, toast)?.let { toast(it.message ?: tr("أُرسل عرضك", "Your offer was sent")); true } ?: false
suspend fun repoAccept(oid: Int, toast: (String) -> Unit): Boolean =
    call({ Net.api.accept(oid) }, toast)?.let { true } ?: false
suspend fun repoStatus(oid: Int, status: String, toast: (String) -> Unit): Boolean =
    call({ Net.api.updateStatus(oid, StatusBody(status)) }, toast)?.let { true } ?: false
suspend fun repoStoreBid(oid: Int, amount: Int, toast: (String) -> Unit): Boolean =
    call({ Net.api.storeBid(oid, BidBody(amount.toDouble())) }, toast)?.let { toast(it.message ?: tr("أُرسل عرضك", "Your offer was sent")); true } ?: false
suspend fun repoStoreStatus(oid: Int, status: String, toast: (String) -> Unit): Boolean =
    call({ Net.api.storeStatus(oid, StatusBody(status)) }, toast)?.let { true } ?: false
suspend fun repoStoreAccept(oid: Int, toast: (String) -> Unit): Boolean =
    call({ Net.api.storeAccept(oid) }, toast)?.let { toast(it.message ?: tr("تم إسناد الطلب إليك", "The order was assigned to you")); true } ?: false
suspend fun repoStoreReject(oid: Int, toast: (String) -> Unit): Boolean =
    call({ Net.api.storeReject(oid) }, toast)?.let { true } ?: false

suspend fun repoStoreRelinquish(oid: Int, toast: (String) -> Unit): Boolean =
    call({ Net.api.storeRelinquish(oid) }, toast)?.let { toast(it.message ?: ""); true } ?: false

suspend fun repoTransportRelinquish(oid: Int, toast: (String) -> Unit): Boolean =
    call({ Net.api.transportRelinquish(oid) }, toast)?.let { toast(it.message ?: ""); true } ?: false
suspend fun repoLocation(lat: Double, lng: Double, toast: (String) -> Unit) {
    call({ Net.api.location(LocBody(lat, lng)) }, toast)
}

suspend fun repoShiftToday(toast: (String) -> Unit) { Drv.shiftToday.value = call({ Net.api.shiftToday() }, toast) }
suspend fun repoShiftCheckIn(toast: (String) -> Unit) { call({ Net.api.shiftCheckIn() }, toast)?.let { toast(it.message ?: "") }; repoShiftToday(toast) }
suspend fun repoShiftCheckOut(toast: (String) -> Unit) { call({ Net.api.shiftCheckOut() }, toast)?.let { toast(it.message ?: "") }; repoShiftToday(toast) }
