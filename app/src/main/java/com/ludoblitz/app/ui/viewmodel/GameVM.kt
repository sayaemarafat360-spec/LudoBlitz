package com.ludoblitz.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ludoblitz.app.data.model.*
import com.ludoblitz.app.domain.ai.AIFactory
import com.ludoblitz.app.domain.gamelogic.LudoGameEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameVM @Inject constructor(private val engine: LudoGameEngine) : ViewModel() {

    private val _game = MutableLiveData<Game>()
    val game: LiveData<Game> = _game
    private val _dice = MutableLiveData(0)
    val dice: LiveData<Int> = _dice
    private val _rolling = MutableLiveData(false)
    val rolling: LiveData<Boolean> = _rolling
    private val _moves = MutableLiveData<List<ValidMove>>(emptyList())
    val moves: LiveData<List<ValidMove>> = _moves
    private val _myTurn = MutableLiveData(true)
    val myTurn: LiveData<Boolean> = _myTurn
    private val _event = MutableLiveData<GameEvent?>(null)
    val event: LiveData<GameEvent?> = _event
    private val _winner = MutableLiveData<Player?>(null)
    val winner: LiveData<Player?> = _winner

    private val ais = mutableMapOf<String, com.ludoblitz.app.domain.ai.LudoAI>()

    fun start(players: Int, diff: BotDifficulty, uid: String?) {
        val colors = TokenColor.values().take(players)
        val list = colors.mapIndexed { i, c ->
            val isBot = i > 0
            Player(
                id = if (isBot) "bot_$i" else (uid ?: "p1"),
                name = if (isBot) "Bot $i" else "You",
                avatarUrl = "",
                color = c,
                tokens = (1..4).map { Token(it, c) },
                isBot = isBot,
                botDifficulty = if (isBot) diff else BotDifficulty.EASY
            ).also { if (isBot) ais[it.id] = AIFactory.create(diff) }
        }
        _game.value = engine.createNewGame(list, GameMode.VS_AI, GameRules())
        _myTurn.value = true
    }

    fun roll() {
        val g = _game.value ?: return
        val p = g.getCurrentPlayer() ?: return
        if (_rolling.value == true) return

        viewModelScope.launch {
            _rolling.value = true
            delay(500)
            val v = engine.rollDice()
            _dice.value = v
            _rolling.value = false
            val m = engine.getValidMoves(p, v)
            _moves.value = m
            if (m.isEmpty()) { delay(800); endTurn() }
            else if (p.isBot) { delay(400); ais[p.id]?.selectMove(m, g, p)?.let { move(it.tokenIndex) } ?: endTurn() }
        }
    }

    fun move(idx: Int) {
        val g = _game.value ?: return
        val d = _dice.value ?: return
        val r = engine.moveToken(g, g.currentPlayerIndex, idx, d)
        _game.value = r.game
        _moves.value = emptyList()
        if (r.capturedToken) _event.value = GameEvent.CAPTURE
        if (r.tokenReachedHome) _event.value = GameEvent.HOME
        if (r.isGameOver) { _winner.value = r.game.players[r.winnerIndex]; return }
        viewModelScope.launch {
            delay(300)
            if (r.bonusTurn) _event.value = GameEvent.BONUS
            else endTurn()
        }
    }

    private fun endTurn() {
        val g = _game.value ?: return
        val next = engine.getNextPlayerIndex(g)
        val p = g.players[next]
        _game.value = g.copy(currentPlayerIndex = next)
        _dice.value = 0
        _myTurn.value = !p.isBot
        if (p.isBot) viewModelScope.launch { delay(600); roll() }
    }

    fun clearEvent() { _event.value = null }
}

sealed class GameEvent {
    object CAPTURE : GameEvent()
    object HOME : GameEvent()
    object BONUS : GameEvent()
}
