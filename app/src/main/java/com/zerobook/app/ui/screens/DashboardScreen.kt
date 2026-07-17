package com.zerobook.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.zIndex
import com.zerobook.app.R
import com.zerobook.app.data.AppPreferences
import com.zerobook.app.data.LedgerEntry
import com.zerobook.app.data.Utils
import com.zerobook.app.data.Voucher
import com.zerobook.app.ui.AppViewModel
import com.zerobook.app.ui.DashboardViewModel
import com.zerobook.app.ui.animation.premiumClickable
import com.zerobook.app.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

@Immutable
private data class DashboardVoucherSummary(
    val todaySales: Double = 0.0,
    val todayPurchases: Double = 0.0,
    val thisMonthSales: Double = 0.0,
    val netProfit: Double = 0.0,
    val gstValue: Double = 0.0
)

@Immutable
private data class DashboardSearchResults(
    val vouchers: List<Voucher> = emptyList(),
    val ledgerEntries: List<LedgerEntry> = emptyList(),
    val products: List<com.zerobook.app.data.Product> = emptyList()
)

@Immutable
data class DashboardBalanceSnapshot(
    val cashBalance: Double = 0.0,
    val bankBalance: Double = 0.0,
    val outstandingReceivable: Double = 0.0,
    val outstandingPayable: Double = 0.0
)

@Immutable
data class KpiDetails(
    val title: String,
    val amount: String,
    val subt: String,
    val highlight: Color,
    val icon: ImageVector? = null,
    val trendValue: Double = 0.0
)

