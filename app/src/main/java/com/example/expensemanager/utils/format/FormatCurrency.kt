package com.example.expensemanager.utils.format

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double, symbol: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)

    if (symbol == "₫") {
        formatter.maximumFractionDigits = 0
    } else {
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 2
    }

    val formattedNumber = formatter.format(amount)

    return when (symbol) {
        "₫" -> "$formattedNumber $symbol"
        else -> "$symbol$formattedNumber"
    }
}