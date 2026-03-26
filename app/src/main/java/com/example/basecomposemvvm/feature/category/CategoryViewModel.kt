package com.example.basecomposemvvm.feature.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basecomposemvvm.data.remote.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class CategoryUiState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categoryTotals: Map<String, Double> = emptyMap()
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    val uiState: StateFlow<CategoryUiState> = repository.getAllLocalTransactions()
        .map { transactions ->
            val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            val totals = transactions.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            CategoryUiState(
                totalBalance = income - expense,
                totalIncome = income,
                totalExpense = expense,
                categoryTotals = totals
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CategoryUiState()
        )

    fun addTransaction(amount: Double, category: String, note: String, isExpense: Boolean) {
        viewModelScope.launch {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val date = LocalDateTime.now().format(formatter)

            val type = if (isExpense) "EXPENSE" else "INCOME"

            repository.addTransaction(
                description = note,
                amount = amount,
                date = date,
                category = category,
                type = type
            ).collect { result ->
            }
        }
    }

    fun addNewCategory(name: String) {
        viewModelScope.launch {
        }
    }
}