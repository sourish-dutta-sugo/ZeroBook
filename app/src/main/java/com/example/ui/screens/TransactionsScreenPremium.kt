package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.Voucher
import com.example.ui.animation.slideInFromBottom
import com.example.ui.components.PremiumCategoryBadge
import com.example.ui.components.PremiumElevatedCard
import com.example.ui.components.PremiumTransactionCard
import com.example.ui.components.formatIndianCurrency
import com.example.ui.theme.AppColors
import com.example.ui.theme.PremiumThemeConfig
import java.text.SimpleDateFormat
import java.util.*

/**
 * Premium Transactions Screen - Modern Transaction Management
 * 
 * Features:
 * - Smart category filtering
 * - Modern transaction cards with icons
 * - Date grouping
 * - Search and filtering
 * - Transaction details
 */

@Composable
fun TransactionsScreenPremium(
    vouchers: List<Voucher>,
    onTransactionClick: (Voucher) -> Unit = {},
    onEdit: (Voucher) -> Unit = {},
    onDelete: (Voucher) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    val categories = listOf("All", "SALE", "PURCHASE", "RECEIPT", "PAYMENT", "RETURN")
    
    // Filter transactions
    val filteredVouchers = remember(vouchers, selectedCategory, searchQuery) {
        vouchers
            .filter { selectedCategory == null || selectedCategory == "All" || it.type == selectedCategory }
            .filter { 
                searchQuery.isEmpty() || 
                it.voucherNo.contains(searchQuery, ignoreCase = true) ||
                (it.partyId?.contains(searchQuery, ignoreCase = true) == true)
            }
            .sortedByDescending { it.date }
    }
    
    // Group by date
    val groupedTransactions = remember(filteredVouchers) {
        filteredVouchers.groupBy { formatDateGroup(it.date) }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumThemeConfig.Semantic.primaryAction.copy(alpha = 0.02f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Transactions",
            fontSize = PremiumThemeConfig.Typography.displaySmall.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary
        )
        
        // Search Bar
        PremiumSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search by voucher or party...",
            modifier = Modifier.slideInFromBottom(delayMillis = 50)
        )
        
        // Category Filter
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .slideInFromBottom(delayMillis = 100),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                PremiumCategoryBadge(
                    label = category,
                    isSelected = selectedCategory == category || (category == "All" && selectedCategory == null),
                    onClick = {
                        selectedCategory = if (category == "All") null else category
                    }
                )
            }
        }
        
        // Transactions List
        if (filteredVouchers.isEmpty()) {
            PremiumEmptyState(
                title = "No Transactions",
                message = "Start by creating a new sale or purchase transaction",
                icon = Icons.Default.Receipt,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .slideInFromBottom(delayMillis = 150),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedTransactions.forEach { (dateGroup, transactions) ->
                    item {
                        Text(
                            text = dateGroup,
                            fontSize = PremiumThemeConfig.Typography.labelLarge.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.textSecondary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    
                    items(transactions) { voucher ->
                        PremiumTransactionListItem(
                            voucher = voucher,
                            onClick = { onTransactionClick(voucher) },
                            onEdit = { onEdit(voucher) },
                            onDelete = { onDelete(voucher) }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ============================================================================
// PREMIUM SEARCH BAR
// ============================================================================

@Composable
fun PremiumSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        placeholder = {
            Text(
                text = placeholder,
                fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
                color = AppColors.textTertiary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = AppColors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = AppColors.textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PremiumThemeConfig.Semantic.primaryAction,
            unfocusedBorderColor = PremiumThemeConfig.Semantic.borderLight,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = PremiumThemeConfig.Typography.bodyMedium.sp
        )
    )
}

// ============================================================================
// PREMIUM TRANSACTION LIST ITEM
// ============================================================================

@Composable
fun PremiumTransactionListItem(
    voucher: Voucher,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val (categoryColor, categoryIcon) = getCategoryStyle(voucher.type)
    val isIncome = voucher.type in listOf("SALE", "RECEIPT")
    
    PremiumElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        elevation = 2,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(PremiumThemeConfig.Spacing.md.dp),
            horizontalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = categoryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = voucher.type,
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Transaction Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = voucher.voucherNo,
                        fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary
                    )
                    Text(
                        text = formatIndianCurrency(voucher.netAmount),
                        fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) PremiumThemeConfig.Semantic.income else PremiumThemeConfig.Semantic.expense
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${voucher.type} • ${voucher.partyId ?: "Cash"}",
                        fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                        color = AppColors.textTertiary,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = formatDate(voucher.date),
                        fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                        color = AppColors.textTertiary,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            
            // Menu
            Box {
                IconButton(
                    onClick = { showMenu = !showMenu },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = AppColors.textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            onEdit()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    )
                }
            }
        }
    }
}

// ============================================================================
// PREMIUM EMPTY STATE
// ============================================================================

@Composable
fun PremiumEmptyState(
    title: String,
    message: String,
    icon: androidx.compose.material.icons.materialIcon? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(PremiumThemeConfig.Spacing.lg.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = PremiumThemeConfig.Semantic.primaryActionLight,
                        shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.lg.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PremiumThemeConfig.Semantic.primaryAction,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Text(
            text = title,
            fontSize = PremiumThemeConfig.Typography.headlineSmall.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Text(
            text = message,
            fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
            color = AppColors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================

fun getCategoryStyle(type: String): Pair<Color, androidx.compose.material.icons.materialIcon> {
    return when (type) {
        "SALE" -> Pair(PremiumThemeConfig.Semantic.income, Icons.Default.TrendingUp)
        "PURCHASE" -> Pair(PremiumThemeConfig.Semantic.expense, Icons.Default.ShoppingCart)
        "RECEIPT" -> Pair(PremiumThemeConfig.Semantic.income, Icons.Default.AttachMoney)
        "PAYMENT" -> Pair(PremiumThemeConfig.Semantic.expense, Icons.Default.Payment)
        "RETURN" -> Pair(PremiumThemeConfig.Semantic.warning, Icons.Default.Undo)
        else -> Pair(PremiumThemeConfig.Semantic.neutral, Icons.Default.Receipt)
    }
}

fun formatDateGroup(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
