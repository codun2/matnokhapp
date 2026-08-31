package com.matnokh.driver.net

import com.google.gson.JsonParser
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.HttpException
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

const val API_BASE = "https://matnokh.bytnova.com/api/"

data class DriverBrief(
    val id: Int, val name: String, val phone: String?, val email: String?, val avatar: String?,
    val vehicle_type: String?, val vehicle_plate: String?, val city_id: Int?, val status: String,
    val is_available: Boolean = false, val balance: Double = 0.0, val rating: Double = 5.0,
    val lat: Double? = null, val lng: Double? = null,
    val vehicle_photo: String? = null, val national_id: String? = null, val license_number: String? = null, val services: List<String>? = null,
    val license_photo: String? = null, val national_id_photo: String? = null, val passport_photo: String? = null,
    val delivery_company_id: Int? = null, val company_name: String? = null,
)
data class MethodsResp(val methods: List<String>)
data class SvcLite(val id: Int, val name: String, val name_en: String? = null, val key: String, val vehicle_sizes: List<String> = emptyList())
data class DriverServicesResp(val services: List<SvcLite>)
data class DrvPackage(val id: Int, val name: String, val service: String? = null, val service_key: String? = null, val price: Double = 0.0, val duration_days: Int = 30)
data class DrvPackagesResp(val packages: List<DrvPackage> = emptyList())
data class DrvSub(val id: Int, val status: String, val service: String? = null, val service_key: String? = null, val price: Double = 0.0, val ends_at: String? = null, val days_left: Int? = null)
data class DrvSubsResp(val subscriptions: List<DrvSub> = emptyList(), val has_active: Boolean = false)
data class PaySubBody(val driver_subscription_id: Int)
data class PayResp(val payment_url: String? = null)
data class PayInfoResp(val bank_name: String? = null, val bank_iban: String? = null, val tap_enabled: Boolean = false, val bank_enabled: Boolean = true, val cash_enabled: Boolean = true)
data class ManualPayBody(val driver_subscription_id: Int, val method: String, val receipt: String? = null)
data class VModel(val id: Int, val name: String)
data class VMake(val id: Int, val name: String, val models: List<VModel> = emptyList())
data class VehicleMakesResp(val makes: List<VMake> = emptyList())
data class LoginBody(val method: String, val phone: String? = null, val email: String? = null, val password: String? = null, val code: String? = null)
data class LoginResp(val token: String?, val driver: DriverBrief?, val message: String?, val status: String?)
data class RegisterBody(val name: String, val phone: String, val email: String?, val password: String, val vehicle_type: String, val vehicle_plate: String? = null, val services: List<String>? = null, val vehicle_photo: String? = null, val license_photo: String? = null, val national_id_photo: String? = null, val passport_photo: String? = null, val national_id: String? = null, val license_number: String? = null, val vehicle_make: String? = null, val vehicle_model: String? = null, val vehicle_year: String? = null, val documents: List<DocItem>? = null, val package_ids: List<Int>? = null)
data class DocItem(val document_type_id: Int, val value: String)
data class DocType(val id: Int, val key: String? = null, val name: String, val description: String? = null, val field: String? = null, val required: Boolean = false, val transport_only: Boolean = false)
data class DocTypesResp(val data: List<DocType> = emptyList())
data class StoreOrderDto(val id: Int, val order_no: String?, val store: String?, val store_logo: String?, val from: String?, val to: String?, val items_total: Double = 0.0, val fee: Double = 0.0, val payment_method: String?, val km: Double?, val my_bid: Double?, val from_lat: Double? = null, val from_lng: Double? = null, val to_lat: Double?, val to_lng: Double?, val status: String?, val dt: String?, val company_fixed: Boolean? = null, val offer_price: Double? = null)
data class StoreOrdersResp(val orders: List<StoreOrderDto>)
data class MsgResp(val message: String?, val status: String?, val dev_code: String?)
data class MeResp(val driver: DriverBrief?)
data class CompanyLite(val id: Int = 0, val name: String = "", val phone: String? = null)
data class SettlementItem(val amount: Double = 0.0, val orders_count: Int = 0, val settled_at: String? = null, val method: String? = null, val reference: String? = null, val note: String? = null, val payment_proof: String? = null)
data class CompanyAccountResp(val company: CompanyLite? = null, val balance: Double = 0.0, val settlements: List<SettlementItem> = emptyList())
data class ChatMsg(val id: Int, val sender: String? = null, val body: String? = null, val image: String? = null, val at: String? = null, val mine: Boolean = false)
data class ChatResp(val thread_id: Int = 0, val locked: Boolean = false, val messages: List<ChatMsg> = emptyList())
data class ChatSendBody(val body: String? = null, val image: String? = null)
data class ChatSendResp(val id: Int = 0)
data class UploadResp(val url: String?)
data class ProfileBody(val name: String? = null, val email: String? = null, val avatar: String? = null)

