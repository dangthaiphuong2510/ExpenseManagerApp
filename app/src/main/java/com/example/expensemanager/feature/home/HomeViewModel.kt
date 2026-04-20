package com.example.expensemanager.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.local.datastore.CurrencyManager
import com.example.expensemanager.data.local.entity.TransactionEntity
import com.example.expensemanager.data.remote.repository.AppRepository
import com.example.expensemanager.utils.format.formatAmount
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
    val currencySymbol: String = "₫",
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isInitialLoad: Boolean = true,
    val notificationCount: Int = 0,
    val budgetWarnings: List<String> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository,
    private val currencyManager: CurrencyManager
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState(isLoading = true))
    val homeState = _homeState.asStateFlow()

    private val _notificationsSeen = MutableStateFlow(false)

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

            val dataFlow = repository.getTransactionsByMonth(month, year)
                .combine(repository.getBudgets(month, year)) { transactions, budgets ->
                    val income = transactions.filter { it.type.contains("INCOME", ignoreCase = true) }.sumOf { it.amount }
                    val expense = transactions.filter { it.type.contains("EXPENSE", ignoreCase = true) }.sumOf { it.amount }
                    val sortedList = transactions.sortedByDescending { it.date }

                    ResultData(
                        income = income,
                        expense = expense,
                        sortedList = sortedList,
                        rawTransactions = transactions,
                        rawBudgets = budgets
                    )
                }

            combine(
                dataFlow,
                _notificationsSeen,
                currencyManager.currencySymbol
            ) { result, isSeen, symbol ->

                val warningMessages = mutableListOf<String>()

                val expensesByCategory = result.rawTransactions
                    .filter { it.type.contains("EXPENSE", ignoreCase = true) }
                    .groupBy { it.category.trim().lowercase() }

                result.rawBudgets.forEach { budget ->
                    val categoryKey = budget.category.trim().lowercase()
                    val totalSpent = expensesByCategory[categoryKey]?.sumOf { it.amount } ?: 0.0

                    if (budget.amount > 0 && totalSpent > (budget.amount * 0.8)) {
                        val spentStr = totalSpent.formatAmount(symbol)
                        val budgetStr = budget.amount.formatAmount(symbol)
                        warningMessages.add("You have spent $spentStr exceeding your $budgetStr limit for ${budget.category}!")
                    }
                }

                if (result.income == 0.0 && result.expense > 0) {
                    warningMessages.add("Warning: You are spending money without any income!")
                }

                val hasTransactionToday = result.rawTransactions.any { it.date >= todayStart }
                if (!hasTransactionToday) {
                    warningMessages.add("Warning: No transactions have been made today.")
                }

                _homeState.update { currentState ->
                    currentState.copy(
                        totalBalance = result.income - result.expense,
                        totalIncome = result.income,
                        totalExpense = result.expense,
                        currencySymbol = symbol,
                        recentTransactions = result.sortedList.take(4),
                        isLoading = false,
                        isInitialLoad = false,
                        notificationCount = if (isSeen) 0 else warningMessages.size,
                        budgetWarnings = warningMessages
                    )
                }
            }
                .onStart { _homeState.update { it.copy(isLoading = true) } }
                .distinctUntilChanged()
                .collect()
        }
    }

    // --- LOGIC CLEAR ALL DATA ---
    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Xóa sạch trong Database (Transactions & Budgets)
                repository.clearAllLocalData()

                // 2. Reset trạng thái UI về mặc định ngay lập tức
                _homeState.update { currentState ->
                    currentState.copy(
                        totalBalance = 0.0,
                        totalIncome = 0.0,
                        totalExpense = 0.0,
                        recentTransactions = emptyList(),
                        budgetWarnings = emptyList(),
                        notificationCount = 0,
                        isLoading = false
                    )
                }

                // 3. Gọi callback để báo UI hiện Toast hoặc tắt Dialog
                onComplete()
            } catch (e: Exception) {
                _homeState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateCurrency(symbol: String) {
        viewModelScope.launch {
            currencyManager.saveCurrency(symbol)
        }
    }

    fun markNotificationsAsRead() {
        _notificationsSeen.value = true
    }

    private fun resetNotificationSeen() {
        _notificationsSeen.value = false
    }

    private data class ResultData(
        val income: Double,
        val expense: Double,
        val sortedList: List<TransactionEntity>,
        val rawTransactions: List<TransactionEntity>,
        val rawBudgets: List<com.example.expensemanager.data.local.entity.BudgetEntity>,
        val allWarnings: List<String> = emptyList()
    )

    fun syncWithApi() {
        viewModelScope.launch {
            repository.getTransactions()
                .catch { e -> _homeState.update { it.copy(isLoading = false) } }
                .collect { }
        }
    }

    fun refresh() {
        resetNotificationSeen()
        observeMonthlyTransactions()
        syncWithApi()
    }
}