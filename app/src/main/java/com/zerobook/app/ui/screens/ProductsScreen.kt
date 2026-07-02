package com.zerobook.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerobook.app.data.HsnLookup
import com.zerobook.app.data.HsnResult
import com.zerobook.app.data.Product
import com.zerobook.app.data.Utils
import com.zerobook.app.data.filterDecimalInput
import com.zerobook.app.ui.AppViewModel
import com.zerobook.app.ui.animation.premiumCombinedClickable
import com.zerobook.app.ui.animation.premiumFabEntrance
import com.zerobook.app.ui.animation.pressScale
import com.zerobook.app.ui.theme.AppColors
import java.util.Locale
import java.util.UUID

enum class ProductSheetMode { ADD, EDIT }

private data class ProductEditorState(
    val id: String? = null,
    val name: String = "",
    val hsnCode: String = "",
    val unit: String = "PCS",
    val saleRate: String = "",
    val purchaseRate: String = "",
    val gstRate: Double = 18.0,
    val openingStock: String = "0",
    val currentStock: String = "0",
    val lowStockThreshold: String = "5",
    val enableStockAlert: Boolean = false,
    val barcodeValue: String = "",
    val secondaryUnit: String = "",
    val conversionFactor: String = "1",
    val batchEnabled: Boolean = false,
    val batchNumber: String = "",
    val expiryEnabled: Boolean = false,
    val expiryDate: String = "",
    val serialEnabled: Boolean = false
)

private fun Product.toEditorState() = ProductEditorState(
    id = id,
    name = name,
    hsnCode = hsnCode,
    unit = unit,
    saleRate = saleRate.toString(),
    purchaseRate = purchaseRate.toString(),
    gstRate = gstRate,
    openingStock = openingStock.toString(),
    currentStock = currentStock.toString(),
    lowStockThreshold = lowStockThreshold.toString(),
    enableStockAlert = enableStockAlert,
    barcodeValue = barcodeValue,
    secondaryUnit = secondaryUnit,
    conversionFactor = conversionFactor.toString(),
    batchEnabled = batchEnabled,
    batchNumber = batchNumber,
    expiryEnabled = expiryEnabled,
    expiryDate = expiryDate,
    serialEnabled = serialEnabled
)

