package com.example.basecomposemvvm.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.basecomposemvvm.R
import com.example.basecomposemvvm.data.local.entity.TransactionEntity
import com.example.basecomposemvvm.designsystem.theme.ExpenseRed
import com.example.basecomposemvvm.designsystem.theme.IncomeGreen
import com.example.basecomposemvvm.feature.history.HistoryViewModel
import com.example.basecomposemvvm.utils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    isOnline: Boolean = true,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.historyState.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.history_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        // CHẶN: Vô hiệu hóa nút menu nếu mất mạng
                        IconButton(
                            onClick = { showMenu = true },
                            enabled = isOnline
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = if (isOnline) MaterialTheme.colorScheme.onSurface else Color.Gray.copy(alpha = 0.5f)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu && isOnline,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete_all), color = Color.Red) },
                                leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    showDeleteAllDialog = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.transactions.isEmpty()) {
                Text(text = stringResource(R.string.no_transactions_found), modifier = Modifier.align(Alignment.Center), color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = uiState.transactions, key = { it.id }) { item ->
                        HistoryTransactionItem(
                            transaction = item,
                            isOnline = isOnline, // Truyền xuống item để chặn nút xóa
                            onDelete = { viewModel.deleteTransaction(item.id) }
                        )
                    }
                }
            }
        }
    }

    // Chặn Dialog nếu mất mạng
    if (showDeleteAllDialog && isOnline) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearAllHistory(); showDeleteAllDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text(stringResource(R.string.delete), color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showDeleteAllDialog = false }) { Text(stringResource(R.string.action_cancel)) } },
            title = { Text(stringResource(R.string.confirm_delete_all_title)) },
            text = { Text(stringResource(R.string.confirm_delete_all_msg)) }
        )
    }
}

@Composable
fun HistoryTransactionItem(
    transaction: TransactionEntity,
    isOnline: Boolean,
    onDelete: () -> Unit
) {
    val isExpense = transaction.type.equals("EXPENSE", ignoreCase = true)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Icon hiển thị loại giao dịch... (giữ nguyên)

            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.description, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${transaction.category} • ${transaction.date}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = (if (isExpense) "-" else "+") + formatCurrency(transaction.amount), fontWeight = FontWeight.Black, color = if (isExpense) ExpenseRed else IncomeGreen)

                IconButton(
                    onClick = onDelete,
                    enabled = isOnline,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = if (isOnline) Color.LightGray.copy(alpha = 0.6f) else Color.LightGray.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}