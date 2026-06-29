package com.zerobook.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import com.zerobook.app.data.Voucher
import com.zerobook.app.ui.animation.slideInFromBottom
import com.zerobook.app.ui.components.PremiumElevatedCard
import com.zerobook.app.ui.components.PremiumKpiCard
import com.zerobook.app.ui.components.formatIndianCurrency
import com.zerobook.app.ui.theme.AppColors
import com.zerobook.app.ui.theme.PremiumThemeConfig
import java.text.SimpleDateFormat
import java.util.*

/**
 * Premium Reports Screen - Professional Financial Reporting
 * 
 * Features:
 * - Visual financial summaries
 * - Monthly/yearly comparisons
 * - Profit & loss analysis
 * - Export actions
 * - Tax compliance reports
 */

@Composable
fun ReportsScreenPremium(
    vouchers: List<Voucher>,
    onExport: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    
    // Calculate metrics
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfMonth = calendar.timeInMillis
    
    val thisMonthSales = remember(vouchers) {
        vouchers.filter { it.type == "SALE" && it.date >= firstDayOfMonth }.sumOf { it.netAmount }
    }
    
    val thisMonthPurchases = remember(vouchers) {
        vouchers.filter { it.type == "PURCHASE" && it.date >= firstDayOfMonth }.sumOf { it.netAmount }
    }
    
    val thisMonthGST = remember(vouchers) {
        vouchers.filter { it.date >= firstDayOfMonth }.sumOf { it.cgst + it.sgst + it.igst }
    }
    
    val totalSales = remember(vouchers) {
        vouchers.filter { it.type == "SALE" }.sumOf { it.netAmount }
    }
    
    val totalPurchases = remember(vouchers) {
        vouchers.filter { it.type == "PURCHASE" }.sumOf { it.netAmount }
    }
    
    val grossProfit = remember(vouchers) {
        val sales = vouchers.filter { it.type == "SALE" }.sumOf { it.taxableAmount }
        val purchase = vouchers.filter { it.type == "PURCHASE" }.sumOf { it.taxableAmount }
        sales - purchase
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumThemeConfig.Semantic.primaryAction.copy(alpha = 0.02f))
            .verticalScroll(scrollState)
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
                text = "Financial Reports",
                fontSize = PremiumThemeConfig.Typography.displaySmall.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            
            IconButton(
                onClick = { onExport("PDF") },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = PremiumThemeConfig.Semantic.primaryActionLight,
                        shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Export",
                    tint = PremiumThemeConfig.Semantic.primaryAction,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // ====================================================================
        // SUMMARY CARDS
        // ====================================================================
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .slideInFromBottom(delayMillis = 50),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "This Month",
                fontSize = PremiumThemeConfig.Typography.titleLarge.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PremiumKpiCard(
                    label = "Sales",
                    value = thisMonthSales,
                    modifier = Modifier.weight(1f)
                )
                PremiumKpiCard(
                    label = "Purchases",
                    value = thisMonthPurchases,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PremiumKpiCard(
                    label = "GST Collected",
                    value = thisMonthGST,
                    modifier = Modifier.weight(1f)
                )
                PremiumKpiCard(
                    label = "Gross Profit",
                    value = thisMonthSales - thisMonthPurchases,
                    isPositive = (thisMonthSales - thisMonthPurchases) >= 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // ====================================================================
        // PROFIT & LOSS STATEMENT
        // ====================================================================
        
        PremiumProfitLossCard(
            sales = totalSales,
            purchases = totalPurchases,
            gst = thisMonthGST,
            modifier = Modifier.slideInFromBottom(delayMillis = 100)
        )
        
        // ====================================================================
        // SALES BREAKDOWN
        // ====================================================================
        
        PremiumSalesBreakdownCard(
            vouchers = vouchers,
            modifier = Modifier.slideInFromBottom(delayMillis = 150)
        )
        
        // ====================================================================
        // TAX COMPLIANCE
        // ====================================================================
        
        PremiumTaxComplianceCard(
            vouchers = vouchers,
            modifier = Modifier.slideInFromBottom(delayMillis = 200)
        )
        
        // ====================================================================
        // EXPORT OPTIONS
        // ====================================================================
        
        PremiumExportOptionsCard(
            onExport = onExport,
            modifier = Modifier.slideInFromBottom(delayMillis = 250)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ============================================================================
// PROFIT & LOSS CARD
// ============================================================================

@Composable
fun PremiumProfitLossCard(
    sales: Double,
    purchases: Double,
    gst: Double,
    modifier: Modifier = Modifier
) {
    val grossProfit = sales - purchases
    val netProfit = grossProfit - gst
    
    PremiumElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 2
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PremiumThemeConfig.Spacing.lg.dp),
            verticalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp)
        ) {
            Text(
                text = "Profit & Loss Statement",
                fontSize = PremiumThemeConfig.Typography.titleLarge.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            
            Divider(color = PremiumThemeConfig.Semantic.divider, thickness = 1.dp)
            
            // Sales
            PremiumPLRow(
                label = "Total Sales",
                amount = sales,
                isHighlight = false
            )
            
            // Purchases
            PremiumPLRow(
                label = "Total Purchases",
                amount = purchases,
                isHighlight = false,
                isExpense = true
            )
            
            Divider(color = PremiumThemeConfig.Semantic.divider, thickness = 1.dp)
            
            // Gross Profit
            PremiumPLRow(
                label = "Gross Profit",
                amount = grossProfit,
                isHighlight = true,
                isPositive = grossProfit >= 0
            )
            
            // GST
            PremiumPLRow(
                label = "GST Collected",
                amount = gst,
                isHighlight = false,
                isExpense = true
            )
            
            Divider(color = PremiumThemeConfig.Semantic.divider, thickness = 1.dp)
            
            // Net Profit
            PremiumPLRow(
                label = "Net Profit",
                amount = netProfit,
                isHighlight = true,
                isPositive = netProfit >= 0
            )
        }
    }
}

@Composable
fun PremiumPLRow(
    label: String,
    amount: Double,
    isHighlight: Boolean = false,
    isExpense: Boolean = false,
    isPositive: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isHighlight) PremiumThemeConfig.Typography.bodyMedium.sp else PremiumThemeConfig.Typography.bodySmall.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = AppColors.textPrimary
        )
        
        Text(
            text = formatIndianCurrency(amount),
            fontSize = if (isHighlight) PremiumThemeConfig.Typography.bodyMedium.sp else PremiumThemeConfig.Typography.bodySmall.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
            color = when {
                isHighlight && isPositive -> PremiumThemeConfig.Semantic.income
                isHighlight && !isPositive -> PremiumThemeConfig.Semantic.expense
                isExpense -> PremiumThemeConfig.Semantic.expense
                else -> AppColors.textPrimary
            }
        )
    }
}

