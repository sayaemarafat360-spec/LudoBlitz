package com.ludoblitz.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ludoblitz.app.utils.NotificationEngine
import com.ludoblitz.app.utils.NotificationScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Broadcast Receiver for scheduled notifications
 * Handles daily rewards, spin wheel reminders, and turn alerts
 */
@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var notificationEngine: NotificationEngine
    
    @Inject
    lateinit var notificationScheduler: NotificationScheduler
    
    private val scope = CoroutineScope(Dispatchers.Default)
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Received notification intent: ${intent.action}")
        
        when (intent.action) {
            NotificationScheduler.ACTION_DAILY_REWARD -> {
                handleDailyRewardNotification()
            }
            NotificationScheduler.ACTION_SPIN_REMINDER -> {
                handleSpinReminderNotification()
            }
            NotificationScheduler.ACTION_TURN_REMINDER -> {
                handleTurnReminderNotification(intent)
            }
            else -> {
                // Handle custom notifications
                val type = intent.getStringExtra(NotificationScheduler.EXTRA_NOTIFICATION_TYPE)
                val title = intent.getStringExtra(NotificationScheduler.EXTRA_TITLE) ?: "Ludo Blitz"
                val message = intent.getStringExtra(NotificationScheduler.EXTRA_MESSAGE) ?: ""
                
                if (!type.isNullOrEmpty()) {
                    notificationEngine.showNotification(
                        title = title,
                        message = message,
                        channelId = NotificationEngine.CHANNEL_GENERAL
                    )
                }
            }
        }
    }
    
    private fun handleDailyRewardNotification() {
        // Show daily reward notification
        notificationEngine.showDailyRewardNotification(coins = 100)
        
        // Schedule next notification
        notificationScheduler.scheduleDailyRewardNotification()
    }
    
    private fun handleSpinReminderNotification() {
        // Show spin wheel notification
        notificationEngine.showSpinWheelNotification()
    }
    
    private fun handleTurnReminderNotification(intent: Intent) {
        val gameId = intent.getStringExtra("gameId") ?: return
        
        // Show turn alert notification
        notificationEngine.showTurnAlertNotification(gameId)
    }
}
