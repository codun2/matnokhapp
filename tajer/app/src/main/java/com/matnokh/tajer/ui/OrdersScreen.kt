@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.matnokh.tajer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matnokh.tajer.R
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.OrderDetailResp
import com.matnokh.tajer.net.OrderRow
import com.matnokh.tajer.net.call
import kotlinx.coroutines.launch

// وضع التجهيز (يُحدّث من إعدادات المتجر)
var needsPrep by mutableStateOf(true)

@Composable
fun OrdersScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf("open") }
    var orders by remember { mutableStateOf<List<OrderRow>?>(null) }
    var detail by remember { mutableStateOf<OrderDetailResp?>(null) }
    var chatFor by remember { mutableStateOf<Pair<Int, String>?>(null) }

    suspend fun load() { orders = null; call({ Net.api.orders(tab) }, toast)?.let { orders = it.orders } }
    LaunchedEffect(tab) { load() }
    // شريط الوضع يعكس إعداد المتجر الفعلي
    LaunchedEffect(Unit) { call({ Net.api.store() }, toast)?.let { needsPrep = it.store.prep_mode } }
    // فتح تفاصيل الطلب تلقائياً عند القدوم من إشعار
    LaunchedEffect(Unit) {
        com.matnokh.tajer.net.NotificationBus.pendingOrderId?.let { id ->
            com.matnokh.tajer.net.NotificationBus.pendingOrderId = null
            call({ Net.api.orderDetail(id) }, toast)?.let { detail = it }
        }
    }

    fun act(block: suspend () -> Unit) = scope.launch { block(); load() }

    val chatCo = chatFor
    if (chatCo != null) { ChatScreen(chatCo.first, "محادثة ${chatCo.second}", { chatFor = null }, onMenu, toast); return }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("طلبات المتجر", onBack, onMenu)

        Box(Modifier.padding(horizontal = 22.dp)) {
            if (needsPrep) ModeBar(R.drawable.ic_clock, true, "وضع التجهيز مفعّل", "— تقبل الطلب وتجهّزه ثم يُبثّ للمناديب")
            else ModeBar(R.drawable.ic_zap, false, "قبول تلقائي", "— الطلبات جاهزة فوراً وتُبثّ للمناديب مباشرة")
        }

        Row(Modifier.padding(start = 22.dp, end = 22.dp, top = 14.dp).fillMaxWidth().clip(CircleShape).background(C.card)
            .border(1.dp, C.line, CircleShape).padding(5.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("open" to "الجارية", "done" to "المكتملة").forEach { (k, lbl) ->
                val on = tab == k
                Box(Modifier.weight(1f).clip(CircleShape).then(if (on) Modifier.background(Grad.green) else Modifier)
                    .clickable { tab = k }.padding(vertical = 9.dp), contentAlignment = Alignment.Center) {
                    T(lbl, 11, FontWeight.ExtraBold, if (on) Color.White else C.muted)
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        val list = orders
        if (list == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }; return@Column }

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
            if (list.isEmpty()) item {
                Box(Modifier.padding(horizontal = 22.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(C.card)
                    .border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(28.dp), contentAlignment = Alignment.Center) {
                    T("لا توجد طلبات في هذا التبويب", 12, FontWeight.Normal, C.muted)
                }
            }
            items(list) { o ->
                OrderCard(o,
                    onOpen = { scope.launch { call({ Net.api.orderDetail(o.id) }, toast)?.let { detail = it } } },
                    onAccept = { act { call({ Net.api.acceptOrder(o.id) }, toast)?.let { toast(it.message ?: "") } } },
                    onReject = { act { call({ Net.api.rejectOrder(o.id) }, toast)?.let { toast(it.message ?: "") } } },
                    onReady = { act { call({ Net.api.readyOrder(o.id) }, toast)?.let { toast(it.message ?: "") } } })
            }
            item {
                Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 2.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFDF6EC)).border(1.dp, Color(0xFFECDCC3), RoundedCornerShape(18.dp)).padding(horizontal = 16.dp, vertical = 13.dp)) {
                    T("التفاوض على السعر لا يشملك", 12, FontWeight.ExtraBold, Color(0xFF75552E))
                    Spacer(Modifier.height(3.dp))
                    T("سعر المنتجات ثابت من متجرك. بعد تجهيز الطلب يُبثّ للمناديب القريبين، ويتفاوضون مع الزبون على أجرة التوصيل فقط — والزبون يختار العرض المناسب. عند استلام المندوب الطلب تُموَّل محفظتك بقيمة المنتجات.",
                        11, FontWeight.Medium, Color(0xFF8A6A3F), lineHeight = 20)
                }
            }
        }
    }

    detail?.let { OrderDetailDialog(it, onClose = { detail = null }, onChat = { d -> detail = null; chatFor = d.order.id to d.order.customer }) }
}

