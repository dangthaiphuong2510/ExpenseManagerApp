package com.example.expensemanager.data.remote.repository

import com.example.expensemanager.data.model.UserResponse
import com.example.expensemanager.data.model.TransactionResponse
import com.example.expensemanager.data.local.entity.*
import kotlinx.coroutines.flow.Flow

interface AppRepository {

    //remote(firebase/api)
    fun getUserProfile(): Flow<Result<UserResponse>>

    fun getTransactions(): Flow<Result<List<TransactionResponse>>>

    fun addTransaction(
        description: String,
        amount: Double,
        date: Long,
        category: String,
        categoryIcon: String,
        type: String
    ): Flow<Result<TransactionResponse>>

    //local(room db)
    fun getTransactionsByMonth(month: Int, year: Int): Flow<List<TransactionEntity>>

    fun getTotalExpenseByMonth(month: Int, year: Int): Flow<Double>

    fun getTotalIncomeByMonth(month: Int, year: Int): Flow<Double>

    fun getAllLocalTransactions(): Flow<List<TransactionEntity>>

    fun getTransactionById(id: Int): Flow<TransactionEntity?>

    suspend fun insertLocalTransaction(transaction: TransactionEntity)

    suspend fun updateLocalTransaction(transaction: TransactionEntity)

    suspend fun deleteTransactionById(id: Int)

    suspend fun deleteAllTransactions()

    suspend fun deleteTransactionsByCategory(categoryName: String)

    suspend fun deleteTransactionsByCategoryAndMonth(categoryName: String, month: Int, year: Int)

    //categories
    fun getAllCategories(): Flow<List<CategoryEntity>>

    suspend fun addCategory(name: String, iconName: String, isExpense: Boolean)

    suspend fun deleteCategory(name: String)

    //budgets
    fun getBudgets(month: Int, year: Int): Flow<List<BudgetEntity>>

    suspend fun upsertBudget(
        category: String,
        amount: Double,
        month: Int,
        year: Int
    )

    suspend fun deleteBudget(
        category: String,
        month: Int,
        year: Int
    )

    suspend fun deleteOnlyBudget(category: String, month: Int, year: Int)
}