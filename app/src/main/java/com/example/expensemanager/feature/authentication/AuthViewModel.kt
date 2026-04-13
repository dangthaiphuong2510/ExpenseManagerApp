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

    // Regex: At least 1 letter, 1 number, and minimum 8 characters
    private val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$".toRegex()

    private fun isValidPassword(password: String): Boolean {
        return password.matches(passwordPattern)
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Please enter email and password")
            return
        }

        // Apply same constraint to Login
        if (!isValidPassword(pass)) {
            _authState.value = AuthState.Error("Password must be at least 8 characters and contain both letters and numbers")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Login failed. Check email and password."
                )
            }
        }
    }

    fun register(email: String, pass: String, confirmPass: String, fullName: String) {
        if (email.isBlank() || pass.isBlank() || confirmPass.isBlank() || fullName.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        // 1. Check if password and confirm password match
        if (pass != confirmPass) {
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }

        // 2. Updated Constraint: Minimum 8 characters + Alphanumeric
        if (!isValidPassword(pass)) {
            _authState.value = AuthState.Error("Password must be at least 8 characters and contain both letters and numbers")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()

                val profileUpdates = userProfileChangeRequest {
                    displayName = fullName
                }
                result.user?.updateProfile(profileUpdates)?.await()

                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.localizedMessage ?: "Registration failed."
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
                    e.localizedMessage ?: "Google Sign-In failed"
                )
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserName(): String {
        return auth.currentUser?.displayName ?: "User"
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
}