private fun Product.isLowStockAlertTriggered(): Boolean =
    enableStockAlert && currentStock <= lowStockThreshold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var sheetMode by remember { mutableStateOf<ProductSheetMode?>(null) }
    var editingProductId by remember { mutableStateOf<String?>(null) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var actionProduct by remember { mutableStateOf<Product?>(null) }

    val normalizedQuery = remember(searchQuery) { searchQuery.trim().lowercase(Locale.US) }
    val filteredProducts = remember(products, normalizedQuery) {
        products.filter { product ->
            val haystack = "${product.name} ${product.hsnCode} ${product.barcodeValue}".lowercase(Locale.US)
            normalizedQuery.isEmpty() || haystack.contains(normalizedQuery)
        }
    }

    val lowStockCount = remember(filteredProducts) {
        filteredProducts.count { it.isLowStockAlertTriggered() }
    }

    if (sheetMode != null) {
        ProductEditorScreen(
            viewModel = viewModel,
            mode = sheetMode ?: ProductSheetMode.ADD,
            existingProduct = products.find { it.id == editingProductId },
            onDismiss = {
                sheetMode = null
                editingProductId = null
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppColors.screenBg,
        topBar = {
            TopAppBar(
                title = { Text("Manage Products", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.cardBg)
            )
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = {
                    editingProductId = null
                    sheetMode = ProductSheetMode.ADD
                },
                containerColor = AppColors.primary,
                contentColor = AppColors.textOnPrimary,
                modifier = Modifier
                    .premiumFabEntrance()
                    .pressScale()
                    .testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.screenBg)
                .padding(innerPadding)
                .imePadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by product, HSN, or barcode") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.inputBg,
                    unfocusedContainerColor = AppColors.inputBg
                )
            )

            if (lowStockCount > 0) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                    Text(
                        text = "$lowStockCount product(s) need stock attention",
                        modifier = Modifier.padding(14.dp),
                        color = Color(0xFFB45309),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No products configured yet.", color = AppColors.textSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(items = filteredProducts, key = { it.id }) { product ->
                        ProductRow(
                            product = product,
                            onOpenDetail = { selectedProduct = product },
                            onEdit = {
                                editingProductId = product.id
                                sheetMode = ProductSheetMode.EDIT
                            },
                            onDelete = { viewModel.deleteProduct(product.id) },
                            onMore = { actionProduct = product }
                        )
                    }
                }
            }
        }
    }

    selectedProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { selectedProduct = null },
            title = { Text(product.name, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("HSN: ${product.hsnCode.ifBlank { "N/A" }}")
                    Text("Unit: ${product.unit}")
                    Text("Sale Rate: ${Utils.formatIndianCurrency(product.saleRate)}")
                    Text("Purchase Rate: ${Utils.formatIndianCurrency(product.purchaseRate)}")
                    Text("GST: ${product.gstRate}%")
                    Text("Opening Stock: ${product.openingStock}")
                    Text("Current Stock: ${product.currentStock}")
                    if (product.enableStockAlert) {
                        Text("Low stock alert below ${product.lowStockThreshold}")
                    } else {
                        Text("Low stock alert disabled")
                    }
                    if (product.barcodeValue.isNotBlank()) {
                        Text("Barcode: ${product.barcodeValue}")
                    }
                    if (product.secondaryUnit.isNotBlank()) {
                        Text("Secondary Unit: ${product.secondaryUnit} (${product.conversionFactor} ${product.unit})")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedProduct = null
                        editingProductId = product.id
                        sheetMode = ProductSheetMode.EDIT
                    },
                    modifier = Modifier.pressScale()
                ) {
                    Text("Edit")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProduct = null }) {
                    Text("Close")
                }
            }
        )
    }

    actionProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { actionProduct = null },
            title = { Text(product.name) },
            text = { Text("Choose an action for this product.") },
            confirmButton = {
                Button(
                    onClick = {
                        actionProduct = null
                        editingProductId = product.id
                        sheetMode = ProductSheetMode.EDIT
                    },
                    modifier = Modifier.pressScale()
                ) {
                    Text("Edit")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.deleteProduct(product.id)
                            actionProduct = null
                        }
                    ) {
                        Text("Delete", color = AppColors.error)
                    }
                    TextButton(onClick = { actionProduct = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ProductRow(
    product: Product,
    onOpenDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMore: () -> Unit
) {
    val statusColor = when {
        product.currentStock <= 0.0 -> Color(0xFFC62828)
        product.isLowStockAlertTriggered() -> Color(0xFFEF6C00)
        else -> Color(0xFF2E7D32)
    }
    val statusLabel = when {
        product.currentStock <= 0.0 -> "Out of Stock"
        product.isLowStockAlertTriggered() -> "Low Stock"
        else -> "In Stock"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFFE8E8E8), RoundedCornerShape(8.dp))
            .premiumCombinedClickable(
                onClick = onOpenDetail,
                onLongClick = onMore
            ),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A1A))
                Spacer(modifier = Modifier.height(4.dp))
                Text("HSN: ${product.hsnCode.ifBlank { "N/A" }}", fontSize = 11.sp, color = AppColors.textSecondary)
                Text(
                    "Stock: ${product.currentStock} ${product.stockUnit.ifBlank { product.unit }}",
                    fontSize = 11.sp,
                    color = AppColors.textSecondary
                )
                if (product.enableStockAlert) {
                    Text("Alert below ${product.lowStockThreshold}", fontSize = 11.sp, color = AppColors.textSecondary)
                }
                if (product.barcodeValue.isNotBlank()) {
                    Text("Barcode: ${product.barcodeValue}", fontSize = 11.sp, color = AppColors.textSecondary)
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit Product", tint = AppColors.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = AppColors.error)
                    }
                }
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(statusLabel, color = statusColor, fontSize = 10.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = statusColor.copy(alpha = 0.12f),
                        disabledLabelColor = statusColor
                    )
                )
                Text(Utils.formatIndianCurrency(product.saleRate), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.primary)
                Text("Sale Rate", fontSize = 10.sp, color = AppColors.textSecondary)
            }
        }
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ProductEditorScreen(
    viewModel: AppViewModel,
    mode: ProductSheetMode,
    existingProduct: Product?,
    onDismiss: () -> Unit
) {
    var editorState by remember(existingProduct, mode) {
        mutableStateOf(existingProduct?.toEditorState() ?: ProductEditorState())
    }
    var showError by remember { mutableStateOf(false) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var gstExpanded by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var hsnResults by remember { mutableStateOf<List<HsnResult>>(emptyList()) }
    var showHsnDialog by remember { mutableStateOf(false) }

    val units = listOf("PCS", "KG", "GM", "MG", "LTR", "ML", "BOX", "BAG", "NOS", "MTR")
    val gstRates = listOf(0.0, 5.0, 12.0, 18.0, 28.0)
    val scrollState = rememberScrollState()

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            onDismiss = { showBarcodeScanner = false },
            onScanned = {
                editorState = editorState.copy(barcodeValue = it)
                showBarcodeScanner = false
            }
        )
    }

    if (showHsnDialog) {
        AlertDialog(
            onDismissRequest = { showHsnDialog = false },
            title = { Text("Select HSN") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hsnResults.isEmpty()) {
                        Text("No HSN results found for this product name.")
                    } else {
                        hsnResults.forEach { result ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
                            ) {
                                TextButton(
                                    onClick = {
                                        editorState = editorState.copy(hsnCode = result.hsnCode)
                                        showHsnDialog = false
                                    }
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                                        Text(result.hsnCode, fontWeight = FontWeight.Bold)
                                        Text(result.description, fontSize = 12.sp, color = AppColors.textSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHsnDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppColors.screenBg,
        topBar = {
            TopAppBar(
                title = { Text(if (mode == ProductSheetMode.EDIT) "Edit Product" else "Add Product", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(scrollState)
                .imePadding()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = editorState.name,
                onValueChange = { editorState = editorState.copy(name = it) },
                label = { Text("Product / Item Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ProductOptionalFields(
                hsnCode = editorState.hsnCode,
                onHsnChange = { editorState = editorState.copy(hsnCode = it) },
                onFindHsn = {
                    hsnResults = HsnLookup.search(editorState.name.trim())
                    showHsnDialog = true
                },
                batchEnabled = editorState.batchEnabled,
                onBatchEnabledChange = { editorState = editorState.copy(batchEnabled = it) },
                batchNumber = editorState.batchNumber,
                onBatchNumberChange = { editorState = editorState.copy(batchNumber = it) },
                expiryEnabled = editorState.expiryEnabled,
                onExpiryEnabledChange = { editorState = editorState.copy(expiryEnabled = it) },
                expiryDate = editorState.expiryDate,
                onExpiryDateChange = { editorState = editorState.copy(expiryDate = it) },
                serialEnabled = editorState.serialEnabled,
                onSerialEnabledChange = { editorState = editorState.copy(serialEnabled = it) }
            )

            OutlinedTextField(
                value = editorState.barcodeValue,
                onValueChange = { editorState = editorState.copy(barcodeValue = it.trim()) },
                label = { Text("Barcode / QR Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { showBarcodeScanner = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Scan barcode")
                    }
                }
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = editorState.unit,
                    onValueChange = {},
                    label = { Text("Unit of Measurement *") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.combinedClickable(onClick = { unitDropdownExpanded = true })
                        )
                    }
                )
                DropdownMenu(
                    expanded = unitDropdownExpanded,
                    onDismissRequest = { unitDropdownExpanded = false }
                ) {
                    units.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = {
                                editorState = editorState.copy(unit = unit)
                                unitDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                DecimalField(
                    value = editorState.saleRate,
                    onValueChange = { editorState = editorState.copy(saleRate = it) },
                    label = "Sale Rate (Rs) *",
                    modifier = Modifier.weight(1f)
                )
                DecimalField(
                    value = editorState.purchaseRate,
                    onValueChange = { editorState = editorState.copy(purchaseRate = it) },
                    label = "Cost Rate (Rs) *",
                    modifier = Modifier.weight(1f)
                )
            }

            ExposedDropdownMenuBox(expanded = gstExpanded, onExpandedChange = { gstExpanded = it }) {
                OutlinedTextField(
                    value = "${editorState.gstRate.toInt()}%",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("GST Rate") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gstExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = gstExpanded, onDismissRequest = { gstExpanded = false }) {
                    gstRates.forEach { rate ->
                        DropdownMenuItem(
                            text = { Text("${rate.toInt()}%") },
                            onClick = {
                                editorState = editorState.copy(gstRate = rate)
                                gstExpanded = false
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                DecimalField(
                    value = editorState.openingStock,
                    onValueChange = { editorState = editorState.copy(openingStock = it) },
                    label = "Opening Stock Quantity",
                    modifier = Modifier.weight(1f)
                )
                DecimalField(
                    value = editorState.currentStock,
                    onValueChange = { editorState = editorState.copy(currentStock = it) },
                    label = "Current Stock",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Low stock alert", fontWeight = FontWeight.Medium)
                    Text(
                        if (editorState.enableStockAlert) "Alert will show when stock reaches threshold"
                        else "This product will never raise low stock warnings",
                        fontSize = 12.sp,
                        color = AppColors.textSecondary
                    )
                }
                Switch(
                    checked = editorState.enableStockAlert,
                    onCheckedChange = {
                        editorState = editorState.copy(enableStockAlert = it)
                    }
                )
            }

            if (editorState.enableStockAlert) {
                DecimalField(
                    value = editorState.lowStockThreshold,
                    onValueChange = { editorState = editorState.copy(lowStockThreshold = it) },
                    label = "Alert when stock falls below",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = editorState.secondaryUnit,
                    onValueChange = { editorState = editorState.copy(secondaryUnit = it.uppercase()) },
                    label = { Text("Secondary Unit") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                DecimalField(
                    value = editorState.conversionFactor,
                    onValueChange = { editorState = editorState.copy(conversionFactor = it) },
                    label = "Conversion Factor",
                    modifier = Modifier.weight(1f)
                )
            }

            if (showError) {
                Text(
                    "Please fill the required product details before saving.",
                    color = AppColors.error,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = {
                    val saleRate = editorState.saleRate.toDoubleOrNull()
                    val purchaseRate = editorState.purchaseRate.toDoubleOrNull()
                    val openingStock = editorState.openingStock.toDoubleOrNull() ?: 0.0
                    val currentStock = editorState.currentStock.toDoubleOrNull() ?: openingStock
                    val lowStockThreshold = editorState.lowStockThreshold.toDoubleOrNull() ?: 5.0
                    val conversionFactor = editorState.conversionFactor.toDoubleOrNull() ?: 1.0
                    if (editorState.name.isBlank() || saleRate == null || purchaseRate == null) {
                        showError = true
                    } else {
                        viewModel.saveProduct(
                            Product(
                                id = editorState.id ?: UUID.randomUUID().toString(),
                                name = editorState.name.trim(),
                                hsnCode = editorState.hsnCode,
                                unit = editorState.unit,
                                saleRate = saleRate,
                                purchaseRate = purchaseRate,
                                gstRate = editorState.gstRate,
                                openingStock = openingStock,
                                currentStock = currentStock,
                                enableStockAlert = editorState.enableStockAlert,
                                lowStockThreshold = lowStockThreshold,
                                stockUnit = editorState.unit,
                                barcodeValue = editorState.barcodeValue,
                                secondaryUnit = editorState.secondaryUnit,
                                conversionFactor = conversionFactor,
                                batchEnabled = editorState.batchEnabled,
                                batchNumber = if (editorState.batchEnabled) editorState.batchNumber.trim() else "",
                                expiryEnabled = editorState.expiryEnabled,
                                expiryDate = if (editorState.expiryEnabled) editorState.expiryDate.trim() else "",
                                serialEnabled = editorState.serialEnabled
                            )
                        ) {
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .pressScale()
                    .testTag("save_product_button"),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (mode == ProductSheetMode.EDIT) "Update Product" else "Save Product",
                    color = AppColors.textOnPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DecimalField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(filterDecimalInput(it)) },
        label = { Text(label) },
        modifier = modifier.onFocusChanged { state ->
            if (state.isFocused && (value == "0" || value == "0.0")) {
                onValueChange("")
            } else if (!state.isFocused && value.isBlank()) {
                onValueChange("0")
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}
