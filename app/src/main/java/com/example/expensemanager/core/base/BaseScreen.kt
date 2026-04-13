@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.expensemanager.core.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun BaseScreen(
    modifier: Modifier = Modifier,
    showLoading: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    pullToRefreshEnabled: Boolean = false,
    errorMessage: String? = null,
    clearErrorMessage: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (pullToRefreshEnabled) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize(), content = content)
            }
        } else {
            content()
        }

        // Loading overlay
        AnimatedVisibility(
            visible = showLoading,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Error dialog
        if (!errorMessage.isNullOrEmpty()) {
            AlertDialog(
                onDismissRequest = clearErrorMessage,
                title = { Text(text = "Error") },
                text = { Text(text = errorMessage) },
                confirmButton = {
                    TextButton(onClick = clearErrorMessage) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
