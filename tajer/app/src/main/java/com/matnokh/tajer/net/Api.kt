package com.matnokh.tajer.net

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

const val API_BASE = "https://matnokh.bytnova.com/api/"

// ── نماذج المصادقة ──
data class MerchantBrief(
    val id: Int, val store_name: String, val owner_name: String?, val phone: String?, val email: String?,
    val logo: String?, val status: String, val is_open: Boolean = false, val prep_mode: Boolean = false,
    val auto_accept: Boolean = false, val balance: Double = 0.0, val rating: Double = 0.0,
)
data class MethodsResp(val methods: List<String>)
data class LoginBody(val method: String, val phone: String? = null, val email: String? = null, val password: String? = null, val code: String? = null)
data class LoginResp(val token: String?, val merchant: MerchantBrief?, val message: String?, val status: String?)
data class RegisterBody(val store_name: String, val owner_name: String, val phone: String, val email: String?, val password: String, val store_category_id: Int? = null, val lat: Double? = null, val lng: Double? = null, val license_photo: String? = null, val commercial_register_photo: String? = null, val manager_phone: String? = null, val manager_id_photo: String? = null, val subscription_plan_id: Int? = null)
data class MsgResp(val message: String?, val status: String?, val dev_code: String?)
data class MeResp(val merchant: MerchantBrief?)
data class NotifItem(val id: Int, val title: String, val body: String, val dt: String?)
data class NotificationsResp(val notifications: List<NotifItem>)
data class PlanDto(val id: Int, val name: String, val type: String = "regular", val price: Double = 0.0, val duration_days: Int = 30, val features: List<String> = emptyList(), val description: String? = null)
data class PlansResp(val plans: List<PlanDto> = emptyList())
data class SubDto(val id: Int, val status: String, val plan_name: String?, val plan_type: String?, val price: Double = 0.0, val starts_at: String?, val ends_at: String?, val days_left: Int?)
data class SubResp(val subscription: SubDto?)
data class PayResp(val payment_url: String? = null)
data class SubscribeBody(val subscription_plan_id: Int)

// ── نماذج المتجر ──
data class StoreData(
    val id: Int, val store_name: String?, val owner_name: String?, val phone: String?, val email: String?,
    val address: String?, val city_id: Int?, val city: String?, val store_category_id: Int?,
    val lat: Double?, val lng: Double?, val logo: String?, val status: String?,
    val rating: Double = 0.0, val balance: Double = 0.0,
    val is_open: Boolean = false, val prep_mode: Boolean = false, val auto_accept: Boolean = false,
    val delivery_mode: String? = null, val delivery_fixed: Double = 0.0, val delivery_per_km: Double = 0.0,
    val branches_count: Int = 0, val sections_count: Int = 0, val products_count: Int = 0,
    val iban: String? = null, val bank_name: String? = null, val account_name: String? = null,
)
data class StoreResp(val store: StoreData, val message: String? = null)
data class StoreUpdate(
    val store_name: String? = null, val owner_name: String? = null, val address: String? = null,
    val city_id: Int? = null, val lat: Double? = null, val lng: Double? = null, val logo: String? = null,
    val is_open: Boolean? = null, val prep_mode: Boolean? = null, val auto_accept: Boolean? = null,
    val delivery_mode: String? = null, val delivery_fixed: Double? = null, val delivery_per_km: Double? = null,
    val iban: String? = null, val bank_name: String? = null, val account_name: String? = null,
)

data class BranchDto(
    val id: Int, val name: String, val city_id: Int?, val city: String?, val phone: String?,
    val hours: String?, val lat: Double?, val lng: Double?, val is_main: Boolean, val is_active: Boolean,
)
data class BranchesResp(val branches: List<BranchDto>)
data class BranchResp(val branch: BranchDto, val message: String? = null)
data class BranchBody(val name: String, val city_id: Int?, val phone: String?, val hours: String?, val lat: Double?, val lng: Double?)
data class BranchUpdate(val is_active: Boolean? = null, val name: String? = null)

data class SectionDto(val id: Int, val name: String, val icon: String?, val products_count: Int = 0)
data class SectionsResp(val sections: List<SectionDto>)
data class IconsResp(val icons: List<String>)
data class SectionResp(val section: SectionDto, val message: String? = null)
data class SectionBody(val name: String, val icon: String?)

data class CityDto(val id: Int, val name: String)
data class CatLite(val id: Int, val name: String, val icon: String? = null)
data class CatsResp(val categories: List<CatLite>)
data class CitiesResp(val cities: List<CityDto>)

data class DocumentDto(val id: Int, val key: String?, val name: String, val description: String?, val field: String, val required: Boolean, val value: String?, val status: String, val note: String?)
data class DocumentsResp(val documents: List<DocumentDto>, val complete: Boolean)
data class UploadResp(val url: String)


