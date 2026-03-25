package com.ludoblitz.app.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ludoblitz.app.R
import com.ludoblitz.app.ui.screens.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Firebase Cloud Messaging Service
 * Handles push notifications for game events
 */
@AndroidEntryPoint
class LudoMessagingService : FirebaseMessagingService() {
    
    @Inject
    lateinit var notificationEngine: com.ludoblitz.app.utils.NotificationEngine
    
    private val scope = CoroutineScope(Dispatchers.Default)
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save the FCM token to database for sending notifications to this user
        scope.launch {
            // Update user's FCM token in database
            // This will be used when other players invite or when it's their turn
            saveFcmToken(token)
        }
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Check if message contains a data payload
        remoteMessage.data.isNotEmpty().let {
            handleDataMessage(remoteMessage.data)
        }
        
        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            showNotification(
                title = it.title ?: "Ludo Blitz",
                body = it.body ?: "",
                data = remoteMessage.data
            )
        }
    }
    
    /**
     * Handle data messages (silent notifications)
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: return
        
        when (type) {
            "game_invite" -> {
                val hostName = data["hostName"] ?: "Someone"
                val roomId = data["roomId"] ?: ""
                notificationEngine.showGameInviteNotification(hostName, roomId)
            }
            "turn_alert" -> {
                val gameId = data["gameId"] ?: ""
                val playerName = data["playerName"]
                notificationEngine.showTurnAlertNotification(gameId, playerName)
            }
            "friend_request" -> {
                val friendName = data["friendName"] ?: "Someone"
                val requestId = data["requestId"] ?: ""
                notificationEngine.showFriendRequestNotification(friendName, requestId)
            }
            "daily_reward" -> {
                val coins = data["coins"]?.toLong() ?: 100L
                notificationEngine.showDailyRewardNotification(coins)
            }
            "achievement" -> {
                val title = data["achievementTitle"] ?: "Achievement Unlocked!"
                val description = data["achievementDescription"] ?: ""
                val reward = data["reward"]?.toLong() ?: 100L
                notificationEngine.showAchievementNotification(title, description, reward)
            }
            "level_up" -> {
                val level = data["level"]?.toInt() ?: 1
                val rewards = data["rewards"] ?: ""
                notificationEngine.showLevelUpNotification(level, rewards)
            }
        }
    }
    
    /**
     * Show notification
     */
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, "ludo_blitz_game")
            .setSmallIcon(R.drawable.ic_dice)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    /**
     * Save FCM token to database
     */
    private suspend fun saveFcmToken(token: String) {
        // This would be implemented to save the token to Firebase Database
        // for later use when sending notifications to this user
    }
}