@Composable
private fun ModeBar(iconId: Int, prep: Boolean, bold: String, rest: String) {
    Row(Modifier.padding(top = 14.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
        .background(if (prep) Color(0xFFF9F1E9) else Color(0xFFEEF4EF))
        .border(1.dp, if (prep) Color(0xFFECDCC3) else Color(0xFFCFE0D4), RoundedCornerShape(16.dp))
        .padding(horizontal = 15.dp, vertical = 12.dp), verticalAlignment = Alignment.Top) {
        Ic(iconId, 17.dp, if (prep) Color(0xFF8A6A3F) else C.greenD, Modifier.padding(top = 2.dp))
        Spacer(Modifier.width(9.dp))
        FlowRow(verticalArrangement = Arrangement.Center) {
            T(bold, 11, FontWeight.Black, if (prep) Color(0xFF8A6A3F) else C.greenD)
            T(" $rest", 11, FontWeight.Bold, if (prep) Color(0xFF8A6A3F) else C.greenD, lineHeight = 20)
        }
    }
}

@Composable
private fun OrderCard(o: OrderRow, onOpen: () -> Unit, onAccept: () -> Unit, onReject: () -> Unit, onReady: () -> Unit) {
    val (lbl, kind) = orderStatus(o.status)
    val st = o.status
    val iconId = when (st) { "withdriver" -> R.drawable.ic_van; "done" -> R.drawable.ic_check; else -> R.drawable.ic_list }
    val bg = when (st) { "new" -> Color(0xFFF6ECE4); "done", "withdriver" -> Color(0xFFE9F0F4); else -> Color(0xFFE7EFE9) }
    val col = when (st) { "new" -> Color(0xFFB5794F); "done", "withdriver" -> C.blueText; else -> C.greenD }

    Column(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth()
        .clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp))
        .clickable(onClick = onOpen).padding(15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(bg), contentAlignment = Alignment.Center) { Ic(iconId, 22.dp, col) }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                T("طلب ${o.customer}", 13, FontWeight.Bold, C.text, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                T("#${(o.order_no ?: o.id.toString()).substringAfterLast("-")} · ${o.items_count} أصناف · ﷼${o.total.toInt()} · ${payLabel(o.payment_method)}", 11, FontWeight.Normal, C.muted, maxLines = 1)
                T(listOfNotNull(o.branch, o.dt, o.driver).joinToString(" · "), 11, FontWeight.Normal, C.muted, maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            StatusPill(lbl, kind)
        }
        when (st) {
            "new" -> ActionRow {
                ActBtn("قبول وتجهيز", R.drawable.ic_check, ActKind.Ok, Modifier.weight(1f), onAccept)
                ActBtn("رفض", R.drawable.ic_x, ActKind.Rj, Modifier.weight(1f), onReject)
            }
            "prep" -> ActionRow { ActBtn("جاهز — أبلغ المناديب", R.drawable.ic_check, ActKind.Ok, Modifier.weight(1f), onReady) }
            "ready" -> ActionRow { ActBtn("بُثّ للمناديب — بانتظار من يستلمه", R.drawable.ic_clock, ActKind.Ghost, Modifier.weight(1f)) {} }
        }
    }
}

private fun payLabel(m: String?): String = when (m) { "card", "tap" -> "بطاقة ✓"; "cash" -> "نقداً"; else -> m ?: "—" }

@Composable
private fun OrderDetailDialog(d: OrderDetailResp, onClose: () -> Unit, onChat: ((OrderDetailResp) -> Unit)? = null) {
    Box(Modifier.fillMaxSize().background(Color(0x80253A34)).clickable(onClick = onClose), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(20.dp).fillMaxWidth().widthIn(max = 360.dp).heightIn(max = 620.dp)
            .clip(RoundedCornerShape(24.dp)).background(C.bg).clickable(enabled = false) {}) {
            // رأس
            Column(Modifier.fillMaxWidth().background(Grad.green).padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        T("طلب #${(d.order.order_no ?: d.order.id.toString()).substringAfterLast("-")}", 15, FontWeight.Black, Color.White)
                        T(d.order.customer, 12, FontWeight.Bold, Color.White.copy(alpha = .9f))
                    }
                    val (lbl, _) = orderStatus(d.order.status)
                    Box(Modifier.clip(CircleShape).background(Color.White.copy(alpha = .22f)).padding(horizontal = 11.dp, vertical = 5.dp)) { T(lbl, 10, FontWeight.ExtraBold, Color.White) }
                }
                d.order.drop_address?.let { Spacer(Modifier.height(6.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Ic(R.drawable.ic_pin, 13.dp, Color.White); Spacer(Modifier.width(5.dp)); T(it, 11, FontWeight.Medium, Color.White.copy(alpha = .9f), maxLines = 1) } }
            }
            // معلومات الطلب + الاتصال بالزبون
            OrderInfoBar(d, onChat)
            // العناصر
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 14.dp)) {
                T("عناصر الطلب (${d.items.size})", 12, FontWeight.ExtraBold, C.head)
                Spacer(Modifier.height(8.dp))
                d.items.forEach { it ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(C.pillLive), contentAlignment = Alignment.Center) { T("${it.qty}×", 11, FontWeight.Black, C.greenD) }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            T(it.name, 12, FontWeight.Bold, C.head)
                            if (it.addons.isNotEmpty()) T(it.addons.joinToString("، ") { a -> "${a.name} +﷼${money(a.price)}" }, 10, FontWeight.Normal, C.muted, maxLines = 2)
                        }
                        T("﷼${it.line_total.toInt()}", 12, FontWeight.Black, C.greenD)
                    }
                    Line()
                }
                Spacer(Modifier.height(10.dp))
                SumRow("قيمة الأصناف", d.order.items_total)
                if (d.order.discount > 0) SumRow("خصم", -d.order.discount)
                SumRow("رسوم التوصيل", d.order.delivery_fee)
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    T("الإجمالي", 14, FontWeight.Black, C.head, Modifier.weight(1f))
                    T("﷼${d.order.total.toInt()}", 15, FontWeight.Black, C.greenD)
                }
            }
            Box(Modifier.fillMaxWidth().clickable(onClick = onClose).padding(14.dp), contentAlignment = Alignment.Center) { T("إغلاق", 13, FontWeight.Bold, C.muted) }
        }
    }
}

