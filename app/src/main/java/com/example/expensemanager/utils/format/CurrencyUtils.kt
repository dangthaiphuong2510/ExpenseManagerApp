package com.example.expensemanager.utils.format

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtils {
    private const val USD_TO_VND_RATE = 25000.0
    private const val EUR_TO_VND_RATE = 27000.0
    private const val JPY_TO_VND_RATE = 160.0
    private const val GBP_TO_VND_RATE = 31500.0
    private const val KRW_TO_VND_RATE = 18.5

    fun formatAmount(amount: Double, currencyCode: String): String {
        return convertAndFormatCurrency(amount, "VND", currencyCode)
    }

    fun convertAndFormatCurrency(
        rawAmount: Double,
        fromCurrency: String,
        toCurrency: String
    ): String {
        val fromCode = fromCurrency.uppercase()
        val toCode = toCurrency.uppercase()

        val amountInVnd = when (fromCode) {
            "VND" -> rawAmount
            "USD" -> rawAmount * USD_TO_VND_RATE
            "EUR" -> rawAmount * EUR_TO_VND_RATE
            "JPY" -> rawAmount * JPY_TO_VND_RATE
            "GBP" -> rawAmount * GBP_TO_VND_RATE
            "KRW" -> rawAmount * KRW_TO_VND_RATE
            else -> rawAmount
        }

        val convertedAmount = when (toCode) {
            "VND" -> amountInVnd
            "USD" -> amountInVnd / USD_TO_VND_RATE
            "EUR" -> amountInVnd / EUR_TO_VND_RATE
            "JPY" -> amountInVnd / JPY_TO_VND_RATE
            "GBP" -> amountInVnd / GBP_TO_VND_RATE
            "KRW" -> amountInVnd / KRW_TO_VND_RATE
            else -> amountInVnd
        }

        val locale = when (toCode) {
            "VND" -> Locale("vi", "VN")
            "USD" -> Locale.US
            "EUR" -> Locale.GERMANY
            "JPY" -> Locale.JAPAN
            "GBP" -> Locale.UK
            "KRW" -> Locale.KOREA
            else -> Locale.getDefault()
        }

        val format = NumberFormat.getCurrencyInstance(locale)

        return try {
            val currency = Currency.getInstance(toCode)
            format.currency = currency

            if (toCode == "VND" || toCode == "JPY" || toCode == "KRW") {
                format.maximumFractionDigits = 0
                format.minimumFractionDigits = 0
            } else {
                format.maximumFractionDigits = 2
                format.minimumFractionDigits = 2
            }

            var formattedString = format.format(convertedAmount)

            if (toCode == "VND" && !formattedString.contains("₫")) {
                formattedString = formattedString.replace("VND", "₫").replace(" ", "") + " ₫"
            }

            formattedString
        } catch (e: Exception) {
            format.format(convertedAmount)
        }
    }
}

fun Double.formatWithLocalCurrency(currencyCode: String = "USD"): String {
    return CurrencyUtils.formatAmount(this, currencyCode)
}

fun Double.formatCurrency(fromCurrency: String = "VND", toCurrency: String = "VND"): String {
    return CurrencyUtils.convertAndFormatCurrency(this, fromCurrency, toCurrency)
}