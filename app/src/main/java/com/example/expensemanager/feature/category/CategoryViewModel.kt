package com.example.expensemanager.feature.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.local.datastore.CurrencyManager
import com.example.expensemanager.data.remote.repository.AppRepository
import com.example.expensemanager.data.remote.repository.impl.SyncRepoImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class TransactionItem(
    val id: String,
    val amount: Double,
    val note: String,
    val date: Long,
    val category: String,
    val type: String
)

data class CategoryItem(
    val name: String,
    val iconName: String,
    val isExpense: Boolean
)

data class CategoryUiState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categoryTotals: Map<String, Double> = emptyMap(),
    val categories: List<CategoryItem> = emptyList(),
    val allTransactions: List<TransactionItem> = emptyList(),
    val selectedMonth: Int = 0,
    val selectedYear: Int = 0,
    val currencyCode: String = "USD",
    val isLoading: Boolean = false
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: AppRepository,
    private val currencyManager: CurrencyManager,
    private val syncRepo: SyncRepoImpl
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow(calendar.get(Calendar.MONTH) + 1)
    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR))

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    private val _saveSuccess = MutableSharedFlow<Unit>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    init {
        viewModelScope.launch {
            android.util.Log.d("DEBUG_FLOW", "Đang tự động Sync khi mở màn hình Category...")
            syncRepo.syncCloudData()
        }
    }

    fun setMonthYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    fun previousMonth() {
        var m = _selectedMonth.value
        var y = _selectedYear.value
        if (m == 1) {
            m = 12
            y -= 1
        } else {
            m -= 1
        }
        setMonthYear(m, y)
    }

    fun nextMonth() {
        var m = _selectedMonth.value
        var y = _selectedYear.value
        if (m == 12) {
            m = 1
            y += 1
        } else {
            m += 1
        }
        setMonthYear(m, y)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CategoryUiState> = combine(
        _selectedMonth,
        _selectedYear,
        currencyManager.currencySymbol
    ) { month, year, symbol ->
        Triple(month, year, symbol)
    }.flatMapLatest { (month, year, symbol) ->

        val userId = repository.getCurrentUserId() ?: ""

        combine(
            repository.getTransactionsByMonth(month, year, userId),
            repository.getCategoriesByTime(month, year, userId)
        ) { transactions, categories ->

            val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            val totals = transactions.groupBy { it.category }
                .mapValues { it.value.sumOf { t -> t.amount } }

            CategoryUiState(
                totalBalance = income - expense,
                totalIncome = income,
                totalExpense = expense,
                categoryTotals = totals,
                categories = categories.map { CategoryItem(it.name, it.iconName, it.isExpense) },
                allTransactions = transactions.map {
                    TransactionItem(it.id, it.amount, it.description, it.date, it.category, it.type)
                },
                selectedMonth = month,
                selectedYear = year,
                currencyCode = symbol,
                isLoading = false
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CategoryUiState(isLoading = true)
    )

    fun addTransaction(
        amount: Double,
        category: String,
        note: String,
        isExpense: Boolean,
        dateMillis: Long
    ) {
        viewModelScope.launch {
            if (note.trim().isEmpty()) {
                _errorEvent.emit("Please enter a note/description!")
                return@launch
            }
            if (amount <= 0) {
                _errorEvent.emit("Amount must be greater than 0!")
                return@launch
            }

            val iconName =
                uiState.value.categories.find { it.name == category }?.iconName ?: "ic_others"

            repository.addTransaction(
                description = note.trim(),
                amount = amount,
                date = dateMillis,
                category = category,
                categoryIcon = iconName,
                type = if (isExpense) "EXPENSE" else "INCOME"
            ).collect { result ->
                result.onSuccess {
                    _saveSuccess.emit(Unit)
                }.onFailure { e ->
                    _errorEvent.emit("Offline saved! Cloud sync waiting: ${e.message}")
                }
            }
        }
    }

    fun updateTransaction(id: String, amount: Double, note: String, dateMillis: Long) {
        viewModelScope.launch {
            if (note.trim().isEmpty()) {
                _errorEvent.emit("Note cannot be empty!")
                return@launch
            }
            val oldTx = uiState.value.allTransactions.find { it.id == id }
            if (oldTx != null) {
                val userId = repository.getCurrentUserId() ?: ""

                repository.deleteTransactionById(id, userId)
                addTransaction(
                    amount,
                    oldTx.category,
                    note.trim(),
                    oldTx.type == "EXPENSE",
                    dateMillis
                )
            }
        }
    }

    fun deleteTransaction(id: String) = viewModelScope.launch {
        val userId = repository.getCurrentUserId() ?: ""
        repository.deleteTransactionById(id, userId)
        _saveSuccess.emit(Unit)
    }

    fun updateCategory(oldName: String, newName: String, newIcon: String, isExpense: Boolean) {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId() ?: ""

            if (oldName != newName) {
                repository.updateTransactionCategoryName(oldName, newName, userId)
            }
            repository.updateTransactionIconByCategory(newName, newIcon, userId)
            repository.deleteCategory(oldName, userId)

            // Cần truyền userId vào hàm addCategory
            repository.addCategory(newName, newIcon, isExpense, null, null, userId)

            _saveSuccess.emit(Unit)
        }
    }

    fun deleteCategory(name: String) = viewModelScope.launch {
        val userId = repository.getCurrentUserId() ?: ""

        repository.deleteTransactionsByCategory(name, userId)
        repository.deleteCategory(name, userId)

        _saveSuccess.emit(Unit)
    }

    fun addNewCategory(name: String, iconName: String, isExpense: Boolean, isPermanent: Boolean) {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId() ?: ""

            if (isPermanent) {
                repository.addCategory(name, iconName, isExpense, null, null, userId)
            } else {
                repository.addCategory(
                    name,
                    iconName,
                    isExpense,
                    _selectedMonth.value,
                    _selectedYear.value,
                    userId
                )
            }
            _saveSuccess.emit(Unit)
        }
    }
}