package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.AppViewModel
import com.example.services.EmailComposer
import com.example.ui.theme.Colors
import com.example.ui.theme.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import com.example.ui.animation.pressScale
import com.example.ui.animation.premiumClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: AppViewModel,
    isDesktop: Boolean = false,
    navigateToLedgerBooks: () -> Unit,
    navigateToExpenses: () -> Unit = {},
    navigateToNewVoucher: (String?) -> Unit,
    onNavigateBack: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val ledgerEntries by viewModel.ledgerEntries.collectAsState()
    val parties by viewModel.parties.collectAsState()
    val products by viewModel.products.collectAsState()
    val vouchers by viewModel.vouchers.collectAsState()

    var activeReport by remember { mutableStateOf(if (isDesktop) "TRIAL" else "MENU") }

    val context = LocalContext.current

    // Compute ledger closing balances reactively
    val (ledgerBalances, debtorsList, creditorsList) = remember(ledgerEntries, parties) {
        val balances = mutableMapOf<String, Double>()

        // 1. Initialise standard ledger accounts with 0
        balances["Cash"] = 0.0
        balances["Bank"] = 0.0
        balances["Sales Account"] = 0.0
        balances["Purchases Account"] = 0.0
        balances["CGST Payable"] = 0.0
        balances["SGST Payable"] = 0.0
        balances["IGST Payable"] = 0.0
        balances["CGST Receivable"] = 0.0
        balances["SGST Receivable"] = 0.0
        balances["IGST Receivable"] = 0.0
        balances["Round Off Account"] = 0.0

        // 2. Add party opening balances
        parties.forEach { p ->
            val initialVal = if (p.balanceType == "DR") p.openingBalance else -p.openingBalance
            balances["Party: ${p.name}"] = initialVal
        }

        // 3. Accumulate ledger entries
        ledgerEntries.forEach { entry ->
            val head = entry.accountHead
            val current = balances[head] ?: 0.0
            balances[head] = current + (entry.debit - entry.credit)
        }

        // Filter debtors (positive balances) and creditors (negative balances)
        val debtors = mutableListOf<Pair<Party, Double>>()
        val creditors = mutableListOf<Pair<Party, Double>>()

        parties.forEach { p ->
            val bal = balances["Party: ${p.name}"] ?: 0.0
            if (bal > 0.0) {
                debtors.add(p to bal)
            } else if (bal < 0.0) {
                creditors.add(p to Math.abs(bal))
            }
        }

        Triple(balances, debtors.sortedByDescending { it.second }, creditors.sortedByDescending { it.second })
    }

    // Compute shared balances & items
    val currentStockValue = remember(products) {
        products.sumOf { it.openingStock * it.purchaseRate }
    }

    val salesRevenue = Math.abs(ledgerBalances["Sales Account"] ?: 0.0)
    val purchasesCost = ledgerBalances["Purchases Account"] ?: 0.0

    val cgstPayable = ledgerBalances["CGST Payable"] ?: 0.0
    val sgstPayable = ledgerBalances["SGST Payable"] ?: 0.0
    val igstPayable = ledgerBalances["IGST Payable"] ?: 0.0

    val cgstRec = ledgerBalances["CGST Receivable"] ?: 0.0
    val sgstRec = ledgerBalances["SGST Receivable"] ?: 0.0
    val igstRec = ledgerBalances["IGST Receivable"] ?: 0.0

    val outputTaxTotal = cgstPayable + sgstPayable + igstPayable
    val inputTaxCredit = cgstRec + sgstRec + igstRec
    val netGstBalance = outputTaxTotal - inputTaxCredit

    val roundOffValue = ledgerBalances["Round Off Account"] ?: 0.0

    // P&L Calculations
    val tradingDebit = purchasesCost
    val tradingCredit = salesRevenue + currentStockValue
    val grossProfit = tradingCredit - tradingDebit

    val netProfit = grossProfit - (if (roundOffValue > 0) roundOffValue else 0.0)

    // Assets and liabilities calculations for Balance Sheet
    val cashVal = ledgerBalances["Cash"] ?: 0.0
    val bankVal = ledgerBalances["Bank"] ?: 0.0
    val totalDebtors = debtorsList.sumOf { it.second }
    val totalCreditors = creditorsList.sumOf { it.second }

    val assetsDuties = if (netGstBalance < 0.0) -netGstBalance else 0.0
    val liabilitiesDuties = if (netGstBalance > 0.0) netGstBalance else 0.0

    val totalAssets = cashVal + bankVal + totalDebtors + currentStockValue + assetsDuties
    val otherLiabilities = totalCreditors + liabilitiesDuties
    val balancingCapitalEquity = totalAssets - otherLiabilities

    if (isDesktop) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left menu pane
            Box(modifier = Modifier.width(360.dp).fillMaxHeight()) {
                Scaffold(
                    containerColor = AppColors.screenBg,
                    topBar = {
                        TopAppBar(
                            title = { Text("Regulatory Financial Books", fontWeight = FontWeight.Bold) },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.cardBg)
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColors.screenBg)
                            .verticalScroll(rememberScrollState())
                            .padding(innerPadding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Reports Menu (${viewModel.financialYear.value})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textSecondary
                        )

                        val reportsList = listOf(
                            Triple("TRIAL", "Trial Balance", "Interactive Net Debits & Credits verification"),
                            Triple("PL", "Profit & Loss Account", "Gross Margin & Net P&L side-by-side"),
                            Triple("BALANCE", "Balance Sheet", "Real assets, liabilities & equity distribution"),
                            Triple("GST", "GST Summary Status", "Tax output liability offset against Input credits"),
                            Triple("RECEIVABLES", "Outstanding Receivables", "Automated customer aging list & alerts"),
                            Triple("PAYABLES", "Outstanding Payables", "Supplier credit balances status"),
                            Triple("STOCK", "Stock Report", "Current stock, low stock, and out-of-stock items")
                        )

                        reportsList.forEach { (type, name, desc) ->
                            val isSelected = activeReport == type
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (isSelected) AppColors.primary else AppColors.border,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .premiumClickable { activeReport = type },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) AppColors.primary.copy(alpha = 0.08f) else AppColors.cardBg
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) AppColors.primary else AppColors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = desc,
                                        fontSize = 10.sp,
                                        color = AppColors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Right Detail view pane
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                Scaffold(
                    containerColor = AppColors.screenBg,
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = when (activeReport) {
                                            "TRIAL" -> "Trial Balance"
                                            "PL" -> "Trading & Profit & Loss Statement"
                                            "BALANCE" -> "Balance Sheet Statement"
                                            "GST" -> "GST Register Summary"
                                            "RECEIVABLES" -> "Outstanding Receivables Ledger"
                                            "PAYABLES" -> "Outstanding Payables Ledger"
                                            "STOCK" -> "Stock Report"
                                            else -> activeReport
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.textPrimary,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "ZeroBook • Financial Year ${viewModel.financialYear.value}",
                                        fontSize = 11.sp,
                                        color = AppColors.textTertiary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.cardBg)
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColors.screenBg)
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        when (activeReport) {
                            "TRIAL" -> {
                                TrialBalanceView(
                                    ledgerBalances = ledgerBalances,
                                    debtorsList = debtorsList,
                                    creditorsList = creditorsList,
                                    currentStockValue = currentStockValue,
                                    roundOffValue = roundOffValue,
                                    salesRevenue = salesRevenue,
                                    purchasesCost = purchasesCost,
                                    cgstPayable = cgstPayable,
                                    sgstPayable = sgstPayable,
                                    igstPayable = igstPayable,
                                    cgstRec = cgstRec,
                                    sgstRec = sgstRec,
                                    igstRec = igstRec
                                )
                            }

                            "PL" -> {
                                TradingAndPLView(
                                    salesRevenue = salesRevenue,
                                    purchasesCost = purchasesCost,
                                    currentStockValue = currentStockValue,
                                    grossProfit = grossProfit,
                                    netProfit = netProfit,
                                    roundOffValue = roundOffValue
                                )
                            }

                            "BALANCE" -> {
                                BalanceSheetView(
                                    cashVal = cashVal,
                                    bankVal = bankVal,
                                    totalDebtors = totalDebtors,
                                    closingStockVal = currentStockValue,
                                    totalCreditors = totalCreditors,
                                    liabilitiesDuties = liabilitiesDuties,
                                    assetsDuties = assetsDuties,
                                    balancingCapital = balancingCapitalEquity,
                                    netProfit = netProfit,
                                    totalAssets = totalAssets
                                )
                            }

                            "GST" -> {
                                GSTSummaryView(
                                    cgstPay = cgstPayable,
                                    sgstPay = sgstPayable,
                                    igstPay = igstPayable,
                                    cgstRec = cgstRec,
                                    sgstRec = sgstRec,
                                    igstRec = igstRec,
                                    outputTotal = outputTaxTotal,
                                    inputTotal = inputTaxCredit,
                                    netPayable = netGstBalance
                                )
                            }

                            "RECEIVABLES" -> {
                                var mSelectedTab by remember { mutableStateOf("BILLS") } // "PARTIES" vs "BILLS"
                                val billsReceivable by viewModel.billsReceivable.collectAsState()

                                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    TabRow(
                                        selectedTabIndex = if (mSelectedTab == "PARTIES") 0 else 1,
                                        containerColor = AppColors.cardBg,
                                        contentColor = AppColors.primary
                                    ) {
                                        Tab(
                                            selected = mSelectedTab == "PARTIES",
                                            onClick = { mSelectedTab = "PARTIES" },
                                            text = { Text("By Customers", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                        )
                                        Tab(
                                            selected = mSelectedTab == "BILLS",
                                            onClick = { mSelectedTab = "BILLS" },
                                            text = { Text("Bill-wise Outstanding", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                        )
                                    }

                                    if (mSelectedTab == "PARTIES") {
                                        DebtorsRemindersView(
                                            debtors = debtorsList,
                                            profile = profile,
                                            context = context
                                        )
                                    } else {
                                        AgedBillsListView(
                                            bills = billsReceivable,
                                            parties = parties,
                                            viewModel = viewModel,
                                            navigateToNewVoucher = navigateToNewVoucher
                                        )
                                    }
                                }
                            }

                            "PAYABLES" -> {
                                PayablesBillsListView(
                                    vouchers = vouchers,
                                    parties = parties,
                                    navigateToNewVoucher = navigateToNewVoucher,
                                    viewModel = viewModel
                                )
                            }

                            "STOCK" -> {
                                StockReportScreen(products = products)
                            }
                        }
                    }
                }
            }
        }
    } else {
        if (activeReport == "MENU") {
            Scaffold(
                containerColor = AppColors.screenBg,
                topBar = {
                    TopAppBar(
                        title = { Text("Regulatory Financial Books & GST Reports", fontWeight = FontWeight.Black, color = AppColors.textPrimary) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.cardBg)
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.screenBg)
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Double Entry Ledger Reports Summary (${viewModel.financialYear.value})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AppColors.textSecondary
                    )

                    ReportMenuCard(
                        title = "Trial Balance",
                        description = "Interactive Group-wise trial verification statement of net Debits and Credits",
                        icon = Icons.Default.AccountBalance,
                        onClick = { activeReport = "TRIAL" }
                    )

                    ReportMenuCard(
                        title = "Profit & Loss Account",
                        description = "View real double-entry Trading (Gross GP) and P&L statements side-by-side",
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        onClick = { activeReport = "PL" }
                    )

                    ReportMenuCard(
                        title = "Balance Sheet",
                        description = "Double entry balanced visual assets, liabilities, and proprietor equity",
                        icon = Icons.Default.Receipt,
                        onClick = { activeReport = "BALANCE" }
                    )

                    ReportMenuCard(
                        title = "GST Summary Status",
                        description = "Estimated output liabilities offset against input credits",
                        icon = Icons.Default.Percent,
                        onClick = { activeReport = "GST" }
                    )

                    ReportMenuCard(
                        title = "Outstanding Receivables (Email automation)",
                        description = "Client outstanding report with automated invoice reminders",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        onClick = { activeReport = "RECEIVABLES" }
                    )

                    ReportMenuCard(
                        title = "Outstanding Payables",
                        description = "Supplier credits outstanding ledger status",
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        onClick = { activeReport = "PAYABLES" }
                    )

                    ReportMenuCard(
                        title = "Stock Report",
                        description = "Current stock, thresholds, and low stock export",
                        icon = Icons.Default.Inventory2,
                        onClick = { activeReport = "STOCK" }
                    )

                    ReportMenuCard(
                        title = "Ledger Books",
                        description = "Browse chart of accounts and ledger balances",
                        icon = Icons.Default.AccountBalance,
                        onClick = navigateToLedgerBooks
                    )

                    ReportMenuCard(
                        title = "Expenses",
                        description = "Track operating expenses and export the register",
                        icon = Icons.Default.Payments,
                        onClick = navigateToExpenses
                    )
                }
            }
        } else {
            Scaffold(
                containerColor = AppColors.screenBg,
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = when (activeReport) {
                                        "TRIAL" -> "Trial Balance"
                                        "PL" -> "Trading & Profit & Loss Statement"
                                        "BALANCE" -> "Balance Sheet Statement"
                                        "GST" -> "GST Register Summary"
                                        "RECEIVABLES" -> "Outstanding Receivables Ledger"
                                        "PAYABLES" -> "Outstanding Payables Ledger"
                                        "STOCK" -> "Stock Report"
                                        else -> activeReport
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.textPrimary,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "ZeroBook • Financial Year ${viewModel.financialYear.value}",
                                    fontSize = 11.sp,
                                    color = AppColors.textTertiary
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { activeReport = "MENU" }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.cardBg)
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.screenBg)
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    when (activeReport) {
                        "TRIAL" -> {
                            TrialBalanceView(
                                ledgerBalances = ledgerBalances,
                                debtorsList = debtorsList,
                                creditorsList = creditorsList,
                                currentStockValue = currentStockValue,
                                roundOffValue = roundOffValue,
                                salesRevenue = salesRevenue,
                                purchasesCost = purchasesCost,
                                cgstPayable = cgstPayable,
                                sgstPayable = sgstPayable,
                                igstPayable = igstPayable,
                                cgstRec = cgstRec,
                                sgstRec = sgstRec,
                                igstRec = igstRec
                            )
                        }

                        "PL" -> {
                            TradingAndPLView(
                                salesRevenue = salesRevenue,
                                purchasesCost = purchasesCost,
                                currentStockValue = currentStockValue,
                                grossProfit = grossProfit,
                                netProfit = netProfit,
                                roundOffValue = roundOffValue
                            )
                        }

                        "BALANCE" -> {
                            BalanceSheetView(
                                cashVal = cashVal,
                                bankVal = bankVal,
                                totalDebtors = totalDebtors,
                                closingStockVal = currentStockValue,
                                totalCreditors = totalCreditors,
                                liabilitiesDuties = liabilitiesDuties,
                                assetsDuties = assetsDuties,
                                balancingCapital = balancingCapitalEquity,
                                netProfit = netProfit,
                                totalAssets = totalAssets
                            )
                        }

                        "GST" -> {
                            GSTSummaryView(
                                cgstPay = cgstPayable,
                                sgstPay = sgstPayable,
                                igstPay = igstPayable,
                                cgstRec = cgstRec,
                                sgstRec = sgstRec,
                                igstRec = igstRec,
                                outputTotal = outputTaxTotal,
                                inputTotal = inputTaxCredit,
                                netPayable = netGstBalance
                            )
                        }

                        "RECEIVABLES" -> {
                            var mSelectedTab by remember { mutableStateOf("BILLS") } // "PARTIES" vs "BILLS"
                            val billsReceivable by viewModel.billsReceivable.collectAsState()

                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                TabRow(
                                    selectedTabIndex = if (mSelectedTab == "PARTIES") 0 else 1,
                                    containerColor = AppColors.cardBg,
                                    contentColor = AppColors.primary
                                ) {
                                    Tab(
                                        selected = mSelectedTab == "PARTIES",
                                        onClick = { mSelectedTab = "PARTIES" },
                                        text = { Text("By Customers", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                    )
                                    Tab(
                                        selected = mSelectedTab == "BILLS",
                                        onClick = { mSelectedTab = "BILLS" },
                                        text = { Text("Bill-wise Outstanding", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                    )
                                }

                                if (mSelectedTab == "PARTIES") {
                                    DebtorsRemindersView(
                                        debtors = debtorsList,
                                        profile = profile,
                                        context = context
                                    )
                                } else {
                                    AgedBillsListView(
                                        bills = billsReceivable,
                                        parties = parties,
                                        viewModel = viewModel,
                                        navigateToNewVoucher = navigateToNewVoucher
                                    )
                                }
                            }
                        }

                            "PAYABLES" -> {
                                PayablesBillsListView(
                                    vouchers = vouchers,
                                    parties = parties,
                                    navigateToNewVoucher = navigateToNewVoucher,
                                    viewModel = viewModel
                                )
                            }

                            "STOCK" -> {
                                StockReportScreen(products = products)
                            }
                        }
                    }
                }
        }
    }
}

