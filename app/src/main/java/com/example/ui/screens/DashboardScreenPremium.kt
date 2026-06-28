package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LedgerEntry
import com.example.data.Voucher
import com.example.ui.AppViewModel
import com.example.ui.DashboardViewModel
import com.example.ui.animation.floatingCardEffect
import com.example.ui.animation.scaleAndFadeIn
import com.example.ui.animation.slideInFromBottom
import com.example.ui.components.PremiumBalanceCard
import com.example.ui.components.PremiumElevatedCard
import com.example.ui.components.PremiumKpiCard
import com.example.ui.components.PremiumQuickActionButton
import com.example.ui.components.PremiumTransactionCard
import com.example.ui.components.formatIndianCurrency
import com.example.ui.theme.AppColors
import com.example.ui.theme.PremiumThemeConfig
import java.text.SimpleDateFormat
import java.util.*

/**
 * Premium Dashboard Screen - Redesigned Fintech Experience
 * 
 * Features:
 * - Large balance card with animated numbers
 * - Income/expense comparison
 * - Quick action buttons
 * - Beautiful analytics charts
 * - Modern transaction list
 * - Professional reporting
 */

@Composable
fun DashboardScreenPremium(
    viewModel: AppViewModel,
    dashboardViewModel: DashboardViewModel,
    isDesktop: Boolean = false,
    onQuickAction: (String) -> Unit
) {
    val vouchers by viewModel.vouchers.collectAsState()
    val ledgerEntries by viewModel.ledgerEntries.collectAsState()
    val products by viewModel.products.collectAsState()
    
    val profile by viewModel.profile.collectAsState()
    val headerState by dashboardViewModel.headerState.collectAsState()
    
    val scrollState = rememberScrollState()
    
    // Calculate KPIs
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val todayStart = calendar.timeInMillis
    
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfMonth = calendar.timeInMillis
    
    val todaySales = remember(vouchers) {
        vouchers.filter { it.type == "SALE" && it.date >= todayStart }.sumOf { it.netAmount }
    }
    
    val todayPurchases = remember(vouchers) {
        vouchers.filter { it.type == "PURCHASE" && it.date >= todayStart }.sumOf { it.netAmount }
    }
    
    val thisMonthSales = remember(vouchers) {
        vouchers.filter { it.type == "SALE" && it.date >= firstDayOfMonth }.sumOf { it.netAmount }
    }
    
    val thisMonthPurchases = remember(vouchers) {
        vouchers.filter { it.type == "PURCHASE" && it.date >= firstDayOfMonth }.sumOf { it.netAmount }
    }
    
    // Ledger Balances
    var cashBalance by remember { mutableStateOf(0.0) }
    var bankBalance by remember { mutableStateOf(0.0) }
    var outstandingReceivable by remember { mutableStateOf(0.0) }
    var outstandingPayable by remember { mutableStateOf(0.0) }
    
    LaunchedEffect(ledgerEntries) {
        var cash = 0.0
        var bank = 0.0
        val partyBalances = mutableMapOf<String, Double>()
        
        ledgerEntries.forEach { entry ->
            val change = entry.debit - entry.credit
            when {
                entry.accountHead == "Cash" -> cash += change
                entry.accountHead == "Bank" -> bank += change
                entry.accountHead.startsWith("Party:") -> {
                    partyBalances[entry.accountHead] = (partyBalances[entry.accountHead] ?: 0.0) + change
                }
            }
        }
        
        var rec = 0.0
        var pay = 0.0
        partyBalances.forEach { (_, netBalance) ->
            if (netBalance > 0) rec += netBalance
            else if (netBalance < 0) pay += Math.abs(netBalance)
        }
        
        cashBalance = cash
        bankBalance = bank
        outstandingReceivable = rec
        outstandingPayable = pay
    }
    
    val netProfit = remember(vouchers) {
        val sales = vouchers.filter { it.type == "SALE" && it.date >= firstDayOfMonth }.sumOf { it.taxableAmount }
        val purchase = vouchers.filter { it.type == "PURCHASE" && it.date >= firstDayOfMonth }.sumOf { it.taxableAmount }
        sales - purchase
    }
    
    val lowStockProducts = remember(products) {
        products.filter { it.enableStockAlert && it.currentStock <= it.lowStockThreshold }
    }
    
    val recentTransactions = remember(vouchers) {
        vouchers.sortedByDescending { it.date }.take(5)
    }
    
    // Main Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumThemeConfig.Semantic.primaryAction.copy(alpha = 0.02f))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ====================================================================
        // HEADER SECTION
        // ====================================================================
        
        PremiumHeaderSection(
            businessName = headerState.businessName,
            fyLabel = headerState.fyLabel,
            gstin = headerState.gstin,
            modifier = Modifier.scaleAndFadeIn(delayMillis = 0)
        )
        
        // ====================================================================
        // ALERTS SECTION
        // ====================================================================
        
        if (lowStockProducts.isNotEmpty()) {
            PremiumAlertCard(
                title = "Low Stock Alert",
                message = "${lowStockProducts.size} product(s) are at or below threshold",
                type = "warning",
                modifier = Modifier.slideInFromBottom(delayMillis = 100)
            )
        }
        
        // ====================================================================
        // MAIN BALANCE CARD - PREMIUM DISPLAY
        // ====================================================================
        
        PremiumBalanceCard(
            title = "Total Balance",
            amount = cashBalance + bankBalance,
            subtitle = "Cash + Bank",
            amountColor = PremiumThemeConfig.Semantic.balancePositive,
            modifier = Modifier
                .floatingCardEffect()
                .slideInFromBottom(delayMillis = 150)
        )
        
        // ====================================================================
        // QUICK STATS ROW - KPI CARDS
        // ====================================================================
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .slideInFromBottom(delayMillis = 200),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PremiumKpiCard(
                label = "Today's Sales",
                value = todaySales,
                modifier = Modifier.weight(1f)
            )
            PremiumKpiCard(
                label = "Month Profit",
                value = netProfit,
                isPositive = netProfit >= 0,
                modifier = Modifier.weight(1f)
            )
        }
        
        // ====================================================================
        // INCOME VS EXPENSE COMPARISON
        // ====================================================================
        
        PremiumComparisonCard(
            thisMonthSales = thisMonthSales,
            thisMonthPurchases = thisMonthPurchases,
            modifier = Modifier.slideInFromBottom(delayMillis = 250)
        )
        
        // ====================================================================
        // QUICK ACTIONS
        // ====================================================================
        
        PremiumQuickActionsSection(
            onQuickAction = onQuickAction,
            modifier = Modifier.slideInFromBottom(delayMillis = 300)
        )
        
        // ====================================================================
        // CASH & BANK BALANCES
        // ====================================================================
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .slideInFromBottom(delayMillis = 350),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PremiumBalanceCard(
                title = "Cash",
                amount = cashBalance,
                amountColor = if (cashBalance >= 0) PremiumThemeConfig.Semantic.income else PremiumThemeConfig.Semantic.expense,
                modifier = Modifier.weight(1f)
            )
            PremiumBalanceCard(
                title = "Bank",
                amount = bankBalance,
                amountColor = if (bankBalance >= 0) PremiumThemeConfig.Semantic.income else PremiumThemeConfig.Semantic.expense,
                modifier = Modifier.weight(1f)
            )
        }
        
        // ====================================================================
        // RECEIVABLES & PAYABLES
        // ====================================================================
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .slideInFromBottom(delayMillis = 400),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PremiumBalanceCard(
                title = "Receivables",
                amount = outstandingReceivable,
                amountColor = PremiumThemeConfig.Semantic.income,
                modifier = Modifier.weight(1f)
            )
            PremiumBalanceCard(
                title = "Payables",
                amount = outstandingPayable,
                amountColor = PremiumThemeConfig.Semantic.expense,
                modifier = Modifier.weight(1f)
            )
        }
        
        // ====================================================================
        // RECENT TRANSACTIONS
        // ====================================================================
        
        if (recentTransactions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .slideInFromBottom(delayMillis = 450),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Recent Transactions",
                    fontSize = PremiumThemeConfig.Typography.headlineSmall.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                recentTransactions.forEach { voucher ->
                    PremiumTransactionCard(
                        title = voucher.voucherNo,
                        subtitle = formatDate(voucher.date),
                        amount = voucher.netAmount,
                        category = voucher.type,
                        isIncome = voucher.type == "SALE" || voucher.type == "RECEIPT",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ============================================================================
// PREMIUM HEADER SECTION
// ============================================================================

@Composable
fun PremiumHeaderSection(
    businessName: String,
    fyLabel: String,
    gstin: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ZeroBook",
                    fontSize = PremiumThemeConfig.Typography.headlineLarge.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                Text(
                    text = businessName.ifBlank { "Business Profile" },
                    fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                    color = AppColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "FY $fyLabel",
                    fontSize = PremiumThemeConfig.Typography.labelMedium.sp,
                    color = PremiumThemeConfig.Semantic.primaryAction,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (gstin.isBlank()) "Non-GST" else "GST: $gstin",
                    fontSize = PremiumThemeConfig.Typography.labelSmall.sp,
                    color = AppColors.textTertiary
                )
            }
        }
        
        Divider(
            color = PremiumThemeConfig.Semantic.divider,
            thickness = 1.dp
        )
    }
}

