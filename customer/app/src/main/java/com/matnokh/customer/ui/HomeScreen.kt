package com.matnokh.customer.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import com.matnokh.customer.R
import com.matnokh.customer.net.Repo
import com.matnokh.customer.net.UiStore
import kotlinx.coroutines.delay

private data class Slide(val brush: Brush, val title: String, val body: String, val tag: String)

@Composable
fun HomeScreen(onMenu: () -> Unit, onCart: () -> Unit, onStores: () -> Unit, onOffers: () -> Unit, onNearby: () -> Unit, onStore: (UiStore) -> Unit, onService: (com.matnokh.customer.net.SvcDto) -> Unit, onTrack: () -> Unit, onBell: () -> Unit, onAllServices: () -> Unit = {}) {
    val homeCtx = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) { if (com.matnokh.customer.net.Repo.here == null) currentLatLng(homeCtx)?.let { com.matnokh.customer.net.Repo.here = it } }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        CustomerHeader(onMenu, onCart, Cart.count(), onBell)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            val slides = listOf(
                Slide(Grad.green, tr("خصم 20٪ على أول طلب 🎉", "20% off your first order 🎉"), tr("على كل خدمات التوصيل والنقل داخل مدينتك", "On all delivery & transport services in your city"), "MTN20"),
                Slide(Grad.terra, tr("نقل عفش كامل مع عمّال 🛋", "Full furniture move with workers 🛋"), tr("تغليف وتحميل وتركيب — احجز بضغطة واحدة", "Packing, loading & assembly — book in one tap"), tr("خدمة نقل الأثاث", "Furniture moving service")),
                Slide(Grad.blue, tr("مطنوخ للشركات 🏢", "Matnokh for Business 🏢"), tr("عقود شهرية لتوصيل بضائعك بأسعار تفضيلية", "Monthly contracts to deliver your goods at preferential rates"), tr("تواصل معنا", "Contact us")),
            )
            var idx by remember { mutableStateOf(0) }
            LaunchedEffect(idx) { delay(3500); idx = (idx + 1) % slides.size }
            Box(Modifier.padding(start = 22.dp, end = 22.dp, top = 16.dp).fillMaxWidth().height(152.dp).clip(RoundedCornerShape(26.dp))) {
                Crossfade(idx, label = "slide") { i ->
                    val s = slides[i]
                    Box(Modifier.fillMaxSize().background(s.brush).padding(20.dp)) {
                        Column(Modifier.align(Alignment.CenterStart)) {
                            T(s.title, 18, FontWeight.Black, Color.White); Spacer(Modifier.height(4.dp)); T(s.body, 12, FontWeight.Normal, Color.White.copy(alpha = .88f), lineHeight = 18)
                            Spacer(Modifier.height(10.dp)); Box(Modifier.clip(CircleShape).background(Color.White.copy(alpha = .24f)).padding(horizontal = 13.dp, vertical = 4.dp)) { T(s.tag, 10, FontWeight.ExtraBold, Color.White) }
                        }
                    }
                }
                Row(Modifier.align(Alignment.BottomStart).padding(start = 22.dp, bottom = 11.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    slides.indices.forEach { i -> Box(Modifier.height(7.dp).width(if (i == idx) 19.dp else 7.dp).clip(CircleShape).background(if (i == idx) Color.White else Color.White.copy(alpha = .45f)).clickable { idx = i }) }
                }
            }

            SecTitle(tr("متاجر مطنوخ", "Matnokh stores"), tr("عرض الكل", "View all"), onLink = onStores)
            if (!Repo.loaded && Repo.stores.isEmpty()) Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }
            else Repo.stores.take(4).forEach { s -> StoreRow(s) { onStore(s) } }

            NearCard(Grad.terra, R.drawable.ic_zap, tr("كل العروض والخصومات 🔥", "All offers & discounts 🔥"), tr("${Repo.offers.size} منتجاً عليه خصم الآن — من كل المتاجر", "${Repo.offers.size} products on sale now — from all stores"), Color(0xFFECDCC3), onOffers)
            NearCard(Grad.blue, R.drawable.ic_pin, tr("متاجر قريبة منك 🗺", "Stores near you 🗺"), tr("افتح الخريطة واختر القسم — تظهر لك الأقرب فالأقرب", "Open the map and pick a category — nearest first"), onClick = onNearby)

            SecTitle(tr("خدمات النقل", "Transport services"), tr("عرض الكل", "View all"), onAllServices)
            Column(Modifier.padding(horizontal = 22.dp)) {
                Repo.services.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { svc -> SvcCard(svc, Modifier.weight(1f)) { onService(svc) } }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                if (Repo.services.isEmpty()) T(tr("لا توجد خدمات نقل مُفعّلة حالياً", "No transport services enabled currently"), 11, FontWeight.Medium, C.muted, Modifier.padding(horizontal = 22.dp))
            }

            SecTitle(tr("طلباتك الجارية", "Your active orders"))
            Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card).border(1.5.dp, Color(0xFFCFE0D4), RoundedCornerShape(22.dp)).clickable(onClick = onTrack).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Grad.green), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_box, 22.dp, Color.White) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) { T(tr("تابع طلباتك الجارية", "Track your active orders"), 14, FontWeight.Bold, C.head); Spacer(Modifier.height(2.dp)); T(tr("اعرض الطلبات النشطة وعروض التوصيل وحالتها", "View active orders, delivery offers and their status"), 11, FontWeight.Normal, C.muted, maxLines = 1) }
                Ic(R.drawable.ic_back, 17.dp, Color(0xFFC3C9C0))
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SvcCard(svc: com.matnokh.customer.net.SvcDto, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.height(140.dp).clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).clickable(onClick = onClick).padding(16.dp)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Grad.green), contentAlignment = Alignment.Center) {
                val si = svc.icon
                when {
                    si != null && si.startsWith("http") -> coil.compose.AsyncImage(model = si, contentDescription = null, contentScale = androidx.compose.ui.layout.ContentScale.Crop, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)))
                    !si.isNullOrBlank() -> T(si, 24, FontWeight.Bold, Color.White)
                    else -> Ic(R.drawable.ic_van, 24.dp, Color.White)
                }
            }
            Spacer(Modifier.height(11.dp)); T(svc.name, 14, FontWeight.Bold, C.head, maxLines = 1)
            Spacer(Modifier.height(3.dp)); T(if (svc.point_type == "pickup_only") tr("نقطة استلام فقط", "Pickup point only") else tr("استلام وتسليم", "Pickup & delivery"), 11, FontWeight.Normal, C.muted, lineHeight = 16, maxLines = 2)
        }
    }
}

