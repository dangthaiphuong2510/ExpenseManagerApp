package com.example.basecomposemvvm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.navigation.compose.rememberNavController
import com.example.basecomposemvvm.core.navigation.AppNavigation
import com.example.basecomposemvvm.core.network.NetworkConnectivityObserver
import com.example.basecomposemvvm.designsystem.theme.AppTheme
import com.example.basecomposemvvm.designsystem.theme.BackgroundApp
import com.example.basecomposemvvm.designsystem.theme.CardWhite
import com.example.basecomposemvvm.designsystem.theme.OrangePrimary
import com.example.basecomposemvvm.designsystem.theme.TextPrimary
import com.google.firebase.auth.FirebaseAuth // THÊM DÒNG NÀY
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkConnectivityObserver: NetworkConnectivityObserver

    @Inject
    lateinit var auth: FirebaseAuth

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
                    AppNavigation(
                        navController = navController,
                        connectivityObserver = networkConnectivityObserver,
                        auth = auth
                    )
                }
            }
        }
    }
}