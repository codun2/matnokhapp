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
                if (bytes == null) { toast("تعذّرت قراءة الصورة"); busy = false; return@launch }
                val part = MultipartBody.Part.createFormData("file", "avatar.jpg", bytes.toRequestBody("image/*".toMediaTypeOrNull()))
                val up = Net.api.upload(part)
                if (up.url != null) {
                    Net.api.updateProfile(ProfileBody(avatar = up.url))
                    Session.avatar = up.url; url = up.url; toast("تم تحديث صورتك ✓")
                } else toast("فشل الرفع")
            } catch (e: Exception) { toast("تعذّر رفع الصورة") }
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
            else -> T(Session.name?.take(2) ?: "زا", 18, FontWeight.ExtraBold, Color.White)
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
        ScreenHeader("عناويني", onBack, onMenu)
        if (loading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }; return@Column }
        if (!Session.isLoggedIn()) { Box(Modifier.fillMaxSize().padding(30.dp), contentAlignment = Alignment.Center) { T("سجّل الدخول لإدارة عناوينك", 13, FontWeight.Bold, C.muted) }; return@Column }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            AddrSection("عناوين الاستقبال", "توصَّل إليها طلباتك", "receive", items, onAdd = { addType = "receive" }, onEdit = { edit = it }, onDel = { a -> scope.launch { runCatching { Net.api.delAddress(a.id) }; reload(); toast("تم الحذف") } })
            AddrSection("عناوين الاستلام", "يُستلَم منها الطلب", "pickup", items, onAdd = { addType = "pickup" }, onEdit = { edit = it }, onDel = { a -> scope.launch { runCatching { Net.api.delAddress(a.id) }; reload(); toast("تم الحذف") } })
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
                    Row(verticalAlignment = Alignment.CenterVertically) { T(a.label, 13, FontWeight.Bold, C.head, maxLines = 1); if (a.is_default) { Spacer(Modifier.width(7.dp)); Box(Modifier.clip(CircleShape).background(C.pillLive).padding(horizontal = 8.dp, vertical = 2.dp)) { T("افتراضي", 9, FontWeight.ExtraBold, C.greenD) } } }
                    a.address?.takeIf { it.isNotBlank() }?.let { Spacer(Modifier.height(2.dp)); T(it, 10, FontWeight.Normal, C.muted, maxLines = 1) }
                }
                Box(Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(C.redBg).clickable { onDel(a) }, contentAlignment = Alignment.Center) { T("×", 15, FontWeight.Black, C.redText) }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color(0xFFF2F8F3)).border(1.5.dp, Color(0xFFCFE0D4), RoundedCornerShape(15.dp)).clickable(onClick = onAdd).padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Ic(R.drawable.ic_plus, 16.dp, C.greenD); Spacer(Modifier.width(7.dp)); T("إضافة عنوان ${if (type == "receive") "استقبال" else "استلام"}", 12, FontWeight.ExtraBold, C.greenD)
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
        ScreenHeader(if (existing == null) "عنوان ${if (type == "receive") "استقبال" else "استلام"} جديد" else "تعديل العنوان", onBack, {})
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 6.dp).fillMaxWidth()) {
                OcTitle(R.drawable.ic_pin, "حدّد الموقع على الخريطة")
                Box(Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, C.line, RoundedCornerShape(16.dp))) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(), cameraPositionState = camera,
                        properties = MapProperties(isMyLocationEnabled = granted),
                        uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false, mapToolbarEnabled = false, compassEnabled = false),
                    ) { OsmTiles() }
                    Box(Modifier.align(Alignment.Center).padding(bottom = 40.dp)) { Ic(R.drawable.ic_pin, 44.dp, if (type == "receive") C.greenD else C.terraText) }
                    Box(Modifier.align(Alignment.CenterEnd).padding(12.dp).size(44.dp).clip(CircleShape).background(C.card).border(1.dp, C.line, CircleShape).clickable {
                        scope.launch { currentLatLng(ctx)?.let { camera.position = CameraPosition.fromLatLngZoom(LatLng(it.first, it.second), 16f) } ?: toast("تعذّر تحديد موقعك") }
                    }, contentAlignment = Alignment.Center) { Ic(R.drawable.ic_nav, 21.dp, C.greenD) }
                    Box(Modifier.align(Alignment.BottomCenter).padding(10.dp).clip(RoundedCornerShape(50.dp)).background(Grad.green).clickable {
                        val t = camera.position.target; lat = t.latitude; lng = t.longitude
                        scope.launch { addr = reverseName(t.latitude, t.longitude) ?: addr }
                        toast("تم تحديد الموقع ✓")
                    }.padding(horizontal = 16.dp, vertical = 9.dp)) { T("تثبيت هذا الموقع", 12, FontWeight.ExtraBold, Color.White) }
                }
                if (lat != null) { Spacer(Modifier.height(8.dp)); T("📍 ${addr.ifBlank { "موقع محدّد" }}", 11, FontWeight.Bold, C.greenD, maxLines = 1) }
            }
            Spacer(Modifier.height(12.dp))
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                OcTitle(R.drawable.ic_msg, "اسم المكان (لتسهيل الوصول لاحقاً)")
                FinField(label, { label = it }, "مثال: البيت · العمل · بيت العائلة")
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth().clickable { isDefault = !isDefault }, verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(22.dp).clip(RoundedCornerShape(7.dp)).background(if (isDefault) C.green else C.card2).border(1.dp, if (isDefault) Color.Transparent else C.line, RoundedCornerShape(7.dp)), contentAlignment = Alignment.Center) { if (isDefault) T("✓", 12, FontWeight.Black, Color.White) }
                    Spacer(Modifier.width(9.dp)); T("اجعله العنوان الافتراضي لهذا النوع", 12, FontWeight.Bold, C.head)
                }
            }
            Spacer(Modifier.height(16.dp))
            WideButton(if (saving) "جارٍ الحفظ…" else "حفظ العنوان", R.drawable.ic_check, modifier = Modifier.padding(horizontal = 22.dp)) {
                if (saving) return@WideButton
                if (label.isBlank()) { toast("اكتب اسماً للمكان"); return@WideButton }
                if (lat == null) { toast("حدّد الموقع على الخريطة"); return@WideButton }
                saving = true
                scope.launch {
                    val body = AddressBody(type, label.trim(), addr.ifBlank { null }, lat, lng, isDefault)
                    val r = runCatching { if (existing == null) Net.api.addAddress(body) else Net.api.updateAddress(existing.id, body) }
                    saving = false
                    if (r.isSuccess) { toast(r.getOrNull()?.message ?: "تم الحفظ ✓"); onSaved() } else toast("تعذّر الحفظ")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ═══════════ وسائل الدفع (مرتّبة حسب الاستخدام) ═══════════ */
private data class PayKind(val type: String, val label: String, val emoji: String)
private val PAY_KINDS = listOf(
    PayKind("cash", "نقداً عند الاستلام", "💵"),
    PayKind("mada", "مدى", "💳"),
    PayKind("card", "بطاقة ائتمان", "💳"),
    PayKind("stcpay", "STC Pay", "📱"),
    PayKind("applepay", "Apple Pay", ""),
    PayKind("bank", "تحويل بنكي", "🏦"),
)
private fun payLabel(type: String, fallback: String?): String = PAY_KINDS.firstOrNull { it.type == type }?.label ?: (fallback ?: type)
private fun payEmoji(type: String): String = PAY_KINDS.firstOrNull { it.type == type }?.emoji ?: "💳"

@Composable
fun AccountPayMethods(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var items by remember { mutableStateOf<List<PayMethodDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var adding by remember { mutableStateOf(false) }
    suspend fun reload() { runCatching { items = Net.api.payMethods().methods }; loading = false }
    LaunchedEffect(Unit) { if (Session.isLoggedIn()) reload() else loading = false }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("وسائل الدفع", onBack, onMenu)
        if (loading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }; return@Column }
        if (!Session.isLoggedIn()) { Box(Modifier.fillMaxSize().padding(30.dp), contentAlignment = Alignment.Center) { T("سجّل الدخول لإدارة وسائل الدفع", 13, FontWeight.Bold, C.muted) }; return@Column }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(22.dp)) {
            if (items.isNotEmpty()) item { T("الأكثر استخداماً أولاً", 11, FontWeight.Bold, C.muted); Spacer(Modifier.height(10.dp)) }
            itemsIndexed(items) { idx, m ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp)).background(C.card).border(1.dp, if (idx == 0) C.green else C.line, RoundedCornerShape(16.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(C.pillLive), contentAlignment = Alignment.Center) { T(payEmoji(m.type), 18, FontWeight.Bold, C.greenD) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { T(payLabel(m.type, m.label), 13, FontWeight.Bold, C.head); if (idx == 0 && m.uses_count > 0) { Spacer(Modifier.width(7.dp)); Box(Modifier.clip(CircleShape).background(Grad.green).padding(horizontal = 8.dp, vertical = 2.dp)) { T("الأكثر استخداماً", 9, FontWeight.ExtraBold, Color.White) } } }
                        T(if (m.uses_count > 0) "استُخدمت ${m.uses_count} مرة" else "لم تُستخدم بعد", 10, FontWeight.Medium, C.muted)
                    }
                    Box(Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(C.redBg).clickable { scope.launch { runCatching { Net.api.delPayMethod(m.id) }; reload(); toast("تم الحذف") } }, contentAlignment = Alignment.Center) { T("×", 15, FontWeight.Black, C.redText) }
                }
            }
            if (items.isEmpty()) item { Box(Modifier.fillMaxWidth().padding(vertical = 30.dp), contentAlignment = Alignment.Center) { T("لم تستخدم أي وسيلة دفع بعد", 12, FontWeight.Medium, C.muted) } }
            item {
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color(0xFFF2F8F3)).border(1.5.dp, Color(0xFFCFE0D4), RoundedCornerShape(15.dp)).clickable { adding = true }.padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Ic(R.drawable.ic_plus, 16.dp, C.greenD); Spacer(Modifier.width(7.dp)); T("إضافة وسيلة دفع", 12, FontWeight.ExtraBold, C.greenD)
                }
            }
        }
    }

    if (adding) {
        val existing = items.map { it.type }.toSet()
        androidx.compose.ui.window.Dialog(onDismissRequest = { adding = false }) {
            Column(Modifier.clip(RoundedCornerShape(22.dp)).background(C.bg).padding(20.dp)) {
                T("اختر وسيلة دفع", 15, FontWeight.ExtraBold, C.head)
                Spacer(Modifier.height(14.dp))
                PAY_KINDS.filter { it.type !in existing }.forEach { k ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(14.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(14.dp)).clickable {
                        scope.launch { runCatching { Net.api.addPayMethod(PayMethodBody(k.type, k.label)) }; adding = false; reload(); toast("تمت الإضافة ✓") }
                    }.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(C.pillLive), contentAlignment = Alignment.Center) { T(k.emoji, 16, FontWeight.Bold, C.greenD) }
                        Spacer(Modifier.width(11.dp)); T(k.label, 13, FontWeight.Bold, C.head)
                    }
                }
                if (PAY_KINDS.all { it.type in existing }) T("أضفت كل الوسائل المتاحة", 12, FontWeight.Medium, C.muted)
            }
        }
    }
}


/* صورة الزبون المصغّرة (تظهر أينما وُجدت صورة الزبون) */
@Composable
fun MiniAvatar(size: Dp, corner: Dp, font: Int = 15) {
    val url = Session.avatar
    if (!url.isNullOrBlank()) AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(size).clip(RoundedCornerShape(corner)))
    else Box(Modifier.size(size).clip(RoundedCornerShape(corner)).background(Grad.terra), contentAlignment = Alignment.Center) { T(Session.name?.take(2) ?: "زا", font, FontWeight.ExtraBold, Color.White) }
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
        ScreenHeader("المفضّلة", onBack, onMenu)
        val list = favStores
        when {
            list == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
            list.isEmpty() -> Box(Modifier.fillMaxSize().padding(30.dp), contentAlignment = Alignment.Center) { T("لم تُضِف متاجر للمفضّلة بعد — اضغط ❤ على بطاقة أي متجر", 13, FontWeight.Bold, C.muted) }
            else -> LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) { items(list) { s -> StoreRow(s) { onStore(s) } } }
        }
    }
}
