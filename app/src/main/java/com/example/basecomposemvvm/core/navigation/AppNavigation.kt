package com.example.basecomposemvvm.core.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.basecomposemvvm.feature.splash.SplashScreen

/**
 * Root navigation host for the app.
 *
 * Sets up the NavHost with [AppDestination.Splash] as the start destination.
 * Register all screen routes inside the NavHost builder.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Splash.route,
        modifier = modifier,
    ) {
        composable(AppDestination.Splash) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(AppDestination.Home) {
            // TODO: Replace with actual HomeScreen composable
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Home")
            }
        }
    }
}

/**
 * Convenience extension to register an [AppDestination] as a composable route.
 *
 * Usage:
 * ```
 * composable(AppDestination.Home) { backStackEntry ->
 *     HomeScreen()
 * }
 * ```
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
