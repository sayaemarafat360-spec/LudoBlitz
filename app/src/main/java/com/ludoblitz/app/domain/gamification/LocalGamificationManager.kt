package com.ludoblitz.app.domain.gamification

import android.content.Context
import com.ludoblitz.app.data.local.PreferenceManager
import com.ludoblitz.app.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * Local Gamification Manager
 * Handles coins, gems, XP, levels, achievements, and daily rewards locally
 * No Firebase required - all data stored locally using DataStore
 */
@Singleton
class LocalGamificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager
) {
    companion object {
        // Level calculation
        const val BASE_XP = 100
        const val XP_MULTIPLIER = 1.5
        
        // Daily rewards (7 day cycle)
        val DAILY_REWARDS = listOf(
            DailyReward(day = 1, coins = 50, gems = 0),
            DailyReward(day = 2, coins = 75, gems = 1),
            DailyReward(day = 3, coins = 100, gems = 2),
            DailyReward(day = 4, coins = 150, gems = 3),
            DailyReward(day = 5, coins = 200, gems = 4),
            DailyReward(day = 6, coins = 300, gems = 5),
            DailyReward(day = 7, coins = 500, gems = 10, isSpecial = true)
        )
        
        // Spin wheel rewards
        val SPIN_REWARDS = listOf(
            SpinReward.Coins(50),
            SpinReward.Coins(100),
            SpinReward.Coins(200),
            SpinReward.Coins(500),
            SpinReward.Gems(1),
            SpinReward.Gems(3),
            SpinReward.Gems(5),
            SpinReward.Jackpot(1000, 10)
        )
        
        // Achievements
        val ACHIEVEMENTS = listOf(
            Achievement(
                id = "first_game",
                title = "First Steps",
                description = "Play your first game",
                iconRes = "ic_dice",
                coinReward = 100,
                xpReward = 50,
                type = AchievementType.GAMES_PLAYED,
                target = 1
            ),
            Achievement(
                id = "first_win",
                title = "Winner!",
                description = "Win your first game",
                iconRes = "ic_coin",
                coinReward = 200,
                xpReward = 100,
                type = AchievementType.GAMES_WON,
                target = 1
            ),
            Achievement(
                id = "veteran",
                title = "Veteran Player",
                description = "Play 100 games",
                iconRes = "ic_stats",
                coinReward = 500,
                xpReward = 200,
                type = AchievementType.GAMES_PLAYED,
                target = 100
            ),
            Achievement(
                id = "champion",
                title = "Champion",
                description = "Win 50 games",
                iconRes = "ic_gem",
                coinReward = 1000,
                xpReward = 500,
                type = AchievementType.GAMES_WON,
                target = 50
            ),
            Achievement(
                id = "six_master",
                title = "Six Master",
                description = "Roll 100 sixes",
                iconRes = "ic_dice",
                coinReward = 300,
                xpReward = 150,
                type = AchievementType.SIXES_ROLLED,
                target = 100
            ),
            Achievement(
                id = "hunter",
                title = "Token Hunter",
                description = "Capture 100 opponent tokens",
                iconRes = "ic_coin",
                coinReward = 400,
                xpReward = 200,
                type = AchievementType.TOKENS_CAPTURED,
                target = 100
            ),
            Achievement(
                id = "survivor",
                title = "Survivor",
                description = "Have all 4 tokens home in a game",
                iconRes = "ic_home",
                coinReward = 500,
                xpReward = 250,
                type = AchievementType.TOKENS_HOME,
                target = 4
            ),
            Achievement(
                id = "streak_5",
                title = "Hot Streak",
                description = "Win 5 games in a row",
                iconRes = "ic_stats",
                coinReward = 600,
                xpReward = 300,
                type = AchievementType.WIN_STREAK,
                target = 5
            ),
            Achievement(
                id = "streak_10",
                title = "Unstoppable",
                description = "Win 10 games in a row",
                iconRes = "ic_gem",
                coinReward = 1500,
                xpReward = 750,
                type = AchievementType.WIN_STREAK,
                target = 10
            ),
            Achievement(
                id = "daily_warrior",
                title = "Daily Warrior",
                description = "Claim daily reward 7 days in a row",
                iconRes = "ic_gift",
                coinReward = 700,
                xpReward = 350,
                type = AchievementType.DAILY_STREAK,
                target = 7
            ),
            Achievement(
                id = "level_10",
                title = "Rising Star",
                description = "Reach Level 10",
                iconRes = "ic_stats",
                coinReward = 500,
                xpReward = 0,
                type = AchievementType.LEVEL,
                target = 10
            ),
            Achievement(
                id = "level_50",
                title = "Ludo Legend",
                description = "Reach Level 50",
                iconRes = "ic_gem",
                coinReward = 5000,
                xpReward = 0,
                type = AchievementType.LEVEL,
                target = 50
            ),
            Achievement(
                id = "collector",
                title = "Collector",
                description = "Unlock 5 board themes",
                iconRes = "ic_shop",
                coinReward = 300,
                xpReward = 150,
                type = AchievementType.ITEMS_OWNED,
                target = 5
            ),
            Achievement(
                id = "millionaire",
                title = "Millionaire",
                description = "Earn 1,000,000 total coins",
                iconRes = "ic_coin",
                coinReward = 5000,
                xpReward = 2000,
                type = AchievementType.TOTAL_COINS,
                target = 1000000
            )
        )
    }
    
    // User progress flow
    val userProgress: Flow<UserProgress> = preferenceManager.userProgress
    
    /**
     * Get current user progress synchronously
     */
    fun getCurrentProgress(): UserProgress = runBlocking {
        userProgress.first()
    }
    
    /**
     * Calculate level from XP
     */
    fun calculateLevel(xp: Long): Int {
        var level = 1
        var xpNeeded = BASE_XP.toLong()
        var totalXp = 0L
        
        while (totalXp + xpNeeded <= xp) {
            totalXp += xpNeeded
            level++
            xpNeeded = (BASE_XP * XP_MULTIPLIER.pow(level - 1).toDouble()).toLong()
        }
        
        return level
    }
    
    /**
     * Get XP required for next level
     */
    fun getXpForNextLevel(currentLevel: Int): Long {
        return (BASE_XP * XP_MULTIPLIER.pow(currentLevel - 1).toDouble()).toLong()
    }
    
    /**
     * Add coins to user balance
     */
    suspend fun addCoins(amount: Long, source: String = "game") {
        preferenceManager.updateUserProgress { progress ->
            progress.copy(
                coins = progress.coins + amount,
                totalCoinsEarned = progress.totalCoinsEarned + amount
            )
        }
        
        // Check achievements
        checkAchievements()
    }
    
    /**
     * Spend coins
     */
    suspend fun spendCoins(amount: Long): Boolean {
        val current = getCurrentProgress()
        if (current.coins < amount) return false
        
        preferenceManager.updateUserProgress { progress ->
            progress.copy(coins = progress.coins - amount)
        }
        return true
    }
    
    /**
     * Add gems to user balance
     */
    suspend fun addGems(amount: Int) {
        preferenceManager.updateUserProgress { progress ->
            progress.copy(gems = progress.gems + amount)
        }
    }
    
    /**
     * Spend gems
     */
    suspend fun spendGems(amount: Int): Boolean {
        val current = getCurrentProgress()
        if (current.gems < amount) return false
        
        preferenceManager.updateUserProgress { progress ->
            progress.copy(gems = progress.gems - amount)
        }
        return true
    }
    
    /**
     * Add XP and check for level up
     */
    suspend fun addXp(amount: Int): LevelUpResult? {
        val current = getCurrentProgress()
        val newXp = current.xp + amount
        val newLevel = calculateLevel(newXp.toLong())
        
        preferenceManager.updateUserProgress { progress ->
            progress.copy(
                xp = newXp,
                level = newLevel
            )
        }
        
        // Check for level up
        return if (newLevel > current.level) {
            val rewards = calculateLevelUpRewards(newLevel)
            LevelUpResult(
                newLevel = newLevel,
                previousLevel = current.level,
                coinReward = rewards.first,
                gemReward = rewards.second
            ).also {
                addCoins(rewards.first.toLong(), "level_up")
                addGems(rewards.second)
            }
        } else null
    }
    
    /**
     * Calculate rewards for level up
     */
    private fun calculateLevelUpRewards(newLevel: Int): Pair<Int, Int> {
        val coins = 50 + (newLevel * 10)
        val gems = if (newLevel % 5 == 0) newLevel / 5 else 0
        return Pair(coins, gems)
    }
    
    /**
     * Record game played
     */
    suspend fun recordGamePlayed(isWin: Boolean, position: Int, coinsEarned: Long, xpEarned: Int) {
        preferenceManager.updateUserProgress { progress ->
            val newWinStreak = if (isWin) progress.currentWinStreak + 1 else 0
            val newBestStreak = maxOf(progress.bestWinStreak, newWinStreak)
            
            progress.copy(
                gamesPlayed = progress.gamesPlayed + 1,
                gamesWon = progress.gamesWon + if (isWin) 1 else 0,
                currentWinStreak = newWinStreak,
                bestWinStreak = newBestStreak,
                coins = progress.coins + coinsEarned,
                xp = progress.xp + xpEarned,
                totalCoinsEarned = progress.totalCoinsEarned + coinsEarned
            )
        }
        
        addXp(xpEarned)
        checkAchievements()
    }
    
    /**
     * Record six rolled
     */
    suspend fun recordSixRolled() {
        preferenceManager.updateUserProgress { progress ->
            progress.copy(sixesRolled = progress.sixesRolled + 1)
        }
        checkAchievements()
    }
    
    /**
     * Record token captured
     */
    suspend fun recordTokenCaptured() {
        preferenceManager.updateUserProgress { progress ->
            progress.copy(tokensCaptured = progress.tokensCaptured + 1)
        }
        checkAchievements()
    }
    
    /**
     * Record token home
     */
    suspend fun recordTokenHome() {
        preferenceManager.updateUserProgress { progress ->
            progress.copy(tokensHome = progress.tokensHome + 1)
        }
        checkAchievements()
    }
    
    /**
     * Get daily reward status
     */
    fun getDailyRewardStatus(): DailyRewardStatus {
        val progress = getCurrentProgress()
        val now = System.currentTimeMillis()
        
        val lastClaim = progress.lastDailyRewardClaim
        val dayDiff = ((now - lastClaim) / (24 * 60 * 60 * 1000)).toInt()
        
        val canClaim = dayDiff >= 1
        val currentStreak = if (dayDiff <= 1) progress.dailyRewardStreak else 0
        val nextRewardDay = if (canClaim) (currentStreak % 7) + 1 else (currentStreak % 7)
        
        return DailyRewardStatus(
            canClaim = canClaim,
            currentStreak = currentStreak,
            nextReward = DAILY_REWARDS.getOrElse(nextRewardDay) { DAILY_REWARDS[0] },
            timeUntilNextClaim = if (!canClaim) {
                (24 * 60 * 60 * 1000) - (now - lastClaim)
            } else 0
        )
    }
    
    /**
     * Claim daily reward
     */
    suspend fun claimDailyReward(): DailyRewardResult {
        val status = getDailyRewardStatus()
        if (!status.canClaim) {
            return DailyRewardResult(
                success = false,
                coins = 0,
                gems = 0,
                streakBroken = false
            )
        }
        
        val newStreak = status.currentStreak + 1
        val reward = DAILY_REWARDS.getOrElse((newStreak - 1) % 7) { DAILY_REWARDS[0] }
        
        // Bonus for 7-day streak
        val bonusMultiplier = if (newStreak % 7 == 0) 2 else 1
        
        preferenceManager.updateUserProgress { progress ->
            progress.copy(
                coins = progress.coins + (reward.coins * bonusMultiplier),
                gems = progress.gems + (reward.gems * bonusMultiplier),
                dailyRewardStreak = newStreak,
                lastDailyRewardClaim = System.currentTimeMillis(),
                totalCoinsEarned = progress.totalCoinsEarned + (reward.coins * bonusMultiplier)
            )
        }
        
        return DailyRewardResult(
            success = true,
            coins = reward.coins * bonusMultiplier,
            gems = reward.gems * bonusMultiplier,
            streakBroken = false,
            dayNumber = newStreak
        )
    }
    
    /**
     * Spin the wheel
     */
    suspend fun spinWheel(isFree: Boolean): SpinResult {
        if (!isFree) {
            val canSpin = spendGems(5)
            if (!canSpin) {
                return SpinResult(success = false, reward = null)
            }
        }
        
        // Weighted random selection
        val weights = listOf(30, 25, 20, 10, 5, 4, 3, 3) // Percentages
        val random = (1..100).random()
        var cumulative = 0
        
        for ((index, weight) in weights.withIndex()) {
            cumulative += weight
            if (random <= cumulative) {
                val reward = SPIN_REWARDS[index]
                
                when (reward) {
                    is SpinReward.Coins -> addCoins(reward.amount.toLong(), "spin_wheel")
                    is SpinReward.Gems -> addGems(reward.amount)
                    is SpinReward.Jackpot -> {
                        addCoins(reward.coins.toLong(), "spin_wheel")
                        addGems(reward.gems)
                    }
                }
                
                // Update last spin time
                preferenceManager.updateUserProgress { progress ->
                    progress.copy(lastSpinTime = System.currentTimeMillis())
                }
                
                return SpinResult(success = true, reward = reward)
            }
        }
        
        return SpinResult(success = false, reward = null)
    }
    
    /**
     * Check if free spin is available
     */
    fun isFreeSpinAvailable(): Boolean {
        val progress = getCurrentProgress()
        val lastSpin = progress.lastSpinTime
        val now = System.currentTimeMillis()
        
        // Free spin every 4 hours
        return (now - lastSpin) >= (4 * 60 * 60 * 1000)
    }
    
    /**
     * Check and unlock achievements
     */
    private suspend fun checkAchievements() {
        val progress = getCurrentProgress()
        val unlockedIds = progress.unlockedAchievements
        
        ACHIEVEMENTS.forEach { achievement ->
            if (achievement.id !in unlockedIds) {
                val unlocked = when (achievement.type) {
                    AchievementType.GAMES_PLAYED -> progress.gamesPlayed >= achievement.target
                    AchievementType.GAMES_WON -> progress.gamesWon >= achievement.target
                    AchievementType.WIN_STREAK -> progress.bestWinStreak >= achievement.target
                    AchievementType.SIXES_ROLLED -> progress.sixesRolled >= achievement.target
                    AchievementType.TOKENS_CAPTURED -> progress.tokensCaptured >= achievement.target
                    AchievementType.TOKENS_HOME -> progress.tokensHome >= achievement.target
                    AchievementType.DAILY_STREAK -> progress.dailyRewardStreak >= achievement.target
                    AchievementType.LEVEL -> progress.level >= achievement.target
                    AchievementType.TOTAL_COINS -> progress.totalCoinsEarned >= achievement.target
                    AchievementType.ITEMS_OWNED -> progress.ownedItems.size >= achievement.target
                }
                
                if (unlocked) {
                    unlockAchievement(achievement)
                }
            }
        }
    }
    
    /**
     * Unlock an achievement
     */
    private suspend fun unlockAchievement(achievement: Achievement) {
        preferenceManager.updateUserProgress { progress ->
            progress.copy(
                unlockedAchievements = progress.unlockedAchievements + achievement.id,
                coins = progress.coins + achievement.coinReward,
                xp = progress.xp + achievement.xpReward,
                totalCoinsEarned = progress.totalCoinsEarned + achievement.coinReward
            )
        }
        
        // Recalculate level after XP gain
        addXp(achievement.xpReward)
    }
    
    /**
     * Get all achievements with unlock status
     */
    fun getAchievementsWithStatus(): List<AchievementStatus> {
        val progress = getCurrentProgress()
        
        return ACHIEVEMENTS.map { achievement ->
            val isUnlocked = achievement.id in progress.unlockedAchievements
            val currentProgress = when (achievement.type) {
                AchievementType.GAMES_PLAYED -> progress.gamesPlayed
                AchievementType.GAMES_WON -> progress.gamesWon
                AchievementType.WIN_STREAK -> progress.bestWinStreak
                AchievementType.SIXES_ROLLED -> progress.sixesRolled
                AchievementType.TOKENS_CAPTURED -> progress.tokensCaptured
                AchievementType.TOKENS_HOME -> progress.tokensHome
                AchievementType.DAILY_STREAK -> progress.dailyRewardStreak
                AchievementType.LEVEL -> progress.level
                AchievementType.TOTAL_COINS -> progress.totalCoinsEarned.toInt()
                AchievementType.ITEMS_OWNED -> progress.ownedItems.size
            }
            
            AchievementStatus(
                achievement = achievement,
                isUnlocked = isUnlocked,
                currentProgress = currentProgress,
                targetProgress = achievement.target
            )
        }
    }
}

