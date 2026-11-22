package com.example.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.R
import com.example.ads.AdManager
import com.example.ads.BannerAdSlot
import com.example.ads.ConsentManager
import com.example.domain.model.DailyTool
import com.example.ui.DailyFixViewModel
import com.example.ui.screens.AllToolsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.tools.AgeCalculatorScreen
import com.example.ui.screens.tools.BmiCalculatorScreen
import com.example.ui.screens.tools.DateCalculatorScreen
import com.example.ui.screens.tools.DiscountCalculatorScreen
import com.example.ui.screens.tools.FuelCostCalculatorScreen
import com.example.ui.screens.tools.LoanCalculatorScreen
import com.example.ui.screens.tools.PercentageCalculatorScreen
import com.example.ui.screens.tools.SmartCalculatorScreen
import com.example.ui.screens.tools.TipCalculatorScreen
import com.example.ui.screens.tools.UnitConverterScreen

private data class TopLevelTab(
    val route: Any,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun DailyFixAppShell(
    viewModel: DailyFixViewModel,
    adManager: AdManager,
    consentManager: ConsentManager,
    onShowPrivacyOptions: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val canRequestAds by consentManager.canRequestAds.collectAsStateWithLifecycle()
    val isSdkInitialized by adManager.isSdkInitialized.collectAsStateWithLifecycle()
    val isPrivacyOptionsRequired by consentManager.isPrivacyOptionsRequired.collectAsStateWithLifecycle()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val context = LocalContext.current
    val activity = context as? Activity

    val tabs = listOf(
        TopLevelTab(HomeRoute, R.string.nav_home, Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
        TopLevelTab(AllToolsRoute, R.string.nav_tools, Icons.Rounded.GridView, Icons.Outlined.GridView, "nav_tools"),
        TopLevelTab(HistoryRoute, R.string.nav_history, Icons.Rounded.History, Icons.Outlined.History, "nav_history"),
        TopLevelTab(SettingsRoute, R.string.nav_settings, Icons.Rounded.Settings, Icons.Outlined.Settings, "nav_settings")
    )

    val isTopLevelDestination = currentDestination == null ||
        currentDestination.hasRoute<HomeRoute>() ||
        currentDestination.hasRoute<AllToolsRoute>() ||
        currentDestination.hasRoute<HistoryRoute>() ||
        currentDestination.hasRoute<SettingsRoute>()

    fun navigateToTab(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openTool(tool: DailyTool) {
        viewModel.recordToolOpened(tool)
        navController.navigate(ToolRoute(tool.id))
    }

    fun exitToolWithNaturalTransition() {
        adManager.maybeShowOnNaturalTransition(
            activity = activity,
            canRequestAds = canRequestAds
        ) {
            navController.popBackStack()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail && isTopLevelDestination) {
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Spacer(modifier = Modifier.weight(0.1f))
                    tabs.forEach { tab ->
                        val selected = when (tab.route) {
                            is HomeRoute -> currentDestination?.hasRoute<HomeRoute>() == true
                            is AllToolsRoute -> currentDestination?.hasRoute<AllToolsRoute>() == true
                            is HistoryRoute -> currentDestination?.hasRoute<HistoryRoute>() == true
                            is SettingsRoute -> currentDestination?.hasRoute<SettingsRoute>() == true
                            else -> false
                        }
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateToTab(tab.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = stringResource(tab.labelRes)
                                )
                            },
                            label = { Text(stringResource(tab.labelRes)) },
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (isTopLevelDestination) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            BannerAdSlot(
                                canRequestAds = canRequestAds,
                                isSdkInitialized = isSdkInitialized,
                                modifier = if (useRail) Modifier.navigationBarsPadding() else Modifier
                            )
                            if (!useRail) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    windowInsets = WindowInsets.navigationBars
                                ) {
                                    tabs.forEach { tab ->
                                        val selected = when (tab.route) {
                                            is HomeRoute -> currentDestination?.hasRoute<HomeRoute>() == true
                                            is AllToolsRoute -> currentDestination?.hasRoute<AllToolsRoute>() == true
                                            is HistoryRoute -> currentDestination?.hasRoute<HistoryRoute>() == true
                                            is SettingsRoute -> currentDestination?.hasRoute<SettingsRoute>() == true
                                            else -> false
                                        }
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { navigateToTab(tab.route) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = stringResource(tab.labelRes)
                                                )
                                            },
                                            label = { Text(stringResource(tab.labelRes)) },
                                            modifier = Modifier.testTag(tab.testTag)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                val topLevelModifier = if (isTopLevelDestination) {
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .statusBarsPadding()
                } else {
                    Modifier.fillMaxSize()
                }

                Box(modifier = topLevelModifier) {
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute
                    ) {
                        composable<HomeRoute> {
                            HomeScreen(
                                uiState = uiState,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onCategorySelected = viewModel::selectCategory,
                                onOpenTool = ::openTool,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onOpenAllTools = { navigateToTab(AllToolsRoute) }
                            )
                        }
                        composable<AllToolsRoute> {
                            AllToolsScreen(
                                uiState = uiState,
                                onOpenTool = ::openTool,
                                onToggleFavorite = viewModel::toggleFavorite
                            )
                        }
                        composable<HistoryRoute> {
                            HistoryScreen(
                                uiState = uiState,
                                onOpenTool = ::openTool,
                                onDeleteHistoryItem = viewModel::deleteHistoryItem,
                                onClearHistory = viewModel::clearHistory
                            )
                        }
                        composable<SettingsRoute> {
                            SettingsScreen(
                                uiState = uiState,
                                isPrivacyOptionsRequired = isPrivacyOptionsRequired,
                                onThemeModeChange = viewModel::setThemeMode,
                                onPrivacyPolicyUrlChange = viewModel::setPrivacyPolicyUrl,
                                onOpenPrivacyPolicy = { navController.navigate(PrivacyPolicyRoute) },
                                onOpenAdPrivacyOptions = onShowPrivacyOptions
                            )
                        }
                        composable<PrivacyPolicyRoute> {
                            PrivacyPolicyScreen(
                                privacyPolicyUrl = uiState.privacyPolicyUrl,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable<ToolRoute> { entry ->
                            val route = entry.toRoute<ToolRoute>()
                            val tool = DailyTool.fromId(route.toolId) ?: DailyTool.SMART_CALCULATOR
                            val isFavorite = tool.id in uiState.favoriteToolIds

                            BackHandler {
                                exitToolWithNaturalTransition()
                            }

                            when (tool) {
                                DailyTool.SMART_CALCULATOR -> SmartCalculatorScreen(
                                    isFavorite = isFavorite,
                                    onToggleFavorite = { viewModel.toggleFavorite(tool) },
                                    onSaveHistory = { expr, res -> viewModel.saveCalculation(tool, expr, res) },
                                    onBack = ::exitToolWithNaturalTransition,
                                    canRequestAds = canRequestAds,
                                    isSdkInitialized = isSdkInitialized
                                )
                                DailyTool.PERCENTAGE_CALCULATOR -> PercentageCalculatorScreen(
                                    isFavorite = isFavorite,
                                    onToggleFavorite = { viewModel.toggleFavorite(tool) },
                                    onSaveHistory = { expr, res -> viewModel.saveCalculation(tool, expr, res) },
                                    onBack = ::exitToolWithNaturalTransition,
                                    canRequestAds = canRequestAds,
                                    isSdkInitialized = isSdkInitialized
                                )
                                DailyTool.DISCOUNT_CALCULATOR -> DiscountCalculatorScreen(
                                    isFavorite = isFavorite,
                                    onToggleFavorite = { viewModel.toggleFavorite(tool) },
                                    onSaveHistory = { expr, res -> viewModel.saveCalculation(tool, expr, res) },
                                    onBack = ::exitToolWithNaturalTransition,
                                    canRequestAds = canRequestAds,
                                    isSdkInitialized = isSdkInitialized
                                )
                                DailyTool.UNIT_CONVERTER -> UnitConverterScreen(
                                    isFavorite = isFavorite,
                                    onToggleFavorite = { viewModel.toggleFavorite(tool) },
                                    onSaveHistory = { expr, res -> viewModel.saveCalculation(tool, expr, res) },
                                    onBack = ::exitToolWithNaturalTransition,
                                    canRequestAds = canRequestAds,
                                    isSdkInitialized = isSdkInitialized
                                )
                                DailyTool.AGE_CALCULATOR -> AgeCalculatorScreen(
                                    isFavorite = isFavorite,
                                    onToggleFavorite = { viewModel.toggleFavorite(tool) },
                                    onSaveHistory = { expr, res -> viewModel.saveCalculation(tool, expr, res) },
                                    onBack = ::exitToolWithNaturalTransition,
                                    canRequestAds = canRequestAds,
                                    isSdkInitialized = isSdkInitialized
                                )
                                DailyTool.DATE_CALCULATOR -> DateCalculatorScreen(
                                    isFavorite = isFavorite,
                                    onToggleFavorite = { viewModel.toggleFavorite(tool) },
                                    onSaveHistory = { expr, res -> viewModel.saveCalculation(tool, expr, res) },
                                    onBack = ::exitToolWithNaturalTransition,
                                    canRequestAds = canRequestAds,
                                    isSdkInitialized = isSdkInitialized
                                )
                                DailyTool.BMI_CALCULATOR -> BmiCalculatorScreen(
                                    isFavorite = isFavorite,
                                    onToggleFavorite = { viewModel.toggleFavorite(tool) },
                                    onSaveHistory = { expr, res -> viewModel.saveCalculation(tool, expr, res) },
                                    onBack = ::exitToolWithNaturalTransition,
                                    canRequestAds = canRequestAds,
                                    isSdkInitialized = isSdkInitialized
                                )
                                DailyTool.TIP_CALCULATOR -> TipCalculatorScreen(
                                    isFavorite = isFavorite,
                                    onToggleFavorite = { viewModel.toggleFavorite(tool) },
                                    onSaveHistory = { expr, res -> viewModel.saveCalculation(tool, expr, res) },
                                    onBack = ::exitToolWithNaturalTransition,
                                    canRequestAds = canRequestAds,
                                    isSdkInitialized = isSdkInitialized
                                )
                                DailyTool.LOAN_CALCULATOR -> LoanCalculatorScreen(
                                    isFavorite = isFavorite,
                                    onToggleFavorite = { viewModel.toggleFavorite(tool) },
                                    onSaveHistory = { expr, res -> viewModel.saveCalculation(tool, expr, res) },
                                    onBack = ::exitToolWithNaturalTransition,
                                    canRequestAds = canRequestAds,
                                    isSdkInitialized = isSdkInitialized
                                )
                                DailyTool.FUEL_COST_CALCULATOR -> FuelCostCalculatorScreen(
                                    isFavorite = isFavorite,
                                    onToggleFavorite = { viewModel.toggleFavorite(tool) },
                                    onSaveHistory = { expr, res -> viewModel.saveCalculation(tool, expr, res) },
                                    onBack = ::exitToolWithNaturalTransition,
                                    canRequestAds = canRequestAds,
                                    isSdkInitialized = isSdkInitialized
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
