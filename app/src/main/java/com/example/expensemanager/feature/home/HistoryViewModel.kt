package com.example.expensemanager.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.local.datastore.CurrencyManager
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
    val selectedYear: Int = 0,
    val currencyCode: String = "USD"
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AppRepository,
    private val currencyManager: CurrencyManager
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow(calendar.get(Calendar.MONTH) + 1)
    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR))
    private val _searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val historyState: StateFlow<HistoryUiState> = combine(
        _selectedMonth,
        _selectedYear,
        _searchQuery,
        currencyManager.currencySymbol
    ) { month, year, query, code ->
        DataParams(month, year, query, code)
    }.flatMapLatest { params ->
        repository.getTransactionsByMonth(params.month, params.year)
            .map { transactions ->
                val filteredTransactions = if (params.query.isEmpty()) {
                    transactions
                } else {
                    transactions.filter { item ->
                        val categoryMatch = item.category.contains(params.query, ignoreCase = true)
                        val noteMatch = item.description.contains(params.query, ignoreCase = true)
                        categoryMatch || noteMatch
                    }
                }
                HistoryUiState(
                    transactions = filteredTransactions.sortedByDescending { it.date },
                    isLoading = false,
                    searchQuery = params.query,
                    selectedMonth = params.month,
                    selectedYear = params.year,
                    currencyCode = params.code
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    private data class DataParams(
        val month: Int,
        val year: Int,
        val query: String,
        val code: String
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
}