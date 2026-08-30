package com.matnokh.customer.net

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

const val API_BASE = "https://matnokh.bytnova.com/api/"

data class CustomerBrief(val id: Int, val name: String?, val phone: String?, val email: String?, val city_id: Int?, val avatar: String? = null, val search_radius_km: Double? = null)
data class AuthResp(val token: String?, val customer: CustomerBrief?, val message: String?)
data class RegisterBody(val name: String, val phone: String, val email: String?, val password: String)
data class LoginBody(val method: String = "phone_password", val phone: String, val password: String? = null, val code: String? = null)
data class MsgResp(val message: String?, val order_no: String?, val order_id: Int?, val dev_code: String?, val payment_url: String? = null)
data class PricingResp(val km_price_min: Double = 1.0, val km_price_max: Double = 3.0)
data class FavResp(val favorite: Boolean = false)
data class IdsResp(val ids: List<Int> = emptyList())
data class UploadResp(val url: String?)
data class ChatMsg(val id: Int, val sender: String? = null, val body: String? = null, val image: String? = null, val at: String? = null, val mine: Boolean = false)
data class ChatResp(val thread_id: Int = 0, val locked: Boolean = false, val messages: List<ChatMsg> = emptyList())
data class ChatSendBody(val body: String? = null, val image: String? = null)
data class ChatSendResp(val id: Int = 0)
data class ProfileBody(val name: String? = null, val email: String? = null, val avatar: String? = null, val search_radius_km: Double? = null)
data class AddressDto(val id: Int, val type: String, val label: String, val address: String? = null, val lat: Double? = null, val lng: Double? = null, val is_default: Boolean = false)
data class AddressesResp(val addresses: List<AddressDto>)
data class AddressBody(val type: String, val label: String, val address: String?, val lat: Double?, val lng: Double?, val is_default: Boolean = false)
data class PayMethodDto(val id: Int, val type: String, val label: String? = null, val uses_count: Int = 0)
data class PayMethodsResp(val methods: List<PayMethodDto>)
data class PayMethodBody(val type: String, val label: String? = null)


