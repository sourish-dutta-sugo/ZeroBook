package com.zerobook.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerobook.app.data.Product
import com.zerobook.app.data.Utils
import com.zerobook.app.data.Voucher
import com.zerobook.app.data.VoucherItem
import com.zerobook.app.ui.AppViewModel
import com.zerobook.app.ui.theme.AppColors
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSaleScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val parties by viewModel.parties.collectAsState()
    val cart = remember { mutableStateListOf<VoucherItem>() }
    var selectedUnitFilter by remember { mutableStateOf("ALL") }
    var walkInCustomer by remember { mutableStateOf(true) }
    var selectedPartyId by remember { mutableStateOf<String?>(null) }
    var paymentMode by remember { mutableStateOf("CASH") }
    var search by remember { mutableStateOf("") }
    val filteredProducts = remember(products, selectedUnitFilter, search) {
        products.filter {
            (selectedUnitFilter == "ALL" || it.unit == selectedUnitFilter) &&
                it.name.contains(search, true)
        }
    }
    val total = cart.sumOf { it.totalAmount }

    Scaffold(
        containerColor = AppColors.screenBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Bolt, contentDescription = null)
                        Text("Quick Sale", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
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
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Walk-in Customer", fontWeight = FontWeight.SemiBold)
                        Switch(checked = walkInCustomer, onCheckedChange = { walkInCustomer = it; if (it) selectedPartyId = null })
                    }
                    if (!walkInCustomer) {
                        parties.filter { it.type == "CUSTOMER" || it.type == "BOTH" }.take(6).forEach { party ->
                            FilterChip(
                                selected = selectedPartyId == party.id,
                                onClick = { selectedPartyId = party.id },
                                label = { Text(party.name) }
                            )
                        }
                    }
                }
            }

            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(0.42f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = search, onValueChange = { search = it }, label = { Text("Search products") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("ALL" to "All").plus(products.map { it.unit }.distinct().take(3).map { it to it }).forEach { (value, label) ->
                            FilterChip(selected = selectedUnitFilter == value, onClick = { selectedUnitFilter = value }, label = { Text(label) })
                        }
                    }
                    filteredProducts.forEach { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val existing = cart.indexOfFirst { it.productId == product.id }
                                    if (existing >= 0) {
                                        val current = cart[existing]
                                        cart[existing] = current.copy(
                                            qty = current.qty + 1,
                                            taxableAmount = (current.qty + 1) * current.rate,
                                            totalAmount = ((current.qty + 1) * current.rate) + current.cgstAmount + current.sgstAmount + current.igstAmount
                                        )
                                    } else {
                                        cart.add(product.toCartItem())
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(product.name, fontWeight = FontWeight.Bold)
                                Text(Utils.formatIndianCurrency(product.saleRate), color = AppColors.primary)
                                Text("Stock: ${product.currentStock}", color = AppColors.textSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Column(modifier = Modifier.weight(0.58f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cart", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(cart, key = { it.id }) { item ->
                            Card(colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontWeight = FontWeight.Bold)
                                        Text("${item.qty} x ${Utils.formatIndianCurrency(item.rate)}", color = AppColors.textSecondary)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            val newQty = item.qty - 1
                                            if (newQty <= 0.0) cart.remove(item) else {
                                                val index = cart.indexOfFirst { it.id == item.id }
                                                cart[index] = item.copy(qty = newQty, taxableAmount = newQty * item.rate, totalAmount = newQty * item.rate)
                                            }
                                        }) {
                                            Icon(Icons.Default.Remove, contentDescription = "Reduce")
                                        }
                                        Text(Utils.formatIndianCurrency(item.totalAmount), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("CASH", "UPI", "CARD").forEach { mode ->
                            FilterChip(selected = paymentMode == mode, onClick = { paymentMode = if (mode == "CARD") "BANK" else mode }, label = { Text(mode) })
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = AppColors.cardBg)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Total", fontSize = 12.sp, color = AppColors.textSecondary)
                            Text(Utils.formatIndianCurrency(total), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.primary)
                        }
                    }
                    Button(
                        onClick = {
                            if (cart.isEmpty()) {
                                Toast.makeText(viewModel.getApplication(), "Add items first", Toast.LENGTH_SHORT).show()
                            } else {
                                val now = System.currentTimeMillis()
                                CoroutineScope(Dispatchers.Main).launch {
                                    val voucherNo = viewModel.generateNextVoucherNo("SALE", now)
                                    viewModel.saveVoucher(
                                        voucher = Voucher(
                                            id = UUID.randomUUID().toString(),
                                            voucherNo = voucherNo,
                                            type = "SALE",
                                            date = now,
                                            partyId = selectedPartyId,
                                            narration = "Quick POS sale",
                                            taxableAmount = cart.sumOf { it.taxableAmount },
                                            cgst = cart.sumOf { it.cgstAmount },
                                            sgst = cart.sumOf { it.sgstAmount },
                                            igst = cart.sumOf { it.igstAmount },
                                            roundOff = 0.0,
                                            netAmount = total,
                                            paymentMode = paymentMode,
                                            chequeNo = null,
                                            chequeDate = null,
                                            bankName = null,
                                            isIgst = false,
                                            status = "POSTED"
                                        ),
                                        items = cart.toList(),
                                        partyName = parties.find { it.id == selectedPartyId }?.name
                                    ) {
                                        cart.clear()
                                        Toast.makeText(viewModel.getApplication(), "Quick sale saved", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("BILL NOW", color = AppColors.textOnPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun Product.toCartItem(): VoucherItem = VoucherItem(
    id = UUID.randomUUID().toString(),
    voucherId = "",
    productId = id,
    productName = name,
    hsnCode = hsnCode,
    qty = 1.0,
    unit = unit,
    rate = saleRate,
    discount = 0.0,
    discountType = "AMOUNT",
    taxableAmount = saleRate,
    gstRate = gstRate,
    cgstAmount = (saleRate * gstRate / 100.0) / 2.0,
    sgstAmount = (saleRate * gstRate / 100.0) / 2.0,
    igstAmount = 0.0,
    totalAmount = saleRate + (saleRate * gstRate / 100.0)
)
