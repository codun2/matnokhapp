@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.matnokh.driver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.matnokh.driver.R
import com.matnokh.driver.net.Net
import com.matnokh.driver.net.RegisterBody
import com.matnokh.driver.net.DrvPackage
import com.matnokh.driver.net.SvcLite
import com.matnokh.driver.net.DocType
import com.matnokh.driver.net.DocItem
import com.matnokh.driver.net.call
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.launch

private val VEHICLES = listOf("small" to tr("مركبة صغيرة", "Small vehicle"), "medium" to tr("مركبة متوسطة", "Medium vehicle"), "large" to tr("مركبة نقل", "Transport vehicle"))
val STORE_SERVICES = listOf("errand" to tr("متاجر قريبة", "Nearby stores"), "store" to tr("متاجر مطنوخ", "Matnokh stores"))
private fun pickSvc(sel: androidx.compose.runtime.snapshots.SnapshotStateList<String>, key: String, isStore: Boolean) {
    if (key in sel) sel.remove(key) else sel.add(key)
}
val DRIVER_SERVICES = listOf(
    "fast" to tr("توصيل سريع", "Fast delivery"), "furniture" to tr("نقل أثاث", "Furniture moving"), "cold" to tr("نقل مبرّد", "Refrigerated transport"),
    "heavy" to tr("نقل ثقيل", "Heavy transport"), "water" to tr("توصيل مياه", "Water delivery"), "errand" to tr("مشاوير وتسوّق", "Errands & shopping"), "transport" to tr("نقل عام", "General transport"),
)