data class CatDto(val id: Int, val name: String, val icon: String?, val name_en: String? = null)
data class CatsResp(val categories: List<CatDto>)
data class StoreDto(val id: Int, val store_name: String, val category_id: Int?, val category_name: String?, val logo: String?, val rating: Double = 0.0, val is_open: Boolean = false, val branches_count: Int = 0, val lat: Double? = null, val lng: Double? = null, val iban: String? = null, val bank_name: String? = null, val account_name: String? = null, val category_name_en: String? = null, val store_name_en: String? = null)
data class StoresResp(val stores: List<StoreDto>, val has_more: Boolean = false)
data class AddonDto(val name: String, val price: Double, val name_en: String? = null)
data class StockDto(val branch_id: Int, val in_stock: Int)
data class ProdDto(val id: Int, val name: String, val description: String?, val section_id: Int?, val price: Double, val price_before: Double = 0.0, val images: List<String> = emptyList(), val addons: List<AddonDto> = emptyList(), val stock: List<StockDto> = emptyList(), val name_en: String? = null, val description_en: String? = null)
data class BranchDto(val id: Int, val name: String)
data class SectionDto(val id: Int, val name: String, val icon: String?, val items: List<ProdDto>, val name_en: String? = null)
data class StoreDetailResp(val store: StoreDto, val branches: List<BranchDto>, val sections: List<SectionDto>)
data class OfferDto(val id: Int, val name: String, val description: String?, val price: Double, val price_before: Double, val images: List<String> = emptyList(), val addons: List<AddonDto> = emptyList(), val store_id: Int, val store_name: String, val store_logo: String?, val store_category: String?, val off: Int, val section_id: Int?, val name_en: String? = null, val description_en: String? = null)
data class OffersResp(val offers: List<OfferDto>)
data class OrderRowDto(val id: Int, val order_no: String?, val store: String, val total: Double, val status: String, val dt: String?, val ts: Long = 0, val store_en: String? = null)
data class OrdersResp(val orders: List<OrderRowDto>)
data class OrderBidDto(val id: Int, val driver: String, val rating: Double = 5.0, val amount: Double)
data class TrackDriver(val name: String? = null, val phone: String? = null, val rating: Double = 5.0, val vehicle_type: String? = null, val lat: Double? = null, val lng: Double? = null)
data class LatLngDto(val lat: Double? = null, val lng: Double? = null)
data class OrderBidsResp(val bids: List<OrderBidDto> = emptyList(), val status: String? = null, val driver_id: Int? = null, val step: Int = 0, val order_no: String? = null, val store: String? = null, val store_en: String? = null, val driver: TrackDriver? = null, val pickup: LatLngDto? = null, val drop: LatLngDto? = null)
data class PickBidBody(val bid_id: Int)
data class NotifItem(val id: Int, val title: String, val body: String, val type: String? = null, val ref_id: Int? = null, val ref_kind: String? = null, val dt: String? = null)
data class NotifResp(val notifications: List<NotifItem>)
data class OrderItemBody(val product_id: Int?, val name: String, val price: Double, val qty: Int, val addons: List<String>)
data class QuoteBody(val merchant_id: Int, val drop_lat: Double? = null, val drop_lng: Double? = null)
data class QuoteResp(val delivery_fee: Double = 0.0, val delivery_mode: String? = null)
data class RateBody(val order_id: Int, val is_transport: Boolean, val stars: Int)
data class CreateOrderBody(val merchant_id: Int, val branch_id: Int?, val payment_method: String, val drop_address: String?, val items: List<OrderItemBody>, val drop_lat: Double? = null, val drop_lng: Double? = null, val payment_proof: String? = null)
data class PlaceDto(val id: String?, val name: String, val lat: Double?, val lng: Double?, val address: String = "", val type: String = "", val rating: Double? = null)
data class PlacesResp(val places: List<PlaceDto>)
data class SvcDto(val id: Int, val name: String, val key: String, val point_type: String = "pickup_dropoff", val base_price: Double = 0.0, val icon: String? = null, val name_en: String? = null)
data class SvcResp(val services: List<SvcDto>)
data class TBidDriver(val id: Int, val name: String, val rating: Double = 5.0, val vehicle_type: String? = null, val lat: Double? = null, val lng: Double? = null, val phone: String? = null)
data class TBid(val id: Int, val amount: Double, val driver: TBidDriver)
data class TOrder(val id: Int, val order_no: String, val service_name: String?, val service_name_en: String? = null, val from: String?, val to: String?, val status: String, val proposed_price: Double = 0.0, val final_fare: Double? = null, val driver: TBidDriver? = null, val bids: List<TBid> = emptyList(), val ts: Long = 0, val from_lat: Double? = null, val from_lng: Double? = null, val to_lat: Double? = null, val to_lng: Double? = null)
data class TOrdersResp(val orders: List<TOrder>)
data class OrderDetailDriver(val name: String? = null, val phone: String? = null, val rating: Double = 5.0, val vehicle_type: String? = null)
data class OrderDetailItem(val name: String = "", val qty: Int = 1, val price: Double = 0.0, val addons: List<String> = emptyList())
data class OrderDetail(val id: Int, val order_no: String? = null, val status: String = "", val store: String = "", val store_en: String? = null, val store_logo: String? = null, val store_phone: String? = null, val driver: OrderDetailDriver? = null, val items: List<OrderDetailItem> = emptyList(), val items_total: Double = 0.0, val delivery_fee: Double = 0.0, val total: Double = 0.0, val payment_method: String? = null, val drop_address: String? = null, val dt: String? = null, val created_at: String? = null)
data class TransportBody(val service_key: String, val service_name: String, val from_address: String, val to_address: String?, val from_lat: Double?, val from_lng: Double?, val note: String?, val mode: String, val proposed_price: Double, val payment_method: String?, val to_lat: Double? = null, val to_lng: Double? = null)

