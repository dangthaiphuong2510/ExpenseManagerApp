package com.example.basecomposemvvm.data.model

sealed class UiState<out T> {
    // Trạng thái đang tải dữ liệu
    object Loading : UiState<Nothing>()

    // Trạng thái lấy dữ liệu thành công
    data class Success<T>(val data: T) : UiState<T>()

    // Trạng thái gặp lỗi (mất mạng, server sập)
    data class Error(val message: String) : UiState<Nothing>()
}