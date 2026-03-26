package com.example.basecomposemvvm.core.navigation

import androidx.navigation.NamedNavArgument

sealed class AppDestination(val route: String) {

    open val arguments: List<NamedNavArgument> = emptyList()

    /** Navigate up / back */
    object Up : AppDestination("up")

    /** Splash screen */
    object Splash : AppDestination("splash")

    /** Home screen */
    object Home : AppDestination("home")

    /** Budget screen */
    object Budget : AppDestination("budget")

    /** Category screen */
    object Category : AppDestination("category")

    /** Report screen */
    object Report : AppDestination("report")

    /** Setting screen */
    object Setting : AppDestination("setting")
}