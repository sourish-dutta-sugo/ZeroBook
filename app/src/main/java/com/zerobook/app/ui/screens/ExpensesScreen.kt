package com.zerobook.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zerobook.app.data.Expense
import com.zerobook.app.data.Utils
import com.zerobook.app.data.filterDecimalInput
import com.zerobook.app.services.ExportStorageManager
import com.zerobook.app.services.ExportTarget
import com.zerobook.app.ui.AppViewModel
import com.zerobook.app.ui.animation.premiumFabEntrance
import com.zerobook.app.ui.animation.pressScale
import com.zerobook.app.ui.theme.AppColors
import java.io.File
import java.util.UUID

private val expenseCategories = listOf(
    "Rent", "Electricity", "Water", "Telephone", "Internet",
    "Staff Salary", "Transport", "Packaging", "Maintenance",
    "Office Supplies", "Advertising", "Insurance", "Bank Charges",
    "Miscellaneous", "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val expenses by viewModel.expenses.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableStateOf("ALL") }
    val filteredExpenses = remember(expenses, activeFilter) {
        expenses.filter { activeFilter == "ALL" || it.category == activeFilter }
    }

    if (showForm) {
        ExpenseEntryScreen(viewModel = viewModel, onDismiss = { showForm = false })
        return
    }

    Scaffold(
        containerColor = AppColors.screenBg,
        topBar = {
            TopAppBar(
                title = { Text("Expenses", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.cardBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showForm = true },
                containerColor = AppColors.primary,
                modifier = Modifier
                    .premiumFabEntrance()
                    .pressScale()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add expense", tint = AppColors.textOnPrimary)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.screenBg)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = activeFilter == "ALL", onClick = { activeFilter = "ALL" }, label = { Text("All") })
                expenseCategories.take(4).forEach { category ->
                    FilterChip(selected = activeFilter == category, onClick = { activeFilter = category }, label = { Text(category) })
                }
            }
            Button(
                onClick = {
                    val csv = buildString {
                        append("date,category,description,amount,payment_mode,reference\n")
                        filteredExpenses.forEach { expense ->
                            append("${Utils.formatDate(expense.date)},${expense.category},${expense.description},${expense.amount},${expense.paymentMode},${expense.referenceNo}\n")
                        }
                    }
                    val result = ExportStorageManager.exportBytes(
                        context = viewModel.getApplication(),
                        bytes = csv.toByteArray(),
                        displayName = "ZeroBook_Expenses.csv",
                        mimeType = "text/csv",
                        target = ExportTarget.Reports
                    )
                    Toast.makeText(viewModel.getApplication(), "Saved to ${result.locationLabel}", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.pressScale()
            ) {
                Text("Export to CSV")
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(filteredExpenses, key = { it.id }) { expense ->
                    Card(colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(expense.category, fontWeight = FontWeight.Bold)
                                Text(expense.description.ifBlank { "No description" }, color = AppColors.textSecondary)
                                Text("${Utils.formatDate(expense.date)} | ${expense.paymentMode}", color = AppColors.textSecondary)
                            }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                Text(Utils.formatIndianCurrency(expense.amount), fontWeight = FontWeight.Bold, color = AppColors.primary)
                                if (expense.referenceNo.isNotBlank()) Text(expense.referenceNo, color = AppColors.textSecondary)
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
private fun ExpenseEntryScreen(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var category by remember { mutableStateOf(expenseCategories.first()) }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("CASH") }
    var referenceNo by remember { mutableStateOf("") }
    var attachmentPath by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    val attachLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        attachmentPath = uri?.toString().orEmpty()
    }

    Scaffold(
        containerColor = AppColors.screenBg,
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = Utils.formatDate(date), onValueChange = {}, readOnly = true, label = { Text("Date") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = "EXP/${viewModel.financialYear.value}/${System.currentTimeMillis().toString().takeLast(4)}", onValueChange = {}, readOnly = true, label = { Text("Expense No") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(8.dp)) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 0.dp)
            )
            DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                expenseCategories.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { category = option; categoryExpanded = false })
                }
            }
            Button(onClick = { categoryExpanded = true }) { Text("Choose Category") }
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = filterDecimalInput(it) },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("CASH", "BANK", "UPI", "CHEQUE").forEach { mode ->
                    FilterChip(selected = paymentMode == mode, onClick = { paymentMode = mode }, label = { Text(mode) })
                }
            }
            OutlinedTextField(value = referenceNo, onValueChange = { referenceNo = it }, label = { Text("Reference No") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { attachLauncher.launch(arrayOf("image/*", "application/pdf")) }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)) {
                Icon(Icons.Default.AttachFile, contentDescription = null, tint = AppColors.textOnPrimary)
                Text(if (attachmentPath.isBlank()) "Upload Receipt" else File(attachmentPath).name, color = AppColors.textOnPrimary)
            }
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (amountValue <= 0.0) {
                        Toast.makeText(viewModel.getApplication(), "Enter a valid amount", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveExpense(
                            Expense(
                                id = UUID.randomUUID().toString(),
                                date = date,
                                category = category,
                                description = description,
                                amount = amountValue,
                                paymentMode = paymentMode,
                                referenceNo = referenceNo,
                                attachmentPath = attachmentPath,
                                voucherNo = "EXP/${viewModel.financialYear.value}/${System.currentTimeMillis().toString().takeLast(4)}"
                            )
                        ) {
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) {
                Text("Save Expense", color = AppColors.textOnPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
