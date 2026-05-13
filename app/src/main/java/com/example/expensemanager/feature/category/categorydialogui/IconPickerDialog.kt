package com.example.expensemanager.feature.category.categorydialogui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensemanager.designsystem.theme.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerDialog(
    onDismiss: () -> Unit,
    onIconSelected: (String) -> Unit,
    selectedIcon: String,
    themeColor: Color
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = themeColor) }
        },
        title = {
            Text(
                text = "Select Icon",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppIcons.IconGroups.forEach { (groupName, icons) ->
                    item {
                        Column {
                            Text(
                                text = groupName,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            icons.chunked(5).forEach { rowIcons ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowIcons.forEach { (iconKey, resId) ->
                                        val isSelected = selectedIcon == iconKey
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) themeColor.copy(alpha = 0.2f)
                                                    else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .clickable {
                                                    onIconSelected(iconKey)
                                                    onDismiss()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppIcons.MyIcon(
                                                resourceId = resId,
                                                size = 24.dp,
                                                tint = if (isSelected) themeColor
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    )
}