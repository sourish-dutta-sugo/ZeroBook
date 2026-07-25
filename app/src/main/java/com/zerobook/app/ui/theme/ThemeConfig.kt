package com.zerobook.app.ui.theme

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppTheme(
    val name: String,
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val backgroundTertiary: Color,
    val accentPrimary: Color,
    val accentLight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val statusBarColor: Color,
    val statusBarDarkIcons: Boolean
) {
    companion object {
        val BEACH = AppTheme(
            name = "BEACH",
            backgroundPrimary = Color(0xFFF7F8FA),
            backgroundSecondary = Color(0xFFFFFFFF),
            backgroundTertiary = Color(0xFFF7F8FA),
            accentPrimary = Color(0xFF1A5C4B),
            accentLight = Color(0xFFE8F5F1),
            textPrimary = Color(0xFF1A1A1A),
            textSecondary = Color(0xFF6B7280),
            textTertiary = Color(0xFF9CA3AF),
            statusBarColor = Color(0xFFF7F8FA),
            statusBarDarkIcons = true
        )

        val BLUE = AppTheme(
            name = "BLUE",
            backgroundPrimary = Color(0xFFF7F8FA),
            backgroundSecondary = Color(0xFFFFFFFF),
            backgroundTertiary = Color(0xFFF7F8FA),
            accentPrimary = Color(0xFF22755F),
            accentLight = Color(0xFFE8F5F1),
            textPrimary = Color(0xFF1A1A1A),
            textSecondary = Color(0xFF6B7280),
            textTertiary = Color(0xFF9CA3AF),
            statusBarColor = Color(0xFFF7F8FA),
            statusBarDarkIcons = true
        )

        val GREEN = AppTheme(
            name = "GREEN",
            backgroundPrimary = Color(0xFFF7F8FA),
            backgroundSecondary = Color(0xFFFFFFFF),
            backgroundTertiary = Color(0xFFF7F8FA),
            accentPrimary = Color(0xFF22A06B),
            accentLight = Color(0xFFE8F8F0),
            textPrimary = Color(0xFF1A1A1A),
            textSecondary = Color(0xFF6B7280),
            textTertiary = Color(0xFF9CA3AF),
            statusBarColor = Color(0xFFF7F8FA),
            statusBarDarkIcons = true
        )

        val PURPLE = AppTheme(
            name = "PURPLE",
            backgroundPrimary = Color(0xFFF7F8FA),
            backgroundSecondary = Color(0xFFFFFFFF),
            backgroundTertiary = Color(0xFFF7F8FA),
            accentPrimary = Color(0xFF7C3AED),
            accentLight = Color(0xFFF5F3FF),
            textPrimary = Color(0xFF1A1A1A),
            textSecondary = Color(0xFF6B7280),
            textTertiary = Color(0xFF9CA3AF),
            statusBarColor = Color(0xFFF7F8FA),
            statusBarDarkIcons = true
        )

        val TEAL = AppTheme(
            name = "TEAL",
            backgroundPrimary = Color(0xFFF7F8FA),
            backgroundSecondary = Color(0xFFFFFFFF),
            backgroundTertiary = Color(0xFFF7F8FA),
            accentPrimary = Color(0xFF1A5C4B),
            accentLight = Color(0xFFE8F5F1),
            textPrimary = Color(0xFF1A1A1A),
            textSecondary = Color(0xFF6B7280),
            textTertiary = Color(0xFF9CA3AF),
            statusBarColor = Color(0xFFF7F8FA),
            statusBarDarkIcons = true
        )

        fun fromName(name: String?): AppTheme = when (name?.uppercase()) {
            BLUE.name -> BLUE
            GREEN.name -> GREEN
            PURPLE.name -> PURPLE
            TEAL.name, "DARK" -> TEAL
            else -> BEACH
        }
    }
}

private object ThemeStorage {
    private const val PREFS_NAME = "zerobook_pref"
    private const val KEY_SELECTED_THEME = "selected_theme"

    fun load(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_THEME, AppTheme.BEACH.name)
            ?: AppTheme.BEACH.name
    }

    fun save(context: Context, name: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_THEME, name)
            .apply()
    }
}

object ThemeRuntime {
    val currentTheme = mutableStateOf(AppTheme.BEACH)

    fun apply(theme: AppTheme) {
        currentTheme.value = theme
    }
}

val LocalAppTheme = staticCompositionLocalOf {
    AppTheme.BEACH
}

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentTheme = MutableStateFlow(AppTheme.BEACH)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    init {
        val theme = AppTheme.fromName(ThemeStorage.load(application))
        _currentTheme.value = theme
        ThemeRuntime.apply(theme)
    }

    fun setTheme(name: String) {
        val theme = AppTheme.fromName(name)
        viewModelScope.launch {
            ThemeStorage.save(getApplication(), theme.name)
            _currentTheme.value = theme
            ThemeRuntime.apply(theme)
        }
    }
}
