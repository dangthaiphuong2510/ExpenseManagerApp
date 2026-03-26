package com.example.basecomposemvvm.data.remote.repository

import com.example.basecomposemvvm.data.remote.api.ApiService
import com.example.basecomposemvvm.data.model.UserResponse
import com.example.basecomposemvvm.data.model.TransactionResponse
import com.example.basecomposemvvm.data.model.TransactionRequest
import com.example.basecomposemvvm.data.local.dao.TransactionDao
import com.example.basecomposemvvm.data.local.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

interface AppRepository {
    fun getUserProfile(): Flow<Result<UserResponse>>

    // Lấy dữ liệu từ API và lưu vào Room
    fun getTransactions(): Flow<Result<List<TransactionResponse>>>

    // Thêm giao dịch mới lên API và lưu vào Room
    fun addTransaction(
        description: String,
        amount: Double,
        date: String,
        category: String,
        type: String
    ): Flow<Result<TransactionResponse>>

    fun getAllLocalTransactions(): Flow<List<TransactionEntity>>
}

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val transactionDao: TransactionDao
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

    override fun getTransactions(): Flow<Result<List<TransactionResponse>>> = flow {
        try {
            val response = apiService.getTransactions()

            val entities = response.map {
                TransactionEntity(
                    id = it.id,
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
        try {
            val request = TransactionRequest(
                description = description,
                amount = amount,
                date = date,
                category = category,
                type = type
            )
            val response = apiService.addTransaction(request)

            transactionDao.insertTransaction(
                TransactionEntity(
                    id = response.id,
                    amount = amount,
                    description = description,
                    date = date,
                    category = category,
                    type = type
                )
            )

            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}