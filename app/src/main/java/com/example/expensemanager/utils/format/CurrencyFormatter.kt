package com.example.expensemanager.utils.format

import java.text.DecimalFormat

fun Double.formatAmount(symbol: String): String {
    return if (symbol == "₫") {
        val formatter = DecimalFormat("#,###")
        "${formatter.format(this)} $symbol"
    } else {
        val formatter = DecimalFormat("#,##0.00")
        "$symbol${formatter.format(this)}"
    }
}