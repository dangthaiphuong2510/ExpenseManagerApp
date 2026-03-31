package com.example.basecomposemvvm.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.basecomposemvvm.designsystem.theme.AppTheme
import com.example.basecomposemvvm.utils.formatCurrency
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    categoryName: String,
    categoryColor: Color,
    onBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val months = listOf("Oct", "Nov", "Dec", "Feb", "Jan", "Current")
    val mockData = remember {
        months.map { it to Random.nextDouble(500000.0, 3000000.0) }
    }
    val maxAmount = mockData.maxOf { it.second }.toFloat()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(categoryName, fontWeight = FontWeight.ExtraBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ChevronLeft, "Back")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        containerColor = colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Recent spending fluctuations",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        mockData.forEachIndexed { index, item ->
                            val amount = item.second
                            val month = item.first

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = formatCurrency(
                                        amount.toString().toDoubleOrNull() ?: 0.0
                                    ),
                                    fontSize = 9.sp,
                                    color = colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .height(((amount.toFloat() / maxAmount) * 160).dp)
                                        .background(
                                            color = if (index == 5) categoryColor else categoryColor.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                        )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = month,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Recent spending",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ReportDetailScreenPreview() {
    AppTheme {
        ReportDetailScreen(
            categoryName = "Food",
            categoryColor = Color.Red,
            onBack = {}
        )
    }
}
