package com.matnokh.customer
import com.matnokh.customer.ui.tr

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.customer.net.*
import com.matnokh.customer.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Session.init(this)
        com.matnokh.customer.ui.Lang.init(this)
        Fcm.init(this)
        readNotif(intent)
        enableEdgeToEdge()
        // كيبورد الشات: على أندرويد 11+ تعتمد الشاشات على WindowInsets (imePadding)؛
        // بعض أجهزة Samsung تُصغّر النافذة أيضاً بسبب adjustResize فيتضاعف التعويض ويظهر فراغ.
        // نعطّل الـresize هنا (API 30+) ونُبقي adjustResize بالمانيفست للأجهزة الأقدم.
        if (android.os.Build.VERSION.SDK_INT >= 30) window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        setContent { MatnokhTheme { androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides if (com.matnokh.customer.ui.Lang.isAr) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr) { Root() } } }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readNotif(intent)
    }

    private fun readNotif(i: android.content.Intent?) {
        i?.getStringExtra("open")?.let { Sel.deeplink = it }
        i?.getStringExtra("order_id")?.toIntOrNull()?.let { Sel.deeplinkOrderId = it }
        i?.getStringExtra("kind")?.let { Sel.deeplinkKind = it }
        i?.getStringExtra("chat_type")?.let { Sel.deeplinkChatType = it }
    }
}

