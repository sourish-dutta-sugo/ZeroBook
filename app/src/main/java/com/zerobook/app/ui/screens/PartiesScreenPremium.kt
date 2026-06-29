package com.zerobook.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerobook.app.ui.animation.slideInFromBottom
import com.zerobook.app.ui.components.*
import com.zerobook.app.ui.theme.AppColors
import com.zerobook.app.ui.theme.PremiumThemeConfig

/**
 * Premium Parties (Customers/Suppliers) Screen
 * 
 * Features:
 * - Party list with balance display
 * - Customer vs Supplier filtering
 * - Quick actions (call, message, invoice)
 * - Balance status indicators
 * - Transaction history
 */

data class PremiumParty(
    val id: String,
    val name: String,
    val type: String, // "CUSTOMER" or "SUPPLIER"
    val phone: String = "",
    val email: String = "",
    val balance: Double,
    val lastTransaction: Long = 0L
)

@Composable
fun PartiesScreenPremium(
    parties: List<PremiumParty> = emptyList(),
    onPartyClick: (PremiumParty) -> Unit = {},
    onAddParty: () -> Unit = {}
) {
    var selectedType by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredParties = remember(parties, selectedType, searchQuery) {
        parties
            .filter { selectedType == null || it.type == selectedType }
            .filter { 
                searchQuery.isEmpty() || 
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true)
            }
            .sortedByDescending { it.balance }
    }
    
    val customersCount = parties.count { it.type == "CUSTOMER" }
    val suppliersCount = parties.count { it.type == "SUPPLIER" }
    val totalReceivable = parties.filter { it.type == "CUSTOMER" && it.balance > 0 }.sumOf { it.balance }
    val totalPayable = parties.filter { it.type == "SUPPLIER" && it.balance < 0 }.sumOf { kotlin.math.abs(it.balance) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumThemeConfig.Semantic.primaryAction.copy(alpha = 0.02f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                text = "Parties",
                fontSize = PremiumThemeConfig.Typography.displaySmall.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
            
            Button(
                onClick = onAddParty,
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
        
        // Search Bar
        PremiumSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search by name or phone...",
            modifier = Modifier.slideInFromBottom(delayMillis = 50)
        )
        
        // Type Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .slideInFromBottom(delayMillis = 100),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PremiumCategoryBadge(
                label = "All (${ parties.size})",
                isSelected = selectedType == null,
                onClick = { selectedType = null }
            )
            PremiumCategoryBadge(
                label = "Customers ($customersCount)",
                isSelected = selectedType == "CUSTOMER",
                onClick = { selectedType = "CUSTOMER" }
            )
            PremiumCategoryBadge(
                label = "Suppliers ($suppliersCount)",
                isSelected = selectedType == "SUPPLIER",
                onClick = { selectedType = "SUPPLIER" }
            )
        }
        
        // Summary Cards
        if (filteredParties.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .slideInFromBottom(delayMillis = 150),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PremiumElevatedCard(
                    modifier = Modifier.weight(1f),
                    elevation = 2
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PremiumThemeConfig.Spacing.md.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Receivable",
                            fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                            color = AppColors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatIndianCurrency(totalReceivable),
                            fontSize = PremiumThemeConfig.Typography.headlineSmall.sp,
                            fontWeight = FontWeight.Bold,
                            color = PremiumThemeConfig.Semantic.income
                        )
                    }
                }
                
                PremiumElevatedCard(
                    modifier = Modifier.weight(1f),
                    elevation = 2
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PremiumThemeConfig.Spacing.md.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Payable",
                            fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                            color = AppColors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatIndianCurrency(totalPayable),
                            fontSize = PremiumThemeConfig.Typography.headlineSmall.sp,
                            fontWeight = FontWeight.Bold,
                            color = PremiumThemeConfig.Semantic.expense
                        )
                    }
                }
            }
        }
        
        // Parties List
        if (filteredParties.isEmpty()) {
            PremiumEmptyState(
                title = "No Parties",
                message = "Add customers or suppliers to get started",
                icon = Icons.Default.People,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .slideInFromBottom(delayMillis = 200),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredParties) { party ->
                    PremiumPartyCard(
                        party = party,
                        onClick = { onPartyClick(party) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ============================================================================
// PARTY CARD
// ============================================================================

@Composable
fun PremiumPartyCard(
    party: PremiumParty,
    onClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val isCustomer = party.type == "CUSTOMER"
    val balanceColor = if (party.balance >= 0) PremiumThemeConfig.Semantic.income else PremiumThemeConfig.Semantic.expense
    val balanceLabel = if (isCustomer) "Receivable" else "Payable"
    
    PremiumElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
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
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isCustomer) PremiumThemeConfig.Semantic.incomeBg else PremiumThemeConfig.Semantic.expenseBg,
                        shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCustomer) Icons.Default.Person else Icons.Default.Business,
                    contentDescription = party.type,
                    tint = if (isCustomer) PremiumThemeConfig.Semantic.income else PremiumThemeConfig.Semantic.expense,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Party Details
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
                        text = party.name,
                        fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary
                    )
                    Text(
                        text = formatIndianCurrency(kotlin.math.abs(party.balance)),
                        fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
                        fontWeight = FontWeight.Bold,
                        color = balanceColor
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = party.phone.ifEmpty { "No phone" },
                        fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                        color = AppColors.textTertiary,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = balanceLabel,
                        fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                        color = balanceColor,
                        fontWeight = FontWeight.SemiBold
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
                        text = { Text("Call") },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Call, contentDescription = "Call") }
                    )
                    DropdownMenuItem(
                        text = { Text("Message") },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Message, contentDescription = "Message") }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                    )
                }
            }
        }
    }
}

