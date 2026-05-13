package com.example.expensemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    @SerialName("id")
    val id: String = java.util.UUID.randomUUID().toString(),

    @SerialName("amount")
    val amount: Double,

    @SerialName("description")
    val description: String,

    @SerialName("date")
    val date: Long,

    @SerialName("category")
    val category: String,

    @SerialName("category_icon")
    val categoryIcon: String,

    @SerialName("type")
    val type: String,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("sync_status")
    val syncStatus: String = "PENDING"
)