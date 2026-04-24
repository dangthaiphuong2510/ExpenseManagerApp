package com.example.expensemanager.feature.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.local.datastore.CurrencyManager
import com.example.expensemanager.data.remote.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class TransactionItem(
    val id: Int,
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
    private val currencyManager: CurrencyManager
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow(calendar.get(Calendar.MONTH) + 1)
    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR))

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    private val _saveSuccess = MutableSharedFlow<Unit>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    init {
        checkAndSeedCategories()
    }

    fun setMonthYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CategoryUiState> = combine(
        _selectedMonth,
        _selectedYear,
        currencyManager.currencySymbol
    ) { month, year, symbol ->
        Triple(month, year, symbol)
    }.flatMapLatest { (month, year, symbol) ->
        combine(
            repository.getTransactionsByMonth(month, year),
            repository.getAllCategories()
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
            ).collect {
                _saveSuccess.emit(Unit)
            }
        }
    }

    fun updateTransaction(id: Int, amount: Double, note: String, dateMillis: Long) {
        viewModelScope.launch {
            if (note.trim().isEmpty()) {
                _errorEvent.emit("Note cannot be empty!")
                return@launch
            }

            val oldTx = uiState.value.allTransactions.find { it.id == id }
            if (oldTx != null) {
                repository.deleteTransactionById(id)
                addTransaction(
                    amount = amount,
                    category = oldTx.category,
                    note = note.trim(),
                    isExpense = oldTx.type == "EXPENSE",
                    dateMillis = dateMillis
                )
            }
        }
    }

    fun deleteTransaction(id: Int) = viewModelScope.launch {
        repository.deleteTransactionById(id)
        _saveSuccess.emit(Unit)
    }

    fun updateCategory(oldName: String, newName: String, newIcon: String, isExpense: Boolean) {
        viewModelScope.launch {
            if (oldName != newName) {
                repository.updateTransactionCategoryName(oldName, newName)
            }
            repository.updateTransactionIconByCategory(newName, newIcon)
            repository.deleteCategory(oldName)
            repository.addCategory(newName, newIcon, isExpense)
            _saveSuccess.emit(Unit)
        }
    }

    fun deleteCategory(name: String) = viewModelScope.launch {
        repository.deleteTransactionsByCategory(name)
        repository.deleteCategory(name)
        _saveSuccess.emit(Unit)
    }

    fun addNewCategory(name: String, iconName: String, isExpense: Boolean) =
        viewModelScope.launch {
            repository.addCategory(name, iconName, isExpense)
            _saveSuccess.emit(Unit)
        }

    private fun checkAndSeedCategories() {
        viewModelScope.launch {
            repository.getAllCategories().take(1).collect { current ->
                if (current.isEmpty()) {
                    val defaults = listOf(
                        CategoryItem("Food", "ic_food", true),
                        CategoryItem("Transport", "ic_transport", true),
                        CategoryItem("Salary", "ic_money", false),
                        CategoryItem("Home", "ic_home", true)
                    )
                    defaults.forEach { repository.addCategory(it.name, it.iconName, it.isExpense) }
                }
            }
        }
    }
}