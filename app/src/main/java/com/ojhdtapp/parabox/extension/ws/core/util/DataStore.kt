package com.ojhdtapp.parabox.extension.ws.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object DataStoreKeys{
    val AUTO_LOGIN = booleanPreferencesKey("auto_login")
    val AUTO_RECONNECT = booleanPreferencesKey("auto_reconnect")
    val FOREGROUND_SERVICE = booleanPreferencesKey("foreground_service")
    val WS_URL = stringPreferencesKey("ws_url")
    val WS_TOKEN = stringPreferencesKey("ws_token")
}