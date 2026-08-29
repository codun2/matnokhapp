package com.matnokh.driver.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.matnokh.driver.net.Net
import com.matnokh.driver.net.ProfileBody
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/* بلاطات جوجل مفعّلة الآن — أُلغيت طبقة OSM لأنها كانت تُبطّئ/تعلّق تحريك الخريطة */
@Composable
fun OsmTiles() { }

/* السحب للأسفل للتحديث */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Refreshable(onRefresh: suspend () -> Unit, content: @Composable () -> Unit) {
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val state = rememberPullRefreshState(refreshing, { scope.launch { refreshing = true; runCatching { onRefresh() }; refreshing = false } })
    Box(Modifier.fillMaxSize().pullRefresh(state)) {
        content()
        PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter).statusBarsPadding(), backgroundColor = C.card, contentColor = C.green)
    }
}

/* صورة المندوب المصغّرة (تظهر أينما وُجدت صورته) */
@Composable
fun MiniAvatar(url: String?, initials: String, size: Dp, corner: Dp, font: Int = 15) {
    if (!url.isNullOrBlank()) AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(size).clip(RoundedCornerShape(corner)))
    else Box(Modifier.size(size).clip(RoundedCornerShape(corner)).background(Grad.sand), contentAlignment = Alignment.Center) { T(initials, font, FontWeight.ExtraBold, Color(0xFF6B5335)) }
}

/* الصورة الشخصية القابلة للتغيير في شاشة حسابي */
@Composable
fun ProfileAvatar(toast: (String) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var url by remember { mutableStateOf(Drv.avatarUrl.value) }
    var busy by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            try {
                val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) { toast(tr("تعذّرت قراءة الصورة", "Couldn't read the image")); busy = false; return@launch }
                val part = MultipartBody.Part.createFormData("file", "avatar.jpg", bytes.toRequestBody("image/*".toMediaTypeOrNull()))
                val up = Net.api.upload(part)
                if (up.url != null) { Net.api.updateProfile(ProfileBody(avatar = up.url)); Drv.avatarUrl.value = up.url; url = up.url; toast(tr("تم تحديث صورتك ✓", "Your photo was updated ✓")) } else toast(tr("فشل الرفع", "Upload failed"))
            } catch (e: Exception) { toast(tr("تعذّر رفع الصورة", "Couldn't upload the image")) }
            busy = false
        }
    }
    Box(Modifier.size(56.dp).clip(RoundedCornerShape(19.dp)).background(Grad.sand).clickable(enabled = !busy) { picker.launch("image/*") }, contentAlignment = Alignment.Center) {
        when {
            busy -> CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            !url.isNullOrBlank() -> AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else -> T(Drv.avatar.value, 18, FontWeight.ExtraBold, Color(0xFF6B5335))
        }
        if (!busy) Box(Modifier.align(Alignment.BottomEnd).size(20.dp).clip(CircleShape).background(Grad.green), contentAlignment = Alignment.Center) { T("+", 13, FontWeight.Black, Color.White) }
    }
}