data class JobDto(
    val id: Int, val order_no: String?, val service_key: String?, val service_name: String,
    val customer: String?, val from: String?, val to: String?, val weight: String?, val options: String?,
    val note: String?, val mode: String?, val price: Double = 0.0, val payment_method: String?,
    val status: String?, val final_fare: Double?, val km: Double?, val my_bid: Double?, val dt: String?,
    val company_fixed: Boolean? = null, val offer_price: Double? = null,
    val from_lat: Double? = null, val from_lng: Double? = null, val to_lat: Double? = null, val to_lng: Double? = null,
    val driver_rating: Double? = null,
)
data class OrdersResp(val orders: List<JobDto>)
data class DashResp(val trips_today: Int = 0, val earnings_today: Double = 0.0, val rating: Double = 5.0, val balance: Double = 0.0)
data class AvailBody(val is_available: Boolean)
data class NotifItem(val title: String = "", val body: String = "", val type: String? = null, val dt: String? = null, val title_en: String? = null, val body_en: String? = null)
data class NotifResp(val notifications: List<NotifItem> = emptyList())
data class EarnDay(val label: String = "", val amount: Double = 0.0)
data class EarnOp(val title: String = "", val dt: String = "", val amount: Double = 0.0, val key: String? = null)
data class EarnResp(val balance: Double = 0.0, val week: Double = 0.0, val trips_week: Int = 0, val days: List<EarnDay> = emptyList(), val operations: List<EarnOp> = emptyList())
data class AvailResp(val is_available: Boolean, val message: String?)
data class LocBody(val lat: Double, val lng: Double)
data class ShiftInfo(val id: Int, val name: String, val start: String, val end: String)
data class ShiftTodayResp(val shift: ShiftInfo?, val status: String?, val check_in: String?, val check_out: String?)
data class ConfigResp(val tracking_interval_min: Int = 5, val dispatch_radius_km: Double = 0.0)
data class BidBody(val amount: Double)
data class StatusBody(val status: String)

