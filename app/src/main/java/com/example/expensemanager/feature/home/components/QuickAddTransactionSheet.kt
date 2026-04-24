package com.example.expensemanager.feature.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensemanager.R
import com.example.expensemanager.feature.category.CategoryGrid
import com.example.expensemanager.feature.category.CategoryItem
import com.example.expensemanager.feature.category.CategoryTabRow
import com.example.expensemanager.feature.category.categorydialogui.CategoryTransactionFormUI

@Composable
fun QuickAddTransactionSheet(
    categories: List<CategoryItem>,
    categoryTotals: Map<String, Double>,
    currencyCode: String,
    onConfirm: (amount: Double, note: String, date: Long, category: String, isExpense: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategoryName by remember { mutableStateOf<String?>(null) }

    val displayList = categories.filter {
        if (selectedTab == 0) it.isExpense else !it.isExpense
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .padding(horizontal = 160.dp)
                .padding(bottom = 8.dp)
        )

        Text(
            text = stringResource(R.string.add_transaction),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        CategoryTabRow(
            selectedTab = selectedTab,
            onTabSelected = {
                selectedTab = it
                selectedCategoryName = null
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            CategoryGrid(
                displayList = displayList,
                categoryTotals = categoryTotals,
                currencyCode = currencyCode,
                modifier = Modifier.fillMaxSize(),
                columns = 3,
                cardColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                showAddCard = false,
                onItemClick = { name ->
                    selectedCategoryName = name
                },
                onItemLongClick = { },
                onAddCategoryClick = { }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (selectedCategoryName != null) {
        CategoryTransactionFormUI(
            initialTransaction = null,
            categoryName = selectedCategoryName!!,
            currencyCode = currencyCode,
            isExpense = selectedTab == 0,
            noteError = null,
            onDismiss = { selectedCategoryName = null },
            onConfirm = { amount, note, date ->
                onConfirm(amount, note, date, selectedCategoryName!!, selectedTab == 0)
                selectedCategoryName = null
                onDismiss()
            },
            onDelete = null
        )
    }
}