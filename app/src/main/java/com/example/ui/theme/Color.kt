package com.petpulse.app.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// Wagmiya Brand Palette — "Calm Plum" (approved redesign)
// Two coordinated schemes (light + dark) share the SAME identity:
// muted plum primary, soft green accent, warm neutral surfaces.
// The loud magenta / gold / red fills are gone; only the SOS
// safety control keeps a (slightly muted) red.
// ============================================================

// ---------- Approved light tokens ----------
val WagmiyaPlum = Color(0xFF6D4C8A)          // primary muted plum / violet
val WagmiyaPlumTint = Color(0xFFEFE9F5)      // selected chip fill, quick tiles, nav highlight
val WagmiyaHairline = Color(0xFFD9CCE6)      // hairline borders
val WagmiyaHeaderBand = Color(0xFFF7F4FA)    // light neutral header band (was pink)
val WagmiyaBodyBg = Color(0xFFFDFCFB)        // warm off-white body background
val WagmiyaCard = Color(0xFFFFFFFF)          // cards
val WagmiyaTextDark = Color(0xFF2B2530)      // primary text
val WagmiyaTextMuted = Color(0xFF8A8291)     // muted text
val WagmiyaGreen = Color(0xFF3E8E5A)         // soft green (positive / reminder)
val WagmiyaGreenTint = Color(0xFFEAF3EE)     // green tint
val WagmiyaBrick = Color(0xFFB0605A)         // emergency outline (muted brick)
val WagmiyaSosRed = Color(0xFFC0392B)        // SOS safety control (muted red, stays obvious)

// ---------- Light scheme ----------
val MagentaPrimaryLight = WagmiyaPlum                 // muted plum
val MagentaSecondaryLight = Color(0xFF8B6BA8)         // muted plum, lighter
val MagentaTertiaryLight = WagmiyaGreen               // soft green accent
val MagentaBackgroundLight = WagmiyaBodyBg            // warm off-white
val MagentaSurfaceLight = WagmiyaCard                 // cards
val MagentaSurfaceVariantLight = WagmiyaHeaderBand    // light neutral band
val MagentaOnBackgroundLight = WagmiyaTextDark
val MagentaOnSurfaceVariantLight = WagmiyaTextMuted
val MagentaOutlineLight = WagmiyaHairline
val MagentaPrimaryContainerLight = WagmiyaPlumTint    // primary tint
val MagentaOnPrimaryLight = Color(0xFFFFFFFF)
val MagentaErrorLight = WagmiyaBrick                  // muted brick (was bright red)

// ---------- Dark scheme ----------
val MagentaPrimaryDark = Color(0xFFB79BD6)            // light plum reads on dark
val MagentaSecondaryDark = Color(0xFFA98FC4)
val MagentaTertiaryDark = Color(0xFF7FC79A)           // soft green (dark)
val MagentaBackgroundDark = Color(0xFF141118)         // warm near-black
val MagentaSurfaceDark = Color(0xFF1D1925)
val MagentaSurfaceVariantDark = Color(0xFF262130)
val MagentaOnBackgroundDark = Color(0xFFECE7F0)
val MagentaOnSurfaceVariantDark = Color(0xFFB3AABF)
val MagentaOutlineDark = Color(0xFF4A4055)
val MagentaPrimaryContainerDark = Color(0xFF3A2C4D)   // dark plum tint
val MagentaOnPrimaryDark = Color(0xFF2A1A3A)
val MagentaErrorDark = Color(0xFFD99B96)              // muted brick (dark)

// ---------- Core brand colors (kept for existing call sites) ----------
val CoralPrimary = MagentaPrimaryLight     // main buttons / active tabs / links
val CoralDark = Color(0xFF4E3563)          // pressed states, links
val CoralMid = Color(0xFF9B7BB8)           // bright mid accent (muted plum)
val CoralLight = MagentaPrimaryContainerLight // chips, tints, subtle fills

// ---------- Fixed semantic / accent colors (deliberately NOT themed) ----------
val TealAccent = WagmiyaGreen              // positive / success green — fixed
val TealLight = WagmiyaGreenTint
val TealDeep = Color(0xFF0F3D36)
val SosRed = WagmiyaSosRed                 // emergency / SOS — fixed, muted red
val AmberGold = Color(0xFFA87A1F)          // gold badge accent — fixed
val SuccessGreen = WagmiyaGreen            // verified / success green — fixed

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
val PurpleSurfaceVariantLight = WagmiyaHeaderBand
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
val TextSecondaryDark = Color(0xFFB3AABF)

// ---------- Legacy blue aliases (now point at the plum palette) ----------
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
