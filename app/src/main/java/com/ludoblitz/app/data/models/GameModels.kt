package com.ludoblitz.app.data.models

import androidx.compose.ui.graphics.Color
import com.ludoblitz.app.ui.theme.*

/**
 * Player color enum representing the four player positions on the board
 */
enum class PlayerColor(val color: Color, val lightColor: Color, val homeColor: Color) {
    RED(TokenRed, TokenRedLight, HomeRed),
    GREEN(TokenGreen, TokenGreenLight, HomeGreen),
    YELLOW(TokenYellow, TokenYellowLight, HomeYellow),
    BLUE(TokenBlue, TokenBlueLight, HomeBlue)
}

/**
 * Token state enum
 */
enum class TokenState {
    HOME,       // Token is in home base
    ACTIVE,     // Token is on the board
    FINISHED    // Token has reached the center (home)
}

/**
 * Token data class
 */
data class Token(
    val id: Int,
    val playerColor: PlayerColor,
    val position: Int = -1, // Board position: -1 = home base, 0-51 = main track, -2 = finished
    val state: TokenState = TokenState.HOME,
    val stepsFromStart: Int = 0 // Steps taken from leaving home (used for home column entry)
)

/**
 * Player data class
 */
data class Player(
    val id: Int,
    val name: String,
    val color: PlayerColor,
    val tokens: List<Token>,
    val isAI: Boolean = false,
    val aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM
)

/**
 * AI Difficulty levels
 */
enum class AIDifficulty {
    EASY,    // Random moves
    MEDIUM,  // Basic strategy
    HARD     // Advanced strategy with lookahead
}

/**
 * Game phase enum
 */
enum class GamePhase {
    WAITING_FOR_ROLL,
    ROLLING,
    WAITING_FOR_TOKEN_SELECTION,
    MOVING_TOKEN,
    CHECKING_CAPTURE,
    CHECKING_WIN,
    NEXT_TURN,
    GAME_OVER
}

/**
 * Game mode enum
 */
enum class GameMode {
    CLASSIC,  // 4 tokens per player
    QUICK     // 2 tokens per player
}

/**
 * Game configuration
 */
data class GameConfig(
    val mode: GameMode = GameMode.CLASSIC,
    val playerCount: Int = 4,
    val tokensPerPlayer: Int = 4,
    val doubleTurnOnSix: Boolean = true,
    val threeSixBurn: Boolean = true,
    val safeZonesEnabled: Boolean = true,
    val captureBonus: Boolean = true,
    val aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM
)

/**
 * Game state
 */
data class GameState(
    val players: List<Player>,
    val currentPlayerIndex: Int = 0,
    val diceValue: Int = 1,
    val isRolling: Boolean = false,
    val phase: GamePhase = GamePhase.WAITING_FOR_ROLL,
    val winner: Player? = null,
    val consecutiveSixes: Int = 0,
    val extraTurn: Boolean = false,
    val lastRolledValue: Int = 0,
    val possibleMoves: List<TokenMove> = emptyList(),
    val message: String = "",
    val turnCount: Int = 0
)

/**
 * Token move data class
 */
data class TokenMove(
    val token: Token,
    val fromPosition: Int,
    val toPosition: Int,
    val steps: Int,
    val isCapture: Boolean = false,
    val capturedToken: Token? = null,
    val isEnteringBoard: Boolean = false,
    val isFinishing: Boolean = false
)

/**
 * Extended TokenMove with additional properties for game logic
 */
data class TokenMoveExtended(
    val token: Token,
    val fromPosition: Int,
    val toPosition: Int,
    val steps: Int,
    val isCapture: Boolean = false,
    val capturedToken: Token? = null,
    val isEnteringBoard: Boolean = false,
    val isFinishing: Boolean = false,
    val isEnteringHomeColumn: Boolean = false,
    val homeColumnPosition: Int = -1
)

/**
 * Game statistics
 */
data class GameStatistics(
    val totalRolls: Int = 0,
    val totalCaptures: Int = 0,
    val totalSixes: Int = 0,
    val gameStartTime: Long = System.currentTimeMillis(),
    var gameEndTime: Long = 0
)

/**
 * Board position data
 */
data class BoardPosition(
    val index: Int,
    val x: Float,
    val y: Float,
    val isSafe: Boolean = false,
    val isStart: Boolean = false,
    val isHomeEntry: Boolean = false,
    val color: PlayerColor? = null
)

/**
 * Sound types for the game
 */
enum class SoundType {
    DICE_ROLL,
    BUTTON_CLICK,
    TOKEN_MOVE,
    TOKEN_CAPTURE,
    TOKEN_HOME,
    VICTORY,
    SIX_ROLLED,
    GAME_START,
    ERROR,
    COIN_COLLECT,
    POP
}
