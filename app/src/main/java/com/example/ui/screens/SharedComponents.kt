package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Utils
import com.example.ui.theme.AppColors
import com.example.ui.theme.zeroBookInputColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

private val TextDark = Color(0xFF0D0D0D)
private val MenuWhite = Color(0xFFFFFFFF)

data class PinLookupResult(
    val city: String,
    val state: String,
    val stateCode: String
)

suspend fun fetchPinLookup(pinCode: String): PinLookupResult? = withContext(Dispatchers.IO) {
    runCatching {
        val response = java.net.URL("https://api.postalpincode.in/pincode/$pinCode").readText()
        val root = JSONArray(response).optJSONObject(0) ?: return@runCatching null
        if (!root.optString("Status").equals("Success", ignoreCase = true)) return@runCatching null
        val postOffice = root.optJSONArray("PostOffice")?.optJSONObject(0) ?: return@runCatching null
        val city = postOffice.optString("District").trim()
        val state = postOffice.optString("State").trim()
        if (city.isBlank() || state.isBlank()) return@runCatching null
        val stateCode = Utils.INDIAN_STATES.firstOrNull {
            it.first.equals(state, ignoreCase = true) ||
                it.first.contains(state, ignoreCase = true) ||
                state.contains(it.first, ignoreCase = true)
        }?.second.orEmpty()
        PinLookupResult(city = city, state = state, stateCode = stateCode)
    }.getOrNull()
}

@Composable
fun StateDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onStateSelected: (Pair<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .fillMaxWidth(0.92f)
            .height(280.dp)
            .background(MenuWhite)
    ) {
        Utils.INDIAN_STATES.forEach { statePair ->
            DropdownMenuItem(
                text = {
                    Text(
                        "${statePair.first} (Code ${statePair.second})",
                        color = TextDark
                    )
                },
                onClick = { onStateSelected(statePair) },
                colors = MenuDefaults.itemColors(
                    textColor = TextDark,
                    leadingIconColor = TextDark,
                    trailingIconColor = TextDark
                )
            )
        }
    }
}

@Composable
fun RetailTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.labelText
            )
        },
        placeholder = placeholder?.let {
            { Text(text = it, fontSize = 15.sp, color = AppColors.inputPlaceholder) }
        },
        modifier = modifier.fillMaxWidth(),
        readOnly = readOnly,
        singleLine = singleLine,
        textStyle = TextStyle(color = AppColors.inputText, fontSize = 15.sp),
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(8.dp),
        isError = isError,
        colors = zeroBookInputColors()
    )
}
