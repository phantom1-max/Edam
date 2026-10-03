package com.edam.desktop

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.util.prefs.Preferences
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Companions on Desktop sharing the exact same sleek, round silhouette and charm as the hero character [EDAM].
 */
enum class DesktopCompanion(
    val id: String,
    val displayName: String,
    val roleTitle: String,
    val speciesBadge: String,
    val bio: String,
    val accentHex: Long
) {
    EDAM(
        id = "edam",
        displayName = "Edam",
        roleTitle = "AI Study Sprout",
        speciesBadge = "Sprout Wheel",
        bio = "Hero character: round golden cheese wheel with twin botanical emerald sprout leaves, warm dimples, and smiling eyes.",
        accentHex = 0xFFF59E0B
    ),
    KORA(
        id = "kora",
        displayName = "Kora",
        roleTitle = "Market Alpha Companion",
        speciesBadge = "Flame Orb",
        bio = "Sleek ruby flame companion with golden momentum crest, rose-cream face, and emerald focus eyes.",
        accentHex = 0xFFE11D48
    ),
    VEX(
        id = "vex",
        displayName = "Vex",
        roleTitle = "Grandmaster Strategy Companion",
        speciesBadge = "Crown Orb",
        bio = "Dignified royal amethyst companion with celestial crystal crown crest, lavender-cream face, and strategy gold eyes.",
        accentHex = 0xFF7C3AED
    ),
    NOVA(
        id = "nova",
        displayName = "Nova",
        roleTitle = "Cyber Pulse Companion",
        speciesBadge = "Orbital Orb",
        bio = "Futuristic cyan tech companion with twin orbital halo crest, ice-cream face, and electric azure eyes.",
        accentHex = 0xFF0284C7
    )
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Edam — Learn Anything (PC Edition for Windows, macOS & Linux)",
        state = rememberWindowState(width = 1120.dp, height = 820.dp)
    ) {
        EdamDesktopApp()
    }
}

