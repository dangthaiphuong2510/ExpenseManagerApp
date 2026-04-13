package com.example.expensemanager.data.remote.api

import com.example.expensemanager.data.model.TransactionResponse
import com.example.expensemanager.data.model.TransactionRequest
import com.example.expensemanager.data.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit API interface.
 * Add endpoint declarations here as the project grows.
 *
 * Example:
 * ```
 * @GET("users")
 * suspend fun getUsers(): BaseResponse<List<User>>
 * ```
 */
interface ApiService {
    @GET("user/profile")
    suspend fun getUserProfile(): UserResponse

    @GET("transactions")
    suspend fun getTransactions(): List<TransactionResponse>

    @POST("transactions")
    suspend fun addTransaction(@Body request: TransactionRequest): TransactionResponse

    @POST("categories")
    suspend fun addCategory(@Body name: String): String
}

