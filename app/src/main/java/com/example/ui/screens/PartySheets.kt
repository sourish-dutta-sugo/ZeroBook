package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.filterDecimalInput
import com.example.data.Party
import com.example.ui.theme.AppColors
import com.example.ui.theme.GstinValidationFeedback
import com.example.ui.theme.parseGstinInput
import com.example.ui.theme.zeroBookInputColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePartyInlineSheet(
    partyType: String = "CUSTOMER",
    onSave: (Party) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(partyType) }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var pan by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var stateCode by remember { mutableStateOf("") }
    var openingBalance by remember { mutableStateOf("") }
    var balanceType by remember { mutableStateOf("DR") }
    var creditLimit by remember { mutableStateOf("0") }
    var creditDays by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var gstinValid by remember { mutableStateOf<Boolean?>(null) }
    var pinLoading by remember { mutableStateOf(false) }
    var pinLookupMessage by remember { mutableStateOf("") }
    var pinLookupSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val stateGstCodes = mapOf(
        "Jammu and Kashmir" to "01", "Himachal Pradesh" to "02", "Punjab" to "03",
        "Chandigarh" to "04", "Uttarakhand" to "05", "Haryana" to "06", "Delhi" to "07",
        "Rajasthan" to "08", "Uttar Pradesh" to "09", "Bihar" to "10", "Sikkim" to "11",
        "Arunachal Pradesh" to "12", "Nagaland" to "13", "Manipur" to "14", "Mizoram" to "15",
        "Tripura" to "16", "Meghalaya" to "17", "Assam" to "18", "West Bengal" to "19",
        "Jharkhand" to "20", "Odisha" to "21", "Chhattisgarh" to "22", "Madhya Pradesh" to "23",
        "Gujarat" to "24", "Maharashtra" to "27", "Karnataka" to "29", "Goa" to "30",
        "Kerala" to "32", "Tamil Nadu" to "33", "Telangana" to "36", "Andhra Pradesh" to "37"
    )

    val fieldColors = zeroBookInputColors()

    LaunchedEffect(pin) {
        if (pin.length == 6 && pin.all { it.isDigit() }) {
            delay(300)
            pinLoading = true
            val result = fetchPinLookup(pin)
            pinLoading = false
            if (result != null) {
                city = result.city
                state = result.state
                stateCode = result.stateCode.ifBlank { stateGstCodes[result.state] ?: stateCode }
                pinLookupMessage = "City: ${result.city}, ${result.state}"
                pinLookupSuccess = true
            } else {
                pinLookupMessage = "City not found — enter manually"
                pinLookupSuccess = false
            }
        } else {
            pinLoading = false
            pinLookupMessage = ""
            pinLookupSuccess = false
        }
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
                .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 32.dp)
        ) {
            Text(
                "Add New ${if (partyType == "CUSTOMER") "Customer" else "Supplier"}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text("Name *", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                placeholder = { Text("Business or person name", color = AppColors.inputPlaceholder) },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("Name is required", color = AppColors.error) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )
            Spacer(Modifier.height(8.dp))

            Text("Party Type", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("CUSTOMER", "SUPPLIER", "BOTH").forEach { option ->
                    FilterChip(
                        selected = type == option,
                        onClick = { type = option },
                        label = { Text(option) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.primary,
                            selectedLabelColor = AppColors.textOnPrimary,
                            labelColor = AppColors.textPrimary
                        )
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            Text("Phone (optional)", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = { Text("Mobile number", color = AppColors.inputPlaceholder) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )
            Spacer(Modifier.height(8.dp))

            Text("Email (optional)", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("email@example.com", color = AppColors.inputPlaceholder) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )
            Spacer(Modifier.height(8.dp))

            Text("GSTIN (optional)", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = gstin,
                onValueChange = { value ->
                    val result = parseGstinInput(value, pan, state, stateCode)
                    gstin = result.gstin
                    gstinValid = result.valid
                    if (result.valid == true) {
                        stateCode = result.stateCode
                        pan = result.pan
                        result.stateName?.let { if (state.isBlank()) state = it }
                    }
                },
                placeholder = { Text("15-digit GSTIN", color = AppColors.inputPlaceholder) },
                trailingIcon = {
                    when (gstinValid) {
                        true -> Icon(Icons.Default.CheckCircle, null, tint = AppColors.success)
                        false -> Icon(Icons.Default.Error, null, tint = AppColors.error)
                        null -> {}
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )
            GstinValidationFeedback(gstin, gstinValid, pan, state, stateCode)
            Spacer(Modifier.height(8.dp))

            Text("PAN (optional)", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = pan,
                onValueChange = { pan = it.uppercase() },
                placeholder = { Text("10-digit PAN", color = AppColors.inputPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )
            Spacer(Modifier.height(8.dp))

            Text("Address (optional)", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                placeholder = { Text("Street address", color = AppColors.inputPlaceholder) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )
            Spacer(Modifier.height(8.dp))

            Text("PIN Code (optional)", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = pin,
                onValueChange = { v -> pin = v.filter { it.isDigit() }.take(6) },
                placeholder = { Text("6-digit PIN", color = AppColors.inputPlaceholder) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                trailingIcon = if (pinLoading) {
                    { CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )
            if (pinLookupMessage.isNotBlank()) {
                Text(
                    text = pinLookupMessage,
                    color = if (pinLookupSuccess) Color(0xFF2E7D32) else Color.Gray,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("City (optional)", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        placeholder = { Text("City", color = AppColors.inputPlaceholder) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("State (optional)", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = state,
                        onValueChange = { v ->
                            state = v
                            stateCode = stateGstCodes[v] ?: stateCode
                        },
                        placeholder = { Text("State", color = AppColors.inputPlaceholder) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            Text("State Code (auto-filled)", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = stateCode,
                onValueChange = { stateCode = it },
                placeholder = { Text("e.g. 19 for West Bengal", color = AppColors.inputPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )
            Spacer(Modifier.height(8.dp))

            Text("Opening Balance (optional)", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = openingBalance,
                    onValueChange = { openingBalance = filterDecimalInput(it) },
                    placeholder = { Text("0.00", color = AppColors.inputPlaceholder) },
                    prefix = { Text("₹", color = AppColors.textPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                if (openingBalance == "0" || openingBalance == "0.0" || openingBalance == "0.00") {
                                    openingBalance = ""
                                }
                            } else if (openingBalance.isBlank()) {
                                openingBalance = "0"
                            }
                        },
                    colors = fieldColors
                )
                Row {
                    listOf("DR", "CR").forEach { bt ->
                        FilterChip(
                            selected = balanceType == bt,
                            onClick = { balanceType = bt },
                            label = { Text(bt) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.primary,
                                selectedLabelColor = AppColors.textOnPrimary,
                                labelColor = AppColors.textPrimary
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }

            Text("Credit Limit (optional)", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = creditLimit,
                onValueChange = { creditLimit = filterDecimalInput(it) },
                placeholder = { Text("0.00", color = AppColors.inputPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = fieldColors
            )
            Spacer(Modifier.height(8.dp))

            Text("Credit Days (optional)", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = creditDays,
                onValueChange = { creditDays = it.filter(Char::isDigit) },
                placeholder = { Text("e.g. 30", color = AppColors.inputPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = fieldColors
            )
            Spacer(Modifier.height(8.dp))

            Text("Notes (optional)", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Additional notes", color = AppColors.inputPlaceholder) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    onSave(
                        Party(
                            id = UUID.randomUUID().toString(),
                            name = name.trim(),
                            type = type,
                            phone = phone.trim(),
                            email = email.trim(),
                            address = address.trim(),
                            city = city.trim(),
                            state = state.trim(),
                            stateCode = stateCode.trim(),
                            pin = pin.trim(),
                            gstin = gstin.trim().ifBlank { null },
                            pan = pan.trim().ifBlank { null },
                            openingBalance = openingBalance.toDoubleOrNull() ?: 0.0,
                            balanceType = balanceType,
                            creditLimit = creditLimit.toDoubleOrNull() ?: 0.0,
                            creditDays = creditDays.toIntOrNull() ?: 0,
                            notes = notes.trim()
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) {
                Text("Save & Select", color = AppColors.textOnPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun BusinessImageUploadSection(
    title: String,
    subtitle: String,
    currentPath: String?,
    onImageSelected: (Uri) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { onImageSelected(it) } }

    val docLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { onImageSelected(it) } }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
        border = BorderStroke(1.dp, AppColors.border),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = AppColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = AppColors.textTertiary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp, 70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, AppColors.border, RoundedCornerShape(8.dp))
                        .background(AppColors.inputBg)
                        .clickable { showDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentPath.isNullOrBlank()) {
                        AsyncImage(
                            model = currentPath,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.AddPhotoAlternate, null, tint = AppColors.inputPlaceholder, modifier = Modifier.size(28.dp))
                            Text("Tap to upload", fontSize = 11.sp, color = AppColors.inputPlaceholder)
                        }
                    }
                }

                Column(Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = {
                            imageLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, AppColors.primary)
                    ) {
                        Icon(Icons.Outlined.Image, null, tint = AppColors.primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Gallery / Image", color = AppColors.primary, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { docLauncher.launch(arrayOf("image/*", "application/pdf")) },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, AppColors.border)
                    ) {
                        Icon(Icons.Outlined.PictureAsPdf, null, tint = Color(0xFF555555), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("PDF / Document", color = Color(0xFF555555), fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showDialog && !currentPath.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = AppColors.cardBg,
            titleContentColor = AppColors.textPrimary,
            textContentColor = AppColors.textSecondary,
            title = { Text(title, color = AppColors.textPrimary) },
            text = {
                AsyncImage(
                    model = currentPath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Fit
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("Close", color = AppColors.primary) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    imageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) { Text("Change", color = AppColors.primary) }
            }
        )
    }
}
