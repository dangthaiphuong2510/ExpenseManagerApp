package com.example.expensemanager.feature.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class CategoryUiState(
    val totalBalance: Double? = null,
    val totalIncome: Double? = null,
    val totalExpense: Double? = null,
    val categoryTotals: Map<String, Double> = emptyMap(),
    val categories: List<CategoryItem> = emptyList(),
    val allTransactions: List<TransactionItem> = emptyList(),
    val selectedMonth: Int = 0,
    val selectedYear: Int = 0,
    val isLoading: Boolean = false
)

data class CategoryItem(val name: String, val iconName: String, val isExpense: Boolean)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow(calendar.get(Calendar.MONTH) + 1)
    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR))

    fun setMonthYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    init { checkAndSeedCategories() }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CategoryUiState> = combine(
        _selectedMonth, _selectedYear
    ) { month, year -> month to year }.flatMapLatest { (month, year) ->
        combine(
            repository.getTransactionsByMonth(month, year),
            repository.getAllCategories()
        ) { transactions, categories ->
            val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            // Dùng name để group chính xác
            val totals = transactions.groupBy { it.category }.mapValues { it.value.sumOf { t -> t.amount } }

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
                isLoading = false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryUiState(isLoading = true))

    fun addTransaction(amount: Double, category: String, note: String, isExpense: Boolean, dateMillis: Long) {
        viewModelScope.launch {
            val iconName = uiState.value.categories.find { it.name == category }?.iconName ?: "ic_others"
            repository.addTransaction(
                description = note.ifEmpty { "Transaction" },
                amount = amount, date = dateMillis, category = category,
                categoryIcon = iconName, type = if (isExpense) "EXPENSE" else "INCOME"
            ).collect()
        }
    }

    // FIX LỖI: Cập nhật giao dịch phải giữ lại Category và Type cũ
    fun updateTransaction(id: Int, amount: Double, note: String, dateMillis: Long) {
        viewModelScope.launch {
            // 1. Tìm transaction cũ trong list hiện tại
            val oldTx = uiState.value.allTransactions.find { it.id == id }

            if (oldTx != null) {
                // 2. Xóa cái cũ
                repository.deleteTransactionById(id)

                // 3. Thêm cái mới nhưng phải truyền đúng Category và Type (Expense/Income) cũ
                val isExpense = oldTx.type == "EXPENSE"
                addTransaction(
                    amount = amount,
                    category = oldTx.category, // Quan trọng: Dùng lại category của tx cũ
                    note = note,
                    isExpense = isExpense,
                    dateMillis = dateMillis
                )
            }
        }
    }

    fun deleteTransaction(id: Int) = viewModelScope.launch {
        repository.deleteTransactionById(id)
    }

    fun updateCategory(oldName: String, newName: String, newIcon: String, isExpense: Boolean) {
        viewModelScope.launch {
            // Lưu ý: Nếu xóa category, các transaction cũ sẽ bị mồ côi nếu repository không xử lý update cascade
            repository.deleteCategory(oldName)
            repository.addCategory(newName, newIcon, isExpense)
        }
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

    fun deleteCategory(name: String) = viewModelScope.launch { repository.deleteCategory(name) }

    fun addNewCategory(name: String, iconName: String, isExpense: Boolean) =
        viewModelScope.launch { repository.addCategory(name, iconName, isExpense) }
}