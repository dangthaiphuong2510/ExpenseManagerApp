package com.example.basecomposemvvm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.basecomposemvvm.data.local.dao.CategoryDao
import com.example.basecomposemvvm.data.local.dao.TransactionDao
import com.example.basecomposemvvm.data.local.entity.CategoryEntity
import com.example.basecomposemvvm.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
}