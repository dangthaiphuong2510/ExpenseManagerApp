package com.example.expensemanager.feature.authentication.login

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensemanager.R
import com.example.expensemanager.data.model.AuthState
import com.example.expensemanager.feature.authentication.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    isOnline: Boolean = true,
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit
) {
    val state by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Google Sign-In Logic
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail().build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { viewModel.signInWithGoogle(it) }
        } catch (e: Exception) { errorText = "Google Sign-In failed" }
    }

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
            viewModel.resetState()
        } else if (state is AuthState.Error) {
            errorText = (state as AuthState.Error).message
        }
    }

    Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginHeader()

            LoginFields(
                email = email,
                onEmailChange = { email = it; errorText = null; viewModel.resetState() },
                password = password,
                onPasswordChange = { password = it; errorText = null; viewModel.resetState() },
                onForgotPasswordClick = { /* Click logic */ }
            )

            if (errorText != null) {
                Text(errorText!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
            } else { Spacer(Modifier.height(8.dp)) }

            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (email.isBlank() || password.isBlank()) errorText = "Please fill all fields"
                    else viewModel.login(email, password)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = isOnline && state !is AuthState.Loading
            ) {
                if (state is AuthState.Loading) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = androidx.compose.ui.graphics.Color.White, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.login), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            LoginSocialSection(onGoogleClick = {
                googleSignInClient.signOut().addOnCompleteListener {
                    googleLauncher.launch(googleSignInClient.signInIntent)
                }
            })

            Spacer(Modifier.height(32.dp))

            LoginFooter(onSignUpClick = { viewModel.resetState(); onGoToRegister() })
        }
    }
}