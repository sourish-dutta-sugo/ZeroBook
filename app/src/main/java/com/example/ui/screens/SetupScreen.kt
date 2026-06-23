package com.example.ui.screens
import com.example.ui.theme.AppColors

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BusinessProfile
import com.example.data.Utils
import com.example.ui.AppViewModel
import com.example.ui.theme.Colors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    viewModel: AppViewModel,
    onSetupComplete: () -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var selectedStateInfo by remember { mutableStateOf(Utils.INDIAN_STATES[18]) } // West Bengal as default
    var pin by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var pan by remember { mutableStateOf("") }
    
    // Bank Details
    var bankName by remember { mutableStateOf("") }
    var accountNo by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf("") }
    var bankBranch by remember { mutableStateOf("") }
    var ifscVerifiedMessage by remember { mutableStateOf("") }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var askForSampleData by remember { mutableStateOf(false) }

    // Loading states for smart lookups
    var isPinLoading by remember { mutableStateOf(false) }
    var isGstinLoading by remember { mutableStateOf(false) }
    var isIfscLoading by remember { mutableStateOf(false) }
    var pinLookupMessage by remember { mutableStateOf("") }
    var pinLookupError by remember { mutableStateOf("") }

    // Validation styling states
    var gstinError by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf(false) }
    var ifscError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(pin) {
        if (pin.length == 6 && pin.all { it.isDigit() }) {
            delay(1000)
            isPinLoading = true
            pinError = false
            val result = fetchPinLookup(pin)
            isPinLoading = false
            if (result != null) {
                city = result.city
                pinLookupError = ""
                Utils.INDIAN_STATES.find {
                    it.second == result.stateCode || it.first.equals(result.state, ignoreCase = true)
                }?.let { selectedStateInfo = it }
            } else {
                pinLookupMessage = "City not found — enter manually"
                pinLookupError = "Unable to fetch location"
            }
        } else {
            isPinLoading = false
            pinLookupError = ""
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppColors.screenBg,
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.zerobook_icon),
                            contentDescription = "ZeroBook",
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ZeroBook Setup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = AppColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Colors.surface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Colors.surface) // Setup screen requested in Section 1 to have White background
                .padding(innerPadding)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header block
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.zerobook_icon),
                        contentDescription = "ZeroBook",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = "ZeroBook",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Record. Transact. Grow.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = AppColors.textSecondary
                    )
                }

                HorizontalDivider(color = AppColors.divider, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Business Profile Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.primary
                )

                // Business Name Field
                RetailTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = "Business Name *",
                    modifier = Modifier.fillMaxWidth().testTag("setup_business_name"),
                    singleLine = true
                )

                // Owner Name Field
                RetailTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = "Owner / Signatory Name *",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Address Field
                RetailTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Address (Street / Area) *",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )

                // PIN & City Row
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // PIN Code
                    RetailTextField(
                        value = pin,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            if (clean.length <= 6) {
                                pin = clean
                            }
                        },
                        label = "PIN Code (6-digit) *",
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = {
                            if (isPinLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        },
                        isError = pinError
                    )
                    // City
                    RetailTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = "City *",
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                if (pinLookupError.isNotBlank()) {
                    Text(
                        text = pinLookupError,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }

                // State selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    RetailTextField(
                        value = "${selectedStateInfo.first} (Code: ${selectedStateInfo.second})",
                        onValueChange = {},
                        label = "State & GST Code *",
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { dropdownExpanded = true }
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(300.dp)
                            .background(AppColors.cardBg)
                    ) {
                        Utils.INDIAN_STATES.forEach { statePair ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${statePair.first} (Code ${statePair.second})",
                                        color = AppColors.textPrimary
                                    )
                                },
                                onClick = {
                                    selectedStateInfo = statePair
                                    dropdownExpanded = false
                                },
                                colors = MenuDefaults.itemColors(textColor = AppColors.textPrimary)
                            )
                        }
                    }
                }

                // Phone & Email
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RetailTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number *",
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    RetailTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address *",
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }

                // GSTIN & PAN
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RetailTextField(
                        value = gstin,
                        onValueChange = { input ->
                            val uppercaseIn = input.uppercase().trim()
                            // GSTIN validation constraints (alphanumeric check)
                            val hasNonAlphanumeric = uppercaseIn.any { !it.isLetterOrDigit() }
                            gstinError = hasNonAlphanumeric || uppercaseIn.length > 15
                            
                            gstin = uppercaseIn

                            // On typing, auto extract PAN String (indices 2 to 11, length 10)
                            if (uppercaseIn.length >= 12) {
                                val extractedPan = uppercaseIn.substring(2, 12)
                                if (extractedPan.all { it.isLetterOrDigit() }) {
                                    pan = extractedPan
                                }
                            }

                            // Match State Code as typed
                            if (uppercaseIn.length >= 2) {
                                val possibleCode = uppercaseIn.substring(0, 2)
                                val resolvedState = Utils.INDIAN_STATES.find { it.second == possibleCode }
                                if (resolvedState != null) {
                                    selectedStateInfo = resolvedState
                                }
                            }

                            if (uppercaseIn.length == 15 && !hasNonAlphanumeric) {
                                // Fetch trading / business name
                                isGstinLoading = true
                                viewModel.fetchGstinDetails(uppercaseIn) { trade, legal ->
                                    isGstinLoading = false
                                    val detectedName = trade ?: legal
                                    if (detectedName != null && businessName.isBlank()) {
                                        businessName = detectedName
                                    }
                                }
                            }
                        },
                        label = "GSTIN (15-digit, Optional)",
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        trailingIcon = {
                            if (isGstinLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        },
                        isError = gstinError
                    )

                    RetailTextField(
                        value = pan,
                        onValueChange = { pan = it.uppercase() },
                        label = "PAN Number (Optional)",
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bank Details
                Text(
                    text = "Bank Details for Invoice Payments",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AppColors.primary
                )

                RetailTextField(
                    value = accountNo,
                    onValueChange = { accountNo = it.filter(Char::isDigit) },
                    label = "Account Number *",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                RetailTextField(
                    value = ifsc,
                    onValueChange = { input ->
                        val uppercaseIn = input.uppercase().replace("\\s".toRegex(), "")
                        if (uppercaseIn.length <= 11) {
                            ifsc = uppercaseIn
                            ifscVerifiedMessage = ""
                            if (uppercaseIn.length == 11) {
                                isIfscLoading = true
                                ifscError = false
                                viewModel.fetchIfscDetails(uppercaseIn) { resolvedBank, resolvedBranch ->
                                    isIfscLoading = false
                                    if (!resolvedBank.isNullOrBlank()) {
                                        bankName = resolvedBank
                                        bankBranch = resolvedBranch.orEmpty()
                                        ifscVerifiedMessage = buildString {
                                            append("Bank verified: ")
                                            append(resolvedBank)
                                            if (!resolvedBranch.isNullOrBlank()) {
                                                append(", ")
                                                append(resolvedBranch)
                                            }
                                        }
                                    } else {
                                        ifscError = true
                                    }
                                }
                            }
                        }
                    },
                    label = "IFSC Code *",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if (isIfscLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    },
                    isError = ifscError
                )

                if (ifscVerifiedMessage.isNotBlank()) {
                    Text(
                        text = ifscVerifiedMessage,
                        color = Color(0xFF2E7D32),
                        fontSize = 12.sp
                    )
                }

                RetailTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = "Bank Name *",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                RetailTextField(
                    value = bankBranch,
                    onValueChange = { bankBranch = it },
                    label = "Bank Branch",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (showError) {
                    Text(
                        text = errorMessage,
                        color = AppColors.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Initialize Submit Button
                Button(
                    onClick = {
                        // Basic validation
                        if (businessName.isBlank() || ownerName.isBlank() || address.isBlank() ||
                            city.isBlank() || pin.isBlank() || phone.isBlank() || email.isBlank() ||
                            bankName.isBlank() || accountNo.isBlank() || ifsc.isBlank()) {
                            errorMessage = "Please fill in all fields marked with *"
                            showError = true
                        } else if (gstin.isNotBlank() && gstin.length != 15) {
                            errorMessage = "GSTIN must be exactly 15 characters long if provided."
                            showError = true
                        } else {
                            showError = false
                            askForSampleData = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_setup_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                ) {
                    Text(text = "Initialize Business", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (askForSampleData) {
        AlertDialog(
            onDismissRequest = { askForSampleData = false },
            containerColor = AppColors.cardBg,
            titleContentColor = AppColors.textPrimary,
            textContentColor = AppColors.textSecondary,
            title = { Text("Explore with Sample Data?", fontWeight = FontWeight.Bold, color = AppColors.textPrimary) },
            text = { Text("Would you like to load sample products, parties, and vouchers to instantly see how the charts, outstanding balances, and GST summary ledger reports operate?", color = AppColors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        val profileObj = BusinessProfile(
                            businessName = businessName,
                            ownerName = ownerName,
                            address = address,
                            city = city,
                            state = selectedStateInfo.first,
                            pin = pin,
                            phone = phone,
                            email = email,
                            gstin = gstin,
                            pan = pan,
                            stateCode = selectedStateInfo.second,
                            bankName = bankName,
                            accountNo = accountNo,
                            ifsc = ifsc,
                            branchName = bankBranch
                        )
                        viewModel.saveProfile(profileObj) {
                            viewModel.loadSampleData()
                            askForSampleData = false
                            onSetupComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                ) {
                    Text("Yes, Import")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val profileObj = BusinessProfile(
                            businessName = businessName,
                            ownerName = ownerName,
                            address = address,
                            city = city,
                            state = selectedStateInfo.first,
                            pin = pin,
                            phone = phone,
                            email = email,
                            gstin = gstin,
                            pan = pan,
                            stateCode = selectedStateInfo.second,
                            bankName = bankName,
                            accountNo = accountNo,
                            ifsc = ifsc,
                            branchName = bankBranch
                        )
                        viewModel.saveProfile(profileObj) {
                            askForSampleData = false
                            onSetupComplete()
                        }
                    }
                ) {
                    Text("No, Start Clean", color = AppColors.primary)
                }
            }
        )
    }
}