@Composable
fun EdamDesktopApp() {
    val prefs = remember { Preferences.userNodeForPackage(DesktopCompanion::class.java) }
    var isDarkMode by remember { mutableStateOf(prefs.getBoolean("is_dark_mode", true)) }
    var currentStreak by remember { mutableIntStateOf(prefs.getInt("current_streak", 4)) }
    var longestStreak by remember { mutableIntStateOf(prefs.getInt("longest_streak", 7)) }
    var studiedToday by remember { mutableStateOf(prefs.getBoolean("studied_today", false)) }
    var dailyGoal by remember { mutableIntStateOf(prefs.getInt("daily_goal", 3)) }
    var completedToday by remember { mutableIntStateOf(prefs.getInt("completed_today", 1)) }
    var activeCompanion by remember { mutableStateOf(DesktopCompanion.EDAM) }

    // Global window cursor position in root coordinates for idle companion eye-tracking
    var windowCursorPos by remember { mutableStateOf<Offset?>(null) }
    var rootOrigin by remember { mutableStateOf(Offset.Zero) }

    val darkScheme = darkColorScheme(
        primary = Color(0xFFF59E0B),
        secondary = Color(0xFF10B981),
        tertiary = Color(0xFF38BDF8),
        background = Color(0xFF111827),
        surface = Color(0xFF1F2937),
        surfaceVariant = Color(0xFF192231),
        onBackground = Color(0xFFEDE9E4),
        onSurface = Color(0xFFEDE9E4),
        outline = Color(0xFF374151)
    )

    val lightScheme = lightColorScheme(
        primary = Color(0xFFD97706),
        secondary = Color(0xFF059669),
        tertiary = Color(0xFF0284C7),
        background = Color(0xFFEDE9E4),
        surface = Color(0xFFF7F5F0),
        surfaceVariant = Color(0xFFE5E0D8),
        onBackground = Color(0xFF1F2937),
        onSurface = Color(0xFF1F2937),
        outline = Color(0xFFD6D1C7)
    )

    val currentScheme = if (isDarkMode) darkScheme else lightScheme

    MaterialTheme(colorScheme = currentScheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(currentScheme.background)
                .onGloballyPositioned { coords ->
                    rootOrigin = coords.localToRoot(Offset.Zero)
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull()
                            if (change != null) {
                                windowCursorPos = rootOrigin + change.position
                            }
                        }
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Top Desktop Navigation Bar with Flame Icon, Streak Count & Desktop Theme Switcher
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = currentScheme.surface,
                    border = BorderStroke(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DesktopCompanionCanvas(
                                companion = activeCompanion,
                                isIdle = false,
                                cursorInRoot = windowCursorPos,
                                size = 44.dp
                            )
                            Column {
                                Text(
                                    text = "Edam PC Studio",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = currentScheme.onBackground
                                )
                                Text(
                                    text = "Windows · macOS · Linux Desktop Edition",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = currentScheme.onSurface.copy(alpha = 0.65f)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Desktop Theme Switcher Component (Toggle between Light & Dark Modes)
                            DesktopThemeSwitcherPill(
                                isDarkMode = isDarkMode,
                                onToggle = {
                                    isDarkMode = !isDarkMode
                                    prefs.putBoolean("is_dark_mode", isDarkMode)
                                }
                            )

                            // Flame Icon + Current Consecutive Day Streak Count
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.16f),
                                border = BorderStroke(1.5.dp, Color(0xFFF59E0B))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalFireDepartment,
                                        contentDescription = "Daily Streak Flame",
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = "$currentStreak Day Streak",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Daily Learning Streak Hero Banner
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = currentScheme.surface,
                    border = BorderStroke(1.dp, currentScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = "Streak Flame",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "$currentStreak Consecutive Days",
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFFB45309)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, Color(0xFF10B981))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CloudDone,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Firestore Streak Synced",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Longest Streak: $longestStreak days · Daily Goal: $completedToday/$dailyGoal lessons completed today",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = currentScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (!studiedToday) {
                                    currentStreak += 1
                                    if (currentStreak > longestStreak) longestStreak = currentStreak
                                    studiedToday = true
                                    prefs.putInt("current_streak", currentStreak)
                                    prefs.putInt("longest_streak", longestStreak)
                                    prefs.putBoolean("studied_today", true)
                                }
                                completedToday = (completedToday + 1).coerceAtMost(10)
                                prefs.putInt("completed_today", completedToday)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (studiedToday) Color(0xFF10B981) else Color(0xFFF59E0B),
                                contentColor = Color(0xFF111827)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = if (studiedToday) Icons.Filled.CheckCircle else Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (studiedToday) "Streak Logged Today ✓ (+1 Lesson)" else "Ignite Today's Streak",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // 3. Companion Roster — All companions share hero character aesthetic & follow mouse cursor
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = currentScheme.surface,
                    border = BorderStroke(1.dp, currentScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Edam Companion Cast (PC Cursor Eye-Tracking)",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = currentScheme.onSurface
                                )
                                Text(
                                    text = "Companions doing nothing (IDLE) stay still and follow your cursor pointer with their eyes across Windows, Mac & Linux.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = currentScheme.onSurface.copy(alpha = 0.65f)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, Color(0xFF0284C7))
                            ) {
                                Text(
                                    text = windowCursorPos?.let { "🖱️ Cursor: (${it.x.toInt()}, ${it.y.toInt()})" } ?: "🖱️ Move Mouse",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DesktopCompanion.entries.forEach { companion ->
                                val isSelected = companion == activeCompanion
                                val isIdle = !isSelected
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) {
                                        if (isDarkMode) Color(0xFF273549) else Color(0xFFE2DDD3)
                                    } else {
                                        if (isDarkMode) Color(0xFF141B26) else Color(0xFFEDE9E4)
                                    },
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(companion.accentHex) else currentScheme.outline
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { activeCompanion = companion }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        DesktopCompanionCanvas(
                                            companion = companion,
                                            isIdle = isIdle,
                                            cursorInRoot = windowCursorPos,
                                            size = 96.dp
                                        )
                                        Text(
                                            text = companion.displayName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = currentScheme.onSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(999.dp),
                                            color = Color(companion.accentHex).copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = if (isIdle) "${companion.speciesBadge} · 👀 Following Cursor" else "${companion.speciesBadge} · Active Guide",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(companion.accentHex),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                        Text(
                                            text = companion.bio,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = currentScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Daily Lesson Goal Progress Tracker
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = currentScheme.surface,
                    border = BorderStroke(1.dp, currentScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Study Target ($completedToday of $dailyGoal Lessons Completed)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = currentScheme.onSurface
                            )
                            Text(
                                text = "${((completedToday.toFloat() / dailyGoal.toFloat()) * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color(0xFFF59E0B)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (completedToday.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f) },
                            color = Color(0xFFF59E0B),
                            trackColor = currentScheme.outline,
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(999.dp))
                        )
                    }
                }
            }
        }
    }
}

