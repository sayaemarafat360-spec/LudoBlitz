package com.ludoblitz.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoblitz.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ResultScreen(
    winner: String,
    onPlayAgain: () -> Unit,
    onNavigateToMainMenu: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "result_animations")

    // Trophy animation
    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophy_scale"
    )

    // Trophy rotation
    val trophyRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophy_rotation"
    )

    // Confetti particles
    val confettiColors = listOf(
        TokenRed, TokenGreen, TokenYellow, TokenBlue,
        AccentPurple, AccentPink, PrimaryGold
    )

    // Animation for confetti
    var showConfetti by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        delay(300)
        showContent = true
        delay(500)
        showConfetti = true
    }

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
        // Confetti effect
        if (showConfetti) {
            ConfettiEffect(colors = confettiColors)
        }

        // Background decorations
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.Center)
                .offset(y = (-100).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryGold.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Trophy
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(trophyScale)
                    .rotate(trophyRotation)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(50.dp),
                        spotColor = PrimaryGold.copy(alpha = 0.5f)
                    )
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                PrimaryGold,
                                PrimaryGoldDark
                            )
                        ),
                        RoundedCornerShape(50.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = BackgroundDark,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Winner text
            Text(
                text = "CONGRATULATIONS!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGold,
                letterSpacing = 4.sp,
                style = MaterialTheme.typography.titleLarge.copy(
                    shadow = Shadow(
                        color = PrimaryGold.copy(alpha = 0.5f),
                        offset = Offset(0f, 2f),
                        blurRadius = 10f
                    )
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = winner.ifEmpty { "Player 1" },
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = MaterialTheme.typography.displaySmall.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = Offset(0f, 4f),
                        blurRadius = 8f
                    )
                )
            )

            Text(
                text = "WON THE GAME!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground.copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Game Statistics",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            icon = Icons.Default.Casino,
                            value = "24",
                            label = "Dice Rolls"
                        )
                        StatItem(
                            icon = Icons.Default.CatchingPokemon,
                            value = "5",
                            label = "Captures"
                        )
                        StatItem(
                            icon = Icons.Default.Schedule,
                            value = "12:34",
                            label = "Duration"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Reward Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AccentGreen.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = PrimaryGold,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "+500 Coins",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGold
                        )
                        Text(
                            text = "Victory Bonus!",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    PrimaryGold,
                                    PrimaryGoldDark
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = null,
                            tint = BackgroundDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "PLAY AGAIN",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BackgroundDark,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateToMainMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(AccentPurple, AccentPink)
                    )
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MAIN MENU",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Share button
            TextButton(
                onClick = { /* Share result */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share your victory",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    CardBackground,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryGold,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun ConfettiEffect(colors: List<Color>) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Generate random confetti particles
        repeat(50) { index ->
            val color = colors[index % colors.size]
            val animDelay = (index * 50L)

            var visible by remember { mutableStateOf(false) }

            LaunchedEffect(key1 = true) {
                delay(animDelay)
                visible = true
            }

            if (visible) {
                val xOffset by infiniteTransition.animateFloat(
                    initialValue = (index % 10) * 40f,
                    targetValue = (index % 10) * 40f + 100f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000 + (index * 100), easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "x_$index"
                )

                val yOffset by infiniteTransition.animateFloat(
                    initialValue = -100f,
                    targetValue = 1000f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000 + (index * 200), easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "y_$index"
                )

                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rot_$index"
                )

                Box(
                    modifier = Modifier
                        .offset(
                            x = xOffset.dp,
                            y = yOffset.dp
                        )
                        .rotate(rotation)
                        .size(if (index % 2 == 0) 8.dp else 12.dp)
                        .background(
                            color,
                            if (index % 3 == 0) CircleShape else RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}
