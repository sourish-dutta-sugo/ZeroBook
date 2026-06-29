package com.zerobook.app.ui.theme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable

@Composable
fun zeroBookInputColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppColors.inputText,
    unfocusedTextColor = AppColors.inputText,
    disabledTextColor = AppColors.textDisabled,
    focusedPlaceholderColor = AppColors.inputPlaceholder,
    unfocusedPlaceholderColor = AppColors.inputPlaceholder,
    focusedBorderColor = AppColors.inputBorderFocus,
    unfocusedBorderColor = AppColors.inputBorder,
    focusedLabelColor = AppColors.inputBorderFocus,
    unfocusedLabelColor = AppColors.labelText,
    cursorColor = AppColors.inputBorderFocus,
    focusedContainerColor = AppColors.inputBg,
    unfocusedContainerColor = AppColors.inputBg,
    errorTextColor = AppColors.inputText,
    errorBorderColor = AppColors.error,
    errorContainerColor = AppColors.inputBg,
    errorLabelColor = AppColors.error,
    errorPlaceholderColor = AppColors.inputPlaceholder,
    disabledContainerColor = AppColors.inputBg,
    disabledBorderColor = AppColors.inputBorder,
)
