package com.example.expensemanager.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanager.R
import com.example.expensemanager.data.local.entity.TransactionEntity
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.feature.home.components.TransactionItem as TransactionCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.historyState.collectAsStateWithLifecycle()
    var isSearchMode by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val currencyCode = uiState.currencyCode

    val monthTitle = remember(uiState.selectedMonth, uiState.selectedYear) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, uiState.selectedYear)
        cal.set(Calendar.MONTH, uiState.selectedMonth - 1)
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    LaunchedEffect(isSearchMode) {
        if (isSearchMode) focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            if (!isSearchMode) {
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
                    actions = {
                        IconButton(onClick = { isSearchMode = true }) {
                            AppIcons.MyIcon(resourceId = AppIcons.Search)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            } else {
                TopAppBar(
                    title = {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.filterTransactions(it) },
                            placeholder = { Text(stringResource(R.string.search_transactions)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchMode = false
                            viewModel.filterTransactions("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    },
                    actions = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.filterTransactions("") }) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        },
        containerColor = if (isSearchMode) Color.White else MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            if (isSearchMode) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(horizontal = 20.dp)
                ) {
                    if (uiState.searchQuery.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Type to search...", color = Color.Gray)
                        }
                    } else {
                        ResultList(uiState.transactions, dateFormatter, currencyCode)
                    }
                }
            } else {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                    AppIcons.ChevronLeft,
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
                                Text(monthTitle, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                AppIcons.MyIcon(
                                    AppIcons.Calendar,
                                    size = 18.dp,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { viewModel.nextMonth() }) {
                                AppIcons.MyIcon(
                                    AppIcons.ChevronRight,
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 18.dp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    ResultList(uiState.transactions, dateFormatter, currencyCode)
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance().apply { timeInMillis = it }
                        viewModel.selectDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun ResultList(
    transactions: List<TransactionEntity>,
    dateFormatter: SimpleDateFormat,
    currencyCode: String
) {
    if (transactions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_transactions_found))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(items = transactions, key = { it.id }) { transaction ->
                TransactionCard(
                    item = transaction,
                    currencyCode = currencyCode,
                    formattedDate = dateFormatter.format(Date(transaction.date))
                )
            }
        }
    }
}