package com.matnokh.tajer.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.matnokh.tajer.net.DocumentDto
import com.matnokh.tajer.net.Net
import com.matnokh.tajer.net.call
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun DocumentsScreen(onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var docs by remember { mutableStateOf<List<DocumentDto>?>(null) }
    var complete by remember { mutableStateOf(true) }
    var busyId by remember { mutableStateOf(-1) }

    suspend fun load() { call({ Net.api.documents() }, toast)?.let { docs = it.documents; complete = it.complete } }
    LaunchedEffect(Unit) { load() }

    fun submit(typeId: Int, value: String) = scope.launch {
        call({ Net.api.submitDocument(mapOf("document_type_id" to typeId.toString(), "value" to value)) }, toast)?.let {
            toast(it.message ?: "تم الحفظ"); load()
        }
    }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader("الوثائق", onBack, onMenu)
        val list = docs
        if (list == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = C.green) }; return@Column }

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Box(Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(if (complete) Color(0xFFEEF4EF) else Color(0xFFFDF6EC))
                    .padding(horizontal = 14.dp, vertical = 12.dp)) {
                    T(if (complete) "✓ اكتملت الوثائق المطلوبة" else "أكمل رفع الوثائق المطلوبة لاعتماد متجرك",
                        11, FontWeight.Bold, if (complete) C.greenD else Color(0xFF8A6A3F))
                }
            }
            items(list) { d -> DocCard(d, busy = busyId == d.id, onText = { v -> submit(d.id, v) }, onPickFile = {
                busyId = d.id
                // سيُطلق المنتقي من داخل DocCard
            }, ctx = ctx, scope = scope, toast = toast, onUploaded = { url -> busyId = -1; submit(d.id, url) }, onBusyEnd = { busyId = -1 }) }
        }
    }
}

@Composable
private fun DocCard(
    d: DocumentDto, busy: Boolean,
    onText: (String) -> Unit, onPickFile: () -> Unit,
    ctx: android.content.Context, scope: kotlinx.coroutines.CoroutineScope, toast: (String) -> Unit,
    onUploaded: (String) -> Unit, onBusyEnd: () -> Unit,
) {
    var text by remember { mutableStateOf(d.value ?: "") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) { onBusyEnd(); return@rememberLauncherForActivityResult }
        scope.launch {
            try {
                val bytes = ctx.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
                val part = MultipartBody.Part.createFormData("file", "doc_${d.key}.jpg", bytes.toRequestBody("*/*".toMediaTypeOrNull()))
                val r = Net.api.upload(part)
                onUploaded(r.url)
            } catch (e: retrofit2.HttpException) { toast(com.matnokh.tajer.net.errorMessage(e) ?: "تعذّر الرفع"); onBusyEnd() }
            catch (e: Exception) { toast("تعذّر رفع الملف"); onBusyEnd() }
        }
    }

    OCard(Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp).fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(C.card2), contentAlignment = Alignment.Center) {
                Ic(if (d.field == "text") R.drawable.ic_cash else R.drawable.ic_doc, 20.dp, C.greenD)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    T(d.name, 13, FontWeight.Bold, C.head, Modifier.weight(1f, fill = false), maxLines = 1)
                    if (d.required) { Spacer(Modifier.width(6.dp)); Box(Modifier.clip(RoundedCornerShape(50.dp)).background(C.redBg).padding(horizontal = 7.dp, vertical = 2.dp)) { T("مطلوب", 9, FontWeight.ExtraBold, C.redText, maxLines = 1) } }
                }
                if (d.description != null) { Spacer(Modifier.height(2.dp)); T(d.description, 10, FontWeight.Normal, C.muted, lineHeight = 16) }
            }
            DocStatus(d.status)
        }
        Spacer(Modifier.height(10.dp))
        if (d.field == "text") {
            FinField(text, { text = it }, "أدخل القيمة (مثال: SA00 0000 …)")
            Spacer(Modifier.height(8.dp))
            WideButton("حفظ", R.drawable.ic_check) { if (text.isNotBlank()) onText(text) }
        } else {
            WideButton(if (busy) "جارٍ الرفع…" else if (d.value != null) "تغيير الملف" else "رفع الملف", R.drawable.ic_img, ghost = d.value != null) {
                if (!busy) { onPickFile(); picker.launch("*/*") }
            }
        }
    }
}

@Composable
private fun DocStatus(status: String) {
    val (label, kind) = when (status) {
        "approved" -> "معتمد" to PillKind.Live
        "pending" -> "قيد المراجعة" to PillKind.Wait
        "rejected" -> "مرفوض" to PillKind.Rj
        else -> "لم تُرفع" to PillKind.Off
    }
    StatusPill(label, kind)
}
