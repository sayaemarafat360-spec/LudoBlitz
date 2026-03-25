package com.ludoblitz.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ludoblitz.app.data.model.User
import com.ludoblitz.app.data.model.Achievement
import com.ludoblitz.app.data.model.Reward
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ludo_blitz_user")

/**
 * Local User Repository - Manages user data locally without Firebase
 * Uses DataStore for persistent storage
 */
@Singleton
class LocalUserRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val AVATAR_ID = stringPreferencesKey("avatar_id")
        val COINS = longPreferencesKey("coins")
        val GEMS = longPreferencesKey("gems")
        val XP = longPreferencesKey("xp")
        val LEVEL = intPreferencesKey("level")
        val TOTAL_GAMES = intPreferencesKey("total_games")
        val TOTAL_WINS = intPreferencesKey("total_wins")
        val WIN_STREAK = intPreferencesKey("win_streak")
        val MAX_WIN_STREAK = intPreferencesKey("max_win_streak")
        val RATING = intPreferencesKey("rating")
        val SELECTED_BOARD = stringPreferencesKey("selected_board")
        val SELECTED_TOKEN = stringPreferencesKey("selected_token")
        val SELECTED_DICE = stringPreferencesKey("selected_dice")
        val UNLOCKED_BOARDS = stringSetPreferencesKey("unlocked_boards")
        val UNLOCKED_TOKENS = stringSetPreferencesKey("unlocked_tokens")
        val UNLOCKED_DICE = stringSetPreferencesKey("unlocked_dice")
        val ACHIEVEMENTS = stringSetPreferencesKey("achievements")
        val LAST_DAILY_REWARD = longPreferencesKey("last_daily_reward")
        val DAILY_REWARD_DAY = intPreferencesKey("daily_reward_day")
        val LAST_SPIN_TIME = longPreferencesKey("last_spin_time")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val GAMES_PLAYED_TODAY = intPreferencesKey("games_played_today")
        val LAST_PLAY_DATE = longPreferencesKey("last_play_date")
        val TOTAL_CAPTURES = intPreferencesKey("total_captures")
        val TOTAL_SIXES = intPreferencesKey("total_sixes")
        val TOKENS_HOME = intPreferencesKey("tokens_home")
    }

    /**
     * Get current user data
     */
    fun getUser(): Flow<User> = context.dataStore.data.map { prefs ->
        User(
            id = prefs[Keys.USER_ID] ?: generateUserId(),
            displayName = prefs[Keys.DISPLAY_NAME] ?: "Player",
            avatarUrl = prefs[Keys.AVATAR_ID] ?: "avatar_1",
            coins = prefs[Keys.COINS] ?: 1000L,
            gems = prefs[Keys.GEMS] ?: 10L,
            xp = prefs[Keys.XP] ?: 0L,
            level = prefs[Keys.LEVEL] ?: 1,
            totalWins = prefs[Keys.TOTAL_WINS] ?: 0,
            totalGames = prefs[Keys.TOTAL_GAMES] ?: 0,
            winStreak = prefs[Keys.WIN_STREAK] ?: 0,
            maxWinStreak = prefs[Keys.MAX_WIN_STREAK] ?: 0,
            rating = prefs[Keys.RATING] ?: 1000,
            selectedBoardTheme = prefs[Keys.SELECTED_BOARD] ?: "classic",
            selectedTokenStyle = prefs[Keys.SELECTED_TOKEN] ?: "default",
            selectedDiceStyle = prefs[Keys.SELECTED_DICE] ?: "default",
            unlockedBoards = prefs[Keys.UNLOCKED_BOARDS]?.toList() ?: listOf("classic"),
            unlockedTokens = prefs[Keys.UNLOCKED_TOKENS]?.toList() ?: listOf("default"),
            unlockedDice = prefs[Keys.UNLOCKED_DICE]?.toList() ?: listOf("default"),
            achievements = prefs[Keys.ACHIEVEMENTS]?.toList() ?: emptyList(),
            isPremium = prefs[Keys.IS_PREMIUM] ?: false,
            lastLoginDate = prefs[Keys.LAST_DAILY_REWARD] ?: System.currentTimeMillis(),
            dailyRewardDay = prefs[Keys.DAILY_REWARD_DAY] ?: 0
        )
    }

    /**
     * Update user data
     */
    suspend fun updateUser(user: User) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_ID] = user.id
            prefs[Keys.DISPLAY_NAME] = user.displayName
            prefs[Keys.AVATAR_ID] = user.avatarUrl
            prefs[Keys.COINS] = user.coins
            prefs[Keys.GEMS] = user.gems
            prefs[Keys.XP] = user.xp
            prefs[Keys.LEVEL] = user.level
            prefs[Keys.TOTAL_GAMES] = user.totalGames
            prefs[Keys.TOTAL_WINS] = user.totalWins
            prefs[Keys.WIN_STREAK] = user.winStreak
            prefs[Keys.MAX_WIN_STREAK] = user.maxWinStreak
            prefs[Keys.RATING] = user.rating
            prefs[Keys.SELECTED_BOARD] = user.selectedBoardTheme
            prefs[Keys.SELECTED_TOKEN] = user.selectedTokenStyle
            prefs[Keys.SELECTED_DICE] = user.selectedDiceStyle
            prefs[Keys.UNLOCKED_BOARDS] = user.unlockedBoards.toSet()
            prefs[Keys.UNLOCKED_TOKENS] = user.unlockedTokens.toSet()
            prefs[Keys.UNLOCKED_DICE] = user.unlockedDice.toSet()
            prefs[Keys.ACHIEVEMENTS] = user.achievements.toSet()
            prefs[Keys.IS_PREMIUM] = user.isPremium
            prefs[Keys.LAST_DAILY_REWARD] = user.lastLoginDate
            prefs[Keys.DAILY_REWARD_DAY] = user.dailyRewardDay
        }
    }

    /**
     * Add coins to user
     */
    suspend fun addCoins(amount: Long) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.COINS] ?: 1000L
            prefs[Keys.COINS] = current + amount
        }
    }

    /**
     * Remove coins from user
     */
    suspend fun removeCoins(amount: Long): Boolean {
        val current = getCoins()
        return if (current >= amount) {
            context.dataStore.edit { prefs ->
                prefs[Keys.COINS] = current - amount
            }
            true
        } else {
            false
        }
    }

    /**
     * Get current coins
     */
    suspend fun getCoins(): Long {
        return context.dataStore.data.map { it[Keys.COINS] ?: 1000L }.first()
    }

    /**
     * Add gems to user
     */
    suspend fun addGems(amount: Long) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.GEMS] ?: 10L
            prefs[Keys.GEMS] = current + amount
        }
    }

    /**
     * Add XP and handle leveling
     */
    suspend fun addXp(amount: Long): Boolean {
        var leveledUp = false
        context.dataStore.edit { prefs ->
            val currentXp = prefs[Keys.XP] ?: 0L
            val currentLevel = prefs[Keys.LEVEL] ?: 1
            var newXp = currentXp + amount
            var newLevel = currentLevel
            
            // Check for level up
            while (newXp >= getXpForLevel(newLevel)) {
                newXp -= getXpForLevel(newLevel)
                newLevel++
                leveledUp = true
            }
            
            prefs[Keys.XP] = newXp
            prefs[Keys.LEVEL] = newLevel
        }
        return leveledUp
    }

    private fun getXpForLevel(level: Int): Long {
        return (level * 1000L).coerceAtLeast(500L)
    }

    /**
     * Record a game played
     */
    suspend fun recordGamePlayed(won: Boolean) {
        context.dataStore.edit { prefs ->
            val totalGames = (prefs[Keys.TOTAL_GAMES] ?: 0) + 1
            prefs[Keys.TOTAL_GAMES] = totalGames
            
            if (won) {
                val totalWins = (prefs[Keys.TOTAL_WINS] ?: 0) + 1
                prefs[Keys.TOTAL_WINS] = totalWins
                
                val winStreak = (prefs[Keys.WIN_STREAK] ?: 0) + 1
                prefs[Keys.WIN_STREAK] = winStreak
                
                val maxStreak = prefs[Keys.MAX_WIN_STREAK] ?: 0
                if (winStreak > maxStreak) {
                    prefs[Keys.MAX_WIN_STREAK] = winStreak
                }
                
                // Update rating
                val rating = prefs[Keys.RATING] ?: 1000
                prefs[Keys.RATING] = rating + 25
            } else {
                prefs[Keys.WIN_STREAK] = 0
                val rating = prefs[Keys.RATING] ?: 1000
                prefs[Keys.RATING] = (rating - 15).coerceAtLeast(0)
            }
        }
    }

    /**
     * Record stats
     */
    suspend fun recordCapture() {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOTAL_CAPTURES] = (prefs[Keys.TOTAL_CAPTURES] ?: 0) + 1
        }
    }

    suspend fun recordSix() {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOTAL_SIXES] = (prefs[Keys.TOTAL_SIXES] ?: 0) + 1
        }
    }

    suspend fun recordTokenHome() {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKENS_HOME] = (prefs[Keys.TOKENS_HOME] ?: 0) + 1
        }
    }

    /**
     * Claim daily reward
     */
    suspend fun claimDailyReward(): Reward {
        val currentDay: Int
        context.dataStore.edit { prefs ->
            val lastClaim = prefs[Keys.LAST_DAILY_REWARD] ?: 0L
            val day = prefs[Keys.DAILY_REWARD_DAY] ?: 0
            val now = System.currentTimeMillis()
            
            // Check if it's a new day
            if (now - lastClaim >= 24 * 60 * 60 * 1000) {
                currentDay = if (now - lastClaim >= 48 * 60 * 60 * 1000) {
                    1 // Missed a day, reset
                } else {
                    (day % 7) + 1
                }
            } else {
                currentDay = day
            }
            
            prefs[Keys.DAILY_REWARD_DAY] = currentDay
            prefs[Keys.LAST_DAILY_REWARD] = now
        }
        
        val reward = getRewardForDay(context.dataStore.data.map { it[Keys.DAILY_REWARD_DAY] ?: 1 }.first())
        
        // Apply reward
        addCoins(reward.coins)
        addGems(reward.gems)
        addXp(reward.xp)
        
        return reward
    }

    private fun getRewardForDay(day: Int): Reward {
        return when (day) {
            1 -> Reward(coins = 100, xp = 50)
            2 -> Reward(coins = 150, xp = 75)
            3 -> Reward(coins = 200, gems = 1, xp = 100)
            4 -> Reward(coins = 250, xp = 125)
            5 -> Reward(coins = 300, gems = 2, xp = 150)
            6 -> Reward(coins = 400, xp = 200)
            7 -> Reward(coins = 500, gems = 5, xp = 300)
            else -> Reward(coins = 100, xp = 50)
        }
    }

    /**
     * Check if can claim daily reward
     */
    suspend fun canClaimDailyReward(): Boolean {
        val lastClaim = context.dataStore.data.map { it[Keys.LAST_DAILY_REWARD] ?: 0L }.first()
        val now = System.currentTimeMillis()
        return (now - lastClaim) >= 24 * 60 * 60 * 1000
    }

    /**
     * Check if can spin wheel
     */
    suspend fun canSpinWheel(): Boolean {
        val lastSpin = context.dataStore.data.map { it[Keys.LAST_SPIN_TIME] ?: 0L }.first()
        val now = System.currentTimeMillis()
        return (now - lastSpin) >= 8 * 60 * 60 * 1000 // 8 hours
    }

    /**
     * Record spin wheel usage
     */
    suspend fun recordSpin() {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_SPIN_TIME] = System.currentTimeMillis()
        }
    }

    /**
     * Unlock item
     */
    suspend fun unlockBoard(boardId: String) {
        context.dataStore.edit { prefs ->
            val unlocked = prefs[Keys.UNLOCKED_BOARDS]?.toMutableSet() ?: mutableSetOf("classic")
            unlocked.add(boardId)
            prefs[Keys.UNLOCKED_BOARDS] = unlocked
        }
    }

    suspend fun unlockToken(tokenId: String) {
        context.dataStore.edit { prefs ->
            val unlocked = prefs[Keys.UNLOCKED_TOKENS]?.toMutableSet() ?: mutableSetOf("default")
            unlocked.add(tokenId)
            prefs[Keys.UNLOCKED_TOKENS] = unlocked
        }
    }

    suspend fun unlockDice(diceId: String) {
        context.dataStore.edit { prefs ->
            val unlocked = prefs[Keys.UNLOCKED_DICE]?.toMutableSet() ?: mutableSetOf("default")
            unlocked.add(diceId)
            prefs[Keys.UNLOCKED_DICE] = unlocked
        }
    }

    /**
     * Select item
     */
    suspend fun selectBoard(boardId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_BOARD] = boardId
        }
    }

    suspend fun selectToken(tokenId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_TOKEN] = tokenId
        }
    }

    suspend fun selectDice(diceId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_DICE] = diceId
        }
    }

    /**
     * Unlock achievement
     */
    suspend fun unlockAchievement(achievementId: String): Boolean {
        var isNew = false
        context.dataStore.edit { prefs ->
            val achievements = prefs[Keys.ACHIEVEMENTS]?.toMutableSet() ?: mutableSetOf()
            if (!achievements.contains(achievementId)) {
                achievements.add(achievementId)
                prefs[Keys.ACHIEVEMENTS] = achievements
                isNew = true
            }
        }
        return isNew
    }

    /**
     * Set premium status
     */
    suspend fun setPremium(isPremium: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_PREMIUM] = isPremium
        }
    }

    /**
     * Update display name
     */
    suspend fun updateDisplayName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DISPLAY_NAME] = name
        }
    }

    /**
     * Update avatar
     */
    suspend fun updateAvatar(avatarId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AVATAR_ID] = avatarId
        }
    }

    /**
     * Generate unique user ID
     */
    private fun generateUserId(): String {
        return "local_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    /**
     * Initialize new user if not exists
     */
    suspend fun initializeUser() {
        val userId = context.dataStore.data.map { it[Keys.USER_ID] }.first()
        if (userId == null) {
            context.dataStore.edit { prefs ->
                prefs[Keys.USER_ID] = generateUserId()
                prefs[Keys.DISPLAY_NAME] = "Player_${(1000..9999).random()}"
                prefs[Keys.COINS] = 1000L
                prefs[Keys.GEMS] = 10L
                prefs[Keys.LEVEL] = 1
                prefs[Keys.RATING] = 1000
                prefs[Keys.UNLOCKED_BOARDS] = setOf("classic")
                prefs[Keys.UNLOCKED_TOKENS] = setOf("default")
                prefs[Keys.UNLOCKED_DICE] = setOf("default")
            }
        }
    }

    /**
     * Clear all user data (for reset)
     */
    suspend fun clearUserData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