/**
 * Desktop Theme Switcher Pill Component.
 * Allows quick toggle between ☀️ Light Mode and 🌙 Dark Mode on Windows, macOS, and Linux.
 */
@Composable
fun DesktopThemeSwitcherPill(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isDarkMode) 360f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "desktop_theme_rot"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isDarkMode) Color(0xFF1F2937) else Color(0xFFE5E0D8),
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF374151) else Color(0xFFD6D1C7)),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .rotate(rotation)
                    .size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isDarkMode) {
                    Icon(
                        imageVector = Icons.Filled.DarkMode,
                        contentDescription = "Switch to Light Mode",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.LightMode,
                        contentDescription = "Switch to Dark Mode",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = if (isDarkMode) "Dark Mode" else "Light Mode",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDarkMode) Color(0xFFEDE9E4) else Color(0xFF1F2937)
            )
        }
    }
}

/**
 * Canvas rendering for Desktop Companions sharing the hero character's spherical aesthetic.
 */
@Composable
fun DesktopCompanionCanvas(
    companion: DesktopCompanion,
    isIdle: Boolean,
    cursorInRoot: Offset?,
    size: Dp
) {
    var mascotCenter by remember { mutableStateOf(Offset.Zero) }

    val infiniteTransition = rememberInfiniteTransition(label = "desk_companion_anim")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isIdle) 0f else -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Eye tracking logic toward cursor
    val (targetLookX, targetLookY) = remember(cursorInRoot, mascotCenter, isIdle) {
        if (isIdle && cursorInRoot != null && mascotCenter != Offset.Zero) {
            val dx = cursorInRoot.x - mascotCenter.x
            val dy = cursorInRoot.y - mascotCenter.y
            val angle = atan2(dy.toDouble(), dx.toDouble()).toFloat()
            val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()
            val maxShift = 10f
            val intensity = (dist / 240f).coerceIn(0.15f, 1.0f)
            Pair(cos(angle) * maxShift * intensity, sin(angle) * (maxShift * 0.75f) * intensity)
        } else {
            Pair(0f, 0f)
        }
    }

    val smoothLookX by animateFloatAsState(targetLookX, spring(dampingRatio = 0.85f, stiffness = 400f), label = "lx")
    val smoothLookY by animateFloatAsState(targetLookY, spring(dampingRatio = 0.85f, stiffness = 400f), label = "ly")

    Box(
        modifier = Modifier
            .size(size)
            .onGloballyPositioned { coordinates ->
                mascotCenter = coordinates.localToRoot(
                    Offset(coordinates.size.width / 2f, coordinates.size.height / 2f)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cw = this.size.width
            val ch = this.size.height
            val cx = cw / 2f
            val cy = ch / 2f + bounceY
            val baseR = cw.coerceAtMost(ch) * 0.35f

            when (companion) {
                DesktopCompanion.EDAM -> drawDesktopEdam(cx, cy, baseR, isIdle, smoothLookX, smoothLookY)
                DesktopCompanion.KORA -> drawDesktopKora(cx, cy, baseR, isIdle, smoothLookX, smoothLookY)
                DesktopCompanion.VEX -> drawDesktopVex(cx, cy, baseR, isIdle, smoothLookX, smoothLookY)
                DesktopCompanion.NOVA -> drawDesktopNova(cx, cy, baseR, isIdle, smoothLookX, smoothLookY)
            }
        }
    }
}

