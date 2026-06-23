package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppColors
import com.example.ui.theme.Colors

@Composable
fun ProductOptionalFields(
    hsnCode: String,
    onHsnChange: (String) -> Unit,
    onFindHsn: () -> Unit,
    batchEnabled: Boolean,
    onBatchEnabledChange: (Boolean) -> Unit,
    batchNumber: String,
    onBatchNumberChange: (String) -> Unit,
    expiryEnabled: Boolean,
    onExpiryEnabledChange: (Boolean) -> Unit,
    expiryDate: String = "",
    onExpiryDateChange: (String) -> Unit = {},
    serialEnabled: Boolean = false,
    onSerialEnabledChange: (Boolean) -> Unit = {}
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AppColors.textPrimary,
        unfocusedTextColor = AppColors.textPrimary,
        focusedBorderColor = AppColors.primary,
        unfocusedBorderColor = Colors.inputBorder
    )

    Text("HSN/SAC Code (optional)", color = AppColors.labelText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    OutlinedTextField(
        value = hsnCode,
        onValueChange = { onHsnChange(it.filter { c -> c.isDigit() }.take(8)) },
        placeholder = { Text("4 to 8 digit HSN code", color = AppColors.inputPlaceholder) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        supportingText = {
            Text("Optional. Required for GST invoices above Rs 5 Lakhs", fontSize = 11.sp, color = AppColors.textTertiary)
        },
        modifier = Modifier.fillMaxWidth(),
        colors = fieldColors
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        OutlinedButton(onClick = onFindHsn) {
            Text("Find HSN")
        }
    }
    Spacer(Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Batch Number", color = AppColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("For medicines, FMCG, food items", color = AppColors.textTertiary, fontSize = 11.sp)
        }
        Switch(
            checked = batchEnabled,
            onCheckedChange = onBatchEnabledChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AppColors.textOnPrimary, checkedTrackColor = AppColors.primary)
        )
    }
    if (batchEnabled) {
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = batchNumber,
            onValueChange = onBatchNumberChange,
            label = { Text("Batch Number") },
            placeholder = { Text("e.g. BTH2025001", color = AppColors.inputPlaceholder) },
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors
        )
    }

    Spacer(Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Expiry Date", color = AppColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("For perishables, medicines", color = AppColors.textTertiary, fontSize = 11.sp)
        }
        Switch(
            checked = expiryEnabled,
            onCheckedChange = onExpiryEnabledChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AppColors.textOnPrimary, checkedTrackColor = AppColors.primary)
        )
    }
    if (expiryEnabled) {
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = expiryDate,
            onValueChange = onExpiryDateChange,
            label = { Text("Expiry Date") },
            placeholder = { Text("DD-MM-YYYY", color = AppColors.inputPlaceholder) },
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors
        )
    }

    Spacer(Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Serial Number Tracking", color = AppColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("For electronics, appliances", color = AppColors.textTertiary, fontSize = 11.sp)
        }
        Switch(
            checked = serialEnabled,
            onCheckedChange = onSerialEnabledChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AppColors.textOnPrimary, checkedTrackColor = AppColors.primary)
        )
    }
}
