package com.matnokh.driver.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.matnokh.driver.R
import com.matnokh.driver.net.ChatMsg
import com.matnokh.driver.net.ChatSendBody
import com.matnokh.driver.net.Net
import com.matnokh.driver.net.call
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** دردشة المندوب مع الزبون — مربوطة بالطلب، تحديث كل ٣ ثوانٍ + دعم الصور. */
@Composable
fun ChatScreen(kind: String, orderId: Int, title: String, onBack: () -> Unit, onMenu: () -> Unit, toast: (String) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val msgs = remember { mutableStateListOf<ChatMsg>() }
    var locked by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    suspend fun poll() {
        val after = msgs.lastOrNull()?.id ?: 0
        call({ Net.api.chatShow(kind, orderId, after) }, { }) ?.let { r ->
            locked = r.locked
            if (r.messages.isNotEmpty()) {
                msgs.addAll(r.messages.filter { m -> msgs.none { it.id == m.id } })
                runCatching { listState.scrollToItem(0) }
            }
        }
    }

    LaunchedEffect(Unit) { while (true) { poll(); delay(3000) } }

    fun sendNow(body: String?, image: String?) {
        if (sending) return
        scope.launch {
            sending = true
            try { call({ Net.api.chatSend(kind, orderId, ChatSendBody(body, image)) }, toast)?.let { input = ""; poll() } }
            finally { sending = false }
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            uploading = true
            try {
                val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) { toast("تعذّرت قراءة الصورة"); uploading = false; return@launch }
                val part = MultipartBody.Part.createFormData("file", "chat.jpg", bytes.toRequestBody("image/*".toMediaTypeOrNull()))
                val up = call({ Net.api.upload(part) }, toast)
                up?.url?.let { sendNow(null, it) }
            } finally { uploading = false }
        }
    }

    Column(Modifier.fillMaxSize().background(C.bg)) {
        ScreenHeader(title, onBack, onMenu)
        LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), state = listState, reverseLayout = true, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Spacer(Modifier.height(4.dp)) }
            items(msgs.reversed(), key = { it.id }) { m ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.mine) Arrangement.End else Arrangement.Start) {
                    Column(
                        Modifier.widthIn(max = 290.dp).clip(RoundedCornerShape(16.dp))
                            .background(if (m.mine) Color(0xFFDCEFE5) else C.card)
                            .border(1.dp, if (m.mine) Color(0xFFC4E2D2) else C.line, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        m.image?.takeIf { it.isNotBlank() }?.let {
                            AsyncImage(model = it, contentDescription = null, contentScale = ContentScale.Crop,
                                modifier = Modifier.size(190.dp).clip(RoundedCornerShape(12.dp)))
                            Spacer(Modifier.height(4.dp))
                        }
                        m.body?.takeIf { it.isNotBlank() }?.let { T(it, 13, FontWeight.Medium, C.head) }
                        T(m.at ?: "", 9, FontWeight.Normal, C.muted)
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
        if (locked) {
            Box(Modifier.fillMaxWidth().background(C.card).padding(14.dp), contentAlignment = Alignment.Center) {
                T("انتهى الطلب — المحادثة مقفلة", 12, FontWeight.Bold, C.muted)
            }
        } else {
            Row(Modifier.fillMaxWidth().background(C.card).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(CircleShape).background(C.card2).clickable(enabled = !uploading) { picker.launch("image/*") }, contentAlignment = Alignment.Center) {
                    if (uploading) T("…", 14, FontWeight.Bold, C.muted) else Ic(R.drawable.ic_img, 18.dp, Color(0xFF5D6B62))
                }
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f).clip(RoundedCornerShape(21.dp)).background(C.bg).border(1.dp, C.line, RoundedCornerShape(21.dp)).padding(horizontal = 14.dp, vertical = 11.dp)) {
                    if (input.isEmpty()) T("اكتب رسالتك…", 13, FontWeight.Normal, C.muted)
                    BasicTextField(value = input, onValueChange = { input = it }, textStyle = TextStyle(fontSize = 13.sp, color = C.head), maxLines = 4, modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(42.dp).clip(CircleShape).background(Grad.green).clickable(enabled = !sending && input.isNotBlank()) { sendNow(input.trim(), null) }, contentAlignment = Alignment.Center) {
                    Ic(R.drawable.ic_back, 18.dp, Color.White)
                }
            }
        }
    }
}
