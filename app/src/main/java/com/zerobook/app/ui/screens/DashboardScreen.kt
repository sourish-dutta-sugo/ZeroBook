package com.zerobook.app.ui.screens
import com.zerobook.app.ui.theme.AppColors

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.unit.sp
import com.zerobook.app.R
import com.zerobook.app.data.AppPreferences
import com.zerobook.app.data.LedgerEntry
import com.zerobook.app.data.Utils
import com.zerobook.app.data.Voucher
import com.zerobook.app.ui.AppViewModel
import com.zerobook.app.ui.DashboardViewModel
import com.zerobook.app.ui.animation.premiumClickable
import com.zerobook.app.ui.theme.Colors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
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

    var searchQuery by remember { mutableStateOf("") }
    var showProgressTracker by remember { mutableStateOf(false) }
    var progressMetric by remember { mutableStateOf("Sales") }
    var progressPeriod by remember { mutableStateOf("Monthly") }
    var progressTarget by remember { mutableStateOf(200000.0) }
    var activeTransactionFilter by remember { mutableStateOf("All Transactions") }
    var activeTransactionSort by remember { mutableStateOf("Newest First") }
    var showProgressDetails by remember { mutableStateOf(false) }
    var selectedAnalyticsCard by remember { mutableStateOf<KpiDetails?>(null) }
    var analyticsFilterByCard by remember { mutableStateOf(mapOf<String, String>()) }
    var showTransactionFilterMenu by remember { mutableStateOf(false) }
    var showTransactionSortMenu by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Date computation boundaries
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val todayStart = calendar.timeInMillis

    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfMonth = calendar.timeInMillis

    // Calculates KPIs
    val todaySales = remember(vouchers) {
        vouchers.filter { it.type == "SALE" && it.date >= todayStart }.sumOf { it.netAmount }
    }
    val todayPurchases = remember(vouchers) {
        vouchers.filter { it.type == "PURCHASE" && it.date >= todayStart }.sumOf { it.netAmount }
    }
    val thisMonthSales = remember(vouchers) {
        vouchers.filter { it.type == "SALE" && it.date >= firstDayOfMonth }.sumOf { it.netAmount }
    }

    // Ledger Balances calculation
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
            if (netBalance > 0) {
                rec += netBalance
            } else if (netBalance < 0) {
                pay += Math.abs(netBalance)
            }
        }

        cashBalance = cash
        bankBalance = bank
        outstandingReceivable = rec
        outstandingPayable = pay
    }

    val netProfit = remember(vouchers) {
        val sales = vouchers.filter { it.type == "SALE" && it.date >= firstDayOfMonth }.sumOf { it.taxableAmount }
        val purchase = vouchers.filter { it.type == "PURCHASE" && it.date >= firstDayOfMonth }.sumOf { it.taxableAmount }
        sales - purchase // Gross margin approximation as profit
    }
    val lowStockProducts = remember(products) {
        products.filter { it.enableStockAlert && it.currentStock <= it.lowStockThreshold }
    }

    LaunchedEffect(Unit) {
        showProgressTracker = AppPreferences.isProgressTrackerEnabled(context)
        progressMetric = AppPreferences.getProgressTrackerMetric(context)
        progressPeriod = AppPreferences.getProgressTrackerPeriod(context)
        progressTarget = AppPreferences.getProgressTrackerTarget(context).toDoubleOrNull() ?: 200000.0
    }

    val visibleTransactions = remember(vouchers, activeTransactionFilter, activeTransactionSort) {
        val filtered = when (activeTransactionFilter) {
            "Sales" -> vouchers.filter { it.type == "SALE" }
            "Purchase" -> vouchers.filter { it.type == "PURCHASE" }
            "Receipt" -> vouchers.filter { it.type == "RECEIPT" }
            "Payment" -> vouchers.filter { it.type == "PAYMENT" }
            "Income" -> vouchers.filter { it.type == "SALE" || it.type == "RECEIPT" }
            "Expense" -> vouchers.filter { it.type == "PURCHASE" || it.type == "PAYMENT" }
            "Receivable" -> vouchers.filter { (it.type == "SALE" || it.type == "RECEIPT") && it.outstandingAmount > 0 }
            "Payable" -> vouchers.filter { (it.type == "PURCHASE" || it.type == "PAYMENT") && it.outstandingAmount > 0 }
            "Due" -> vouchers.filter { it.outstandingAmount > 0 }
            "Cancelled" -> vouchers.filter { it.status == "DRAFT" }
            "Draft" -> vouchers.filter { it.status == "DRAFT" }
            "GST Transactions" -> vouchers.filter { it.cgst + it.sgst + it.igst > 0.0 }
            else -> vouchers
        }
        when (activeTransactionSort) {
            "Oldest First" -> filtered.sortedBy { it.date }
            "Amount (High → Low)" -> filtered.sortedByDescending { it.netAmount }
            "Amount (Low → High)" -> filtered.sortedBy { it.netAmount }
            "Voucher Number (Ascending)" -> filtered.sortedBy { it.voucherNo.ifBlank { it.type } }
            "Voucher Number (Descending)" -> filtered.sortedByDescending { it.voucherNo.ifBlank { it.type } }
            "Party Name (A → Z)" -> filtered.sortedBy { it.partyId ?: "Cash" }
            "Party Name (Z → A)" -> filtered.sortedByDescending { it.partyId ?: "Cash" }
            else -> filtered.sortedByDescending { it.date }
        }
    }

    val recentTransactions = remember(visibleTransactions) {
        visibleTransactions.take(8)
    }

    val showGstCard = remember(profile) { profile?.gstin?.isNotBlank() == true }

    BackHandler(enabled = searchQuery.isNotBlank()) {
        searchQuery = ""
    }

    BackHandler(enabled = selectedAnalyticsCard != null) {
        selectedAnalyticsCard = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.screenBg)
            .verticalScroll(scrollState)
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // High Density Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_icon),
                    contentDescription = "ZeroBook",
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "ZeroBook",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "FY ${headerState.fyLabel}",
                    fontSize = 11.sp,
                    color = AppColors.primary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = headerState.businessName.ifBlank { "Business Profile" },
                    fontSize = 10.sp,
                    color = AppColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (headerState.gstin.isBlank()) "Non-GST" else "GST: ${headerState.gstin}",
                    fontSize = 10.sp,
                    color = AppColors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        HorizontalDivider(color = AppColors.divider, thickness = 1.dp)

        if (lowStockProducts.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                border = BorderStroke(1.dp, Color(0xFFF59E0B))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Low stock alert", fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                    Text(
                        "${lowStockProducts.size} product(s) are at or below threshold.",
                        color = Color(0xFF92400E),
                        fontSize = 12.sp
                    )
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search vouchers, ledger, or stock...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .clickable {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1A73E8),
                unfocusedBorderColor = Color(0xFFE8E8E8),
                focusedContainerColor = Color(0xFFF8F9FA),
                unfocusedContainerColor = Color(0xFFF8F9FA)
            ),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
        )

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.lowercase()
            
            val foundVouchers = vouchers.filter { 
                it.voucherNo.lowercase().contains(q) || 
                (it.partyId?.lowercase()?.contains(q) == true) || 
                it.type.lowercase().contains(q) || 
                Utils.formatDate(it.date).lowercase().contains(q) 
            }.take(5)
            
            val foundLedger = ledgerEntries.filter { 
                it.id.lowercase().contains(q) || 
                it.accountHead.lowercase().contains(q) || 
                (it.narration?.lowercase()?.contains(q) == true) || 
                Utils.formatDate(it.date).lowercase().contains(q) 
            }.take(5)
            
            val foundProducts = products.filter {
                it.id.lowercase().contains(q) ||
                it.name.lowercase().contains(q) ||
                (it.hsnCode?.lowercase()?.contains(q) == true)
            }.take(5)

            if (foundVouchers.isEmpty() && foundLedger.isEmpty() && foundProducts.isEmpty()) {
                Text("No results found.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE8E8E8)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (foundVouchers.isNotEmpty()) {
                            Text("Vouchers", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A73E8))
                            foundVouchers.forEach { v ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${v.voucherNo} • ${v.type}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${v.partyId ?: "Cash"} • ${Utils.formatDate(v.date)}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text(Utils.formatIndianCurrency(v.netAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                                }
                                HorizontalDivider(color = Color(0xFFF2F2F2), thickness = 0.5.dp)
                            }
                        }
                        
                        if (foundLedger.isNotEmpty()) {
                            if (foundVouchers.isNotEmpty()) Spacer(modifier = Modifier.height(4.dp))
                            Text("Ledger Entries", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9C27B0))
                            foundLedger.forEach { l ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(l.accountHead, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text(l.narration ?: "No Narration", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    val amt = if (l.debit > 0) "Dr. ${Utils.formatIndianCurrency(l.debit)}" else "Cr. ${Utils.formatIndianCurrency(l.credit)}"
                                    Text(amt, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (l.debit > 0) Color(0xFFDC3545) else Color(0xFF28A745))
                                }
                                HorizontalDivider(color = Color(0xFFF2F2F2), thickness = 0.5.dp)
                            }
                        }
                        
                        if (foundProducts.isNotEmpty()) {
                            if (foundVouchers.isNotEmpty() || foundLedger.isNotEmpty()) Spacer(modifier = Modifier.height(4.dp))
                            Text("Stock Items", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF28A745))
                            foundProducts.forEach { p ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${p.name}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("HSN: ${p.hsnCode ?: "N/A"} • Stock: ${p.openingStock}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text(Utils.formatIndianCurrency(p.saleRate), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                                }
                                HorizontalDivider(color = Color(0xFFF2F2F2), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
        
        if (searchQuery.isBlank()) {
            if (showProgressTracker) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .premiumClickable { showProgressDetails = true },
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${progressMetric} Progress", fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                            Text("${String.format(Locale.US, "%.2f", ((netProfit / progressTarget.coerceAtLeast(1.0)) * 100.0).coerceIn(0.0, 100.0))}%", fontWeight = FontWeight.Bold, color = AppColors.primary)
                        }
                        LinearProgressIndicator(
                            progress = { ((netProfit / progressTarget.coerceAtLeast(1.0)) * 100.0).coerceIn(0.0, 100.0).toFloat() / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = AppColors.primary,
                            trackColor = AppColors.border.copy(alpha = 0.3f)
                        )
                        Text("${progressPeriod} target • ${progressMetric}", fontSize = 11.sp, color = AppColors.textSecondary)
                    }
                }
            }

            // High Density Styled Quick Action circular buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickActionItem(
                    label = "Quick Sale",
                    icon = Icons.Default.Bolt,
                    backgroundColor = Color(0xFFE3F2FD),
                    iconColor = Color(0xFF1A73E8),
                    onClick = { onQuickAction("QUICK_SALE") }
                )
                QuickActionItem(
                    label = "Receipt",
                    icon = Icons.Default.Payments,
                    backgroundColor = Color(0xFFE8F5E9),
                    iconColor = Color(0xFF28A745),
                    onClick = { onQuickAction("RECEIPT") }
                )
                QuickActionItem(
                    label = "Payments",
                    icon = Icons.Default.Add,
                    backgroundColor = Color(0xFFFCE4EC),
                    iconColor = Color(0xFFDC3545),
                    onClick = { onQuickAction("PAYMENT") }
                )
                QuickActionItem(
                    label = "Reports",
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    backgroundColor = Color(0xFFFFF3E0),
                    iconColor = Color(0xFFFF9800),
                    onClick = { onQuickAction("REPORTS") }
                )
                QuickActionItem(
                    label = "Expenses",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    backgroundColor = Color(0xFFF3E5F5),
                    iconColor = Color(0xFF9C27B0),
                    onClick = { onQuickAction("EXPENSES") }
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Analytical Cards Grid Structure (2-columns wide)
            val cardList = buildList {
                add(KpiDetails("Today's Sales", Utils.formatIndianCurrency(todaySales), "", Color(0xFF1A73E8)))
                add(KpiDetails("Today's Purchases", Utils.formatIndianCurrency(todayPurchases), "", Color(0xFFBAC5D6)))
                add(KpiDetails("This Month's Sales", Utils.formatIndianCurrency(thisMonthSales), "", Color(0xFF28A745)))
                add(KpiDetails("Net Profit (Est.)", Utils.formatIndianCurrency(netProfit), "", if (netProfit >= 0) Color(0xFF28A745) else Color(0xFFDC3545)))
                add(KpiDetails("Receivables (Dr)", Utils.formatIndianCurrency(outstandingReceivable), "", Color(0xFFDC3545)))
                add(KpiDetails("Payables (Cr)", Utils.formatIndianCurrency(outstandingPayable), "", Color(0xFF9C27B0)))
                add(KpiDetails("Cash Account", Utils.formatIndianCurrency(cashBalance), "", Color(0xFFFD7E14)))
                add(KpiDetails("Bank & UPI", Utils.formatIndianCurrency(bankBalance), "", Color(0xFF17A2B8)))
                add(KpiDetails("Inventory", Utils.formatIndianCurrency(products.sumOf { it.currentStock * it.saleRate }), "", Color(0xFF6F42C1)))
                if (showGstCard) {
                    add(KpiDetails("GST", Utils.formatIndianCurrency(vouchers.sumOf { it.cgst + it.sgst + it.igst }), "", Color(0xFF0D9488)))
                }
            }

            // Render card structure cleanly without nesting LazyVerticalGrid inside a scrollable Column
            val chunkSize = if (isDesktop) 4 else 2
            val kpiChunks = cardList.chunked(chunkSize)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                kpiChunks.forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { card ->
                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    details = card,
                                    isSelected = selectedAnalyticsCard?.title == card.title,
                                    onClick = { selectedAnalyticsCard = card }
                                )
                            }
                        }
                        if (pair.size < chunkSize) {
                            Spacer(modifier = Modifier.weight((chunkSize - pair.size).toFloat()))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (selectedAnalyticsCard != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedAnalyticsCard = null },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    dragHandle = {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(width = 48.dp, height = 4.dp)
                                .background(Color(0xFFE5E7EB), RoundedCornerShape(50))
                        )
                    }
                ) {
                    val analyticsFilter = analyticsFilterByCard[selectedAnalyticsCard?.title.orEmpty()] ?: "This Month"
                    val analyticsSeries = remember(selectedAnalyticsCard?.title, analyticsFilter, vouchers, products, ledgerEntries) {
                        val now = System.currentTimeMillis()
                        val monthStart = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                        val quarterStart = Calendar.getInstance().apply { set(Calendar.MONTH, (get(Calendar.MONTH) / 3) * 3); set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                        val yearStart = Calendar.getInstance().apply { set(Calendar.MONTH, Calendar.JANUARY); set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                        val windowStart = when (analyticsFilter) {
                            "This Quarter" -> quarterStart
                            "This Year" -> yearStart
                            else -> monthStart
                        }
                        val buckets = mutableListOf<Double>()
                        repeat(6) { index ->
                            val bucketStart = when (analyticsFilter) {
                                "This Quarter" -> Calendar.getInstance().apply {
                                    set(Calendar.MONTH, (get(Calendar.MONTH) / 3) * 3 - index)
                                    set(Calendar.DAY_OF_MONTH, 1)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                                "This Year" -> Calendar.getInstance().apply {
                                    set(Calendar.MONTH, get(Calendar.MONTH) - index)
                                    set(Calendar.DAY_OF_MONTH, 1)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                                else -> Calendar.getInstance().apply {
                                    add(Calendar.MONTH, -index)
                                    set(Calendar.DAY_OF_MONTH, 1)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                            }
                            val bucketEnd = when (analyticsFilter) {
                                "This Quarter" -> Calendar.getInstance().apply {
                                    timeInMillis = bucketStart
                                    add(Calendar.MONTH, 3)
                                }.timeInMillis
                                "This Year" -> Calendar.getInstance().apply {
                                    timeInMillis = bucketStart
                                    add(Calendar.MONTH, 1)
                                }.timeInMillis
                                else -> Calendar.getInstance().apply {
                                    timeInMillis = bucketStart
                                    add(Calendar.MONTH, 1)
                                }.timeInMillis
                            }
                            val value = when (selectedAnalyticsCard?.title) {
                                "Today's Sales" -> vouchers.filter { it.type == "SALE" && it.date in bucketStart until bucketEnd }.sumOf { it.netAmount }
                                "Today's Purchases" -> vouchers.filter { it.type == "PURCHASE" && it.date in bucketStart until bucketEnd }.sumOf { it.netAmount }
                                "This Month's Sales" -> vouchers.filter { it.type == "SALE" && it.date in bucketStart until bucketEnd }.sumOf { it.netAmount }
                                "Net Profit (Est.)" -> (vouchers.filter { it.type == "SALE" && it.date in bucketStart until bucketEnd }.sumOf { it.taxableAmount } - vouchers.filter { it.type == "PURCHASE" && it.date in bucketStart until bucketEnd }.sumOf { it.taxableAmount })
                                "Receivables (Dr)" -> vouchers.filter { it.type == "SALE" && it.date in bucketStart until bucketEnd }.sumOf { it.netAmount } * 0.35
                                "Payables (Cr)" -> vouchers.filter { it.type == "PURCHASE" && it.date in bucketStart until bucketEnd }.sumOf { it.netAmount } * 0.32
                                "Cash Account" -> (ledgerEntries.filter { it.accountHead == "Cash" && it.date in bucketStart until bucketEnd }.sumOf { it.debit - it.credit })
                                "Bank & UPI" -> (ledgerEntries.filter { it.accountHead == "Bank" && it.date in bucketStart until bucketEnd }.sumOf { it.debit - it.credit })
                                "Inventory" -> products.filter { it.createdAt in bucketStart until bucketEnd }.sumOf { it.currentStock * it.saleRate }
                                "GST" -> vouchers.filter { it.date in bucketStart until bucketEnd }.sumOf { it.cgst + it.sgst + it.igst }
                                else -> 0.0
                            }
                            buckets.add(value)
                        }
                        buckets.reversed()
                    }
                    val currentValue = analyticsSeries.lastOrNull() ?: 0.0
                    val previousValue = analyticsSeries.firstOrNull() ?: 0.0
                    val growth = if (previousValue != 0.0) ((currentValue - previousValue) / previousValue) * 100.0 else 0.0
                    val highestValue = analyticsSeries.maxOrNull() ?: 0.0
                    val lowestValue = analyticsSeries.minOrNull() ?: 0.0
                    val averageValue = if (analyticsSeries.isNotEmpty()) analyticsSeries.average() else 0.0
                    val trendSummary = when {
                        currentValue > previousValue -> "Momentum is trending upward"
                        currentValue < previousValue -> "Momentum is easing slightly"
                        else -> "Performance is stable"
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(selectedAnalyticsCard?.title.orEmpty(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Text("Live business insight", fontSize = 12.sp, color = AppColors.textSecondary)
                            }
                            TextButton(onClick = { selectedAnalyticsCard = null }) {
                                Text("Close")
                            }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(Utils.formatIndianCurrency(currentValue), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (growth >= 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                                RoundedCornerShape(999.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("${if (growth >= 0) "+" else ""}${String.format(Locale.US, "%.1f", growth)}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (growth >= 0) Color(0xFF166534) else Color(0xFFB91C1C))
                                    }
                                    Text("growth", fontSize = 11.sp, color = AppColors.textSecondary)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp)
                                        .background(Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                        val width = size.width
                                        val height = size.height
                                        for (i in 0..4) {
                                            val y = 12f + i * ((height - 24f) / 4f)
                                            drawLine(Color(0xFFE5E7EB), Offset(0f, y), Offset(width, y), 1f)
                                        }
                                        for (i in 0..4) {
                                            val x = 12f + i * ((width - 24f) / 4f)
                                            drawLine(Color(0xFFE5E7EB), Offset(x, 0f), Offset(x, height), 1f)
                                        }
                                        if (analyticsSeries.isNotEmpty()) {
                                            val maxValue = (analyticsSeries.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
                                            val points = analyticsSeries.mapIndexed { index, value ->
                                                val x = 12f + index * ((width - 24f) / (analyticsSeries.size - 1).coerceAtLeast(1).toFloat())
                                                val y = height - 12f - (value / maxValue * (height - 24f)).toFloat()
                                                Offset(x, y)
                                            }
                                            points.windowed(2).forEach { (start, end) ->
                                                drawLine(Color(0xFF1A73E8), start, end, 2.5f)
                                            }
                                        }
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    AnalyticsStat("Highest", Utils.formatIndianCurrency(highestValue))
                                    AnalyticsStat("Lowest", Utils.formatIndianCurrency(lowestValue))
                                    AnalyticsStat("Average", Utils.formatIndianCurrency(averageValue))
                                }
                                Text(trendSummary, fontSize = 12.sp, color = AppColors.textSecondary)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("This Month", "This Quarter", "This Year", "Custom").forEach { option ->
                                FilterChip(
                                    selected = analyticsFilter == option,
                                    onClick = {
                                        analyticsFilterByCard = analyticsFilterByCard + (selectedAnalyticsCard?.title.orEmpty() to option)
                                    },
                                    label = { Text(option, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TRANSACTIONS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box {
                                OutlinedButton(onClick = { showTransactionFilterMenu = true }, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(34.dp)) {
                                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Filter", fontSize = 11.sp)
                                }
                                DropdownMenu(expanded = showTransactionFilterMenu, onDismissRequest = { showTransactionFilterMenu = false }) {
                                    val filterOptions = if (showGstCard) listOf("All Transactions", "Sales", "Purchase", "Receipt", "Payment", "Income", "Expense", "Receivable", "Payable", "Due", "Cancelled", "Draft", "GST Transactions") else listOf("All Transactions", "Sales", "Purchase", "Receipt", "Payment", "Income", "Expense", "Receivable", "Payable", "Due", "Cancelled", "Draft")
                                    filterOptions.forEach { option ->
                                        DropdownMenuItem(text = { Text(option) }, onClick = { activeTransactionFilter = option; showTransactionFilterMenu = false })
                                    }
                                }
                            }
                            Box {
                                OutlinedButton(onClick = { showTransactionSortMenu = true }, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(34.dp)) {
                                    Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Sort", fontSize = 11.sp)
                                }
                                DropdownMenu(expanded = showTransactionSortMenu, onDismissRequest = { showTransactionSortMenu = false }) {
                                    listOf("Newest First", "Oldest First", "Amount (High → Low)", "Amount (Low → High)", "Voucher Number (Ascending)", "Voucher Number (Descending)", "Party Name (A → Z)", "Party Name (Z → A)").forEach { option ->
                                        DropdownMenuItem(text = { Text(option) }, onClick = { activeTransactionSort = option; showTransactionSortMenu = false })
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransactionSummaryPill(modifier = Modifier.weight(1f), label = "Total Transactions", value = "${visibleTransactions.size}")
                        TransactionSummaryPill(modifier = Modifier.weight(1f), label = "Total Amount", value = Utils.formatIndianCurrency(visibleTransactions.sumOf { it.netAmount }))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransactionSummaryPill(modifier = Modifier.weight(1f), label = "Due Amount", value = Utils.formatIndianCurrency(visibleTransactions.filter { it.outstandingAmount > 0 }.sumOf { it.outstandingAmount }))
                        TransactionSummaryPill(modifier = Modifier.weight(1f), label = "Overdue Amount", value = Utils.formatIndianCurrency(visibleTransactions.filter { it.outstandingAmount > 0 && it.date < System.currentTimeMillis() }.sumOf { it.outstandingAmount }))
                    }
                    Text("Recent Transactions", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    if (recentTransactions.isEmpty()) {
                        Text("No matching transactions yet.", fontSize = 12.sp, color = AppColors.textSecondary, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(recentTransactions, key = { it.id }) { voucher ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    when (voucher.type) {
                                                        "RECEIPT", "SALE" -> Color(0xFFE8F5E9)
                                                        "PAYMENT", "PURCHASE" -> Color(0xFFFCE4EC)
                                                        else -> Color(0xFFE3F2FD)
                                                    },
                                                    RoundedCornerShape(10.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (voucher.type) {
                                                    "RECEIPT" -> Icons.Default.ArrowUpward
                                                    "PAYMENT" -> Icons.Default.ArrowDownward
                                                    "SALE" -> Icons.Default.Receipt
                                                    else -> Icons.Default.Payments
                                                },
                                                contentDescription = voucher.type,
                                                tint = when (voucher.type) {
                                                    "RECEIPT", "SALE" -> Color(0xFF28A745)
                                                    "PAYMENT", "PURCHASE" -> Color(0xFFDC3545)
                                                    else -> AppColors.primary
                                                },
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(voucher.type, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
                                            Text(voucher.voucherNo.ifBlank { voucher.type }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            Text("${voucher.partyId ?: "Cash"} • ${Utils.formatDate(voucher.date)}", fontSize = 10.sp, color = AppColors.textSecondary)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(Utils.formatIndianCurrency(voucher.netAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(deriveTransactionStatus(voucher), fontSize = 10.sp, color = when (deriveTransactionStatus(voucher)) {
                                            "Paid" -> Color(0xFF28A745)
                                            "Due" -> Color(0xFFDC3545)
                                            "Partially Paid" -> Color(0xFFFF9800)
                                            else -> AppColors.textSecondary
                                        })
                                    }
                                }
                            }
                        }
                    }
                    TextButton(onClick = { onQuickAction("TRANSACTIONS") }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("View All Transactions")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showProgressDetails) {
        AlertDialog(
            onDismissRequest = { showProgressDetails = false },
            confirmButton = {
                TextButton(onClick = { showProgressDetails = false }) {
                    Text("Close")
                }
            },
            title = { Text("Progress Analytics") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Metric: $progressMetric")
                    Text("Period: $progressPeriod")
                    Text("Target: ${Utils.formatIndianCurrency(progressTarget)}")
                    Text("Current: ${Utils.formatIndianCurrency(netProfit)}")
                    val percent = ((netProfit / progressTarget.coerceAtLeast(1.0)) * 100.0).coerceIn(0.0, 100.0)
                    Text("Progress: ${String.format(Locale.US, "%.2f", percent)}%")
                }
            }
        )
    }
}

data class KpiDetails(val title: String, val amount: String, val subt: String, val highlight: Color)

@Composable
fun AnalyticsStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 10.sp, color = AppColors.textSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
    }
}

@Composable
fun TransactionSummaryPill(modifier: Modifier = Modifier, label: String, value: String) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontSize = 10.sp, color = AppColors.textSecondary)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        }
    }
}

fun deriveTransactionStatus(voucher: Voucher): String {
    return when {
        voucher.status == "DRAFT" -> "Cancelled"
        voucher.outstandingAmount <= 0.0 -> "Paid"
        voucher.outstandingAmount < voucher.netAmount -> "Partially Paid"
        else -> "Due"
    }
}

@Composable
fun QuickActionItem(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .premiumClickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(backgroundColor, RoundedCornerShape(24.dp)), // Circular shape
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
    }
}

@Composable
fun KpiCard(
    details: KpiDetails,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (isSelected) AppColors.primary else Color(0xFFE8E8E8),
                RoundedCornerShape(16.dp)
            )
            .premiumClickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = details.title.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF666666),
                letterSpacing = 0.5.sp
            )
            Text(
                text = details.amount,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }
    }
}

@Composable
fun SalesPurchasesBarChart(vouchers: List<Voucher>) {
    // Generate bars calculation
    val salesByMonth = DoubleArray(6) { 0.0 }
    val purchaseByMonth = DoubleArray(6) { 0.0 }
    val monthNames = ArrayList<String>()

    val cal = Calendar.getInstance()
    for (i in 5 downTo 0) {
        val mCal = Calendar.getInstance()
        mCal.add(Calendar.MONTH, -i)
        val monthCode = SimpleDateFormat("MMM", Locale.getDefault()).format(mCal.time)
        monthNames.add(monthCode)
    }

    vouchers.forEach { v ->
        val vCal = Calendar.getInstance().apply { timeInMillis = v.date }
        val diffMonths = (cal.get(Calendar.YEAR) - vCal.get(Calendar.YEAR)) * 12 + cal.get(Calendar.MONTH) - vCal.get(Calendar.MONTH)
        if (diffMonths in 0..5) {
            val index = 5 - diffMonths
            if (v.type == "SALE") {
                salesByMonth[index] += v.netAmount
            } else if (v.type == "PURCHASE") {
                purchaseByMonth[index] += v.netAmount
            }
        }
    }

    val maxAmount = Math.max(
        (salesByMonth.maxOrNull() ?: 1.0),
        (purchaseByMonth.maxOrNull() ?: 1.0)
    ).coerceAtLeast(100.0)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        val width = size.width
        val height = size.height
        val barGroupWidth = width / 6f
        val maxBarHeight = height - 30f

        // Draw grid boundaries
        drawLine(Color(0xFFE8E8E8), Offset(0f, height - 20f), Offset(width, height - 20f), 1f)

        for (i in 0 until 6) {
            val xCenter = i * barGroupWidth + barGroupWidth / 2f
            
            // Sales bar (Blue)
            val salesH = (salesByMonth[i] / maxAmount * maxBarHeight).toFloat()
            drawRect(
                color = Color(0xFF1A73E8),
                topLeft = Offset(xCenter - 22f, height - 20f - salesH),
                size = Size(16f, salesH)
            )

            // Purchase bar (Gray)
            val purchaseH = (purchaseByMonth[i] / maxAmount * maxBarHeight).toFloat()
            drawRect(
                color = Color(0xFFBAC5D6),
                topLeft = Offset(xCenter + 2f, height - 20f - purchaseH),
                size = Size(16f, purchaseH)
            )
        }
    }

    // Legend
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).background(Color(0xFF1A73E8)))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Sales", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(end = 16.dp))

        Box(modifier = Modifier.size(10.dp).background(Color(0xFFBAC5D6)))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Purchases", fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun CashFlowLineChart(vouchers: List<Voucher>) {
    val dailyFlow = DoubleArray(30) { 0.0 }
    val cal = Calendar.getInstance()

    vouchers.forEach { v ->
        val diffDays = ((cal.timeInMillis - v.date) / (24 * 3600 * 1000)).toInt()
        if (diffDays in 0..29) {
            val idx = 29 - diffDays
            if (v.type == "SALE" || v.type == "RECEIPT") {
                dailyFlow[idx] += v.netAmount
            } else if (v.type == "PURCHASE" || v.type == "PAYMENT") {
                dailyFlow[idx] -= v.netAmount
            }
        }
    }

    var runningTotal = 0.0
    val cashTrend = DoubleArray(30) { idx ->
        runningTotal += dailyFlow[idx]
        runningTotal
    }

    val minVal = cashTrend.minOrNull() ?: 0.0
    val maxVal = (cashTrend.maxOrNull() ?: 1.0).coerceAtLeast(minVal + 100.0)
    val totalRange = maxVal - minVal

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(top = 16.dp, bottom = 12.dp)
    ) {
        val w = size.width
        val h = size.height
        val xStep = w / 29f

        drawLine(Color(0xFFE8E8E8), Offset(0f, h - 10f), Offset(w, h - 10f), 1f)

        var prevOffset: Offset? = null
        for (i in 0..29) {
            val yVal = cashTrend[i]
            val yOffset = h - 20f - ((yVal - minVal) / totalRange * (h - 30f)).toFloat()
            val currentOffset = Offset(i * xStep, yOffset)

            if (prevOffset != null) {
                drawLine(
                    color = Color(0xFF1A73E8),
                    start = prevOffset,
                    end = currentOffset,
                    strokeWidth = 2.5f
                )
            }
            prevOffset = currentOffset
        }
    }
    Text(
        text = "30 Days trend of aggregated local ledger entries cash flow",
        fontSize = 11.sp,
        color = Color.LightGray
    )
}

@Composable
fun GstPieChart(vouchers: List<Voucher>) {
    var cgst = 0.0
    var sgst = 0.0
    var igst = 0.0

    vouchers.filter { it.type == "SALE" }.forEach { v ->
        cgst += v.cgst
        sgst += v.sgst
        igst += v.igst
    }

    val totalGst = (cgst + sgst + igst).coerceAtLeast(1.0)
    val cgstAngle = (cgst / totalGst * 360f).toFloat()
    val sgstAngle = (sgst / totalGst * 360f).toFloat()
    val igstAngle = 360f - cgstAngle - sgstAngle

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            // Circle diagram
            if (cgst + sgst + igst == 0.0) {
                drawCircle(color = Color(0xFFF0F0F0), style = Stroke(12f))
            } else {
                drawArc(
                    color = Color(0xFF1A73E8),
                    startAngle = 0f,
                    sweepAngle = cgstAngle,
                    useCenter = false,
                    size = size,
                    style = Stroke(16f)
                )
                drawArc(
                    color = Color(0xFF28A745),
                    startAngle = cgstAngle,
                    sweepAngle = sgstAngle,
                    useCenter = false,
                    size = size,
                    style = Stroke(16f)
                )
                drawArc(
                    color = Color(0xFFFD7E14),
                    startAngle = cgstAngle + sgstAngle,
                    sweepAngle = igstAngle,
                    useCenter = false,
                    size = size,
                    style = Stroke(16f)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GstLegendRow("CGST (Intrastate Central)", Utils.formatIndianCurrency(cgst), Color(0xFF1A73E8))
            GstLegendRow("SGST (Intrastate State)", Utils.formatIndianCurrency(sgst), Color(0xFF28A745))
            GstLegendRow("IGST (Interstate Integrated)", Utils.formatIndianCurrency(igst), Color(0xFFFD7E14))
        }
    }
}

@Composable
fun GstLegendRow(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: ",
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 11.sp,
            color = Color(0xFF1A1A1A),
            fontWeight = FontWeight.Bold
        )
    }
}
