package com.example.expensemanager.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanager.R
import com.example.expensemanager.feature.home.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToSetting: () -> Unit,
    isOnline: Boolean,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.homeState.collectAsStateWithLifecycle()

    // Sử dụng lại showNotificationSheet và sheetState
    var showNotificationSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        HomeHeader(
            notificationCount = uiState.notificationCount,
            onNotificationClick = {
                showNotificationSheet = true
                viewModel.markNotificationsAsRead()
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        TotalBalanceCard(
            balance = uiState.totalBalance ?: 0.0,
            income = uiState.totalIncome ?: 0.0,
            expense = uiState.totalExpense ?: 0.0,
            isLoading = uiState.isInitialLoad
        )

        Spacer(modifier = Modifier.height(28.dp))

        RecentTransactionsHeader(onSeeAllClick = onNavigateToHistory)

        Spacer(modifier = Modifier.height(10.dp))

        //Transactions List
        Box(modifier = Modifier.weight(1f)) {
            if (uiState.isLoading && uiState.isInitialLoad) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.recentTransactions.isEmpty()) {
                EmptyTransactionsPlaceholder()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(items = uiState.recentTransactions, key = { it.id }) { item ->
                        TransactionItem(
                            item = item,
                            formattedDate = dateFormatter.format(Date(item.date))
                        )
                    }
                }
            }
        }
    }

    // Quay lại sử dụng NotificationBottomSheet
    if (showNotificationSheet) {
        NotificationBottomSheet(
            sheetState = sheetState,
            uiState = uiState, // Lưu ý: File này cần được cập nhật List budgetWarnings
            onDismiss = { showNotificationSheet = false }
        )
    }
}

@Composable
fun RecentTransactionsHeader(onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.recent_transactions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onSeeAllClick) {
            Text(text = stringResource(R.string.see_all), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptyTransactionsPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.no_transactions_found),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}