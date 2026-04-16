package com.example.expensemanager.feature.authentication

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
fun RegisterScreen(
    isOnline: Boolean,
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.authState.collectAsState()
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Local error states for immediate validation
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var showCustomToast by remember { mutableStateOf(false) }

    //At least 1 letter, 1 number, min 8 chars
    val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$".toRegex()

    // Logic Google Sign-In
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
        } catch (e: Exception) { }
    }

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            showCustomToast = true
            delay(1000)
            showCustomToast = false
            onRegisterSuccess()
            viewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.create_account),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.start_managing_your_finances_today),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // Full Name Field
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    fullNameError = null
                },
                label = { Text(stringResource(R.string.full_name)) },
                leadingIcon = { AppIcons.MyIcon(resourceId = AppIcons.User) },
                isError = fullNameError != null,
                supportingText = { fullNameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                    viewModel.resetState()
                },
                label = { Text(stringResource(R.string.email)) },
                leadingIcon = { AppIcons.MyIcon(resourceId = AppIcons.Email) },
                isError = emailError != null,
                supportingText = { emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                    viewModel.resetState()
                },
                label = { Text(stringResource(R.string.password)) },
                leadingIcon = { AppIcons.MyIcon(resourceId = AppIcons.Password) },
                isError = passwordError != null,
                supportingText = {
                    if (passwordError != null) {
                        Text(passwordError!!, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Min 8 characters with letters & numbers", style = MaterialTheme.typography.labelSmall)
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        AppIcons.MyIcon(resourceId = if (passwordVisible) AppIcons.Eye else AppIcons.EyeCrossed)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = null
                },
                label = { Text(stringResource(R.string.confirm_password)) },
                leadingIcon = { AppIcons.MyIcon(resourceId = AppIcons.ConfirmPassword) },
                isError = confirmPasswordError != null,
                supportingText = { confirmPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        AppIcons.MyIcon(resourceId = if (confirmPasswordVisible) AppIcons.Eye else AppIcons.EyeCrossed)
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            // Global Error from Firebase/ViewModel
            if (state is AuthState.Error) {
                Text(
                    text = (state as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SignUp Button
            Button(
                onClick = {
                    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    val isPassPatternValid = password.matches(passwordPattern)
                    val isMatch = password == confirmPassword
                    val isNameValid = fullName.isNotBlank()

                    if (!isNameValid) fullNameError = "Full name is required"
                    if (!isEmailValid) emailError = "Invalid email format"
                    if (!isPassPatternValid) passwordError = "Password must be at least 8 chars with letters & numbers"
                    if (!isMatch) confirmPasswordError = "Passwords do not match"

                    if (isEmailValid && isPassPatternValid && isMatch && isNameValid) {
                        viewModel.register(email, password, confirmPassword, fullName)
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
                    Text("SIGN UP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isOnline && state !is AuthState.Loading
            ) {
                AppIcons.MyIcon(resourceId = AppIcons.Google, size = 28.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.continue_with_google), color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = stringResource(R.string.already_have_an_account), color = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sign In",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel.resetState()
                        onGoToLogin()
                    }
                )
            }

            if (!isOnline) {
                Text(
                    text = stringResource(R.string.please_check_your_internet_connection),
                    color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
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
                    Text(text = stringResource(R.string.register_successful), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}