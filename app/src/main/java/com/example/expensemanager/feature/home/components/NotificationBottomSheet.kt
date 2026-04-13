package com.example.expensemanager.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.designsystem.theme.ExpenseRed
import com.example.expensemanager.feature.home.HomeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBottomSheet(
    sheetState: SheetState,
    uiState: HomeUiState,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(text = "Notifications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.notificationCount == 0) {
                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Text("No new notifications", color = Color.Gray)
                }
            } else {
                uiState.budgetWarning?.let {
                    NotificationRow(title = "Budget Alert", message = it, icon = AppIcons.Info, color = ExpenseRed)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                }

                val hasToday = uiState.recentTransactions.any { android.text.format.DateUtils.isToday(it.date) }
                if (!hasToday) {
                    NotificationRow(
                        title = "Daily Reminder",
                        message = "Don't forget to track your expenses today!",
                        icon = AppIcons.Edit,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(title: String, message: String, icon: Int, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(44.dp).background(color.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
            AppIcons.MyIcon(resourceId = icon, tint = color, size = 20.dp)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}