package com.ludoblitz.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoblitz.app.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAvatarSelection: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "profile_animations")
    val avatarScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_scale"
    )

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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                    text = "Profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.weight(1f))

                // Edit button
                IconButton(
                    onClick = { /* Edit profile */ },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            CardBackground.copy(alpha = 0.6f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Avatar and Name
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(avatarScale)
                        .shadow(16.dp, CircleShape, spotColor = PrimaryGold.copy(alpha = 0.3f))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    PrimaryGold,
                                    PrimaryGoldDark
                                )
                            ),
                            CircleShape
                        )
                        .border(4.dp, PrimaryGoldLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = BackgroundDark,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Player 1",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = PrimaryGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Level 15",
                        fontSize = 14.sp,
                        color = PrimaryGold
                    )
                }

                // Level progress bar
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .width(200.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CardBackground)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.7f)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryGold, PrimaryGoldDark)
                                )
                            )
                    )
                }

                Text(
                    text = "2450 / 3500 XP",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Currency Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground.copy(alpha = 0.8f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CurrencyDisplay(
                        icon = Icons.Default.MonetizationOn,
                        amount = 5000,
                        label = "Coins",
                        color = PrimaryGold
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(60.dp)
                            .background(CardBorder)
                    )

                    CurrencyDisplay(
                        icon = Icons.Default.Diamond,
                        amount = 150,
                        label = "Gems",
                        color = AccentBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Statistics
            Text(
                text = "Statistics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    StatRow(
                        icon = Icons.Default.SportsEsports,
                        label = "Games Played",
                        value = "142"
                    )
                    Divider(color = CardBorder, modifier = Modifier.padding(vertical = 8.dp))
                    StatRow(
                        icon = Icons.Default.EmojiEvents,
                        label = "Games Won",
                        value = "87"
                    )
                    Divider(color = CardBorder, modifier = Modifier.padding(vertical = 8.dp))
                    StatRow(
                        icon = Icons.Default.TrendingUp,
                        label = "Win Rate",
                        value = "61.3%"
                    )
                    Divider(color = CardBorder, modifier = Modifier.padding(vertical = 8.dp))
                    StatRow(
                        icon = Icons.Default.CatchingPokemon,
                        label = "Total Captures",
                        value = "543"
                    )
                    Divider(color = CardBorder, modifier = Modifier.padding(vertical = 8.dp))
                    StatRow(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "Best Win Streak",
                        value = "12"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Achievements preview
            Text(
                text = "Recent Achievements",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AchievementCard(
                    icon = Icons.Default.EmojiEvents,
                    title = "First Win",
                    color = PrimaryGold,
                    modifier = Modifier.weight(1f)
                )
                AchievementCard(
                    icon = Icons.Default.LocalFireDepartment,
                    title = "Hot Streak",
                    color = TokenRed,
                    modifier = Modifier.weight(1f)
                )
                AchievementCard(
                    icon = Icons.Default.Star,
                    title = "Star Player",
                    color = AccentPurple,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Button(
                onClick = onNavigateToAvatarSelection,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPurple
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Change Avatar",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { /* Share profile */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryGold, PrimaryGoldDark)
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share Profile",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CurrencyDisplay(
    icon: ImageVector,
    amount: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (amount >= 1000) "${amount / 1000}K" else amount.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun StatRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(CardBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryGold,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = label,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun AchievementCard(
    icon: ImageVector,
    title: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
