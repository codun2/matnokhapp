package com.matnokh.tajer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/* شعار المتجر (يظهر أينما وُجد شعار التاجر). حالة قابلة للتفاعل تُحدَّث عند التحميل/الحفظ. */
object StoreInfo {
    val logo = mutableStateOf<String?>(null)
}

@Composable
fun StoreLogoBox(size: Dp, corner: Dp, emojiSize: Int = 20) {
    val url = StoreInfo.logo.value
    if (!url.isNullOrBlank()) {
        AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(size).clip(RoundedCornerShape(corner)))
    } else {
        Box(Modifier.size(size).clip(RoundedCornerShape(corner)).background(Grad.blue), contentAlignment = Alignment.Center) { Text("🛒", fontSize = emojiSize.sp) }
    }
}
