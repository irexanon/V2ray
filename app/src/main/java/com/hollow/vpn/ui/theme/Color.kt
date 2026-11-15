package com.hollow.vpn.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Expressive Material 3 fallback tonal palettes (used on Android < 12; Android 12+ uses dynamicColorScheme)
val ExpressiveLightColorScheme = lightColorScheme(
    primary = Color(0xFF3955C8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDEE1FF),
    onPrimaryContainer = Color(0xFF001159),
    secondary = Color(0xFF565E79),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDBE2FF),
    onSecondaryContainer = Color(0xFF131B32),
    tertiary = Color(0xFF006B5E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF9DF2E1),
    onTertiaryContainer = Color(0xFF00201B),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF1A1B23),
    surface = Color(0xFFFBF8FF),
    onSurface = Color(0xFF1A1B23),
    surfaceVariant = Color(0xFFE3E1EC),
    onSurfaceVariant = Color(0xFF454652),
    outline = Color(0xFF767683),
    outlineVariant = Color(0xFFC6C5D3),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4F3FC),
    surfaceContainer = Color(0xFFEEEDF6),
    surfaceContainerHigh = Color(0xFFE8E7F0),
    surfaceContainerHighest = Color(0xFFE2E1EB)
)

val ExpressiveDarkColorScheme = darkColorScheme(
    primary = Color(0xFFBAC3FF),
    onPrimary = Color(0xFF0B258A),
    primaryContainer = Color(0xFF213CA9),
    onPrimaryContainer = Color(0xFFDEE1FF),
    secondary = Color(0xFFBEC5E5),
    onSecondary = Color(0xFF283048),
    secondaryContainer = Color(0xFF3E4660),
    onSecondaryContainer = Color(0xFFDBE2FF),
    tertiary = Color(0xFF81D5C5),
    onTertiary = Color(0xFF003730),
    tertiaryContainer = Color(0xFF005046),
    onTertiaryContainer = Color(0xFF9DF2E1),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF11131A),
    onBackground = Color(0xFFE3E1EC),
    surface = Color(0xFF11131A),
    onSurface = Color(0xFFE3E1EC),
    surfaceVariant = Color(0xFF454652),
    onSurfaceVariant = Color(0xFFC6C5D3),
    outline = Color(0xFF90909D),
    outlineVariant = Color(0xFF454652),
    surfaceContainerLowest = Color(0xFF0C0E15),
    surfaceContainerLow = Color(0xFF1A1B23),
    surfaceContainer = Color(0xFF1E1F27),
    surfaceContainerHigh = Color(0xFF282932),
    surfaceContainerHighest = Color(0xFF33343D)
)

// Semantic signal quality accents harmonized for Material 3 light & dark readability
val SignalStrongLight = Color(0xFF0E7C5D)
val SignalMediumLight = Color(0xFFB45309)
val SignalWeakLight = Color(0xFFBA1A1A)

val SignalStrongDark = Color(0xFF6EE7B7)
val SignalMediumDark = Color(0xFFFBBF24)
val SignalWeakDark = Color(0xFFFF897D)
