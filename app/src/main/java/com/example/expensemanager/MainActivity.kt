package com.example.expensemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.toArgb // Import quan trọng để chuyển đổi màu
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.expensemanager.core.navigation.AppNavigation
import com.example.expensemanager.core.network.NetworkConnectivityObserver
import com.example.expensemanager.designsystem.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkConnectivityObserver: NetworkConnectivityObserver

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // Khởi tạo Splash Screen trước
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        actionBar?.hide()

        setContent {
            // 1. Quản lý trạng thái Theme (System, Dark, Light)
            var themeState by rememberSaveable { mutableStateOf("System") }
            val isDarkTheme = when (themeState) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            val navController = rememberNavController()

            // 2. Bọc trong AppTheme để có thể lấy màu từ MaterialTheme
            AppTheme(darkTheme = isDarkTheme) {

                val backgroundColor = MaterialTheme.colorScheme.background.toArgb()

                LaunchedEffect(isDarkTheme, backgroundColor) {
                    enableEdgeToEdge(
                        statusBarStyle = if (!isDarkTheme) {
                            // Chế độ sáng: Ép màu nền và icon tối
                            SystemBarStyle.light(backgroundColor, backgroundColor)
                        } else {
                            // Chế độ tối: Ép màu nền và icon sáng
                            SystemBarStyle.dark(backgroundColor)
                        },
                        navigationBarStyle = SystemBarStyle.auto(
                            backgroundColor,
                            backgroundColor
                        ) { isDarkTheme }
                    )
                }

                AppNavigation(
                    navController = navController,
                    connectivityObserver = networkConnectivityObserver,
                    auth = auth,
                    currentTheme = themeState,
                    onThemeChange = { themeState = it }
                )
            }
        }
    }
}