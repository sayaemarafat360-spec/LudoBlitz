package com.ludoblitz.app.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.ludoblitz.app.data.local.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Vibration Manager - Handles haptic feedback for game events
 * Supports different vibration patterns for different events
 */
@Singleton
class VibrationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager
) {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.SDK_INT.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // Vibration patterns (duration in milliseconds)
    companion object {
        // Short vibrations
        const val SHORT_CLICK = 10L
        const val MEDIUM_CLICK = 25L
        const val LONG_CLICK = 50L

        // Pattern for dice roll (rattling effect)
        val DICE_ROLL_PATTERN = longArrayOf(0, 20, 30, 20, 30, 20, 30, 20)

        // Pattern for token movement
        val TOKEN_MOVE_PATTERN = longArrayOf(0, 15, 10, 15)

        // Pattern for capture
        val CAPTURE_PATTERN = longArrayOf(0, 50, 30, 80)

        // Pattern for six rolled
        val SIX_ROLLED_PATTERN = longArrayOf(0, 30, 20, 30, 20, 50)

        // Pattern for token reaching home
        val TOKEN_HOME_PATTERN = longArrayOf(0, 20, 10, 20, 10, 20, 50)

        // Pattern for victory
        val VICTORY_PATTERN = longArrayOf(0, 100, 50, 100, 50, 200)

        // Pattern for defeat
        val DEFEAT_PATTERN = longArrayOf(0, 200, 100, 200)

        // Pattern for level up
        val LEVEL_UP_PATTERN = longArrayOf(0, 50, 30, 50, 30, 50, 30, 100)

        // Pattern for button click
        val BUTTON_CLICK_PATTERN = longArrayOf(0, 10)

        // Pattern for error
        val ERROR_PATTERN = longArrayOf(0, 100)
    }

    /**
     * Check if vibration is enabled
     */
    private fun isVibrationEnabled(): Boolean {
        val prefs = context.getSharedPreferences("ludo_blitz_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("vibration_enabled", true) && vibrator.hasVibrator()
    }

    /**
     * Vibrate with pattern
     */
    private fun vibrate(pattern: LongArray, repeat: Int = -1) {
        if (!isVibrationEnabled()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, repeat)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, repeat)
        }
    }

    /**
     * Vibrate for specific duration
     */
    private fun vibrate(duration: Long) {
        if (!isVibrationEnabled()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    /**
     * Short click vibration
     */
    fun shortClick() {
        vibrate(SHORT_CLICK)
    }

    /**
     * Medium click vibration
     */
    fun mediumClick() {
        vibrate(MEDIUM_CLICK)
    }

    /**
     * Long click vibration
     */
    fun longClick() {
        vibrate(LONG_CLICK)
    }

    /**
     * Button click vibration
     */
    fun buttonClick() {
        vibrate(BUTTON_CLICK_PATTERN)
    }

    /**
     * Dice roll vibration
     */
    fun diceRoll() {
        vibrate(DICE_ROLL_PATTERN)
    }

    /**
     * Token move vibration
     */
    fun tokenMove() {
        vibrate(TOKEN_MOVE_PATTERN)
    }

    /**
     * Capture vibration (strong feedback)
     */
    fun capture() {
        vibrate(CAPTURE_PATTERN)
    }

    /**
     * Six rolled vibration (celebratory)
     */
    fun sixRolled() {
        vibrate(SIX_ROLLED_PATTERN)
    }

    /**
     * Token home vibration
     */
    fun tokenHome() {
        vibrate(TOKEN_HOME_PATTERN)
    }

    /**
     * Victory vibration (strong celebration)
     */
    fun victory() {
        vibrate(VICTORY_PATTERN)
    }

    /**
     * Defeat vibration (sad feedback)
     */
    fun defeat() {
        vibrate(DEFEAT_PATTERN)
    }

    /**
     * Level up vibration (ascending pattern)
     */
    fun levelUp() {
        vibrate(LEVEL_UP_PATTERN)
    }

    /**
     * Error vibration
     */
    fun error() {
        vibrate(ERROR_PATTERN)
    }

    /**
     * Custom vibration pattern
     */
    fun customPattern(pattern: LongArray, repeat: Int = -1) {
        vibrate(pattern, repeat)
    }

    /**
     * Cancel ongoing vibration
     */
    fun cancel() {
        vibrator.cancel()
    }

    /**
     * Check if device supports vibration
     */
    fun hasVibrator(): Boolean = vibrator.hasVibrator()
}
