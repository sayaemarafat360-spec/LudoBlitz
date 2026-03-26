package com.ludoblitz.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoblitz.app.R
import com.ludoblitz.app.ui.components.*
import com.ludoblitz.app.ui.theme.*

@Composable
fun MainMenuScreen(
    onNavigateToGameMode: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToDailyRewards: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "menu_animations")

    // Floating animation
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    // Pulsating animation for play button
    val playScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "play_scale"
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
        // Background decorative elements
        BackgroundDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Top bar with profile, coins, and settings
            TopBar(
                coins = 5000,
                gems = 150,
                onProfileClick = onNavigateToProfile,
                onSettingsClick = onNavigateToSettings,
                onShopClick = onNavigateToShop
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Logo section
            LogoSection(floatingOffset)

            Spacer(modifier = Modifier.height(32.dp))

            // Daily rewards banner
            DailyRewardBanner(
                onClick = onNavigateToDailyRewards,
                claimed = false
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Play Button
            PlayButton(
                onClick = onNavigateToGameMode,
                scale = playScale
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Game mode cards
            GameModeCards(
                onVsComputerClick = onNavigateToGameMode,
                onLocalMultiplayerClick = onNavigateToGameMode,
                onPlayWithFriendsClick = { /* Coming soon */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom menu items
            BottomMenuItems(
                onLeaderboardClick = onNavigateToLeaderboard,
                onShareClick = { /* Share functionality */ }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BackgroundDecorations() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top right glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryGold.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Bottom left glow
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-150).dp, y = 100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AccentPurple.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Center subtle glow
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.Center)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryGold.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun TopBar(
    coins: Int,
    gems: Int,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShopClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile button
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    CardBackground.copy(alpha = 0.6f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // Currency display
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Coins
            CurrencyBadge(
                icon = Icons.Default.MonetizationOn,
                amount = coins,
                color = PrimaryGold
            )

            // Gems
            CurrencyBadge(
                icon = Icons.Default.Diamond,
                amount = gems,
                color = AccentBlue
            )
        }

        // Settings button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    CardBackground.copy(alpha = 0.6f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun CurrencyBadge(
    icon: ImageVector,
    amount: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .background(
                CardBackground.copy(alpha = 0.8f),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
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
private fun LogoSection(floatingOffset: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.offset(y = floatingOffset.dp)
    ) {
        // Dice logo
        Box(
            modifier = Modifier
                .size(100.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = PrimaryGold.copy(alpha = 0.3f)
                )
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryGold,
                            PrimaryGoldDark
                        )
                    ),
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Casino,
                contentDescription = "Ludo Blitz",
                tint = BackgroundDark,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "LUDO",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGold,
            style = MaterialTheme.typography.displayMedium.copy(
                shadow = Shadow(
                    color = PrimaryGold.copy(alpha = 0.4f),
                    offset = Offset(0f, 3f),
                    blurRadius = 15f
                )
            )
        )
        Text(
            text = "BLITZ",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = MaterialTheme.typography.displaySmall.copy(
                shadow = Shadow(
                    color = AccentPurple.copy(alpha = 0.4f),
                    offset = Offset(0f, 3f),
                    blurRadius = 10f
                )
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Roll. Race. Dominate!",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun DailyRewardBanner(
    onClick: () -> Unit,
    claimed: Boolean
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AccentPurple.copy(alpha = 0.3f)
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
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
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = BackgroundDark,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column {
                    Text(
                        text = "Daily Rewards",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (claimed) "Come back tomorrow!" else "Tap to claim your reward!",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = PrimaryGold,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PlayButton(
    onClick: () -> Unit,
    scale: Float
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(180.dp)
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                spotColor = PrimaryGold.copy(alpha = 0.4f)
            ),
        shape = CircleShape,
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
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = BackgroundDark,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "PLAY",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BackgroundDark,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
private fun GameModeCards(
    onVsComputerClick: () -> Unit,
    onLocalMultiplayerClick: () -> Unit,
    onPlayWithFriendsClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameModeCard(
            icon = Icons.Default.Computer,
            title = "Play vs Computer",
            subtitle = "Challenge the AI opponent",
            color = TokenGreen,
            onClick = onVsComputerClick
        )

        GameModeCard(
            icon = Icons.Default.People,
            title = "Local Multiplayer",
            subtitle = "Play with friends on same device",
            color = TokenBlue,
            onClick = onLocalMultiplayerClick
        )

        GameModeCard(
            icon = Icons.Default.Wifi,
            title = "Play with Friends",
            subtitle = "Coming Soon - Online multiplayer",
            color = AccentPurple,
            onClick = onPlayWithFriendsClick,
            isLocked = true
        )
    }
}

@Composable
private fun GameModeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    isLocked: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomMenuItems(
    onLeaderboardClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BottomMenuItem(
            icon = Icons.Default.Leaderboard,
            label = "Leaderboard",
            color = PrimaryGold,
            onClick = onLeaderboardClick
        )

        BottomMenuItem(
            icon = Icons.Default.Share,
            label = "Share",
            color = AccentBlue,
            onClick = onShareClick
        )

        BottomMenuItem(
            icon = Icons.Default.Star,
            label = "Rate Us",
            color = TokenYellow,
            onClick = { /* Rate us */ }
        )
    }
}

@Composable
private fun BottomMenuItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .background(
                    CardBackground.copy(alpha = 0.6f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}
