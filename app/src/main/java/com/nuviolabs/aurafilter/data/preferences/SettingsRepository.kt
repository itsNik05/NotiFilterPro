package com.nuviolabs.aurafilter.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.booleanPreferencesKey

private val Context.dataStore by preferencesDataStore(name = "user_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DIGEST_INTERVAL_KEY = intPreferencesKey("digest_interval_hours")
    // NEW: Keys for our stats
    private val BLOCKED_COUNT_KEY = intPreferencesKey("blocked_count")
    private val ALLOWED_COUNT_KEY = intPreferencesKey("allowed_count")
    private val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")

    val digestIntervalFlow: Flow<Int> = context.dataStore.data.map { it[DIGEST_INTERVAL_KEY] ?: 2 }

    // NEW: Flows to read the stats
    val blockedCountFlow: Flow<Int> = context.dataStore.data.map { it[BLOCKED_COUNT_KEY] ?: 0 }
    val allowedCountFlow: Flow<Int> = context.dataStore.data.map { it[ALLOWED_COUNT_KEY] ?: 0 }

    val isDarkModeFlow: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE_KEY]
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDark
        }
    }

    suspend fun saveDigestInterval(hours: Int) {
        context.dataStore.edit { it[DIGEST_INTERVAL_KEY] = hours }
    }

    // NEW: Functions to add +1 to our stats
    suspend fun incrementBlockedCount() {
        context.dataStore.edit { preferences ->
            val current = preferences[BLOCKED_COUNT_KEY] ?: 0
            preferences[BLOCKED_COUNT_KEY] = current + 1
        }
    }

    suspend fun incrementAllowedCount() {
        context.dataStore.edit { preferences ->
            val current = preferences[ALLOWED_COUNT_KEY] ?: 0
            preferences[ALLOWED_COUNT_KEY] = current + 1
        }
    }
}
