package com.example.expensemanager.feature.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.remote.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BudgetUiState(
    val budgetList: List<BudgetItem> = emptyList(),
    val totalBudgetLimit: Double = 0.0,
    val totalSpent: Double = 0.0,
    val selectedMonth: Int = 0,
    val selectedYear: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow(calendar.get(Calendar.MONTH) + 1)
    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR))

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<BudgetUiState> = combine(
        _selectedMonth,
        _selectedYear
    ) { month, year -> month to year }
        .flatMapLatest { (month, year) ->
            combine(
                repository.getTransactionsByMonth(month, year),
                repository.getBudgets(month, year),
                repository.getAllCategories()
            ) { transactions, budgets, categories ->

                val budgetMap = budgets.associate { it.category to it.amount }

                val spentMap = transactions
                    .filter { it.type == "EXPENSE" }
                    .groupBy { it.category }
                    .mapValues { it.value.sumOf { it.amount } }

                val budgetItems = categories
                    .filter { it.isExpense }
                    .map { cat ->
                        BudgetItem(
                            category = cat.name,
                            limit = budgetMap[cat.name] ?: 0.0,
                            spent = spentMap[cat.name] ?: 0.0
                        )
                    }

                BudgetUiState(
                    budgetList = budgetItems,
                    totalBudgetLimit = budgetItems.sumOf { it.limit },
                    totalSpent = budgetItems.sumOf { it.spent },
                    selectedMonth = month,
                    selectedYear = year,
                    isLoading = false
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            BudgetUiState(isLoading = true)
        )

    fun changeMonth(offset: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _selectedYear.value)
            set(Calendar.MONTH, _selectedMonth.value - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.add(Calendar.MONTH, offset)

        _selectedMonth.value = cal.get(Calendar.MONTH) + 1
        _selectedYear.value = cal.get(Calendar.YEAR)
    }

    fun updateBudget(category: String, amount: Double) {
        viewModelScope.launch {
            repository.upsertBudget(
                category = category,
                amount = amount,
                month = _selectedMonth.value,
                year = _selectedYear.value
            )
        }
    }
}