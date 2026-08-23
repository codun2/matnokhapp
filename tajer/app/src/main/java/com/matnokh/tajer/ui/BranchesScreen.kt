@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.matnokh.tajer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.matnokh.tajer.R
import com.matnokh.tajer.net.BranchBody
import com.matnokh.tajer.net.BranchDto
import com.matnokh.tajer.net.BranchUpdate
import com.matnokh.tajer.net.CityDto
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.call
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun BranchesScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var branches by remember { mutableStateOf<List<BranchDto>?>(null) }
    var cities by remember { mutableStateOf<List<CityDto>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var cityIdx by remember { mutableStateOf(0) }
    var phone by remember { mutableStateOf("") }
    var from by remember { mutableStateOf("08:00") }
    var to by remember { mutableStateOf("22:00") }
    var loc by remember { mutableStateOf<Offset?>(null) }
    var latLng by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var suggestName by remember { mutableStateOf<String?>(null) }

    suspend fun load() { call({ Net.api.branches() }, toast)?.let { branches = it.branches } }
    LaunchedEffect(Unit) {
        call({ Net.api.cities() }, toast)?.let { cities = it.cities }
        load()
    }

    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("فروع المتجر", onBack, onMenu)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                OCard(Modifier.padding(horizontal = 22.dp).fillMaxWidth()) {
                    OcTitle(R.drawable.ic_plus, "إضافة فرع جديد")
                    FieldLabel("اسم الفرع", required = true)
                    FinField(name, { name = it }, "مثال: فرع البيرة")
                    FieldLabel("المدينة")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        cities.forEachIndexed { i, c -> Chip(c.name, cityIdx == i) { cityIdx = i; suggestName = nextBranchName(c.name, (branches ?: emptyList()).map { it.name }) } }
                    }
                    FieldLabel("هاتف الفرع")
                    FinField(phone, { phone = it }, "+9665xxxxxxxx", keyboard = KeyboardType.Phone)
                    FieldLabel("أوقات الدوام")
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Box(Modifier.weight(1f)) { FinField(from, { from = it }, keyboard = KeyboardType.Number) }
                        Box(Modifier.weight(1f)) { FinField(to, { to = it }, keyboard = KeyboardType.Number) }
                    }
                    FieldLabel("موقع الفرع على الخريطة", required = true)
                    BranchMapPick(latLng?.first, latLng?.second) { la, ln -> latLng = la to ln }
                    Spacer(Modifier.height(9.dp))
                    LocBox(latLng)
                    Spacer(Modifier.height(14.dp))
                    WideButton("أضف الفرع", R.drawable.ic_plus) {
                        val n = name.trim()
                        if (n.isEmpty()) { toast("اكتب اسم الفرع"); return@WideButton }
                        if (latLng == null) { toast("حدّد موقع الفرع على الخريطة"); return@WideButton }
                        val cid = cities.getOrNull(cityIdx)?.id
                        scope.launch {
                            call({ Net.api.addBranch(BranchBody(n, cid, phone.ifBlank { null }, "$from - $to", latLng!!.first, latLng!!.second)) }, toast)?.let {
                                name = ""; phone = ""; loc = null; latLng = null; toast(it.message ?: "أُضيف الفرع ✓"); load()
                            }
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    T("الفروع الحالية", 16, FontWeight.ExtraBold, C.head, Modifier.weight(1f))
                    branches?.let { StatusPill("${it.size}", PillKind.Live) }
                }
            }
            val list = branches
            if (list == null) item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) } }
            else items(list) { b ->
                ListRow(
                    leading = { Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(C.card2), contentAlignment = Alignment.Center) { Ic(R.drawable.ic_pin, 22.dp, C.greenD) } },
                    title = b.name + if (b.is_main) " (رئيسي)" else "",
                    subtitle = listOfNotNull(b.city, b.hours).joinToString(" · ") + (b.phone?.let { "\n$it" } ?: ""),
                    trailing = {
                        Sw(b.is_active) { scope.launch { call({ Net.api.updateBranch(b.id, BranchUpdate(is_active = !b.is_active)) }, toast)?.let { toast("تم تحديث حالة الفرع"); load() } } }
                        if (!b.is_main) {
                            Spacer(Modifier.width(8.dp))
                            Box(Modifier.size(34.dp).clip(RoundedCornerShape(12.dp)).background(C.redBg)
                                .clickable { scope.launch { call({ Net.api.deleteBranch(b.id) }, toast)?.let { toast(it.message ?: "حُذف الفرع"); load() } } },
                                contentAlignment = Alignment.Center) { Ic(R.drawable.ic_x, 15.dp, C.redText) }
                        }
                    },
                )
            }
        }
    }
    suggestName?.let { sug ->
        ConfirmNameDialog(sug, onYes = { name = sug; suggestName = null }, onNo = { suggestName = null })
    }
    }
}