// Data classes for gamification
data class UserProgress(
    val coins: Long = 500,
    val gems: Int = 5,
    val xp: Int = 0,
    val level: Int = 1,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val currentWinStreak: Int = 0,
    val bestWinStreak: Int = 0,
    val sixesRolled: Int = 0,
    val tokensCaptured: Int = 0,
    val tokensHome: Int = 0,
    val dailyRewardStreak: Int = 0,
    val lastDailyRewardClaim: Long = 0,
    val lastSpinTime: Long = 0,
    val totalCoinsEarned: Long = 0,
    val unlockedAchievements: Set<String> = emptySet(),
    val ownedItems: Set<String> = emptySet(),
    val equippedBoard: String = "classic",
    val equippedTokens: String = "default",
    val equippedDice: String = "default"
)

data class DailyReward(
    val day: Int,
    val coins: Int,
    val gems: Int,
    val isSpecial: Boolean = false
)

data class DailyRewardStatus(
    val canClaim: Boolean,
    val currentStreak: Int,
    val nextReward: DailyReward,
    val timeUntilNextClaim: Long
)

data class DailyRewardResult(
    val success: Boolean,
    val coins: Int,
    val gems: Int,
    val streakBroken: Boolean,
    val dayNumber: Int = 0
)

sealed class SpinReward {
    data class Coins(val amount: Int) : SpinReward()
    data class Gems(val amount: Int) : SpinReward()
    data class Jackpot(val coins: Int, val gems: Int) : SpinReward()
}

data class SpinResult(
    val success: Boolean,
    val reward: SpinReward?
)

data class LevelUpResult(
    val newLevel: Int,
    val previousLevel: Int,
    val coinReward: Int,
    val gemReward: Int
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: String,
    val coinReward: Long,
    val xpReward: Int,
    val type: AchievementType,
    val target: Int
)

enum class AchievementType {
    GAMES_PLAYED,
    GAMES_WON,
    WIN_STREAK,
    SIXES_ROLLED,
    TOKENS_CAPTURED,
    TOKENS_HOME,
    DAILY_STREAK,
    LEVEL,
    TOTAL_COINS,
    ITEMS_OWNED
}

data class AchievementStatus(
    val achievement: Achievement,
    val isUnlocked: Boolean,
    val currentProgress: Int,
    val targetProgress: Int
) {
    val progressPercent: Float
        get() = (currentProgress.toFloat() / targetProgress.toFloat()).coerceAtMost(1f)
}
