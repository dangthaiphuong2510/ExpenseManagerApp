package com.example.expensemanager.feature.authentication.register

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensemanager.data.model.AuthState
import com.example.expensemanager.feature.authentication.AuthViewModel

@Composable
fun RegisterScreen(
    isOnline: Boolean,
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$".toRegex()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onRegisterSuccess()
            viewModel.resetState()
        } else if (state is AuthState.Error) {
            errorText = (state as AuthState.Error).message
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegisterHeader()

            RegisterFields(
                fullName = fullName,
                onFullNameChange = { fullName = it; errorText = null },
                email = email,
                onEmailChange = { email = it; errorText = null; viewModel.resetState() },
                password = password,
                onPasswordChange = { password = it; errorText = null; viewModel.resetState() },
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = { confirmPassword = it; errorText = null }
            )

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    val isPassPatternValid = password.matches(passwordPattern)
                    val isMatch = password == confirmPassword
                    val isNameValid = fullName.isNotBlank()

                    when {
                        !isNameValid || email.isBlank() || password.isBlank() -> { errorText = "Please fill in all fields" }
                        !isEmailValid -> { errorText = "Invalid email format" }
                        !isPassPatternValid -> { errorText = "Password must be at least 8 characters with letters & numbers" }
                        !isMatch -> { errorText = "Passwords do not match" }
                        else -> {
                            errorText = null
                            viewModel.register(email, password, confirmPassword, fullName)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = isOnline && state !is AuthState.Loading
            ) {
                if (state is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("SIGN UP", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            RegisterFooter(onGoToLogin = {
                viewModel.resetState()
                onGoToLogin()
            })
        }
    }
}