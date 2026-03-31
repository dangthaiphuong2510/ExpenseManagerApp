package com.example.basecomposemvvm.designsystem.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

object AppIcons {

   //cate icons
    val CategoryIconsList: List<Pair<String, ImageVector>> = listOf(
        "Food" to Icons.Rounded.Restaurant,
        "Transport" to Icons.Rounded.DirectionsCar,
        "Clothes" to Icons.Rounded.Checkroom,
        "Cosmetics" to Icons.Rounded.AutoFixHigh,
        "Education" to Icons.Rounded.School,
        "Home" to Icons.Rounded.Home,
        "Health" to Icons.Rounded.MedicalServices,
        "Salary" to Icons.Rounded.Payments,
        "Shopping" to Icons.Rounded.ShoppingBag,
        "Bill" to Icons.Rounded.ReceiptLong,
        "Other" to Icons.Rounded.MoreHoriz
    )

    fun getIconByName(name: String): ImageVector {
        return CategoryIconsList.find { it.first == name }?.second
            ?: Icons.Rounded.MoreHoriz
    }

    // Navigation Icon
    val NavHome = Icons.Rounded.Home
    val NavBudget = Icons.Rounded.AccountBalanceWallet
    val NavCategory = Icons.Rounded.Category
    val NavReport = Icons.Rounded.PieChart
    val NavSettings = Icons.Rounded.Settings

    //action icons

    val Add = Icons.Rounded.Add
    val Edit = Icons.Rounded.Edit
    val Delete = Icons.Rounded.DeleteOutline
    val ChevronRight = Icons.Rounded.ChevronRight
    val ChevronLeft = Icons.Rounded.ChevronLeft
    val More = Icons.Rounded.MoreVert
    val Notifications = Icons.Rounded.Notifications
    val Info = Icons.Rounded.Info
}