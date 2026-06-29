@file:Suppress("DEPRECATION")

package com.zerobook.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerobook.app.data.Product
import com.zerobook.app.data.Utils
import com.zerobook.app.data.VoucherItem
import com.zerobook.app.data.filterDecimalInput
import com.zerobook.app.data.filterHsnInput
import com.zerobook.app.data.searchHsn
import com.zerobook.app.data.suggestHsn
import com.zerobook.app.ui.theme.AppColors
import com.zerobook.app.ui.theme.TextDark
import com.zerobook.app.ui.theme.TextGray
import com.zerobook.app.ui.theme.TextHint
import com.zerobook.app.ui.theme.darkDropdownItemColors
import com.zerobook.app.ui.theme.zeroBookInputColors
import java.util.UUID

private val UNIT_OPTIONS = listOf("PCS", "KG", "LTR", "MTR", "BOX", "BAG", "NOS")
private val GST_OPTIONS = listOf(0.0, 5.0, 12.0, 18.0, 28.0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherItemEntrySheet(
    products: List<Product>,
    existingItem: VoucherItem?,
    isPurchase: Boolean,
    hasGst: Boolean,
    globalGstEnabled: Boolean,
    globalGstRate: Double,
    isInterstate: Boolean,
    preselectedProduct: Product? = null,
    onDismiss: () -> Unit,
    onSave: (VoucherItem) -> Unit,
    onCreateProduct: (String) -> Unit,
    onProductConsumed: () -> Unit = {}
) {
    val fieldColors = zeroBookInputColors()
    val isEditing = existingItem != null

    var productName by remember { mutableStateOf(existingItem?.productName ?: "") }
    var productId by remember { mutableStateOf(existingItem?.productId ?: "") }
    var hsnCode by remember { mutableStateOf(existingItem?.hsnCode ?: "") }
    var qtyText by remember {
        mutableStateOf(existingItem?.let { if (it.qty == 0.0) "" else it.qty.toString() } ?: "")
    }
    var unit by remember { mutableStateOf(existingItem?.unit ?: "PCS") }
    var rateText by remember {
        mutableStateOf(existingItem?.let { if (it.rate == 0.0) "" else it.rate.toString() } ?: "")
    }
    var discountText by remember {
        mutableStateOf(existingItem?.let { if (it.discount == 0.0) "" else it.discount.toString() } ?: "")
    }
    var discountType by remember { mutableStateOf(existingItem?.discountType ?: "PERCENT") }
    var gstRate by remember {
        mutableStateOf(
            when {
                !hasGst -> 0.0
                globalGstEnabled -> globalGstRate
                existingItem != null -> existingItem.gstRate
                else -> 0.0
            }
        )
    }

    var showProductDropdown by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var qtyError by remember { mutableStateOf(false) }
    var rateError by remember { mutableStateOf(false) }
    var suggestedHsn by remember { mutableStateOf<com.zerobook.app.data.HsnResult?>(null) }

    val filteredProducts = remember(productName, products) {
        if (productName.isBlank()) products.take(8)
        else products.filter {
            it.name.contains(productName, ignoreCase = true) ||
                it.barcodeValue.contains(productName, ignoreCase = true)
        }.take(8)
    }

    val hsnSuggestions = remember(productName, hsnCode) {
        if (hsnCode.isNotBlank()) emptyList()
        else searchHsn(productName)
    }

    LaunchedEffect(productName, products) {
        val query = productName.trim()
        if (query.length < 2 || hsnCode.isNotBlank()) {
            suggestedHsn = null
        } else {
            kotlinx.coroutines.delay(1200)
            suggestedHsn = suggestHsn(query, products)
        }
    }

    val qty = qtyText.toDoubleOrNull() ?: 0.0
    val rate = rateText.toDoubleOrNull() ?: 0.0
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val subtotal = qty * rate
    val discAmount = if (discountType == "PERCENT") subtotal * discount / 100.0 else discount
    val taxable = (subtotal - discAmount).coerceAtLeast(0.0)
    val gstAmount = if (hasGst) taxable * gstRate / 100.0 else 0.0
    val itemTotal = taxable + gstAmount

    fun applyProductSelection(prod: Product) {
        Snapshot.withMutableSnapshot {
            productId = prod.id
            productName = prod.name
            hsnCode = prod.hsnCode
            unit = prod.unit
            rateText = (if (isPurchase) prod.purchaseRate else prod.saleRate).let {
                if (it == 0.0) "" else it.toString()
            }
            if (hasGst && !globalGstEnabled) {
                gstRate = prod.gstRate
            }
            showProductDropdown = false
            nameError = false
        }
    }

    LaunchedEffect(preselectedProduct?.id) {
        preselectedProduct?.let {
            applyProductSelection(it)
            onProductConsumed()
        }
    }

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            onDismiss = { showBarcodeScanner = false },
            onScanned = { code ->
                showBarcodeScanner = false
                val matched = products.firstOrNull { it.barcodeValue.equals(code, ignoreCase = true) }
                if (matched != null) {
                    applyProductSelection(matched)
                } else {
                    productName = code
                    productId = ""
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.cardBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                if (isEditing) "Edit Item" else "Add Item",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            // FIELD 1 — Product name
            Text("Item / Product Name *", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Box {
                OutlinedTextField(
                    value = productName,
                    onValueChange = {
                        productName = it
                        productId = ""
                        showProductDropdown = true
                        nameError = false
                    },
                    placeholder = { Text("Search or type product name", color = TextHint) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) showProductDropdown = true },
                    colors = fieldColors,
                    singleLine = true,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showBarcodeScanner = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Scan barcode", tint = TextGray)
                            }
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                null,
                                tint = TextGray,
                                modifier = Modifier.clickable { showProductDropdown = true }
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = showProductDropdown && products.isNotEmpty(),
                    onDismissRequest = { showProductDropdown = false },
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .background(AppColors.cardBg)
                ) {
                    filteredProducts.forEach { prod ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(prod.name, color = TextDark, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    Text(
                                        "HSN: ${prod.hsnCode} | Rate: ₹${prod.saleRate}",
                                        color = TextGray,
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            onClick = { applyProductSelection(prod) },
                            colors = darkDropdownItemColors()
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, null, tint = AppColors.primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Create new product", color = AppColors.primary, fontWeight = FontWeight.Bold)
                            }
                        },
                        onClick = {
                            showProductDropdown = false
                            onCreateProduct(productName)
                        },
                        colors = darkDropdownItemColors()
                    )
                }
                if (showProductDropdown && products.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                        border = BorderStroke(1.dp, AppColors.border)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("No products added yet", color = TextDark, fontWeight = FontWeight.Bold)
                            OutlinedButton(
                                onClick = {
                                    showProductDropdown = false
                                    onCreateProduct(productName)
                                },
                                border = BorderStroke(1.dp, AppColors.primary)
                            ) {
                                Text("Add your first product", color = AppColors.primary)
                            }
                        }
                    }
                }
            }
            if (nameError) Text("Product name is required", color = Color(0xFFC62828), fontSize = 11.sp)

            // FIELD 2 — HSN
            Text("HSN/SAC Code", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = hsnCode,
                onValueChange = { hsnCode = filterHsnInput(it) },
                placeholder = { Text("Numbers only", color = TextHint) },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                singleLine = true
            )
            suggestedHsn?.let { suggestion ->
                AssistChip(
                    onClick = { hsnCode = suggestion.hsnCode },
                    label = {
                        Text(
                            "Suggested HSN: ${suggestion.hsnCode}",
                            fontSize = 11.sp,
                            color = TextDark
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = AppColors.primaryLight
                    )
                )
            }
            Text("Type a keyword to search HSN database", color = TextHint, fontSize = 10.sp)
            if (hsnSuggestions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    hsnSuggestions.forEach { result ->
                        SuggestionChip(
                            onClick = { hsnCode = result.hsnCode },
                            label = { Text("${result.hsnCode} ${result.description}", fontSize = 11.sp, color = TextDark) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = AppColors.primaryLight)
                        )
                    }
                }
            }

            // FIELD 3 — Quantity
            DecimalInputField(
                value = qtyText,
                onValueChange = { qtyText = it; qtyError = false },
                label = "Quantity *",
                isError = qtyError,
                modifier = Modifier.fillMaxWidth()
            )
            if (qtyError) Text("Quantity must be greater than zero", color = Color(0xFFC62828), fontSize = 11.sp)

            // FIELD 4 — Unit
            var unitExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = unit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unit", color = TextGray, fontSize = 12.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = fieldColors
                )
                ExposedDropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false },
                    modifier = Modifier.background(AppColors.cardBg)
                ) {
                    UNIT_OPTIONS.forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u, color = TextDark) },
                            onClick = { unit = u; unitExpanded = false },
                            colors = darkDropdownItemColors()
                        )
                    }
                }
            }

            // FIELD 5 — Rate
            DecimalInputField(
                value = rateText,
                onValueChange = { rateText = it; rateError = false },
                label = "Rate (₹) *",
                isError = rateError,
                prefix = { Text("₹", color = TextDark) },
                modifier = Modifier.fillMaxWidth()
            )
            if (rateError) Text("Rate must be greater than zero", color = Color(0xFFC62828), fontSize = 11.sp)

            // FIELD 6 — Discount
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DecimalInputField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = if (discountType == "PERCENT") "Discount (%)" else "Discount (₹)",
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = discountType == "PERCENT",
                    onClick = { discountType = if (discountType == "PERCENT") "AMOUNT" else "PERCENT" },
                    label = { Text(if (discountType == "PERCENT") "%" else "₹", color = TextDark) },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // FIELD 7 — GST Rate
            if (hasGst) {
                Text("GST Rate", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                if (globalGstEnabled) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("${globalGstRate.toInt()}%", color = TextDark) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = AppColors.primaryLight,
                            disabledContainerColor = AppColors.primaryLight
                        )
                    )
                    Text(
                        "Global rate applied — toggle off to change",
                        color = TextHint,
                        fontSize = 10.sp
                    )
                } else {
                    var gstExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = gstExpanded,
                        onExpandedChange = { gstExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = "${gstRate.toInt()}%",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("GST Rate", color = TextGray) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gstExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = fieldColors
                        )
                        ExposedDropdownMenu(
                            expanded = gstExpanded,
                            onDismissRequest = { gstExpanded = false },
                            modifier = Modifier.background(AppColors.cardBg)
                        ) {
                            GST_OPTIONS.forEach { rate ->
                                DropdownMenuItem(
                                    text = { Text("${rate.toInt()}%", color = TextDark) },
                                    onClick = { gstRate = rate; gstExpanded = false },
                                    colors = darkDropdownItemColors()
                                )
                            }
                        }
                    }
                }
            }

            // Live calculation card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CalcLine("Taxable Amount", Utils.formatIndianCurrency(taxable))
                    CalcLine("GST Amount", Utils.formatIndianCurrency(gstAmount))
                    CalcLine("Item Total", Utils.formatIndianCurrency(itemTotal), bold = true)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = TextDark)
                }
                Button(
                    onClick = {
                        nameError = productName.isBlank()
                        qtyError = qty <= 0.0
                        rateError = rate <= 0.0
                        if (nameError || qtyError || rateError) return@Button

                        val halfGst = if (isInterstate || !hasGst) 0.0 else gstAmount / 2.0
                        val item = VoucherItem(
                            id = existingItem?.id ?: UUID.randomUUID().toString(),
                            voucherId = existingItem?.voucherId ?: "",
                            productId = productId.ifBlank { "CUSTOM" },
                            productName = productName,
                            hsnCode = hsnCode.ifBlank { "9900" },
                            qty = qty,
                            unit = unit,
                            rate = rate,
                            discount = discount,
                            discountType = discountType,
                            taxableAmount = taxable,
                            gstRate = if (hasGst) gstRate else 0.0,
                            cgstAmount = halfGst,
                            sgstAmount = halfGst,
                            igstAmount = if (isInterstate && hasGst) gstAmount else 0.0,
                            totalAmount = itemTotal
                        )
                        onSave(item)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                ) {
                    Text(
                        if (isEditing) "Update Item" else "Add to Invoice",
                        color = AppColors.textOnPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcLine(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextGray, fontSize = 12.sp)
        Text(
            value,
            color = TextDark,
            fontSize = 12.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun DecimalInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    prefix: @Composable (() -> Unit)? = null
) {
    var displayValue by remember { mutableStateOf(value) }
    var hadFocus by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (!hadFocus) displayValue = value
    }

    OutlinedTextField(
        value = displayValue,
        onValueChange = { newVal ->
            val filtered = filterDecimalInput(newVal)
            displayValue = filtered
            onValueChange(filtered)
        },
        label = { Text(label, color = TextGray, fontSize = 12.sp) },
        modifier = modifier.onFocusChanged { focus ->
            if (focus.isFocused) {
                hadFocus = true
                if (displayValue == "0" || displayValue == "0.0" || displayValue == "0.00") {
                    displayValue = ""
                    onValueChange("")
                }
            } else {
                hadFocus = false
                if (displayValue.isBlank()) {
                    displayValue = "0"
                    onValueChange("0")
                }
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        isError = isError,
        prefix = prefix,
        colors = zeroBookInputColors(),
        textStyle = androidx.compose.ui.text.TextStyle(color = AppColors.textPrimary, fontSize = 15.sp)
    )
}
