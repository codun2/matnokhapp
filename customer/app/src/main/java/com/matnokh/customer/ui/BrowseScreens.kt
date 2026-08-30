@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.matnokh.customer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matnokh.customer.R
import com.matnokh.customer.net.CatDto
import com.matnokh.customer.net.Repo
import com.matnokh.customer.net.UiOffer
import com.matnokh.customer.net.UiStore

@Composable
fun StoresScreen(onBack: () -> Unit, onCart: () -> Unit, onMenu: () -> Unit, onStore: (UiStore) -> Unit) {
    var cat by remember { mutableStateOf<Int?>(null) }
    var query by remember { mutableStateOf("") }
    var stores by remember { mutableStateOf<List<UiStore>>(emptyList()) }
    var page by remember { mutableStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    suspend fun loadPage(reset: Boolean) {
        if (loading) return
        loading = true
        val next = if (reset) 1 else page + 1
        val r = runCatching { com.matnokh.customer.net.Net.api.stores(cat, next, 20, query.trim().ifBlank { null }) }.getOrNull()
        if (r != null) {
            val ui = Repo.toUiStores(r.stores)
            stores = if (reset) ui else stores + ui
            page = next
            hasMore = r.has_more
        }
        loading = false
    }
    LaunchedEffect(cat, query) { kotlinx.coroutines.delay(300); stores = emptyList(); page = 0; hasMore = true; loadPage(true) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .collect { last -> if (hasMore && !loading && stores.isNotEmpty() && last >= stores.size - 4) loadPage(false) }
    }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        CustBackHeader(tr("المتاجر", "Stores"), onBack, onCart, onMenu)
        CatBar(cat, Repo.categories) { cat = it }
        FinField(query, { query = it }, placeholder = tr("ابحث عن متجر بالاسم أو القسم\u2026", "Search stores by name or category\u2026"), modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 6.dp))
        Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            T(if (cat == null) tr("كل المتاجر", "All stores") else Repo.categories.firstOrNull { it.id == cat }?.let { trd(it.name, it.name_en) } ?: tr("متاجر", "Stores"), 15, FontWeight.ExtraBold, C.head, Modifier.weight(1f))
            StorePill(tr("${stores.size}${if (hasMore) "+" else ""} متجراً", "${stores.size}${if (hasMore) "+" else ""} stores"), C.pillLive, C.greenD)
        }
        LazyColumn(Modifier.weight(1f), state = listState, contentPadding = PaddingValues(bottom = 24.dp)) {
            items(stores, key = { it.id }) { s -> StoreRow(s) { onStore(s) } }
            if (loading) item { Box(Modifier.fillMaxWidth().padding(vertical = 18.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) } }
            if (!loading && stores.isEmpty()) item { CenterHint(tr("لا توجد متاجر", "No stores")) }
        }
    }
}

@Composable
fun CatBar(cur: Int?, cats: List<CatDto>, onPick: (Int?) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        CatChip("🏬", tr("الكل", "All"), cur == null) { onPick(null) }
        cats.forEach { c -> CatChip(c.icon ?: "🏬", trd(c.name, c.name_en), cur == c.id) { onPick(c.id) } }
    }
}

@Composable
private fun CatChip(emoji: String, name: String, on: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(Modifier.size(62.dp).clip(RoundedCornerShape(21.dp)).then(if (on) Modifier.background(Grad.green) else Modifier.background(C.card).border(1.5.dp, C.line, RoundedCornerShape(21.dp))), contentAlignment = Alignment.Center) { if (emoji.startsWith("http")) coil.compose.AsyncImage(model = emoji, contentDescription = null, contentScale = androidx.compose.ui.layout.ContentScale.Crop, modifier = Modifier.size(62.dp).clip(RoundedCornerShape(21.dp))) else Text(emoji, fontSize = 27.sp) }
        Spacer(Modifier.height(7.dp)); T(name, 11, FontWeight.ExtraBold, if (on) C.greenD else C.muted, maxLines = 1)
    }
}

@Composable
fun CenterHint(text: String) { Box(Modifier.fillMaxWidth().padding(26.dp), contentAlignment = Alignment.Center) { T(text, 12, FontWeight.Normal, C.muted) } }

@Composable
fun OffersAllScreen(onBack: () -> Unit, onCart: () -> Unit, onMenu: () -> Unit, onOffer: (UiOffer) -> Unit) {
    val all = Repo.offers
    Column(Modifier.fillMaxSize().background(C.bg)) {
        CustBackHeader(tr("العروض والخصومات", "Offers & discounts"), onBack, onCart, onMenu)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Column(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Grad.terra).padding(20.dp)) {
                    T(tr("🔥 وفّر أكثر", "🔥 Save more"), 19, FontWeight.Black, Color.White); Spacer(Modifier.height(4.dp)); T(tr("كل المنتجات المخفّضة في متاجر مطنوخ", "All discounted products in Matnokh stores"), 12, FontWeight.Normal, Color.White.copy(alpha = .9f), lineHeight = 18)
                    Spacer(Modifier.height(11.dp)); Box(Modifier.clip(CircleShape).background(Color.White.copy(alpha = .24f)).padding(horizontal = 14.dp, vertical = 5.dp)) { T(tr("${all.size} عرضاً · بتوفير حتى ${all.maxOfOrNull { it.off } ?: 0}٪", "${all.size} offers · save up to ${all.maxOfOrNull { it.off } ?: 0}%"), 11, FontWeight.ExtraBold, Color.White) }
                }
                Spacer(Modifier.height(12.dp))
            }
            items(all) { o -> OfferRowCard(o) { onOffer(o) } }
            if (all.isEmpty()) item { CenterHint(tr("لا توجد عروض حالياً", "No offers currently")) }
        }
    }
}

