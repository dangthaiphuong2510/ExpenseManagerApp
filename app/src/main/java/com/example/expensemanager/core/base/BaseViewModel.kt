package com.example.expensemanager.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import com.example.expensemanager.core.navigation.AppDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    // Loading count mechanism — supports concurrent requests
    private var loadingCount: Int = 0

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error handling
    protected val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    // Navigation events
    protected val _navigator = MutableSharedFlow<AppDestination>()
    val navigator: SharedFlow<AppDestination> = _navigator.asSharedFlow()

    @Synchronized
    protected fun showLoading() {
        loadingCount++
        _isLoading.value = true
    }

    @Synchronized
    protected fun hideLoading() {
        loadingCount--
        if (loadingCount <= 0) {
            loadingCount = 0
            _isLoading.value = false
        }
    }

    /**
     * Launch a coroutine in viewModelScope with optional custom context.
     */
    protected fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        job: suspend () -> Unit
    ) {
        viewModelScope.launch(context) {
            try {
                job()
            } catch (e: Throwable) {
                _error.value = e
            }
        }
    }

    /**
     * Extension to automatically show/hide loading when Flow starts/completes.
     */
    fun <T> Flow<T>.loading(): Flow<T> = this
        .onStart { showLoading() }
        .onCompletion { hideLoading() }

    /**
     * Collect Flow with automatic loading and error handling.
     */
    suspend fun <T> Flow<T>.async(action: suspend (T) -> Unit) {
        this.loading().collect { value ->
            try {
                action(value)
            } catch (e: Throwable) {
                _error.value = e
            }
        }
    }
}
