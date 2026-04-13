package com.example.expensemanager.data.remote.repository

import com.example.expensemanager.data.local.dao.TransactionDao
import com.example.expensemanager.data.local.dao.CategoryDao
import com.example.expensemanager.data.local.dao.BudgetDao
import com.example.expensemanager.data.local.entity.*
import com.example.expensemanager.data.model.TransactionRequest
import com.example.expensemanager.data.model.TransactionResponse
import com.example.expensemanager.data.model.UserResponse
import com.example.expensemanager.data.remote.api.ApiService
import com.example.expensemanager.designsystem.theme.AppIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) : AppRepository {

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private fun formatMonth(month: Int) = month.toString().padStart(2, '0')

    private fun formatDate(timestamp: Long): String {
        return sdf.format(Date(timestamp))
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            // Kiểm tra nếu là chuỗi số (timestamp) thì chuyển thẳng
            dateStr.toLongOrNull() ?: sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    // --- Transaction Logic ---

    override fun getTransactionsByMonth(month: Int, year: Int): Flow<List<TransactionEntity>> {
        return transactionDao
            .getTransactionsByMonth(formatMonth(month), year.toString())
            .flowOn(Dispatchers.IO)
    }

    override fun getTotalExpenseByMonth(month: Int, year: Int): Flow<Double> {
        return transactionDao
            .getTotalExpenseByMonth(formatMonth(month), year.toString())
            .map { it ?: 0.0 }
            .flowOn(Dispatchers.IO)
    }

    override fun getTotalIncomeByMonth(month: Int, year: Int): Flow<Double> {
        return transactionDao
            .getTotalIncomeByMonth(formatMonth(month), year.toString())
            .map { it ?: 0.0 }
            .flowOn(Dispatchers.IO)
    }

    override fun getTransactionById(id: Int): Flow<TransactionEntity?> {
        return transactionDao.getTransactionById(id).flowOn(Dispatchers.IO)
    }

    override suspend fun insertLocalTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    override suspend fun updateLocalTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    override suspend fun deleteOnlyBudget(category: String, month: Int, year: Int) {
        transactionDao.deleteOnlyBudget(
            category = category,
            month = formatMonth(month),
            year = year.toString()
        )
    }

    override suspend fun deleteTransactionsByCategoryAndMonth(
        categoryName: String,
        month: Int,
        year: Int
    ) {
        transactionDao.deleteTransactionsByCategoryAndMonth(
            category = categoryName,
            month = formatMonth(month),
            year = year.toString()
        )
    }

    override fun getAllLocalTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions().flowOn(Dispatchers.IO)
    }

    override suspend fun deleteTransactionById(id: Int) {
        transactionDao.deleteTransactionById(id)
    }

    override suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }

    override suspend fun deleteTransactionsByCategory(categoryName: String) {
        transactionDao.deleteTransactionByCategory(categoryName)
    }

    override fun getTransactions(): Flow<Result<List<TransactionResponse>>> = flow {
        try {
            val response = apiService.getTransactions()
            val entities = response.map { it ->
                TransactionEntity(
                    id = 0,
                    amount = it.amount,
                    description = it.description,
                    date = parseDate(it.date),
                    category = it.category,
                    categoryIcon = it.categoryIcon ?: AppIcons.getIconKeyByName(it.category),
                    type = it.type
                )
            }
            transactionDao.insertTransactions(entities)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun addTransaction(
        description: String,
        amount: Double,
        date: Long,
        category: String,
        categoryIcon: String,
        type: String
    ): Flow<Result<TransactionResponse>> = flow {

        val localEntity = TransactionEntity(
            id = 0,
            amount = amount,
            description = description,
            date = date,
            category = category,
            categoryIcon = categoryIcon,
            type = type
        )
        transactionDao.insertTransaction(localEntity)

        val formattedDate = formatDate(date)

        try {
            val response = apiService.addTransaction(
                TransactionRequest(
                    amount = amount,
                    description = description,
                    date = formattedDate,
                    category = category,
                    categoryIcon = categoryIcon,
                    type = type
                )
            )
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(
                Result.success(
                    TransactionResponse(
                        id = System.currentTimeMillis().toString(),
                        amount = amount,
                        description = description,
                        date = formattedDate,
                        category = category,
                        categoryIcon = categoryIcon,
                        type = type
                    )
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    // --- Category Logic ---

    override fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories().flowOn(Dispatchers.IO)
    }

    override suspend fun addCategory(name: String, iconName: String, isExpense: Boolean) {
        categoryDao.insertCategory(CategoryEntity(name, iconName, isExpense))
    }

    override suspend fun deleteCategory(name: String) {
        categoryDao.deleteCategoryByName(name)
        transactionDao.deleteTransactionByCategory(name)
    }

    //budget logic

    override fun getBudgets(month: Int, year: Int): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgets(month, year).flowOn(Dispatchers.IO)
    }

    override suspend fun upsertBudget(category: String, amount: Double, month: Int, year: Int) {
        budgetDao.deleteBudget(category, month, year)
        budgetDao.insertBudget(
            BudgetEntity(category = category, amount = amount, month = month, year = year)
        )
    }

    override suspend fun deleteBudget(category: String, month: Int, year: Int) {
        budgetDao.deleteBudget(category, month, year)
    }

    // --- User Logic ---

    override fun getUserProfile(): Flow<Result<UserResponse>> = flow {
        try {
            emit(Result.success(apiService.getUserProfile()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}