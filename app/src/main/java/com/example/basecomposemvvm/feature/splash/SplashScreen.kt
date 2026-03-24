package com.example.basecomposemvvm.feature.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Splash screen composable — the app's launch screen.
 *
 * Displays briefly then navigates to Home, clearing itself from the back stack
 * so the user cannot navigate back to it.
 *
 * @param onNavigateToHome Callback invoked when the splash duration elapses.
 */
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "BaseComposeMVVM",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
            ),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private const val SPLASH_DURATION_MS = 1_500L
