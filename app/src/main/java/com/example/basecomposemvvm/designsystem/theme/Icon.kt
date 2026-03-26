package com.example.basecomposemvvm.designsystem.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

object AppIcons {
    // Icons Categories
    val Food = Icons.Rounded.Restaurant
    val Transport = Icons.Rounded.DirectionsCar
    val Clothes = Icons.Rounded.Checkroom
    val Cosmetics = Icons.Rounded.AutoFixHigh
    val Education = Icons.Rounded.School
    val Home = Icons.Rounded.Home
    val Health = Icons.Rounded.MedicalServices
    val Salary = Icons.Rounded.Payments
    val Other = Icons.Rounded.MoreHoriz
    val Shopping = Icons.Rounded.ShoppingBag
    val Bill = Icons.Rounded.ReceiptLong

    // Icons Navigation
    val NavHome = Icons.Rounded.Home
    val NavBudget = Icons.Rounded.AccountBalanceWallet
    val NavCategory = Icons.Rounded.Category
    val NavReport = Icons.Rounded.PieChart
    val NavSettings = Icons.Rounded.Settings

    // Icons Actions
    val Add = Icons.Rounded.Add
    val Edit = Icons.Rounded.Edit
    val Delete = Icons.Rounded.DeleteOutline
    val ChevronRight = Icons.Rounded.ChevronRight
    val ChevronLeft = Icons.Rounded.ChevronLeft
    val More = Icons.Rounded.MoreVert
    val Notifications = Icons.Rounded.Notifications
    val Info = Icons.Rounded.Info

    val CategoryIconsList = listOf(
        Food, Transport, Clothes, Cosmetics, Education,
        Home, Health, Salary, Shopping, Bill, Other, Info
    )
}