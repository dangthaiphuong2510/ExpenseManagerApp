package com.example.expensemanager.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.local.datastore.CurrencyManager
import com.example.expensemanager.data.remote.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.YearMonth
import javax.inject.Inject

data class ReportUiState(
    val categoryData: List<Pair<String, Double>> = emptyList(),
    val totalAmount: Double = 0.0,
    val selectedMonth: YearMonth = YearMonth.now(),
    val categoryHistory: Map<String, List<Pair<YearMonth, Double>>> = emptyMap(),
    val currencyCode: String = "USD",
    val isLoading: Boolean = false
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: AppRepository,
    private val currencyManager: CurrencyManager
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ReportUiState> = combine(
        _selectedMonth,
        currencyManager.currencySymbol
    ) { month, symbol ->
        month to symbol
    }.flatMapLatest { (selectedMonth, symbol) ->
        val userId = repository.getCurrentUserId() ?: ""
        val last6Months = (5 downTo 0).map { selectedMonth.minusMonths(it.toLong()) }

        val flows = last6Months.map { month ->
            repository.getTransactionsByMonth(month = month.monthValue, year = month.year, userId = userId)
                .map { transactions ->
                    month to transactions.filter {
                        it.type == "EXPENSE" && it.description != "Budget Overwrite"
                    }
                }
        }

        combine(flows) { results ->
            val allHistoryMap = mutableMapOf<String, MutableList<Pair<YearMonth, Double>>>()
            var currentMonthCategoryData = emptyList<Pair<String, Double>>()
            var currentMonthTotal = 0.0

            results.forEach { (month, transactions) ->
                val monthlyTotals = transactions
                    .groupBy { it.category }
                    .mapValues { it.value.sumOf { t -> t.amount } }

                monthlyTotals.forEach { (category, amount) ->
                    val historyList = allHistoryMap.getOrPut(category) { mutableListOf() }
                    historyList.add(month to amount)
                }

                if (month == selectedMonth) {
                    currentMonthCategoryData =
                        monthlyTotals.toList().sortedByDescending { it.second }
                    currentMonthTotal = monthlyTotals.values.sum()
                }
            }

            val finalHistory = allHistoryMap.mapValues { (_, data) ->
                last6Months.map { m ->
                    data.find { it.first == m } ?: (m to 0.0)
                }
            }

            ReportUiState(
                categoryData = currentMonthCategoryData,
                totalAmount = currentMonthTotal,
                selectedMonth = selectedMonth,
                categoryHistory = finalHistory,
                currencyCode = symbol,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportUiState(isLoading = true, selectedMonth = YearMonth.now())
    )

    fun changeMonth(offset: Int) {
        _selectedMonth.value = _selectedMonth.value.plusMonths(offset.toLong())
    }

    fun setMonth(year: Int, month: Int) {
        _selectedMonth.value = YearMonth.of(year, month)
    }
}