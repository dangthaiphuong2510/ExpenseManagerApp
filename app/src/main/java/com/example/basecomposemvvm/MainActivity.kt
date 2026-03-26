package com.example.basecomposemvvm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController // QUAN TRỌNG: Thêm import này
import com.example.basecomposemvvm.core.navigation.AppNavigation
import com.example.basecomposemvvm.designsystem.theme.AppTheme
import com.example.basecomposemvvm.designsystem.theme.BackgroundApp
import com.example.basecomposemvvm.designsystem.theme.CardWhite
import com.example.basecomposemvvm.designsystem.theme.OrangePrimary
import com.example.basecomposemvvm.designsystem.theme.TextPrimary
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar?.hide()
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            AppTheme {
                MaterialTheme(
                    colorScheme = lightColorScheme(
                        primary = OrangePrimary,
                        background = BackgroundApp,
                        surface = CardWhite,
                        onBackground = TextPrimary,
                        onSurface = TextPrimary
                    )
                ) {
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}