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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludoblitz.app.ui.theme.*

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToThemeSelection: () -> Unit
) {
    var soundEnabled by remember { mutableStateOf(true) }
    var musicEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }

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
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Audio Settings Section
            SettingsSection(title = "Audio", icon = Icons.Default.VolumeUp) {
                SettingsToggle(
                    title = "Sound Effects",
                    subtitle = "Dice rolls, captures, and game sounds",
                    icon = Icons.Default.VolumeUp,
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )

                SettingsToggle(
                    title = "Background Music",
                    subtitle = "Ambient music during gameplay",
                    icon = Icons.Default.MusicNote,
                    checked = musicEnabled,
                    onCheckedChange = { musicEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Haptic Settings Section
            SettingsSection(title = "Haptics", icon = Icons.Default.Vibration) {
                SettingsToggle(
                    title = "Vibration",
                    subtitle = "Vibrate on important events",
                    icon = Icons.Default.Vibration,
                    checked = vibrationEnabled,
                    onCheckedChange = { vibrationEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notifications Section
            SettingsSection(title = "Notifications", icon = Icons.Default.Notifications) {
                SettingsToggle(
                    title = "Push Notifications",
                    subtitle = "Daily rewards and game updates",
                    icon = Icons.Default.Notifications,
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Appearance Section
            SettingsSection(title = "Appearance", icon = Icons.Default.Palette) {
                SettingsItemClick(
                    title = "Theme",
                    subtitle = "Royal Gold",
                    icon = Icons.Default.Palette,
                    onClick = onNavigateToThemeSelection
                )

                SettingsItemClick(
                    title = "Language",
                    subtitle = "English",
                    icon = Icons.Default.Language,
                    onClick = { /* Open language selection */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Game Rules Section
            SettingsSection(title = "Game Rules", icon = Icons.Default.Rule) {
                SettingsItemClick(
                    title = "Customize Rules",
                    subtitle = "Modify game settings",
                    icon = Icons.Default.Tune,
                    onClick = { /* Open rules customization */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            SettingsSection(title = "About", icon = Icons.Default.Info) {
                SettingsItemClick(
                    title = "About Ludo Blitz",
                    subtitle = "Version 1.0.0",
                    icon = Icons.Default.Info,
                    onClick = { /* Show about */ }
                )

                SettingsItemClick(
                    title = "Privacy Policy",
                    subtitle = "How we handle your data",
                    icon = Icons.Default.PrivacyTip,
                    onClick = { /* Show privacy policy */ }
                )

                SettingsItemClick(
                    title = "Terms of Service",
                    subtitle = "Terms and conditions",
                    icon = Icons.Default.Description,
                    onClick = { /* Show terms */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Section
            SettingsSection(title = "Support Us", icon = Icons.Default.Favorite) {
                SettingsItemClick(
                    title = "Rate Us",
                    subtitle = "Love the game? Rate 5 stars!",
                    icon = Icons.Default.Star,
                    onClick = { /* Open play store */ }
                )

                SettingsItemClick(
                    title = "Share with Friends",
                    subtitle = "Spread the fun!",
                    icon = Icons.Default.Share,
                    onClick = { /* Share app */ }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryGold,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground.copy(alpha = 0.6f)
            )
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
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
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = subtitle,
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

@Composable
private fun SettingsItemClick(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        CardBorder.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
