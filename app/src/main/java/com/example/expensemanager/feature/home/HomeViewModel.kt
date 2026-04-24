package com.example.expensemanager.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.local.datastore.CurrencyManager
import com.example.expensemanager.data.local.entity.BudgetEntity
import com.example.expensemanager.data.local.entity.TransactionEntity
import com.example.expensemanager.data.remote.repository.AppRepository
import com.example.expensemanager.feature.category.CategoryItem
import com.example.expensemanager.utils.format.CurrencyUtils
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
    // Đổi tên từ currencySymbol thành currencyCode để chuẩn ISO
    val currencyCode: String = "USD",
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val allCategories: List<CategoryItem> = emptyList(),
    val categoryTotals: Map<String, Double> = emptyMap(),
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
        observeHomeData()
        syncWithApi()
    }

    private fun observeHomeData() {
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

            val monthlyDataFlow = repository.getTransactionsByMonth(month, year)
                .combine(repository.getBudgets(month, year)) { transactions, budgets ->
                    val income = transactions.filter { it.type.equals("INCOME", true) }.sumOf { it.amount }
                    val expense = transactions.filter { it.type.equals("EXPENSE", true) }.sumOf { it.amount }
                    val totals = transactions.groupBy { it.category }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }

                    ResultData(
                        income = income,
                        expense = expense,
                        sortedList = transactions.sortedByDescending { it.date },
                        rawTransactions = transactions,
                        rawBudgets = budgets,
                        categoryTotals = totals
                    )
                }

            val categoriesFlow = repository.getAllCategories().map { list ->
                list.map { CategoryItem(it.name, it.iconName, it.isExpense) }
            }

            // Lắng nghe sự thay đổi của currencySymbol (giờ đóng vai trò là Code)
            combine(
                monthlyDataFlow,
                categoriesFlow,
                _notificationsSeen,
                currencyManager.currencySymbol
            ) { result, categories, isSeen, code ->

                val warningMessages = mutableListOf<String>()
                val expensesByCategory = result.rawTransactions
                    .filter { it.type.equals("EXPENSE", true) }
                    .groupBy { it.category.trim().lowercase() }

                result.rawBudgets.forEach { budget ->
                    val categoryKey = budget.category.trim().lowercase()
                    val totalSpent = expensesByCategory[categoryKey]?.sumOf { it.amount } ?: 0.0

                    if (budget.amount > 0 && totalSpent > (budget.amount * 0.8)) {
                        // SỬ DỤNG FORMATTER MỚI: Tự động thêm ký hiệu dựa trên Code
                        val spentStr = CurrencyUtils.formatAmount(totalSpent, code)
                        val budgetStr = CurrencyUtils.formatAmount(budget.amount, code)
                        warningMessages.add("You have spent $spentStr, exceeding 80% of your $budgetStr limit for ${budget.category}!")
                    }
                }

                if (result.income == 0.0 && result.expense > 0) {
                    warningMessages.add("Warning: You are spending money without any income recorded!")
                }

                val hasTransactionToday = result.rawTransactions.any { it.date >= todayStart }
                if (!hasTransactionToday) {
                    warningMessages.add("You haven't recorded any transactions today.")
                }

                _homeState.update { currentState ->
                    currentState.copy(
                        totalBalance = result.income - result.expense,
                        totalIncome = result.income,
                        totalExpense = result.expense,
                        currencyCode = code, // Cập nhật Code
                        recentTransactions = result.sortedList.take(4),
                        allCategories = categories,
                        categoryTotals = result.categoryTotals,
                        isLoading = false,
                        isInitialLoad = false,
                        notificationCount = if (isSeen) 0 else warningMessages.size,
                        budgetWarnings = warningMessages
                    )
                }
            }
                .onStart { _homeState.update { it.copy(isLoading = true) } }
                .catch { e -> _homeState.update { it.copy(isLoading = false) } }
                .collect()
        }
    }

    // Logic đổi tiền tệ: Giờ chúng ta truyền ISO CODE (VND, USD) thay vì ký hiệu
    fun updateCurrency(code: String) {
        viewModelScope.launch {
            currencyManager.saveCurrency(code)
        }
    }

    // ... (Các hàm addQuickTransaction, clearAllData, refresh giữ nguyên)

    fun addQuickTransaction(amount: Double, note: String, dateMillis: Long, category: String, isExpense: Boolean) {
        viewModelScope.launch {
            try {
                val iconName = _homeState.value.allCategories
                    .find { it.name == category }?.iconName ?: "other"

                val transaction = TransactionEntity(
                    amount = amount,
                    category = category,
                    description = note,
                    date = dateMillis,
                    type = if (isExpense) "EXPENSE" else "INCOME",
                    categoryIcon = iconName
                )
                repository.insertTransaction(transaction)
                refresh()
            } catch (e: Exception) {
                _homeState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.clearAllLocalData()
                _homeState.update { HomeUiState() }
                onComplete()
            } catch (e: Exception) {
                _homeState.update { it.copy(isLoading = false) }
            }
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
        val rawBudgets: List<BudgetEntity>,
        val categoryTotals: Map<String, Double>
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
        observeHomeData()
        syncWithApi()
    }
}