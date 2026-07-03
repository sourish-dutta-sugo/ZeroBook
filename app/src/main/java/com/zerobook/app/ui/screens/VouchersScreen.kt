package com.zerobook.app.ui.screens

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import com.zerobook.app.data.*
import com.zerobook.app.data.EmailReminderScheduler
import com.zerobook.app.services.InvoiceGenerator
import com.zerobook.app.services.configureInvoiceWebView
import com.zerobook.app.ui.AppViewModel
import com.zerobook.app.ui.animation.premiumClickable
import com.zerobook.app.ui.animation.premiumFabEntrance
import com.zerobook.app.ui.animation.pressScale
import com.zerobook.app.ui.theme.AppColors
import com.zerobook.app.ui.theme.Colors
import com.zerobook.app.ui.theme.zeroBookInputColors
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.zerobook.app.utils.copyUriToInternalStorage
import com.zerobook.app.data.HsnLookup
import com.zerobook.app.data.HsnResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.resume
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.*

private data class ParsedBillItemDraft(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val hsn: String,
    val qty: String,
    val unit: String,
    val rate: String,
    val included: Boolean = true
)

private data class VoucherTypeCardData(
    val key: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accent: Color
)

private fun voucherTypeCards(): List<VoucherTypeCardData> = listOf(
    VoucherTypeCardData("JOURNAL", "Journal", "Manual ledger-style entry", Icons.Outlined.Edit, Color(0xFF334155)),
    VoucherTypeCardData("SALE", "Sales", "Record a sale to customer", Icons.AutoMirrored.Filled.ReceiptLong, Color(0xFF2563EB)),
    VoucherTypeCardData("PURCHASE", "Purchase", "Record purchase from supplier", Icons.Default.Store, Color(0xFF7C3AED)),
    VoucherTypeCardData("RECEIPT", "Receipt", "Receive payment from customer", Icons.Default.Payments, Color(0xFF059669)),
    VoucherTypeCardData("PAYMENT", "Payment", "Pay a supplier or expense", Icons.Default.CreditCard, Color(0xFFDC2626)),
    VoucherTypeCardData("SALE_RETURN", "Sales Return", "Customer returns goods", Icons.Default.SwapHoriz, Color(0xFFEA580C)),
    VoucherTypeCardData("PURCHASE_RETURN", "Purchase Return", "Return goods to supplier", Icons.Default.SwapHoriz, Color(0xFFB91C1C)),
    VoucherTypeCardData("BILLS_RECEIVABLE", "Bills Receivable", "View amounts owed to you", Icons.AutoMirrored.Filled.ReceiptLong, Color(0xFF0F766E)),
    VoucherTypeCardData("BILLS_PAYABLE", "Bills Payable", "View amounts you owe", Icons.AutoMirrored.Filled.ReceiptLong, Color(0xFF92400E)),
    VoucherTypeCardData("DEBIT_NOTE", "Debit Note", "Raise debit against party", Icons.Default.Description, Color(0xFF9333EA)),
    VoucherTypeCardData("CREDIT_NOTE", "Credit Note", "Issue credit to party", Icons.Default.Description, Color(0xFF1D4ED8)),
    VoucherTypeCardData("QUOTATION", "Quotation", "Create estimate or quote", Icons.Default.RequestQuote, Color(0xFF4F46E5)),
    VoucherTypeCardData("DELIVERY_CHALLAN", "Delivery Challan", "Record goods dispatch", Icons.Default.LocalShipping, Color(0xFF0891B2)),
    VoucherTypeCardData("INCOME", "Income", "Track incoming funds", Icons.Default.Payments, Color(0xFF0F766E)),
    VoucherTypeCardData("EXPENSE", "Expense", "Record a business expense", Icons.Default.Inventory2, Color(0xFFBE185D))
)

