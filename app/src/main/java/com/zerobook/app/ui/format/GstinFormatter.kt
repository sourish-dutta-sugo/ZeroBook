package com.zerobook.app.ui.format

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
import com.zerobook.app.ui.theme.AppColors

// Re-export from theme for backward compatibility
@Deprecated("Use ui.format.GstinFormatter instead", ReplaceWith("parseGstinInput(value, currentPan, currentState, currentStateCode)"))
fun parseGstinInputFromTheme(
    value: String,
    currentPan: String,
    currentState: String,
    currentStateCode: String
) = com.zerobook.app.ui.theme.parseGstinInput(value, currentPan, currentState, currentStateCode)

val STATE_CODE_TO_NAME = com.zerobook.app.ui.theme.STATE_CODE_TO_NAME
