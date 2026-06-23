package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LedgerAccount
import com.example.ui.AppViewModel
import com.example.ui.theme.AppColors
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerListScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val ledgerAccounts by viewModel.ledgerAccounts.collectAsState()
    val ledgerEntries by viewModel.ledgerEntries.collectAsState()
    var viewingLedger by remember { mutableStateOf<LedgerAccount?>(null) }
    var editingLedger by remember { mutableStateOf<LedgerAccount?>(null) }

    val balanceMap = remember(ledgerEntries) {
        ledgerEntries.groupingBy { it.accountHead }
            .fold(0.0) { acc, entry -> acc + entry.debit - entry.credit }
    }

    val groupedAccounts = remember(ledgerAccounts) {
        ledgerAccounts.groupBy { it.groupName }.toSortedMap()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppColors.screenBg,
        topBar = {
            TopAppBar(
                title = { Text("Ledger Books", fontWeight = FontWeight.Bold, color = AppColors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.topBarBg,
                    titleContentColor = AppColors.textPrimary,
                    navigationIconContentColor = AppColors.textSecondary
                )
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
                .padding(start = 16.dp, end = 16.dp, bottom = 100.dp)
        ) {
            Text(
                text = "Full ledger chart with current debit / credit balances",
                color = AppColors.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (ledgerAccounts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No ledger accounts configured yet.", color = AppColors.textSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    contentPadding = PaddingValues(start = 0.dp, end = 0.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    groupedAccounts.forEach { (groupName, accounts) ->
                        item(key = "header_$groupName") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AppColors.sectionHeaderBg, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    groupName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = AppColors.primaryText
                                )
                            }
                        }
                        accounts.forEachIndexed { rowIndex, account ->
                            item(key = account.id) {
                                EditableLedgerAccountRow(
                                    account = account,
                                    balance = balanceMap[account.name] ?: 0.0,
                                    rowIndex = rowIndex,
                                    onView = { viewingLedger = account },
                                    onEdit = { editingLedger = account }
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }

    viewingLedger?.let { account ->
        AlertDialog(
            onDismissRequest = { viewingLedger = null },
            containerColor = AppColors.cardBg,
            titleContentColor = AppColors.textPrimary,
            textContentColor = AppColors.textSecondary,
            title = { Text(account.name, fontWeight = FontWeight.Bold, color = AppColors.textPrimary) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Group: ${account.groupName}", color = AppColors.textPrimary)
                    Text("Opening Balance: ${account.openingBalance}", color = AppColors.textPrimary)
                    Text("Balance Type: ${account.balanceType}", color = AppColors.textPrimary)
                    if (account.isParty == 1) {
                        if (account.phone.isNotBlank()) Text("Phone: ${account.phone}", color = AppColors.textPrimary)
                        if (account.email.isNotBlank()) Text("Email: ${account.email}", color = AppColors.textPrimary)
                        if (account.address.isNotBlank()) Text("Address: ${account.address}", color = AppColors.textPrimary)
                    }
                    if (account.isSystem == 1) {
                        Text("System account - name cannot be changed", color = AppColors.textTertiary, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewingLedger = null
                    editingLedger = account
                }) {
                    Text("Edit", color = AppColors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewingLedger = null }) {
                    Text("Close", color = AppColors.textPrimary)
                }
            }
        )
    }

    editingLedger?.let { account ->
        EditLedgerSheet(
            ledger = account,
            onDismiss = { editingLedger = null },
            onSave = { updated ->
                viewModel.insertLedgerAccount(updated)
                editingLedger = null
            }
        )
    }
}

@Composable
private fun LedgerAccountRow(
    account: LedgerAccount,
    balance: Double,
    rowIndex: Int,
    onView: () -> Unit,
    onEdit: () -> Unit
) {
    val isDr = balance >= 0
    val absBal = kotlin.math.abs(balance)
    val rowBg = if (rowIndex % 2 == 0) AppColors.tableRowEven else AppColors.tableRowOdd

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .border(0.5.dp, AppColors.divider)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(account.name, fontWeight = FontWeight.Normal, fontSize = 13.sp, color = AppColors.textPrimary, modifier = Modifier.weight(1f))
        Text(
            text = if (absBal < 0.01) "Settled" else "₹ ${"%,.2f".format(absBal)} ${if (isDr) "DR" else "CR"}",
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = when {
                absBal < 0.01 -> AppColors.credit
                isDr -> AppColors.debit
                else -> AppColors.credit
            }
        )
    }
}

@Composable
private fun EditableLedgerAccountRow(
    account: LedgerAccount,
    balance: Double,
    rowIndex: Int,
    onView: () -> Unit,
    onEdit: () -> Unit
) {
    val isDr = balance >= 0
    val absBal = kotlin.math.abs(balance)
    val rowBg = if (rowIndex % 2 == 0) AppColors.tableRowEven else AppColors.tableRowOdd

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .border(0.5.dp, AppColors.divider)
            .clickable(onClick = onView)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(account.name, fontWeight = FontWeight.Normal, fontSize = 13.sp, color = AppColors.textPrimary)
            if (account.isSystem == 1) {
                Text("System account", fontSize = 11.sp, color = AppColors.textTertiary)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (absBal < 0.01) "Settled" else "Rs ${"%,.2f".format(absBal)} ${if (isDr) "DR" else "CR"}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = when {
                    absBal < 0.01 -> AppColors.credit
                    isDr -> AppColors.debit
                    else -> AppColors.credit
                }
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit ledger", tint = AppColors.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditLedgerSheet(
    ledger: LedgerAccount,
    onDismiss: () -> Unit,
    onSave: (LedgerAccount) -> Unit
) {
    var ledgerName by remember(ledger.id) { mutableStateOf(ledger.name) }
    var groupName by remember(ledger.id) { mutableStateOf(ledger.groupName) }
    var openingBalance by remember(ledger.id) { mutableStateOf(if (ledger.openingBalance == 0.0) "" else ledger.openingBalance.toString()) }
    var balanceType by remember(ledger.id) { mutableStateOf(ledger.balanceType) }
    var phone by remember(ledger.id) { mutableStateOf(ledger.phone) }
    var email by remember(ledger.id) { mutableStateOf(ledger.email) }
    var address by remember(ledger.id) { mutableStateOf(ledger.address) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.cardBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Edit Ledger", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.textPrimary)

            OutlinedTextField(
                value = ledgerName,
                onValueChange = { ledgerName = it },
                label = { Text("Ledger Name", color = Color(0xFF444444)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = ledger.isSystem == 0,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.inputBg,
                    unfocusedContainerColor = AppColors.inputBg,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary
                )
            )

            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Under Group", color = Color(0xFF444444)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = ledger.isSystem == 0,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.inputBg,
                    unfocusedContainerColor = AppColors.inputBg,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary
                )
            )

            if (ledger.isSystem == 1) {
                Text("System account - name cannot be changed", color = AppColors.textTertiary, fontSize = 12.sp)
            }

            if (ledger.name == "Capital Account") {
                Text("Enter your total opening capital", color = AppColors.textTertiary, fontSize = 12.sp)
            }

            OutlinedTextField(
                value = openingBalance,
                onValueChange = { openingBalance = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Opening Balance", color = Color(0xFF444444)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                prefix = { Text("Rs", color = AppColors.textPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.inputBg,
                    unfocusedContainerColor = AppColors.inputBg,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("DR", "CR").forEach { type ->
                    FilterChip(
                        selected = balanceType == type,
                        onClick = { balanceType = type },
                    label = { Text(type, color = AppColors.textPrimary) }
                )
            }
            }

            if (ledger.isParty == 1) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone", color = Color(0xFF444444)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.inputBg,
                        unfocusedContainerColor = AppColors.inputBg,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary
                    )
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = Color(0xFF444444)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.inputBg,
                        unfocusedContainerColor = AppColors.inputBg,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary
                    )
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address", color = Color(0xFF444444)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.inputBg,
                        unfocusedContainerColor = AppColors.inputBg,
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary
                    )
                )
            }

            Button(
                onClick = {
                    onSave(
                        ledger.copy(
                            name = if (ledger.isSystem == 1) ledger.name else ledgerName.ifBlank { ledger.name },
                            groupName = if (ledger.isSystem == 1) ledger.groupName else groupName.ifBlank { ledger.groupName },
                            openingBalance = openingBalance.toDoubleOrNull() ?: 0.0,
                            balanceType = balanceType,
                            phone = phone,
                            email = email,
                            address = address
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) {
                Text("Save", color = AppColors.textOnPrimary)
            }
        }
    }
}
