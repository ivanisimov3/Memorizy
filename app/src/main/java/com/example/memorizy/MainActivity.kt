package com.example.memorizy

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.memorizy.data.sync.SyncManager
import com.example.memorizy.ui.MemorizyApp
import com.example.memorizy.ui.main.MainViewModel
import com.example.memorizy.ui.theme.MemorizyTheme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

// Единственная Activity

@AndroidEntryPoint  // Говорим Hilt где вклиниться в приложение
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var syncManager: SyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()   // Подписываемся на StateFlow

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    viewModel.onNotificationPermissionResult(isGranted)
                }

                LaunchedEffect(uiState.shouldRequestNotificationPermission) {
                    if (uiState.shouldRequestNotificationPermission) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            } else {
                LaunchedEffect(uiState.shouldRequestNotificationPermission) {
                    if (uiState.shouldRequestNotificationPermission) {
                        viewModel.onNotificationPermissionResult(true)
                    }
                }
            }

            LaunchedEffect(uiState.isDarkTheme) {   // Подписываемся на изменение isDarkTheme
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !uiState.isDarkTheme // Перекрашиваем statusBar при изменении темы
            }

            MemorizyTheme(darkTheme = uiState.isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    MemorizyApp()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        syncManager.scheduleOneTimeSync()
    }
}