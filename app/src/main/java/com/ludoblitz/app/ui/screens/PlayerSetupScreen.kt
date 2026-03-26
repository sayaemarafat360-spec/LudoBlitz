package com.ludoblitz.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoblitz.app.ui.theme.*

data class PlayerSetupData(
    val name: String,
    val color: Color,
    val avatar: Int,
    val isAI: Boolean = false
)

@Composable
fun PlayerSetupScreen(
    gameMode: String,
    difficulty: String,
    onNavigateToGame: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val playerColors = listOf(TokenRed, TokenGreen, TokenYellow, TokenBlue)
    val playerNames = listOf("Player 1", "Player 2", "Player 3", "Player 4")
    val avatars = listOf(
        Icons.Default.Person,
        Icons.Default.Face,
        Icons.Default.EmojiEmotions,
        Icons.Default.SportsEsports
    )

    var players by remember {
        mutableStateOf(
            listOf(
                PlayerSetupData(playerNames[0], playerColors[0], 0, false),
                PlayerSetupData(playerNames[1], playerColors[1], 1, true),
                PlayerSetupData(playerNames[2], playerColors[2], 2, true),
                PlayerSetupData(playerNames[3], playerColors[3], 3, true)
            )
        )
    }

    var expandedPlayer by remember { mutableStateOf(-1) }

    val infiniteTransition = rememberInfiniteTransition(label = "animations")
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
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
        // Background decoration
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-200).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryGold.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

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

                Column {
                    Text(
                        text = "Player Setup",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${gameMode.replaceFirstChar { it.uppercase() }} Mode • ${difficulty.replaceFirstChar { it.uppercase() }}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Player Cards
            players.forEachIndexed { index, player ->
                PlayerSetupCard(
                    player = player,
                    playerIndex = index,
                    isExpanded = expandedPlayer == index,
                    avatars = avatars,
                    onExpand = { expandedPlayer = if (expandedPlayer == index) -1 else index },
                    onNameChange = { newName ->
                        players = players.toMutableList().apply {
                            this[index] = player.copy(name = newName)
                        }
                    },
                    onAIToggle = { isAI ->
                        players = players.toMutableList().apply {
                            this[index] = player.copy(isAI = isAI)
                        }
                    },
                    onAvatarChange = { avatarIndex ->
                        players = players.toMutableList().apply {
                            this[index] = player.copy(avatar = avatarIndex)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Game Rules Summary
            GameRulesSummary(gameMode = gameMode)

            Spacer(modifier = Modifier.height(32.dp))

            // Start Game Button
            Button(
                onClick = onNavigateToGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(8.dp, RoundedCornerShape(30.dp)),
                shape = RoundedCornerShape(30.dp),
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
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = BackgroundDark,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "START GAME",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BackgroundDark,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PlayerSetupCard(
    player: PlayerSetupData,
    playerIndex: Int,
    isExpanded: Boolean,
    avatars: List<ImageVector>,
    onExpand: () -> Unit,
    onNameChange: (String) -> Unit,
    onAIToggle: (Boolean) -> Unit,
    onAvatarChange: (Int) -> Unit
) {
    val colorNames = listOf("Red", "Green", "Yellow", "Blue")

    Card(
        onClick = onExpand,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar with color indicator
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                player.color.copy(alpha = 0.2f),
                                CircleShape
                            )
                            .border(3.dp, player.color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = avatars[player.avatar],
                            contentDescription = null,
                            tint = player.color,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = player.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(player.color, CircleShape)
                            )
                            Text(
                                text = colorNames[playerIndex],
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            if (player.isAI) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "AI",
                                    tint = AccentPurple,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "AI",
                                    fontSize = 12.sp,
                                    color = AccentPurple
                                )
                            }
                        }
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Expanded Content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = CardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Name Input
                OutlinedTextField(
                    value = player.name,
                    onValueChange = onNameChange,
                    label = { Text("Player Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = player.color,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = player.color,
                        unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // AI Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = if (player.isAI) AccentPurple else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "AI Player",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Switch(
                        checked = player.isAI,
                        onCheckedChange = onAIToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentPurple,
                            checkedTrackColor = AccentPurple.copy(alpha = 0.5f),
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = CardBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar Selection
                Text(
                    text = "Select Avatar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    avatars.forEachIndexed { index, icon ->
                        AvatarOption(
                            icon = icon,
                            isSelected = player.avatar == index,
                            color = player.color,
                            onClick = { onAvatarChange(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarOption(
    icon: ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .then(
                if (isSelected) {
                    Modifier.background(color, CircleShape)
                } else {
                    Modifier
                        .background(CardBackground, CircleShape)
                        .border(2.dp, CardBorder, CircleShape)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) BackgroundDark else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun GameRulesSummary(gameMode: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PrimaryGold,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Game Rules",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val rules = listOf(
                "Roll 6 to bring token out of home",
                "Roll 6 again for an extra turn",
                "Three consecutive 6s will skip your turn",
                "Land on opponent's token to capture it",
                "Safe zones protect tokens from capture",
                "First player to get all tokens home wins!"
            )

            rules.forEach { rule ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = rule,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
