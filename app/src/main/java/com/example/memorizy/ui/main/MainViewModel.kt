package com.example.memorizy.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorizy.data.source.local.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsDataStore: SettingsDataStore
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = settingsDataStore.isDarkTheme
        .map { it }
        .stateIn(   // Превращаем поток в поток состояния
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )
}