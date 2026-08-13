@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.matnokh.tajer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.matnokh.tajer.R
import com.matnokh.tajer.net.BranchMini
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.ProductDto
import com.matnokh.tajer.net.StockBody
import com.matnokh.tajer.net.call
import kotlinx.coroutines.launch

@Composable
fun ProductsScreen(onBack: () -> Unit, onMenu: () -> Unit, onNewProduct: () -> Unit, onEdit: (Int) -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var filter by remember { mutableStateOf("all") }
    var products by remember { mutableStateOf<List<ProductDto>?>(null) }
    var branches by remember { mutableStateOf<List<BranchMini>>(emptyList()) }
    var stockDialog by remember { mutableStateOf<Pair<ProductDto, Int>?>(null) } // product, branchIndex

    suspend fun load() { call({ Net.api.products() }, toast)?.let { products = it.products; branches = it.branches } }
    LaunchedEffect(Unit) { load() }

    Box(Modifier.fillMaxSize().background(C.bg)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                HeaderSquare(R.drawable.ic_back, 42.dp, 14.dp, onBack)
                Spacer(Modifier.width(10.dp))
                T("المنتجات", 18, FontWeight.ExtraBold, C.head, Modifier.weight(1f))
                products?.let { StatusPill("${it.size} منتجاً", PillKind.Live) }
                Spacer(Modifier.width(9.dp))
                HeaderSquare(R.drawable.ic_menu, 44.dp, 15.dp, onMenu)
            }
            Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(CircleShape).background(C.card).border(1.dp, C.line, CircleShape).padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("all" to "الكل", "active" to "نشط", "draft" to "مسودة", "archived" to "مؤرشف").forEach { (k, lbl) ->
                    val on = filter == k
                    Box(Modifier.weight(1f).clip(CircleShape).then(if (on) Modifier.background(Grad.green) else Modifier).clickable { filter = k }.padding(vertical = 9.dp), contentAlignment = Alignment.Center) {
                        T(lbl, 11, FontWeight.ExtraBold, if (on) Color.White else C.muted)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            val all = products
            if (all == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }; return@Column }
            val list = if (filter == "all") all else all.filter { it.status == filter }
            if (list.isEmpty()) Box(Modifier.fillMaxWidth().padding(26.dp), contentAlignment = Alignment.Center) { T("لا توجد منتجات في هذا التبويب", 12, FontWeight.Normal, C.muted) }
            else LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 100.dp)) {
                items(list) { p ->
                    ProductCard(p, branches,
                        onToggle = { scope.launch { call({ Net.api.toggleProduct(p.id) }, toast)?.let { toast(it.message ?: ""); load() } } },
                        onEdit = { onEdit(p.id) },
                        onChip = { bi -> stockDialog = p to bi })
                }
            }
        }
        Box(Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 24.dp).size(56.dp).clip(RoundedCornerShape(19.dp)).background(Grad.green).clickable(onClick = onNewProduct), contentAlignment = Alignment.Center) {
            Ic(R.drawable.ic_plus, 28.dp, Color.White)
        }
    }

    stockDialog?.let { (p, bi) ->
        StockDialog(p, bi, onClose = { stockDialog = null }, onSave = { qty ->
            val branchId = p.stock.getOrNull(bi)?.branch_id ?: return@StockDialog
            scope.launch { call({ Net.api.setStock(p.id, StockBody(branchId, qty)) }, toast)?.let { stockDialog = null; toast(it.message ?: "تم"); load() } }
        })
    }
}

