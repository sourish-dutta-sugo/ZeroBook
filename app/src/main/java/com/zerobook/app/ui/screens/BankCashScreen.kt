package com.zerobook.app.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerobook.app.data.*
import com.zerobook.app.ui.AppViewModel
import com.zerobook.app.ui.animation.premiumClickable
import com.zerobook.app.ui.animation.premiumFabEntrance
import com.zerobook.app.ui.animation.pressScale
import com.zerobook.app.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankCashScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val ledgerEntries by viewModel.ledgerEntries.collectAsState()
    val parties by viewModel.parties.collectAsState()

    var activeTab by remember { mutableStateOf("CASH") } // "CASH", "BANK"
    var showAddForm by remember { mutableStateOf(false) }

    // Calculate dynamic cash / bank running total balance
    val (cashTotal, bankTotal) = remember(ledgerEntries) {
        var cash = 0.0
        var bank = 0.0
        ledgerEntries.forEach { entry ->
            val delta = entry.debit - entry.credit
            if (entry.accountHead == "Cash") {
                cash += delta
            } else if (entry.accountHead == "Bank") {
                bank += delta
            }
        }
        cash to bank
    }

    val matchedTransactions = remember(transactions, activeTab) {
        transactions.filter { tx ->
            if (activeTab == "CASH") tx.mode == "CASH" else tx.mode != "CASH"
        }
    }

    if (showAddForm) {
        AddTransactionForm(
            viewModel = viewModel,
            parties = parties,
            onDismiss = { showAddForm = false }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = AppColors.screenBg,
            topBar = {
                TopAppBar(
                    title = { Text("Bank & Cash Register", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.cardBg)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddForm = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier
                        .premiumFabEntrance()
                        .pressScale()
                        .testTag("add_tx_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.screenBg)
                    .padding(innerPadding)
                    .imePadding()
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large balance summary card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, Color(0xFFE8E8E8), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CASH BALANCE", fontSize = 11.sp, color = AppColors.textSecondary, fontWeight = FontWeight.SemiBold)
                            Text(
                                Utils.formatIndianCurrency(cashTotal),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (cashTotal >= 0) SuccessGreen else DangerRed
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(38.dp)
                                .background(Color.LightGray)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BANK BALANCE", fontSize = 11.sp, color = AppColors.textSecondary, fontWeight = FontWeight.SemiBold)
                            Text(
                                Utils.formatIndianCurrency(bankTotal),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (bankTotal >= 0) SuccessGreen else DangerRed
                            )
                        }
                    }
                }

                // Sub-Tabs Bar selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf("CASH", "BANK")
                    tabs.forEach { tab ->
                        FilterChip(
                            selected = activeTab == tab,
                            onClick = { activeTab = tab },
                            label = { Text(tab, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp)) },
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                }

                // Header log text
                Text(
                    text = "TRANSACTION LOG",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                if (matchedTransactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No cash or bank transactions registered in this selected view.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding(),
                        contentPadding = PaddingValues(start = 0.dp, end = 0.dp, bottom = 120.dp)
                    ) {
                        items(matchedTransactions) { tx ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.5.dp, Color(0xFFE8E8E8), RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val icon = if (tx.type == "RECEIPT") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                                        val iconColor = if (tx.type == "RECEIPT") SuccessGreen else DangerRed
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = iconColor
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = tx.partyName ?: "General Direct",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF1A1A1A)
                                            )
                                            Text(text = "Mode: ${tx.mode} | ${tx.narration}", fontSize = 11.sp, color = Color.Gray)
                                            Text(text = Utils.formatDate(tx.date), fontSize = 10.sp, color = Color.LightGray)
                                        }
                                    }

                                    Text(
                                        text = "${if (tx.type == "RECEIPT") "+" else "-"} ${Utils.formatIndianCurrency(tx.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (tx.type == "RECEIPT") SuccessGreen else Color(0xFF1A1A1A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionForm(
    viewModel: AppViewModel,
    parties: List<Party>,
    onDismiss: () -> Unit
) {
    var type by remember { mutableStateOf("RECEIPT") } // "RECEIPT", "PAYMENT"
    var mode by remember { mutableStateOf("CASH") } // "CASH", "BANK", "CHEQUE", "UPI", "NEFT"
    var amountStr by remember { mutableStateOf("") }
    var selectedParty by remember { mutableStateOf<Party?>(null) }
    var narration by remember { mutableStateOf("") }

    // Cheque fields
    var chequeNo by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }

    var partyDropdownExpanded by remember { mutableStateOf(false) }
    var modeDropdownExpanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppColors.screenBg,
        topBar = {
            TopAppBar(
                title = { Text("Record manual flow", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.cardBg)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.screenBg)
                .padding(innerPadding)
                .imePadding()
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Receipt vs payment chips
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tx Type:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(12.dp))
                listOf("RECEIPT", "PAYMENT").forEach { opt ->
                    FilterChip(
                        selected = type == opt,
                        onClick = { type = opt },
                        label = { Text(opt) },
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            }

            // Mode dropdown selector
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = mode,
                    onValueChange = {},
                    label = { Text("Transfer Mode *") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { modeDropdownExpanded = true }
                        )
                    }
                )
                DropdownMenu(
                    expanded = modeDropdownExpanded,
                    onDismissRequest = { modeDropdownExpanded = false }
                ) {
                    val modes = listOf("CASH", "BANK", "CHEQUE", "UPI", "NEFT", "RTGS", "IMPS")
                    modes.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m) },
                            onClick = {
                                mode = m
                                modeDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = filterDecimalInput(it) },
                label = { Text("Transaction Amount *") },
                modifier = Modifier.fillMaxWidth().testTag("manual_tx_amount"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(8.dp)
            )

            // Select Party drop down
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedParty?.name ?: "No party (Direct Cash / Generic expense)",
                    onValueChange = {},
                    label = { Text("Linked Party Account") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { partyDropdownExpanded = true }
                        )
                    }
                )
                DropdownMenu(
                    expanded = partyDropdownExpanded,
                    onDismissRequest = { partyDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f).height(240.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("None (Direct Transaction)") },
                        onClick = {
                            selectedParty = null
                            partyDropdownExpanded = false
                        }
                    )
                    parties.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name) },
                            onClick = {
                                selectedParty = p
                                partyDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            if (mode == "CHEQUE") {
                OutlinedTextField(
                    value = chequeNo,
                    onValueChange = { chequeNo = it },
                    label = { Text("Cheque / draft Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Cleared Bank Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            OutlinedTextField(
                value = narration,
                onValueChange = { narration = it },
                label = { Text("Memo Narration") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            if (showError) {
                Text("Please fill in the amount with valid number values.", color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        showError = true
                    } else {
                        val transactionObj = BankCashTransaction(
                            id = UUID.randomUUID().toString(),
                            type = type,
                            mode = mode,
                            amount = amt,
                            date = System.currentTimeMillis(),
                            partyId = selectedParty?.id,
                            partyName = selectedParty?.name ?: "Generic Account Direct",
                            narration = narration,
                            chequeNo = if (mode == "CHEQUE") chequeNo else null,
                            chequeDate = if (mode == "CHEQUE") System.currentTimeMillis() else null,
                            bankName = if (mode == "CHEQUE") bankName else null
                        )
                        viewModel.saveTransaction(transactionObj) {
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_manual_tx_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text("Commit Transaction", fontWeight = FontWeight.Bold)
            }
        }
        }
    }
}
