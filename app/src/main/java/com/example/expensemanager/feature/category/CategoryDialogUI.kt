package com.example.expensemanager.feature.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.expensemanager.R
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.designsystem.theme.ExpenseRed
import com.example.expensemanager.utils.format.formatWithLocalCurrency
import java.text.SimpleDateFormat
import java.util.*

// --- CATEGORY MANAGEMENT DIALOG ---
@Composable
fun CategoryManageDialogUI(
    displayList: List<CategoryItem>,
    onDismiss: () -> Unit,
    onEdit: (CategoryItem) -> Unit,
    onDelete: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.manage_categories), fontWeight = FontWeight.Bold) },
        containerColor = Color.White,
        text = {
            Box(modifier = Modifier.height(300.dp)) {
                LazyColumn {
                    items(displayList) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppIcons.MyIcon(
                                    AppIcons.getIconIdByName(item.iconName),
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 20.dp
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(item.name, fontWeight = FontWeight.Bold)
                            }
                            Row {
                                IconButton(onClick = { onEdit(item) }) {
                                    AppIcons.MyIcon(AppIcons.Edit, size = 18.dp)
                                }
                                IconButton(onClick = { onDelete(item.name) }) {
                                    AppIcons.MyIcon(
                                        AppIcons.Delete,
                                        tint = ExpenseRed,
                                        size = 19.dp
                                    )
                                }
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } }
    )
}

// --- CATEGORY TRANSACTION HISTORY DIALOG ---
@Composable
fun CategoryHistoryDialogUI(
    categoryName: String,
    transactions: List<TransactionItem>,
    onDismiss: () -> Unit,
    onEditTransaction: (TransactionItem) -> Unit,
    onAddNew: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("History: $categoryName", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (transactions.isEmpty()) {
                    Text(
                        "No transactions in this month.",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(transactions) { item ->
                            ListItem(
                                modifier = Modifier.clickable { onEditTransaction(item) },
                                headlineContent = {
                                    Text(
                                        text = item.amount.formatWithLocalCurrency(),
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                supportingContent = { Text(item.note.ifEmpty { "No note" }) },
                                trailingContent = {
                                    val date =
                                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                                            Date(item.date)
                                        )
                                    Text(date, style = MaterialTheme.typography.labelSmall)
                                }
                            )
                            HorizontalDivider(thickness = 0.5.dp)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onAddNew,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Add New Transaction")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } }
    )
}

//transaction form dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTransactionFormUI(
    initialTransaction: TransactionItem? = null,
    categoryName: String,
    isExpense: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, Long) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var amount by remember {
        mutableStateOf(
            initialTransaction?.amount?.toLong()?.toString() ?: ""
        )
    }
    var note by remember { mutableStateOf(initialTransaction?.note ?: "") }
    var selectedDateMillis by remember {
        mutableLongStateOf(
            initialTransaction?.date ?: System.currentTimeMillis()
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis ?: selectedDateMillis
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = if (initialTransaction == null)
                    (if (isExpense) stringResource(R.string.add_expense) else stringResource(R.string.add_income))
                else "Edit Transaction",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Category: $categoryName",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))

                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcons.MyIcon(
                            AppIcons.Calendar,
                            size = 20.dp,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                                Date(
                                    selectedDateMillis
                                )
                            )
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amount = it },
                    label = { Text(stringResource(R.string.enter_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.note)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (initialTransaction != null) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { onDelete?.invoke() },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text("Delete this transaction", color = ExpenseRed)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0, note, selectedDateMillis) },
                shape = RoundedCornerShape(12.dp)
            ) { Text(stringResource(R.string.Save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

// --- ADD/EDIT CATEGORY DIALOG ---
@Composable
fun CategoryFormDialogUI(
    title: String,
    initialName: String = "",
    initialIcon: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(if (initialIcon.isEmpty()) AppIcons.CategoryIconsList[0].first else initialIcon) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = Color.White) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                AppIcons.MyIcon(
                    AppIcons.getIconIdByName(selectedIcon),
                    size = 48.dp,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(AppIcons.CategoryIconsList) { pair ->
                        Box(
                            Modifier
                                .size(45.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedIcon == pair.first) MaterialTheme.colorScheme.primary.copy(
                                        0.2f
                                    ) else Color.Transparent
                                )
                                .clickable { selectedIcon = pair.first },
                            contentAlignment = Alignment.Center
                        ) {
                            AppIcons.MyIcon(
                                pair.second,
                                size = 24.dp,
                                tint = if (selectedIcon == pair.first) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
                    Button(
                        onClick = { if (name.isNotBlank()) onConfirm(name, selectedIcon) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.action_ok))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDeleteConfirmDialogUI(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Category?") },
        text = { Text("Are you sure you want to delete '$categoryName'?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.delete),
                    color = Color.Red
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}