@Composable
private fun ProductCard(p: ProductDto, branches: List<BranchMini>, onToggle: () -> Unit, onEdit: () -> Unit, onChip: (Int) -> Unit) {
    Column(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp)) {
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)).background(C.card)
            .border(1.dp, C.line, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)).clickable(onClick = onEdit).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            val img = p.images.firstOrNull()
            if (img != null) AsyncImage(model = img, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)))
            else EmojiBox("🍽️", 54.dp, 16.dp, 25)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                T(p.name, 13, FontWeight.Bold, C.head, maxLines = 2)
                Spacer(Modifier.height(2.dp))
                val extra = buildString { append(p.section ?: "بلا قسم"); if (p.addons.isNotEmpty()) append(" · ${p.addons.size} إضافات"); if (p.images.size > 1) append(" · ${p.images.size} صور") }
                T(extra, 10, FontWeight.Normal, C.muted, maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                T("﷼${money(p.price)}", 14, FontWeight.Black, C.greenD)
                if (p.price_before > 0) {
                    Text("﷼${money(p.price_before)}", fontFamily = Cairo, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = C.strike, textDecoration = TextDecoration.LineThrough)
                    Spacer(Modifier.height(4.dp)); StatusPill("خصم ${p.discount}٪", PillKind.Wait)
                }
            }
            Spacer(Modifier.width(10.dp))
            Sw(p.status == "active", onToggle)
        }
        FlowRow(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp)).background(Color(0xFFFAF8F4))
            .border(1.dp, C.line, RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp)).padding(horizontal = 13.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            p.stock.forEachIndexed { bi, st ->
                val has = st.in_stock > 0
                val short = st.branch.replace("الفرع ", "").replace("فرع ", "")
                Row(Modifier.clip(CircleShape).background(if (has) C.pillLive else C.redBg).clickable { onChip(bi) }.padding(horizontal = 11.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    T(short, 10, FontWeight.ExtraBold, if (has) C.greenD else C.redText)
                    Spacer(Modifier.width(5.dp))
                    if (has) T("${st.in_stock}", 10, FontWeight.Black, C.greenD) else T("✕", 10, FontWeight.Black, C.redText)
                }
            }
            if (p.stock.isEmpty()) T("أضف فرعاً لإدارة الكمية", 10, FontWeight.Medium, C.muted)
        }
    }
}

@Composable
private fun StockDialog(p: ProductDto, bi: Int, onClose: () -> Unit, onSave: (Int) -> Unit) {
    val st = p.stock.getOrNull(bi) ?: return
    var qty by remember { mutableStateOf(st.in_stock) }
    Box(Modifier.fillMaxSize().background(Color(0x80253A34)).clickable(onClick = onClose), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(24.dp).widthIn(max = 300.dp).fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(C.bg).clickable(enabled = false) {}) {
            Column(Modifier.fillMaxWidth().background(Grad.green).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Ic(R.drawable.ic_pin, 26.dp, Color.White)
                Spacer(Modifier.height(6.dp)); T(p.name, 14, FontWeight.Black, Color.White, maxLines = 2)
                T(st.branch, 11, FontWeight.Normal, Color.White.copy(alpha = .9f))
            }
            Column(Modifier.padding(20.dp)) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (st.in_stock > 0) T("الكمية الحالية: ${st.in_stock} قطعة", 11, FontWeight.Bold, C.greenD)
                    else T("نفدت الكمية في هذا الفرع", 11, FontWeight.Bold, C.redText)
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StepBtn("−") { qty = maxOf(0, qty - 1) }
                    Box(Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).border(1.5.dp, Color(0xFFCFE0D4), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { T("$qty", 23, FontWeight.Black, C.greenD) }
                    StepBtn("+") { qty += 1 }
                }
                Spacer(Modifier.height(16.dp))
                WideButton("حفظ الكمية", R.drawable.ic_check) { onSave(qty) }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(C.redBg).clickable { onSave(0) }.padding(14.dp), contentAlignment = Alignment.Center) { T("نفاذ الكمية في هذا الفرع", 13, FontWeight.ExtraBold, C.redText) }
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().clickable(onClick = onClose).padding(9.dp), contentAlignment = Alignment.Center) { T("إغلاق", 13, FontWeight.Bold, C.muted) }
            }
        }
    }
}

@Composable
private fun StepBtn(label: String, onClick: () -> Unit) {
    Box(Modifier.size(48.dp, 52.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFFAF8F4)).border(1.dp, C.line, RoundedCornerShape(16.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, fontFamily = Cairo, fontSize = 22.sp, fontWeight = FontWeight.Black, color = C.greenD)
    }
}
