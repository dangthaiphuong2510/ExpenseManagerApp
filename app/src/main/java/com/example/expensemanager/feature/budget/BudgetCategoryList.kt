package com.example.expensemanager.feature.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.designsystem.theme.ExpenseRed
import com.example.expensemanager.utils.format.formatWithLocalCurrency

@Composable
fun BudgetCategoryList(
    modifier: Modifier = Modifier,
    budgetList: List<BudgetItem>,
    chartColors: List<Color>,
    currencyCode: String,
    onItemClick: (BudgetItem) -> Unit
) {

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        itemsIndexed(budgetList) { index, item ->
            val isOverBudget = item.spent > item.limit && item.limit > 0
            val actualProgress = if (item.limit == 0.0) 0f else (item.spent / item.limit).toFloat()

            val dotColor = chartColors[index % chartColors.size]
            val alertColor = ExpenseRed
            val statusColor = if (isOverBudget) alertColor else dotColor

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) }
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .background(dotColor, CircleShape)
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            Arrangement.SpaceBetween,
                            Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.category,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            val displayPercent =
                                if (isOverBudget) 100 else (actualProgress * 100).toInt()
                            Text(
                                text = "$displayPercent%",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isOverBudget) alertColor else Color.Gray,
                                fontWeight = if (isOverBudget) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "${item.spent.formatWithLocalCurrency(currencyCode)} / ${
                                item.limit.formatWithLocalCurrency(currencyCode)
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOverBudget) alertColor else Color.Gray
                        )

                        Spacer(Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { actualProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = statusColor,
                            trackColor = statusColor.copy(alpha = 0.1f),
                            strokeCap = StrokeCap.Round
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    IconButton(onClick = { onItemClick(item) }) {
                        AppIcons.MyIcon(
                            AppIcons.Edit,
                            size = 20.dp,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}