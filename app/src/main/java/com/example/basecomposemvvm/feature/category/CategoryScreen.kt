package com.example.basecomposemvvm.feature.category

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.basecomposemvvm.R
import com.example.basecomposemvvm.designsystem.theme.AppIcons
import com.example.basecomposemvvm.designsystem.theme.ExpenseRed
import com.example.basecomposemvvm.designsystem.theme.IncomeGreen
import com.example.basecomposemvvm.utils.formatCurrency
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryScreen(
    isOnline : Boolean = true,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val formatter = DateTimeFormatter.ofPattern("MM/yyyy")
    val displayText = currentMonth.format(formatter)

    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog States
    var showTransactionDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showManageDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryItem?>(null) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }

    var selectedCategoryName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val displayList = uiState.categories.filter {
        if (selectedTab == 0) it.isExpense else !it.isExpense
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCategoryDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = { showManageDialog = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Manage",
                            tint = Color.Gray
                        )
                    }

                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            stringResource(R.string.total_balance),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "${formatCurrency(uiState.totalBalance)} ",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2D3436)
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    stringResource(R.string.income),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    "${formatCurrency(uiState.totalIncome)} ",
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    stringResource(R.string.expense),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    "${formatCurrency(uiState.totalExpense)} ",
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            //Month Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(AppIcons.ChevronLeft, null, tint = Color.Gray)
                }
                Text(
                    displayText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showDatePicker = true }
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(AppIcons.ChevronRight, null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            //TabRow
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.expense), fontWeight = FontWeight.Bold) })
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.income), fontWeight = FontWeight.Bold) })
            }

            Spacer(modifier = Modifier.height(16.dp))

            //Category Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(displayList) { item ->
                    val amountForCategory = uiState.categoryTotals[item.name] ?: 0.0
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(1.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                selectedCategoryName = item.name
                                showTransactionDialog = true
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = AppIcons.getIconByName(item.iconName),
                                contentDescription = item.name,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                item.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${formatCurrency(amountForCategory)} ",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (amountForCategory > 0) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }


    if (showManageDialog) {
        AlertDialog(
            onDismissRequest = { showManageDialog = false },
            title = { Text("Manage Categories") },
            containerColor = Color.White,
            text = {
                Box(modifier = Modifier.height(300.dp)) {
                    LazyColumn {
                        items(displayList) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        AppIcons.getIconByName(item.iconName),
                                        null,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(item.name, fontWeight = FontWeight.Bold)
                                }
                                Row {
                                    IconButton(onClick = { categoryToEdit = item }) {
                                        Icon(Icons.Default.Edit, null, tint = Color.Gray)
                                    }
                                    IconButton(onClick = { categoryToDelete = item.name }) {
                                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                                    }
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showManageDialog = false }) { Text("Close") } }
        )
    }

    categoryToEdit?.let { item ->
        CategoryFormDialog(
            title = "Edit Category",
            initialName = item.name,
            initialIcon = item.iconName,
            onDismiss = { categoryToEdit = null },
            onConfirm = { newName, newIcon ->
                viewModel.updateCategory(item.name, newName, newIcon, item.isExpense)
                categoryToEdit = null
            }
        )
    }

    categoryToDelete?.let { name ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete Category?") },
            text = { Text("Are you sure you want to delete '$name'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(name)
                    categoryToDelete = null
                }) { Text(stringResource(R.string.delete), color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text(
                        stringResource(R.string.action_cancel)
                    )
                }
            }
        )
    }

    if (showAddCategoryDialog) {
        CategoryFormDialog(
            title = "New Category",
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, icon ->
                viewModel.addNewCategory(name, icon, selectedTab == 0)
                showAddCategoryDialog = false
            }
        )
    }

    if (showTransactionDialog) {
        AlertDialog(
            onDismissRequest = { showTransactionDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    if (selectedTab == 0) stringResource(R.string.add_expense) else stringResource(R.string.add_income),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Category: $selectedCategoryName", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.all { c -> c.isDigit() }) amount = it },
                        label = { Text(stringResource(R.string.enter_amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(stringResource(R.string.note)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        if (amountValue > 0) {
                            viewModel.addTransaction(
                                amountValue,
                                selectedCategoryName,
                                note,
                                selectedTab == 0
                            )
                        }
                        showTransactionDialog = false
                        amount = ""; note = ""
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text(stringResource(R.string.Save)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTransactionDialog = false
                }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        currentMonth = YearMonth.of(selectedDate.year, selectedDate.month)
                    }
                }) { Text(stringResource(R.string.action_ok), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun CategoryFormDialog(
    title: String,
    initialName: String = "",
    initialIcon: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedIconName by remember { mutableStateOf(if (initialIcon.isEmpty()) AppIcons.CategoryIconsList[0].first else initialIcon) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = Color.White) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Icon(
                    AppIcons.getIconByName(selectedIconName),
                    null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(AppIcons.CategoryIconsList) { pair ->
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedIconName == pair.first) MaterialTheme.colorScheme.primary.copy(
                                        0.2f
                                    ) else Color.Transparent
                                )
                                .clickable { selectedIconName = pair.first },
                            contentAlignment = Alignment.Center
                        ) { Icon(pair.second, null, modifier = Modifier.size(24.dp)) }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
                    Button(
                        onClick = { if (name.isNotBlank()) onConfirm(name, selectedIconName) },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(stringResource(R.string.create)) }
                }
            }
        }
    }
}