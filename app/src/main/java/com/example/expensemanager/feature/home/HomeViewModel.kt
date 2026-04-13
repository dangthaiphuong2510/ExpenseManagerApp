package com.example.expensemanager.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.local.entity.TransactionEntity
import com.example.expensemanager.data.remote.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val totalBalance: Double? = null,
    val totalIncome: Double? = null,
    val totalExpense: Double? = null,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isInitialLoad: Boolean = true,
    val notificationCount: Int = 0,
    val budgetWarning: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState(isLoading = true))
    val homeState = _homeState.asStateFlow()

    private var observationJob: Job? = null

    init {
        observeMonthlyTransactions()
        syncWithApi()
    }

    private fun observeMonthlyTransactions() {
        observationJob?.cancel()

        observationJob = viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)

            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            repository.getTransactionsByMonth(month, year)
                .onStart {
                    _homeState.update { it.copy(isLoading = true) }
                }
                .distinctUntilChanged()
                .collect { transactions ->
                    val income = transactions.filter { it.type.trim().equals("INCOME", true) }.sumOf { it.amount }
                    val expense = transactions.filter { it.type.trim().equals("EXPENSE", true) }.sumOf { it.amount }
                    val sortedList = transactions.sortedByDescending { it.date }

                    var alertCount = 0
                    var warningMsg: String? = null

                    if (income > 0) {
                        if (expense > (income * 0.8)) {
                            alertCount++
                            warningMsg = "Warning: You've spent over 80% of your income!"
                        }
                    } else if (expense > 0) {
                        alertCount++
                        warningMsg = "Alert: You are spending without any recorded income!"
                    }

                    val calendarNow = Calendar.getInstance()
                    val currentHour = calendarNow.get(Calendar.HOUR_OF_DAY)
                    val hasTransactionToday = transactions.any { it.date >= todayStart }

                    if (!hasTransactionToday && currentHour >= 18) {
                        alertCount++
                    }

                    _homeState.update { currentState ->
                        currentState.copy(
                            totalBalance = income - expense,
                            totalIncome = income,
                            totalExpense = expense,
                            recentTransactions = sortedList.take(4),
                            isLoading = false,
                            isInitialLoad = false,
                            notificationCount = alertCount,
                            budgetWarning = warningMsg
                        )
                    }
                }
        }
    }

    fun syncWithApi() {
        viewModelScope.launch {
            repository.getTransactions()
                .catch { e ->
                    _homeState.update { it.copy(isLoading = false) }
                }
                .collect { }
        }
    }

    fun refresh() {
        observeMonthlyTransactions()
        syncWithApi()
    }
}