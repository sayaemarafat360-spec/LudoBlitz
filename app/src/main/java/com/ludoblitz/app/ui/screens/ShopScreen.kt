package com.ludoblitz.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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

data class ShopItem(
    val id: String,
    val name: String,
    val price: Int,
    val type: ShopItemType,
    val icon: ImageVector,
    val color: Color,
    val isOwned: Boolean = false,
    val isNew: Boolean = false
)

enum class ShopItemType { THEME, AVATAR, DICE, TOKEN }

@Composable
fun ShopScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Themes", "Avatars", "Dice", "Tokens")

    val shopItems = remember {
        listOf(
            // Themes
            ShopItem("theme_1", "Royal Gold", 500, ShopItemType.THEME, Icons.Default.Palette, PrimaryGold, isOwned = true),
            ShopItem("theme_2", "Neon Nights", 750, ShopItemType.THEME, Icons.Default.Palette, AccentPurple, isNew = true),
            ShopItem("theme_3", "Fantasy", 1000, ShopItemType.THEME, Icons.Default.Palette, AccentPink),
            ShopItem("theme_4", "Nature", 600, ShopItemType.THEME, Icons.Default.Palette, AccentGreen),
            ShopItem("theme_5", "Ocean", 800, ShopItemType.THEME, Icons.Default.Palette, AccentBlue),

            // Avatars
            ShopItem("avatar_1", "Knight", 300, ShopItemType.AVATAR, Icons.Default.Shield, TokenRed, isOwned = true),
            ShopItem("avatar_2", "Wizard", 400, ShopItemType.AVATAR, Icons.Default.AutoFixHigh, AccentPurple, isNew = true),
            ShopItem("avatar_3", "Dragon", 600, ShopItemType.AVATAR, Icons.Default.Whatshot, TokenYellow),
            ShopItem("avatar_4", "Phoenix", 550, ShopItemType.AVATAR, Icons.Default.LocalFireDepartment, TokenRed),

            // Dice
            ShopItem("dice_1", "Classic", 0, ShopItemType.DICE, Icons.Default.Casino, Color.White, isOwned = true),
            ShopItem("dice_2", "Golden", 400, ShopItemType.DICE, Icons.Default.Casino, PrimaryGold),
            ShopItem("dice_3", "Crystal", 500, ShopItemType.DICE, Icons.Default.Casino, AccentBlue, isNew = true),
            ShopItem("dice_4", "Neon", 450, ShopItemType.DICE, Icons.Default.Casino, AccentPink),

            // Tokens
            ShopItem("token_1", "Classic", 0, ShopItemType.TOKEN, Icons.Default.CatchingPokemon, TokenRed, isOwned = true),
            ShopItem("token_2", "Gem", 350, ShopItemType.TOKEN, Icons.Default.Diamond, AccentPurple),
            ShopItem("token_3", "Star", 300, ShopItemType.TOKEN, Icons.Default.Star, PrimaryGold, isNew = true),
            ShopItem("token_4", "Ball", 400, ShopItemType.TOKEN, Icons.Default.SportsSoccer, TokenGreen)
        )
    }

    val filteredItems = when (selectedTab) {
        0 -> shopItems.filter { it.type == ShopItemType.THEME }
        1 -> shopItems.filter { it.type == ShopItemType.AVATAR }
        2 -> shopItems.filter { it.type == ShopItemType.DICE }
        else -> shopItems.filter { it.type == ShopItemType.TOKEN }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "shop_animations")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
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
                    text = "Shop",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.weight(1f))

                // Currency display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CurrencyBadge(icon = Icons.Default.MonetizationOn, amount = 5000, color = PrimaryGold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = Color.White,
                edgePadding = 24.dp,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PrimaryGold
                        )
                    }
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = PrimaryGold,
                        unselectedContentColor = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Featured Item
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(120.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AccentPurple.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "NEW!",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .background(
                                        AccentPink,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dragon Theme Pack",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Unlock all dragon items!",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(scale)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        TokenYellow,
                                        TokenRed
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grid of items
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredItems) { item ->
                    ShopItemCard(
                        item = item,
                        onBuy = { /* Purchase item */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
private fun ShopItemCard(
    item: ShopItem,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
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
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Item icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            item.color.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .border(2.dp, item.color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.color,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price or status
                when {
                    item.isOwned -> {
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
                                text = "Owned",
                                fontSize = 12.sp,
                                color = AccentGreen
                            )
                        }
                    }
                    item.price == 0 -> {
                        Text(
                            text = "FREE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen
                        )
                    }
                    else -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = PrimaryGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = item.price.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGold
                            )
                        }
                    }
                }
            }

            // New badge
            if (item.isNew) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            AccentPink,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "NEW",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
