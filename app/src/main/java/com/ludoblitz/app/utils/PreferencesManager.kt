package com.ludoblitz.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ludo_blitz_prefs")

/**
 * Preferences Manager - Handles persistent storage of game settings
 */
class PreferencesManager(private val context: Context) {

    companion object {
        // Sound settings
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")

        // Game settings
        val DIFFICULTY = stringPreferencesKey("difficulty")
        val TOKENS_PER_PLAYER = intPreferencesKey("tokens_per_player")
        val DOUBLE_TURN_ON_SIX = booleanPreferencesKey("double_turn_on_six")
        val THREE_SIX_BURN = booleanPreferencesKey("three_six_burn")
        val SAFE_ZONES_ENABLED = booleanPreferencesKey("safe_zones_enabled")
        val CAPTURE_BONUS = booleanPreferencesKey("capture_bonus")

        // Theme settings
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
        val SELECTED_AVATAR = intPreferencesKey("selected_avatar")

        // Player stats
        val TOTAL_GAMES_PLAYED = intPreferencesKey("total_games_played")
        val TOTAL_GAMES_WON = intPreferencesKey("total_games_won")
        val TOTAL_CAPTURES = intPreferencesKey("total_captures")
        val BEST_WIN_STREAK = intPreferencesKey("best_win_streak")
        val COINS = intPreferencesKey("coins")
        val GEMS = intPreferencesKey("gems")

        // Daily rewards
        val LAST_REWARD_CLAIM = stringPreferencesKey("last_reward_claim")
        val REWARD_STREAK = intPreferencesKey("reward_streak")
    }

    // Sound enabled
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SOUND_ENABLED] ?: true
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SOUND_ENABLED] = enabled
        }
    }

    // Music enabled
    val musicEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[MUSIC_ENABLED] ?: true
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[MUSIC_ENABLED] = enabled
        }
    }

    // Vibration enabled
    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[VIBRATION_ENABLED] ?: true
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[VIBRATION_ENABLED] = enabled
        }
    }

    // Difficulty
    val difficulty: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[DIFFICULTY] ?: "MEDIUM"
    }

    suspend fun setDifficulty(difficulty: String) {
        context.dataStore.edit { prefs ->
            prefs[DIFFICULTY] = difficulty
        }
    }

    // Tokens per player
    val tokensPerPlayer: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[TOKENS_PER_PLAYER] ?: 4
    }

    suspend fun setTokensPerPlayer(count: Int) {
        context.dataStore.edit { prefs ->
            prefs[TOKENS_PER_PLAYER] = count
        }
    }

    // Double turn on six
    val doubleTurnOnSix: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DOUBLE_TURN_ON_SIX] ?: true
    }

    suspend fun setDoubleTurnOnSix(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DOUBLE_TURN_ON_SIX] = enabled
        }
    }

    // Three six burn
    val threeSixBurn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[THREE_SIX_BURN] ?: true
    }

    suspend fun setThreeSixBurn(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[THREE_SIX_BURN] = enabled
        }
    }

    // Safe zones enabled
    val safeZonesEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SAFE_ZONES_ENABLED] ?: true
    }

    suspend fun setSafeZonesEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SAFE_ZONES_ENABLED] = enabled
        }
    }

    // Capture bonus
    val captureBonus: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[CAPTURE_BONUS] ?: true
    }

    suspend fun setCaptureBonus(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[CAPTURE_BONUS] = enabled
        }
    }

    // Selected theme
    val selectedTheme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_THEME] ?: "royal_gold"
    }

    suspend fun setSelectedTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_THEME] = theme
        }
    }

    // Selected avatar
    val selectedAvatar: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_AVATAR] ?: 0
    }

    suspend fun setSelectedAvatar(avatarId: Int) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_AVATAR] = avatarId
        }
    }

    // Player statistics
    val totalGamesPlayed: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[TOTAL_GAMES_PLAYED] ?: 0
    }

    suspend fun incrementGamesPlayed() {
        context.dataStore.edit { prefs ->
            val current = prefs[TOTAL_GAMES_PLAYED] ?: 0
            prefs[TOTAL_GAMES_PLAYED] = current + 1
        }
    }

    val totalGamesWon: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[TOTAL_GAMES_WON] ?: 0
    }

    suspend fun incrementGamesWon() {
        context.dataStore.edit { prefs ->
            val current = prefs[TOTAL_GAMES_WON] ?: 0
            prefs[TOTAL_GAMES_WON] = current + 1
        }
    }

    val totalCaptures: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[TOTAL_CAPTURES] ?: 0
    }

    suspend fun addCaptures(count: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[TOTAL_CAPTURES] ?: 0
            prefs[TOTAL_CAPTURES] = current + count
        }
    }

    val bestWinStreak: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[BEST_WIN_STREAK] ?: 0
    }

    suspend fun updateBestWinStreak(streak: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[BEST_WIN_STREAK] ?: 0
            if (streak > current) {
                prefs[BEST_WIN_STREAK] = streak
            }
        }
    }

    // Currency
    val coins: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[COINS] ?: 1000
    }

    suspend fun addCoins(amount: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[COINS] ?: 0
            prefs[COINS] = current + amount
        }
    }

    suspend fun spendCoins(amount: Int): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val current = prefs[COINS] ?: 0
            if (current >= amount) {
                prefs[COINS] = current - amount
                success = true
            }
        }
        return success
    }

    val gems: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[GEMS] ?: 50
    }

    suspend fun addGems(amount: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[GEMS] ?: 0
            prefs[GEMS] = current + amount
        }
    }

    // Daily rewards
    val lastRewardClaim: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LAST_REWARD_CLAIM] ?: ""
    }

    suspend fun setLastRewardClaim(date: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_REWARD_CLAIM] = date
        }
    }

    val rewardStreak: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[REWARD_STREAK] ?: 0
    }

    suspend fun setRewardStreak(streak: Int) {
        context.dataStore.edit { prefs ->
            prefs[REWARD_STREAK] = streak
        }
    }
}
