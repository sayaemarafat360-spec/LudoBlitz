package com.ludoblitz.app.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ludoblitz.app.data.models.*
import com.ludoblitz.app.data.viewmodel.GameViewModel
import com.ludoblitz.app.ui.theme.*
import com.ludoblitz.app.utils.BoardConstants
import com.ludoblitz.app.utils.BoardPath
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameBoardScreen(
    onNavigateToResult: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: GameViewModel = viewModel()
    val gameState by viewModel.gameState.collectAsState()
    val showExitDialog by viewModel.showExitDialog.collectAsState()
    val selectedTokenId by viewModel.selectedTokenId.collectAsState()
    val context = LocalContext.current

    // Initialize game if not already started
    var gameInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!gameInitialized) {
            viewModel.startGame(
                mode = GameMode.CLASSIC,
                difficulty = AIDifficulty.MEDIUM,
                playerCount = 4
            )
            gameInitialized = true
        }
    }

    // Handle game over
    LaunchedEffect(gameState?.phase) {
        if (gameState?.phase == GamePhase.GAME_OVER) {
            delay(1500)
            gameState?.winner?.let { winner ->
                onNavigateToResult(winner.name)
            }
        }
    }

    // Dice animation
    var diceRotation by remember { mutableStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "dice")

    val rotationAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulsating animation for current player indicator
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val currentPlayer = gameState?.players?.getOrNull(gameState?.currentPlayerIndex ?: 0)
    val playerColors = listOf(TokenRed, TokenGreen, TokenYellow, TokenBlue)
    val playerNames = gameState?.players?.map { it.name } ?: listOf("You", "AI 1", "AI 2", "AI 3")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDark,
                        BackgroundDarker,
                        Color(0xFF0A0A15)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            GameTopBar(
                currentPlayer = gameState?.currentPlayerIndex ?: 0,
                playerNames = playerNames,
                playerColors = playerColors,
                pulseScale = pulseScale,
                onBackClick = { viewModel.showExitDialog() }
            )

            // Game Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(12.dp)
            ) {
                gameState?.let { state ->
                    LudoBoard(
                        players = state.players,
                        possibleMoves = state.possibleMoves,
                        selectedTokenId = selectedTokenId,
                        onTokenClick = { tokenId ->
                            viewModel.selectToken(tokenId)
                        }
                    )
                }
            }

            // Game message
            gameState?.message?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground.copy(alpha = 0.8f)
                    )
                ) {
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }

            // Bottom controls
            gameState?.let { state ->
                GameBottomControls(
                    diceValue = state.diceValue,
                    isRolling = state.isRolling,
                    rotation = if (state.isRolling) rotationAnimation else 0f,
                    playerColor = currentPlayer?.color?.color ?: TokenRed,
                    canRoll = state.phase == GamePhase.WAITING_FOR_ROLL &&
                            !gameEngineIsCurrentPlayerAI(state),
                    onRollDice = { viewModel.rollDice() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideExitDialog() },
            title = {
                Text(
                    text = "Exit Game?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to quit? Your progress will be lost.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmExit()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Error
                    )
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.hideExitDialog() }) {
                    Text("Cancel")
                }
            },
            containerColor = CardBackground,
            titleContentColor = Color.White,
            textContentColor = TextSecondary
        )
    }
}

// Helper function to check if current player is AI
private fun gameEngineIsCurrentPlayerAI(state: GameState): Boolean {
    return state.players.getOrNull(state.currentPlayerIndex)?.isAI ?: false
}

