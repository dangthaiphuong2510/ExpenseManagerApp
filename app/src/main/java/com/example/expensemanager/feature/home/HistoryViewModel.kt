package com.example.expensemanager.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.local.entity.TransactionEntity
import com.example.expensemanager.data.remote.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HistoryUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedMonth: Int = 0,
    val selectedYear: Int = 0
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow(calendar.get(Calendar.MONTH) + 1)
    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR))
    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val historyState: StateFlow<HistoryUiState> = combine(
        _selectedMonth,
        _selectedYear,
        _searchQuery
    ) { month, year, query ->
        Triple(month, year, query)
    }.flatMapLatest { (month, year, query) ->
        repository.getTransactionsByMonth(month, year)
            .map { transactions ->
                val filteredTransactions = if (query.isEmpty()) {
                    transactions
                } else {
                    transactions.filter { item ->
                        val categoryMatch = item.category.contains(query, ignoreCase = true)
                        val noteMatch = item.description?.contains(query, ignoreCase = true) ?: false
                        categoryMatch || noteMatch
                    }
                }
                HistoryUiState(
                    transactions = filteredTransactions,
                    isLoading = false,
                    searchQuery = query,
                    selectedMonth = month,
                    selectedYear = year
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    fun previousMonth() {
        if (_selectedMonth.value == 1) {
            _selectedMonth.value = 12
            _selectedYear.value -= 1
        } else {
            _selectedMonth.value -= 1
        }
    }

    fun nextMonth() {
        if (_selectedMonth.value == 12) {
            _selectedMonth.value = 1
            _selectedYear.value += 1
        } else {
            _selectedMonth.value += 1
        }
    }

    fun selectDate(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
    }

    fun filterTransactions(query: String) {
        _searchQuery.value = query
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllTransactions()
        }
    }
}