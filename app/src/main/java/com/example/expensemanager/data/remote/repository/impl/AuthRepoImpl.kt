package com.example.expensemanager.data.remote.repository.impl

import com.example.expensemanager.data.local.dao.BudgetDao
import com.example.expensemanager.data.local.dao.TransactionDao
import com.example.expensemanager.data.model.UserResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepoImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    fun getCurrentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    fun getUserProfile(): Flow<Result<UserResponse>> = flow {
        try {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                val userResponse = UserResponse(
                    id = user.id,
                    email = user.email ?: "",
                    name = user.userMetadata?.get("full_name")?.toString()?.replace("\"", "")
                        ?: "User"
                )
                emit(Result.success(userResponse))
            } else {
                emit(Result.failure(Exception("No user session found")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun clearAllLocalData() {
        withContext(Dispatchers.IO) {
            supabase.auth.signOut()
            transactionDao.deleteAllTransactions()
            budgetDao.deleteAllBudgets()
        }
    }
}