interface DriverApi {
    @GET("driver/login-method") suspend fun methods(): MethodsResp
    @POST("driver/login") suspend fun login(@Body b: LoginBody): LoginResp
    @POST("driver/register") suspend fun register(@Body b: RegisterBody): MsgResp
    @GET("driver/document-types") suspend fun docTypes(): DocTypesResp
    @GET("driver/services") suspend fun driverServices(): DriverServicesResp
    @GET("driver/packages") suspend fun driverPackages(@Query("vehicle") vehicle: String): DrvPackagesResp
    @GET("driver/subscriptions") suspend fun driverSubscriptions(): DrvSubsResp
    @POST("driver/subscription/pay") suspend fun driverPaySub(@Body body: PaySubBody): PayResp
    @GET("driver/pay-info") suspend fun driverPayInfo(): PayInfoResp
    @POST("driver/subscription/manual") suspend fun driverManualPay(@Body body: ManualPayBody): MsgResp
    @GET("driver/vehicle-makes") suspend fun vehicleMakes(): VehicleMakesResp
    @Multipart @POST("driver/register-upload") suspend fun registerUpload(@Part file: MultipartBody.Part): UploadResp
    @POST("driver/request-otp") suspend fun requestOtp(@Body b: Map<String, String>): MsgResp
    @GET("driver/me") suspend fun me(): MeResp
    @GET("driver/company-account") suspend fun companyAccount(): CompanyAccountResp
    @GET("driver/chat/{kind}/{id}") suspend fun chatShow(@Path("kind") kind: String, @Path("id") id: Int, @Query("after") after: Int = 0): ChatResp
    @POST("driver/chat/{kind}/{id}") suspend fun chatSend(@Path("kind") kind: String, @Path("id") id: Int, @Body b: ChatSendBody): ChatSendResp
    @Multipart @POST("driver/uploads") suspend fun upload(@Part file: MultipartBody.Part): UploadResp
    @PATCH("driver/profile") suspend fun updateProfile(@Body b: ProfileBody): MsgResp
    @POST("driver/logout") suspend fun logout(): MsgResp
    @POST("driver/device-token") suspend fun registerDeviceToken(@Body b: Map<String, String>): MsgResp
    @PATCH("driver/availability") suspend fun availability(@Body b: AvailBody): AvailResp
    @PATCH("driver/location") suspend fun location(@Body b: LocBody): MsgResp
    @GET("driver/shift/today") suspend fun shiftToday(): ShiftTodayResp
    @POST("driver/shift/check-in") suspend fun shiftCheckIn(): MsgResp
    @POST("driver/shift/check-out") suspend fun shiftCheckOut(): MsgResp
    @GET("config") suspend fun config(): ConfigResp
    @GET("driver/orders/nearby") suspend fun nearby(): OrdersResp
    @GET("driver/store-orders") suspend fun storeOrders(): StoreOrdersResp
    @GET("driver/store-active") suspend fun storeActive(): StoreOrdersResp
    @GET("driver/notifications") suspend fun notifications(): NotifResp
    @retrofit2.http.POST("driver/language") suspend fun setLanguage(@retrofit2.http.Body b: LangBody): LangResp
    @GET("driver/my-offers") suspend fun myOffers(): OrdersResp
    @GET("driver/earnings") suspend fun earnings(): EarnResp
    @POST("driver/store-orders/{id}/bid") suspend fun storeBid(@Path("id") id: Int, @Body b: BidBody): MsgResp
    @PATCH("driver/store-orders/{id}/status") suspend fun storeStatus(@Path("id") id: Int, @Body b: StatusBody): MsgResp
    @POST("driver/store-orders/{id}/accept") suspend fun storeAccept(@Path("id") id: Int): MsgResp
    @POST("driver/store-orders/{id}/reject") suspend fun storeReject(@Path("id") id: Int): MsgResp
    @POST("driver/store-orders/{id}/relinquish") suspend fun storeRelinquish(@Path("id") id: Int): MsgResp
    @POST("driver/orders/{id}/relinquish") suspend fun transportRelinquish(@Path("id") id: Int): MsgResp
    @GET("driver/orders") suspend fun myOrders(@Query("tab") tab: String): OrdersResp
    @GET("driver/dashboard") suspend fun dashboard(): DashResp
    @POST("driver/orders/{id}/bid") suspend fun bid(@Path("id") id: Int, @Body b: BidBody): MsgResp
    @POST("driver/orders/{id}/accept") suspend fun accept(@Path("id") id: Int): MsgResp
    @PATCH("driver/orders/{id}/status") suspend fun updateStatus(@Path("id") id: Int, @Body b: StatusBody): MsgResp
}

object Net {
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val b = chain.request().newBuilder().header("Accept", "application/json")
            Session.token?.let { if (it.isNotBlank()) b.header("Authorization", "Bearer $it") }
            chain.proceed(b.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    val api: DriverApi = Retrofit.Builder()
        .baseUrl(API_BASE).client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(DriverApi::class.java)
}

fun errorMessage(e: HttpException): String? = try {
    val body = e.response()?.errorBody()?.string()
    if (body.isNullOrBlank()) null else {
        val obj = JsonParser.parseString(body).asJsonObject
        when {
            obj.has("message") && !obj.get("message").isJsonNull -> obj.get("message").asString
            obj.has("errors") -> obj.getAsJsonObject("errors").entrySet().firstOrNull()?.value?.asJsonArray?.firstOrNull()?.asString
            else -> null
        }
    }
} catch (_: Exception) { null }

data class LangBody(val lang: String)
data class LangResp(val lang: String? = null)
