package com.example.expensemanager.feature.category.categorydialogui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.expensemanager.R
import com.example.expensemanager.designsystem.theme.ExpenseRed

@Composable
fun CategoryDeleteConfirmDialogUI(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Category") },
        text = { Text("Delete '$categoryName'? Transactions will be kept.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(ExpenseRed)
            ) { Text(stringResource(R.string.delete), color = Color.White) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}