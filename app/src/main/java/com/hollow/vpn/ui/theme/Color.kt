package com.hollow.vpn.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Hollow Editorial Design System Tokens
val HollowCream = Color(0xFFF7F4ED)
val HollowCreamSurface = Color(0xFFECE7DD)
val HollowCreamSurfaceHigh = Color(0xFFE3DDD1)
val HollowCreamDivider = Color(0xFFE2DDD3)

val HollowInk = Color(0xFF141413)
val HollowInkSecondary = Color(0xFF6E6A63)
val HollowInkMuted = Color(0xFF8C877E)

val HollowOrange = Color(0xFFFF4422)
val HollowOrangeContainer = Color(0xFFFFE5DF)

// Page 2: Connecting (Obsidian + Amber)
val HollowObsidian = Color(0xFF111110)
val HollowObsidianSurface = Color(0xFF1C1B19)
val HollowObsidianDivider = Color(0xFF262523)
val HollowObsidianMuted = Color(0xFF7A766F)
val HollowAmber = Color(0xFFF59E0B)

// Page 3: Connected (Forest Green + Lime)
val HollowForest = Color(0xFF0C2E1E)
val HollowForestCard = Color(0xFF16422D)
val HollowForestDivider = Color(0xFF1D4B34)
val HollowForestMuted = Color(0xFF8BB09C)
val HollowLime = Color(0xFFAEF03B)
val HollowLimeBarMuted = Color(0xFF426E47)

val ExpressiveLightColorScheme = lightColorScheme(
    primary = HollowInk,
    onPrimary = HollowCream,
    primaryContainer = HollowCreamSurface,
    onPrimaryContainer = HollowInk,
    secondary = HollowOrange,
    onSecondary = Color.White,
    secondaryContainer = HollowCreamSurface,
    onSecondaryContainer = HollowInk,
    tertiary = HollowForest,
    onTertiary = HollowCream,
    tertiaryContainer = Color(0xFFD8EFE0),
    onTertiaryContainer = HollowForest,
    error = HollowOrange,
    onError = Color.White,
    errorContainer = HollowOrangeContainer,
    onErrorContainer = Color(0xFF7A1705),
    background = HollowCream,
    onBackground = HollowInk,
    surface = HollowCream,
    onSurface = HollowInk,
    surfaceVariant = HollowCreamSurface,
    onSurfaceVariant = HollowInkSecondary,
    outline = Color(0xFFBDB7AC),
    outlineVariant = HollowCreamDivider,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = HollowCream,
    surfaceContainer = HollowCreamSurface,
    surfaceContainerHigh = HollowCreamSurfaceHigh,
    surfaceContainerHighest = Color(0xFFDAD3C5),
    inverseSurface = HollowInk,
    inverseOnSurface = HollowCream
)

val ExpressiveDarkColorScheme = darkColorScheme(
    primary = HollowCream,
    onPrimary = HollowInk,
    primaryContainer = HollowObsidianSurface,
    onPrimaryContainer = HollowCream,
    secondary = HollowOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF282724),
    onSecondaryContainer = HollowCream,
    tertiary = HollowLime,
    onTertiary = HollowInk,
    tertiaryContainer = HollowForestCard,
    onTertiaryContainer = HollowCream,
    error = Color(0xFFFF6B4A),
    onError = HollowInk,
    errorContainer = Color(0xFF54180B),
    onErrorContainer = Color(0xFFFFD8CF),
    background = HollowObsidian,
    onBackground = HollowCream,
    surface = HollowObsidian,
    onSurface = HollowCream,
    surfaceVariant = HollowObsidianSurface,
    onSurfaceVariant = HollowObsidianMuted,
    outline = Color(0xFF494741),
    outlineVariant = HollowObsidianDivider,
    surfaceContainerLowest = Color(0xFF0B0B0A),
    surfaceContainerLow = HollowObsidian,
    surfaceContainer = HollowObsidianSurface,
    surfaceContainerHigh = Color(0xFF252421),
    surfaceContainerHighest = Color(0xFF31302C),
    inverseSurface = HollowCream,
    inverseOnSurface = HollowInk
)

// Semantic signal quality accents
val SignalStrongLight = Color(0xFF15803D)
val SignalMediumLight = Color(0xFFB45309)
val SignalWeakLight = Color(0xFFFF4422)

val SignalStrongDark = Color(0xFFAEF03B)
val SignalMediumDark = Color(0xFFF59E0B)
val SignalWeakDark = Color(0xFFFF6B4A)
