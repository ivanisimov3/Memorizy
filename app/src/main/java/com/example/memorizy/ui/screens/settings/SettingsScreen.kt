package com.example.memorizy.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import com.example.memorizy.ui.utils.AppIcon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import com.example.memorizy.ui.utils.GlassContainer
import com.example.memorizy.ui.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
    uiState: SettingsState,
    onSyncClick: () -> Unit,
    onThemeChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit
){
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        AppIcon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        SettingsScreenBody(
            modifier = Modifier
                .padding(paddingValues),
            uiState = uiState,
            onLoginClick = onLoginClick,
            onSyncClick = onSyncClick,
            onThemeChange = onThemeChange,
            onNotificationsChange = onNotificationsChange,
            onLogoutButtonClick = { showLogoutDialog = true }
        )
    }

    if (showLogoutDialog){
        LogoutDialog(
            onConfirmLogout = {
                onLogoutClick()
                showLogoutDialog = false
            },
            onDismiss = {
                showLogoutDialog = false
            }
        )
    }
}

@Composable
private fun SettingsScreenBody(
    modifier: Modifier,
    uiState: SettingsState,
    onLoginClick: () -> Unit,
    onSyncClick: () -> Unit,
    onThemeChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onLogoutButtonClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ){
        SettingsSection(title = stringResource(R.string.account_text)) {
            if (uiState.isLoggedIn) {
                Text(
                    text = stringResource(
                        R.string.settings_user_warning,
                        uiState.username ?: stringResource(R.string.guest_text)
                    ),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(12.dp))

                GlassContainer(
                    modifier = Modifier
                        .clickable(onClick = onLogoutButtonClick)
                        .height(40.dp)
                        .width(90.dp)
                ) {
                    Text(stringResource(R.string.logout))
                }
            } else {
                Text(
                    text = stringResource(R.string.settings_guest_warning),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(12.dp))

                GlassContainer(
                    modifier = Modifier
                        .clickable(onClick = onLoginClick)
                        .height(40.dp)
                        .width(90.dp)
                ) {
                    Text(
                        text = stringResource(R.string.login_button_text),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(Modifier.height(2.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.secondary)

        SettingsSection(title = stringResource(R.string.Synchronization_text)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = onSyncClick,
                        enabled = uiState.isLoggedIn,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                            )
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.synchronize_button_text),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (uiState.isLoggedIn && uiState.lastSyncTime != null) {

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = stringResource(
                                R.string.sync_last_time,
                                DateUtils.formatFullDateTime(uiState.lastSyncTime)
                            ),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            if (!uiState.isLoggedIn) {
                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.settings_sync_warning),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(Modifier.height(2.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.secondary)

        SettingsSection(title = stringResource(R.string.app_theme_text)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.settings_dark_theme_text),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = uiState.isDarkTheme,
                    onCheckedChange = onThemeChange
                )
            }
        }

        Spacer(Modifier.height(2.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.secondary)

        SettingsSection(title = stringResource(R.string.notifications_text)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.settings_notifications_text),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = onNotificationsChange
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
    }
}

@Composable
private fun LogoutDialog(
    onConfirmLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        shape = RoundedCornerShape(18.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.logout_question),
                style = MaterialTheme.typography.displayMedium
            )
        },
        text = {
            Text(
                text = stringResource(R.string.logout_warning),
                style = MaterialTheme.typography.bodySmall
            )
       },
        confirmButton = {
            TextButton(
                onClick = onConfirmLogout
            ) {
                Text(
                    text = stringResource(R.string.logout),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.cancel_text),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}