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

private val VEHICLES = listOf("small" to "مركبة صغيرة", "medium" to "مركبة متوسطة", "large" to "مركبة نقل")
val STORE_SERVICES = listOf("errand" to "متاجر قريبة", "store" to "متاجر مطنوخ")
private fun pickSvc(sel: androidx.compose.runtime.snapshots.SnapshotStateList<String>, key: String, isStore: Boolean) {
    if (key in sel) sel.remove(key) else sel.add(key)
}
val DRIVER_SERVICES = listOf(
    "fast" to "توصيل سريع", "furniture" to "نقل أثاث", "cold" to "نقل مبرّد",
    "heavy" to "نقل ثقيل", "water" to "توصيل مياه", "errand" to "مشاوير وتسوّق", "transport" to "نقل عام",
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
                    if (up.url != null) { vehiclePhoto = up.url; toast("تم رفع صورة السيارة ✓") } else toast("فشل الرفع")
                }
            } catch (e: Exception) { toast("تعذّر رفع الصورة") }
            uploadingPhoto = false
        }
    }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("التسجيل كسائق جديد", onBack, {})
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 6.dp).fillMaxWidth()) {
                FieldLabel("الاسم الكامل", required = true); FinField(name, { name = it }, "مثال: أبو أحمد النجار")
                Spacer(Modifier.height(10.dp)); FieldLabel("رقم الجوال", required = true); FinField(phone, { phone = it }, "05xxxxxxxx", keyboard = KeyboardType.Phone)
                Spacer(Modifier.height(10.dp)); FieldLabel("كلمة المرور", required = true); FinField(password, { password = it }, "٦ أحرف على الأقل", keyboard = KeyboardType.Password)
                Spacer(Modifier.height(10.dp)); FieldLabel("لوحة المركبة"); FinField(plate, { plate = it }, "مثال: أ ب ج 1234")
                Spacer(Modifier.height(10.dp)); FieldLabel("رقم الهوية / الإقامة"); FinField(nationalId, { nationalId = it }, "رقم الهوية أو الإقامة")
                Spacer(Modifier.height(10.dp)); FieldLabel("رقم رخصة القيادة"); FinField(licenseNumber, { licenseNumber = it }, "رقم الرخصة")
            }
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 12.dp).fillMaxWidth()) {
                FieldLabel("نوع المركبة", required = true)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    VEHICLES.forEach { (k, label) -> Chip(label, vehicle == k) { vehicle = k } }
                }
            }
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 12.dp).fillMaxWidth()) {
                FieldLabel("شركة السيارة")
                if (vMakes.isEmpty()) T("جارٍ التحميل…", 10, FontWeight.Medium, C.muted)
                else FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    vMakes.forEach { mk -> Chip(mk.name, make == mk.name) { make = mk.name; model = null } }
                }
                val curModels = vMakes.firstOrNull { it.name == make }?.models ?: emptyList()
                if (make != null && curModels.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp)); FieldLabel("نوع السيارة")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        curModels.forEach { md -> Chip(md.name, model == md.name) { model = md.name } }
                    }
                }
                Spacer(Modifier.height(12.dp)); FieldLabel("الموديل (سنة الصنع)")
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val cur = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    ((cur + 1) downTo 1990).forEach { y -> Chip(y.toString(), year == y.toString()) { year = y.toString() } }
                }
            }
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 12.dp).fillMaxWidth()) {
                FieldLabel("صورة السيارة")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(76.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF1EEE8)), contentAlignment = Alignment.Center) {
                        if (uploadingPhoto) CircularProgressIndicator(color = C.green, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        else if (vehiclePhoto != null) AsyncImage(model = vehiclePhoto, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        else Text("🚗", fontSize = 30.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF4EF)).clickable(enabled = !uploadingPhoto) { photoPicker.launch("image/*") }.padding(horizontal = 16.dp, vertical = 11.dp)) {
                        T(if (uploadingPhoto) "جارٍ الرفع…" else if (vehiclePhoto != null) "تغيير صورة السيارة" else "رفع صورة السيارة", 12, FontWeight.ExtraBold, C.green)
                    }
                }
            }
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 12.dp).fillMaxWidth()) {
                FieldLabel("المستندات المطلوبة", required = true)
                if (docTypes.isEmpty()) T("جارٍ تحميل الوثائق…", 10, FontWeight.Medium, C.muted)
                else docTypes.filter { !it.transport_only || vehicle == "large" }.forEach { dt -> DocUpload(dt.name, dt.required, { docValues[dt.id] = it }, toast) }
            }
            OCard(Modifier.padding(horizontal = 22.dp).padding(top = 12.dp).fillMaxWidth()) {
                OcTitle(R.drawable.ic_zap, "باقات الاشتراك", required = true)
                T("اختر الباقات المتاحة لحجم مركبتك — كل باقة = خدمة تقدّمها. (تُدفَع أو تُفعَّل بعد اعتماد حسابك)", 10, androidx.compose.ui.text.font.FontWeight.Medium, C.muted, lineHeight = 16)
                Spacer(Modifier.height(10.dp))
                if (packages.isEmpty()) T("لا توجد باقات متاحة لهذا الحجم حالياً.", 10, androidx.compose.ui.text.font.FontWeight.Medium, C.muted)
                else packages.forEach { p ->
                    val sel = p.id in pkgIds
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(14.dp)).background(C.card).clickable { if (sel) pkgIds.remove(p.id) else pkgIds.add(p.id) }.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        T(if (sel) "✓" else "○", 16, FontWeight.Bold, if (sel) C.green else C.muted)
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) { T(p.name, 13, FontWeight.Bold, C.head, maxLines = 1); T((p.service ?: "") + " · " + (if (p.duration_days % 30 == 0 && p.duration_days > 0) "${p.duration_days / 30} شهر" else "${p.duration_days} يوم"), 10, FontWeight.Medium, C.muted) }
                        T(if (p.price <= 0.0) "مجاناً" else "﷼${p.price.toInt()}", 14, FontWeight.ExtraBold, C.greenD)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            WideButton(if (loading) "جارٍ الإرسال…" else "إرسال طلب التسجيل", R.drawable.ic_check, modifier = Modifier.padding(horizontal = 22.dp)) {
                if (loading) return@WideButton
                if (name.isBlank() || phone.isBlank() || password.length < 6) { toast("أكمل الاسم والجوال وكلمة مرور ٦ أحرف"); return@WideButton }
                if (pkgIds.isEmpty()) { toast("اختر باقة واحدة على الأقل"); return@WideButton }
                if (docTypes.any { it.required && (!it.transport_only || vehicle == "large") && docValues[it.id].isNullOrBlank() }) { toast("ارفع جميع الوثائق المطلوبة قبل الإرسال"); return@WideButton }
                loading = true
                scope.launch {
                    val r = call({ Net.api.register(RegisterBody(name.trim(), phone.trim(), null, password, vehicle, plate.ifBlank { null }, null, vehiclePhoto, national_id = nationalId.ifBlank { null }, license_number = licenseNumber.ifBlank { null }, vehicle_make = make, vehicle_model = model, vehicle_year = year, documents = docValues.map { DocItem(it.key, it.value) }, package_ids = pkgIds.toList())) }, toast)
                    loading = false
                    if (r != null) { toast(r.message ?: "تم إرسال طلبك — بانتظار اعتماد الإدارة"); onDone() }
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
                if (bytes != null) { val part = MultipartBody.Part.createFormData("file", "doc.jpg", bytes.toRequestBody("image/*".toMediaTypeOrNull())); val up = Net.api.registerUpload(part); if (up.url != null) { url = up.url; onUploaded(up.url); toast("تم رفع $label ✓") } else toast("فشل الرفع") }
            } catch (e: Exception) { toast("تعذّر الرفع") }
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
        Column(Modifier.weight(1f)) { T(label, 12, FontWeight.Bold, C.head); T(if (url != null) "تم الرفع ✓" else if (required) "مطلوب" else "اختياري", 10, FontWeight.Normal, C.muted) }
        Box(Modifier.clip(RoundedCornerShape(11.dp)).background(Color(0xFFEEF4EF)).clickable(enabled = !busy) { picker.launch("image/*") }.padding(horizontal = 13.dp, vertical = 8.dp)) { T(if (url != null) "تغيير" else "رفع", 11, FontWeight.ExtraBold, C.green) }
    }
}
