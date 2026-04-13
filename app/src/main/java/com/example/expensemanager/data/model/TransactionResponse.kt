package com.example.expensemanager.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TransactionResponse(
    @Json(name = "id") val id: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "description") val description: String,
    @Json(name = "date") val date: String,
    @Json(name = "category") val category: String,
    @Json(name = "categoryIcon") val categoryIcon: String,
    @Json(name = "type") val type: String
)