@Composable
private fun OrderInfoBar(d: OrderDetailResp, onChat: ((OrderDetailResp) -> Unit)? = null) {
    val ctx = LocalContext.current
    val o = d.order
    Column(Modifier.fillMaxWidth().background(C.card).padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Ic(R.drawable.ic_pin, 14.dp, C.green); Spacer(Modifier.width(6.dp))
            T(o.branch ?: "—", 11, FontWeight.Bold, C.head, Modifier.weight(1f), maxLines = 1)
            o.dt?.let { T(it, 10, FontWeight.Medium, C.muted) }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Ic(R.drawable.ic_cash, 14.dp, C.green); Spacer(Modifier.width(6.dp))
            T(payLabel(o.payment_method), 11, FontWeight.Bold, C.head, Modifier.weight(1f))
            Box(Modifier.clip(CircleShape).background(if (o.is_paid) C.pillLive else C.pillWait).padding(horizontal = 10.dp, vertical = 4.dp)) {
                T(if (o.is_paid) "مدفوع ✓" else "غير مدفوع", 9, FontWeight.ExtraBold, if (o.is_paid) C.greenD else C.terraText)
            }
        }
        o.phone?.takeIf { it.isNotBlank() }?.let { phone ->
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFEEF4EF))
                .clickable { runCatching { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) } }
                .padding(vertical = 11.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Ic(R.drawable.ic_phone, 15.dp, C.greenD); Spacer(Modifier.width(7.dp))
                T("اتصال بالزبون · $phone", 12, FontWeight.ExtraBold, C.greenD)
            }
        }
        if (onChat != null) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFEEF4EF))
                .clickable { onChat(d) }
                .padding(vertical = 11.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Ic(R.drawable.ic_msg, 15.dp, C.greenD); Spacer(Modifier.width(7.dp))
                T("محادثة الزبون", 12, FontWeight.ExtraBold, C.greenD)
            }
        }
    }
}

@Composable
private fun SumRow(label: String, amount: Double) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        T(label, 11, FontWeight.Medium, C.muted, Modifier.weight(1f))
        T("﷼${amount.toInt()}", 11, FontWeight.Bold, C.head)
    }
}

@Composable
private fun Line() {
    androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(Color(0xFFF0ECE3), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 1f)
    }
}

private enum class ActKind { Ok, Rj, Ghost }

@Composable
private fun ActionRow(content: @Composable RowScope.() -> Unit) {
    Column {
        Spacer(Modifier.height(12.dp))
        androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(1.dp)) { drawLine(Color(0xFFE8E3D9), androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 1f) }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun ActBtn(label: String, iconId: Int, kind: ActKind, modifier: Modifier, onClick: () -> Unit) {
    val row = when (kind) {
        ActKind.Ok -> modifier.clip(RoundedCornerShape(14.dp)).background(Grad.green)
        ActKind.Rj -> modifier.clip(RoundedCornerShape(14.dp)).background(C.redBg)
        ActKind.Ghost -> modifier.clip(RoundedCornerShape(14.dp)).background(Color(0xFFFAF8F4)).border(1.dp, C.line, RoundedCornerShape(14.dp))
    }
    val fg = when (kind) { ActKind.Ok -> Color.White; ActKind.Rj -> C.redText; ActKind.Ghost -> Color(0xFF5D6B62) }
    Row(row.clickable(onClick = onClick).padding(vertical = 11.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Ic(iconId, 15.dp, fg); Spacer(Modifier.width(6.dp)); T(label, 12, FontWeight.ExtraBold, fg, maxLines = 1)
    }
}
