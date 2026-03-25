package com.ludoblitz.app.data.firebase

import com.ludoblitz.app.data.model.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Remote Config Manager
 * Controls the entire app dynamically from Firebase Console
 * No hardcoding - all values fetched from Firebase
 */
@Singleton
class FirebaseConfigManager @Inject constructor() {
    
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    
    companion object {
        // Remote Config Keys - All app configurations
        const val KEY_APP_VERSION = "app_version"
        const val KEY_FORCE_UPDATE = "force_update"
        const val KEY_UPDATE_URL = "update_url"
        const val KEY_MAINTENANCE_MODE = "maintenance_mode"
        const val KEY_MAINTENANCE_MESSAGE = "maintenance_message"
        
        // Game Configuration
        const val KEY_DICE_ANIMATION_SPEED = "dice_animation_speed"
        const val KEY_TOKEN_MOVE_SPEED = "token_move_speed"
        const val KEY_TURN_TIMEOUT_SECONDS = "turn_timeout_seconds"
        const val KEY_AUTO_SKIP_ENABLED = "auto_skip_enabled"
        const val KEY_MAX_CONSECUTIVE_SIXES = "max_consecutive_sixes"
        
        // Rewards Configuration
        const val KEY_COINS_PER_WIN = "coins_per_win"
        const val KEY_COINS_PER_CAPTURE = "coins_per_capture"
        const val KEY_COINS_PER_SIX = "coins_per_six"
        const val KEY_COINS_PER_TOKEN_HOME = "coins_per_token_home"
        const val KEY_XP_PER_WIN = "xp_per_win"
        const val KEY_XP_PER_CAPTURE = "xp_per_capture"
        const val KEY_XP_PER_TOKEN_HOME = "xp_per_token_home"
        const val KEY_DAILY_REWARD_COINS = "daily_reward_coins"
        const val KEY_DAILY_REWARD_GEMS = "daily_reward_gems"
        const val KEY_SPIN_WHEEL_COST = "spin_wheel_cost"
        const val KEY_FREE_SPIN_INTERVAL_HOURS = "free_spin_interval_hours"
        
        // Ad Configuration
        const val KEY_INTERSTITIAL_AD_UNIT_ID = "interstitial_ad_unit_id"
        const val KEY_REWARDED_AD_UNIT_ID = "rewarded_ad_unit_id"
        const val KEY_BANNER_AD_UNIT_ID = "banner_ad_unit_id"
        const val KEY_AD_SHOW_INTERVAL_GAMES = "ad_show_interval_games"
        const val KEY_ADS_ENABLED = "ads_enabled"
        const val KEY_AD_FREE_COINS_REWARD = "ad_free_coins_reward"
        
        // IAP Configuration
        const val KEY_REMOVE_ADS_PRICE = "remove_ads_price"
        const val KEY_COIN_PACKS = "coin_packs"
        const val KEY_GEM_PACKS = "gem_packs"
        
        // Feature Flags
        const val KEY_ONLINE_MODE_ENABLED = "online_mode_enabled"
        const val KEY_TOURNAMENTS_ENABLED = "tournaments_enabled"
        const val KEY_FRIENDS_SYSTEM_ENABLED = "friends_system_enabled"
        const val KEY_CHAT_ENABLED = "chat_enabled"
        const val KEY_LEADERBOARD_ENABLED = "leaderboard_enabled"
        const val KEY_ACHIEVEMENTS_ENABLED = "achievements_enabled"
        
        // Game Rules (Server-controlled)
        const val KEY_REQUIRE_SIX_TO_RELEASE = "require_six_to_release"
        const val KEY_THREE_SIXES_RULE = "three_sixes_rule"
        const val KEY_CAPTURE_GIVES_BONUS = "capture_gives_bonus"
        
        // AI Configuration
        const val KEY_AI_THINKING_DELAY_MS = "ai_thinking_delay_ms"
        const val KEY_AI_EASY_MISTAKE_CHANCE = "ai_easy_mistake_chance"
        const val KEY_AI_EXPERT_LOOKAHEAD = "ai_expert_lookahead"
        
        // Level Configuration
        const val KEY_BASE_XP = "base_xp"
        const val KEY_XP_MULTIPLIER = "xp_multiplier"
        const val KEY_LEVEL_UP_COIN_REWARD = "level_up_coin_reward"
        
        // Tutorial
        const val KEY_TUTORIAL_ENABLED = "tutorial_enabled"
        
        // Social Features
        const val KEY_MAX_FRIENDS = "max_friends"
        const val KEY_FRIEND_REQUEST_COOLDOWN = "friend_request_cooldown"
        const val KEY_MAX_GAME_INVITES_PER_DAY = "max_game_invites_per_day"
        
        // Rate & Share
        const val KEY_RATE_APP_AFTER_GAMES = "rate_app_after_games"
        const val KEY_SHARE_TEXT = "share_text"
        const val KEY_SHARE_LINK = "share_link"
        
        // Notifications
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled_default"
        const val KEY_DAILY_REWARD_REMINDER_HOUR = "daily_reward_reminder_hour"
    }
    
