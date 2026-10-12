package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun isAppDark(): Boolean = LocalIsDarkTheme.current

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
    dynamicColor: Boolean = true, // Monet wallpaper color integration
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val colorScheme = when {
        // Monet wallpaper dynamic color on Android 12+ (API 31+) blended with crisp MIUIX surfaces
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) {
                val dynamic = dynamicDarkColorScheme(context)
                dynamic.copy(
                    background = MiuiDarkBackground,
                    surface = MiuiDarkSurface,
                    surfaceVariant = MiuiDarkSurfaceVariant,
                    surfaceContainer = MiuiDarkSurface,
                    surfaceContainerLow = MiuiDarkBackground,
                    surfaceContainerHigh = MiuiDarkSurfaceVariant,
                    outline = MiuiDarkOutline
                )
            } else {
                val dynamic = dynamicLightColorScheme(context)
                dynamic.copy(
                    background = MiuiLightBackground,
                    surface = MiuiLightSurface, // Pure crisp white for cards
                    surfaceVariant = MiuiLightSurfaceVariant,
                    surfaceContainer = MiuiLightSurface,
                    surfaceContainerLow = MiuiLightBackground,
                    surfaceContainerHigh = MiuiLightSurfaceVariant,
                    outline = MiuiLightOutline
                )
            }
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalIsDarkTheme provides isDark
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
