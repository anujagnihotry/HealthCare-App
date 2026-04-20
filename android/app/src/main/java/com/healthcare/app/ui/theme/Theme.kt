package com.healthcare.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Core palette (navy + warm neutrals + coral / rose accents) ─────────────

val Navy950 = Color(0xFF0B1220)
val Navy900 = Color(0xFF121A2B)
val Navy800 = Color(0xFF1E293B)
val Navy700 = Color(0xFF334155)
val Navy100 = Color(0xFFE8ECF5)
val Navy50 = Color(0xFFF4F6FA)

val Coral = Color(0xFFFF7A45)
val Rose = Color(0xFFFF6B9D)
val PeachMist = Color(0xFFFFF3EE)
val WarmBackground = Color(0xFFF7F5F2)
val WarmSurface = Color(0xFFFFFEFE)

val Red500 = Color(0xFFE85D5D)
val Red100 = Color(0xFFFFE4E4)

val White = Color(0xFFFFFFFF)
val Gray50 = Color(0xFFFAFAFA)
val Gray100 = Color(0xFFF0EEEB)
val Gray300 = Color(0xFFD4D2CE)
val Gray600 = Color(0xFF64748B)
val Gray900 = Color(0xFF0F172A)

/** Horizontal accent (coral → rose), for selected chips / highlights */
fun accentHorizontalBrush(): Brush = Brush.linearGradient(
    colors = listOf(Coral, Rose),
    start = Offset(0f, 0f),
    end = Offset(400f, 120f),
)

/** Soft hero background (auth / landing) */
fun authBackgroundBrush(): Brush = Brush.verticalGradient(
    colors = listOf(WarmBackground, PeachMist, Color(0xFFFFF8F5)),
    startY = 0f,
    endY = 1800f,
)

/** Deep navy glossy hero (splash + login) */
fun splashBackgroundBrush(): Brush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0A1628),
        Color(0xFF0F2744),
        Color(0xFF152A52),
        Color(0xFF0B1220),
    ),
    startY = 0f,
    endY = 1600f,
)

// Legacy names kept for a few call sites; values match the modern palette
val Blue700 = Navy700
val Blue800 = Navy900
val Blue100 = Navy100
val Green500 = Navy800
val Green100 = Navy50

private val LightColorScheme = lightColorScheme(
    primary = Navy900,
    onPrimary = White,
    primaryContainer = Navy100,
    onPrimaryContainer = Navy900,
    secondary = Coral,
    onSecondary = White,
    secondaryContainer = PeachMist,
    onSecondaryContainer = Color(0xFF9A3412),
    tertiary = Rose,
    onTertiary = White,
    tertiaryContainer = Color(0xFFFFE4EC),
    onTertiaryContainer = Color(0xFF9D174D),
    error = Red500,
    onError = White,
    errorContainer = Red100,
    onErrorContainer = Color(0xFF7F1D1D),
    background = WarmBackground,
    onBackground = Gray900,
    surface = WarmSurface,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    outline = Gray300,
    outlineVariant = Color(0xFFE2E0DC),
)

private val DarkColorScheme = darkColorScheme(
    primary = Navy100,
    onPrimary = Navy950,
    primaryContainer = Navy800,
    onPrimaryContainer = Navy100,
    secondary = Coral,
    onSecondary = Navy950,
    secondaryContainer = Color(0xFF5C2E24),
    onSecondaryContainer = PeachMist,
    tertiary = Rose,
    onTertiary = Navy950,
    tertiaryContainer = Color(0xFF5C2440),
    onTertiaryContainer = Color(0xFFFFD6E8),
    error = Red100,
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Red100,
    background = Navy950,
    onBackground = Navy100,
    surface = Navy900,
    onSurface = Navy100,
    surfaceVariant = Navy800,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Navy700,
    outlineVariant = Color(0xFF334155),
)

@Composable
fun HealthCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HealthCareTypography,
        content = content,
    )
}
