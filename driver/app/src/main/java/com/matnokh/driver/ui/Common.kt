package com.matnokh.driver.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── أيقونة SVG ──
@Composable
fun Ic(id: Int, size: Dp = 22.dp, tint: Color = C.text, modifier: Modifier = Modifier) {
    Icon(painter = painterResource(id), contentDescription = null, tint = tint, modifier = modifier.size(size))
}

// ── نص Cairo سريع ──
@Composable
fun T(
    text: String,
    size: Int = 13,
    weight: FontWeight = FontWeight.Normal,
    color: Color = C.text,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: Int = 0,
) = Text(
    text = text,
    fontFamily = Cairo,
    fontSize = size.sp,
    fontWeight = weight,
    color = color,
    modifier = modifier,
    maxLines = maxLines,
    overflow = TextOverflow.Ellipsis,
    lineHeight = if (lineHeight > 0) lineHeight.sp else androidx.compose.ui.unit.TextUnit.Unspecified,
)

// ── بطاقة o-card ──
@Composable
fun OCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(C.card)
            .border(1.dp, C.line, RoundedCornerShape(22.dp))
            .padding(padding),
        content = content,
    )
}

// ── شارة الحالة st ──
@Composable
fun StatusPill(label: String, kind: PillKind, modifier: Modifier = Modifier) {
    val (bg, fg, border) = when (kind) {
        PillKind.Live -> Triple(C.pillLive, C.greenD, null)
        PillKind.Ok   -> Triple(C.pillOk, C.blueText, null)
        PillKind.Wait -> Triple(C.pillWait, C.terraText, null)
        PillKind.Off  -> Triple(C.pillOff, Color(0xFF9AA198), C.line)
        PillKind.Rj   -> Triple(C.redBg, C.redText, null)
    }
    Box(
        modifier
            .clip(CircleShape)
            .background(bg)
            .then(if (border != null) Modifier.border(1.dp, border, CircleShape) else Modifier)
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) { T(label, 10, FontWeight.ExtraBold, fg) }
}

// ── المفتاح sw ──
@Composable
fun Sw(on: Boolean, onToggle: () -> Unit) {
    val knob by animateDpAsState(if (on) 21.dp else 3.dp, label = "knob")
    Box(
        Modifier
            .size(42.dp, 24.dp)
            .clip(CircleShape)
            .background(if (on) C.green else C.trackOff)
            .clickable(onClick = onToggle)
    ) {
        Box(
            Modifier
                .padding(start = knob, top = 3.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

// ── مربّع بأيقونة على تدرّج (o-ico / av …) ──
@Composable
fun GradIcon(
    brush: Brush,
    iconId: Int,
    boxSize: Dp = 48.dp,
    corner: Dp = 16.dp,
    iconSize: Dp = 22.dp,
    tint: Color = Color.White,
) {
    Box(
        Modifier.size(boxSize).clip(RoundedCornerShape(corner)).background(brush),
        contentAlignment = Alignment.Center,
    ) { Ic(iconId, iconSize, tint) }
}

@Composable
fun EmojiBox(emoji: String, boxSize: Dp, corner: Dp, fontSize: Int, bg: Color = C.card2) {
    Box(
        Modifier.size(boxSize).clip(RoundedCornerShape(corner)).background(bg),
        contentAlignment = Alignment.Center,
    ) { Text(emoji, fontSize = fontSize.sp) }
}

// أداة صغيرة: صندوق بلون خالص
fun solid(c: Color): Brush = SolidColor(c)
