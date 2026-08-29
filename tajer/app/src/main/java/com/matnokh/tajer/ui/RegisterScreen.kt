package com.matnokh.tajer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.matnokh.tajer.R
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.CatLite
import com.matnokh.tajer.net.PlanDto
import com.matnokh.tajer.net.RegisterBody
import com.matnokh.tajer.net.errorMessage
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun RegisterScreen(onDone: (String) -> Unit, onBackToLogin: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var store by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var categories by remember { mutableStateOf<List<CatLite>>(emptyList()) }
    var catId by remember { mutableStateOf<Int?>(null) }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lng by remember { mutableStateOf<Double?>(null) }
    var managerPhone by remember { mutableStateOf("") }
    var licensePhoto by remember { mutableStateOf<String?>(null) }
    var commercialPhoto by remember { mutableStateOf<String?>(null) }
    var managerIdPhoto by remember { mutableStateOf<String?>(null) }
    var plans by remember { mutableStateOf<List<PlanDto>>(emptyList()) }
    var planId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) { runCatching { categories = Net.api.storeCategories().categories }; runCatching { plans = Net.api.plans().plans } }

    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF3F6F2), C.bg)))
            .verticalScroll(rememberScrollState()).safeDrawingPadding().padding(horizontal = 28.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HeaderSquare(R.drawable.ic_back, 42.dp, 14.dp, onBackToLogin)
            Spacer(Modifier.width(10.dp))
            T(tr("تسجيل متجر جديد", "Register a new store"), 18, FontWeight.ExtraBold, C.head)
        }
        Spacer(Modifier.height(18.dp))
        Box(Modifier.size(72.dp).clip(RoundedCornerShape(22.dp)).background(Grad.green), contentAlignment = Alignment.Center) {
            Ic(R.drawable.ic_shop, 38.dp, Color.White)
        }
        Spacer(Modifier.height(10.dp))
        T(tr("سجّل متجرك ويُعتمد من الإدارة", "Register your store, approved by admin"), 13, FontWeight.Medium, C.muted)
        Spacer(Modifier.height(18.dp))

        OCard(Modifier.fillMaxWidth()) {
            FieldLabel(tr("اسم المتجر", "Store name"), required = true)
            FinField(store, { store = it }, tr("مثال: أسواق السلام", "e.g. Al-Salam Markets"))
            FieldLabel(tr("اسم صاحب المتجر", "Store owner name"), required = true)
            FinField(owner, { owner = it }, tr("الاسم الكامل", "Full name"))
            FieldLabel(tr("رقم الهاتف", "Phone number"), required = true)
            FinField(phone, { phone = it }, "05xxxxxxxx", keyboard = KeyboardType.Phone)
            FieldLabel(tr("البريد الإلكتروني (اختياري)", "Email (optional)"))
            FinField(email, { email = it }, "you@example.com", keyboard = KeyboardType.Email)
            FieldLabel(tr("كلمة المرور", "Password"), required = true)
            FinField(password, { password = it }, tr("6 أحرف على الأقل", "At least 6 characters"), keyboard = KeyboardType.Password)
            FieldLabel(tr("هاتف المسؤول عن المتجر", "Store manager's phone"))
            FinField(managerPhone, { managerPhone = it }, tr("جوال المسؤول عن المتجر", "Store manager's phone"), keyboard = KeyboardType.Phone)
        }

        Spacer(Modifier.height(12.dp))
        OCard(Modifier.fillMaxWidth()) {
            FieldLabel(tr("التصنيف", "Category"), required = true)
            if (categories.isEmpty()) T(tr("جارٍ تحميل التصنيفات…", "Loading categories…"), 11, FontWeight.Medium, C.muted)
            else Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { c -> Chip(c.name, catId == c.id) { catId = c.id } }
            }
        }
        Spacer(Modifier.height(12.dp))
        StoreLocationCard(lat, lng, autoLocate = true) { la, ln -> lat = la; lng = ln }
        Spacer(Modifier.height(12.dp))
        OCard(Modifier.fillMaxWidth()) {
            OcTitle(R.drawable.ic_doc, tr("وثائق المتجر", "Store documents"))
            RegPhoto(tr("رخصة سارية للمتجر", "Valid store license"), licensePhoto, { licensePhoto = it }, toast)
            RegPhoto(tr("السجل التجاري", "Commercial register"), commercialPhoto, { commercialPhoto = it }, toast)
            RegPhoto(tr("صورة هوية المسؤول عن المتجر", "Store manager's ID photo"), managerIdPhoto, { managerIdPhoto = it }, toast)
        }
        Spacer(Modifier.height(12.dp))
        OCard(Modifier.fillMaxWidth()) {
            OcTitle(R.drawable.ic_card, tr("باقة الاشتراك", "Subscription package"), required = true)
            if (plans.isEmpty()) T(tr("جارٍ تحميل الباقات…", "Loading packages…"), 11, FontWeight.Medium, C.muted)
            else {
                val regularP = plans.filter { it.type != "marketing" }
                val marketingP = plans.filter { it.type == "marketing" }
                if (regularP.isNotEmpty()) { T(tr("الباقات العادية", "Regular packages"), 11, FontWeight.ExtraBold, C.muted, Modifier.padding(vertical = 4.dp)); regularP.forEach { pp -> RegPlanRow(pp, planId == pp.id) { planId = pp.id } } }
                if (marketingP.isNotEmpty()) { T(tr("الباقات التسويقية 📣", "Marketing packages 📣"), 11, FontWeight.ExtraBold, C.muted, Modifier.padding(top = 8.dp, bottom = 4.dp)); marketingP.forEach { pp -> RegPlanRow(pp, planId == pp.id) { planId = pp.id } } }
            }
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth()) {
            WideButton(if (loading) "…" else tr("إنشاء المتجر", "Create store"), if (loading) null else R.drawable.ic_check) {
                if (loading) return@WideButton
                if (store.isBlank() || owner.isBlank() || phone.isBlank() || password.length < 6) {
                    toast(tr("أكمل الحقول المطلوبة (كلمة المرور 6 أحرف فأكثر)", "Complete the required fields (password 6+ characters)")); return@WideButton
                }
                if (catId == null) { toast(tr("اختر تصنيف المتجر", "Choose the store category")); return@WideButton }
                if (licensePhoto == null || commercialPhoto == null || managerIdPhoto == null) { toast(tr("ارفع وثائق المتجر الثلاث: رخصة سارية، سجل تجاري، هوية المسؤول", "Upload the store's three documents: valid license, commercial register, manager ID")); return@WideButton }
                if (planId == null) { toast(tr("اختر باقة الاشتراك", "Choose a subscription package")); return@WideButton }
                scope.launch {
                    loading = true
                    try {
                        val r = Net.api.register(RegisterBody(store.trim(), owner.trim(), phone.trim(), email.trim().ifBlank { null }, password, catId, lat, lng, license_photo = licensePhoto, commercial_register_photo = commercialPhoto, manager_phone = managerPhone.trim().ifBlank { null }, manager_id_photo = managerIdPhoto, subscription_plan_id = planId))
                        onDone(r.message ?: tr("تم إنشاء متجرك — بانتظار اعتماد الإدارة", "Your store was created — awaiting admin approval"))
                    } catch (e: retrofit2.HttpException) {
                        toast(errorMessage(e) ?: tr("تعذّر التسجيل", "Registration failed"))
                    } catch (e: Exception) {
                        toast(tr("تعذّر الاتصال بالخادم", "Couldn't reach the server"))
                    } finally { loading = false }
                }
            }
            if (loading) CircularProgressIndicator(Modifier.align(Alignment.CenterEnd).padding(end = 20.dp).size(20.dp), color = Color.White, strokeWidth = 2.dp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@androidx.compose.runtime.Composable
private fun RegPhoto(label: String, url: String?, onUploaded: (String) -> Unit, toast: (String) -> Unit) {
    val ctx = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var busy by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            try {
                val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val part = MultipartBody.Part.createFormData("file", "doc.jpg", bytes.toRequestBody("image/*".toMediaTypeOrNull()))
                    val up = Net.api.registerUpload(part)
                    onUploaded(up.url); toast(tr("تم رفع ", "Uploaded ") + label + " ✓")
                }
            } catch (e: Exception) { toast(tr("تعذّر رفع ", "Couldn't upload ") + label) }
            busy = false
        }
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF1EEE8)), contentAlignment = Alignment.Center) {
            if (busy) CircularProgressIndicator(color = C.green, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            else if (url != null) AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Ic(R.drawable.ic_doc, 22.dp, C.muted)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { T(label, 12, FontWeight.Bold, C.head); T(if (url != null) tr("تم الرفع ✓", "Uploaded ✓") else tr("مطلوب", "Required"), 10, FontWeight.Normal, if (url != null) C.muted else C.greenD) }
        Box(Modifier.clip(RoundedCornerShape(11.dp)).background(Color(0xFFEEF4EF)).clickable(enabled = !busy) { picker.launch("image/*") }.padding(horizontal = 14.dp, vertical = 9.dp)) { T(if (url != null) tr("تغيير", "Change") else tr("رفع", "Upload"), 11, FontWeight.ExtraBold, C.green) }
    }
}

@androidx.compose.runtime.Composable
private fun RegPlanRow(p: com.matnokh.tajer.net.PlanDto, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(14.dp))
            .background(if (selected) C.pillLive else C.card)
            .border(1.5.dp, if (selected) C.green else C.line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(18.dp).clip(androidx.compose.foundation.shape.CircleShape).border(2.dp, if (selected) C.green else C.line, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
            if (selected) Box(Modifier.size(9.dp).clip(androidx.compose.foundation.shape.CircleShape).background(C.green))
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            T(p.name, 13, FontWeight.Bold, C.head, maxLines = 1)
            T(if (p.duration_days % 30 == 0 && p.duration_days > 0) tr("${p.duration_days / 30} شهر", "${p.duration_days / 30} months") else tr("${p.duration_days} يوم", "${p.duration_days} days"), 10, FontWeight.Medium, C.muted)
        }
        T(if (p.price <= 0.0) tr("مجاناً", "Free") else "·﷼${money(p.price)}", 14, FontWeight.ExtraBold, C.greenD)
    }
}
