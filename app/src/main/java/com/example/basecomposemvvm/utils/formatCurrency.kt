package com.example.basecomposemvvm.utils

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    return formatter.format(amount)
}