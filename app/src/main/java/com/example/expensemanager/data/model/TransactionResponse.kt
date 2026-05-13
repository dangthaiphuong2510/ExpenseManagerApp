package com.example.expensemanager.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionResponse(
    @SerialName("id")
    val id: String? = null,

    @SerialName("amount")
    val amount: Double,

    @SerialName("description")
    val description: String,

    @SerialName("date")
    val date: Long,

    @SerialName("category")
    val category: String,

    @SerialName("category_icon")
    val categoryIcon: String? = null,

    @SerialName("type")
    val type: String,

    @SerialName("user_id")
    val userId: String? = null
)