interface CustomerApi {
    @POST("customer/register") suspend fun register(@Body b: RegisterBody): AuthResp
    @POST("customer/login") suspend fun login(@Body b: LoginBody): AuthResp
    @POST("customer/request-otp") suspend fun requestOtp(@Body b: Map<String, String>): MsgResp
    @GET("customer/me") suspend fun me(): AuthResp
    @GET("customer/pricing") suspend fun pricing(): PricingResp
    @GET("customer/favorites") suspend fun favorites(): StoresResp
    @GET("customer/favorite-ids") suspend fun favoriteIds(): IdsResp
    @POST("customer/favorites/{id}") suspend fun toggleFavorite(@Path("id") id: Int): FavResp
    @Multipart @POST("customer/uploads") suspend fun upload(@Part file: MultipartBody.Part): UploadResp
    @GET("customer/chat/{kind}/{id}/{type}") suspend fun chatShow(@Path("kind") kind: String, @Path("id") id: Int, @Path("type") type: String, @Query("after") after: Int = 0): ChatResp
    @POST("customer/chat/{kind}/{id}/{type}") suspend fun chatSend(@Path("kind") kind: String, @Path("id") id: Int, @Path("type") type: String, @Body b: ChatSendBody): ChatSendResp
    @PATCH("customer/profile") suspend fun updateProfile(@Body b: ProfileBody): AuthResp
    @GET("customer/addresses") suspend fun addresses(): AddressesResp
    @POST("customer/addresses") suspend fun addAddress(@Body b: AddressBody): MsgResp
    @PUT("customer/addresses/{id}") suspend fun updateAddress(@Path("id") id: Int, @Body b: AddressBody): MsgResp
    @DELETE("customer/addresses/{id}") suspend fun delAddress(@Path("id") id: Int): MsgResp
    @GET("customer/payment-methods") suspend fun payMethods(): PayMethodsResp
    @POST("customer/payment-methods") suspend fun addPayMethod(@Body b: PayMethodBody): MsgResp
    @DELETE("customer/payment-methods/{id}") suspend fun delPayMethod(@Path("id") id: Int): MsgResp
    @GET("customer/categories") suspend fun categories(): CatsResp
    @GET("customer/stores") suspend fun stores(@Query("category_id") cat: Int? = null, @Query("page") page: Int? = null, @Query("per") per: Int? = null, @Query("q") q: String? = null): StoresResp
    @GET("customer/stores/{id}") suspend fun storeDetail(@Path("id") id: Int): StoreDetailResp
    @GET("customer/offers") suspend fun offers(): OffersResp
    @POST("customer/orders") suspend fun createOrder(@Body b: CreateOrderBody): MsgResp
    @POST("customer/orders/quote") suspend fun quoteDelivery(@Body b: QuoteBody): QuoteResp
    @POST("customer/rate") suspend fun rate(@Body b: RateBody): MsgResp
    @GET("customer/nearby-places") suspend fun nearbyPlaces(@Query("lat") lat: Double, @Query("lng") lng: Double, @Query("type") type: String): PlacesResp
    @GET("customer/services") suspend fun services(): SvcResp
    @GET("customer/transport-orders") suspend fun transportOrders(): TOrdersResp
    @POST("customer/transport-orders") suspend fun createTransport(@Body b: TransportBody): MsgResp
    @POST("customer/transport-orders/{id}/pick") suspend fun pickTransport(@Path("id") id: Int, @Body b: PickBidBody): MsgResp
    @GET("customer/orders") suspend fun orders(): OrdersResp
    @GET("customer/orders/{id}") suspend fun orderDetail(@Path("id") id: Int): OrderDetail
    @POST("customer/orders/{id}/cancel") suspend fun cancelOrder(@Path("id") id: Int): MsgResp
    @POST("customer/transport-orders/{id}/cancel") suspend fun cancelTransport(@Path("id") id: Int): MsgResp
    @POST("customer/transport-orders/{id}/rebroadcast") suspend fun rebroadcastTransport(@Path("id") id: Int): MsgResp
    @GET("customer/orders/{id}/bids") suspend fun orderBids(@Path("id") id: Int): OrderBidsResp
    @POST("customer/orders/{id}/pick-bid") suspend fun pickBid(@Path("id") id: Int, @Body b: PickBidBody): MsgResp
    @POST("customer/device-token") suspend fun registerDeviceToken(@Body b: Map<String, String>): MsgResp
    @GET("customer/notifications") suspend fun notifications(): NotifResp
}

object Net {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .callTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val b = chain.request().newBuilder().header("Accept", "application/json")
            Session.token?.let { if (it.isNotBlank()) b.header("Authorization", "Bearer $it") }
            chain.proceed(b.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()
    val api: CustomerApi = Retrofit.Builder().baseUrl(API_BASE).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build().create(CustomerApi::class.java)
}

fun errorMessage(e: retrofit2.HttpException): String? = try {
    val body = e.response()?.errorBody()?.string()
    if (body.isNullOrBlank()) null else {
        val o = com.google.gson.JsonParser.parseString(body).asJsonObject
        when { o.has("message") && !o.get("message").isJsonNull -> o.get("message").asString
            o.has("errors") -> o.getAsJsonObject("errors").entrySet().firstOrNull()?.value?.asJsonArray?.firstOrNull()?.asString
            else -> null }
    }
} catch (_: Exception) { null }