/** 1. EDAM on Desktop (Hero character: round cheese wheel with sprout) */
private fun DrawScope.drawDesktopEdam(
    cx: Float, cy: Float, baseR: Float, isIdle: Boolean, lookX: Float, lookY: Float
) {
    val stemTopY = cy - baseR * 1.30f
    drawLine(Color(0xFF047857), Offset(cx, cy - baseR * 0.90f), Offset(cx, stemTopY), baseR * 0.13f, StrokeCap.Round)
    rotate(-26f, Offset(cx, stemTopY)) {
        drawOval(Color(0xFF10B981), Offset(cx - baseR * 0.48f, stemTopY - baseR * 0.18f), Size(baseR * 0.50f, baseR * 0.28f))
    }
    rotate(26f, Offset(cx, stemTopY)) {
        drawOval(Color(0xFF34D399), Offset(cx - baseR * 0.02f, stemTopY - baseR * 0.18f), Size(baseR * 0.46f, baseR * 0.26f))
    }
    drawCircle(Brush.radialGradient(listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFD97706)), Offset(cx - baseR * 0.25f, cy - baseR * 0.25f), baseR * 1.35f), baseR, Offset(cx, cy))
    drawCircle(Color(0xFFB45309), baseR, Offset(cx, cy), style = Stroke(baseR * 0.07f))
    drawCircle(Color(0xFFFEF3C7), baseR * 0.80f, Offset(cx, cy + baseR * 0.03f))
    drawCircle(Color(0xFFFDE68A), baseR * 0.11f, Offset(cx - baseR * 0.56f, cy - baseR * 0.46f))
    drawCircle(Color(0xFFFDE68A), baseR * 0.07f, Offset(cx + baseR * 0.60f, cy + baseR * 0.44f))
    drawOval(Color(0xFFF87171).copy(alpha = 0.5f), Offset(cx - baseR * 0.54f, cy + baseR * 0.08f), Size(baseR * 0.26f, baseR * 0.14f))
    drawOval(Color(0xFFF87171).copy(alpha = 0.5f), Offset(cx + baseR * 0.30f, cy + baseR * 0.08f), Size(baseR * 0.26f, baseR * 0.14f))
    drawDesktopEyes(cx, cy, baseR, isIdle, lookX, lookY, Color(0xFF059669))
}

/** 2. KORA on Desktop (Market Alpha Companion: round ruby flame orb with momentum crest) */
private fun DrawScope.drawDesktopKora(
    cx: Float, cy: Float, baseR: Float, isIdle: Boolean, lookX: Float, lookY: Float
) {
    val stemTopY = cy - baseR * 1.30f
    drawLine(Color(0xFFD97706), Offset(cx, cy - baseR * 0.90f), Offset(cx, stemTopY), baseR * 0.13f, StrokeCap.Round)
    rotate(-26f, Offset(cx, stemTopY)) {
        drawOval(Color(0xFFF59E0B), Offset(cx - baseR * 0.48f, stemTopY - baseR * 0.20f), Size(baseR * 0.50f, baseR * 0.28f))
    }
    rotate(28f, Offset(cx, stemTopY)) {
        drawOval(Color(0xFFFBBF24), Offset(cx - baseR * 0.02f, stemTopY - baseR * 0.20f), Size(baseR * 0.46f, baseR * 0.26f))
    }
    drawCircle(Brush.radialGradient(listOf(Color(0xFFFB7185), Color(0xFFE11D48), Color(0xFFBE123C)), Offset(cx - baseR * 0.25f, cy - baseR * 0.25f), baseR * 1.35f), baseR, Offset(cx, cy))
    drawCircle(Color(0xFF9F1239), baseR, Offset(cx, cy), style = Stroke(baseR * 0.07f))
    drawCircle(Color(0xFFFFF1F2), baseR * 0.80f, Offset(cx, cy + baseR * 0.03f))
    drawCircle(Color(0xFFFDE68A), baseR * 0.09f, Offset(cx - baseR * 0.56f, cy - baseR * 0.46f))
    drawCircle(Color(0xFFFDE68A), baseR * 0.06f, Offset(cx + baseR * 0.60f, cy + baseR * 0.44f))
    drawOval(Color(0xFFFB7185).copy(alpha = 0.5f), Offset(cx - baseR * 0.54f, cy + baseR * 0.08f), Size(baseR * 0.26f, baseR * 0.14f))
    drawOval(Color(0xFFFB7185).copy(alpha = 0.5f), Offset(cx + baseR * 0.30f, cy + baseR * 0.08f), Size(baseR * 0.26f, baseR * 0.14f))
    drawDesktopEyes(cx, cy, baseR, isIdle, lookX, lookY, Color(0xFF059669))
}

