package com.example.basecomposemvvm.data.remote.repository

import com.example.basecomposemvvm.data.remote.api.ApiService
import javax.inject.Inject

/**
 * Repository interface for app-level data operations.
 * Add method declarations here as the project grows.
 */
interface AppRepository

class AppRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AppRepository
