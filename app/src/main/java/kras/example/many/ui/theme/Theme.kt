package kras.example.many.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.Density
import kras.example.many.R

@OptIn(ExperimentalTextApi::class)
val OfficeFont = FontFamily(
    Font(R.font.office_sans, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.office_sans, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.office_sans, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.office_sans, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

data class Palette(
    val isDark: Boolean,
    val bg: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val line: Color,
    val text: Color,
    val textDim: Color,
    val accent: Color,
    val onAccent: Color,
    val good: Color,
    val warn: Color,
    val bad: Color,
) {
    /** Цвет по уровню опьянения 0..4. */
    fun level(level: Int): Color = when (level) {
        0 -> good
        1 -> warn
        else -> bad
    }
}

val LightPalette = Palette(
    isDark = false,
    bg = Color(0xFFF4F5F7),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFECEEF1),
    line = Color(0xFFDDE0E5),
    text = Color(0xFF16181D),
    textDim = Color(0xFF6B7280),
    accent = Color(0xFF303C4B),
    onAccent = Color(0xFFFFFFFF),
    good = Color(0xFF2E7D32),
    warn = Color(0xFFB26A00),
    bad = Color(0xFFC62828),
)

val DarkPalette = Palette(
    isDark = true,
    bg = Color(0xFF121316),
    surface = Color(0xFF1B1D21),
    surfaceHigh = Color(0xFF26292E),
    line = Color(0xFF2F3238),
    text = Color(0xFFE9EAEC),
    textDim = Color(0xFF9AA0A8),
    accent = Color(0xFFD3DAE3),
    onAccent = Color(0xFF202832),
    good = Color(0xFF66BB6A),
    warn = Color(0xFFFFB74D),
    bad = Color(0xFFEF5350),
)

enum class ThemeId(val title: String) {
    SYSTEM("Как в системе"),
    LIGHT("Светлая"),
    DARK("Тёмная"),
}

@Composable
fun ThemeId.isDark(): Boolean = when (this) {
    ThemeId.SYSTEM -> isSystemInDarkTheme()
    ThemeId.LIGHT -> false
    ThemeId.DARK -> true
}

val LocalPalette = staticCompositionLocalOf { LightPalette }

object Ui {
    val c: Palette @Composable get() = LocalPalette.current
}

@Composable
fun AppTheme(dark: Boolean, content: @Composable () -> Unit) {
    val p = if (dark) DarkPalette else LightPalette
    val scheme = if (dark) {
        darkColorScheme(
            primary = p.accent, onPrimary = p.onAccent,
            background = p.bg, onBackground = p.text, surface = p.surface, onSurface = p.text,
            surfaceContainerHigh = p.surfaceHigh, surfaceContainerLow = p.surface, onSurfaceVariant = p.textDim,
        )
    } else {
        lightColorScheme(
            primary = p.accent, onPrimary = p.onAccent,
            background = p.bg, onBackground = p.text, surface = p.surface, onSurface = p.text,
            surfaceContainerHigh = p.surfaceHigh, surfaceContainerLow = p.surface, onSurfaceVariant = p.textDim,
        )
    }
    val density = LocalDensity.current
    val base = Typography()
    val typography = Typography(
        displayLarge = base.displayLarge.copy(fontFamily = OfficeFont),
        displayMedium = base.displayMedium.copy(fontFamily = OfficeFont),
        displaySmall = base.displaySmall.copy(fontFamily = OfficeFont),
        headlineLarge = base.headlineLarge.copy(fontFamily = OfficeFont),
        headlineMedium = base.headlineMedium.copy(fontFamily = OfficeFont),
        headlineSmall = base.headlineSmall.copy(fontFamily = OfficeFont),
        titleLarge = base.titleLarge.copy(fontFamily = OfficeFont),
        titleMedium = base.titleMedium.copy(fontFamily = OfficeFont),
        titleSmall = base.titleSmall.copy(fontFamily = OfficeFont),
        bodyLarge = base.bodyLarge.copy(fontFamily = OfficeFont),
        bodyMedium = base.bodyMedium.copy(fontFamily = OfficeFont),
        bodySmall = base.bodySmall.copy(fontFamily = OfficeFont),
        labelLarge = base.labelLarge.copy(fontFamily = OfficeFont),
        labelMedium = base.labelMedium.copy(fontFamily = OfficeFont),
        labelSmall = base.labelSmall.copy(fontFamily = OfficeFont),
    )
    CompositionLocalProvider(LocalPalette provides p, LocalDensity provides Density(density.density, fontScale = 1f)) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}
