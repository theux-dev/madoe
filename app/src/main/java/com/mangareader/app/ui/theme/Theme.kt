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
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.mangareader.app.data.model.AppPalette

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

/** As cores dinâmicas (Material You) existem a partir do Android 12. */
fun isDynamicColorAvailable(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

private fun PaletteSeed.toColorScheme(dark: Boolean): ColorScheme {
    val primary = Color(primary)
    val onPrimary = Color(onPrimary)
    val background = Color(background)
    val onBackground = Color(onBackground)
    val card = Color(card)
    val onCard = Color(onCard)
    val outline = Color(outline)
    return if (dark) {
        darkColorScheme(
            primary = primary, onPrimary = onPrimary,
            primaryContainer = card, onPrimaryContainer = onBackground,
            secondary = onCard, onSecondary = background,
            secondaryContainer = card, onSecondaryContainer = onBackground,
            tertiary = Orange,
            background = background, onBackground = onBackground,
            surface = background, onSurface = onBackground,
            surfaceVariant = card, onSurfaceVariant = onCard,
            surfaceContainer = background, surfaceContainerHigh = card,
            outline = outline, error = Color(0xFFFFB4A9),
        )
    } else {
        lightColorScheme(
            primary = primary, onPrimary = onPrimary,
            primaryContainer = card, onPrimaryContainer = onBackground,
            secondary = onCard, onSecondary = background,
            secondaryContainer = card, onSecondaryContainer = onBackground,
            tertiary = Orange,
            background = background, onBackground = onBackground,
            surface = background, onSurface = onBackground,
            surfaceVariant = card, onSurfaceVariant = onCard,
            surfaceContainer = background, surfaceContainerHigh = background,
            outline = outline, error = Brick,
        )
    }
}

@Composable
fun MadoeTheme(darkTheme: Boolean, palette: AppPalette = AppPalette.MADOE, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = remember(palette, darkTheme) {
        val fallback = if (darkTheme) DarkColors else LightColors
        when {
            palette == AppPalette.SYSTEM && isDynamicColorAvailable() ->
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            palette == AppPalette.MADOE || palette == AppPalette.SYSTEM -> fallback
            else -> Palettes.seed(palette, darkTheme)?.toColorScheme(darkTheme) ?: fallback
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
