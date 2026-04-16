package com.example.expensemanager.feature.category

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensemanager.R
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.designsystem.theme.ExpenseRed
import com.example.expensemanager.designsystem.theme.IncomeGreen
import com.example.expensemanager.utils.format.formatCurrency

@Composable
fun CategoryBalanceCard(
    totalBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    onManageClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onManageClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                AppIcons.MyIcon(
                    AppIcons.SettingSliders,
                    size = 20.dp,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    stringResource(R.string.total_balance),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    formatCurrency(totalBalance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CategoryMiniStat(stringResource(R.string.income), totalIncome, IncomeGreen)
                    CategoryMiniStat(
                        stringResource(R.string.expense),
                        totalExpense,
                        ExpenseRed,
                        Alignment.End
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryMiniStat(
    label: String,
    amount: Double,
    color: Color,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(horizontalAlignment = alignment) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(formatCurrency(amount), color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CategoryMonthSelector(
    monthYearText: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCalendarClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            AppIcons.MyIcon(AppIcons.ChevronLeft, size = 16.dp)
        }
        Text(
            text = monthYearText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onCalendarClick() }
        )
        IconButton(onClick = onNextMonth) {
            AppIcons.MyIcon(AppIcons.ChevronRight, size = 16.dp)
        }
    }
}

@Composable
fun CategoryTabRow(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent,
        divider = {},
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            text = { Text(stringResource(R.string.expense), fontWeight = FontWeight.Bold) }
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            text = { Text(stringResource(R.string.income), fontWeight = FontWeight.Bold) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryGrid(
    displayList: List<CategoryItem>,
    categoryTotals: Map<String, Double>,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit,
    onItemLongClick: (CategoryItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(displayList) { item ->
            val amount = categoryTotals[item.name] ?: 0.0

            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .aspectRatio(1f)
                    .combinedClickable(
                        onClick = { onItemClick(item.name) },
                        onLongClick = { onItemLongClick(item) }
                    )
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    Arrangement.Center,
                    Alignment.CenterHorizontally
                ) {
                    AppIcons.MyIcon(
                        resourceId = AppIcons.getIconIdByName(item.iconName),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatCurrency(amount),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (amount > 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}