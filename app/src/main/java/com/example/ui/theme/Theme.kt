package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EdamLightColorScheme = lightColorScheme(
    primary = Color(0xFF0071E3),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0x1A0071E3),
    onPrimaryContainer = Color(0xFF005BB5),
    secondary = Color(0xFFAF52DE),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0x1FAF52DE),
    onSecondaryContainer = Color(0xFF6B278C),
    tertiary = Color(0xFF16803C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0x2434C759),
    onTertiaryContainer = Color(0xFF0F5E2B),
    error = Color(0xFFFF3B30),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0x1FFF3B30),
    onErrorContainer = Color(0xFFB3261E),
    background = Color(0xFFF5F5F7),
    onBackground = Color(0xFF1D1D1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1D1D1F),
    surfaceVariant = Color(0xB8FFFFFF),
    onSurfaceVariant = Color(0xFF6E6E73),
    outline = Color(0x14000000),
    outlineVariant = Color(0xCCFFFFFF)
)

private val EdamTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 50.sp,
        letterSpacing = (-2.0).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-1.0).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 27.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

private val EdamShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun EdamTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EdamLightColorScheme,
        typography = EdamTypography,
        shapes = EdamShapes,
        content = content
    )
}
