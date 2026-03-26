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

data class AvatarItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val price: Int,
    val isOwned: Boolean = false,
    val isEquipped: Boolean = false,
    val isNew: Boolean = false
)

@Composable
fun AvatarSelectionScreen(
    onNavigateBack: () -> Unit
) {
    var selectedAvatar by remember { mutableStateOf("avatar_1") }

    val avatars = remember {
        listOf(
            AvatarItem("avatar_1", "Player", Icons.Default.Person, PrimaryGold, 0, true, true),
            AvatarItem("avatar_2", "Knight", Icons.Default.Shield, TokenRed, 300, true, false),
            AvatarItem("avatar_3", "Wizard", Icons.Default.AutoFixHigh, AccentPurple, 400, false, false, true),
            AvatarItem("avatar_4", "Dragon", Icons.Default.Whatshot, TokenYellow, 600, false, false),
            AvatarItem("avatar_5", "Phoenix", Icons.Default.LocalFireDepartment, Color(0xFFFF6B6B), 550, false, false),
            AvatarItem("avatar_6", "Star", Icons.Default.Star, PrimaryGold, 250, true, false),
            AvatarItem("avatar_7", "Crown", Icons.Default.EmojiEvents, PrimaryGold, 500, false, false),
            AvatarItem("avatar_8", "Heart", Icons.Default.Favorite, TokenRed, 200, true, false),
            AvatarItem("avatar_9", "Lightning", Icons.Default.FlashOn, AccentBlue, 350, false, false, true),
            AvatarItem("avatar_10", "Music", Icons.Default.MusicNote, AccentPink, 280, false, false),
            AvatarItem("avatar_11", "Sports", Icons.Default.SportsEsports, AccentGreen, 320, false, false),
            AvatarItem("avatar_12", "Diamond", Icons.Default.Diamond, AccentBlue, 450, false, false)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "avatar_animations")
    val selectedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "selected_scale"
    )

    val selectedAvatarItem = avatars.find { it.id == selectedAvatar }

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
                    text = "Select Avatar",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Selected Avatar Preview
            selectedAvatarItem?.let { avatar ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(selectedScale)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                spotColor = avatar.color.copy(alpha = 0.4f)
                            )
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        avatar.color,
                                        avatar.color.copy(alpha = 0.7f)
                                    )
                                ),
                                CircleShape
                            )
                            .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = avatar.icon,
                            contentDescription = avatar.name,
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = avatar.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (avatar.isEquipped) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Equipped",
                                fontSize = 14.sp,
                                color = AccentGreen
                            )
                        }
                    } else if (avatar.isOwned) {
                        Button(
                            onClick = { /* Equip avatar */ },
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGold
                            )
                        ) {
                            Text(
                                text = "EQUIP",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BackgroundDark
                            )
                        }
                    } else {
                        Button(
                            onClick = { /* Buy avatar */ },
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPurple
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = avatar.price.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Avatar Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(avatars) { avatar ->
                    AvatarGridItem(
                        avatar = avatar,
                        isSelected = selectedAvatar == avatar.id,
                        onClick = { selectedAvatar = avatar.id }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AvatarGridItem(
    avatar: AvatarItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier
                        .background(avatar.color)
                        .border(3.dp, Color.White, CircleShape)
                } else {
                    Modifier
                        .background(CardBackground.copy(alpha = 0.6f))
                        .border(
                            2.dp,
                            if (avatar.isOwned) avatar.color.copy(alpha = 0.5f) else CardBorder,
                            CircleShape
                        )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (avatar.isOwned || isSelected) {
            Icon(
                imageVector = avatar.icon,
                contentDescription = avatar.name,
                tint = if (isSelected) Color.White else avatar.color,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = avatar.icon,
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(16.dp)
                        .background(CardBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        // New badge
        if (avatar.isNew && !isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(8.dp)
                    .background(AccentPink, CircleShape)
            )
        }
    }
}