/** 3. VEX on Desktop (Grandmaster Strategy Companion: round amethyst orb with crystal crown crest) */
private fun DrawScope.drawDesktopVex(
    cx: Float, cy: Float, baseR: Float, isIdle: Boolean, lookX: Float, lookY: Float
) {
    val stemTopY = cy - baseR * 1.30f
    drawLine(Color(0xFF4C1D95), Offset(cx, cy - baseR * 0.90f), Offset(cx, stemTopY), baseR * 0.13f, StrokeCap.Round)
    drawOval(Color(0xFFFBBF24), Offset(cx - baseR * 0.14f, stemTopY - baseR * 0.26f), Size(baseR * 0.28f, baseR * 0.38f))
    rotate(-30f, Offset(cx, stemTopY)) {
        drawOval(Color(0xFFFDE68A), Offset(cx - baseR * 0.44f, stemTopY - baseR * 0.16f), Size(baseR * 0.44f, baseR * 0.24f))
    }
    rotate(30f, Offset(cx, stemTopY)) {
        drawOval(Color(0xFFFBBF24), Offset(cx, stemTopY - baseR * 0.16f), Size(baseR * 0.44f, baseR * 0.24f))
    }
    drawCircle(Brush.radialGradient(listOf(Color(0xFFA78BFA), Color(0xFF7C3AED), Color(0xFF6D28D9)), Offset(cx - baseR * 0.25f, cy - baseR * 0.25f), baseR * 1.35f), baseR, Offset(cx, cy))
    drawCircle(Color(0xFF5B21B6), baseR, Offset(cx, cy), style = Stroke(baseR * 0.07f))
    drawCircle(Color(0xFFF5F3FF), baseR * 0.80f, Offset(cx, cy + baseR * 0.03f))
    drawCircle(Color(0xFFDDD6FE), baseR * 0.09f, Offset(cx - baseR * 0.56f, cy - baseR * 0.46f))
    drawOval(Color(0xFFC084FC).copy(alpha = 0.5f), Offset(cx - baseR * 0.54f, cy + baseR * 0.08f), Size(baseR * 0.26f, baseR * 0.14f))
    drawOval(Color(0xFFC084FC).copy(alpha = 0.5f), Offset(cx + baseR * 0.30f, cy + baseR * 0.08f), Size(baseR * 0.26f, baseR * 0.14f))
    drawDesktopEyes(cx, cy, baseR, isIdle, lookX, lookY, Color(0xFFD97706))
}

