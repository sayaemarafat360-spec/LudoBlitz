package com.ludoblitz.app.ui.screens

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ludoblitz.app.LudoBlitzApp
import com.ludoblitz.app.R
import com.ludoblitz.app.data.model.*
import com.ludoblitz.app.databinding.ActivityGameBinding
import com.ludoblitz.app.databinding.DialogGameOverBinding
import com.ludoblitz.app.databinding.DialogPauseMenuBinding
import com.ludoblitz.app.domain.gamelogic.GameState
import com.ludoblitz.app.domain.gamelogic.LudoGameEngine
import com.ludoblitz.app.domain.gamelogic.MoveResult
import com.ludoblitz.app.domain.gamelogic.ValidMove
import com.ludoblitz.app.ui.viewmodel.GameEvent
import com.ludoblitz.app.ui.viewmodel.LocalGameViewModel
import com.ludoblitz.app.utils.SoundManager
import com.ludoblitz.app.utils.VibrationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Game Activity - Main gameplay screen for Ludo Blitz
 * Complete implementation with game logic, animations, and sound
 */
@AndroidEntryPoint
class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private val viewModel: LocalGameViewModel by viewModels()
    
    @Inject
    lateinit var soundManager: SoundManager
    
    @Inject
    lateinit var vibrationManager: VibrationManager
    
    @Inject
    lateinit var gameEngine: LudoGameEngine

    private var isAnimating = false
    private var selectedTokenIndex: Int? = null
    private var highlightedTokens = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get game configuration from intent
        val playerCount = intent.getIntExtra(EXTRA_PLAYER_COUNT, 4)
        val botDifficulty = intent.getSerializableExtra(EXTRA_DIFFICULTY) as? BotDifficulty ?: BotDifficulty.MEDIUM
        val isClassicMode = intent.getBooleanExtra(EXTRA_CLASSIC_MODE, true)
        
        // Initialize game
        setupGame(playerCount, botDifficulty, isClassicMode)
        setupClickListeners()
        setupObservers()
        setupBackPressedHandler()
    }

    private fun setupGame(playerCount: Int, difficulty: BotDifficulty, classicMode: Boolean) {
        viewModel.initializeGame(playerCount, difficulty, classicMode)
        
        // Play game start sound
        soundManager.playGameStart()
        vibrationManager.mediumClick()
        
        // Animate entrance
        animateGameStart()
    }

    private fun setupClickListeners() {
        // Dice click - roll the dice
        binding.diceContainer.setOnClickListener {
            if (viewModel.canRollDice() && !isAnimating) {
                rollDice()
            }
        }
        
        // Auto-select token button
        binding.btnAutoSelect.setOnClickListener {
            autoSelectToken()
        }
        
        // Pause button
        binding.btnPause.setOnClickListener {
            showPauseMenu()
        }
        
        // Board touch for token selection
        binding.boardView.setOnClickListener { view ->
            // Token selection handled by board view
        }
    }

    private fun setupObservers() {
        // Observe game state
        lifecycleScope.launch {
            viewModel.gameState.collect { state ->
                when (state) {
                    is GameState.WaitingForRoll -> {
                        updateUIForWaitingRoll()
                    }
                    is GameState.Rolling -> {
                        // Rolling animation in progress
                    }
                    is GameState.SelectingMove -> {
                        updateUIForSelectingMove(state.validMoves)
                    }
                    is GameState.Moving -> {
                        // Token moving animation
                    }
                    is GameState.TurnComplete -> {
                        handleTurnComplete(state.bonusTurn)
                    }
                    is GameState.GameOver -> {
                        handleGameOver(state.winner)
                    }
                    else -> {}
                }
            }
        }
        
        // Observe current player
        lifecycleScope.launch {
            viewModel.currentPlayer.collect { player ->
                updateCurrentPlayerUI(player)
            }
        }
        
        // Observe dice value
        lifecycleScope.launch {
            viewModel.diceValue.collect { value ->
                if (value > 0) {
                    updateDiceUI(value)
                }
            }
        }
        
        // Observe events
        lifecycleScope.launch {
            viewModel.events.collect { event ->
                handleGameEvent(event)
            }
        }
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showPauseMenu()
            }
        })
    }

    // ==================== GAME ACTIONS ====================

    private fun rollDice() {
        if (isAnimating) return
        
        isAnimating = true
        soundManager.playDiceRoll()
        vibrationManager.diceRoll()
        
        // Animate dice roll
        animateDiceRoll {
            val diceValue = viewModel.rollDice()
            showDiceResult(diceValue)
            
            // Check if can move
            if (!viewModel.hasValidMoves()) {
                lifecycleScope.launch {
                    delay(1000)
                    showNoValidMoves()
                    viewModel.skipTurn()
                }
            }
            
            isAnimating = false
        }
    }

    private fun selectToken(tokenIndex: Int) {
        if (isAnimating) return
        
        val canMove = viewModel.canMoveToken(tokenIndex)
        if (canMove) {
            moveToken(tokenIndex)
        }
    }

    private fun moveToken(tokenIndex: Int) {
        isAnimating = true
        
        val result = viewModel.moveToken(tokenIndex)
        
        // Play move sound
        soundManager.playTokenMove(viewModel.getLastDiceValue())
        vibrationManager.tokenMove()
        
        // Animate token movement
        animateTokenMovement(tokenIndex, result) {
            handleMoveResult(result)
            isAnimating = false
        }
    }

    private fun autoSelectToken() {
        val bestMove = viewModel.getBestMove()
        if (bestMove != null) {
            selectToken(bestMove.tokenIndex)
        }
    }

    // ==================== UI UPDATES ====================

    private fun updateUIForWaitingRoll() {
        binding.tvTurnStatus.text = getString(R.string.your_turn)
        binding.diceContainer.isEnabled = true
        binding.diceContainer.alpha = 1f
        binding.btnAutoSelect.visibility = View.GONE
        
        // Highlight dice container
        binding.diceContainer.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(300)
            .setInterpolator(BounceInterpolator())
            .start()
    }

    private fun updateUIForSelectingMove(validMoves: List<ValidMove>) {
        binding.tvTurnStatus.text = getString(R.string.select_token)
        binding.diceContainer.isEnabled = false
        binding.diceContainer.alpha = 0.6f
        
        // Show auto-select button
        binding.btnAutoSelect.visibility = View.VISIBLE
        
        // Highlight valid tokens on board
        highlightValidTokens(validMoves)
    }

    private fun updateCurrentPlayerUI(player: Player?) {
        player ?: return
        
        binding.tvCurrentPlayer.text = player.name
        binding.tvCurrentPlayer.setTextColor(getTokenColor(player.color))
        
        // Update player cards
        updatePlayerCards()
    }

    private fun updatePlayerCards() {
        val game = viewModel.getGame() ?: return
        
        game.players.forEachIndexed { index, player ->
            val card = when (player.color) {
                TokenColor.RED -> binding.playerCardRed
                TokenColor.GREEN -> binding.playerCardGreen
                TokenColor.YELLOW -> binding.playerCardYellow
                TokenColor.BLUE -> binding.playerCardBlue
            }
            
            // Update card appearance
            card.alpha = if (index == viewModel.currentPlayerIndex.value) 1f else 0.6f
            
            // Update token home count
            card.findViewById<android.widget.TextView>(R.id.tv_tokens_home)?.text = 
                "${player.getFinishedTokensCount()}/4"
        }
    }

    private fun updateDiceUI(value: Int) {
        val drawableRes = when (value) {
            1 -> R.drawable.ic_dice_1
            2 -> R.drawable.ic_dice_2
            3 -> R.drawable.ic_dice_3
            4 -> R.drawable.ic_dice_4
            5 -> R.drawable.ic_dice_5
            6 -> R.drawable.ic_dice_6
            else -> R.drawable.ic_dice
        }
        binding.diceView.setImageResource(drawableRes)
    }

    private fun highlightValidTokens(validMoves: List<ValidMove>) {
        highlightedTokens.clear()
        
        validMoves.forEach { move ->
            highlightedTokens.add(move.tokenIndex)
        }
        
        // Update board view to highlight tokens
        binding.boardView.invalidate()
    }

    // ==================== ANIMATIONS ====================

    private fun animateGameStart() {
        // Fade in board
        binding.boardView.alpha = 0f
        binding.boardView.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
        
        // Slide in player cards
        val cards = listOf(
            binding.playerCardRed,
            binding.playerCardGreen,
            binding.playerCardYellow,
            binding.playerCardBlue
        )
        
        cards.forEachIndexed { index, card ->
            card.translationY = if (index < 2) -50f else 50f
            card.alpha = 0f
            card.animate()
                .translationY(0f)
                .alpha(1f)
                .setStartDelay((index * 100).toLong())
                .setDuration(300)
                .start()
        }
        
        // Dice entrance
        binding.diceContainer.scaleX = 0f
        binding.diceContainer.scaleY = 0f
        binding.diceContainer.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(400)
            .setDuration(400)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    private fun animateDiceRoll(onComplete: () -> Unit) {
        // Shake animation
        val shakeX = ObjectAnimator.ofFloat(binding.diceView, "translationX", 0f, 10f, -10f, 10f, -10f, 5f, -5f, 0f)
        val shakeY = ObjectAnimator.ofFloat(binding.diceView, "translationY", 0f, -10f, 10f, -10f, 10f, -5f, 5f, 0f)
        val rotate = ObjectAnimator.ofFloat(binding.diceView, "rotation", 0f, 360f)
        
        AnimatorSet().apply {
            playTogether(shakeX, shakeY, rotate)
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onComplete()
                }
            })
            start()
        }
    }

    private fun showDiceResult(value: Int) {
        binding.tvDiceResult.text = value.toString()
        binding.tvDiceResult.visibility = View.VISIBLE
        binding.tvDiceResult.scaleX = 0f
        binding.tvDiceResult.scaleY = 0f
        
        binding.tvDiceResult.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setInterpolator(OvershootInterpolator())
            .start()
        
        // Special animation for 6
        if (value == 6) {
            binding.tvDiceResult.setTextColor(ContextCompat.getColor(this, R.color.success))
            Handler(Looper.getMainLooper()).postDelayed({
                binding.tvDiceResult.visibility = View.GONE
            }, 2000)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.tvDiceResult.visibility = View.GONE
            }, 1000)
        }
    }

    private fun animateTokenMovement(tokenIndex: Int, result: MoveResult, onComplete: () -> Unit) {
        val game = viewModel.getGame() ?: return
        val playerIndex = viewModel.currentPlayerIndex.value
        val player = game.players[playerIndex]
        
        // Animate on board view
        binding.boardView.animateTokenMove(
            playerColor = player.color,
            tokenIndex = tokenIndex,
            fromPosition = 0, // Would get from previous state
            toPosition = result.game.players[playerIndex].tokens[tokenIndex].position,
            duration = 400
        ) {
            onComplete()
        }
    }

    private fun showNoValidMoves() {
        Toast.makeText(this, R.string.no_valid_moves, Toast.LENGTH_SHORT).show()
        soundManager.error()
        vibrationManager.error()
    }

    // ==================== EVENT HANDLING ====================

    private fun handleGameEvent(event: GameEvent) {
        when (event) {
            is GameEvent.RolledSix -> {
                soundManager.playSixRolled()
                vibrationManager.sixRolled()
                Toast.makeText(this, R.string.rolled_six, Toast.LENGTH_SHORT).show()
            }
            is GameEvent.BonusTurn -> {
                Toast.makeText(this, R.string.bonus_turn, Toast.LENGTH_SHORT).show()
            }
            is GameEvent.TokenCaptured -> {
                soundManager.playCapture()
                vibrationManager.capture()
                showCaptureAnimation()
            }
            is GameEvent.TokenHome -> {
                soundManager.playTokenHome()
                vibrationManager.tokenHome()
                showHomeAnimation()
            }
            is GameEvent.Victory -> {
                soundManager.playVictory()
                vibrationManager.victory()
            }
            is GameEvent.LevelUp -> {
                soundManager.playLevelUp()
                vibrationManager.levelUp()
                showLevelUpAnimation()
            }
            is GameEvent.CoinsEarned -> {
                showCoinsEarnedAnimation(event.amount)
            }
            is GameEvent.AIThinking -> {
                binding.tvTurnStatus.text = "AI is thinking..."
            }
            else -> {}
        }
    }

    private fun handleTurnComplete(bonusTurn: Boolean) {
        lifecycleScope.launch {
            delay(500)
            
            if (bonusTurn) {
                // Bonus turn - reset for next roll
                binding.diceContainer.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .start()
            } else {
                viewModel.nextTurn()
            }
        }
    }

    private fun handleMoveResult(result: MoveResult) {
        if (result.threeConsecutiveSixes) {
            Toast.makeText(this, "3 consecutive 6s - Turn skipped!", Toast.LENGTH_SHORT).show()
        }
        
        if (result.capturedTokens.isNotEmpty()) {
            showCaptureAnimation()
        }
        
        if (result.tokenReachedHome) {
            showHomeAnimation()
        }
        
        if (result.isGameOver) {
            viewModel.handleGameOver()
        }
    }

    private fun handleGameOver(winner: Player) {
        lifecycleScope.launch {
            delay(500)
            showGameOverDialog(winner)
        }
    }

    // ==================== SPECIAL ANIMATIONS ====================

    private fun showCaptureAnimation() {
        binding.captureLottie.apply {
            visibility = View.VISIBLE
            setAnimation("capture.json")
            playAnimation()
            addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = View.GONE
                }
            })
        }
    }

    private fun showHomeAnimation() {
        binding.homeLottie.apply {
            visibility = View.VISIBLE
            setAnimation("token_home.json")
            playAnimation()
            addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = View.GONE
                }
            })
        }
    }

    private fun showLevelUpAnimation() {
        binding.levelUpLottie.apply {
            visibility = View.VISIBLE
            setAnimation("level_up.json")
            playAnimation()
        }
    }

    private fun showCoinsEarnedAnimation(coins: Long) {
        binding.tvRewardCoins.text = "+$coins"
        binding.rewardContainer.visibility = View.VISIBLE
        binding.rewardContainer.translationY = 0f
        binding.rewardContainer.alpha = 1f
        
        binding.rewardContainer.animate()
            .translationY(-100f)
            .alpha(0f)
            .setDuration(1500)
            .withEndAction {
                binding.rewardContainer.visibility = View.GONE
            }
            .start()
    }

    // ==================== DIALOGS ====================

    private fun showPauseMenu() {
        viewModel.pauseGame()
        
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DialogPauseMenuBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        
        dialogBinding.btnResume.setOnClickListener {
            viewModel.resumeGame()
            dialog.dismiss()
        }
        
        dialogBinding.btnRestart.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Restart Game?")
                .setMessage("Current progress will be lost. Are you sure?")
                .setPositiveButton("Restart") { _, _ ->
                    restartGame()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        
        dialogBinding.btnQuit.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Quit Game?")
                .setMessage("Are you sure you want to quit? This will count as a loss.")
                .setPositiveButton("Quit") { _, _ ->
                    viewModel.recordLoss()
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        
        dialogBinding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        dialog.show()
    }

    private fun showGameOverDialog(winner: Player) {
        val dialogBinding = DialogGameOverBinding.inflate(layoutInflater)
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        
        dialogBinding.tvWinnerName.text = winner.name
        
        // Check if current player won
        val isWinner = winner.id == viewModel.getCurrentPlayerId()
        
        if (isWinner) {
            dialogBinding.tvResult.text = "YOU WON!"
            dialogBinding.tvResult.setTextColor(ContextCompat.getColor(this, R.color.success))
            
            // Award coins and XP
            val coinsEarned = 100L
            val xpEarned = 50L
            
            dialogBinding.tvWinnerCoins.text = "+$coinsEarned"
            dialogBinding.tvWinnerXp.text = "+$xpEarned XP"
            
            viewModel.recordWin(coinsEarned, xpEarned)
            
            // Show confetti
            dialogBinding.confettiLottie.setAnimation("confetti.json")
            dialogBinding.confettiLottie.playAnimation()
        } else {
            dialogBinding.tvResult.text = "Game Over"
            dialogBinding.tvResult.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            
            dialogBinding.tvWinnerCoins.text = "+10"
            dialogBinding.tvWinnerXp.text = "+10 XP"
            
            viewModel.recordLoss()
        }
        
        dialogBinding.btnPlayAgain.setOnClickListener {
            restartGame()
            dialog.dismiss()
        }
        
        dialogBinding.btnHome.setOnClickListener {
            finish()
        }
        
        dialogBinding.btnShare.setOnClickListener {
            shareResult(winner)
        }
        
        dialog.show()
    }

    private fun shareResult(winner: Player) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "I just played Ludo Blitz! 🎲 ${winner.name} won! Can you beat me? Download now!")
        }
        startActivity(Intent.createChooser(shareIntent, "Share"))
    }

    // ==================== UTILITY ====================

    private fun restartGame() {
        val playerCount = intent.getIntExtra(EXTRA_PLAYER_COUNT, 4)
        val difficulty = intent.getSerializableExtra(EXTRA_DIFFICULTY) as? BotDifficulty ?: BotDifficulty.MEDIUM
        val classicMode = intent.getBooleanExtra(EXTRA_CLASSIC_MODE, true)
        
        setupGame(playerCount, difficulty, classicMode)
    }

    private fun getTokenColor(color: TokenColor): Int {
        return when (color) {
            TokenColor.RED -> ContextCompat.getColor(this, R.color.token_red)
            TokenColor.GREEN -> ContextCompat.getColor(this, R.color.token_green)
            TokenColor.YELLOW -> ContextCompat.getColor(this, R.color.token_yellow)
            TokenColor.BLUE -> ContextCompat.getColor(this, R.color.token_blue)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cleanup()
    }

    companion object {
        const val EXTRA_PLAYER_COUNT = "player_count"
        const val EXTRA_DIFFICULTY = "difficulty"
        const val EXTRA_CLASSIC_MODE = "classic_mode"
        
        fun newIntent(
            context: Context,
            playerCount: Int,
            difficulty: BotDifficulty,
            classicMode: Boolean
        ): Intent {
            return Intent(context, GameActivity::class.java).apply {
                putExtra(EXTRA_PLAYER_COUNT, playerCount)
                putExtra(EXTRA_DIFFICULTY, difficulty)
                putExtra(EXTRA_CLASSIC_MODE, classicMode)
            }
        }
    }
}
