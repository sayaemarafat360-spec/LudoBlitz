package com.ludoblitz.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("ludo_prefs")

class PreferenceManager(private val context: Context) {
    private val ds = context.dataStore

    val soundEnabled: Flow<Boolean> = ds.data.map { it[booleanPreferencesKey("sound")] ?: true }
    val vibrationEnabled: Flow<Boolean> = ds.data.map { it[booleanPreferencesKey("vibration")] ?: true }
    val darkMode: Flow<Boolean> = ds.data.map { it[booleanPreferencesKey("dark_mode")] ?: false }
    val userId: Flow<String?> = ds.data.map { it[stringPreferencesKey("user_id")] }
    val hasSeenTutorial: Flow<Boolean> = ds.data.map { it[booleanPreferencesKey("tutorial")] ?: false }

    suspend fun setSound(v: Boolean) { ds.edit { it[booleanPreferencesKey("sound")] = v } }
    suspend fun setVibration(v: Boolean) { ds.edit { it[booleanPreferencesKey("vibration")] = v } }
    suspend fun setDarkMode(v: Boolean) { ds.edit { it[booleanPreferencesKey("dark_mode")] = v } }
    suspend fun setUserId(v: String) { ds.edit { it[stringPreferencesKey("user_id")] = v } }
    suspend fun setTutorialSeen(v: Boolean) { ds.edit { it[booleanPreferencesKey("tutorial")] = v } }
}
