package com.example.basecomposemvvm.core.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.basecomposemvvm.designsystem.theme.AppIcons
import com.example.basecomposemvvm.feature.budget.BudgetScreen
import com.example.basecomposemvvm.feature.category.CategoryScreen
import com.example.basecomposemvvm.feature.home.HistoryScreen
import com.example.basecomposemvvm.feature.home.HomeScreen
import com.example.basecomposemvvm.feature.report.ReportScreen
import com.example.basecomposemvvm.feature.setting.SettingScreen
import com.example.basecomposemvvm.feature.splash.SplashScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = remember {
        listOf(
            BottomNavItem("Home", AppIcons.NavHome, AppDestination.Home.route),
            BottomNavItem("Budget", AppIcons.NavBudget, AppDestination.Budget.route),
            BottomNavItem("Category", AppIcons.NavCategory, AppDestination.Category.route),
            BottomNavItem("Report", AppIcons.NavReport, AppDestination.Report.route),
            BottomNavItem("Setting", AppIcons.NavSettings, AppDestination.Setting.route)
        )
    }

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
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
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
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Splash.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(AppDestination.Splash) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(AppDestination.Home.route) {
                            popUpTo(AppDestination.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(AppDestination.Home) {
                HomeScreen(
                    onNavigateToHistory = { navController.navigate(AppDestination.History.route) },
                    onNavigateToBudget = { navController.navigate(AppDestination.Budget.route) },
                    onNavigateToReport = { navController.navigate(AppDestination.Report.route) },
                    onNavigateToSetting = { navController.navigate(AppDestination.Setting.route) }
                )
            }

            composable(AppDestination.History) {
                HistoryScreen(onBack = { navController.popBackStack() })
            }

            composable(AppDestination.Category) { CategoryScreen() }
            composable(AppDestination.Budget) { BudgetScreen() }
            composable(AppDestination.Report) { ReportScreen() }
            composable(AppDestination.Setting) { SettingScreen() }
        }
    }
}

/**
 * Data class cho các mục của Bottom Navigation
 */
data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

/**
 * Extension function để viết code NavHost ngắn gọn hơn với AppDestination
 */
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