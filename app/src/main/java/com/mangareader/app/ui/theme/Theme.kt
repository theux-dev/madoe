package com.mangareader.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mangareader.app.data.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = Sage,
    onPrimaryContainer = Ink,
    secondary = InkSoft,
    onSecondary = Cream,
    secondaryContainer = Sage,
    onSecondaryContainer = Ink,
    tertiary = Orange,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = Sage,
    onSurfaceVariant = InkSoft,
    surfaceContainer = Cream,
    surfaceContainerHigh = Cream,
    outline = InkSoft,
    error = Brick,
)

private val DarkColors = darkColorScheme(
    primary = NightTeal,
    onPrimary = Color(0xFF00201E),
    primaryContainer = NightCard,
    onPrimaryContainer = NightText,
    secondary = Sage,
    onSecondary = Ink,
    secondaryContainer = NightCard,
    onSecondaryContainer = NightText,
    tertiary = Orange,
    background = NightBg,
    onBackground = NightText,
    surface = NightBg,
    onSurface = NightText,
    surfaceVariant = NightCard,
    onSurfaceVariant = Color(0xFFC9C6B5),
    surfaceContainer = NightBg,
    surfaceContainerHigh = NightCard,
    outline = Color(0xFF9A9886),
    error = Color(0xFFFFB4A9),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun MadoeTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
