package com.ludoblitz.app.domain.gamelogic

import com.ludoblitz.app.data.model.*
import kotlin.random.Random

class LudoGameEngine {
    companion object {
        const val BOARD_SIZE = 52
        const val HOME_STRETCH_SIZE = 5
        val SAFE_POSITIONS = setOf(1, 9, 14, 22, 27, 35, 40, 48)
    }

    fun createNewGame(players: List<Player>, gameMode: GameMode, rules: GameRules): Game {
        return Game(
            players = players,
            gameMode = gameMode,
            rules = rules,
            gameStatus = GameStatus.IN_PROGRESS
        )
    }

    fun rollDice(): Int = Random.nextInt(1, 7)

    fun getValidMoves(player: Player, diceValue: Int): List<ValidMove> {
        val moves = mutableListOf<ValidMove>()
        player.tokens.forEachIndexed { index, token ->
            if (token.isHome) return@forEachIndexed
            if (token.isInBase()) {
                if (diceValue == 6) {
                    moves.add(ValidMove(index, -1, token.color.startPosition))
                }
            } else {
                val newPos = calculatePosition(token, diceValue)
                if (newPos != null) moves.add(ValidMove(index, token.position, newPos))
            }
        }
        return moves
    }

    private fun calculatePosition(token: Token, diceValue: Int): Int? {
        val newSteps = token.stepsTaken + diceValue
        if (newSteps > 57) return null
        if (newSteps == 57) return 100
        return (token.position + diceValue) % BOARD_SIZE
    }

    fun moveToken(game: Game, playerIndex: Int, tokenIndex: Int, diceValue: Int): MoveResult {
        val players = game.players.toMutableList()
        val player = players[playerIndex]
        val tokens = player.tokens.toMutableList()
        val token = tokens[tokenIndex]

        var bonusTurn = false
        var captured = false
        var home = false

        if (token.isInBase()) {
            tokens[tokenIndex] = token.copy(position = token.color.startPosition, stepsTaken = 0)
            bonusTurn = true
        } else {
            val newSteps = token.stepsTaken + diceValue
            val newPos = calculatePosition(token, diceValue)

            if (newPos == 100) {
                tokens[tokenIndex] = token.copy(position = -1, isHome = true, stepsTaken = 57)
                home = true
                bonusTurn = true
            } else if (newPos != null) {
                tokens[tokenIndex] = token.copy(position = newPos, stepsTaken = newSteps)
                captured = checkCapture(players, playerIndex, newPos)
                if (diceValue == 6) bonusTurn = true
            }
        }

        players[playerIndex] = player.copy(tokens = tokens, hasRolled = true)
        val isGameOver = players[playerIndex].hasWon()

        return MoveResult(
            game = game.copy(players = players, updatedAt = System.currentTimeMillis()),
            success = true,
            bonusTurn = bonusTurn && !captured,
            capturedToken = captured,
            tokenReachedHome = home,
            isGameOver = isGameOver,
            winnerIndex = if (isGameOver) playerIndex else -1
        )
    }

    private fun checkCapture(players: List<Player>, currentPlayerIndex: Int, position: Int): Boolean {
        if (position in SAFE_POSITIONS) return false
        for (i in players.indices) {
            if (i == currentPlayerIndex) continue
            players[i].tokens.forEach { token ->
                if (!token.isInBase() && !token.isHome && token.position == position) return true
            }
        }
        return false
    }

    fun getNextPlayerIndex(game: Game): Int {
        var next = (game.currentPlayerIndex + 1) % game.players.size
        var attempts = 0
        while (game.players[next].hasWon() && attempts < game.players.size) {
            next = (next + 1) % game.players.size
            attempts++
        }
        return next
    }

    private val TokenColor.startPosition: Int
        get() = when (this) {
            TokenColor.RED -> 1
            TokenColor.GREEN -> 14
            TokenColor.YELLOW -> 27
            TokenColor.BLUE -> 40
        }
}
