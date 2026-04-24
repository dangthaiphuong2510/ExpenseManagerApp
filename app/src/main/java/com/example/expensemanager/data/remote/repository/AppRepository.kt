package com.example.expensemanager.data.remote.repository

import com.example.expensemanager.data.local.entity.*
import com.example.expensemanager.data.model.TransactionResponse
import com.example.expensemanager.data.model.UserResponse
import kotlinx.coroutines.flow.Flow

interface AppRepository {

    // --- Transaction (Giao dịch) ---
    fun getTransactionsByMonth(month: Int, year: Int): Flow<List<TransactionEntity>>
    fun getTotalExpenseByMonth(month: Int, year: Int): Flow<Double>
    fun getTotalIncomeByMonth(month: Int, year: Int): Flow<Double>
    fun getTransactionById(id: Int): Flow<TransactionEntity?>
    fun getAllLocalTransactions(): Flow<List<TransactionEntity>>
    fun getTransactions(): Flow<Result<List<TransactionResponse>>>

    suspend fun insertTransaction(transaction: TransactionEntity)
    suspend fun updateLocalTransaction(transaction: TransactionEntity)
    suspend fun deleteTransactionById(id: Int)
    suspend fun deleteAllTransactions()

    suspend fun deleteTransactionsByCategory(categoryName: String)

    suspend fun deleteTransactionsByCategoryAndMonth(categoryName: String, month: Int, year: Int)

    suspend fun updateTransactionCategoryName(oldName: String, newName: String)

    suspend fun updateTransactionIconByCategory(categoryName: String, newIcon: String)

    fun addTransaction(
        description: String,
        amount: Double,
        date: Long,
        category: String,
        categoryIcon: String,
        type: String
    ): Flow<Result<TransactionResponse>>

    //Category
    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun addCategory(name: String, iconName: String, isExpense: Boolean)
    suspend fun deleteCategory(name: String)

    //Budget
    fun getBudgets(month: Int, year: Int): Flow<List<BudgetEntity>>
    suspend fun upsertBudget(category: String, amount: Double, month: Int, year: Int)
    suspend fun deleteBudget(category: String, month: Int, year: Int)

    suspend fun deleteOnlyBudget(category: String, month: Int, year: Int)
    suspend fun deleteAllBudgets()

    suspend fun updateBudgetCategoryName(oldName: String, newName: String)

    //Global & User
    suspend fun clearAllLocalData()
    fun getUserProfile(): Flow<Result<UserResponse>>
}