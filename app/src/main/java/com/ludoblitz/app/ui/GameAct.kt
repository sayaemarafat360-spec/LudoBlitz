package com.ludoblitz.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ludoblitz.app.R
import com.ludoblitz.app.data.model.BotDifficulty
import com.ludoblitz.app.data.model.GameEvent
import com.ludoblitz.app.data.model.TokenColor
import com.ludoblitz.app.databinding.GameActBinding
import com.ludoblitz.app.ui.viewmodel.GameVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameAct : AppCompatActivity() {
    private lateinit var b: GameActBinding
    private val vm: GameVM by viewModels()

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = GameActBinding.inflate(layoutInflater)
        setContentView(b.root)
        setup()
        observe()
    }

    private fun setup() {
        val players = intent.getIntExtra("players", 2)
        val diff = BotDifficulty.valueOf(intent.getStringExtra("diff") ?: "MEDIUM")
        val uid = intent.getStringExtra("uid")
        vm.start(players, diff, uid)

        b.btnRoll.setOnClickListener { vm.roll() }
        b.btnExit.setOnClickListener { finish() }
    }

    private fun observe() {
        vm.game.observe(this) { g ->
            b.tvPlayer.text = g.getCurrentPlayer()?.name ?: ""
            val c = g.getCurrentPlayer()?.color ?: TokenColor.RED
            b.viewColor.setBackgroundColor(ContextCompat.getColor(this, colorOf(c)))
        }
        vm.dice.observe(this) { d -> b.tvDice.text = if (d > 0) d.toString() else "-" }
        vm.rolling.observe(this) { r -> b.btnRoll.isEnabled = !r && vm.myTurn.value == true }
        vm.myTurn.observe(this) { m -> b.btnRoll.isEnabled = m && vm.rolling.value == false }
        vm.moves.observe(this) { m ->
            if (m.isNotEmpty()) b.tvMoves.text = "${m.size} moves. Tap to auto-select"
            else b.tvMoves.text = ""
        }
        vm.event.observe(this) { e ->
            when (e) {
                GameEvent.CAPTURE -> toast("Captured!")
                GameEvent.HOME -> toast("Token home!")
                GameEvent.BONUS -> toast("Bonus turn!")
                null -> {}
            }
            vm.clearEvent()
        }
        vm.winner.observe(this) { w -> if (w != null) { b.tvWin.text = "${w.name} wins!"; b.tvWin.visibility = android.view.View.VISIBLE } }
    }

    private fun colorOf(c: TokenColor) = when (c) {
        TokenColor.RED -> R.color.token_red
        TokenColor.GREEN -> R.color.token_green
        TokenColor.YELLOW -> R.color.token_yellow
        TokenColor.BLUE -> R.color.token_blue
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