@Composable
fun PayablesBillsListView(
    vouchers: List<Voucher>,
    parties: List<Party>,
    viewModel: AppViewModel,
    navigateToNewVoucher: (String?) -> Unit
) {
    val purchaseBills = remember(vouchers) {
        vouchers
            .filter { voucher ->
                voucher.type == "PURCHASE" &&
                    voucher.status == "POSTED" &&
                    voucher.partyId != null &&
                    voucher.outstandingAmount > 0.0
            }
            .sortedByDescending { it.date }
    }

    if (purchaseBills.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No active supplier bills are awaiting payment.", fontSize = 13.sp, color = AppColors.textSecondary)
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)
        ) {
            items(purchaseBills) { bill ->
                val party = parties.find { it.id == bill.partyId }
                val outstanding = bill.outstandingAmount.coerceAtLeast(0.0)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                    border = BorderStroke(1.dp, AppColors.border)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Purchase #${bill.voucherNo}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AppColors.textPrimary
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE8E6)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (outstanding < bill.netAmount) "PARTIAL" else "UNPAID",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC5221F),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Supplier: ${party?.name ?: "Unknown Supplier"}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = AppColors.textSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Bill Date: ${Utils.formatDate(bill.date)}", fontSize = 11.sp, color = AppColors.textTertiary)
                                party?.phone?.takeIf { it.isNotBlank() }?.let {
                                    Text("Phone: $it", fontSize = 11.sp, color = AppColors.textTertiary)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Original: ${Utils.formatIndianCurrency(bill.netAmount)}", fontSize = 11.sp, color = AppColors.textSecondary)
                                Text(
                                    "Paid so far: ${Utils.formatIndianCurrency((bill.netAmount - outstanding).coerceAtLeast(0.0))}",
                                    fontSize = 11.sp,
                                    color = AppColors.textSecondary
                                )
                                Text(
                                    "Remaining: ${Utils.formatIndianCurrency(outstanding)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.error
                                )
                            }
                        }

                        HorizontalDivider(color = AppColors.border.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    viewModel.setVoucherPrefillRequest(
                                        AppViewModel.VoucherPrefillRequest(
                                            voucherType = "PAYMENT",
                                            partyId = bill.partyId,
                                            invoiceId = bill.id,
                                            amount = outstanding
                                        )
                                    )
                                    navigateToNewVoucher(null)
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// TRIAL BALANCE VIEW (Grouped, Expandable, with Subheads)
// ═══════════════════════════════════════════════════════
@Composable
fun TrialBalanceView(
    ledgerBalances: Map<String, Double>,
    debtorsList: List<Pair<Party, Double>>,
    creditorsList: List<Pair<Party, Double>>,
    currentStockValue: Double,
    roundOffValue: Double,
    salesRevenue: Double,
    purchasesCost: Double,
    cgstPayable: Double,
    sgstPayable: Double,
    igstPayable: Double,
    cgstRec: Double,
    sgstRec: Double,
    igstRec: Double
) {
    var expandedGroups by remember { mutableStateOf(setOf<String>()) }
    val listState = rememberScrollState()

    val toggleGroup: (String) -> Unit = { groupName ->
        expandedGroups = if (expandedGroups.contains(groupName)) {
            expandedGroups - groupName
        } else {
            expandedGroups + groupName
        }
    }

    // Prepare groups:
    val capBalance = 0.0 // Custom manual capital additions if any
    val gstPayBalance = cgstPayable + sgstPayable + igstPayable
    val gstRecBalance = cgstRec + sgstRec + igstRec

    val cashBalance = ledgerBalances["Cash"] ?: 0.0
    val bankBalance = ledgerBalances["Bank"] ?: 0.0

    val debtorsTotal = debtorsList.sumOf { it.second }
    val creditorsTotal = creditorsList.sumOf { it.second }

    // Column grand summation
    val grandDr = purchasesCost + cashBalance + bankBalance + debtorsTotal + currentStockValue + gstRecBalance + (if (roundOffValue > 0.0) roundOffValue else 0.0)
    val grandCr = salesRevenue + CapitalAccountDefault + creditorsTotal + gstPayBalance + (if (roundOffValue < 0.0) -roundOffValue else 0.0)

    val difference = Math.abs(grandDr - grandCr)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(listState)
            .border(1.dp, AppColors.border, RoundedCornerShape(8.dp))
            .background(Colors.surface)
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.primary)
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("PARTICULARS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AppColors.textOnPrimary, modifier = Modifier.weight(1.5f))
            Text("DEBIT DR (₹)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AppColors.textOnPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text("CREDIT CR (₹)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AppColors.textOnPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }

        // Expanded Group helper
        @Composable
        fun TrialGroupHeader(name: String, dr: Double, cr: Double, isExpanded: Boolean, onClick: () -> Unit) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .padding(vertical = 12.dp, horizontal = 12.dp)
                    .background(AppColors.sectionHeaderBg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AppColors.primaryText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AppColors.primaryText,
                    modifier = Modifier.weight(1.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (dr > 0) String.format("%,.2f", dr) else "-",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (dr > 0) AppColors.debit else AppColors.textPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
                Text(
                    text = if (cr > 0) String.format("%,.2f", cr) else "-",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (cr > 0) AppColors.credit else AppColors.textPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
            HorizontalDivider(color = AppColors.divider)
        }

        @Composable
        fun TrialLedgerRow(name: String, balance: Double, isDr: Boolean, paddingLeft: Int = 24, rowIndex: Int = 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (rowIndex % 2 == 0) AppColors.tableRowEven else AppColors.tableRowOdd)
                    .padding(vertical = 8.dp, horizontal = 12.dp)
                    .padding(start = paddingLeft.dp)
            ) {
                Text(
                    text = name,
                    fontSize = 11.sp,
                    color = AppColors.textPrimary,
                    modifier = Modifier.weight(1.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isDr) String.format("%,.2f", balance) else "-",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.debit,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
                Text(
                    text = if (!isDr) String.format("%,.2f", balance) else "-",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.credit,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
            HorizontalDivider(color = AppColors.divider)
        }

        // 1. CAPITAL ACCOUNT GROUP
        val isCapExpanded = expandedGroups.contains("CAPITAL")
        TrialGroupHeader("Capital Account", dr = 0.0, cr = CapitalAccountDefault, isExpanded = isCapExpanded, onClick = { toggleGroup("CAPITAL") })
        if (isCapExpanded) {
            TrialLedgerRow("Proprietor Capital", CapitalAccountDefault, isDr = false)
        }

        // 2. SUNDRY CREDITORS GROUP
        val isCredsExpanded = expandedGroups.contains("CREDITORS")
        TrialGroupHeader("Sundry Creditors (Current Liabilities)", dr = 0.0, cr = creditorsTotal, isExpanded = isCredsExpanded, onClick = { toggleGroup("CREDITORS") })
        if (isCredsExpanded) {
            if (creditorsList.isEmpty()) {
                TrialLedgerRow("[No Supplier Dues]", 0.0, isDr = false)
            } else {
                creditorsList.forEach { (p, b) ->
                    TrialLedgerRow(p.name, b, isDr = false)
                }
            }
        }

        // 3. DUTIES & TAXES PAYABLE
        val isDutiesPayExpanded = expandedGroups.contains("DUTIESPAY")
        TrialGroupHeader("Duties & Taxes (GST Payable)", dr = 0.0, cr = gstPayBalance, isExpanded = isDutiesPayExpanded, onClick = { toggleGroup("DUTIESPAY") })
        if (isDutiesPayExpanded) {
            if (cgstPayable > 0) TrialLedgerRow("CGST Output Tax", cgstPayable, isDr = false)
            if (sgstPayable > 0) TrialLedgerRow("SGST Output Tax", sgstPayable, isDr = false)
            if (igstPayable > 0) TrialLedgerRow("IGST Output Tax", igstPayable, isDr = false)
        }

        // 4. FIXED ASSETS
        val isFixedExpanded = expandedGroups.contains("FIXED")
        TrialGroupHeader("Fixed Assets", dr = 0.0, cr = 0.0, isExpanded = isFixedExpanded, onClick = { toggleGroup("FIXED") })
        if (isFixedExpanded) {
            TrialLedgerRow("[No Fixed Asset Ledger Setup]", 0.0, isDr = true)
        }

        // 5. CURRENT ASSETS (CASH & BANK)
        val isCashBankExpanded = expandedGroups.contains("CASHBANK")
        TrialGroupHeader("Cash-in-hand & Bank Accounts", dr = (cashBalance + bankBalance), cr = 0.0, isExpanded = isCashBankExpanded, onClick = { toggleGroup("CASHBANK") })
        if (isCashBankExpanded) {
            TrialLedgerRow("Cash in Hand", cashBalance, isDr = true)
            TrialLedgerRow("Bank Account Balance", bankBalance, isDr = true)
        }

        // 6. SUNDRY DEBTORS GROUP
        val isDebtorsExpanded = expandedGroups.contains("DEBTORS")
        TrialGroupHeader("Sundry Debtors (Current Assets)", dr = debtorsTotal, cr = 0.0, isExpanded = isDebtorsExpanded, onClick = { toggleGroup("DEBTORS") })
        if (isDebtorsExpanded) {
            if (debtorsList.isEmpty()) {
                TrialLedgerRow("[No Active Debtor Dues]", 0.0, isDr = true)
            } else {
                debtorsList.forEach { (p, b) ->
                    TrialLedgerRow(p.name, b, isDr = true)
                }
            }
        }

        // 7. STOCK IN HAND
        val isStockExpanded = expandedGroups.contains("STOCK")
        TrialGroupHeader("Stock-in-hand (Inventory)", dr = currentStockValue, cr = 0.0, isExpanded = isStockExpanded, onClick = { toggleGroup("STOCK") })
        if (isStockExpanded) {
            TrialLedgerRow("Closing Stock Value", currentStockValue, isDr = true)
        }

        // 8. DUTIES & TAXES RECEIVABLE
        val isDutiesRecExpanded = expandedGroups.contains("DUTIESREC")
        TrialGroupHeader("Duties & Taxes (ITC Credit)", dr = gstRecBalance, cr = 0.0, isExpanded = isDutiesRecExpanded, onClick = { toggleGroup("DUTIESREC") })
        if (isDutiesRecExpanded) {
            if (cgstRec > 0) TrialLedgerRow("CGST Input Credit", cgstRec, isDr = true)
            if (sgstRec > 0) TrialLedgerRow("SGST Input Credit", sgstRec, isDr = true)
            if (igstRec > 0) TrialLedgerRow("IGST Input Credit", igstRec, isDr = true)
        }

        // 9. SALES ACCOUNT GROUP
        val isSalesExpanded = expandedGroups.contains("SALES")
        TrialGroupHeader("Sales Accounts", dr = 0.0, cr = salesRevenue, isExpanded = isSalesExpanded, onClick = { toggleGroup("SALES") })
        if (isSalesExpanded) {
            TrialLedgerRow("Sales Account Sales Revenue", salesRevenue, isDr = false)
        }

        // 10. PURCHASE ACCOUNT GROUP
        val isPurchExpanded = expandedGroups.contains("PURCHASE_GP")
        TrialGroupHeader("Purchase Accounts", dr = purchasesCost, cr = 0.0, isExpanded = isPurchExpanded, onClick = { toggleGroup("PURCHASE_GP") })
        if (isPurchExpanded) {
            TrialLedgerRow("Purchases Supplier Cost", purchasesCost, isDr = true)
        }

        // 11. INDIRECT EXPENSES & ADJUSTS
        if (roundOffValue != 0.0) {
            val isIndExpanded = expandedGroups.contains("INDIRECT")
            val drVal = if (roundOffValue > 0.0) roundOffValue else 0.0
            val crVal = if (roundOffValue < 0.0) -roundOffValue else 0.0
            TrialGroupHeader("Indirect Adjustments (Round Off)", dr = drVal, cr = crVal, isExpanded = isIndExpanded, onClick = { toggleGroup("INDIRECT") })
            if (isIndExpanded) {
                TrialLedgerRow("Invoice Round Off Ledger", Math.abs(roundOffValue), isDr = roundOffValue > 0.0)
            }
        }

        // Difference entry (balancing parameters)
        if (difference >= 0.01) {
            val reconcileWithCapital = grandDr - grandCr
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Colors.warning.copy(alpha = 0.12f))
                    .padding(10.dp)
            ) {
                Text(
                    text = "Difference in Opening / Capital values (Balanced on Capital):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary,
                    modifier = Modifier.weight(1.5f),
                    maxLines = 2
                )
                Text(
                    text = if (reconcileWithCapital < 0) String.format("DR: %,.2f", -reconcileWithCapital) else "-",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.error,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
                Text(
                    text = if (reconcileWithCapital > 0) String.format("CR: %,.2f", reconcileWithCapital) else "-",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.success,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
            HorizontalDivider()
        }

        // Grand Totals (CR = DR absolutely reconciles)
        val finalDrSum = if (grandDr > grandCr) grandDr else grandCr
        val finalCrSum = finalDrSum

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.primary)
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("GRAND BALANCED TOTALS (Tally Engine)", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = AppColors.textOnPrimary, modifier = Modifier.weight(1.5f))
            Text(String.format("₹ %,.2f", finalDrSum), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp, color = AppColors.textOnPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text(String.format("₹ %,.2f", finalCrSum), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 11.sp, color = AppColors.textOnPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
    }
}

// ═══════════════════════════════════════════════════════
// TRADING AND P&L VIEW (Two Columns: Debit vs Credit)
// ═══════════════════════════════════════════════════════
@Composable
fun TradingAndPLView(
    salesRevenue: Double,
    purchasesCost: Double,
    currentStockValue: Double,
    grossProfit: Double,
    netProfit: Double,
    roundOffValue: Double
) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "PART I: TRADING ACCOUNT (Gross Margin Analysis)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = AppColors.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Dual column split representation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppColors.border)
                ) {
                    // Left: DEBIT SIDE (Costs)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(0.5.dp, AppColors.border))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppColors.screenBg)
                                .padding(6.dp)
                        ) {
                            Text("DEBIT (DR) / COST", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = AppColors.textPrimary)
                        }
                        HorizontalDivider()

                        PLColumnItem("Opening Stock", 0.0)
                        PLColumnItem("Purchase Cost ledger", purchasesCost)

                        if (grossProfit >= 0) {
                            PLColumnItem("Gross Profit c/o", grossProfit, highlight = true, isProfit = true)
                        } else {
                            PLColumnItem("", 0.0)
                        }

                        HorizontalDivider()

                        val lhsSum = purchasesCost + (if (grossProfit >= 0) grossProfit else 0.0)
                        PLTotalItem(lhsSum)
                    }

                    // Right: CREDIT SIDE (Sales & Stock)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(0.5.dp, AppColors.border))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppColors.screenBg)
                                .padding(6.dp)
                        ) {
                            Text("CREDIT (CR) / REVENUE", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = AppColors.textPrimary)
                        }
                        HorizontalDivider()

                        PLColumnItem("Sales Value ledger", salesRevenue)
                        PLColumnItem("Stock Valuation", currentStockValue)

                        if (grossProfit < 0) {
                            PLColumnItem("Gross Loss c/o", -grossProfit, highlight = true, isProfit = false)
                        } else {
                            PLColumnItem("", 0.0)
                        }

                        HorizontalDivider()

                        val rhsSum = salesRevenue + currentStockValue + (if (grossProfit < 0) -grossProfit else 0.0)
                        PLTotalItem(rhsSum)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "PART II: PROFIT & LOSS ACCOUNT (Business Net Margin)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = AppColors.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppColors.border)
                ) {
                    // Left Column (Loss & Indirect Expenses)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(0.5.dp, AppColors.border))
                    ) {
                         Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppColors.screenBg)
                                .padding(6.dp)
                        ) {
                            Text("DEBIT (DR) / CHARGES", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = AppColors.textPrimary)
                        }
                        HorizontalDivider()

                        if (grossProfit < 0) {
                            PLColumnItem("Gross Loss b/f", -grossProfit)
                        } else {
                            PLColumnItem("", 0.0)
                        }

                        if (roundOffValue > 0) {
                            PLColumnItem("Round Off Expense", roundOffValue)
                        }

                        if (netProfit >= 0) {
                            PLColumnItem("NET REAL PROFIT", netProfit, highlight = true, isProfit = true)
                        }

                        HorizontalDivider()

                        val lhsSum = (if (grossProfit < 0) -grossProfit else 0.0) + (if (roundOffValue > 0) roundOffValue else 0.0) + (if (netProfit >= 0) netProfit else 0.0)
                        PLTotalItem(lhsSum)
                    }

                    // Right Column (Gross Profit B/F and Indirect Income)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(0.5.dp, AppColors.border))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppColors.screenBg)
                                .padding(6.dp)
                        ) {
                            Text("CREDIT (CR) / GAINS", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = AppColors.textPrimary)
                        }
                        HorizontalDivider()

                        if (grossProfit >= 0) {
                            PLColumnItem("Gross Profit b/f", grossProfit)
                        } else {
                            PLColumnItem("", 0.0)
                        }

                        if (roundOffValue < 0) {
                            PLColumnItem("Round Off Income", -roundOffValue)
                        }

                        if (netProfit < 0) {
                            PLColumnItem("NET REAL LOSS", -netProfit, highlight = true, isProfit = false)
                        }

                        HorizontalDivider()

                        val rhsSum = (if (grossProfit >= 0) grossProfit else 0.0) + (if (roundOffValue < 0) -roundOffValue else 0.0) + (if (netProfit < 0) -netProfit else 0.0)
                        PLTotalItem(rhsSum)
                    }
                }
            }
        }
    }
}

