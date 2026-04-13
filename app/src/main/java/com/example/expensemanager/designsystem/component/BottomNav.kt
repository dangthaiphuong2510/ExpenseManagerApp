package com.example.expensemanager.designsystem.component

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.expensemanager.designsystem.theme.AppIcons

@Composable
fun BottomNav(navController: NavHostController) {

    val items = listOf(
        Triple("home", "Home", AppIcons.NavHome),
        Triple("budget", "Budget", AppIcons.NavBudget),
        Triple("category", "Category", AppIcons.NavCategory),
        Triple("report", "Report", AppIcons.NavReport),
        Triple("setting", "Setting", AppIcons.NavSettings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {

        items.forEach { (route, label, icon) ->
            val isSelected = currentRoute == route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(

                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,

                    indicatorColor = MaterialTheme.colorScheme.primary,

                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),

                ),
                icon = {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = label,
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}