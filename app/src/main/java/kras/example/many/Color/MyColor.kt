package kras.example.many.Color

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kras.example.many.R

enum class AppTheme {
    LIGHT,
    DARK,
    PINK
}

object MyColor {
    
    private const val PREFS_NAME = "app_settings"
    private const val KEY_THEME = "selected_theme"
    
    private var sharedPreferences: SharedPreferences? = null
    
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadTheme()
    }
    
    private fun loadTheme() {
        val savedTheme = sharedPreferences?.getString(KEY_THEME, AppTheme.LIGHT.name) ?: AppTheme.LIGHT.name
        currentTheme = try {
            AppTheme.valueOf(savedTheme)
        } catch (e: IllegalArgumentException) {
            AppTheme.LIGHT
        }
    }
    
    private fun saveTheme(theme: AppTheme) {
        sharedPreferences?.edit()?.putString(KEY_THEME, theme.name)?.apply()
    }
    
    // Светлая тема
    private val LIGHT_BACKGROUND = Color(0xFFF4F6FA)
    private val LIGHT_TITLE = Color(0xFF1B1D27)
    private val LIGHT_SECTION_TITLE = Color(0xFF0872B1)
    private val LIGHT_SUBTITLE = Color(0xFF828D96)
    private val LIGHT_RESULT = Color(0xFFF5A10B)
    private val LIGHT_BOX = Color(0xFFFFFFFF)
    
    // Темная тема
    private val DARK_BACKGROUND = Color(0xFF1A1A2E)
    private val DARK_TITLE = Color(0xFFEAEAEA)
    private val DARK_SECTION_TITLE = Color(0xFF5BA3E0)
    private val DARK_SUBTITLE = Color(0xFFA0A5B0)
    private val DARK_RESULT = Color(0xFFFFC34A)
    private val DARK_BOX = Color(0xFF2A2A3E)
    
    // Розовая тема
    private val PINK_BACKGROUND = Color(0xFFFFE6F0)
    private val PINK_TITLE = Color(0xFF4A192C)
    private val PINK_SECTION_TITLE = Color(0xFFE83E8C)
    private val PINK_SUBTITLE = Color(0xFF8B5A6F)
    private val PINK_RESULT = Color(0xFFFF8C42)
    private val PINK_BOX = Color(0xFFFFF0F5)
    
    // Текущая тема (по умолчанию светлая)
    var currentTheme: AppTheme by mutableStateOf(AppTheme.LIGHT)
        private set
    
    @Composable
    fun getBackgroundColor(): Color {
        return when (currentTheme) {
            AppTheme.LIGHT -> LIGHT_BACKGROUND
            AppTheme.DARK -> DARK_BACKGROUND
            AppTheme.PINK -> PINK_BACKGROUND
        }
    }
    
    @Composable
    fun getTitleColor(): Color {
        return when (currentTheme) {
            AppTheme.LIGHT -> LIGHT_TITLE
            AppTheme.DARK -> DARK_TITLE
            AppTheme.PINK -> PINK_TITLE
        }
    }

    
    @Composable
    fun getSectionTitleColor(): Color {
        return when (currentTheme) {
            AppTheme.LIGHT -> LIGHT_SECTION_TITLE
            AppTheme.DARK -> DARK_SECTION_TITLE
            AppTheme.PINK -> PINK_SECTION_TITLE
        }
    }

    @Composable
    fun getSubtitleColor(): Color {
        return when (currentTheme) {
            AppTheme.LIGHT -> LIGHT_SUBTITLE
            AppTheme.DARK -> DARK_SUBTITLE
            AppTheme.PINK -> PINK_SUBTITLE
        }
    }

    @Composable
    fun getResultColor(): Color {
        return when (currentTheme) {
            AppTheme.LIGHT -> LIGHT_RESULT
            AppTheme.DARK -> DARK_RESULT
            AppTheme.PINK -> PINK_RESULT
        }
    }

    @Composable
    fun getBoxColor(): Color {
        return when (currentTheme) {
            AppTheme.LIGHT -> LIGHT_BOX
            AppTheme.DARK -> DARK_BOX
            AppTheme.PINK -> PINK_BOX
        }
    }



    // Удобные свойства для использования в коде
    val BACKGROUND: Color
        @Composable get() = getBackgroundColor()
    val TITLE: Color
        @Composable get() = getTitleColor()
    val SECTION_TITLE: Color
        @Composable get() = getSectionTitleColor()
    val SUBTITLE: Color
        @Composable get() = getSubtitleColor()
    val RESULT: Color
        @Composable get() = getResultColor()
    val BOX: Color
        @Composable get() = getBoxColor()
    
    fun toggleTheme() {
        currentTheme = when (currentTheme) {
            AppTheme.LIGHT -> AppTheme.DARK
            AppTheme.DARK -> AppTheme.PINK
            AppTheme.PINK -> AppTheme.LIGHT
        }
        saveTheme(currentTheme)
    }
    
    // Определяет, должна ли тема использовать светлые иконки статус-бара/навигации
    fun isLightTheme(): Boolean {
        return when (currentTheme) {
            AppTheme.LIGHT -> true  // Светлый фон, темные иконки
            AppTheme.DARK -> false  // Темный фон, светлые иконки
            AppTheme.PINK -> true  // Светлый фон, темные иконки
        }
    }

    fun icoTheme():Int{
        return when (currentTheme) {
            AppTheme.LIGHT -> R.drawable.palette_day
            AppTheme.DARK -> R.drawable.palette_night
            AppTheme.PINK -> R.drawable.palette_pink
        }
    }
}