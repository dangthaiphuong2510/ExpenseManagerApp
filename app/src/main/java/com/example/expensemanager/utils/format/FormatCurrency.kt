package com.example.expensemanager.utils.format

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double, symbol: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)

    if (symbol == "₫" || symbol == "¥" || symbol == "₩") {
        formatter.maximumFractionDigits = 0
    } else {
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 2
    }

    val formattedNumber = formatter.format(amount)

    return when (symbol) {
        "₫", "€" -> "$formattedNumber $symbol"
        else -> "$symbol$formattedNumber"
    }
}

fun formatCurrencyForInput(rawAmount: String, currencyIdentifier: String): String {
    if (rawAmount.isEmpty()) return ""
    return try {
        val locale = when (currencyIdentifier.uppercase()) {
            "VND", "₫" -> Locale("vi", "VN")
            "USD", "$" -> Locale.US
            "EUR", "€" -> Locale.GERMANY
            "JPY", "¥" -> Locale.JAPAN
            "GBP", "£" -> Locale.UK
            "KRW", "₩" -> Locale.KOREA
            else -> Locale.getDefault()
        }
        val formatter = NumberFormat.getNumberInstance(locale)
        formatter.format(rawAmount.toLong())
    } catch (e: Exception) {
        rawAmount
    }
}

class CurrencyVisualTransformation(private val currencyIdentifier: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text.trim()
        if (originalText.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }
        val formattedText = formatCurrencyForInput(originalText, currencyIdentifier)
        return TransformedText(
            AnnotatedString(formattedText),
            CurrencyOffsetMapping(originalText, formattedText)
        )
    }
}

class CurrencyOffsetMapping(private val originalText: String, private val formattedText: String) :
    OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        if (offset <= 0) return 0
        if (offset >= originalText.length) return formattedText.length
        var originalDigitsCount = 0
        for (i in formattedText.indices) {
            if (formattedText[i].isDigit()) originalDigitsCount++
            if (originalDigitsCount == offset) return i + 1
        }
        return formattedText.length
    }

    override fun transformedToOriginal(offset: Int): Int {
        if (offset <= 0) return 0
        if (offset >= formattedText.length) return originalText.length
        var originalDigitsCount = 0
        for (i in 0 until offset) {
            if (formattedText[i].isDigit()) originalDigitsCount++
        }
        return originalDigitsCount
    }
}