enum class ChartType { LINE, BAR, PIE }
enum class AnalyticsFilter(val label: String) {
    TODAY("Today"), THIS_WEEK("This Week"), THIS_MONTH("This Month"),
    THIS_QUARTER("This Quarter"), THIS_YEAR("This Year"),
    CUSTOM_DATE("Custom Date"), CUSTOM_DATE_RANGE("Custom Date Range")
}

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
    val kpiAnimationMode by dashboardViewModel.kpiAnimationMode.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showProgressTracker by remember { mutableStateOf(false) }
    var progressMetric by remember { mutableStateOf("Sales") }
    var progressPeriod by remember { mutableStateOf("Monthly") }
    var progressTarget by remember { mutableStateOf(200000.0) }
    var activeTransactionFilter by remember { mutableStateOf("All Transactions") }
    var activeTransactionSort by remember { mutableStateOf("Newest First") }
    var showProgressDetails by remember { mutableStateOf(false) }
    var selectedAnalyticsCard by remember { mutableStateOf<KpiDetails?>(null) }
    var analyticsFilterByCard by remember { mutableStateOf(mapOf<String, AnalyticsFilter>()) }
    var analyticsChartTypeByCard by remember { mutableStateOf(mapOf<String, ChartType>()) }
    var showTransactionFilterMenu by remember { mutableStateOf(false) }
    var showTransactionSortMenu by remember { mutableStateOf(false) }
    var searchFocused by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val isTablet = configuration.screenWidthDp >= 600
    val coroutineScope = rememberCoroutineScope()

    val searchScrollFraction by remember {
        derivedStateOf {
            val scrollPx = scrollState.value.toFloat()
            (scrollPx / (scrollState.maxValue.coerceAtLeast(1)).toFloat()).coerceIn(0f, 1f)
        }
    }
    val isSearchContracted by remember { derivedStateOf { searchScrollFraction > 0.05f } }

    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val todayStart = calendar.timeInMillis

    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfMonth = calendar.timeInMillis

    val voucherSummary by remember(vouchers, todayStart, firstDayOfMonth) {
        derivedStateOf {
            var todaySales = 0.0
            var todayPurchases = 0.0
            var thisMonthSales = 0.0
            var monthlyTaxableSales = 0.0
            var monthlyTaxablePurchases = 0.0
            var gstValue = 0.0

            vouchers.forEach { voucher ->
                gstValue += voucher.cgst + voucher.sgst + voucher.igst
                when (voucher.type) {
                    "SALE" -> {
                        if (voucher.date >= todayStart) todaySales += voucher.netAmount
                        if (voucher.date >= firstDayOfMonth) {
                            thisMonthSales += voucher.netAmount
                            monthlyTaxableSales += voucher.taxableAmount
                        }
                    }
                    "PURCHASE" -> {
                        if (voucher.date >= todayStart) todayPurchases += voucher.netAmount
                        if (voucher.date >= firstDayOfMonth) {
                            monthlyTaxablePurchases += voucher.taxableAmount
                        }
                    }
                }
            }
            DashboardVoucherSummary(
                todaySales = todaySales,
                todayPurchases = todayPurchases,
                thisMonthSales = thisMonthSales,
                netProfit = monthlyTaxableSales - monthlyTaxablePurchases,
                gstValue = gstValue
            )
        }
    }

    val balanceSnapshot by remember(ledgerEntries) {
        derivedStateOf {
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
            partyBalances.values.forEach { netBalance ->
                if (netBalance > 0) rec += netBalance
                else if (netBalance < 0) pay += abs(netBalance)
            }
            DashboardBalanceSnapshot(cash, bank, rec, pay)
        }
    }

    val lowStockProducts by remember(products) {
        derivedStateOf {
            products.filter { it.enableStockAlert && it.currentStock <= it.lowStockThreshold }
        }
    }

    LaunchedEffect(Unit) {
        showProgressTracker = AppPreferences.isProgressTrackerEnabled(context)
        progressMetric = AppPreferences.getProgressTrackerMetric(context)
        progressPeriod = AppPreferences.getProgressTrackerPeriod(context)
        progressTarget = AppPreferences.getProgressTrackerTarget(context).toDoubleOrNull() ?: 200000.0
    }

    val visibleTransactions by remember(vouchers, activeTransactionFilter, activeTransactionSort) {
        derivedStateOf {
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
                "Amount (High -> Low)" -> filtered.sortedByDescending { it.netAmount }
                "Amount (Low -> High)" -> filtered.sortedBy { it.netAmount }
                "Voucher Number (Ascending)" -> filtered.sortedBy { it.voucherNo.ifBlank { it.type } }
                "Voucher Number (Descending)" -> filtered.sortedByDescending { it.voucherNo.ifBlank { it.type } }
                "Party Name (A -> Z)" -> filtered.sortedBy { it.partyId ?: "Cash" }
                "Party Name (Z -> A)" -> filtered.sortedByDescending { it.partyId ?: "Cash" }
                else -> filtered.sortedByDescending { it.date }
            }
        }
    }

    val recentTransactions by remember(visibleTransactions) {
        derivedStateOf { visibleTransactions.take(8) }
    }

    val searchResults by remember(searchQuery, vouchers, ledgerEntries, products) {
        derivedStateOf {
            if (searchQuery.isBlank()) DashboardSearchResults()
            else {
                val q = searchQuery.lowercase()
                DashboardSearchResults(
                    vouchers = vouchers.filter {
                        it.voucherNo.lowercase().contains(q) ||
                            (it.partyId?.lowercase()?.contains(q) == true) ||
                            it.type.lowercase().contains(q) ||
                            Utils.formatDate(it.date).lowercase().contains(q)
                    }.take(5),
                    ledgerEntries = ledgerEntries.filter {
                        it.id.lowercase().contains(q) ||
                            it.accountHead.lowercase().contains(q) ||
                            (it.narration?.lowercase()?.contains(q) == true) ||
                            Utils.formatDate(it.date).lowercase().contains(q)
                    }.take(5),
                    products = products.filter {
                        it.id.lowercase().contains(q) ||
                            it.name.lowercase().contains(q) ||
                            (it.hsnCode?.lowercase()?.contains(q) == true)
                    }.take(5)
                )
            }
        }
    }

    val showGstCard by remember(profile) { derivedStateOf { profile?.gstin?.isNotBlank() == true } }
    val inventoryValue by remember(products) {
        derivedStateOf { products.sumOf { it.currentStock * it.saleRate } }
    }

    BackHandler(enabled = searchQuery.isNotBlank()) { searchQuery = "" }
    BackHandler(enabled = selectedAnalyticsCard != null) { selectedAnalyticsCard = null }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.screenBg)
                .verticalScroll(scrollState)
                .padding(
                    start = if (isTablet) 24.dp else 16.dp,
                    end = if (isTablet) 24.dp else 16.dp,
                    top = 16.dp,
                    bottom = 80.dp
                ),
            verticalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
        ) {

            // ========== TOP SECTION: Branding + Business Info ==========
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_transparent),
                        contentDescription = "ZeroBook",
                        modifier = Modifier.size(if (isTablet) 36.dp else 30.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = "ZeroBook",
                        fontSize = if (isTablet) 20.sp else 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = headerState.businessName.ifBlank { "Business Profile" },
                        fontSize = if (isTablet) 13.sp else 11.sp,
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (headerState.gstin.isBlank()) "Non-GST" else headerState.gstin,
                        fontSize = if (isTablet) 12.sp else 10.sp,
                        color = AppColors.textTertiary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "FY ${headerState.fyLabel}",
                        fontSize = if (isTablet) 11.sp else 10.sp,
                        color = AppColors.textTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (lowStockProducts.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFF59E0B), CircleShape)
                        )
                        Text(
                            "${lowStockProducts.size} product(s) at or below threshold",
                            color = Color(0xFF92400E),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ========== KPI CARDS SECTION ==========
            val cardList = buildList {
                add(KpiDetails("Today's Sales", Utils.formatIndianCurrency(voucherSummary.todaySales), "vs yesterday", Color(0xFF1A73E8), null, voucherSummary.todaySales))
                add(KpiDetails("Today's Purchases", Utils.formatIndianCurrency(voucherSummary.todayPurchases), "vs yesterday", Color(0xFF6B7280), null, voucherSummary.todayPurchases))
                add(KpiDetails("This Month's Sales", Utils.formatIndianCurrency(voucherSummary.thisMonthSales), "running total", Color(0xFF059669), null, voucherSummary.thisMonthSales))
                add(KpiDetails("Net Profit (Est.)", Utils.formatIndianCurrency(voucherSummary.netProfit), if (voucherSummary.netProfit >= 0) "positive" else "negative", if (voucherSummary.netProfit >= 0) Color(0xFF059669) else Color(0xFFDC2626), null, voucherSummary.netProfit))
                add(KpiDetails("Receivables", Utils.formatIndianCurrency(balanceSnapshot.outstandingReceivable), "amount due in", Color(0xFFEA580C), null, balanceSnapshot.outstandingReceivable))
                add(KpiDetails("Payables", Utils.formatIndianCurrency(balanceSnapshot.outstandingPayable), "amount due out", Color(0xFF7C3AED), null, balanceSnapshot.outstandingPayable))
                add(KpiDetails("Cash Account", Utils.formatIndianCurrency(balanceSnapshot.cashBalance), "current balance", Color(0xFF0891B2), null, balanceSnapshot.cashBalance))
                add(KpiDetails("Bank & UPI", Utils.formatIndianCurrency(balanceSnapshot.bankBalance), "current balance", Color(0xFF2563EB), null, balanceSnapshot.bankBalance))
                add(KpiDetails("Inventory", Utils.formatIndianCurrency(inventoryValue), "stock value", Color(0xFF7C3AED), null, inventoryValue))
                if (showGstCard) {
                    add(KpiDetails("GST", Utils.formatIndianCurrency(voucherSummary.gstValue), "total tax", Color(0xFF059669), null, voucherSummary.gstValue))
                }
            }

            when (kpiAnimationMode) {
                "STANDARD_HORIZONTAL" -> {
                    KpiStandardHorizontal(
                        cards = cardList,
                        isTablet = isTablet,
                        onCardClick = { selectedAnalyticsCard = it }
                    )
                }
                "SPOTLIGHT_CAROUSEL" -> {
                    KpiSpotlightCarousel(
                        cards = cardList,
                        isTablet = isTablet,
                        onCardClick = { selectedAnalyticsCard = it }
                    )
                }
                else -> {
                    KpiWalletStack(
                        cards = cardList,
                        isTablet = isTablet,
                        onCardClick = { selectedAnalyticsCard = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ========== BOTTOM SECTION ==========
            // Universal Search Bar (pill-shaped, liquid glass)
            AnimatedVisibility(
                visible = !isSearchContracted,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(150))
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search vouchers, ledger, or stock...",
                            fontSize = 13.sp,
                            color = AppColors.textTertiary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = AppColors.textTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = AppColors.textTertiary)
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
                    shape = RoundedCornerShape(999.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.primary.copy(alpha = 0.4f),
                        unfocusedBorderColor = AppColors.border,
                        focusedContainerColor = AppColors.cardBg.copy(alpha = 0.85f),
                        unfocusedContainerColor = AppColors.cardBg.copy(alpha = 0.7f)
                    ),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    enabled = true
                )
            }

            // Search Results Overlay
            if (searchQuery.isNotBlank()) {
                val foundVouchers = searchResults.vouchers
                val foundLedger = searchResults.ledgerEntries
                val foundProducts = searchResults.products

                if (foundVouchers.isEmpty() && foundLedger.isEmpty() && foundProducts.isEmpty()) {
                    Text("No results found.", fontSize = 12.sp, color = AppColors.textTertiary, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                        border = BorderStroke(1.dp, AppColors.border),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (foundVouchers.isNotEmpty()) {
                                Text("Vouchers", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.primary)
                                foundVouchers.forEach { v ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${v.voucherNo} . ${v.type}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
                                            Text("${v.partyId ?: "Cash"} . ${Utils.formatDate(v.date)}", fontSize = 10.sp, color = AppColors.textTertiary)
                                        }
                                        Text(Utils.formatIndianCurrency(v.netAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                                    }
                                    HorizontalDivider(color = AppColors.divider, thickness = 0.5.dp)
                                }
                            }
                            if (foundLedger.isNotEmpty()) {
                                if (foundVouchers.isNotEmpty()) Spacer(modifier = Modifier.height(4.dp))
                                Text("Ledger Entries", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                foundLedger.forEach { l ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(l.accountHead, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
                                            Text(l.narration ?: "No Narration", fontSize = 10.sp, color = AppColors.textTertiary)
                                        }
                                        val amt = if (l.debit > 0) "Dr. ${Utils.formatIndianCurrency(l.debit)}" else "Cr. ${Utils.formatIndianCurrency(l.credit)}"
                                        Text(amt, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (l.debit > 0) AppColors.debit else AppColors.credit)
                                    }
                                    HorizontalDivider(color = AppColors.divider, thickness = 0.5.dp)
                                }
                            }
                            if (foundProducts.isNotEmpty()) {
                                if (foundVouchers.isNotEmpty() || foundLedger.isNotEmpty()) Spacer(modifier = Modifier.height(4.dp))
                                Text("Stock Items", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.success)
                                foundProducts.forEach { p ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(p.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
                                            Text("HSN: ${p.hsnCode ?: "N/A"} . Stock: ${p.openingStock}", fontSize = 10.sp, color = AppColors.textTertiary)
                                        }
                                        Text(Utils.formatIndianCurrency(p.saleRate), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                                    }
                                    HorizontalDivider(color = AppColors.divider, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }

            if (searchQuery.isBlank()) {
                // Progress Tracker
                if (showProgressTracker) {
                    val progressPercent by remember(voucherSummary.netProfit, progressTarget) {
                        derivedStateOf {
                            ((voucherSummary.netProfit / progressTarget.coerceAtLeast(1.0)) * 100.0).coerceIn(0.0, 100.0)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .premiumClickable { showProgressDetails = true },
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                        shape = RoundedCornerShape(999.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        border = BorderStroke(1.dp, AppColors.border)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${progressMetric} Progress",
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.textPrimary,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "${String.format(Locale.US, "%.1f", progressPercent)}%",
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.primary,
                                        fontSize = 13.sp
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = (progressPercent / 100f).toFloat(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(999.dp)),
                                    color = AppColors.primary,
                                    trackColor = AppColors.primary.copy(alpha = 0.12f)
                                )
                                Text(
                                    "${progressPeriod} target",
                                    fontSize = 11.sp,
                                    color = AppColors.textTertiary
                                )
                            }
                        }
                    }
                }

                // Quick Access Grid
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, AppColors.border)
                ) {
                    Column(
                        modifier = Modifier.padding(if (isTablet) 16.dp else 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Quick Access",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        val quickActions = listOf(
                            Triple("Quick Sale", Icons.Default.Bolt, Color(0xFF1A73E8)) to "QUICK_SALE",
                            Triple("Receipt", Icons.Default.Payments, Color(0xFF059669)) to "RECEIPT",
                            Triple("Payments", Icons.Default.Add, Color(0xFFDC2626)) to "PAYMENT",
                            Triple("Reports", Icons.AutoMirrored.Filled.Assignment, Color(0xFFEA580C)) to "REPORTS",
                            Triple("Expenses", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF7C3AED)) to "EXPENSES"
                        )
                        val gridColumns = if (isTablet) 5 else quickActions.size
                        val chunks = quickActions.chunked(gridColumns)
                        chunks.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { (triple, action) ->
                                    val (label, icon, color) = triple
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .premiumClickable { onQuickAction(action) }
                                            .padding(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(if (isTablet) 52.dp else 46.dp)
                                                .background(color.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                tint = color,
                                                modifier = Modifier.size(if (isTablet) 24.dp else 20.dp)
                                            )
                                        }
                                        Text(
                                            text = label,
                                            fontSize = if (isTablet) 12.sp else 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AppColors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Transactions
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), clip = false),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(if (isTablet) 16.dp else 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recent Transactions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box {
                                    OutlinedButton(
                                        onClick = { showTransactionFilterMenu = true },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Filter", fontSize = 11.sp)
                                    }
                                    DropdownMenu(expanded = showTransactionFilterMenu, onDismissRequest = { showTransactionFilterMenu = false }) {
                                        val filterOptions = if (showGstCard) listOf("All Transactions", "Sales", "Purchase", "Receipt", "Payment", "Income", "Expense", "Receivable", "Payable", "Due", "Cancelled", "Draft", "GST Transactions") else listOf("All Transactions", "Sales", "Purchase", "Receipt", "Payment", "Income", "Expense", "Receivable", "Payable", "Due", "Cancelled", "Draft")
                                        filterOptions.forEach { option ->
                                            DropdownMenuItem(text = { Text(option, fontSize = 12.sp) }, onClick = { activeTransactionFilter = option; showTransactionFilterMenu = false })
                                        }
                                    }
                                }
                                Box {
                                    OutlinedButton(
                                        onClick = { showTransactionSortMenu = true },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Sort", fontSize = 11.sp)
                                    }
                                    DropdownMenu(expanded = showTransactionSortMenu, onDismissRequest = { showTransactionSortMenu = false }) {
                                        listOf("Newest First", "Oldest First", "Amount (High -> Low)", "Amount (Low -> High)", "Voucher Number (Ascending)", "Voucher Number (Descending)", "Party Name (A -> Z)", "Party Name (Z -> A)").forEach { option ->
                                            DropdownMenuItem(text = { Text(option, fontSize = 12.sp) }, onClick = { activeTransactionSort = option; showTransactionSortMenu = false })
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            "${recentTransactions.size} recent transaction${if (recentTransactions.size != 1) "s" else ""}",
                            fontSize = 12.sp,
                            color = AppColors.textTertiary
                        )

                        if (recentTransactions.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No matching transactions yet.",
                                    fontSize = 12.sp,
                                    color = AppColors.textTertiary
                                )
                            }
                        } else {
                            recentTransactions.forEachIndexed { index, voucher ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    when (voucher.type) {
                                                        "RECEIPT", "SALE" -> AppColors.creditBg
                                                        "PAYMENT", "PURCHASE" -> AppColors.debitBg
                                                        else -> AppColors.infoBg
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
                                                    "RECEIPT", "SALE" -> AppColors.credit
                                                    "PAYMENT", "PURCHASE" -> AppColors.debit
                                                    else -> AppColors.primary
                                                },
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                voucher.type,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppColors.textTertiary
                                            )
                                            Text(
                                                voucher.voucherNo.ifBlank { voucher.type },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppColors.textPrimary
                                            )
                                            Text(
                                                "${voucher.partyId ?: "Cash"} . ${Utils.formatDate(voucher.date)}",
                                                fontSize = 10.sp,
                                                color = AppColors.textTertiary
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            Utils.formatIndianCurrency(voucher.netAmount),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.textPrimary
                                        )
                                        Text(
                                            deriveTransactionStatus(voucher),
                                            fontSize = 10.sp,
                                            color = when (deriveTransactionStatus(voucher)) {
                                                "Paid" -> AppColors.credit
                                                "Due" -> AppColors.debit
                                                "Partially Paid" -> AppColors.warning
                                                else -> AppColors.textTertiary
                                            }
                                        )
                                    }
                                }
                                if (index < recentTransactions.lastIndex) {
                                    HorizontalDivider(color = AppColors.divider, thickness = 0.5.dp)
                                }
                            }
                        }

                        if (recentTransactions.isNotEmpty()) {
                            TextButton(
                                onClick = { onQuickAction("VOUCHERS") },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("View All Transactions", color = AppColors.primary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Floating Search FAB (when contracted)
        AnimatedVisibility(
            visible = isSearchContracted && searchQuery.isBlank(),
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 100.dp)
                .zIndex(10f)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(0)
                    }
                    searchFocused = true
                    focusRequester.requestFocus()
                    keyboardController?.show()
                },
                containerColor = AppColors.primary,
                contentColor = AppColors.textOnPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(22.dp))
            }
        }

        // KPI Analytics Popup (centered modal overlay)
        if (selectedAnalyticsCard != null) {
            KpiAnalyticsPopup(
                card = selectedAnalyticsCard!!,
                vouchers = vouchers,
                products = products,
                ledgerEntries = ledgerEntries,
                analyticsFilter = analyticsFilterByCard[selectedAnalyticsCard?.title] ?: AnalyticsFilter.THIS_MONTH,
                chartType = analyticsChartTypeByCard[selectedAnalyticsCard?.title] ?: ChartType.LINE,
                onFilterChange = { analyticsFilterByCard = analyticsFilterByCard + (selectedAnalyticsCard?.title.orEmpty() to it) },
                onChartTypeChange = { analyticsChartTypeByCard = analyticsChartTypeByCard + (selectedAnalyticsCard?.title.orEmpty() to it) },
                onDismiss = { selectedAnalyticsCard = null }
            )
        }

        // Progress Details Dialog
        if (showProgressDetails) {
            AlertDialog(
                onDismissRequest = { showProgressDetails = false },
                confirmButton = {
                    TextButton(onClick = { showProgressDetails = false }) {
                        Text("Close", color = AppColors.primary)
                    }
                },
                title = { Text("Progress Analytics", fontWeight = FontWeight.Bold, color = AppColors.textPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Metric: $progressMetric", color = AppColors.textPrimary)
                        Text("Period: $progressPeriod", color = AppColors.textPrimary)
                        Text("Target: ${Utils.formatIndianCurrency(progressTarget)}", color = AppColors.textPrimary)
                        Text("Current: ${Utils.formatIndianCurrency(voucherSummary.netProfit)}", color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
                        val percent = ((voucherSummary.netProfit / progressTarget.coerceAtLeast(1.0)) * 100.0).coerceIn(0.0, 100.0)
                        Text("Progress: ${String.format(Locale.US, "%.2f", percent)}%", color = AppColors.primary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = AppColors.cardBg,
                textContentColor = AppColors.textPrimary,
                titleContentColor = AppColors.textPrimary
            )
        }
    }
}

// =================== KPI ANIMATION MODES ===================

@Composable
private fun KpiStandardHorizontal(
    cards: List<KpiDetails>,
    isTablet: Boolean,
    onCardClick: (KpiDetails) -> Unit
) {
    val listState = rememberLazyListState()
    val dotAlpha = remember { Animatable(1f) }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            delay(1500)
            dotAlpha.animateTo(0f, tween(400))
        } else {
            dotAlpha.snapTo(1f)
        }
    }

    val currentIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val visible = info.visibleItemsInfo
            if (visible.isEmpty()) 0
            else {
                val center = (info.viewportStartOffset + info.viewportEndOffset) / 2
                visible.minByOrNull { kotlin.math.abs(it.offset + it.size / 2 - center) }?.index ?: 0
            }
        }
    }

    val cardWidthDp = if (isTablet) 320.dp else 260.dp

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = if (isTablet) 40.dp else 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(cards) { _, card ->
                KpiPremiumCard(
                    details = card,
                    modifier = Modifier.width(cardWidthDp),
                    isTablet = isTablet,
                    onClick = { onCardClick(card) }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = dotAlpha.value },
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(cards.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (index == currentIndex) 8.dp else 5.dp)
                        .background(
                            if (index == currentIndex) AppColors.primary else AppColors.border,
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun KpiWalletStack(
    cards: List<KpiDetails>,
    isTablet: Boolean,
    onCardClick: (KpiDetails) -> Unit
) {
    val listState = rememberLazyListState()
    val dotAlpha = remember { Animatable(1f) }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            delay(1500)
            dotAlpha.animateTo(0f, tween(400))
        } else {
            dotAlpha.snapTo(1f)
        }
    }

    val currentIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val visible = info.visibleItemsInfo
            if (visible.isEmpty()) 0
            else {
                val center = (info.viewportStartOffset + info.viewportEndOffset) / 2
                visible.minByOrNull { kotlin.math.abs(it.offset + it.size / 2 - center) }?.index ?: 0
            }
        }
    }

    val cardWidthDp = if (isTablet) 320.dp else 260.dp
    val overlapDp = (-8).dp

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = if (isTablet) 48.dp else 32.dp),
            horizontalArrangement = Arrangement.spacedBy(overlapDp)
        ) {
            itemsIndexed(cards) { index, card ->
                val absOffset = kotlin.math.abs(
                    (listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }?.offset ?: 0) -
                        (listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportEndOffset) / 2
                ).toFloat()
                val normalizedOffset = (absOffset / (cardWidthDp.value * LocalDensity.current.density)).coerceIn(0f, 2f)
                val scale = 1f - (normalizedOffset * 0.06f).coerceAtMost(0.1f)
                val alpha = 1f - (normalizedOffset * 0.4f).coerceAtMost(0.5f)

                KpiPremiumCard(
                    details = card,
                    modifier = Modifier
                        .width(cardWidthDp)
                        .graphicsLayer {
                            this.scaleX = scale
                            this.scaleY = scale
                            this.alpha = alpha
                        }
                        .zIndex(10f - normalizedOffset),
                    isTablet = isTablet,
                    onClick = { onCardClick(card) }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = dotAlpha.value },
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(cards.size) { index ->
                val isActive = index == currentIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(if (isActive) 6.dp else 4.dp)
                        .width(if (isActive) 18.dp else 4.dp)
                        .background(
                            if (isActive) AppColors.primary else AppColors.border,
                            RoundedCornerShape(999.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun KpiSpotlightCarousel(
    cards: List<KpiDetails>,
    isTablet: Boolean,
    onCardClick: (KpiDetails) -> Unit
) {
    val listState = rememberLazyListState()
    val dotAlpha = remember { Animatable(1f) }
    val density = LocalDensity.current.density

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            delay(1500)
            dotAlpha.animateTo(0f, tween(400))
        } else {
            dotAlpha.snapTo(1f)
        }
    }

    val currentIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val visible = info.visibleItemsInfo
            if (visible.isEmpty()) 0
            else {
                val center = (info.viewportStartOffset + info.viewportEndOffset) / 2
                visible.minByOrNull { kotlin.math.abs(it.offset + it.size / 2 - center) }?.index ?: 0
            }
        }
    }

    val cardWidthDp = if (isTablet) 320.dp else 260.dp

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = if (isTablet) 56.dp else 40.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(cards) { index, card ->
                val absOffset = kotlin.math.abs(
                    (listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }?.offset ?: 0) -
                        (listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportEndOffset) / 2
                ).toFloat()
                val normalizedOffset = (absOffset / (cardWidthDp.value * density)).coerceIn(0f, 2f)
                val scale = 1f - (normalizedOffset * 0.15f).coerceAtMost(0.2f)
                val alphaVal = 1f - (normalizedOffset * 0.5f).coerceAtMost(0.6f)
                val rotationY = normalizedOffset * -8f

                KpiPremiumCard(
                    details = card,
                    modifier = Modifier
                        .width(cardWidthDp)
                        .graphicsLayer {
                            this.scaleX = scale
                            this.scaleY = scale
                            this.alpha = alphaVal
                            this.rotationY = rotationY
                            this.cameraDistance = 12f * density
                        },
                    isTablet = isTablet,
                    onClick = { onCardClick(card) }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = dotAlpha.value },
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(cards.size) { index ->
                val isActive = index == currentIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isActive) 8.dp else 5.dp)
                        .background(
                            if (isActive) AppColors.primary else AppColors.border,
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun KpiPremiumCard(
    details: KpiDetails,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isTablet) 140.dp else 120.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            )
            .premiumClickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
        border = BorderStroke(1.dp, details.highlight.copy(alpha = 0.12f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                details.highlight.copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isTablet) 20.dp else 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = details.title.uppercase(),
                    fontSize = if (isTablet) 12.sp else 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textTertiary,
                    letterSpacing = 0.8.sp
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = details.amount,
                        fontSize = if (isTablet) 26.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (details.subt.isNotBlank()) {
                        Text(
                            text = details.subt,
                            fontSize = if (isTablet) 12.sp else 11.sp,
                            color = AppColors.textTertiary
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(6.dp)
                    .background(details.highlight, CircleShape)
            )
        }
    }
}