@Composable
fun PLColumnItem(name: String, amount: Double, highlight: Boolean = false, isProfit: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (name.isNotEmpty()) {
            Text(
                text = name,
                fontSize = 10.sp,
                color = if (highlight) (if (isProfit) AppColors.credit else AppColors.debit) else AppColors.textSecondary,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = String.format("%.2f", amount),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (highlight) (if (isProfit) AppColors.credit else AppColors.debit) else AppColors.textSecondary,
                textAlign = TextAlign.End
            )
        } else {
            Text("-", fontSize = 10.sp, color = Color.LightGray)
        }
    }
    HorizontalDivider(color = AppColors.divider)
}

@Composable
fun PLTotalItem(total: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.primary.copy(alpha = 0.08f))
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Total", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = AppColors.primary)
        Text(
            text = String.format("₹%,.2f", total),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = AppColors.primary,
            textAlign = TextAlign.End
        )
    }
}

// ═══════════════════════════════════════════════════════
// BALANCE SHEET VIEW (Two Columns: Liabilities vs Assets)
// ═══════════════════════════════════════════════════════
@Composable
fun BalanceSheetView(
    cashVal: Double,
    bankVal: Double,
    totalDebtors: Double,
    closingStockVal: Double,
    totalCreditors: Double,
    liabilitiesDuties: Double,
    assetsDuties: Double,
    balancingCapital: Double,
    netProfit: Double,
    totalAssets: Double
) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
    ) {
        Text(
            "Double Entry Statement of Assets, Liabilities, and Owner's Capital",
            fontSize = 12.sp,
            color = AppColors.textSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppColors.border)
        ) {
            // Left Column: Liabilities
            Column(
                modifier = Modifier
                    .weight(1f)
                    .border(BorderStroke(0.5.dp, AppColors.border))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.screenBg)
                        .padding(8.dp)
                ) {
                    Text("LIABILITIES & EQUITY", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AppColors.textPrimary)
                }
                HorizontalDivider()

                PLColumnItem("Capital (Balancing Account)", balancingCapital - netProfit)
                PLColumnItem("Net Profit this term", netProfit, highlight = true, isProfit = true)
                PLColumnItem("Sundry Creditors (Dues)", totalCreditors)

                if (liabilitiesDuties > 0) {
                    PLColumnItem("GST Duties Liability", liabilitiesDuties)
                } else {
                    PLColumnItem("", 0.0)
                }

                HorizontalDivider()
                PLTotalItem(totalAssets)
            }

            // Right Column: Assets
            Column(
                modifier = Modifier
                    .weight(1f)
                    .border(BorderStroke(0.5.dp, AppColors.border))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.screenBg)
                        .padding(8.dp)
                ) {
                    Text("ASSETS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AppColors.textPrimary)
                }
                HorizontalDivider()

                PLColumnItem("Stock-in-hand Inventory", closingStockVal)
                PLColumnItem("Sundry Debtors", totalDebtors)
                PLColumnItem("Cash-in-hand Account", cashVal)
                PLColumnItem("Bank Account Reserves", bankVal)

                if (assetsDuties > 0) {
                    PLColumnItem("GST Input Tax Assets", assetsDuties)
                } else {
                    PLColumnItem("", 0.0)
                }

                HorizontalDivider()
                PLTotalItem(totalAssets)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// GST SUMMARY STATEMENT VIEW
// ═══════════════════════════════════════════════════════
@Composable
fun GSTSummaryView(
    cgstPay: Double,
    sgstPay: Double,
    igstPay: Double,
    cgstRec: Double,
    sgstRec: Double,
    igstRec: Double,
    outputTotal: Double,
    inputTotal: Double,
    netPayable: Double
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("OUTWARD SUPPLY LIABILITIES (OUTPUT TAX)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.primary)
                HorizontalDivider()

                ReportRow("CGST Output Collector", cgstPay)
                ReportRow("SGST Output Collector", sgstPay)
                ReportRow("IGST Output Collector", igstPay)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Outward Tax Liability:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textPrimary)
                    Text(String.format("₹ %,.2f", outputTotal), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textPrimary)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("INWARD SUPPLY CREDIT offsets (INPUT TAX CREDIT)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.primary)
                HorizontalDivider()

                ReportRow("CGST Input credits", cgstRec)
                ReportRow("SGST Input credits", sgstRec)
                ReportRow("IGST Input credits", igstRec)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Input Tax credits (ITC):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textPrimary)
                    Text(String.format("₹ %,.2f", inputTotal), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textPrimary)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("GST PAYABLE POSITION (NET TO FILE)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.primary)
                HorizontalDivider()

                val label = if (netPayable >= 0) "Net Payable GST to Government:" else "Carryover ITC balance (Asset Credit):"
                val color = if (netPayable >= 0) AppColors.error else AppColors.success

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, fontWeight = FontWeight.Black, fontSize = 13.sp, color = AppColors.textPrimary, modifier = Modifier.weight(1f))
                    Text(
                        text = String.format("₹ %,.2f", Math.abs(netPayable)),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = color
                    )
                }
                Text(
                    text = if (netPayable >= 0) "Payable on GSTR-3B filings before the 20th of the following month." else "Automated ITC offset will reduce the next tax period liabilities.",
                    fontSize = 11.sp,
                    color = AppColors.textSecondary
                )
            }
        }
    }
}

