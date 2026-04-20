package com.example.expensemanager.feature.setting

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanager.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.feature.home.HomeViewModel

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
            // General Section
            item { SettingHeader(stringResource(R.string.general)) }
            item {
                SettingCard {
                    Column {
                        SettingItem(
                            iconRes = AppIcons.ThemeMode,
                            title = stringResource(R.string.dark_mode),
                            trailing = {
                                Text(
                                    text = currentTheme,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = { showThemeDialog = true }
                        )
                        SettingDivider()

                        SettingItem(
                            iconRes = AppIcons.NavBudget,
                            title = "Currency Unit",
                            trailing = {
                                Text(
                                    text = homeState.currencySymbol,
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

            // Data Management Section
            item { SettingHeader(stringResource(R.string.data_management)) }
            item {
                SettingCard {
                    Column {
                        SettingItem(
                            iconRes = AppIcons.Download,
                            title = "Export Data (CSV/PDF)",
                            onClick = { if (isOnline) { /* Logic Export */ } }
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

            // About Section
            item { SettingHeader(stringResource(R.string.about)) }
            item {
                SettingCard {
                    Column {
                        SettingItem(
                            iconRes = AppIcons.Info,
                            title = stringResource(R.string.version),
                            trailing = {
                                Text("1.0.0", color = Color.Gray)
                            }
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

            // Account Section
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

    // Logic xử lý xóa toàn bộ dữ liệu
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