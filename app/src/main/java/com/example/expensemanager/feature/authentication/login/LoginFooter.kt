package com.example.expensemanager.feature.authentication.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensemanager.R

@Composable
fun LoginFooter(onSignUpClick: () -> Unit) {
    Row {
        Text(stringResource(R.string.don_t_have_an_account), color = Color.Gray)
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.sign_up),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.clickable { onSignUpClick() }
        )
    }
}