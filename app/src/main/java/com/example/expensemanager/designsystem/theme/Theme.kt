package com.example.expensemanager.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    // MAIN COLORS
    primary = OrangePrimary,
    onPrimary = Color.White,

    secondaryContainer = TextSecondary.copy(alpha = 0.2f),

    // BACKGROUND COLORS
    background = Color(0xFF121212),
    onBackground = Color(0xFFE3E3E3),

    // SURFACE COLORS (Cards, Bottom Navigation)
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE3E3E3),

    // SECONDARY / SUB TEXT
    onSurfaceVariant = Color(0xFFB0B0B0),

    // BORDER / DIVIDER
    surfaceVariant = Color(0xFF333333),

    // ERROR COLORS
    error = Color(0xFFCF6679)
)


private val LightColorScheme = lightColorScheme(
    // MAIN COLORS
    primary = OrangePrimary,
    onPrimary = CardWhite,

    secondaryContainer = TextSecondary.copy(alpha = 0.1f),

    // BACKGROUND COLORS
    background = BackgroundApp,
    onBackground = TextPrimary,

    // SURFACE COLORS (Cards, Bottom Navigation, etc.)
    surface = CardWhite,
    onSurface = TextPrimary,

    // SUB TEXT
    onSurfaceVariant = TextSecondary,

    // BORDER / DIVIDER
    surfaceVariant = DividerLight,

    // ERROR COLORS
    error = Color(0xFFE74C3C)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}