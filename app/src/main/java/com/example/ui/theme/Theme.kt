package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MiuiGreen,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003816),
    onPrimaryContainer = Color(0xFF80FFAD),
    secondary = MiuiBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF00315F),
    tertiary = MiuiCyan,
    background = MiuiDarkBackground,
    onBackground = MiuiDarkOnSurface,
    surface = MiuiDarkSurface,
    onSurface = MiuiDarkOnSurface,
    surfaceVariant = MiuiDarkSurfaceVariant,
    onSurfaceVariant = MiuiDarkOnSurfaceVariant,
    outline = MiuiDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = MiuiGreenDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7FDD9),
    onPrimaryContainer = Color(0xFF002208),
    secondary = MiuiBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4E7FF),
    tertiary = MiuiCyan,
    background = MiuiLightBackground,
    onBackground = MiuiLightOnSurface,
    surface = MiuiLightSurface,
    onSurface = MiuiLightOnSurface,
    surfaceVariant = MiuiLightSurfaceVariant,
    onSurfaceVariant = MiuiLightOnSurfaceVariant,
    outline = MiuiLightOutline
)

@Composable
fun VoltPowerTheme(
    themeMode: String = "SYSTEM", // SYSTEM, DARK, LIGHT
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
