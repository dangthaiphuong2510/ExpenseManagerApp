package com.example.basecomposemvvm.feature.budget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.basecomposemvvm.R
import java.text.DecimalFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    isOnline: Boolean = true,
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val formatter = DateTimeFormatter.ofPattern("MM/yyyy")

    var showDatePicker by remember { mutableStateOf(false) }

    var budgetList by remember {
        mutableStateOf(
            listOf(
                Triple("Food", 2000000, 1200000),
                Triple("Transport", 1000000, 300000),
                Triple("Education", 3000000, 2000000)
            )
        )
    }

    var totalBudgetLimit by remember { mutableIntStateOf(6000000) }
    var showDialog by remember { mutableStateOf(false) }
    var showEditTotalDialog by remember { mutableStateOf(false) }
    var selectedCategoryToEdit by remember { mutableStateOf<String?>(null) }

    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val categories = listOf("Food", "Transport", "Education", "Health", "Shopping")

    val chartColors = listOf(
        colorScheme.primary,
        colorScheme.secondary,
        Color(0xFFFFB74D),
        Color(0xFF81C784),
        Color(0xFFBA68C8)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(top = 8.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                showDialog = true
                category = ""
                amount = ""
            }) {
                Text(stringResource(R.string.set_budget), fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Text("<", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                currentMonth.format(formatter),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clickable { showDatePicker = true }
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Text(">", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
            }
        }

        Spacer(Modifier.height(16.dp))

        val totalUsed = budgetList.sumOf { it.third }
        val remaining = totalBudgetLimit - totalUsed

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        var startAngle = -90f
                        if (budgetList.isEmpty() || totalBudgetLimit == 0) {
                            drawCircle(color = Color(0xFFEEEEEE), style = Stroke(width = 20f))
                        } else {
                            budgetList.forEachIndexed { index, item ->
                                val sweepAngle = (item.second.toFloat() / totalBudgetLimit) * 360f
                                drawArc(
                                    color = chartColors[index % chartColors.size],
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = 40f, cap = StrokeCap.Round)
                                )
                                startAngle += sweepAngle
                            }
                            val totalSet = budgetList.sumOf { it.second }
                            if (totalSet < totalBudgetLimit) {
                                drawArc(
                                    color = Color(0xFFF0F0F0),
                                    startAngle = startAngle,
                                    sweepAngle = 360f - (totalSet.toFloat() / totalBudgetLimit * 360f),
                                    useCenter = false,
                                    style = Stroke(width = 40f)
                                )
                            }
                        }
                    }

                    val usagePercent =
                        if (totalBudgetLimit > 0) (totalUsed * 100 / totalBudgetLimit) else 0
                    Text(
                        "$usagePercent%",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.Remaining),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        IconButton(
                            onClick = { showEditTotalDialog = true },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                    Text(
                        formatMoney(remaining),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (remaining < 0) Color(0xFFE74C3C) else Color(0xFF2D3436)
                    )
                    Divider(Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F2F6))
                    Text(
                        "Limit: ${formatMoney(totalBudgetLimit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Category Allocation",
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3436)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            items(budgetList.size) { i ->
                val item = budgetList[i]
                // Tỷ lệ % dựa trên số tiền đã tiêu / hạn mức của Category đó
                val percentUsed = if (item.second == 0) 0f else item.third.toFloat() / item.second
                var showItemMenu by remember { mutableStateOf(false) }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(chartColors[i % chartColors.size], CircleShape)
                        )

                        Spacer(Modifier.width(16.dp))

                        Column(Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    item.first,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2D3436)
                                )
                                //% của Budget Category này chiếm trong Tổng Budget
                                val ratioInTotal =
                                    (item.second.toFloat() / totalBudgetLimit * 100).toInt()
                                Text(
                                    "$ratioInTotal%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "${formatMoney(item.third)} / ${formatMoney(item.second)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (percentUsed > 1f) Color.Red else Color.Gray
                            )

                            Spacer(Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { percentUsed.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = if (percentUsed > 1f) Color.Red else chartColors[i % chartColors.size],
                                trackColor = chartColors[i % chartColors.size].copy(0.1f),
                                strokeCap = StrokeCap.Round
                            )
                        }

                        Box {
                            IconButton(onClick = { showItemMenu = true }) {
                                Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                            }
                            DropdownMenu(
                                expanded = showItemMenu,
                                onDismissRequest = { showItemMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Edit Limit") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Edit,
                                            null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        showItemMenu = false
                                        selectedCategoryToEdit = item.first
                                        amount = item.second.toString()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Remove", color = Color.Red) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            null,
                                            tint = Color.Red,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        showItemMenu = false
                                        budgetList = budgetList.filter { it.first != item.first }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    //dialogs
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            DatePicker(state = rememberDatePickerState())
        }
    }

    if (showDialog) {
        var expanded by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.set_budget)) },
            containerColor = Color.White,
            text = {
                Column {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.category)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }) {
                            categories.forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = { category = it; expanded = false })
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.all { c -> c.isDigit() }) amount = it },
                        label = { Text(stringResource(R.string.enter_amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val value = amount.toIntOrNull() ?: 0
                    if (category.isNotEmpty()) {
                        val existingItem = budgetList.find { it.first == category }
                        val spent = existingItem?.third ?: 0

                        budgetList = budgetList.filterNot { it.first == category } + Triple(
                            category,
                            value,
                            spent
                        )
                    }
                    showDialog = false
                }) { Text(stringResource(R.string.Save)) }
            }
        )
    }

    if (showEditTotalDialog) {
        var value by remember { mutableStateOf(totalBudgetLimit.toString()) }
        AlertDialog(
            onDismissRequest = { showEditTotalDialog = false },
            title = { Text(stringResource(R.string.edit_total_budget)) },
            containerColor = Color.White,
            text = {
                OutlinedTextField(
                    value = value,
                    onValueChange = { if (it.all { c -> c.isDigit() }) value = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    totalBudgetLimit = value.toIntOrNull() ?: 0
                    showEditTotalDialog = false
                }) { Text(stringResource(R.string.Save)) }
            }
        )
    }

    if (selectedCategoryToEdit != null) {
        AlertDialog(
            onDismissRequest = { selectedCategoryToEdit = null },
            title = { Text("Edit $selectedCategoryToEdit Limit") },
            containerColor = Color.White,
            text = {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amount = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val value = amount.toIntOrNull() ?: 0
                    budgetList = budgetList.map {
                        if (it.first == selectedCategoryToEdit) Triple(
                            it.first,
                            value,
                            it.third
                        ) else it
                    }
                    selectedCategoryToEdit = null
                }) { Text(stringResource(R.string.Save)) }
            }
        )
    }
}

fun formatMoney(value: Int): String = DecimalFormat("#,###").format(value) + " đ"

