package com.ludoblitz.app.utils

import com.ludoblitz.app.data.models.*
import kotlin.random.Random

/**
 * Game Engine - Handles all game logic for Ludo
 */
class GameEngine(private val config: GameConfig) {

    /**
     * Initialize the game with players
     */
    fun initializeGame(): GameState {
        val players = mutableListOf<Player>()

        for (i in 0 until config.playerCount) {
            val playerColor = PlayerColor.values()[i]
            val tokens = mutableListOf<Token>()

            for (j in 0 until config.tokensPerPlayer) {
                tokens.add(
                    Token(
                        id = j,
                        playerColor = playerColor,
                        position = -1,
                        state = TokenState.HOME,
                        stepsFromStart = 0
                    )
                )
            }

            players.add(
                Player(
                    id = i,
                    name = "Player ${i + 1}",
                    color = playerColor,
                    tokens = tokens,
                    isAI = i > 0, // First player is human, rest are AI
                    aiDifficulty = if (i > 0) config.aiDifficulty else AIDifficulty.EASY
                )
            )
        }

        return GameState(
            players = players,
            message = "${players[0].name}'s turn. Roll the dice!"
        )
    }

    /**
     * Roll the dice
     */
    fun rollDice(state: GameState): GameState {
        val diceValue = Random.nextInt(BoardConstants.DICE_MIN, BoardConstants.DICE_MAX + 1)

        return state.copy(
            diceValue = diceValue,
            isRolling = true,
            phase = GamePhase.ROLLING,
            lastRolledValue = diceValue
        )
    }

    /**
     * Process dice roll result and determine possible moves
     */
    fun processDiceRoll(state: GameState): GameState {
        val currentPlayer = state.players[state.currentPlayerIndex]
        val diceValue = state.lastRolledValue

        // Check for three consecutive sixes
        if (diceValue == 6 && state.consecutiveSixes >= 2 && config.threeSixBurn) {
            return state.copy(
                phase = GamePhase.NEXT_TURN,
                consecutiveSixes = 0,
                message = "Three consecutive 6s! Turn skipped!",
                isRolling = false
            )
        }

        // Find possible moves
        val possibleMoves = findPossibleMoves(currentPlayer, diceValue, state)

        if (possibleMoves.isEmpty()) {
            // No valid moves, check if turn should pass
            val message = if (diceValue != 6) {
                "No valid moves. Turn passes to next player."
            } else {
                "No valid moves. Roll again!"
            }

            return if (diceValue == 6 && config.doubleTurnOnSix) {
                state.copy(
                    phase = GamePhase.WAITING_FOR_ROLL,
                    possibleMoves = emptyList(),
                    message = message,
                    isRolling = false,
                    consecutiveSixes = state.consecutiveSixes + 1
                )
            } else {
                state.copy(
                    phase = GamePhase.NEXT_TURN,
                    possibleMoves = emptyList(),
                    message = message,
                    isRolling = false
                )
            }
        }

        // If only one move possible and it's AI, auto-select
        val message = if (possibleMoves.size == 1) {
            "Moving token..."
        } else {
            "Select a token to move"
        }

        return state.copy(
            phase = GamePhase.WAITING_FOR_TOKEN_SELECTION,
            possibleMoves = possibleMoves,
            message = message,
            isRolling = false,
            consecutiveSixes = if (diceValue == 6) state.consecutiveSixes + 1 else 0
        )
    }

