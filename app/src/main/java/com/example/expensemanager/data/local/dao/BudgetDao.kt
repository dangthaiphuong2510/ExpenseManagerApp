package com.example.expensemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.expensemanager.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun getBudgets(month: Int, year: Int): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE category = :category AND month = :month AND year = :year")
    suspend fun deleteBudget(category: String, month: Int, year: Int)

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()
}