@Composable
fun ReportRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = AppColors.textTertiary)
        Text(String.format("%.2f", amount), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = AppColors.textSecondary)
    }
    HorizontalDivider(color = AppColors.divider)
}

// ═══════════════════════════════════════════════════════
// OUTSTANDING DEBTORS REMINDERS (Email Automation)
// ═══════════════════════════════════════════════════════
@Composable
fun DebtorsRemindersView(
    debtors: List<Pair<Party, Double>>,
    profile: BusinessProfile?,
    context: Context
) {
    if (debtors.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active outstanding debtor balances.", fontSize = 13.sp, color = AppColors.textSecondary)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(debtors) { (party, balance) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                    border = BorderStroke(1.dp, AppColors.border)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(party.name, fontWeight = FontWeight.Black, fontSize = 14.sp, color = AppColors.textPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Email: ${party.email.ifBlank { "N/A" }}", fontSize = 11.sp, color = AppColors.textSecondary)
                            Text("Phone: ${party.phone}", fontSize = 11.sp, color = AppColors.textSecondary)
                        }

                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = String.format("₹ %,.2f", balance),
                                fontWeight = FontWeight.Bold,
                                color = AppColors.error,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            if (party.email.isNotBlank()) {
                                Button(
                                    onClick = {
                                        sendDebtorReminderEmail(context, party, balance, profile)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Send Email", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("[No Email Configured]", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper to launch email intent
fun sendDebtorReminderEmail(context: Context, party: Party, balance: Double, profile: BusinessProfile?) {
    val bName = profile?.businessName ?: "Our Store"
    val bankDesc = if (profile != null && profile.accountNo.isNotBlank()) {
        "\nBank details for payments:\nBank: ${profile.bankName}\nA/C No: ${profile.accountNo}\nIFSC Name: ${profile.ifsc}\n"
    } else ""

    val subject = "Balance Statement Reminder - $bName"
    val emailText = """Dear ${party.name},

This is a professional notification from $bName. 

Your ledger account shows an active outstanding due amount of ₹ ${String.format("%,.2f", balance)}.

Please clear your pending bills at your earliest convenience.$bankDesc
If you have already processed the transfer, please share the transaction reference.

Sincerely,
$bName
Phone: ${profile?.phone ?: ""}
""".trimIndent()

    EmailComposer.compose(
        context = context,
        draft = EmailComposer.Draft(
            recipients = listOf(party.email),
            subject = subject,
            body = emailText
        ),
        chooserTitle = "Send Reminder Email"
    )
}

// ═══════════════════════════════════════════════════════
// OUTSTANDING CREDITORS LIST VIEW
// ═══════════════════════════════════════════════════════
@Composable
fun CreditorsListView(creditors: List<Pair<Party, Double>>) {
    if (creditors.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active outstanding supplier liabilities.", fontSize = 13.sp, color = AppColors.textSecondary)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
            items(creditors) { (party, balance) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                    border = BorderStroke(1.dp, AppColors.border)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(party.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.textPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Email: ${party.email.ifBlank { "N/A" }}", fontSize = 11.sp, color = AppColors.textSecondary)
                            Text("Phone: ${party.phone}", fontSize = 11.sp, color = AppColors.textSecondary)
                        }

                        Text(
                            text = String.format("₹ %,.2f", balance),
                            fontWeight = FontWeight.Bold,
                            color = AppColors.success,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// System Constant for simple retail accounts structure balancing
const val CapitalAccountDefault = 500000.00

@Composable
fun ReportMenuCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .premiumClickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AppColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = AppColors.textTertiary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppColors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AgedBillsListView(
    bills: List<com.example.data.BillReceivable>,
    parties: List<com.example.data.Party>,
    viewModel: com.example.ui.AppViewModel,
    navigateToNewVoucher: (String?) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val activeBills = remember(bills) { bills.filter { it.outstandingAmount > 0.0 } }
    
    var showDialog by remember { mutableStateOf(false) }
    var selectedBill by remember { mutableStateOf<com.example.data.BillReceivable?>(null) }
    
    var paidAmountText by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("BANK") } // CASH, BANK, UPI
    var receiptDate by remember { mutableStateOf(System.currentTimeMillis()) }

    if (activeBills.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("All credit invoices have been fully paid off!", fontSize = 13.sp, color = AppColors.textSecondary)
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)
        ) {
            items(activeBills) { bill ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                    border = BorderStroke(1.dp, AppColors.border)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Invoice #${bill.voucherNo}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AppColors.textPrimary
                            )
                            
                            val badgeBgColor: Color
                            val badgeTxtColor: Color
                            when (bill.status) {
                                "PAID" -> {
                                    badgeBgColor = Color(0xFFE6F4EA)
                                    badgeTxtColor = Color(0xFF137333)
                                }
                                "OVERDUE" -> {
                                    badgeBgColor = Color(0xFFFCE8E6)
                                    badgeTxtColor = Color(0xFFC5221F)
                                }
                                "PARTIAL" -> {
                                    badgeBgColor = Color(0xFFE8F0FE)
                                    badgeTxtColor = Color(0xFF1A73E8)
                                }
                                else -> { // UNPAID
                                    badgeBgColor = Color(0xFFFEF7E0)
                                    badgeTxtColor = Color(0xFFB06000)
                                }
                            }
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = badgeBgColor),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = bill.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeTxtColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = "Customer: ${bill.partyName}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = AppColors.textSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val sdf = remember { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()) }
                            val dateStr = sdf.format(java.util.Date(bill.billDate))
                            val dueDateStr = if (bill.dueDate != null) sdf.format(java.util.Date(bill.dueDate)) else "N/A"
                            
                            Column {
                                Text("Bill Date: $dateStr", fontSize = 11.sp, color = AppColors.textTertiary)
                                Text("Due Date: $dueDateStr (${bill.daysOverdue} days overdue)", fontSize = 11.sp, color = AppColors.textTertiary)
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Net bill: ₹${String.format("%,.2f", bill.originalAmount)}", fontSize = 11.sp, color = AppColors.textSecondary)
                                Text(
                                    "Pending: ₹${String.format("%,.2f", bill.outstandingAmount)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.error
                                )
                            }
                        }
                        
                        HorizontalDivider(color = AppColors.border.copy(alpha = 0.5f))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    viewModel.setVoucherPrefillRequest(
                                        AppViewModel.VoucherPrefillRequest(
                                            voucherType = "RECEIPT",
                                            partyId = bill.partyId,
                                            invoiceId = bill.voucherId,
                                            amount = bill.outstandingAmount
                                        )
                                    )
                                    navigateToNewVoucher(null)
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .pressScale()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Receive Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog && selectedBill != null) {
        val bill = selectedBill!!
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color(0xFFFFFFFF),
            titleContentColor = Color(0xFF0D0D0D),
            textContentColor = Color(0xFF0D0D0D),
            title = {
                Text(
                    "Receive Payment - Invoice #${bill.voucherNo}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0D0D0D)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Outstanding: ₹${String.format("%,.2f", bill.outstandingAmount)}",
                        fontWeight = FontWeight.Bold,
                        color = AppColors.error,
                        fontSize = 14.sp
                    )
                    
                    OutlinedTextField(
                        value = paidAmountText,
                        onValueChange = { paidAmountText = filterDecimalInput(it) },
                        label = { Text("Amount to Pay") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    Text("Payment Mode", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("CASH", "BANK", "UPI").forEach { mode ->
                            val isSel = paymentMode == mode
                            Button(
                                onClick = { paymentMode = mode },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) AppColors.primary else AppColors.screenBg,
                                    contentColor = if (isSel) Color.White else AppColors.textSecondary
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        1.dp,
                                        if (isSel) AppColors.primary else AppColors.border,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .pressScale()
                            ) {
                                Text(mode, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Text("Receipt Date", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val sdf = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }
                        Text(sdf.format(java.util.Date(receiptDate)), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        
                        OutlinedButton(
                            onClick = {
                                receiptDate += 24L * 3600L * 1000L
                            },
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.pressScale()
                        ) {
                            Text("+1 Day", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amtVal = paidAmountText.toDoubleOrNull()
                        if (amtVal == null || amtVal <= 0.0) {
                            Toast.makeText(context, "Enter a valid positive payment amount", Toast.LENGTH_SHORT).show()
                        } else if (amtVal > bill.outstandingAmount) {
                            Toast.makeText(context, "Payment amount cannot exceed outstanding balance", Toast.LENGTH_SHORT).show()
                        } else {
                            val receiptVoucherId = java.util.UUID.randomUUID().toString()
                            scope.launch {
                                val nextNo = viewModel.generateNextVoucherNo("RECEIPT", receiptDate)
                                val recvVoucher = com.example.data.Voucher(
                                    id = receiptVoucherId,
                                    voucherNo = nextNo,
                                    type = "RECEIPT",
                                    date = receiptDate,
                                    partyId = bill.partyId,
                                    narration = "Amount received from ${bill.partyName} on Invoice #${bill.voucherNo}",
                                    taxableAmount = amtVal,
                                    cgst = 0.0,
                                    sgst = 0.0,
                                    igst = 0.0,
                                    roundOff = 0.0,
                                    netAmount = amtVal,
                                    paymentMode = paymentMode,
                                    chequeNo = null,
                                    chequeDate = null,
                                    bankName = null,
                                    isIgst = false,
                                    status = "POSTED",
                                    outstandingAmount = 0.0,
                                    createdAt = System.currentTimeMillis()
                                )
                                
                                viewModel.saveVoucher(recvVoucher, emptyList(), bill.partyName) {
                                    val allocation = com.example.data.ReceiptAllocation(
                                        id = java.util.UUID.randomUUID().toString(),
                                        receiptId = receiptVoucherId,
                                        invoiceId = bill.voucherId,
                                        allocatedAmount = amtVal,
                                        createdAt = System.currentTimeMillis()
                                    )
                                    viewModel.insertAllocation(allocation)
                                    Toast.makeText(context, "Payment of ₹${String.format("%,.2f", amtVal)} captured!", Toast.LENGTH_SHORT).show()
                                    showDialog = false
                                }
                            }
                        }
                    }
                    ,
                    modifier = Modifier.pressScale()
                ) {
                    Text("Capture Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}
