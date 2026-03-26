package com.ludoblitz.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoblitz.app.R
import com.ludoblitz.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToMainMenu: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    var showLogo by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }
    var showDice by remember { mutableStateOf(false) }

    // Logo rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Floating animation for dice
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    // Pulsating animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Progress animation
    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(2000, easing = EaseOutQuad),
        label = "progress"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(300)
        showLogo = true
        delay(500)
        showTitle = true
        delay(400)
        showTagline = true
        delay(300)
        showDice = true
        delay(2000)
        onNavigateToMainMenu()
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
        // Background decorative circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryGold.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AccentPurple.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo
            AnimatedVisibility(
                visible = showLogo,
                enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.5f),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(scale)
                        .shadow(
                            elevation = 20.dp,
                            shape = CircleShape,
                            spotColor = PrimaryGold.copy(alpha = 0.3f)
                        )
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    PrimaryGold,
                                    PrimaryGoldDark
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Dice Icon
                    Box(
                        modifier = Modifier
                            .rotate(rotation)
                            .size(100.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color(0xFFF0F0F0)
                                    )
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Dice dots pattern
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Dot()
                                Spacer(modifier = Modifier.width(20.dp))
                                Dot()
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Dot()
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Dot()
                                Spacer(modifier = Modifier.width(20.dp))
                                Dot()
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Title
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LUDO",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGold,
                        style = MaterialTheme.typography.displayLarge.copy(
                            shadow = Shadow(
                                color = PrimaryGold.copy(alpha = 0.5f),
                                offset = Offset(0f, 4f),
                                blurRadius = 20f
                            )
                        )
                    )
                    Text(
                        text = "BLITZ",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = MaterialTheme.typography.displayMedium.copy(
                            shadow = Shadow(
                                color = AccentPurple.copy(alpha = 0.5f),
                                offset = Offset(0f, 4f),
                                blurRadius = 15f
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline
            AnimatedVisibility(
                visible = showTagline,
                enter = fadeIn(tween(500)),
                exit = fadeOut()
            ) {
                Text(
                    text = "Roll. Race. Dominate!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Decorative dice
            AnimatedVisibility(
                visible = showDice,
                enter = fadeIn(tween(500)) + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Row(
                    modifier = Modifier.offset(y = floatingOffset.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MiniDice(number = 1, color = TokenRed)
                    MiniDice(number = 3, color = TokenGreen)
                    MiniDice(number = 5, color = TokenYellow)
                    MiniDice(number = 6, color = TokenBlue)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Loading progress
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(300)),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Loading...",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(CardBackground)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            PrimaryGold,
                                            PrimaryGoldDark
                                        )
                                    )
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Dot(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF1A1A2E)
) {
    Box(
        modifier = modifier
            .size(12.dp)
            .background(color, CircleShape)
    )
}

@Composable
private fun MiniDice(
    number: Int,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFF5F5F5)
                    )
                ),
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
