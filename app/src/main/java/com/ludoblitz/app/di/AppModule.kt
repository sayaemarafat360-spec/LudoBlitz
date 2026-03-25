package com.ludoblitz.app.di

import android.content.Context
import com.ludoblitz.app.data.firebase.FirebaseAuthManager
import com.ludoblitz.app.data.firebase.FirebaseConfigManager
import com.ludoblitz.app.data.firebase.FirebaseDatabaseManager
import com.ludoblitz.app.data.local.LocalUserRepository
import com.ludoblitz.app.data.local.PreferenceManager
import com.ludoblitz.app.domain.gamelogic.EnhancedGameEngine
import com.ludoblitz.app.domain.gamelogic.LudoGameEngine
import com.ludoblitz.app.domain.session.LocalGameSessionManager
import com.ludoblitz.app.domain.ai.LudoAI
import com.ludoblitz.app.domain.gamification.LocalGamificationManager
import com.ludoblitz.app.utils.AdManager
import com.ludoblitz.app.utils.NotificationEngine
import com.ludoblitz.app.utils.NotificationScheduler
import com.ludoblitz.app.utils.SoundManager
import com.ludoblitz.app.utils.VibrationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for providing app-wide dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferenceManager(
        @ApplicationContext context: Context
    ): PreferenceManager {
        return PreferenceManager(context)
    }

    @Provides
    @Singleton
    fun provideLocalUserRepository(
        @ApplicationContext context: Context
    ): LocalUserRepository {
        return LocalUserRepository(context)
    }

    @Provides
    @Singleton
    fun provideSoundManager(
        @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): SoundManager {
        return SoundManager(context, preferenceManager)
    }

    @Provides
    @Singleton
    fun provideVibrationManager(
        @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): VibrationManager {
        return VibrationManager(context, preferenceManager)
    }

    @Provides
    @Singleton
    fun provideAdManager(
        @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): AdManager {
        return AdManager(context, preferenceManager)
    }

    @Provides
    @Singleton
    fun provideNotificationEngine(
        @ApplicationContext context: Context
    ): NotificationEngine {
        return NotificationEngine(context)
    }

    @Provides
    @Singleton
    fun provideNotificationScheduler(
        @ApplicationContext context: Context,
        notificationEngine: NotificationEngine
    ): NotificationScheduler {
        return NotificationScheduler(context, notificationEngine)
    }

    @Provides
    @Singleton
    fun provideLudoGameEngine(): LudoGameEngine {
        return LudoGameEngine()
    }

    @Provides
    @Singleton
    fun provideEnhancedGameEngine(
        @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): EnhancedGameEngine {
        return EnhancedGameEngine(context, preferenceManager)
    }

    @Provides
    @Singleton
    fun provideLudoAI(): LudoAI {
        return LudoAI()
    }

    @Provides
    @Singleton
    fun provideLocalGameSessionManager(
        gameEngine: LudoGameEngine,
        enhancedEngine: EnhancedGameEngine,
        ai: LudoAI
    ): LocalGameSessionManager {
        return LocalGameSessionManager(gameEngine, enhancedEngine, ai)
    }

    @Provides
    @Singleton
    fun provideLocalGamificationManager(
        @ApplicationContext context: Context,
        preferenceManager: PreferenceManager
    ): LocalGamificationManager {
        return LocalGamificationManager(context, preferenceManager)
    }

    // ==================== Firebase Providers ====================

    @Provides
    @Singleton
    fun provideFirebaseAuthManager(
        @ApplicationContext context: Context
    ): FirebaseAuthManager {
        return FirebaseAuthManager(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabaseManager(
        authManager: FirebaseAuthManager
    ): FirebaseDatabaseManager {
        return FirebaseDatabaseManager(authManager)
    }

    @Provides
    @Singleton
    fun provideFirebaseConfigManager(): FirebaseConfigManager {
        return FirebaseConfigManager()
    }
}