/** 4. NOVA on Desktop (Cyber Pulse Companion: round cyan orb with orbital halo crest) */
private fun DrawScope.drawDesktopNova(
    cx: Float, cy: Float, baseR: Float, isIdle: Boolean, lookX: Float, lookY: Float
) {
    val stemTopY = cy - baseR * 1.30f
    drawLine(Color(0xFF0E7490), Offset(cx, cy - baseR * 0.90f), Offset(cx, stemTopY), baseR * 0.13f, StrokeCap.Round)
    drawCircle(Color(0xFF38BDF8), baseR * 0.11f, Offset(cx, stemTopY - baseR * 0.12f))
    rotate(-28f, Offset(cx, stemTopY)) {
        drawOval(Color(0xFF06B6D4), Offset(cx - baseR * 0.48f, stemTopY - baseR * 0.20f), Size(baseR * 0.50f, baseR * 0.28f))
    }
    rotate(28f, Offset(cx, stemTopY)) {
        drawOval(Color(0xFF38BDF8), Offset(cx - baseR * 0.02f, stemTopY - baseR * 0.20f), Size(baseR * 0.46f, baseR * 0.26f))
    }
    drawCircle(Brush.radialGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1)), Offset(cx - baseR * 0.25f, cy - baseR * 0.25f), baseR * 1.35f), baseR, Offset(cx, cy))
    drawCircle(Color(0xFF075985), baseR, Offset(cx, cy), style = Stroke(baseR * 0.07f))
    drawCircle(Color(0xFFECFEFF), baseR * 0.80f, Offset(cx, cy + baseR * 0.03f))
    drawCircle(Color(0xFFA5F3FC), baseR * 0.09f, Offset(cx - baseR * 0.56f, cy - baseR * 0.46f))
    drawOval(Color(0xFF38BDF8).copy(alpha = 0.45f), Offset(cx - baseR * 0.54f, cy + baseR * 0.08f), Size(baseR * 0.26f, baseR * 0.14f))
    drawOval(Color(0xFF38BDF8).copy(alpha = 0.45f), Offset(cx + baseR * 0.30f, cy + baseR * 0.08f), Size(baseR * 0.26f, baseR * 0.14f))
    drawDesktopEyes(cx, cy, baseR, isIdle, lookX, lookY, Color(0xFF2563EB))
}

/** Shared Cursor-Tracking Eyes for Desktop */
private fun DrawScope.drawDesktopEyes(
    cx: Float, cy: Float, baseR: Float, isIdle: Boolean, lookX: Float, lookY: Float, irisColor: Color
) {
    val eyeY = cy - baseR * 0.08f
    val socketRadius = baseR * 0.185f
    val pupilShiftX = (lookX * (baseR * 0.012f)).coerceIn(-socketRadius * 0.48f, socketRadius * 0.48f)
    val pupilShiftY = (lookY * (baseR * 0.012f)).coerceIn(-socketRadius * 0.42f, socketRadius * 0.42f)

    listOf(cx - baseR * 0.32f, cx + baseR * 0.32f).forEach { eyeX ->
        // Sclera
        drawOval(Color.White, Offset(eyeX - socketRadius, eyeY - socketRadius), Size(socketRadius * 2f, socketRadius * 2f))
        // Iris
        val irisR = baseR * 0.13f
        drawOval(irisColor, Offset(eyeX - irisR + pupilShiftX, eyeY - irisR + pupilShiftY), Size(irisR * 2f, irisR * 2f))
        // Pupil
        val pupilR = baseR * 0.08f
        drawOval(Color(0xFF111827), Offset(eyeX - pupilR + pupilShiftX * 1.18f, eyeY - pupilR + pupilShiftY * 1.18f), Size(pupilR * 2f, pupilR * 2f))
        // Highlight
        drawCircle(Color.White, baseR * 0.04f, Offset(eyeX - baseR * 0.04f + pupilShiftX * 0.9f, eyeY - baseR * 0.04f + pupilShiftY * 0.9f))
    }

    // Smile mouth
    val smileW = baseR * 0.34f
    val smileH = baseR * 0.22f
    drawArc(
        color = Color(0xFF111827),
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(cx - smileW / 2f, cy + baseR * 0.25f - smileH * 0.45f),
        size = Size(smileW, smileH),
        style = Stroke(width = baseR * 0.07f, cap = StrokeCap.Round)
    )
}
