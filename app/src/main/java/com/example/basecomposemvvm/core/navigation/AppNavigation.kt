package com.example.basecomposemvvm.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.basecomposemvvm.designsystem.theme.AppIcons
import com.example.basecomposemvvm.designsystem.theme.BrownSoft
import com.example.basecomposemvvm.feature.budget.BudgetScreen
import com.example.basecomposemvvm.feature.category.CategoryScreen
import com.example.basecomposemvvm.feature.home.HomeScreen
import com.example.basecomposemvvm.feature.report.ReportScreen
import com.example.basecomposemvvm.feature.setting.SettingScreen

object Routes {
    const val HOME = "home"
    const val BUDGET = "budget"
    const val CATEGORY = "category"
    const val REPORT = "report"
    const val SETTING = "setting"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                BottomNavItem.entries.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                contentDescription = item.label,
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Black else FontWeight.Medium
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
                            selectedIconColor = BrownSoft,
                            selectedTextColor = BrownSoft,
                            indicatorColor = BrownSoft.copy(alpha = 0.12f),
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { padding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) { HomeScreen() }
        composable(Routes.BUDGET) { BudgetScreen() }
        composable(Routes.CATEGORY) { CategoryScreen() }
        composable(Routes.REPORT) { ReportScreen() }
        composable(Routes.SETTING) { SettingScreen() }
    }
}

enum class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
) {
    Home("Home", AppIcons.NavHome, Routes.HOME),
    Budget("Budget", AppIcons.NavBudget, Routes.BUDGET),
    Category("Category", AppIcons.NavCategory, Routes.CATEGORY),
    Report("Report", AppIcons.NavReport, Routes.REPORT),
    Setting("Setting", AppIcons.NavSettings, Routes.SETTING)
}
@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    AppNavigation()
}