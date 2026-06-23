package com.example.ui.theme
import com.example.ui.theme.AppColors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val STATE_CODE_TO_NAME = mapOf(
    "01" to "Jammu and Kashmir", "02" to "Himachal Pradesh", "03" to "Punjab",
    "04" to "Chandigarh", "05" to "Uttarakhand", "06" to "Haryana", "07" to "Delhi",
    "08" to "Rajasthan", "09" to "Uttar Pradesh", "10" to "Bihar", "11" to "Sikkim",
    "12" to "Arunachal Pradesh", "13" to "Nagaland", "14" to "Manipur", "15" to "Mizoram",
    "16" to "Tripura", "17" to "Meghalaya", "18" to "Assam", "19" to "West Bengal",
    "20" to "Jharkhand", "21" to "Odisha", "22" to "Chhattisgarh", "23" to "Madhya Pradesh",
    "24" to "Gujarat", "27" to "Maharashtra", "29" to "Karnataka", "30" to "Goa",
    "32" to "Kerala", "33" to "Tamil Nadu", "34" to "Puducherry", "36" to "Telangana",
    "37" to "Andhra Pradesh", "38" to "Ladakh"
)

private val GSTIN_REGEX = Regex(
    "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$"
)

data class GstinParseResult(
    val gstin: String,
    val valid: Boolean?,
    val stateCode: String,
    val pan: String,
    val stateName: String?
)

fun parseGstinInput(
    value: String,
    currentPan: String,
    currentState: String,
    currentStateCode: String
): GstinParseResult {
    val v = value.uppercase().filter { it.isLetterOrDigit() }.take(15)
    if (v.length != 15) {
        return GstinParseResult(v, null, currentStateCode, currentPan, null)
    }
    val valid = GSTIN_REGEX.matches(v)
    if (!valid) {
        return GstinParseResult(v, false, currentStateCode, currentPan, null)
    }
    val code = v.substring(0, 2)
    val extractedPan = v.substring(2, 12)
    val stateName = STATE_CODE_TO_NAME[code]
    return GstinParseResult(
        gstin = v,
        valid = true,
        stateCode = code,
        pan = extractedPan,
        stateName = stateName
    )
}

@Composable
fun GstinValidationFeedback(
    gstin: String,
    gstinValid: Boolean?,
    pan: String,
    state: String,
    stateCode: String
) {
    when {
        gstin.length == 15 && gstinValid == true -> {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    "✓ Valid GSTIN",
                    color = AppColors.success,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(AppColors.successBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                if (pan.isNotBlank()) {
                    Text(
                        "PAN: $pan (auto-extracted)",
                        color = AppColors.textSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (state.isNotBlank()) {
                    Text(
                        "State: $state (Code: $stateCode)",
                        color = AppColors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
        gstin.length == 15 && gstinValid == false -> {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    "✗ Invalid GSTIN format",
                    color = AppColors.error,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(AppColors.errorBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Text(
                    "Check: 2 digits + 5 letters + 4 digits + letter + digit/letter + Z + digit/letter",
                    color = AppColors.textTertiary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
