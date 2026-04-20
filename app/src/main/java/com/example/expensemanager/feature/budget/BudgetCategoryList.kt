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
import java.text.DecimalFormat

@Composable
fun BudgetCategoryList(
    modifier: Modifier = Modifier,
    budgetList: List<BudgetItem>,
    chartColors: List<Color>,
    onItemClick: (BudgetItem) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        itemsIndexed(budgetList) { index, item ->
            val progress = if (item.limit == 0.0) 0f else (item.spent / item.limit).toFloat()
            val dotColor = chartColors[index % chartColors.size]
            val progressColor = if (progress > 1f) ExpenseRed else dotColor

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) }
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier
                        .size(12.dp)
                        .background(dotColor, CircleShape))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text(item.category, fontWeight = FontWeight.Bold)
                            Text(
                                "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Text(
                            "${DecimalFormat("#,###").format(item.spent.toInt())} / ${
                                DecimalFormat(
                                    "#,###"
                                ).format(item.limit.toInt())
                            } đ",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (progress > 1f) ExpenseRed else Color.Gray
                        )
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .padding(top = 4.dp),
                            color = progressColor,
                            strokeCap = StrokeCap.Round
                        )
                    }
                    IconButton(onClick = { onItemClick(item) }) {
                        AppIcons.MyIcon(
                            AppIcons.Edit,
                            size = 18.dp,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}