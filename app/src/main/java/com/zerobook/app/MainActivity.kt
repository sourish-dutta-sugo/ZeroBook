package com.zerobook.app

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zerobook.app.data.AppPreferences
import com.zerobook.app.data.ChangelogData
import com.zerobook.app.data.ChangelogLoader
import com.zerobook.app.ui.AppViewModel
import com.zerobook.app.ui.DashboardViewModel
import com.zerobook.app.ui.animation.PremiumBottomNavContent
import com.zerobook.app.ui.animation.premiumClickable
import com.zerobook.app.ui.animation.premiumDialogEnter
import com.zerobook.app.ui.animation.premiumDialogExit
import com.zerobook.app.ui.animation.premiumEnterTransition
import com.zerobook.app.ui.animation.premiumExitTransition
import com.zerobook.app.ui.screens.BankCashScreen
import com.zerobook.app.ui.screens.DashboardScreen
import com.zerobook.app.ui.screens.ExpensesScreen
import com.zerobook.app.ui.screens.InvoiceScreen
import com.zerobook.app.ui.screens.LedgerListScreen
import com.zerobook.app.ui.screens.NewVoucherScreen
import com.zerobook.app.ui.screens.PartiesScreen
import com.zerobook.app.ui.screens.PartyDetailScreen
import com.zerobook.app.ui.screens.ProductsScreen
import com.zerobook.app.ui.screens.QuickSaleScreen
import com.zerobook.app.ui.screens.ReportsScreen
import com.zerobook.app.ui.screens.SettingsScreen
import com.zerobook.app.ui.screens.SetupScreen
import com.zerobook.app.ui.screens.SplashScreen
import com.zerobook.app.ui.screens.VouchersScreen
import com.zerobook.app.ui.theme.AppColors
import com.zerobook.app.ui.theme.LocalAppTheme
import com.zerobook.app.ui.theme.ZeroBookTheme
import com.zerobook.app.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

private object Routes {
    const val Dashboard = "dashboard"
    const val Vouchers = "vouchers"
    const val Parties = "parties"
    const val Settings = "settings"
    const val Reports = "reports"
    const val LedgerBooks = "ledger_books"
    const val Expenses = "expenses"
    const val QuickSale = "quick_sale"
    const val Products = "products"
    const val BankCash = "bank_cash"
    const val NewVoucher = "new_voucher?voucherId={voucherId}"
    const val NewVoucherBase = "new_voucher"
    const val Invoice = "invoice/{voucherId}"
    const val PartyDetail = "party_detail/{partyId}"
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.Dashboard, "Dashboard", Icons.Default.GridView),
    TopLevelDestination(Routes.Vouchers, "Vouchers", Icons.AutoMirrored.Filled.Assignment),
    TopLevelDestination(Routes.Parties, "Parties", Icons.Default.Group),
    TopLevelDestination(Routes.Settings, "Settings", Icons.Default.Settings)
)

private fun newVoucherRoute(voucherId: String? = null): String =
    voucherId?.let { "${Routes.NewVoucherBase}?voucherId=$it" } ?: Routes.NewVoucherBase

private fun invoiceRoute(voucherId: String): String = "invoice/$voucherId"

private fun partyDetailRoute(partyId: String): String = "party_detail/$partyId"

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
                ZeroBookTheme(appTheme = currentTheme) {
                    MainAppEntry(viewModel, themeViewModel, dashboardViewModel)
                }
            }
        }
    }
}

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
            AppContent(
                viewModel = viewModel,
                themeViewModel = themeViewModel,
                dashboardViewModel = dashboardViewModel
            )
        }
    }
}

