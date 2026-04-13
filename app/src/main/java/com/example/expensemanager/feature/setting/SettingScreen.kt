package com.example.expensemanager.feature.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanager.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensemanager.designsystem.theme.AppIcons
import com.example.expensemanager.feature.authentication.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    isOnline: Boolean = true,
    onLogout: () -> Unit,
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
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
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = { showThemeDialog = true }
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
                            onClick = { if (isOnline) { /* Logic export */ } }
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
                                Text(
                                    "1.0.0",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
                                )
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

            // Khoảng trống để cuộn ẩn dưới Navigation Bar
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    // Dialog chọn Theme
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onDismiss = { showThemeDialog = false },
            onSelect = { theme ->
                onThemeChange(theme) // Gọi hàm đổi theme của hệ thống
                showThemeDialog = false
            }
        )
    }

    if (showDeleteDialog && isOnline) {
        DeleteConfirmDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = { /* Logic clear data */ }
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val themes = listOf("Light", "Dark", "System")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_theme), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                themes.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (theme == currentTheme),
                                onClick = { onSelect(theme) }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = { onSelect(theme) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = theme, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

// Các Composable phụ trợ (SettingItem, SettingHeader, v.v.) giữ nguyên như code cũ của bạn
@Composable
fun SettingItem(
    iconRes: Int,
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcons.MyIcon(
            resourceId = iconRes,
            tint = if (titleColor == Color.Red) Color.Red else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = titleColor
        )
        trailing?.invoke() ?: AppIcons.MyIcon(
            resourceId = AppIcons.ChevronRight,
            tint = Color.Gray.copy(alpha = 0.5f),
            size = 18.dp
        )
    }
}

@Composable
fun SettingHeader(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, start = 8.dp)
    )
}

@Composable
fun SettingCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        content()
    }
}

@Composable
fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun DeleteConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.confirm_delete_title)) },
        text = { Text(stringResource(R.string.confirm_delete_message)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(stringResource(R.string.action_ok), color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}