@Composable
private fun OfferRowCard(o: UiOffer, onClick: () -> Unit) {
    Row(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 11.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).clickable(onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(56.dp)) { ProductImg(o.product.images.firstOrNull(), 56.dp, 16.dp) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            T(o.product.name, 13, FontWeight.Bold, C.head, maxLines = 1); Spacer(Modifier.height(2.dp)); T("${o.storeName} · ${o.storeCategory}", 10, FontWeight.Normal, C.muted, maxLines = 1)
            Spacer(Modifier.height(4.dp)); Box(Modifier.clip(CircleShape).background(Color(0xFFF6ECE4)).padding(horizontal = 8.dp, vertical = 2.dp)) { T(tr("وفّر $RY${money(o.product.oldPrice - o.product.price)}", "Save $RY${money(o.product.oldPrice - o.product.price)}"), 9, FontWeight.ExtraBold, C.terraText) }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            T("$RY${money(o.product.price)}", 16, FontWeight.Black, C.greenD)
            Text("$RY${money(o.product.oldPrice)}", fontFamily = Cairo, fontSize = 11.sp, color = Color(0xFF6E776D), textDecoration = TextDecoration.LineThrough)
        }
        Spacer(Modifier.width(8.dp)); Box(Modifier.clip(CircleShape).background(Grad.terra).padding(horizontal = 9.dp, vertical = 4.dp)) { T(tr("−${o.off}٪", "−${o.off}%"), 10, FontWeight.Black, Color.White) }
    }
}

@Composable
fun NearbyStoresLegacy(onBack: () -> Unit, onCart: () -> Unit, onMenu: () -> Unit, onExpand: () -> Unit, onStore: (UiStore) -> Unit) {
    var cat by remember { mutableStateOf<Int?>(Repo.categories.firstOrNull()?.id) }
    val list = if (cat == null) Repo.stores else Repo.stores.filter { s -> Repo.categories.firstOrNull { it.id == cat }?.let { s.categoryName == it.name } ?: true }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        CustBackHeader(tr("متاجر قريبة", "Nearby stores"), onBack, onCart, onMenu)
        Box(Modifier.fillMaxWidth().height(280.dp)) {
            val withCoords = list.filter { it.lat != null && it.lng != null }
            val center = withCoords.firstOrNull()?.let { com.google.android.gms.maps.model.LatLng(it.lat!!, it.lng!!) } ?: com.google.android.gms.maps.model.LatLng(24.7136, 46.6753)
            val camera = com.google.maps.android.compose.rememberCameraPositionState { position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(center, 11f) }
            com.google.maps.android.compose.GoogleMap(
                modifier = Modifier.fillMaxSize(), cameraPositionState = camera,
                uiSettings = com.google.maps.android.compose.MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
            ) {
                withCoords.forEach { s ->
                    com.google.maps.android.compose.Marker(
                        state = com.google.maps.android.compose.MarkerState(com.google.android.gms.maps.model.LatLng(s.lat!!, s.lng!!)),
                        title = s.name, snippet = trd(s.categoryName, s.categoryNameEn), onClick = { onStore(s); true },
                    )
                }
            }
            Box(Modifier.align(Alignment.TopEnd).padding(12.dp).clip(RoundedCornerShape(50.dp)).background(Color(0xF2FFFFFF)).border(1.dp, C.line, RoundedCornerShape(50.dp)).clickable(onClick = onExpand).padding(horizontal = 12.dp, vertical = 8.dp)) { T(tr("\uD83D\uDDFA\uFE0F ملء الشاشة", "\uD83D\uDDFA\uFE0F Fullscreen"), 11, FontWeight.ExtraBold, C.greenD) }
            Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().horizontalScroll(rememberScrollState()).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Repo.categories.forEach { c ->
                    val on = cat == c.id
                    Box(Modifier.clip(CircleShape).then(if (on) Modifier.background(Grad.green) else Modifier.background(Color.White.copy(alpha = .92f))).clickable { cat = c.id }.padding(horizontal = 14.dp, vertical = 8.dp)) { T(catText(c.icon, trd(c.name, c.name_en)), 11, FontWeight.ExtraBold, if (on) Color.White else Color(0xFF4B5A51)) }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            T(tr("${Repo.categories.firstOrNull { it.id == cat }?.name ?: "المتاجر"} قريبة منك", "${Repo.categories.firstOrNull { it.id == cat }?.let { it.name_en ?: it.name } ?: "Stores"} near you"), 15, FontWeight.ExtraBold, C.head, Modifier.weight(1f))
            StorePill("${list.size}", C.pillLive, C.greenD)
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(list) { s -> StoreRow(s) { onStore(s) } }
            if (list.isEmpty()) item { CenterHint(tr("لا توجد متاجر قريبة في هذا القسم", "No nearby stores in this category")) }
        }
    }
}

@Composable
fun MapGrid() {
    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
        val step = 34.dp.toPx()
        var x = 0f; while (x < size.width) { drawLine(Color(0x147A9684), androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height), 1f); x += step }
        var y = 0f; while (y < size.height) { drawLine(Color(0x147A9684), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 1f); y += step }
    }
}

fun catText(icon: String?, name: String): String = if (icon != null && !icon.startsWith("http")) "$icon $name" else name
