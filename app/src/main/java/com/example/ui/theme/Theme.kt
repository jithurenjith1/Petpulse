package com.petpulse.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// Wagmiya Material3 theme — light + dark schemes.
// Both schemes share the SAME calm-plum identity (approved redesign)
// so the app looks coherent whichever mode the user picks.
//
// The approved palette is applied here through the colour tokens in
// Color.kt that the two schemes below reference:
//   primary            = #6D4C8A  muted plum (was magenta #C724B1)
//   primaryContainer   = #EFE9F5  primary tint (selected chip / quick tiles / nav highlight)
//   surfaceVariant     = #F7F4FA  light neutral header band (was pink #FDF2F8)
//   background         = #FDFCFB  warm off-white body (was cream #FFF8F0)
//   surface            = #FFFFFF  cards
//   onBackground/onSurface = #2B2530  text dark
//   onSurfaceVariant   = #8A8291  text muted
//   outline            = #D9CCE6  hairline borders
//   error              = #B0605A  muted brick (emergency outline, was bright red)
//   tertiary/TealAccent = #3E8E5A soft green (positive / reminder), tint #EAF3EE
// The dark scheme uses sensible equivalents (light plum #B79BD6 on a warm
// near-black #141118) so dark mode keeps working and stays readable.
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
