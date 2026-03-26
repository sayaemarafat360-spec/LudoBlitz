package com.ludoblitz.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoblitz.app.ui.theme.*

data class DailyReward(
    val day: Int,
    val coins: Int,
    val gems: Int = 0,
    val isClaimed: Boolean = false,
    val isCurrent: Boolean = false
)

@Composable
fun DailyRewardsScreen(
    onNavigateBack: () -> Unit
) {
    var rewards by remember {
        mutableStateOf(
            listOf(
                DailyReward(1, 100, isClaimed = true),
                DailyReward(2, 150, isClaimed = true),
                DailyReward(3, 200, isCurrent = true),
                DailyReward(4, 250),
                DailyReward(5, 300),
                DailyReward(6, 400, gems = 5),
                DailyReward(7, 500, gems = 10)
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rewards_animations")
    val giftScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gift_scale"
    )

    val currentReward = rewards.find { it.isCurrent }

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
        // Background decoration
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-100).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryGold.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            CardBackground.copy(alpha = 0.6f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Daily Rewards",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Gift Box Animation
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(giftScale)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = PrimaryGold.copy(alpha = 0.4f)
                        )
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    PrimaryGold,
                                    PrimaryGoldDark
                                )
                            ),
                            RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Daily Gift",
                        tint = BackgroundDark,
                        modifier = Modifier.size(70.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Day ${currentReward?.day ?: 3} Reward",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = PrimaryGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "+${currentReward?.coins ?: 200}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGold
                        )
                    }

                    if ((currentReward?.gems ?: 0) > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "+${currentReward?.gems ?: 0}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Claim button
                Button(
                    onClick = {
                        // Claim reward logic
                        rewards = rewards.map {
                            when {
                                it.isCurrent -> it.copy(isClaimed = true, isCurrent = false)
                                !it.isClaimed && it.day == (currentReward?.day ?: 3) + 1 -> it.copy(isCurrent = true)
                                else -> it
                            }
                        }
                    },
                    enabled = currentReward?.isClaimed == false,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGold,
                        disabledContainerColor = ButtonDisabled
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CLAIM",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BackgroundDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 7 Day Progress
            Text(
                text = "7 Day Streak",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rewards.forEach { reward ->
                    DayRewardCard(
                        reward = reward,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bonus Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AccentPurple.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(AccentPurple.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Streak Bonus",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Login daily to maintain your streak and get bonus rewards!",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Watch Ad for Extra
            Card(
                onClick = { /* Watch ad */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(PrimaryGold.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = PrimaryGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Watch Ad for Extra",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = PrimaryGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "+50 Coins",
                                    fontSize = 12.sp,
                                    color = PrimaryGold
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DayRewardCard(
    reward: DailyReward,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "day_${reward.day}")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (reward.isCurrent) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier.scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .then(
                    when {
                        reward.isClaimed -> Modifier.background(
                            AccentGreen.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        reward.isCurrent -> Modifier
                            .background(
                                PrimaryGold.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                            .border(2.dp, PrimaryGold, RoundedCornerShape(12.dp))
                        else -> Modifier.background(
                            CardBackground.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                reward.isClaimed -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                reward.isCurrent -> {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = PrimaryGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                else -> {
                    Text(
                        text = reward.coins.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Day ${reward.day}",
            fontSize = 8.sp,
            color = if (reward.isCurrent) PrimaryGold else TextSecondary
        )
    }
}
