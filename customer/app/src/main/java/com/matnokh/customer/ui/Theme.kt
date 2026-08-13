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
    val bg      = Color(0xFFF7F5F0)
    val window  = Color(0xFFEFECE5)
    val card    = Color(0xFFFFFFFF)
    val card2   = Color(0xFFF2EFE9)
    val line    = Color(0xFFE8E3D9)
    val text    = Color(0xFF2D3A34)
    val muted   = Color(0xFF8A9389)
    val head    = Color(0xFF3A4F44)
    val green   = Color(0xFF5C8D76)
    val greenD  = Color(0xFF47705D)
    val sage    = Color(0xFFA8BFAE)
    val sand    = Color(0xFFD9C8A9)
    val terra   = Color(0xFFC98D6B)
    val blue    = Color(0xFF7DA2B8)

    // ظلال / درجات مساعدة مستخدمة في التصميم
    val blueText = Color(0xFF5A809A)
    val terraText= Color(0xFFB5794F)
    val redText  = Color(0xFFB3573D)

    // خلفيات الشارات
    val pillLive = Color(0xFFE7EFE9)
    val pillOk   = Color(0xFFE9F0F4)
    val pillWait = Color(0xFFF6ECE4)
    val pillOff  = Color(0xFFF2EFE9)
    val redBg    = Color(0xFFF7E7E2)

    val trackOff = Color(0xFFDDD6C9)
    val strike   = Color(0xFFC3C9C0)
    val chev     = Color(0xFFCDD4CB)
}

// ── التدرّجات ──
object Grad {
    val green = Brush.linearGradient(listOf(Color(0xFF6A9D84), Color(0xFF4F7F68)))
    val terra = Brush.linearGradient(listOf(Color(0xFFD69C78), Color(0xFFC1815D)))
    val blue  = Brush.linearGradient(listOf(Color(0xFF8FB3C9), Color(0xFF6D95AD)))
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
