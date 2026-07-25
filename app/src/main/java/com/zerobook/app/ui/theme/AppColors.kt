package com.zerobook.app.ui.theme

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
    val bottomBarBg get() = Color(0xFFFFFFFF)
    val topBarBg get() = theme.backgroundSecondary
    val divider = Color(0xFFE5E7EB)
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

    val border = Color(0xFFE5E7EB)
    val borderFocus get() = theme.accentPrimary
    val borderLight = Color(0xFFF7F8FA)

    val debit = Color(0xFFE24B4A)
    val debitBg = Color(0xFFFEF0F0)
    val credit = Color(0xFF22A06B)
    val creditBg = Color(0xFFE8F8F0)

    val gold = Color(0xFFC8943A)
    val goldLight = Color(0xFFF0C060)

    val badgeSaleBg = Color(0xFFE8F8F0)
    val badgeSaleText = Color(0xFF22A06B)
    val badgePurchaseBg = Color(0xFFFEF0F0)
    val badgePurchaseText = Color(0xFFE24B4A)
    val badgeReceiptBg = Color(0xFFFFF7E6)
    val badgeReceiptText = Color(0xFFD97706)
    val badgePaymentBg = Color(0xFFEEF2FF)
    val badgePaymentText = Color(0xFF6366F1)
    val badgeReturnBg = Color(0xFFFEF0F0)
    val badgeReturnText = Color(0xFFE24B4A)
    val badgeOverdueBg = Color(0xFFFEF0F0)
    val badgeOverdueText = Color(0xFFE24B4A)
    val badgePartialBg = Color(0xFFFFF7E6)
    val badgePartialText = Color(0xFFD97706)
    val badgePaidBg = Color(0xFFE8F8F0)
    val badgePaidText = Color(0xFF22A06B)

    val inputText get() = theme.textPrimary
    val inputPlaceholder get() = theme.textTertiary
    val inputBorder = Color(0xFFE5E7EB)
    val inputBorderFocus get() = theme.accentPrimary
    val labelText get() = theme.textSecondary

    val success = Color(0xFF22A06B)
    val successBg = Color(0xFFE8F8F0)
    val error = Color(0xFFE24B4A)
    val errorBg = Color(0xFFFEF0F0)
    val warning = Color(0xFFD97706)
    val warningBg = Color(0xFFFFF7E6)
    val info = Color(0xFF6366F1)
    val infoBg = Color(0xFFEEF2FF)
}
