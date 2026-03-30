package com.example.basecomposemvvm.data.remote.repository

import com.example.basecomposemvvm.data.remote.api.ApiService
import com.example.basecomposemvvm.data.model.UserResponse
import com.example.basecomposemvvm.data.model.TransactionResponse
import com.example.basecomposemvvm.data.model.TransactionRequest
import com.example.basecomposemvvm.data.local.dao.TransactionDao
import com.example.basecomposemvvm.data.local.entity.TransactionEntity
import com.example.basecomposemvvm.data.local.dao.CategoryDao
import com.example.basecomposemvvm.data.local.entity.CategoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

interface AppRepository {
    fun getUserProfile(): Flow<Result<UserResponse>>

    fun getTransactions(): Flow<Result<List<TransactionResponse>>>

    fun addTransaction(
        description: String,
        amount: Double,
        date: String,
        category: String,
        type: String
    ): Flow<Result<TransactionResponse>>

    // Local DB Transactions
    fun getAllLocalTransactions(): Flow<List<TransactionEntity>>

    suspend fun deleteTransactionById(id: Int)
    suspend fun deleteAllTransactions()
    suspend fun deleteTransactionsByCategory(categoryName: String)

    // Categories
    fun getAllCategories(): Flow<List<CategoryEntity>>

    suspend fun addCategory(name: String, iconName: String, isExpense: Boolean)

    suspend fun deleteCategory(name: String)
}

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) : AppRepository {

    override fun getUserProfile(): Flow<Result<UserResponse>> = flow {
        try {
            val response = apiService.getUserProfile()
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAllLocalTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    override suspend fun deleteTransactionById(id: Int) {
        transactionDao.deleteTransactionById(id.toString())
    }

    override suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }

    override suspend fun deleteTransactionsByCategory(categoryName: String) {
        transactionDao.deleteTransactionByCategory(categoryName)
    }

    override fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories()
    }

    override suspend fun addCategory(name: String, iconName: String, isExpense: Boolean) {
        val category = CategoryEntity(
            name = name,
            iconName = iconName,
            isExpense = isExpense
        )
        categoryDao.insertCategory(category)
    }

    override suspend fun deleteCategory(name: String) {
        categoryDao.deleteCategoryByName(name)
        transactionDao.deleteTransactionByCategory(name)
    }

    override fun getTransactions(): Flow<Result<List<TransactionResponse>>> = flow {
        try {
            val response = apiService.getTransactions()

            val entities = response.map {
                TransactionEntity(
                    id = 0,
                    amount = it.amount,
                    description = it.description,
                    date = it.date,
                    category = it.category,
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
        date: String,
        category: String,
        type: String
    ): Flow<Result<TransactionResponse>> = flow {

        val local = TransactionEntity(
            id = 0, // autoGenerate
            amount = amount,
            description = description,
            date = date,
            category = category,
            type = type
        )

        transactionDao.insertTransaction(local)

        try {
            val response = apiService.addTransaction(
                TransactionRequest(
                    description = description,
                    amount = amount,
                    date = date,
                    category = category,
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
                        date = date,
                        category = category,
                        type = type
                    )
                )
            )
        }
    }.flowOn(Dispatchers.IO)
}