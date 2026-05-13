package com.example.expensemanager.di

import android.content.Context
import androidx.room.Room
import com.example.expensemanager.data.local.AppDatabase
import com.example.expensemanager.data.local.dao.BudgetDao
import com.example.expensemanager.data.local.dao.CategoryDao
import com.example.expensemanager.data.local.dao.TransactionDao
import com.example.expensemanager.data.remote.api.ApiService
import com.example.expensemanager.data.remote.repository.AppRepository
import com.example.expensemanager.data.remote.repository.impl.AppRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideBudgetDao(database: AppDatabase): BudgetDao {
        return database.budgetDao()
    }

}

