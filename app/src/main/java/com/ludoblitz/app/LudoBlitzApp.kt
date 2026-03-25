package com.ludoblitz.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ludoblitz.app.data.firebase.FirebaseAuthManager
import com.ludoblitz.app.data.firebase.FirebaseConfigManager
import com.ludoblitz.app.data.local.PreferenceManager
import com.ludoblitz.app.utils.AdPreloadManager
import com.ludoblitz.app.utils.NotificationEngine
import com.ludoblitz.app.utils.NotificationScheduler
import com.ludoblitz.app.utils.SoundManager
import com.ludoblitz.app.utils.VibrationManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main Application class for Ludo Blitz
 * Handles initialization of Firebase, AdMob, sound system, and theme
 * 
 * Firebase Services Used:
 * - Authentication (Email, Google, Anonymous)
 * - Realtime Database (Online multiplayer, leaderboards, friends)
 * - Remote Config (Dynamic app control without updates)
 * - Cloud Messaging (Push notifications)
 * - Analytics (User behavior tracking)
 * - Crashlytics (Crash reporting)
 * 
 * NOT using Firebase Storage (requires paid plan) - 
 * Instead, we use AI-generated avatars! 🎨
 */
@HiltAndroidApp
class LudoBlitzApp : Application() {

    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    @Inject
    lateinit var soundManager: SoundManager
    
    @Inject
    lateinit var vibrationManager: VibrationManager
    
    @Inject
    lateinit var firebaseAuthManager: FirebaseAuthManager
    
    @Inject
    lateinit var firebaseConfigManager: FirebaseConfigManager
    
    @Inject
    lateinit var notificationEngine: NotificationEngine
    
    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    companion object {
        lateinit var instance: LudoBlitzApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize Firebase
        initializeFirebase()
        
        // Initialize AdMob
        initializeAdMob()
        
        // Setup theme based on user preference
        setupTheme()
        
        // Initialize local user data
        initializeUser()
        
        // Schedule notifications
        scheduleNotifications()
    }
    
    /**
     * Initialize Firebase services
     */
    private fun initializeFirebase() {
        // Initialize Firebase App
        FirebaseApp.initializeApp(this)
        
        // Enable Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        
        // Fetch Remote Config
        applicationScope.launch {
            try {
                val success = firebaseConfigManager.initialize()
                if (success) {
                    android.util.Log.d("LudoBlitzApp", "Remote Config initialized successfully")
                    
                    // Check for maintenance mode
                    val config = firebaseConfigManager.getConfig()
                    if (config.maintenanceMode) {
                        android.util.Log.w("LudoBlitzApp", "App is in maintenance mode: ${config.maintenanceMessage}")
                    }
                    
                    // Check for force update
                    if (config.forceUpdate) {
                        android.util.Log.w("LudoBlitzApp", "Force update required")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LudoBlitzApp", "Failed to initialize Remote Config", e)
            }
        }
    }
    
    /**
     * Initialize AdMob with preloading
     */
    private fun initializeAdMob() {
        applicationScope.launch {
            MobileAds.initialize(this@LudoBlitzApp) {
                android.util.Log.d("LudoBlitzApp", "AdMob initialized")
                // Preload ads when app starts
                AdPreloadManager.getInstance().preloadAds(this@LudoBlitzApp)
            }
        }
    }

    private fun setupTheme() {
        applicationScope.launch {
            val isDarkMode = preferenceManager.isDarkMode.first()
            val mode = if (isDarkMode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    private fun initializeUser() {
        applicationScope.launch {
            // Initialize first-time user data
            val prefs = getSharedPreferences("ludo_blitz_prefs", MODE_PRIVATE)
            if (!prefs.contains("user_initialized")) {
                prefs.edit().apply {
                    putBoolean("user_initialized", true)
                    putBoolean("sound_enabled", true)
                    putBoolean("music_enabled", true)
                    putBoolean("vibration_enabled", true)
                    putBoolean("notifications_enabled", true)
                    putLong("coins", 500L)
                    putLong("gems", 5L)
                    putInt("level", 1)
                    putInt("rating", 1000)
                    apply()
                }
            }
        }
    }
    
    /**
     * Schedule periodic notifications
     */
    private fun scheduleNotifications() {
        applicationScope.launch {
            val notificationsEnabled = preferenceManager.isNotificationsEnabled.first()
            if (notificationsEnabled) {
                notificationScheduler.scheduleDailyRewardNotification()
            }
        }
    }

    fun isOnline(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) 
            as android.net.ConnectivityManager
        val network = connectivityManager.activeNetworkInfo
        return network != null && network.isConnected
    }
    
    fun getSoundManager(): SoundManager = soundManager
    
    fun getVibrationManager(): VibrationManager = vibrationManager
    
    fun getFirebaseAuthManager(): FirebaseAuthManager = firebaseAuthManager
    
    fun getFirebaseConfigManager(): FirebaseConfigManager = firebaseConfigManager
}
