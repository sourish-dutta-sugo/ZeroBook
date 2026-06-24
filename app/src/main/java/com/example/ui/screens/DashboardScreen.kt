package com.example.ui.screens
import com.example.ui.theme.AppColors

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.LedgerEntry
import com.example.data.Utils
import com.example.data.Voucher
import com.example.ui.AppViewModel
import com.example.ui.DashboardViewModel
import com.example.ui.animation.premiumClickable
import com.example.ui.theme.Colors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    
    var searchQuery by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

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

    val profile by viewModel.profile.collectAsState()
    val headerState by dashboardViewModel.headerState.collectAsState()

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
                    painter = painterResource(R.drawable.zerobook_icon),
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

        // Global Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search vouchers, ledger, or stock (ID/Name/Date)...", fontSize = 12.sp) },
            leadingIcon = { Icon(androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search", tint = Color.Gray, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                        Icon(androidx.compose.material.icons.Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
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
            val cardList = listOf(
                KpiDetails("Today's Sales", Utils.formatIndianCurrency(todaySales), "Refreshed live", Color(0xFF1A73E8)),
                KpiDetails("Today's Purchases", Utils.formatIndianCurrency(todayPurchases), "Outgoing items", Color(0xFFBAC5D6)),
                KpiDetails("This Month's Sales", Utils.formatIndianCurrency(thisMonthSales), "Monthly target", Color(0xFF28A745)),
                KpiDetails("Net Profit (Est.)", Utils.formatIndianCurrency(netProfit), "Revenue minus cost", if (netProfit >= 0) Color(0xFF28A745) else Color(0xFFDC3545)),
                KpiDetails("Receivables (Dr)", Utils.formatIndianCurrency(outstandingReceivable), "Owed by customers", Color(0xFFDC3545)),
                KpiDetails("Payables (Cr)", Utils.formatIndianCurrency(outstandingPayable), "Owed to suppliers", Color(0xFF9C27B0)),
                KpiDetails("Cash Account", Utils.formatIndianCurrency(cashBalance), "In-hand cash float", Color(0xFFFD7E14)),
                KpiDetails("Bank & UPI", Utils.formatIndianCurrency(bankBalance), "Account running total", Color(0xFF17A2B8))
            )

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
                                KpiCard(card)
                            }
                        }
                        if (pair.size < chunkSize) {
                            Spacer(modifier = Modifier.weight((chunkSize - pair.size).toFloat()))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isDesktop) {
                // Desktop Side-by-Side row containing weekly sales and net cash flow trend charts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Weekly Sales (6 months)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A1A)
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "VIEW REPORT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A73E8)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            SalesPurchasesBarChart(vouchers)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Net Cash Flow Trend (Last 30 Days)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            CashFlowLineChart(vouchers)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "GST Collected Liabilities Breakdown",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            GstPieChart(vouchers)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            } else {
                // Custom Native Visual Chart 1: Sales vs Purchases Bar Chart (Last 6 Months)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Sales (6 months)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "VIEW REPORT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A73E8)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        SalesPurchasesBarChart(vouchers)
                    }
                }

                // Custom Chart 2: Daily Cash Flow Line Chart (Last 30 Days)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Net Cash Flow Trend (Last 30 Days)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CashFlowLineChart(vouchers)
                    }
                }

                // Custom Chart 3: GST Liability Breakdown (CGST / SGST / IGST)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "GST Collected Liabilities Breakdown",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        GstPieChart(vouchers)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

data class KpiDetails(val title: String, val amount: String, val subt: String, val highlight: Color)

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
fun KpiCard(details: KpiDetails) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(16.dp)),
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
            Text(
                text = details.subt,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    details.title.contains("Sales") -> Color(0xFF28A745)
                    details.title.contains("Profit") && !details.amount.contains("-") -> Color(0xFF28A745)
                    details.title.contains("Receivables") || details.title.contains("Payables") || details.amount.contains("-") -> Color(0xFFDC3545)
                    else -> Color(0xFF666666)
                }
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
