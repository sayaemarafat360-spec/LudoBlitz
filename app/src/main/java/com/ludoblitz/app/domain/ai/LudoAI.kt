package com.ludoblitz.app.domain.ai

import com.ludoblitz.app.data.model.*
import kotlin.random.Random

interface LudoAI {
    fun selectMove(validMoves: List<ValidMove>, game: Game, player: Player): ValidMove?
}

class EasyAI : LudoAI {
    override fun selectMove(validMoves: List<ValidMove>, game: Game, player: Player): ValidMove? {
        return validMoves.randomOrNull()
    }
}

class MediumAI : LudoAI {
    override fun selectMove(validMoves: List<ValidMove>, game: Game, player: Player): ValidMove? {
        return validMoves.maxByOrNull { it.toPosition - it.fromPosition }
    }
}

class HardAI : LudoAI {
    override fun selectMove(validMoves: List<ValidMove>, game: Game, player: Player): ValidMove? {
        val releaseMove = validMoves.find { it.fromPosition == -1 }
        if (releaseMove != null && player.getTokensInBaseCount() > 2) return releaseMove
        return validMoves.maxByOrNull { it.toPosition }
    }
}

class ExpertAI : LudoAI {
    override fun selectMove(validMoves: List<ValidMove>, game: Game, player: Player): ValidMove? {
        val homeMove = validMoves.find { it.toPosition == 100 }
        if (homeMove != null) return homeMove
        return validMoves.maxByOrNull { it.toPosition - it.fromPosition }
    }
}

object AIFactory {
    fun create(difficulty: BotDifficulty): LudoAI = when (difficulty) {
        BotDifficulty.EASY -> EasyAI()
        BotDifficulty.MEDIUM -> MediumAI()
        BotDifficulty.HARD -> HardAI()
        BotDifficulty.EXPERT -> ExpertAI()
    }
}
