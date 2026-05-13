package com.example.expensemanager.data.remote.repository.impl

import com.example.expensemanager.data.local.dao.BudgetDao
import com.example.expensemanager.data.local.dao.CategoryDao
import com.example.expensemanager.data.local.dao.TransactionDao
import com.example.expensemanager.data.local.entity.BudgetEntity
import com.example.expensemanager.data.local.entity.CategoryEntity
import com.example.expensemanager.data.local.entity.TransactionEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncRepoImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) {
    suspend fun syncCloudData(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId =
                    supabase.auth.currentUserOrNull()?.id ?: throw Exception("User not logged in")

                transactionDao.deleteAllTransactions()
                categoryDao.deleteAllCategories()
                budgetDao.deleteAllBudgets()
                android.util.Log.d("DEBUG_FLOW", "Đã dọn sạch kho Room cho user: $userId")


                try {
                    val categories = supabase.postgrest["categories"]
                        .select { filter { eq("user_id", userId) } }
                        .decodeList<CategoryEntity>()

                    android.util.Log.d(
                        "DEBUG_FLOW",
                        "TRẠM 1 (Sync): Đã kéo từ mây về được ${categories.size} danh mục"
                    )

                    if (categories.isEmpty()) {
                        val defaultCategories = listOf(
                            CategoryEntity(
                                name = "Food",
                                iconName = "ic_food",
                                isExpense = true,
                                userId = userId,
                                targetMonth = null,
                                targetYear = null
                            ),
                            CategoryEntity(
                                name = "Transport",
                                iconName = "ic_transport",
                                isExpense = true,
                                userId = userId,
                                targetMonth = null,
                                targetYear = null
                            ),
                            CategoryEntity(
                                name = "Home",
                                iconName = "ic_home",
                                isExpense = true,
                                userId = userId,
                                targetMonth = null,
                                targetYear = null
                            ),
                            CategoryEntity(
                                name = "Salary",
                                iconName = "ic_salary",
                                isExpense = false,
                                userId = userId,
                                targetMonth = null,
                                targetYear = null
                            )
                        )

                        defaultCategories.forEach { categoryDao.insertCategory(it) }
                        supabase.postgrest["categories"].insert(defaultCategories)
                    } else {
                        categories.forEach { categoryDao.insertCategory(it) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    val budgets = supabase.postgrest["budgets"]
                        .select { filter { eq("user_id", userId) } }
                        .decodeList<BudgetEntity>()

                    budgets.forEach { budgetDao.insertBudget(it) }
                    android.util.Log.d(
                        "DEBUG_FLOW",
                        "TRẠM 1 (Sync): Đã kéo về ${budgets.size} ngân sách"
                    )
                } catch (e: Exception) {
                    android.util.Log.e("DEBUG_FLOW", "TRẠM 1 LỖI (Budgets): ${e.message}")
                    e.printStackTrace()
                }

                try {
                    val transactions = supabase.postgrest["transactions"]
                        .select { filter { eq("user_id", userId) } }
                        .decodeList<TransactionEntity>()

                    transactions.forEach { transactionDao.insertTransaction(it) }
                    android.util.Log.d(
                        "DEBUG_FLOW",
                        "TRẠM 1 (Sync): Đã kéo về ${transactions.size} giao dịch"
                    )
                } catch (e: Exception) {
                    android.util.Log.e("DEBUG_FLOW", "TRẠM 1 LỖI (Transactions): ${e.message}")
                    e.printStackTrace()
                }

                Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
}