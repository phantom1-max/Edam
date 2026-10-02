package com.example.ui.theme

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.R

enum class EdamThemeMode(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    val previewBg: Color,
    val previewSurface: Color,
    val previewAccent: Color,
    val previewText: Color,
    val previewBorder: Color
) {
    SYSTEM_DEFAULT(
        titleRes = R.string.theme_system_default,
        descriptionRes = R.string.theme_system_default_desc,
        previewBg = Color(0xFFEDE9E4),
        previewSurface = Color(0xFF1F2937),
        previewAccent = Color(0xFFF59E0B),
        previewText = Color(0xFF1F2937),
        previewBorder = Color(0xFF9CA3AF)
    ),
    LIGHT(
        titleRes = R.string.theme_light,
        descriptionRes = R.string.theme_light_desc,
        previewBg = Color(0xFFEDE9E4),
        previewSurface = Color(0xFFF7F5F0),
        previewAccent = Color(0xFFF59E0B),
        previewText = Color(0xFF1F2937),
        previewBorder = Color(0xFFD6D1C7)
    ),
    EXTRA_LIGHT(
        titleRes = R.string.theme_extra_light,
        descriptionRes = R.string.theme_extra_light_desc,
        previewBg = Color(0xFFFAF8F5),
        previewSurface = Color(0xFFFFFFFF),
        previewAccent = Color(0xFFD97706),
        previewText = Color(0xFF1F2937),
        previewBorder = Color(0xFFE5E0D8)
    ),
    DARK(
        titleRes = R.string.theme_dark,
        descriptionRes = R.string.theme_dark_desc,
        previewBg = Color(0xFF111827),
        previewSurface = Color(0xFF1F2937),
        previewAccent = Color(0xFFF59E0B),
        previewText = Color(0xFFEDE9E4),
        previewBorder = Color(0xFF374151)
    ),
    EXTRA_DARK(
        titleRes = R.string.theme_extra_dark,
        descriptionRes = R.string.theme_extra_dark_desc,
        previewBg = Color(0xFF090D16),
        previewSurface = Color(0xFF151C28),
        previewAccent = Color(0xFFFBBF24),
        previewText = Color(0xFFEDE9E4),
        previewBorder = Color(0xFF283244)
    ),
    HIGH_CONTRAST_LIGHT(
        titleRes = R.string.theme_high_contrast_light,
        descriptionRes = R.string.theme_high_contrast_light_desc,
        previewBg = Color(0xFFFFFFFF),
        previewSurface = Color(0xFFFFFFFF),
        previewAccent = Color(0xFFB45309),
        previewText = Color(0xFF000000),
        previewBorder = Color(0xFF000000)
    ),
    HIGH_CONTRAST_BLACK(
        titleRes = R.string.theme_high_contrast_black,
        descriptionRes = R.string.theme_high_contrast_black_desc,
        previewBg = Color(0xFF000000),
        previewSurface = Color(0xFF000000),
        previewAccent = Color(0xFFFBBF24),
        previewText = Color(0xFFFFFFFF),
        previewBorder = Color(0xFFFFFFFF)
    );

    companion object {
        fun fromName(name: String?): EdamThemeMode {
            return entries.find { it.name == name } ?: SYSTEM_DEFAULT
        }
    }
}

@Immutable
data class EdamThemeSpec(
    val mode: EdamThemeMode = EdamThemeMode.SYSTEM_DEFAULT,
    val isDark: Boolean = false,
    val isHighContrast: Boolean = false,
    val borderWidth: Dp = 1.dp,
    val radialGlowAlpha: Float = 0.12f
)

val LocalEdamThemeSpec = staticCompositionLocalOf { EdamThemeSpec() }

// 1. Official Edam Warm Ivory Light (#EDE9E4 warm ivory background, #1F2937 deep charcoal text, #F59E0B amber-orange sprout accent)
private val EdamLightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF1F2937),
    secondary = Color(0xFF1F2937),
    onSecondary = Color(0xFFEDE9E4),
    secondaryContainer = Color(0xFFE5E0D8),
    onSecondaryContainer = Color(0xFF1F2937),
    tertiary = Color(0xFF059669),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    background = Color(0xFFEDE9E4),
    onBackground = Color(0xFF1F2937),
    surface = Color(0xFFF7F5F0),
    onSurface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFFDFBF9),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFF9CA3AF),
    outlineVariant = Color(0xFFD6D1C7)
)

// 2. Extra Light (Crisp Alabaster Ivory with Amber-Orange & Charcoal accents)
private val EdamExtraLightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF1F2937),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF3F4F6),
    onSecondaryContainer = Color(0xFF1F2937),
    tertiary = Color(0xFF15803D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDCFCE7),
    onTertiaryContainer = Color(0xFF14532D),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    background = Color(0xFFFAF8F5),
    onBackground = Color(0xFF1F2937),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFF5F2EC),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E0D8)
)