    // Cached configuration
    private var configCache: AppRemoteConfig? = null
    
    /**
     * Initialize and fetch remote config
     */
    suspend fun initialize(): Boolean {
        return try {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // 1 hour cache
                .build()
            
            remoteConfig.setConfigSettingsAsync(configSettings)
            setDefaults()
            
            // Fetch and activate
            remoteConfig.fetchAndActivate().await()
            
            // Cache the config
            configCache = buildConfig()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Use defaults on failure
            configCache = buildConfig()
            false
        }
    }
    
    /**
     * Set default values (fallback)
     */
    private fun setDefaults() {
        val defaults = mapOf(
            // App
            KEY_APP_VERSION to "1.0.0",
            KEY_FORCE_UPDATE to false,
            KEY_UPDATE_URL to "",
            KEY_MAINTENANCE_MODE to false,
            KEY_MAINTENANCE_MESSAGE to "Server maintenance. Please try again later.",
            
            // Game
            KEY_DICE_ANIMATION_SPEED to 500L,
            KEY_TOKEN_MOVE_SPEED to 200L,
            KEY_TURN_TIMEOUT_SECONDS to 30,
            KEY_AUTO_SKIP_ENABLED to true,
            KEY_MAX_CONSECUTIVE_SIXES to 3,
            
            // Rewards
            KEY_COINS_PER_WIN to 50L,
            KEY_COINS_PER_CAPTURE to 5L,
            KEY_COINS_PER_SIX to 2L,
            KEY_COINS_PER_TOKEN_HOME to 10L,
            KEY_XP_PER_WIN to 100L,
            KEY_XP_PER_CAPTURE to 15L,
            KEY_XP_PER_TOKEN_HOME to 25L,
            KEY_DAILY_REWARD_COINS to "[50, 75, 100, 150, 200, 300, 500]",
            KEY_DAILY_REWARD_GEMS to "[0, 1, 2, 3, 4, 5, 10]",
            KEY_SPIN_WHEEL_COST to 5L,
            KEY_FREE_SPIN_INTERVAL_HOURS to 4L,
            
            // Ads
            KEY_INTERSTITIAL_AD_UNIT_ID to "ca-app-pub-3940256099942544/1033173712",
            KEY_REWARDED_AD_UNIT_ID to "ca-app-pub-3940256099942544/5224354917",
            KEY_BANNER_AD_UNIT_ID to "ca-app-pub-3940256099942544/6300978111",
            KEY_AD_SHOW_INTERVAL_GAMES to 3L,
            KEY_ADS_ENABLED to true,
            KEY_AD_FREE_COINS_REWARD to 50L,
            
            // IAP
            KEY_REMOVE_ADS_PRICE to "\$2.99",
            
            // Features
            KEY_ONLINE_MODE_ENABLED to true,
            KEY_TOURNAMENTS_ENABLED to false,
            KEY_FRIENDS_SYSTEM_ENABLED to true,
            KEY_CHAT_ENABLED to true,
            KEY_LEADERBOARD_ENABLED to true,
            KEY_ACHIEVEMENTS_ENABLED to true,
            
            // Rules
            KEY_REQUIRE_SIX_TO_RELEASE to true,
            KEY_THREE_SIXES_RULE to true,
            KEY_CAPTURE_GIVES_BONUS to true,
            
            // AI
            KEY_AI_THINKING_DELAY_MS to 800L,
            KEY_AI_EASY_MISTAKE_CHANCE to 0.3,
            KEY_AI_EXPERT_LOOKAHEAD to 3L,
            
            // Level
            KEY_BASE_XP to 100L,
            KEY_XP_MULTIPLIER to 1.5,
            KEY_LEVEL_UP_COIN_REWARD to 50L,
            
            // Social
            KEY_MAX_FRIENDS to 100L,
            KEY_FRIEND_REQUEST_COOLDOWN to 60L,
            KEY_MAX_GAME_INVITES_PER_DAY to 20L,
            
            // Rate
            KEY_RATE_APP_AFTER_GAMES to 10L,
            KEY_SHARE_TEXT to "Play Ludo Blitz with me! 🎲",
            KEY_SHARE_LINK to "https://play.google.com/store/apps/details?id=com.ludoblitz.app",
            
            // Notifications
            KEY_NOTIFICATIONS_ENABLED to true,
            KEY_DAILY_REWARD_REMINDER_HOUR to 10L
        )
        
        remoteConfig.setDefaultsAsync(defaults)
    }
    
