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
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.expensemanager.core.navigation.AppNavigation
import com.example.expensemanager.core.network.NetworkConnectivityObserver
import com.example.expensemanager.data.local.datastore.CurrencyManager
import com.example.expensemanager.designsystem.theme.AppTheme
import com.example.expensemanager.utils.format.LocalCurrencySymbol
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var networkConnectivityObserver: NetworkConnectivityObserver
    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var currencyManager: CurrencyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // Cài đặt splash screen trước super.onCreate
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        enableEdgeToEdge()

        setContent {
            // Lấy trạng thái từ DataStore
            val isCurrencySet by currencyManager.isCurrencySet.collectAsStateWithLifecycle(initialValue = false)
            val currencySymbol by currencyManager.currencySymbol.collectAsStateWithLifecycle(initialValue = "₫")

            // Xử lý Theme
            var themeState by rememberSaveable { mutableStateOf("System") }
            val isDarkTheme = when (themeState) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            val navController = rememberNavController()

            // Cung cấp Symbol tiền tệ cho toàn bộ App
            CompositionLocalProvider(LocalCurrencySymbol provides currencySymbol) {
                AppTheme(darkTheme = isDarkTheme) {
                    val backgroundColor = MaterialTheme.colorScheme.background.toArgb()

                    // Cập nhật thanh trạng thái hệ thống theo màu nền của Theme
                    LaunchedEffect(isDarkTheme, backgroundColor) {
                        enableEdgeToEdge(
                            statusBarStyle = SystemBarStyle.auto(backgroundColor, backgroundColor) { isDarkTheme },
                            navigationBarStyle = SystemBarStyle.auto(backgroundColor, backgroundColor) { isDarkTheme }
                        )
                    }

                    AppNavigation(
                        navController = navController,
                        connectivityObserver = networkConnectivityObserver,
                        auth = auth,
                        currentTheme = themeState,
                        onThemeChange = { themeState = it },
                        isCurrencySet = isCurrencySet
                    )
                }
            }
        }
    }
}