// 3. Official Edam Charcoal-Navy Dark (#111827 / #1F2937 deep charcoal, #EDE9E4 warm ivory text, #F59E0B amber-orange sprout accent)
private val EdamDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF59E0B),
    onPrimary = Color(0xFF111827),
    primaryContainer = Color(0xFF3B2A14),
    onPrimaryContainer = Color(0xFFFDE68A),
    secondary = Color(0xFFEDE9E4),
    onSecondary = Color(0xFF1F2937),
    secondaryContainer = Color(0xFF283446),
    onSecondaryContainer = Color(0xFFEDE9E4),
    tertiary = Color(0xFF10B981),
    onTertiary = Color(0xFF002D1E),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),
    background = Color(0xFF111827),
    onBackground = Color(0xFFEDE9E4),
    surface = Color(0xFF1F2937),
    onSurface = Color(0xFFEDE9E4),
    surfaceVariant = Color(0xFF192230),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF374151),
    outlineVariant = Color(0xFF2C3647)
)

// 4. Extra Dark (Midnight Obsidian with Warm Ivory & Amber-Orange)
private val EdamExtraDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFBBF24),
    onPrimary = Color(0xFF111827),
    primaryContainer = Color(0xFF33240F),
    onPrimaryContainer = Color(0xFFFEF08A),
    secondary = Color(0xFFEDE9E4),
    onSecondary = Color(0xFF111827),
    secondaryContainer = Color(0xFF1F2937),
    onSecondaryContainer = Color(0xFFEDE9E4),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF00291B),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),
    background = Color(0xFF090D16),
    onBackground = Color(0xFFEDE9E4),
    surface = Color(0xFF141B26),
    onSurface = Color(0xFFEDE9E4),
    surfaceVariant = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF283244),
    outlineVariant = Color(0xFF1F2937)
)

// 5. High Contrast Light (Pure white, ink black text, bold ink borders)
private val EdamHighContrastLightColorScheme = lightColorScheme(
    primary = Color(0xFF0033CC),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E2FF),
    onPrimaryContainer = Color(0xFF001966),
    secondary = Color(0xFF6A00B8),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF2D9FF),
    onSecondaryContainer = Color(0xFF35005C),
    tertiary = Color(0xFF00611C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC2F5CE),
    onTertiaryContainer = Color(0xFF00330D),
    error = Color(0xFFB80000),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFD6D6),
    onErrorContainer = Color(0xFF5C0000),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFF5F5F7),
    onSurfaceVariant = Color(0xFF141414),
    outline = Color(0xFF000000),
    outlineVariant = Color(0xFF000000)
)

// 6. High Contrast Black (Pure black, crisp white borders, vivid accessible accents)
private val EdamHighContrastBlackColorScheme = darkColorScheme(
    primary = Color(0xFF5AC8FA),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF003B5C),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFFFD60A),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF4D3E00),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF32D74B),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF004712),
    onTertiaryContainer = Color(0xFFFFFFFF),
    error = Color(0xFFFF453A),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF5C0000),
    onErrorContainer = Color(0xFFFFFFFF),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF0A0A0A),
    onSurfaceVariant = Color(0xFFEBEBF0),
    outline = Color(0xFFFFFFFF),
    outlineVariant = Color(0xFFFFFFFF)
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
    themeMode: EdamThemeMode = EdamThemeMode.SYSTEM_DEFAULT,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()

    val resolvedMode = when (themeMode) {
        EdamThemeMode.SYSTEM_DEFAULT -> if (systemDark) EdamThemeMode.DARK else EdamThemeMode.LIGHT
        else -> themeMode
    }

    val colorScheme: ColorScheme = when (resolvedMode) {
        EdamThemeMode.LIGHT -> EdamLightColorScheme
        EdamThemeMode.EXTRA_LIGHT -> EdamExtraLightColorScheme
        EdamThemeMode.DARK -> EdamDarkColorScheme
        EdamThemeMode.EXTRA_DARK -> EdamExtraDarkColorScheme
        EdamThemeMode.HIGH_CONTRAST_LIGHT -> EdamHighContrastLightColorScheme
        EdamThemeMode.HIGH_CONTRAST_BLACK -> EdamHighContrastBlackColorScheme
        EdamThemeMode.SYSTEM_DEFAULT -> EdamLightColorScheme
    }

    val isDark = resolvedMode == EdamThemeMode.DARK ||
        resolvedMode == EdamThemeMode.EXTRA_DARK ||
        resolvedMode == EdamThemeMode.HIGH_CONTRAST_BLACK

    val isHighContrast = resolvedMode == EdamThemeMode.HIGH_CONTRAST_LIGHT ||
        resolvedMode == EdamThemeMode.HIGH_CONTRAST_BLACK

    val spec = EdamThemeSpec(
        mode = themeMode,
        isDark = isDark,
        isHighContrast = isHighContrast,
        borderWidth = if (isHighContrast) 1.75.dp else 1.dp,
        radialGlowAlpha = when (resolvedMode) {
            EdamThemeMode.HIGH_CONTRAST_LIGHT,
            EdamThemeMode.HIGH_CONTRAST_BLACK -> 0f
            EdamThemeMode.EXTRA_LIGHT,
            EdamThemeMode.EXTRA_DARK -> 0.06f
            else -> 0.12f
        }
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalEdamThemeSpec provides spec) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = EdamTypography,
            shapes = EdamShapes,
            content = content
        )
    }
}
