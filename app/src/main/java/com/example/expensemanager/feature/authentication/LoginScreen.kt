package com.example.expensemanager.feature.authentication

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensemanager.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensemanager.data.model.AuthState
import com.example.expensemanager.designsystem.theme.AppIcons
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    isOnline: Boolean = true,
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit
) {
    val state by viewModel.authState.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var showCustomToast by remember { mutableStateOf(false) }

    // Regex: At least 1 letter, 1 number, min 8 chars
    val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$".toRegex()

    // Google Login Logic
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { viewModel.signInWithGoogle(it) }
        } catch (e: Exception) { /* Handle error */ }
    }

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            showCustomToast = true
            delay(1000)
            showCustomToast = false
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.home_welcome_back),
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.login_to_your_account),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                    viewModel.resetState() // Clear global error when typing
                },
                label = { Text(stringResource(R.string.email)) },
                leadingIcon = { AppIcons.MyIcon(resourceId = AppIcons.Email) },
                isError = emailError != null,
                supportingText = {
                    if (emailError != null) {
                        Text(text = emailError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                    viewModel.resetState() // Clear global error when typing
                },
                label = { Text(stringResource(R.string.password)) },
                leadingIcon = { AppIcons.MyIcon(resourceId = AppIcons.Password) },
                isError = passwordError != null,
                supportingText = {
                    if (passwordError != null) {
                        Text(text = passwordError!!, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Min 8 characters with letters & numbers", style = MaterialTheme.typography.labelSmall)
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        AppIcons.MyIcon(
                            resourceId = if (passwordVisible) AppIcons.Eye else AppIcons.EyeCrossed
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button
            Button(
                onClick = {
                    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    val isPassPatternValid = password.matches(passwordPattern)

                    if (!isEmailValid) {
                        emailError = "Invalid email format"
                    }
                    if (!isPassPatternValid) {
                        passwordError = "Password must be at least 8 characters with letters and numbers"
                    }

                    if (isEmailValid && isPassPatternValid) {
                        viewModel.login(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isOnline && state !is AuthState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOnline) MaterialTheme.colorScheme.primary else Color.Gray
                )
            ) {
                if (state is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.login), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Login
            OutlinedButton(
                onClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isOnline && state !is AuthState.Loading
            ) {
                AppIcons.MyIcon(resourceId = AppIcons.Google, size = 28.dp)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.continue_with_google), color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Link
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(stringResource(R.string.don_t_have_an_account), color = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.sign_up),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel.resetState()
                        onGoToRegister()
                    }
                )
            }

            // Global Error from Firebase/ViewModel
            if (state is AuthState.Error) {
                Text(
                    text = (state as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (!isOnline) {
                Text(
                    text = stringResource(R.string.please_check_your_internet_connection),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Custom Success Toast
        if (showCustomToast) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.logoexpense), null, Modifier.size(24.dp), tint = Color.Unspecified)
                    Text(stringResource(R.string.login_successful), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}