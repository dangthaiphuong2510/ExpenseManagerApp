package com.example.expensemanager.feature.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanager.designsystem.theme.AppIcons
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTopBar(
    selectedMonth: YearMonth,
    onMonthChange: (Int) -> Unit,
    onTitleClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MM / yyyy")

    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                IconButton(onClick = { onMonthChange(-1) }) {
                    AppIcons.MyIcon(
                        AppIcons.ChevronLeft,
                        size = 16.dp
                    )
                }

                Text(
                    text = selectedMonth.format(formatter),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTitleClick() }
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )

                IconButton(onClick = { onMonthChange(1) }) {
                    AppIcons.MyIcon(
                        AppIcons.ChevronRight,
                        size = 16.dp
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}