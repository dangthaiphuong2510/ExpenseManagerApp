package com.example.basecomposemvvm.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.basecomposemvvm.designsystem.theme.AppTheme

@Composable
fun AppAlertDialog(
    title: String,
    content: String,
    confirmButtonText: String = "OK",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = content) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmButtonText)
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun AppAlertDialogPreview() {
    AppTheme {
        AppAlertDialog(
            title = "Error",
            content = "Something went wrong",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
