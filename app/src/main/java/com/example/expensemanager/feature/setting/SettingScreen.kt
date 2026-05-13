package com.example.expensemanager.feature.setting

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanager.R
import com.example.expensemanager.data.local.entity.TransactionEntity
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.feature.home.HomeViewModel
import com.example.expensemanager.utils.format.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    isOnline: Boolean = true,
    onLogout: () -> Unit,
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    onNavigateToCurrencySelection: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val homeState by viewModel.homeState.collectAsStateWithLifecycle()

    var showThemeDialog by remember { mutableStateOf(false) }
    var isNotificationEnabled by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            val transactionList = homeState.allTransactions

            if (transactionList.isEmpty()) {
                Toast.makeText(context, "No data to export!", Toast.LENGTH_SHORT).show()
                return@let
            }

            val csvData = generateCsvFromTransactions(transactionList)

            saveCsvToFile(context, it, csvData)
            openFile(context, it)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.settings), fontWeight = FontWeight.ExtraBold)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SettingHeader(stringResource(R.string.general)) }
            item {
                SettingCard {
                    Column {
                        SettingItem(
                            iconRes = AppIcons.ThemeMode,
                            title = stringResource(R.string.dark_mode),
                            trailing = {
                                Text(text = currentTheme, color = MaterialTheme.colorScheme.primary)
                            },
                            onClick = { showThemeDialog = true }
                        )
                        SettingDivider()

                        SettingItem(
                            iconRes = AppIcons.NavBudget,
                            title = "Currency Unit",
                            trailing = {
                                Text(
                                    text = homeState.currencyCode,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = onNavigateToCurrencySelection
                        )
                        SettingDivider()

                        SettingItem(
                            iconRes = AppIcons.Notifications,
                            title = stringResource(R.string.notifications),
                            trailing = {
                                Switch(
                                    checked = isNotificationEnabled,
                                    onCheckedChange = { isNotificationEnabled = it })
                            }
                        )
                    }
                }
            }

            item { SettingHeader(stringResource(R.string.data_management)) }
            item {
                SettingCard {
                    Column {
                        SettingItem(
                            iconRes = AppIcons.Download,
                            title = "Export Data (CSV)",
                            onClick = {
                                if (isOnline) {
                                    val fileName =
                                        "Expense_Report_${System.currentTimeMillis()}.csv"
                                    exportCsvLauncher.launch(fileName)
                                }
                            }
                        )
                        SettingDivider()
                        SettingItem(
                            iconRes = AppIcons.Delete,
                            title = stringResource(R.string.clear_all_data),
                            titleColor = if (isOnline) Color.Red else Color.Gray,
                            onClick = { if (isOnline) showDeleteDialog = true }
                        )
                    }
                }
            }

            item { SettingHeader(stringResource(R.string.about)) }
            item {
                SettingCard {
                    Column {
                        SettingItem(
                            iconRes = AppIcons.Info,
                            title = stringResource(R.string.version),
                            trailing = { Text("1.0.0", color = Color.Gray) }
                        )
                        SettingDivider()
                        SettingItem(
                            iconRes = AppIcons.Rate,
                            title = stringResource(R.string.rate_app),
                            onClick = { }
                        )
                    }
                }
            }

            item { SettingHeader(stringResource(R.string.account)) }
            item {
                SettingCard {
                    SettingItem(
                        iconRes = AppIcons.Logout,
                        title = stringResource(R.string.logout),
                        titleColor = if (isOnline) Color.Red else Color.Gray,
                        onClick = { if (isOnline) onLogout() },
                        trailing = {
                            if (!isOnline) Text("Offline", color = Color.Gray, fontSize = 12.sp)
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onDismiss = { showThemeDialog = false },
            onSelect = { theme ->
                onThemeChange(theme)
                showThemeDialog = false
            }
        )
    }

    if (showDeleteDialog && isOnline) {
        DeleteConfirmDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.clearAllData {
                    Toast.makeText(context, "All data has been cleared", Toast.LENGTH_SHORT).show()
                    showDeleteDialog = false
                }
            }
        )
    }
}

fun generateCsvFromTransactions(transactions: List<TransactionEntity>): String {
    val sb = StringBuilder()
    sb.append("Date,Category,Amount,Note,Type\n")

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    transactions.forEach { item ->
        val dateStr = dateFormat.format(Date(item.date))
        val noteSafe = item.description ?: ""
        val cleanNote = noteSafe.replace(",", ";")
        val cleanCategory = item.category.replace(",", ";")
        val typeStr = item.type

        sb.append("$dateStr,$cleanCategory,${item.amount},$cleanNote,$typeStr\n")
    }

    return sb.toString()
}

fun saveCsvToFile(context: Context, uri: Uri, csvData: String) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            outputStream.write(csvData.toByteArray(Charsets.UTF_8))
            outputStream.flush()
        }
        Toast.makeText(context, "File saved successfully!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun openFile(context: Context, uri: Uri) {
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "text/csv")
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(android.content.Intent.createChooser(intent, "Open with..."))
    } catch (e: Exception) {
        Toast.makeText(context, "No app found to open CSV.", Toast.LENGTH_SHORT).show()
    }
}