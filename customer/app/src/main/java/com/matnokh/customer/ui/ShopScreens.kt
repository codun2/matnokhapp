@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.matnokh.customer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matnokh.customer.R
import com.matnokh.customer.net.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun StoreScreen(onBack: () -> Unit, onCart: () -> Unit, onMenu: () -> Unit, onProduct: (UiProduct) -> Unit, toast: (String) -> Unit) {
    val d = Repo.detail
    var branch by remember(d) { mutableStateOf(0) }
    var sec by remember(d) { mutableStateOf(Sel.sectionIdx) }
    var query by remember(d) { mutableStateOf("") }
    if (d == null) { Column(Modifier.fillMaxSize().background(C.bg)) { CustBackHeader(tr("المتجر", "Store"), onBack, onCart, onMenu); Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) } }; return }
    val s = d.store
    val preloadCtx = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(d) {
        val loader = coil.Coil.imageLoader(preloadCtx)
        d.sections.flatMap { it.items }.mapNotNull { it.images.firstOrNull() }.distinct().take(8).forEach { u ->
            loader.enqueue(coil.request.ImageRequest.Builder(preloadCtx).data(u).build())
        }
    }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        CustBackHeader(trd(s.name, s.nameEn), onBack, onCart, onMenu) { StorePill(if (s.isOpen) tr("متاح", "Available") else tr("مغلق", "Closed"), if (s.isOpen) C.pillLive else C.pillOff, if (s.isOpen) C.ok else Color(0xFF9AA198)) }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                StoreLogo(s.logo, 52.dp, 16.dp, trd(s.categoryName, s.categoryNameEn))
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) { T(s.name, 14, FontWeight.Bold, C.head); Spacer(Modifier.height(3.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Text("★", color = Color(0xFFD9A441), fontSize = 11.sp); Spacer(Modifier.width(4.dp)); T("${s.rating} · ${trd(s.categoryName, s.categoryNameEn)}", 11, FontWeight.Normal, C.muted) } }
                Box(Modifier.clip(CircleShape).background(Color(0xFFE9F0F4)).padding(horizontal = 9.dp, vertical = 3.dp)) { T(tr("${s.dist} كم", "${s.dist} km"), 10, FontWeight.ExtraBold, C.blueText) }
            }
            FinField(query, { query = it }, placeholder = tr("ابحث في منتجات المتجر\u2026", "Search store products\u2026"), modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp))
            if (d.sections.isNotEmpty()) {
                if (query.isBlank()) Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 22.dp, end = 22.dp, top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    d.sections.forEachIndexed { i, sc ->
                        val on = sec == i
                        Box(Modifier.clip(CircleShape).then(if (on) Modifier.background(Grad.green) else Modifier.background(C.card).border(1.dp, C.line, CircleShape)).clickable { sec = i; Sel.sectionIdx = i; Sel.sectionStoreId = d.store.id }.padding(horizontal = 17.dp, vertical = 8.dp)) { T(sc.name, 12, FontWeight.ExtraBold, if (on) Color.White else C.muted) }
                    }
                }
                val items = if (query.isNotBlank()) d.sections.flatMap { it.items }.filter { it.name.contains(query.trim(), true) } else (d.sections.getOrNull(sec)?.items ?: emptyList())
                Column(Modifier.padding(horizontal = 22.dp, vertical = 12.dp)) {
                    if (items.isEmpty() && query.isNotBlank()) CenterHint(tr("لا نتائج مطابقة للبحث", "No results match your search"))
                    items.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth().padding(bottom = 11.dp), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                            row.forEach { p -> val out = branch in p.outBranches; ProductCard(p, out, Modifier.weight(1f)) { if (out) toast(tr("${p.name} غير متوفر في هذا الفرع", "${p.name} not available at this branch")) else { Sel.branchIdx = branch; onProduct(p) } } }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            } else CenterHint(tr("لا توجد منتجات في هذا المتجر بعد", "No products in this store yet"))
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ProductCard(p: UiProduct, out: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val off = if (p.oldPrice > 0) ((1 - p.price.toFloat() / p.oldPrice) * 100).roundToInt() else 0
    Box(modifier.clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).then(if (out) Modifier else Modifier.clickable(onClick = onClick)).padding(12.dp).alpha(if (out) .62f else 1f)) {
        Column {
            ProductImg(p.images.firstOrNull(), 74.dp, 15.dp)
            Spacer(Modifier.height(9.dp)); T(p.name, 12, FontWeight.Bold, C.head, maxLines = 1); Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.Bottom) { T("$RY${money(p.price)}", 14, FontWeight.Black, C.greenD); if (p.oldPrice > 0) { Spacer(Modifier.width(6.dp)); Text("$RY${money(p.oldPrice)}", fontFamily = Cairo, fontSize = 10.sp, color = Color(0xFF6E776D), textDecoration = TextDecoration.LineThrough) } }
        }
        if (out) Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().clip(CircleShape).background(Color(0xFFF7E7E2)).padding(vertical = 4.dp), contentAlignment = Alignment.Center) { T(tr("غير متوفر بهذا الفرع", "Not available at this branch"), 9, FontWeight.ExtraBold, C.redText) }
        else if (p.oldPrice > 0) Box(Modifier.align(Alignment.TopStart).clip(CircleShape).background(Grad.terra).padding(horizontal = 8.dp, vertical = 3.dp)) { T(tr("خصم $off٪", "$off% off"), 9, FontWeight.ExtraBold, Color.White) }
    }
}

