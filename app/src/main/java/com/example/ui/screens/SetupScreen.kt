package com.example.ui.screens

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BusinessProfile
import com.example.data.Utils
import com.example.ui.AppViewModel
import com.example.ui.theme.AppColors
import com.example.ui.theme.Colors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun SetupScreen(
    viewModel: AppViewModel,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var businessName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var selectedStateInfo by remember { mutableStateOf(Utils.INDIAN_STATES[18]) }
    var pin by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var pan by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var accountNo by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf("") }
    var bankBranch by remember { mutableStateOf("") }
    var ifscVerifiedMessage by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var askForSampleData by remember { mutableStateOf(false) }
    var isPinLoading by remember { mutableStateOf(false) }
    var isGstinLoading by remember { mutableStateOf(false) }
    var isIfscLoading by remember { mutableStateOf(false) }
    var pinLookupError by remember { mutableStateOf("") }
    var locationDetectionMessage by remember { mutableStateOf("") }
    var submitAttempted by remember { mutableStateOf(false) }
    var locationPermissionRequested by rememberSaveable { mutableStateOf(false) }
    var locationDetectionAttempted by rememberSaveable { mutableStateOf(false) }
    var isLocationDetecting by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    val businessNameFocusRequester = remember { FocusRequester() }
    val addressFocusRequester = remember { FocusRequester() }
    val pinFocusRequester = remember { FocusRequester() }

    val businessNameBringIntoViewRequester = remember { BringIntoViewRequester() }
    val addressBringIntoViewRequester = remember { BringIntoViewRequester() }
    val pinBringIntoViewRequester = remember { BringIntoViewRequester() }

    val missingFields = remember(businessName, address, pin) {
        missingRequiredBusinessFields(
            businessName = businessName,
            address = address,
            pinCode = pin
        )
    }

    val businessNameError = submitAttempted && missingFields.contains(RequiredBusinessField.BusinessName)
    val addressError = submitAttempted && missingFields.contains(RequiredBusinessField.Address)
    val pinError = submitAttempted && missingFields.contains(RequiredBusinessField.PinCode)

    fun focusFirstInvalidField(field: RequiredBusinessField) {
        scope.launch {
            when (field) {
                RequiredBusinessField.BusinessName -> {
                    businessNameBringIntoViewRequester.bringIntoView()
                    businessNameFocusRequester.requestFocus()
                }
                RequiredBusinessField.Address -> {
                    addressBringIntoViewRequester.bringIntoView()
                    addressFocusRequester.requestFocus()
                }
                RequiredBusinessField.PinCode -> {
                    pinBringIntoViewRequester.bringIntoView()
                    pinFocusRequester.requestFocus()
                }
            }
            keyboardController?.show()
        }
    }

    LaunchedEffect(pin) {
        if (pin.length == 6 && pin.all { it.isDigit() }) {
            delay(1000)
            isPinLoading = true
            val result = fetchPinLookup(pin)
            isPinLoading = false
            if (result != null) {
                city = result.city
                pinLookupError = ""
                resolveIndianStateInfo(result.state)?.let { selectedStateInfo = it }
            } else {
                pinLookupError = "Unable to fetch location"
            }
        } else {
            isPinLoading = false
            pinLookupError = ""
        }
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted && !locationPermissionRequested) {
            locationPermissionRequested = true
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted && !locationDetectionAttempted) {
            locationDetectionAttempted = true
            isLocationDetecting = true
            detectGstStateInfoFromLocation(context)?.let { detectedState ->
                selectedStateInfo = detectedState
                locationDetectionMessage = "GST state code auto-detected via location."
            }
            isLocationDetecting = false
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
                .background(Colors.surface)
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

                Column(modifier = Modifier.bringIntoViewRequester(businessNameBringIntoViewRequester)) {
                    RetailTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = "Business Name *",
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(businessNameFocusRequester)
                            .testTag("setup_business_name"),
                        singleLine = true,
                        isError = businessNameError,
                        supportingText = if (businessNameError) {
                            {
                                Text("This field is required", color = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            null
                        }
                    )
                }

                RetailTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = "Owner / Signatory Name",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column(modifier = Modifier.bringIntoViewRequester(addressBringIntoViewRequester)) {
                    RetailTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Address (Street / Area) *",
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(addressFocusRequester),
                        singleLine = false,
                        isError = addressError,
                        supportingText = if (addressError) {
                            {
                                Text("This field is required", color = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            null
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .bringIntoViewRequester(pinBringIntoViewRequester)
                    ) {
                        RetailTextField(
                            value = pin,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }
                                if (clean.length <= 6) {
                                    pin = clean
                                }
                            },
                            label = "PIN Code (6-digit) *",
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(pinFocusRequester),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = {
                                if (isPinLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            },
                            isError = pinError,
                            supportingText = if (pinError) {
                                {
                                    Text("This field is required", color = MaterialTheme.colorScheme.error)
                                }
                            } else {
                                null
                            }
                        )
                    }

                    RetailTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = "City / District",
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (pinLookupError.isNotBlank()) {
                    Text(
                        text = pinLookupError,
                        color = AppColors.textSecondary,
                        fontSize = 11.sp
                    )
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    RetailTextField(
                        value = "${selectedStateInfo.first} (Code: ${selectedStateInfo.second})",
                        onValueChange = {},
                        label = "State & GST Code",
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
                    StateDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        onStateSelected = {
                            selectedStateInfo = it
                            dropdownExpanded = false
                            locationDetectionMessage = ""
                        },
                        modifier = Modifier.background(AppColors.cardBg)
                    )
                }

                if (isLocationDetecting || locationDetectionMessage.isNotBlank()) {
                    Text(
                        text = if (isLocationDetecting) {
                            "Detecting GST state code from your location..."
                        } else {
                            locationDetectionMessage
                        },
                        color = AppColors.textSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RetailTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number",
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    RetailTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RetailTextField(
                        value = gstin,
                        onValueChange = { input ->
                            val uppercaseInput = input.uppercase().trim().take(15)
                            gstin = uppercaseInput

                            if (uppercaseInput.length >= 12) {
                                val extractedPan = uppercaseInput.substring(2, 12)
                                if (extractedPan.all { it.isLetterOrDigit() }) {
                                    pan = extractedPan
                                }
                            }

                            if (uppercaseInput.length >= 2) {
                                resolveIndianStateInfo(uppercaseInput.substring(0, 2))?.let {
                                    selectedStateInfo = it
                                }
                                Utils.INDIAN_STATES.find { it.second == uppercaseInput.substring(0, 2) }?.let {
                                    selectedStateInfo = it
                                }
                            }

                            if (uppercaseInput.length == 15) {
                                isGstinLoading = true
                                viewModel.fetchGstinDetails(uppercaseInput) { trade, legal ->
                                    isGstinLoading = false
                                    val detectedName = trade ?: legal
                                    if (detectedName != null && businessName.isBlank()) {
                                        businessName = detectedName
                                    }
                                }
                            } else {
                                isGstinLoading = false
                            }
                        },
                        label = "GSTIN",
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        trailingIcon = {
                            if (isGstinLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    )

                    RetailTextField(
                        value = pan,
                        onValueChange = { pan = it.uppercase() },
                        label = "PAN",
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Bank Details for Invoice Payments",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AppColors.primary
                )

                RetailTextField(
                    value = accountNo,
                    onValueChange = { accountNo = it.filter(Char::isDigit) },
                    label = "Account Number",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                RetailTextField(
                    value = ifsc,
                    onValueChange = { input ->
                        val uppercaseInput = input.uppercase().replace("\\s".toRegex(), "")
                        if (uppercaseInput.length <= 11) {
                            ifsc = uppercaseInput
                            ifscVerifiedMessage = ""
                            if (uppercaseInput.length == 11) {
                                isIfscLoading = true
                                viewModel.fetchIfscDetails(uppercaseInput) { resolvedBank, resolvedBranch ->
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
                                    }
                                }
                            }
                        }
                    },
                    label = "IFSC Code",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if (isIfscLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                )

                if (ifscVerifiedMessage.isNotBlank()) {
                    Text(
                        text = ifscVerifiedMessage,
                        color = AppColors.primary,
                        fontSize = 12.sp
                    )
                }

                RetailTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = "Bank Name",
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

                if (missingFields.isNotEmpty()) {
                    Text(
                        text = "\u26A0 Pending: ${missingFields.joinToString(", ") { it.label }}",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        submitAttempted = true
                        val firstInvalidField = missingFields.firstOrNull()
                        if (firstInvalidField != null) {
                            focusFirstInvalidField(firstInvalidField)
                        } else {
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
            title = {
                Text(
                    "Explore with Sample Data?",
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
            },
            text = {
                Text(
                    "Would you like to load sample products, parties, and vouchers to instantly see how the charts, outstanding balances, and GST summary ledger reports operate?",
                    color = AppColors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val profile = BusinessProfile(
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
                        viewModel.saveProfile(profile) {
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
                        val profile = BusinessProfile(
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
                        viewModel.saveProfile(profile) {
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