@Composable
fun Root() {
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(if (Session.isLoggedIn()) "home" else "splash") }
    var sentStore by remember { mutableStateOf("") }
    var sentOrderId by remember { mutableStateOf<Int?>(null) }
    var drawerOpen by remember { mutableStateOf(false) }
    var toastMsg by remember { mutableStateOf<String?>(null) }
    val toast: (String) -> Unit = { toastMsg = it }
    val openMenu = { drawerOpen = true }
    LaunchedEffect(toastMsg) { if (toastMsg != null) { delay(2400); toastMsg = null } }
    LaunchedEffect(Unit) { call({ Repo.loadHome() }, toast) }
    LaunchedEffect(screen) { if (screen == "stores") runCatching { Repo.reloadStores() } }
    val ctx = LocalContext.current
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    LaunchedEffect(Unit) {
        val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) perms.add(Manifest.permission.POST_NOTIFICATIONS)
        permLauncher.launch(perms.toTypedArray())
        Fcm.registerToken(ctx)
    }

    LaunchedEffect(screen, Sel.deeplink) {
        val d = Sel.deeplink
        if (d != null && Session.isLoggedIn() && screen !in setOf("splash", "login", "register")) { if (d == "transportbids") Sel.transportId = Sel.deeplinkOrderId; if (d == "chat") { Sel.chatKind = Sel.deeplinkKind ?: "store"; Sel.chatId = Sel.deeplinkOrderId; Sel.chatType = if (Sel.deeplinkChatType == "cm") "merchant" else "driver"; Sel.chatTitle = if (Sel.deeplinkChatType == "cm") tr("محادثة المتجر", "Store chat") else tr("محادثة المندوب", "Courier chat"); Sel.chatBack = "orders" }; screen = d; Sel.deeplink = null }
    }
    fun openStore(s: UiStore) { Sel.store = s; Sel.sectionIdx = 0; Sel.sectionStoreId = s.id; Sel.storeBack = if (screen in setOf("home", "stores", "nearby")) screen else "home"; screen = "store"; scope.launch { call({ Repo.loadStore(s.id) }, toast) } }
    fun openOffer(o: UiOffer) { Sel.store = UiStore(o.storeId, o.storeName, o.storeCategory, o.storeLogo, "", true, 0, ""); Sel.product = o.product; Sel.prodBack = "offersall"; screen = "product" }
    val onCart = { if (Cart.lines.isEmpty()) toast(tr("سلتك فارغة — تصفّح المتاجر وأضف منتجات", "Your cart is empty — browse stores and add products")) else run { screen = "cart" } }
    val logout = { Session.clear(); Cart.clear(); drawerOpen = false; screen = "login" }
    val nav: (String) -> Unit = { if (it == "splash") logout() else screen = it }

    Box(Modifier.fillMaxSize().background(C.bg)) {
        when (screen) {
            "splash" -> SplashScreen(onStart = { screen = if (Session.isLoggedIn()) "home" else "login" })
            "login" -> LoginScreen(onLoggedIn = { screen = "home"; Fcm.registerToken(ctx) }, onRegister = { screen = "register" }, toast = toast)
            "register" -> RegisterScreen(onDone = { screen = "home"; Fcm.registerToken(ctx) }, onBack = { screen = "login" }, toast = toast)
            else -> Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    Refreshable({ RefreshBus.tick++; runCatching { when (screen) { "home", "offersall", "track" -> Repo.loadHome(); "stores" -> Repo.reloadStores(); else -> {} } } }) {
                    when (screen) {
                        "home" -> HomeScreen(openMenu, onCart, { screen = "stores" }, { screen = "offersall" }, { screen = "nearby" }, { openStore(it) }, { Sel.svc = it; screen = "order" }, { screen = "activeoffers" }, { screen = "notifications" }, onAllServices = { screen = "services" })
                        "stores" -> StoresScreen({ screen = "home" }, onCart, openMenu) { openStore(it) }
                        "offersall" -> OffersAllScreen({ screen = "home" }, onCart, openMenu) { openOffer(it) }
                        "nearby" -> NearbyScreen({ screen = "home" }, onCart, openMenu, { screen = "storemap" }) { Sel.place = it; screen = "errand" }
                        "storemap" -> PlacesMapFull({ screen = "nearby" }) { Sel.place = it; screen = "errand" }
                        "errand" -> Sel.place?.let { pl -> PlaceErrandScreen(pl, { screen = "nearby" }, openMenu, { oid -> Sel.transportId = oid; screen = "transportbids" }, { Sel.destBack = "errand"; screen = "destination" }, toast) }
                        "store" -> StoreScreen({ screen = Sel.storeBack }, onCart, openMenu, { Sel.product = it; screen = "product" }, toast)
                        "product" -> ProductScreen({ screen = Sel.prodBack }, onCart, openMenu, { screen = Sel.prodBack }, toast)
                        "cart" -> CartScreen({ screen = "store" }, openMenu, { sn, oid -> sentStore = sn; sentOrderId = oid; screen = "ordersent" }, { Sel.destBack = "cart"; screen = "destination" }, toast)
                        "destination" -> DestinationScreen({ screen = Sel.destBack }, toast)
                        "ordersent" -> OrderSentScreen(sentStore, onTrack = { Sel.deeplinkOrderId = sentOrderId; screen = "orders" }, onPayment = { screen = "payment" }, onOrders = { screen = "orders" }, onHome = { screen = "home" })
                        "payment" -> PaymentMethodsScreen(sentStore, onDone = { screen = "home" }, onMenu = openMenu)
                        "services" -> TransportServicesScreen({ screen = "home" }, openMenu, { Sel.svc = it; screen = "order" })
                        "order" -> OrderScreen({ screen = "services" }, openMenu, { oid -> Sel.transportId = oid; screen = "transportbids" }, toast)
                        "transportbids" -> TransportBidsScreen({ screen = "home" }, openMenu, { screen = "track" }, toast)
                        "track" -> TrackScreen({ screen = "home" }, openMenu, toast, onChat = { Sel.chatKind = "transport"; Sel.chatId = Sel.transportId; Sel.chatType = "driver"; Sel.chatTitle = tr("محادثة السائق", "Driver chat"); Sel.chatBack = "track"; screen = "chat" })
                        "chat" -> Sel.chatId?.let { cid -> ChatScreen(Sel.chatKind, cid, Sel.chatType, Sel.chatTitle, { screen = Sel.chatBack }, openMenu, toast) } ?: run { screen = "home" }
                        "orderdetails" -> OrderDetailsScreen({ screen = "orders" }, openMenu, { act, oid -> when (act) { "pickStore" -> { Sel.deeplinkOrderId = oid; screen = "orders" }; "bidsT" -> { Sel.transportId = oid; screen = "transportbids" }; "trackT" -> { Sel.transportId = oid; screen = "track" }; "chatD" -> { Sel.chatKind = if (Sel.detailIsTransport) "transport" else "store"; Sel.chatId = oid; Sel.chatType = "driver"; Sel.chatTitle = tr("محادثة المندوب", "Courier chat"); Sel.chatBack = "orderdetails"; screen = "chat" }; "chatM" -> { Sel.chatKind = "store"; Sel.chatId = oid; Sel.chatType = "merchant"; Sel.chatTitle = tr("محادثة المتجر", "Store chat"); Sel.chatBack = "orderdetails"; screen = "chat" }; "cancel" -> { scope.launch { val r = call({ com.matnokh.customer.net.Net.api.cancelOrder(oid) }, toast); if (r != null) { toast(r.message ?: tr("تم إلغاء الطلب", "Order canceled")); screen = "orders" } } }; "cancelT" -> { scope.launch { val r = call({ com.matnokh.customer.net.Net.api.cancelTransport(oid) }, toast); if (r != null) { toast(r.message ?: tr("تم إلغاء الطلب", "Order canceled")); screen = "orders" } } }; "rebroadcastT" -> { scope.launch { val r = call({ com.matnokh.customer.net.Net.api.rebroadcastTransport(oid) }, toast); if (r != null) { toast(r.message ?: tr("أُعيد بثّ الطلب", "Order rebroadcast")); screen = "orders" } } } } }, toast)
                        "orders" -> OrdersScreen({ screen = "home" }, openMenu, { screen = "track" }, toast, onTransport = { tid, st -> Sel.transportId = tid; screen = if (st == "broadcasting") "transportbids" else "track" }, onOpenDetails = { oid, isT -> Sel.detailOrderId = oid; Sel.detailIsTransport = isT; screen = "orderdetails" })
                        "activeoffers" -> OrdersScreen({ screen = "home" }, openMenu, { screen = "track" }, toast, activeOnly = true, onTransport = { tid, st -> Sel.transportId = tid; screen = if (st == "broadcasting") "transportbids" else "track" }, onOpenDetails = { oid, isT -> Sel.detailOrderId = oid; Sel.detailIsTransport = isT; screen = "orderdetails" })
                        "notifications" -> NotificationsScreen({ screen = "home" }, openMenu, toast = toast)
                        "profile" -> ProfileScreen({ screen = "home" }, openMenu, { if (Session.isLoggedIn()) logout() else run { screen = "login" } }, toast, onNav = { screen = it })
                        "addresses" -> AddressesScreen({ screen = "profile" }, openMenu, toast)
                        "paymethods" -> AccountPayMethods({ screen = "profile" }, openMenu, toast)
                        "favorites" -> FavoritesScreen({ screen = "profile" }, openMenu, { openStore(it) }, toast)
                        "settings" -> SettingsScreen({ screen = "home" }, openMenu, { screen = it }, logout, toast)
                        "help" -> AccountScreen("help", { screen = "profile" }, openMenu, toast)
                    }
                    }
                }
                BottomNav(current = screen, onSelect = { screen = it })
            }
        }

        CustomerDrawer(open = drawerOpen, current = screen, onClose = { drawerOpen = false }, onNavigate = nav, toast = toast)

        AnimatedVisibility(visible = toastMsg != null, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp, start = 22.dp, end = 22.dp)) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(C.head).padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.Center) { T(toastMsg ?: "", 12, FontWeight.Bold, Color.White) }
        }
    }
}
