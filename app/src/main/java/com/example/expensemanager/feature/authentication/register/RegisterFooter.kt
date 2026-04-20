package com.example.expensemanager.feature.authentication.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RegisterFooter(onGoToLogin: () -> Unit) {
    Row(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(text = "Already have an account?", color = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Sign In",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.clickable { onGoToLogin() }
        )
    }
}