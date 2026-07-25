package com.zerobook.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object GlobalStyles {
    val inputModifier = Modifier
        .background(AppColors.inputBg, RoundedCornerShape(12.dp))
        .border(0.5.dp, AppColors.inputBorder, RoundedCornerShape(12.dp))
        .padding(horizontal = 14.dp, vertical = 14.dp)

    val labelTextStyle = TextStyle(
        color = AppColors.labelText,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
    )

    val cardModifier = Modifier
        .clip(RoundedCornerShape(14.dp))
        .background(AppColors.cardBg)
        .border(0.5.dp, AppColors.border, RoundedCornerShape(14.dp))
        .padding(16.dp)

    val screenBackgroundModifier = Modifier
        .background(AppColors.screenBg)

    val sectionTitleTextStyle = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.textPrimary
    )

    val buttonModifier = Modifier
        .clip(RoundedCornerShape(14.dp))
        .background(AppColors.primary)
        .padding(vertical = 15.dp)

    val buttonTextStyle = TextStyle(
        color = AppColors.textOnPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold
    )

    val emptyStateTextStyle = TextStyle(
        color = AppColors.textTertiary,
        fontSize = 15.sp
    )
}
