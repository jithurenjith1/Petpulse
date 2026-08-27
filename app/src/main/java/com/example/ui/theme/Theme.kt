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
    primary = BluePrimaryDarkTheme,
    onPrimary = Color(0xFF5C1A00),
    primaryContainer = BluePrimaryDark,
    onPrimaryContainer = Color(0xFFFFDBCC),
    secondary = BlueSecondaryDarkTheme,
    onSecondary = Color(0xFF003830),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Color(0xFFB0F5EC),
    tertiary = BlueTertiaryDarkTheme,
    background = BlueBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = BlueSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = BlueSurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCC),
    onPrimaryContainer = Color(0xFF5C1A00),
    secondary = BlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB0F5EC),
    onSecondaryContainer = Color(0xFF003830),
    tertiary = BlueTertiary,
    background = BlueBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = BlueSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = BlueSurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
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