@Composable
private fun AppContent(
    viewModel: AppViewModel,
    themeViewModel: ThemeViewModel,
    dashboardViewModel: DashboardViewModel
) {
    val context = LocalContext.current
    val isSetupCompleted by viewModel.isSetupCompleted.collectAsState()
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
        return
    }

    val sharedPreferences = remember {
        context.getSharedPreferences("zerobook_pref", Context.MODE_PRIVATE)
    }
    var pinRequired by remember {
        mutableStateOf(sharedPreferences.getBoolean("pin_enabled", false))
    }
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
            pinRequired = sharedPreferences.getBoolean("pin_enabled", false)
        }
    }

    when {
        !isSetupCompleted -> {
            SetupScreen(
                viewModel = viewModel,
                onSetupComplete = {}
            )
        }

        pinRequired && !pinAuthed -> {
            PinLockScreen(
                correctPin = sharedPreferences.getString("lock_pin", "1234") ?: "1234",
                onAuthentic = { pinAuthed = true }
            )
        }

        else -> {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val currentRoute = currentDestination?.route
            val isTopLevel = topLevelDestinations.any { destination ->
                currentDestination?.hierarchy?.any { it.route == destination.route } == true
            }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                containerColor = AppColors.screenBg,
                bottomBar = {
                    if (isTopLevel) {
                        NavigationBar(
                            containerColor = AppColors.cardBg,
                            tonalElevation = 6.dp,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .height(72.dp)
                        ) {
                            topLevelDestinations.forEach { destination ->
                                val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navController.navigateToTopLevel(destination.route) },
                                    icon = {
                                        PremiumBottomNavContent(
                                            selected = selected,
                                            icon = destination.icon,
                                            label = destination.label
                                        )
                                    },
                                    label = {},
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
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ZeroBookNavHost(
                        navController = navController,
                        viewModel = viewModel,
                        themeViewModel = themeViewModel,
                        dashboardViewModel = dashboardViewModel
                    )
                }
            }

            if (showChangelog && changelogData != null) {
                val configuration = LocalConfiguration.current
                androidx.compose.animation.AnimatedVisibility(
                    visible = showChangelog,
                    enter = premiumDialogEnter(),
                    exit = premiumDialogExit()
                ) {
                    androidx.compose.material3.AlertDialog(
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

@Composable
private fun ZeroBookNavHost(
    navController: NavHostController,
    viewModel: AppViewModel,
    themeViewModel: ThemeViewModel,
    dashboardViewModel: DashboardViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Dashboard,
        enterTransition = premiumEnterTransition(navigatingBack = false),
        exitTransition = premiumExitTransition(navigatingBack = false),
        popEnterTransition = premiumEnterTransition(navigatingBack = true),
        popExitTransition = premiumExitTransition(navigatingBack = true)
    ) {
        composable(Routes.Dashboard) {
            DashboardScreen(
                viewModel = viewModel,
                dashboardViewModel = dashboardViewModel,
                isDesktop = false,
                onQuickAction = { action ->
                    when (action) {
                        "SALE", "PURCHASE" -> navController.navigate(newVoucherRoute())
                        "RECEIPT", "PAYMENT" -> navController.navigate(Routes.BankCash)
                        "REPORTS" -> navController.navigate(Routes.Reports)
                        "QUICK_SALE" -> navController.navigate(Routes.QuickSale)
                        "EXPENSES" -> navController.navigate(Routes.Expenses)
                        "PARTY" -> navController.navigateToTopLevel(Routes.Parties)
                    }
                }
            )
        }

        composable(Routes.Vouchers) {
            VouchersScreen(
                viewModel = viewModel,
                isDesktop = false,
                navigateToNewVoucher = { id -> navController.navigate(newVoucherRoute(id)) },
                navigateToInvoice = { id -> navController.navigate(invoiceRoute(id)) }
            )
        }

        composable(Routes.Parties) {
            PartiesScreen(
                viewModel = viewModel,
                isDesktop = false,
                onPartySelected = { id -> navController.navigate(partyDetailRoute(id)) }
            )
        }

        composable(Routes.Settings) {
            SettingsScreen(
                viewModel = viewModel,
                themeViewModel = themeViewModel,
                isDesktop = false,
                navigateToProducts = { navController.navigate(Routes.Products) },
                navigateToLedgerBooks = { navController.navigate(Routes.LedgerBooks) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Reports) {
            ReportsScreen(
                viewModel = viewModel,
                isDesktop = false,
                navigateToLedgerBooks = { navController.navigate(Routes.LedgerBooks) },
                navigateToExpenses = { navController.navigate(Routes.Expenses) },
                navigateToNewVoucher = { navController.navigate(newVoucherRoute(it)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Products) {
            ProductsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.BankCash) {
            BankCashScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Expenses) {
            ExpensesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.QuickSale) {
            QuickSaleScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.NewVoucher,
            arguments = listOf(
                navArgument("voucherId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            NewVoucherScreen(
                viewModel = viewModel,
                voucherId = entry.arguments?.getString("voucherId"),
                isDesktop = false,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInvoice = { voucherId ->
                    navController.popBackStack()
                    navController.navigate(invoiceRoute(voucherId))
                },
                onNavigateToPartyDetail = { id -> navController.navigate(partyDetailRoute(id)) }
            )
        }

        composable(
            route = Routes.Invoice,
            arguments = listOf(navArgument("voucherId") { type = NavType.StringType })
        ) { entry ->
            val voucherId = entry.arguments?.getString("voucherId").orEmpty()
            InvoiceScreen(
                viewModel = viewModel,
                voucherId = voucherId,
                onNavigateBack = { navController.popBackStack() },
                onEditVoucher = { id -> navController.navigate(newVoucherRoute(id)) },
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
                    navController.navigate(newVoucherRoute())
                }
            )
        }

        composable(Routes.LedgerBooks) {
            LedgerListScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PartyDetail,
            arguments = listOf(navArgument("partyId") { type = NavType.StringType })
        ) { entry ->
            PartyDetailScreen(
                viewModel = viewModel,
                partyId = entry.arguments?.getString("partyId").orEmpty(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

private fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
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

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { index ->
                val active = index < enteredText.length
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

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(0.6f)
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
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .premiumClickable {
                                    hasError = false
                                    when (key) {
                                        "CLR" -> enteredText = ""
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
                                                enteredText += key
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
                                text = key,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (key == "CLR" || key == "OK") {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Black
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
