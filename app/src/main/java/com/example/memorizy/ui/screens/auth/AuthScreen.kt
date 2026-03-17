package com.example.memorizy.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.ui.utils.GlassContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthClick: () -> Unit,
    uiState: AuthState,
    onBackClick: () -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .shadow(elevation = 8.dp),
                title = {
                    Text(
                        text = stringResource(R.string.register_screen_lable),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        AuthScreenBody(
            modifier = Modifier
                .padding(paddingValues),
            uiState = uiState,
            onUsernameChanged = onUsernameChanged,
            onPasswordChanged = onPasswordChanged,
            onLoginClick = onLoginClick,
            onRegisterClick = onRegisterClick
        )
    }
}

@Composable
private fun AuthScreenBody(
    modifier: Modifier,
    uiState: AuthState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = onUsernameChanged,
                    label = {
                        Text(
                            text = stringResource(R.string.login_text),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(18.dp)
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChanged,
                    label = {
                        Text(
                            text = stringResource(R.string.password_text),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(18.dp)
                )

                if (uiState.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(uiState.error),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ){
                    GlassContainer(
                        modifier = Modifier
                            .height(40.dp)
                            .width(220.dp),
                        onClick = onRegisterClick,
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Text(
                            text = stringResource(R.string.register_button_text),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(Modifier.size(8.dp))

                    GlassContainer(
                        modifier = Modifier
                            .height(40.dp)
                            .width(90.dp),
                        onClick = onLoginClick,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = stringResource(R.string.login_button_text),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}