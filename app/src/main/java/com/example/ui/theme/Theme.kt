package com.petpulse.app.ui.theme

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

// One consistent Petpulse look, day and night:
// warm cream background, coral primary, teal secondary, near-black text.
// (Same scheme is used whether the system is in light or dark mode, so the
// app always looks coherent. A true auto-dark variant can come later.)
private val PetpulseColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = CoralLight,
    onPrimaryContainer = CoralDark,
    secondary = TealAccent,
    onSecondary = Color.White,
    secondaryContainer = TealLight,
    onSecondaryContainer = TealDeep,
    tertiary = AmberGold,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3E7CC),
    onTertiaryContainer = Color(0xFF4A3710),
    background = CreamBg,
    onBackground = DarkText,
    surface = SurfaceWhite,
    onSurface = DarkText,
    surfaceVariant = Color(0xFFECE4DB),
    onSurfaceVariant = TextGray,
    outline = BorderColor,
    error = SosRed,
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
        else -> PetpulseColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
