package kras.example.many.ui.theme

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
    val bgGlow: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val text: Color,
    val textDim: Color,
    val accent: Color,
    val accent2: Color,
    val onAccent: Color,
) {
    val good = Color(0xFF3DDC84)
    val warn = Color(0xFFFFB020)
    val bad = Color(0xFFFF4D4F)

    /** Цвет по уровню опьянения 0..4. */
    fun level(level: Int): Color = when (level) {
        0 -> good
        1 -> warn
        2 -> Color(0xFFFF7A2F)
        else -> bad
    }
}

enum class ThemeId(val title: String, val palette: Palette) {
    GRAPHITE(
        "Графит", Palette(
            true, Color(0xFF0E0F13), Color(0xFF2A1F12), Color(0xFF181A20), Color(0xFF242730),
            Color(0xFFF3F4F6), Color(0xFF8E93A3), Color(0xFFFFB547), Color(0xFFFF6B5A), Color(0xFF1A1205),
        )
    ),
    NEON(
        "Неон", Palette(
            true, Color(0xFF0B0718), Color(0xFF34104A), Color(0xFF171128), Color(0xFF241A3B),
            Color(0xFFF5F0FF), Color(0xFF9C90B8), Color(0xFF00E5C7), Color(0xFFFF3D9A), Color(0xFF02201C),
        )
    ),
    OCEAN(
        "Океан", Palette(
            true, Color(0xFF06141C), Color(0xFF0B3446), Color(0xFF0F2230), Color(0xFF173244),
            Color(0xFFE8F6FB), Color(0xFF86A5B3), Color(0xFF3FD0FF), Color(0xFF7CFFB2), Color(0xFF02202C),
        )
    ),
    DAY(
        "День", Palette(
            false, Color(0xFFF5F3EF), Color(0xFFFFE2C4), Color(0xFFFFFFFF), Color(0xFFF0ECE6),
            Color(0xFF1A1A1F), Color(0xFF75757F), Color(0xFFE8890C), Color(0xFFE04E39), Color(0xFFFFFFFF),
        )
    ),
    ROSE(
        "Роза", Palette(
            false, Color(0xFFFFF1F4), Color(0xFFFFCFDE), Color(0xFFFFFFFF), Color(0xFFFFE6EE),
            Color(0xFF3A1626), Color(0xFF9A6B7E), Color(0xFFE6457A), Color(0xFFFF9A5A), Color(0xFFFFFFFF),
        )
    ),
}

val LocalPalette = staticCompositionLocalOf { ThemeId.GRAPHITE.palette }

object Ui {
    val c: Palette @Composable get() = LocalPalette.current
}

@Composable
fun AppTheme(theme: ThemeId, content: @Composable () -> Unit) {
    val p = theme.palette
    val scheme = if (p.isDark) {
        darkColorScheme(
            primary = p.accent, onPrimary = p.onAccent, secondary = p.accent2,
            background = p.bg, onBackground = p.text, surface = p.surface, onSurface = p.text,
            surfaceContainerHigh = p.surfaceHigh, surfaceContainerLow = p.surface, onSurfaceVariant = p.textDim,
        )
    } else {
        lightColorScheme(
            primary = p.accent, onPrimary = p.onAccent, secondary = p.accent2,
            background = p.bg, onBackground = p.text, surface = p.surface, onSurface = p.text,
            surfaceContainerHigh = p.surfaceHigh, surfaceContainerLow = p.surface, onSurfaceVariant = p.textDim,
        )
    }
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
