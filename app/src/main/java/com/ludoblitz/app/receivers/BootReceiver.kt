package com.ludoblitz.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ludoblitz.app.utils.NotificationScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Boot Receiver
 * Reschedules notifications after device reboot
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var notificationScheduler: NotificationScheduler
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Device booted, rescheduling notifications")
            
            // Reschedule daily reward notification
            notificationScheduler.scheduleDailyRewardNotification()
        }
    }
}