    /**
     * Build config object from remote values
     */
    private fun buildConfig(): AppRemoteConfig {
        return AppRemoteConfig(
            // App
            appVersion = remoteConfig.getString(KEY_APP_VERSION),
            forceUpdate = remoteConfig.getBoolean(KEY_FORCE_UPDATE),
            updateUrl = remoteConfig.getString(KEY_UPDATE_URL),
            maintenanceMode = remoteConfig.getBoolean(KEY_MAINTENANCE_MODE),
            maintenanceMessage = remoteConfig.getString(KEY_MAINTENANCE_MESSAGE),
            
            // Game
            diceAnimationSpeed = remoteConfig.getLong(KEY_DICE_ANIMATION_SPEED),
            tokenMoveSpeed = remoteConfig.getLong(KEY_TOKEN_MOVE_SPEED),
            turnTimeoutSeconds = remoteConfig.getLong(KEY_TURN_TIMEOUT_SECONDS).toInt(),
            autoSkipEnabled = remoteConfig.getBoolean(KEY_AUTO_SKIP_ENABLED),
            maxConsecutiveSixes = remoteConfig.getLong(KEY_MAX_CONSECUTIVE_SIXES).toInt(),
            
            // Rewards
            coinsPerWin = remoteConfig.getLong(KEY_COINS_PER_WIN),
            coinsPerCapture = remoteConfig.getLong(KEY_COINS_PER_CAPTURE),
            coinsPerSix = remoteConfig.getLong(KEY_COINS_PER_SIX),
            coinsPerTokenHome = remoteConfig.getLong(KEY_COINS_PER_TOKEN_HOME),
            xpPerWin = remoteConfig.getLong(KEY_XP_PER_WIN).toInt(),
            xpPerCapture = remoteConfig.getLong(KEY_XP_PER_CAPTURE).toInt(),
            xpPerTokenHome = remoteConfig.getLong(KEY_XP_PER_TOKEN_HOME).toInt(),
            dailyRewardCoins = parseJsonArray(remoteConfig.getString(KEY_DAILY_REWARD_COINS)),
            dailyRewardGems = parseJsonArray(remoteConfig.getString(KEY_DAILY_REWARD_GEMS)),
            spinWheelCost = remoteConfig.getLong(KEY_SPIN_WHEEL_COST).toInt(),
            freeSpinIntervalHours = remoteConfig.getLong(KEY_FREE_SPIN_INTERVAL_HOURS).toInt(),
            
            // Ads
            interstitialAdUnitId = remoteConfig.getString(KEY_INTERSTITIAL_AD_UNIT_ID),
            rewardedAdUnitId = remoteConfig.getString(KEY_REWARDED_AD_UNIT_ID),
            bannerAdUnitId = remoteConfig.getString(KEY_BANNER_AD_UNIT_ID),
            adShowIntervalGames = remoteConfig.getLong(KEY_AD_SHOW_INTERVAL_GAMES).toInt(),
            adsEnabled = remoteConfig.getBoolean(KEY_ADS_ENABLED),
            adFreeCoinsReward = remoteConfig.getLong(KEY_AD_FREE_COINS_REWARD),
            
            // IAP
            removeAdsPrice = remoteConfig.getString(KEY_REMOVE_ADS_PRICE),
            
            // Features
            onlineModeEnabled = remoteConfig.getBoolean(KEY_ONLINE_MODE_ENABLED),
            tournamentsEnabled = remoteConfig.getBoolean(KEY_TOURNAMENTS_ENABLED),
            friendsSystemEnabled = remoteConfig.getBoolean(KEY_FRIENDS_SYSTEM_ENABLED),
            chatEnabled = remoteConfig.getBoolean(KEY_CHAT_ENABLED),
            leaderboardEnabled = remoteConfig.getBoolean(KEY_LEADERBOARD_ENABLED),
            achievementsEnabled = remoteConfig.getBoolean(KEY_ACHIEVEMENTS_ENABLED),
            
            // Rules
            requireSixToRelease = remoteConfig.getBoolean(KEY_REQUIRE_SIX_TO_RELEASE),
            threeSixesRule = remoteConfig.getBoolean(KEY_THREE_SIXES_RULE),
            captureGivesBonus = remoteConfig.getBoolean(KEY_CAPTURE_GIVES_BONUS),
            
            // AI
            aiThinkingDelayMs = remoteConfig.getLong(KEY_AI_THINKING_DELAY_MS),
            aiEasyMistakeChance = getDouble(KEY_AI_EASY_MISTAKE_CHANCE),
            aiExpertLookahead = remoteConfig.getLong(KEY_AI_EXPERT_LOOKAHEAD).toInt(),
            
            // Level
            baseXp = remoteConfig.getLong(KEY_BASE_XP).toInt(),
            xpMultiplier = getDouble(KEY_XP_MULTIPLIER),
            levelUpCoinReward = remoteConfig.getLong(KEY_LEVEL_UP_COIN_REWARD).toInt(),
            
            // Social
            maxFriends = remoteConfig.getLong(KEY_MAX_FRIENDS).toInt(),
            friendRequestCooldown = remoteConfig.getLong(KEY_FRIEND_REQUEST_COOLDOWN).toInt(),
            maxGameInvitesPerDay = remoteConfig.getLong(KEY_MAX_GAME_INVITES_PER_DAY).toInt(),
            
            // Rate & Share
            rateAppAfterGames = remoteConfig.getLong(KEY_RATE_APP_AFTER_GAMES).toInt(),
            shareText = remoteConfig.getString(KEY_SHARE_TEXT),
            shareLink = remoteConfig.getString(KEY_SHARE_LINK),
            
            // Notifications
            notificationsEnabledDefault = remoteConfig.getBoolean(KEY_NOTIFICATIONS_ENABLED),
            dailyRewardReminderHour = remoteConfig.getLong(KEY_DAILY_REWARD_REMINDER_HOUR).toInt()
        )
    }
    
