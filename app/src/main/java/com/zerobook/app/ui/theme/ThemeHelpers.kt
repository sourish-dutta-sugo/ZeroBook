package com.zerobook.app.ui.theme

import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TextDark @Composable get() = AppColors.textPrimary
val TextGray @Composable get() = AppColors.textSecondary
val TextHint @Composable get() = AppColors.textTertiary
val ScreenBackground @Composable get() = AppColors.screenBg
val CardWhite @Composable get() = AppColors.cardBg
val InputWhite @Composable get() = AppColors.inputBg
val BorderDefault = Color(0xFFE0E4EA)
val PrimaryBlue @Composable get() = AppColors.primary
val SectionHeaderBg @Composable get() = AppColors.sectionHeaderBg
val TableRowOdd @Composable get() = AppColors.tableRowOdd
val DebitRed = Color(0xFFC62828)
val CreditGreen = Color(0xFF1E8A3C)

@Composable
fun darkDropdownItemColors(): MenuItemColors = MenuDefaults.itemColors(
    textColor = AppColors.textPrimary,
    leadingIconColor = AppColors.textPrimary,
    trailingIconColor = AppColors.textPrimary,
    disabledTextColor = AppColors.textTertiary
)
