package com.example.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    private val theme: AppTheme
        get() = ThemeRuntime.currentTheme.value

    val screenBg get() = theme.backgroundPrimary
    val cardBg get() = theme.backgroundSecondary
    val inputBg get() = theme.backgroundTertiary
    val sectionHeaderBg get() = theme.accentLight
    val tableHeaderBg get() = theme.accentLight
    val tableRowEven get() = theme.backgroundSecondary
    val tableRowOdd get() = theme.backgroundPrimary
    val bottomBarBg get() = theme.backgroundSecondary
    val topBarBg get() = theme.backgroundSecondary
    val divider = Color(0xFFEAEEF3)
    val shimmerBg get() = theme.accentLight.copy(alpha = 0.45f)

    val textPrimary get() = theme.textPrimary
    val textSecondary get() = theme.textSecondary
    val textTertiary get() = theme.textTertiary
    val textHint get() = theme.textTertiary
    val textOnPrimary = Color(0xFFFFFFFF)
    val textOnDark = Color(0xFFFFFFFF)
    val textDisabled = Color(0xFFBBBBBB)

    val primary get() = theme.accentPrimary
    val primaryDark get() = theme.accentPrimary.copy(alpha = 0.85f)
    val primaryLight get() = theme.accentLight
    val primaryText get() = theme.accentPrimary

    val border = Color(0xFFE0E4EA)
    val borderFocus get() = theme.accentPrimary
    val borderLight = Color(0xFFF0F2F5)

    val debit = Color(0xFFC62828)
    val debitBg = Color(0xFFFDECEA)
    val credit = Color(0xFF1E8A3C)
    val creditBg = Color(0xFFE6F4EA)

    val badgeSaleBg = Color(0xFFE6F4EA)
    val badgeSaleText = Color(0xFF1E6E35)
    val badgePurchaseBg = Color(0xFFFFF3E0)
    val badgePurchaseText = Color(0xFFBF5000)
    val badgeReceiptBg = Color(0xFFE1F5FE)
    val badgeReceiptText = Color(0xFF01579B)
    val badgePaymentBg = Color(0xFFF3E5F5)
    val badgePaymentText = Color(0xFF4A148C)
    val badgeReturnBg = Color(0xFFFDECEA)
    val badgeReturnText = Color(0xFF8B0000)
    val badgeOverdueBg = Color(0xFFFDECEA)
    val badgeOverdueText = Color(0xFFC62828)
    val badgePartialBg = Color(0xFFE1F5FE)
    val badgePartialText = Color(0xFF0277BD)
    val badgePaidBg = Color(0xFFE6F4EA)
    val badgePaidText = Color(0xFF1E8A3C)

    val inputText get() = theme.textPrimary
    val inputPlaceholder get() = theme.textTertiary
    val inputBorder = Color(0xFFE0E4EA)
    val inputBorderFocus get() = theme.accentPrimary
    val labelText get() = theme.textSecondary

    val success = Color(0xFF1E8A3C)
    val successBg = Color(0xFFE6F4EA)
    val error = Color(0xFFC62828)
    val errorBg = Color(0xFFFDECEA)
    val warning = Color(0xFFE65100)
    val warningBg = Color(0xFFFFF3E0)
    val info = Color(0xFF0277BD)
    val infoBg = Color(0xFFE1F5FE)
}
