package com.example.expensemanager.feature.category

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.expensemanager.R
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDialogContainer(

    showManage: Boolean,
    showAdd: Boolean,
    showHistory: Boolean,
    showTransaction: Boolean,
    showDatePicker: Boolean,

    //data
    categoryToEdit: CategoryItem?,
    categoryToDelete: String?,
    transactionToEdit: TransactionItem?,
    selectedCategoryName: String,
    selectedTab: Int,
    datePickerState: DatePickerState,
    displayList: List<CategoryItem>,
    transactionsForCategory: List<TransactionItem>,

    //callback function
    onDismiss: () -> Unit,
    onConfirmAdd: (String, String) -> Unit,
    onConfirmEdit: (CategoryItem, String, String) -> Unit,
    onConfirmDelete: (String) -> Unit,
    onShowDeleteConfirm: (String) -> Unit,

    // Logic Transaction new
    onConfirmTransaction: (Double, String, Long) -> Unit,
    onAddNewTransaction: () -> Unit,
    onEditTransaction: (TransactionItem) -> Unit,
    onDeleteTransaction: (Int) -> Unit,
    onDateSelected: (YearMonth) -> Unit
) {
    if (showHistory) {
        CategoryHistoryDialogUI(
            categoryName = selectedCategoryName,
            transactions = transactionsForCategory,
            onDismiss = onDismiss,
            onEditTransaction = onEditTransaction,
            onAddNew = onAddNewTransaction
        )
    }

    if (showTransaction) {
        CategoryTransactionFormUI(
            initialTransaction = transactionToEdit,
            categoryName = selectedCategoryName,
            isExpense = selectedTab == 0,
            onDismiss = onDismiss,
            onConfirm = onConfirmTransaction,
            onDelete = {
                transactionToEdit?.let { onDeleteTransaction(it.id) }
            }
        )
    }

    if (showManage) {
        CategoryManageDialogUI(
            displayList = displayList,
            onDismiss = onDismiss,
            onEdit = { item -> onConfirmEdit(item, "", "") },
            onDelete = { name -> onShowDeleteConfirm(name) }
        )
    }

    categoryToDelete?.let { name ->
        CategoryDeleteConfirmDialogUI(
            categoryName = name,
            onDismiss = onDismiss,
            onConfirm = { onConfirmDelete(name) }
        )
    }

    if (showAdd) {
        CategoryFormDialogUI(
            title = stringResource(R.string.add_new_category),
            onDismiss = onDismiss,
            onConfirm = onConfirmAdd
        )
    }

    categoryToEdit?.let { item ->
        CategoryFormDialogUI(
            title = stringResource(R.string.edit_category),
            initialName = item.name,
            initialIcon = item.iconName,
            onDismiss = onDismiss,
            onConfirm = { n, i -> onConfirmEdit(item, n, i) }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(YearMonth.of(date.year, date.month))
                    }
                    onDismiss()
                }) { Text(stringResource(R.string.action_ok)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}