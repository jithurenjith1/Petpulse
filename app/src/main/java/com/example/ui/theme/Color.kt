package com.petpulse.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// Petpulse Brand Palette — "Coral & Teal"
// Designed to stay readable in bright daylight (high contrast)
// and easy on the eyes at night (soft warm background).
// ============================================================

// ---------- Core brand colors ----------
val CoralPrimary = Color(0xFFBC5233)   // main buttons — 4.8:1 contrast with white text
val CoralDark = Color(0xFF9C4227)      // pressed states, links
val CoralMid = Color(0xFFD97F5C)       // warm mid accent
val CoralLight = Color(0xFFF6DFD4)     // chips, tints, subtle fills
val TealAccent = Color(0xFF1D7A6E)     // secondary actions — 5.2:1 with white text
val TealLight = Color(0xFFD6EBE6)      // teal tint
val TealDeep = Color(0xFF0F3D36)       // text on teal tints
val CreamBg = Color(0xFFFBF6F0)        // soft warm background (not blinding at night)
val SurfaceWhite = Color(0xFFFFFFFF)
val DarkText = Color(0xFF272220)        // 14.6:1 on cream — sharp in daylight
val TextGray = Color(0xFF5C554F)        // secondary text — 6.8:1 on cream
val BorderColor = Color(0xFFE9DED4)
val SosRed = Color(0xFFD62828)          // emergency / errors — 5.0:1 with white
val AmberGold = Color(0xFFA87A1F)       // gold accent, readable on white
val SuccessGreen = Color(0xFF2E7D32)

// ---------- Legacy purple names (kept so existing code compiles) ----------
// The app used to be purple-themed; these now point to the brand palette.
val PurplePrimary = CoralPrimary
val PurplePrimaryLight = CoralMid
val PurplePrimaryDark = CoralDark
val PurpleSecondary = TealAccent
val PurpleTertiary = CoralPrimary
val PurpleBackgroundLight = CreamBg
val PurpleSurfaceLight = SurfaceWhite
val PurpleSurfaceVariantLight = Color(0xFFECE4DB)
val PurpleCardLight = SurfaceWhite
val PurplePrimaryDarkTheme = Color(0xFFE8917A)
val PurpleSecondaryDarkTheme = Color(0xFF6FC7B9)
val PurpleTertiaryDarkTheme = Color(0xFFE8917A)
val PurpleBackgroundDark = Color(0xFF17130F)
val PurpleSurfaceDark = Color(0xFF272019)
val PurpleSurfaceVariantDark = Color(0xFF3A2F26)

// ---------- Accent aliases ----------
val AccentAmber = AmberGold
val AccentGreen = TealAccent
val AccentRed = SosRed
val AccentPurple = CoralPrimary

// ---------- Legacy text colors ----------
val TextPrimaryLight = DarkText
val TextSecondaryLight = TextGray
val TextPrimaryDark = Color(0xFFF5EFE7)
val TextSecondaryDark = Color(0xFFB5AAA0)

// ---------- Legacy blue aliases ----------
val BluePrimary = PurplePrimary
val BluePrimaryLight = PurplePrimaryLight
val BluePrimaryDark = PurplePrimaryDark
val BlueSecondary = PurpleSecondary
val BlueTertiary = PurpleTertiary
val BlueBackgroundLight = PurpleBackgroundLight
val BlueSurfaceLight = PurpleSurfaceLight
val BlueSurfaceVariantLight = PurpleSurfaceVariantLight
val BlueCardLight = PurpleCardLight
val BluePrimaryDarkTheme = PurplePrimaryDarkTheme
val BlueSecondaryDarkTheme = PurpleSecondaryDarkTheme
val BlueTertiaryDarkTheme = PurpleTertiaryDarkTheme
val BlueBackgroundDark = PurpleBackgroundDark
val BlueSurfaceDark = PurpleSurfaceDark
val BlueSurfaceVariantDark = PurpleSurfaceVariantDark
