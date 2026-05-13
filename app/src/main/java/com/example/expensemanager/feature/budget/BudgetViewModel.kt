package com.example.expensemanager.feature.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.local.datastore.CurrencyManager
import com.example.expensemanager.data.remote.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BudgetItem(
    val category: String,
    val limit: Double,
    val spent: Double
) {
    val progress: Float
        get() = if (limit > 0) (spent / limit).toFloat() else 0f

    val displayProgress: Float
        get() = progress.coerceAtMost(1f)

    val isOverBudget: Boolean
        get() = spent > limit

    val remaining: Double
        get() = limit - spent
}

data class BudgetUiState(
    val budgetList: List<BudgetItem> = emptyList(),
    val allCategories: List<String> = emptyList(),
    val currencyCode: String = "VND",
    val totalBudgetLimit: Double = 0.0,
    val totalSpent: Double = 0.0,
    val selectedMonth: Int = 0,
    val selectedYear: Int = 0,
    val isLoading: Boolean = false
) {
    val totalProgress: Float
        get() = if (totalBudgetLimit > 0) (totalSpent / totalBudgetLimit).toFloat().coerceAtMost(1f) else 0f

    val isTotalOverBudget: Boolean
        get() = totalSpent > totalBudgetLimit
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: AppRepository,
    private val currencyManager: CurrencyManager
) : ViewModel() {

    private val calendar = Calendar.getInstance()

    private val _selectedMonth = MutableStateFlow(calendar.get(Calendar.MONTH) + 1)
    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR))

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<BudgetUiState> = combine(
        _selectedMonth,
        _selectedYear,
        currencyManager.currencySymbol
    ) { month, year, symbol ->
        Triple(month, year, symbol)
    }.flatMapLatest { (month, year, symbol) ->

        val userId = repository.getCurrentUserId() ?: ""

        combine(
            repository.getTransactionsByMonth(month, year, userId),
            repository.getBudgets(month, year, userId),
            repository.getAllCategories(userId)
        ) { transactions, budgets, categories ->
            val budgetMap = budgets.associate { it.category to it.amount }

            val spentMap = transactions.filter { it.type == "EXPENSE" }
                .groupBy { it.category }
                .mapValues { it.value.sumOf { t -> t.amount } }

            val expenseCategories = categories.filter { it.isExpense }

            val budgetItems = expenseCategories.map { cat ->
                BudgetItem(
                    category = cat.name,
                    limit = budgetMap[cat.name] ?: 0.0,
                    spent = spentMap[cat.name] ?: 0.0
                )
            }

            BudgetUiState(
                budgetList = budgetItems,
                allCategories = expenseCategories.map { it.name },
                currencyCode = symbol,
                totalBudgetLimit = budgetItems.sumOf { it.limit },
                totalSpent = budgetItems.sumOf { it.spent },
                selectedMonth = month,
                selectedYear = year,
                isLoading = false
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        BudgetUiState(
            isLoading = true,
            selectedMonth = calendar.get(Calendar.MONTH) + 1,
            selectedYear = calendar.get(Calendar.YEAR)
        )
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

    fun setMonthYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    fun updateBudget(category: String, amount: Double) {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId() ?: ""
            repository.upsertBudget(category, amount, _selectedMonth.value, _selectedYear.value, userId)
        }
    }
}