package com.example.basecomposemvvm.feature.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.basecomposemvvm.R
import com.example.basecomposemvvm.designsystem.theme.AppTheme
import com.example.basecomposemvvm.utils.formatCurrency
import java.time.Instant
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen() {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val formatter = DateTimeFormatter.ofPattern("MM / yyyy")
    var showDatePicker by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf<Pair<String, Color>?>(null) }

    val colorScheme = MaterialTheme.colorScheme

    val categories = listOf("Food", "Transport", "Shopping", "Entertainment", "Bill")
    val chartColors = listOf(
        colorScheme.primary,
        colorScheme.secondary,
        Color(0xFFFFB74D),
        Color(0xFF81C784),
        Color(0xFFBA68C8)
    )

    val categoryData = remember(currentMonth) {
        categories.map { it to Random.nextInt(500000, 2000000).toDouble() }
    }

    if (selectedCategory != null) {
        ReportDetailScreen(
            categoryName = selectedCategory!!.first,
            categoryColor = selectedCategory!!.second,
            onBack = { selectedCategory = null }
        )
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showDatePicker = true }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                                Icon(
                                    Icons.Rounded.ChevronLeft,
                                    null,
                                    tint = colorScheme.onBackground
                                )
                            }
                            Text(
                                text = currentMonth.format(formatter),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = colorScheme.onBackground
                            )
                            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                                Icon(
                                    Icons.Rounded.ChevronRight,
                                    null,
                                    tint = colorScheme.onBackground
                                )
                            }
                        }
                    },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = colorScheme.background
                    )
                )
            },
            containerColor = colorScheme.background
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),

                ) {
                item {
                    Text(
                        stringResource(R.string.report_overview),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorScheme.onBackground,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        SimplePieChart(data = categoryData, chartColors = chartColors)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                itemsIndexed(categoryData) { index, pair ->
                    val total = categoryData.sumOf { it.second }
                    val percentage = if (total > 0) (pair.second / total * 100).toInt() else 0
                    val color = chartColors.getOrElse(index) { Color.Gray }

                    CategoryListCard(
                        name = pair.first,
                        amount = pair.second,
                        percentage = percentage,
                        color = color,
                        onClick = { selectedCategory = pair.first to color }
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    //datepicker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentMonth.atDay(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        currentMonth = YearMonth.of(date.year, date.month)
                    }
                    showDatePicker = false
                }) {
                    Text(
                        stringResource(R.string.action_ok),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun CategoryListCard(
    name: String,
    amount: Double,
    percentage: Int,
    color: Color,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Column(horizontalAlignment = Alignment.End) {
                Text(text = formatCurrency(amount.toLong()), fontWeight = FontWeight.Bold)
                Text(text = "$percentage%", fontSize = 12.sp, color = colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun SimplePieChart(data: List<Pair<String, Double>>, chartColors: List<Color>) {
    val total = data.sumOf { it.second }
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(200.dp)) {
                var startAngle = -90f
                data.forEachIndexed { index, pair ->
                    val sweep = (pair.second.toFloat() / total.toFloat()) * 360f
                    drawArc(
                        color = chartColors.getOrElse(index) { Color.Gray },
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true
                    )
                    startAngle += sweep
                }
            }

            Canvas(modifier = Modifier.size(140.dp)) {
                drawCircle(color = colorScheme.surface)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.expense_income_total_label),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = formatCurrency(total.toLong()),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.onSurface
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    AppTheme {
        ReportScreen()
    }
}