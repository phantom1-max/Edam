package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.LeaderboardCompetitor
import com.example.data.model.LeaderboardZone
import com.example.data.model.LeagueTier
import com.example.data.model.SpeedDrillQuestion
import com.example.ui.theme.LocalEdamThemeSpec

@Composable
fun DuolingoLeaderboardScreen(
    selectedLeague: LeagueTier,
    competitors: List<LeaderboardCompetitor>,
    currentUserRank: Int,
    currentUserWeeklyXp: Int,
    isDrillModalVisible: Boolean,
    currentDrillQuestion: SpeedDrillQuestion?,
    drillSelectedOption: Int?,
    drillFeedbackMessage: String?,
    isDrillAnswerCorrect: Boolean?,
    streakBonusClaimedToday: Boolean,
    currentStreakDays: Int = 3,
    onSelectLeague: (LeagueTier) -> Unit,
    onStartSpeedDrill: () -> Unit,
    onSubmitDrillAnswer: (Int) -> Unit,
    onCloseSpeedDrill: () -> Unit,
    onClaimStreakBonus: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalEdamThemeSpec.current
    val leagueColor = Color(selectedLeague.primaryColorHex)
    val nextLeague = selectedLeague.nextLeague()
    val prevLeague = selectedLeague.previousLeague()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("duolingo_leaderboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Screen Header with Back Button
        item(key = "leaderboard_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("leaderboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    EdamMascot(
                        expression = EdamExpression.HAPPY,
                        character = EdamCompanionCharacter.NOVA,
                        size = 46.dp,
                        showTablet = false
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.leaderboard_screen_title),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.leaderboard_season_ends_format, "3d 14h 22m"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = leagueColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, leagueColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(selectedLeague.emoji, fontSize = 16.sp)
                        Text(
                            text = selectedLeague.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = leagueColor
                        )
                    }
                }
            }
        }

        // 2. Horizontal League Tier Progression Bar (Bronze -> Diamond)
        item(key = "league_tier_selector") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "SELECT LEAGUE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(LeagueTier.entries) { tier ->
                        val isCurrent = tier == selectedLeague
                        val tierColor = Color(tier.primaryColorHex)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectLeague(tier) }
                                .testTag("league_chip_${tier.name.lowercase()}"),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCurrent) tierColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = if (isCurrent) tierColor else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(tier.emoji, fontSize = 16.sp)
                                Text(
                                    text = tier.displayName.replace(" League", ""),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isCurrent) tierColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Current User Quick Stats & Actions Card
        item(key = "user_stats_action_card") {
            UserCompetitionActionCard(
                rank = currentUserRank,
                weeklyXp = currentUserWeeklyXp,
                league = selectedLeague,
                streakClaimed = streakBonusClaimedToday,
                streakDays = currentStreakDays,
                onStartDrill = onStartSpeedDrill,
                onClaimStreak = onClaimStreakBonus
            )
        }

        // 4. League Rules Notice (Promotion vs Demotion zone criteria)
        item(key = "zone_rules_card") {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Text(
                            text = "Ranks 1–5: Promotes to ${nextLeague?.displayName ?: "Apex"}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )
                        Text(
                            text = "Ranks 26–30: Drops to ${prevLeague?.displayName ?: "Base"}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 5. Leaderboard Rankings Table
        itemsIndexed(competitors, key = { _, comp -> comp.id }) { index, competitor ->
            // Insert Zone Section Dividers
            if (index == 0) {
                ZoneHeaderBanner(
                    title = stringResource(
                        R.string.leaderboard_promotion_zone,
                        nextLeague?.displayName ?: "Next League"
                    ),
                    color = Color(0xFF22C55E),
                    isPromotion = true
                )
                Spacer(modifier = Modifier.height(4.dp))
            } else if (index == 5) {
                ZoneHeaderBanner(
                    title = stringResource(
                        R.string.leaderboard_safe_zone,
                        selectedLeague.displayName
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    isPromotion = false
                )
                Spacer(modifier = Modifier.height(4.dp))
            } else if (index == 25 && competitors.size > 25) {
                ZoneHeaderBanner(
                    title = stringResource(
                        R.string.leaderboard_demotion_zone,
                        prevLeague?.displayName ?: "Previous League"
                    ),
                    color = Color(0xFFEF4444),
                    isPromotion = false
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            DuolingoCompetitorRow(
                competitor = competitor,
                rank = index + 1
            )
        }
    }

    // 6. Rapid Speed Quiz Drill Dialog
    if (isDrillModalVisible && currentDrillQuestion != null) {
        SpeedDrillModalDialog(
            question = currentDrillQuestion,
            selectedOption = drillSelectedOption,
            feedbackMessage = drillFeedbackMessage,
            isCorrect = isDrillAnswerCorrect,
            onSelectOption = onSubmitDrillAnswer,
            onClose = onCloseSpeedDrill
        )
    }
}

@Composable
private fun UserCompetitionActionCard(
    rank: Int,
    weeklyXp: Int,
    league: LeagueTier,
    streakClaimed: Boolean,
    streakDays: Int = 3,
    onStartDrill: () -> Unit,
    onClaimStreak: () -> Unit
) {
    val leagueColor = Color(league.primaryColorHex)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_competition_card"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.5.dp, leagueColor)
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(leagueColor, MaterialTheme.colorScheme.primary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "YOUR POSITION",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (rank <= 5) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color(0xFF22C55E).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "PROMOTING ⬆",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = Color(0xFF22C55E),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = stringResource(R.string.leaderboard_xp_format, weeklyXp),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Daily Streak Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Whatshot,
                            contentDescription = "Streak",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "$streakDays DAYS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }

            // Quick XP Boost Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStartDrill,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("rapid_xp_challenge_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.leaderboard_btn_rapid_drill),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                OutlinedButton(
                    onClick = onClaimStreak,
                    enabled = !streakClaimed,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("claim_streak_bonus_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (streakClaimed) {
                            stringResource(R.string.leaderboard_streak_claimed)
                        } else {
                            stringResource(R.string.leaderboard_btn_claim_streak)
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoneHeaderBanner(
    title: String,
    color: Color,
    isPromotion: Boolean
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 12.dp)
        )
    }
}

@Composable
private fun DuolingoCompetitorRow(
    competitor: LeaderboardCompetitor,
    rank: Int
) {
    val isCurrentUser = competitor.isCurrentUser
    val isTop3 = rank in 1..3
    val themeSpec = LocalEdamThemeSpec.current

    val medalIcon = when (rank) {
        1 -> "👑"
        2 -> "🥈"
        3 -> "🥉"
        else -> null
    }

    val medalColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (isCurrentUser) "current_user_row" else "competitor_row_$rank"),
        shape = RoundedCornerShape(16.dp),
        color = if (isCurrentUser) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (isCurrentUser) 2.dp else if (isTop3) 1.2.dp else 0.7.dp,
            color = if (isCurrentUser) {
                MaterialTheme.colorScheme.primary
            } else if (isTop3) {
                medalColor.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        tonalElevation = if (isCurrentUser) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rank Number or Medal
                Box(
                    modifier = Modifier.width(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (medalIcon != null) {
                        Text(medalIcon, fontSize = 20.sp)
                    } else {
                        Text(
                            text = "$rank",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (rank <= 5) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(competitor.avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = competitor.avatarInitial,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                // Competitor Name + YOU tag
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = competitor.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isCurrentUser) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "YOU",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    // Flame Streak
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${competitor.streakDays}d streak · ${competitor.titleBadge}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Weekly XP Score
            Text(
                text = stringResource(R.string.leaderboard_xp_format, competitor.weeklyXp),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun SpeedDrillModalDialog(
    question: SpeedDrillQuestion,
    selectedOption: Int?,
    feedbackMessage: String?,
    isCorrect: Boolean?,
    onSelectOption: (Int) -> Unit,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(26.dp))
                .testTag("speed_drill_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EdamMascot(
                            expression = when (isCorrect) {
                                true -> EdamExpression.SUCCESS
                                false -> EdamExpression.ENCOURAGEMENT
                                null -> EdamExpression.CURIOUS
                            },
                            character = EdamCompanionCharacter.NOVA,
                            size = 48.dp,
                            showTablet = false
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.drill_modal_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "+${question.xpReward} XP BOUNTY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_drill_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = question.question,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 4 Options
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.options.forEachIndexed { optIdx, optText ->
                        val isSelected = selectedOption == optIdx
                        val isAnswerGiven = selectedOption != null

                        val optColor = when {
                            !isAnswerGiven -> MaterialTheme.colorScheme.surfaceVariant
                            optIdx == question.correctIndex -> Color(0xFF22C55E).copy(alpha = 0.25f)
                            isSelected && optIdx != question.correctIndex -> Color(0xFFEF4444).copy(alpha = 0.25f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }

                        val borderColor = when {
                            !isAnswerGiven && isSelected -> MaterialTheme.colorScheme.primary
                            isAnswerGiven && optIdx == question.correctIndex -> Color(0xFF22C55E)
                            isAnswerGiven && isSelected && optIdx != question.correctIndex -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = selectedOption == null) {
                                    onSelectOption(optIdx)
                                }
                                .testTag("drill_option_$optIdx"),
                            shape = RoundedCornerShape(14.dp),
                            color = optColor,
                            border = BorderStroke(1.2.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(borderColor.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${('A'.code + optIdx).toChar()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = optText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Feedback box
                if (!feedbackMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCorrect == true) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (isCorrect == true) Color(0xFF22C55E) else Color(0xFFEF4444))
                    ) {
                        Text(
                            text = feedbackMessage,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isCorrect == true) Color(0xFF15803D) else Color(0xFFB91C1C),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Close Button
                Button(
                    onClick = onClose,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.drill_btn_close))
                }
            }
        }
    }
}
