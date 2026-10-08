package kras.example.many.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

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
    accent = Color(0xFF1F4E9C),
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
    accent = Color(0xFF7FA8E8),
    onAccent = Color(0xFF0E1A2E),
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
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
