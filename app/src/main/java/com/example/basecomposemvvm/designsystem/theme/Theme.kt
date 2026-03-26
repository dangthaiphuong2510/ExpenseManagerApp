package com.example.basecomposemvvm.designsystem.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(

    //MAIN
    primary = OrangePrimary,
    onPrimary = CardWhite,

    //BACKGROUND
    background = BackgroundApp,
    onBackground = TextPrimary,

    //SURFACE (Card, BottomNav,...)
    surface = CardWhite,
    onSurface = TextPrimary,

    //SUB TEXT
    onSurfaceVariant = TextSecondary,

    //BORDER / DIVIDER
    surfaceVariant = DividerLight,

    //ERROR (expense đỏ)
    error = Color(0xFFE74C3C)
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}