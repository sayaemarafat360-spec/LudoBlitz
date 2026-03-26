package com.ludoblitz.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoblitz.app.ui.theme.*

data class ThemeItem(
    val id: String,
    val name: String,
    val previewColors: List<Color>,
    val price: Int,
    val isOwned: Boolean = false,
    val isEquipped: Boolean = false
)

@Composable
fun ThemeSelectionScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTheme by remember { mutableStateOf("royal_gold") }

    val themes = remember {
        listOf(
            ThemeItem("classic", "Classic", listOf(TokenRed, TokenGreen, TokenYellow, TokenBlue), 0, true, false),
            ThemeItem("royal_gold", "Royal Gold", listOf(PrimaryGold, PrimaryGoldDark, BackgroundDark, Color(0xFFFFA500)), 500, true, true),
            ThemeItem("neon", "Neon Nights", listOf(AccentPurple, AccentPink, AccentBlue, AccentGreen), 750, false, false),
            ThemeItem("fantasy", "Fantasy", listOf(Color(0xFF6B4EE6), Color(0xFFFF69B4), Color(0xFF00CED1), Color(0xFFFFD700)), 1000, false, false),
            ThemeItem("nature", "Nature", listOf(Color(0xFF228B22), Color(0xFF90EE90), Color(0xFF8B4513), Color(0xFF87CEEB)), 600, false, false),
            ThemeItem("ocean", "Ocean", listOf(Color(0xFF006994), Color(0xFF40E0D0), Color(0xFF20B2AA), Color(0xFF5F9EA0)), 800, false, false),
            ThemeItem("sunset", "Sunset", listOf(Color(0xFFFF6B6B), Color(0xFFFFA07A), Color(0xFFFFD93D), Color(0xFFFF8C00)), 700, false, false),
            ThemeItem("galaxy", "Galaxy", listOf(Color(0xFF1A1A2E), Color(0xFF4A4E69), Color(0xFF9A8C98), Color(0xFFC9ADA7)), 900, false, false)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "theme_animations")
    val selectedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "selected_scale"
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
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
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
                    text = "Select Theme",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Theme Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(themes) { theme ->
                    ThemeCard(
                        theme = theme,
                        isSelected = selectedTheme == theme.id,
                        scale = if (selectedTheme == theme.id) selectedScale else 1f,
                        onSelect = { selectedTheme = theme.id },
                        onEquip = { /* Equip theme */ },
                        onBuy = { /* Buy theme */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ThemeCard(
    theme: ThemeItem,
    isSelected: Boolean,
    scale: Float,
    onSelect: () -> Unit,
    onEquip: () -> Unit,
    onBuy: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .scale(scale)
            .then(
                if (isSelected) {
                    Modifier.shadow(8.dp, RoundedCornerShape(20.dp), spotColor = PrimaryGold.copy(alpha = 0.4f))
                } else {
                    Modifier.shadow(4.dp, RoundedCornerShape(16.dp))
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground.copy(alpha = 0.8f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Preview with player colors
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = theme.previewColors.take(2) + listOf(
                                    theme.previewColors[2].copy(alpha = 0.5f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Mini board preview
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        theme.previewColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(color, CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Theme name
                Text(
                    text = theme.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Price or status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    when {
                        theme.isEquipped -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Equipped",
                                    fontSize = 11.sp,
                                    color = AccentGreen
                                )
                            }
                        }
                        theme.isOwned -> {
                            TextButton(
                                onClick = onEquip,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Equip",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGold
                                )
                            }
                        }
                        else -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = PrimaryGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = theme.price.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGold
                                )
                            }
                        }
                    }
                }
            }

            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(PrimaryGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = BackgroundDark,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
