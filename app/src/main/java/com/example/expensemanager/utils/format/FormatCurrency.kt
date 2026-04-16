package com.example.expensemanager.utils.format

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}