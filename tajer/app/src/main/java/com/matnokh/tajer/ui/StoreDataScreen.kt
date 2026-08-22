@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.matnokh.tajer.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.matnokh.tajer.R
import com.matnokh.tajer.net.CityDto
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.StoreData
import com.matnokh.tajer.net.StoreUpdate
import com.matnokh.tajer.net.call
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun StoreDataScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var store by remember { mutableStateOf<StoreData?>(null) }
    var cities by remember { mutableStateOf<List<CityDto>>(emptyList()) }

    var name by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var cityId by remember { mutableStateOf<Int?>(null) }
    var logo by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lng by remember { mutableStateOf<Double?>(null) }
    var deliveryMode by remember { mutableStateOf("fixed") }
    var deliveryFixed by remember { mutableStateOf("") }
    var deliveryPerKm by remember { mutableStateOf("") }
    var iban by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        call({ Net.api.store() }, toast)?.let {
            store = it.store
            name = it.store.store_name ?: ""
            owner = it.store.owner_name ?: ""
            address = it.store.address ?: ""
            cityId = it.store.city_id
            logo = it.store.logo
            lat = it.store.lat
            lng = it.store.lng
            deliveryMode = it.store.delivery_mode ?: "fixed"
            deliveryFixed = it.store.delivery_fixed.takeIf { v -> v > 0 }?.let { v -> if (v % 1.0 == 0.0) v.toInt().toString() else v.toString() } ?: ""
            deliveryPerKm = it.store.delivery_per_km.takeIf { v -> v > 0 }?.let { v -> if (v % 1.0 == 0.0) v.toInt().toString() else v.toString() } ?: ""
            iban = it.store.iban ?: ""
            bankName = it.store.bank_name ?: ""
            accountName = it.store.account_name ?: ""
            StoreInfo.logo.value = it.store.logo
        }
        call({ Net.api.cities() }, toast)?.let { cities = it.cities }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            uploading = true
            scope.launch {
                try {
                    val bytes = ctx.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
                    val part = MultipartBody.Part.createFormData("file", "logo.jpg", bytes.toRequestBody("image/*".toMediaTypeOrNull()))
                    call({ Net.api.upload(part) }, toast)?.let { logo = it.url; StoreInfo.logo.value = it.url; toast("تم رفع الشعار ✓") }
                } finally { uploading = false }
            }
        }
    }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("بيانات المتجر", onBack, onMenu)
        val s = store
        if (s == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
            return@Column
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {

            // الشعار
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                OcTitle(R.drawable.ic_img, "شعار المتجر")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(C.card2), contentAlignment = Alignment.Center) {
                        if (logo.isNullOrBlank()) Text("🛒", fontSize = 26.sp) else AsyncImage(model = logo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        T(if (logo.isNullOrBlank()) "لا يوجد شعار" else "تم رفع الشعار ✓", 12, FontWeight.Bold, C.head)
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF4EF))
                                .clickable(enabled = !uploading) { picker.launch("image/*") }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) { T(if (uploading) "جارٍ الرفع…" else (if (logo.isNullOrBlank()) "رفع شعار" else "تغيير الشعار"), 11, FontWeight.ExtraBold, C.greenD) }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // البيانات
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                OcTitle(R.drawable.ic_shop, "بيانات المتجر")
                FieldLabel("اسم المتجر", required = true)
                FinField(name, { name = it }, "اسم المتجر")
                FieldLabel("اسم صاحب المتجر")
                FinField(owner, { owner = it }, "الاسم الكامل")
                FieldLabel("العنوان")
                FinField(address, { address = it }, "الحي · الشارع · المدينة", singleLine = false, minHeight = 60.dp)
                FieldLabel("المدينة")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cities.forEach { c -> Chip(c.name, cityId == c.id) { cityId = c.id } }
                }
            }
            Spacer(Modifier.height(12.dp))

            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                OcTitle(R.drawable.ic_shop, "الحساب البنكي (للتحويل)")
                FieldLabel("رقم الآيبان (IBAN)")
                FinField(iban, { iban = it }, "SA00 0000 0000 0000 0000 0000")
                FieldLabel("اسم البنك")
                FinField(bankName, { bankName = it }, "مثال: مصرف الراجحي")
                FieldLabel("اسم المستفيد")
                FinField(accountName, { accountName = it }, "اسم صاحب الحساب")
                Spacer(Modifier.height(6.dp))
                T("يظهر هذا الحساب للزبون عند اختيار الدفع بتحويل بنكي. اتركه فارغاً لتعطيل التحويل.", 10, FontWeight.Normal, C.muted, lineHeight = 15)
            }
            Spacer(Modifier.height(12.dp))

            StoreLocationCard(lat, lng) { la, ln -> lat = la; lng = ln }
            Spacer(Modifier.height(12.dp))


            // حقول للقراءة فقط
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                ReadRow("رقم الهاتف (للدخول)", s.phone ?: "—")
                Divider2()
                ReadRow("البريد الإلكتروني", s.email ?: "—")
                Divider2()
                ReadRow("حالة المتجر", when (s.status) { "approved" -> "معتمد"; "pending" -> "قيد المراجعة"; "rejected" -> "مرفوض"; else -> "موقوف" })
            }
            Spacer(Modifier.height(16.dp))

            Box(Modifier.padding(horizontal = 22.dp)) {
                WideButton(if (saving) "…" else "حفظ التعديلات", if (saving) null else R.drawable.ic_check) {
                    if (saving) return@WideButton
                    if (name.isBlank()) { toast("اسم المتجر مطلوب"); return@WideButton }
                    scope.launch {
                        saving = true
                        call({ Net.api.updateStore(StoreUpdate(store_name = name.trim(), owner_name = owner.trim(), address = address.trim(), city_id = cityId, lat = lat, lng = lng, logo = logo, iban = iban.trim(), bank_name = bankName.trim(), account_name = accountName.trim())) }, toast)?.let {
                            store = it.store; com.matnokh.tajer.net.Session.logo = logo; StoreInfo.logo.value = logo; toast("تم حفظ بيانات المتجر ✓"); onBack()
                        }
                        saving = false
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReadRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        T(label, 12, FontWeight.Medium, C.muted, Modifier.weight(1f))
        T(value, 12, FontWeight.Bold, C.head)
    }
}

@Composable
private fun Divider2() {
    androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(Color(0xFFF0ECE3), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 1f)
    }
}

