package com.ludoblitz.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ludoblitz.app.data.model.*
import com.ludoblitz.app.data.local.LocalUserRepository
import com.ludoblitz.app.domain.ai.AIFactory
import com.ludoblitz.app.domain.ai.AIDecision
import com.ludoblitz.app.domain.gamelogic.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Local Game ViewModel - Manages game state without Firebase
 */
@HiltViewModel
class LocalGameViewModel @Inject constructor(
    private val gameEngine: LudoGameEngine,
    private val userRepository: LocalUserRepository
) : ViewModel() {

    // Game state
    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game.asStateFlow()
    
    // Current player
    private val _currentPlayer = MutableStateFlow<Player?>(null)
    val currentPlayer: StateFlow<Player?> = _currentPlayer.asStateFlow()
    
    // Current player index
    private val _currentPlayerIndex = MutableStateFlow(0)
    val currentPlayerIndex: StateFlow<Int> = _currentPlayerIndex.asStateFlow()
    
    // Dice value
    private val _diceValue = MutableStateFlow(0)
    val diceValue: StateFlow<Int> = _diceValue.asStateFlow()
    
    // Game state
    private val _gameState = MutableStateFlow<GameState>(GameState.WaitingForRoll)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    // Events
    private val _events = MutableStateFlow<GameEvent>(GameEvent.None)
    val events: StateFlow<GameEvent> = _events.asStateFlow()
    
    // Valid moves
    private val _validMoves = MutableStateFlow<List<ValidMove>>(emptyList())
    val validMoves: StateFlow<List<ValidMove>> = _validMoves.asStateFlow()
    
    // AI players
    private val aiPlayers = mutableMapOf<String, LudoAI>()
    
    // Game configuration
    private var gameRules = GameRules()
    private var isPlayerTurn = true
    private var lastDiceValue = 0
    private var consecutiveSixes = 0

    /**
     * Initialize a new game
     */
    fun initializeGame(playerCount: Int, difficulty: BotDifficulty, classicMode: Boolean) {
        gameRules = GameRules(requireSixToRelease = classicMode)
        
        val colors = TokenColor.values().take(playerCount)
        val players = mutableListOf<Player>()
        
        colors.forEachIndexed { index, color ->
            val tokens = (1..4).map { tokenId ->
                Token(id = tokenId, color = color)
            }
            
            val isBot = index > 0 // First player is always human
            val playerName = if (isBot) {
                "Bot ${index}"
            } else {
                "You"
            }
            
            val player = Player(
                id = if (isBot) "bot_$index" else "player_1",
                name = playerName,
                avatarUrl = "",
                color = color,
                tokens = tokens,
                isBot = isBot,
                botDifficulty = if (isBot) difficulty else BotDifficulty.EASY
            )
            
            players.add(player)
            
            // Initialize AI for bot players
            if (isBot) {
                aiPlayers["bot_$index"] = AIFactory.createAI(difficulty, gameEngine)
            }
        }
        
        val newGame = gameEngine.createNewGame(
            players = players,
            gameMode = GameMode.VS_AI,
            rules = gameRules
        )
        
        _game.value = newGame
        _currentPlayerIndex.value = 0
        _currentPlayer.value = newGame.players[0]
        _gameState.value = GameState.WaitingForRoll
        _diceValue.value = 0
        _validMoves.value = emptyList()
        consecutiveSixes = 0
        isPlayerTurn = true
    }

    /**
     * Check if player can roll dice
     */
    fun canRollDice(): Boolean {
        val state = _gameState.value
        val player = _currentPlayer.value ?: return false
        return state is GameState.WaitingForRoll && !player.isBot
    }

    /**
     * Roll the dice
     */
    fun rollDice(): Int {
        val value = gameEngine.rollDice()
        lastDiceValue = value
        _diceValue.value = value
        
        val game = _game.value ?: return value
        val player = _currentPlayer.value ?: return value
        
        // Check for consecutive sixes
        if (value == 6) {
            consecutiveSixes++
            
            if (consecutiveSixes >= 3 && gameRules.threeSixesRule) {
                // Skip turn
                viewModelScope.launch {
                    _events.value = GameEvent.RolledSix
                    delay(1000)
                    consecutiveSixes = 0
                    nextTurn()
                }
                return value
            }
            
            _events.value = GameEvent.RolledSix
        } else {
            consecutiveSixes = 0
        }
        
        // Get valid moves
        val moves = gameEngine.getValidMoves(player, value)
        _validMoves.value = moves
        
        if (moves.isEmpty()) {
            // No valid moves, end turn
            viewModelScope.launch {
                delay(1500)
                nextTurn()
            }
        } else {
            _gameState.value = GameState.SelectingMove(
                _currentPlayerIndex.value,
                value,
                moves
            )
            
            // If AI turn, make AI decision
            if (player.isBot) {
                makeAIMove(player, value)
            }
        }
        
        return value
    }

    /**
     * Check if has valid moves
     */
    fun hasValidMoves(): Boolean {
        return _validMoves.value.isNotEmpty()
    }

    /**
     * Check if token can move
     */
    fun canMoveToken(tokenIndex: Int): Boolean {
        return _validMoves.value.any { it.tokenIndex == tokenIndex }
    }

    /**
     * Move a token
     */
    fun moveToken(tokenIndex: Int): MoveResult {
        val game = _game.value ?: throw IllegalStateException("Game not initialized")
        val playerIndex = _currentPlayerIndex.value
        val diceVal = _diceValue.value
        
        viewModelScope.launch(Dispatchers.Default) {
            val result = gameEngine.moveToken(game, playerIndex, tokenIndex, diceVal)
            
            // Update game
            _game.value = result.game
            
            // Handle events
            if (result.capturedTokens.isNotEmpty()) {
                _events.value = GameEvent.TokenCaptured
                userRepository.recordCapture()
            }
            
            if (result.tokenReachedHome) {
                _events.value = GameEvent.TokenHome
                userRepository.recordTokenHome()
            }
            
            if (result.isGameOver) {
                val winner = result.game.players[playerIndex]
                _gameState.value = GameState.GameOver(winner, emptyList())
                _events.value = GameEvent.Victory
                return@launch
            }
            
            // Check for bonus turn
            if (result.bonusTurn && !result.threeConsecutiveSixes) {
                _events.value = GameEvent.BonusTurn
                // Reset for next roll
                _gameState.value = GameState.WaitingForRoll
                _validMoves.value = emptyList()
                
                // If AI, continue playing
                val player = result.game.players[playerIndex]
                if (player.isBot) {
                    delay(800)
                    rollDice()
                }
            } else {
                delay(500)
                nextTurn()
            }
        }
        
        // Return placeholder result
        return MoveResult(true, game, emptyList(), false, false)
    }

    /**
     * Skip turn
     */
    fun skipTurn() {
        nextTurn()
    }

    /**
     * Next turn
     */
    fun nextTurn() {
        val game = _game.value ?: return
        
        val nextIndex = gameEngine.getNextPlayerIndex(game)
        val nextPlayer = game.players[nextIndex]
        
        _currentPlayerIndex.value = nextIndex
        _currentPlayer.value = nextPlayer
        _gameState.value = GameState.WaitingForRoll
        _validMoves.value = emptyList()
        _diceValue.value = 0
        consecutiveSixes = 0
        isPlayerTurn = !nextPlayer.isBot
        
        // If AI turn, start AI logic
        if (nextPlayer.isBot) {
            viewModelScope.launch {
                _events.value = GameEvent.AIThinking
                delay(500)
                rollDice()
            }
        }
    }

    /**
     * Make AI move decision
     */
    private fun makeAIMove(player: Player, diceValue: Int) {
        viewModelScope.launch {
            val ai = aiPlayers[player.id] ?: return@launch
            
            delay(800) // Thinking delay
            
            val decision = ai.makeMoveDecision(
                game = _game.value!!,
                player = player,
                diceValue = diceValue
            )
            
            if (decision.skipTurn) {
                delay(500)
                nextTurn()
            } else {
                moveToken(decision.tokenIndex)
            }
        }
    }

    /**
     * Get best move for auto-select
     */
    fun getBestMove(): ValidMove? {
        return _validMoves.value.firstOrNull()
    }

    /**
     * Get last dice value
     */
    fun getLastDiceValue(): Int = lastDiceValue

    /**
     * Get current game
     */
    fun getGame(): Game? = _game.value

    /**
     * Get current player ID
     */
    fun getCurrentPlayerId(): String? = _currentPlayer.value?.id

    /**
     * Record win
     */
    fun recordWin(coins: Long, xp: Long) {
        viewModelScope.launch {
            userRepository.recordGamePlayed(won = true)
            userRepository.addCoins(coins)
            val leveledUp = userRepository.addXp(xp)
            
            if (leveledUp) {
                _events.value = GameEvent.LevelUp
            }
            
            _events.value = GameEvent.CoinsEarned(coins)
        }
    }

    /**
     * Record loss
     */
    fun recordLoss() {
        viewModelScope.launch {
            userRepository.recordGamePlayed(won = false)
        }
    }

    /**
     * Pause game
     */
    fun pauseGame() {
        _game.value?.let {
            _game.value = it.copy(gameStatus = GameStatus.PAUSED)
        }
    }

    /**
     * Resume game
     */
    fun resumeGame() {
        _game.value?.let {
            _game.value = it.copy(gameStatus = GameStatus.IN_PROGRESS)
        }
    }

    /**
     * Handle game over
     */
    fun handleGameOver() {
        _gameState.value = GameState.GameOver(
            winner = _currentPlayer.value!!,
            rankings = emptyList()
        )
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        aiPlayers.clear()
    }
}

/**
 * Game events
 */
sealed class GameEvent {
    object None : GameEvent()
    object RolledSix : GameEvent()
    object BonusTurn : GameEvent()
    object TokenCaptured : GameEvent()
    object TokenHome : GameEvent()
    object Victory : GameEvent()
    object Defeat : GameEvent()
    object LevelUp : GameEvent()
    object AIThinking : GameEvent()
    data class CoinsEarned(val amount: Long) : GameEvent()
    data class XpEarned(val amount: Long) : GameEvent()
}