// ============================================================================
// PARTY DETAILS SHEET
// ============================================================================

@Composable
fun PremiumPartyDetailsSheet(
    party: PremiumParty,
    onDismiss: () -> Unit,
    onCall: () -> Unit = {},
    onMessage: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val isCustomer = party.type == "CUSTOMER"
    val balanceColor = if (party.balance >= 0) PremiumThemeConfig.Semantic.income else PremiumThemeConfig.Semantic.expense
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = PremiumThemeConfig.Semantic.cardElevated,
                shape = RoundedCornerShape(
                    topStart = PremiumThemeConfig.BorderRadius.lg.dp,
                    topEnd = PremiumThemeConfig.BorderRadius.lg.dp
                )
            )
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
                    text = party.name,
                    fontSize = PremiumThemeConfig.Typography.headlineSmall.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                Text(
                    text = if (isCustomer) "Customer" else "Supplier",
                    fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                    color = AppColors.textSecondary
                )
            }
            
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        
        Divider(color = PremiumThemeConfig.Semantic.divider, thickness = 1.dp)
        
        // Balance
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = balanceColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
                )
                .padding(PremiumThemeConfig.Spacing.md.dp)
        ) {
            Column {
                Text(
                    text = if (isCustomer) "Amount Due" else "Amount to Pay",
                    fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                    color = AppColors.textSecondary
                )
                Text(
                    text = formatIndianCurrency(kotlin.math.abs(party.balance)),
                    fontSize = PremiumThemeConfig.Typography.displaySmall.sp,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor
                )
            }
        }
        
        // Contact Info
        if (party.phone.isNotEmpty()) {
            PremiumContactRow(
                icon = Icons.Default.Phone,
                label = "Phone",
                value = party.phone,
                onClick = onCall
            )
        }
        
        if (party.email.isNotEmpty()) {
            PremiumContactRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = party.email,
                onClick = onMessage
            )
        }
        
        Divider(color = PremiumThemeConfig.Semantic.divider, thickness = 1.dp)
        
        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp)
        ) {
            Button(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumThemeConfig.Semantic.primaryActionLight,
                    contentColor = PremiumThemeConfig.Semantic.primaryAction
                ),
                shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
            }
            
            Button(
                onClick = onDelete,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumThemeConfig.Semantic.errorBg,
                    contentColor = PremiumThemeConfig.Semantic.error
                ),
                shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun PremiumContactRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = PremiumThemeConfig.Semantic.primaryActionLight,
                shape = RoundedCornerShape(PremiumThemeConfig.BorderRadius.md.dp)
            )
            .clickable(enabled = true) { onClick() }
            .padding(horizontal = PremiumThemeConfig.Spacing.md.dp),
        horizontalArrangement = Arrangement.spacedBy(PremiumThemeConfig.Spacing.md.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = PremiumThemeConfig.Semantic.primaryAction,
            modifier = Modifier.size(20.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = PremiumThemeConfig.Typography.bodySmall.sp,
                color = AppColors.textSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = PremiumThemeConfig.Typography.bodyMedium.sp,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
