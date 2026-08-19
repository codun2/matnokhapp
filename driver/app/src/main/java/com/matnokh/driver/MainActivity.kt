package com.matnokh.driver

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.driver.net.Session
import com.matnokh.driver.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Session.init(this)
        DrvNotif.open = intent?.getStringExtra("open")
        DrvNotif.kind = intent?.getStringExtra("kind")
        DrvNotif.orderId = intent?.getStringExtra("order_id")?.toIntOrNull()
        enableEdgeToEdge()
        setContent { MatnokhTheme { Root() } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        DrvNotif.open = intent.getStringExtra("open")
        DrvNotif.kind = intent.getStringExtra("kind")
        DrvNotif.orderId = intent.getStringExtra("order_id")?.toIntOrNull()
    }
}

object DrvNotif { var open by mutableStateOf<String?>(null); var kind: String? = null; var orderId: Int? = null }

@Composable
fun Root() {
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(if (Session.isLoggedIn()) "home" else "splash") }
    LaunchedEffect(DrvNotif.open) { DrvNotif.open?.let { o -> if (Session.isLoggedIn()) screen = o; DrvNotif.open = null } }
    val fcmCtx = androidx.compose.ui.platform.LocalContext.current
    val permLauncher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()) { }
    LaunchedEffect(Unit) {
        val perms = mutableListOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) perms.add(android.Manifest.permission.POST_NOTIFICATIONS)
        permLauncher.launch(perms.toTypedArray())
        com.matnokh.driver.net.Fcm.registerToken(fcmCtx)
    }
    var drawerOpen by remember { mutableStateOf(false) }
    var bidJob by remember { mutableStateOf<Job?>(null) }
    var toastMsg by remember { mutableStateOf<String?>(null) }
    val toast: (String) -> Unit = { toastMsg = it }
    val openMenu = { drawerOpen = true }
    LaunchedEffect(toastMsg) { if (toastMsg != null) { delay(2400); toastMsg = null } }

    fun goHomeLoaded() { scope.launch { repoMe(toast); repoActive(toast); repoDash(toast); if (Drv.available.value) { Drv.received.clear(); repoNearby(toast); repoStoreOrders(toast) } }; screen = "home" }
    fun logout() { scope.launch { com.matnokh.driver.net.call({ com.matnokh.driver.net.Net.api.logout() }, toast) }; Session.clear(); Drv.reset(); drawerOpen = false; screen = "login" }

    // بعد إرسال العرض: انتظار اختيار الزبون (استطلاع)
    LaunchedEffect(screen) {
        if (screen == "wait") {
            repeat(40) {
                delay(3500); repoActive(toast)
                if (Drv.nowOrders.isNotEmpty()) { screen = "active"; return@LaunchedEffect }
            }
        }
    }

    val nav: (String) -> Unit = { t -> if (t == "splash") logout() else screen = t }

    LaunchedEffect(Unit) { if (Session.isLoggedIn()) { repoMe(toast); repoActive(toast); repoDash(toast); if (Drv.available.value) { Drv.received.clear(); repoNearby(toast); repoStoreOrders(toast) } } }
    val locCtx = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) { runCatching { Drv.trackIntervalMin.value = com.matnokh.driver.net.Net.api.config().tracking_interval_min } }
    // إرسال الموقع صار عبر خدمة أمامية مستقلّة (تعمل حتى مع قفل الشاشة)
    LaunchedEffect(Session.isLoggedIn(), Drv.available.value, Drv.nowOrders.size) { com.matnokh.driver.LocationService.sync(locCtx) }
    Box(Modifier.fillMaxSize().background(C.bg)) {
        when (screen) {
            "splash" -> SplashScreen(onStart = { if (Session.isLoggedIn()) { com.matnokh.driver.net.Fcm.registerToken(fcmCtx); goHomeLoaded() } else screen = "login" })
            "login" -> LoginScreen(onLoggedIn = { goHomeLoaded(); com.matnokh.driver.net.Fcm.registerToken(fcmCtx) }, onRegister = { screen = "register" }, toast = toast)
            "register" -> RegisterScreen(onDone = { screen = "login" }, onBack = { screen = "login" }, toast = toast)
            else -> Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    Refreshable({ when (screen) { "home" -> { repoMe(toast); repoActive(toast); repoDash(toast); if (Drv.available.value) { Drv.received.clear(); repoNearby(toast); repoStoreOrders(toast) } }; "myorders" -> { repoActive(toast); repoPast(toast) }; "earn" -> { repoDash(toast); repoPast(toast) }; "notifications" -> repoMe(toast); else -> {} } }) {
                    when (screen) {
                        "home" -> HomeScreen(openMenu, onNotifications = { screen = "notifications" },
                            onBid = { bidJob = it; screen = "bid" },
                            onAcceptDirect = { j -> scope.launch { val ok = if (j.isStore) repoStoreAccept(j.oid, toast) else repoAccept(j.oid, toast); if (ok) { Drv.received.removeAll { it.oid == j.oid }; Drv.nowOrders.clear(); if (j.isStore) repoStoreActive(toast) else repoNow(toast); screen = "active" } } },
                            onStoreReject = { j -> scope.launch { repoStoreReject(j.oid, toast); Drv.received.removeAll { it.oid == j.oid }; toast("تم رفض الطلب — نبحث عن مندوب آخر") } }, toast = toast, onExpandMap = { screen = "ordersmap" })
                        "myorders" -> MyOrdersScreen({ screen = "home" }, openMenu, onOpenActive = { if (Drv.nowOrders.isNotEmpty()) screen = "active" else toast("لا يوجد طلب نشط") }, toast = toast)
                        "myoffers" -> MyOffersScreen({ screen = "home" }, openMenu, toast)
                        "bid" -> bidJob?.let { j -> BidScreen(j, { screen = "home" }, openMenu, onSend = { amt -> scope.launch { val ok = if (j.isStore) repoStoreBid(j.oid, amt, toast) else repoBid(j.oid, amt, toast); if (ok) screen = "wait" } }) }
                        "wait" -> WaitScreen(bidJob?.cust ?: "الزبون", bidJob?.let { Drv.received.firstOrNull { o -> o.oid == it.oid }?.price ?: it.price } ?: 0, { screen = "home" }, openMenu)
                        "active" -> ActiveScreen(Drv.nowOrders.firstOrNull(), Drv.fare.value, { screen = "home" }, openMenu, toast,
                            onStatus = { st -> scope.launch { Drv.nowOrders.firstOrNull()?.let { o -> if (o.isStore) { val mapped = if (st == "loaded") "picked_up" else st; if (repoStoreStatus(o.oid, mapped, toast)) { if (st == "delivered") Drv.activeStep.value = 4 else { val idx = Drv.nowOrders.indexOfFirst { it.oid == o.oid }; if (idx >= 0) Drv.nowOrders[idx] = Drv.nowOrders[idx].copy(status = mapped); Drv.activeStep.value = statusToStep(mapped) } } } else { if (repoStatus(o.oid, st, toast)) { if (st == "delivered") Drv.activeStep.value = 4 else { repoActive(toast) } } } } } },
                            onFinish = { scope.launch { repoNow(toast); repoPast(toast); repoDash(toast) }; screen = "home" }, onExpand = { screen = "routemap" },
                            onChat = { if (Drv.nowOrders.isNotEmpty()) { DrvNotif.kind = null; DrvNotif.orderId = null; screen = "chat" } })
                        "chat" -> {
                            val nk = DrvNotif.kind; val nid = DrvNotif.orderId
                            if (nid != null) ChatScreen(nk ?: "store", nid, "محادثة الزبون", { DrvNotif.kind = null; DrvNotif.orderId = null; screen = "home" }, openMenu, toast)
                            else Drv.nowOrders.firstOrNull()?.let { j -> ChatScreen(if (j.isStore) "store" else "transport", j.oid, "محادثة ${j.cust}", { screen = "active" }, openMenu, toast) } ?: run { screen = "home" }
                        }
                        "earn" -> EarnScreen({ screen = "home" }, openMenu, toast)
                        "profile" -> ProfileScreen({ screen = "home" }, openMenu, onLogout = { logout() }, toast, onNav = { screen = it })
                        "company" -> CompanyScreen({ screen = "profile" }, openMenu, toast)
                        "vehicle" -> DriverInfoScreen("vehicle", { screen = "profile" }, openMenu, toast)
                        "documents" -> DriverInfoScreen("documents", { screen = "profile" }, openMenu, toast)
                        "myservices" -> DriverInfoScreen("myservices", { screen = "profile" }, openMenu, toast)
                        "mypackages" -> DriverSubscriptionsScreen({ screen = "home" }, openMenu, toast)
                        "support" -> DriverInfoScreen("support", { screen = "profile" }, openMenu, toast)
                        "notifications" -> NotificationsScreen({ screen = "home" }, openMenu)
                        "payments" -> PaymentsScreen({ screen = "home" }, openMenu, toast)
                        "ordersmap" -> OrdersMapFull({ screen = "home" })
                        "routemap" -> RouteMapFull(Drv.nowOrders.firstOrNull(), { screen = "active" })
                    }
                    }
                }
                BottomNav(current = screen, onSelect = { screen = it })
            }
        }

        DrawerOverlay(open = drawerOpen, current = screen, onClose = { drawerOpen = false }, onNavigate = nav, toast = toast)

        AnimatedVisibility(visible = toastMsg != null, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp, start = 22.dp, end = 22.dp)) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(C.head).padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.Center) { T(toastMsg ?: "", 12, FontWeight.Bold, Color.White) }
        }
    }
}
