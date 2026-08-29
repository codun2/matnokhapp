package com.matnokh.customer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.matnokh.customer.R
import com.matnokh.customer.net.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/* ═══════════ الصورة الشخصية (تُستخدم في شاشة حسابي) ═══════════ */
@Composable
fun ProfileAvatar(toast: (String) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var url by remember { mutableStateOf(Session.avatar) }
    var busy by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            try {
                val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) { toast(tr("تعذّرت قراءة الصورة", "Couldn't read the image")); busy = false; return@launch }
                val part = MultipartBody.Part.createFormData("file", "avatar.jpg", bytes.toRequestBody("image/*".toMediaTypeOrNull()))
                val up = Net.api.upload(part)
                if (up.url != null) {
                    Net.api.updateProfile(ProfileBody(avatar = up.url))
                    Session.avatar = up.url; url = up.url; toast(tr("تم تحديث صورتك ✓", "Your photo was updated ✓"))
                } else toast(tr("فشل الرفع", "Upload failed"))
            } catch (e: Exception) { toast(tr("تعذّر رفع الصورة", "Couldn't upload the image")) }
            busy = false
        }
    }
    Box(
        Modifier.size(56.dp).clip(RoundedCornerShape(19.dp)).background(Grad.terra)
            .clickable(enabled = Session.isLoggedIn() && !busy) { picker.launch("image/*") },
        contentAlignment = Alignment.Center,
    ) {
        when {
            busy -> CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            !url.isNullOrBlank() -> AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else -> T(Session.name?.take(2) ?: tr("زا", "Za"), 18, FontWeight.ExtraBold, Color.White)
        }
        if (Session.isLoggedIn() && !busy) Box(Modifier.align(Alignment.BottomEnd).size(20.dp).clip(CircleShape).background(Grad.green), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_plus, 12.dp, Color.White) }
    }
}

/* ═══════════ عناويني: استقبال + استلام ═══════════ */
@Composable
fun AddressesScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<AddressDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var edit by remember { mutableStateOf<AddressDto?>(null) }
    var addType by remember { mutableStateOf<String?>(null) }
    suspend fun reload() { runCatching { items = Net.api.addresses().addresses }; loading = false }
    LaunchedEffect(Unit) { if (Session.isLoggedIn()) reload() else loading = false }

    if (edit != null || addType != null) {
        AddressEdit(existing = edit, type = addType ?: edit!!.type,
            onBack = { edit = null; addType = null },
            onSaved = { edit = null; addType = null; scope.launch { reload() } }, toast = toast)
        return
    }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("عناويني", "My addresses"), onBack, onMenu)
        if (loading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }; return@Column }
        if (!Session.isLoggedIn()) { Box(Modifier.fillMaxSize().padding(30.dp), contentAlignment = Alignment.Center) { T(tr("سجّل الدخول لإدارة عناوينك", "Log in to manage your addresses"), 13, FontWeight.Bold, C.muted) }; return@Column }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            AddrSection(tr("عناوين الاستقبال", "Receiving addresses"), tr("توصَّل إليها طلباتك", "Where your orders are delivered"), "receive", items, onAdd = { addType = "receive" }, onEdit = { edit = it }, onDel = { a -> scope.launch { runCatching { Net.api.delAddress(a.id) }; reload(); toast(tr("تم الحذف", "Deleted")) } })
            AddrSection(tr("عناوين الاستلام", "Pickup addresses"), tr("يُستلَم منها الطلب", "Where the order is picked up"), "pickup", items, onAdd = { addType = "pickup" }, onEdit = { edit = it }, onDel = { a -> scope.launch { runCatching { Net.api.delAddress(a.id) }; reload(); toast(tr("تم الحذف", "Deleted")) } })
        }
    }
}

