package com.example.expensemanager.feature.budget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanager.R
import com.example.expensemanager.designsystem.theme.ExpenseRed
import com.example.expensemanager.utils.format.formatWithLocalCurrency
import kotlin.math.abs

@Composable
fun BudgetSummaryCard(
    totalLimit: Double,
    totalSpent: Double,
    budgetList: List<BudgetItem>,
    currencyCode: String,
    chartColors: List<Color>
) {
    val isOverBudget = totalSpent > totalLimit && totalLimit > 0
    val remaining = totalLimit - totalSpent

    val percent = if (totalLimit <= 0) 0 else (totalSpent * 100 / totalLimit).toInt()
    val displayPercent = if (percent > 100) 100 else percent

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(90.dp)) {
                    var startAngle = -90f

                    if (totalLimit > 0) {
                        if (isOverBudget) {
                            drawArc(
                                color = ExpenseRed.copy(alpha = 0.15f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 20f)
                            )
                        }

                        budgetList.forEachIndexed { index, item ->
                            if (item.limit > 0) {
                                val sweep = (item.limit / totalLimit).toFloat() * 360f

                                val color = if (item.isOverBudget) ExpenseRed else chartColors[index % chartColors.size]

                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = 25f, cap = StrokeCap.Round)
                                )
                                startAngle += sweep
                            }
                        }
                    } else {
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 20f)
                        )
                    }
                }

                Text(
                    text = "$displayPercent%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isOverBudget) ExpenseRed else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOverBudget) "Exceeded" else stringResource(R.string.remaining),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isOverBudget) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isOverBudget) FontWeight.Bold else FontWeight.Medium
                )

                Text(
                    text = abs(remaining).formatWithLocalCurrency(currencyCode),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = if (isOverBudget) ExpenseRed else MaterialTheme.colorScheme.primary,
                    lineHeight = 28.sp
                )

                HorizontalDivider(
                    Modifier.padding(vertical = 10.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.2f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.total_limit) + ": ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = totalLimit.formatWithLocalCurrency(currencyCode),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}