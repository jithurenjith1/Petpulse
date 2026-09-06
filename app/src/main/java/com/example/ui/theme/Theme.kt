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
    primary = PurplePrimaryDarkTheme,
    onPrimary = Color(0xFF1A0F2E),
    primaryContainer = PurplePrimaryDark,
    onPrimaryContainer = Color(0xFFE0D0F0),
    secondary = PurpleSecondaryDarkTheme,
    onSecondary = Color(0xFF3A2E00),
    secondaryContainer = Color(0xFF524500),
    onSecondaryContainer = Color(0xFFF5E5A0),
    tertiary = PurpleTertiaryDarkTheme,
    background = PurpleBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = PurpleSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = PurpleSurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    error = AccentRed,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0D0F0),
    onPrimaryContainer = Color(0xFF1A0F2E),
    secondary = PurpleSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5E5A0),
    onSecondaryContainer = Color(0xFF3A2E00),
    tertiary = PurpleTertiary,
    background = PurpleBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = PurpleSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = PurpleSurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    error = AccentRed,
    onError = Color.White,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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

