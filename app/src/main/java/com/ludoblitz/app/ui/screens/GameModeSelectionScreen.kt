package com.ludoblitz.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoblitz.app.ui.theme.*

@Composable
fun GameModeSelectionScreen(
    onNavigateToPlayerSetup: (String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedMode by remember { mutableStateOf("classic") }
    var selectedDifficulty by remember { mutableStateOf("medium") }

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

                Text(
                    text = "Select Game Mode",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Game Mode Selection
            SectionTitle(
                title = "Game Mode",
                icon = Icons.Default.VideogameAsset
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ModeCard(
                    modifier = Modifier.weight(1f),
                    title = "Classic",
                    subtitle = "4 Tokens",
                    icon = Icons.Default.Stars,
                    color = PrimaryGold,
                    isSelected = selectedMode == "classic",
                    onClick = { selectedMode = "classic" }
                )

                ModeCard(
                    modifier = Modifier.weight(1f),
                    title = "Quick",
                    subtitle = "2 Tokens",
                    icon = Icons.Default.FlashOn,
                    color = AccentPurple,
                    isSelected = selectedMode == "quick",
                    onClick = { selectedMode = "quick" }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Difficulty Selection
            SectionTitle(
                title = "AI Difficulty",
                icon = Icons.Default.Psychology
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DifficultyCard(
                    title = "Easy",
                    description = "Perfect for beginners. AI makes random moves.",
                    color = EasyColor,
                    icon = Icons.Default.SentimentSatisfied,
                    isSelected = selectedDifficulty == "easy",
                    onClick = { selectedDifficulty = "easy" }
                )

                DifficultyCard(
                    title = "Medium",
                    description = "Balanced challenge. AI uses basic strategy.",
                    color = MediumColor,
                    icon = Icons.Default.SentimentNeutral,
                    isSelected = selectedDifficulty == "medium",
                    onClick = { selectedDifficulty = "medium" }
                )

                DifficultyCard(
                    title = "Hard",
                    description = "Expert level. AI uses advanced tactics.",
                    color = HardColor,
                    icon = Icons.Default.SentimentVeryDissatisfied,
                    isSelected = selectedDifficulty == "hard",
                    onClick = { selectedDifficulty = "hard" }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Number of Players
            SectionTitle(
                title = "Number of Players",
                icon = Icons.Default.People
            )

            Spacer(modifier = Modifier.height(16.dp))

            PlayerCountSelector()

            Spacer(modifier = Modifier.height(40.dp))

            // Start Game Button
            Button(
                onClick = { onNavigateToPlayerSetup(selectedMode, selectedDifficulty) },
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
private fun SectionTitle(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryGold,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ModeCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(140.dp)
            .shadow(
                elevation = if (isSelected) 12.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isSelected) color.copy(alpha = 0.3f) else Color.Transparent
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.2f) else CardBackground.copy(alpha = 0.6f)
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder(true)
        } else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.15f),
                                    color.copy(alpha = 0.05f)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
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
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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

            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun DifficultyCard(
    title: String,
    description: String,
    color: Color,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isSelected) 8.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f) else CardBackground.copy(alpha = 0.6f)
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
                    .size(52.dp)
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

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayerCountSelector() {
    var selectedCount by remember { mutableStateOf(4) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground.copy(alpha = 0.4f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        (2..4).forEach { count ->
            PlayerCountButton(
                count = count,
                isSelected = selectedCount == count,
                onClick = { selectedCount = count }
            )
        }
    }
}

@Composable
private fun PlayerCountButton(
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(64.dp)
                .then(
                    if (isSelected) {
                        Modifier.background(
                            PrimaryGold,
                            CircleShape
                        )
                    } else {
                        Modifier.background(
                            CardBackground,
                            CircleShape
                        )
                    }
                )
        ) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) BackgroundDark else Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Players",
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}
