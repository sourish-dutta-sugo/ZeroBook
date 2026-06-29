package com.zerobook.app.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zerobook.app.data.BusinessProfile
import com.zerobook.app.data.DEFAULT_TERMS_AND_CONDITIONS
import com.zerobook.app.data.Utils
import com.zerobook.app.ui.AppViewModel
import com.zerobook.app.ui.theme.AppColors
import com.zerobook.app.ui.theme.GstinValidationFeedback
import com.zerobook.app.ui.theme.TextDark
import com.zerobook.app.ui.theme.parseGstinInput
import com.zerobook.app.ui.theme.zeroBookInputColors
import com.zerobook.app.utils.copyUriToInternalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileSettingsSection(
    viewModel: AppViewModel,
    profile: BusinessProfile?,
    isDesktop: Boolean,
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val profileScope = rememberCoroutineScope()
    val formScroll = rememberScrollState()

    val resolvedProfile = profile ?: BusinessProfile(
        businessName = "",
        ownerName = "",
        address = "",
        city = "",
        state = "West Bengal",
        stateCode = "19",
        pin = "",
        phone = "",
        email = "",
        gstin = "",
        pan = "",
        bankName = "",
        accountNo = "",
        ifsc = "",
        logoPath = null,
        signaturePath = null
    )

    var businessName by remember { mutableStateOf(resolvedProfile.businessName) }
    var ownerName by remember { mutableStateOf(resolvedProfile.ownerName) }
    var address by remember { mutableStateOf(resolvedProfile.address) }
    var city by remember { mutableStateOf(resolvedProfile.city) }
    var pinCode by remember { mutableStateOf(resolvedProfile.pin) }
    var selectedStateInfo by remember {
        mutableStateOf(
            Utils.INDIAN_STATES.find { it.first == resolvedProfile.state }
                ?: Utils.INDIAN_STATES[18]
        )
    }
    var phone by remember { mutableStateOf(resolvedProfile.phone) }
    var email by remember { mutableStateOf(resolvedProfile.email) }
    var gstin by remember { mutableStateOf(resolvedProfile.gstin) }
    var gstinValid by remember { mutableStateOf<Boolean?>(null) }
    var pan by remember { mutableStateOf(resolvedProfile.pan) }
    var bankName by remember { mutableStateOf(resolvedProfile.bankName) }
    var accountNo by remember { mutableStateOf(resolvedProfile.accountNo) }
    var ifsc by remember { mutableStateOf(resolvedProfile.ifsc) }
    var bankBranch by remember { mutableStateOf(resolvedProfile.branchName) }
    var isIfscLoading by remember { mutableStateOf(false) }
    var ifscVerifiedMessage by remember { mutableStateOf("") }
    var isPinLoading by remember { mutableStateOf(false) }
    var pinLookupMessage by remember { mutableStateOf("") }
    var pinLookupSuccess by remember { mutableStateOf(false) }
    var stateDropdownExpanded by remember { mutableStateOf(false) }
    var logoPath by remember { mutableStateOf(resolvedProfile.logoPath) }
    var uploadedSignaturePath by remember { mutableStateOf(resolvedProfile.signaturePath) }
    var termsAndConditions by remember {
        mutableStateOf(resolvedProfile.termsAndConditions.ifBlank { DEFAULT_TERMS_AND_CONDITIONS })
    }
    var submitAttempted by remember { mutableStateOf(false) }
    var locationDetectionMessage by remember { mutableStateOf("") }
    var isLocationDetecting by remember { mutableStateOf(false) }

    val businessNameFocusRequester = remember { FocusRequester() }
    val addressFocusRequester = remember { FocusRequester() }
    val pinFocusRequester = remember { FocusRequester() }
    val businessNameBringIntoViewRequester = remember { BringIntoViewRequester() }
    val addressBringIntoViewRequester = remember { BringIntoViewRequester() }
    val pinBringIntoViewRequester = remember { BringIntoViewRequester() }

    val missingFields = remember(businessName, address, pinCode) {
        missingRequiredBusinessFields(
            businessName = businessName,
            address = address,
            pinCode = pinCode
        )
    }

    val businessNameError = submitAttempted && missingFields.contains(RequiredBusinessField.BusinessName)
    val addressError = submitAttempted && missingFields.contains(RequiredBusinessField.Address)
    val pinError = submitAttempted && missingFields.contains(RequiredBusinessField.PinCode)

    fun focusFirstInvalidField(field: RequiredBusinessField) {
        profileScope.launch {
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

    suspend fun runLocationDetection() {
        isLocationDetecting = true
        val detectedState = detectGstStateInfoFromLocation(context)
        if (detectedState != null) {
            selectedStateInfo = detectedState
            locationDetectionMessage = "GST state code auto-detected via location."
        } else {
            locationDetectionMessage = ""
        }
        isLocationDetecting = false
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            profileScope.launch { runLocationDetection() }
        }
    }

    LaunchedEffect(pinCode) {
        if (pinCode.length == 6 && pinCode.all { it.isDigit() }) {
            delay(300)
            isPinLoading = true
            val result = fetchPinLookup(pinCode)
            isPinLoading = false
            if (result != null) {
                city = result.city
                pinLookupMessage = "City: ${result.city}, ${result.state}"
                pinLookupSuccess = true
                resolveIndianStateInfo(result.state)?.let { selectedStateInfo = it }
            } else {
                pinLookupMessage = "City not found, enter manually"
                pinLookupSuccess = false
            }
        } else {
            isPinLoading = false
            pinLookupMessage = ""
            pinLookupSuccess = false
        }
    }

    val signatureUploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            profileScope.launch {
                val ext = when {
                    context.contentResolver.getType(uri)?.contains("pdf") == true -> ".pdf"
                    else -> ".jpg"
                }
                val path = withContext(Dispatchers.IO) {
                    copyUriToInternalStorage(
                        context,
                        uri,
                        "business_signature_${System.currentTimeMillis()}$ext"
                    )
                }
                path?.let {
                    uploadedSignaturePath = it
                    Toast.makeText(context, "Signature uploaded!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = AppColors.screenBg,
        topBar = {
            TopAppBar(
                title = { Text("Business Profile", fontWeight = FontWeight.Bold, color = AppColors.textPrimary) },
                navigationIcon = {
                    if (!isDesktop) {
                        IconButton(onClick = onBackToMenu) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = AppColors.textPrimary
                            )
                        }
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
                .verticalScroll(formScroll)
                .imePadding()
                .padding(innerPadding)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.bringIntoViewRequester(businessNameBringIntoViewRequester)) {
                RetailTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = "Business Name *",
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(businessNameFocusRequester)
                        .testTag("edit_bname"),
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
                label = "Owner / Proprietor"
            )

            Column(modifier = Modifier.bringIntoViewRequester(addressBringIntoViewRequester)) {
                RetailTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Business Address *",
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(addressFocusRequester),
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
                        value = pinCode,
                        onValueChange = { pinCode = it.filter(Char::isDigit).take(6) },
                        label = "PIN Code *",
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(pinFocusRequester),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            if (isPinLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
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
                    modifier = Modifier.weight(1f)
                )
            }

            if (pinLookupMessage.isNotBlank()) {
                Text(
                    text = pinLookupMessage,
                    color = if (pinLookupSuccess) AppColors.primary else AppColors.textSecondary,
                    fontSize = 11.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    RetailTextField(
                        value = "${selectedStateInfo.first} (${selectedStateInfo.second})",
                        onValueChange = {},
                        label = "State & GST Code",
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AppColors.textSecondary,
                                modifier = Modifier.clickable { stateDropdownExpanded = true }
                            )
                        }
                    )
                    StateDropdownMenu(
                        expanded = stateDropdownExpanded,
                        onDismissRequest = { stateDropdownExpanded = false },
                        onStateSelected = {
                            selectedStateInfo = it
                            stateDropdownExpanded = false
                            locationDetectionMessage = ""
                        },
                        modifier = Modifier.background(AppColors.cardBg)
                    )
                }

                OutlinedButton(
                    onClick = {
                        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (hasFine || hasCoarse) {
                            profileScope.launch { runLocationDetection() }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier.height(56.dp)
                ) {
                    if (isLocationDetecting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Detect via Location", fontSize = 12.sp)
                    }
                }
            }

            if (locationDetectionMessage.isNotBlank()) {
                Text(
                    text = locationDetectionMessage,
                    color = AppColors.textSecondary,
                    fontSize = 11.sp
                )
            }

            RetailTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone Number",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            RetailTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Text(
                "GSTIN Number (Optional)",
                color = AppColors.labelText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = gstin,
                onValueChange = { value ->
                    val result = parseGstinInput(
                        value,
                        pan,
                        selectedStateInfo.first,
                        selectedStateInfo.second
                    )
                    gstin = result.gstin
                    gstinValid = result.valid
                    if (result.valid == true) {
                        pan = result.pan
                        result.stateName?.let { name ->
                            resolveIndianStateInfo(name)?.let { selectedStateInfo = it }
                        }
                    }
                },
                placeholder = { Text("15-digit GSTIN", color = AppColors.inputPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                colors = zeroBookInputColors(),
                singleLine = true
            )
            GstinValidationFeedback(
                gstin,
                gstinValid,
                pan,
                selectedStateInfo.first,
                selectedStateInfo.second
            )

            RetailTextField(
                value = pan,
                onValueChange = { pan = it.uppercase() },
                label = "PAN Card Number"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text("BANK DETAILS SETUP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textSecondary)

            RetailTextField(
                value = accountNo,
                onValueChange = { accountNo = it.filter(Char::isDigit) },
                label = "Account Number",
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
                trailingIcon = {
                    if (isIfscLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    }
                }
            )

            if (ifscVerifiedMessage.isNotBlank()) {
                Text(ifscVerifiedMessage, color = AppColors.primary, fontSize = 12.sp)
            }

            RetailTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = "Bank Name"
            )

            RetailTextField(
                value = bankBranch,
                onValueChange = { bankBranch = it },
                label = "Bank Branch"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text("BRANDING & INVOICE IMAGES", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textSecondary)

            BusinessImageUploadSection(
                title = "Business Logo",
                subtitle = "Appears on invoice header. PNG or JPG recommended.",
                currentPath = logoPath,
                onImageSelected = { uri ->
                    profileScope.launch {
                        val path = withContext(Dispatchers.IO) {
                            copyUriToInternalStorage(
                                context,
                                uri,
                                "business_logo_${System.currentTimeMillis()}.jpg"
                            )
                        }
                        path?.let { logoPath = it }
                    }
                }
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                border = BorderStroke(1.dp, AppColors.border),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Authorized Signature",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextDark
                    )
                    Text(
                        "Upload a photo of your signature",
                        fontSize = 11.sp,
                        color = AppColors.textSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )
                    val signatureFile = uploadedSignaturePath?.let { java.io.File(it) }
                    if (signatureFile != null && signatureFile.exists() && !signatureFile.extension.equals("pdf", ignoreCase = true)) {
                        AsyncImage(
                            model = signatureFile,
                            contentDescription = "Signature preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.screenBg),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    signatureUploadLauncher.launch(arrayOf("image/*", "application/pdf"))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Change", color = TextDark, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { uploadedSignaturePath = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Remove", color = AppColors.error, fontSize = 12.sp)
                            }
                        }
                    } else if (signatureFile != null && signatureFile.exists()) {
                        Text("PDF signature uploaded: ${signatureFile.name}", color = TextDark, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    signatureUploadLauncher.launch(arrayOf("image/*", "application/pdf"))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Change", color = TextDark, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { uploadedSignaturePath = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Remove", color = AppColors.error, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                signatureUploadLauncher.launch(arrayOf("image/*", "application/pdf"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                        ) {
                            Text("Upload Signature", color = AppColors.textOnPrimary)
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text("INVOICE SETTINGS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textSecondary)
            RetailTextField(
                value = termsAndConditions,
                onValueChange = { termsAndConditions = it },
                label = "Terms & Conditions / Declaration",
                placeholder = "Enter your default terms and conditions that will appear on every invoice",
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )

            if (missingFields.isNotEmpty()) {
                Text(
                    text = "\u26A0 Pending: ${missingFields.joinToString(", ") { it.label }}",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    submitAttempted = true
                    val firstInvalidField = missingFields.firstOrNull()
                    if (firstInvalidField != null) {
                        focusFirstInvalidField(firstInvalidField)
                    } else {
                        val nextProfile = resolvedProfile.copy(
                            businessName = businessName,
                            ownerName = ownerName,
                            address = address,
                            city = city,
                            pin = pinCode,
                            state = selectedStateInfo.first,
                            stateCode = selectedStateInfo.second,
                            phone = phone,
                            email = email,
                            gstin = gstin,
                            pan = pan,
                            bankName = bankName,
                            accountNo = accountNo,
                            ifsc = ifsc,
                            branchName = bankBranch,
                            logoPath = logoPath,
                            signaturePath = uploadedSignaturePath,
                            termsAndConditions = termsAndConditions.ifBlank { DEFAULT_TERMS_AND_CONDITIONS }
                        )
                        viewModel.updateProfile(nextProfile) {
                            Toast.makeText(context, "Business Profile successfully updated!", Toast.LENGTH_SHORT).show()
                            onBackToMenu()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_business_profile"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text("Save Alterations Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}
