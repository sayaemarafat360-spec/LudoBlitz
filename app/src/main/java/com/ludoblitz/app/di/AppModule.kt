package com.ludoblitz.app.di

import android.content.Context
import com.ludoblitz.app.data.firebase.AuthManager
import com.ludoblitz.app.data.firebase.DatabaseManager
import com.ludoblitz.app.data.local.PreferenceManager
import com.ludoblitz.app.domain.ai.AIFactory
import com.ludoblitz.app.domain.gamelogic.LudoGameEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun providePrefs(@ApplicationContext c: Context) = PreferenceManager(c)

    @Provides @Singleton
    fun provideGameEngine() = LudoGameEngine()

    @Provides @Singleton
    fun provideAuthManager(@ApplicationContext c: Context) = AuthManager(c)

    @Provides @Singleton
    fun provideDatabaseManager() = DatabaseManager()
}
