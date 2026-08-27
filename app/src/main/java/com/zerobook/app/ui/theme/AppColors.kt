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
    val bottomBarBg get() = theme.backgroundSecondary
    val topBarBg get() = theme.backgroundSecondary
    val divider = Color(0xFFE0E4EA)
    val shimmerBg get() = theme.accentLight.copy(alpha = 0.45f)

    val textPrimary get() = theme.textPrimary
    val textSecondary get() = theme.textSecondary
    val textTertiary get() = theme.textTertiary
    val textHint get() = theme.textTertiary
    val textOnPrimary = Color(0xFFFFFFFF)
    val textOnDark = Color(0xFFFFFFFF)
    val textDisabled = Color(0xFFB0B0B0)

    val primary get() = theme.accentPrimary
    val primaryDark get() = theme.accentPrimary.copy(alpha = 0.85f)
    val primaryLight get() = theme.accentLight
    val primaryText get() = theme.accentPrimary

    val border = Color(0xFFE0E4EA)
    val borderFocus get() = theme.accentPrimary
    val borderLight = Color(0xFFF3F2EF)

    val debit = Color(0xFFE24B4A)
    val debitBg = Color(0xFFFEF0F0)
    val credit = Color(0xFF22A06B)
    val creditBg = Color(0xFFE8F8F0)

    val gold = Color(0xFFC8943A)
    val goldLight = Color(0xFFF0C060)

    val badgeSaleBg = Badge.saleBg
    val badgeSaleText = Badge.saleText
    val badgePurchaseBg = Badge.purchaseBg
    val badgePurchaseText = Badge.purchaseText
    val badgeReceiptBg = Badge.receiptBg
    val badgeReceiptText = Badge.receiptText
    val badgePaymentBg = Badge.paymentBg
    val badgePaymentText = Badge.paymentText
    val badgeReturnBg = Badge.returnBg
    val badgeReturnText = Badge.returnText
    val badgeOverdueBg = Badge.overdueBg
    val badgeOverdueText = Badge.overdueText
    val badgePartialBg = Badge.partialBg
    val badgePartialText = Badge.partialText
    val badgePaidBg = Badge.paidBg
    val badgePaidText = Badge.paidText

    val inputText get() = theme.textPrimary
    val inputPlaceholder get() = theme.textTertiary
    val inputBorder = Color(0xFFE0E4EA)
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
