package com.example.expensemanager.data.remote.repository

import com.example.expensemanager.data.local.entity.*
import com.example.expensemanager.data.model.TransactionResponse
import com.example.expensemanager.data.model.UserResponse
import kotlinx.coroutines.flow.Flow

interface AppRepository {

    fun getTransactionsByMonth(month: Int, year: Int, userId: String): Flow<List<TransactionEntity>>
    fun getTotalExpenseByMonth(month: Int, year: Int, userId: String): Flow<Double>
    fun getTotalIncomeByMonth(month: Int, year: Int, userId: String): Flow<Double>
    fun getTransactionById(id: String, userId: String): Flow<TransactionEntity?>
    fun getAllLocalTransactions(userId: String): Flow<List<TransactionEntity>>

    suspend fun insertTransaction(transaction: TransactionEntity)
    suspend fun updateLocalTransaction(transaction: TransactionEntity)

    suspend fun deleteTransactionById(id: String, userId: String)
    suspend fun deleteTransactionsByCategory(categoryName: String, userId: String)
    suspend fun deleteTransactionsByCategoryAndMonth(categoryName: String, month: Int, year: Int, userId: String)
    suspend fun updateTransactionCategoryName(oldName: String, newName: String, userId: String)
    suspend fun updateTransactionIconByCategory(categoryName: String, newIcon: String, userId: String)

    suspend fun deleteAllTransactions()

    fun getAllCategories(userId: String): Flow<List<CategoryEntity>>
    fun getCategoriesByTime(month: Int, year: Int, userId: String): Flow<List<CategoryEntity>>

    suspend fun addCategory(
        name: String, iconName: String, isExpense: Boolean, month: Int? = null,
        year: Int? = null, userId: String
    )
    suspend fun deleteCategory(name: String, userId: String)
    fun getBudgets(month: Int, year: Int, userId: String): Flow<List<BudgetEntity>>
    suspend fun upsertBudget(category: String, amount: Double, month: Int, year: Int, userId: String)
    suspend fun deleteBudget(category: String, month: Int, year: Int, userId: String)
    suspend fun deleteOnlyBudget(category: String, month: Int, year: Int, userId: String)
    suspend fun updateBudgetCategoryName(oldName: String, newName: String, userId: String)
    suspend fun deleteAllBudgets()

    suspend fun clearAllLocalData()
    fun getUserProfile(): Flow<Result<UserResponse>>
    fun getCurrentUserId(): String?

    suspend fun syncCloudData(): Result<Unit>

    fun getTransactions(): Flow<Result<List<TransactionResponse>>>
    fun addTransaction(
        description: String,
        amount: Double,
        date: Long,
        category: String,
        categoryIcon: String,
        type: String
    ): Flow<Result<TransactionResponse>>
}