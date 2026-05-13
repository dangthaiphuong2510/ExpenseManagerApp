package com.example.expensemanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    @SerialName("name")
    val name: String,

    @SerialName("iconName")
    val iconName: String,

    @SerialName("isExpense")
    val isExpense: Boolean,

    @SerialName("user_id")
    val userId: String?,

    @SerialName("target_month")
    val targetMonth: Int? = null,

    @SerialName("target_year")
    val targetYear: Int? = null
)




