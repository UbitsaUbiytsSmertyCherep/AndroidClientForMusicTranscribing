package com.example.firstapplication

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context){
    private val API_ENDPOINT_KEY = stringPreferencesKey("api_endpoint")
    val apiEndpointFlow: Flow<String> = context.dataStore.data.map{ preferences ->
        preferences[API_ENDPOINT_KEY] ?: "https://default-api.com"
    }

    suspend fun saveApiEndpoint(url: String){
        context.dataStore.edit { preferences ->
            preferences[API_ENDPOINT_KEY] = url
        }
    }
}