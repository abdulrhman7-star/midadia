package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoContainerLight,
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = EmeraldAccent,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainerLight,
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = AmberAccent,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainerLight,
    onTertiaryContainer = Color(0xFF451A03),
    error = RoseAction,
    onError = Color.White,
    errorContainer = RoseContainerLight,
    onErrorContainer = Color(0xFF4C0519),
    background = SlateBgLight,
    onBackground = SlateDarkNavy,
    surface = Color.White,
    onSurface = SlateDarkNavy,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SlateMutedLight,
    outline = SlateBorderLight,
    outlineVariant = Color(0xFFCBD5E1)
)

private val DarkColors = darkColorScheme(
    primary = IndigoPrimaryDark,
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = IndigoContainerDark,
    onPrimaryContainer = IndigoContainerLight,
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF022C22),
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = EmeraldContainerLight,
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = AmberContainerLight,
    error = Color(0xFFFB7185),
    onError = Color(0xFF4C0519),
    errorContainer = RoseContainerDark,
    onErrorContainer = RoseContainerLight,
    background = SlateBgDark,
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = SlateSurfaceDark,
    onSurfaceVariant = SlateMutedDark,
    outline = SlateBorderDark,
    outlineVariant = Color(0xFF475569)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MidaddTheme(
    darkTheme: Boolean,
    isArabic: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = buildAppTypography(isArabic),
        shapes = AppShapes,
        content = content
    )
}
