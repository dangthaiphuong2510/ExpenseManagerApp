package com.example.expensemanager.feature.category.categorydialogui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.designsystem.theme.ExpenseRed
import com.example.expensemanager.designsystem.theme.IncomeGreen

@Composable
fun CategoryFormDialogUI(
    title: String,
    initialName: String = "",
    initialIcon: String = "",
    initialIsExpense: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(if (initialIcon.isEmpty()) "ic_others" else initialIcon) }
    var isExpense by remember { mutableStateOf(initialIsExpense) }
    var showIconPicker by remember { mutableStateOf(false) }
    val themeColor = if (isExpense) ExpenseRed else IncomeGreen

    if (showIconPicker) {
        IconPickerDialog(
            onDismiss = { showIconPicker = false },
            onIconSelected = { selectedIcon = it },
            selectedIcon = selectedIcon,
            themeColor = themeColor
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
            Column(
                Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    val mod = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                    Box(
                        modifier = mod
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isExpense) ExpenseRed else Color.Transparent)
                            .clickable { isExpense = true }, contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Expense",
                            color = if (isExpense) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = mod
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isExpense) IncomeGreen else Color.Transparent)
                            .clickable { isExpense = false }, contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Income",
                            color = if (!isExpense) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedCard(
                    onClick = { showIconPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Icon", color = Color.Gray)
                        AppIcons.MyIcon(
                            AppIcons.getIconIdByName(selectedIcon),
                            size = 24.dp,
                            tint = themeColor
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) onConfirm(
                                name,
                                selectedIcon,
                                isExpense
                            )
                        },
                        Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(themeColor)
                    ) { Text("Save") }
                }
            }
        }
    }
}