@Composable
private fun GameTopBar(
    currentPlayer: Int,
    playerNames: List<String>,
    playerColors: List<Color>,
    pulseScale: Float,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .background(
                    CardBackground.copy(alpha = 0.6f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Current player indicator
        if (currentPlayer in playerColors.indices && currentPlayer in playerNames.indices) {
            Card(
                modifier = Modifier
                    .scale(pulseScale)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = playerColors[currentPlayer].copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(playerColors[currentPlayer], CircleShape)
                    )
                    Text(
                        text = "${playerNames[currentPlayer]}'s Turn",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Settings button
        IconButton(
            onClick = { /* Open settings */ },
            modifier = Modifier
                .size(40.dp)
                .background(
                    CardBackground.copy(alpha = 0.6f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LudoBoard(
    players: List<Player>,
    possibleMoves: List<TokenMove>,
    selectedTokenId: Int?,
    onTokenClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(BoardBackground)
    ) {
        // Draw board base
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawBoardBase(this)
        }

        // Draw player home bases
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left column (Red and Green homes)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                PlayerHomeBase(
                    player = players.getOrNull(0),
                    possibleMoves = possibleMoves,
                    selectedTokenId = selectedTokenId,
                    onTokenClick = onTokenClick,
                    modifier = Modifier.weight(1f)
                )
                PlayerHomeBase(
                    player = players.getOrNull(2),
                    possibleMoves = possibleMoves,
                    selectedTokenId = selectedTokenId,
                    onTokenClick = onTokenClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // Center column
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                CenterHome()
            }

            // Right column (Yellow and Blue homes)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                PlayerHomeBase(
                    player = players.getOrNull(1),
                    possibleMoves = possibleMoves,
                    selectedTokenId = selectedTokenId,
                    onTokenClick = onTokenClick,
                    modifier = Modifier.weight(1f)
                )
                PlayerHomeBase(
                    player = players.getOrNull(3),
                    possibleMoves = possibleMoves,
                    selectedTokenId = selectedTokenId,
                    onTokenClick = onTokenClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Draw active tokens on board
        players.forEach { player ->
            player.tokens.filter { it.state == TokenState.ACTIVE }.forEach { token ->
                // This would draw tokens at their positions
                // For simplicity, we're showing them in home bases
            }
        }
    }
}

private fun drawBoardBase(drawScope: DrawScope) {
    val size = drawScope.size
    val cellSize = size.minDimension / 15f

    // Draw colored paths to center
    // Red path (going down from top center-left)
    for (i in 1..5) {
        drawScope.drawRoundRect(
            color = TokenRed.copy(alpha = 0.3f),
            topLeft = Offset(6 * cellSize, i * cellSize),
            size = Size(cellSize, cellSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )
    }

    // Green path (going right from left center-top)
    for (i in 1..5) {
        drawScope.drawRoundRect(
            color = TokenGreen.copy(alpha = 0.3f),
            topLeft = Offset(i * cellSize, 6 * cellSize),
            size = Size(cellSize, cellSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )
    }

    // Yellow path (going up from bottom center-right)
    for (i in 9..13) {
        drawScope.drawRoundRect(
            color = TokenYellow.copy(alpha = 0.3f),
            topLeft = Offset(8 * cellSize, i * cellSize),
            size = Size(cellSize, cellSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )
    }

    // Blue path (going left from right center-bottom)
    for (i in 9..13) {
        drawScope.drawRoundRect(
            color = TokenBlue.copy(alpha = 0.3f),
            topLeft = Offset(i * cellSize, 8 * cellSize),
            size = Size(cellSize, cellSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )
    }

    // Draw safe zone indicators
    BoardConstants.SAFE_POSITIONS.forEach { position ->
        val coord = BoardPath.MAIN_TRACK_COORDINATES.getOrElse(position) { Pair(7, 7) }
        drawScope.drawCircle(
            color = Color.Gray.copy(alpha = 0.2f),
            radius = cellSize / 4,
            center = Offset(coord.second * cellSize + cellSize / 2, coord.first * cellSize + cellSize / 2)
        )
    }
}

@Composable
private fun PlayerHomeBase(
    player: Player?,
    possibleMoves: List<TokenMove>,
    selectedTokenId: Int?,
    onTokenClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val playerColor = player?.color?.color ?: TokenRed
    val lightColor = player?.color?.lightColor ?: TokenRedLight

    val tokensInHome = player?.tokens?.filter { it.state == TokenState.HOME } ?: emptyList()
    val tokensFinished = player?.tokens?.filter { it.state == TokenState.FINISHED } ?: emptyList()

    // Check which home tokens can be moved (need 6)
    val movableHomeTokens = possibleMoves.filter { it.isEnteringBoard }.map { it.token.id }

    Box(
        modifier = modifier
            .padding(6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(lightColor.copy(alpha = 0.25f))
    ) {
        // Home base with token positions
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (i in 0 until 2) {
                    if (i < tokensInHome.size) {
                        val token = tokensInHome[i]
                        val canMove = token.id in movableHomeTokens
                        val isSelected = token.id == selectedTokenId

                        HomeToken(
                            color = playerColor,
                            canMove = canMove,
                            isSelected = isSelected,
                            onClick = {
                                if (canMove) onTokenClick(token.id)
                            }
                        )
                    } else {
                        // Empty slot
                        EmptySlot(color = playerColor)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (i in 2 until 4) {
                    if (i < tokensInHome.size) {
                        val token = tokensInHome[i]
                        val canMove = token.id in movableHomeTokens
                        val isSelected = token.id == selectedTokenId

                        HomeToken(
                            color = playerColor,
                            canMove = canMove,
                            isSelected = isSelected,
                            onClick = {
                                if (canMove) onTokenClick(token.id)
                            }
                        )
                    } else {
                        EmptySlot(color = playerColor)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Show finished count
        if (tokensFinished.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(playerColor.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "🏆 ${tokensFinished.size}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun HomeToken(
    color: Color,
    canMove: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "token_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (canMove) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .size(26.dp)
            .scale(if (isSelected) 1.2f else pulseScale)
            .then(
                if (canMove) {
                    Modifier.shadow(6.dp, CircleShape, spotColor = color.copy(alpha = 0.6f))
                } else {
                    Modifier.shadow(3.dp, CircleShape)
                }
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        color,
                        color.copy(alpha = 0.85f)
                    )
                ),
                CircleShape
            )
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, Color.White, CircleShape)
                } else {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                }
            )
            .then(
                if (canMove) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    )
}

@Composable
private fun EmptySlot(color: Color) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .background(
                color.copy(alpha = 0.15f),
                CircleShape
            )
            .border(1.dp, color.copy(alpha = 0.3f), CircleShape)
    )
}

@Composable
private fun CenterHome() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Center triangle areas
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(
                    Color.White.copy(alpha = 0.9f),
                    RoundedCornerShape(6.dp)
                )
        ) {
            // Four colored triangles
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(TokenRed.copy(alpha = 0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(TokenGreen.copy(alpha = 0.3f))
                    )
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(TokenYellow.copy(alpha = 0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(TokenBlue.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

@Composable
private fun GameBottomControls(
    diceValue: Int,
    isRolling: Boolean,
    rotation: Float,
    playerColor: Color,
    canRoll: Boolean,
    onRollDice: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dice display
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .rotate(rotation)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFF5F5F5)
                            )
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                DiceFace(value = if (isRolling) (1..6).random() else diceValue, color = playerColor)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Roll button
            Button(
                onClick = onRollDice,
                enabled = canRoll && !isRolling,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp)
                    .shadow(4.dp, RoundedCornerShape(25.dp)),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = playerColor,
                    disabledContainerColor = ButtonDisabled
                )
            ) {
                if (isRolling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ROLL DICE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (!canRoll && !isRolling) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AI is thinking...",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DiceFace(value: Int, color: Color) {
    val dotSize = 10.dp
    val spacing = 12.dp

    Box(
        modifier = Modifier.size(60.dp),
        contentAlignment = Alignment.Center
    ) {
        when (value) {
            1 -> {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .background(color, CircleShape)
                )
            }
            2 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                    }
                }
            }
            3 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                    }
                }
            }
            4 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                    }
                }
            }
            5 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                        Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                    }
                }
            }
            6 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                            Box(modifier = Modifier.size(dotSize).background(color, CircleShape))
                        }
                    }
                }
            }
        }
    }
}
