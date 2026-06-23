package com.example
import com.example.ui.theme.AppColors

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.widget.Toast
import com.example.data.AppPreferences
import com.example.data.ChangelogData
import com.example.data.ChangelogLoader
import com.example.ui.AppViewModel
import com.example.ui.DashboardViewModel
import com.example.ui.screens.*
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

sealed class Screen {
    object Setup : Screen()
    object Dashboard : Screen()
    object Vouchers : Screen()
    object Parties : Screen()
    object Settings : Screen()
    object Reports : Screen()
    object LedgerBooks : Screen()
    object Expenses : Screen()
    object QuickSale : Screen()
    
    // Sub-screens
    object Products : Screen()
    object BankCash : Screen()
    data class NewVoucher(val voucherId: String? = null) : Screen()
    data class Invoice(val voucherId: String) : Screen()
    data class PartyDetail(val partyId: String) : Screen()
}

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            val currentTheme by themeViewModel.currentTheme.collectAsState()
            val windowInsetsController = remember(window) {
                WindowInsetsControllerCompat(window, window.decorView)
            }

            @Suppress("DEPRECATION")
            SideEffect {
                window.statusBarColor = currentTheme.statusBarColor.toArgb()
                windowInsetsController.isAppearanceLightStatusBars = currentTheme.statusBarDarkIcons
                window.navigationBarColor = currentTheme.backgroundPrimary.toArgb()
                windowInsetsController.isAppearanceLightNavigationBars = currentTheme.statusBarDarkIcons
            }

            CompositionLocalProvider(LocalAppTheme provides currentTheme) {
                MyApplicationTheme(appTheme = currentTheme) {
                    MainAppEntry(viewModel, themeViewModel, dashboardViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppEntry(
    viewModel: AppViewModel,
    themeViewModel: ThemeViewModel,
    dashboardViewModel: DashboardViewModel
) {
    val context = LocalContext.current
    val dbState by viewModel.dbInitState.collectAsState()

    when (val state = dbState) {
        is AppViewModel.DbInitState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.screenBg),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF1A73E8))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Initializing Secure Database...",
                        fontSize = 14.sp,
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        is AppViewModel.DbInitState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.screenBg)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Database Connection Failed",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFDC3545)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.message,
                            fontSize = 14.sp,
                            color = Color(0xFF555555),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (context is android.app.Activity) {
                                    context.recreate()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Retry Connection", color = AppColors.textOnPrimary)
                        }
                    }
                }
            }
        }
        is AppViewModel.DbInitState.Success -> {
            val isSetupCompleted by viewModel.isSetupCompleted.collectAsState()
            var showSplash by remember { mutableStateOf(true) }

            if (showSplash) {
                SplashScreen(onTimeout = { showSplash = false })
            } else {
                // Persistent Navigation stack
                val backstack = remember { mutableStateListOf<Screen>() }
                var isNavigatingBack by remember { mutableStateOf(false) }

                // Screen navigation helpers
                fun navigateTo(screen: Screen) {
                    isNavigatingBack = false
                    backstack.add(screen)
                }

                fun navigateBack() {
                    if (backstack.isNotEmpty()) {
                        isNavigatingBack = true
                        backstack.removeAt(backstack.size - 1)
                    }
                }

                fun navigateToInvoiceAfterVoucherSave(voucherId: String) {
                    isNavigatingBack = false
                    if (backstack.lastOrNull() is Screen.NewVoucher) {
                        backstack.removeAt(backstack.lastIndex)
                    }
                    backstack.add(Screen.Invoice(voucherId))
                }

                // Handle Android software/hardware back gestures and buttons gracefully
                androidx.activity.compose.BackHandler(enabled = backstack.isNotEmpty()) {
                    navigateBack()
                }

                // Load App Lock Status
                val sp = remember { context.getSharedPreferences("zerobook_pref", Context.MODE_PRIVATE) }
                var pinRequired by remember { mutableStateOf(sp.getBoolean("pin_enabled", false)) }
                var pinAuthed by remember { mutableStateOf(false) }
                var changelogData by remember { mutableStateOf<ChangelogData?>(null) }
                var showChangelog by remember { mutableStateOf(false) }
                val uiScope = rememberCoroutineScope()

                LaunchedEffect(isSetupCompleted, pinRequired, pinAuthed) {
                    if (isSetupCompleted && (!pinRequired || pinAuthed)) {
                        viewModel.autoAdvanceFinancialYearIfNeeded(context)?.let { updatedFy ->
                            Toast.makeText(context, "Financial year updated to FY $updatedFy", Toast.LENGTH_LONG).show()
                        }
                        val loadedChangelog = ChangelogLoader.load(context)
                        val lastSeenVersion = AppPreferences.getLastSeenChangelogVersion(context)
                        if (loadedChangelog != null && (lastSeenVersion == null || lastSeenVersion != loadedChangelog.version)) {
                            changelogData = loadedChangelog
                            showChangelog = true
                        }
                    }
                }

                val currentScreen = if (!isSetupCompleted) {
                    Screen.Setup
                } else if (backstack.isEmpty()) {
                    Screen.Dashboard
                } else {
                    backstack.last()
                }

                if (pinRequired && !pinAuthed && isSetupCompleted) {
                    PinLockScreen(
                        correctPin = sp.getString("lock_pin", "1234") ?: "1234",
                        onAuthentic = { pinAuthed = true }
                    )
                } else {
                    val isMajorScreen = currentScreen is Screen.Dashboard || currentScreen is Screen.Vouchers || 
                                        currentScreen is Screen.Parties || currentScreen is Screen.Settings

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding(),
                        containerColor = AppColors.screenBg,
                        bottomBar = {
                            // Display standard One UI bottom bar on major root tab screens
                            if (isMajorScreen) {
                                NavigationBar(
                                    containerColor = AppColors.cardBg,
                                    tonalElevation = 6.dp,
                                    modifier = Modifier
                                        .navigationBarsPadding()
                                        .height(72.dp)
                                ) {
                                    val currentTab = currentScreen
                                    
                                    NavigationBarItem(
                                        selected = currentTab is Screen.Dashboard,
                                        onClick = {
                                            backstack.clear()
                                        },
                                        icon = { Icon(imageVector = Icons.Default.GridView, contentDescription = "Dashboard") },
                                        label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = AppColors.primary,
                                            unselectedIconColor = AppColors.textTertiary,
                                            selectedTextColor = AppColors.primary,
                                            unselectedTextColor = AppColors.textTertiary,
                                            indicatorColor = AppColors.primary.copy(alpha = 0.12f)
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = currentTab is Screen.Vouchers,
                                        onClick = {
                                            backstack.clear()
                                            navigateTo(Screen.Vouchers)
                                        },
                                        icon = { Icon(imageVector = Icons.AutoMirrored.Filled.Assignment, contentDescription = "Vouchers") },
                                        label = { Text("Vouchers", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = AppColors.primary,
                                            unselectedIconColor = AppColors.textTertiary,
                                            selectedTextColor = AppColors.primary,
                                            unselectedTextColor = AppColors.textTertiary,
                                            indicatorColor = AppColors.primary.copy(alpha = 0.12f)
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = currentTab is Screen.Parties,
                                        onClick = {
                                            backstack.clear()
                                            navigateTo(Screen.Parties)
                                        },
                                        icon = { Icon(imageVector = Icons.Default.Group, contentDescription = "Parties") },
                                        label = { Text("Parties", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = AppColors.primary,
                                            unselectedIconColor = AppColors.textTertiary,
                                            selectedTextColor = AppColors.primary,
                                            unselectedTextColor = AppColors.textTertiary,
                                            indicatorColor = AppColors.primary.copy(alpha = 0.12f)
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = currentTab is Screen.Settings,
                                        onClick = {
                                            backstack.clear()
                                            navigateTo(Screen.Settings)
                                        },
                                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                                        label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = AppColors.primary,
                                            unselectedIconColor = AppColors.textTertiary,
                                            selectedTextColor = AppColors.primary,
                                            unselectedTextColor = AppColors.textTertiary,
                                            indicatorColor = AppColors.primary.copy(alpha = 0.12f)
                                        )
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    if (isNavigatingBack) {
                                        slideInHorizontally(
                                            initialOffsetX = { -it / 4 },
                                            animationSpec = tween(300)
                                        ) togetherWith slideOutHorizontally(
                                            targetOffsetX = { it },
                                            animationSpec = tween(300)
                                        )
                                    } else {
                                        slideInHorizontally(
                                            initialOffsetX = { it },
                                            animationSpec = tween(300)
                                        ) togetherWith slideOutHorizontally(
                                            targetOffsetX = { -it / 4 },
                                            animationSpec = tween(300)
                                        )
                                    }
                                },
                                label = "main_screen_navigation"
                            ) { animatedScreen ->
                            when (animatedScreen) {
                                is Screen.Setup -> {
                                    SetupScreen(
                                        viewModel = viewModel,
                                        onSetupComplete = {
                                            // Profile saved triggers StateFlow update automatically
                                        }
                                    )
                                }

                                is Screen.Dashboard -> {
                                    DashboardScreen(
                                        viewModel = viewModel,
                                        dashboardViewModel = dashboardViewModel,
                                        isDesktop = false,
                                        onQuickAction = { action ->
                                            when (action) {
                                                "SALE" -> navigateTo(Screen.NewVoucher())
                                                "PURCHASE" -> navigateTo(Screen.NewVoucher())
                                                "RECEIPT" -> navigateTo(Screen.BankCash)
                                                "PAYMENT" -> navigateTo(Screen.BankCash)
                                                "REPORTS" -> navigateTo(Screen.Reports)
                                                "QUICK_SALE" -> navigateTo(Screen.QuickSale)
                                                "EXPENSES" -> navigateTo(Screen.Expenses)
                                                "PARTY" -> {
                                                    backstack.clear()
                                                    navigateTo(Screen.Parties)
                                                }
                                            }
                                        }
                                    )
                                }

                                is Screen.Vouchers -> {
                                    VouchersScreen(
                                        viewModel = viewModel,
                                        isDesktop = false,
                                        navigateToNewVoucher = { navigateTo(Screen.NewVoucher()) },
                                        navigateToInvoice = { id -> navigateTo(Screen.Invoice(id)) }
                                    )
                                }

                                is Screen.Parties -> {
                                    PartiesScreen(
                                        viewModel = viewModel,
                                        isDesktop = false,
                                        onPartySelected = { id -> navigateTo(Screen.PartyDetail(id)) }
                                    )
                                }

                                is Screen.Settings -> {
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        themeViewModel = themeViewModel,
                                        isDesktop = false,
                                        navigateToProducts = { navigateTo(Screen.Products) },
                                        navigateToLedgerBooks = { navigateTo(Screen.LedgerBooks) },
                                        onNavigateBack = { navigateBack() }
                                    )
                                }

                                is Screen.Reports -> {
                                    ReportsScreen(
                                        viewModel = viewModel,
                                        isDesktop = false,
                                        navigateToLedgerBooks = { navigateTo(Screen.LedgerBooks) },
                                        navigateToExpenses = { navigateTo(Screen.Expenses) },
                                        navigateToNewVoucher = { navigateTo(Screen.NewVoucher(it)) },
                                        onNavigateBack = { navigateBack() }
                                    )
                                }

                                is Screen.Products -> {
                                    ProductsScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { navigateBack() }
                                    )
                                }

                                is Screen.BankCash -> {
                                    BankCashScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { navigateBack() }
                                    )
                                }

                                is Screen.Expenses -> {
                                    ExpensesScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { navigateBack() }
                                    )
                                }

                                is Screen.QuickSale -> {
                                    QuickSaleScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { navigateBack() }
                                    )
                                }

                                is Screen.NewVoucher -> {
                                    NewVoucherScreen(
                                        viewModel = viewModel,
                                        voucherId = animatedScreen.voucherId,
                                        isDesktop = false,
                                        onNavigateBack = { navigateBack() },
                                        onNavigateToInvoice = { id -> navigateToInvoiceAfterVoucherSave(id) },
                                        onNavigateToPartyDetail = { id -> navigateTo(Screen.PartyDetail(id)) }
                                    )
                                }

                                is Screen.Invoice -> {
                                    InvoiceScreen(
                                        viewModel = viewModel,
                                        voucherId = animatedScreen.voucherId,
                                        onNavigateBack = { navigateBack() },
                                        onEditVoucher = { id -> navigateTo(Screen.NewVoucher(id)) },
                                        onCreateSaleFromVoucher = { sourceVoucherId ->
                                            val sourceVoucher = viewModel.getVoucherById(sourceVoucherId)
                                            viewModel.setVoucherPrefillRequest(
                                                AppViewModel.VoucherPrefillRequest(
                                                    voucherType = "SALE",
                                                    partyId = sourceVoucher?.partyId,
                                                    invoiceId = null,
                                                    amount = null,
                                                    sourceVoucherId = sourceVoucherId
                                                )
                                            )
                                            navigateTo(Screen.NewVoucher())
                                        }
                                    )
                                }

                                is Screen.LedgerBooks -> {
                                    LedgerListScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { navigateBack() }
                                    )
                                }

                                is Screen.PartyDetail -> {
                                    PartyDetailScreen(
                                        viewModel = viewModel,
                                        partyId = animatedScreen.partyId,
                                        onNavigateBack = { navigateBack() }
                                    )
                                }
                            }
                            }
                        }
                    }

                    if (showChangelog && changelogData != null) {
                        val configuration = LocalConfiguration.current
                        AlertDialog(
                            onDismissRequest = {},
                            confirmButton = {
                                Button(
                                    onClick = {
                                        val version = changelogData?.version.orEmpty()
                                        if (version.isNotBlank()) {
                                            uiScope.launch {
                                                AppPreferences.setLastSeenChangelogVersion(context, version)
                                                showChangelog = false
                                            }
                                        } else {
                                            showChangelog = false
                                        }
                                    }
                                ) {
                                    Text("Got it")
                                }
                            },
                            title = {
                                Text("What's New in ZeroBook ${changelogData?.version.orEmpty()}")
                            },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = configuration.screenHeightDp.dp * 0.6f)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    changelogData?.changes?.forEach { change ->
                                        Text("• $change", color = Color(0xFF111827))
                                    }
                                }
                            },
                            containerColor = Color.White,
                            textContentColor = Color(0xFF111827),
                            titleContentColor = Color(0xFF111827)
                        )
                    }
                }
}
}
}
}

@Composable
fun PinLockScreen(
    correctPin: String,
    onAuthentic: () -> Unit
) {
    var enteredText by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.screenBg)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ZeroBook Ledger Lock",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Enter 4-digit offline PIN parameters to access financial databases",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Visual indicator bullets
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (i in 0 until 4) {
                val active = i < enteredText.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = if (active) MaterialTheme.colorScheme.primary else Color(0xFFE5E5E5),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }
        }

        if (hasError) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Incorrect safety PIN! Try again.", color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Grid PIN panel keys
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.width(240.dp)
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("CLR", "0", "OK")
            )
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { k ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clickable {
                                    hasError = false
                                    when (k) {
                                        "CLR" -> {
                                            enteredText = ""
                                        }

                                        "OK" -> {
                                            if (enteredText == correctPin) {
                                                onAuthentic()
                                            } else {
                                                hasError = true
                                                enteredText = ""
                                            }
                                        }

                                        else -> {
                                            if (enteredText.length < 4) {
                                                enteredText += k
                                                if (enteredText.length == 4 && enteredText == correctPin) {
                                                    onAuthentic()
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = k,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (k == "CLR" || k == "OK") MaterialTheme.colorScheme.primary else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
