package com.zerobook.app.ui.screens

import androidx.compose.runtime.Composable
import com.zerobook.app.feature.billing.BillingScreen
import com.zerobook.app.ui.AppViewModel

/** @deprecated Use [BillingScreen] from com.zerobook.app.feature.billing instead. */
@Deprecated("Use BillingScreen from feature.billing package", ReplaceWith("BillingScreen(viewModel, onNavigateBack)"))
@Composable
fun QuickSaleScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) = BillingScreen(viewModel, onNavigateBack)
