package com.example.expensemanager.feature.budget

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import java.time.YearMonth
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanager.R
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currencyCode = uiState.currencyCode // Lấy mã tiền tệ (VND/USD)

    var showDialog by remember { mutableStateOf(false) }
    var selectedCategoryToEdit by remember { mutableStateOf<BudgetItem?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    val chartColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        Color(0xFFFFB74D),
        Color(0xFF81C784),
        Color(0xFFBA68C8)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            BudgetTopBar(
                selectedMonth = try {
                    YearMonth.of(uiState.selectedYear, uiState.selectedMonth.coerceIn(1, 12))
                } catch (e: Exception) {
                    YearMonth.now()
                },
                onMonthChange = { viewModel.changeMonth(it) },
                onTitleClick = { showDatePicker = true }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            BudgetSummaryCard(
                totalLimit = uiState.totalBudgetLimit,
                totalSpent = uiState.totalSpent,
                budgetList = uiState.budgetList,
                currencyCode = currencyCode,
                chartColors = chartColors
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.category),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { showDialog = true }) {
                    Text(stringResource(R.string.set_budget), fontWeight = FontWeight.Bold)
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                BudgetCategoryList(
                    modifier = Modifier.fillMaxSize(),
                    budgetList = uiState.budgetList,
                    chartColors = chartColors,
                    currencyCode = currencyCode,
                    onItemClick = { selectedCategoryToEdit = it }
                )
            }
        }
    }

    if (showDialog || selectedCategoryToEdit != null) {
        BudgetActionDialog(
            title = if (selectedCategoryToEdit != null)
                stringResource(R.string.edit_category)
            else
                stringResource(R.string.set_budget),
            categories = uiState.allCategories,
            initialCategory = selectedCategoryToEdit?.category ?: "",
            initialAmount = selectedCategoryToEdit?.limit?.toString() ?: "",
            isCategoryFixed = selectedCategoryToEdit != null,
            currencyCode = currencyCode,
            onDismiss = {
                showDialog = false
                selectedCategoryToEdit = null
            },
            onConfirm = { cat, amtDouble ->
                if (cat.isNotEmpty()) {
                    viewModel.updateBudget(cat, amtDouble)
                }
                showDialog = false
                selectedCategoryToEdit = null
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.setMonthYear(date.monthValue, date.year)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}