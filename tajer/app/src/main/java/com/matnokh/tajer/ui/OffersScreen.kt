package com.matnokh.tajer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.tajer.R
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.ProductDto
import com.matnokh.tajer.net.ProductsResp
import com.matnokh.tajer.net.call

@Composable
fun OffersScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    var data by remember { mutableStateOf<ProductsResp?>(null) }
    LaunchedEffect(Unit) { call({ Net.api.products() }, toast)?.let { data = it } }
    val d = data
    val offers = d?.products?.filter { it.price_before > 0.0 && it.price_before > it.price } ?: emptyList()

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(tr("العروض والخصومات", "Offers & discounts"), onBack, onMenu)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(16.dp))
            if (d != null && offers.isEmpty()) {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth(), PaddingValues(22.dp)) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            T(tr("لا توجد عروض حالياً", "No offers currently"), 13, FontWeight.Bold, C.head)
                            Spacer(Modifier.height(6.dp))
                            T(tr("لإضافة عرض: عدّل منتجاً وحدّد «السعر قبل الخصم» أعلى من السعر الحالي — يظهر هنا وللزبائن تلقائياً.", "To add an offer: edit a product and set «Price before discount» higher than the current price — it appears here and to customers automatically."),
                                11, FontWeight.Normal, C.muted, lineHeight = 18)
                        }
                    }
                }
            } else {
                Box(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp)) {
                    T(tr("منتجاتك التي عليها خصم حالياً (${offers.size})", "Your products currently on discount (${offers.size})"), 11, FontWeight.Medium, C.muted)
                }
                offers.forEach { p -> OfferRow(p) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OfferRow(p: ProductDto) {
    val pct = if (p.price_before > 0.0) (((p.price_before - p.price) / p.price_before) * 100).toInt() else 0
    OCard(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth(), PaddingValues(15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(C.pillLive), contentAlignment = Alignment.Center) {
                Ic(R.drawable.ic_zap, 22.dp, C.greenD)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                T(p.name, 13, FontWeight.Bold, C.text, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                T("﷼" + money(p.price) + tr(" · قبل ﷼", " · before ﷼") + money(p.price_before) + (p.section?.let { " · $it" } ?: ""), 11, FontWeight.Normal, C.muted, maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.clip(RoundedCornerShape(50)).background(Grad.terra).padding(horizontal = 10.dp, vertical = 4.dp)) {
                T(tr("−$pct٪", "−$pct%"), 11, FontWeight.Black, Color.White)
            }
        }
    }
}
