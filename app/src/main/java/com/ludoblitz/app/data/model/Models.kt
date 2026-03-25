package com.ludoblitz.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val coins: Long = 1000,
    val gems: Long = 0,
    val xp: Long = 0,
    val level: Int = 1,
    val totalWins: Int = 0,
    val totalGames: Int = 0,
    val winStreak: Int = 0,
    val rating: Int = 1000,
    val isPremium: Boolean = false,
    val lastLoginDate: Long = System.currentTimeMillis(),
    val dailyRewardDay: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    fun getWinRate(): Float = if (totalGames > 0) (totalWins.toFloat() / totalGames) * 100 else 0f
    fun getXpForNextLevel(): Long = (level * 1000L).coerceAtLeast(1000)
    fun getXpProgress(): Float = (xp.toFloat() / getXpForNextLevel()) * 100
}

enum class TokenColor { RED, GREEN, YELLOW, BLUE }

@Parcelize
data class Token(
    val id: Int,
    val color: TokenColor,
    var position: Int = -1,
    var isHome: Boolean = false,
    var stepsTaken: Int = 0
) : Parcelable {
    fun isInBase(): Boolean = position == -1
    fun isFinished(): Boolean = isHome
}

@Parcelize
data class Player(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val color: TokenColor,
    val tokens: List<Token>,
    val isBot: Boolean = false,
    val botDifficulty: BotDifficulty = BotDifficulty.MEDIUM,
    var hasRolled: Boolean = false,
    var consecutiveSixes: Int = 0,
    var finishedPosition: Int = 0
) : Parcelable {
    fun getFinishedTokensCount(): Int = tokens.count { it.isHome }
    fun getTokensInBaseCount(): Int = tokens.count { it.isInBase() }
    fun hasWon(): Boolean = tokens.all { it.isHome }
    fun hasMovableToken(diceValue: Int): Boolean = tokens.any { !it.isHome && (it.isInBase() && diceValue == 6 || !it.isInBase()) }
}

enum class BotDifficulty { EASY, MEDIUM, HARD, EXPERT }

@Parcelize
data class GameRules(
    val requireSixToRelease: Boolean = true,
    val threeSixesRule: Boolean = true,
    val maxPlayers: Int = 4
) : Parcelable

@Parcelize
data class Game(
    val id: String = "",
    val players: List<Player> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val diceValue: Int = 0,
    val gameStatus: GameStatus = GameStatus.WAITING,
    val gameMode: GameMode = GameMode.LOCAL,
    val rules: GameRules = GameRules(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
    fun getCurrentPlayer(): Player? = players.getOrNull(currentPlayerIndex)
}

enum class GameStatus { WAITING, IN_PROGRESS, PAUSED, FINISHED }
enum class GameMode { LOCAL, VS_AI, ONLINE }

@Parcelize
data class ValidMove(val tokenIndex: Int, val fromPosition: Int, val toPosition: Int) : Parcelable

@Parcelize
data class MoveResult(
    val game: Game,
    val success: Boolean,
    val bonusTurn: Boolean = false,
    val capturedToken: Boolean = false,
    val tokenReachedHome: Boolean = false,
    val isGameOver: Boolean = false,
    val winnerIndex: Int = -1
) : Parcelable

@Parcelize
data class Reward(val coins: Long = 0, val gems: Long = 0, val xp: Long = 0) : Parcelable
