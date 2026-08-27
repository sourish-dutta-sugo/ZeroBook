package com.zerobook.app.ui.theme

import android.app.Application
import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
        val SAFFRON = AppTheme(
            name = ThemeNames.SAFFRON,
            backgroundPrimary = Color(0xFFF8F7F4),
            backgroundSecondary = Color(0xFFFFFFFF),
            backgroundTertiary = Color(0xFFEFEFED),
            accentPrimary = Color(0xFF1A5C4B),
            accentLight = Color(0xFFE8F5F1),
            textPrimary = Color(0xFF1A1A1A),
            textSecondary = Color(0xFF4A4A4A),
            textTertiary = Color(0xFF888888),
            statusBarColor = Color(0xFFF8F7F4),
            statusBarDarkIcons = true
        )

        val SLATE = AppTheme(
            name = ThemeNames.SLATE,
            backgroundPrimary = Color(0xFFF8F7F4),
            backgroundSecondary = Color(0xFFFFFFFF),
            backgroundTertiary = Color(0xFFEFEFED),
            accentPrimary = Color(0xFF22755F),
            accentLight = Color(0xFFE8F5F1),
            textPrimary = Color(0xFF1A1A1A),
            textSecondary = Color(0xFF4A4A4A),
            textTertiary = Color(0xFF888888),
            statusBarColor = Color(0xFFF8F7F4),
            statusBarDarkIcons = true
        )

        val LEDGER = AppTheme(
            name = ThemeNames.LEDGER,
            backgroundPrimary = Color(0xFFF8F7F4),
            backgroundSecondary = Color(0xFFFFFFFF),
            backgroundTertiary = Color(0xFFEFEFED),
            accentPrimary = Color(0xFF22A06B),
            accentLight = Color(0xFFE8F8F0),
            textPrimary = Color(0xFF1A1A1A),
            textSecondary = Color(0xFF4A4A4A),
            textTertiary = Color(0xFF888888),
            statusBarColor = Color(0xFFF8F7F4),
            statusBarDarkIcons = true
        )

        val INK = AppTheme(
            name = ThemeNames.INK,
            backgroundPrimary = Color(0xFFF8F7F4),
            backgroundSecondary = Color(0xFFFFFFFF),
            backgroundTertiary = Color(0xFFEFEFED),
            accentPrimary = Color(0xFF7C3AED),
            accentLight = Color(0xFFF5F3FF),
            textPrimary = Color(0xFF1A1A1A),
            textSecondary = Color(0xFF4A4A4A),
            textTertiary = Color(0xFF888888),
            statusBarColor = Color(0xFFF8F7F4),
            statusBarDarkIcons = true
        )

        fun fromName(name: String?): AppTheme = when (name?.uppercase()) {
            ThemeNames.SLATE, "BLUE" -> SLATE
            ThemeNames.LEDGER, "GREEN" -> LEDGER
            ThemeNames.INK, "PURPLE", "DARK" -> INK
            ThemeNames.SAFFRON, "BEACH", "TEAL" -> SAFFRON
            else -> SAFFRON
        }
    }
}

private object ThemeStorage {
    private const val PREFS_NAME = "zerobook_pref"
    private const val KEY_SELECTED_THEME = "selected_theme"

    fun load(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_THEME, AppTheme.SAFFRON.name)
            ?: AppTheme.SAFFRON.name
    }

    fun save(context: Context, name: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_THEME, name)
            .apply()
    }
}

object ThemeRuntime {
    val currentTheme = mutableStateOf(AppTheme.SAFFRON)

    fun apply(theme: AppTheme) {
        currentTheme.value = theme
    }
}

val LocalAppTheme = staticCompositionLocalOf {
    AppTheme.SAFFRON
}

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentTheme = MutableStateFlow(AppTheme.SAFFRON)
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

@Composable
fun ZeroBookTheme(
    appTheme: AppTheme = LocalAppTheme.current,
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = appTheme.accentPrimary,
        onPrimary = TextColors.onPrimary,
        secondary = appTheme.textSecondary,
        background = appTheme.backgroundPrimary,
        surface = appTheme.backgroundSecondary,
        outline = Surface.border,
        onBackground = appTheme.textPrimary,
        onSurface = appTheme.textPrimary,
        surfaceVariant = appTheme.backgroundTertiary,
        onSurfaceVariant = appTheme.textSecondary
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun themedInputColors(): TextFieldColors {
    val theme = LocalAppTheme.current
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = theme.textPrimary,
        unfocusedTextColor = theme.textPrimary,
        disabledTextColor = TextColors.disabled,
        focusedPlaceholderColor = theme.textTertiary,
        unfocusedPlaceholderColor = theme.textTertiary,
        focusedBorderColor = theme.accentPrimary,
        unfocusedBorderColor = Surface.inputBorder,
        focusedLabelColor = theme.accentPrimary,
        unfocusedLabelColor = theme.textSecondary,
        cursorColor = theme.accentPrimary,
        focusedContainerColor = theme.backgroundTertiary,
        unfocusedContainerColor = theme.backgroundTertiary,
        errorTextColor = theme.textPrimary,
        errorBorderColor = Semantic.error,
        errorContainerColor = theme.backgroundTertiary,
        errorLabelColor = Semantic.error,
        errorPlaceholderColor = theme.textTertiary,
        disabledContainerColor = theme.backgroundTertiary,
        disabledBorderColor = Surface.inputBorder
    )
}

@Composable
fun zeroBookInputColors(): TextFieldColors = themedInputColors()