// ── الطلبات ──
data class OrderRow(val id: Int, val order_no: String?, val customer: String, val branch: String?, val items_count: Int, val total: Double, val status: String, val dt: String?, val driver: String?, val payment_method: String?, val is_paid: Boolean = false, val payment_status: String? = null, val payment_proof: String? = null)
data class OrdersResp(val orders: List<OrderRow>)
data class OrderAddon(val name: String, val price: Double)
data class OrderItemDto(val id: Int, val name: String, val price: Double, val qty: Int, val addons: List<OrderAddon> = emptyList(), val line_total: Double)
data class OrderDetail(val id: Int, val order_no: String?, val customer: String, val phone: String?, val branch: String?, val status: String, val items_total: Double, val delivery_fee: Double, val discount: Double, val total: Double, val payment_method: String?, val is_paid: Boolean, val drop_address: String?, val dt: String?, val payment_status: String? = null, val payment_proof: String? = null)
data class OrderDetailResp(val order: OrderDetail, val items: List<OrderItemDto>)
data class ChatMsg(val id: Int, val sender: String? = null, val body: String? = null, val image: String? = null, val at: String? = null, val mine: Boolean = false)
data class ChatResp(val thread_id: Int = 0, val locked: Boolean = false, val messages: List<ChatMsg> = emptyList())
data class ChatSendBody(val body: String? = null, val image: String? = null)
data class ChatSendResp(val id: Int = 0)

// ── بوابات الدفع ──
data class PayField(val key: String, val label: String, val secret: Boolean = false)
data class PayProvider(val key: String, val name: String, val fields: List<PayField> = emptyList(), val linked: Boolean = false, val enabled: Boolean = false, val masked: Map<String, String>? = null)
data class PaymentsResp(val providers: List<PayProvider>)
data class LinkPayBody(val provider: String, val credentials: Map<String, String>)
data class TogglePayBody(val provider: String, val is_enabled: Boolean)

// ── المنتجات ──
data class BranchMini(val id: Int, val name: String)
data class PStock(val branch_id: Int, val branch: String, val in_stock: Int)
data class PAddon(val name: String, val price: Double)
data class ProductDto(val id: Int, val name: String, val description: String?, val section_id: Int?, val section: String?, val price: Double, val price_before: Double = 0.0, val discount: Int = 0, val status: String, val images: List<String> = emptyList(), val addons: List<PAddon> = emptyList(), val stock: List<PStock> = emptyList())
data class ProductsResp(val branches: List<BranchMini>, val products: List<ProductDto>)
data class ProductBody(val name: String, val description: String?, val store_section_id: Int?, val price: Double, val price_before: Double?, val status: String, val images: List<String>, val addons: List<PAddon>, val stock: Map<String, Int>)
data class ProductResp(val product: ProductDto, val message: String? = null)
data class StockBody(val branch_id: Int, val in_stock: Int)

data class DashWeekDay(val label: String, val total: Double)
data class DashOrder(val id: Int, val order_no: String?, val customer: String, val items_count: Int, val total: Double, val status: String, val driver: String? = null, val dt: String? = null, val items: String? = null)
data class DashResp(val sales_month: Double, val orders_month: Int, val growth_pct: Int? = null, val branches: Int, val products: Int, val rating: Double, val week: List<DashWeekDay> = emptyList(), val recent: List<DashOrder> = emptyList())

data class WalletTx(val id: Int, val title: String, val dt: String? = null, val amount: Double)
data class WalletResp(val balance: Double, val transactions: List<WalletTx> = emptyList())
data class Period(val sales: Double, val orders: Int)
data class ReportsResp(val today: Period, val week: Period, val month: Period, val year: Period, val all: Period)

interface MerchantApi {
    @GET("merchant/login-method") suspend fun loginMethods(): MethodsResp
    @POST("merchant/register") suspend fun register(@Body body: RegisterBody): MsgResp
    @Multipart @POST("merchant/register-upload") suspend fun registerUpload(@Part file: MultipartBody.Part): UploadResp
    @GET("merchant/chat/{id}") suspend fun chatShow(@Path("id") id: Int, @Query("after") after: Int = 0): ChatResp
    @POST("merchant/chat/{id}") suspend fun chatSend(@Path("id") id: Int, @Body b: ChatSendBody): ChatSendResp
    @POST("merchant/request-otp") suspend fun requestOtp(@Body body: Map<String, String>): MsgResp
    @POST("merchant/login") suspend fun login(@Body body: LoginBody): LoginResp
    @GET("merchant/me") suspend fun me(): MeResp
    @POST("merchant/device-token") suspend fun registerDeviceToken(@Body body: Map<String, String>): MsgResp
    @GET("merchant/notifications") suspend fun notifications(): NotificationsResp
    @GET("merchant/plans") suspend fun plans(): PlansResp
    @GET("merchant/subscription") suspend fun subscription(): SubResp
    @POST("merchant/subscribe") suspend fun subscribe(@Body body: SubscribeBody): MsgResp
    @POST("merchant/subscription/pay") suspend fun paySubscription(): PayResp
    @POST("merchant/logout") suspend fun logout(): MsgResp