    /**
     * Get current config
     */
    fun getConfig(): AppRemoteConfig {
        return configCache ?: buildConfig()
    }
    
    /**
     * Refresh config from Firebase
     */
    suspend fun refresh(): Boolean {
        return try {
            remoteConfig.fetchAndActivate().await()
            configCache = buildConfig()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Parse JSON array to List
     */
    private fun parseJsonArray(json: String): List<Int> {
        return try {
            json.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().toInt() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get double value from remote config
     */
    private fun getDouble(key: String): Double {
        return try {
            remoteConfig.getString(key).toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }
}

/**
 * Remote Config Data Class
 * Contains all dynamic app configurations controlled from Firebase
 */
data class AppRemoteConfig(
    // App
    val appVersion: String,
    val forceUpdate: Boolean,
    val updateUrl: String,
    val maintenanceMode: Boolean,
    val maintenanceMessage: String,
    
    // Game
    val diceAnimationSpeed: Long,
    val tokenMoveSpeed: Long,
    val turnTimeoutSeconds: Int,
    val autoSkipEnabled: Boolean,
    val maxConsecutiveSixes: Int,
    
    // Rewards
    val coinsPerWin: Long,
    val coinsPerCapture: Long,
    val coinsPerSix: Long,
    val coinsPerTokenHome: Long,
    val xpPerWin: Int,
    val xpPerCapture: Int,
    val xpPerTokenHome: Int,
    val dailyRewardCoins: List<Int>,
    val dailyRewardGems: List<Int>,
    val spinWheelCost: Int,
    val freeSpinIntervalHours: Int,
    
    // Ads
    val interstitialAdUnitId: String,
    val rewardedAdUnitId: String,
    val bannerAdUnitId: String,
    val adShowIntervalGames: Int,
    val adsEnabled: Boolean,
    val adFreeCoinsReward: Long,
    
    // IAP
    val removeAdsPrice: String,
    
    // Features
    val onlineModeEnabled: Boolean,
    val tournamentsEnabled: Boolean,
    val friendsSystemEnabled: Boolean,
    val chatEnabled: Boolean,
    val leaderboardEnabled: Boolean,
    val achievementsEnabled: Boolean,
    
    // Rules
    val requireSixToRelease: Boolean,
    val threeSixesRule: Boolean,
    val captureGivesBonus: Boolean,
    
    // AI
    val aiThinkingDelayMs: Long,
    val aiEasyMistakeChance: Double,
    val aiExpertLookahead: Int,
    
    // Level
    val baseXp: Int,
    val xpMultiplier: Double,
    val levelUpCoinReward: Int,
    
    // Social
    val maxFriends: Int,
    val friendRequestCooldown: Int,
    val maxGameInvitesPerDay: Int,
    
    // Rate & Share
    val rateAppAfterGames: Int,
    val shareText: String,
    val shareLink: String,
    
    // Notifications
    val notificationsEnabledDefault: Boolean,
    val dailyRewardReminderHour: Int
)
