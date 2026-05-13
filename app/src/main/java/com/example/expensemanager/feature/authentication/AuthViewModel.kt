package com.example.expensemanager.feature.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.model.AuthState
import com.example.expensemanager.data.remote.repository.AppRepository
import com.example.expensemanager.data.remote.repository.impl.SyncRepoImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.compose.auth.composeAuth
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: Auth,
    private val repository: AppRepository,
    private val syncRepo: SyncRepoImpl,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWith(Email) {
                    this.email = email
                    this.password = pass
                }

                syncRepo.syncCloudData()

                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Login failed. Please check your credentials."
                )
            }
        }
    }

    fun register(email: String, pass: String, confirmPass: String, fullName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signUpWith(Email) {
                    this.email = email
                    this.password = pass
                    data = buildJsonObject {
                        put("full_name", fullName)
                    }
                }

                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Registration failed. Email might already exist."
                )
            }
        }
    }

    fun signInWithGoogle(token: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWith(IDToken) {
                    idToken = token
                }

                syncRepo.syncCloudData()

                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Google Login Failed")
            }
        }
    }

    fun syncDataAfterGoogleLogin() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                syncRepo.syncCloudData()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Error syncing data after Google login"
                )
            }
        }
    }

    fun sendResetPasswordOtp(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank()) {
            onError("Please enter your email")
            return
        }
        viewModelScope.launch {
            try {
                auth.resetPasswordForEmail(email)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unable to send the encrypted OTP. Please check your email again.")
            }
        }
    }

    fun verifyOtpAndResetPassword(
        email: String,
        otp: String,
        newPass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (otp.length != 6) {
            onError("The OTP code must consist of exactly 6 digits.")
            return
        }
        if (newPass.length < 6) {
            onError("Password new must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            try {
                auth.verifyEmailOtp(
                    type = OtpType.Email.RECOVERY,
                    email = email,
                    token = otp
                )

                auth.updateUser {
                    password = newPass
                }

                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "OTP invalid or expired")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private fun getCurrentUser(): UserInfo? = auth.currentUserOrNull()

    fun isUserLoggedIn(): Boolean = getCurrentUser() != null

    fun getCurrentUserName(): String {
        return getCurrentUser()?.userMetadata?.get("full_name")?.toString()?.replace("\"", "")
            ?: "User"
    }

    fun getCurrentUserEmail(): String = getCurrentUser()?.email ?: ""

    fun logout() {
        viewModelScope.launch {
            try {
                withContext(NonCancellable) {
                    repository.clearAllLocalData()
                    auth.signOut()
                }

                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Logout failed")
            }
        }
    }

    fun getComposeAuth() = supabaseClient.composeAuth
}