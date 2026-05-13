package com.example.expensemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    @SerialName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerialName("category")
    val category: String,

    @SerialName("amount")
    val amount: Double,

    @SerialName("month")
    val month: Int,

    @SerialName("year")
    val year: Int,

    @SerialName("user_id")
    val userId: String? = null
)