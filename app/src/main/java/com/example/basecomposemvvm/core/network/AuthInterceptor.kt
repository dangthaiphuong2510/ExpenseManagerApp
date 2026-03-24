package com.example.basecomposemvvm.core.network

import com.example.basecomposemvvm.data.local.datastore.PreferenceDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val preferenceDataStore: PreferenceDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            preferenceDataStore.token.firstOrNull().orEmpty()
        }

        val request = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(request)
    }
}