@Composable
fun RegisterScreen(onDone: () -> Unit, onBack: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var vehicle by remember { mutableStateOf("small") }
    var nationalId by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    val pkgIds = remember { mutableStateListOf<Int>() }
    var packages by remember { mutableStateOf<List<DrvPackage>>(emptyList()) }
    var vMakes by remember { mutableStateOf<List<com.matnokh.driver.net.VMake>>(emptyList()) }
    var make by remember { mutableStateOf<String?>(null) }
    var model by remember { mutableStateOf<String?>(null) }
    var year by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(vehicle) { pkgIds.clear(); runCatching { packages = Net.api.driverPackages(vehicle).packages } }
    LaunchedEffect(Unit) { runCatching { vMakes = Net.api.vehicleMakes().makes } }
    var docTypes by remember { mutableStateOf<List<DocType>>(emptyList()) }
    val docValues = remember { mutableStateMapOf<Int, String>() }
    LaunchedEffect(Unit) { runCatching { docTypes = Net.api.docTypes().data } }
    var loading by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    var vehiclePhoto by remember { mutableStateOf<String?>(null) }
    var uploadingPhoto by remember { mutableStateOf(false) }
    var licensePhoto by remember { mutableStateOf<String?>(null) }
    var idPhoto by remember { mutableStateOf<String?>(null) }
    var passportPhoto by remember { mutableStateOf<String?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) scope.launch {
            uploadingPhoto = true
            try {
                val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val part = MultipartBody.Part.createFormData("file", "vehicle.jpg", bytes.toRequestBody("image/*".toMediaTypeOrNull()))
                    val up = Net.api.registerUpload(part)
                    if (up.url != null) { vehiclePhoto = up.url; toast(tr("تم رفع صورة السيارة ✓", "Vehicle photo uploaded ✓")) } else toast(tr("فشل الرفع", "Upload failed"))
                }
            } catch (e: Exception) { toast(tr("تعذّر رفع الصورة", "Couldn't upload the image")) }
            uploadingPhoto = false
        }
    }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("التسجيل كسائق جديد", "Register as a new driver"), onBack, {})
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 6.dp).fillMaxWidth()) {
                FieldLabel(tr("الاسم الكامل", "Full name"), required = true); FinField(name, { name = it }, tr("مثال: أبو أحمد النجار", "e.g. Abu Ahmad Al-Najjar"))
                Spacer(Modifier.height(10.dp)); FieldLabel(tr("رقم الجوال", "Phone number"), required = true); FinField(phone, { phone = it }, "05xxxxxxxx", keyboard = KeyboardType.Phone)
                Spacer(Modifier.height(10.dp)); FieldLabel(tr("كلمة المرور", "Password"), required = true); FinField(password, { password = it }, tr("٦ أحرف على الأقل", "At least 6 characters"), keyboard = KeyboardType.Password)
                Spacer(Modifier.height(10.dp)); FieldLabel(tr("لوحة المركبة", "Vehicle plate")); FinField(plate, { plate = it }, tr("مثال: أ ب ج 1234", "e.g. ABC 1234"))
                Spacer(Modifier.height(10.dp)); FieldLabel(tr("رقم الهوية / الإقامة", "ID / residence number")); FinField(nationalId, { nationalId = it }, tr("رقم الهوية أو الإقامة", "ID or residence number"))
                Spacer(Modifier.height(10.dp)); FieldLabel(tr("رقم رخصة القيادة", "Driver's license number")); FinField(licenseNumber, { licenseNumber = it }, tr("رقم الرخصة", "License number"))
            }
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 12.dp).fillMaxWidth()) {
                FieldLabel(tr("نوع المركبة", "Vehicle type"), required = true)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    VEHICLES.forEach { (k, label) -> Chip(label, vehicle == k) { vehicle = k } }
                }
            }
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 12.dp).fillMaxWidth()) {
                FieldLabel(tr("شركة السيارة", "Vehicle make"))
                if (vMakes.isEmpty()) T(tr("جارٍ التحميل…", "Loading…"), 10, FontWeight.Medium, C.muted)
                else FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    vMakes.forEach { mk -> Chip(mk.name, make == mk.name) { make = mk.name; model = null } }
                }
                val curModels = vMakes.firstOrNull { it.name == make }?.models ?: emptyList()
                if (make != null && curModels.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp)); FieldLabel(tr("نوع السيارة", "Car type"))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        curModels.forEach { md -> Chip(md.name, model == md.name) { model = md.name } }
                    }
                }
                Spacer(Modifier.height(12.dp)); FieldLabel(tr("الموديل (سنة الصنع)", "Model (year)"))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val cur = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    ((cur + 1) downTo 1990).forEach { y -> Chip(y.toString(), year == y.toString()) { year = y.toString() } }
                }
            }
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 12.dp).fillMaxWidth()) {
                FieldLabel(tr("صورة السيارة", "Vehicle photo"))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(76.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF1EEE8)), contentAlignment = Alignment.Center) {
                        if (uploadingPhoto) CircularProgressIndicator(color = C.green, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        else if (vehiclePhoto != null) AsyncImage(model = vehiclePhoto, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        else Text("🚗", fontSize = 30.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF4EF)).clickable(enabled = !uploadingPhoto) { photoPicker.launch("image/*") }.padding(horizontal = 16.dp, vertical = 11.dp)) {
                        T(if (uploadingPhoto) tr("جارٍ الرفع…", "Uploading…") else if (vehiclePhoto != null) tr("تغيير صورة السيارة", "Change vehicle photo") else tr("رفع صورة السيارة", "Upload vehicle photo"), 12, FontWeight.ExtraBold, C.green)
                    }
                }
            }
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 12.dp).fillMaxWidth()) {
                FieldLabel(tr("المستندات المطلوبة", "Required documents"), required = true)
                if (docTypes.isEmpty()) T(tr("جارٍ تحميل الوثائق…", "Loading documents…"), 10, FontWeight.Medium, C.muted)
                else docTypes.filter { !it.transport_only || vehicle == "large" }.forEach { dt -> DocUpload(dt.name, dt.required, { docValues[dt.id] = it }, toast) }
            }
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 12.dp).fillMaxWidth()) {
                OcTitle(R.drawable.ic_zap, tr("باقات الاشتراك", "Subscription packages"), required = true)
                T(tr("اختر الباقات المتاحة لحجم مركبتك — كل باقة = خدمة تقدّمها. (تُدفَع أو تُفعَّل بعد اعتماد حسابك)", "Choose the packages available for your vehicle size — each package = a service you offer. (Paid or activated after your account is approved)"), 10, androidx.compose.ui.text.font.FontWeight.Medium, C.muted, lineHeight = 16)
                Spacer(Modifier.height(10.dp))
                if (packages.isEmpty()) T(tr("لا توجد باقات متاحة لهذا الحجم حالياً.", "No packages available for this size currently."), 10, androidx.compose.ui.text.font.FontWeight.Medium, C.muted)
                else packages.forEach { p ->
                    val sel = p.id in pkgIds
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(14.dp)).background(C.card).clickable { if (sel) pkgIds.remove(p.id) else pkgIds.add(p.id) }.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        T(if (sel) "✓" else "○", 16, FontWeight.Bold, if (sel) C.green else C.muted)
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) { T(trd(p.name, p.name_en), 13, FontWeight.Bold, C.head, maxLines = 1); T((p.service?.let { trd(it, p.service_en) } ?: "") + " · " + (if (p.duration_days % 30 == 0 && p.duration_days > 0) tr("${p.duration_days / 30} شهر", "${p.duration_days / 30} months") else tr("${p.duration_days} يوم", "${p.duration_days} days")), 10, FontWeight.Medium, C.muted) }
                        T(if (p.price <= 0.0) tr("مجاناً", "Free") else "$RY${p.price.toInt()}", 14, FontWeight.ExtraBold, C.greenD)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            WideButton(if (loading) tr("جارٍ الإرسال…", "Sending…") else tr("إرسال طلب التسجيل", "Send registration request"), R.drawable.ic_check, modifier = Modifier.padding(horizontal = 22.dp)) {
                if (loading) return@WideButton
                if (name.isBlank() || phone.isBlank() || password.length < 6) { toast(tr("أكمل الاسم والجوال وكلمة مرور ٦ أحرف", "Complete your name, phone, and a 6-char password")); return@WideButton }
                if (pkgIds.isEmpty()) { toast(tr("اختر باقة واحدة على الأقل", "Choose at least one package")); return@WideButton }
                if (docTypes.any { it.required && (!it.transport_only || vehicle == "large") && docValues[it.id].isNullOrBlank() }) { toast(tr("ارفع جميع الوثائق المطلوبة قبل الإرسال", "Upload all required documents before submitting")); return@WideButton }
                loading = true
                scope.launch {
                    val r = call({ Net.api.register(RegisterBody(name.trim(), phone.trim(), null, password, vehicle, plate.ifBlank { null }, null, vehiclePhoto, national_id = nationalId.ifBlank { null }, license_number = licenseNumber.ifBlank { null }, vehicle_make = make, vehicle_model = model, vehicle_year = year, documents = docValues.map { DocItem(it.key, it.value) }, package_ids = pkgIds.toList())) }, toast)
                    loading = false
                    if (r != null) { toast(r.message ?: tr("تم إرسال طلبك — بانتظار اعتماد الإدارة", "Your request was sent — awaiting admin approval")); onDone() }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun DocUpload(label: String, required: Boolean, onUploaded: (String) -> Unit, toast: (String) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var url by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) scope.launch {
            busy = true
            try {
                val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) { val part = MultipartBody.Part.createFormData("file", "doc.jpg", bytes.toRequestBody("image/*".toMediaTypeOrNull())); val up = Net.api.registerUpload(part); if (up.url != null) { url = up.url; onUploaded(up.url); toast(tr("تم رفع $label ✓", "$label uploaded ✓")) } else toast(tr("فشل الرفع", "Upload failed")) }
            } catch (e: Exception) { toast(tr("تعذّر الرفع", "Upload failed")) }
            busy = false
        }
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFFF1EEE8)), contentAlignment = Alignment.Center) {
            if (busy) CircularProgressIndicator(color = C.green, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
            else if (url != null) AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Ic(R.drawable.ic_doc, 22.dp, C.muted)
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) { T(label, 12, FontWeight.Bold, C.head); T(if (url != null) tr("تم الرفع ✓", "Uploaded ✓") else if (required) tr("مطلوب", "Required") else tr("اختياري", "Optional"), 10, FontWeight.Normal, C.muted) }
        Box(Modifier.clip(RoundedCornerShape(11.dp)).background(Color(0xFFEEF4EF)).clickable(enabled = !busy) { picker.launch("image/*") }.padding(horizontal = 13.dp, vertical = 8.dp)) { T(if (url != null) tr("تغيير", "Change") else tr("رفع", "Upload"), 11, FontWeight.ExtraBold, C.green) }
    }
}
