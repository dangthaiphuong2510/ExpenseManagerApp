package com.example.expensemanager.data.remote.repository.impl

import com.example.expensemanager.data.local.dao.TransactionDao
import com.example.expensemanager.data.local.entity.TransactionEntity
import com.example.expensemanager.data.model.TransactionResponse
import com.example.expensemanager.designsystem.theme.AppIcons
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class TransactionRepoImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val transactionDao: TransactionDao
) {
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private fun formatMonth(month: Int) = month.toString().padStart(2, '0')

    fun getTransactionsByMonth(month: Int, year: Int, userId: String): Flow<List<TransactionEntity>> {
        return transactionDao
            .getTransactionsByMonth(formatMonth(month), year.toString(), userId)
            .flowOn(Dispatchers.IO)
    }

    fun getTotalExpenseByMonth(month: Int, year: Int, userId: String): Flow<Double> {
        return transactionDao
            .getTotalExpenseByMonth(formatMonth(month), year.toString(), userId)
            .map { it ?: 0.0 }
            .flowOn(Dispatchers.IO)
    }

    fun getTotalIncomeByMonth(month: Int, year: Int, userId: String): Flow<Double> {
        return transactionDao
            .getTotalIncomeByMonth(formatMonth(month), year.toString(), userId)
            .map { it ?: 0.0 }
            .flowOn(Dispatchers.IO)
    }

    fun getTransactionById(id: String, userId: String): Flow<TransactionEntity?> {
        return transactionDao.getTransactionById(id, userId).flowOn(Dispatchers.IO)
    }

    fun getAllLocalTransactions(userId: String): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions(userId).flowOn(Dispatchers.IO)

    suspend fun insertTransaction(transaction: TransactionEntity) {
        withContext(Dispatchers.IO) {
            transactionDao.insertTransaction(transaction)
        }
    }

    suspend fun updateLocalTransaction(transaction: TransactionEntity) {
        withContext(Dispatchers.IO) {
            // Khi update, chuyển trạng thái về PENDING để chờ Sync đẩy lên lại
            val pendingTransaction = transaction.copy(syncStatus = "PENDING")
            transactionDao.updateTransaction(pendingTransaction)
        }
    }

    fun addTransaction(
        description: String,
        amount: Double,
        date: Long,
        category: String,
        categoryIcon: String,
        type: String
    ): Flow<Result<TransactionResponse>> = flow {
        val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("User not logged in")
        val transactionId = UUID.randomUUID().toString()

        val localEntity = TransactionEntity(
            id = transactionId,
            amount = amount,
            description = description,
            date = date,
            category = category,
            categoryIcon = categoryIcon,
            type = type,
            userId = userId,
            syncStatus = "PENDING"
        )
        transactionDao.insertTransaction(localEntity)


        val transactionRequest = TransactionResponse(
            id = transactionId,
            amount = amount,
            description = description,
            date = date,
            category = category,
            categoryIcon = categoryIcon,
            type = type,
            userId = userId
        )

        try {
            val response = supabase.postgrest["transactions"]
                .insert(transactionRequest) { select() }
                .decodeSingle<TransactionResponse>()

            transactionDao.insertTransaction(localEntity.copy(syncStatus = "SYNCED"))
            emit(Result.success(response))

        } catch (e: Exception) {
            android.util.Log.e("SYNC_ERROR", "Offline Mode - Đã lưu Room, chờ Sync lên mây sau.")
            emit(Result.success(transactionRequest))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun deleteTransactionById(id: String, userId: String) {
        withContext(Dispatchers.IO) {
            // Xóa ở Room trước
            transactionDao.deleteTransactionById(id, userId)
            try {
                supabase.postgrest["transactions"].delete {
                    filter {
                        eq("id", id)
                        eq("user_id", userId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteOnlyBudget(category: String, month: Int, year: Int, userId: String) {
        withContext(Dispatchers.IO) {
            transactionDao.deleteOnlyBudget(category, formatMonth(month), year.toString(), userId)
        }
    }

    suspend fun deleteTransactionsByCategoryAndMonth(categoryName: String, month: Int, year: Int, userId: String) {
        withContext(Dispatchers.IO) {
            transactionDao.deleteTransactionsByCategoryAndMonth(categoryName, formatMonth(month), year.toString(), userId)
        }
    }

    suspend fun deleteTransactionsByCategory(categoryName: String, userId: String) {
        withContext(Dispatchers.IO) {
            transactionDao.deleteTransactionByCategory(categoryName, userId)
        }
    }

    suspend fun updateTransactionCategoryName(oldName: String, newName: String, userId: String) {
        withContext(Dispatchers.IO) {
            transactionDao.updateCategoryName(oldName, newName, userId)
        }
    }

    suspend fun updateTransactionIconByCategory(categoryName: String, newIcon: String, userId: String) {
        withContext(Dispatchers.IO) {
            transactionDao.updateIconByCategory(categoryName, newIcon, userId)
        }
    }

    fun getTransactions(): Flow<Result<List<TransactionResponse>>> = flow {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("User not logged in")

            val response = supabase.postgrest["transactions"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<TransactionResponse>()

            val entities = response.map {
                TransactionEntity(
                    id = it.id ?: UUID.randomUUID().toString(),
                    amount = it.amount,
                    description = it.description,
                    date = it.date,
                    category = it.category,
                    categoryIcon = it.categoryIcon ?: AppIcons.getIconKeyByName(it.category),
                    type = it.type,
                    userId = it.userId,
                    syncStatus = "SYNCED"
                )
            }
            transactionDao.insertTransactions(entities)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun deleteAllTransactions() {
        withContext(Dispatchers.IO) { transactionDao.deleteAllTransactions() }
    }
}