    /**
     * Find all possible moves for a player
     */
    private fun findPossibleMoves(
        player: Player,
        diceValue: Int,
        state: GameState
    ): List<TokenMove> {
        val moves = mutableListOf<TokenMove>()
        val startPos = BoardConstants.START_POSITIONS[player.color] ?: 0

        for (token in player.tokens) {
            when (token.state) {
                TokenState.HOME -> {
                    // Token can only leave home with a 6
                    if (diceValue == 6) {
                        moves.add(
                            TokenMove(
                                token = token,
                                fromPosition = -1,
                                toPosition = startPos,
                                steps = 0,
                                isEnteringBoard = true
                            )
                        )
                    }
                }
                TokenState.ACTIVE -> {
                    val newStepsFromStart = token.stepsFromStart + diceValue

                    // Check if token would complete
                    if (newStepsFromStart >= BoardConstants.STEPS_TO_COMPLETE) {
                        // Exact or overshoot - can finish
                        moves.add(
                            TokenMove(
                                token = token,
                                fromPosition = token.position,
                                toPosition = -2, // Finished
                                steps = diceValue,
                                isFinishing = true
                            )
                        )
                    } else {
                        // Regular move on the board
                        val newPosition = calculatePosition(player.color, newStepsFromStart)

                        // Check for capture
                        val capturedToken = findTokenAtPosition(newPosition, state, player.color)

                        moves.add(
                            TokenMove(
                                token = token,
                                fromPosition = token.position,
                                toPosition = newPosition,
                                steps = diceValue,
                                isCapture = capturedToken != null && !isSafePosition(newPosition),
                                capturedToken = capturedToken
                            )
                        )
                    }
                }
                TokenState.FINISHED -> {
                    // Token is already finished, no moves
                }
            }
        }

        return moves
    }

    /**
     * Calculate the board position for a token
     */
    private fun calculatePosition(playerColor: PlayerColor, stepsFromStart: Int): Int {
        if (stepsFromStart < 0) return -1
        if (stepsFromStart >= BoardConstants.MAIN_TRACK_SIZE) {
            // In home column
            return -3
        }

        val startPos = BoardConstants.START_POSITIONS[playerColor] ?: 0
        return (startPos + stepsFromStart) % BoardConstants.MAIN_TRACK_SIZE
    }

    /**
     * Find a token at a specific position
     */
    private fun findTokenAtPosition(
        position: Int,
        state: GameState,
        excludeColor: PlayerColor
    ): Token? {
        if (position < 0) return null

        for (player in state.players) {
            if (player.color == excludeColor) continue

            for (token in player.tokens) {
                if (token.state == TokenState.ACTIVE && token.position == position) {
                    return token
                }
            }
        }

        return null
    }

    /**
     * Execute a token move
     */
    fun executeMove(state: GameState, move: TokenMove): GameState {
        val updatedPlayers = state.players.map { player ->
            if (player.id == move.token.playerColor.ordinal) {
                // Update the moved token
                val updatedTokens = player.tokens.map { token ->
                    if (token.id == move.token.id) {
                        when {
                            move.isFinishing -> token.copy(
                                state = TokenState.FINISHED,
                                position = -2,
                                stepsFromStart = BoardConstants.STEPS_TO_COMPLETE
                            )
                            move.isEnteringBoard -> token.copy(
                                state = TokenState.ACTIVE,
                                position = move.toPosition,
                                stepsFromStart = 0
                            )
                            else -> token.copy(
                                position = move.toPosition,
                                stepsFromStart = token.stepsFromStart + move.steps
                            )
                        }
                    } else {
                        token
                    }
                }
                player.copy(tokens = updatedTokens)
            } else if (move.isCapture && move.capturedToken != null && player.color == move.capturedToken.playerColor) {
                // Update captured token (send back to home)
                val updatedTokens = player.tokens.map { token ->
                    if (token.id == move.capturedToken.id) {
                        token.copy(
                            state = TokenState.HOME,
                            position = -1,
                            stepsFromStart = 0
                        )
                    } else {
                        token
                    }
                }
                player.copy(tokens = updatedTokens)
            } else {
                player
            }
        }

        // Check for win
        val currentPlayer = updatedPlayers[state.currentPlayerIndex]
        val hasWon = currentPlayer.tokens.all { it.state == TokenState.FINISHED }

        if (hasWon) {
            return state.copy(
                players = updatedPlayers,
                phase = GamePhase.GAME_OVER,
                winner = currentPlayer,
                message = "${currentPlayer.name} wins!",
                turnCount = state.turnCount + 1
            )
        }

        // Determine next phase
        val message = when {
            move.isCapture -> "Token captured! ${if (config.captureBonus) "+20 bonus moves" else ""}"
            move.isFinishing -> "Token reached home!"
            move.isEnteringBoard -> "Token entered the board!"
            state.lastRolledValue == 6 && config.doubleTurnOnSix -> "Rolled 6! Roll again!"
            else -> "Token moved ${move.steps} steps"
        }

        // Check if player gets another turn
        val extraTurn = state.lastRolledValue == 6 && config.doubleTurnOnSix

        return if (extraTurn) {
            state.copy(
                players = updatedPlayers,
                phase = GamePhase.WAITING_FOR_ROLL,
                extraTurn = true,
                message = message,
                possibleMoves = emptyList(),
                turnCount = state.turnCount + 1
            )
        } else {
            state.copy(
                players = updatedPlayers,
                phase = GamePhase.NEXT_TURN,
                message = message,
                possibleMoves = emptyList(),
                turnCount = state.turnCount + 1
            )
        }
    }

