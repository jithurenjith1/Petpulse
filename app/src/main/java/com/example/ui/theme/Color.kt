package com.petpulse.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// Wagmiya Brand Palette — "Magenta"
// Two coordinated schemes (light + dark) share the SAME identity:
// magenta primary, purple secondary, fuchsia tertiary.
// ============================================================

// ---------- Light scheme ----------
val MagentaPrimaryLight = Color(0xFFC724B1)          // magenta
val MagentaSecondaryLight = Color(0xFF7C4DFF)        // purple
val MagentaTertiaryLight = Color(0xFFE040FB)         // fuchsia
val MagentaBackgroundLight = Color(0xFFFFF8F0)       // cream
val MagentaSurfaceLight = Color(0xFFFFFFFF)
val MagentaSurfaceVariantLight = Color(0xFFF6EEF6)
val MagentaOnBackgroundLight = Color(0xFF2D2438)
val MagentaOnSurfaceVariantLight = Color(0xFF6E6478)
val MagentaOutlineLight = Color(0xFFE4DCEF)
val MagentaPrimaryContainerLight = Color(0xFFF3E1F2)
val MagentaOnPrimaryLight = Color(0xFFFFFFFF)
val MagentaErrorLight = Color(0xFFD32F2F)

// ---------- Dark scheme ----------
val MagentaPrimaryDark = Color(0xFFE040FB)           // brighter fuchsia reads better on dark
val MagentaSecondaryDark = Color(0xFF7C4DFF)
val MagentaTertiaryDark = Color(0xFFC724B1)
val MagentaBackgroundDark = Color(0xFF08060C)
val MagentaSurfaceDark = Color(0xFF1A1622)
val MagentaSurfaceVariantDark = Color(0xFF221C2E)
val MagentaOnBackgroundDark = Color(0xFFFFFFFF)
val MagentaOnSurfaceVariantDark = Color(0xFFA79FB5)
val MagentaOutlineDark = Color(0xFF2A2338)
val MagentaPrimaryContainerDark = Color(0xFF3A1140)
val MagentaOnPrimaryDark = Color(0xFF1A0020)
val MagentaErrorDark = Color(0xFFFF6B6B)

// ---------- Core brand colors (kept for existing call sites) ----------
val CoralPrimary = MagentaPrimaryLight     // main buttons / active tabs / links
val CoralDark = Color(0xFF8E1A7E)          // pressed states, links
val CoralMid = Color(0xFFE040FB)           // bright mid accent (fuchsia)
val CoralLight = MagentaPrimaryContainerLight // chips, tints, subtle fills

// ---------- Fixed semantic / accent colors (deliberately NOT themed) ----------
val TealAccent = Color(0xFF1D7A6E)         // brand teal accent — fixed
val TealLight = Color(0xFFD6EBE6)
val TealDeep = Color(0xFF0F3D36)
val SosRed = Color(0xFFD62828)             // emergency / errors — fixed
val AmberGold = Color(0xFFA87A1F)          // gold badge accent — fixed
val SuccessGreen = Color(0xFF2E7D32)       // verified / success green — fixed

// ---------- Neutrals (light scheme values) ----------
val CreamBg = MagentaBackgroundLight       // soft background
val SurfaceWhite = Color(0xFFFFFFFF)
val DarkText = MagentaOnBackgroundLight    // primary text on light
val TextGray = MagentaOnSurfaceVariantLight
val BorderColor = MagentaOutlineLight

// ---------- Legacy purple names (kept so existing code compiles) ----------
val PurplePrimary = CoralPrimary
val PurplePrimaryLight = CoralMid
val PurplePrimaryDark = CoralDark
val PurpleSecondary = TealAccent
val PurpleTertiary = CoralPrimary
val PurpleBackgroundLight = CreamBg
val PurpleSurfaceLight = SurfaceWhite
val PurpleSurfaceVariantLight = Color(0xFFF6EEF6)
val PurpleCardLight = SurfaceWhite
val PurplePrimaryDarkTheme = MagentaPrimaryDark
val PurpleSecondaryDarkTheme = MagentaSecondaryDark
val PurpleTertiaryDarkTheme = MagentaTertiaryDark
val PurpleBackgroundDark = MagentaBackgroundDark
val PurpleSurfaceDark = MagentaSurfaceDark
val PurpleSurfaceVariantDark = MagentaSurfaceVariantDark

// ---------- Accent aliases ----------
val AccentAmber = AmberGold
val AccentGreen = TealAccent
val AccentRed = SosRed
val AccentPurple = CoralPrimary

// ---------- Legacy text colors ----------
val TextPrimaryLight = DarkText
val TextSecondaryLight = TextGray
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0xFFA79FB5)

// ---------- Legacy blue aliases (now point at the magenta palette) ----------
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
