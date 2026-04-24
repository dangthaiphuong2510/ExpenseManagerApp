package com.example.expensemanager.feature.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanager.R
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    isSelectionMode: Boolean = false,
    onBack: () -> Unit = {},
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currencyCode = uiState.currencyCode

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val datePickerState = rememberDatePickerState()

    var showHistory by remember { mutableStateOf(false) }
    var showTransactionForm by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }
    var showManage by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var categoryToEdit by remember { mutableStateOf<CategoryItem?>(null) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    var selectedCategoryName by remember { mutableStateOf("") }
    var transactionToEdit by remember { mutableStateOf<TransactionItem?>(null) }
    var transactionNoteError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { message ->
            transactionNoteError = message
        }
    }

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collect {
            showTransactionForm = false
            showAddCategory = false
            categoryToEdit = null
            transactionToEdit = null
            transactionNoteError = null

            if (isSelectionMode) {
                onBack()
            }
        }
    }

    LaunchedEffect(currentMonth) {
        viewModel.setMonthYear(currentMonth.monthValue, currentMonth.year)
    }

    val filteredList = uiState.categories.filter {
        if (selectedTab == 0) it.isExpense else !it.isExpense
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            CategoryBalanceCard(
                totalBalance = uiState.totalBalance ?: 0.0,
                totalIncome = uiState.totalIncome ?: 0.0,
                totalExpense = uiState.totalExpense ?: 0.0,
                currencyCode = currencyCode,
                onManageClick = { showManage = true }
            )

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
                displayList = filteredList,
                categoryTotals = uiState.categoryTotals,
                currencyCode = currencyCode,
                modifier = Modifier.weight(1f),
                showAddCard = true,
                onItemClick = { name ->
                    selectedCategoryName = name
                    if (isSelectionMode) {
                        showTransactionForm = true
                    } else {
                        showHistory = true
                    }
                },
                onItemLongClick = { item ->
                    if (!isSelectionMode) {
                        categoryToEdit = item
                    }
                },
                onAddCategoryClick = {
                    showAddCategory = true
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
        currencyCode = currencyCode,
        categoryToEdit = categoryToEdit,
        categoryToDelete = categoryToDelete,
        transactionToEdit = transactionToEdit,
        selectedCategoryName = selectedCategoryName,
        selectedTab = selectedTab,
        datePickerState = datePickerState,
        displayList = uiState.categories,
        noteError = transactionNoteError,
        transactionsForCategory = uiState.allTransactions.filter {
            it.category == selectedCategoryName
        },
        onDismiss = {
            when {
                categoryToDelete != null -> categoryToDelete = null
                categoryToEdit != null -> categoryToEdit = null
                showAddCategory -> showAddCategory = false
                showTransactionForm -> {
                    showTransactionForm = false
                    transactionToEdit = null
                    transactionNoteError = null
                }
                showHistory -> showHistory = false
                showDatePicker -> showDatePicker = false
                showManage -> showManage = false
            }
        },
        onConfirmAdd = { name, icon, isExp ->
            viewModel.addNewCategory(name, icon, isExp)
        },
        onConfirmEdit = { item, newName, newIcon, newIsExp ->
            if (newName.isNotEmpty()) {
                viewModel.updateCategory(
                    oldName = item.name,
                    newName = newName,
                    newIcon = newIcon,
                    isExpense = newIsExp
                )
            }
        },
        onShowDeleteConfirm = { name ->
            categoryToDelete = name
        },
        onConfirmDelete = { name ->
            viewModel.deleteCategory(name)
            categoryToDelete = null
        },
        onReorderCategory = { newList ->
        },
        onAddNewCategoryFromManage = {
            showAddCategory = true
        },
        onAddNewTransaction = {
            transactionToEdit = null
            transactionNoteError = null
            showTransactionForm = true
        },
        onEditTransaction = { transaction ->
            transactionToEdit = transaction
            transactionNoteError = null
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
        },
        onDeleteTransaction = { id ->
            viewModel.deleteTransaction(id)
            showTransactionForm = false
            transactionToEdit = null
        },
        onDateSelected = { currentMonth = it }
    )
}