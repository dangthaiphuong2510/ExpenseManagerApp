package com.example.basecomposemvvm.core.network

import com.squareup.moshi.Moshi
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.UnknownHostException

fun Throwable.mapNetworkError(): Throwable {
    return when (this) {
        is UnknownHostException,
        is InterruptedIOException -> NoConnectivityException

        is HttpException -> {
            val errorBody = response()?.errorBody()?.string()
            val errorResponse = try {
                errorBody?.let {
                    Moshi.Builder().build()
                        .adapter(ErrorResponse::class.java)
                        .fromJson(it)
                }
            } catch (e: Exception) {
                null
            }
            ApiException(
                error = errorResponse,
                httpCode = code(),
                httpMessage = message()
            )
        }

        else -> this
    }
}
