package com.ludoblitz.app.domain.gamelogic

import android.content.Context
import com.ludoblitz.app.data.local.PreferenceManager
import com.ludoblitz.app.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.random.Random

/**
 * Enhanced Game Engine with advanced Ludo rules and mechanics
 * Handles complex game scenarios including blocking, stacking, and special rules
 */
@Singleton
class EnhancedGameEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager
) {
    companion object {
        // Board constants
        const val TOTAL_POSITIONS = 52
        const val HOME_STRETCH_LENGTH = 5
        const val TOKENS_PER_PLAYER = 4
        
        // Safe positions (stars)
        val SAFE_POSITIONS = setOf(1, 9, 14, 22, 27, 35, 40, 48)
        
        // Starting positions for each color
        val START_POSITIONS = mapOf(
            TokenColor.RED to 1,
            TokenColor.GREEN to 14,
            TokenColor.YELLOW to 27,
            TokenColor.BLUE to 40
        )
        
        // Home entry positions
        val HOME_ENTRY_POSITIONS = mapOf(
            TokenColor.RED to 51,
            TokenColor.GREEN to 12,
            TokenColor.YELLOW to 25,
            TokenColor.BLUE to 38
        )
        
        // XP and Coin rewards
        const val XP_PER_MOVE = 1
        const val XP_PER_CAPTURE = 15
        const val XP_PER_TOKEN_HOME = 25
        const val XP_PER_WIN = 100
        const val COINS_PER_CAPTURE = 5
        const val COINS_PER_TOKEN_HOME = 10
        const val COINS_PER_WIN_BASE = 50
        const val COINS_PER_SIX = 2
    }
    
    /**
     * Calculate board position coordinates
     */
    fun getBoardCoordinates(position: Int, color: TokenColor): Pair<Int, Int> {
        // Board is 15x15 grid
        // Returns (column, row) coordinates
        
        val adjustedPos = when {
            position < 0 -> return getBaseCoordinates(color)
            position >= 57 -> return getHomeCoordinates(color)
            position > 51 -> position // Home stretch
            else -> position
        }
        
        // Simplified coordinate calculation
        // In production, this would be more accurate
        return when (color) {
            TokenColor.RED -> {
                when {
                    position in 1..5 -> Pair(position, 7)
                    position in 52..56 -> Pair(position - 51, 7)
                    else -> calculateMainTrackCoords(position)
                }
            }
            TokenColor.GREEN -> {
                when {
                    position in 52..56 -> Pair(7, position - 51)
                    else -> calculateMainTrackCoords(position)
                }
            }
            TokenColor.YELLOW -> {
                when {
                    position in 52..56 -> Pair(14 - (position - 51), 7)
                    else -> calculateMainTrackCoords(position)
                }
            }
            TokenColor.BLUE -> {
                when {
                    position in 52..56 -> Pair(7, 14 - (position - 51))
                    else -> calculateMainTrackCoords(position)
                }
            }
        }
    }
    
    private fun getBaseCoordinates(color: TokenColor): Pair<Int, Int> {
        return when (color) {
            TokenColor.RED -> Pair(1, 1)
            TokenColor.GREEN -> Pair(13, 1)
            TokenColor.YELLOW -> Pair(13, 13)
            TokenColor.BLUE -> Pair(1, 13)
        }
    }
    
    private fun getHomeCoordinates(color: TokenColor): Pair<Int, Int> {
        return Pair(7, 7) // Center of board
    }
    
    private fun calculateMainTrackCoords(position: Int): Pair<Int, Int> {
        // Map position 1-52 to board coordinates
        val adjustedPos = ((position - 1) % 52) + 1
        
        return when {
            adjustedPos <= 5 -> Pair(adjustedPos, 7)
            adjustedPos <= 12 -> Pair(6, 13 - adjustedPos + 5)
            adjustedPos <= 13 -> Pair(7, 1)
            adjustedPos <= 19 -> Pair(adjustedPos - 13 + 7, 6)
            adjustedPos <= 25 -> Pair(13, adjustedPos - 19 + 1)
            adjustedPos <= 26 -> Pair(13, 7)
            adjustedPos <= 32 -> Pair(14 - (adjustedPos - 26), 7)
            adjustedPos <= 38 -> Pair(8, adjustedPos - 32 + 1)
            adjustedPos <= 39 -> Pair(7, 7)
            adjustedPos <= 45 -> Pair(6, 14 - (adjustedPos - 39))
            adjustedPos <= 51 -> Pair(adjustedPos - 45, 8)
            else -> Pair(7, 8)
        }
    }
    
    /**
     * Check if a token is blocking other tokens
     */
    fun isBlockingToken(token: Token, allTokens: List<Token>, position: Int): Boolean {
        // A token is blocking if there are 2+ tokens of the same color stacked
        val sameColorTokens = allTokens.filter { 
            it.position == position && !it.isInBase() && !it.isHome 
        }
        return sameColorTokens.size >= 2
    }
    
    /**
     * Check if a token can pass through a block
     */
    fun canPassThroughBlock(tokensAtPosition: Int, color: TokenColor): Boolean {
        // Cannot pass through a block (2+ tokens stacked)
        return tokensAtPosition < 2
    }
    
    /**
     * Calculate distance to nearest safe zone
     */
    fun distanceToSafeZone(position: Int, color: TokenColor): Int {
        val safePositionsList = SAFE_POSITIONS.toList().sorted()
        var minDistance = Int.MAX_VALUE
        
        for (safePos in safePositionsList) {
            val distance = if (safePos >= position) {
                safePos - position
            } else {
                (52 - position) + safePos
            }
            minDistance = minOf(minDistance, distance)
        }
        
        // Also consider home stretch as safe
        val homeEntry = HOME_ENTRY_POSITIONS[color] ?: 51
        val distToHome = if (homeEntry >= position) {
            homeEntry - position
        } else {
            (52 - position) + homeEntry
        }
        
        return minOf(minDistance, distToHome)
    }
    
    /**
     * Check if position is in colored home stretch
     */
    fun isInHomeStretch(position: Int, color: TokenColor): Boolean {
        return position in 52..56
    }
    
    /**
     * Calculate reward for game events
     */
    fun calculateReward(event: GameEvent, currentPlayer: Player): Reward {
        return when (event) {
            is GameEvent.TokenMoved -> Reward(
                xp = XP_PER_MOVE * event.steps,
                coins = 0
            )
            is GameEvent.TokenCaptured -> Reward(
                xp = XP_PER_CAPTURE,
                coins = COINS_PER_CAPTURE
            )
            is GameEvent.TokenHome -> Reward(
                xp = XP_PER_TOKEN_HOME,
                coins = COINS_PER_TOKEN_HOME
            )
            is GameEvent.SixRolled -> Reward(
                xp = 2,
                coins = COINS_PER_SIX
            )
            is GameEvent.GameWon -> {
                val bonusMultiplier = when (event.position) {
                    1 -> 3.0f
                    2 -> 2.0f
                    3 -> 1.5f
                    else -> 1.0f
                }
                Reward(
                    xp = (XP_PER_WIN * bonusMultiplier).toInt(),
                    coins = (COINS_PER_WIN_BASE * bonusMultiplier).toInt()
                )
            }
            else -> Reward(xp = 0, coins = 0)
        }
    }
    
    /**
     * Get strategic move suggestion for AI
     */
    fun getStrategicMove(
        player: Player,
        diceValue: Int,
        allPlayers: List<Player>,
        difficulty: AIDifficulty
    ): StrategicMove? {
        val validMoves = mutableListOf<StrategicMove>()
        
        player.tokens.forEachIndexed { index, token ->
            if (canTokenMove(token, diceValue, player, allPlayers)) {
                val move = evaluateMove(token, index, diceValue, player, allPlayers, difficulty)
                validMoves.add(move)
            }
        }
        
        return validMoves.maxByOrNull { it.score }
    }
    
    private fun canTokenMove(
        token: Token,
        diceValue: Int,
        player: Player,
        allPlayers: List<Player>
    ): Boolean {
        if (token.isHome) return false
        
        if (token.isInBase()) {
            return diceValue == 6 || !preferenceManager.getGameRules().first().requireSixToRelease
        }
        
        // Check if move is valid and not blocked
        val newPosition = calculateNewPosition(token, diceValue, player.color)
        return newPosition != null
    }
    
    private fun calculateNewPosition(token: Token, diceValue: Int, color: TokenColor): Int? {
        if (token.isHome) return null
        
        if (token.isInBase()) {
            return if (token.stepsTaken == -1 && diceValue == 6) {
                START_POSITIONS[color]
            } else null
        }
        
        val newSteps = token.stepsTaken + diceValue
        val totalSteps = TOTAL_POSITIONS + HOME_STRETCH_LENGTH + 1
        
        if (newSteps > totalSteps) return null
        
        return if (newSteps > TOTAL_POSITIONS) {
            // In home stretch
            52 + (newSteps - TOTAL_POSITIONS - 1)
        } else {
            val startPos = START_POSITIONS[color]!!
            ((startPos + newSteps - 1) % TOTAL_POSITIONS) + 1
        }
    }
    
    private fun evaluateMove(
        token: Token,
        tokenIndex: Int,
        diceValue: Int,
        player: Player,
        allPlayers: List<Player>,
        difficulty: AIDifficulty
    ): StrategicMove {
        var score = 0
        val newPosition = calculateNewPosition(token, diceValue, player.color) ?: -1
        
        // Base scores
        if (token.isInBase() && diceValue == 6) {
            score += 30 // Release token
        }
        
        if (newPosition >= 52) {
            score += 50 // Entering home stretch
        }
        
        if (newPosition == 57) {
            score += 100 // Reaching home
        }
        
        // Safe zone bonus
        if (newPosition in SAFE_POSITIONS) {
            score += 20
        }
        
        // Capture potential
        allPlayers.filter { it.color != player.color }.forEach { opponent ->
            opponent.tokens.forEach { opponentToken ->
                if (!opponentToken.isInBase() && !opponentToken.isHome && 
                    opponentToken.position == newPosition && !opponentToken.isInSafeZone) {
                    score += 40 // Can capture
                }
            }
        }
        
        // Danger avoidance (being captured)
        allPlayers.filter { it.color != player.color }.forEach { opponent ->
            // Check if any opponent can capture this token in next move
            for (i in 1..6) {
                opponent.tokens.forEach { opponentToken ->
                    if (!opponentToken.isInBase() && !opponentToken.isHome) {
                        val opponentNewPos = calculateNewPosition(opponentToken, i, opponent.color)
                        if (opponentNewPos == newPosition && newPosition !in SAFE_POSITIONS) {
                            score -= 30 / i // Higher penalty for closer threats
                        }
                    }
                }
            }
        }
        
        // Progress bonus
        score += (token.stepsTaken + diceValue) / 5
        
        // Difficulty modifier
        score = when (difficulty) {
            AIDifficulty.EASY -> score + Random.nextInt(-20, 10)
            AIDifficulty.MEDIUM -> score + Random.nextInt(-10, 5)
            AIDifficulty.HARD -> score + Random.nextInt(-5, 3)
            AIDifficulty.EXPERT -> score
        }
        
        return StrategicMove(
            tokenIndex = tokenIndex,
            currentPosition = token.position,
            newPosition = newPosition,
            score = score
        )
    }
}

/**
 * Game events for reward calculation
 */
sealed class GameEvent {
    data class TokenMoved(val steps: Int) : GameEvent()
    object TokenCaptured : GameEvent()
    object TokenHome : GameEvent()
    object SixRolled : GameEvent()
    data class GameWon(val position: Int) : GameEvent()
    object TokenReleased : GameEvent()
    object TurnSkipped : GameEvent()
}

/**
 * Reward structure
 */
data class Reward(
    val xp: Int,
    val coins: Int
)

/**
 * Strategic move for AI
 */
data class StrategicMove(
    val tokenIndex: Int,
    val currentPosition: Int,
    val newPosition: Int,
    val score: Int
)

/**
 * AI Difficulty levels
 */
enum class AIDifficulty {
    EASY, MEDIUM, HARD, EXPERT
}
