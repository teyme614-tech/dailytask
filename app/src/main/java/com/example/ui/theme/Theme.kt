package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryLight,
    onPrimary = Color.Black,
    primaryContainer = TealPrimaryDark,
    onPrimaryContainer = TealContainer,
    secondary = SlateSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = SlateSecondary,
    onSecondaryContainer = Color.White,
    tertiary = AmberTertiary,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = OnTealContainer,
    secondary = SlateSecondary,
    onSecondary = Color.White,
    secondaryContainer = SlateContainer,
    onSecondaryContainer = SlateSecondary,
    tertiary = AmberTertiary,
    background = NeutralLightBg,
    surface = NeutralSurface,
    surfaceVariant = NeutralSurfaceVariant,
    onBackground = NeutralTextPrimary,
    onSurface = NeutralTextPrimary,
    onSurfaceVariant = NeutralTextSecondary,
    outline = NeutralBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional brand colors for productivity & calendar
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
