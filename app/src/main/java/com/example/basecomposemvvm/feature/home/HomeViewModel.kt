package com.example.basecomposemvvm.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basecomposemvvm.data.model.TransactionResponse
import com.example.basecomposemvvm.data.model.UiState
import com.example.basecomposemvvm.data.remote.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<TransactionResponse>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getTransactions().collect { result ->
                result.onSuccess { data ->
                    _uiState.value = UiState.Success(data)
                }
                result.onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Error loading transactions")
                }
            }
        }
    }
}