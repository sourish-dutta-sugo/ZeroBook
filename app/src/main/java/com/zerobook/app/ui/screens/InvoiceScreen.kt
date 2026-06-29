package com.zerobook.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.zerobook.app.services.ExportStorageManager
import com.zerobook.app.services.InvoiceGenerator
import com.zerobook.app.services.configureInvoiceWebView
import com.zerobook.app.services.printInvoice
import com.zerobook.app.services.shareInvoicePdf
import com.zerobook.app.services.shareInvoicePdfToWhatsApp
import com.zerobook.app.ui.AppViewModel
import com.zerobook.app.ui.theme.AppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    viewModel: AppViewModel,
    voucherId: String,
    onNavigateBack: () -> Unit,
    onEditVoucher: (String) -> Unit,
    onCreateSaleFromVoucher: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var renderBundle by remember { mutableStateOf<InvoiceGenerator.InvoiceRenderBundle?>(null) }
    var isWorking by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var lastGeneratedPdf by remember { mutableStateOf<java.io.File?>(null) }
    var pendingSaveAsFile by remember { mutableStateOf<java.io.File?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val sourceFile = pendingSaveAsFile
        pendingSaveAsFile = null
        if (uri == null || sourceFile == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                ExportStorageManager.writeFileToUri(context, sourceFile, uri)
                val action = snackbarHostState.showSnackbar(
                    message = "Invoice saved successfully",
                    actionLabel = "Open"
                )
                if (action == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }
            }.onFailure {
                Toast.makeText(context, it.message ?: "Failed to save invoice", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun refreshBundle() {
        scope.launch {
            isWorking = true
            renderBundle = viewModel.getInvoiceRenderBundle(voucherId)
            isWorking = false
        }
    }

    LaunchedEffect(voucherId) {
        refreshBundle()
    }

    fun openPdfUri(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPdf(file: java.io.File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        openPdfUri(uri)
    }

    fun runPdfAction(action: suspend (InvoiceGenerator.InvoiceRenderBundle, java.io.File) -> Unit) {
        scope.launch {
            isWorking = true
            val freshBundle = viewModel.getInvoiceRenderBundle(voucherId)
            if (freshBundle == null) {
                isWorking = false
                Toast.makeText(context, "Failed to load latest invoice data", Toast.LENGTH_SHORT).show()
                return@launch
            }
            renderBundle = freshBundle
            runCatching {
                val pdfFile = InvoiceGenerator.renderBundleToPdf(context, freshBundle)
                lastGeneratedPdf = pdfFile
                action(freshBundle, pdfFile)
            }.onFailure {
                Toast.makeText(context, it.message ?: "Invoice action failed", Toast.LENGTH_LONG).show()
            }
            isWorking = false
        }
    }

    Scaffold(
        containerColor = AppColors.screenBg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Invoice Viewer", fontWeight = FontWeight.Bold, color = Color(0xFF0D0D0D))
                        Text(
                            renderBundle?.document?.invoiceNumber ?: "",
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF444444))
                    }
                },
                actions = {
                    IconButton(onClick = { webViewRef?.zoomOut() }) {
                        Icon(Icons.Outlined.Remove, contentDescription = "Zoom Out", tint = Color(0xFF444444))
                    }
                    IconButton(onClick = { webViewRef?.zoomIn() }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Zoom In", tint = Color(0xFF444444))
                    }
                    IconButton(onClick = { refreshBundle() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Regenerate", tint = Color(0xFF444444))
                    }
                    IconButton(onClick = { onEditVoucher(voucherId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF444444))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        val bundle = renderBundle
        if (bundle == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.screenBg)
                    .padding(innerPadding)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                configureInvoiceWebView(this)
                                webViewClient = WebViewClient()
                                webViewRef = this
                            }
                        },
                        update = { webView ->
                            webViewRef = webView
                            webView.loadDataWithBaseURL("file:///", bundle.html, "text/html", "UTF-8", null)
                        }
                    )
                }

                if (isWorking) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AppColors.primary)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InvoiceActionButton(
                            icon = Icons.Outlined.Share,
                            label = "Share",
                            enabled = !isWorking,
                            onClick = {
                                runPdfAction { _, pdfFile -> shareInvoicePdf(context, pdfFile) }
                            }
                        )
                        InvoiceActionButton(
                            icon = Icons.Outlined.Download,
                            label = "Save As",
                            tint = Color(0xFF1A73E8),
                            enabled = !isWorking,
                            onClick = {
                                runPdfAction { _, pdfFile ->
                                    pendingSaveAsFile = pdfFile
                                    savePdfLauncher.launch(bundle.exportFileName)
                                }
                            }
                        )
                        InvoiceActionButton(
                            icon = Icons.Outlined.Print,
                            label = "Print",
                            enabled = !isWorking,
                            onClick = {
                                scope.launch {
                                    isWorking = true
                                    val freshBundle = viewModel.getInvoiceRenderBundle(voucherId)
                                    if (freshBundle != null) {
                                        renderBundle = freshBundle
                                        runCatching {
                                            printInvoice(context, freshBundle.html, freshBundle.document.invoiceNumber)
                                        }.onFailure {
                                            Toast.makeText(context, it.message ?: "Print failed", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    isWorking = false
                                }
                            }
                        )
                        InvoiceActionButton(
                            icon = Icons.AutoMirrored.Outlined.Send,
                            label = "WhatsApp",
                            enabled = !isWorking,
                            onClick = {
                                runPdfAction { _, pdfFile ->
                                    shareInvoicePdfToWhatsApp(context, pdfFile)
                                }
                            }
                        )
                        InvoiceActionButton(
                            icon = Icons.Outlined.Close,
                            label = "Close",
                            enabled = !isWorking,
                            onClick = onNavigateBack
                        )
                    }
                }
                if (bundle.document.voucher.type == "QUOTATION" || bundle.document.voucher.type == "DELIVERY_CHALLAN") {
                    Button(
                        onClick = { onCreateSaleFromVoucher(voucherId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                    ) {
                        Text(
                            if (bundle.document.voucher.type == "QUOTATION") "Convert to Invoice" else "Convert Challan to Invoice",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    tint: Color = Color(0xFF555555),
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (enabled) tint else Color(0xFFAAAAAA),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (enabled) tint else Color(0xFFAAAAAA),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
