package com.matnokh.tajer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// عنوان حقل fl + شارة "مطلوب"
@Composable
fun FieldLabel(text: String, required: Boolean = false, iconId: Int? = null) {
    Row(
        Modifier.padding(top = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconId != null) { Ic(iconId, 14.dp, C.head); Spacer(Modifier.width(6.dp)) }
        T(text, 11, FontWeight.ExtraBold, C.head)
        if (required) {
            Spacer(Modifier.width(6.dp))
            Box(Modifier.clip(CircleShape).background(C.redBg).padding(horizontal = 7.dp, vertical = 2.dp)) {
                T("مطلوب", 9, FontWeight.ExtraBold, C.redText)
            }
        }
    }
}

// حقل إدخال fin
@Composable
fun FinField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboard: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minHeight: Dp = 46.dp,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.Right,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = LocalTextStyle.current.copy(
            fontFamily = Cairo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = C.text, textAlign = align,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        cursorBrush = SolidColor(C.green),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                Modifier.fillMaxWidth().heightIn(min = minHeight)
                    .clip(RoundedCornerShape(14.dp)).background(Color(0xFFFAF8F4))
                    .border(1.dp, C.line, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
            ) {
                if (value.isEmpty()) T(placeholder, 13, FontWeight.Medium, Color(0xFFB6BDB4))
                inner()
            }
        },
    )
}

// شريحة اختيار chip
@Composable
fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(CircleShape)
            .background(if (selected) Color(0xFFEEF4EF) else Color(0xFFFAF8F4))
            .border(1.5.dp, if (selected) C.green else C.line, CircleShape)
            .clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 8.dp),
    ) { T(label, 11, FontWeight.ExtraBold, if (selected) C.greenD else C.muted, maxLines = 1) }
}

// أيقونة emoji قابلة للاختيار
@Composable
fun Emo(emoji: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(46.dp).clip(RoundedCornerShape(15.dp))
            .background(if (selected) Color(0xFFEEF4EF) else Color(0xFFFAF8F4))
            .border(1.5.dp, if (selected) C.green else C.line, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(emoji, fontSize = 22.sp) }
}

// عنوان قسم داخلي oc-title
@Composable
fun OcTitle(iconId: Int, title: String, required: Boolean = false) {
    Row(Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Ic(iconId, 18.dp, C.green)
        Spacer(Modifier.width(8.dp))
        T(title, 13, FontWeight.ExtraBold, Color(0xFF4B5A51))
        if (required) {
            Spacer(Modifier.width(6.dp))
            Box(Modifier.clip(CircleShape).background(C.redBg).padding(horizontal = 7.dp, vertical = 2.dp)) {
                T("مطلوب", 9, FontWeight.ExtraBold, C.redText)
            }
        }
    }
}

// زر عريض o-btn
@Composable
fun WideButton(label: String, iconId: Int? = null, ghost: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp))
            .then(if (ghost) Modifier.background(C.card).border(1.dp, C.line, RoundedCornerShape(17.dp)) else Modifier.background(Grad.green))
            .clickable(onClick = onClick).padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
    ) {
        T(label, 15, FontWeight.ExtraBold, if (ghost) Color(0xFF5D6B62) else Color.White)
        if (iconId != null) { Spacer(Modifier.width(8.dp)); Ic(iconId, 17.dp, if (ghost) Color(0xFF5D6B62) else Color.White) }
    }
}

// ترويسة o-head (رجوع + عنوان + زر قائمة)
@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, onMenu: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderSquare(com.matnokh.tajer.R.drawable.ic_back, 42.dp, 14.dp, onBack)
        Spacer(Modifier.width(10.dp))
        T(title, 18, FontWeight.ExtraBold, C.head, Modifier.weight(1f), maxLines = 1)
        if (trailing != null) { trailing(); Spacer(Modifier.width(9.dp)) }
        HeaderSquare(com.matnokh.tajer.R.drawable.ic_menu, 44.dp, 15.dp, onMenu)
    }
}

@Composable
fun HeaderSquare(iconId: Int, size: Dp, corner: Dp, onClick: () -> Unit) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(corner)).background(C.card)
            .border(1.dp, C.line, RoundedCornerShape(corner)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Ic(iconId, 17.dp, Color(0xFF5D6B62)) }
}

// صف قائمة/فرع lrow
@Composable
fun ListRow(
    leading: @Composable () -> Unit,
    title: String,
    subtitle: String,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        Modifier.padding(start = 22.dp, end = 22.dp, bottom = 10.dp).fillMaxWidth()
            .clip(RoundedCornerShape(22.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(22.dp)).padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            T(title, 13, FontWeight.Bold, C.head, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            T(subtitle, 10, FontWeight.Normal, C.muted, lineHeight = 17)
        }
        trailing()
    }
}

fun money(v: Double): String {
    val r = Math.round(v * 100.0) / 100.0
    return if (r % 1.0 == 0.0) r.toLong().toString() else r.toString().trimEnd('0').trimEnd('.')
}
fun decInput(s: String): String {
    val f = s.filter { it.isDigit() || it == '.' }
    val i = f.indexOf('.')
    return if (i < 0) f else f.substring(0, i + 1) + f.substring(i + 1).replace(".", "")
}