@Composable
fun StoreLocationCard(lat: Double?, lng: Double?, autoLocate: Boolean = false, onPick: (Double, Double) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(lat ?: 24.7136, lng ?: 46.6753), if (lat != null) 16f else 10f) }
    fun useHere() { scope.launch { currentLatLng(ctx)?.let { onPick(it.first, it.second); camera.position = CameraPosition.fromLatLngZoom(LatLng(it.first, it.second), 16f) } } }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { g -> granted = g; if (g) useHere() }
    LaunchedEffect(Unit) { if (autoLocate && lat == null && granted) useHere() }
    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
        OcTitle(R.drawable.ic_pin, "موقع المتجر")
        T("يُستخدم لإسناد أقرب مندوب إليك تلقائياً عند تجهيز الطلب. اضغط على الخريطة لتحديد الموقع، أو استخدم موقعك الحالي.", 11, FontWeight.Medium, C.muted, lineHeight = 17)
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp))) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = camera,
                properties = MapProperties(isMyLocationEnabled = granted),
                uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false, mapToolbarEnabled = false),
                onMapClick = { ll -> onPick(ll.latitude, ll.longitude) },
            ) {
                if (lat != null && lng != null) Marker(state = MarkerState(LatLng(lat, lng)), title = "موقع المتجر")
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF4EF))
                    .clickable { if (granted) useHere() else launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            ) { T("📍 استخدام موقعي الحالي", 11, FontWeight.ExtraBold, C.greenD) }
            Spacer(Modifier.width(10.dp))
            if (lat != null && lng != null) T("✓ " + "%.4f".format(lat) + ", " + "%.4f".format(lng), 10, FontWeight.Bold, C.head)
            else T("لم يُحدَّد بعد", 10, FontWeight.Bold, C.muted)
        }
    }
}

@Composable
private fun StoreDeliveryCard(mode: String, fixed: String, perKm: String, onMode: (String) -> Unit, onFixed: (String) -> Unit, onPerKm: (String) -> Unit) {
    OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
        OcTitle(R.drawable.ic_van, "تسعير التوصيل")
        T("اختر كيف تُحتسب أجرة التوصيل التي يدفعها الزبون لطلبات متجرك.", 11, FontWeight.Medium, C.muted, lineHeight = 17)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DModeChip("سعر ثابت", mode == "fixed", Modifier.weight(1f)) { onMode("fixed") }
            DModeChip("حسب الكيلومتر", mode == "per_km", Modifier.weight(1f)) { onMode("per_km") }
        }
        Spacer(Modifier.height(12.dp))
        if (mode == "per_km") {
            FieldLabel("سعر الكيلومتر الواحد (﷼)")
            FinField(perKm, onPerKm, "مثال: 2", keyboard = androidx.compose.ui.text.input.KeyboardType.Decimal)
            Spacer(Modifier.height(5.dp))
            T("الأجرة = سعر الكيلو × المسافة من متجرك إلى الزبون.", 10, FontWeight.Medium, C.muted, lineHeight = 16)
        } else {
            FieldLabel("أجرة التوصيل الثابتة (﷼)")
            FinField(fixed, onFixed, "مثال: 15", keyboard = androidx.compose.ui.text.input.KeyboardType.Decimal)
            Spacer(Modifier.height(5.dp))
            T("أجرة ثابتة لكل طلب داخل نطاق خدمتك.", 10, FontWeight.Medium, C.muted, lineHeight = 16)
        }
    }
}

@Composable
private fun DModeChip(label: String, on: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(13.dp))
            .then(if (on) Modifier.background(Grad.green) else Modifier.background(Color(0xFFF2EFE9)))
            .border(1.dp, if (on) Color.Transparent else C.line, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick).padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) { T(label, 12, FontWeight.ExtraBold, if (on) Color.White else C.muted) }
}
