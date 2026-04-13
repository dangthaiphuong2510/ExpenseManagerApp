package com.example.expensemanager.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHeader(
    notificationCount: Int,
    onNotificationClick: () -> Unit
) {
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val displayName = currentUser?.displayName ?: stringResource(id = R.string.user_name)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.home_welcome_back),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            BadgedBox(
                badge = {
                    if (notificationCount > 0) {
                        Badge(containerColor = ExpenseRed) {
                            Text(text = notificationCount.toString(), color = Color.White)
                        }
                    }
                }
            ) {
                AppIcons.MyIcon(
                    resourceId = AppIcons.Notifications,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 22.dp
                )
            }
        }
    }
}