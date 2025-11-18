package com.example.memorizy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.memorizy.ui.MemorizyApp
import com.example.memorizy.ui.theme.MemorizyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint  // MainActivity получает все зависимости
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MemorizyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    MemorizyApp()
                }
            }
        }
    }
}