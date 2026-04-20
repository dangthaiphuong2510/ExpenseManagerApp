package com.example.expensemanager.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanager.R
import com.example.expensemanager.designsystem.theme.ExpenseRed
import com.example.expensemanager.designsystem.theme.IncomeGreen
import com.example.expensemanager.utils.format.formatWithLocalCurrency

@Composable
fun TotalBalanceCard(balance: Double, income: Double, expense: Double, isLoading: Boolean) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = stringResource(id = R.string.total_balance),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isLoading) "---" else balance.formatWithLocalCurrency(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BalanceInfo(
                    label = stringResource(R.string.income),
                    amount = if (isLoading) "---" else income.formatWithLocalCurrency(),
                    color = IncomeGreen
                )
                BalanceInfo(
                    label = stringResource(R.string.expense),
                    amount = if (isLoading) "---" else expense.formatWithLocalCurrency(),
                    color = ExpenseRed,
                    isEnd = true
                )
            }
        }
    }
}

@Composable
private fun BalanceInfo(label: String, amount: String, color: Color, isEnd: Boolean = false) {
    Column(horizontalAlignment = if (isEnd) Alignment.End else Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color))
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(amount, fontWeight = FontWeight.Bold, color = color, fontSize = 16.sp)
    }
}