package com.zerobook.app.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Email
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerobook.app.R
import com.zerobook.app.data.*
import com.zerobook.app.services.ExportStorageManager
import com.zerobook.app.services.ExportTarget
import com.zerobook.app.services.CsvTransferManager
import com.zerobook.app.ui.AppViewModel
import com.zerobook.app.ui.theme.AppColors
import com.zerobook.app.ui.theme.Colors
import com.zerobook.app.ui.theme.GstinValidationFeedback
import com.zerobook.app.ui.theme.LocalAppTheme
import com.zerobook.app.ui.theme.ThemeViewModel
import com.zerobook.app.ui.theme.parseGstinInput
import com.zerobook.app.ui.theme.*
import com.zerobook.app.ui.animation.pressScale
import com.zerobook.app.ui.animation.premiumClickable
import com.zerobook.app.utils.copyUriToInternalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.time.LocalDateTime
import java.time.LocalTime
import com.zerobook.app.BuildConfig

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: AppViewModel,
    themeViewModel: ThemeViewModel,
    isDesktop: Boolean = false,
    navigateToProducts: () -> Unit,
    navigateToLedgerBooks: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val origProfile by viewModel.profile.collectAsState()
    val currentTheme by themeViewModel.currentTheme.collectAsState()
    val vouchersForExport by viewModel.vouchers.collectAsState(initial = emptyList())
    val partiesForExport by viewModel.parties.collectAsState(initial = emptyList())
    val productsForExport by viewModel.products.collectAsState(initial = emptyList())
    val ledgerForExport by viewModel.ledgerEntries.collectAsState(initial = emptyList())
    val importCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                val summary = withContext(Dispatchers.IO) {
                    CsvTransferManager.importCsv(
                        context = context,
                        uri = uri,
                        financialYearCode = viewModel.financialYear.value
                    )
                }
                Toast.makeText(
                    context,
                    "Imported ${summary.importedCount} records, ${summary.skippedCount} skipped",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    var activeSubMode by remember { mutableStateOf(if (isDesktop) "BUSINESS" else "MENU") }
    var changelogData by remember { mutableStateOf<ChangelogData?>(null) }
    var showChangelogDialog by remember { mutableStateOf(false) }

    LaunchedEffect(context) {
        changelogData = ChangelogLoader.load(context)
    }

    val exportCsv: () -> Unit = {
        try {
            CsvTransferManager.exportAll(
                context = context,
                vouchers = vouchersForExport,
                parties = partiesForExport,
                products = productsForExport,
                ledgerEntries = ledgerForExport,
                partyLookup = partiesForExport.associate { it.id to it.name },
                financialYearLabel = viewModel.financialYear.value
            )
            Toast.makeText(context, "Exported to Downloads/ZeroBook/", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    val importCsv: () -> Unit = {
        importCsvLauncher.launch(arrayOf("text/*", "text/csv", "application/csv", "*/*"))
    }

    @Composable
    fun DetailContent() {
        if (activeSubMode == "BUSINESS") {
            BusinessProfileSettingsSection(
                viewModel = viewModel,
                profile = origProfile,
                isDesktop = isDesktop,
                onBackToMenu = { activeSubMode = "MENU" }
            )
            return
        }
        if (activeSubMode == "CUSTOMIZE") {
            ProgressTrackerSettingsScreen(
                onBackToMenu = { activeSubMode = "MENU" }
            )
            return
        }
        if (false && activeSubMode == "BUSINESS") {
            // Business Profile Form Editor
            val profile = origProfile ?: BusinessProfile(
                businessName = "", ownerName = "", address = "", city = "", state = "West Bengal", stateCode = "19",
                pin = "", phone = "", email = "", gstin = "", pan = "", bankName = "", accountNo = "", ifsc = "",
                logoPath = null, signaturePath = null
            )

            var bName by remember { mutableStateOf(profile.businessName) }
            var owner by remember { mutableStateOf(profile.ownerName) }
            var address by remember { mutableStateOf(profile.address) }
            var city by remember { mutableStateOf(profile.city) }
            var pinCode by remember { mutableStateOf(profile.pin) }
            var selectedStateInfo by remember { mutableStateOf(Utils.INDIAN_STATES.find { it.first == profile.state } ?: Utils.INDIAN_STATES[18]) }
            var phone by remember { mutableStateOf(profile.phone) }
            var email by remember { mutableStateOf(profile.email) }
            var gstin by remember { mutableStateOf(profile.gstin) }
            var gstinValid by remember { mutableStateOf<Boolean?>(null) }
            var pan by remember { mutableStateOf(profile.pan) }
            var bankName by remember { mutableStateOf(profile.bankName) }
            var accountNo by remember { mutableStateOf(profile.accountNo) }
            var ifsc by remember { mutableStateOf(profile.ifsc) }
            var bankBranch by remember { mutableStateOf(profile.branchName) }
            var isIfscLoading by remember { mutableStateOf(false) }
            var ifscVerifiedMessage by remember { mutableStateOf("") }
            var isPinLoading by remember { mutableStateOf(false) }
            var pinLookupMessage by remember { mutableStateOf("") }
            var pinLookupSuccess by remember { mutableStateOf(false) }
            var stateDropdownExpanded by remember { mutableStateOf(false) }
            var logoPath by remember { mutableStateOf(profile.logoPath) }
            var uploadedSignaturePath by remember { mutableStateOf(profile.signaturePath) }
            var termsAndConditions by remember {
                mutableStateOf(profile.termsAndConditions.ifBlank { DEFAULT_TERMS_AND_CONDITIONS })
            }
            val profileScope = rememberCoroutineScope()
            val formScroll = rememberScrollState()

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
                        Utils.INDIAN_STATES.find {
                            it.second == result.stateCode || it.first.equals(result.state, ignoreCase = true)
                        }?.let { selectedStateInfo = it }
                    } else {
                        pinLookupMessage = "City not found — enter manually"
                        pinLookupSuccess = false
                    }
                } else {
                    isPinLoading = false
                    pinLookupMessage = ""
                    pinLookupSuccess = false
                }
            }

            val signatureUploadLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    profileScope.launch {
                        val ext = when {
                            context.contentResolver.getType(uri)?.contains("pdf") == true -> ".pdf"
                            else -> ".jpg"
                        }
                        val path = withContext(kotlinx.coroutines.Dispatchers.IO) {
                            com.zerobook.app.utils.copyUriToInternalStorage(
                                context, uri,
                                "business_signature_${System.currentTimeMillis()}$ext"
                            )
                        }
                        path?.let {
                            uploadedSignaturePath = it
                            android.widget.Toast.makeText(context, "Signature uploaded!", android.widget.Toast.LENGTH_SHORT).show()
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
                            IconButton(onClick = { activeSubMode = "MENU" }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
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
                RetailTextField(
                    value = bName,
                    onValueChange = { bName = it },
                    label = "Business Name *",
                    modifier = Modifier.fillMaxWidth().testTag("edit_bname")
                )

                RetailTextField(
                    value = owner,
                    onValueChange = { owner = it },
                    label = "Owner / Proprietor *"
                )

                RetailTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Business Address *"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RetailTextField(
                        value = pinCode,
                        onValueChange = { pinCode = it.filter(Char::isDigit).take(6) },
                        label = "PIN Code *",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = {
                            if (isPinLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            }
                        }
                    )

                    RetailTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = "City *",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (pinLookupMessage.isNotBlank()) {
                    Text(
                        text = pinLookupMessage,
                        color = if (pinLookupSuccess) Color(0xFF2E7D32) else Color.Gray,
                        fontSize = 11.sp
                    )
                }

                // State Selector Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    RetailTextField(
                        value = "${selectedStateInfo.first} (${selectedStateInfo.second})",
                        onValueChange = {},
                        label = "State Code Dropdown *",
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
                    DropdownMenu(
                        expanded = stateDropdownExpanded,
                        onDismissRequest = { stateDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(240.dp)
                            .background(AppColors.cardBg)
                    ) {
                        Utils.INDIAN_STATES.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.first, color = AppColors.textPrimary) },
                                onClick = {
                                    selectedStateInfo = item
                                    stateDropdownExpanded = false
                                },
                                colors = MenuDefaults.itemColors(textColor = AppColors.textPrimary)
                            )
                        }
                    }
                }

                RetailTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone Number *",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                RetailTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address *",
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
                            value, pan, selectedStateInfo.first, selectedStateInfo.second
                        )
                        gstin = result.gstin
                        gstinValid = result.valid
                        if (result.valid == true) {
                            pan = result.pan
                            result.stateName?.let { name ->
                                Utils.INDIAN_STATES.find { it.first == name }
                                    ?.let { selectedStateInfo = it }
                            }
                        }
                    },
                    placeholder = { Text("15-digit GSTIN", color = AppColors.inputPlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = com.zerobook.app.ui.theme.zeroBookInputColors(),
                    singleLine = true
                )
                GstinValidationFeedback(
                    gstin, gstinValid, pan, selectedStateInfo.first, selectedStateInfo.second
                )

                RetailTextField(
                    value = pan,
                    onValueChange = { pan = it.uppercase() },
                    label = "PAN Card Number (Optional)"
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("BANK DETAILS SETUP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textSecondary)

                RetailTextField(
                    value = accountNo,
                    onValueChange = { accountNo = it.filter(Char::isDigit) },
                    label = "Account Number *",
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
                                    }
                                }
                            }
                        }
                    },
                    label = "IFSC Code *",
                    trailingIcon = {
                        if (isIfscLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        }
                    }
                )

                if (ifscVerifiedMessage.isNotBlank()) {
                    Text(ifscVerifiedMessage, color = Color(0xFF2E7D32), fontSize = 12.sp)
                }

                RetailTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = "Bank Name *"
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
                                    context, uri,
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
                            color = com.zerobook.app.ui.theme.TextDark
                        )
                        Text(
                            "Upload a photo of your signature",
                            fontSize = 11.sp,
                            color = AppColors.textSecondary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )
                        val sigFile = uploadedSignaturePath?.let { java.io.File(it) }
                        if (sigFile != null && sigFile.exists() && !sigFile.extension.equals("pdf", ignoreCase = true)) {
                            coil.compose.AsyncImage(
                                model = sigFile,
                                contentDescription = "Signature preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, AppColors.border, RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        signatureUploadLauncher.launch(arrayOf("image/*", "application/pdf"))
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Change", color = com.zerobook.app.ui.theme.TextDark, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { uploadedSignaturePath = null },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Remove", color = AppColors.error, fontSize = 12.sp)
                                }
                            }
                        } else if (sigFile != null && sigFile.exists()) {
                            Text("PDF signature uploaded: ${sigFile.name}", color = com.zerobook.app.ui.theme.TextDark, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    signatureUploadLauncher.launch(arrayOf("image/*", "application/pdf"))
                                }, modifier = Modifier.weight(1f)) {
                                    Text("Change", color = com.zerobook.app.ui.theme.TextDark, fontSize = 12.sp)
                                }
                                OutlinedButton(onClick = { uploadedSignaturePath = null }, modifier = Modifier.weight(1f)) {
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

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (bName.isBlank() || owner.isBlank() || address.isBlank() || city.isBlank() || phone.isBlank() || bankName.isBlank() || accountNo.isBlank() || ifsc.isBlank()) {
                            Toast.makeText(context, "Please fill in all required fields accurately", Toast.LENGTH_SHORT).show()
                        } else if (gstin.isNotBlank() && gstin.length != 15) {
                            Toast.makeText(context, "GSTIN must be exactly 15 characters long if provided", Toast.LENGTH_SHORT).show()
                        } else {
                            val nextProfile = profile.copy(
                                businessName = bName, ownerName = owner, address = address, city = city,
                                pin = pinCode,
                                state = selectedStateInfo.first, stateCode = selectedStateInfo.second,
                                phone = phone, email = email, gstin = gstin, pan = pan,
                                bankName = bankName, accountNo = accountNo, ifsc = ifsc, branchName = bankBranch,
                                logoPath = logoPath,
                                signaturePath = uploadedSignaturePath,
                                termsAndConditions = termsAndConditions.ifBlank { DEFAULT_TERMS_AND_CONDITIONS }
                            )
                            viewModel.updateProfile(nextProfile) {
                                Toast.makeText(context, "Business Profile successfully updated!", Toast.LENGTH_SHORT).show()
                                activeSubMode = "MENU"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_business_profile"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Save Alterations Settings", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else if (activeSubMode == "LOCK") {
        // App security control
        val sp = context.getSharedPreferences("zerobook_pref", Context.MODE_PRIVATE)
        var pinEnabled by remember { mutableStateOf(sp.getBoolean("pin_enabled", false)) }
        var enteredPin by remember { mutableStateOf(sp.getString("lock_pin", "") ?: "") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("App Pin Protection Lock", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        if (!isDesktop) {
                            IconButton(onClick = { activeSubMode = "MENU" }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PIN Code Protection Toggle", fontWeight = FontWeight.Bold)
                        Text("Enable security verification on launching ZeroBook", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = pinEnabled,
                        onCheckedChange = {
                            pinEnabled = it
                            sp.edit().putBoolean("pin_enabled", it).apply()
                        }
                    )
                }

                if (pinEnabled) {
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 4) {
                                enteredPin = it
                                sp.edit().putString("lock_pin", it).apply()
                            }
                        },
                        label = { Text("Initialize 4-digit security PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("app_pin_setup"),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Text("Current stored code will be prompted next time you trigger the app.", fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        activeSubMode = "MENU"
                        Toast.makeText(context, "Security configurations recorded", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Apply Parameters")
                }
            }
        }
    } else if (activeSubMode == "ABOUT") {
        // About & compliance view
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("About ZeroBook Detail", fontWeight = FontWeight.Bold, color = AppColors.textPrimary) },
                    navigationIcon = {
                        if (!isDesktop) {
                            IconButton(onClick = { activeSubMode = "MENU" }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
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
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_icon),
                    contentDescription = "ZeroBook",
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "ZeroBook",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                Text(
                    text = "Record. Transact. Grow.",
                    fontSize = 13.sp,
                    color = AppColors.textSecondary
                )
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    fontSize = 12.sp,
                    color = AppColors.textSecondary
                )
                Text(
                    text = "Built for Indian small retailers",
                    fontSize = 11.sp,
                    color = AppColors.textSecondary
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showChangelogDialog = true },
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                    border = BorderStroke(1.dp, AppColors.border),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Change Log", fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
                            Text(
                                "View the full release history",
                                fontSize = 11.sp,
                                color = AppColors.textSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = AppColors.textTertiary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    } else if (activeSubMode == "THEME") {
            Scaffold(
                containerColor = AppColors.screenBg,
                topBar = {
                    TopAppBar(
                        title = { Text("Theme & Colors", fontWeight = FontWeight.Bold, color = AppColors.textPrimary) },
                        navigationIcon = {
                            if (!isDesktop) {
                                IconButton(onClick = { activeSubMode = "MENU" }) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
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
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                        border = BorderStroke(1.dp, AppColors.border),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "App Appearance",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AppColors.textPrimary
                            )
                            Text(
                                text = "Pick a theme and the whole app updates immediately.",
                                fontSize = 12.sp,
                                color = AppColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ThemePickerRow(
                                selectedThemeName = currentTheme.name,
                                onThemeSelected = themeViewModel::setTheme
                            )
                        }
                    }
                }
            }
    } else if (activeSubMode == "FY") {
        val context = LocalContext.current
        val profile = origProfile ?: BusinessProfile(
            businessName = "", ownerName = "", address = "", city = "", state = "West Bengal", stateCode = "19",
            pin = "", phone = "", email = "", gstin = "", pan = "", bankName = "", accountNo = "", ifsc = "",
            logoPath = null, signaturePath = null
        )
        val currentFyLabel = viewModel.financialYear.collectAsState().value
        var fyStartYearText by remember(currentFyLabel) {
            mutableStateOf(currentFyLabel.substringBefore("-"))
        }
        val parsedStartYear = fyStartYearText.toIntOrNull()
        val calculatedEndYear = parsedStartYear?.plus(1)
        val calculatedFyLabel = remember(parsedStartYear) {
            parsedStartYear?.let { String.format("%04d-%02d", it, (it + 1) % 100) }.orEmpty()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Financial Year Control", fontWeight = FontWeight.Bold, color = AppColors.textPrimary) },
                    navigationIcon = {
                        if (!isDesktop) {
                            IconButton(onClick = { activeSubMode = "MENU" }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
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
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppColors.border, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FINANCIAL YEAR CONFIGURATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.primary,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Active Financial Year",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary
                        )

                        RetailTextField(
                            value = fyStartYearText,
                            onValueChange = { input ->
                                fyStartYearText = input.filter(Char::isDigit).take(4)
                            },
                            label = "Start Year",
                            placeholder = "e.g. 2025",
                            modifier = Modifier.width(220.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AppColors.primary.copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, AppColors.primary.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Current active year: $currentFyLabel", fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
                                Text("End year: ${calculatedEndYear?.toString() ?: "-"}", fontSize = 13.sp, color = AppColors.textSecondary)
                                Text("FY Label: ${calculatedFyLabel.ifBlank { "-" }}", fontSize = 13.sp, color = AppColors.textSecondary)
                                Text("The app will auto-advance the financial year after 31 March when needed. Only the start year is editable here.", fontSize = 12.sp, color = AppColors.textSecondary)
                            }
                        }
                    }
                }
                
                Button(
                    onClick = {
                        if (parsedStartYear == null || fyStartYearText.length != 4) {
                            Toast.makeText(context, "Enter a valid 4-digit financial year start", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.switchFinancialYear(calculatedFyLabel) {
                                viewModel.updateProfile(profile.copy(fyLabel = calculatedFyLabel)) {}
                                Toast.makeText(context, "Active financial year switched to $calculatedFyLabel", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                ) {
                    Text("Save Financial Year", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    } else if (activeSubMode == "EMAIL") {
        EmailAutomationSection(viewModel = viewModel)
        /*
        var automationEnabled by remember { mutableStateOf(sp.getBoolean("email_automation_enabled", false)) }
        var triggerTimeText by remember { mutableStateOf(sp.getString("email_trigger_time", "09:00 AM") ?: "09:00 AM") }
        var scheduledAt by remember { mutableStateOf(EmailReminderScheduler.loadScheduledAt(context)) }
        var isRunningSim by remember { mutableStateOf(false) }
        
        var logString by remember { mutableStateOf(sp.getString("email_logs_list", "") ?: "") }
        val logsList = remember(logString) {
            if (logString.isBlank()) emptyList<String>() else logString.split("\n")
        }

        val parties by viewModel.parties.collectAsState()
        val bills by viewModel.billsReceivable.collectAsState()
        val scope = rememberCoroutineScope()
        val senderEmail = origProfile?.email?.ifBlank { "yourcompany@example.com" } ?: "yourcompany@example.com"
        var smtpEmail by remember(origProfile?.smtpEmail) { mutableStateOf(origProfile?.smtpEmail.orEmpty()) }
        var smtpPassword by remember(origProfile?.smtpPassword) { mutableStateOf(origProfile?.smtpPassword.orEmpty()) }
        var smtpHost by remember(origProfile?.smtpHost) { mutableStateOf(origProfile?.smtpHost?.ifBlank { "smtp.gmail.com" } ?: "smtp.gmail.com") }
        var smtpPort by remember(origProfile?.smtpPort) { mutableStateOf(origProfile?.smtpPort?.ifBlank { "587" } ?: "587") }
        var useCustomTemplate by remember { mutableStateOf(EmailReminderScheduler.isUsingCustomTemplate(context)) }
        var templateText by remember { mutableStateOf(EmailReminderScheduler.loadTemplate(context)) }
        val notificationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Toast.makeText(context, "Notification permission is needed for due reminder alerts", Toast.LENGTH_SHORT).show()
            }
        }
        val eligibleRecipients = remember(parties, bills) {
            bills
                .filter { it.outstandingAmount > 0.0 }
                .groupBy { it.partyId }
                .mapNotNull { (partyId, groupedBills) ->
                    val party = parties.find { it.id == partyId } ?: return@mapNotNull null
                    if (party.email.isBlank()) return@mapNotNull null
                    ReminderRecipientUi(
                        partyId = party.id,
                        partyName = party.name,
                        email = party.email,
                        dueAmount = groupedBills.sumOf { it.outstandingAmount }
                    )
                }
                .sortedBy { it.partyName.lowercase() }
        }
        var refreshSchedulesToken by remember { mutableStateOf(0) }
        val individualSchedules = remember(refreshSchedulesToken) {
            EmailReminderScheduler.getSchedules(context)
                .filter { it.reminderType == "INDIVIDUAL" }
                .associateBy { it.partyId }
        }
        val universalSchedule = remember(refreshSchedulesToken) {
            EmailReminderScheduler.getUniversalSchedule(context)
        }
        var recurringIntervalDays by remember(universalSchedule?.intervalDays) {
            mutableStateOf(universalSchedule?.intervalDays?.takeIf { it > 0 } ?: 7)
        }
        var recurringTime24h by remember(universalSchedule?.scheduledTime) {
            mutableStateOf(universalSchedule?.scheduledTime?.takeIf { it.isNotBlank() } ?: "09:00")
        }
        val scheduleSummary = universalSchedule?.takeIf { it.isActive }?.let {
            "${it.scheduledDate} ${it.scheduledTime}"
        }.orEmpty()

        val saveSettings = {
            sp.edit()
                .putBoolean("email_automation_enabled", automationEnabled)
                .putString("email_trigger_time", triggerTimeText)
                .apply()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Email Automation System", fontWeight = FontWeight.Bold, color = AppColors.textPrimary) },
                    navigationIcon = {
                        if (!isDesktop) {
                            IconButton(onClick = { activeSubMode = "MENU" }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
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
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppColors.border, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("AUTOMATIC REMINDERS", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.primary)
                                val subText = if (automationEnabled) "SERVICE STATUS: ACTIVE (Running)" else "SERVICE STATUS: SHUT (Disabled)"
                                Text(subText, fontSize = 11.sp, color = if (automationEnabled) AppColors.credit else AppColors.error)
                            }
                            Switch(
                                checked = automationEnabled,
                                onCheckedChange = {
                                    automationEnabled = it
                                    if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    saveSettings()
                                    if (!it) {
                                        scheduledAt = null
                                        EmailReminderScheduler.schedule(context, null)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AppColors.primary,
                                    checkedTrackColor = AppColors.primary.copy(alpha = 0.38f)
                                )
                            )
                        }

                        HorizontalDivider()

                        Text("Email Template", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Use custom message", fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
                                Text(
                                    if (useCustomTemplate) "Your custom template will be used for reminders"
                                    else "Default reminder preview is shown below",
                                    fontSize = 11.sp,
                                    color = AppColors.textSecondary
                                )
                            }
                            Switch(
                                checked = useCustomTemplate,
                                onCheckedChange = {
                                    useCustomTemplate = it
                                    EmailReminderScheduler.setUseCustomTemplate(context, it)
                                }
                            )
                        }
                        OutlinedTextField(
                            value = EmailReminderScheduler.previewTemplate(
                                template = templateText,
                                businessName = origProfile?.businessName.orEmpty().ifBlank { "ZeroBook" },
                                businessPhone = origProfile?.phone.orEmpty().ifBlank { "9876543210" }
                            ),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Preview") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.primary,
                                unfocusedBorderColor = AppColors.border
                            )
                        )
                        if (useCustomTemplate) {
                            OutlinedTextField(
                                value = templateText,
                                onValueChange = {
                                    templateText = it
                                    EmailReminderScheduler.saveTemplate(context, it)
                                },
                                label = { Text("Custom message") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 6,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.primary,
                                    unfocusedBorderColor = AppColors.border
                                )
                            )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "{party_name}",
                                "{invoice_no}",
                                "{amount}",
                                "{due_date}",
                                "{outstanding}",
                                "{business_name}",
                                "{business_phone}"
                            ).forEach { variable ->
                                AssistChip(
                                    onClick = {
                                        templateText += if (templateText.endsWith(" ") || templateText.isBlank()) variable else " $variable"
                                        EmailReminderScheduler.saveTemplate(context, templateText)
                                    },
                                    label = { Text(variable) }
                                )
                            }
                        }

                        Text("Sender", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textSecondary)
                        OutlinedTextField(
                            value = senderEmail,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Authenticated company email") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.primary,
                                unfocusedBorderColor = AppColors.border
                            )
                        )

                        HorizontalDivider()

                        Text("SMTP Settings", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textSecondary)
                        OutlinedTextField(
                            value = smtpEmail,
                            onValueChange = { smtpEmail = it },
                            label = { Text("Sender Email") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.primary,
                                unfocusedBorderColor = AppColors.border
                            )
                        )
                        OutlinedTextField(
                            value = smtpPassword,
                            onValueChange = { smtpPassword = it },
                            label = { Text("App Password") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.primary,
                                unfocusedBorderColor = AppColors.border
                            )
                        )
                        Text(
                            "Use Gmail App Password, not your main password. Enable 2FA first. How to get Gmail App Password",
                            fontSize = 11.sp,
                            color = AppColors.textSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = smtpHost,
                                onValueChange = { smtpHost = it },
                                label = { Text("SMTP Host") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.primary,
                                    unfocusedBorderColor = AppColors.border
                                )
                            )
                            OutlinedTextField(
                                value = smtpPort,
                                onValueChange = { smtpPort = it.filter(Char::isDigit) },
                                label = { Text("SMTP Port") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.primary,
                                    unfocusedBorderColor = AppColors.border
                                )
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    val profile = origProfile
                                    if (profile == null) {
                                        Toast.makeText(context, "Business profile not available", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.updateProfile(
                                            profile.copy(
                                                smtpEmail = smtpEmail.trim(),
                                                smtpPassword = smtpPassword,
                                                smtpHost = smtpHost.trim().ifBlank { "smtp.gmail.com" },
                                                smtpPort = smtpPort.trim().ifBlank { "587" }
                                            )
                                        ) {
                                            Toast.makeText(context, "SMTP settings saved", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save SMTP")
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val testTo = smtpEmail.trim().ifBlank { senderEmail }
                                        val result = withContext(Dispatchers.IO) {
                                            EmailReminderScheduler.sendTestEmail(context, testTo)
                                        }
                                        Toast.makeText(
                                            context,
                                            if (result.isSuccess) "Test email sent" else "Test email failed: ${result.exceptionOrNull()?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Test Email")
                            }
                        }

                        HorizontalDivider()

                        Text("Per-Party Scheduling", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textSecondary)
                        if (eligibleRecipients.isEmpty()) {
                            Text(
                                "No parties currently have outstanding dues.",
                                fontSize = 11.sp,
                                color = AppColors.textSecondary
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                eligibleRecipients.forEach { recipient ->
                                    val partySchedule = individualSchedules[recipient.partyId]
                                    var partyConfig by remember(recipient.partyId) {
                                        mutableStateOf(EmailReminderScheduler.loadPartyReminderConfig(context, recipient.partyId))
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, AppColors.border, RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(recipient.partyName, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimary)
                                            Text(
                                                if (recipient.email.isBlank()) "No email saved for this party" else recipient.email,
                                                fontSize = 11.sp,
                                                color = AppColors.textSecondary
                                            )
                                            Text(
                                                partySchedule?.let { "Reminder set for ${it.scheduledDate} ${it.scheduledTime}" } ?: "No reminder set",
                                                fontSize = 11.sp,
                                                color = AppColors.textSecondary
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text("Send on due date", fontSize = 11.sp, color = AppColors.textSecondary)
                                                Switch(
                                                    checked = partyConfig.sendOnDueDate,
                                                    onCheckedChange = {
                                                        partyConfig = partyConfig.copy(sendOnDueDate = it)
                                                        EmailReminderScheduler.savePartyReminderConfig(context, recipient.partyId, partyConfig)
                                                    }
                                                )
                                            }
                                            OutlinedTextField(
                                                value = partyConfig.daysBeforeDue.toString(),
                                                onValueChange = {
                                                    partyConfig = partyConfig.copy(daysBeforeDue = it.filter(Char::isDigit).ifBlank { "0" }.toInt())
                                                    EmailReminderScheduler.savePartyReminderConfig(context, recipient.partyId, partyConfig)
                                                },
                                                label = { Text("Send X days before due date") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                            )
                                            OutlinedTextField(
                                                value = partyConfig.remindAfterDueEveryDays.toString(),
                                                onValueChange = {
                                                    partyConfig = partyConfig.copy(remindAfterDueEveryDays = it.filter(Char::isDigit).ifBlank { "1" }.toInt())
                                                    EmailReminderScheduler.savePartyReminderConfig(context, recipient.partyId, partyConfig)
                                                },
                                                label = { Text("Send reminder every X days after due") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text("Until paid", fontSize = 11.sp, color = AppColors.textSecondary)
                                                Switch(
                                                    checked = partyConfig.untilPaid,
                                                    onCheckedChange = {
                                                        partyConfig = partyConfig.copy(untilPaid = it)
                                                        EmailReminderScheduler.savePartyReminderConfig(context, recipient.partyId, partyConfig)
                                                    }
                                                )
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                Utils.formatIndianCurrency(recipient.dueAmount),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.primary
                                            )
                                            TextButton(
                                                onClick = {
                                                    val partyBills = bills.filter { it.partyId == recipient.partyId && it.outstandingAmount > 0.0 }
                                                    val parsedTime = runCatching { LocalTime.parse(recurringTime24h) }.getOrDefault(LocalTime.of(9, 0))
                                                    EmailReminderScheduler.schedulePartyPlan(
                                                        context = context,
                                                        partyId = recipient.partyId,
                                                        partyName = recipient.partyName,
                                                        bills = partyBills,
                                                        config = partyConfig,
                                                        sendTime = parsedTime
                                                    )
                                                    refreshSchedulesToken++
                                                    Toast.makeText(context, "Schedule saved for ${recipient.partyName}", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Text("Save schedule", color = AppColors.primary)
                                            }
                                            TextButton(
                                                onClick = {
                                                    scope.launch {
                                                        val result = withContext(Dispatchers.IO) {
                                                            EmailReminderScheduler.sendNowForParty(context, recipient.partyId)
                                                        }
                                                        Toast.makeText(
                                                            context,
                                                            if (result.isSuccess) "Mail app opened for ${recipient.partyName}" else "Send now failed: ${result.exceptionOrNull()?.message}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            ) {
                                                Text("Send now", color = AppColors.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider()

                        Text("Universal Recurring Reminder", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textSecondary)
                        Text("Send automatic recurring reminders", fontSize = 11.sp, color = AppColors.textSecondary)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(2, 3, 7, 15, 30).forEach { intervalDays ->
                                FilterChip(
                                    selected = recurringIntervalDays == intervalDays,
                                    onClick = { recurringIntervalDays = intervalDays },
                                    label = { Text("Every $intervalDays days") }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = recurringTime24h,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("09:00") },
                            singleLine = true,
                            label = { Text("Preferred send time") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.primary,
                                unfocusedBorderColor = AppColors.border
                            ),
                            trailingIcon = {
                                TextButton(
                                    onClick = {
                                        val calendar = Calendar.getInstance()
                                        TimePickerDialog(
                                            context,
                                            { _, hourOfDay, minute ->
                                                recurringTime24h = String.format("%02d:%02d", hourOfDay, minute)
                                            },
                                            calendar.get(Calendar.HOUR_OF_DAY),
                                            calendar.get(Calendar.MINUTE),
                                            false
                                        ).show()
                                    }
                                ) {
                                    Text("Pick", color = AppColors.primary)
                                }
                            }
                        )

                        Text("Reminder frequency after overdue", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.textSecondary)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Daily" to 1,
                                "Every 3 days" to 3,
                                "Weekly" to 7,
                                "Every 15 days" to 15,
                                "Monthly" to 30
                            ).forEach { (label, interval) ->
                                FilterChip(
                                    selected = recurringIntervalDays == interval,
                                    onClick = { recurringIntervalDays = interval },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Custom Option", fontSize = 11.sp, color = AppColors.textSecondary)
                                Text(
                                    text = if (scheduleSummary.isBlank()) "No reminder scheduled yet" else scheduleSummary,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textPrimary
                                )
                            }
                            TextButton(
                                onClick = {
                                    val parsedTime = runCatching { LocalTime.parse(recurringTime24h) }.getOrDefault(LocalTime.of(9, 0))
                                    EmailReminderScheduler.scheduleRecurringReminder(
                                        context = context,
                                        enabled = automationEnabled,
                                        intervalDays = recurringIntervalDays,
                                        sendTime = parsedTime
                                    )
                                    refreshSchedulesToken++
                                    Toast.makeText(
                                        context,
                                        if (automationEnabled) "Recurring reminders saved" else "Recurring reminders turned off",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            ) {
                                Text("Save recurring", color = AppColors.primary)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            isRunningSim = true
                            val sentCount = withContext(Dispatchers.IO) {
                                EmailReminderScheduler.processReminders(context)
                            }
                            logString = sp.getString("email_logs_list", "") ?: ""
                            Toast.makeText(
                                context,
                                if (sentCount > 0) "Reminder run completed for $sentCount recipients" else "No eligible reminders were sent",
                                Toast.LENGTH_SHORT
                            ).show()
                            isRunningSim = false
                            return@launch
                            kotlinx.coroutines.delay(1200) // simulated shoot reminder delay
                            val activeBills = bills.filter { it.outstandingAmount > 0.0 }
                            if (activeBills.isEmpty()) {
                                Toast.makeText(context, "No outstanding bills found to notify", Toast.LENGTH_SHORT).show()
                                val noDuesLog = "INFO: Scanned database | No outstanding bills with pending dues found."
                                val updatedLogs = if (logString.isEmpty()) noDuesLog else "$noDuesLog\n$logString"
                                sp.edit().putString("email_logs_list", updatedLogs).apply()
                                logString = updatedLogs
                            } else {
                                val addedLogs = mutableListOf<String>()
                                for (bill in activeBills) {
                                    val partyObj = parties.find { it.id == bill.partyId }
                                    val emailStr = if (partyObj?.email.isNullOrBlank()) "agarwal@test.com" else partyObj!!.email
                                    val amtFormatted = String.format("₹%,.0f", bill.outstandingAmount)
                                    val formattedTime = triggerTimeText.ifBlank { "09:00 AM" }
                                    val row = "SENT to $emailStr | Amt: $amtFormatted | Status: SUCCESS | Time: $formattedTime"
                                    addedLogs.add(row)
                                }
                                val joinedNewLog = addedLogs.joinToString("\n")
                                val finalLogs = if (logString.isEmpty()) joinedNewLog else "$joinedNewLog\n$logString"
                                sp.edit().putString("email_logs_list", finalLogs).apply()
                                logString = finalLogs
                                Toast.makeText(context, "Notifier process triggered: ${addedLogs.size} reminders sent!", Toast.LENGTH_SHORT).show()
                            }
                            isRunningSim = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary),
                    enabled = !isRunningSim
                ) {
                    if (isRunningSim) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Running reminders...", color = Color.White)
                    } else {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Run Reminder Now", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppColors.border, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Reminder Logs", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.textSecondary)
                            TextButton(onClick = {
                                sp.edit().putString("email_logs_list", "").apply()
                                logString = ""
                            }) {
                                Text("Clear", color = AppColors.error, fontSize = 11.sp)
                            }
                        }

                        HorizontalDivider()

                        if (logsList.isEmpty()) {
                            Text("No reminder runs yet. Use Run Reminder Now or a scheduled reminder above.", fontSize = 11.sp, color = AppColors.textTertiary)
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                logsList.forEach { logLine ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = AppColors.screenBg),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = logLine,
                                            fontSize = 11.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            color = AppColors.textPrimary,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        */
    }
}

    if (isDesktop) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.width(360.dp).fillMaxHeight()) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Application Settings", fontWeight = FontWeight.Bold, color = AppColors.textPrimary) },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.cardBg)
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        SettingsMenuSection(
                            viewModel = viewModel,
                            activeSubMode = activeSubMode,
                            onSelect = { activeSubMode = it },
                            openChangeLog = { showChangelogDialog = true },
                            exportCsv = exportCsv,
                            importCsv = importCsv,
                            context = context,
                            navigateToProducts = navigateToProducts,
                            navigateToLedgerBooks = navigateToLedgerBooks
                        )
                    }
                }
            }
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(AppColors.border))
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                DetailContent()
            }
        }
    } else {
        if (activeSubMode == "MENU") {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Application Settings", fontWeight = FontWeight.Bold, color = AppColors.textPrimary) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.cardBg)
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    SettingsMenuSection(
                        viewModel = viewModel,
                        activeSubMode = activeSubMode,
                        onSelect = { activeSubMode = it },
                        openChangeLog = { showChangelogDialog = true },
                        exportCsv = exportCsv,
                        importCsv = importCsv,
                        context = context,
                        navigateToProducts = navigateToProducts,
                        navigateToLedgerBooks = navigateToLedgerBooks
                    )
                }
            }
        } else {
            DetailContent()
        }
    }

    if (showChangelogDialog && changelogData != null) {
        AlertDialog(
            onDismissRequest = { showChangelogDialog = false },
            confirmButton = {
                Button(onClick = { showChangelogDialog = false }) {
                    Text("Close")
                }
            },
            title = {
                Text("Change Log")
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    changelogData?.history?.forEach { entry ->
                        Text(
                            text = "Version ${entry.version}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        entry.changes.forEach { change ->
                            Text(
                                text = "• $change",
                                color = Color(0xFF111827)
                            )
                        }
                    }
                }
            },
            containerColor = Color.White,
            textContentColor = Color(0xFF111827),
            titleContentColor = Color(0xFF111827)
        )
    }
}

