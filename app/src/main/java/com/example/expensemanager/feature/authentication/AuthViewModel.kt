package com.example.expensemanager.feature.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.model.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
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
                // Create user
                val result = auth.createUserWithEmailAndPassword(email, pass).await()

                val profileUpdates = userProfileChangeRequest {
                    displayName = fullName
                }
                result.user?.updateProfile(profileUpdates)?.await()

                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Registration failed. Try a different email."
                )
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Google Authentication failed"
                )
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserName(): String = auth.currentUser?.displayName ?: "User"

    fun getCurrentUserEmail(): String = auth.currentUser?.email ?: ""


    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
}