@Composable
private fun ConfirmNameDialog(suggested: String, onYes: () -> Unit, onNo: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0x80253A34)).clickable(onClick = onNo), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(24.dp).widthIn(max = 320.dp).fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(C.bg).clickable(enabled = false) {}) {
            Column(Modifier.fillMaxWidth().background(Grad.green).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Ic(R.drawable.ic_pin, 26.dp, Color.White)
                Spacer(Modifier.height(6.dp)); T("تسمية الفرع", 15, FontWeight.Black, Color.White)
            }
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                T("هل تريد تسمية الفرع باسم:", 12, FontWeight.Bold, C.muted)
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF2F8F3)).border(1.dp, Color(0xFFCFE0D4), RoundedCornerShape(14.dp)).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    T("\u00AB$suggested\u00BB", 16, FontWeight.Black, C.greenD)
                }
                Spacer(Modifier.height(16.dp))
                WideButton("نعم، سمِّه", R.drawable.ic_check, onClick = onYes)
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().clickable(onClick = onNo).padding(11.dp), contentAlignment = Alignment.Center) { T("لا، سأكتبه بنفسي", 13, FontWeight.Bold, C.muted) }
            }
        }
    }
}

@Composable
private fun BranchMapPick(lat: Double?, lng: Double?, onPick: (Double, Double) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(lat ?: 24.7136, lng ?: 46.6753), if (lat != null) 16f else 10f) }
    fun useHere() { scope.launch { currentLatLng(ctx)?.let { onPick(it.first, it.second); camera.position = CameraPosition.fromLatLngZoom(LatLng(it.first, it.second), 16f) } } }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { g -> granted = g; if (g) useHere() }
    Column {
        Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp))) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = camera,
                properties = MapProperties(isMyLocationEnabled = granted),
                uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false, mapToolbarEnabled = false),
                onMapClick = { ll -> onPick(ll.latitude, ll.longitude) },
            ) {
                if (lat != null && lng != null) Marker(state = MarkerState(LatLng(lat, lng)), title = "موقع الفرع")
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF4EF))
                .clickable { if (granted) useHere() else launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
                .padding(horizontal = 14.dp, vertical = 9.dp),
        ) { T("📍 استخدام موقعي الحالي", 11, FontWeight.ExtraBold, C.greenD) }
    }
}

@Composable
private fun LocBox(latLng: Pair<Double, Double>?) {
    val set = latLng != null
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
            .background(if (set) Color(0xFFF2F8F3) else Color(0xFFFAF8F4))
            .border(1.dp, if (set) Color(0xFFCFE0D4) else C.line, RoundedCornerShape(13.dp))
            .padding(horizontal = 13.dp, vertical = 10.dp),
    ) {
        if (set) T("✓ حُدّد الموقع — ${"%.4f".format(latLng!!.first)}, ${"%.4f".format(latLng.second)}", 11, FontWeight.Bold, C.head)
        else T("اضغط على الخريطة لتحديد موقع الفرع", 11, FontWeight.Bold, C.muted)
    }
}

// ── تسمية الفرع تلقائياً حسب المدينة مع الترقيم ──
private fun arabicDigitToInt(c: Char): Int? = when (c) {
    in '0'..'9' -> c - '0'
    in '\u0660'..'\u0669' -> c - '\u0660'
    else -> null
}

private fun toArabicDigits(n: Int): String =
    n.toString().map { if (it in '0'..'9') ('\u0660' + (it - '0')) else it }.joinToString("")

/** "فرع جدة" أول مرة، ثم "فرع جدة ١"، "فرع جدة ٢"… بناءً على الفروع الموجودة. */
private fun nextBranchName(city: String, existing: List<String>): String {
    val base = "فرع $city"
    var maxNum = 0
    var baseExists = false
    for (raw in existing) {
        val nm = raw.trim()
        if (nm == base) { baseExists = true; continue }
        if (nm.startsWith("$base ")) {
            val suffix = nm.removePrefix("$base ").trim()
            val digits = suffix.mapNotNull { arabicDigitToInt(it) }
            if (suffix.isNotEmpty() && digits.size == suffix.length) {
                val num = digits.fold(0) { acc, d -> acc * 10 + d }
                if (num > maxNum) maxNum = num
            }
        }
    }
    return if (!baseExists && maxNum == 0) base else "$base ${toArabicDigits(maxNum + 1)}"
}