@Composable
private fun AddrSection(title: String, sub: String, type: String, all: List<AddressDto>, onAdd: () -> Unit, onEdit: (AddressDto) -> Unit, onDel: (AddressDto) -> Unit) {
    val list = all.filter { it.type == type }
    val color = if (type == "receive") C.green else C.terra
    Column(Modifier.padding(horizontal = 22.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(if (type == "receive") C.pillLive else Color(0xFFF6ECE4)), contentAlignment = Alignment.Center) { Ic(if (type == "receive") R.drawable.ic_pin else R.drawable.ic_flag, 17.dp, color) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) { T(title, 14, FontWeight.ExtraBold, C.head); T(sub, 10, FontWeight.Normal, C.muted) }
        }
        Spacer(Modifier.height(10.dp))
        list.forEach { a ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(16.dp)).clickable { onEdit(a) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Ic(R.drawable.ic_pin, 18.dp, color); Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { T(a.label, 13, FontWeight.Bold, C.head, maxLines = 1); if (a.is_default) { Spacer(Modifier.width(7.dp)); Box(Modifier.clip(CircleShape).background(C.pillLive).padding(horizontal = 8.dp, vertical = 2.dp)) { T(tr("افتراضي", "Default"), 9, FontWeight.ExtraBold, C.greenD) } } }
                    a.address?.takeIf { it.isNotBlank() }?.let { Spacer(Modifier.height(2.dp)); T(it, 10, FontWeight.Normal, C.muted, maxLines = 1) }
                }
                Box(Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(C.redBg).clickable { onDel(a) }, contentAlignment = Alignment.Center) { T("×", 15, FontWeight.Black, C.redText) }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color(0xFFF2F8F3)).border(1.5.dp, Color(0xFFCFE0D4), RoundedCornerShape(15.dp)).clickable(onClick = onAdd).padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Ic(R.drawable.ic_plus, 16.dp, C.greenD); Spacer(Modifier.width(7.dp)); T(tr("إضافة عنوان ${if (type == "receive") "استقبال" else "استلام"}", "Add ${if (type == "receive") "receiving" else "pickup"} address"), 12, FontWeight.ExtraBold, C.greenD)
        }
    }
}

