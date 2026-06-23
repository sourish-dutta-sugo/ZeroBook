package com.example.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable

@Composable
fun themedInputColors(): TextFieldColors {
    val theme = LocalAppTheme.current
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = theme.textPrimary,
        unfocusedTextColor = theme.textPrimary,
        disabledTextColor = theme.textTertiary,
        focusedPlaceholderColor = theme.textTertiary,
        unfocusedPlaceholderColor = theme.textTertiary,
        focusedBorderColor = theme.accentPrimary,
        unfocusedBorderColor = AppColors.border,
        focusedLabelColor = theme.accentPrimary,
        unfocusedLabelColor = theme.textSecondary,
        cursorColor = theme.accentPrimary,
        focusedContainerColor = theme.backgroundTertiary,
        unfocusedContainerColor = theme.backgroundTertiary,
        errorTextColor = theme.textPrimary,
        errorBorderColor = AppColors.error,
        errorContainerColor = theme.backgroundTertiary,
        errorLabelColor = AppColors.error,
        errorPlaceholderColor = theme.textTertiary,
        disabledContainerColor = theme.backgroundTertiary,
        disabledBorderColor = AppColors.border
    )
}
