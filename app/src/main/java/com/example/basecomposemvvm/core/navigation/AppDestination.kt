package com.example.basecomposemvvm.core.navigation

import androidx.navigation.NamedNavArgument

/**
 * Sealed class representing all navigation destinations in the app.
 *
 * Each destination defines its route, optional arguments, and parcelable argument support.
 * New screens should be added as objects or data classes extending AppDestination.
 */
sealed class AppDestination(val route: String = "") {

    open val arguments: List<NamedNavArgument> = emptyList()

    open var destination: String = route

    open var parcelableArgument: Pair<String, Any?> = "" to null

    /** Navigate up / back in the navigation stack. */
    data object Up : AppDestination()

    /** Splash screen — default startDestination. */
    data object Splash : AppDestination("splash")

    /** Home screen. */
    data object Home : AppDestination("home")
}
