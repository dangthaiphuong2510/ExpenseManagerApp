package com.example.expensemanager.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanager.R
import com.example.expensemanager.designsystem.theme.AppIcons
// Chú ý: Đảm bảo import đúng đường dẫn component bạn đã tách
import com.example.expensemanager.feature.home.components.TransactionItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.historyState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val monthTitle = remember(uiState.selectedMonth, uiState.selectedYear) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, uiState.selectedYear)
        cal.set(Calendar.MONTH, uiState.selectedMonth - 1)
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.history_title),
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        AppIcons.MyIcon(resourceId = AppIcons.ChevronLeft, size = 18.dp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Month Selector Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        AppIcons.MyIcon(
                            resourceId = AppIcons.ChevronLeft,
                            tint = MaterialTheme.colorScheme.primary,
                            size = 18.dp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        AppIcons.MyIcon(
                            resourceId = AppIcons.Calendar,
                            size = 18.dp,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { viewModel.nextMonth() }) {
                        AppIcons.MyIcon(
                            resourceId = AppIcons.ChevronRight,
                            tint = MaterialTheme.colorScheme.primary,
                            size = 18.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search TextField
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.filterTransactions(it) },
                placeholder = { Text(stringResource(R.string.search_transactions)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { AppIcons.MyIcon(AppIcons.Search) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(0.5f),
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // List History
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.transactions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.no_transactions_found),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(
                        items = uiState.transactions,
                        key = { it.id }
                    ) { item ->
                        TransactionItem(
                            item = item,
                            formattedDate = dateFormatter.format(Date(item.date))
                        )
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        viewModel.selectDate(
                            year = cal.get(Calendar.YEAR),
                            month = cal.get(Calendar.MONTH) + 1
                        )
                    }
                    showDatePicker = false
                }) { Text(stringResource(id = R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) { Text(stringResource(id = R.string.action_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}