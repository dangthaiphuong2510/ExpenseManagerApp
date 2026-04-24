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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.expensemanager.R
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.designsystem.theme.ExpenseRed
import com.example.expensemanager.designsystem.theme.IncomeGreen
import com.example.expensemanager.utils.format.formatWithLocalCurrency

@Composable
fun CategoryBalanceCard(
    totalBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    currencyCode: String,
    onManageClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    text = totalBalance.formatWithLocalCurrency(currencyCode),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CategoryMiniStat(
                        stringResource(R.string.income),
                        totalIncome,
                        currencyCode,
                        IncomeGreen
                    )
                    CategoryMiniStat(
                        stringResource(R.string.expense),
                        totalExpense,
                        currencyCode,
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
    currencyCode: String,
    color: Color,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(horizontalAlignment = alignment) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(
            text = amount.formatWithLocalCurrency(currencyCode),
            color = color,
            fontWeight = FontWeight.Bold
        )
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
            if (selectedTab < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
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
    currencyCode: String,
    modifier: Modifier = Modifier,
    columns: Int = 3,
    cardColor: Color? = null,
    showAddCard: Boolean = true,
    onItemClick: (String) -> Unit,
    onItemLongClick: (CategoryItem) -> Unit,
    onAddCategoryClick: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(displayList, key = { it.name }) { item ->
            val amount = categoryTotals[item.name] ?: 0.0
            CategoryCard(
                name = item.name,
                iconName = item.iconName,
                amount = amount,
                currencyCode = currencyCode,
                cardColor = cardColor,
                onClick = { onItemClick(item.name) },
                onLongClick = { onItemLongClick(item) }
            )
        }

        if (showAddCard) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onAddCategoryClick() }
                ) {
                    Column(
                        Modifier.fillMaxSize(),
                        Arrangement.Center,
                        Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = stringResource(R.string.add_new_category),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryCard(
    name: String,
    iconName: String,
    amount: Double,
    currencyCode: String,
    cardColor: Color?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor ?: MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(8.dp),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {
            AppIcons.MyIcon(
                resourceId = AppIcons.getIconIdByName(iconName),
                tint = MaterialTheme.colorScheme.primary,
                size = 28.dp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            if (amount != 0.0) {
                Text(
                    text = amount.formatWithLocalCurrency(currencyCode),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}