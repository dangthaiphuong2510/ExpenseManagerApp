package com.example.expensemanager.feature.category.categorydialogui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.expensemanager.utils.format.CurrencyVisualTransformation
import com.example.expensemanager.utils.format.formatCurrencyForInput
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTransactionFormUI(
    initialTransaction: TransactionItem? = null,
    categoryName: String,
    currencyCode: String,
    isExpense: Boolean,
    selectedMonth: Int,
    selectedYear: Int,
    noteError: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, Long) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var amount by remember {
        mutableStateOf(initialTransaction?.amount?.toLong()?.toString() ?: "")
    }
    var note by remember { mutableStateOf(initialTransaction?.note ?: "") }

    val initialDateMillis = remember(selectedMonth, selectedYear) {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val currentCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val currentMonth = currentCal.get(Calendar.MONTH) + 1
        val currentYear = currentCal.get(Calendar.YEAR)

        if (selectedMonth == currentMonth && selectedYear == currentYear) {
            currentCal.timeInMillis
        } else {
            cal.set(Calendar.YEAR, selectedYear)
            cal.set(Calendar.MONTH, selectedMonth - 1)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
    }

    var date by remember { mutableLongStateOf(initialTransaction?.date ?: initialDateMillis) }
    var showPicker by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date,
            initialDisplayedMonthMillis = date
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    date = state.selectedDateMillis ?: date
                    showPicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            }
        ) { DatePicker(state) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                if (initialTransaction == null) "Add Entry" else "Edit Entry",
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
                OutlinedCard(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AppIcons.MyIcon(
                            AppIcons.Calendar,
                            size = 20.dp,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(dateFormatter.format(Date(date)))
                    }
                }
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        amount = newValue.filter { it.isDigit() }.take(12)
                    },
                    label = { Text("Amount") },
                    suffix = {
                        Text(
                            currencyCode, // Tự động hiển thị VND, USD...
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    // Đổi sang KeyboardType.Number vì đã lọc chữ số
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = CurrencyVisualTransformation(currencyCode)
                )

                //QUICK SUGGESTIONS
                if (amount.isNotEmpty() && amount.length <= 8) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val suffixes = when (currencyCode.uppercase()) {
                            "VND", "₫" -> listOf("000", "0000", "00000", "000000")
                            "JPY", "¥", "KRW", "₩" -> listOf("00", "000", "0000")
                            else -> listOf("00", "000")
                        }

                        items(suffixes) { zeros ->
                            val suggestedValue = amount + zeros
                            if (suggestedValue.length <= 12) {
                                AssistChip(
                                    onClick = { amount = suggestedValue },
                                    label = {
                                        Text(formatCurrencyForInput(suggestedValue, currencyCode))
                                    }
                                )
                            }
                        }
                    }
                }

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