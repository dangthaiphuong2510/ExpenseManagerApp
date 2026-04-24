package com.example.expensemanager.core.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.expensemanager.R
import com.example.expensemanager.core.network.ConnectivityObserver
import com.example.expensemanager.core.network.NetworkConnectivityObserver
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.designsystem.theme.ExpenseRed
import com.example.expensemanager.feature.authentication.login.LoginScreen
import com.example.expensemanager.feature.authentication.register.RegisterScreen
import com.example.expensemanager.feature.budget.BudgetScreen
import com.example.expensemanager.feature.category.CategoryScreen
import com.example.expensemanager.feature.currency.CurrencySelectionScreen
import com.example.expensemanager.feature.history.HistoryScreen
import com.example.expensemanager.feature.home.HomeScreen
import com.example.expensemanager.feature.report.ReportScreen
import com.example.expensemanager.feature.setting.SettingScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(
    navController: NavHostController,
    connectivityObserver: NetworkConnectivityObserver,
    auth: FirebaseAuth,
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    isCurrencySet: Boolean
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val networkStatus by connectivityObserver.observe().collectAsState(
        initial = ConnectivityObserver.Status.Available
    )
    val isOnline = networkStatus == ConnectivityObserver.Status.Available

    val startDestination = remember(auth.currentUser, isCurrencySet) {
        if (auth.currentUser != null) {
            if (isCurrencySet) AppDestination.Home.route
            else "currency_selection?isFromSetting=false"
        } else {
            "login"
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem("Home", AppIcons.NavHome, AppDestination.Home.route),
        BottomNavItem("Budget", AppIcons.NavBudget, AppDestination.Budget.route),
        BottomNavItem("Category", AppIcons.NavCategory, AppDestination.Category.route),
        BottomNavItem("Report", AppIcons.NavReport, AppDestination.Report.route),
        BottomNavItem("Setting", AppIcons.NavSettings, AppDestination.Setting.route)
    )

    val currentRoute = currentDestination?.route ?: ""
    val isSelectionMode = currentRoute.contains("selectionMode=true")
    val showBottomBar =
        bottomNavItems.any { it.route == currentRoute.split("?")[0] } && !isSelectionMode

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route?.split("?")?.get(0) == item.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                AppIcons.MyIcon(
                                    resourceId = item.icon,
                                    tint = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                                )
                            },
                            selected = selected,
                            alwaysShowLabel = true,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = 0.5f
                                )
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {

                composable("login") {
                    LoginScreen(
                        isOnline = isOnline,
                        onLoginSuccess = {
                            val dest = if (isCurrencySet) AppDestination.Home.route
                            else "currency_selection?isFromSetting=false"
                            navController.navigate(dest) {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onGoToRegister = { navController.navigate("register") }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        isOnline = isOnline,
                        onRegisterSuccess = {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
                            }
                        },
                        onGoToLogin = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "currency_selection?isFromSetting={isFromSetting}",
                    arguments = listOf(
                        navArgument("isFromSetting") {
                            type = NavType.BoolType
                            defaultValue = false
                        }
                    )
                ) { backStackEntry ->
                    val isFromSetting = backStackEntry.arguments?.getBoolean("isFromSetting") ?: false

                    CurrencySelectionScreen(
                        isFromSetting = isFromSetting,
                        onFinished = {
                            if (isFromSetting) {
                                navController.popBackStack()
                            } else {
                                navController.navigate(AppDestination.Home.route) {
                                    popUpTo("currency_selection") { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable(AppDestination.Home) {
                    HomeScreen(
                        onNavigateToHistory = { navController.navigate(AppDestination.History.route) },
                        onNavigateToBudget = { navController.navigate(AppDestination.Budget.route) },
                        onNavigateToReport = { navController.navigate(AppDestination.Report.route) },
                        onNavigateToSetting = { navController.navigate(AppDestination.Setting.route) },
                        onNavigateToAddTransaction = {
                            navController.navigate("${AppDestination.Category.route}?selectionMode=true")
                        },
                        isOnline = isOnline
                    )
                }

                composable(
                    route = "${AppDestination.Category.route}?selectionMode={selectionMode}",
                    arguments = listOf(
                        navArgument("selectionMode") {
                            type = NavType.BoolType
                            defaultValue = false
                        }
                    )
                ) { backStackEntry ->
                    val selectionMode =
                        backStackEntry.arguments?.getBoolean("selectionMode") ?: false
                    CategoryScreen(
                        isSelectionMode = selectionMode,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(AppDestination.History) {
                    HistoryScreen(onBack = { navController.popBackStack() })
                }

                composable(AppDestination.Budget) { BudgetScreen() }
                composable(AppDestination.Report) { ReportScreen() }

                composable(AppDestination.Setting) {
                    SettingScreen(
                        isOnline = isOnline,
                        currentTheme = currentTheme,
                        onThemeChange = onThemeChange,
                        onNavigateToCurrencySelection = {
                            // Khi đi từ Setting, truyền isFromSetting = true
                            navController.navigate("currency_selection?isFromSetting=true")
                        },
                        onLogout = {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = !isOnline,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ExpenseRed)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_internet_connection),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: Int,
    val route: String
)

fun NavGraphBuilder.composable(
    destination: AppDestination,
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = destination.route,
        arguments = destination.arguments,
        deepLinks = deepLinks,
        content = content,
    )
}