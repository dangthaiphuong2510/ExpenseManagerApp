package com.example.expensemanager.data.remote.repository.impl

import com.example.expensemanager.data.local.dao.BudgetDao
import com.example.expensemanager.data.local.entity.BudgetEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BudgetRepoImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val budgetDao: BudgetDao
) {
    fun getBudgets(month: Int, year: Int, userId: String): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgets(month, year, userId).flowOn(Dispatchers.IO)
    }

    suspend fun upsertBudget(category: String, amount: Double, month: Int, year: Int, userId: String) {
        withContext(Dispatchers.IO) {
            val budget = BudgetEntity(
                category = category,
                amount = amount,
                month = month,
                year = year,
                userId = userId
            )

            budgetDao.deleteBudget(category, month, year, userId)
            budgetDao.insertBudget(budget)

            try {
                supabase.postgrest["budgets"].upsert(budget) {
                    select()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteBudget(category: String, month: Int, year: Int, userId: String) {
        withContext(Dispatchers.IO) {
            budgetDao.deleteBudget(category, month, year, userId)

            try {
                supabase.postgrest["budgets"].delete {
                    filter {
                        eq("category", category)
                        eq("month", month)
                        eq("year", year)
                        eq("user_id", userId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun updateBudgetCategoryName(oldName: String, newName: String, userId: String) {
        withContext(Dispatchers.IO) {
            budgetDao.updateBudgetCategoryName(oldName, newName, userId)

        }
    }

    suspend fun deleteAllBudgets() {
        withContext(Dispatchers.IO) {
            budgetDao.deleteAllBudgets()
        }
    }
}