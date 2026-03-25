package com.ludoblitz.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ludoblitz.app.R
import com.ludoblitz.app.ui.screens.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Robust Notification Engine for Ludo Blitz
 * Handles all notification types: in-game alerts, push notifications, scheduled reminders
 */
@Singleton
class NotificationEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Notification Channel IDs
        const val CHANNEL_GENERAL = "ludo_blitz_general"
        const val CHANNEL_GAME = "ludo_blitz_game"
        const val CHANNEL_SOCIAL = "ludo_blitz_social"
        const val CHANNEL_REWARDS = "ludo_blitz_rewards"
        const val CHANNEL_ACHIEVEMENTS = "ludo_blitz_achievements"
        
        // Notification IDs
        const val NOTIF_DAILY_REWARD = 1001
        const val NOTIF_FRIEND_REQUEST = 1002
        const val NOTIF_GAME_INVITE = 1003
        const val NOTIF_TURN_ALERT = 1004
        const val NOTIF_ACHIEVEMENT = 1005
        const val NOTIF_LEVEL_UP = 1006
        const val NOTIF_SPIN_WHEEL = 1007
        const val NOTIF_TOURNAMENT = 1008
        
        // Preference keys
        const val PREF_NOTIFS_ENABLED = "notifications_enabled"
        const val PREF_NOTIFS_SOUND = "notifications_sound"
        const val PREF_NOTIFS_VIBRATE = "notifications_vibrate"
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Create all notification channels for Android O+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            
            // General notifications
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                context.getString(R.string.notif_channel_general),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_general_desc)
                enableLights(true)
                lightColor = Color.parseColor("#6C63FF")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }
            
            // Game notifications (higher priority)
            val gameChannel = NotificationChannel(
                CHANNEL_GAME,
                context.getString(R.string.notif_channel_game),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_game_desc)
                enableLights(true)
                lightColor = Color.parseColor("#FF6B6B")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 100, 100, 100, 100)
                setShowBadge(true)
            }
            
            // Social notifications
            val socialChannel = NotificationChannel(
                CHANNEL_SOCIAL,
                context.getString(R.string.notif_channel_social),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_social_desc)
                enableLights(true)
                lightColor = Color.parseColor("#4ECDC4")
            }
            
            // Rewards notifications
            val rewardsChannel = NotificationChannel(
                CHANNEL_REWARDS,
                context.getString(R.string.notif_channel_rewards),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_rewards_desc)
                enableLights(true)
                lightColor = Color.parseColor("#FFD93D")
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
            }
            
            // Achievements notifications (high priority)
            val achievementsChannel = NotificationChannel(
                CHANNEL_ACHIEVEMENTS,
                context.getString(R.string.notif_channel_achievements),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_achievements_desc)
                enableLights(true)
                lightColor = Color.parseColor("#FF6B6B")
                setShowBadge(true)
            }
            
            // Register channels
            val systemNotifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            systemNotifManager.createNotificationChannels(listOf(
                generalChannel, gameChannel, socialChannel, rewardsChannel, achievementsChannel
            ))
        }
    }
    
    /**
     * Show daily reward notification
     */
    fun showDailyRewardNotification(coins: Long) {
        if (!areNotificationsEnabled()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "daily_reward")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_REWARDS)
            .setSmallIcon(R.drawable.ic_coin)
            .setContentTitle(context.getString(R.string.notif_daily_reward_title))
            .setContentText(context.getString(R.string.notif_daily_reward_body, coins))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(context.getString(R.string.notif_daily_reward_big, coins)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(ContextCompat.getColor(context, R.color.primary))
            .build()
        
        notificationManager.notify(NOTIF_DAILY_REWARD, notification)
    }
    
    /**
     * Show friend request notification
     */
    fun showFriendRequestNotification(friendName: String, requestId: String) {
        if (!areNotificationsEnabled()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "friend_request")
            putExtra("requestId", requestId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, Random.nextInt(10000), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_SOCIAL)
            .setSmallIcon(R.drawable.ic_profile)
            .setContentTitle(context.getString(R.string.notif_friend_request_title))
            .setContentText(context.getString(R.string.notif_friend_request_body, friendName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setColor(ContextCompat.getColor(context, R.color.accent))
            .addAction(
                R.drawable.ic_coin,
                context.getString(R.string.accept),
                pendingIntent
            )
            .build()
        
        notificationManager.notify(NOTIF_FRIEND_REQUEST + Random.nextInt(100), notification)
    }
    
    /**
     * Show game invite notification
     */
    fun showGameInviteNotification(hostName: String, roomId: String) {
        if (!areNotificationsEnabled()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "game_invite")
            putExtra("roomId", roomId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, Random.nextInt(10000), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_GAME)
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle(context.getString(R.string.notif_game_invite_title))
            .setContentText(context.getString(R.string.notif_game_invite_body, hostName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setFullScreenIntent(pendingIntent, true)
            .setTimeoutAfter(60000) // Expires after 1 minute
            .setColor(ContextCompat.getColor(context, R.color.token_red))
            .addAction(
                R.drawable.ic_play,
                context.getString(R.string.join_now),
                pendingIntent
            )
            .build()
        
        notificationManager.notify(NOTIF_GAME_INVITE + Random.nextInt(100), notification)
    }
    
    /**
     * Show turn alert notification
     */
    fun showTurnAlertNotification(gameId: String, playerName: String? = null) {
        if (!areNotificationsEnabled()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "turn_alert")
            putExtra("gameId", gameId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val title = context.getString(R.string.notif_turn_alert_title)
        val body = playerName?.let {
            context.getString(R.string.notif_turn_alert_body_named, it)
        } ?: context.getString(R.string.notif_turn_alert_body)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_GAME)
            .setSmallIcon(R.drawable.ic_dice)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOnlyAlertOnce(false)
            .setColor(ContextCompat.getColor(context, R.color.primary))
            .setVibrate(longArrayOf(0, 200, 100, 200, 100, 200))
            .build()
        
        notificationManager.notify(NOTIF_TURN_ALERT, notification)
    }
    
    /**
     * Show achievement unlocked notification
     */
    fun showAchievementNotification(
        achievementTitle: String,
        achievementDescription: String,
        reward: Long
    ) {
        if (!areNotificationsEnabled()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "achievement")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENTS)
            .setSmallIcon(R.drawable.ic_gem)
            .setContentTitle(context.getString(R.string.notif_achievement_title))
            .setContentText(achievementTitle)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$achievementTitle\n\n$achievementDescription\n+${reward} coins"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setColor(ContextCompat.getColor(context, R.color.token_yellow))
            .build()
        
        notificationManager.notify(NOTIF_ACHIEVEMENT + Random.nextInt(100), notification)
    }
    
    /**
     * Show level up notification
     */
    fun showLevelUpNotification(newLevel: Int, rewards: String) {
        if (!areNotificationsEnabled()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "level_up")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ACHIEVEMENTS)
            .setSmallIcon(R.drawable.ic_stats)
            .setContentTitle(context.getString(R.string.notif_level_up_title))
            .setContentText(context.getString(R.string.notif_level_up_body, newLevel))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(context.getString(R.string.notif_level_up_big, newLevel, rewards)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setColor(ContextCompat.getColor(context, R.color.accent))
            .build()
        
        notificationManager.notify(NOTIF_LEVEL_UP, notification)
    }
    
    /**
     * Show spin wheel available notification
     */
    fun showSpinWheelNotification() {
        if (!areNotificationsEnabled()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "spin_wheel")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_REWARDS)
            .setSmallIcon(R.drawable.ic_spin)
            .setContentTitle(context.getString(R.string.notif_spin_wheel_title))
            .setContentText(context.getString(R.string.notif_spin_wheel_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_PROMO)
            .setColor(ContextCompat.getColor(context, R.color.token_green))
            .addAction(
                R.drawable.ic_spin,
                context.getString(R.string.spin_now),
                pendingIntent
            )
            .build()
        
        notificationManager.notify(NOTIF_SPIN_WHEEL, notification)
    }
    
    /**
     * Show game result notification
     */
    fun showGameResultNotification(isWinner: Boolean, coinsWon: Long, xpGained: Int) {
        if (!areNotificationsEnabled()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "game_result")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val title = if (isWinner) {
            context.getString(R.string.notif_victory_title)
        } else {
            context.getString(R.string.notif_game_over_title)
        }
        
        val body = if (isWinner) {
            context.getString(R.string.notif_victory_body, coinsWon, xpGained)
        } else {
            context.getString(R.string.notif_game_over_body, xpGained)
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_GAME)
            .setSmallIcon(if (isWinner) R.drawable.ic_coin else R.drawable.ic_dice)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setColor(if (isWinner) 
                ContextCompat.getColor(context, R.color.token_yellow) 
                else ContextCompat.getColor(context, R.color.text_secondary))
            .build()
        
        notificationManager.notify(NOTIF_DAILY_REWARD + Random.nextInt(100), notification)
    }
    
    /**
     * Show tournament notification
     */
    fun showTournamentNotification(tournamentName: String, startTime: String) {
        if (!areNotificationsEnabled()) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "tournament")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_GAME)
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle(context.getString(R.string.notif_tournament_title))
            .setContentText(context.getString(R.string.notif_tournament_body, tournamentName, startTime))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setColor(ContextCompat.getColor(context, R.color.token_red))
            .addAction(
                R.drawable.ic_play,
                context.getString(R.string.view_details),
                pendingIntent
            )
            .build()
        
        notificationManager.notify(NOTIF_TOURNAMENT, notification)
    }
    
    /**
     * Show generic notification
     */
    fun showNotification(
        title: String,
        message: String,
        channelId: String = CHANNEL_GENERAL,
        smallIcon: Int = R.drawable.ic_logo,
        largeIcon: Bitmap? = null,
        actionIntent: Intent? = null
    ) {
        if (!areNotificationsEnabled()) return
        
        val intent = actionIntent ?: Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, Random.nextInt(10000), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.primary))
        
        largeIcon?.let { builder.setLargeIcon(it) }
        
        notificationManager.notify(Random.nextInt(10000), builder.build())
    }
    
    /**
     * Cancel specific notification
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
    
    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    /**
     * Check if notifications are enabled
     */
    private fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
    
    /**
     * Check if specific channel is enabled
     */
    fun isChannelEnabled(channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .getNotificationChannel(channelId)
            return channel?.importance != NotificationManager.IMPORTANCE_NONE
        }
        return true
    }
    
    /**
     * Open notification settings
     */
    fun openNotificationSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
        }
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
