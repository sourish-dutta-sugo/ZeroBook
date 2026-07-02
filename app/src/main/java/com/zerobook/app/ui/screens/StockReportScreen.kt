package com.zerobook.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerobook.app.data.Product
import com.zerobook.app.services.ExportStorageManager
import com.zerobook.app.services.ExportTarget
import com.zerobook.app.ui.theme.AppColors

@Composable
fun StockReportScreen(products: List<Product>) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("ALL") }

    val filteredProducts = remember(products, searchQuery, activeFilter) {
        products.filter { product ->
            val matchesSearch = product.name.contains(searchQuery, true) || product.hsnCode.contains(searchQuery, true)
            val matchesFilter = when (activeFilter) {
                "LOW" -> product.enableStockAlert && product.currentStock > 0.0 && product.currentStock <= product.lowStockThreshold
                "OUT" -> product.currentStock <= 0.0
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.screenBg),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val rows = buildString {
                        append("name,hsn,current_stock,unit,low_stock_threshold,status\n")
                        filteredProducts.forEach { product ->
                            val status = when {
                                product.currentStock <= 0.0 -> "Out of Stock"
                                product.enableStockAlert && product.currentStock <= product.lowStockThreshold -> "Low Stock"
                                else -> "In Stock"
                            }
                            append(
                                listOf(
                                    product.name,
                                    product.hsnCode,
                                    product.currentStock.toString(),
                                    product.stockUnit.ifBlank { product.unit },
                                    product.lowStockThreshold.toString(),
                                    status
                                ).joinToString(",")
                            )
                            append('\n')
                        }
                    }
                    val result = ExportStorageManager.exportBytes(
                        context = context,
                        bytes = rows.toByteArray(),
                        displayName = "ZeroBook_Stock_Report.csv",
                        mimeType = "text/csv",
                        target = ExportTarget.Reports
                    )
                    Toast.makeText(context, "Saved to ${result.locationLabel}", Toast.LENGTH_LONG).show()
                }
            ) {
                Text("Export CSV")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL" to "All", "LOW" to "Low Stock", "OUT" to "Out of Stock").forEach { (value, label) ->
                FilterChip(
                    selected = activeFilter == value,
                    onClick = { activeFilter = value },
                    label = { Text(label) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(filteredProducts, key = { it.id }) { product ->
                val statusColor = when {
                    product.currentStock <= 0.0 -> Color(0xFFC62828)
                    product.enableStockAlert && product.currentStock <= product.lowStockThreshold -> Color(0xFFEF6C00)
                    else -> Color(0xFF2E7D32)
                }
                val statusLabel = when {
                    product.currentStock <= 0.0 -> "Out of Stock"
                    product.enableStockAlert && product.currentStock <= product.lowStockThreshold -> "Low Stock"
                    else -> "In Stock"
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(product.name, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text(statusLabel, fontSize = 10.sp, color = statusColor) },
                                colors = AssistChipDefaults.assistChipColors(
                                    disabledContainerColor = statusColor.copy(alpha = 0.12f),
                                    disabledLabelColor = statusColor
                                )
                            )
                        }
                        Text("HSN: ${product.hsnCode.ifBlank { "N/A" }}", color = AppColors.textSecondary, fontSize = 12.sp)
                        Text(
                            "Current stock: ${product.currentStock} ${product.stockUnit.ifBlank { product.unit }}",
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (product.enableStockAlert) "Low stock threshold: ${product.lowStockThreshold}" else "Low stock alert disabled",
                            color = AppColors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
