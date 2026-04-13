package com.example.expensemanager.feature.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.expensemanager.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetActionDialog(
    title: String,
    categories: List<String>,
    initialCategory: String,
    initialAmount: String,
    isCategoryFixed: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var selectedCat by remember { mutableStateOf(if (initialCategory.isEmpty() && categories.isNotEmpty()) categories[0] else initialCategory) }
    var amt by remember { mutableStateOf(initialAmount) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = Color.White) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                if (!isCategoryFixed) {
                    Box {
                        OutlinedTextField(
                            value = selectedCat,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.select_category)) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { category ->
                                DropdownMenuItem(text = { Text(category) }, onClick = { selectedCat = category; expanded = false })
                            }
                        }
                    }
                } else {
                    Text("Category: $selectedCat", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = amt,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amt = it },
                    label = { Text("Limit Amount") },
                    suffix = { Text("đ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
                    Button(onClick = { onConfirm(selectedCat, amt.toDoubleOrNull() ?: 0.0) }, shape = RoundedCornerShape(12.dp)) {
                        Text(stringResource(R.string.Save))
                    }
                }
            }
        }
    }
}