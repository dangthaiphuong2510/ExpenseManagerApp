package com.example.expensemanager.core.network

data class ApiException(
    val error: ErrorResponse?,
    val httpCode: Int,
    val httpMessage: String?
) : RuntimeException()