@Composable
private fun ServiceCard(svc: Service, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.height(152.dp).clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).clickable(onClick = onClick).padding(16.dp)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(svcGradients[svc.gradient]), contentAlignment = Alignment.Center) { Ic(svc.iconId, 24.dp, Color.White) }
            Spacer(Modifier.height(11.dp))
            T(svc.name, 14, FontWeight.Bold, C.head, maxLines = 1)
            Spacer(Modifier.height(3.dp))
            T(svc.desc, 11, FontWeight.Normal, C.muted, lineHeight = 17, maxLines = 2)
        }
        svc.tag?.let { Box(Modifier.align(Alignment.TopEnd).clip(CircleShape).background(if (svc.tagTerra) Color(0xFFF6ECE4) else Color(0xFFEEF4EF)).padding(horizontal = 9.dp, vertical = 3.dp)) { T(it, 9, FontWeight.ExtraBold, if (svc.tagTerra) C.terraText else C.green) } }
    }
}

@Composable
fun TransportServicesScreen(onBack: () -> Unit, onMenu: () -> Unit, onService: (com.matnokh.customer.net.SvcDto) -> Unit) {
    LaunchedEffect(Unit) { if (Repo.services.isEmpty()) runCatching { Repo.services = com.matnokh.customer.net.Net.api.services().services } }
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("خدمات النقل", "Transport services"), onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(6.dp))
            T(tr("اختر الخدمة التي تريدها لبدء طلب نقل", "Choose the service to start a transport request"), 12, FontWeight.Medium, C.muted, Modifier.padding(horizontal = 22.dp, vertical = 6.dp))
            Column(Modifier.padding(horizontal = 22.dp)) {
                Repo.services.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { svc -> SvcCard(svc, Modifier.weight(1f)) { onService(svc) } }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                if (Repo.services.isEmpty()) CenterHint(tr("لا توجد خدمات نقل مُفعّلة حالياً", "No transport services enabled currently"))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
