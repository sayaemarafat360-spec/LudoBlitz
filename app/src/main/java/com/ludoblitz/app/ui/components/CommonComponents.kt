package com.ludoblitz.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoblitz.app.ui.theme.*

@Composable
fun LudoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "button_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = ButtonDisabled
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) {
                        Brush.linearGradient(
                            colors = listOf(
                                PrimaryGold,
                                PrimaryGoldDark
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                ButtonDisabled,
                                ButtonDisabled
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = BackgroundDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BackgroundDark,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
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
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun PlayerToken(
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "token_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "token_scale"
    )

    Box(
        modifier = modifier
            .size(32.dp)
            .scale(scale)
            .shadow(4.dp, CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        color,
                        color.copy(alpha = 0.8f)
                    )
                ),
                CircleShape
            )
            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
    )
}

@Composable
fun CoinDisplay(
    amount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                CardBackground.copy(alpha = 0.8f),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = null,
            tint = PrimaryGold,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = if (amount >= 1000) "${amount / 1000}K" else amount.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun GemDisplay(
    amount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                CardBackground.copy(alpha = 0.8f),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Diamond,
            contentDescription = null,
            tint = AccentBlue,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = amount.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun AnimatedDice(
    value: Int,
    isRolling: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.Red
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dice_animation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isRolling) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .size(80.dp)
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
        // Dice dots
        DiceDots(value = if (isRolling) (1..6).random() else value, color = color)
    }
}

@Composable
private fun DiceDots(value: Int, color: Color) {
    val dotPositions = when (value) {
        1 -> listOf(Offset(0, 0))
        2 -> listOf(Offset(-12, -12), Offset(12, 12))
        3 -> listOf(Offset(-12, -12), Offset(0, 0), Offset(12, 12))
        4 -> listOf(
            Offset(-12, -12), Offset(12, -12),
            Offset(-12, 12), Offset(12, 12)
        )
        5 -> listOf(
            Offset(-12, -12), Offset(12, -12),
            Offset(0, 0),
            Offset(-12, 12), Offset(12, 12)
        )
        6 -> listOf(
            Offset(-12, -12), Offset(12, -12),
            Offset(-12, 0), Offset(12, 0),
            Offset(-12, 12), Offset(12, 12)
        )
        else -> emptyList()
    }

    Box(
        modifier = Modifier.size(60.dp),
        contentAlignment = Alignment.Center
    ) {
        dotPositions.forEach { (x, y) ->
            Box(
                modifier = Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size(12.dp)
                    .background(color, CircleShape)
            )
        }
    }
}

data class Offset(val x: Int, val y: Int)
