package com.example.echomuse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurpleLight,
    onPrimary = Color.White,
    primaryContainer = NeonPurple,
    onPrimaryContainer = Color.White,
    secondary = ElectricCyan,
    onSecondary = Color.Black,
    tertiary = DeepMagenta,
    background = DarkBackground,
    onBackground = OnDarkSurface,
    surface = DarkSurface,
    onSurface = OnDarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceMuted
)

private val LightColorScheme = lightColorScheme(
    primary = NeonPurple,
    onPrimary = Color.White,
    primaryContainer = NeonPurpleLight,
    secondary = ElectricCyan,
    tertiary = DeepMagenta,
    background = Color(0xFFF5F5FA),
    onBackground = Color(0xFF101020),
    surface = Color.White,
    onSurface = Color(0xFF101020),
    surfaceVariant = Color(0xFFE5E5F0),
    onSurfaceVariant = Color(0xFF505070)
)

@Composable
fun EchoMuseTheme(
    darkTheme: Boolean = true, // Default to sleek dark music theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