    @GET("merchant/store") suspend fun store(): StoreResp
    @PUT("merchant/store") suspend fun updateStore(@Body body: StoreUpdate): StoreResp

    @GET("merchant/branches") suspend fun branches(): BranchesResp
    @POST("merchant/branches") suspend fun addBranch(@Body body: BranchBody): BranchResp
    @PUT("merchant/branches/{id}") suspend fun updateBranch(@Path("id") id: Int, @Body body: BranchUpdate): BranchResp
    @DELETE("merchant/branches/{id}") suspend fun deleteBranch(@Path("id") id: Int): MsgResp

    @GET("merchant/sections") suspend fun sections(): SectionsResp
    @GET("merchant/section-icons") suspend fun sectionIcons(): IconsResp
    @POST("merchant/sections") suspend fun addSection(@Body body: SectionBody): SectionResp
    @DELETE("merchant/sections/{id}") suspend fun deleteSection(@Path("id") id: Int): MsgResp

    @GET("merchant/cities") suspend fun cities(): CitiesResp
    @GET("merchant/store-categories") suspend fun storeCategories(): CatsResp

    @GET("merchant/documents") suspend fun documents(): DocumentsResp
    @POST("merchant/documents") suspend fun submitDocument(@Body body: Map<String, String>): MsgResp
    @Multipart @POST("merchant/uploads") suspend fun upload(@Part file: MultipartBody.Part): UploadResp

    @GET("merchant/dashboard") suspend fun dashboard(): DashResp
    @GET("merchant/wallet") suspend fun wallet(): WalletResp
    @GET("merchant/reports") suspend fun reports(): ReportsResp
    @GET("merchant/orders") suspend fun orders(@Query("tab") tab: String): OrdersResp
    @GET("merchant/orders/{id}") suspend fun orderDetail(@Path("id") id: Int): OrderDetailResp
    @POST("merchant/orders/{id}/accept") suspend fun acceptOrder(@Path("id") id: Int): MsgResp
    @POST("merchant/orders/{id}/reject") suspend fun rejectOrder(@Path("id") id: Int): MsgResp
    @POST("merchant/orders/{id}/ready") suspend fun readyOrder(@Path("id") id: Int): MsgResp
    @POST("merchant/orders/{id}/confirm-payment") suspend fun confirmPayment(@Path("id") id: Int): MsgResp
    @POST("merchant/orders/{id}/reject-payment") suspend fun rejectPayment(@Path("id") id: Int): MsgResp

    @GET("merchant/payments") suspend fun payments(): PaymentsResp
    @POST("merchant/payments") suspend fun linkPayment(@Body body: LinkPayBody): MsgResp
    @PATCH("merchant/payments/toggle") suspend fun togglePayment(@Body body: TogglePayBody): MsgResp
    @DELETE("merchant/payments/{provider}") suspend fun unlinkPayment(@Path("provider") provider: String): MsgResp

    @GET("merchant/products") suspend fun products(): ProductsResp
    @GET("merchant/products/{id}") suspend fun product(@Path("id") id: Int): ProductResp
    @POST("merchant/products") suspend fun createProduct(@Body body: ProductBody): ProductResp
    @PUT("merchant/products/{id}") suspend fun updateProduct(@Path("id") id: Int, @Body body: ProductBody): ProductResp
    @DELETE("merchant/products/{id}") suspend fun deleteProduct(@Path("id") id: Int): MsgResp
    @PATCH("merchant/products/{id}/stock") suspend fun setStock(@Path("id") id: Int, @Body body: StockBody): MsgResp
    @PATCH("merchant/products/{id}/toggle") suspend fun toggleProduct(@Path("id") id: Int): MsgResp

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

    val api: MerchantApi = Retrofit.Builder()
        .baseUrl(API_BASE).client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(MerchantApi::class.java)
}

fun errorMessage(e: retrofit2.HttpException): String? = try {
    val body = e.response()?.errorBody()?.string()
    if (body.isNullOrBlank()) null else {
        val obj = com.google.gson.JsonParser.parseString(body).asJsonObject
        when {
            obj.has("message") && !obj.get("message").isJsonNull -> obj.get("message").asString
            obj.has("errors") -> obj.getAsJsonObject("errors").entrySet().firstOrNull()?.value?.asJsonArray?.firstOrNull()?.asString
            else -> null
        }
    }
} catch (_: Exception) { null }
