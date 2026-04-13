package com.example.expensemanager.feature.budget

data class BudgetItem(
    val category: String,
    val limit: Double,
    val spent: Double
)