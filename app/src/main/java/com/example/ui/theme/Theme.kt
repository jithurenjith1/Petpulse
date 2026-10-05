package com.petpulse.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// Wagmiya Material3 theme — light + dark schemes.
// Both schemes share the SAME magenta identity so the app looks
// coherent whichever mode the user picks.
// ============================================================

private val WagmiyaLightColors = lightColorScheme(
    primary = MagentaPrimaryLight,
    onPrimary = MagentaOnPrimaryLight,
    primaryContainer = MagentaPrimaryContainerLight,
    onPrimaryContainer = MagentaOnBackgroundLight,
    secondary = MagentaSecondaryLight,
    onSecondary = Color.White,
    tertiary = MagentaTertiaryLight,
    onTertiary = Color.White,
    background = MagentaBackgroundLight,
    onBackground = MagentaOnBackgroundLight,
    surface = MagentaSurfaceLight,
    onSurface = MagentaOnBackgroundLight,
    surfaceVariant = MagentaSurfaceVariantLight,
    onSurfaceVariant = MagentaOnSurfaceVariantLight,
    outline = MagentaOutlineLight,
    error = MagentaErrorLight,
    onError = Color.White,
)

private val WagmiyaDarkColors = darkColorScheme(
    primary = MagentaPrimaryDark,
    onPrimary = MagentaOnPrimaryDark,
    primaryContainer = MagentaPrimaryContainerDark,
    onPrimaryContainer = MagentaOnBackgroundDark,
    secondary = MagentaSecondaryDark,
    onSecondary = Color.White,
    tertiary = MagentaTertiaryDark,
    onTertiary = Color.White,
    background = MagentaBackgroundDark,
    onBackground = MagentaOnBackgroundDark,
    surface = MagentaSurfaceDark,
    onSurface = MagentaOnBackgroundDark,
    surfaceVariant = MagentaSurfaceVariantDark,
    onSurfaceVariant = MagentaOnSurfaceVariantDark,
    outline = MagentaOutlineDark,
    error = MagentaErrorDark,
    onError = Color.White,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // dynamicColor kept for API compatibility but the Wagmiya palette is
    // always used so the magenta brand identity is preserved.
    val colorScheme = if (darkTheme) WagmiyaDarkColors else WagmiyaLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
