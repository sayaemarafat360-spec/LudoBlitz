package com.ludoblitz.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGold,
    onPrimary = BackgroundDark,
    primaryContainer = PrimaryGoldDark,
    onPrimaryContainer = PrimaryGoldLight,
    secondary = AccentPurple,
    onSecondary = Color.White,
    secondaryContainer = AccentPurple.copy(alpha = 0.3f),
    onSecondaryContainer = Color.White,
    tertiary = AccentBlue,
    onTertiary = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = Color.White,
    errorContainer = Error.copy(alpha = 0.2f),
    outline = CardBorder,
    outlineVariant = CardBorder,
    inverseSurface = TextPrimary,
    inverseOnSurface = BackgroundDark,
    inversePrimary = PrimaryGoldDark,
    scrim = BackgroundDarker
)

@Composable
fun LudoBlitzTheme(
    darkTheme: Boolean = true, // Always use dark theme for this app
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to maintain custom theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            darkColorScheme() // Use our custom dark scheme instead
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Always use dark theme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDarker.toArgb()
            window.navigationBarColor = BackgroundDarker.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LudoTypography,
        content = content
    )
}
