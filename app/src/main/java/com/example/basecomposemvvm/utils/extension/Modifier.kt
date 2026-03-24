package com.example.basecomposemvvm.utils.extension

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Conditionally applies [modifier] when [condition] is true.
 * Returns the original Modifier unchanged when [condition] is false.
 */
fun Modifier.thenIf(condition: Boolean, modifier: Modifier): Modifier =
    if (condition) this.then(modifier) else this

/**
 * Conditionally applies a modifier builder when [condition] is true.
 * Returns the original Modifier unchanged when [condition] is false.
 */
fun Modifier.applyIf(condition: Boolean, block: Modifier.() -> Modifier): Modifier =
    if (condition) block() else this

/**
 * Click modifier with debounce to prevent rapid double-taps.
 *
 * @param debounceMillis minimum interval between clicks (default 400ms)
 * @param onClick callback invoked on valid click
 */
@Composable
fun Modifier.debouncedClickable(
    debounceMillis: Long = 400L,
    onClick: () -> Unit
): Modifier = composed {
    var lastClickTime by remember { mutableLongStateOf(0L) }

    this.clickable {
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= debounceMillis) {
            lastClickTime = now
            onClick()
        }
    }
}
