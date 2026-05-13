package com.example.expensemanager.feature.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.expensemanager.R
import com.example.expensemanager.utils.format.CurrencyVisualTransformation
import com.example.expensemanager.utils.format.formatCurrencyForInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetActionDialog(
    title: String,
    categories: List<String>,
    initialCategory: String,
    initialAmount: String,
    isCategoryFixed: Boolean,
    currencyCode: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var selectedCat by remember {
        mutableStateOf(if (initialCategory.isEmpty() && categories.isNotEmpty()) categories[0] else initialCategory)
    }

    var amt by remember {
        mutableStateOf(initialAmount.toDoubleOrNull()?.toLong()?.toString() ?: "")
    }

    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(24.dp))

                if (!isCategoryFixed) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCat,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.select_category)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(text = category) },
                                    onClick = {
                                        selectedCat = category
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = selectedCat,
                        onValueChange = {},
                        enabled = false,
                        label = { Text(stringResource(R.string.category)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = amt,
                    onValueChange = { input ->
                        amt = input.filter { it.isDigit() }.take(12)
                    },
                    label = { Text(stringResource(R.string.enter_amount)) },
                    suffix = {
                        Text(
                            text = currencyCode,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = CurrencyVisualTransformation(currencyCode)
                )

                if (amt.isNotEmpty() && amt.length <= 8) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val suffixes = when (currencyCode.uppercase()) {
                            "VND", "₫" -> listOf("000", "0000", "00000", "000000")
                            "JPY", "¥", "KRW", "₩" -> listOf("00", "000", "0000")
                            else -> listOf("00", "000")
                        }

                        items(suffixes) { zeros ->
                            val suggestedValue = amt + zeros
                            if (suggestedValue.length <= 12) {
                                AssistChip(
                                    onClick = { amt = suggestedValue },
                                    label = {
                                        Text(formatCurrencyForInput(suggestedValue, currencyCode))
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val finalAmount = amt.toDoubleOrNull() ?: 0.0
                            onConfirm(selectedCat, finalAmount)
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = amt.isNotEmpty()
                    ) {
                        Text(
                            text = stringResource(R.string.Save),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}