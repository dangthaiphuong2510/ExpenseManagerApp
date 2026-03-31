package com.example.basecomposemvvm.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basecomposemvvm.data.local.entity.TransactionEntity
import com.example.basecomposemvvm.data.remote.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState(isLoading = true))
    val homeState = _homeState.asStateFlow()

    init {
        syncWithApi()
        observeLocalTransactions()
    }

    private fun observeLocalTransactions() {
        viewModelScope.launch {
            repository.getAllLocalTransactions().collect { transactions ->
                val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                val expense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

                _homeState.update { currentState ->
                    currentState.copy(
                        totalBalance = income - expense,
                        totalIncome = income,
                        totalExpense = expense,
                        recentTransactions = transactions.take(4),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun syncWithApi() {
        viewModelScope.launch {
            repository.getTransactions().collect { result ->

            }
        }
    }
}