package com.example.basecomposemvvm.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.basecomposemvvm.designsystem.theme.AppTheme

@Composable
fun BottomNav(navController: NavHostController) {

    val items = listOf(
        Triple("home", "Home", Icons.Default.Home),
        Triple("budget", "Budget", Icons.Default.AccountBalanceWallet),
        Triple("category", "Category", Icons.Default.Category),
        Triple("report", "Report", Icons.Default.PieChart),
        Triple("setting", "Setting", Icons.Default.Settings)
    )

    // Lấy route hiện tại
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {

        items.forEach { (route, label, icon) ->

            NavigationBarItem(
                selected = currentRoute == route,

                onClick = {
                    navController.navigate(route) {

                        popUpTo(navController.graph.findStartDestination().id)

                        launchSingleTop = true
                    }
                },

                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label
                    )
                },

                label = {
                    Text(
                        text = label,
                        fontWeight = if (currentRoute == route)
                            FontWeight.Bold
                        else
                            FontWeight.Normal
                    )
                }
            )
        }
    }
}
