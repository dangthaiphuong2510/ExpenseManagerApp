package com.example.expensemanager.utils.format

import java.text.NumberFormat
import java.util.*

object CurrencyUtils {

    fun formatAmount(amount: Double, currencyCode: String): String {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())

        return try {

            val currency = Currency.getInstance(currencyCode)
            format.currency = currency

            if (currencyCode == "VND") {
                format.maximumFractionDigits = 0
            } else {
                format.maximumFractionDigits = 2
            }

            format.format(amount)
        } catch (e: Exception) {
            format.format(amount)
        }
    }
}
fun Double.formatWithLocalCurrency(currencyCode: String = "USD"): String {
    return CurrencyUtils.formatAmount(this, currencyCode)
}