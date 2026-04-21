package com.example.expensemanager.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanager.data.local.entity.TransactionEntity
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.designsystem.theme.ExpenseRed
import com.example.expensemanager.designsystem.theme.IncomeGreen
import com.example.expensemanager.utils.format.formatWithLocalCurrency

@Composable
fun TransactionItem(item: TransactionEntity, formattedDate: String) {
    val isIncome = item.type.equals("INCOME", ignoreCase = true)
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isIncome) IncomeGreen.copy(0.1f) else ExpenseRed.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                AppIcons.MyIcon(
                    resourceId = AppIcons.getIconIdByName(item.categoryIcon ?: item.category),
                    tint = if (isIncome) IncomeGreen else ExpenseRed,
                    size = 24.dp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.category, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(text = formattedDate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(
                text = (if (isIncome) "+" else "-") + item.amount.formatWithLocalCurrency(),
                fontWeight = FontWeight.Black,
                color = if (isIncome) IncomeGreen else ExpenseRed,
                fontSize = 16.sp
            )
        }
    }
}