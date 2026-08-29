package com.matnokh.tajer
import com.matnokh.tajer.ui.tr

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.matnokh.tajer.net.Fcm
import com.matnokh.tajer.net.NotificationBus
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import com.matnokh.tajer.net.Session
import com.matnokh.tajer.ui.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Session.init(this)
        com.matnokh.tajer.ui.Lang.init(this)
        Fcm.init(this)
        enableEdgeToEdge()
        handleNotificationIntent(intent)
        setContent { MatnokhTheme { androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides if (com.matnokh.tajer.ui.Lang.isAr) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr) { Root() } } }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    // يقرأ بيانات الإشعار (من الضغط عليه) ويحدّد وجهة الفتح
    private fun handleNotificationIntent(intent: android.content.Intent?) {
        val orderId = intent?.getStringExtra("order_id")?.toIntOrNull()
        val type = intent?.getStringExtra("type")
        if (orderId != null || type != null) NotificationBus.routeFrom(type, orderId)
    }
}

@Composable
fun Root() {
    var screen by remember { mutableStateOf(if (Session.isLoggedIn()) "dash" else "splash") }
    var drawerOpen by remember { mutableStateOf(false) }
    var editProductId by remember { mutableStateOf<Int?>(null) }
    var subActive by remember { mutableStateOf(true) }
    var toastMsg by remember { mutableStateOf<String?>(null) }
    val toast: (String) -> Unit = { toastMsg = it }
    val openMenu = { drawerOpen = true }
    val logout = { Session.clear(); drawerOpen = false; screen = "login" }
    val nav: (String) -> Unit = { if (it == "splash" || it == "logout") logout() else { if (it == "newproduct") editProductId = null; screen = it } }

    val ctx = LocalContext.current
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    LaunchedEffect(Unit) {
        val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) perms.add(Manifest.permission.POST_NOTIFICATIONS)
        permLauncher.launch(perms.toTypedArray())
        Fcm.registerToken(ctx)
    }

    LaunchedEffect(toastMsg) { if (toastMsg != null) { delay(2400); toastMsg = null } }
    LaunchedEffect(screen) {
        if (Session.isLoggedIn() && screen in listOf("dash", "products", "sections", "store", "branches")) {
            subActive = (runCatching { com.matnokh.tajer.net.Net.api.subscription() }.getOrNull()?.subscription?.status == "active")
        }
    }
    LaunchedEffect(Unit) {
        if (com.matnokh.tajer.net.Session.isLoggedIn()) {
            com.matnokh.tajer.ui.StoreInfo.logo.value = com.matnokh.tajer.net.Session.logo
            runCatching { com.matnokh.tajer.net.Net.api.store() }.getOrNull()?.let { com.matnokh.tajer.ui.StoreInfo.logo.value = it.store.logo; com.matnokh.tajer.net.Session.logo = it.store.logo }
        }
    }

    // توجيه عند الضغط على الإشعار → الانتقال للوجهة (بعد الدخول)
    LaunchedEffect(NotificationBus.pendingScreen, Session.isLoggedIn()) {
        val s = NotificationBus.pendingScreen
        if (s != null && Session.isLoggedIn()) { screen = s; NotificationBus.pendingScreen = null }
    }


    Box(Modifier.fillMaxSize().background(C.bg)) {
        when (screen) {
            "splash" -> SplashScreen(onEnter = { screen = "login" })
            "login" -> LoginScreen(onLoggedIn = { screen = "dash"; Fcm.registerToken(ctx) }, onRegister = { screen = "register" }, toast = toast)
            "register" -> RegisterScreen(onDone = { msg -> toast(msg); screen = "login" }, onBackToLogin = { screen = "login" }, toast = toast)
            else -> Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    when (screen) {
                        "dash" -> DashScreen(onMenu = openMenu, onOpenOrders = { screen = "orders" }, onNotifications = { screen = "notifications" }, toast = toast)
                        "products" -> ProductsScreen(onBack = { screen = "dash" }, onMenu = openMenu, onNewProduct = { if (subActive) { editProductId = null; screen = "newproduct" } else toast(tr("عذراً، يرجى إكمال عملية الدفع أو التواصل مع مدير التطبيق", "Sorry, please complete the payment or contact the app admin")) }, onEdit = { if (subActive) { editProductId = it; screen = "newproduct" } else toast(tr("عذراً، يرجى إكمال عملية الدفع أو التواصل مع مدير التطبيق", "Sorry, please complete the payment or contact the app admin")) }, toast = toast)
                        "newproduct" -> NewProductScreen(productId = editProductId, onBack = { screen = "products" }, onMenu = openMenu, onNewSection = { screen = "sections" }, toast = toast)
                        "sections" -> SectionsScreen(onBack = { screen = "store" }, onMenu = openMenu, toast = toast, canWrite = subActive)
                        "branches" -> BranchesScreen(onBack = { screen = "store" }, onMenu = openMenu, toast = toast)
                        "documents" -> DocumentsScreen(onBack = { screen = "store" }, onMenu = openMenu, toast = toast)
                        "orders" -> OrdersScreen(onBack = { screen = "dash" }, onMenu = openMenu, toast = toast)
                        "wallet" -> WalletScreen(onBack = { screen = "dash" }, onMenu = openMenu, onPayments = { screen = "payments" }, toast = toast)
                        "reports" -> ReportsScreen(onBack = { screen = "dash" }, onMenu = openMenu, toast = toast)
                        "offers" -> OffersScreen(onBack = { screen = "dash" }, onMenu = openMenu, toast = toast)
                        "payments" -> PaymentsScreen(onBack = { screen = "wallet" }, onMenu = openMenu, toast = toast)
                        "store" -> StoreScreen(onBack = { screen = "dash" }, onMenu = openMenu,
                            onBranches = { screen = "branches" }, onSections = { screen = "sections" }, onDocuments = { screen = "documents" }, onStoreData = { screen = "storedata" }, onLogout = logout, toast = toast)
                        "storedata" -> StoreDataScreen(onBack = { screen = "store" }, onMenu = openMenu, toast = toast)
                        "notifications" -> NotificationsScreen(onBack = { screen = "dash" }, onMenu = openMenu, onOpen = { t -> when (t) { "chat", "new_order", "order_update", "store_offer", "store_assigned" -> screen = "orders"; "subscription", "withdraw" -> screen = "wallet"; else -> {} } }, toast = toast)
                        "packages" -> PackagesScreen(onBack = { screen = "dash" }, onMenu = openMenu, toast = toast)
                    }
                }
                BottomNav(current = screen, onSelect = { screen = it })
            }
        }

        DrawerOverlay(open = drawerOpen, current = screen, onClose = { drawerOpen = false }, onNavigate = nav, toast = toast)

        AnimatedVisibility(
            visible = toastMsg != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomStart).padding(26.dp).navigationBarsPadding(),
        ) {
            Box(Modifier.clip(RoundedCornerShape(16.dp)).background(C.head).padding(horizontal = 20.dp, vertical = 13.dp)) {
                T(toastMsg ?: "", 12, FontWeight.Bold, Color.White)
            }
        }

        // بطاقة الطلب الجديد داخل التطبيق (تظهر عند وصول إشعار وFCM والتطبيق مفتوح)
        NotificationBus.incoming?.let { msg ->
            IncomingOrderCard(
                orderId = msg.orderId,
                fallbackTitle = msg.title,
                fallbackBody = msg.body,
                onClose = { NotificationBus.dismiss(); NotificationBus.markSeen() },
                onOpen = { msg.orderId?.let { NotificationBus.pendingOrderId = it }; screen = "orders" },
                toast = toast,
            )
        }
    }
}
