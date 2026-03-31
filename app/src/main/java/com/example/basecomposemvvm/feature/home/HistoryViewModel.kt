package com.example.basecomposemvvm.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basecomposemvvm.data.local.entity.TransactionEntity
import com.example.basecomposemvvm.data.remote.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. Định nghĩa UI State cho màn hình History
data class HistoryUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    // 2. Quan sát toàn bộ giao dịch từ Local DB
    val historyState: StateFlow<HistoryUiState> = repository.getAllLocalTransactions()
        .map { transactions ->
            // Sắp xếp giao dịch mới nhất lên đầu (Dựa trên date hoặc ID)
            val sortedList = transactions.sortedByDescending { it.date }
            HistoryUiState(transactions = sortedList, isLoading = false)
        }
        .onStart {
            // Có thể hiển thị loading khi bắt đầu load
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState(isLoading = true)
        )

    /**
     * Xóa một giao dịch cụ thể theo ID
     * Được gọi khi người dùng quẹt (swipe) hoặc nhấn nút xóa trên item
     */
    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    /**
     * Xóa sạch toàn bộ lịch sử giao dịch
     * Được gọi khi nhấn nút "Clear All" trong menu hoặc dialog
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllTransactions()
        }
    }

    // Bạn có thể thêm tính năng lọc (Filter) theo Category hoặc Type nếu cần
    fun filterTransactions(query: String) {
        // Logic lọc dữ liệu...
    }
}