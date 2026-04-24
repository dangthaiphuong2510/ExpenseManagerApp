package com.example.expensemanager.feature.category.categorydialogui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensemanager.R
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.designsystem.theme.ExpenseRed
import com.example.expensemanager.feature.category.TransactionItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTransactionFormUI(
    initialTransaction: TransactionItem? = null,
    categoryName: String,
    currencyCode: String,
    isExpense: Boolean,
    noteError: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, Long) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var amount by remember { mutableStateOf(initialTransaction?.amount?.toString() ?: "") }
    var note by remember { mutableStateOf(initialTransaction?.note ?: "") }
    var date by remember { mutableLongStateOf(initialTransaction?.date ?: System.currentTimeMillis()) }
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        val state = rememberDatePickerState(date)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    date = state.selectedDateMillis ?: date; showPicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            }
        ) { DatePicker(state) }
    }

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.White,
        title = {
            Text(if (initialTransaction == null) "Add Entry" else "Edit Entry", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Category: $categoryName", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                OutlinedCard(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AppIcons.MyIcon(AppIcons.Calendar, size = 20.dp, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(date)))
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                    label = { Text("Amount") },
                    suffix = { Text(currencyCode, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.note)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = noteError != null,
                    supportingText = { if (noteError != null) Text(noteError) }
                )
                if (initialTransaction != null) {
                    TextButton(onClick = { onDelete?.invoke() }) {
                        Text(stringResource(R.string.delete), color = ExpenseRed)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0, note, date) }) {
                Text(stringResource(R.string.Save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}