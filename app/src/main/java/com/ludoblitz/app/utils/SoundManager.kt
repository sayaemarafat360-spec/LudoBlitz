package com.ludoblitz.app.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.MediaPlayer
import androidx.compose.runtime.*
import com.ludoblitz.app.R
import com.ludoblitz.app.data.models.SoundType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sound Manager - Handles all game audio
 */
class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var bgMusicPlayer: MediaPlayer? = null

    private val soundIds = mutableMapOf<SoundType, Int>()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _isMusicEnabled = MutableStateFlow(true)
    val isMusicEnabled: StateFlow<Boolean> = _isMusicEnabled.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(true)
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    init {
        initializeSoundPool()
    }

    /**
     * Initialize the sound pool
     */
    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load all sounds
        loadSounds()
    }

    /**
     * Load all game sounds
     */
    private fun loadSounds() {
        soundPool?.let { pool ->
            soundIds[SoundType.DICE_ROLL] = pool.load(context, R.raw.dice_roll, 1)
            soundIds[SoundType.BUTTON_CLICK] = pool.load(context, R.raw.button_click, 1)
            soundIds[SoundType.TOKEN_MOVE] = pool.load(context, R.raw.token_move, 1)
            soundIds[SoundType.TOKEN_CAPTURE] = pool.load(context, R.raw.capture, 1)
            soundIds[SoundType.TOKEN_HOME] = pool.load(context, R.raw.token_home, 1)
            soundIds[SoundType.VICTORY] = pool.load(context, R.raw.victory, 1)
            soundIds[SoundType.SIX_ROLLED] = pool.load(context, R.raw.six_rolled, 1)
            soundIds[SoundType.GAME_START] = pool.load(context, R.raw.game_start, 1)
            soundIds[SoundType.ERROR] = pool.load(context, R.raw.error, 1)
            soundIds[SoundType.COIN_COLLECT] = pool.load(context, R.raw.coin_collect, 1)
            soundIds[SoundType.POP] = pool.load(context, R.raw.pop, 1)
        }
    }

    /**
     * Play a sound effect
     */
    fun playSound(soundType: SoundType) {
        if (!_isSoundEnabled.value) return

        soundIds[soundType]?.let { soundId ->
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    /**
     * Play dice roll sound with variation
     */
    fun playDiceRoll() {
        playSound(SoundType.DICE_ROLL)
    }

    /**
     * Play token move sound
     */
    fun playTokenMove() {
        playSound(SoundType.TOKEN_MOVE)
    }

    /**
     * Play capture sound
     */
    fun playCapture() {
        playSound(SoundType.TOKEN_CAPTURE)
    }

    /**
     * Play token home sound
     */
    fun playTokenHome() {
        playSound(SoundType.TOKEN_HOME)
    }

    /**
     * Play victory sound
     */
    fun playVictory() {
        playSound(SoundType.VICTORY)
    }

    /**
     * Play six rolled sound
     */
    fun playSixRolled() {
        playSound(SoundType.SIX_ROLLED)
    }

    /**
     * Play game start sound
     */
    fun playGameStart() {
        playSound(SoundType.GAME_START)
    }

    /**
     * Play button click sound
     */
    fun playButtonClick() {
        playSound(SoundType.BUTTON_CLICK)
    }

    /**
     * Play error sound
     */
    fun playError() {
        playSound(SoundType.ERROR)
    }

    /**
     * Play coin collect sound
     */
    fun playCoinCollect() {
        playSound(SoundType.COIN_COLLECT)
    }

    /**
     * Toggle sound effects
     */
    fun toggleSound() {
        _isSoundEnabled.value = !_isSoundEnabled.value
    }

    /**
     * Set sound enabled state
     */
    fun setSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
    }

    /**
     * Toggle background music
     */
    fun toggleMusic() {
        _isMusicEnabled.value = !_isMusicEnabled.value
        if (_isMusicEnabled.value) {
            startBackgroundMusic()
        } else {
            stopBackgroundMusic()
        }
    }

    /**
     * Set music enabled state
     */
    fun setMusicEnabled(enabled: Boolean) {
        _isMusicEnabled.value = enabled
        if (enabled) {
            startBackgroundMusic()
        } else {
            stopBackgroundMusic()
        }
    }

    /**
     * Start background music
     */
    private fun startBackgroundMusic() {
        // Background music would be implemented here
        // For now, it's a placeholder
    }

    /**
     * Stop background music
     */
    private fun stopBackgroundMusic() {
        bgMusicPlayer?.pause()
    }

    /**
     * Toggle vibration
     */
    fun toggleVibration() {
        _isVibrationEnabled.value = !_isVibrationEnabled.value
    }

    /**
     * Set vibration enabled state
     */
    fun setVibrationEnabled(enabled: Boolean) {
        _isVibrationEnabled.value = enabled
    }

    /**
     * Release all resources
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        bgMusicPlayer?.release()
        bgMusicPlayer = null
    }
}

/**
 * Composable function to remember SoundManager instance
 */
@Composable
fun rememberSoundManager(context: Context): SoundManager {
    val soundManager = remember { SoundManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    return soundManager
}
