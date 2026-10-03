package com.example.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Quick single-tap theme toggle button for Top App Bars and Navigation Bars.
 * Seamlessly flips between Light Mode (☀️) and Dark Mode (🌙) with smooth icon rotation and scale animation.
 */
@Composable
fun EdamThemeQuickToggleButton(
    themeMode: EdamThemeMode,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val systemInDark = isSystemInDarkTheme()
    val isCurrentlyDark = when (themeMode) {
        EdamThemeMode.DARK,
        EdamThemeMode.EXTRA_DARK,
        EdamThemeMode.HIGH_CONTRAST_BLACK -> true
        EdamThemeMode.LIGHT,
        EdamThemeMode.EXTRA_LIGHT,
        EdamThemeMode.HIGH_CONTRAST_LIGHT -> false
        EdamThemeMode.SYSTEM_DEFAULT -> systemInDark
    }

    val rotation by animateFloatAsState(
        targetValue = if (isCurrentlyDark) 360f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "theme_toggle_rotation"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isCurrentlyDark) Color(0xFFFBBF24) else Color(0xFFF59E0B),
        label = "theme_toggle_icon_color"
    )

    val containerBg by animateColorAsState(
        targetValue = if (isCurrentlyDark) Color(0xFF1F2937) else Color(0xFFF3EFEA),
        label = "theme_toggle_container_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isCurrentlyDark) Color(0xFF374151) else Color(0xFFD6D1C7),
        label = "theme_toggle_border_color"
    )

    Surface(
        shape = RoundedCornerShape(13.dp),
        color = containerBg,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .clickable(onClick = onToggleTheme)
            .minimumInteractiveComponentSize()
            .testTag("quick_theme_toggle_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .rotate(rotation)
                    .size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrentlyDark) {
                    Icon(
                        imageVector = Icons.Filled.DarkMode,
                        contentDescription = "Switch to Light Mode",
                        tint = iconColor,
                        modifier = Modifier.size(17.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.LightMode,
                        contentDescription = "Switch to Dark Mode",
                        tint = iconColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Text(
                text = if (isCurrentlyDark) "Dark" else "Light",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isCurrentlyDark) Color(0xFFEDE9E4) else Color(0xFF1F2937)
            )
        }
    }
}

/**
 * Segmented Pill Theme Switcher Component [ ☀️ Light | 🌙 Dark | ⚙️ Auto ].
 * Allows quick one-touch switching across platforms (Mobile & Desktop).
 */
@Composable
fun EdamThemeSegmentedSwitcher(
    themeMode: EdamThemeMode,
    onSelectTheme: (EdamThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalEdamThemeSpec.current
    val systemInDark = isSystemInDarkTheme()

    val options = listOf(
        Triple(EdamThemeMode.LIGHT, "Light", Icons.Filled.LightMode),
        Triple(EdamThemeMode.DARK, "Dark", Icons.Filled.DarkMode),
        Triple(EdamThemeMode.SYSTEM_DEFAULT, "System", Icons.Filled.SettingsBrightness)
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.testTag("theme_segmented_switcher")
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (mode, label, icon) ->
                val isSelected = when (mode) {
                    EdamThemeMode.SYSTEM_DEFAULT -> themeMode == EdamThemeMode.SYSTEM_DEFAULT
                    EdamThemeMode.LIGHT -> themeMode == EdamThemeMode.LIGHT || themeMode == EdamThemeMode.EXTRA_LIGHT || themeMode == EdamThemeMode.HIGH_CONTRAST_LIGHT
                    EdamThemeMode.DARK -> themeMode == EdamThemeMode.DARK || themeMode == EdamThemeMode.EXTRA_DARK || themeMode == EdamThemeMode.HIGH_CONTRAST_BLACK
                    else -> false
                }

                val targetBg = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                }

                val targetContentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                val animatedBg by animateColorAsState(targetValue = targetBg, label = "seg_bg")
                val animatedColor by animateColorAsState(targetValue = targetContentColor, label = "seg_color")

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = animatedBg,
                    border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectTheme(mode) }
                        .minimumInteractiveComponentSize()
                        .testTag("theme_switcher_pill_${label.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = animatedColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            ),
                            color = animatedColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Full Theme Card for Profile & Settings Screens.
 * Contains preview badges, descriptions, and accessible high-contrast options.
 */
@Composable
fun EdamFullThemeSwitcherCard(
    currentMode: EdamThemeMode,
    onSelectMode: (EdamThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalEdamThemeSpec.current

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .testTag("full_theme_switcher_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SettingsBrightness,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Display Theme & Contrast",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Toggle light & dark modes across mobile & desktop",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (themeSpec.isDark) "🌙 Dark" else "☀️ Light",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Quick segmented switcher bar
            EdamThemeSegmentedSwitcher(
                themeMode = currentMode,
                onSelectTheme = onSelectMode,
                modifier = Modifier.fillMaxWidth()
            )

            // Individual theme mode selection list
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val primaryModes = listOf(
                    EdamThemeMode.SYSTEM_DEFAULT,
                    EdamThemeMode.LIGHT,
                    EdamThemeMode.DARK,
                    EdamThemeMode.EXTRA_DARK,
                    EdamThemeMode.HIGH_CONTRAST_BLACK
                )

                primaryModes.forEach { mode ->
                    val isSelected = mode == currentMode
                    Surface(
                        onClick = { onSelectMode(mode) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        },
                        border = BorderStroke(
                            width = if (isSelected) 1.75.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("theme_card_option_${mode.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Color palette preview mini-swatch
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(mode.previewBg)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(mode.previewAccent)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(mode.titleRes),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = stringResource(mode.descriptionRes),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
