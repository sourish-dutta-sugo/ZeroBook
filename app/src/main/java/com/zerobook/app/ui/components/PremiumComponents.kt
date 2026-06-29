package com.zerobook.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerobook.app.ui.theme.AppColors
import com.zerobook.app.ui.theme.PremiumThemeConfig

/**
 * Premium Neumorphic Components for Fintech Design
 * 
 * Provides reusable UI components with soft neumorphic styling,
 * including elevated cards, buttons, and interactive elements.
 */

// ============================================================================
// ELEVATED CARD WITH NEUMORPHIC EFFECT
// ============================================================================

@Composable
fun PremiumElevatedCard(
    modifier: Modifier = Modifier,
    elevation: Int = 4,
    backgroundColor: Color = PremiumThemeConfig.Semantic.cardElevated,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val elevationDp = when (elevation) {
        1 -> 2.dp
        2 -> 4.dp
        3 -> 6.dp
        4 -> 8.dp
        else -> 12.dp
    }
    
    val shadowColor = PremiumThemeConfig.Semantic.shadowLight
    
    Card(
        modifier = modifier
            .shadow(
                elevation = elevationDp,
                shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.lg.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.lg.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

// ============================================================================
// BALANCE CARD - LARGE PREMIUM DISPLAY
// ============================================================================

@Composable
fun PremiumBalanceCard(
    title: String,
    amount: Double,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null,
    amountColor: Color = PremiumThemeConfig.Semantic.balanceNeutral,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    PremiumElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 3,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PremiumThemeConfig.Spacing.lg.dp),
            verticalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp)
        ) {
            // Header with icon and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                        color = AppColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 10.sp,
                            color = AppColors.textTertiary,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                if (icon != null) {
                    icon()
                }
            }
            
            // Amount display
            Text(
                text = formatIndianCurrency(amount),
                fontSize = PremiumThemeConfig.Typography.displaySmall.sp,
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================================
// QUICK ACTION BUTTON - NEUMORPHIC STYLE
// ============================================================================

@Composable
fun PremiumQuickActionButton(
    label: String,
    icon: @Composable (() -> Unit)? = null,
    backgroundColor: Color = PremiumThemeConfig.Semantic.primaryActionLight,
    textColor: Color = PremiumThemeConfig.Semantic.primaryAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumElevatedCard(
        modifier = modifier
            .height(56.dp),
        elevation = 2,
        backgroundColor = backgroundColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PremiumThemeConfig.Spacing.md.dp),
            horizontalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.sm.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                icon()
            }
            Text(
                text = label,
                fontSize = PremiumThemeConfig.Typography.labelLarge.sp,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ============================================================================
// TRANSACTION CARD - MODERN DESIGN
// ============================================================================

@Composable
fun PremiumTransactionCard(
    title: String,
    subtitle: String,
    amount: Double,
    category: String,
    categoryIcon: @Composable (() -> Unit)? = null,
    isIncome: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    PremiumElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PremiumThemeConfig.Spacing.md.dp),
            horizontalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            if (categoryIcon != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isIncome) PremiumThemeConfig.Semantic.incomeBg else PremiumThemeConfig.Semantic.expenseBg,
                            shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    categoryIcon()
                }
            }
            
            // Title and Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$category • $subtitle",
                    fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                    color = AppColors.textTertiary,
                    fontWeight = FontWeight.Normal
                )
            }
            
            // Amount
            Text(
                text = formatIndianCurrency(amount),
                fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
                color = if (isIncome) PremiumThemeConfig.Semantic.income else PremiumThemeConfig.Semantic.expense,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================================
// KPI STAT CARD - FOR DASHBOARD METRICS
// ============================================================================

@Composable
fun PremiumKpiCard(
    label: String,
    value: Double,
    change: Double? = null,
    isPositive: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    PremiumElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
        elevation = 2
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PremiumThemeConfig.Spacing.md.dp),
            verticalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.sm.dp)
        ) {
            // Header with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                    color = AppColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
                if (icon != null) {
                    icon()
                }
            }
            
            // Value
            Text(
                text = formatIndianCurrency(value),
                fontSize = PremiumThemeConfig.Typography.headlineMedium.sp,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            
            // Change indicator
            if (change != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (change >= 0) "↑" else "↓",
                        fontSize = 12.sp,
                        color = if (isPositive) PremiumThemeConfig.Semantic.income else PremiumThemeConfig.Semantic.expense,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${String.format("%.1f", kotlin.math.abs(change))}%",
                        fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                        color = if (isPositive) PremiumThemeConfig.Semantic.income else PremiumThemeConfig.Semantic.expense,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ============================================================================
// CATEGORY BADGE - FOR TRANSACTION CATEGORIZATION
// ============================================================================

@Composable
fun PremiumCategoryBadge(
    label: String,
    isSelected: Boolean = false,
    backgroundColor: Color = if (isSelected) PremiumThemeConfig.Semantic.primaryAction else PremiumThemeConfig.Semantic.primaryActionLight,
    textColor: Color = if (isSelected) Color.White else PremiumThemeConfig.Semantic.primaryAction,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(PremiumThemeConfig.BorderRadius.full.dp))
            .background(backgroundColor)
            .clickable(
                enabled = onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick?.invoke() }
            )
            .padding(
                horizontal = PremiumThemeConfig.Spacing.md.dp,
                vertical = PremiumThemeConfig.Spacing.sm.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = PremiumThemeConfig.Typography.labelMedium.sp,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ============================================================================
// HELPER FUNCTION - FORMAT INDIAN CURRENCY
// ============================================================================

fun formatIndianCurrency(amount: Double): String {
    val absAmount = kotlin.math.abs(amount)
    val sign = if (amount < 0) "-" else ""
    
    return when {
        absAmount >= 10000000 -> String.format("%s₹%.2fCr", sign, absAmount / 10000000)
        absAmount >= 100000 -> String.format("%s₹%.2fL", sign, absAmount / 100000)
        absAmount >= 1000 -> String.format("%s₹%.2fK", sign, absAmount / 1000)
        else -> String.format("%s₹%.2f", sign, absAmount)
    }
}
