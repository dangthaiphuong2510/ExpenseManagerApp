package com.example.expensemanager.core.navigation

import androidx.navigation.NamedNavArgument

sealed class AppDestination(val route: String) {

    open val arguments: List<NamedNavArgument> = emptyList()

    object Up : AppDestination("up")

    object Splash : AppDestination("splash")

    object Home : AppDestination("home")

    object History : AppDestination("history")

    object Budget : AppDestination("budget")

    object Category : AppDestination("category")

    object Report : AppDestination("report")

    object Setting : AppDestination("setting")
}