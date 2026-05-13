package com.example.expensemanager

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.expensemanager.core.navigation.AppNavigation
import com.example.expensemanager.core.network.NetworkConnectivityObserver
import com.example.expensemanager.data.local.datastore.CurrencyManager
import com.example.expensemanager.data.remote.repository.impl.SyncRepoImpl // Import cái này
import com.example.expensemanager.designsystem.theme.AppTheme
import com.example.expensemanager.utils.format.LocalCurrencySymbol
import io.github.jan.supabase.auth.Auth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkConnectivityObserver: NetworkConnectivityObserver

    @Inject
    lateinit var auth: Auth

    @Inject
    lateinit var currencyManager: CurrencyManager

    @Inject
    lateinit var syncRepo: SyncRepoImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        enableEdgeToEdge()

        lifecycleScope.launch {
            Log.d("MAIN_SYNC", "Starting sync...")
            syncRepo.syncCloudData()
        }

        setContent {
            val isCurrencySet by currencyManager.isCurrencySet.collectAsStateWithLifecycle(
                initialValue = false
            )
            val currencySymbol by currencyManager.currencySymbol.collectAsStateWithLifecycle(
                initialValue = "₫"
            )

            // Theme state
            var themeState by rememberSaveable { mutableStateOf("System") }
            val isDarkTheme = when (themeState) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            val navController = rememberNavController()

            CompositionLocalProvider(LocalCurrencySymbol provides currencySymbol) {
                AppTheme(darkTheme = isDarkTheme) {
                    val backgroundColor = MaterialTheme.colorScheme.background.toArgb()

                    LaunchedEffect(isDarkTheme, backgroundColor) {
                        enableEdgeToEdge(
                            statusBarStyle = SystemBarStyle.auto(
                                backgroundColor,
                                backgroundColor
                            ) { isDarkTheme },
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
                        onThemeChange = { themeState = it },
                        isCurrencySet = isCurrencySet
                    )
                }
            }
        }
    }
}