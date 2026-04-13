package com.example.expensemanager.utils

object AuthValidator {
    //At least 1 letter, at least 1 number, and a minimum of 8 characters
    private val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$".toRegex()

    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Password cannot be empty"
            password.length < 8 -> "Password must be at least 8 characters long"
            !password.matches(passwordPattern) -> "Password must contain both letters and numbers"
            else -> null
        }
    }
}