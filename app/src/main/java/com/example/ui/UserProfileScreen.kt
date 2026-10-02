package com.example.ui

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.DailyStreakManager
import com.example.data.local.DailyStreakState
import com.example.data.model.BadgeDefinition
import com.example.data.model.BadgeDisplayItem
import com.example.data.model.PlanTier
import com.example.notification.EdamNotificationHelper
import com.example.ui.theme.LocalEdamThemeSpec
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class BadgeFilter {
    ALL,
    UNLOCKED,
    IN_PROGRESS
}

@Composable
fun UserProfileDialog(
    userDisplayName: String,
    userEmail: String,
    planTier: PlanTier,
    totalCoursesCount: Int,
    totalCompletedLessons: Int,
    averageMasteryPct: Int,
    badgeDisplayItems: List<BadgeDisplayItem>,
    pushNotificationsEnabled: Boolean,
    dailyStreak: DailyStreakState? = null,
    selectedCompanion: EdamCompanionCharacter = EdamCompanionCharacter.EDAM,
    onSelectCompanion: (EdamCompanionCharacter) -> Unit = {},
    onCheckInStreak: (() -> Unit)? = null,
    onToggleStreakFreeze: (() -> Unit)? = null,
    onTogglePushNotifications: (Boolean) -> Unit,
    onSendTestNotification: () -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    val localStreakManager = remember(context) { DailyStreakManager(context) }
    val prefsStreakState by localStreakManager.streakState.collectAsState()
    val effectiveStreak = dailyStreak ?: prefsStreakState
    val themeSpec = LocalEdamThemeSpec.current
    var activeFilter by remember { mutableStateOf(BadgeFilter.ALL) }
    var hasPermission by remember {
        mutableStateOf(EdamNotificationHelper.hasNotificationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            onTogglePushNotifications(true)
            onSendTestNotification()
        }
    }

    val unlockedCount = badgeDisplayItems.count { it.isUnlocked }
    val totalBadgesCount = badgeDisplayItems.size

    val filteredBadges = remember(badgeDisplayItems, activeFilter) {
        when (activeFilter) {
            BadgeFilter.ALL -> badgeDisplayItems
            BadgeFilter.UNLOCKED -> badgeDisplayItems.filter { it.isUnlocked }
            BadgeFilter.IN_PROGRESS -> badgeDisplayItems.filter { !it.isUnlocked }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(12.dp)
                .testTag("user_profile_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EdamMascot(
                            expression = EdamExpression.HAPPY,
                            character = selectedCompanion,
                            size = 44.dp,
                            showTablet = false
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.profile_screen_title),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = stringResource(R.string.profile_screen_subtitle),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Profile",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // 1. Identity & Metrics Card
                    item(key = "identity_card") {
                        ProfileIdentityCard(
                            userDisplayName = userDisplayName,
                            userEmail = userEmail,
                            planTier = planTier,
                            selectedCompanion = selectedCompanion,
                            unlockedBadges = unlockedCount,
                            totalBadges = totalBadgesCount,
                            totalCourses = totalCoursesCount,
                            totalLessons = totalCompletedLessons,
                            averageMastery = averageMasteryPct
                        )
                    }

                    // 1B. Dedicated Streak Visualizer with Edam Mascot & SharedPreferences Persistence
                    item(key = "streak_visualizer_section") {
                        StreakVisualizer(
                            dailyStreak = effectiveStreak,
                            selectedCompanion = selectedCompanion,
                            onCheckInToday = {
                                if (onCheckInStreak != null) {
                                    onCheckInStreak()
                                } else {
                                    localStreakManager.recordStudyActivity(xpEarned = 20)
                                }
                            },
                            onToggleStreakFreeze = {
                                if (onToggleStreakFreeze != null) {
                                    onToggleStreakFreeze()
                                } else {
                                    localStreakManager.toggleStreakFreeze()
                                }
                            }
                        )
                    }

                    // 1C. Animated Edam & Companion Roster Selector
                    item(key = "companion_roster_card") {
                        EdamCompanionRosterCard(
                            selectedCharacter = selectedCompanion,
                            onSelectCharacter = onSelectCompanion
                        )
                    }

                    // 2. Push Notifications Control Card
                    item(key = "notifications_card") {
                        ProfileNotificationCard(
                            pushNotificationsEnabled = pushNotificationsEnabled,
                            hasPermission = hasPermission,
                            onTogglePush = onTogglePushNotifications,
                            onRequestPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onTogglePushNotifications(true)
                                    onSendTestNotification()
                                }
                            },
                            onSendTest = onSendTestNotification
                        )
                    }

                    // 3. Local Storage Badge Showcase Header & Filter Tabs
                    item(key = "badges_header") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.EmojiEvents,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.profile_section_badges, unlockedCount, totalBadgesCount),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Storage,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "Room SQLite",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }

                            // Filter Tabs: All, Unlocked, In Progress
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BadgeFilter.entries.forEach { filter ->
                                    val isSelected = filter == activeFilter
                                    val count = when (filter) {
                                        BadgeFilter.ALL -> totalBadgesCount
                                        BadgeFilter.UNLOCKED -> unlockedCount
                                        BadgeFilter.IN_PROGRESS -> totalBadgesCount - unlockedCount
                                    }
                                    val label = when (filter) {
                                        BadgeFilter.ALL -> "All ($count)"
                                        BadgeFilter.UNLOCKED -> "Unlocked ($count)"
                                        BadgeFilter.IN_PROGRESS -> "In Progress ($count)"
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else themeSpec.borderWidth,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .clickable { activeFilter = filter }
                                            .minimumInteractiveComponentSize()
                                            .testTag("badge_filter_${filter.name.lowercase()}")
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Badge Cards List
                    items(filteredBadges, key = { it.definition.id }) { item ->
                        BadgeCardItem(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileIdentityCard(
    userDisplayName: String,
    userEmail: String,
    planTier: PlanTier,
    selectedCompanion: EdamCompanionCharacter,
    unlockedBadges: Int,
    totalBadges: Int,
    totalCourses: Int,
    totalLessons: Int,
    averageMastery: Int
) {
    val themeSpec = LocalEdamThemeSpec.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_identity_card"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (themeSpec.isHighContrast) 0.dp else 2.dp,
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Mascot Avatar, Display Name, Email, Plan Tier
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                EdamMascot(
                    expression = EdamExpression.SUCCESS,
                    character = selectedCompanion,
                    size = 64.dp,
                    showTablet = false
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = userDisplayName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = planTier.badgeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (userEmail.isNotBlank()) {
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Local Learner (Offline Ready)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            // 4-Column Stat Bento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileStatPill(
                    label = stringResource(R.string.profile_stat_badges),
                    value = "$unlockedBadges/$totalBadges",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatPill(
                    label = stringResource(R.string.profile_stat_courses),
                    value = totalCourses.toString(),
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatPill(
                    label = stringResource(R.string.profile_stat_lessons),
                    value = totalLessons.toString(),
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatPill(
                    label = stringResource(R.string.profile_stat_mastery),
                    value = "$averageMastery%",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Dedicated 'Streak Visualizer' component on the Profile Screen.
 * Uses the animated Edam mascot to visualize the learner's current daily streak,
 * milestone progress ring, 7-day consistency timeline, and streak shield,
 * backed directly by SharedPreferences via [DailyStreakManager].
 */
@Composable
fun StreakVisualizer(
    dailyStreak: DailyStreakState? = null,
    selectedCompanion: EdamCompanionCharacter = EdamCompanionCharacter.EDAM,
    onCheckInToday: (() -> Unit)? = null,
    onToggleStreakFreeze: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localStreakManager = remember(context) { DailyStreakManager(context) }
    val prefsStreak by localStreakManager.streakState.collectAsState()
    val streak = dailyStreak ?: prefsStreak

    val themeSpec = LocalEdamThemeSpec.current
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    val milestones = listOf(3, 7, 14, 30)

    val animatedRingProgress by animateFloatAsState(
        targetValue = streak.milestoneProgress,
        animationSpec = spring(),
        label = "streak_visualizer_ring_progress"
    )

    val mascotExpression = when {
        streak.studiedToday -> EdamExpression.SUCCESS
        streak.currentStreak >= 3 -> EdamExpression.HAPPY
        streak.currentStreak > 0 -> EdamExpression.CURIOUS
        else -> EdamExpression.ENCOURAGEMENT
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("streak_visualizer_card")
            .testTag("profile_daily_streak_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.75f)),
        tonalElevation = if (themeSpec.isHighContrast) 0.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header Row: Eyebrow + Title + SharedPreferences Storage Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = "Streak Visualizer Flame",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "EDAM STREAK VISUALIZER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.9.sp
                            ),
                            color = Color(0xFFF59E0B)
                        )
                        Text(
                            text = streak.streakTierTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // SharedPreferences & Streak Shield Status Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = {
                            if (onToggleStreakFreeze != null) {
                                onToggleStreakFreeze()
                            } else {
                                localStreakManager.toggleStreakFreeze()
                            }
                        },
                        shape = RoundedCornerShape(999.dp),
                        color = if (streak.streakFreezeActive) {
                            Color(0xFF0EA5E9).copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(
                            1.dp,
                            if (streak.streakFreezeActive) Color(0xFF38BDF8) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.testTag("streak_visualizer_freeze_button")
                    ) {
                        Text(
                            text = if (streak.streakFreezeActive) {
                                "🛡️ Shield ON (${streak.streakFreezesAvailable})"
                            } else {
                                "🛡️ Shield OFF"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (streak.streakFreezeActive) {
                                Color(0xFF38BDF8)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "SharedPrefs",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 2. Hero Mascot Ring Stage + Live Streak Counter & Companion Coach Message
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1F2937),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("streak_visualizer_mascot_stage")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF111827),
                                    Color(0xFF1F2937),
                                    Color(0xFF273549)
                                )
                            )
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Circular Flame Progress Ring around the Animated Edam Mascot
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .testTag("streak_visualizer_mascot"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeW = 7.dp.toPx()
                            val diameter = size.minDimension - strokeW
                            val topLeft = Offset(strokeW / 2f, strokeW / 2f)

                            // Track ring
                            drawArc(
                                color = Color(0xFF374151),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(diameter, diameter),
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )

                            // Active Amber-Orange Streak Progress Arc
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFFF59E0B),
                                        Color(0xFFFBBF24),
                                        Color(0xFF10B981),
                                        Color(0xFFF59E0B)
                                    )
                                ),
                                startAngle = 135f,
                                sweepAngle = 270f * animatedRingProgress,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(diameter, diameter),
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                        }

                        EdamMascot(
                            expression = mascotExpression,
                            character = selectedCompanion,
                            size = 78.dp,
                            showTablet = !streak.studiedToday
                        )
                    }

                    // Streak Count, Next Goal & Edam's Dynamic Speech Message
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${streak.currentStreak}",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 36.sp,
                                    lineHeight = 38.sp
                                ),
                                color = Color(0xFFFBBF24),
                                modifier = Modifier.testTag("streak_visualizer_count")
                            )
                            Text(
                                text = "${streak.currentStreak} Day Streak",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color(0xFFEDE9E4),
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .testTag("profile_current_streak_text")
                            )
                        }

                        Text(
                            text = when {
                                streak.studiedToday ->
                                    "${selectedCompanion.displayName} is celebrating! You've locked in today's study session and are ${(streak.nextMilestoneDays - streak.currentStreak).coerceAtLeast(1)} days from the ${streak.nextMilestoneDays}-day milestone."
                                streak.currentStreak > 0 ->
                                    "${selectedCompanion.displayName} is ready! Log a quick check-in or lesson today to push your streak to ${streak.currentStreak + 1} days."
                                else ->
                                    "${selectedCompanion.displayName} says: Every master starts with Day 1. Tap below to ignite your new learning streak!"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = Color(0xFFEDE9E4).copy(alpha = 0.86f)
                        )
                    }
                }
            }

            // 3. Milestone Progress Bar & Milestone Checkpoints (3d -> 7d -> 14d -> 30d)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next Milestone: ${streak.nextMilestoneDays}-Day Streak",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${streak.currentStreak} / ${streak.nextMilestoneDays} days (${(animatedRingProgress * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFFF59E0B)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("streak_visualizer_milestone_bar")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedRingProgress)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFF59E0B),
                                        Color(0xFFFBBF24)
                                    )
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    milestones.forEach { targetDays ->
                        val reached = streak.currentStreak >= targetDays
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (reached) {
                                Color(0xFFF59E0B).copy(alpha = 0.18f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = BorderStroke(
                                1.dp,
                                if (reached) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Text(
                                text = if (reached) "🔥 ${targetDays}d ✓" else "🎯 ${targetDays}d",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (reached) FontWeight.ExtraBold else FontWeight.Medium
                                ),
                                color = if (reached) {
                                    Color(0xFFF59E0B)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 4. 7-Day Weekly Consistency Timeline (M T W T F S S)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEachIndexed { index, dayLabel ->
                    val completed = streak.weeklyStudyDays.getOrElse(index) { false }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (completed) Color(0xFFF59E0B) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = "$dayLabel streak status",
                                tint = if (completed) Color(0xFF111827) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (completed) FontWeight.ExtraBold else FontWeight.Medium
                            ),
                            color = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 5. Streak & XP Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileStatPill(
                    label = "Current Streak",
                    value = "${streak.currentStreak}d",
                    accentColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                ProfileStatPill(
                    label = "Best Streak",
                    value = "${streak.longestStreak}d",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatPill(
                    label = "Total XP",
                    value = "${streak.totalXp}",
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatPill(
                    label = "Cards & Chess",
                    value = "${streak.flashcardsReviewedCount + streak.chessPuzzlesSolvedCount}",
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            // 6. Interactive SharedPreferences Daily Streak Check-In Button
            Button(
                onClick = {
                    if (onCheckInToday != null) {
                        onCheckInToday()
                    } else {
                        localStreakManager.recordStudyActivity(xpEarned = 20)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (streak.studiedToday) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        Color(0xFFF59E0B)
                    },
                    contentColor = if (streak.studiedToday) {
                        MaterialTheme.colorScheme.onTertiary
                    } else {
                        Color(0xFF111827)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("streak_visualizer_checkin_button")
            ) {
                Icon(
                    imageVector = if (streak.studiedToday) {
                        Icons.Filled.CheckCircle
                    } else {
                        Icons.Filled.LocalFireDepartment
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (streak.studiedToday) {
                        "Today's Streak Logged in SharedPreferences ✓ (+20 Bonus XP)"
                    } else {
                        "Ignite Today's Streak with ${selectedCompanion.displayName} (+20 XP)"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}

@Composable
private fun ProfileStatPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = accentColor,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProfileNotificationCard(
    pushNotificationsEnabled: Boolean,
    hasPermission: Boolean,
    onTogglePush: (Boolean) -> Unit,
    onRequestPermission: () -> Unit,
    onSendTest: () -> Unit
) {
    val themeSpec = LocalEdamThemeSpec.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_notification_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.profile_section_notifications),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (pushNotificationsEnabled && hasPermission) {
                                stringResource(R.string.profile_permission_granted)
                            } else if (!hasPermission) {
                                "Permission Required on Device"
                            } else {
                                "Notifications Paused"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (pushNotificationsEnabled && hasPermission) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Switch(
                    checked = pushNotificationsEnabled,
                    onCheckedChange = onTogglePush,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("notification_toggle_switch")
                )
            }

            Text(
                text = stringResource(R.string.profile_notifications_desc),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Permission Request or Test Notification Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!hasPermission) {
                    Button(
                        onClick = onRequestPermission,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("request_notification_permission_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.profile_btn_grant_permission),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onSendTest,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("send_test_notification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.profile_btn_test_notification),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeCardItem(
    item: BadgeDisplayItem
) {
    val themeSpec = LocalEdamThemeSpec.current
    val def = item.definition
    val isUnlocked = item.isUnlocked

    val categoryColor = remember(def.colorCategory) {
        when (def.colorCategory) {
            "bronze" -> Color(0xFFCD7F32)
            "silver" -> Color(0xFFC0C0C0)
            "gold" -> Color(0xFFFFD700)
            "emerald" -> Color(0xFF10B981)
            "diamond" -> Color(0xFF38BDF8)
            "ruby" -> Color(0xFFF43F5E)
            else -> Color(0xFF8B5CF6)
        }
    }

    val iconVector: ImageVector = remember(def.iconKey) {
        when (def.iconKey) {
            "spark" -> Icons.Filled.AutoAwesome
            "momentum" -> Icons.AutoMirrored.Filled.TrendingUp
            "titan" -> Icons.Filled.MilitaryTech
            "adept" -> Icons.Filled.Star
            "scholar" -> Icons.Filled.School
            "market_bull" -> Icons.Filled.Verified
            else -> Icons.Filled.EmojiEvents
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = (item.currentProgressPct.toFloat() / def.percentageRequired.toFloat()).coerceIn(0f, 1f),
        animationSpec = spring(),
        label = "badge_progress"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("badge_card_${def.id}"),
        shape = RoundedCornerShape(18.dp),
        color = if (isUnlocked) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        border = BorderStroke(
            width = if (isUnlocked) 1.5.dp else themeSpec.borderWidth,
            color = if (isUnlocked) categoryColor else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Badge Icon Emblem Container
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUnlocked) {
                                categoryColor.copy(alpha = 0.22f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUnlocked) iconVector else Icons.Filled.Lock,
                        contentDescription = def.title,
                        tint = if (isUnlocked) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = def.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = categoryColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${def.percentageRequired}% Milestone",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = categoryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = def.description,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Unlock Status Pill
                if (isUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = stringResource(R.string.profile_badge_status_unlocked),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Progress or Unlock Timestamp Details
            if (isUnlocked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateFormatted = remember(item.unlockedAt) {
                        if (item.unlockedAt != null && item.unlockedAt > 0) {
                            val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
                            sdf.format(Date(item.unlockedAt))
                        } else {
                            "Course Completion Milestone"
                        }
                    }
                    Text(
                        text = if (item.courseTitle != null) {
                            "Earned in: ${item.courseTitle}"
                        } else {
                            "Earned via Course Completion"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Course Progress Tracker",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(
                                R.string.profile_badge_status_progress,
                                item.currentProgressPct,
                                def.percentageRequired
                            ),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Progress Track Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .clip(RoundedCornerShape(99.dp))
                                .background(categoryColor)
                        )
                    }
                }
            }
        }
    }
}
