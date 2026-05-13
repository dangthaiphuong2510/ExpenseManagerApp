package com.example.expensemanager.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensemanager.R
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.designsystem.theme.ExpenseRed
import com.example.expensemanager.feature.home.HomeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBottomSheet(
    sheetState: SheetState,
    uiState: HomeUiState,
    onDismiss: () -> Unit,
    onNotificationClick: (String) -> Unit,
    onMarkAllAsRead: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.notifications),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (uiState.unreadCount > 0) {
                    TextButton(onClick = onMarkAllAsRead) {
                        Text(
                            text = "Mark all as read",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You have no notifications",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(uiState.notifications) { notif ->
                        val isWarning =
                            notif.message.contains("Warning") || notif.message.contains("exceeding")
                        val iconRes = if (isWarning) AppIcons.Info else AppIcons.Edit
                        val iconColor =
                            if (isWarning) ExpenseRed else MaterialTheme.colorScheme.primary
                        val title = if (isWarning) "Spending Warning" else "Daily Reminder"

                        NotificationRow(
                            title = title,
                            message = notif.message,
                            icon = iconRes,
                            iconColor = iconColor,
                            isRead = notif.isRead,
                            onClick = { onNotificationClick(notif.message) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationRow(
    title: String,
    message: String,
    icon: Int,
    iconColor: Color,
    isRead: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (!isRead) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        Color.Transparent
    }

    val titleColor =
        if (!isRead) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    val messageColor = if (!isRead) MaterialTheme.colorScheme.onSurface else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AppIcons.MyIcon(
                resourceId = icon,
                tint = if (!isRead) iconColor else Color.Gray,
                size = 20.dp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = if (!isRead) FontWeight.ExtraBold else FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = messageColor,
                fontWeight = if (!isRead) FontWeight.Medium else FontWeight.Normal
            )
        }
        if (!isRead) {
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color.Red, CircleShape)
            )
        }
    }
}