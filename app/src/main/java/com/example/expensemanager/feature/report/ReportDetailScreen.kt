package com.example.expensemanager.feature.report

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanager.R
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.utils.format.formatCurrency
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    categoryName: String,
    categoryColor: Color,
    historyData: List<Pair<YearMonth, Double>>,
    selectedMonth: YearMonth,
    onBack: () -> Unit
) {
    val maxAmount = remember(historyData) {
        val max = historyData.maxOfOrNull { it.second } ?: 0.0
        if (max <= 0) 1.0 else max
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(categoryName, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        AppIcons.MyIcon(AppIcons.ChevronLeft, size = 16.dp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.recent_spending_fluctuations),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        historyData.forEach { (month, value) ->
                            val isSelected = month == selectedMonth
                            val targetHeight = ((value / maxAmount) * 140).dp
                            val animatedHeight by animateDpAsState(targetValue = targetHeight, label = "barHeight")

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (value > 0) {
                                    Text(
                                        text = formatCurrency(value),
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) categoryColor else Color.Gray,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                } else {
                                    Text("", fontSize = 7.sp)
                                }

                                Spacer(Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height(animatedHeight)
                                        .background(
                                            color = if (isSelected) categoryColor
                                            else categoryColor.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                        )
                                )

                                Spacer(Modifier.height(8.dp))

                                // 5. Tên tháng
                                Text(
                                    text = month.month.getDisplayName(
                                        TextStyle.SHORT,
                                        Locale.getDefault()
                                    ),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                    else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}