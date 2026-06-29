package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.slideInFromBottom
import com.example.ui.components.PremiumElevatedCard
import com.example.ui.components.PremiumKpiCard
import com.example.ui.components.formatIndianCurrency
import com.example.ui.theme.AppColors
import com.example.ui.theme.PremiumThemeConfig

/**
 * Premium Expenses & Budget Screen
 * 
 * Features:
 * - Budget overview and tracking
 * - Expense categories
 * - Budget vs actual comparison
 * - Spending insights
 * - Category breakdown
 */

data class ExpenseCategory(
    val id: String,
    val name: String,
    val icon: androidx.compose.material.icons.materialIcon,
    val color: Color,
    val budget: Double,
    val spent: Double
)

@Composable
fun ExpensesBudgetScreenPremium(
    expenses: List<ExpenseCategory> = emptyList(),
    onCategoryClick: (ExpenseCategory) -> Unit = {},
    onAddExpense: () -> Unit = {}
) {
    var selectedPeriod by remember { mutableStateOf("Month") }
    
    val totalBudget = expenses.sumOf { it.budget }
    val totalSpent = expenses.sumOf { it.spent }
    val budgetRemaining = totalBudget - totalSpent
    val spendingPercent = if (totalBudget > 0) (totalSpent / totalBudget * 100).toInt() else 0
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumThemeConfig.Semantic.primaryAction.copy(alpha = 0.02f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .slideInFromBottom(delayMillis = 0),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Budget & Expenses",
                fontSize = PremiumThemeConfig.Typography.displaySmall.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            
            Button(
                onClick = onAddExpense,
                modifier = Modifier
                    .height(40.dp)
                    .width(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumThemeConfig.Semantic.primaryAction
                ),
                shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        
        // Period Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .slideInFromBottom(delayMillis = 50),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Week", "Month", "Year").forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { selectedPeriod = period },
                    label = { Text(period) },
                    modifier = Modifier.height(36.dp)
                )
            }
        }
        
        // Budget Overview Card
        PremiumBudgetOverviewCard(
            totalBudget = totalBudget,
            totalSpent = totalSpent,
            budgetRemaining = budgetRemaining,
            spendingPercent = spendingPercent,
            modifier = Modifier.slideInFromBottom(delayMillis = 100)
        )
        
        // Summary Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .slideInFromBottom(delayMillis = 150),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PremiumKpiCard(
                label = "Spent",
                value = totalSpent,
                modifier = Modifier.weight(1f)
            )
            PremiumKpiCard(
                label = "Remaining",
                value = budgetRemaining,
                isPositive = budgetRemaining >= 0,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Categories List
        if (expenses.isNotEmpty()) {
            Text(
                text = "Expense Categories",
                fontSize = PremiumThemeConfig.Typography.titleLarge.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary,
                modifier = Modifier.slideInFromBottom(delayMillis = 200)
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .slideInFromBottom(delayMillis = 250),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(expenses) { category ->
                    PremiumExpenseCategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category) }
                    )
                }
            }
        } else {
            PremiumEmptyState(
                title = "No Budgets",
                message = "Create a budget to start tracking expenses",
                icon = Icons.Default.AccountBalanceWallet,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ============================================================================
// BUDGET OVERVIEW CARD
// ============================================================================

@Composable
fun PremiumBudgetOverviewCard(
    totalBudget: Double,
    totalSpent: Double,
    budgetRemaining: Double,
    spendingPercent: Int,
    modifier: Modifier = Modifier
) {
    PremiumElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 3
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PremiumThemeConfig.Spacing.lg.dp),
            verticalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Budget",
                        fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                        color = AppColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatIndianCurrency(totalBudget),
                        fontSize = PremiumThemeConfig.Typography.displaySmall.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = PremiumThemeConfig.Semantic.primaryActionLight,
                            shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$spendingPercent%",
                        fontSize = PremiumThemeConfig.Typography.headlineSmall.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (spendingPercent <= 75) PremiumThemeConfig.Semantic.income else PremiumThemeConfig.Semantic.warning
                    )
                }
            }
            
            Divider(color = PremiumThemeConfig.Semantic.divider, thickness = 1.dp)
            
            // Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Spent",
                        fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                        color = AppColors.textSecondary
                    )
                    Text(
                        text = formatIndianCurrency(totalSpent),
                        fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            color = PremiumThemeConfig.Semantic.expenseBg,
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = (spendingPercent / 100f).coerceIn(0f, 1f))
                            .background(
                                color = if (spendingPercent <= 75) PremiumThemeConfig.Semantic.expense else PremiumThemeConfig.Semantic.warning,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
            
            // Remaining
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = PremiumThemeConfig.Semantic.incomeBg,
                        shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
                    )
                    .padding(PremiumThemeConfig.Spacing.md.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget Remaining",
                    fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                    color = PremiumThemeConfig.Semantic.income,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatIndianCurrency(budgetRemaining),
                    fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
                    fontWeight = FontWeight.Bold,
                    color = PremiumThemeConfig.Semantic.income
                )
            }
        }
    }
}

// ============================================================================
// EXPENSE CATEGORY CARD
// ============================================================================

@Composable
fun PremiumExpenseCategoryCard(
    category: ExpenseCategory,
    onClick: () -> Unit = {}
) {
    val spentPercent = if (category.budget > 0) (category.spent / category.budget * 100).toInt() else 0
    val isOverBudget = category.spent > category.budget
    
    PremiumElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = 2,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PremiumThemeConfig.Spacing.md.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.sm.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = category.color.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.sm.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.name,
                            tint = category.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = category.name,
                            fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.textPrimary
                        )
                        Text(
                            text = "$spentPercent% of budget",
                            fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                            color = AppColors.textTertiary
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View",
                    tint = AppColors.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        color = if (isOverBudget) PremiumThemeConfig.Semantic.expenseBg else PremiumThemeConfig.Semantic.incomeBg,
                        shape = RoundedCornerShape(3.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = (spentPercent / 100f).coerceIn(0f, 1f))
                        .background(
                            color = if (isOverBudget) PremiumThemeConfig.Semantic.expense else PremiumThemeConfig.Semantic.income,
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
            
            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatIndianCurrency(category.spent),
                    fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                Text(
                    text = "of ${formatIndianCurrency(category.budget)}",
                    fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                    color = AppColors.textTertiary
                )
            }
        }
    }
}

// ============================================================================
// BUDGET CREATION DIALOG
// ============================================================================

@Composable
fun PremiumBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var budgetAmount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Budget",
                fontSize = PremiumThemeConfig.Typography.headlineSmall.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp)
            ) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
                )
                
                OutlinedTextField(
                    value = budgetAmount,
                    onValueChange = { budgetAmount = it },
                    label = { Text("Budget Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = budgetAmount.toDoubleOrNull() ?: 0.0
                    if (categoryName.isNotEmpty() && amount > 0) {
                        onConfirm(categoryName, amount)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumThemeConfig.Semantic.primaryAction
                )
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumThemeConfig.Semantic.primaryActionLight,
                    contentColor = PremiumThemeConfig.Semantic.primaryAction
                )
            ) {
                Text("Cancel")
            }
        }
    )
}
