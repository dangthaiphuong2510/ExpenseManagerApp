package com.example.expensemanager.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanager.R
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.utils.format.formatWithLocalCurrency

@Composable
fun ReportScreen(viewModel: ReportViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currencyCode = uiState.currencyCode

    var showDatePicker by remember { mutableStateOf(false) }

    var selectedCategoryName by remember { mutableStateOf<String?>(null) }
    var selectedCategoryColor by remember { mutableStateOf(Color.Gray) }

    val chartColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        Color(0xFFFFB74D),
        Color(0xFF81C784),
        Color(0xFFBA68C8)
    )

    if (selectedCategoryName != null) {
        val historyData = uiState.categoryHistory[selectedCategoryName!!] ?: emptyList()

        ReportDetailScreen(
            categoryName = selectedCategoryName!!,
            categoryColor = selectedCategoryColor,
            historyData = historyData,
            selectedMonth = uiState.selectedMonth,
            currencyCode = currencyCode,
            onBack = { selectedCategoryName = null }
        )
    } else {
        Scaffold(
            topBar = {
                ReportTopBar(
                    selectedMonth = uiState.selectedMonth,
                    onMonthChange = viewModel::changeMonth,
                    onTitleClick = { showDatePicker = true }
                )
            }
        ) { innerPadding ->
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.categoryData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_transactions_found),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.report_overview),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        Card(
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            SimplePieChart(
                                data = uiState.categoryData,
                                chartColors = chartColors,
                                totalAmount = uiState.totalAmount,
                                currencyCode = currencyCode
                            )
                        }
                    }
                    itemsIndexed(uiState.categoryData) { index, pair ->
                        val color = chartColors[index % chartColors.size]
                        val name = pair.first
                        val amount = pair.second
                        val percentage =
                            if (uiState.totalAmount > 0) (amount / uiState.totalAmount * 100).toInt() else 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategoryName = name
                                    selectedCategoryColor = color
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(color.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AppIcons.MyIcon(
                                        resourceId = AppIcons.getIconIdByName(name),
                                        tint = color,
                                        size = 20.dp
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "$percentage%",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Text(
                                    text = amount.formatWithLocalCurrency(currencyCode),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(Modifier.width(8.dp))
                                AppIcons.MyIcon(
                                    resourceId = AppIcons.ChevronRight,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    size = 14.dp
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showDatePicker) {
        ReportDatePickerDialog(
            selectedMonth = uiState.selectedMonth,
            onDismiss = { showDatePicker = false },
            onDateSelected = { year, month -> viewModel.setMonth(year, month) }
        )
    }
}