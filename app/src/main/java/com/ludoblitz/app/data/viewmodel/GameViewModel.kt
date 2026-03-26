package com.ludoblitz.app.data.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ludoblitz.app.data.models.*
import com.ludoblitz.app.utils.GameEngine
import com.ludoblitz.app.utils.SoundManager
import com.ludoblitz.app.utils.SoundType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Game ViewModel - Manages game state and UI interactions
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext

    // Game configuration
    private var gameConfig = GameConfig()
    private var gameEngine = GameEngine(gameConfig)

    // Sound manager
    private val soundManager = SoundManager(context)

    // Game state
    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    // Game statistics
    private val _statistics = MutableStateFlow(GameStatistics())
    val statistics: StateFlow<GameStatistics> = _statistics.asStateFlow()

    // UI state
    private val _showExitDialog = MutableStateFlow(false)
    val showExitDialog: StateFlow<Boolean> = _showExitDialog.asStateFlow()

    private val _selectedTokenId = MutableStateFlow<Int?>(null)
    val selectedTokenId: StateFlow<Int?> = _selectedTokenId.asStateFlow()

    // AI turn job
    private var aiTurnJob: Job? = null

    // Settings
    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _musicEnabled = MutableStateFlow(true)
    val musicEnabled: StateFlow<Boolean> = _musicEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    /**
     * Start a new game with configuration
     */
    fun startGame(
        mode: GameMode,
        difficulty: AIDifficulty,
        playerCount: Int,
        playerNames: List<String> = emptyList()
    ) {
        gameConfig = GameConfig(
            mode = mode,
            tokensPerPlayer = if (mode == GameMode.QUICK) 2 else 4,
            playerCount = playerCount,
            aiDifficulty = difficulty,
            doubleTurnOnSix = true,
            threeSixBurn = true,
            safeZonesEnabled = true,
            captureBonus = true
        )

        gameEngine = GameEngine(gameConfig)
        val initialState = gameEngine.initializeGame()

        // Update player names if provided
        val updatedPlayers = initialState.players.mapIndexed { index, player ->
            if (playerNames.isNotEmpty() && index < playerNames.size) {
                player.copy(name = playerNames[index])
            } else {
                player
            }
        }

        _gameState.value = initialState.copy(players = updatedPlayers)
        _statistics.value = GameStatistics()

        // Play game start sound
        playSound(SoundType.GAME_START)
    }

    /**
     * Roll the dice
     */
    fun rollDice() {
        val state = _gameState.value ?: return

        if (state.phase != GamePhase.WAITING_FOR_ROLL) return
        if (state.isRolling) return

        viewModelScope.launch {
            // Start rolling animation
            _gameState.value = gameEngine.rollDice(state)

            // Play dice roll sound
            playSound(SoundType.DICE_ROLL)

            // Rolling animation delay
            delay(800)

            // Process the result
            val rolledState = _gameState.value ?: return@launch
            val processedState = gameEngine.processDiceRoll(rolledState)
            _gameState.value = processedState

            // Update statistics
            _statistics.value = _statistics.value.copy(
                totalRolls = _statistics.value.totalRolls + 1,
                totalSixes = if (processedState.lastRolledValue == 6)
                    _statistics.value.totalSixes + 1 else _statistics.value.totalSixes
            )

            // Play special sound for six
            if (processedState.lastRolledValue == 6) {
                playSound(SoundType.SIX_ROLLED)
            }

            // If AI's turn and has moves, execute AI move after delay
            if (processedState.phase == GamePhase.WAITING_FOR_TOKEN_SELECTION &&
                gameEngine.isCurrentPlayerAI(processedState)) {
                executeAITurn(processedState)
            }

            // If only one move and it's AI, auto-execute
            if (processedState.phase == GamePhase.WAITING_FOR_TOKEN_SELECTION &&
                gameEngine.canAutoSelect(processedState) &&
                gameEngine.isCurrentPlayerAI(processedState)) {
                delay(300)
                processedState.possibleMoves.firstOrNull()?.let { move ->
                    selectToken(move.token.id)
                }
            }
        }
    }

    /**
     * Select a token to move
     */
    fun selectToken(tokenId: Int) {
        val state = _gameState.value ?: return

        if (state.phase != GamePhase.WAITING_FOR_TOKEN_SELECTION) return

        val move = state.possibleMoves.find { it.token.id == tokenId }
        if (move == null) {
            playSound(SoundType.ERROR)
            return
        }

        _selectedTokenId.value = tokenId

        viewModelScope.launch {
            delay(200) // Brief delay for visual feedback

            val newState = gameEngine.executeMove(state, move)
            _gameState.value = newState

            // Play appropriate sound
            when {
                move.isCapture -> {
                    playSound(SoundType.TOKEN_CAPTURE)
                    _statistics.value = _statistics.value.copy(
                        totalCaptures = _statistics.value.totalCaptures + 1
                    )
                }
                move.isFinishing -> {
                    playSound(SoundType.TOKEN_HOME)
                }
                else -> {
                    playSound(SoundType.TOKEN_MOVE)
                }
            }

            // Check for game over
            if (newState.phase == GamePhase.GAME_OVER) {
                playSound(SoundType.VICTORY)
                _statistics.value = _statistics.value.copy(
                    gameEndTime = System.currentTimeMillis()
                )
                return@launch
            }

            // Handle next turn or extra turn
            delay(500)

            when {
                newState.phase == GamePhase.NEXT_TURN -> {
                    val nextTurnState = gameEngine.nextTurn(newState)
                    _gameState.value = nextTurnState

                    // If next player is AI, trigger their turn
                    if (gameEngine.isCurrentPlayerAI(nextTurnState)) {
                        delay(500)
                        rollDice()
                    }
                }
                newState.phase == GamePhase.WAITING_FOR_ROLL && newState.extraTurn -> {
                    // Same player gets another turn
                    if (gameEngine.isCurrentPlayerAI(newState)) {
                        delay(500)
                        rollDice()
                    }
                }
            }

            _selectedTokenId.value = null
        }
    }

    /**
     * Execute AI turn
     */
    private fun executeAITurn(state: GameState) {
        aiTurnJob?.cancel()

        aiTurnJob = viewModelScope.launch {
            delay(500) // Thinking delay

            val currentPlayer = gameEngine.getCurrentPlayer(state)
            val move = gameEngine.getAIMove(state, currentPlayer.aiDifficulty)

            if (move != null) {
                selectToken(move.token.id)
            }
        }
    }

    /**
     * Show exit dialog
     */
    fun showExitDialog() {
        _showExitDialog.value = true
    }

    /**
     * Hide exit dialog
     */
    fun hideExitDialog() {
        _showExitDialog.value = false
    }

    /**
     * Confirm exit
     */
    fun confirmExit() {
        _showExitDialog.value = false
        _gameState.value = null
        aiTurnJob?.cancel()
    }

    /**
     * Toggle sound
     */
    fun toggleSound() {
        _soundEnabled.value = !_soundEnabled.value
        soundManager.setSoundEnabled(_soundEnabled.value)
    }

    /**
     * Toggle music
     */
    fun toggleMusic() {
        _musicEnabled.value = !_musicEnabled.value
        soundManager.setMusicEnabled(_musicEnabled.value)
    }

    /**
     * Toggle vibration
     */
    fun toggleVibration() {
        _vibrationEnabled.value = !_vibrationEnabled.value
        soundManager.setVibrationEnabled(_vibrationEnabled.value)
    }

    /**
     * Play a sound
     */
    private fun playSound(soundType: SoundType) {
        if (_soundEnabled.value) {
            soundManager.playSound(soundType)
        }
    }

    /**
     * Get formatted game duration
     */
    fun getGameDuration(): String {
        val stats = _statistics.value
        if (stats.gameEndTime == 0L) {
            val duration = System.currentTimeMillis() - stats.gameStartTime
            return formatDuration(duration)
        }
        return formatDuration(stats.gameEndTime - stats.gameStartTime)
    }

    /**
     * Format duration in MM:SS format
     */
    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
        aiTurnJob?.cancel()
    }
}
