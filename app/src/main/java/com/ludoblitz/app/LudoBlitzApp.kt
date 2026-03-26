package com.ludoblitz.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.android.gms.ads.MobileAds

class LudoBlitzApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize AdMob
        MobileAds.initialize(this) {
            // AdMob initialized
        }

        // Create notification channels for Android O+
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Game notifications channel
            val gameChannel = NotificationChannel(
                CHANNEL_GAME_NOTIFICATIONS,
                "Game Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications about game events and rewards"
            }
            notificationManager.createNotificationChannel(gameChannel)

            // Daily rewards channel
            val rewardsChannel = NotificationChannel(
                CHANNEL_DAILY_REWARDS,
                "Daily Rewards",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for daily reward collection"
            }
            notificationManager.createNotificationChannel(rewardsChannel)
        }
    }

    companion object {
        const val CHANNEL_GAME_NOTIFICATIONS = "game_notifications"
        const val CHANNEL_DAILY_REWARDS = "daily_rewards"
    }
}
