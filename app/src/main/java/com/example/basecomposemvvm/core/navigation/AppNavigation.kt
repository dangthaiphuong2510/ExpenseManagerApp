package com.example.basecomposemvvm.core.navigation

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
import androidx.navigation.compose.*
import com.example.basecomposemvvm.R
import com.example.basecomposemvvm.core.network.ConnectivityObserver
import com.example.basecomposemvvm.core.network.NetworkConnectivityObserver
import com.example.basecomposemvvm.designsystem.theme.AppIcons
import com.example.basecomposemvvm.designsystem.theme.ExpenseRed
import com.example.basecomposemvvm.feature.authentication.LoginScreen
import com.example.basecomposemvvm.feature.authentication.RegisterScreen
import com.example.basecomposemvvm.feature.budget.BudgetScreen
import com.example.basecomposemvvm.feature.category.CategoryScreen
import com.example.basecomposemvvm.feature.home.HistoryScreen
import com.example.basecomposemvvm.feature.home.HomeScreen
import com.example.basecomposemvvm.feature.report.ReportScreen
import com.example.basecomposemvvm.feature.setting.SettingScreen
import com.example.basecomposemvvm.feature.splash.SplashScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(
    navController: NavHostController,
    connectivityObserver: NetworkConnectivityObserver,
    auth: FirebaseAuth
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val networkStatus by connectivityObserver.observe().collectAsState(
        initial = ConnectivityObserver.Status.Available
    )
    val isOnline = networkStatus == ConnectivityObserver.Status.Available

    val bottomNavItems = listOf(
        BottomNavItem("Home", AppIcons.NavHome, AppDestination.Home.route),
        BottomNavItem("Budget", AppIcons.NavBudget, AppDestination.Budget.route),
        BottomNavItem("Category", AppIcons.NavCategory, AppDestination.Category.route),
        BottomNavItem("Report", AppIcons.NavReport, AppDestination.Report.route),
        BottomNavItem("Setting", AppIcons.NavSettings, AppDestination.Setting.route)
    )

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
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
        Column(modifier = Modifier.padding(padding)) {

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

            NavHost(
                navController = navController,
                startDestination = AppDestination.Splash.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(AppDestination.Splash) {
                    SplashScreen(
                        onNavigateToHome = {
                            val destination = if (auth.currentUser != null) {
                                AppDestination.Home.route
                            } else {
                                "login"
                            }
                            navController.navigate(destination) {
                                popUpTo(AppDestination.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable("login") {
                    LoginScreen(
                        isOnline = isOnline,
                        onLoginSuccess = {
                            navController.navigate(AppDestination.Home.route) {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onGoToRegister = {
                            navController.navigate("register")
                        }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        isOnline = isOnline,
                        onRegisterSuccess = {
                            navController.popBackStack()
                        },
                        onGoToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(AppDestination.Home) {
                    HomeScreen(
                        isOnline = isOnline,
                        onNavigateToHistory = { navController.navigate(AppDestination.History.route) },
                        onNavigateToBudget = { navController.navigate(AppDestination.Budget.route) },
                        onNavigateToReport = { navController.navigate(AppDestination.Report.route) },
                        onNavigateToSetting = { navController.navigate(AppDestination.Setting.route) }
                    )
                }

                composable(AppDestination.History) {
                    HistoryScreen(
                        onBack = { navController.popBackStack() },
                        isOnline = isOnline
                    )
                }

                composable(AppDestination.Category) {
                    CategoryScreen(isOnline = isOnline)
                }

                composable(AppDestination.Budget) {
                    BudgetScreen(isOnline = isOnline)
                }

                composable(AppDestination.Report) {
                    ReportScreen()
                }

                composable(AppDestination.Setting) {
                    SettingScreen(
                        isOnline = isOnline,
                        onLogout = {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
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