// ============================================================================
// PREMIUM ALERT CARD
// ============================================================================

@Composable
fun PremiumAlertCard(
    title: String,
    message: String,
    type: String = "info",
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (type) {
        "warning" -> Triple(
            PremiumThemeConfig.Semantic.warningBg,
            PremiumThemeConfig.Semantic.warning,
            PremiumThemeConfig.Semantic.warning.copy(alpha = 0.3f)
        )
        "error" -> Triple(
            PremiumThemeConfig.Semantic.errorBg,
            PremiumThemeConfig.Semantic.error,
            PremiumThemeConfig.Semantic.error.copy(alpha = 0.3f)
        )
        else -> Triple(
            PremiumThemeConfig.Semantic.infoBg,
            PremiumThemeConfig.Semantic.info,
            PremiumThemeConfig.Semantic.info.copy(alpha = 0.3f)
        )
    }
    
    PremiumElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 1,
        backgroundColor = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PremiumThemeConfig.Spacing.md.dp),
            horizontalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (type) {
                    "warning" -> Icons.Default.Warning
                    "error" -> Icons.Default.Error
                    else -> Icons.Default.Info
                },
                contentDescription = title,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = PremiumThemeConfig.Typography.labelLarge.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = message,
                    fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                    color = textColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// ============================================================================
