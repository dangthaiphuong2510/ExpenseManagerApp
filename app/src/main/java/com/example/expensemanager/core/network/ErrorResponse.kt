package com.example.expensemanager.core.network

import com.squareup.moshi.Json

data class ErrorResponse(
    @Json(name = "message") val message: String
)