@Composable
private fun AddressEdit(existing: AddressDto?, type: String, onBack: () -> Unit, onSaved: () -> Unit, toast: (String) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var label by remember { mutableStateOf(existing?.label ?: "") }
    var addr by remember { mutableStateOf(existing?.address ?: "") }
    var lat by remember { mutableStateOf(existing?.lat) }
    var lng by remember { mutableStateOf(existing?.lng) }
    var isDefault by remember { mutableStateOf(existing?.is_default ?: false) }
    var saving by remember { mutableStateOf(false) }
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    val start = LatLng(lat ?: 24.7136, lng ?: 46.6753)
    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(start, if (lat != null) 16f else 12f) }
    LaunchedEffect(Unit) {
        if (!granted) launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        if (lat == null) currentLatLng(ctx)?.let { camera.position = CameraPosition.fromLatLngZoom(LatLng(it.first, it.second), 16f) }
    }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(if (existing == null) tr("عنوان ${if (type == "receive") "استقبال" else "استلام"} جديد", "New ${if (type == "receive") "receiving" else "pickup"} address") else tr("تعديل العنوان", "Edit address"), onBack, {})
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 6.dp).fillMaxWidth()) {
                OcTitle(R.drawable.ic_pin, tr("حدّد الموقع على الخريطة", "Set location on the map"))
                Box(Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, C.line, RoundedCornerShape(16.dp))) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(), cameraPositionState = camera,
                        properties = MapProperties(isMyLocationEnabled = granted),
                        uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false, mapToolbarEnabled = false, compassEnabled = false),
                    ) { OsmTiles() }
                    Box(Modifier.align(Alignment.Center).padding(bottom = 40.dp)) { Ic(R.drawable.ic_pin, 44.dp, if (type == "receive") C.greenD else C.terraText) }
                    Box(Modifier.align(Alignment.CenterEnd).padding(12.dp).size(44.dp).clip(CircleShape).background(C.card).border(1.dp, C.line, CircleShape).clickable {
                        scope.launch { currentLatLng(ctx)?.let { camera.position = CameraPosition.fromLatLngZoom(LatLng(it.first, it.second), 16f) } ?: toast(tr("تعذّر تحديد موقعك", "Couldn't determine your location")) }
                    }, contentAlignment = Alignment.Center) { Ic(R.drawable.ic_nav, 21.dp, C.greenD) }
                    Box(Modifier.align(Alignment.BottomCenter).padding(10.dp).clip(RoundedCornerShape(50.dp)).background(Grad.green).clickable {
                        val t = camera.position.target; lat = t.latitude; lng = t.longitude
                        scope.launch { addr = reverseName(t.latitude, t.longitude) ?: addr }
                        toast(tr("تم تحديد الموقع ✓", "Location set ✓"))
                    }.padding(horizontal = 16.dp, vertical = 9.dp)) { T(tr("تثبيت هذا الموقع", "Set this location"), 12, FontWeight.ExtraBold, Color.White) }
                }
                if (lat != null) { Spacer(Modifier.height(8.dp)); T(tr("📍 ${addr.ifBlank { "موقع محدّد" }}", "📍 ${addr.ifBlank { "Set location" }}"), 11, FontWeight.Bold, C.greenD, maxLines = 1) }
            }
            Spacer(Modifier.height(12.dp))
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                OcTitle(R.drawable.ic_msg, tr("اسم المكان (لتسهيل الوصول لاحقاً)", "Place name (for easier access later)"))
                FinField(label, { label = it }, tr("مثال: البيت · العمل · بيت العائلة", "e.g. Home · Work · Family home"))
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth().clickable { isDefault = !isDefault }, verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(22.dp).clip(RoundedCornerShape(7.dp)).background(if (isDefault) C.green else C.card2).border(1.dp, if (isDefault) Color.Transparent else C.line, RoundedCornerShape(7.dp)), contentAlignment = Alignment.Center) { if (isDefault) T("✓", 12, FontWeight.Black, Color.White) }
                    Spacer(Modifier.width(9.dp)); T(tr("اجعله العنوان الافتراضي لهذا النوع", "Make it the default address for this type"), 12, FontWeight.Bold, C.head)
                }
            }
            Spacer(Modifier.height(16.dp))
            WideButton(if (saving) tr("جارٍ الحفظ…", "Saving…") else tr("حفظ العنوان", "Save address"), R.drawable.ic_check, modifier = Modifier.padding(horizontal = 22.dp)) {
                if (saving) return@WideButton
                if (label.isBlank()) { toast(tr("اكتب اسماً للمكان", "Enter a name for the place")); return@WideButton }
                if (lat == null) { toast(tr("حدّد الموقع على الخريطة", "Set location on the map")); return@WideButton }
                saving = true
                scope.launch {
                    val body = AddressBody(type, label.trim(), addr.ifBlank { null }, lat, lng, isDefault)
                    val r = runCatching { if (existing == null) Net.api.addAddress(body) else Net.api.updateAddress(existing.id, body) }
                    saving = false
                    if (r.isSuccess) { toast(r.getOrNull()?.message ?: tr("تم الحفظ ✓", "Saved ✓")); onSaved() } else toast(tr("تعذّر الحفظ", "Couldn't save"))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ═══════════ وسائل الدفع (مرتّبة حسب الاستخدام) ═══════════ */
private data class PayKind(val type: String, val label: String, val emoji: String)
private val PAY_KINDS = listOf(
    PayKind("cash", tr("نقداً عند الاستلام", "Cash on delivery"), "💵"),
    PayKind("mada", tr("مدى", "Mada"), "💳"),
    PayKind("card", tr("بطاقة ائتمان", "Credit card"), "💳"),
    PayKind("stcpay", "STC Pay", "📱"),
    PayKind("applepay", "Apple Pay", ""),
    PayKind("bank", tr("تحويل بنكي", "Bank transfer"), "🏦"),
)
private fun payLabel(type: String, fallback: String?): String = PAY_KINDS.firstOrNull { it.type == type }?.label ?: (fallback ?: type)
private fun payEmoji(type: String): String = PAY_KINDS.firstOrNull { it.type == type }?.emoji ?: "💳"

@Composable
fun AccountPayMethods(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("طرق الدفع", "Payment methods"), onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(22.dp)) {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFFEEF4EF)).border(1.dp, Color(0xFFCFE0D4), RoundedCornerShape(18.dp)).padding(14.dp), verticalAlignment = Alignment.Top) {
                Ic(R.drawable.ic_card, 20.dp, C.greenD, Modifier.padding(top = 1.dp))
                Spacer(Modifier.width(11.dp))
                T(tr("تختار طريقة الدفع عند تأكيد كل طلب. هذه هي الطرق المتاحة:", "You pick the payment method when confirming each order. These are the available methods:"), 11, FontWeight.Medium, C.greenD, lineHeight = 18)
            }
            Spacer(Modifier.height(14.dp))
            PayInfoCard("\uD83D\uDCB3", tr("بطاقة", "Card"), tr("دفع إلكتروني آمن عبر بوابة الدفع — مدى · فيزا · ماستركارد.", "Secure online payment via the gateway — Mada · Visa · Mastercard."))
            PayInfoCard("\uD83C\uDFE6", tr("تحويل بنكي", "Bank transfer"), tr("تحوّل قيمة المشتريات لحساب المتجر وترفع صورة الإيصال.", "Transfer the items value to the store's account and upload the receipt."))
            PayInfoCard("\uD83D\uDCB5", tr("نقداً عند الاستلام", "Cash on delivery"), tr("تدفع للمندوب نقداً عند استلام طلبك.", "Pay the courier in cash when you receive your order."))
        }
    }
}

