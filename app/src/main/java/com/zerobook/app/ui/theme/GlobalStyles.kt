package com.zerobook.app.ui.theme
import com.zerobook.app.ui.theme.AppColors

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
        .background(AppColors.inputBg, RoundedCornerShape(8.dp))
        .border(1.dp, Colors.inputBorder, RoundedCornerShape(8.dp))
        .padding(horizontal = 12.dp, vertical = 12.dp)

    val labelTextStyle = TextStyle(
        color = AppColors.labelText,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    )

    val cardModifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .background(AppColors.cardBg)
        .border(1.dp, AppColors.border, RoundedCornerShape(12.dp))
        .padding(16.dp)

    val screenBackgroundModifier = Modifier
        .background(AppColors.screenBg)

    val sectionTitleTextStyle = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = AppColors.textPrimary
    )

    val buttonModifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(AppColors.primary)
        .padding(vertical = 14.dp)

    val buttonTextStyle = TextStyle(
        color = AppColors.textOnPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )

    val emptyStateTextStyle = TextStyle(
        color = AppColors.textTertiary,
        fontSize = 15.sp
    )
}