    /**
     * Move to next player's turn
     */
    fun nextTurn(state: GameState): GameState {
        val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
        val nextPlayer = state.players[nextPlayerIndex]

        return state.copy(
            currentPlayerIndex = nextPlayerIndex,
            phase = GamePhase.WAITING_FOR_ROLL,
            consecutiveSixes = 0,
            extraTurn = false,
            message = "${nextPlayer.name}'s turn. Roll the dice!",
            possibleMoves = emptyList()
        )
    }

    /**
     * Get AI's chosen move
     */
    fun getAIMove(state: GameState, difficulty: AIDifficulty): TokenMove? {
        val possibleMoves = state.possibleMoves

        if (possibleMoves.isEmpty()) return null
        if (possibleMoves.size == 1) return possibleMoves.first()

        return when (difficulty) {
            AIDifficulty.EASY -> {
                // Random selection
                possibleMoves.random()
            }
            AIDifficulty.MEDIUM -> {
                // Basic strategy: prioritize captures and finishing
                possibleMoves.sortedWith(compareByDescending<TokenMove> { move ->
                    when {
                        move.isFinishing -> 100
                        move.isCapture -> 80
                        move.isEnteringBoard -> 60
                        else -> move.steps
                    }
                }).first()
            }
            AIDifficulty.HARD -> {
                // Advanced strategy with lookahead
                selectBestMove(possibleMoves, state)
            }
        }
    }

    /**
     * Select the best move for AI (advanced strategy)
     */
    private fun selectBestMove(moves: List<TokenMove>, state: GameState): TokenMove {
        return moves.maxByOrNull { move ->
            var score = 0

            // Finishing is best
            if (move.isFinishing) score += 200

            // Capturing is very good
            if (move.isCapture) score += 100

            // Entering the board is good
            if (move.isEnteringBoard) score += 50

            // Moving towards home is good
            score += move.steps

            // Entering home column is good
            val player = state.players[state.currentPlayerIndex]
            val token = move.token
            val newSteps = token.stepsFromStart + move.steps
            if (newSteps >= BoardConstants.MAIN_TRACK_SIZE) {
                score += 30
            }

            // Landing on safe zone is good
            if (isSafePosition(move.toPosition)) {
                score += 20
            }

            // Check if this position is dangerous (enemy nearby)
            if (isPositionDangerous(move.toPosition, state, player.color)) {
                score -= 40
            }

            score
        } ?: moves.first()
    }

    /**
     * Check if a position is dangerous (enemy could capture)
     */
    private fun isPositionDangerous(position: Int, state: GameState, myColor: PlayerColor): Boolean {
        if (position < 0 || isSafePosition(position)) return false

        for (player in state.players) {
            if (player.color == myColor) continue

            for (token in player.tokens) {
                if (token.state != TokenState.ACTIVE) continue

                // Check if enemy token is within 6 steps
                for (dice in 1..6) {
                    val enemyNewPos = (token.position + dice) % BoardConstants.MAIN_TRACK_SIZE
                    if (enemyNewPos == position) {
                        return true
                    }
                }
            }
        }

        return false
    }

    /**
     * Check if auto-selection is possible (only one move)
     */
    fun canAutoSelect(state: GameState): Boolean {
        return state.possibleMoves.size == 1
    }

    /**
     * Check if current player is AI
     */
    fun isCurrentPlayerAI(state: GameState): Boolean {
        return state.players[state.currentPlayerIndex].isAI
    }

    /**
     * Get current player
     */
    fun getCurrentPlayer(state: GameState): Player {
        return state.players[state.currentPlayerIndex]
    }
}
