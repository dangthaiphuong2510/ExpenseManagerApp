package com.example.expensemanager.feature.category.categorydialogui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // CẦN THIÊM DÒNG NÀY
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensemanager.R
import com.example.expensemanager.designsystem.theme.ExpenseRed
import com.example.expensemanager.designsystem.theme.IncomeGreen
import com.example.expensemanager.feature.category.TransactionItem
import com.example.expensemanager.utils.format.formatWithLocalCurrency
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CategoryHistoryDialogUI(
    categoryName: String,
    transactions: List<TransactionItem>,
    currencyCode: String,
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
                Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                if (transactions.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("No data", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn {
                        items(transactions) { tx ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditTransaction(tx) }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.note, fontWeight = FontWeight.Medium)
                                    Text(
                                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                                            Date(tx.date)
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    tx.amount.formatWithLocalCurrency(currencyCode),
                                    color = if (tx.type == "EXPENSE") ExpenseRed else IncomeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider(thickness = 0.5.dp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onAddNew) {
                Text("Add New")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}