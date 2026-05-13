package com.example.expensemanager.feature.category.categorydialogui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensemanager.R
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.designsystem.theme.ExpenseRed
import com.example.expensemanager.designsystem.theme.IncomeGreen
import com.example.expensemanager.feature.category.CategoryItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryManageDialogUI(
    displayList: List<CategoryItem>,
    onDismiss: () -> Unit,
    onOpenFullAdd: () -> Unit,
    onDelete: (String) -> Unit,
    onReorder: (List<CategoryItem>) -> Unit
) {
    val listData = remember(displayList) { mutableStateListOf(*displayList.toTypedArray()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Categories", fontWeight = FontWeight.Bold) },
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedCard(
                    onClick = onOpenFullAdd,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.Gray)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.add_new_category), color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()

                Box(Modifier.height(350.dp)) {
                    LazyColumn {
                        itemsIndexed(listData, key = { _, item -> item.name }) { index, item ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DragHandle, null,
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .pointerInput(Unit) {
                                            detectDragGesturesAfterLongPress(
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    val target =
                                                        if (dragAmount.y > 0) index + 1 else index - 1
                                                    if (target in listData.indices) {
                                                        listData.apply {
                                                            val from = get(index)
                                                            set(index, get(target))
                                                            set(target, from)
                                                        }
                                                    }
                                                },
                                                onDragEnd = { onReorder(listData.toList()) }
                                            )
                                        }
                                )
                                AppIcons.MyIcon(
                                    AppIcons.getIconIdByName(item.iconName),
                                    size = 24.dp,
                                    tint = if (item.isExpense) ExpenseRed else IncomeGreen
                                )
                                Text(
                                    item.name, Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp), fontWeight = FontWeight.Medium
                                )
                                IconButton(onClick = { onDelete(item.name) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}