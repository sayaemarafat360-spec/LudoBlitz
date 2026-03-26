package com.ludoblitz.app.ui.screens

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoblitz.app.ui.theme.*

@Composable
fun RulesCustomizationScreen(
    onNavigateBack: () -> Unit
) {
    var doubleTurnOnSix by remember { mutableStateOf(true) }
    var threeSixBurn by remember { mutableStateOf(true) }
    var safeZones by remember { mutableStateOf(true) }
    var captureBonus by remember { mutableStateOf(true) }
    var tokenCount by remember { mutableStateOf(4) }

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
                    text = "Game Rules",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tokens per player
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground.copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    PrimaryGold.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CatchingPokemon,
                                contentDescription = null,
                                tint = PrimaryGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Tokens per Player",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Number of tokens each player starts with",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TokenCountOption(
                            count = 2,
                            isSelected = tokenCount == 2,
                            onClick = { tokenCount = 2 }
                        )
                        TokenCountOption(
                            count = 4,
                            isSelected = tokenCount == 4,
                            onClick = { tokenCount = 4 }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rule toggles
            RuleToggleCard(
                title = "Double Turn on 6",
                description = "Roll again after getting a 6",
                icon = Icons.Default.Replay,
                checked = doubleTurnOnSix,
                onCheckedChange = { doubleTurnOnSix = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RuleToggleCard(
                title = "Three 6s Burn",
                description = "Three consecutive 6s will skip your turn",
                icon = Icons.Default.Block,
                checked = threeSixBurn,
                onCheckedChange = { threeSixBurn = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RuleToggleCard(
                title = "Safe Zones",
                description = "Tokens on safe spots cannot be captured",
                icon = Icons.Default.Security,
                checked = safeZones,
                onCheckedChange = { safeZones = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RuleToggleCard(
                title = "Capture Bonus",
                description = "Get 20 bonus moves when capturing opponent token",
                icon = Icons.Default.AddCircle,
                checked = captureBonus,
                onCheckedChange = { captureBonus = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Reset to defaults
            OutlinedButton(
                onClick = {
                    doubleTurnOnSix = true
                    threeSixBurn = true
                    safeZones = true
                    captureBonus = true
                    tokenCount = 4
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(AccentPurple, AccentPink)
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reset to Defaults",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TokenCountOption(
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(100.dp)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryGold.copy(alpha = 0.2f) else CardBackground.copy(alpha = 0.5f)
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder(true).copy(
                brush = Brush.linearGradient(
                    colors = listOf(PrimaryGold, PrimaryGoldDark)
                )
            )
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) PrimaryGold else Color.White
            )
            Text(
                text = "Tokens",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun RuleToggleCard(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground.copy(alpha = 0.6f)
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
                    .size(44.dp)
                    .background(
                        if (checked) PrimaryGold.copy(alpha = 0.2f) else CardBorder.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) PrimaryGold else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PrimaryGold,
                    checkedTrackColor = PrimaryGold.copy(alpha = 0.5f),
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = CardBorder
                )
            )
        }
    }
}
