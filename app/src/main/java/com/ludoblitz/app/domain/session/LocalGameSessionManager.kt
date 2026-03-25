package com.ludoblitz.app.domain.session

import com.ludoblitz.app.data.model.*
import com.ludoblitz.app.domain.ai.LudoAI
import com.ludoblitz.app.domain.gamelogic.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Local Game Session Manager
 * Handles complete game sessions including local multiplayer and AI opponents
 * No Firebase required - all game state managed locally
 */
@Singleton
class LocalGameSessionManager @Inject constructor(
    private val gameEngine: LudoGameEngine,
    private val enhancedEngine: EnhancedGameEngine,
    private val ai: LudoAI
) {
    // Current game state
    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gameState: StateFlow<GameState> = _gameState
    
    // Current game
    private val _currentGame = MutableStateFlow<Game?>(null)
    val currentGame: StateFlow<Game?> = _currentGame
    
    // Game events for UI
    private val _gameEvents = MutableSharedFlow<GameEvent>()
    val gameEvents: SharedFlow<GameEvent> = _gameEvents
    
    // Game scope for coroutines
    private var gameScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // AI difficulty for current game
    private var aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM
    
    /**
     * Start a new local game
     */
    fun startLocalGame(
        numPlayers: Int,
        playerNames: List<String>,
        playerColors: List<TokenColor>,
        rules: GameRules = GameRules(),
        aiPlayers: List<Int> = emptyList() // Indices of AI players
    ): Game {
        // Create players
        val players = mutableListOf<Player>()
        
        for (i in 0 until numPlayers) {
            val isAI = i in aiPlayers
            val tokens = List(4) { Token(
                id = "${playerColors[i]}_$i",
                color = playerColors[i],
                position = -1,
                stepsTaken = -1,
                isHome = false,
                isInSafeZone = false
            )}
            
            players.add(Player(
                id = "player_$i",
                name = playerNames.getOrElse(i) { "Player ${i + 1}" },
                color = playerColors[i],
                tokens = tokens,
                isAI = isAI,
                consecutiveSixes = 0,
                totalMoves = 0,
                captures = 0
            ))
        }
        
        // Create game
        val game = gameEngine.createNewGame(
            players = players,
            gameMode = if (aiPlayers.isNotEmpty()) GameMode.VS_AI else GameMode.LOCAL,
            rules = rules
        )
        
        _currentGame.value = game
        _gameState.value = GameState.WaitingForRoll(0)
        
        // Emit game start event
        gameScope.launch {
            _gameEvents.emit(GameEvent.GameStarted(game))
        }
        
        return game
    }
    
    /**
     * Start a quick AI game
     */
    fun startAIGame(
        difficulty: AIDifficulty,
        playerCount: Int = 4,
        playerName: String = "You",
        rules: GameRules = GameRules()
    ): Game {
        aiDifficulty = difficulty
        
        val colors = listOf(TokenColor.RED, TokenColor.GREEN, TokenColor.YELLOW, TokenColor.BLUE)
        val names = mutableListOf(playerName)
        val aiIndices = mutableListOf<Int>()
        
        // Add AI names
        for (i in 1 until playerCount) {
            names.add("Bot ${i}")
            aiIndices.add(i)
        }
        
        return startLocalGame(
            numPlayers = playerCount,
            playerNames = names,
            playerColors = colors.take(playerCount),
            rules = rules,
            aiPlayers = aiIndices
        )
    }
    
    /**
     * Roll dice for current player
     */
    suspend fun rollDice(): Int {
        val game = _currentGame.value ?: return 0
        val state = _gameState.value
        
        if (state !is GameState.WaitingForRoll) return 0
        
        val playerIndex = state.playerIndex
        val diceValue = gameEngine.rollDice()
        
        // Update game with dice value
        _currentGame.value = game.copy(
            diceValue = diceValue,
            currentPlayerIndex = playerIndex
        )
        
        // Check for six
        if (diceValue == 6) {
            _gameEvents.emit(GameEvent.SixRolled(playerIndex))
        }
        
        // Get valid moves
        val player = game.players[playerIndex]
        val validMoves = gameEngine.getValidMoves(player, diceValue)
        
        if (validMoves.isEmpty()) {
            // No valid moves - skip turn
            _gameEvents.emit(GameEvent.NoValidMoves(playerIndex))
            endTurn(diceValue == 6)
        } else {
            _gameState.value = GameState.SelectingMove(playerIndex, diceValue, validMoves)
            
            // If AI player, auto-select move
            if (player.isAI) {
                delay(500) // Thinking delay
                val move = enhancedEngine.getStrategicMove(
                    player, diceValue, game.players, aiDifficulty
                )
                move?.let { selectToken(it.tokenIndex) }
            } else {
                _gameEvents.emit(GameEvent.DiceRolled(playerIndex, diceValue, validMoves))
            }
        }
        
        return diceValue
    }
    
    /**
     * Select a token to move
     */
    suspend fun selectToken(tokenIndex: Int): Boolean {
        val game = _currentGame.value ?: return false
        val state = _gameState.value
        
        if (state !is GameState.SelectingMove) return false
        
        val playerIndex = state.playerIndex
        val diceValue = state.diceValue
        val player = game.players[playerIndex]
        val token = player.tokens[tokenIndex]
        
        // Check if move is valid
        val validMoves = state.validMoves.filter { it.tokenIndex == tokenIndex }
        if (validMoves.isEmpty()) return false
        
        // Execute move
        _gameState.value = GameState.Moving(playerIndex, tokenIndex)
        
        val moveResult = if (token.isInBase()) {
            gameEngine.releaseToken(game, playerIndex, tokenIndex)
        } else {
            gameEngine.moveToken(game, playerIndex, tokenIndex, diceValue)
        }
        
        if (moveResult.success) {
            _currentGame.value = moveResult.game
            
            // Emit events
            if (moveResult.capturedTokens.isNotEmpty()) {
                _gameEvents.emit(GameEvent.TokenCaptured(
                    playerIndex, tokenIndex, moveResult.capturedTokens
                ))
            }
            
            if (moveResult.tokenReachedHome) {
                _gameEvents.emit(GameEvent.TokenHome(playerIndex, tokenIndex))
            }
            
            if (moveResult.isGameOver) {
                endGame(moveResult.game.players[playerIndex])
                return true
            }
            
            if (moveResult.threeConsecutiveSixes) {
                _gameEvents.emit(GameEvent.ThreeSixes(playerIndex))
                endTurn(false)
            } else {
                endTurn(moveResult.bonusTurn)
            }
        }
        
        return moveResult.success
    }
    
    /**
     * End current turn
     */
    private suspend fun endTurn(bonusTurn: Boolean) {
        val game = _currentGame.value ?: return
        
        if (bonusTurn) {
            // Same player rolls again
            _gameState.value = GameState.WaitingForRoll(game.currentPlayerIndex)
            _gameEvents.emit(GameEvent.BonusTurn(game.currentPlayerIndex))
            
            // If AI, auto-roll
            val player = game.players[game.currentPlayerIndex]
            if (player.isAI) {
                delay(800)
                rollDice()
            }
        } else {
            // Next player
            val nextIndex = gameEngine.getNextPlayerIndex(game)
            _currentGame.value = game.copy(currentPlayerIndex = nextIndex)
            _gameState.value = GameState.WaitingForRoll(nextIndex)
            _gameEvents.emit(GameEvent.TurnChanged(nextIndex))
            
            // If AI, auto-roll
            val nextPlayer = game.players[nextIndex]
            if (nextPlayer.isAI) {
                delay(1000)
                rollDice()
            }
        }
    }
    
    /**
     * End the game
     */
    private suspend fun endGame(winner: Player) {
        val game = _currentGame.value ?: return
        
        // Calculate rankings
        val rankings = game.players.sortedByDescending { player ->
            player.tokens.count { it.isHome }
        }
        
        _gameState.value = GameState.GameOver(winner, rankings)
        _gameEvents.emit(GameEvent.GameEnded(winner, rankings))
    }
    
    /**
     * Get current game state for saving
     */
    fun getSavedGameState(): SavedGameState? {
        val game = _currentGame.value ?: return null
        val state = _gameState.value
        
        return SavedGameState(
            game = game,
            gameState = state,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Restore game from saved state
     */
    fun restoreGame(savedState: SavedGameState) {
        _currentGame.value = savedState.game
        _gameState.value = savedState.gameState
        
        gameScope.launch {
            _gameEvents.emit(GameEvent.GameRestored(savedState.game))
        }
    }
    
    /**
     * Pause current game
     */
    fun pauseGame() {
        _gameState.value = GameState.Paused
    }
    
    /**
     * Resume game
     */
    fun resumeGame() {
        val game = _currentGame.value ?: return
        _gameState.value = GameState.WaitingForRoll(game.currentPlayerIndex)
    }
    
    /**
     * Quit current game
     */
    fun quitGame() {
        _currentGame.value = null
        _gameState.value = GameState.Idle
        gameScope.coroutineContext.cancelChildren()
    }
    
    /**
     * Clean up
     */
    fun cleanup() {
        gameScope.cancel()
    }
}

/**
 * Game states
 */
sealed class GameState {
    object Idle : GameState()
    data class WaitingForRoll(val playerIndex: Int) : GameState()
    data class SelectingMove(
        val playerIndex: Int,
        val diceValue: Int,
        val validMoves: List<ValidMove>
    ) : GameState()
    data class Moving(val playerIndex: Int, val tokenIndex: Int) : GameState()
    data class TurnComplete(val playerIndex: Int, val bonusTurn: Boolean) : GameState()
    data class GameOver(val winner: Player, val rankings: List<Player>) : GameState()
    object Paused : GameState()
}

/**
 * Game events for UI updates
 */
sealed class GameEvent {
    data class GameStarted(val game: Game) : GameEvent()
    data class DiceRolled(val playerIndex: Int, val value: Int, val validMoves: List<ValidMove>) : GameEvent()
    data class TokenSelected(val playerIndex: Int, val tokenIndex: Int) : GameEvent()
    data class TokenMoved(val playerIndex: Int, val tokenIndex: Int, val from: Int, val to: Int) : GameEvent()
    data class TokenCaptured(
        val playerIndex: Int,
        val tokenIndex: Int,
        val capturedTokens: List<CapturedToken>
    ) : GameEvent()
    data class TokenHome(val playerIndex: Int, val tokenIndex: Int) : GameEvent()
    data class SixRolled(val playerIndex: Int) : GameEvent()
    data class ThreeSixes(val playerIndex: Int) : GameEvent()
    data class NoValidMoves(val playerIndex: Int) : GameEvent()
    data class BonusTurn(val playerIndex: Int) : GameEvent()
    data class TurnChanged(val playerIndex: Int) : GameEvent()
    data class GameEnded(val winner: Player, val rankings: List<Player>) : GameEvent()
    data class GameRestored(val game: Game) : GameEvent()
}

/**
 * Saved game state for persistence
 */
data class SavedGameState(
    val game: Game,
    val gameState: GameState,
    val timestamp: Long
)
