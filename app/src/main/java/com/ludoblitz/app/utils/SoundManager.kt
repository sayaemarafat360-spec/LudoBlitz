package com.ludoblitz.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.MediaPlayer
import com.ludoblitz.app.R
import com.ludoblitz.app.data.local.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sound Manager - Handles all game sound effects
 * Supports sound preloading, volume control, and enabling/disabling
 */
@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager
) {

    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<SoundType, Int>()
    private var isLoaded = false
    private var mediaPlayer: MediaPlayer? = null

    // Volume levels (0.0 to 1.0)
    private var masterVolume = 1.0f
    private var sfxVolume = 1.0f
    private var musicVolume = 0.7f

    enum class SoundType {
        DICE_ROLL,
        DICE_ROLLING,
        TOKEN_MOVE,
        TOKEN_CAPTURE,
        TOKEN_CAPTURED,
        TOKEN_SAFE,
        TOKEN_HOME,
        SIX_ROLLED,
        VICTORY,
        DEFEAT,
        BUTTON_CLICK,
        COIN_COLLECT,
        LEVEL_UP,
        ACHIEVEMENT,
        COUNTDOWN,
        GAME_START,
        SPIN_WHEEL,
        REWARD_CLAIM,
        ERROR,
        NOTIFICATION,
        TURN_START
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                isLoaded = true
            }
        }

        loadSounds()
    }

    private fun loadSounds() {
        try {
            soundMap[SoundType.DICE_ROLL] = soundPool.load(context, R.raw.dice_roll, 1)
            soundMap[SoundType.TOKEN_MOVE] = soundPool.load(context, R.raw.token_move, 1)
            soundMap[SoundType.TOKEN_CAPTURE] = soundPool.load(context, R.raw.token_kill, 1)
            soundMap[SoundType.TOKEN_SAFE] = soundPool.load(context, R.raw.token_safe, 1)
            soundMap[SoundType.VICTORY] = soundPool.load(context, R.raw.victory, 1)
            soundMap[SoundType.DEFEAT] = soundPool.load(context, R.raw.lose, 1)
            soundMap[SoundType.BUTTON_CLICK] = soundPool.load(context, R.raw.button_click, 1)
            soundMap[SoundType.COIN_COLLECT] = soundPool.load(context, R.raw.coin_collect, 1)
            soundMap[SoundType.TOKEN_HOME] = soundPool.load(context, R.raw.token_home, 1)
            soundMap[SoundType.SIX_ROLLED] = soundPool.load(context, R.raw.six_rolled, 1)
            // Duplicate mappings for aliases
            soundMap[SoundType.DICE_ROLLING] = soundMap[SoundType.DICE_ROLL] ?: 0
            soundMap[SoundType.TOKEN_CAPTURED] = soundMap[SoundType.TOKEN_CAPTURE] ?: 0
        } catch (e: Exception) {
            // Sounds not available yet
        }
    }

    /**
     * Play a sound effect
     */
    fun play(soundType: SoundType, volume: Float = 1.0f, rate: Float = 1.0f) {
        if (!isSoundEnabled()) return
        
        soundMap[soundType]?.let { soundId ->
            val finalVolume = volume * sfxVolume * masterVolume
            soundPool.play(soundId, finalVolume, finalVolume, 1, 0, rate)
        }
    }

    /**
     * Play dice roll with varying pitch
     */
    fun playDiceRoll() {
        if (!isSoundEnabled()) return
        play(SoundType.DICE_ROLL, volume = 0.8f, rate = 0.9f + (Math.random() * 0.2f).toFloat())
    }

    /**
     * Play token move with distance-based volume
     */
    fun playTokenMove(steps: Int = 1) {
        if (!isSoundEnabled()) return
        val volume = (0.5f + steps * 0.1f).coerceAtMost(1.0f)
        play(SoundType.TOKEN_MOVE, volume = volume, rate = 1.0f + steps * 0.05f)
    }

    /**
     * Play capture sound
     */
    fun playCapture() {
        if (!isSoundEnabled()) return
        play(SoundType.TOKEN_CAPTURE, volume = 1.0f, rate = 1.0f)
        // Play second sound after short delay for impact
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            play(SoundType.TOKEN_CAPTURED, volume = 0.7f, rate = 0.8f)
        }, 100)
    }

    /**
     * Play six rolled (bonus)
     */
    fun playSixRolled() {
        if (!isSoundEnabled()) return
        play(SoundType.SIX_ROLLED, volume = 1.0f)
    }

    /**
     * Play token reached home
     */
    fun playTokenHome() {
        if (!isSoundEnabled()) return
        play(SoundType.TOKEN_HOME, volume = 1.0f)
    }

    /**
     * Play victory fanfare
     */
    fun playVictory() {
        if (!isSoundEnabled()) return
        play(SoundType.VICTORY, volume = 1.0f)
    }

    /**
     * Play defeat sound
     */
    fun playDefeat() {
        if (!isSoundEnabled()) return
        play(SoundType.DEFEAT, volume = 0.8f)
    }

    /**
     * Play button click
     */
    fun playButtonClick() {
        if (!isSoundEnabled()) return
        play(SoundType.BUTTON_CLICK, volume = 0.5f)
    }

    /**
     * Play coin collect
     */
    fun playCoinCollect(coins: Long) {
        if (!isSoundEnabled()) return
        val rate = 1.0f.coerceAtMost(0.8f + (coins / 100) * 0.1f)
        play(SoundType.COIN_COLLECT, volume = 0.8f, rate = rate)
    }

    /**
     * Play level up
     */
    fun playLevelUp() {
        if (!isSoundEnabled()) return
        // Play ascending tones
        play(SoundType.COIN_COLLECT, volume = 0.6f, rate = 1.0f)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            play(SoundType.COIN_COLLECT, volume = 0.8f, rate = 1.2f)
        }, 150)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            play(SoundType.COIN_COLLECT, volume = 1.0f, rate = 1.4f)
        }, 300)
    }

    /**
     * Play game start
     */
    fun playGameStart() {
        if (!isSoundEnabled()) return
        play(SoundType.GAME_START, volume = 1.0f)
    }

    /**
     * Play sound by name (for dynamic sound calls)
     */
    fun playSound(soundName: String) {
        if (!isSoundEnabled()) return
        when (soundName.lowercase()) {
            "dice_roll", "dice_rolling" -> playDiceRoll()
            "token_move" -> playTokenMove()
            "token_kill", "capture" -> playCapture()
            "token_home" -> playTokenHome()
            "six_rolled", "six" -> playSixRolled()
            "victory", "win" -> playVictory()
            "defeat", "lose" -> playDefeat()
            "button_click", "click" -> playButtonClick()
            "coin_collect", "coins" -> playCoinCollect(100)
            "level_up" -> playLevelUp()
            "game_start" -> playGameStart()
            else -> playButtonClick() // Default sound
        }
    }

    /**
     * Check if sound is enabled
     */
    private fun isSoundEnabled(): Boolean {
        // Use a simple check since we can't use coroutines here
        val prefs = context.getSharedPreferences("ludo_blitz_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("sound_enabled", true)
    }

    /**
     * Set master volume
     */
    fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
    }

    /**
     * Set SFX volume
     */
    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
    }

    /**
     * Release resources
     */
    fun release() {
        soundPool.release()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * Pause all sounds
     */
    fun pauseAll() {
        soundPool.autoPause()
        mediaPlayer?.pause()
    }

    /**
     * Resume all sounds
     */
    fun resumeAll() {
        soundPool.autoResume()
        mediaPlayer?.start()
    }
}
