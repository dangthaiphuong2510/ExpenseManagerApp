package com.example.basecomposemvvm.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * Collect a Flow as a LaunchedEffect side-effect in Compose.
 *
 * Useful for one-shot events (navigation, snackbar, toast) emitted from a ViewModel.
 * The collection restarts whenever [key] changes.
 */
@Composable
fun <T> Flow<T>.collectAsEffect(
    key: Any? = Unit,
    action: suspend (T) -> Unit
) {
    LaunchedEffect(key) {
        collectLatest(action)
    }
}
