package com.ojhdtapp.parabox.extension.ws.ui.main

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ojhdtapp.parabox.extension.ws.core.util.DataStoreKeys
import com.ojhdtapp.parabox.extension.ws.core.util.dataStore
import com.ojhdtapp.parabox.extension.ws.domain.util.ServiceStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext val context: Context,
) : ViewModel() {
    // UiEvent
    private val _uiEventFlow = MutableSharedFlow<UiEvent>()
    val uiEventFlow = _uiEventFlow.asSharedFlow()
    fun emitToUiEventFlow(event: UiEvent) {
        viewModelScope.launch {
            _uiEventFlow.emit(event)
        }
    }

    // MainApp Installation
    private val _isMainAppInstalled = MutableStateFlow(false)
    val isMainAppInstalled get() = _isMainAppInstalled.asStateFlow()
    fun setMainAppInstalled(isInstalled: Boolean) {
        viewModelScope.launch {
            _isMainAppInstalled.emit(isInstalled)
        }
    }

    // Service Status
    private val _serviceStatusStateFlow = MutableStateFlow<ServiceStatus>(ServiceStatus.Stop)
    val serviceStatusStateFlow = _serviceStatusStateFlow.asStateFlow()
    fun updateServiceStatusStateFlow(value: ServiceStatus) {
        viewModelScope.launch {
            _serviceStatusStateFlow.emit(value)
        }
    }

    // Auto Login Switch
    val autoLoginSwitchFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { settings ->
            settings[DataStoreKeys.AUTO_LOGIN] ?: false
        }

    fun setAutoLoginSwitch(value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { settings ->
                settings[DataStoreKeys.AUTO_LOGIN] = value
            }
        }
    }

    // Auto Reconnect Switch
    val autoReconnectSwitchFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { settings ->
            settings[DataStoreKeys.AUTO_RECONNECT] ?: true
        }

    fun setAutoReconnectSwitch(value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { settings ->
                settings[DataStoreKeys.AUTO_RECONNECT] = value
            }
        }
    }

    // Foreground Service
    val foregroundServiceSwitchFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { settings ->
            settings[DataStoreKeys.FOREGROUND_SERVICE] ?: true
        }

    fun setForegroundServiceSwitch(value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { settings ->
                settings[DataStoreKeys.FOREGROUND_SERVICE] = value
            }
        }
    }

    // ws
    val wsUrlFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { settings ->
            settings[DataStoreKeys.WS_URL] ?: ""
        }

    fun setWSUrl(value: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[DataStoreKeys.WS_URL] = value
            }
        }
    }

    val wsTokenFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { settings ->
            settings[DataStoreKeys.WS_TOKEN] ?: ""
        }

    fun setWSToken(value: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[DataStoreKeys.WS_TOKEN] = value
            }
        }
    }

    private val _refreshingKey = mutableStateOf<Int>(0)
    val refreshingKey: State<Int> = _refreshingKey
    fun refreshKey() {
        _refreshingKey.value = _refreshingKey.value + 1
    }

    val appVersion = com.ojhdtapp.parabox.extension.ws.BuildConfig.VERSION_NAME
}

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}