@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.matnokh.tajer.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.matnokh.tajer.R
import com.matnokh.tajer.net.BranchDto
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.PAddon
import com.matnokh.tajer.net.ProductBody
import com.matnokh.tajer.net.SectionDto
import com.matnokh.tajer.net.call
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.math.roundToInt

@Composable
fun NewProductScreen(productId: Int?, onBack: () -> Unit, onMenu: () -> Unit, onNewSection: () -> Unit, toast: (String) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val editing = productId != null

    var sections by remember { mutableStateOf<List<SectionDto>>(emptyList()) }
    var branches by remember { mutableStateOf<List<BranchDto>>(emptyList()) }
    val images = remember { mutableStateListOf<String>() }
    var name by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var secId by remember { mutableStateOf<Int?>(null) }
    var desc by remember { mutableStateOf("") }
    var descEn by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var offer by remember { mutableStateOf(false) }
    var oldPrice by remember { mutableStateOf("") }
    var addonsOn by remember { mutableStateOf(false) }
    val addons = remember { mutableStateListOf<PAddon>() }
    var status by remember { mutableStateOf("active") }
    val stock = remember { mutableStateMapOf<Int, String>() } // branchId -> qty
    var uploading by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        call({ Net.api.sections() }, toast)?.let { sections = it.sections; if (secId == null) secId = it.sections.firstOrNull()?.id }
        call({ Net.api.branches() }, toast)?.let { branches = it.branches; it.branches.forEach { b -> if (stock[b.id] == null) stock[b.id] = "0" } }
        if (editing) call({ Net.api.product(productId!!) }, toast)?.let { r ->
            val p = r.product
            name = p.name; desc = p.description ?: ""; nameEn = p.name_en ?: ""; descEn = p.description_en ?: ""; secId = p.section_id; price = money(p.price)
            if (p.price_before > 0) { offer = true; oldPrice = money(p.price_before) }
            status = p.status; images.clear(); images.addAll(p.images)
            if (p.addons.isNotEmpty()) { addonsOn = true; addons.clear(); addons.addAll(p.addons) }
            p.stock.forEach { stock[it.branch_id] = it.in_stock.toString() }
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        uploading = true
        scope.launch {
            try {
                val bytes = ctx.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
                val part = MultipartBody.Part.createFormData("file", "img.jpg", bytes.toRequestBody("image/*".toMediaTypeOrNull()))
                images.add(Net.api.upload(part).url)
            } catch (e: Exception) { toast(tr("تعذّر رفع الصورة", "Couldn't upload the image")) } finally { uploading = false }
        }
    }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(if (editing) tr("تعديل منتج", "Edit product") else tr("منتج جديد", "New product"), onBack, onMenu)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                    OcTitle(R.drawable.ic_img, tr("صور المنتج", "Product images"), required = true)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        images.forEachIndexed { i, url ->
                            Box(Modifier.size(72.dp)) {
                                AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)))
                                Box(Modifier.align(Alignment.TopStart).offset(x = (-6).dp, y = (-6).dp).size(20.dp).clip(CircleShape).background(C.redBg).clickable { images.removeAt(i) }, contentAlignment = Alignment.Center) { T("×", 13, FontWeight.Black, C.redText) }
                                if (i == 0) Box(Modifier.align(Alignment.BottomCenter).offset(y = 7.dp).clip(CircleShape).background(C.green).padding(horizontal = 7.dp, vertical = 2.dp)) { T(tr("رئيسية", "Home"), 8, FontWeight.ExtraBold, Color.White) }
                            }
                        }
                        if (images.size < 4) Box(Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFFAF8F4)).border(1.5.dp, C.line, RoundedCornerShape(16.dp)).clickable(enabled = !uploading) { picker.launch("image/*") }, contentAlignment = Alignment.Center) {
                            if (uploading) Text("…", fontSize = 22.sp, color = C.muted) else Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("+", fontSize = 24.sp, color = Color(0xFFC3C9C0)); T(tr("صورة", "Image"), 9, FontWeight.ExtraBold, Color(0xFFC3C9C0)) }
                        }
                    }
                    Spacer(Modifier.height(8.dp)); T(tr("أول صورة هي الرئيسية. حتى 4 صور.", "The first image is the main one. Up to 4 images."), 10, FontWeight.Medium, C.muted)
                }
            }
            item {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                    OcTitle(R.drawable.ic_box, tr("بيانات المنتج", "Product details"))
                    FieldLabel(tr("اسم المنتج", "Product name"), required = true); FinField(name, { name = it }, tr("مثال: كبسة لحم — طبق كبير", "e.g. Meat Kabsa — large plate"))
                    Spacer(Modifier.height(10.dp)); FieldLabel(tr("الاسم بالإنجليزية (English)", "Name in English")); androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) { FinField(nameEn, { nameEn = it }, "e.g. Meat Kabsa — large", align = androidx.compose.ui.text.style.TextAlign.Left) }
                    FieldLabel(tr("القسم داخل المتجر", "Section within the store"), required = true)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        sections.forEach { s -> Chip("${s.icon ?: ""} ${s.name}", secId == s.id) { secId = s.id } }
                        Chip(tr("+ قسم جديد", "+ New section"), false, onClick = onNewSection)
                    }
                    FieldLabel(tr("الوصف", "Description")); FinField(desc, { desc = it }, tr("وصف مختصر يظهر للزبون", "A short description shown to the customer"), singleLine = false, minHeight = 64.dp)
                    Spacer(Modifier.height(10.dp)); FieldLabel(tr("الوصف بالإنجليزية (English)", "Description in English")); androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) { FinField(descEn, { descEn = it }, "Short description shown to the customer", singleLine = false, minHeight = 64.dp, align = androidx.compose.ui.text.style.TextAlign.Left) }
                }
            }
            item {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                    OcTitle(R.drawable.ic_cash, tr("السعر والعرض", "Price & offer"))
                    FieldLabel(tr("السعر ($RY)", "Price ($RY)"), required = true); FinField(price, { price = decInput(it) }, "0", keyboard = KeyboardType.Decimal)
                    SwRow(tr("هل على المنتج عرض؟", "Does the product have an offer?"), tr("يظهر للزبون بشارة خصم وسعر مشطوب", "Shows the customer a discount badge and a struck-through price"), offer) { offer = !offer }
                    if (offer) {
                        FieldLabel(tr("السعر قبل الخصم ($RY)", "Price before discount ($RY)")); FinField(oldPrice, { oldPrice = decInput(it) }, "0", keyboard = KeyboardType.Decimal)
                        Spacer(Modifier.height(11.dp)); CalcBox(price.toDoubleOrNull() ?: 0.0, oldPrice.toDoubleOrNull() ?: 0.0)
                    }
                }
            }
            item {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                    SwRow(tr("إضافات على المنتج", "Product add-ons"), tr("مثل: جبن · صوص — يختارها الزبون ويُضاف سعرها", "e.g. cheese · sauce — the customer selects them and their price is added"), addonsOn, topBorder = false) { addonsOn = !addonsOn; if (addonsOn && addons.isEmpty()) addons.add(PAddon("", 0.0)); if (!addonsOn) addons.clear() }
                    if (addonsOn) {
                        Spacer(Modifier.height(4.dp))
                        addons.forEachIndexed { i, a ->
                            Row(Modifier.fillMaxWidth().padding(bottom = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.weight(1f)) { FinField(a.name, { addons[i] = a.copy(name = it) }, tr("اسم الإضافة", "Add-on name")) }
                                Box(Modifier.width(74.dp)) { FinField(if (a.price == 0.0) "" else money(a.price), { addons[i] = a.copy(price = decInput(it).toDoubleOrNull() ?: 0.0) }, "$RY", keyboard = KeyboardType.Decimal, align = androidx.compose.ui.text.style.TextAlign.Center) }
                                Box(Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(C.redBg).clickable { addons.removeAt(i) }, contentAlignment = Alignment.Center) { T("×", 15, FontWeight.Black, C.redText) }
                            }
                        }
                        if (addons.size < 6) Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF2F8F3)).border(1.5.dp, Color(0xFFCFE0D4), RoundedCornerShape(14.dp)).clickable { addons.add(PAddon("", 0.0)) }.padding(vertical = 11.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { Ic(R.drawable.ic_plus, 16.dp, C.greenD); Spacer(Modifier.width(7.dp)); T(tr("إضافة صنف إضافي", "Add an extra item"), 12, FontWeight.ExtraBold, C.greenD) }
                    }
                }
            }
            item {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                    OcTitle(R.drawable.ic_pin, tr("الكميات حسب الفرع", "Quantities by branch"))
                    branches.forEachIndexed { i, b ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Ic(R.drawable.ic_pin, 15.dp, C.green); Spacer(Modifier.width(8.dp))
                            T(b.name, 12, FontWeight.Bold, C.head, Modifier.weight(1f))
                            Box(Modifier.width(78.dp)) {
                                val cur = stock[b.id] ?: "0"
                                FinField(if (cur == "0") "" else cur, { v ->
                                    val d = v.filter { c -> c.isDigit() }.trimStart('0'); stock[b.id] = if (d.isEmpty()) "0" else d
                                }, placeholder = "0", keyboard = KeyboardType.Number, align = androidx.compose.ui.text.style.TextAlign.Center, minHeight = 40.dp)
                            }
                        }
                        if (i < branches.lastIndex) ProdLine()
                    }
                    if (branches.isEmpty()) T(tr("أضف فرعاً أولاً لإدارة الكميات", "Add a branch first to manage quantities"), 11, FontWeight.Medium, C.muted)
                }
            }
            item {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                    OcTitle(R.drawable.ic_check, tr("حالة النشر", "Publish status"))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Chip(tr("✓ نشر مباشرة", "✓ Publish directly"), status == "active") { status = "active" }
                        Chip(tr("◷ حفظ كمسودة", "◷ Save as draft"), status == "draft") { status = "draft" }
                        Chip(tr("🗄 أرشفة", "🗄 Archive"), status == "archived") { status = "archived" }
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 22.dp)) {
                    WideButton(if (saving) "…" else tr("حفظ المنتج", "Save product"), R.drawable.ic_check) {
                        if (saving) return@WideButton
                        val nm = name.trim()
                        if (nm.isEmpty()) { toast(tr("اكتب اسم المنتج أولاً", "Enter the product name first")); return@WideButton }
                        if (images.isEmpty()) { toast(tr("أضف صورة واحدة على الأقل", "Add at least one image")); return@WideButton }
                        val pr = price.toDoubleOrNull() ?: 0.0
                        if (pr <= 0.0) { toast(tr("أدخل سعر المنتج", "Enter the product price")); return@WideButton }
                        val o = if (offer) (oldPrice.toDoubleOrNull() ?: 0.0) else 0.0
                        val body = ProductBody(nm, desc.ifBlank { null }, secId, pr, if (o > pr) o else null, status,
                            images.toList(), addons.filter { it.name.isNotBlank() }, stock.mapKeys { it.key.toString() }.mapValues { it.value.toIntOrNull() ?: 0 }, nameEn.ifBlank { null }, descEn.ifBlank { null })
                        scope.launch {
                            saving = true
                            val r = call({ if (editing) Net.api.updateProduct(productId!!, body) else Net.api.createProduct(body) }, toast)
                            saving = false
                            if (r != null) { toast(r.message ?: tr("تم الحفظ", "Saved")); onBack() }
                        }
                    }
                    Spacer(Modifier.height(9.dp)); WideButton(tr("إلغاء", "Cancel"), ghost = true, onClick = onBack)
                    if (editing) {
                        Spacer(Modifier.height(9.dp))
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(C.redBg).clickable { confirmDelete = true }.padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Ic(R.drawable.ic_x, 16.dp, C.redText); Spacer(Modifier.width(8.dp)); T(tr("حذف المنتج", "Delete product"), 14, FontWeight.ExtraBold, C.redText)
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) androidx.compose.material3.AlertDialog(
        onDismissRequest = { confirmDelete = false },
        containerColor = C.bg,
        title = { T(tr("حذف المنتج", "Delete product"), 15, FontWeight.Black, C.head) },
        text = { T(tr("هل أنت متأكد من حذف «$name»؟ لا يمكن التراجع.", "Are you sure you want to delete «$name»? This can't be undone."), 13, FontWeight.Medium, C.muted, lineHeight = 20) },
        confirmButton = {
            Text(tr("حذف", "Delete"), fontFamily = Cairo, fontWeight = FontWeight.ExtraBold, color = C.redText,
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
                    confirmDelete = false
                    scope.launch { call({ Net.api.deleteProduct(productId!!) }, toast)?.let { toast(it.message ?: tr("حُذف المنتج", "Product deleted")); onBack() } }
                }.padding(horizontal = 14.dp, vertical = 8.dp))
        },
        dismissButton = {
            Text(tr("إلغاء", "Cancel"), fontFamily = Cairo, fontWeight = FontWeight.Bold, color = C.muted,
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { confirmDelete = false }.padding(horizontal = 14.dp, vertical = 8.dp))
        },
    )
}

