package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = White,
    primaryContainer = DarkBlue,
    onPrimaryContainer = OffWhite,
    secondary = RoyalBlue,
    onSecondary = White,
    secondaryContainer = SaudiGold,
    onSecondaryContainer = DeepNavy,
    background = OffWhite,
    onBackground = TextPrimary,
    surface = White,
    onSurface = TextPrimary,
    surfaceVariant = OffWhite,
    onSurfaceVariant = TextSecondary,
    outline = TextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = SaudiGold,
    onPrimary = DeepNavy,
    primaryContainer = RoyalBlue,
    onPrimaryContainer = OffWhite,
    secondary = RoyalBlue,
    onSecondary = White,
    secondaryContainer = SaudiGold,
    onSecondaryContainer = DeepNavy,
    background = DarkBackground,
    onBackground = OffWhite,
    surface = DarkSurface,
    onSurface = OffWhite,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = TextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
