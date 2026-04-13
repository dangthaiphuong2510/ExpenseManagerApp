package com.example.expensemanager.feature.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanager.designsystem.theme.AppIcons
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(viewModel: CategoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(currentMonth) {
        viewModel.setMonthYear(currentMonth.monthValue, currentMonth.year)
    }

    var showHistory by remember { mutableStateOf(false) }
    var showTransactionForm by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }
    var showManage by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var categoryToEdit by remember { mutableStateOf<CategoryItem?>(null) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    var selectedCategoryName by remember { mutableStateOf("") }

    var transactionToEdit by remember { mutableStateOf<TransactionItem?>(null) }

    val displayList = uiState.categories.filter {
        if (selectedTab == 0) it.isExpense else !it.isExpense
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCategory = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.surface,
                shape = CircleShape
            ) {
                AppIcons.MyIcon(AppIcons.Add, size = 20.dp)
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            CategoryBalanceCard(
                totalBalance = uiState.totalBalance ?: 0.0,
                totalIncome = uiState.totalIncome ?: 0.0,
                totalExpense = uiState.totalExpense ?: 0.0
            ) {
                showManage = true
            }

            Spacer(Modifier.height(20.dp))

            CategoryMonthSelector(
                monthYearText = currentMonth.format(DateTimeFormatter.ofPattern("MM/yyyy")),
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                onCalendarClick = { showDatePicker = true }
            )

            Spacer(Modifier.height(12.dp))
            CategoryTabRow(selectedTab) { selectedTab = it }
            Spacer(Modifier.height(16.dp))

            CategoryGrid(
                displayList = displayList,
                categoryTotals = uiState.categoryTotals,
                modifier = Modifier.weight(1f),
                onItemClick = { name ->
                    selectedCategoryName = name
                    showHistory = true
                },
                onItemLongClick = { item ->
                    categoryToEdit = item
                }
            )
        }
    }

    CategoryDialogContainer(
        showManage = showManage,
        showAdd = showAddCategory,
        showHistory = showHistory,
        showTransaction = showTransactionForm,
        showDatePicker = showDatePicker,
        categoryToEdit = categoryToEdit,
        categoryToDelete = categoryToDelete,
        transactionToEdit = transactionToEdit,
        selectedCategoryName = selectedCategoryName,
        selectedTab = selectedTab,
        datePickerState = datePickerState,
        displayList = uiState.categories,

        transactionsForCategory = uiState.allTransactions.filter {
            it.category == selectedCategoryName
        },

        onDismiss = {
            showManage = false
            showAddCategory = false
            showTransactionForm = false
            showHistory = false
            showDatePicker = false
            categoryToEdit = null
            categoryToDelete = null
            transactionToEdit = null
        },
        onConfirmAdd = { name, icon ->
            viewModel.addNewCategory(name, icon, selectedTab == 0)
            showAddCategory = false
        },
        onConfirmEdit = { item, newName, newIcon ->
            if (newName.isNotEmpty()) {
                viewModel.updateCategory(item.name, newName, newIcon, item.isExpense)
                categoryToEdit = null
            }
        },
        onShowDeleteConfirm = { name ->
            categoryToDelete = name
        },
        onConfirmDelete = { name ->
            viewModel.deleteCategory(name)
            categoryToDelete = null
        },

        //logic handle transaction
        onAddNewTransaction = {
            transactionToEdit = null
            showTransactionForm = true
        },
        onEditTransaction = { transaction ->
            transactionToEdit = transaction
            showTransactionForm = true
        },
        onConfirmTransaction = { amount, note, dateMillis ->
            if (transactionToEdit == null) {
                viewModel.addTransaction(
                    amount = amount,
                    category = selectedCategoryName,
                    note = note,
                    isExpense = (selectedTab == 0),
                    dateMillis = dateMillis
                )
            } else {
                viewModel.updateTransaction(
                    id = transactionToEdit!!.id,
                    amount = amount,
                    note = note,
                    dateMillis = dateMillis
                )
            }
            showTransactionForm = false
            transactionToEdit = null
        },
        onDeleteTransaction = { id ->
            viewModel.deleteTransaction(id)
            showTransactionForm = false
            transactionToEdit = null
        },
        onDateSelected = { currentMonth = it }
    )
}