@Composable
fun ProductScreen(onBack: () -> Unit, onCart: () -> Unit, onMenu: () -> Unit, onAdded: () -> Unit, toast: (String) -> Unit) {
    val p = Sel.product ?: return
    val store = Sel.store ?: return
    var imgIdx by remember(p) { mutableStateOf(0) }
    var qty by remember(p) { mutableStateOf(1) }
    val addQty = remember(p) { mutableStateMapOf<Int, Int>() }
    var showConflict by remember { mutableStateOf(false) }
    val off = if (p.oldPrice > 0) ((1 - p.price.toFloat() / p.oldPrice) * 100).roundToInt() else 0
    val addonsTotal = addQty.entries.sumOf { p.addons[it.key].price * it.value }
    val itemPrice = p.price * qty + addonsTotal
    val oldItemPrice = if (p.oldPrice > 0) p.oldPrice * qty + addonsTotal else 0.0
    Column(Modifier.fillMaxSize().background(C.bg)) {
        CustBackHeader(p.name, onBack, onCart, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box(Modifier.padding(horizontal = 22.dp).fillMaxWidth().height(168.dp).clip(RoundedCornerShape(26.dp)).border(1.dp, C.line, RoundedCornerShape(26.dp)), contentAlignment = Alignment.Center) {
                ProductImg(p.images.getOrNull(imgIdx), 168.dp, 26.dp)
                if (p.oldPrice > 0) Box(Modifier.align(Alignment.TopStart).padding(14.dp).clip(CircleShape).background(Grad.terra).padding(horizontal = 9.dp, vertical = 3.dp)) { T(tr("خصم $off٪", "$off% off"), 10, FontWeight.ExtraBold, Color.White) }
                if (p.images.size > 1) {
                    Box(Modifier.align(Alignment.CenterEnd).padding(12.dp).size(36.dp).clip(RoundedCornerShape(13.dp)).background(Color.White.copy(alpha = .95f)).clickable { imgIdx = (imgIdx - 1 + p.images.size) % p.images.size }, contentAlignment = Alignment.Center) { Text("‹", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF5D6B62)) }
                    Box(Modifier.align(Alignment.CenterStart).padding(12.dp).size(36.dp).clip(RoundedCornerShape(13.dp)).background(Color.White.copy(alpha = .95f)).clickable { imgIdx = (imgIdx + 1) % p.images.size }, contentAlignment = Alignment.Center) { Text("›", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF5D6B62)) }
                    Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 11.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) { p.images.indices.forEach { i -> Box(Modifier.height(6.dp).width(if (i == imgIdx) 18.dp else 6.dp).clip(CircleShape).background(if (i == imgIdx) C.green else Color(0xFFD5CFC2))) } }
                }
            }
            Spacer(Modifier.height(12.dp))
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                T(p.name, 15, FontWeight.Bold, C.head); Spacer(Modifier.height(3.dp)); T(p.desc.ifBlank { trd(store.name, store.nameEn) }, 11, FontWeight.Normal, C.muted, lineHeight = 18)
                Spacer(Modifier.height(11.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { T("$RY${money(p.price)}", 22, FontWeight.Black, C.greenD); if (p.oldPrice > 0) { Spacer(Modifier.width(9.dp)); Text("$RY${money(p.oldPrice)}", fontFamily = Cairo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6E776D), textDecoration = TextDecoration.LineThrough); Spacer(Modifier.width(9.dp)); Box(Modifier.clip(CircleShape).background(Color(0xFFF6ECE4)).padding(horizontal = 10.dp, vertical = 4.dp)) { T(tr("وفّر $RY${money(p.oldPrice - p.price)}", "Save $RY${money(p.oldPrice - p.price)}"), 10, FontWeight.ExtraBold, C.terraText) } } }
            }
            if (p.addons.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Ic(R.drawable.ic_plus, 18.dp, C.green); Spacer(Modifier.width(8.dp)); T(tr("أضف على طلبك", "Add to your order"), 13, FontWeight.ExtraBold, Color(0xFF4B5A51)); Spacer(Modifier.width(6.dp)); T(tr("— اختياري، حدّد العدد", "— optional, set the quantity"), 11, FontWeight.Normal, C.muted) }
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        p.addons.forEachIndexed { i, a ->
                            val c = addQty[i] ?: 0
                            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).then(if (c > 0) Modifier.background(Color(0xFFEEF4EF)).border(1.5.dp, C.green, RoundedCornerShape(14.dp)) else Modifier.background(C.card2).border(1.dp, C.line, RoundedCornerShape(14.dp))).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("➕", fontSize = 18.sp); Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) { T(a.name, 12, FontWeight.ExtraBold, if (c > 0) C.greenD else C.head); Spacer(Modifier.height(1.dp)); T(tr("+$RY${money(a.price)} للحبة", "+$RY${money(a.price)} each"), 10, FontWeight.ExtraBold, C.blueText) }
                                AddStep("−") { if (c > 0) { if (c <= 1) addQty.remove(i) else addQty[i] = c - 1 } }
                                Box(Modifier.widthIn(min = 28.dp), contentAlignment = Alignment.Center) { T("$c", 15, FontWeight.Black, if (c > 0) C.greenD else C.muted) }
                                AddStep("+") { addQty[i] = c + 1 }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) { Ic(R.drawable.ic_box, 18.dp, C.green); Spacer(Modifier.width(8.dp)); T(tr("الكمية", "Quantity"), 13, FontWeight.ExtraBold, Color(0xFF4B5A51)) }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { QBtn("+") { qty++ }; Spacer(Modifier.width(16.dp)); T("$qty", 20, FontWeight.Black, C.head, Modifier.widthIn(min = 32.dp)); Spacer(Modifier.width(16.dp)); QBtn("−") { qty = maxOf(1, qty - 1) } }
            }
            Spacer(Modifier.height(14.dp))
            val doAddToCart = {
                Cart.merchantId = store.id; Cart.branchId = Repo.detail?.branches?.getOrNull(Sel.branchIdx)?.id; Cart.storeName = store.name
                Cart.lines.add(CartLine(p.id, p.name, p.images.firstOrNull(), qty, addQty.entries.filter { it.value > 0 }.sortedBy { it.key }.map { p.addons[it.key].name + if (it.value > 1) " ×${it.value}" else "" }, itemPrice, oldItemPrice)); toast(tr("أُضيف للسلة ✓", "Added to cart ✓")); onAdded()
            }
            Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Grad.green).clickable {
                if (!store.isOpen) { toast(tr("متجر ${trd(store.name, store.nameEn)} لا يستقبل طلبات حالياً — يمكنك الطلب منه في أوقات الدوام", "${trd(store.name, store.nameEn)} isn't accepting orders now — you can order during opening hours")); return@clickable }
                if (Cart.lines.isNotEmpty() && Cart.merchantId != null && Cart.merchantId != store.id) { showConflict = true; return@clickable }
                doAddToCart()
            }.padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { T(if (store.isOpen) tr("أضف للسلة — $RY${money(itemPrice)}", "Add to cart — $RY${money(itemPrice)}") else tr("المتجر مغلق حالياً — للتصفّح فقط", "Store currently closed — browsing only"), 15, FontWeight.ExtraBold, Color.White); Spacer(Modifier.width(8.dp)); Ic(R.drawable.ic_check, 17.dp, Color.White) }
            if (showConflict) androidx.compose.material3.AlertDialog(
                onDismissRequest = { showConflict = false },
                confirmButton = { androidx.compose.material3.TextButton(onClick = { Cart.clear(); showConflict = false; doAddToCart() }) { T(tr("إفراغ السلة وإضافة", "Empty cart & add"), 13, FontWeight.Bold, C.redText) } },
                dismissButton = { androidx.compose.material3.TextButton(onClick = { showConflict = false }) { T(tr("إلغاء", "Cancel"), 13, FontWeight.Bold, C.muted) } },
                title = { T(tr("سلة من متجر آخر", "Cart from another store"), 15, FontWeight.Bold, C.head) },
                text = { T(tr("سلتك تحتوي على منتجات من «${Cart.storeName}». أكمل طلبك الحالي أولاً، أو أفرغ السلة للطلب من «${trd(store.name, store.nameEn)}».", "Your cart has items from «${Cart.storeName}». Finish your current order first, or empty the cart to order from «${trd(store.name, store.nameEn)}»."), 12, FontWeight.Normal, C.muted, lineHeight = 18) }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QBtn(ch: String, onClick: () -> Unit) { Box(Modifier.size(46.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFFAF8F4)).border(1.dp, C.line, RoundedCornerShape(16.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) { Text(ch, fontFamily = Cairo, fontSize = 21.sp, fontWeight = FontWeight.Black, color = C.greenD) } }

@Composable
private fun AddStep(ch: String, onClick: () -> Unit) { Box(Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(Color(0xFFFAF8F4)).border(1.dp, C.line, RoundedCornerShape(11.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) { Text(ch, fontFamily = Cairo, fontSize = 17.sp, fontWeight = FontWeight.Black, color = C.greenD) } }

@Composable
private fun PayChip(label: String, selected: Boolean, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(14.dp)).background(if (selected) Grad.green else androidx.compose.ui.graphics.SolidColor(C.card)).border(1.5.dp, if (selected) Color.Transparent else C.line, RoundedCornerShape(14.dp)).alpha(if (enabled) 1f else 0.45f).clickable(enabled = enabled, onClick = onClick).padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
        T(label, 13, FontWeight.ExtraBold, if (selected) Color.White else C.head)
    }
}

@Composable
fun CartScreen(onBack: () -> Unit, onMenu: () -> Unit, onOrdered: (String, Int?) -> Unit, onDest: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var sending by remember { mutableStateOf(false) }
    var fee by remember { mutableStateOf<Double?>(null) }
    var feeMode by remember { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var payMethod by remember { mutableStateOf("card") }
    var bankIban by remember { mutableStateOf<String?>(null) }
    var bankName by remember { mutableStateOf<String?>(null) }
    var bankAccount by remember { mutableStateOf<String?>(null) }
    var proofUrl by remember { mutableStateOf<String?>(null) }
    var uploading by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            uploading = true
            try {
                val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) { toast(tr("تعذّرت قراءة الصورة", "Couldn't read the image")); uploading = false; return@launch }
                val part = MultipartBody.Part.createFormData("file", "receipt.jpg", bytes.toRequestBody("image/*".toMediaTypeOrNull()))
                call({ Net.api.upload(part) }, toast)?.url?.let { proofUrl = it; toast(tr("تم رفع الإيصال ✓", "Receipt uploaded ✓")) }
            } finally { uploading = false }
        }
    }
    LaunchedEffect(Cart.merchantId, Sel.destLat, Sel.destLng) {
        val mid = Cart.merchantId
        if (mid != null) runCatching { Net.api.quoteDelivery(com.matnokh.customer.net.QuoteBody(mid, Sel.destLat, Sel.destLng)) }.getOrNull()?.let { fee = it.delivery_fee; feeMode = it.delivery_mode }
    }
    LaunchedEffect(Cart.merchantId) {
        val mid = Cart.merchantId
        if (mid != null) runCatching { Net.api.storeDetail(mid) }.getOrNull()?.store?.let { bankIban = it.iban; bankName = it.bank_name; bankAccount = it.account_name }
    }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        Row(Modifier.fillMaxWidth().background(C.bg.copy(alpha = .96f)).statusBarsPadding().padding(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            HeaderIcon(R.drawable.ic_back, onBack); Spacer(Modifier.width(10.dp)); T(tr("سلتي", "My cart"), 18, FontWeight.ExtraBold, C.head, Modifier.weight(1f)); StorePill(Cart.storeName.ifBlank { "—" }, C.pillLive, C.greenD); Spacer(Modifier.width(9.dp)); HeaderIcon(R.drawable.ic_menu, onMenu)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(vertical = 4.dp)) {
                if (Cart.lines.isEmpty()) CenterHint(tr("سلتك فارغة", "Your cart is empty")) else Cart.lines.forEachIndexed { i, c ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(42.dp)) { ProductImg(c.image, 42.dp, 13.dp) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) { T("${c.name} ×${c.qty}", 12, FontWeight.Bold, C.head, maxLines = 1); Spacer(Modifier.height(1.dp)); T(if (c.addons.isEmpty()) tr("بدون إضافات", "No add-ons") else tr("إضافات: ${c.addons.joinToString(" · ")}", "Add-ons: ${c.addons.joinToString(" · ")}"), 10, FontWeight.Medium, C.muted, maxLines = 1) }
                        Column(horizontalAlignment = Alignment.End) { T("$RY${money(c.price)}", 13, FontWeight.Black, C.greenD); if (c.oldPrice > c.price) Text("$RY${money(c.oldPrice)}", fontFamily = Cairo, fontSize = 9.sp, color = Color(0xFF6E776D), textDecoration = TextDecoration.LineThrough) }; Spacer(Modifier.width(8.dp))
                        Box(Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(C.redBg).clickable { Cart.lines.removeAt(i) }, contentAlignment = Alignment.Center) { T("×", 14, FontWeight.Black, C.redText) }
                    }
                }
            }
            run {
                val itemsT = Cart.total()
                val perKmNoDest = feeMode == "per_km" && (Sel.destLat == null || Sel.destLng == null)
                val f = if (perKmNoDest) 0.0 else (fee ?: 0.0)
                Column(Modifier.padding(horizontal = 22.dp, vertical = 4.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.5.dp, Color(0xFFCFE0D4), RoundedCornerShape(22.dp)).padding(17.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { T(tr("مجموع المنتجات", "Products total"), 12, FontWeight.Medium, C.muted, Modifier.weight(1f)); T("$RY${money(itemsT)}", 13, FontWeight.Bold, C.head) }
                    Spacer(Modifier.height(9.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { T(tr("أجرة التوصيل", "Delivery fee"), 12, FontWeight.Medium, C.muted, Modifier.weight(1f)); T(if (perKmNoDest) tr("حدّد الوجهة لحسابها", "Set the destination to calculate") else if (f > 0) "$RY${money(f)}" else tr("مجانية", "Free"), 12, FontWeight.Bold, if (perKmNoDest) C.muted else C.head) }
                    Spacer(Modifier.height(11.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(C.line)); Spacer(Modifier.height(11.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { T(tr("الإجمالي", "Total"), 14, FontWeight.Black, C.head, Modifier.weight(1f)); T("$RY${money(itemsT + f)}", 19, FontWeight.Black, C.greenD) }
                    if (feeMode == "per_km") { Spacer(Modifier.height(6.dp)); T(tr("تُحتسب أجرة التوصيل حسب بُعد وجهتك عن المتجر.", "The delivery fee is calculated by your destination's distance from the store."), 9, FontWeight.Normal, C.muted) }
                }
            }
            Column(Modifier.padding(horizontal = 22.dp, vertical = 12.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFFF2F8F3)).border(1.dp, Color(0xFFCFE0D4), RoundedCornerShape(18.dp)).padding(horizontal = 15.dp, vertical = 12.dp)) { T(tr("بعد تأكيد الطلب يُرسَل للمتجر، وبعد تجهيزه يُسنَد لأقرب مندوب لتوصيله إليك.", "After you confirm, the order goes to the store; once prepared it's assigned to the nearest courier to deliver it to you."), 11, FontWeight.Medium, Color(0xFF4B5A51), lineHeight = 20) }
            DestRow(onDest)
            // قسم اختيار الدفع يظهر فقط إذا أدخل المتجر حسابه البنكي؛ وإلا يُتمّ الطلب بالبطاقة مباشرة (payMethod=card الافتراضي)
            if (!bankIban.isNullOrBlank()) Column(Modifier.padding(horizontal = 22.dp, vertical = 4.dp).fillMaxWidth()) {
                T(tr("وسيلة الدفع", "Payment method"), 13, FontWeight.Bold, C.head)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PayChip(tr("بطاقة", "Card"), payMethod == "card", Modifier.weight(1f)) { payMethod = "card" }
                    if (!bankIban.isNullOrBlank()) PayChip(tr("تحويل بنكي", "Bank transfer"), payMethod == "bank_transfer", Modifier.weight(1f)) { payMethod = "bank_transfer" }
                }
                if (payMethod == "bank_transfer") {
                    Spacer(Modifier.height(10.dp))
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF2F8F3)).border(1.dp, Color(0xFFCFE0D4), RoundedCornerShape(16.dp)).padding(14.dp)) {
                        bankName?.takeIf { it.isNotBlank() }?.let { T(tr("البنك: ", "Bank: ") + it, 12, FontWeight.Bold, C.head); Spacer(Modifier.height(4.dp)) }
                        bankAccount?.takeIf { it.isNotBlank() }?.let { T(tr("اسم المستفيد: ", "Beneficiary name: ") + it, 12, FontWeight.Medium, C.head); Spacer(Modifier.height(4.dp)) }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { T(tr("رقم الآيبان (IBAN)", "IBAN number"), 10, FontWeight.Normal, C.muted); T(bankIban ?: "—", 13, FontWeight.Black, C.greenD) }
                            Box(Modifier.clip(RoundedCornerShape(10.dp)).background(Grad.green).clickable { bankIban?.let { clipboard.setText(AnnotatedString(it)); toast(tr("نُسخ الآيبان ✓", "IBAN copied ✓")) } }.padding(horizontal = 12.dp, vertical = 8.dp)) { T(tr("نسخ", "Copy"), 11, FontWeight.ExtraBold, Color.White) }
                        }
                        Spacer(Modifier.height(10.dp))
                        T(tr("المبلغ المطلوب تحويله (قيمة المنتجات فقط)", "Amount to transfer (items value only)"), 10, FontWeight.Normal, C.muted); T("$RY${money(Cart.total())}", 18, FontWeight.Black, C.greenD); Spacer(Modifier.height(6.dp)); T(tr("حوّل هذا المبلغ لحساب المتجر ثم ارفع صورة الإيصال. أمّا أجرة التوصيل $RY${money(fee ?: 0.0)} فتُدفع للمندوب نقدًا عند استلامك الطلب.", "Transfer this amount to the store's account, then upload the receipt. The delivery fee $RY${money(fee ?: 0.0)} is paid to the courier in cash when you receive your order."), 10, FontWeight.Normal, Color(0xFF4B5A51), lineHeight = 16)
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(C.card).border(1.dp, if (proofUrl != null) C.greenD else C.line, RoundedCornerShape(12.dp)).clickable(enabled = !uploading) { picker.launch("image/*") }.padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            T(if (uploading) tr("جارٍ الرفع…", "Uploading…") else if (proofUrl != null) tr("تم رفع الإيصال ✓ — تغيير", "Receipt uploaded ✓ — change") else tr("ارفع صورة إيصال التحويل", "Upload transfer receipt"), 12, FontWeight.ExtraBold, if (proofUrl != null) C.greenD else C.head)
                        }
                    }
                }
            }
            if (Cart.lines.isNotEmpty()) Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Grad.green).clickable {
                if (sending) return@clickable
                val mid = Cart.merchantId
                if (mid == null) { toast(tr("خطأ في المتجر", "Store error")); return@clickable }
                if (!Session.isLoggedIn()) { toast(tr("سجّل الدخول لإتمام الطلب", "Log in to complete the order")); return@clickable }
                if (payMethod == "bank_transfer" && proofUrl == null) { toast(tr("ارفع صورة إيصال التحويل أولاً", "Upload the transfer receipt first")); return@clickable }
                val sn = Cart.storeName
                scope.launch {
                    sending = true
                    val body = CreateOrderBody(mid, Cart.branchId, payMethod, Sel.destAddr ?: Sel.destLabel, Cart.lines.map { OrderItemBody(it.productId, it.name, (it.price.toDouble() / it.qty), it.qty, it.addons) }, Sel.destLat, Sel.destLng, payment_proof = if (payMethod == "bank_transfer") proofUrl else null)
                    call({ Net.api.createOrder(body) }, toast)?.let { r -> Cart.clear(); r.payment_url?.let { u -> runCatching { ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(u)).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) } }; onOrdered(sn, r.order_id) }
                    sending = false
                }
            }.padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { T(if (sending) tr("جارٍ الإرسال…", "Sending…") else if (payMethod == "bank_transfer") tr("تأكيد الطلب وإرسال الإيصال", "Confirm order & send receipt") else tr("تأكيد الطلب وإرساله للمتجر", "Confirm & send order to store"), 14, FontWeight.ExtraBold, Color.White); Spacer(Modifier.width(8.dp)); Ic(R.drawable.ic_check, 16.dp, Color.White) }
            Spacer(Modifier.height(24.dp))
        }
    }
}
