package com.example.expensemanager.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.local.datastore.CurrencyManager
import com.example.expensemanager.data.local.entity.BudgetEntity
import com.example.expensemanager.data.local.entity.TransactionEntity
import com.example.expensemanager.data.remote.repository.AppRepository
import com.example.expensemanager.feature.category.CategoryItem
import com.example.expensemanager.utils.format.CurrencyUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class NotificationItem(
    val message: String,
    val isRead: Boolean = false
)

data class HomeUiState(
    val totalBalance: Double? = null,
    val totalIncome: Double? = null,
    val totalExpense: Double? = null,
    val currencyCode: String = "USD",
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val allCategories: List<CategoryItem> = emptyList(),
    val allTransactions: List<TransactionEntity> = emptyList(),
    val categoryTotals: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = false,
    val isInitialLoad: Boolean = true,
    val unreadCount: Int = 0,
    val notifications: List<NotificationItem> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository,
    private val currencyManager: CurrencyManager,

    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState(isLoading = true))
    val homeState = _homeState.asStateFlow()

    private val prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)

    private val _readMessages = MutableStateFlow<Set<String>>(
        prefs.getStringSet("read_messages", emptySet()) ?: emptySet()
    )
    private var observationJob: Job? = null

    init {
        observeHomeData()
        syncWithApi()
    }

    private fun observeHomeData() {
        observationJob?.cancel()

        observationJob = viewModelScope.launch {
            val userId = repository.getCurrentUserId() ?: ""

            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)

            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val monthlyDataFlow = repository.getTransactionsByMonth(month, year, userId)
                .combine(repository.getBudgets(month, year, userId)) { transactions, budgets ->
                    val income =
                        transactions.filter { it.type.equals("INCOME", true) }.sumOf { it.amount }
                    val expense =
                        transactions.filter { it.type.equals("EXPENSE", true) }.sumOf { it.amount }
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

            val categoriesFlow = repository.getAllCategories(userId).map { list ->
                list.map { CategoryItem(it.name, it.iconName, it.isExpense) }
            }

            combine(
                monthlyDataFlow,
                categoriesFlow,
                _readMessages,
                currencyManager.currencySymbol
            ) { result, categories, readMsgs, code ->

                val warningMessages = mutableListOf<String>()
                val expensesByCategory = result.rawTransactions
                    .filter { it.type.equals("EXPENSE", true) }
                    .groupBy { it.category.trim().lowercase() }

                result.rawBudgets.forEach { budget ->
                    val categoryKey = budget.category.trim().lowercase()
                    val totalSpent = expensesByCategory[categoryKey]?.sumOf { it.amount } ?: 0.0

                    if (budget.amount > 0 && totalSpent > (budget.amount * 0.8)) {
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

                val finalNotifications = warningMessages.map { msg ->
                    NotificationItem(
                        message = msg,
                        isRead = readMsgs.contains(msg)
                    )
                }

                _homeState.update { currentState ->
                    currentState.copy(
                        totalBalance = result.income - result.expense,
                        totalIncome = result.income,
                        totalExpense = result.expense,
                        currencyCode = code,
                        recentTransactions = result.sortedList.take(4),
                        allTransactions = result.sortedList,
                        allCategories = categories,
                        categoryTotals = result.categoryTotals,
                        isLoading = false,
                        isInitialLoad = false,
                        notifications = finalNotifications,
                        unreadCount = finalNotifications.count { !it.isRead }
                    )
                }
            }
                .onStart { _homeState.update { it.copy(isLoading = true) } }
                .catch { e -> _homeState.update { it.copy(isLoading = false) } }
                .collect()
        }
    }

    fun updateCurrency(code: String) {
        viewModelScope.launch {
            currencyManager.saveCurrency(code)
        }
    }

    fun addQuickTransaction(
        amount: Double,
        note: String,
        dateMillis: Long,
        category: String,
        isExpense: Boolean
    ) {
        viewModelScope.launch {
            try {
                val userId = repository.getCurrentUserId() ?: ""
                val iconName = _homeState.value.allCategories
                    .find { it.name == category }?.iconName ?: "other"

                val transaction = TransactionEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    amount = amount,
                    category = category,
                    description = note,
                    date = dateMillis,
                    type = if (isExpense) "EXPENSE" else "INCOME",
                    categoryIcon = iconName,
                    userId = userId,
                    syncStatus = "PENDING"
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

                prefs.edit().clear().apply()
                _readMessages.value = emptySet()

                onComplete()
            } catch (e: Exception) {
                _homeState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun markNotificationAsRead(message: String) {
        _readMessages.update { currentSet ->
            val newSet = currentSet + message
            prefs.edit().putStringSet("read_messages", newSet).apply()
            newSet
        }
    }

    fun markAllNotificationsAsRead() {
        val allCurrentMessages = _homeState.value.notifications.map { it.message }
        _readMessages.update { currentSet ->
            val newSet = currentSet + allCurrentMessages
            prefs.edit().putStringSet("read_messages", newSet).apply()
            newSet
        }
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
            try {
                repository.syncCloudData()
            } catch (e: Exception) {
                _homeState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refresh() {
        observeHomeData()
        syncWithApi()
    }
}