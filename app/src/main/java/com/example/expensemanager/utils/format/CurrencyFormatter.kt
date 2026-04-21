package com.example.expensemanager.utils.format

import androidx.compose.runtime.Composable
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


@Composable
fun Double.formatWithLocalCurrency(): String {
    val symbol = LocalCurrencySymbol.current

    return this.formatAmount(symbol)
}

fun Double.formatAmount(symbol: String): String {
    val symbols = DecimalFormatSymbols(Locale.US)

    return when (symbol) {
        "₫" -> {
            val formatter = DecimalFormat("#,###", symbols)
            "${formatter.format(this)} $symbol"
        }
        "¥", "₩" -> {
            val formatter = DecimalFormat("#,###", symbols)
            "$symbol${formatter.format(this)}"
        }
        else -> {
            val formatter = DecimalFormat("#,##0.00", symbols)
            "$symbol${formatter.format(this)}"
        }
    }
}