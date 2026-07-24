package com.example.firstapplication

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AuthManager(private val context: Context) {

    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val EXPIRES_AT_KEY = longPreferencesKey("expires_at")

    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        val token = preferences[TOKEN_KEY]
        val expiresAt = preferences[EXPIRES_AT_KEY] ?: 0L
        val now = System.currentTimeMillis() / 1000
        if (token != null && expiresAt > now) {
            token
        } else {
            null
        }
    }

    suspend fun saveAuthResponse(authResponse: AuthResponse) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = authResponse.token
            val expiration = System.currentTimeMillis() / 1000 + (24 * 60 * 60)
            preferences[EXPIRES_AT_KEY] = expiration
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(EXPIRES_AT_KEY)
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.first().let { prefs ->
            val token = prefs[TOKEN_KEY]
            val expiresAt = prefs[EXPIRES_AT_KEY] ?: 0L
            val now = System.currentTimeMillis() / 1000
            if (token != null && expiresAt > now) token else null
        }
    }
}