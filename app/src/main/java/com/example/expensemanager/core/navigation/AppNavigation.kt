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
import com.example.expensemanager.R
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
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
    isCurrencySet: Boolean // Giá trị từ DataStore truyền từ MainActivity
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val networkStatus by connectivityObserver.observe().collectAsState(
        initial = ConnectivityObserver.Status.Available
    )
    val isOnline = networkStatus == ConnectivityObserver.Status.Available

    val startDestination = remember(auth.currentUser, isCurrencySet) {
        if (auth.currentUser != null) {
            if (isCurrencySet) AppDestination.Home.route else "currency_selection"
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

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    bottomNavItems.forEach { item ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                AppIcons.MyIcon(
                                    resourceId = item.icon,
                                    tint = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f)
                                )
                            },
                            selected = selected,
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
                            }
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
                        if (isCurrencySet) {
                            navController.navigate(AppDestination.Home.route) {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            navController.navigate("currency_selection") {
                                popUpTo("login") { inclusive = true }
                            }
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

                composable("currency_selection") {
                    CurrencySelectionScreen(
                        onFinished = {
                            val hasBackstack = navController.previousBackStackEntry != null
                            val fromSetting = navController.previousBackStackEntry?.destination?.route == AppDestination.Setting.route

                            if (fromSetting) {
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
                        onNavigateToAddTransaction = { navController.navigate("add_transaction") },
                        isOnline = isOnline
                    )
                }

                composable(AppDestination.History) {
                    HistoryScreen(
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(AppDestination.Category) {
                    CategoryScreen()
                }

                composable(AppDestination.Budget) {
                    BudgetScreen()
                }

                composable(AppDestination.Report) {
                    ReportScreen()
                }

                composable(AppDestination.Setting) {
                    SettingScreen(
                        isOnline = isOnline,
                        currentTheme = currentTheme,
                        onThemeChange = onThemeChange,
                        onNavigateToCurrencySelection = {
                            navController.navigate("currency_selection")
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