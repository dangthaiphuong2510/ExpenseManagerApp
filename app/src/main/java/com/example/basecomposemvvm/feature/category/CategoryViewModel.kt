package com.example.basecomposemvvm.feature.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basecomposemvvm.data.remote.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CategoryUiState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categoryTotals: Map<String, Double> = emptyMap(),
    val categories: List<CategoryItem> = emptyList()
)

data class CategoryItem(val name: String, val iconName: String, val isExpense: Boolean)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    init {
        checkAndSeedCategories()
    }

    val uiState: StateFlow<CategoryUiState> = combine(
        repository.getAllLocalTransactions(),
        repository.getAllCategories()
    ) { transactions, categories ->
        val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val expense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val totals = transactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        CategoryUiState(
            totalBalance = income - expense,
            totalIncome = income,
            totalExpense = expense,
            categoryTotals = totals,
            categories = categories.map { CategoryItem(it.name, it.iconName, it.isExpense) }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryUiState())

    private fun checkAndSeedCategories() {
        viewModelScope.launch {
            val current = repository.getAllCategories().first()
            if (current.isEmpty()) {
                val defaults = listOf(
                    CategoryItem("Food", "Food", true),
                    CategoryItem("Transport", "Transport", true),
                    CategoryItem("Salary", "Salary", false),
                    CategoryItem("Home", "Home", true)
                )
                defaults.forEach {
                    repository.addCategory(it.name, it.iconName, it.isExpense)
                }
            }
        }
    }

    fun addTransaction(amount: Double, category: String, note: String, isExpense: Boolean) {
        viewModelScope.launch {
            repository.addTransaction(
                description = note,
                amount = amount,
                date = LocalDate.now().toString(),
                category = category,
                type = if (isExpense) "EXPENSE" else "INCOME"
            ).first()
        }
    }

    fun overwriteTransaction(amount: Double, category: String, note: String, isExpense: Boolean) {
        viewModelScope.launch {
            repository.deleteTransactionsByCategory(category)

            repository.addTransaction(
                description = note.ifEmpty { "Enter amount description" },
                amount = amount,
                date = LocalDate.now().toString(),
                category = category,
                type = if (isExpense) "EXPENSE" else "INCOME"
            ).first()
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            repository.deleteCategory(name)
        }
    }

    fun addNewCategory(name: String, iconName: String, isExpense: Boolean) {
        viewModelScope.launch {
            repository.addCategory(name, iconName, isExpense)
        }
    }

    fun updateCategory(oldName: String, newName: String, newIcon: String, isExpense: Boolean) {
        viewModelScope.launch {
            repository.deleteCategory(oldName)
            repository.addCategory(newName, newIcon, isExpense)
        }
    }
}