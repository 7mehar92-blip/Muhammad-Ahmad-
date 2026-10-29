package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AlgoDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF001F28),
    primaryContainer = Color(0xFF004D59),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = CyberPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3700B3),
    onSecondaryContainer = Color(0xFFD0BCFF),
    tertiary = CyberGold,
    onTertiary = Color(0xFF231B00),
    background = CyberBackground,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CyberSurfaceBorder,
    error = CyberRed,
    onError = Color.White
)

@Composable
fun AlgoTradeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AlgoDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AlgoTradeTheme(content = content)
}
