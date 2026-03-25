package com.ludoblitz.app.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import com.ludoblitz.app.receivers.NotificationReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Notification Scheduler
 * Schedules local notifications for daily rewards, reminders, etc.
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationEngine: NotificationEngine
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    companion object {
        const val ACTION_DAILY_REWARD = "com.ludoblitz.app.DAILY_REWARD"
        const val ACTION_SPIN_REMINDER = "com.ludoblitz.app.SPIN_REMINDER"
        const val ACTION_TURN_REMINDER = "com.ludoblitz.app.TURN_REMINDER"
        
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
        
        // Request codes
        const val REQUEST_DAILY_REWARD = 1001
        const val REQUEST_SPIN_REMINDER = 1002
        const val REQUEST_TURN_REMINDER = 1003
    }
    
    /**
     * Schedule daily reward notification
     * Notifies user when daily reward is available
     */
    fun scheduleDailyRewardNotification() {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_DAILY_REWARD
            putExtra(EXTRA_NOTIFICATION_TYPE, "daily_reward")
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_DAILY_REWARD,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Schedule for 24 hours from now
        val triggerAtMillis = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
        
        scheduleExactAlarm(triggerAtMillis, pendingIntent)
    }
    
    /**
     * Schedule spin wheel reminder
     * Reminds user when free spin is available
     */
    fun scheduleSpinReminder(delayHours: Long = 4) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_SPIN_REMINDER
            putExtra(EXTRA_NOTIFICATION_TYPE, "spin_wheel")
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_SPIN_REMINDER,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val triggerAtMillis = System.currentTimeMillis() + (delayHours * 60 * 60 * 1000)
        
        scheduleExactAlarm(triggerAtMillis, pendingIntent)
    }
    
    /**
     * Schedule turn reminder for online games
     * Reminds player it's their turn
     */
    fun scheduleTurnReminder(gameId: String, delaySeconds: Int = 30) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_TURN_REMINDER
            putExtra(EXTRA_NOTIFICATION_TYPE, "turn_alert")
            putExtra("gameId", gameId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_TURN_REMINDER,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val triggerAtMillis = System.currentTimeMillis() + (delaySeconds * 1000)
        
        scheduleExactAlarm(triggerAtMillis, pendingIntent)
    }
    
    /**
     * Cancel daily reward notification
     */
    fun cancelDailyRewardNotification() {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_DAILY_REWARD
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_DAILY_REWARD,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        
        pendingIntent?.let { alarmManager.cancel(it) }
    }
    
    /**
     * Cancel all scheduled notifications
     */
    fun cancelAllNotifications() {
        cancelDailyRewardNotification()
        
        val spinIntent = Intent(context, NotificationReceiver::class.java)
        val spinPending = PendingIntent.getBroadcast(
            context, REQUEST_SPIN_REMINDER, spinIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        spinPending?.let { alarmManager.cancel(it) }
        
        val turnIntent = Intent(context, NotificationReceiver::class.java)
        val turnPending = PendingIntent.getBroadcast(
            context, REQUEST_TURN_REMINDER, turnIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        turnPending?.let { alarmManager.cancel(it) }
    }
    
    /**
     * Schedule an exact alarm
     */
    private fun scheduleExactAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Check if we have permission to schedule exact alarms
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    // Fall back to inexact alarm
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Handle permission issues
            e.printStackTrace()
        }
    }
    
    /**
     * Schedule a repeating notification
     */
    fun scheduleRepeatingNotification(
        type: String,
        intervalMillis: Long,
        title: String,
        message: String
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "REPEATING_$type"
            putExtra(EXTRA_NOTIFICATION_TYPE, type)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
        }
        
        val requestCode = type.hashCode()
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Use inexact repeating for battery efficiency
        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + intervalMillis,
            intervalMillis,
            pendingIntent
        )
    }
}
