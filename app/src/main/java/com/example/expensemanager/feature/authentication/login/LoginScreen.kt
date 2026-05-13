package com.example.expensemanager.feature.authentication.login

import android.view.autofill.AutofillManager
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle

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

    val autofillManager = context.getSystemService(AutofillManager::class.java)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val googleAction = viewModel.getComposeAuth().rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    viewModel.syncDataAfterGoogleLogin()
                }

                is NativeSignInResult.Error -> {
                    errorText = "Google Error: ${result.message}"
                }

                is NativeSignInResult.ClosedByUser -> {
                }

                is NativeSignInResult.NetworkError -> {
                    errorText = "Error connecting to Google"
                }
            }
        }
    )

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            autofillManager?.commit()

            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
            viewModel.resetState()
        } else if (state is AuthState.Error) {
            errorText = (state as AuthState.Error).message
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginHeader()

            LoginFields(
                email = email,
                onEmailChange = { email = it; errorText = null; viewModel.resetState() },
                password = password,
                onPasswordChange = { password = it; errorText = null; viewModel.resetState() },
                onForgotPasswordClick = {
                    showForgotPasswordDialog = true
                }
            )

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (email.isBlank() || password.isBlank()) {
                        errorText = "Please enter all required fields"
                    } else {
                        viewModel.login(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = isOnline && state !is AuthState.Loading
            ) {
                if (state is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.login),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            LoginSocialSection(onGoogleClick = {
                if (isOnline) {
                    googleAction.startFlow()
                } else {
                    errorText = "Need Internet Connection"
                }
            })

            Spacer(Modifier.height(32.dp))

            LoginFooter(onSignUpClick = { viewModel.resetState(); onGoToRegister() })
        }
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            initialEmail = email,
            viewModel = viewModel,
            onDismiss = { showForgotPasswordDialog = false }
        )
    }
}