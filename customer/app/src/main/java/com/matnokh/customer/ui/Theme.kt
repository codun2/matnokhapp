package com.matnokh.customer.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.matnokh.customer.R

// ── ألوان الهوية (من :root في merchant.html) ──
object C {
    val bg      = Color(0xFFFFF7F2)
    val window  = Color(0xFFF6E7DE)
    val card    = Color(0xFFFFFFFF)
    val card2   = Color(0xFFFBEEE6)
    val line    = Color(0xFFF3E1D6)
    val text    = Color(0xFF2A150C)
    val muted   = Color(0xFFA88C7B)
    val head    = Color(0xFF33190D)
    val green   = Color(0xFFFF5A1F)
    val greenD  = Color(0xFFDA3B00)
    val ok      = Color(0xFF2E9E6B)
    val sage    = Color(0xFFF7C0A6)
    val sand    = Color(0xFFD9C8A9)
    val terra   = Color(0xFFFFB020)
    val blue    = Color(0xFFFF3D6E)

    // ظلال / درجات مساعدة مستخدمة في التصميم
    val blueText = Color(0xFFC81E5B)
    val terraText= Color(0xFFB4791E)
    val redText  = Color(0xFFB3573D)

    // خلفيات الشارات
    val pillLive = Color(0xFFE7F4EC)
    val pillOk   = Color(0xFFFDE7EE)
    val pillWait = Color(0xFFF6ECE4)
    val pillOff  = Color(0xFFF2EFE9)
    val redBg    = Color(0xFFF7E7E2)

    val trackOff = Color(0xFFDDD6C9)
    val strike   = Color(0xFFC3C9C0)
    val chev     = Color(0xFFCDD4CB)
}

// ── التدرّجات ──
object Grad {
    val green = Brush.linearGradient(listOf(Color(0xFFFF6A2B), Color(0xFFFF2E63)))
    val terra = Brush.linearGradient(listOf(Color(0xFFFFB020), Color(0xFFFF6A2B)))
    val blue  = Brush.linearGradient(listOf(Color(0xFFFF3D6E), Color(0xFFC81E5B)))
    val sand  = Brush.linearGradient(listOf(Color(0xFFE0CFAE), Color(0xFFCBB894)))
}

// ── خط Cairo (خط متغيّر واحد + أوزان عبر FontVariation) ──
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun cairo(w: Int, fw: FontWeight) =
    Font(R.font.cairo, weight = fw, variationSettings = FontVariation.Settings(FontVariation.weight(w)))

val Cairo = FontFamily(
    cairo(400, FontWeight.Normal),
    cairo(500, FontWeight.Medium),
    cairo(600, FontWeight.SemiBold),
    cairo(700, FontWeight.Bold),
    cairo(800, FontWeight.ExtraBold),
    cairo(900, FontWeight.Black),
)

private val AppTypography: Typography
    @Composable get() {
        val base = Typography()
        fun androidx.compose.ui.text.TextStyle.c() = copy(fontFamily = Cairo)
        return Typography(
            displayLarge = base.displayLarge.c(), displayMedium = base.displayMedium.c(), displaySmall = base.displaySmall.c(),
            headlineLarge = base.headlineLarge.c(), headlineMedium = base.headlineMedium.c(), headlineSmall = base.headlineSmall.c(),
            titleLarge = base.titleLarge.c(), titleMedium = base.titleMedium.c(), titleSmall = base.titleSmall.c(),
            bodyLarge = base.bodyLarge.c(), bodyMedium = base.bodyMedium.c(), bodySmall = base.bodySmall.c(),
            labelLarge = base.labelLarge.c(), labelMedium = base.labelMedium.c(), labelSmall = base.labelSmall.c(),
        )
    }

@Composable
fun MatnokhTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(
        primary = C.green, background = C.bg, surface = C.card, onBackground = C.text, onSurface = C.text
    )
    val selection = TextSelectionColors(handleColor = C.green, backgroundColor = C.green.copy(alpha = .25f))
    MaterialTheme(colorScheme = scheme, typography = AppTypography) {
        // كامل التطبيق RTL + تثبيت مقياس الخط حتى لا تتغيّر الأبعاد باختلاف إعدادات الجهاز
        val dens = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(dens.density, 1f),
            LocalLayoutDirection provides LayoutDirection.Rtl,
            LocalTextSelectionColors provides selection,
        ) { content() }
    }
}
