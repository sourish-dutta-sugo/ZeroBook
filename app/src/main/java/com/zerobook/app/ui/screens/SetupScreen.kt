package com.zerobook.app.ui.screens

import android.Manifest
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.zerobook.app.R
import com.zerobook.app.data.BusinessProfile
import com.zerobook.app.data.Utils
import com.zerobook.app.ui.AppViewModel
import com.zerobook.app.ui.theme.AppColors
import com.zerobook.app.ui.theme.Colors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
private fun SetupInputSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        content()
    }
}

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
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val contentMaxWidth: Dp = if (isTablet) 760.dp else Dp.Infinity
    val contentHorizontalPadding = if (isTablet) 24.dp else 16.dp
    val contentTopPadding = if (isTablet) 12.dp else 8.dp

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

    val businessNameError =
        submitAttempted && missingFields.contains(RequiredBusinessField.BusinessName)
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
                            painter = painterResource(R.drawable.logo_icon),
                            contentDescription = "ZeroBook",
                            modifier = Modifier.size(36.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ZeroBook",
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
                    .imePadding(),
                horizontalAlignment = if (isTablet) Alignment.CenterHorizontally else Alignment.Start
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = contentMaxWidth)
                        .padding(
                            horizontal = contentHorizontalPadding,
                            vertical = contentTopPadding
                        )
                        .padding(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Business Profile Setup",
                            fontSize = if (isTablet) 24.sp else 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.textPrimary
                        )
                    }

                    HorizontalDivider(
                        color = AppColors.divider,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    Text(
                        text = "Business Profile Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.primary
                    )

                    Column(
                        modifier = Modifier.bringIntoViewRequester(
                            businessNameBringIntoViewRequester
                        )
                    ) {
                        SetupInputSurface {
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
                                        Text(
                                            "This field is required",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
                        }
                    }

                    SetupInputSurface {
                        RetailTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = "Owner / Signatory Name",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    Column(modifier = Modifier.bringIntoViewRequester(addressBringIntoViewRequester)) {
                        SetupInputSurface {
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
                                        Text(
                                            "This field is required",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                } else {
                                    null
                                }
                            )
                        }
                    }

                    if (isTablet) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .bringIntoViewRequester(pinBringIntoViewRequester)
                            ) {
                                SetupInputSurface {
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
                                                Text(
                                                    "This field is required",
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        } else {
                                            null
                                        }
                                    )
                                }
                            }

                            SetupInputSurface(modifier = Modifier.weight(1f)) {
                                RetailTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = "City / District",
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(
                                modifier = Modifier.bringIntoViewRequester(
                                    pinBringIntoViewRequester
                                )
                            ) {
                                SetupInputSurface {
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
                                                Text(
                                                    "This field is required",
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        } else {
                                            null
                                        }
                                    )
                                }
                            }

                            SetupInputSurface {
                                RetailTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = "City / District",
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    if (pinLookupError.isNotBlank()) {
                        Text(
                            text = pinLookupError,
                            color = AppColors.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        SetupInputSurface {
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
                        }
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

                    if (isTablet) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SetupInputSurface(modifier = Modifier.weight(1f)) {
                                RetailTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = "Phone Number",
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                            }
                            SetupInputSurface(modifier = Modifier.weight(1f)) {
                                RetailTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = "Email Address",
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SetupInputSurface(modifier = Modifier.weight(1.2f)) {
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
                                            resolveIndianStateInfo(
                                                uppercaseInput.substring(
                                                    0,
                                                    2
                                                )
                                            )?.let {
                                                selectedStateInfo = it
                                            }
                                            Utils.INDIAN_STATES.find {
                                                it.second == uppercaseInput.substring(
                                                    0,
                                                    2
                                                )
                                            }?.let {
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
                            }

                            SetupInputSurface(modifier = Modifier.weight(0.8f)) {
                                RetailTextField(
                                    value = pan,
                                    onValueChange = { pan = it.uppercase() },
                                    label = "PAN",
                                    modifier = Modifier.weight(0.8f),
                                    singleLine = true
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SetupInputSurface {
                                RetailTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = "Phone Number",
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                            }
                            SetupInputSurface {
                                RetailTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = "Email Address",
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )
                            }
                            SetupInputSurface {
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
                                            resolveIndianStateInfo(
                                                uppercaseInput.substring(
                                                    0,
                                                    2
                                                )
                                            )?.let {
                                                selectedStateInfo = it
                                            }
                                            Utils.INDIAN_STATES.find {
                                                it.second == uppercaseInput.substring(
                                                    0,
                                                    2
                                                )
                                            }?.let {
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
                                    modifier = Modifier.fillMaxWidth(),
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
                            }
                            SetupInputSurface {
                                RetailTextField(
                                    value = pan,
                                    onValueChange = { pan = it.uppercase() },
                                    label = "PAN",
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Bank Details for Invoice Payments",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AppColors.primary
                    )

                    SetupInputSurface {
                        RetailTextField(
                            value = accountNo,
                            onValueChange = { accountNo = it.filter(Char::isDigit) },
                            label = "Account Number",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    SetupInputSurface {
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
                    }

                    if (ifscVerifiedMessage.isNotBlank()) {
                        Text(
                            text = ifscVerifiedMessage,
                            color = AppColors.primary,
                            fontSize = 12.sp
                        )
                    }

                    SetupInputSurface {
                        RetailTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            label = "Bank Name",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    SetupInputSurface {
                        RetailTextField(
                            value = bankBranch,
                            onValueChange = { bankBranch = it },
                            label = "Bank Branch",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    if (submitAttempted && missingFields.isNotEmpty()) {
                        Text(
                            text = "\u26A0 Pending: ${missingFields.joinToString(", ") { it.label }}",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val buttonInteractionSource = remember { MutableInteractionSource() }
                    val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()
                    val buttonScale by animateFloatAsState(
                        targetValue = if (isButtonPressed) 0.98f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "setupButtonScale"
                    )

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
                            .testTag("submit_setup_button")
                            .shadow(2.dp, RoundedCornerShape(10.dp), clip = false)
                            .scale(buttonScale),
                        interactionSource = buttonInteractionSource,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                    ) {
                        Text(
                            text = "Initialize Business",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
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
}