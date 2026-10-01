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
        previewBg = Color(0xFFF5F5F7),
        previewSurface = Color(0xFF1A1D26),
        previewAccent = Color(0xFF0071E3),
        previewText = Color(0xFF1D1D1F),
        previewBorder = Color(0xFF6E6E73)
    ),
    LIGHT(
        titleRes = R.string.theme_light,
        descriptionRes = R.string.theme_light_desc,
        previewBg = Color(0xFFF5F5F7),
        previewSurface = Color(0xFFFFFFFF),
        previewAccent = Color(0xFF0071E3),
        previewText = Color(0xFF1D1D1F),
        previewBorder = Color(0xFFD2D2D7)
    ),
    EXTRA_LIGHT(
        titleRes = R.string.theme_extra_light,
        descriptionRes = R.string.theme_extra_light_desc,
        previewBg = Color(0xFFFFFFFF),
        previewSurface = Color(0xFFF8FAFC),
        previewAccent = Color(0xFF0284C7),
        previewText = Color(0xFF0F172A),
        previewBorder = Color(0xFFCBD5E1)
    ),
    DARK(
        titleRes = R.string.theme_dark,
        descriptionRes = R.string.theme_dark_desc,
        previewBg = Color(0xFF0F1117),
        previewSurface = Color(0xFF1C202B),
        previewAccent = Color(0xFF47A1FF),
        previewText = Color(0xFFF5F5F7),
        previewBorder = Color(0xFF323846)
    ),
    EXTRA_DARK(
        titleRes = R.string.theme_extra_dark,
        descriptionRes = R.string.theme_extra_dark_desc,
        previewBg = Color(0xFF000000),
        previewSurface = Color(0xFF0C0E12),
        previewAccent = Color(0xFF3898FF),
        previewText = Color(0xFFFAFAFC),
        previewBorder = Color(0xFF242832)
    ),
    HIGH_CONTRAST_LIGHT(
        titleRes = R.string.theme_high_contrast_light,
        descriptionRes = R.string.theme_high_contrast_light_desc,
        previewBg = Color(0xFFFFFFFF),
        previewSurface = Color(0xFFFFFFFF),
        previewAccent = Color(0xFF0033CC),
        previewText = Color(0xFF000000),
        previewBorder = Color(0xFF000000)
    ),
    HIGH_CONTRAST_BLACK(
        titleRes = R.string.theme_high_contrast_black,
        descriptionRes = R.string.theme_high_contrast_black_desc,
        previewBg = Color(0xFF000000),
        previewSurface = Color(0xFF000000),
        previewAccent = Color(0xFF5AC8FA),
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

// 1. Original Edam Light (Soft Apple off-white + frosted glass)
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
    surfaceVariant = Color(0xD9FFFFFF),
    onSurfaceVariant = Color(0xFF6E6E73),
    outline = Color(0x1A000000),
    outlineVariant = Color(0xE6FFFFFF)
)

// 2. Extra Light (Pure daylight alabaster white with airy sky-blue accents)
private val EdamExtraLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF6366F1),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF4338CA),
    tertiary = Color(0xFF15803D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDCFCE7),
    onTertiaryContainer = Color(0xFF14532D),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFCBD5E1)
)

// 3. Dark (Balanced deep slate night mode)
private val EdamDarkColorScheme = darkColorScheme(
    primary = Color(0xFF47A1FF),
    onPrimary = Color(0xFF001D36),
    primaryContainer = Color(0xFF123254),
    onPrimaryContainer = Color(0xFFCCE5FF),
    secondary = Color(0xFFC77DFF),
    onSecondary = Color(0xFF2D004D),
    secondaryContainer = Color(0xFF3E1F5B),
    onSecondaryContainer = Color(0xFFEED2FF),
    tertiary = Color(0xFF34C759),
    onTertiary = Color(0xFF003915),
    tertiaryContainer = Color(0xFF134524),
    onTertiaryContainer = Color(0xFFB7F397),
    error = Color(0xFFFF6961),
    onError = Color(0xFF410002),
    errorContainer = Color(0xFF531D1B),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F1117),
    onBackground = Color(0xFFF5F5F7),
    surface = Color(0xFF1A1E29),
    onSurface = Color(0xFFF5F5F7),
    surfaceVariant = Color(0xFF161922),
    onSurfaceVariant = Color(0xFFA0A7B8),
    outline = Color(0xFF2E3444),
    outlineVariant = Color(0xFF282E3D)
)

// 4. Extra Dark (True AMOLED pitch-black with deep charcoal cards)
private val EdamExtraDarkColorScheme = darkColorScheme(
    primary = Color(0xFF3898FF),
    onPrimary = Color(0xFF001933),
    primaryContainer = Color(0xFF0A2440),
    onPrimaryContainer = Color(0xFFB8DCFF),
    secondary = Color(0xFFB56BFF),
    onSecondary = Color(0xFF24003D),
    secondaryContainer = Color(0xFF2C1245),
    onSecondaryContainer = Color(0xFFE5C7FF),
    tertiary = Color(0xFF30D158),
    onTertiary = Color(0xFF003814),
    tertiaryContainer = Color(0xFF0B3318),
    onTertiaryContainer = Color(0xFFA7F5B9),
    error = Color(0xFFFF554A),
    onError = Color(0xFF3B0000),
    errorContainer = Color(0xFF421111),
    onErrorContainer = Color(0xFFFFD2CE),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFAFAFC),
    surface = Color(0xFF0B0D11),
    onSurface = Color(0xFFFAFAFC),
    surfaceVariant = Color(0xFF07080B),
    onSurfaceVariant = Color(0xFF8E94A3),
    outline = Color(0xFF222630),
    outlineVariant = Color(0xFF1C2029)
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
