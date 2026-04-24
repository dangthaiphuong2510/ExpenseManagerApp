package com.example.expensemanager.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.expensemanager.utils.format.formatWithLocalCurrency
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
    onNavigateToAddTransaction: () -> Unit,
    isOnline: Boolean,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.homeState.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()

    var showQuickAddSheet by remember { mutableStateOf(false) }
    val quickAddSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showNotificationSheet by remember { mutableStateOf(false) }
    val notificationSheetState = rememberModalBottomSheetState()

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(56.dp)
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_transaction),
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

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
                currencyCode = uiState.currencyCode,
                isLoading = uiState.isInitialLoad
            )

            Spacer(modifier = Modifier.height(28.dp))

            RecentTransactionsHeader(onSeeAllClick = onNavigateToHistory)

            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading && uiState.isInitialLoad) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.recentTransactions.isEmpty()) {
                    EmptyTransactionsPlaceholder()
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(items = uiState.recentTransactions, key = { it.id }) { item ->
                            TransactionItem(
                                item = item,
                                currencyCode = uiState.currencyCode,
                                formattedDate = dateFormatter.format(Date(item.date))
                            )
                        }
                    }
                }
            }
        }
    }

    //QUICK ADD BOTTOM SHEET
    if (showQuickAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQuickAddSheet = false },
            sheetState = quickAddSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            QuickAddTransactionSheet(
                categories = uiState.allCategories,
                currencyCode = uiState.currencyCode,
                categoryTotals = uiState.categoryTotals,

                onConfirm = { amount, note, dateMillis, category, isExpense ->
                    viewModel.addQuickTransaction(
                        amount = amount,
                        note = note,
                        dateMillis = dateMillis,
                        category = category,
                        isExpense = isExpense
                    )
                    showQuickAddSheet = false
                },
                onDismiss = { showQuickAddSheet = false }
            )
        }
    }

    if (showNotificationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationSheet = false },
            sheetState = notificationSheetState
        ) {
            NotificationBottomSheetContent(uiState = uiState)
        }
    }
}

@Composable
fun RecentTransactionsHeader(onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.recent_transactions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = onSeeAllClick) {
            Text(
                text = stringResource(id = R.string.see_all),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptyTransactionsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.no_transactions_found),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun NotificationBottomSheetContent(uiState: HomeUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = stringResource(id = R.string.notifications),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.budgetWarnings.isEmpty()) {
            Text(
                text = "No notification available",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            uiState.budgetWarnings.forEach { warning ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = warning,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}