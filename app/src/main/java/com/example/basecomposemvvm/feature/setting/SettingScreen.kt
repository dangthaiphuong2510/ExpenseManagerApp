package com.example.basecomposemvvm.feature.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.basecomposemvvm.R
import com.example.basecomposemvvm.designsystem.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen() {
    val colorScheme = MaterialTheme.colorScheme

    var isDarkMode by remember { mutableStateOf(false) }
    var isNotificationEnabled by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.settings), fontWeight = FontWeight.ExtraBold)
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        containerColor = colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //setting
            item {
                Text(
                    stringResource(R.string.general),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, start = 8.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column {
                        SettingItem(
                            icon = Icons.Rounded.DarkMode,
                            title = stringResource(R.string.dark_mode),
                            trailing = {
                                Switch(checked = isDarkMode, onCheckedChange = { isDarkMode = it })
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                        SettingItem(
                            icon = Icons.Rounded.Notifications,
                            title = stringResource(R.string.notifications),
                            trailing = {
                                Switch(checked = isNotificationEnabled, onCheckedChange = { isNotificationEnabled = it })
                            }
                        )
                    }
                }
            }

            //qly dữ liệu
            item {
                Text(
                    stringResource(R.string.data_management),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, start = 8.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingItem(
                            icon = Icons.Rounded.FileDownload,
                            title = "Export Data (CSV/PDF)",
                            onClick = { }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                        SettingItem(
                            icon = Icons.Rounded.DeleteForever,
                            title = stringResource(R.string.clear_all_data),
                            titleColor = Color.Red,
                            onClick = { showDeleteDialog = true }
                        )
                    }
                }
            }

            // information app
            item {
                Text(
                    stringResource(R.string.about),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, start = 8.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingItem(
                            icon = Icons.Rounded.Info,
                            title = stringResource(R.string.version),
                            trailing = { Text("1.0.0", color = Color.Gray) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                        SettingItem(
                            icon = Icons.Rounded.Star,
                            title = stringResource(R.string.rate_app),
                            onClick = { }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.action_ok), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}


@Composable
fun SettingItem(
    icon: ImageVector,
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (titleColor == Color.Red) Color.Red else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = titleColor
        )
        trailing?.invoke() ?: Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}
@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    AppTheme {
        SettingScreen()
    }
}
