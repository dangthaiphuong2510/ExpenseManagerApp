package com.example.expensemanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.expensemanager.data.local.dao.BudgetDao
import com.example.expensemanager.data.local.dao.CategoryDao
import com.example.expensemanager.data.local.dao.TransactionDao
import com.example.expensemanager.data.local.entity.CategoryEntity
import com.example.expensemanager.data.local.entity.TransactionEntity
import com.example.expensemanager.data.local.entity.BudgetEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

}