@Composable
fun SwRow(title: String, sub: String, on: Boolean, topBorder: Boolean = true, onToggle: () -> Unit) {
    Column {
        if (topBorder) { Spacer(Modifier.height(12.dp)); ProdLine(); Spacer(Modifier.height(12.dp)) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { T(title, 12, FontWeight.Bold, C.head); Spacer(Modifier.height(2.dp)); T(sub, 10, FontWeight.Normal, C.muted, lineHeight = 16) }
            Spacer(Modifier.width(12.dp)); Sw(on, onToggle)
        }
    }
}

@Composable
private fun CalcBox(price: Double, old: Double) {
    val invalid = old <= price
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF9F1E9)).border(1.dp, Color(0xFFECDCC3), RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Ic(if (invalid) R.drawable.ic_info else R.drawable.ic_zap, 15.dp, C.terra); Spacer(Modifier.width(8.dp))
        if (invalid) T(tr("السعر قبل الخصم يجب أن يكون أعلى من سعر البيع", "The pre-discount price must be higher than the sale price"), 11, FontWeight.ExtraBold, Color(0xFFA06A3C), lineHeight = 18)
        else { val off = ((1 - price.toFloat() / old) * 100).roundToInt(); T(tr("يظهر للزبون: خصم $off٪ · وفّر $RY${money(old - price)}", "Shown to the customer: $off% off · save $RY${money(old - price)}"), 11, FontWeight.ExtraBold, Color(0xFFA06A3C), lineHeight = 18) }
    }
}

@Composable
private fun ProdLine() {
    androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(1.dp)) { drawLine(Color(0xFFE8E3D9), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 1f) }
}
