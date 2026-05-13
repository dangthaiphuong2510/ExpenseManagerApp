package com.example.expensemanager.feature.category

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.expensemanager.R
import com.example.expensemanager.feature.category.categorydialogui.CategoryDeleteConfirmDialogUI
import com.example.expensemanager.feature.category.categorydialogui.CategoryFormDialogUI
import com.example.expensemanager.feature.category.categorydialogui.CategoryHistoryDialogUI
import com.example.expensemanager.feature.category.categorydialogui.CategoryManageDialogUI
import com.example.expensemanager.feature.category.categorydialogui.CategoryTransactionFormUI
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDialogContainer(
    showManage: Boolean,
    showAdd: Boolean,
    showHistory: Boolean,
    showTransaction: Boolean,
    showDatePicker: Boolean,
    currencyCode: String,
    selectedMonth: Int,
    selectedYear: Int,

    // data
    categoryToEdit: CategoryItem?,
    categoryToDelete: String?,
    transactionToEdit: TransactionItem?,
    selectedCategoryName: String,
    selectedTab: Int,
    datePickerState: DatePickerState,
    displayList: List<CategoryItem>,
    transactionsForCategory: List<TransactionItem>,
    noteError: String? = null,

    // callback functions
    onDismiss: () -> Unit,
    onConfirmAdd: (String, String, Boolean) -> Unit,
    onConfirmEdit: (CategoryItem, String, String, Boolean) -> Unit,
    onConfirmDelete: (String) -> Unit,
    onShowDeleteConfirm: (String) -> Unit,
    onReorderCategory: (List<CategoryItem>) -> Unit,

    // Logic Transaction
    onConfirmTransaction: (Double, String, Long) -> Unit,
    onAddNewTransaction: () -> Unit,
    onEditTransaction: (TransactionItem) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onAddNewCategoryFromManage: () -> Unit,
    onDateSelected: (YearMonth) -> Unit
) {
    //History Transaction
    if (showHistory) {
        CategoryHistoryDialogUI(
            categoryName = selectedCategoryName,
            transactions = transactionsForCategory,
            currencyCode = currencyCode,
            onDismiss = onDismiss,
            onEditTransaction = onEditTransaction,
            onAddNew = onAddNewTransaction
        )
    }

    //Form Transaction
    if (showTransaction) {
        CategoryTransactionFormUI(
            initialTransaction = transactionToEdit,
            categoryName = selectedCategoryName,
            currencyCode = currencyCode,
            isExpense = selectedTab == 0,

            selectedMonth = selectedMonth,
            selectedYear = selectedYear,

            onDismiss = onDismiss,
            onConfirm = onConfirmTransaction,
            noteError = noteError,
            onDelete = { transactionToEdit?.let { onDeleteTransaction(it.id) } }
        )
    }

    //Manage Categories
    if (showManage) {
        CategoryManageDialogUI(
            displayList = displayList,
            onDismiss = onDismiss,
            onOpenFullAdd = onAddNewCategoryFromManage,
            onDelete = { name -> onShowDeleteConfirm(name) },
            onReorder = onReorderCategory
        )
    }

    //Delete confirm
    categoryToDelete?.let { name ->
        CategoryDeleteConfirmDialogUI(
            categoryName = name,
            onDismiss = onDismiss,
            onConfirm = { onConfirmDelete(name) }
        )
    }

    //Add Category
    if (showAdd) {
        CategoryFormDialogUI(
            title = stringResource(R.string.add_new_category),
            onDismiss = onDismiss,
            onConfirm = { name, icon, isExp -> onConfirmAdd(name, icon, isExp) }
        )
    }

    //Edit Category
    categoryToEdit?.let { item ->
        CategoryFormDialogUI(
            title = stringResource(R.string.edit_category),
            initialName = item.name,
            initialIcon = item.iconName,
            initialIsExpense = item.isExpense,
            onDismiss = onDismiss,
            onConfirm = { n, i, isExp -> onConfirmEdit(item, n, i, isExp) }
        )
    }

    if (showDatePicker) {
        val initialMillis = remember(selectedMonth, selectedYear) {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            cal.set(Calendar.YEAR, selectedYear)
            cal.set(Calendar.MONTH, selectedMonth - 1)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }

        val localDatePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            initialDisplayedMonthMillis = initialMillis
        )

        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    localDatePickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        onDateSelected(YearMonth.of(date.year, date.monthValue))
                    }
                    onDismiss()
                }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = localDatePickerState)
        }
    }
}