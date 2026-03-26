package com.ludoblitz.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoblitz.app.ui.theme.*

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val wins: Int,
    val points: Int,
    val isYou: Boolean = false
)

@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    val leaderboardData = remember {
        listOf(
            LeaderboardEntry(1, "Champion", 156, 15420),
            LeaderboardEntry(2, "ProGamer", 142, 14100),
            LeaderboardEntry(3, "LudoMaster", 128, 13050),
            LeaderboardEntry(4, "DiceKing", 115, 12100),
            LeaderboardEntry(5, "Player 1", 87, 8700, isYou = true),
            LeaderboardEntry(6, "LuckyRoll", 82, 8400),
            LeaderboardEntry(7, "TokenHunter", 78, 7900),
            LeaderboardEntry(8, "BoardRunner", 72, 7500),
            LeaderboardEntry(9, "QuickSix", 65, 6800),
            LeaderboardEntry(10, "Newbie", 45, 4800)
        )
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
                    text = "Leaderboard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LeaderboardTab(
                    text = "Weekly",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                LeaderboardTab(
                    text = "All Time",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Top 3 Players
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place
                TopPlayerCard(
                    rank = 2,
                    name = leaderboardData[1].name,
                    points = leaderboardData[1].points,
                    color = Color(0xFFC0C0C0),
                    height = 140.dp
                )

                // 1st Place
                TopPlayerCard(
                    rank = 1,
                    name = leaderboardData[0].name,
                    points = leaderboardData[0].points,
                    color = PrimaryGold,
                    height = 170.dp
                )

                // 3rd Place
                TopPlayerCard(
                    rank = 3,
                    name = leaderboardData[2].name,
                    points = leaderboardData[2].points,
                    color = Color(0xFFCD7F32),
                    height = 120.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Leaderboard list
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground.copy(alpha = 0.6f)
                )
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(leaderboardData.drop(3)) { entry ->
                        LeaderboardItem(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryGold else CardBackground.copy(alpha = 0.6f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) BackgroundDark else TextSecondary
            )
        }
    }
}

@Composable
private fun TopPlayerCard(
    rank: Int,
    name: String,
    points: Int,
    color: Color,
    height: androidx.compose.ui.unit.Dp
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(height),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (rank == 1) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = BackgroundDark,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = rank.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BackgroundDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(CardBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )

            Text(
                text = "${points}pts",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun LeaderboardItem(entry: LeaderboardEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (entry.isYou) {
                    Modifier.shadow(4.dp, RoundedCornerShape(12.dp), spotColor = PrimaryGold.copy(alpha = 0.3f))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isYou) PrimaryGold.copy(alpha = 0.15f) else CardBackground.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        when (entry.rank) {
                            4 -> Color(0xFFC0C0C0).copy(alpha = 0.3f)
                            5 -> PrimaryGold.copy(alpha = 0.3f)
                            else -> CardBackground
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${entry.rank}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.isYou) PrimaryGold else Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(CardBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and stats
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = entry.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.isYou) PrimaryGold else Color.White
                    )
                    if (entry.isYou) {
                        Text(
                            text = "(You)",
                            fontSize = 10.sp,
                            color = PrimaryGold
                        )
                    }
                }
                Text(
                    text = "${entry.wins} wins",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Points
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = entry.points.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGold
                )
                Text(
                    text = "points",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