@Composable
private fun PayInfoCard(emoji: String, title: String, desc: String) {
    Row(Modifier.fillMaxWidth().padding(bottom = 11.dp).clip(RoundedCornerShape(18.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(18.dp)).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(C.pillLive), contentAlignment = Alignment.Center) { T(emoji, 20, FontWeight.Bold, C.greenD) }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            T(title, 14, FontWeight.Bold, C.head)
            Spacer(Modifier.height(3.dp))
            T(desc, 10, FontWeight.Medium, C.muted, lineHeight = 16)
        }
    }
}

/* صورة الزبون المصغّرة (تظهر أينما وُجدت صورة الزبون) */
@Composable
fun MiniAvatar(size: Dp, corner: Dp, font: Int = 15) {
    val url = Session.avatar
    if (!url.isNullOrBlank()) AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(size).clip(RoundedCornerShape(corner)))
    else Box(Modifier.size(size).clip(RoundedCornerShape(corner)).background(Grad.terra), contentAlignment = Alignment.Center) { T(Session.name?.take(2) ?: tr("زا", "Za"), font, FontWeight.ExtraBold, Color.White) }
}

/* شريط أفقي لعناوين الزبون المحفوظة للاختيار السريع داخل الخريطة */
@Composable
fun SavedAddrStrip(type: String, onPick: (AddressDto) -> Unit) {
    if (!Session.isLoggedIn()) return
    var items by remember(type) { mutableStateOf<List<AddressDto>>(emptyList()) }
    LaunchedEffect(type) { runCatching { items = Net.api.addresses().addresses.filter { it.type == type } } }
    if (items.isEmpty()) return
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 4.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { a ->
            Row(Modifier.clip(CircleShape).background(Color(0xF2FFFFFF)).border(1.dp, C.line, CircleShape).clickable { onPick(a) }.padding(horizontal = 13.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Ic(R.drawable.ic_pin, 13.dp, C.greenD); Spacer(Modifier.width(6.dp)); T(a.label, 11, FontWeight.ExtraBold, C.greenD)
            }
        }
    }
}

@Composable
fun FavoritesScreen(onBack: () -> Unit, onMenu: () -> Unit, onStore: (UiStore) -> Unit, toast: (String) -> Unit) {
    var favStores by remember { mutableStateOf<List<UiStore>?>(null) }
    LaunchedEffect(Unit) { favStores = if (Session.isLoggedIn()) Repo.favoriteStores() else emptyList() }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("المفضّلة", "Favorites"), onBack, onMenu)
        val list = favStores
        when {
            list == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
            list.isEmpty() -> Box(Modifier.fillMaxSize().padding(30.dp), contentAlignment = Alignment.Center) { T(tr("لم تُضِف متاجر للمفضّلة بعد — اضغط ❤ على بطاقة أي متجر", "You haven't added stores to favorites yet — tap ❤ on any store card"), 13, FontWeight.Bold, C.muted) }
            else -> LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) { items(list) { s -> StoreRow(s) { onStore(s) } } }
        }
    }
}