private fun voucherTypeLabel(type: String): String = when (type) {
    "SALE" -> "Sales"
    "PURCHASE" -> "Purchase"
    "RECEIPT" -> "Receipt"
    "PAYMENT" -> "Payment"
    "JOURNAL" -> "Journal"
    "SALE_RETURN" -> "Sales Return"
    "PURCHASE_RETURN" -> "Purchase Return"
    "BILLS_RECEIVABLE" -> "Bills Receivable"
    "BILLS_PAYABLE" -> "Bills Payable"
    "DEBIT_NOTE" -> "Debit Note"
    "CREDIT_NOTE" -> "Credit Note"
    "QUOTATION" -> "Quotation"
    "DELIVERY_CHALLAN" -> "Delivery Challan"
    "INCOME" -> "Income"
    "EXPENSE" -> "Expense"
    else -> type.replace('_', ' ').replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

private fun voucherBadgeColor(type: String): Color = AppColors.primary

private fun sortedVouchersForDisplay(
    vouchers: List<Voucher>,
    sortOption: String,
    startDate: Long?,
    endDate: Long?
): List<Voucher> {
    val baseList = when (sortOption) {
        "LATEST" -> vouchers.sortedByDescending { it.date }
        "OLDEST" -> vouchers.sortedBy { it.date }
        "THIS_WEEK" -> vouchers.filter { it.date >= System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000 }
        "THIS_MONTH" -> vouchers.filter { it.date >= System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000 }
        "THIS_YEAR" -> vouchers.filter { it.date >= System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000 }
        "CUSTOM" -> {
            val start = startDate ?: 0L
            val end = endDate ?: Long.MAX_VALUE
            vouchers.filter { voucher -> voucher.date in start..end }
        }
        else -> vouchers
    }

    return if (sortOption == "CUSTOM" && (startDate == null || endDate == null)) {
        vouchers
    } else {
        baseList
    }
}

private data class JournalUiRow(
    val id: String = UUID.randomUUID().toString(),
    val accountHead: String = "",
    val debitText: String = "",
    val creditText: String = ""
)

@Composable
private fun transportFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color(0xFF0D0D0D),
    unfocusedTextColor = Color(0xFF0D0D0D),
    focusedContainerColor = Color(0xFFFFFFFF),
    unfocusedContainerColor = Color(0xFFFFFFFF),
    focusedBorderColor = Color(0xFF1A73E8),
    unfocusedBorderColor = Color(0xFFE0E4EA)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransportDetailField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    imeAction: ImeAction,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = imeAction
    )
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color(0xFF444444)) },
        placeholder = { Text(placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    coroutineScope.launch {
                        delay(300)
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        colors = transportFieldColors()
    )
}

@Composable
private fun TransportDetailsSection(
    isTablet: Boolean,
    amount: Double,
    transportName: String,
    onTransportNameChange: (String) -> Unit,
    transportVehicle: String,
    onTransportVehicleChange: (String) -> Unit,
    transportLrNo: String,
    onTransportLrNoChange: (String) -> Unit,
    transportGstin: String,
    onTransportGstinChange: (String) -> Unit,
    transportDestination: String,
    onTransportDestinationChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TransportDetailField(
                        value = transportName,
                        onValueChange = onTransportNameChange,
                        label = "Transporter Name",
                        placeholder = "Name of transport agency",
                        imeAction = ImeAction.Next
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TransportDetailField(
                        value = transportVehicle,
                        onValueChange = onTransportVehicleChange,
                        label = "Vehicle No.",
                        placeholder = "e.g. WB-01-AB-1234",
                        imeAction = ImeAction.Next,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TransportDetailField(
                        value = transportLrNo,
                        onValueChange = onTransportLrNoChange,
                        label = "LR/GR No.",
                        placeholder = "Lorry receipt number",
                        imeAction = ImeAction.Next
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TransportDetailField(
                        value = transportGstin,
                        onValueChange = onTransportGstinChange,
                        label = "Transporter GSTIN",
                        placeholder = "Optional - 15 digit GSTIN",
                        imeAction = ImeAction.Next,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        )
                    )
                }
            }

            TransportDetailField(
                value = transportDestination,
                onValueChange = onTransportDestinationChange,
                label = "Destination",
                placeholder = "Delivery location",
                imeAction = ImeAction.Done,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )
        } else {
            TransportDetailField(
                value = transportName,
                onValueChange = onTransportNameChange,
                label = "Transporter Name",
                placeholder = "Name of transport agency",
                imeAction = ImeAction.Next
            )
            if (amount > 0.0 && transportName.isBlank()) {
                Text(
                    "Transporter name recommended for records",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            TransportDetailField(
                value = transportVehicle,
                onValueChange = onTransportVehicleChange,
                label = "Vehicle No.",
                placeholder = "e.g. WB-01-AB-1234",
                imeAction = ImeAction.Next,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next
                )
            )
            TransportDetailField(
                value = transportLrNo,
                onValueChange = onTransportLrNoChange,
                label = "LR/GR No.",
                placeholder = "Lorry receipt number",
                imeAction = ImeAction.Next
            )
            TransportDetailField(
                value = transportGstin,
                onValueChange = onTransportGstinChange,
                label = "Transporter GSTIN",
                placeholder = "Optional - 15 digit GSTIN",
                imeAction = ImeAction.Next,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next
                )
            )
            TransportDetailField(
                value = transportDestination,
                onValueChange = onTransportDestinationChange,
                label = "Destination",
                placeholder = "Delivery location",
                imeAction = ImeAction.Done,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VouchersScreen(
    viewModel: AppViewModel,
    isDesktop: Boolean = false,
    navigateToNewVoucher: (String?) -> Unit,
    navigateToInvoice: (String) -> Unit
) {
    val vouchers by viewModel.vouchers.collectAsState()
    val parties by viewModel.parties.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var pendingFilter by remember { mutableStateOf("ALL") }
    var pendingSort by remember { mutableStateOf("DEFAULT") }
    var pendingStartDate by remember { mutableStateOf<Long?>(null) }
    var pendingEndDate by remember { mutableStateOf<Long?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("DEFAULT") }
    var customStartDate by remember { mutableStateOf<Long?>(null) }
    var customEndDate by remember { mutableStateOf<Long?>(null) }

    val filteredVouchers = remember(vouchers, searchQuery, selectedTypeFilter, parties) {
        vouchers.filter { voucher ->
            val partyName = parties.find { it.id == voucher.partyId }?.name ?: "Cash / Bank"
            val matchesSearch = voucher.voucherNo.contains(searchQuery, ignoreCase = true) ||
                    partyName.contains(searchQuery, ignoreCase = true)
            val matchesType = when (selectedTypeFilter) {
                "ALL" -> true
                "INCOME" -> voucher.type in setOf("INCOME", "SALE", "RECEIPT")
                "EXPENSE" -> voucher.type in setOf("EXPENSE", "PURCHASE", "PAYMENT")
                else -> voucher.type == selectedTypeFilter
            }
            matchesSearch && matchesType
        }
    }

    val displayedVouchers = remember(filteredVouchers, sortOption, customStartDate, customEndDate) {
        sortedVouchersForDisplay(filteredVouchers, sortOption, customStartDate, customEndDate)
    }

    var selectedVoucherId by remember { mutableStateOf<String?>(null) }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isTablet = screenWidthDp >= 600

    LaunchedEffect(displayedVouchers, isDesktop) {
        if (isDesktop && selectedVoucherId == null && displayedVouchers.isNotEmpty()) {
            selectedVoucherId = displayedVouchers.first().id
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Filter vouchers", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                val filterOptions = listOf("ALL", "JOURNAL", "SALE", "PURCHASE", "RECEIPT", "PAYMENT", "SALE_RETURN", "PURCHASE_RETURN", "DEBIT_NOTE", "CREDIT_NOTE", "BILLS_RECEIVABLE", "BILLS_PAYABLE", "QUOTATION", "DELIVERY_CHALLAN", "INCOME", "EXPENSE")
                filterOptions.forEach { type ->
                    FilterChip(
                        selected = pendingFilter == type,
                        onClick = { pendingFilter = type },
                        label = { Text(voucherTypeLabel(type)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.primary,
                            selectedLabelColor = AppColors.textOnPrimary
                        )
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showFilterSheet = false }) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        selectedTypeFilter = pendingFilter
                        showFilterSheet = false
                    }) { Text("Apply") }
                }
            }
        }
    }

    if (showSortSheet) {
        ModalBottomSheet(onDismissRequest = { showSortSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Sort vouchers", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                val sortOptions = listOf(
                    "DEFAULT" to "Default order",
                    "LATEST" to "Latest first",
                    "OLDEST" to "Oldest first",
                    "THIS_WEEK" to "This week",
                    "THIS_MONTH" to "This month",
                    "THIS_YEAR" to "This year",
                    "CUSTOM" to "Custom date range"
                )
                sortOptions.forEach { (value, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { pendingSort = value },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label)
                        if (pendingSort == value) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = AppColors.primary)
                        }
                    }
                }
                if (pendingSort == "CUSTOM") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showStartDatePicker = true }) { Text(pendingStartDate?.let { Utils.formatDate(it) } ?: "Start date") }
                        OutlinedButton(onClick = { showEndDatePicker = true }) { Text(pendingEndDate?.let { Utils.formatDate(it) } ?: "End date") }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showSortSheet = false }) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        sortOption = pendingSort
                        customStartDate = pendingStartDate
                        customEndDate = pendingEndDate
                        showSortSheet = false
                    }) { Text("Apply") }
                }
            }
        }
    }

    if (showStartDatePicker) {
        val startPickerState = rememberDatePickerState(initialSelectedDateMillis = pendingStartDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pendingStartDate = startPickerState.selectedDateMillis
                    showStartDatePicker = false
                }) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = startPickerState, showModeToggle = false)
        }
    }

    if (showEndDatePicker) {
        val endPickerState = rememberDatePickerState(initialSelectedDateMillis = pendingEndDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pendingEndDate = endPickerState.selectedDateMillis
                    showEndDatePicker = false
                }) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = endPickerState, showModeToggle = false)
        }
    }

    if (isDesktop) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(360.dp).fillMaxHeight()) {
                Scaffold(
                    containerColor = Color(0xFFF2F4F7),
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { navigateToNewVoucher(null) },
                            containerColor = AppColors.primary,
                            contentColor = AppColors.textOnPrimary,
                            modifier = Modifier
                                .premiumFabEntrance()
                                .pressScale()
                                .testTag("add_voucher_fab")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Voucher")
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
                        Text(
                            text = "Vouchers",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary
                        )

                        RetailTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = "Search Vouchers",
                            placeholder = "Search by voucher...",
                            trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AppColors.textSecondary) },
                            modifier = Modifier.fillMaxWidth().testTag("voucher_search_bar")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(onClick = { pendingFilter = selectedTypeFilter; showFilterSheet = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Filter")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(onClick = { pendingSort = sortOption; pendingStartDate = customStartDate; pendingEndDate = customEndDate; showSortSheet = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sort")
                            }
                        }

                        if (displayedVouchers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No vouchers found.", color = AppColors.textSecondary)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(displayedVouchers) { voucher ->
                                    val partyName = parties.find { it.id == voucher.partyId }?.name ?: "Cash / Bank Account"
                                    val isSelected = selectedVoucherId == voucher.id
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(6.dp, RoundedCornerShape(16.dp))
                                            .border(
                                                1.dp,
                                                if (isSelected) AppColors.primary else AppColors.border.copy(alpha = 0.7f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .premiumClickable { selectedVoucherId = voucher.id },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) AppColors.primary.copy(alpha = 0.06f) else AppColors.cardBg
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = voucher.voucherNo,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF111827)
                                                )
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = AppColors.primary.copy(alpha = 0.12f)),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = voucherTypeLabel(voucher.type),
                                                        color = AppColors.primary,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = partyName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF374151)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = Utils.formatDate(voucher.date),
                                                    fontSize = 10.sp,
                                                    color = AppColors.textSecondary
                                                )
                                                Text(
                                                    text = Utils.formatIndianCurrency(voucher.netAmount),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF111827)
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                selectedVoucherId?.let { vId ->
                    InvoiceScreen(
                        viewModel = viewModel,
                        voucherId = vId,
                        onNavigateBack = { selectedVoucherId = null },
                        onEditVoucher = { id -> navigateToNewVoucher(id) }
                    )
                } ?: Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Select a voucher to see the layout representation.", color = AppColors.textSecondary)
                }
            }
        }
    } else {
        Scaffold(
            containerColor = Color(0xFFF2F4F7),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigateToNewVoucher(null) },
                    containerColor = AppColors.primary,
                    contentColor = AppColors.textOnPrimary,
                    modifier = Modifier
                        .premiumFabEntrance()
                        .pressScale()
                        .testTag("add_voucher_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Voucher")
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
                Text(
                    text = "Vouchers",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )

                // Search Bar
                RetailTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Search Vouchers",
                    placeholder = "Search by voucher number or party...",
                    trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AppColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("voucher_search_bar")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { pendingFilter = selectedTypeFilter; showFilterSheet = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Filter")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = { pendingSort = sortOption; pendingStartDate = customStartDate; pendingEndDate = customEndDate; showSortSheet = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sort")
                    }
                }

                if (vouchers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                tint = AppColors.textTertiary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No vouchers yet. Tap + to create your first sale.",
                                color = AppColors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (displayedVouchers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = AppColors.textTertiary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No matching vouchers found.", color = AppColors.textSecondary, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(displayedVouchers) { voucher ->
                            val partyName = parties.find { it.id == voucher.partyId }?.name ?: "Cash / Bank Account"

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(6.dp, RoundedCornerShape(16.dp))
                                    .border(1.dp, AppColors.border.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                    .premiumClickable { navigateToNewVoucher(voucher.id) },
                                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = voucher.voucherNo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF111827)
                                        )
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = AppColors.primary.copy(alpha = 0.12f)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = voucherTypeLabel(voucher.type),
                                                color = AppColors.primary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = partyName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF374151)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = Utils.formatDate(voucher.date),
                                            fontSize = 11.sp,
                                            color = AppColors.textSecondary
                                        )
                                        Text(
                                            text = Utils.formatIndianCurrency(voucher.netAmount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF111827)
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

// Interactive Sub-screen for Voucher Add / Post Flow
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NewVoucherScreen(
    viewModel: AppViewModel,
    voucherId: String? = null,
    isDesktop: Boolean = false,
    onNavigateBack: () -> Unit,
    onNavigateToInvoice: (String) -> Unit,
    onNavigateToPartyDetail: (String) -> Unit = {}
) {
    val profile by viewModel.profile.collectAsState()
    val parties by viewModel.parties.collectAsState()
    val products by viewModel.products.collectAsState()
    val ledgerEntries by viewModel.ledgerEntries.collectAsState()
    val ledgerAccounts by viewModel.ledgerAccounts.collectAsState()
    val vouchers by viewModel.vouchers.collectAsState()
    val voucherPrefillRequest by viewModel.voucherPrefillRequest.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val coroutineScope = rememberCoroutineScope()
    val isEditMode = !voucherId.isNullOrBlank()
    
    var step by remember { mutableStateOf(voucherId?.let { 2 } ?: 1) } // 1: Type selection, 2: Form & Line items
    var formStep by remember { mutableStateOf(1) }
    
    // Voucher details
    var selectedType by remember { mutableStateOf("SALE") }
    var voucherNo by remember { mutableStateOf("") }
    var voucherDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedParty by remember { mutableStateOf<Party?>(null) }
    var paymentMode by remember { mutableStateOf("CASH") }
    var partialAmountPaidText by remember { mutableStateOf("") }
    var partialPaymentSubmode by remember { mutableStateOf("CASH") }
    var creditDueDateText by remember { mutableStateOf("") }
    var isAdvanceReceipt by remember { mutableStateOf(false) }
    var advanceForText by remember { mutableStateOf("") }
    var narration by remember { mutableStateOf("") }
    val pendingInvoiceChecks = remember { mutableStateMapOf<String, Boolean>() }
    var returnReason by remember { mutableStateOf("Damaged") }
    var selectedSourceVoucherId by remember { mutableStateOf<String?>(null) }
    var sourceInvoiceSearchQuery by remember { mutableStateOf("") }
    var showReturnInvoiceSheet by remember { mutableStateOf(false) }
    val originalReturnItems = remember { mutableStateMapOf<String, VoucherItem>() }
    var chequeNo by remember { mutableStateOf("") }
    var chequeDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var bankName by remember { mutableStateOf("") }
    var attachmentUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var attachmentName by remember { mutableStateOf("") }
    var savedAttachmentPath by remember { mutableStateOf<String?>(null) }
    var isExtractingBill by remember { mutableStateOf(false) }
    var billExtractMessage by remember { mutableStateOf("") }
    var showParsedBillDialog by remember { mutableStateOf(false) }
    val parsedBillItems = remember { mutableStateListOf<ParsedBillItemDraft>() }
    var bankIfsc by remember { mutableStateOf("") }
    var bankAccountHolder by remember { mutableStateOf("") }
    var bankNameDetail by remember { mutableStateOf("") }
    var memoNumber by remember { mutableStateOf("") }
    var branchName by remember { mutableStateOf("") }
    var transportName by remember { mutableStateOf("") }
    var transportVehicle by remember { mutableStateOf("") }
    var transportLrNo by remember { mutableStateOf("") }
    var transportGstin by remember { mutableStateOf("") }
    var transportDestination by remember { mutableStateOf("") }
    var saleReferenceNo by remember { mutableStateOf("") }
    var saleOtherReferences by remember { mutableStateOf("") }
    var showAdvanceReceiptEmailDialog by remember { mutableStateOf(false) }
    var pendingAdvanceReceiptVoucherId by remember { mutableStateOf<String?>(null) }
    var pendingAdvanceReceiptEmail by remember { mutableStateOf("") }
    var pendingAfterSaveAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val journalRows = remember {
        mutableStateListOf(JournalUiRow(), JournalUiRow())
    }
    
    // Line items
    val lineItems = remember { mutableStateListOf<VoucherItem>() }
    val additionalCharges = remember { mutableStateListOf<AdditionalCharge>() }
    
    // Load existing data if editing
    LaunchedEffect(voucherId) {
        if (!voucherId.isNullOrBlank()) {
            val voucher = viewModel.getVoucherById(voucherId)
            if (voucher != null) {
                selectedType = voucher.type
                formStep = if (voucher.type == "SALE" || voucher.type == "PURCHASE") 3 else 1
                voucherNo = voucher.voucherNo
                voucherDate = voucher.date
                selectedParty = parties.find { it.id == voucher.partyId }
                paymentMode = voucher.paymentMode
                narration = voucher.narration ?: ""
                chequeNo = voucher.chequeNo.orEmpty()
                chequeDate = voucher.chequeDate ?: voucher.date
                bankName = voucher.bankName.orEmpty()
                savedAttachmentPath = voucher.attachmentPath
                attachmentName = voucher.attachmentPath?.let { File(it).name }.orEmpty()
                bankIfsc = voucher.bankIfsc.orEmpty()
                bankAccountHolder = voucher.bankAccountHolder.orEmpty()
                bankNameDetail = voucher.bankNameDetail.orEmpty()
                memoNumber = voucher.memoNumber.orEmpty()
                branchName = voucher.branchName.orEmpty()
                transportName = voucher.transporterName
                transportVehicle = voucher.vehicleNo
                transportLrNo = voucher.lrNo
                transportGstin = voucher.transportGstin
                transportDestination = voucher.destination
                val extras = viewModel.getVoucherSaveExtras(voucherId)
                partialAmountPaidText = extras.partialAmountPaid.takeIf { it > 0.0 }?.toString().orEmpty()
                partialPaymentSubmode = extras.partialPaymentSubmode.ifBlank { partialPaymentSubmode }
                creditDueDateText = extras.creditDueDate
                isAdvanceReceipt = extras.isAdvance
                advanceForText = extras.advanceFor
                val (referenceNo, otherReferences) = viewModel.getSaleVoucherReferenceFields(voucherId)
                saleReferenceNo = referenceNo
                saleOtherReferences = otherReferences
                
                val items = viewModel.getItemsForVoucher(voucherId).firstOrNull()
                if (items != null) {
                    lineItems.clear()
                    lineItems.addAll(items)
                }
                additionalCharges.clear()
                additionalCharges.addAll(InvoiceGenerator.parseAdditionalCharges(voucher.additionalChargesJson))
            }
        }
    }

    // GST control checking
    val hasGst = remember(profile) { !profile?.gstin.isNullOrBlank() }
    var globalGstEnabled by remember { mutableStateOf(false) }
    var globalGstRate by remember { mutableStateOf(0.0) }
    var showQuickAddPartyDialog by remember { mutableStateOf(false) }
    var showQuickAddProductDialog by remember { mutableStateOf(false) }
    var quickAddInitialProductName by remember { mutableStateOf("") }
    var pendingSelectedProduct by remember { mutableStateOf<Product?>(null) }
    var quickAddProductItemIndex by remember { mutableStateOf<Int?>(null) }
    var showItemEntrySheet by remember { mutableStateOf(false) }
    var editingItemIndex by remember { mutableStateOf<Int?>(null) }
    var showStockReportSheet by remember { mutableStateOf(false) }
    var inlineCreatedProductId by remember { mutableStateOf<String?>(null) }
    var inlineEditProductId by remember { mutableStateOf<String?>(null) }

    // Dialog trigger flags
    var showUpiPaymentDialog by remember { mutableStateOf(false) }
    var showConfirmSaveDialog by remember { mutableStateOf(false) }
    var saveShouldPrint by remember { mutableStateOf(false) }
    var showPrintReceiptDialog by remember { mutableStateOf(false) }
    var printedVoucherId by remember { mutableStateOf<String?>(null) }
    var isSavingAndPrinting by remember { mutableStateOf(false) }
    var pendingSaveAsPdfFile by remember { mutableStateOf<File?>(null) }
    var navigateBackAfterPdfSave by remember { mutableStateOf(false) }
    val saveInvoicePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val sourceFile = pendingSaveAsPdfFile
        pendingSaveAsPdfFile = null
        if (uri == null || sourceFile == null) {
            navigateBackAfterPdfSave = false
            return@rememberLauncherForActivityResult
        }
        coroutineScope.launch {
            runCatching {
                com.zerobook.app.services.ExportStorageManager.writeFileToUri(context, sourceFile, uri)
                android.widget.Toast.makeText(
                    context,
                    "Invoice saved successfully",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }.onFailure {
                android.widget.Toast.makeText(
                    context,
                    it.message ?: "Failed to save PDF",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            if (navigateBackAfterPdfSave) {
                navigateBackAfterPdfSave = false
                onNavigateBack()
            }
        }
    }

    // Populate dynamic number on type/date change
    LaunchedEffect(selectedType, voucherDate) {
        if (!isEditMode) {
            val nextNo = viewModel.generateNextVoucherNo(selectedType, voucherDate)
            voucherNo = nextNo
        }
    }

    LaunchedEffect(selectedType) {
        if (selectedType != "SALE_RETURN" && selectedType != "PURCHASE_RETURN") {
            selectedSourceVoucherId = null
            originalReturnItems.clear()
            sourceInvoiceSearchQuery = ""
        } else {
            additionalCharges.clear()
        }
    }

    // Party selection
    var showPartyPickerSheet by remember { mutableStateOf(false) }
    var partySearchQuery by remember { mutableStateOf("") }

    // Calculations
    val taxableAmount = remember { derivedStateOf { lineItems.sumOf { it.taxableAmount } } }
    val cgst = remember { derivedStateOf { lineItems.sumOf { it.cgstAmount } } }
    val sgst = remember { derivedStateOf { lineItems.sumOf { it.sgstAmount } } }
    val igst = remember { derivedStateOf { lineItems.sumOf { it.igstAmount } } }
    val additionalChargesTotal = remember { derivedStateOf { additionalCharges.sumOf { it.amount } } }
    val rawTotal = remember { derivedStateOf { taxableAmount.value + cgst.value + sgst.value + igst.value + additionalChargesTotal.value } }
    val netAmount = remember { derivedStateOf { Math.round(rawTotal.value).toDouble() } }
    val roundOff = remember { derivedStateOf { netAmount.value - rawTotal.value } }

    val isInterstate = remember(selectedParty, profile) {
        val pstate = selectedParty?.stateCode ?: ""
        val bstate = profile?.stateCode ?: ""
        pstate.isNotEmpty() && pstate != bstate
    }
    val isCustomerVoucher = selectedType == "SALE" || selectedType == "RECEIPT" || selectedType == "SALE_RETURN"
    val isThreeStepVoucher = selectedType == "SALE" || selectedType == "PURCHASE"
    val pagerState = rememberPagerState(initialPage = (formStep - 1).coerceIn(0, 2), pageCount = { 3 })
    val voucherTabs = remember(selectedType) {
        if (selectedType == "PURCHASE") {
            listOf(
                Triple("Supplier & Items", Icons.Default.Store, 0),
                Triple("Payment & Charges", Icons.Default.CreditCard, 1),
                Triple("Review", Icons.Default.Check, 2)
            )
        } else {
            listOf(
                Triple("Party & Items", Icons.AutoMirrored.Filled.Assignment, 0),
                Triple("Payment & Charges", Icons.Default.CreditCard, 1),
                Triple("Review", Icons.Default.Check, 2)
            )
        }
    }

    LaunchedEffect(isThreeStepVoucher, formStep) {
        if (isThreeStepVoucher) {
            pagerState.scrollToPage((formStep - 1).coerceIn(0, 2))
        }
    }

    LaunchedEffect(isThreeStepVoucher, pagerState.currentPage) {
        if (isThreeStepVoucher) {
            formStep = pagerState.currentPage + 1
        }
    }
    val partyTypeLabel = if (isCustomerVoucher) "customer" else "supplier"
    val hasTransportDetails = remember(
        transportName,
        transportVehicle,
        transportLrNo,
        transportGstin,
        transportDestination
    ) {
        transportName.isNotBlank() ||
            transportVehicle.isNotBlank() ||
            transportLrNo.isNotBlank() ||
            transportGstin.isNotBlank() ||
            transportDestination.isNotBlank()
    }

    fun isTransportChargeType(label: String): Boolean {
        return label.equals("Transport", ignoreCase = true) ||
            label.equals("Carriage Inward", ignoreCase = true) ||
            label.equals("Carriage Outward", ignoreCase = true) ||
            label.equals("Freight", ignoreCase = true)
    }
    val createPartyLabel = if (isCustomerVoucher) "Create new customer" else "Create new supplier"
    val visibleParties = remember(parties, selectedType) {
        parties.filter { party ->
            when {
                isCustomerVoucher -> party.type == "CUSTOMER" || party.type == "BOTH"
                selectedType == "PURCHASE" || selectedType == "PAYMENT" || selectedType == "PURCHASE_RETURN" ->
                    party.type == "SUPPLIER" || party.type == "BOTH"
                else -> true
            }
        }
    }
    val partyBalanceMap = remember(visibleParties, ledgerEntries) {
        visibleParties.associate { party ->
            val openingSign = if (party.balanceType == "CR") -1 else 1
            val runningBalance = ledgerEntries
                .filter { it.accountHead == "Party: ${party.name}" }
                .sumOf { it.debit - it.credit }
            party.id to (party.openingBalance * openingSign + runningBalance)
        }
    }
    val filteredParties = remember(visibleParties, partySearchQuery) {
        if (partySearchQuery.isBlank()) {
            visibleParties
        } else {
            visibleParties.filter { party ->
                party.name.contains(partySearchQuery, ignoreCase = true) ||
                    party.phone.contains(partySearchQuery, ignoreCase = true)
            }
        }
    }

    fun Double?.orEmptyBalance(): Double = this ?: 0.0

    fun parseDueDateInput(value: String): Long? {
        value.toLongOrNull()?.let { return it }
        return runCatching {
            SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).parse(value)?.time
        }.getOrNull()
    }

    fun showDatePicker(
        currentValue: String,
        onDateSelected: (Long) -> Unit
    ) {
        val baseCalendar = Calendar.getInstance().apply {
            parseDueDateInput(currentValue)?.let { timeInMillis = it }
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onDateSelected(selectedCalendar.timeInMillis)
            },
            baseCalendar.get(Calendar.YEAR),
            baseCalendar.get(Calendar.MONTH),
            baseCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun setDirectAmount(amount: Double) {
        lineItems.clear()
        lineItems.add(
            VoucherItem(
                id = UUID.randomUUID().toString(),
                voucherId = "",
                productId = "",
                productName = "Direct Transaction",
                hsnCode = "",
                qty = 1.0,
                unit = "",
                rate = amount,
                discount = 0.0,
                discountType = "AMOUNT",
                taxableAmount = amount,
                gstRate = 0.0,
                cgstAmount = 0.0,
                sgstAmount = 0.0,
                igstAmount = 0.0,
                totalAmount = amount
            )
        )
    }

    suspend fun restoreReturnSourceInvoice(invoice: Voucher) {
        val sourceItems = viewModel.getItemsForVoucher(invoice.id).firstOrNull().orEmpty()
        selectedSourceVoucherId = invoice.id
        selectedParty = parties.find { it.id == invoice.partyId }
        originalReturnItems.clear()
        lineItems.clear()
        additionalCharges.clear()
        sourceItems.forEach { sourceItem ->
            val returnItemId = UUID.randomUUID().toString()
            originalReturnItems[returnItemId] = sourceItem
            lineItems.add(sourceItem.copy(id = returnItemId, voucherId = ""))
        }
        narration = returnReason
    }

    val pendingInvoices = remember(selectedType, selectedParty, vouchers) {
        val invoiceType = when (selectedType) {
            "RECEIPT" -> "SALE"
            "PAYMENT" -> "PURCHASE"
            else -> ""
        }
        if (invoiceType.isBlank() || selectedParty == null || (selectedType == "RECEIPT" && isAdvanceReceipt)) {
            emptyList()
        } else {
            vouchers.filter {
                it.type == invoiceType &&
                    it.partyId == selectedParty?.id &&
                    it.status == "POSTED" &&
                    (it.outstandingAmount > 0.0 ||
                        ((it.paymentMode == "CREDIT" || it.paymentMode == "PART PAYMENT") && it.netAmount > 0.0))
            }.sortedBy { it.date }
        }
    }
    val checkedInvoicesTotal = remember(pendingInvoices, pendingInvoiceChecks) {
        pendingInvoices.filter { pendingInvoiceChecks[it.id] != false }.sumOf { it.outstandingAmount }
    }
    val returnInvoiceType = when (selectedType) {
        "SALE_RETURN" -> "SALE"
        "PURCHASE_RETURN" -> "PURCHASE"
        else -> ""
    }
    val returnSourceInvoices = remember(returnInvoiceType, vouchers, parties, sourceInvoiceSearchQuery) {
        if (returnInvoiceType.isBlank()) {
            emptyList()
        } else {
            vouchers.filter { it.type == returnInvoiceType && it.status == "POSTED" }
                .filter { invoice ->
                    if (sourceInvoiceSearchQuery.isBlank()) true else {
                        val partyName = parties.find { it.id == invoice.partyId }?.name.orEmpty()
                        invoice.voucherNo.contains(sourceInvoiceSearchQuery, ignoreCase = true) ||
                            partyName.contains(sourceInvoiceSearchQuery, ignoreCase = true)
                    }
                }
                .sortedByDescending { it.date }
        }
    }

    LaunchedEffect(selectedType, selectedParty?.id, pendingInvoices.map { it.id + it.outstandingAmount }) {
        if ((selectedType == "RECEIPT" && !isAdvanceReceipt) || selectedType == "PAYMENT") {
            pendingInvoiceChecks.clear()
            pendingInvoices.forEach { pendingInvoiceChecks[it.id] = true }
            if (selectedParty != null) {
                setDirectAmount(checkedInvoicesTotal)
            }
        }
    }

    LaunchedEffect(voucherPrefillRequest, parties, pendingInvoices.map { it.id + it.outstandingAmount }) {
        val request = voucherPrefillRequest ?: return@LaunchedEffect
        if (request.voucherType == "RECEIPT" || request.voucherType == "PAYMENT") {
            selectedType = request.voucherType
            step = 2
            selectedParty = parties.find { it.id == request.partyId }
            if (selectedParty != null) {
                pendingInvoiceChecks.clear()
                pendingInvoices.forEach { invoice ->
                    pendingInvoiceChecks[invoice.id] = request.invoiceId == null || invoice.id == request.invoiceId
                }
                setDirectAmount(request.amount ?: pendingInvoices.filter { pendingInvoiceChecks[it.id] != false }.sumOf { it.outstandingAmount })
            }
            viewModel.setVoucherPrefillRequest(null)
        } else if (request.voucherType == "SALE" && !request.sourceVoucherId.isNullOrBlank()) {
            val sourceVoucher = vouchers.find { it.id == request.sourceVoucherId }
            if (sourceVoucher != null) {
                selectedType = "SALE"
                step = 2
                selectedParty = parties.find { it.id == sourceVoucher.partyId }
                lineItems.clear()
                lineItems.addAll(viewModel.getItemsForVoucher(sourceVoucher.id).firstOrNull().orEmpty().map {
                    it.copy(id = UUID.randomUUID().toString(), voucherId = "")
                })
                additionalCharges.clear()
                if (sourceVoucher.type == "QUOTATION") {
                    paymentMode = "CREDIT"
                }
            }
            viewModel.setVoucherPrefillRequest(null)
        }
    }

    fun applyGstToItem(item: VoucherItem, rate: Double): VoucherItem {
        val dynamicGst = if (hasGst) (item.taxableAmount * rate / 100.0) else 0.0
        return item.copy(
            gstRate = rate,
            cgstAmount = if (isInterstate || !hasGst) 0.0 else dynamicGst / 2.0,
            sgstAmount = if (isInterstate || !hasGst) 0.0 else dynamicGst / 2.0,
            igstAmount = if (isInterstate && hasGst) dynamicGst else 0.0,
            totalAmount = item.taxableAmount + dynamicGst
        )
    }

    fun applyGlobalGstToAll() {
        for (i in lineItems.indices) {
            lineItems[i] = applyGstToItem(lineItems[i], globalGstRate)
        }
    }

    val validateAndSave: (Boolean) -> Unit = { shouldPrint ->
        if (selectedType == "JOURNAL") {
            val totalDr = journalRows.sumOf { it.debitText.toDoubleOrNull() ?: 0.0 }
            val totalCr = journalRows.sumOf { it.creditText.toDoubleOrNull() ?: 0.0 }
            val hasValidRows = journalRows.count { it.accountHead.isNotBlank() } >= 2
            if (!hasValidRows) {
                android.widget.Toast.makeText(context, "Add at least two journal rows.", android.widget.Toast.LENGTH_LONG).show()
            } else if (totalDr <= 0.0 || totalCr <= 0.0 || kotlin.math.abs(totalDr - totalCr) > 0.009) {
                android.widget.Toast.makeText(context, "Total DR must equal total CR.", android.widget.Toast.LENGTH_LONG).show()
            } else {
                saveShouldPrint = false
                showConfirmSaveDialog = true
            }
        } else {
            val isBill = (selectedType != "RECEIPT" && selectedType != "PAYMENT")
            if ((selectedType == "SALE_RETURN" || selectedType == "PURCHASE_RETURN") && selectedSourceVoucherId == null) {
                android.widget.Toast.makeText(context, "Cannot save: Select the original invoice first.", android.widget.Toast.LENGTH_LONG).show()
            } else if ((selectedType == "SALE_RETURN" || selectedType == "PURCHASE_RETURN") && lineItems.none { it.qty > 0.0 }) {
                android.widget.Toast.makeText(context, "Cannot save: Keep at least one return quantity above zero.", android.widget.Toast.LENGTH_LONG).show()
            } else if (isBill && lineItems.isEmpty()) {
                android.widget.Toast.makeText(context, "Cannot save: Add at least 1 item!", android.widget.Toast.LENGTH_LONG).show()
            } else if (isBill && lineItems.any { it.qty <= 0.0 }) {
                android.widget.Toast.makeText(context, "Cannot save: Invalid quantities!", android.widget.Toast.LENGTH_LONG).show()
            } else if (!isBill && netAmount.value <= 0.0) {
                android.widget.Toast.makeText(context, "Cannot save: Invalid amount!", android.widget.Toast.LENGTH_LONG).show()
            } else if (selectedType == "SALE" && paymentMode == "PART PAYMENT" && (partialAmountPaidText.toDoubleOrNull() ?: 0.0) <= 0.0) {
                android.widget.Toast.makeText(context, "Cannot save: Enter part payment amount.", android.widget.Toast.LENGTH_LONG).show()
            } else if (selectedType == "SALE" && paymentMode == "PART PAYMENT" && (partialAmountPaidText.toDoubleOrNull() ?: 0.0) >= netAmount.value) {
                android.widget.Toast.makeText(context, "Cannot save: Part payment must be less than net total.", android.widget.Toast.LENGTH_LONG).show()
            } else if (paymentMode == "CHEQUE" && (chequeNo.isBlank() || bankName.isBlank())) {
                android.widget.Toast.makeText(context, "Cannot save: Main Cheque details are missing!", android.widget.Toast.LENGTH_LONG).show()
            } else if (voucherNo.isBlank()) {
                android.widget.Toast.makeText(context, "Cannot save: Voucher Number is missing!", android.widget.Toast.LENGTH_LONG).show()
            } else if ((selectedType == "PURCHASE" || selectedType == "PAYMENT") && selectedParty == null) {
                android.widget.Toast.makeText(context, "Cannot save: Party is required for Purchase and Payment!", android.widget.Toast.LENGTH_LONG).show()
            } else {
                saveShouldPrint = shouldPrint
                showConfirmSaveDialog = true
            }
        }
    }

    val saveTheVoucher: (Boolean) -> Unit = { shouldPrint ->
        if (selectedType == "JOURNAL") {
            val finalId = voucherId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
            val lines = journalRows.mapNotNull { row ->
                if (row.accountHead.isBlank()) null else com.zerobook.app.data.JournalLine(
                    accountHead = row.accountHead,
                    debit = row.debitText.toDoubleOrNull() ?: 0.0,
                    credit = row.creditText.toDoubleOrNull() ?: 0.0
                )
            }
            val journalTotal = lines.sumOf { it.debit }
            viewModel.saveJournalVoucher(
                voucher = Voucher(
                    id = finalId,
                    voucherNo = voucherNo,
                    type = "JOURNAL",
                    date = voucherDate,
                    partyId = null,
                    narration = narration,
                    taxableAmount = journalTotal,
                    cgst = 0.0,
                    sgst = 0.0,
                    igst = 0.0,
                    roundOff = 0.0,
                    netAmount = journalTotal,
                    paymentMode = "JOURNAL",
                    chequeNo = null,
                    chequeDate = null,
                    bankName = null,
                    isIgst = false,
                    status = "POSTED"
                ),
                lines = lines
            ) {
                onNavigateBack()
            }
        } else {
            val finalId = voucherId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
            val finalLineItems = if (selectedType == "SALE_RETURN" || selectedType == "PURCHASE_RETURN") {
                lineItems.filter { it.qty > 0.0 }
            } else {
                lineItems.toList()
            }
            val finalAdditionalCharges = if (selectedType == "SALE_RETURN" || selectedType == "PURCHASE_RETURN") {
                emptyList()
            } else {
                additionalCharges.toList()
            }
            val partialAmountPaid = partialAmountPaidText.toDoubleOrNull() ?: 0.0
            val remainingCreditAmount = when {
                selectedType == "SALE" && paymentMode == "PART PAYMENT" -> (netAmount.value - partialAmountPaid).coerceAtLeast(0.0)
                selectedType == "SALE" && paymentMode == "CREDIT" -> netAmount.value
                else -> 0.0
            }
            val voucherObj = Voucher(
                id = finalId,
                voucherNo = voucherNo,
                type = selectedType,
                date = voucherDate,
                partyId = selectedParty?.id,
                narration = if (selectedType == "SALE_RETURN" || selectedType == "PURCHASE_RETURN") returnReason else narration,
                taxableAmount = taxableAmount.value,
                cgst = cgst.value,
                sgst = sgst.value,
                igst = igst.value,
                roundOff = roundOff.value,
                netAmount = netAmount.value,
                additionalChargesJson = InvoiceGenerator.additionalChargesToJson(finalAdditionalCharges),
                transporterName = transportName,
                lrNo = transportLrNo,
                vehicleNo = transportVehicle,
                transportGstin = transportGstin,
                destination = transportDestination,
                referenceNo = if (selectedType in setOf("SALE", "PURCHASE", "RECEIPT", "PAYMENT")) saleReferenceNo else "",
                paymentMode = paymentMode,
                chequeNo = if (paymentMode == "CHEQUE") chequeNo else null,
                chequeDate = if (paymentMode == "CHEQUE") chequeDate else null,
                bankName = if (paymentMode == "CHEQUE") bankName else null,
                isIgst = isInterstate,
                status = if (selectedType == "QUOTATION" || selectedType == "DELIVERY_CHALLAN") "DRAFT" else "POSTED",
                receiptImagePath = null,
                attachmentPath = savedAttachmentPath,
                bankIfsc = if (paymentMode == "BANK") bankIfsc else null,
                bankAccountHolder = if (paymentMode == "BANK") bankAccountHolder else null,
                bankNameDetail = if (paymentMode == "BANK" || paymentMode == "UPI") bankNameDetail else null,
                memoNumber = if (paymentMode == "CHEQUE") memoNumber else null,
                branchName = if (paymentMode == "BANK" || paymentMode == "CHEQUE") branchName else null,
                outstandingAmount = when {
                    selectedType == "SALE" && (paymentMode == "CREDIT" || paymentMode == "PART PAYMENT") -> remainingCreditAmount
                    selectedType == "PURCHASE" && selectedParty != null -> netAmount.value
                    else -> 0.0
                }
            )
            viewModel.saveVoucher(
                voucherObj,
                finalLineItems,
                selectedParty?.name,
                VoucherSaveExtras(
                    partialAmountPaid = partialAmountPaid,
                    partialPaymentSubmode = partialPaymentSubmode,
                    creditDueDate = creditDueDateText,
                    remainingCreditAmount = remainingCreditAmount,
                    isAdvance = selectedType == "RECEIPT" && isAdvanceReceipt,
                    advanceFor = advanceForText,
                    referenceNo = saleReferenceNo,
                    otherReferences = saleOtherReferences
                )
            ) {
                val completeSaveFlow = {
                    if (shouldPrint) {
                        printedVoucherId = finalId
                        showPrintReceiptDialog = true
                    } else if (selectedType == "SALE" || isEditMode) {
                        onNavigateToInvoice(finalId)
                    } else {
                        onNavigateBack()
                    }
                }
                if (selectedType in setOf("SALE", "PURCHASE", "RECEIPT", "PAYMENT")) {
                    viewModel.syncSaleVoucherReferenceFields(finalId, saleReferenceNo, saleOtherReferences)
                }
                if (
                    selectedType == "SALE" &&
                    paymentMode in listOf("CREDIT", "PART PAYMENT") &&
                    creditDueDateText.isNotBlank()
                ) {
                    viewModel.scheduleDueReminder(finalId, creditDueDateText)
                }
                if (selectedType == "RECEIPT" || selectedType == "PAYMENT") {
                    var remainingAllocation = netAmount.value
                    pendingInvoices
                        .filter { pendingInvoiceChecks[it.id] != false }
                        .forEach { invoice ->
                            if (remainingAllocation > 0.0) {
                                val allocated = minOf(remainingAllocation, invoice.outstandingAmount)
                                if (allocated > 0.0) {
                                    viewModel.insertAllocation(
                                        ReceiptAllocation(
                                            id = UUID.randomUUID().toString(),
                                            receiptId = finalId,
                                            invoiceId = invoice.id,
                                            allocatedAmount = allocated,
                                            createdAt = System.currentTimeMillis()
                                        )
                                    )
                                    remainingAllocation -= allocated
                                }
                            }
                        }
                }
                if (
                    selectedType == "RECEIPT" &&
                    isAdvanceReceipt &&
                    !selectedParty?.email.isNullOrBlank()
                ) {
                    pendingAdvanceReceiptVoucherId = finalId
                    pendingAdvanceReceiptEmail = selectedParty?.email.orEmpty()
                    pendingAfterSaveAction = completeSaveFlow
                    showAdvanceReceiptEmailDialog = true
                } else {
                    completeSaveFlow()
                }
            }
        }
    }

    if (showAdvanceReceiptEmailDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdvanceReceiptEmailDialog = false
                pendingAfterSaveAction?.invoke()
                pendingAfterSaveAction = null
            },
            title = { Text("Send advance receipt") },
            text = { Text("Send advance receipt to ${pendingAdvanceReceiptEmail}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val receiptVoucherId = pendingAdvanceReceiptVoucherId
                        val recipient = pendingAdvanceReceiptEmail
                        showAdvanceReceiptEmailDialog = false
                        if (!receiptVoucherId.isNullOrBlank() && recipient.isNotBlank()) {
                            coroutineScope.launch {
                                runCatching {
                                    val bundle = viewModel.getInvoiceRenderBundle(receiptVoucherId)
                                    if (bundle != null) {
                                        val pdfFile = InvoiceGenerator.renderBundleToPdf(context, bundle)
                                        InvoiceGenerator.emailInvoicePdf(
                                            context = context,
                                            pdfFile = pdfFile,
                                            recipient = recipient,
                                            subject = "Advance Receipt - ${bundle.document.invoiceNumber}",
                                            body = "Please find attached the advance receipt ${bundle.document.invoiceNumber} from ${bundle.document.business.businessName}."
                                        )
                                    }
                                }
                                pendingAfterSaveAction?.invoke()
                                pendingAfterSaveAction = null
                            }
                        } else {
                            pendingAfterSaveAction?.invoke()
                            pendingAfterSaveAction = null
                        }
                    }
                ) { Text("Yes") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAdvanceReceiptEmailDialog = false
                        pendingAfterSaveAction?.invoke()
                        pendingAfterSaveAction = null
                    }
                ) { Text("No") }
            }
        )
    }

    if (step == 1) {
        // Step 1: Type Selection Screen
        Scaffold(
            containerColor = AppColors.screenBg,
            topBar = {
                TopAppBar(
                    title = { Text("Select Voucher Type", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppColors.cardBg,
                        titleContentColor = Color(0xFF0F172A),
                        navigationIconContentColor = Color(0xFF0F172A)
                    )
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
                val voucherTypes = voucherTypeCards()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (isTablet) 2 else 1),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(voucherTypes.size) { index ->
                        val type = voucherTypes[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clickable {
                                    if (type.key == "INCOME") {
                                        // intentionally no navigation; visual tap feedback only
                                    } else {
                                        selectedType = type.key
                                        formStep = 1
                                        step = 2
                                    }
                                }
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                                .border(1.dp, type.accent.copy(alpha = 0.22f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(type.accent.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(type.icon, contentDescription = null, tint = type.accent)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(type.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(type.description, fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Step 2: Main Entry layout
        if (selectedType == "EXPENSE") {
            ExpensesScreen(viewModel = viewModel, onNavigateBack = { step = 1 })
            return
        }
        if (selectedType == "BILLS_RECEIVABLE") {
            val billsReceivable by viewModel.billsReceivable.collectAsState()
            Scaffold(
                containerColor = AppColors.screenBg,
                topBar = {
                    TopAppBar(
                        title = { Text("Bills Receivable", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { step = 1 }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
                    AgedBillsListView(
                        bills = billsReceivable,
                        parties = parties,
                        viewModel = viewModel,
                        navigateToNewVoucher = { step = 2 }
                    )
                }
            }
            return
        }
        if (selectedType == "BILLS_PAYABLE") {
            Scaffold(
                containerColor = AppColors.screenBg,
                topBar = {
                    TopAppBar(
                        title = { Text("Bills Payable", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { step = 1 }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
                    PayablesBillsListView(
                        vouchers = vouchers,
                        parties = parties,
                        navigateToNewVoucher = { step = 2 },
                        viewModel = viewModel
                    )
                }
            }
            return
        }
        if (selectedType == "JOURNAL") {
            Scaffold(
                containerColor = AppColors.screenBg,
                topBar = {
                    TopAppBar(
                        title = { Text("Journal Entry", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { step = 1 }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                },
                bottomBar = {
                    StickyBottomBar(
                        netAmount = journalRows.sumOf { it.debitText.toDoubleOrNull() ?: 0.0 },
                        selectedType = "JOURNAL",
                        saveButtonLabel = "Save & Post",
                        onSaveClick = { validateAndSave(false) }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RetailTextField(
                        value = Utils.formatDate(voucherDate),
                        onValueChange = {},
                        label = "Date",
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    RetailTextField(
                        value = narration,
                        onValueChange = { narration = it },
                        label = "Narration",
                        modifier = Modifier.fillMaxWidth()
                    )
                    journalRows.forEachIndexed { index, row ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                            border = BorderStroke(1.dp, AppColors.border)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                var accountExpanded by remember(row.id) { mutableStateOf(false) }
                                OutlinedTextField(
                                    value = row.accountHead,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Account name") },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            modifier = Modifier.clickable { accountExpanded = true }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = accountExpanded,
                                    onDismissRequest = { accountExpanded = false }
                                ) {
                                    ledgerAccounts.forEach { account ->
                                        DropdownMenuItem(
                                            text = { Text(account.name) },
                                            onClick = {
                                                journalRows[index] = row.copy(accountHead = account.name)
                                                accountExpanded = false
                                            }
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    RetailTextField(
                                        value = row.debitText,
                                        onValueChange = { journalRows[index] = row.copy(debitText = filterDecimalInput(it), creditText = if (it.isNotBlank()) "" else row.creditText) },
                                        label = "DR amount",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )
                                    RetailTextField(
                                        value = row.creditText,
                                        onValueChange = { journalRows[index] = row.copy(creditText = filterDecimalInput(it), debitText = if (it.isNotBlank()) "" else row.debitText) },
                                        label = "CR amount",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = { journalRows.add(JournalUiRow()) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add row")
                    }
                    val totalDr = journalRows.sumOf { it.debitText.toDoubleOrNull() ?: 0.0 }
                    val totalCr = journalRows.sumOf { it.creditText.toDoubleOrNull() ?: 0.0 }
                    Text("Total DR: ${Utils.formatIndianCurrency(totalDr)}", fontWeight = FontWeight.SemiBold)
                    Text("Total CR: ${Utils.formatIndianCurrency(totalCr)}", fontWeight = FontWeight.SemiBold)
                }
            }
            return
        }
        val listState = rememberLazyListState()

        Scaffold(
            containerColor = AppColors.screenBg,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                if (selectedType == "SALE") "Sale Voucher"
                                else if (selectedType == "PURCHASE") "Purchase Voucher"
                                else "New $selectedType",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (isThreeStepVoucher && pagerState.currentPage > 0) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                } else step = 1
                            }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = AppColors.cardBg,
                            titleContentColor = Color(0xFF0F172A),
                            navigationIconContentColor = Color(0xFF0F172A)
                        )
                    )
                    if (isThreeStepVoucher) {
                        TabRow(selectedTabIndex = pagerState.currentPage) {
                            voucherTabs.forEachIndexed { index, tab ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    text = { Text(tab.first) },
                                    icon = { Icon(tab.second, contentDescription = null) }
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (!isDesktop) {
                    if (isThreeStepVoucher && formStep < 3) {
                        Surface(
                            tonalElevation = 8.dp,
                            shadowElevation = 8.dp,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (pagerState.currentPage > 0) {
                                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                                        } else step = 1
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp)
                                ) {
                                    Text("Back")
                                }
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(2))
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                                ) {
                                    Text("Next", color = AppColors.textOnPrimary)
                                }
                            }
                        }
                    } else {
                        StickyBottomBar(
                            netAmount = netAmount.value,
                            selectedType = selectedType,
                            saveButtonLabel = if (selectedType == "QUOTATION" || selectedType == "DELIVERY_CHALLAN") {
                                if (isEditMode) "Update Draft" else "Save Draft"
                            } else {
                                if (isEditMode) "Update & Post" else "Save & Post"
                            },
                            onSaveClick = { shouldPrint -> validateAndSave(shouldPrint) }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            @Composable
            fun FormContent(showStickyBar: Boolean, forcedStep: Int? = null) {
                val activeStep = forcedStep ?: formStep
                val showDetailsStep = !isThreeStepVoucher || activeStep == 1
                val showItemsStep = !isThreeStepVoucher || activeStep == 1
                val showPaymentChargesStep = !isThreeStepVoucher || activeStep == 2
                val showReviewStep = !isThreeStepVoucher || activeStep == 3
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = if (showStickyBar) 180.dp else 160.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    item(key = "voucher_form_content") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    if (showDetailsStep) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RetailTextField(
                            value = voucherNo,
                            onValueChange = {},
                            label = "Voucher Number",
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )

                        RetailTextField(
                            value = Utils.formatDate(voucherDate),
                            onValueChange = {},
                            label = "Date",
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (selectedType == "SALE_RETURN" || selectedType == "PURCHASE_RETURN") {
                        val sourceVoucher = vouchers.find { it.id == selectedSourceVoucherId }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    sourceInvoiceSearchQuery = ""
                                    showReturnInvoiceSheet = true
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFFFF),
                            border = BorderStroke(1.dp, Color(0xFFE0E4EA))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Return against which invoice?", fontSize = 11.sp, color = Color(0xFF444444), fontWeight = FontWeight.Medium)
                                if (sourceVoucher == null) {
                                    Text("Tap to search by voucher number or party name", fontSize = 14.sp, color = Color(0xFF777777))
                                } else {
                                    val sourcePartyName = parties.find { it.id == sourceVoucher.partyId }?.name.orEmpty()
                                    Text(sourceVoucher.voucherNo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D0D0D))
                                    Text(
                                        "$sourcePartyName  •  ${Utils.formatDate(sourceVoucher.date)}  •  ${Utils.formatIndianCurrency(sourceVoucher.netAmount)}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF777777)
                                    )
                                }
                            }
                        }

                        if (sourceVoucher != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        "Original Invoice Amount: ${Utils.formatIndianCurrency(sourceVoucher.netAmount)}",
                                        color = Color(0xFF444444),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        "Return Amount: ${Utils.formatIndianCurrency(netAmount.value)}",
                                        color = Color(0xFFC62828),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Invoice-level additional charges are not copied into return vouchers.",
                                        color = Color(0xFF777777),
                                        fontSize = 11.sp
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    restoreReturnSourceInvoice(sourceVoucher)
                                                }
                                            },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Restore Original Items")
                                        }
                                        Text(
                                            "Party locked from source invoice",
                                            color = Color(0xFF777777),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        val reasonOptions = listOf("Damaged", "Wrong Item", "Quality Issue", "Customer Rejected", "Other")
                        var reasonExpanded by remember(selectedType) { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = returnReason,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Reason", color = Color(0xFF444444)) },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.clickable { reasonExpanded = true }
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFFFFFFF),
                                    unfocusedContainerColor = Color(0xFFFFFFFF),
                                    focusedTextColor = Color(0xFF0D0D0D),
                                    unfocusedTextColor = Color(0xFF0D0D0D)
                                )
                            )
                            DropdownMenu(
                                expanded = reasonExpanded,
                                onDismissRequest = { reasonExpanded = false },
                                modifier = Modifier.background(Color(0xFFFFFFFF))
                            ) {
                                reasonOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, color = Color(0xFF0D0D0D)) },
                                        onClick = {
                                            returnReason = option
                                            reasonExpanded = false
                                        },
                                        colors = MenuDefaults.itemColors(textColor = Color(0xFF0D0D0D))
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("select_party_input")
                            .clickable(enabled = !(selectedType == "SALE_RETURN" || selectedType == "PURCHASE_RETURN") || selectedSourceVoucherId == null) {
                                partySearchQuery = ""
                                showPartyPickerSheet = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFFFFF),
                        border = BorderStroke(
                            1.dp,
                            if (selectedParty == null) Color(0xFFE0E4EA) else Color(0xFF1A73E8)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Select Party",
                                    fontSize = 11.sp,
                                    color = Color(0xFF444444),
                                    fontWeight = FontWeight.Medium
                                )
                                if (selectedParty == null) {
                                    Text(
                                        text = "Tap to select or add party",
                                        fontSize = 14.sp,
                                        color = Color(0xFF777777)
                                    )
                                } else {
                                    val selectedBalance = partyBalanceMap[selectedParty?.id].orEmptyBalance()
                                    Text(
                                        text = selectedParty?.name.orEmpty(),
                                        fontSize = 15.sp,
                                        color = Color(0xFF0D0D0D),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = listOf(
                                            selectedParty?.phone?.takeIf { it.isNotBlank() },
                                            "Balance ${Utils.formatIndianCurrency(selectedBalance)}"
                                        ).joinToString("  •  "),
                                        fontSize = 12.sp,
                                        color = Color(0xFF777777)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    if ((selectedType == "SALE_RETURN" || selectedType == "PURCHASE_RETURN") && selectedSourceVoucherId != null) {
                                        showReturnInvoiceSheet = true
                                    } else if (selectedParty == null) {
                                        partySearchQuery = ""
                                        showPartyPickerSheet = true
                                    } else {
                                        selectedParty = null
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (selectedParty == null) Icons.Default.Search else Icons.Default.Close,
                                    contentDescription = if (selectedParty == null) "Search party" else "Clear party",
                                    tint = if (selectedParty == null) Color(0xFF777777) else Color(0xFF1A73E8)
                                )
                            }
                        }
                    }

                    if (selectedParty != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                            border = BorderStroke(1.dp, AppColors.border)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(selectedParty?.name.orEmpty(), fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                                    Text(
                                        "Balance ${Utils.formatIndianCurrency(partyBalanceMap[selectedParty?.id].orEmptyBalance())} | ${selectedParty?.phone.orEmpty()}",
                                        fontSize = 11.sp,
                                        color = AppColors.textSecondary
                                    )
                                    Text(
                                        "Party State: ${selectedParty?.state} | Interstate/IGST: ${if (isInterstate) "YES" else "NO"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isInterstate) Color(0xFFFD7E14) else Color(0xFF1A73E8)
                                    )
                                }
                                TextButton(
                                    onClick = { selectedParty?.id?.let(onNavigateToPartyDetail) }
                                ) {
                                    Text("View")
                                }
                            }
                        }
                    }

                    val isPurchaseType = selectedType == "PURCHASE" || selectedType == "PURCHASE_RETURN"
                    if (isPurchaseType) {
                        val attachContext = LocalContext.current
                        val attachScope = rememberCoroutineScope()
                        val attachLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.OpenDocument()
                        ) { uri ->
                            uri?.let {
                                attachmentUri = it
                                attachmentName = it.lastPathSegment ?: "bill_attachment"
                                attachScope.launch {
                                    val extension = when {
                                        attachContext.contentResolver.getType(it)?.contains("pdf", ignoreCase = true) == true -> ".pdf"
                                        attachContext.contentResolver.getType(it)?.contains("png", ignoreCase = true) == true -> ".png"
                                        attachContext.contentResolver.getType(it)?.contains("webp", ignoreCase = true) == true -> ".webp"
                                        else -> ".jpg"
                                    }
                                    savedAttachmentPath = copyUriToInternalStorage(
                                        attachContext, it,
                                        "purchase_bill_${System.currentTimeMillis()}$extension"
                                    )
                                }
                            }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                            border = BorderStroke(1.dp, AppColors.border),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Attach Supplier Bill", color = AppColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Upload image or PDF to auto-read items", color = AppColors.textTertiary, fontSize = 11.sp)
                                Spacer(Modifier.height(10.dp))
                                if (!savedAttachmentPath.isNullOrBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(AppColors.primaryLight, RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Icon(Icons.Outlined.AttachFile, null, tint = AppColors.primary, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            attachmentName,
                                            color = AppColors.primary,
                                            fontSize = 12.sp,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        IconButton(onClick = {
                                            attachmentUri = null
                                            attachmentName = ""
                                            savedAttachmentPath = null
                                        }) {
                                            Icon(Icons.Default.Close, null, tint = AppColors.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val attachmentFile = savedAttachmentPath?.let(::File)
                                            if (attachmentFile == null || !attachmentFile.exists()) {
                                                billExtractMessage = "Bill attached for reference. Add items manually."
                                            } else {
                                                attachScope.launch {
                                                    isExtractingBill = true
                                                    billExtractMessage = "Reading bill..."
                                                    val parsedItems = runCatching {
                                                        parseBillItemsFromText(extractBillText(attachmentFile))
                                                    }.getOrElse { emptyList() }
                                                    isExtractingBill = false
                                                    if (parsedItems.isEmpty()) {
                                                        billExtractMessage = "Could not read items automatically. Please add items manually."
                                                    } else {
                                                        parsedBillItems.clear()
                                                        parsedBillItems.addAll(parsedItems)
                                                        billExtractMessage = ""
                                                        showParsedBillDialog = true
                                                    }
                                                }
                                            }
                                        },
                                        enabled = !isExtractingBill,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                                    ) {
                                        if (isExtractingBill) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Reading bill...", color = Color.White)
                                        } else {
                                            Text("Extract Items from Bill", color = Color.White)
                                        }
                                    }
                                    if (billExtractMessage.isNotBlank()) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            billExtractMessage,
                                            color = if (billExtractMessage.startsWith("Could not") || billExtractMessage.startsWith("Bill attached")) Color.Gray else AppColors.credit,
                                            fontSize = 11.sp
                                        )
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { attachLauncher.launch(arrayOf("image/*", "application/pdf")) },
                                        modifier = Modifier.fillMaxWidth(),
                                        border = BorderStroke(1.dp, AppColors.primary)
                                    ) {
                                        Icon(Icons.Outlined.Upload, null, tint = AppColors.primary, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Upload Supplier Bill", color = AppColors.primary)
                                    }
                                }
                            }
                        }
                    }
                    }

                    if (showItemsStep) {
                    RetailTextField(
                        value = narration,
                        onValueChange = { narration = it },
                        label = "Voucher Narration / Memo Card Details"
                    )

                    val isSaleType = selectedType == "SALE" || selectedType == "SALE_RETURN"
                    if (hasGst && isSaleType) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (globalGstEnabled) AppColors.primaryLight else AppColors.cardBg
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (globalGstEnabled) AppColors.primary else AppColors.border
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Apply Same GST to All Items",
                                            color = AppColors.textPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            if (globalGstEnabled) "All items: ${globalGstRate.toInt()}% GST"
                                            else "Each item uses its own rate",
                                            color = AppColors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Switch(
                                        checked = globalGstEnabled,
                                        onCheckedChange = {
                                            globalGstEnabled = it
                                            if (it) applyGlobalGstToAll()
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = AppColors.textOnPrimary,
                                            checkedTrackColor = AppColors.primary,
                                            uncheckedThumbColor = AppColors.textTertiary,
                                            uncheckedTrackColor = AppColors.border
                                        )
                                    )
                                }
                                if (globalGstEnabled) {
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        "GST Rate for All Items",
                                        color = AppColors.labelText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(0.0, 5.0, 12.0, 18.0, 28.0).forEach { rate ->
                                            FilterChip(
                                                selected = globalGstRate == rate,
                                                onClick = {
                                                    globalGstRate = rate
                                                    applyGlobalGstToAll()
                                                },
                                                label = {
                                                    Text(
                                                        "${rate.toInt()}%",
                                                        fontWeight = if (globalGstRate == rate) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = AppColors.primary,
                                                    selectedLabelColor = AppColors.textOnPrimary,
                                                    containerColor = AppColors.cardBg,
                                                    labelColor = AppColors.textPrimary
                                                )
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "This rate stays on until you toggle it off",
                                        color = AppColors.textTertiary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Line Items Panel for Invoice Types
                    val isInvoiceType = selectedType == "SALE" || selectedType == "PURCHASE" ||
                            selectedType == "SALE_RETURN" || selectedType == "PURCHASE_RETURN"

                    if (isInvoiceType) {
                        val isReturnType = selectedType == "SALE_RETURN" || selectedType == "PURCHASE_RETURN"
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Items Included", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = com.zerobook.app.ui.theme.TextDark)
                                TextButton(onClick = { showStockReportSheet = true }) {
                                    Text("Stock")
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (!isReturnType) {
                                    Button(
                                        onClick = {
                                            editingItemIndex = null
                                            showItemEntrySheet = true
                                        },
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.testTag("add_item_button")
                                    ) {
                                        Text("Add Item", fontSize = 11.sp, color = Color(0xFFFFFFFF))
                                    }
                                }
                            }
                        }

                        if (lineItems.isEmpty()) {
                            Text(
                                "No items yet. Tap Add Item to add products.",
                                color = AppColors.textTertiary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        lineItems.forEachIndexed { index, item ->
                            key(item.id) {
                            val originalItem = originalReturnItems[item.id]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(enabled = !isReturnType) {
                                        editingItemIndex = index
                                        showItemEntrySheet = true
                                    },
                                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                                border = BorderStroke(1.dp, AppColors.border),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            item.productName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = com.zerobook.app.ui.theme.TextDark
                                        )
                                        if (item.hsnCode.isNotBlank()) {
                                            Text(
                                                "HSN ${item.hsnCode}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF777777)
                                            )
                                        }
                                        Text(
                                            "${item.qty} ${item.unit} × ${Utils.formatIndianCurrency(item.rate)}",
                                            fontSize = 11.sp,
                                            color = AppColors.textSecondary
                                        )
                                        if ((selectedType == "SALE_RETURN" || selectedType == "PURCHASE_RETURN") && originalItem != null) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text("Original Qty: ${originalItem.qty}", fontSize = 10.sp, color = Color(0xFF777777))
                                                OutlinedTextField(
                                                    value = if (item.qty == 0.0) "" else item.qty.toString(),
                                                    onValueChange = { qtyText ->
                                                        val parsedQty = filterDecimalInput(qtyText).toDoubleOrNull() ?: 0.0
                                                        val newQty = parsedQty.coerceIn(0.0, originalItem.qty)
                                                        val ratio = if (originalItem.qty <= 0.0) 0.0 else newQty / originalItem.qty
                                                        lineItems[index] = item.copy(
                                                            qty = newQty,
                                                            taxableAmount = originalItem.taxableAmount * ratio,
                                                            cgstAmount = originalItem.cgstAmount * ratio,
                                                            sgstAmount = originalItem.sgstAmount * ratio,
                                                            igstAmount = originalItem.igstAmount * ratio,
                                                            totalAmount = originalItem.totalAmount * ratio
                                                        )
                                                    },
                                                    label = { Text("Return Qty", fontSize = 10.sp) },
                                                    modifier = Modifier.width(120.dp),
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedContainerColor = Color(0xFFFFFFFF),
                                                        unfocusedContainerColor = Color(0xFFFFFFFF),
                                                        focusedTextColor = Color(0xFF0D0D0D),
                                                        unfocusedTextColor = Color(0xFF0D0D0D)
                                                    )
                                                )
                                            }
                                            Text(
                                                "Return quantity cannot exceed the original invoice quantity.",
                                                fontSize = 10.sp,
                                                color = AppColors.textTertiary
                                            )
                                        }
                                        if (hasGst && item.gstRate > 0) {
                                            Text(
                                                "GST ${item.gstRate.toInt()}%",
                                                fontSize = 10.sp,
                                                color = AppColors.textTertiary
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            Utils.formatIndianCurrency(item.totalAmount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = com.zerobook.app.ui.theme.TextDark
                                        )
                                        if (!isReturnType) {
                                            IconButton(
                                                onClick = { lineItems.removeAt(index) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Remove",
                                                    tint = AppColors.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            }
                        }

                        if (isThreeStepVoucher && activeStep == 1) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                                border = BorderStroke(1.dp, AppColors.border)
                            ) {
                                Text(
                                    text = "Items ${lineItems.size} | Taxable ${Utils.formatIndianCurrency(taxableAmount.value)} | GST ${Utils.formatIndianCurrency(cgst.value + sgst.value + igst.value)} | Total ${Utils.formatIndianCurrency(netAmount.value)}",
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.textPrimary
                                )
                            }
                        }
                    } else {
                        // Non-invoice: direct Receipt/Payment amount dialog
                        if (selectedType == "RECEIPT") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                                border = BorderStroke(1.dp, AppColors.border)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("This is an advance receipt", fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                                        Text(
                                            if (isAdvanceReceipt) "No pending invoices will be allocated." else "Pending invoices will auto-populate from this party.",
                                            fontSize = 11.sp,
                                            color = AppColors.textSecondary
                                        )
                                    }
                                    Switch(checked = isAdvanceReceipt, onCheckedChange = { isAdvanceReceipt = it })
                                }
                            }
                        }
                        if ((selectedType == "RECEIPT" || selectedType == "PAYMENT") && selectedParty != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(selectedParty?.name.orEmpty(), fontWeight = FontWeight.Bold, color = Color(0xFF0D0D0D))
                                    Text(
                                        "Total Outstanding: ${Utils.formatIndianCurrency(checkedInvoicesTotal)}",
                                        color = Color(0xFFC62828),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("(${pendingInvoices.size} pending invoices)", color = Color(0xFF777777), fontSize = 12.sp)
                                }
                            }

                            pendingInvoices.forEach { invoice ->
                                key(invoice.id) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Checkbox(
                                            checked = pendingInvoiceChecks[invoice.id] != false,
                                            onCheckedChange = { checked ->
                                                pendingInvoiceChecks[invoice.id] = checked
                                                setDirectAmount(
                                                    pendingInvoices.filter { pendingInvoiceChecks[it.id] != false }
                                                        .sumOf { it.outstandingAmount }
                                                )
                                            }
                                        )
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(invoice.voucherNo, fontWeight = FontWeight.Bold, color = Color(0xFF0D0D0D))
                                            Text("Invoice date: ${Utils.formatDate(invoice.date)}", fontSize = 12.sp, color = Color(0xFF777777))
                                            val invoiceOutstanding = invoice.outstandingAmount.coerceAtLeast(0.0)
                                            Text("Original: ${Utils.formatIndianCurrency(invoice.netAmount)}", fontSize = 12.sp, color = Color(0xFF444444))
                                            Text("Paid so far: ${Utils.formatIndianCurrency((invoice.netAmount - invoiceOutstanding).coerceAtLeast(0.0))}", fontSize = 12.sp, color = Color(0xFF444444))
                                            Text("Remaining: ${Utils.formatIndianCurrency(invoiceOutstanding)}", fontSize = 12.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                }
                            }
                        }
                        if (selectedType == "RECEIPT" && isAdvanceReceipt) {
                            RetailTextField(
                                value = advanceForText,
                                onValueChange = { advanceForText = it },
                                label = "For",
                                placeholder = "What is this advance for?"
                            )
                        }
                        RetailTextField(
                            value = netAmount.value.toString(),
                            onValueChange = {
                                val valueDouble = filterDecimalInput(it).toDoubleOrNull() ?: 0.0
                                setDirectAmount(valueDouble)
                            },
                            label = if (selectedType == "RECEIPT" && isAdvanceReceipt) "Advance Amount (₹) *" else "Amount (₹) *",
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("direct_amount_input")
                                .onFocusChanged { focusState ->
                                    if (!focusState.isFocused && netAmount.value == 0.0) {
                                        setDirectAmount(0.0)
                                    }
                                },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        if ((selectedType == "RECEIPT" && !isAdvanceReceipt) || selectedType == "PAYMENT") {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(25, 50, 75, 100).forEach { percent ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { setDirectAmount(checkedInvoicesTotal * percent / 100.0) },
                                        label = { Text(if (percent == 100) "Full" else "$percent%") }
                                    )
                                }
                            }
                            val amountValue = netAmount.value
                            val statusText = when {
                                amountValue == checkedInvoicesTotal -> "Full Payment ✓"
                                amountValue < checkedInvoicesTotal -> "Partial Payment - ${Utils.formatIndianCurrency(checkedInvoicesTotal - amountValue)} still due"
                                else -> "Advance ${Utils.formatIndianCurrency(amountValue - checkedInvoicesTotal)} will be credited to account"
                            }
                            val statusColor = when {
                                amountValue == checkedInvoicesTotal -> Color(0xFF2E7D32)
                                amountValue < checkedInvoicesTotal -> Color(0xFFEF6C00)
                                else -> Color(0xFF1A73E8)
                            }
                            Text(statusText, color = statusColor, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                    }
                }

                if (selectedType in setOf("SALE", "PURCHASE", "RECEIPT", "PAYMENT")) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("References", fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                            RetailTextField(
                                value = saleReferenceNo,
                                onValueChange = { saleReferenceNo = it },
                                label = "Reference No. & Date",
                                placeholder = "e.g. PO number or order reference"
                            )
                            RetailTextField(
                                value = saleOtherReferences,
                                onValueChange = { saleOtherReferences = it },
                                label = "Other References",
                                placeholder = "Any other reference"
                            )
                        }
                    }
                }

                if (showPaymentChargesStep) {
                // Payment Mode Selector (Moved to Summary Area)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("Mode:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.textSecondary)
                    val modes = if (selectedType == "SALE") {
                        listOf("CASH", "BANK", "UPI", "CHEQUE", "PART PAYMENT", "CREDIT")
                    } else {
                        listOf("CASH", "BANK", "UPI", "CHEQUE")
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        modes.forEach { mode ->
                            FilterChip(
                                selected = paymentMode == mode,
                                onClick = { paymentMode = mode },
                                label = { Text(mode, fontSize = 10.sp) },
                                shape = RoundedCornerShape(4.dp)
                            )
                        }
                    }
                }

                // Payment Mode Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "$paymentMode DETAILS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AppColors.primary,
                            letterSpacing = 0.5.sp
                        )
                        
                        if (paymentMode == "CASH") {
                            Text(
                                text = "Payment will be registered instantly in the local Cash Ledger. Normal cash-in-hand flows apply.",
                                fontSize = 11.sp,
                                color = AppColors.textSecondary
                            )
                        }
                        
                        if (paymentMode == "BANK") {
                            RetailTextField(
                                value = bankNameDetail,
                                onValueChange = { bankNameDetail = it },
                                label = "Bank Name *"
                            )
                            RetailTextField(
                                value = bankAccountHolder,
                                onValueChange = { bankAccountHolder = it },
                                label = "Account Holder Name *"
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RetailTextField(
                                    value = bankIfsc,
                                    onValueChange = { bankIfsc = it },
                                    label = "IFSC Code *",
                                    modifier = Modifier.weight(1f)
                                )
                                RetailTextField(
                                    value = branchName,
                                    onValueChange = { branchName = it },
                                    label = "Branch Name",
                                    modifier = Modifier.weight(1.2f)
                                )
                            }
                        }
                        
                        if (paymentMode == "CHEQUE") {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RetailTextField(
                                    value = chequeNo,
                                    onValueChange = { chequeNo = it },
                                    label = "Cheque No *",
                                    modifier = Modifier.weight(1f)
                                )
                                RetailTextField(
                                    value = bankName,
                                    onValueChange = { bankName = it },
                                    label = "Bank Name *",
                                    modifier = Modifier.weight(1.2f)
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RetailTextField(
                                    value = branchName,
                                    onValueChange = { branchName = it },
                                    label = "Branch Name",
                                    modifier = Modifier.weight(1f)
                                )
                                RetailTextField(
                                    value = memoNumber,
                                    onValueChange = { memoNumber = it },
                                    label = "Memo/Voucher No",
                                    modifier = Modifier.weight(1.2f)
                                )
                            }
                        }
                        
                        if (paymentMode == "UPI") {
                            Text(
                                text = "UPI Mode active. For sales, saving the transaction will show an on-screen UPI QR Code scanner linked to your business account to automatically pull, verify, and auto-print customer invoices.",
                                fontSize = 11.sp,
                                color = AppColors.textSecondary
                            )
                            RetailTextField(
                                value = bankNameDetail,
                                onValueChange = { bankNameDetail = it },
                                label = "Paying UPI / Reference App ID (Optional)"
                            )
                        }

                        if (paymentMode == "PART PAYMENT") {
                            val paidNow = partialAmountPaidText.toDoubleOrNull() ?: 0.0
                            val remainingDue = (netAmount.value - paidNow).coerceAtLeast(0.0)
                            Text("Part Payment Details", fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                            RetailTextField(
                                value = partialAmountPaidText,
                                onValueChange = { partialAmountPaidText = filterDecimalInput(it) },
                                label = "Amount Paid Now",
                                modifier = Modifier.onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        if (partialAmountPaidText == "0" || partialAmountPaidText == "0.0" || partialAmountPaidText == "0.00") {
                                            partialAmountPaidText = ""
                                        }
                                    } else if (partialAmountPaidText.isBlank()) {
                                        partialAmountPaidText = "0"
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("CASH", "BANK", "UPI", "CHEQUE").forEach { mode ->
                                    FilterChip(
                                        selected = partialPaymentSubmode == mode,
                                        onClick = { partialPaymentSubmode = mode },
                                        label = { Text(mode, fontSize = 10.sp) }
                                    )
                                }
                            }
                            Text(
                                text = "Balance Due: ${Utils.formatIndianCurrency(remainingDue)}",
                                color = AppColors.debit,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showDatePicker(creditDueDateText) { selectedMillis ->
                                            creditDueDateText = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(Date(selectedMillis))
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = AppColors.inputBg,
                                border = BorderStroke(1.dp, AppColors.border)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                                    Text("Credit due date for remaining amount", fontSize = 11.sp, color = AppColors.textSecondary)
                                    Text(
                                        text = creditDueDateText.ifBlank { "Select optional due date" },
                                        color = AppColors.textPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        if (paymentMode == "CREDIT") {
                            Text("Credit Sale", fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                            Text("Full amount on credit", color = AppColors.textSecondary, fontSize = 11.sp)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showDatePicker(creditDueDateText) { selectedMillis ->
                                            creditDueDateText = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(Date(selectedMillis))
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = AppColors.inputBg,
                                border = BorderStroke(1.dp, AppColors.border)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                                    Text("Due Date", fontSize = 11.sp, color = AppColors.textSecondary)
                                    Text(
                                        text = creditDueDateText.ifBlank { "Select optional due date" },
                                        color = AppColors.textPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Text(
                                text = "${Utils.formatIndianCurrency(netAmount.value)} due",
                                color = Color(0xFFEF6C00),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (selectedType == "SALE") {
                    val chargeTypes = listOf(
                        "Transport",
                        "Freight",
                        "Carriage Inward",
                        "Carriage Outward",
                        "Loading / Unloading",
                        "Labour Charges",
                        "Packing Charges",
                        "Forwarding Charges",
                        "Insurance",
                        "Other"
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Additional Charges", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0D0D0D))
                                IconButton(
                                    onClick = {
                                        additionalCharges.add(
                                            AdditionalCharge(
                                                label = "Transport",
                                                amount = 0.0,
                                                isTaxable = false,
                                                gstRate = 0.0,
                                                gstAmount = 0.0
                                            )
                                        )
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add charge", tint = Color(0xFF1A73E8))
                                }
                            }

                            additionalCharges.forEachIndexed { index, charge ->
                                key("charge-row-$index") {
                                val isOtherCharge = charge.label.isBlank() || chargeTypes.none { it == charge.label }
                                val isTransportCharge = isTransportChargeType(charge.label)
                                var chargeTypeExpanded by remember(index, charge.label) { mutableStateOf(false) }
                                var transportExpanded by remember(index, charge.label) {
                                    mutableStateOf(isTransportCharge && hasTransportDetails)
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateContentSize(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (!isTablet) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedTextField(
                                                    value = if (isOtherCharge) "Other" else charge.label,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Charge type", color = Color(0xFF444444)) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    trailingIcon = {
                                                        Icon(
                                                            Icons.Default.KeyboardArrowDown,
                                                            contentDescription = null,
                                                            modifier = Modifier.clickable { chargeTypeExpanded = true }
                                                        )
                                                    },
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedContainerColor = Color(0xFFFFFFFF),
                                                        unfocusedContainerColor = Color(0xFFFFFFFF),
                                                        focusedTextColor = Color(0xFF0D0D0D),
                                                        unfocusedTextColor = Color(0xFF0D0D0D)
                                                    )
                                                )
                                                DropdownMenu(
                                                    expanded = chargeTypeExpanded,
                                                    onDismissRequest = { chargeTypeExpanded = false },
                                                    modifier = Modifier.background(Color(0xFFFFFFFF))
                                                ) {
                                                    chargeTypes.forEach { type ->
                                                        DropdownMenuItem(
                                                            text = { Text(type, color = Color(0xFF0D0D0D)) },
                                                            onClick = {
                                                                additionalCharges[index] = charge.copy(label = if (type == "Other") "" else type)
                                                                transportExpanded = isTransportChargeType(type)
                                                                chargeTypeExpanded = false
                                                            },
                                                            colors = MenuDefaults.itemColors(textColor = Color(0xFF0D0D0D))
                                                        )
                                                    }
                                                }
                                            }

                                            if (isOtherCharge) {
                                                OutlinedTextField(
                                                    value = charge.label,
                                                    onValueChange = { additionalCharges[index] = charge.copy(label = it) },
                                                    label = { Text("Label", color = Color(0xFF444444)) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedContainerColor = Color(0xFFFFFFFF),
                                                        unfocusedContainerColor = Color(0xFFFFFFFF),
                                                        focusedTextColor = Color(0xFF0D0D0D),
                                                        unfocusedTextColor = Color(0xFF0D0D0D)
                                                    )
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedTextField(
                                                    value = if (charge.amount == 0.0) "" else charge.amount.toString(),
                                                    onValueChange = {
                                                        additionalCharges[index] = charge.copy(amount = filterDecimalInput(it).toDoubleOrNull() ?: 0.0)
                                                    },
                                                    label = { Text("Amount", color = Color(0xFF444444)) },
                                                    modifier = Modifier.weight(1f),
                                                    prefix = { Text("Rs", color = Color(0xFF0D0D0D)) },
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Decimal,
                                                        imeAction = ImeAction.Next
                                                    ),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedContainerColor = Color(0xFFFFFFFF),
                                                        unfocusedContainerColor = Color(0xFFFFFFFF),
                                                        focusedTextColor = Color(0xFF0D0D0D),
                                                        unfocusedTextColor = Color(0xFF0D0D0D)
                                                    )
                                                )

                                                IconButton(onClick = { additionalCharges.removeAt(index) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete charge", tint = Color(0xFFC62828))
                                                }
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(modifier = Modifier.weight(1.2f)) {
                                                OutlinedTextField(
                                                    value = if (isOtherCharge) "Other" else charge.label,
                                                    onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Charge type", color = Color(0xFF444444)) },
                                                modifier = Modifier.fillMaxWidth(),
                                                trailingIcon = {
                                                    Icon(
                                                        Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        modifier = Modifier.clickable { chargeTypeExpanded = true }
                                                    )
                                                },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = Color(0xFFFFFFFF),
                                                    unfocusedContainerColor = Color(0xFFFFFFFF),
                                                    focusedTextColor = Color(0xFF0D0D0D),
                                                    unfocusedTextColor = Color(0xFF0D0D0D)
                                                )
                                            )
                                            DropdownMenu(
                                                expanded = chargeTypeExpanded,
                                                onDismissRequest = { chargeTypeExpanded = false },
                                                modifier = Modifier.background(Color(0xFFFFFFFF))
                                            ) {
                                                chargeTypes.forEach { type ->
                                                    DropdownMenuItem(
                                                        text = { Text(type, color = Color(0xFF0D0D0D)) },
                                                        onClick = {
                                                            additionalCharges[index] = charge.copy(label = if (type == "Other") "" else type)
                                                            transportExpanded = isTransportChargeType(type)
                                                            chargeTypeExpanded = false
                                                        },
                                                        colors = MenuDefaults.itemColors(textColor = Color(0xFF0D0D0D))
                                                    )
                                                }
                                            }
                                        }

                                            if (isOtherCharge) {
                                                OutlinedTextField(
                                                    value = charge.label,
                                                    onValueChange = { additionalCharges[index] = charge.copy(label = it) },
                                                    label = { Text("Label", color = Color(0xFF444444)) },
                                                    modifier = Modifier.weight(1f),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedContainerColor = Color(0xFFFFFFFF),
                                                        unfocusedContainerColor = Color(0xFFFFFFFF),
                                                        focusedTextColor = Color(0xFF0D0D0D),
                                                        unfocusedTextColor = Color(0xFF0D0D0D)
                                                    )
                                                )
                                            }

                                            OutlinedTextField(
                                                value = if (charge.amount == 0.0) "" else charge.amount.toString(),
                                                onValueChange = {
                                                    additionalCharges[index] = charge.copy(amount = filterDecimalInput(it).toDoubleOrNull() ?: 0.0)
                                                },
                                                label = { Text("Amount", color = Color(0xFF444444)) },
                                                modifier = Modifier.weight(0.9f),
                                                prefix = { Text("Rs", color = Color(0xFF0D0D0D)) },
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Decimal,
                                                    imeAction = ImeAction.Next
                                                ),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = Color(0xFFFFFFFF),
                                                    unfocusedContainerColor = Color(0xFFFFFFFF),
                                                    focusedTextColor = Color(0xFF0D0D0D),
                                                    unfocusedTextColor = Color(0xFF0D0D0D)
                                                )
                                            )

                                            IconButton(onClick = { additionalCharges.removeAt(index) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete charge", tint = Color(0xFFC62828))
                                            }
                                        }
                                    }

                                    if (isTransportCharge) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Transport Details",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF0D0D0D)
                                            )
                                            TextButton(onClick = { transportExpanded = !transportExpanded }) {
                                                Text(if (transportExpanded) "Hide" else "Add Details", color = Color(0xFF666666))
                                            }
                                        }

                                        if (transportExpanded) {
                                            TransportDetailsSection(
                                                isTablet = isTablet,
                                                amount = charge.amount,
                                                transportName = transportName,
                                                onTransportNameChange = { transportName = it },
                                                transportVehicle = transportVehicle,
                                                onTransportVehicleChange = { transportVehicle = it.uppercase() },
                                                transportLrNo = transportLrNo,
                                                onTransportLrNoChange = { transportLrNo = it },
                                                transportGstin = transportGstin,
                                                onTransportGstinChange = { transportGstin = it.uppercase() },
                                                transportDestination = transportDestination,
                                                onTransportDestinationChange = { transportDestination = it }
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
                    }

                if (showParsedBillDialog) {
                    ParsedBillItemsDialog(
                        items = parsedBillItems,
                        onDismiss = { showParsedBillDialog = false },
                        onItemsChange = {
                            parsedBillItems.clear()
                            parsedBillItems.addAll(it)
                        },
                        onConfirm = {
                            parsedBillItems.filter { it.included }.forEach { parsedItem ->
                                val qty = parsedItem.qty.toDoubleOrNull() ?: 1.0
                                val rate = parsedItem.rate.toDoubleOrNull() ?: 0.0
                                val taxable = qty * rate
                                lineItems.add(
                                    VoucherItem(
                                        id = UUID.randomUUID().toString(),
                                        voucherId = voucherId ?: "",
                                        productId = "CUSTOM",
                                        productName = parsedItem.name,
                                        hsnCode = parsedItem.hsn.ifBlank { "9900" },
                                        qty = qty,
                                        unit = parsedItem.unit.ifBlank { "PCS" },
                                        rate = rate,
                                        discount = 0.0,
                                        discountType = "PERCENT",
                                        taxableAmount = taxable,
                                        gstRate = 0.0,
                                        cgstAmount = 0.0,
                                        sgstAmount = 0.0,
                                        igstAmount = 0.0,
                                        totalAmount = taxable
                                    )
                                )
                            }
                            showParsedBillDialog = false
                        }
                    )
                }

                if (showReviewStep) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                        border = BorderStroke(1.dp, AppColors.border),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Review Before Saving", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.textPrimary)
                            Text(
                                when (selectedType) {
                                    "SALE" -> "Check party, payment mode, items, and final total before posting the sale."
                                    "PURCHASE" -> "Check supplier, bill attachment, items, and final total before posting the purchase."
                                    else -> "Review the voucher details before saving."
                                },
                                fontSize = 12.sp,
                                color = AppColors.textSecondary
                            )
                            Text("Party: ${selectedParty?.name ?: "Cash / Walk-in Customer"}", fontSize = 12.sp, color = AppColors.textPrimary)
                            Text("Mode: $paymentMode", fontSize = 12.sp, color = AppColors.textPrimary)
                            Text("Items: ${lineItems.size}", fontSize = 12.sp, color = AppColors.textPrimary)
                        }
                    }

                    if (!isDesktop) {
                        LiveInvoicePreview(
                            profile = profile,
                            party = selectedParty,
                            voucherNo = voucherNo,
                            voucherDate = voucherDate,
                            paymentMode = paymentMode,
                            creditDueDate = creditDueDateText,
                            partialAmountPaid = partialAmountPaidText.toDoubleOrNull() ?: 0.0,
                            partialPaymentSubmode = partialPaymentSubmode,
                            remainingCreditAmount = when {
                                selectedType == "SALE" && paymentMode == "PART PAYMENT" -> (netAmount.value - (partialAmountPaidText.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)
                                selectedType == "SALE" && paymentMode == "CREDIT" -> netAmount.value
                                else -> 0.0
                            },
                            lineItems = lineItems,
                            additionalCharges = additionalCharges.toList(),
                            taxableAmount = taxableAmount.value,
                            cgst = cgst.value,
                            sgst = sgst.value,
                            igst = igst.value,
                            roundOff = roundOff.value,
                            netAmount = netAmount.value,
                            selectedType = selectedType
                        )
                    }

                    // GST Live Summary Panel
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(0.dp)),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Taxable Amount:", fontSize = 11.sp, color = AppColors.textSecondary)
                                Text(Utils.formatIndianCurrency(taxableAmount.value), fontSize = 11.sp, color = AppColors.textSecondary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Voucher:", fontSize = 11.sp, color = AppColors.textSecondary)
                                Text(voucherNo, fontSize = 11.sp, color = AppColors.textSecondary)
                            }
                            additionalCharges.forEach { charge ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(charge.label.ifBlank { "Other" }, fontSize = 11.sp, color = AppColors.textSecondary)
                                    Text(Utils.formatIndianCurrency(charge.amount), fontSize = 11.sp, color = AppColors.textSecondary)
                                }
                            }
                            if (isInterstate) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("IGST:", fontSize = 11.sp, color = AppColors.textSecondary)
                                    Text(Utils.formatIndianCurrency(igst.value), fontSize = 11.sp, color = AppColors.textSecondary)
                                }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("CGST:", fontSize = 11.sp, color = AppColors.textSecondary)
                                    Text(Utils.formatIndianCurrency(cgst.value), fontSize = 11.sp, color = AppColors.textSecondary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("SGST:", fontSize = 11.sp, color = AppColors.textSecondary)
                                    Text(Utils.formatIndianCurrency(sgst.value), fontSize = 11.sp, color = AppColors.textSecondary)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Round Off:", fontSize = 11.sp, color = AppColors.textSecondary)
                                Text(Utils.formatIndianCurrency(roundOff.value), fontSize = 11.sp, color = AppColors.textSecondary)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Net Total Owed:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(Utils.formatIndianCurrency(netAmount.value), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                    }
                    }
            }

            if (showStickyBar) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    StickyBottomBar(
                        netAmount = netAmount.value,
                        selectedType = selectedType,
                        saveButtonLabel = if (selectedType == "QUOTATION" || selectedType == "DELIVERY_CHALLAN") {
                            if (isEditMode) "Update Draft" else "Save Draft"
                        } else {
                            if (isEditMode) "Update & Post" else "Save & Post"
                        },
                        onSaveClick = { shouldPrint -> validateAndSave(shouldPrint) }
                    )
                }
            }
        }

        if (isDesktop) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.screenBg)
                    .padding(innerPadding)
            ) {
                Box(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                    FormContent(showStickyBar = true)
                }
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(AppColors.border))
                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight()
                        .background(AppColors.screenBg)
                ) {
                    if (!isThreeStepVoucher || formStep == 3) {
                        LiveInvoicePreview(
                            profile = profile,
                            party = selectedParty,
                            voucherNo = voucherNo,
                            voucherDate = voucherDate,
                            paymentMode = paymentMode,
                            creditDueDate = creditDueDateText,
                            partialAmountPaid = partialAmountPaidText.toDoubleOrNull() ?: 0.0,
                            partialPaymentSubmode = partialPaymentSubmode,
                            remainingCreditAmount = when {
                                selectedType == "SALE" && paymentMode == "PART PAYMENT" -> (netAmount.value - (partialAmountPaidText.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)
                                selectedType == "SALE" && paymentMode == "CREDIT" -> netAmount.value
                                else -> 0.0
                            },
                            lineItems = lineItems,
                            additionalCharges = additionalCharges.toList(),
                            taxableAmount = taxableAmount.value,
                            cgst = cgst.value,
                            sgst = sgst.value,
                            igst = igst.value,
                            roundOff = roundOff.value,
                            netAmount = netAmount.value,
                            selectedType = selectedType
                        )
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                            border = BorderStroke(1.dp, AppColors.border),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Preview unlocks on Step 3", fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                                Text(
                                    "Finish the details and items steps to review the live voucher preview before saving.",
                                    color = AppColors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.screenBg)
                    .padding(innerPadding)
                    .imePadding()
            ) {
                if (isThreeStepVoucher) {
                    HorizontalPager(state = pagerState) { page ->
                        FormContent(showStickyBar = false, forcedStep = page + 1)
                    }
                } else {
                    FormContent(showStickyBar = false)
                }
            }
        }
    }
}

    // Interactive UPI Verification & Scanning Dialog
    if (showUpiPaymentDialog) {
        var simulationProgress by remember { mutableStateOf(0f) }
        var simulationStatus by remember { mutableStateOf("Generating secure merchant settlement QR...") }
        var showSimulatedCheckMark by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            // Step 1: Generating QR
            delay(1000)
            simulationProgress = 0.3f
            simulationStatus = "QR Generated. Listening for bank transaction broadcast..."
            
            // Step 2: Simulating scanned status
            delay(1500)
            simulationProgress = 0.7f
            simulationStatus = "Customer scanned! Awaiting user banking PIN authentication..."
            
            // Step 3: Simulating settlement
            delay(2000)
            simulationProgress = 1.0f
            simulationStatus = "PIN Verified! Settling Rupees ${Utils.formatIndianCurrency(netAmount.value)} to account..."
            
            // Step 4: Complete!
            delay(1000)
            showSimulatedCheckMark = true
            simulationStatus = "PAYMENT CONFIRMED! Rs. ${Utils.formatIndianCurrency(netAmount.value)} Credited."
            
            // Step 5: Save and Auto-trigger print
            delay(1200)
            showUpiPaymentDialog = false
            saveTheVoucher(true)
        }

        AlertDialog(
            onDismissRequest = { /* Prevent dismiss during active settlement transaction */ },
            containerColor = AppColors.cardBg,
            titleContentColor = AppColors.textPrimary,
            textContentColor = AppColors.textSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = AppColors.success,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ZeroBook UPI Cash Terminal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Scan with GPay, PhonePe, Paytm, BHIM, or any Banking App",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    // A beautiful custom visual QR Code mock inside a neat target box
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(2.dp, AppColors.primary, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (showSimulatedCheckMark) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
                                    tint = AppColors.success,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("TRANSACTION DONE", color = AppColors.success, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Draws a highly realistic abstract QR Code graphic using Canvas
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val grid = 12
                                val unitW = w / grid
                                val unitH = h / grid

                                // Draw position detection patterns (outer corners)
                                val strokeWidth = 14f
                                // Top-Left
                                drawRect(Color(0xFF0F172A), Offset(0f, 0f), Size(unitW * 3, unitH * 3))
                                drawRect(Color.White, Offset(unitW * 0.5f, unitH * 0.5f), Size(unitW * 2, unitH * 2))
                                drawRect(Color(0xFF0F172A), Offset(unitW * 1f, unitH * 1f), Size(unitW, unitH))

                                // Top-Right
                                drawRect(Color(0xFF0F172A), Offset(w - unitW * 3, 0f), Size(unitW * 3, unitH * 3))
                                drawRect(Color.White, Offset(w - unitW * 2.5f, unitH * 0.5f), Size(unitW * 2, unitH * 2))
                                drawRect(Color(0xFF0F172A), Offset(w - unitW * 2f, unitH * 1f), Size(unitW, unitH))

                                // Bottom-Left
                                drawRect(Color(0xFF0F172A), Offset(0f, h - unitH * 3), Size(unitW * 3, unitH * 3))
                                drawRect(Color.White, Offset(unitW * 0.5f, h - unitH * 2.5f), Size(unitW * 2, unitH * 2))
                                drawRect(Color(0xFF0F172A), Offset(unitW * 1f, h - unitH * 2f), Size(unitW, unitH))

                                // Dynamic center accent logo from dynamic colors
                                drawCircle(Color(0xFF1A73E8), radius = unitW * 1.5f, center = Offset(w/2, h/2))

                                // Draw abstract QR dots / blocks randomly
                                val random = java.util.Random(123456)
                                for (col in 0 until grid) {
                                    for (row in 0 until grid) {
                                        // Skip position pattern zones
                                        if ((col < 3 && row < 3) || (col > 8 && row < 3) || (col < 3 && row > 8)) continue
                                        // Skip center logo zone
                                        if (col in 4..7 && row in 4..7) continue

                                        if (random.nextBoolean()) {
                                            drawRect(
                                                color = Color(0xFF0F172A),
                                                topLeft = Offset(col * unitW + unitW * 0.1f, row * unitH + unitH * 0.1f),
                                                size = Size(unitW * 0.8f, unitH * 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "AMOUNT DUE: ${Utils.formatIndianCurrency(netAmount.value)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val upiId = "${profile?.accountNo?.ifBlank { "98700" } ?: "98700"}@${profile?.bankName?.lowercase()?.filter { it.isLetter() }?.ifBlank { "icici" } ?: "icici"}"
                        Text(
                            text = "Merchant ID: $upiId",
                            fontSize = 11.sp,
                            color = AppColors.textTertiary
                        )
                        Text(
                            text = "Business: ${profile?.businessName?.ifBlank { "ZeroBook Pvt" } ?: "ZeroBook Pvt"}",
                            fontSize = 11.sp,
                            color = AppColors.textTertiary
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = simulationStatus,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showSimulatedCheckMark) AppColors.success else AppColors.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { simulationProgress },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = if (showSimulatedCheckMark) AppColors.success else AppColors.primary,
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showUpiPaymentDialog = false
                    // Backup manual confirm override
                    saveTheVoucher(true)
                }) {
                    Text("Manual Code Override (Bypass)", color = AppColors.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpiPaymentDialog = false }) {
                    Text("Cancel", color = AppColors.error)
                }
            }
        )
    }

    // Direct Receipt Thermal-style Print Dialog
    if (showPrintReceiptDialog) {
        var savedInvoiceHtml by remember(printedVoucherId) { mutableStateOf<String?>(null) }
        var savedInvoiceLoading by remember(printedVoucherId) { mutableStateOf(true) }
        var savedInvoiceError by remember(printedVoucherId) { mutableStateOf<String?>(null) }

        LaunchedEffect(printedVoucherId) {
            val voucherId = printedVoucherId
            savedInvoiceLoading = true
            savedInvoiceError = null
            savedInvoiceHtml = null
            if (voucherId.isNullOrBlank()) {
                savedInvoiceLoading = false
                savedInvoiceError = "Invoice preview unavailable."
            } else {
                runCatching { viewModel.getInvoiceRenderBundle(voucherId)?.html }
                    .onSuccess { html ->
                        savedInvoiceHtml = html
                        savedInvoiceError = if (html == null) "Invoice preview unavailable." else null
                    }
                    .onFailure { error ->
                        savedInvoiceError = error.message ?: "Invoice preview unavailable."
                    }
                savedInvoiceLoading = false
            }
        }

        AlertDialog(
            onDismissRequest = { 
                showPrintReceiptDialog = false
                onNavigateBack() // Must navigate back as specified by "save and print means saving it and then printing directly"
            },
            containerColor = AppColors.cardBg,
            titleContentColor = AppColors.textPrimary,
            textContentColor = AppColors.textSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AppColors.success, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Transaction Saved Successfully", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.textPrimary)
                }
            },
            text = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    tonalElevation = 4.dp
                ) {
                    when {
                        savedInvoiceLoading -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                        savedInvoiceHtml != null -> {
                            val invoiceHtml = savedInvoiceHtml.orEmpty()
                            AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.allowFileAccess = true
                                    settings.allowContentAccess = true
                                    @Suppress("DEPRECATION")
                                    settings.allowFileAccessFromFileURLs = true
                                    @Suppress("DEPRECATION")
                                    settings.allowUniversalAccessFromFileURLs = true
                                    settings.javaScriptEnabled = false
                                    settings.setSupportZoom(true)
                                    settings.builtInZoomControls = true
                                    settings.displayZoomControls = false
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    webViewClient = WebViewClient()
                                }
                            },
                            update = { webView ->
                                webView.loadDataWithBaseURL(
                                    "file:///",
                                    invoiceHtml,
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(420.dp)
                        )
                        }
                        else -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = savedInvoiceError ?: "Invoice preview unavailable.",
                                color = AppColors.textSecondary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            showPrintReceiptDialog = false
                            coroutineScope.launch {
                                val savedVoucherId = printedVoucherId
                                if (savedVoucherId.isNullOrBlank()) {
                                    android.widget.Toast.makeText(context, "Saved invoice not found", android.widget.Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                    return@launch
                                }
                                runCatching {
                                    val bundle = viewModel.getInvoiceRenderBundle(savedVoucherId)
                                        ?: error("Failed to load latest invoice data")
                                    val pdfFile = InvoiceGenerator.renderBundleToPdf(context, bundle)
                                    pendingSaveAsPdfFile = pdfFile
                                    navigateBackAfterPdfSave = true
                                    saveInvoicePdfLauncher.launch(bundle.exportFileName)
                                }.onFailure {
                                    android.widget.Toast.makeText(
                                        context,
                                        it.message ?: "Failed to save PDF",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                    navigateBackAfterPdfSave = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save PDF As", color = Color.White)
                    }
                    Button(
                        onClick = {
                            showPrintReceiptDialog = false
                            coroutineScope.launch {
                                val savedVoucherId = printedVoucherId
                                if (savedVoucherId.isNullOrBlank()) {
                                    android.widget.Toast.makeText(context, "Saved invoice not found", android.widget.Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                    return@launch
                                }
                                runCatching {
                                    val bundle = viewModel.getInvoiceRenderBundle(savedVoucherId)
                                        ?: error("Failed to load latest invoice data")
                                    val pdfFile = InvoiceGenerator.renderBundleToPdf(context, bundle)
                                    com.zerobook.app.services.shareInvoicePdfToWhatsApp(context, pdfFile)
                                }.onFailure {
                                    android.widget.Toast.makeText(
                                        context,
                                        it.message ?: "Failed to share PDF",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share via WhatsApp / Other", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPrintReceiptDialog = false
                    onNavigateBack()
                }) {
                    Text("Close & Exit", color = AppColors.error)
                }
            }
        )
    }

    if (showConfirmSaveDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmSaveDialog = false },
            containerColor = AppColors.cardBg,
            titleContentColor = AppColors.textPrimary,
            textContentColor = AppColors.textSecondary,
            title = {
                Text(
                    if (isEditMode) "Update this voucher?" else "Confirm Save",
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
            },
            text = {
                Text(
                    if (isEditMode) {
                        "Ledger entries will be recalculated."
                    } else {
                        "Are you sure you want to save this transaction?"
                    },
                    fontSize = 14.sp,
                    color = AppColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmSaveDialog = false
                    val isSaleOrReturn = selectedType == "SALE" || selectedType == "SALE_RETURN"
                    if (saveShouldPrint && paymentMode == "UPI" && isSaleOrReturn) {
                        showUpiPaymentDialog = true
                    } else {
                        saveTheVoucher(saveShouldPrint)
                    }
                }) {
                    Text(if (isEditMode) "Update" else "Save", color = AppColors.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSaveDialog = false }) {
                    Text("Cancel", color = AppColors.error)
                }
            }
        )
    }

    if (showQuickAddPartyDialog) {
        val inlinePartyType = if (isCustomerVoucher) "CUSTOMER" else "SUPPLIER"
        CreatePartyInlineSheet(
            partyType = inlinePartyType,
            onSave = { partyObj ->
                viewModel.saveParty(partyObj) {
                    selectedParty = partyObj
                    showQuickAddPartyDialog = false
                }
            },
            onDismiss = { showQuickAddPartyDialog = false }
        )
    }

    if (showStockReportSheet) {
        BackHandler { showStockReportSheet = false }
        ModalBottomSheet(
            onDismissRequest = { showStockReportSheet = false },
            containerColor = AppColors.cardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text("Current Stock Levels", fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                StockReportScreen(products = products)
            }
        }
    }

    inlineCreatedProductId?.let { productId ->
        val inlineProduct = products.find { it.id == productId }
        if (inlineProduct != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                border = BorderStroke(1.dp, AppColors.border)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Product added: ${inlineProduct.name}", color = AppColors.textPrimary, modifier = Modifier.weight(1f))
                    TextButton(onClick = { inlineEditProductId = inlineProduct.id }) {
                        Text("Edit details later")
                    }
                }
            }
        }
    }

    inlineEditProductId?.let { productId ->
        val product = products.find { it.id == productId }
        if (product != null) {
            ProductEditorScreen(
                viewModel = viewModel,
                mode = ProductSheetMode.EDIT,
                existingProduct = product,
                onDismiss = { inlineEditProductId = null }
            )
        }
    }

    if (showPartyPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPartyPickerSheet = false },
            containerColor = AppColors.cardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = partySearchQuery,
                    onValueChange = { partySearchQuery = it },
                    placeholder = { Text("Search customer name or phone", color = AppColors.textTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textTertiary)
                    },
                    colors = zeroBookInputColors()
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedParty = null
                                    showPartyPickerSheet = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE0E4EA))
                        ) {
                            Text(
                                text = "Cash / Bank (Walk-in Customer)",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                color = Color(0xFF0D0D0D),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    items(filteredParties, key = { it.id }) { party ->
                        val partyBalance = partyBalanceMap[party.id].orEmptyBalance()
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedParty = party
                                    showPartyPickerSheet = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFFFF),
                            border = BorderStroke(1.dp, Color(0xFFE0E4EA))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = party.name,
                                        color = Color(0xFF0D0D0D),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = party.phone.ifBlank { "No phone" },
                                        color = Color(0xFF777777),
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = Utils.formatIndianCurrency(partyBalance),
                                    color = Color(0xFF1A73E8),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPartyPickerSheet = false
                                    showQuickAddPartyDialog = true
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE0E4EA))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF1A73E8))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = createPartyLabel,
                                    color = Color(0xFF1A73E8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReturnInvoiceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReturnInvoiceSheet = false },
            containerColor = Color(0xFFFFFFFF)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = sourceInvoiceSearchQuery,
                    onValueChange = { sourceInvoiceSearchQuery = it },
                    placeholder = { Text("Search by voucher number or party name", color = Color(0xFF777777)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF777777)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFFFFF),
                        unfocusedContainerColor = Color(0xFFFFFFFF),
                        focusedTextColor = Color(0xFF0D0D0D),
                        unfocusedTextColor = Color(0xFF0D0D0D)
                    )
                )
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(returnSourceInvoices, key = { it.id }) { invoice ->
                        val partyName = parties.find { it.id == invoice.partyId }?.name.orEmpty()
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        restoreReturnSourceInvoice(invoice)
                                        showReturnInvoiceSheet = false
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFFFF),
                            border = BorderStroke(1.dp, Color(0xFFE0E4EA))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(invoice.voucherNo, fontWeight = FontWeight.Bold, color = Color(0xFF0D0D0D))
                                Text(partyName, fontSize = 12.sp, color = Color(0xFF777777))
                                Text(
                                    "${Utils.formatDate(invoice.date)}  •  ${Utils.formatIndianCurrency(invoice.netAmount)}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF777777)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showItemEntrySheet) {
        val isPurchaseType = selectedType == "PURCHASE" || selectedType == "PURCHASE_RETURN"
        val isSaleTypeLocal = selectedType == "SALE" || selectedType == "SALE_RETURN"
        VoucherItemEntrySheet(
            products = products,
            existingItem = editingItemIndex?.let { lineItems.getOrNull(it) },
            isPurchase = isPurchaseType,
            hasGst = hasGst,
            globalGstEnabled = globalGstEnabled && isSaleTypeLocal,
            globalGstRate = globalGstRate,
            isInterstate = isInterstate,
            preselectedProduct = pendingSelectedProduct,
            onDismiss = { showItemEntrySheet = false },
            onSave = { item ->
                val idx = editingItemIndex
                if (idx != null && idx < lineItems.size) {
                    lineItems[idx] = item
                } else {
                    lineItems.add(item)
                }
                editingItemIndex = null
            },
            onCreateProduct = { initialName ->
                showItemEntrySheet = false
                quickAddInitialProductName = initialName
                showQuickAddProductDialog = true
            },
            onProductConsumed = { pendingSelectedProduct = null }
        )
    }

    if (showQuickAddProductDialog) {
        var newProdName by remember(showQuickAddProductDialog) { mutableStateOf(quickAddInitialProductName) }
        var newProdHsn by remember { mutableStateOf("") }
        var quickHsnSuggestions by remember { mutableStateOf<List<HsnResult>>(emptyList()) }
        var showQuickHsnDialog by remember { mutableStateOf(false) }
        var newProdUnit by remember { mutableStateOf("PCS") }
        var newProdSaleRate by remember { mutableStateOf("") }
        var newProdPurchaseRate by remember { mutableStateOf("") }
        var newProdGstRate by remember { mutableStateOf("18.0") }
        var batchEnabled by remember { mutableStateOf(false) }
        var batchNumber by remember { mutableStateOf("") }
        var expiryEnabled by remember { mutableStateOf(false) }
        var expiryDate by remember { mutableStateOf("") }
        var serialEnabled by remember { mutableStateOf(false) }
        var unitExpanded by remember { mutableStateOf(false) }
        var gstExpanded by remember { mutableStateOf(false) }

        val units = listOf("PCS", "KG", "GM", "MG", "LTR", "ML", "BOX", "BAG", "NOS", "MTR")
        val gstRates = listOf("0.0", "5.0", "12.0", "18.0", "28.0")

        AlertDialog(
            onDismissRequest = {
                showQuickAddProductDialog = false
                quickAddInitialProductName = ""
            },
            containerColor = AppColors.cardBg,
            titleContentColor = AppColors.textPrimary,
            textContentColor = AppColors.textSecondary,
            title = { Text("Quick Add Product", fontWeight = FontWeight.Bold, color = AppColors.textPrimary) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    RetailTextField(
                        value = newProdName,
                        onValueChange = { newProdName = it },
                        label = "Product Name *"
                    )

                    ProductOptionalFields(
                        hsnCode = newProdHsn,
                        onHsnChange = { newProdHsn = it },
                        onFindHsn = {
                            quickHsnSuggestions = HsnLookup.search(newProdName.trim())
                            showQuickHsnDialog = true
                        },
                        batchEnabled = batchEnabled,
                        onBatchEnabledChange = { batchEnabled = it },
                        batchNumber = batchNumber,
                        onBatchNumberChange = { batchNumber = it },
                        expiryEnabled = expiryEnabled,
                        onExpiryEnabledChange = { expiryEnabled = it },
                        expiryDate = expiryDate,
                        onExpiryDateChange = { expiryDate = it },
                        serialEnabled = serialEnabled,
                        onSerialEnabledChange = { serialEnabled = it }
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            RetailTextField(
                                value = newProdUnit,
                                onValueChange = { newProdUnit = it.uppercase() },
                                label = "Unit *",
                                readOnly = false,
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.clickable { unitExpanded = true }.padding(4.dp)
                                    )
                                }
                            )
                            DropdownMenu(
                                expanded = unitExpanded,
                                onDismissRequest = { unitExpanded = false },
                                modifier = Modifier.background(AppColors.cardBg)
                            ) {
                                units.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item, color = AppColors.textPrimary) },
                                        onClick = {
                                            newProdUnit = item
                                            unitExpanded = false
                                        },
                                        colors = MenuDefaults.itemColors(textColor = AppColors.textPrimary)
                                    )
                                }
                            }
                        }

                        if (hasGst) {
                            Box(modifier = Modifier.weight(1.2f)) {
                                RetailTextField(
                                    value = "$newProdGstRate%",
                                    onValueChange = {},
                                    label = "GST Rate *",
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            modifier = Modifier.clickable { gstExpanded = true }
                                        )
                                    }
                                )
                                DropdownMenu(
                                    expanded = gstExpanded,
                                    onDismissRequest = { gstExpanded = false },
                                    modifier = Modifier.background(AppColors.cardBg)
                                ) {
                                    gstRates.forEach { rate ->
                                        DropdownMenuItem(
                                            text = { Text("$rate%", color = AppColors.textPrimary) },
                                            onClick = {
                                                newProdGstRate = rate
                                                gstExpanded = false
                                            },
                                            colors = MenuDefaults.itemColors(textColor = AppColors.textPrimary)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RetailTextField(
                            value = newProdSaleRate,
                            onValueChange = { newProdSaleRate = filterDecimalInput(it) },
                            label = "Sale Rate (₹) *",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        if (newProdSaleRate == "0" || newProdSaleRate == "0.0" || newProdSaleRate == "0.00") {
                                            newProdSaleRate = ""
                                        }
                                    } else if (newProdSaleRate.isBlank()) {
                                        newProdSaleRate = "0"
                                    }
                                }
                        )

                        RetailTextField(
                            value = newProdPurchaseRate,
                            onValueChange = { newProdPurchaseRate = filterDecimalInput(it) },
                            label = "Purchase Rate (₹) *",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        if (newProdPurchaseRate == "0" || newProdPurchaseRate == "0.0" || newProdPurchaseRate == "0.00") {
                                            newProdPurchaseRate = ""
                                        }
                                    } else if (newProdPurchaseRate.isBlank()) {
                                        newProdPurchaseRate = "0"
                                    }
                                }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sRate = newProdSaleRate.toDoubleOrNull() ?: 0.0
                        val pRate = newProdPurchaseRate.toDoubleOrNull() ?: 0.0
                        if (newProdName.isNotBlank()) {
                            val prodObj = Product(
                                id = UUID.randomUUID().toString(),
                                name = newProdName.trim(),
                                hsnCode = newProdHsn.trim(),
                                unit = newProdUnit,
                                saleRate = sRate,
                                purchaseRate = pRate,
                                gstRate = if (hasGst) (newProdGstRate.toDoubleOrNull() ?: 0.0) else 0.0,
                                openingStock = 0.0,
                                batchEnabled = batchEnabled,
                                batchNumber = if (batchEnabled) batchNumber.trim() else "",
                                expiryEnabled = expiryEnabled,
                                expiryDate = if (expiryEnabled) expiryDate.trim() else "",
                                serialEnabled = serialEnabled,
                                createdAt = System.currentTimeMillis()
                            )
                            viewModel.saveProduct(prodObj) {
                                pendingSelectedProduct = prodObj
                                inlineCreatedProductId = prodObj.id
                                // Auto-assign this product details to the selected Voucher item row!
                                quickAddProductItemIndex?.let { index ->
                                    if (index in lineItems.indices) {
                                        val rateValue = if (selectedType == "PURCHASE" || selectedType == "PURCHASE_RETURN") prodObj.purchaseRate else prodObj.saleRate
                                        val curItem = lineItems[index]
                                        val dynamicTaxable = rateValue * curItem.qty
                                        val prodGstRate = if (hasGst) prodObj.gstRate else 0.0
                                        val dummyGstAngle = if (hasGst) (dynamicTaxable * prodGstRate / 100.0) else 0.0

                                        lineItems[index] = curItem.copy(
                                            productId = prodObj.id,
                                            productName = prodObj.name,
                                            hsnCode = prodObj.hsnCode,
                                            unit = prodObj.unit,
                                            rate = rateValue,
                                            gstRate = prodGstRate,
                                            taxableAmount = dynamicTaxable,
                                            cgstAmount = if (isInterstate || !hasGst) 0.0 else dummyGstAngle / 2.0,
                                            sgstAmount = if (isInterstate || !hasGst) 0.0 else dummyGstAngle / 2.0,
                                            igstAmount = if (isInterstate && hasGst) dummyGstAngle else 0.0,
                                            totalAmount = dynamicTaxable + dummyGstAngle
                                        )
                                    }
                                }
                                showQuickAddProductDialog = false
                                quickAddInitialProductName = ""
                                showItemEntrySheet = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                ) {
                    Text("Save & Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showQuickAddProductDialog = false
                    quickAddInitialProductName = ""
                }) {
                    Text("Cancel", color = AppColors.primary)
                }
            }
        )

        if (showQuickHsnDialog) {
            AlertDialog(
                onDismissRequest = { showQuickHsnDialog = false },
                title = { Text("Select HSN") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (quickHsnSuggestions.isEmpty()) {
                            Text("No HSN results found for this product name.")
                        } else {
                            quickHsnSuggestions.forEach { result ->
                                TextButton(
                                    onClick = {
                                        newProdHsn = result.hsnCode
                                        showQuickHsnDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                        Text(result.hsnCode, fontWeight = FontWeight.Bold)
                                        Text(result.description, fontSize = 12.sp, color = AppColors.textSecondary)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showQuickHsnDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun LiveInvoicePreview(
    profile: com.zerobook.app.data.BusinessProfile?,
    party: com.zerobook.app.data.Party?,
    voucherNo: String,
    voucherDate: Long,
    paymentMode: String,
    creditDueDate: String,
    partialAmountPaid: Double,
    partialPaymentSubmode: String,
    remainingCreditAmount: Double,
    lineItems: List<com.zerobook.app.data.VoucherItem>,
    additionalCharges: List<com.zerobook.app.data.AdditionalCharge>,
    taxableAmount: Double,
    cgst: Double,
    sgst: Double,
    igst: Double,
    roundOff: Double,
    netAmount: Double,
    selectedType: String
) {
    val context = LocalContext.current
    val previewBusiness = profile ?: BusinessProfile(
        businessName = "ZeroBook Business",
        ownerName = "",
        address = "",
        city = "",
        state = "",
        pin = "",
        phone = "",
        email = "",
        gstin = "",
        pan = "",
        stateCode = "",
        bankName = "",
        accountNo = "",
        ifsc = ""
    )
    val previewVoucher = remember(
        voucherNo,
        voucherDate,
        paymentMode,
        selectedType,
        taxableAmount,
        cgst,
        sgst,
        igst,
        roundOff,
        netAmount,
        party
    ) {
        Voucher(
            id = "preview-$voucherNo",
            voucherNo = voucherNo,
            type = selectedType,
            date = voucherDate,
            partyId = party?.id,
            narration = "",
            taxableAmount = taxableAmount,
            cgst = cgst,
            sgst = sgst,
            igst = igst,
            roundOff = roundOff,
            netAmount = netAmount,
            paymentMode = paymentMode,
            chequeNo = null,
            chequeDate = null,
            bankName = null,
            isIgst = igst > 0.0,
            status = "DRAFT"
        )
    }
    val html = remember(
        previewVoucher,
        lineItems,
        previewBusiness,
        party,
        additionalCharges
    ) {
        InvoiceGenerator.buildInvoiceHtml(
            voucher = previewVoucher,
            items = lineItems,
            business = previewBusiness,
            party = party,
            additionalCharges = additionalCharges,
            renderExtras = InvoiceGenerator.VoucherRenderExtras(
                partialAmountPaid = partialAmountPaid,
                partialPaymentSubmode = partialPaymentSubmode,
                creditDueDate = creditDueDate,
                remainingCreditAmount = remainingCreditAmount
            )
        )
    }
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = "Live invoice preview",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    WebView(context).apply {
                        configureInvoiceWebView(this)
                        webViewClient = WebViewClient()
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL("file:///", html, "text/html", "UTF-8", null)
                }
            )
        }
    }
}

private suspend fun recognizeTextFromBitmap(bitmap: Bitmap): String =
    suspendCancellableCoroutine { continuation ->
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { result ->
                continuation.resume(result.text)
                recognizer.close()
            }
            .addOnFailureListener {
                continuation.resume("")
                recognizer.close()
            }
    }

private suspend fun extractBillText(file: File): String {
    if (!file.exists()) return ""
    val lowerName = file.name.lowercase(Locale.ENGLISH)
    return if (lowerName.endsWith(".pdf")) {
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        try {
            buildString {
                val totalPages = minOf(renderer.pageCount, 3)
                repeat(totalPages) { index ->
                    val page = renderer.openPage(index)
                    val scale = 2
                    val bitmap = Bitmap.createBitmap(
                        page.width * scale,
                        page.height * scale,
                        Bitmap.Config.ARGB_8888
                    )
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    append(recognizeTextFromBitmap(bitmap))
                    append('\n')
                    page.close()
                }
            }
        } finally {
            renderer.close()
            pfd.close()
        }
    } else {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return ""
        recognizeTextFromBitmap(bitmap)
    }
}

private fun parseBillItemsFromText(text: String): List<ParsedBillItemDraft> {
    val ignored = listOf("total", "invoice", "tax", "gst", "amount", "balance")
    val units = listOf("PCS", "KG", "LTR", "MTR", "BOX", "BAG", "NOS")
    return text.lineSequence()
        .map { it.trim() }
        .filter { it.length > 4 && it.any(Char::isDigit) }
        .mapNotNull { line ->
            val normalized = line.replace(",", " ")
            val numberRegex = Regex("""\d+(?:\.\d+)?""")
            val numberMatches = numberRegex.findAll(normalized).map { it.value }.toList()
            if (numberMatches.size < 2) return@mapNotNull null
            val lowercase = normalized.lowercase(Locale.ENGLISH)
            if (ignored.any { lowercase.contains(it) }) return@mapNotNull null

            val qty = numberMatches.firstOrNull()?.takeIf { it.toDoubleOrNull() != null } ?: return@mapNotNull null
            val rate = numberMatches.drop(1).lastOrNull()?.takeIf { it.toDoubleOrNull() != null } ?: return@mapNotNull null
            val hsn = Regex("""\b\d{4,8}\b""").find(normalized)?.value.orEmpty()
            val unit = units.firstOrNull { normalized.uppercase(Locale.ENGLISH).contains(it) } ?: "PCS"
            val cleanedName = normalized
                .replace(hsn, "")
                .replace(qty, "")
                .replace(rate, "")
                .replace(unit, "", ignoreCase = true)
                .replace(Regex("""\s+"""), " ")
                .trim('-', ':', ' ')
            if (cleanedName.length < 3) return@mapNotNull null
            ParsedBillItemDraft(
                name = cleanedName,
                hsn = hsn,
                qty = qty,
                unit = unit,
                rate = rate
            )
        }
        .distinctBy { "${it.name}-${it.qty}-${it.rate}" }
        .take(12)
        .toList()
}

@Composable
private fun ParsedBillItemsDialog(
    items: List<ParsedBillItemDraft>,
    onDismiss: () -> Unit,
    onItemsChange: (List<ParsedBillItemDraft>) -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Items found in bill") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Review and confirm. Edit if needed.", fontSize = 12.sp, color = AppColors.textSecondary)
                items.forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, AppColors.border)
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = item.included,
                                    onCheckedChange = { checked ->
                                        onItemsChange(items.map {
                                            if (it.id == item.id) it.copy(included = checked) else it
                                        })
                                    }
                                )
                                Text("Include", fontSize = 12.sp, color = AppColors.textSecondary)
                                Spacer(Modifier.weight(1f))
                                TextButton(
                                    onClick = { onItemsChange(items.filterNot { it.id == item.id }) }
                                ) { Text("Delete") }
                            }
                            RetailTextField(
                                value = item.name,
                                onValueChange = { value ->
                                    onItemsChange(items.map { if (it.id == item.id) it.copy(name = value) else it })
                                },
                                label = "Name"
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RetailTextField(
                                    value = item.qty,
                                    onValueChange = { value ->
                                        onItemsChange(items.map { if (it.id == item.id) it.copy(qty = filterDecimalInput(value)) else it })
                                    },
                                    label = "Qty",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f)
                                )
                                RetailTextField(
                                    value = item.rate,
                                    onValueChange = { value ->
                                        onItemsChange(items.map { if (it.id == item.id) it.copy(rate = filterDecimalInput(value)) else it })
                                    },
                                    label = "Rate",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RetailTextField(
                                    value = item.hsn,
                                    onValueChange = { value ->
                                        onItemsChange(items.map { if (it.id == item.id) it.copy(hsn = value.filter(Char::isDigit)) else it })
                                    },
                                    label = "HSN",
                                    modifier = Modifier.weight(1f)
                                )
                                RetailTextField(
                                    value = item.unit,
                                    onValueChange = { value ->
                                        onItemsChange(items.map { if (it.id == item.id) it.copy(unit = value.uppercase(Locale.ENGLISH)) else it })
                                    },
                                    label = "Unit",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Add ${items.count { it.included }} items to voucher")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Skip — add manually") }
        }
    )
}

@Composable
fun StickyBottomBar(
    netAmount: Double,
    selectedType: String,
    saveButtonLabel: String,
    onSaveClick: (shouldPrint: Boolean) -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Net Amount",
                        fontSize = 11.sp,
                        color = AppColors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = Utils.formatIndianCurrency(netAmount),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.primary
                    )
                }
                
                val isSaleOrReturn = selectedType == "SALE" || selectedType == "SALE_RETURN"
                if (isSaleOrReturn) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onSaveClick(false) },
                            modifier = Modifier
                                .height(44.dp)
                                .pressScale()
                                .testTag("save_and_exit_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(saveButtonLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        
                        Button(
                            onClick = { onSaveClick(true) },
                            modifier = Modifier
                                .height(44.dp)
                                .pressScale()
                                .testTag("save_and_print_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Print", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = { onSaveClick(false) },
                        modifier = Modifier
                            .height(44.dp)
                            .pressScale()
                            .testTag("save_voucher_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(saveButtonLabel, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
