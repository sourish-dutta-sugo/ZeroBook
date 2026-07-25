package com.zerobook.app.ui.theme

import androidx.compose.ui.graphics.Color

object PremiumThemeConfig {

    val PREMIUM_FINTECH = AppTheme(
        name = "PREMIUM_FINTECH",
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

    object Semantic {
        val primaryAction = Color(0xFF1A5C4B)
        val primaryActionHover = Color(0xFF144A3C)
        val primaryActionLight = Color(0xFFE8F5F1)

        val secondaryAction = Color(0xFF6B7280)
        val secondaryActionLight = Color(0xFFE5E7EB)

        val shadowLight = Color(0x0D000000)
        val shadowMedium = Color(0x1A000000)
        val shadowDark = Color(0x26000000)

        val highlightLight = Color(0xFFFFFFFF)
        val highlightMedium = Color(0xFFF7F8FA)

        val cardElevated = Color(0xFFFFFFFF)
        val cardElevatedAlt = Color(0xFFF7F8FA)

        val borderLight = Color(0xFFE5E7EB)
        val borderMedium = Color(0xFFD1D5DB)
        val divider = Color(0xFFE5E7EB)

        val gold = Color(0xFFC8943A)
        val goldLight = Color(0xFFF0C060)

        val income = Color(0xFF22A06B)
        val incomeBg = Color(0xFFE8F8F0)
        val expense = Color(0xFFE24B4A)
        val expenseBg = Color(0xFFFEF0F0)
        val neutral = Color(0xFF6B7280)
        val neutralBg = Color(0xFFF3F4F6)

        val warning = Color(0xFFD97706)
        val warningBg = Color(0xFFFFF7E6)
        val info = Color(0xFF6366F1)
        val infoBg = Color(0xFFEEF2FF)

        val success = Color(0xFF22A06B)
        val successBg = Color(0xFFE8F8F0)
        val error = Color(0xFFE24B4A)
        val errorBg = Color(0xFFFEF0F0)

        val balancePositive = Color(0xFF22A06B)
        val balanceNegative = Color(0xFFE24B4A)
        val balanceNeutral = Color(0xFF1A1A1A)

        val chartIncome = Color(0xFF22A06B)
        val chartExpense = Color(0xFFE24B4A)
        val chartPending = Color(0xFFD97706)
        val chartNeutral = Color(0xFF9CA3AF)
    }

    object Typography {
        val displayLarge = 32f
        val displayMedium = 28f
        val displaySmall = 24f
        val headlineLarge = 22f
        val headlineMedium = 20f
        val headlineSmall = 18f
        val titleLarge = 16f
        val titleMedium = 14f
        val titleSmall = 12f
        val bodyLarge = 16f
        val bodyMedium = 14f
        val bodySmall = 12f
        val labelLarge = 14f
        val labelMedium = 12f
        val labelSmall = 11f
    }

    object Spacing {
        val xs = 4f
        val sm = 8f
        val md = 12f
        val lg = 16f
        val xl = 24f
        val xxl = 32f
    }

    object BorderRadius {
        val xs = 4f
        val sm = 8f
        val md = 12f
        val lg = 14f
        val xl = 20f
        val full = 999f
    }

    object Shadows {
        val elevationSmall = 2f
        val elevationMedium = 4f
        val elevationLarge = 8f
        val elevationXLarge = 12f
    }
}