// ============================================================================
// SALES BREAKDOWN CARD
// ============================================================================

@Composable
fun PremiumSalesBreakdownCard(
    vouchers: List<Voucher>,
    modifier: Modifier = Modifier
) {
    val salesCount = vouchers.filter { it.type == "SALE" }.size
    val purchaseCount = vouchers.filter { it.type == "PURCHASE" }.size
    val returnCount = vouchers.filter { it.type == "RETURN" }.size
    
    PremiumElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 2
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PremiumThemeConfig.Spacing.lg.dp),
            verticalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp)
        ) {
            Text(
                text = "Transaction Breakdown",
                fontSize = PremiumThemeConfig.Typography.titleLarge.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp)
            ) {
                PremiumBreakdownItem(
                    label = "Sales",
                    count = salesCount,
                    color = PremiumThemeConfig.Semantic.income,
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                PremiumBreakdownItem(
                    label = "Purchases",
                    count = purchaseCount,
                    color = PremiumThemeConfig.Semantic.expense,
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f)
                )
                PremiumBreakdownItem(
                    label = "Returns",
                    count = returnCount,
                    color = PremiumThemeConfig.Semantic.warning,
                    icon = Icons.Default.Undo,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PremiumBreakdownItem(
    label: String,
    count: Int,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
            )
            .padding(PremiumThemeConfig.Spacing.md.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.sm.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = count.toString(),
            fontSize = PremiumThemeConfig.Typography.headlineSmall.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
            color = AppColors.textSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

// ============================================================================
// TAX COMPLIANCE CARD
// ============================================================================

@Composable
fun PremiumTaxComplianceCard(
    vouchers: List<Voucher>,
    modifier: Modifier = Modifier
) {
    val totalCGST = vouchers.sumOf { it.cgst }
    val totalSGST = vouchers.sumOf { it.sgst }
    val totalIGST = vouchers.sumOf { it.igst }
    val totalGST = totalCGST + totalSGST + totalIGST
    
    PremiumElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 2
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PremiumThemeConfig.Spacing.lg.dp),
            verticalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp)
        ) {
            Text(
                text = "GST Compliance",
                fontSize = PremiumThemeConfig.Typography.titleLarge.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            
            Divider(color = PremiumThemeConfig.Semantic.divider, thickness = 1.dp)
            
            PremiumPLRow(label = "CGST", amount = totalCGST)
            PremiumPLRow(label = "SGST", amount = totalSGST)
            PremiumPLRow(label = "IGST", amount = totalIGST)
            
            Divider(color = PremiumThemeConfig.Semantic.divider, thickness = 1.dp)
            
            PremiumPLRow(
                label = "Total GST",
                amount = totalGST,
                isHighlight = true
            )
        }
    }
}

// ============================================================================
// EXPORT OPTIONS CARD
// ============================================================================

@Composable
fun PremiumExportOptionsCard(
    onExport: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 2
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PremiumThemeConfig.Spacing.lg.dp),
            verticalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp)
        ) {
            Text(
                text = "Export Reports",
                fontSize = PremiumThemeConfig.Typography.titleLarge.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp)
            ) {
                PremiumExportButton(
                    label = "PDF",
                    icon = Icons.Default.Description,
                    onClick = { onExport("PDF") },
                    modifier = Modifier.weight(1f)
                )
                PremiumExportButton(
                    label = "Excel",
                    icon = Icons.Default.TableChart,
                    onClick = { onExport("EXCEL") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PremiumExportButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PremiumThemeConfig.Semantic.primaryActionLight,
            contentColor = PremiumThemeConfig.Semantic.primaryAction
        ),
        shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = PremiumThemeConfig.Typography.labelLarge.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