@Composable
fun SettingsMenuSection(
    viewModel: AppViewModel,
    activeSubMode: String,
    onSelect: (String) -> Unit,
    openChangeLog: () -> Unit,
    exportCsv: () -> Unit,
    importCsv: () -> Unit,
    context: android.content.Context,
    navigateToProducts: () -> Unit,
    navigateToLedgerBooks: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.screenBg)
            .verticalScroll(scrollState)
            .imePadding()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Menu items
        SettingsMenuCard(
            title = "Edit Business Profile",
            description = "Update GSTIN, Address, PAN, signature, and bank details",
            icon = Icons.Default.Business,
            onClick = { onSelect("BUSINESS") }
        )

        SettingsMenuCard(
            title = "Manage Products Master",
            description = "Configure stock prices, units, and standard HSN codes",
            icon = Icons.Default.ShoppingBag,
            onClick = navigateToProducts
        )

        SettingsMenuCard(
                            title = "Ledger Books & Account Heads",
                            description = "View full ledger account list with balances and groups",
                            icon = Icons.Default.AccountBalance,
                            onClick = navigateToLedgerBooks
                        )

        SettingsMenuCard(
            title = "Customize",
            description = "Enable progress tracker and future customization controls",
            icon = Icons.Default.CheckCircle,
            onClick = { onSelect("CUSTOMIZE") }
        )

        SettingsMenuCard(
            title = "Theme & Colors",
            description = "Switch between Beach, Blue, Green, Purple, and Dark",
            icon = Icons.Default.CheckCircle,
            onClick = { onSelect("THEME") }
        )

        SettingsMenuCard(
            title = "Financial Year Control",
            description = "Configure custom financial year with auto-save and validations",
            icon = Icons.Default.DateRange,
            onClick = { onSelect("FY") }
        )

        SettingsMenuCard(
            title = "Change Log",
            description = "Read what changed in each ZeroBook release",
            icon = Icons.Default.Info,
            onClick = openChangeLog
        )

        SettingsMenuCard(
            title = "About ZeroBook",
            description = "Check compliance versions and regulatory details",
            icon = Icons.Default.Info,
            onClick = { onSelect("ABOUT") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Backup Section Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppColors.border, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("BACKUP & RESTORE DATA", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.textSecondary)
                Text(
                    "Export your complete SQLite database file directly as an encrypted local backup to safe-keep transaction ledgers.",
                    fontSize = 11.sp,
                    color = AppColors.textSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            exportCsv()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .pressScale(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.primary,
                            contentColor = AppColors.textOnPrimary
                        )
                    ) {
                        Text("Export to CSV", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { importCsv() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .pressScale()
                    ) {
                        Text("Import CSV", fontSize = 11.sp)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val result = viewModel.backupDatabase(context)
                            if (result != null) {
                                Toast.makeText(context, "Backup saved to ${result.locationLabel}", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Backup failed", Toast.LENGTH_LONG).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .pressScale()
                    ) {
                        Text("Backup Database", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressTrackerSettingsScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var enabled by remember { mutableStateOf(false) }
    var metric by remember { mutableStateOf("Sales") }
    var period by remember { mutableStateOf("Monthly") }
    var target by remember { mutableStateOf("200000") }

    LaunchedEffect(Unit) {
        enabled = AppPreferences.isProgressTrackerEnabled(context)
        metric = AppPreferences.getProgressTrackerMetric(context)
        period = AppPreferences.getProgressTrackerPeriod(context)
        target = AppPreferences.getProgressTrackerTarget(context)
    }

    Scaffold(
        containerColor = AppColors.screenBg,
        topBar = {
            TopAppBar(
                title = { Text("Customize", fontWeight = FontWeight.Bold, color = AppColors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackToMenu) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
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
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.border, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Progress Tracker", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.textPrimary)
                    Text("Enable a compact progress tracker on the dashboard and keep it connected to live accounting data.", fontSize = 12.sp, color = AppColors.textSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable / Disable", fontWeight = FontWeight.Medium, color = AppColors.textPrimary)
                        Switch(
                            checked = enabled,
                            onCheckedChange = {
                                enabled = it
                                scope.launch { AppPreferences.setProgressTrackerEnabled(context, it) }
                            }
                        )
                    }
                    OutlinedTextField(
                        value = metric,
                        onValueChange = { metric = it },
                        label = { Text("Metric") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = period,
                        onValueChange = { period = it },
                        label = { Text("Time") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it.filter(Char::isDigit) },
                        label = { Text("Target Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                AppPreferences.setProgressTrackerEnabled(context, enabled)
                                AppPreferences.setProgressTrackerMetric(context, metric.ifBlank { "Sales" })
                                AppPreferences.setProgressTrackerPeriod(context, period.ifBlank { "Monthly" })
                                AppPreferences.setProgressTrackerTarget(context, target.ifBlank { "200000" })
                                Toast.makeText(context, "Progress tracker settings saved", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary, contentColor = AppColors.textOnPrimary)
                    ) {
                        Text("Save")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.border, RoundedCornerShape(16.dp)).premiumClickable { },
                colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Other Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.textPrimary)
                    Text("Future customization options will be added here without changing the current production structure.", fontSize = 12.sp, color = AppColors.textSecondary)
                }
            }
        }
    }
}

@Composable
fun SettingsMenuCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .premiumClickable { onClick() }
            .border(1.dp, AppColors.border, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColors.primary.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AppColors.primary,
                        modifier = Modifier.padding(10.dp).size(24.dp)
                    )
                }
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.textPrimary)
                    Text(text = description, color = AppColors.textSecondary, fontSize = 11.sp)
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppColors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ThemePickerRow(
    selectedThemeName: String,
    onThemeSelected: (String) -> Unit
) {
    val themeOptions = listOf(
        Triple("BEACH", "Beach", Color(0xFFFDF6EC)),
        Triple("BLUE", "Blue", Color(0xFF1A73E8)),
        Triple("GREEN", "Green", Color(0xFF1E8A3C)),
        Triple("PURPLE", "Purple", Color(0xFF6200EA)),
        Triple("TEAL", "Teal", Color(0xFF0F9D8A))
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        themeOptions.forEach { (name, label, swatch) ->
            val isSelected = selectedThemeName == name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .premiumClickable { onThemeSelected(name) }
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(swatch)
                        .border(
                            width = if (name == "BEACH") 2.dp else 1.dp,
                            color = when {
                                isSelected -> AppColors.primary
                                name == "BEACH" -> Color(0xFF8D6E63)
                                else -> AppColors.border
                            },
                            shape = RoundedCornerShape(21.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = label,
                            tint = if (name == "BEACH") AppColors.textPrimary else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = AppColors.textSecondary
                )
            }
        }
    }
}