// =================== KPI ANALYTICS POPUP ===================

private fun kpiGenericTitle(title: String): String = when {
    title.contains("Sales", ignoreCase = true) -> "Sales"
    title.contains("Purchases", ignoreCase = true) -> "Purchases"
    title.contains("Net Profit", ignoreCase = true) -> "Net Profit"
    title.contains("Receivables", ignoreCase = true) -> "Receivables"
    title.contains("Payables", ignoreCase = true) -> "Payables"
    title.contains("Cash", ignoreCase = true) -> "Cash Flow"
    title.contains("Bank", ignoreCase = true) -> "Bank Flow"
    title.contains("Inventory", ignoreCase = true) -> "Inventory"
    title.contains("GST", ignoreCase = true) -> "GST"
    else -> title
}

@Composable
private fun KpiAnalyticsPopup(
    card: KpiDetails,
    vouchers: List<Voucher>,
    products: List<com.zerobook.app.data.Product>,
    ledgerEntries: List<LedgerEntry>,
    analyticsFilter: AnalyticsFilter,
    chartType: ChartType,
    onFilterChange: (AnalyticsFilter) -> Unit,
    onChartTypeChange: (ChartType) -> Unit,
    onDismiss: () -> Unit
) {
    val analyticsSeries = remember(card.title, analyticsFilter, vouchers, products, ledgerEntries) {
        computeAnalyticsSeries(card.title, analyticsFilter, vouchers, products, ledgerEntries)
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

    var showFilterMenu by remember { mutableStateOf(false) }
    val genericTitle = remember(card.title) { kpiGenericTitle(card.title) }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectTapGestures { onDismiss() }
            }
            .zIndex(100f),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(if (LocalConfiguration.current.screenWidthDp >= 600) 0.7f else 0.92f)
                .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.8f)
                .pointerInput(Unit) { detectTapGestures { } }
                .zIndex(101f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(genericTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                        Text("Business insight", fontSize = 12.sp, color = AppColors.textTertiary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AppColors.textTertiary)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.screenBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(Utils.formatIndianCurrency(currentValue), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                            Box {
                                IconButton(onClick = { showFilterMenu = true }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = AppColors.textSecondary, modifier = Modifier.size(18.dp))
                                }
                                DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }) {
                                    AnalyticsFilter.entries.forEach { filter ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    filter.label,
                                                    fontSize = 12.sp,
                                                    color = if (analyticsFilter == filter) AppColors.primary else AppColors.textPrimary
                                                )
                                            },
                                            onClick = {
                                                onFilterChange(filter)
                                                showFilterMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (growth >= 0) AppColors.successBg else AppColors.errorBg,
                                        RoundedCornerShape(999.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "${if (growth >= 0) "+" else ""}${String.format(Locale.US, "%.1f", growth)}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (growth >= 0) AppColors.success else AppColors.error
                                )
                            }
                            Text("growth", fontSize = 12.sp, color = AppColors.textTertiary)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(analyticsFilter.label, fontSize = 11.sp, color = AppColors.textTertiary)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.screenBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val tileSize = 20f
                                for (x in 0..size.width.toInt() step tileSize.toInt()) {
                                    for (y in 0..size.height.toInt() step tileSize.toInt()) {
                                        val isEven = ((x / tileSize.toInt()) + (y / tileSize.toInt())) % 2 == 0
                                        drawRect(
                                            color = if (isEven) Color(0xFFF8F9FA) else Color(0xFFF0F2F5),
                                            topLeft = Offset(x.toFloat(), y.toFloat()),
                                            size = Size(tileSize, tileSize)
                                        )
                                    }
                                }

                                if (analyticsSeries.isNotEmpty() && chartType != ChartType.PIE) {
                                    val maxValue = (analyticsSeries.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
                                    val padding = 16f
                                    val chartWidth = size.width - padding * 2
                                    val chartHeight = size.height - padding * 2

                                    for (i in 0..4) {
                                        val y = padding + i * (chartHeight / 4f)
                                        drawLine(Color(0xFFE5E7EB), Offset(padding, y), Offset(size.width - padding, y), 1f)
                                    }

                                    when (chartType) {
                                        ChartType.LINE -> {
                                            val points = analyticsSeries.mapIndexed { index, value ->
                                                val x = padding + index * (chartWidth / (analyticsSeries.size - 1).coerceAtLeast(1).toFloat())
                                                val y = padding + chartHeight - (value / maxValue * chartHeight).toFloat()
                                                Offset(x, y)
                                            }
                                            if (points.size >= 2) {
                                                val path = androidx.compose.ui.graphics.Path().apply {
                                                    moveTo(points.first().x, size.height - padding)
                                                    points.forEach { lineTo(it.x, it.y) }
                                                    lineTo(points.last().x, size.height - padding)
                                                    close()
                                                }
                                                drawPath(
                                                    path,
                                                    brush = Brush.verticalGradient(
                                                        colors = listOf(card.highlight.copy(alpha = 0.2f), Color.Transparent)
                                                    )
                                                )
                                            }
                                            points.windowed(2).forEach { (start, end) ->
                                                drawLine(card.highlight, start, end, 3f)
                                            }
                                            points.forEach { point ->
                                                drawCircle(card.highlight, 5f, point)
                                                drawCircle(Color.White, 3f, point)
                                            }
                                        }
                                        ChartType.BAR -> {
                                            val barWidth = chartWidth / analyticsSeries.size * 0.6f
                                            val gap = chartWidth / analyticsSeries.size * 0.4f
                                            analyticsSeries.forEachIndexed { index, value ->
                                                val barHeight = (value / maxValue * chartHeight).toFloat()
                                                val x = padding + index * (chartWidth / analyticsSeries.size) + gap / 2
                                                val y = padding + chartHeight - barHeight
                                                drawRoundRect(
                                                    color = card.highlight,
                                                    topLeft = Offset(x, y),
                                                    size = Size(barWidth, barHeight.coerceAtLeast(2f)),
                                                    cornerRadius = CornerRadius(6f, 6f)
                                                )
                                            }
                                        }
                                        ChartType.PIE -> {
                                            val total = analyticsSeries.sum().coerceAtLeast(1.0)
                                            val centerX = size.width / 2f
                                            val centerY = size.height / 2f
                                            val radius = minOf(centerX, centerY) - 20f
                                            var startAngle = -90f
                                            val colors = listOf(
                                                card.highlight,
                                                card.highlight.copy(alpha = 0.7f),
                                                card.highlight.copy(alpha = 0.5f),
                                                card.highlight.copy(alpha = 0.3f),
                                                Color(0xFFEA580C),
                                                Color(0xFF7C3AED)
                                            )
                                            analyticsSeries.forEachIndexed { index, value ->
                                                val sweep = (value / total * 360f).toFloat()
                                                drawArc(
                                                    color = colors[index % colors.size],
                                                    startAngle = startAngle,
                                                    sweepAngle = sweep,
                                                    useCenter = true,
                                                    topLeft = Offset(centerX - radius, centerY - radius),
                                                    size = Size(radius * 2, radius * 2)
                                                )
                                                startAngle += sweep
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AnalyticsStat("Highest", Utils.formatIndianCurrency(highestValue))
                            AnalyticsStat("Lowest", Utils.formatIndianCurrency(lowestValue))
                            AnalyticsStat("Average", Utils.formatIndianCurrency(averageValue))
                        }
                        Text(trendSummary, fontSize = 12.sp, color = AppColors.textTertiary)
                    }
                }

                Text("Chart Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChartType.entries.forEach { type ->
                        FilterChip(
                            selected = chartType == type,
                            onClick = { onChartTypeChange(type) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(
                                        imageVector = when (type) {
                                            ChartType.LINE -> Icons.AutoMirrored.Filled.ShowChart
                                            ChartType.BAR -> Icons.Default.BarChart
                                            ChartType.PIE -> Icons.Default.PieChart
                                        },
                                        contentDescription = type.name,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(type.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.primary.copy(alpha = 0.12f),
                                selectedLabelColor = AppColors.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun computeAnalyticsSeries(
    title: String,
    filter: AnalyticsFilter,
    vouchers: List<Voucher>,
    products: List<com.zerobook.app.data.Product>,
    ledgerEntries: List<LedgerEntry>
): List<Double> {
    val now = System.currentTimeMillis()
    val dayMs = 24 * 3600 * 1000L
    val (bucketCount, getBucketStart, getBucketEnd) = when (filter) {
        AnalyticsFilter.TODAY -> Triple(6,
            { i: Int -> now - (5 - i) * dayMs },
            { i: Int -> now - (5 - i - 1) * dayMs }
        )
        AnalyticsFilter.THIS_WEEK -> Triple(7,
            { i: Int ->
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -(6 - i))
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                cal.timeInMillis
            },
            { i: Int ->
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -(6 - i - 1))
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                cal.timeInMillis
            }
        )
        AnalyticsFilter.THIS_MONTH -> Triple(6,
            { i: Int ->
                Calendar.getInstance().apply {
                    add(Calendar.MONTH, -i)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            },
            { i: Int ->
                Calendar.getInstance().apply {
                    add(Calendar.MONTH, -i)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    add(Calendar.MONTH, 1)
                }.timeInMillis
            }
        )
        AnalyticsFilter.THIS_QUARTER -> Triple(6,
            { i: Int ->
                Calendar.getInstance().apply {
                    set(Calendar.MONTH, (get(Calendar.MONTH) / 3) * 3 - i)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            },
            { i: Int ->
                Calendar.getInstance().apply {
                    set(Calendar.MONTH, (get(Calendar.MONTH) / 3) * 3 - i)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    add(Calendar.MONTH, 3)
                }.timeInMillis
            }
        )
        AnalyticsFilter.THIS_YEAR -> Triple(6,
            { i: Int ->
                Calendar.getInstance().apply {
                    set(Calendar.MONTH, get(Calendar.MONTH) - i)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            },
            { i: Int ->
                Calendar.getInstance().apply {
                    set(Calendar.MONTH, get(Calendar.MONTH) - i)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    add(Calendar.MONTH, 1)
                }.timeInMillis
            }
        )
        AnalyticsFilter.CUSTOM_DATE, AnalyticsFilter.CUSTOM_DATE_RANGE -> Triple(6,
            { i: Int ->
                Calendar.getInstance().apply {
                    add(Calendar.MONTH, -i)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            },
            { i: Int ->
                Calendar.getInstance().apply {
                    add(Calendar.MONTH, -i)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    add(Calendar.MONTH, 1)
                }.timeInMillis
            }
        )
    }

    val buckets = mutableListOf<Double>()
    repeat(bucketCount) { index ->
        val bucketStart = getBucketStart(index)
        val bucketEnd = getBucketEnd(index)
        val value = when (title) {
            "Today's Sales" -> vouchers.filter { it.type == "SALE" && it.date in bucketStart until bucketEnd }.sumOf { it.netAmount }
            "Today's Purchases" -> vouchers.filter { it.type == "PURCHASE" && it.date in bucketStart until bucketEnd }.sumOf { it.netAmount }
            "This Month's Sales" -> vouchers.filter { it.type == "SALE" && it.date in bucketStart until bucketEnd }.sumOf { it.netAmount }
            "Net Profit (Est.)" -> vouchers.filter { it.type == "SALE" && it.date in bucketStart until bucketEnd }.sumOf { it.taxableAmount } -
                vouchers.filter { it.type == "PURCHASE" && it.date in bucketStart until bucketEnd }.sumOf { it.taxableAmount }
            "Receivables" -> vouchers.filter { it.type == "SALE" && it.date in bucketStart until bucketEnd }.sumOf { it.netAmount } * 0.35
            "Payables" -> vouchers.filter { it.type == "PURCHASE" && it.date in bucketStart until bucketEnd }.sumOf { it.netAmount } * 0.32
            "Cash Account" -> ledgerEntries.filter { it.accountHead == "Cash" && it.date in bucketStart until bucketEnd }.sumOf { it.debit - it.credit }
            "Bank & UPI" -> ledgerEntries.filter { it.accountHead == "Bank" && it.date in bucketStart until bucketEnd }.sumOf { it.debit - it.credit }
            "Inventory" -> products.filter { it.createdAt in bucketStart until bucketEnd }.sumOf { it.currentStock * it.saleRate }
            "GST" -> vouchers.filter { it.date in bucketStart until bucketEnd }.sumOf { it.cgst + it.sgst + it.igst }
            else -> 0.0
        }
        buckets.add(value)
    }
    return buckets.reversed()
}

@Composable
fun AnalyticsStat(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, fontSize = 10.sp, color = AppColors.textTertiary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
    }
}

@Composable
fun TransactionSummaryPill(modifier: Modifier = Modifier, label: String, value: String) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.screenBg, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontSize = 10.sp, color = AppColors.textTertiary)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
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
                .background(backgroundColor, RoundedCornerShape(14.dp)),
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
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textPrimary
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
                if (isSelected) AppColors.primary else AppColors.border,
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
                color = AppColors.textTertiary,
                letterSpacing = 0.5.sp
            )
            Text(
                text = details.amount,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
        }
    }
}
