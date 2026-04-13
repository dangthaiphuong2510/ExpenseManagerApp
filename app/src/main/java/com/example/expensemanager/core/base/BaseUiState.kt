package com.example.expensemanager.core.base

/**
 * Base interface for screen UI states.
 * Each screen creates its own data class implementing this interface.
 */
interface BaseUiState {
    val errorMessage: String
    val showLoading: Boolean
}

/**
 * Generic sealed class for one-shot UI events (Loading, Success, Error).
 */
sealed class UiEvent<T>(val data: T? = null, val message: String? = null) {
    class Loading<T> : UiEvent<T>()
    class Success<T>(data: T) : UiEvent<T>(data = data)
    class Error<T>(message: String) : UiEvent<T>(message = message)
}
