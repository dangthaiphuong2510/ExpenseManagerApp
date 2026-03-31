package com.example.basecomposemvvm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val name: String,
    val iconName: String,
    val isExpense: Boolean
)