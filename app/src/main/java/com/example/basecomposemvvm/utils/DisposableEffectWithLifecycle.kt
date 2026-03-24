package com.example.basecomposemvvm.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Lifecycle-aware side-effect for Compose.
 *
 * Registers callbacks for specific lifecycle events and automatically
 * cleans up when the composable leaves the composition.
 *
 * @param lifecycleOwner the lifecycle owner to observe (defaults to current)
 * @param onResume called when lifecycle reaches ON_RESUME
 * @param onPause called when lifecycle reaches ON_PAUSE
 * @param onStart called when lifecycle reaches ON_START
 * @param onStop called when lifecycle reaches ON_STOP
 * @param onDispose called when the effect leaves composition
 */
@Composable
fun DisposableEffectWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onResume: () -> Unit = {},
    onPause: () -> Unit = {},
    onStart: () -> Unit = {},
    onStop: () -> Unit = {},
    onDispose: () -> Unit = {}
) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> onStart()
                Lifecycle.Event.ON_RESUME -> onResume()
                Lifecycle.Event.ON_PAUSE -> onPause()
                Lifecycle.Event.ON_STOP -> onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            onDispose()
        }
    }
}