// INCOME VS EXPENSE COMPARISON CARD
// ============================================================================

@Composable
fun PremiumComparisonCard(
    thisMonthSales: Double,
    thisMonthPurchases: Double,
    modifier: Modifier = Modifier
) {
    val total = thisMonthSales + thisMonthPurchases
    val salesPercent = if (total > 0) (thisMonthSales / total * 100).toInt() else 0
    val purchasePercent = if (total > 0) (thisMonthPurchases / total * 100).toInt() else 0
    
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
                text = "This Month Overview",
                fontSize = PremiumThemeConfig.Typography.titleLarge.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            
            // Sales Row
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sales",
                        fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
                        color = AppColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatIndianCurrency(thisMonthSales),
                        fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
                        color = PremiumThemeConfig.Semantic.income,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(PremiumThemeConfig.Semantic.incomeBg, RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = (salesPercent / 100f).coerceIn(0f, 1f))
                            .background(PremiumThemeConfig.Semantic.income, RoundedCornerShape(3.dp))
                    )
                }
            }
            
            // Purchases Row
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Purchases",
                        fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
                        color = AppColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatIndianCurrency(thisMonthPurchases),
                        fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
                        color = PremiumThemeConfig.Semantic.expense,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(PremiumThemeConfig.Semantic.expenseBg, RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = (purchasePercent / 100f).coerceIn(0f, 1f))
                            .background(PremiumThemeConfig.Semantic.expense, RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

// ============================================================================
// QUICK ACTIONS SECTION
// ============================================================================

@Composable
fun PremiumQuickActionsSection(
    onQuickAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp)
    ) {
        Text(
            text = "Quick Actions",
            fontSize = PremiumThemeConfig.Typography.titleLarge.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp)
        ) {
            PremiumQuickActionButton(
                label = "New Sale",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Sale",
                        tint = PremiumThemeConfig.Semantic.primaryAction,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = { onQuickAction("SALE") },
                modifier = Modifier.weight(1f)
            )
            
            PremiumQuickActionButton(
                label = "New Purchase",
                icon = {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "New Purchase",
                        tint = PremiumThemeConfig.Semantic.primaryAction,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = { onQuickAction("PURCHASE") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp)
        ) {
            PremiumQuickActionButton(
                label = "View Reports",
                icon = {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Reports",
                        tint = PremiumThemeConfig.Semantic.primaryAction,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = { onQuickAction("REPORTS") },
                modifier = Modifier.weight(1f)
            )
            
            PremiumQuickActionButton(
                label = "Manage Stock",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "Stock",
                        tint = PremiumThemeConfig.Semantic.primaryAction,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = { onQuickAction("PRODUCTS") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
