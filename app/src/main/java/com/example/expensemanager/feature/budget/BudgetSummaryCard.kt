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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.expensemanager.R
import com.example.expensemanager.designsystem.theme.ExpenseRed
import java.text.DecimalFormat

@Composable
fun BudgetSummaryCard(
    totalLimit: Double,
    totalSpent: Double,
    budgetList: List<BudgetItem>,
    chartColors: List<Color>
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        budgetList.forEachIndexed { index, item ->
                            if (item.limit > 0) {
                                val sweep = (item.limit / totalLimit).toFloat() * 360f
                                drawArc(
                                    color = chartColors[index % chartColors.size],
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = 25f, cap = StrokeCap.Round)
                                )
                                startAngle += sweep
                            }
                        }
                    } else {
                        drawArc(Color.LightGray, -90f, 360f, false, style = Stroke(width = 25f))
                    }
                }
                val percent = if (totalLimit == 0.0) 0 else (totalSpent * 100 / totalLimit).toInt()
                Text(
                    "$percent%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    stringResource(R.string.remaining),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                val remaining = totalLimit - totalSpent
                Text(
                    DecimalFormat("#,###").format(remaining.toInt()) + " đ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = if (remaining < 0) ExpenseRed else MaterialTheme.colorScheme.primary
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                Text(
                    "Total Limit: ${DecimalFormat("#,###").format(totalLimit.toInt())} đ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}
