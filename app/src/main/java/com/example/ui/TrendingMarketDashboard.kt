package com.example.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CourseCandle
import com.example.data.model.MarketChartType
import com.example.data.model.MarketFilterTab
import com.example.data.model.MarketGlobalSummary
import com.example.data.model.MarketTimeframe
import com.example.data.model.TrendingCourseTicker
import com.example.ui.theme.LocalEdamThemeSpec
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
private fun rememberBullishColor(isDark: Boolean, isHighContrast: Boolean): Color {
    return when {
        isHighContrast && isDark -> Color(0xFF00FF87)
        isHighContrast && !isDark -> Color(0xFF006B2D)
        isDark -> Color(0xFF22C55E)
        else -> Color(0xFF15803D)
    }
}

@Composable
private fun rememberBearishColor(isDark: Boolean, isHighContrast: Boolean): Color {
    return when {
        isHighContrast && isDark -> Color(0xFFFF4D6D)
        isHighContrast && !isDark -> Color(0xFFB00020)
        isDark -> Color(0xFFF87171)
        else -> Color(0xFFDC2626)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrendingCoursesMarketDashboard(
    tickers: List<TrendingCourseTicker>,
    selectedTickerId: String,
    timeframe: MarketTimeframe,
    chartType: MarketChartType,
    filterTab: MarketFilterTab,
    searchQuery: String,
    bookmarkedTickerIds: Set<String>,
    isLiveFeedActive: Boolean,
    globalSummary: MarketGlobalSummary,
    isExpanded: Boolean,
    onSelectTicker: (String) -> Unit,
    onSelectTimeframe: (MarketTimeframe) -> Unit,
    onSelectChartType: (MarketChartType) -> Unit,
    onSelectFilterTab: (MarketFilterTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleBookmarkTicker: (String) -> Unit,
    onToggleLiveFeed: () -> Unit,
    onLaunchTickerCourse: (TrendingCourseTicker) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalEdamThemeSpec.current
    val bullishColor = rememberBullishColor(themeSpec.isDark, themeSpec.isHighContrast)
    val bearishColor = rememberBearishColor(themeSpec.isDark, themeSpec.isHighContrast)

    val selectedTicker = remember(tickers, selectedTickerId) {
        tickers.find { it.id == selectedTickerId } ?: tickers.firstOrNull()
    }

    val filteredTickers = remember(tickers, filterTab, searchQuery, bookmarkedTickerIds) {
        val tabFiltered = when (filterTab) {
            MarketFilterTab.ALL -> tickers
            MarketFilterTab.BOOKMARKED -> tickers.filter { bookmarkedTickerIds.contains(it.id) }
            MarketFilterTab.TOP_GAINERS -> tickers.sortedByDescending { it.percentChange }
            MarketFilterTab.MOST_ACTIVE -> tickers.sortedByDescending { it.activeLearners }
            MarketFilterTab.MY_COURSES -> tickers.filter { it.isUserCourse }
        }
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            tabFiltered
        } else {
            tabFiltered.filter { ticker ->
                ticker.symbol.contains(query, ignoreCase = true) ||
                    ticker.title.contains(query, ignoreCase = true) ||
                    ticker.category.contains(query, ignoreCase = true) ||
                    ticker.level.contains(query, ignoreCase = true) ||
                    ticker.goal.contains(query, ignoreCase = true)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trending_courses_market_dashboard"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (themeSpec.isHighContrast) 0.dp else 3.dp,
        shadowElevation = if (themeSpec.isHighContrast) 0.dp else 10.dp,
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isExpanded) 28.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Exchange Terminal Header + Live Feed Toggle
            MarketDashboardHeader(
                isLiveFeedActive = isLiveFeedActive,
                bullishColor = bullishColor,
                onToggleLiveFeed = onToggleLiveFeed
            )

            // 2. Horizontal Live Ticker Tape
            MarketTickerTapeStrip(
                tickers = tickers,
                selectedTickerId = selectedTicker?.id,
                bookmarkedTickerIds = bookmarkedTickerIds,
                bullishColor = bullishColor,
                bearishColor = bearishColor,
                onSelectTicker = onSelectTicker
            )

            // 3. Global Market Index Bento Row (EDAM-100, 24h Active Learners, Avg Quiz Yield)
            MarketGlobalSummaryStrip(
                summary = globalSummary,
                isExpanded = isExpanded,
                bullishColor = bullishColor,
                bearishColor = bearishColor
            )

            // 4. Primary Interactive Financial Chart Terminal for Selected Course
            if (selectedTicker != null) {
                SelectedTickerChartTerminal(
                    ticker = selectedTicker,
                    isBookmarked = bookmarkedTickerIds.contains(selectedTicker.id),
                    timeframe = timeframe,
                    chartType = chartType,
                    isLiveFeedActive = isLiveFeedActive,
                    bullishColor = bullishColor,
                    bearishColor = bearishColor,
                    isExpanded = isExpanded,
                    onToggleBookmark = { onToggleBookmarkTicker(selectedTicker.id) },
                    onSelectTimeframe = onSelectTimeframe,
                    onSelectChartType = onSelectChartType,
                    onLaunchTickerCourse = { onLaunchTickerCourse(selectedTicker) }
                )
            }

            // 5. Watchlist Search Bar, Filter Tabs & Course Market Movers Table
            TrendingWatchlistSection(
                tickers = filteredTickers,
                selectedTickerId = selectedTicker?.id,
                activeFilter = filterTab,
                searchQuery = searchQuery,
                bookmarkedTickerIds = bookmarkedTickerIds,
                bullishColor = bullishColor,
                bearishColor = bearishColor,
                isExpanded = isExpanded,
                onSearchQueryChange = onSearchQueryChange,
                onSelectFilterTab = onSelectFilterTab,
                onSelectTicker = onSelectTicker,
                onToggleBookmarkTicker = onToggleBookmarkTicker,
                onLaunchTickerCourse = onLaunchTickerCourse
            )
        }
    }
}

@Composable
private fun MarketDashboardHeader(
    isLiveFeedActive: Boolean,
    bullishColor: Color,
    onToggleLiveFeed: () -> Unit
) {
    val themeSpec = LocalEdamThemeSpec.current
    val infiniteTransition = rememberInfiniteTransition(label = "market_live_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.market_exchange_eyebrow),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Live Stream Toggle Chip
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (isLiveFeedActive) {
                    bullishColor.copy(alpha = 0.14f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    width = themeSpec.borderWidth,
                    color = if (isLiveFeedActive) bullishColor else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onToggleLiveFeed)
                    .minimumInteractiveComponentSize()
                    .testTag("market_live_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isLiveFeedActive) {
                                    bullishColor.copy(alpha = pulseAlpha)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                    )
                    Text(
                        text = if (isLiveFeedActive) {
                            stringResource(R.string.market_live_badge)
                        } else {
                            stringResource(R.string.market_paused_badge)
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (isLiveFeedActive) {
                            bullishColor
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Icon(
                        imageVector = if (isLiveFeedActive) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isLiveFeedActive) "Pause real-time feed" else "Resume real-time feed",
                        tint = if (isLiveFeedActive) bullishColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.market_dashboard_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(R.string.market_dashboard_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MarketTickerTapeStrip(
    tickers: List<TrendingCourseTicker>,
    selectedTickerId: String?,
    bookmarkedTickerIds: Set<String>,
    bullishColor: Color,
    bearishColor: Color,
    onSelectTicker: (String) -> Unit
) {
    val themeSpec = LocalEdamThemeSpec.current
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .testTag("market_ticker_tape"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tickers.forEach { ticker ->
            val isSelected = ticker.id == selectedTickerId
            val isBookmarked = bookmarkedTickerIds.contains(ticker.id)
            val trendColor = if (ticker.isPositive) bullishColor else bearishColor
            val sign = if (ticker.isPositive) "+" else ""
            val arrow = if (ticker.isPositive) "▲" else "▼"

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    width = if (isSelected) 1.8.dp else themeSpec.borderWidth,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectTicker(ticker.id) }
                    .minimumInteractiveComponentSize()
                    .testTag("ticker_tape_chip_${ticker.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isBookmarked) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = ticker.symbol,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f", ticker.currentIndex),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = String.format(Locale.US, "%s%.2f%% %s", sign, ticker.percentChange, arrow),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = trendColor
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketGlobalSummaryStrip(
    summary: MarketGlobalSummary,
    isExpanded: Boolean,
    bullishColor: Color,
    bearishColor: Color
) {
    val isCompositeUp = summary.compositeChangePct >= 0f
    val compositeColor = if (isCompositeUp) bullishColor else bearishColor
    val compositeSign = if (isCompositeUp) "+" else ""

    if (isExpanded) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("market_global_summary_row"),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MarketMetricBentoCard(
                label = stringResource(R.string.market_index_edam100),
                primaryValue = String.format(Locale.US, "%,.2f", summary.compositeIndexValue),
                subValue = String.format(
                    Locale.US,
                    "%s%.2f%% (%d ADV / %d DEC)",
                    compositeSign,
                    summary.compositeChangePct,
                    summary.advancingCount,
                    summary.decliningCount
                ),
                accentColor = compositeColor,
                modifier = Modifier.weight(1f)
            )
            MarketMetricBentoCard(
                label = stringResource(R.string.market_active_learners_24h),
                primaryValue = String.format(Locale.US, "%,d", summary.totalActiveLearners),
                subValue = "Concurrent study & quiz sessions",
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            MarketMetricBentoCard(
                label = stringResource(R.string.market_quiz_yield_avg),
                primaryValue = String.format(Locale.US, "%.1f%%", summary.avgQuizEngagementPct),
                subValue = "Practice accuracy & retention velocity",
                accentColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("market_global_summary_column"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MarketMetricBentoCard(
                label = stringResource(R.string.market_index_edam100),
                primaryValue = String.format(Locale.US, "%,.2f", summary.compositeIndexValue),
                subValue = String.format(
                    Locale.US,
                    "%s%.2f%% · %d Advancing / %d Cooling",
                    compositeSign,
                    summary.compositeChangePct,
                    summary.advancingCount,
                    summary.decliningCount
                ),
                accentColor = compositeColor,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MarketMetricBentoCard(
                    label = stringResource(R.string.market_active_learners_24h),
                    primaryValue = String.format(Locale.US, "%,d", summary.totalActiveLearners),
                    subValue = "Live learner volume",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MarketMetricBentoCard(
                    label = stringResource(R.string.market_quiz_yield_avg),
                    primaryValue = String.format(Locale.US, "%.1f%%", summary.avgQuizEngagementPct),
                    subValue = "Quiz pass velocity",
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MarketMetricBentoCard(
    label: String,
    primaryValue: String,
    subValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val themeSpec = LocalEdamThemeSpec.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    letterSpacing = 0.6.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = primaryValue,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subValue,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectedTickerChartTerminal(
    ticker: TrendingCourseTicker,
    isBookmarked: Boolean,
    timeframe: MarketTimeframe,
    chartType: MarketChartType,
    isLiveFeedActive: Boolean,
    bullishColor: Color,
    bearishColor: Color,
    isExpanded: Boolean,
    onToggleBookmark: () -> Unit,
    onSelectTimeframe: (MarketTimeframe) -> Unit,
    onSelectChartType: (MarketChartType) -> Unit,
    onLaunchTickerCourse: () -> Unit
) {
    val themeSpec = LocalEdamThemeSpec.current
    var scrubbedIndex by remember(ticker.id, timeframe) { mutableStateOf<Int?>(null) }

    val displayedCandles = remember(ticker.candles, timeframe) {
        ticker.candles.takeLast(timeframe.candleCount)
    }

    val baselineOpen = remember(displayedCandles, ticker.previousClose) {
        displayedCandles.firstOrNull()?.open ?: ticker.previousClose
    }

    val activeCandle = remember(displayedCandles, scrubbedIndex) {
        val idx = scrubbedIndex
        if (idx != null && idx in displayedCandles.indices) {
            displayedCandles[idx]
        } else {
            displayedCandles.lastOrNull()
        }
    }

    val activeValue = activeCandle?.close ?: ticker.currentIndex
    val periodDelta = activeValue - baselineOpen
    val periodPct = if (baselineOpen <= 0.01f) 0f else (periodDelta / baselineOpen) * 100f
    val isPositive = periodDelta >= 0f
    val trendColor = if (isPositive) bullishColor else bearishColor
    val sign = if (isPositive) "+" else ""
    val arrow = if (isPositive) "▲" else "▼"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("selected_ticker_chart_terminal"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (themeSpec.isHighContrast) 2.dp else 1.2.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isExpanded) 22.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row: Ticker Symbol, Title, Level, Momentum Signal + Bookmark + Price / Growth Index Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = ticker.symbol,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${ticker.category} · ${ticker.level}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = trendColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = ticker.momentumSignal,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = trendColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = ticker.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isExpanded) 22.sp else 19.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = ticker.goal,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Right Column: Bookmark Button + Engagement Index Price & Change
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isBookmarked) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .testTag("bookmark_selected_ticker_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) {
                                    Icons.Filled.Bookmark
                                } else {
                                    Icons.Filled.BookmarkBorder
                                },
                                contentDescription = if (isBookmarked) {
                                    stringResource(R.string.bookmark_remove_desc)
                                } else {
                                    stringResource(R.string.bookmark_add_desc)
                                },
                                tint = if (isBookmarked) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        Text(
                            text = String.format(Locale.US, "%.2f", activeValue),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (isExpanded) 30.sp else 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("selected_ticker_index_value")
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = trendColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = String.format(
                                Locale.US,
                                "%s%.2f (%s%.2f%%) %s",
                                sign,
                                periodDelta,
                                sign,
                                periodPct,
                                arrow
                            ),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = trendColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = if (scrubbedIndex != null && activeCandle != null) {
                            "Candle ${activeCandle.label} · Vol ${activeCandle.learnerVolume}"
                        } else {
                            timeframe.description
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Chart Controls Bar: Timeframe Buttons (1H, 24H, 7D, 30D) + Chart Type Toggle (Growth Curve vs OHLC Candles)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MarketTimeframe.entries.forEach { tf ->
                        val isSelected = tf == timeframe
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = BorderStroke(
                                width = themeSpec.borderWidth,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    scrubbedIndex = null
                                    onSelectTimeframe(tf)
                                }
                                .minimumInteractiveComponentSize()
                                .testTag("market_timeframe_${tf.name}")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = tf.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MarketChartType.entries.forEach { type ->
                        val isSelected = type == chartType
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = BorderStroke(
                                width = themeSpec.borderWidth,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectChartType(type) }
                                .minimumInteractiveComponentSize()
                                .testTag("market_chart_type_${type.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (type == MarketChartType.AREA_LINE) {
                                        Icons.Filled.Timeline
                                    } else {
                                        Icons.Filled.BarChart
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = type.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Selected / Scrubbed OHLC Telemetry Bar
            if (activeCandle != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OhlcStatItem("OPEN", String.format(Locale.US, "%.2f", activeCandle.open))
                        OhlcStatItem("HIGH", String.format(Locale.US, "%.2f", activeCandle.high))
                        OhlcStatItem("LOW", String.format(Locale.US, "%.2f", activeCandle.low))
                        OhlcStatItem("CLOSE", String.format(Locale.US, "%.2f", activeCandle.close))
                        OhlcStatItem("VOL", String.format(Locale.US, "%,d", activeCandle.learnerVolume))
                    }
                }
            }

            // Interactive Financial Chart Canvas (Supports Area Growth Curve & OHLC Candlesticks + Volume Bars)
            FinancialMarketChartCanvas(
                candles = displayedCandles,
                chartType = chartType,
                isPositive = isPositive,
                isLiveFeedActive = isLiveFeedActive,
                bullishColor = bullishColor,
                bearishColor = bearishColor,
                scrubbedIndex = scrubbedIndex,
                onScrubIndexChange = { scrubbedIndex = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isExpanded) 250.dp else 210.dp)
                    .testTag("financial_market_chart_canvas")
            )

            Text(
                text = stringResource(R.string.market_scrub_hint),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Course Fundamentals Grid (Active Learners, Quiz Velocity, Completion Yield, 24h High/Low)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FundamentalStatPill(
                    label = "ACTIVE LEARNERS",
                    value = String.format(Locale.US, "%,d", ticker.activeLearners),
                    modifier = Modifier.weight(1f)
                )
                FundamentalStatPill(
                    label = "QUIZ VELOCITY",
                    value = "${String.format(Locale.US, "%,d", ticker.quizVelocityPerHr)}/hr",
                    modifier = Modifier.weight(1f)
                )
                FundamentalStatPill(
                    label = "PASS YIELD",
                    value = String.format(Locale.US, "%.1f%%", ticker.completionYieldPct),
                    modifier = Modifier.weight(1f)
                )
                if (isExpanded) {
                    FundamentalStatPill(
                        label = "24H RANGE",
                        value = String.format(Locale.US, "%.1f–%.1f", ticker.low24h, ticker.high24h),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Action CTA Button to Open or Build the Selected Course
            Button(
                onClick = onLaunchTickerCourse,
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("market_launch_selected_course_button")
            ) {
                Icon(
                    imageVector = if (ticker.linkedCourseId != null) {
                        Icons.Filled.OfflinePin
                    } else {
                        Icons.Filled.Bolt
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (ticker.linkedCourseId != null) {
                        stringResource(R.string.market_btn_open_saved_course)
                    } else {
                        "${stringResource(R.string.market_btn_build_trending_course)}: ${ticker.title}"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun OhlcStatItem(
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FundamentalStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FinancialMarketChartCanvas(
    candles: List<CourseCandle>,
    chartType: MarketChartType,
    isPositive: Boolean,
    isLiveFeedActive: Boolean,
    bullishColor: Color,
    bearishColor: Color,
    scrubbedIndex: Int?,
    onScrubIndexChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val crosshairColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
    val volumeNeutralColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    val activeTrendColor = if (isPositive) bullishColor else bearishColor

    val infiniteTransition = rememberInfiniteTransition(label = "chart_beacon")
    val beaconRadiusFactor by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_radius"
    )

    val chartDesc = "Financial course growth and learner engagement chart with ${candles.size} intervals"

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .semantics { contentDescription = chartDesc }
            .pointerInput(candles.size) {
                detectTapGestures(
                    onPress = { offset ->
                        if (candles.isNotEmpty() && size.width > 0) {
                            val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            val idx = (fraction * (candles.size - 1)).roundToInt().coerceIn(candles.indices)
                            onScrubIndexChange(idx)
                            tryAwaitRelease()
                            onScrubIndexChange(null)
                        }
                    }
                )
            }
            .pointerInput(candles.size) {
                detectHorizontalDragGestures(
                    onDragEnd = { onScrubIndexChange(null) },
                    onDragCancel = { onScrubIndexChange(null) },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        if (candles.isNotEmpty() && size.width > 0) {
                            val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                            val idx = (fraction * (candles.size - 1)).roundToInt().coerceIn(candles.indices)
                            onScrubIndexChange(idx)
                        }
                    }
                )
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        if (candles.isEmpty() || canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

        val horizontalPad = 14.dp.toPx()
        val topPad = 14.dp.toPx()
        val bottomPad = 10.dp.toPx()
        val usableWidth = (canvasWidth - horizontalPad * 2f).coerceAtLeast(1f)

        // Upper 72% for Price/Growth Index, Lower 22% for Learner Engagement Volume Bars
        val priceRegionHeight = (canvasHeight - topPad - bottomPad) * 0.72f
        val volumeRegionTop = topPad + priceRegionHeight + ((canvasHeight - topPad - bottomPad) * 0.06f)
        val volumeRegionHeight = (canvasHeight - volumeRegionTop - bottomPad).coerceAtLeast(1f)

        val minLow = candles.minOf { it.low }
        val maxHigh = candles.maxOf { it.high }
        val priceRange = (maxHigh - minLow).coerceAtLeast(1f)
        val maxVolume = candles.maxOf { it.learnerVolume }.coerceAtLeast(1)

        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

        // Draw 4 Horizontal Dashed Reference Gridlines
        for (g in 0..3) {
            val y = topPad + (priceRegionHeight * (g / 3f))
            drawLine(
                color = gridColor,
                start = Offset(horizontalPad, y),
                end = Offset(canvasWidth - horizontalPad, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )
        }

        // Divider between Price Area and Volume Bars
        drawLine(
            color = gridColor,
            start = Offset(horizontalPad, volumeRegionTop - 4.dp.toPx()),
            end = Offset(canvasWidth - horizontalPad, volumeRegionTop - 4.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )

        fun priceToY(value: Float): Float {
            val normalized = ((value - minLow) / priceRange).coerceIn(0f, 1f)
            return topPad + priceRegionHeight * (1f - normalized)
        }

        val stepX = if (candles.size > 1) usableWidth / (candles.size - 1).toFloat() else usableWidth

        // Draw Bottom Learner Engagement Volume Bars
        val barWidth = (usableWidth / candles.size.toFloat() * 0.56f).coerceIn(3.dp.toPx(), 16.dp.toPx())
        candles.forEachIndexed { index, candle ->
            val cx = horizontalPad + index * stepX
            val volRatio = (candle.learnerVolume.toFloat() / maxVolume.toFloat()).coerceIn(0.08f, 1f)
            val barH = volumeRegionHeight * volRatio
            val barColor = if (chartType == MarketChartType.CANDLESTICK) {
                if (candle.isBullish) bullishColor.copy(alpha = 0.45f) else bearishColor.copy(alpha = 0.45f)
            } else {
                volumeNeutralColor
            }
            drawRoundRect(
                color = barColor,
                topLeft = Offset(cx - barWidth / 2f, volumeRegionTop + volumeRegionHeight - barH),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }

        if (chartType == MarketChartType.AREA_LINE) {
            val linePath = Path()
            val fillPath = Path()

            candles.forEachIndexed { index, candle ->
                val x = horizontalPad + index * stepX
                val y = priceToY(candle.close)
                if (index == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, topPad + priceRegionHeight)
                    fillPath.lineTo(x, y)
                } else {
                    val prevX = horizontalPad + (index - 1) * stepX
                    val prevY = priceToY(candles[index - 1].close)
                    val controlX1 = (prevX + x) / 2f
                    linePath.cubicTo(controlX1, prevY, controlX1, y, x, y)
                    fillPath.cubicTo(controlX1, prevY, controlX1, y, x, y)
                }
                if (index == candles.lastIndex) {
                    fillPath.lineTo(x, topPad + priceRegionHeight)
                    fillPath.close()
                }
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        activeTrendColor.copy(alpha = 0.34f),
                        activeTrendColor.copy(alpha = 0.02f)
                    ),
                    startY = topPad,
                    endY = topPad + priceRegionHeight
                )
            )

            drawPath(
                path = linePath,
                color = activeTrendColor,
                style = Stroke(
                    width = 2.8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Pulsing live beacon on latest data point
            val lastX = horizontalPad + (candles.size - 1) * stepX
            val lastY = priceToY(candles.last().close)
            if (isLiveFeedActive) {
                drawCircle(
                    color = activeTrendColor.copy(alpha = 0.28f),
                    radius = 5.dp.toPx() * beaconRadiusFactor,
                    center = Offset(lastX, lastY)
                )
            }
            drawCircle(
                color = activeTrendColor,
                radius = 4.5.dp.toPx(),
                center = Offset(lastX, lastY)
            )
        } else {
            // OHLC Candlestick Mode
            val candleBodyWidth = (usableWidth / candles.size.toFloat() * 0.62f).coerceIn(4.dp.toPx(), 18.dp.toPx())
            candles.forEachIndexed { index, candle ->
                val cx = horizontalPad + index * stepX
                val highY = priceToY(candle.high)
                val lowY = priceToY(candle.low)
                val openY = priceToY(candle.open)
                val closeY = priceToY(candle.close)

                val cColor = if (candle.isBullish) bullishColor else bearishColor

                // High-Low Wick
                drawLine(
                    color = cColor,
                    start = Offset(cx, highY),
                    end = Offset(cx, lowY),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Open-Close Body
                val bodyTop = min(openY, closeY)
                val bodyBottom = max(openY, closeY)
                val bodyHeight = (bodyBottom - bodyTop).coerceAtLeast(3.dp.toPx())

                drawRoundRect(
                    color = cColor,
                    topLeft = Offset(cx - candleBodyWidth / 2f, bodyTop),
                    size = Size(candleBodyWidth, bodyHeight),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }

        // Interactive Scrub Crosshair
        if (scrubbedIndex != null && scrubbedIndex in candles.indices) {
            val sx = horizontalPad + scrubbedIndex * stepX
            val sy = priceToY(candles[scrubbedIndex].close)

            drawLine(
                color = crosshairColor,
                start = Offset(sx, topPad),
                end = Offset(sx, canvasHeight - bottomPad),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = dashEffect
            )
            drawLine(
                color = crosshairColor,
                start = Offset(horizontalPad, sy),
                end = Offset(canvasWidth - horizontalPad, sy),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )
            drawCircle(
                color = activeTrendColor,
                radius = 6.dp.toPx(),
                center = Offset(sx, sy)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrendingWatchlistSection(
    tickers: List<TrendingCourseTicker>,
    selectedTickerId: String?,
    activeFilter: MarketFilterTab,
    searchQuery: String,
    bookmarkedTickerIds: Set<String>,
    bullishColor: Color,
    bearishColor: Color,
    isExpanded: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectFilterTab: (MarketFilterTab) -> Unit,
    onSelectTicker: (String) -> Unit,
    onToggleBookmarkTicker: (String) -> Unit,
    onLaunchTickerCourse: (TrendingCourseTicker) -> Unit
) {
    val themeSpec = LocalEdamThemeSpec.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.market_watchlist_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.market_watchlist_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Market Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.market_search_placeholder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("market_search_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("market_search_input")
        )

        // Filter Tabs (including Bookmarked ★)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MarketFilterTab.entries.forEach { tab ->
                val isSelected = tab == activeFilter
                val labelText = if (tab == MarketFilterTab.BOOKMARKED && bookmarkedTickerIds.isNotEmpty()) {
                    "${tab.label} (${bookmarkedTickerIds.size})"
                } else {
                    tab.label
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(
                        width = themeSpec.borderWidth,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onSelectFilterTab(tab) }
                        .minimumInteractiveComponentSize()
                        .testTag("market_filter_tab_${tab.name}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = labelText,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }

        // Watchlist Rows or Friendly Empty State
        if (tickers.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("market_watchlist_empty_state"),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(themeSpec.borderWidth, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (activeFilter == MarketFilterTab.BOOKMARKED) {
                            Icons.Filled.BookmarkBorder
                        } else {
                            Icons.Filled.Search
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = if (activeFilter == MarketFilterTab.BOOKMARKED && searchQuery.isBlank()) {
                            stringResource(R.string.empty_bookmarks_tip)
                        } else {
                            stringResource(R.string.empty_search_tip)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                tickers.forEach { ticker ->
                    TrendingCourseWatchlistRow(
                        ticker = ticker,
                        isSelected = ticker.id == selectedTickerId,
                        isBookmarked = bookmarkedTickerIds.contains(ticker.id),
                        bullishColor = bullishColor,
                        bearishColor = bearishColor,
                        isExpanded = isExpanded,
                        onClick = { onSelectTicker(ticker.id) },
                        onToggleBookmark = { onToggleBookmarkTicker(ticker.id) },
                        onQuickAction = { onLaunchTickerCourse(ticker) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendingCourseWatchlistRow(
    ticker: TrendingCourseTicker,
    isSelected: Boolean,
    isBookmarked: Boolean,
    bullishColor: Color,
    bearishColor: Color,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onQuickAction: () -> Unit
) {
    val themeSpec = LocalEdamThemeSpec.current
    val trendColor = if (ticker.isPositive) bullishColor else bearishColor
    val sign = if (ticker.isPositive) "+" else ""
    val arrow = if (ticker.isPositive) "▲" else "▼"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag("watchlist_row_${ticker.id}"),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (themeSpec.isHighContrast) 1f else 0.45f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (isSelected) 2.dp else themeSpec.borderWidth,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bookmark IconButton
            IconButton(
                onClick = onToggleBookmark,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("bookmark_ticker_${ticker.id}")
            ) {
                Icon(
                    imageVector = if (isBookmarked) {
                        Icons.Filled.Bookmark
                    } else {
                        Icons.Filled.BookmarkBorder
                    },
                    contentDescription = if (isBookmarked) {
                        "Remove ${ticker.title} from bookmarks"
                    } else {
                        "Bookmark ${ticker.title}"
                    },
                    tint = if (isBookmarked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Left: Symbol Badge + Course Name & Learner Volume
            Column(
                modifier = Modifier.weight(1.35f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = ticker.symbol,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = ticker.level,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (ticker.linkedCourseId != null) {
                        Icon(
                            imageVector = Icons.Filled.OfflinePin,
                            contentDescription = "Offline Ready",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = ticker.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${String.format(Locale.US, "%,d", ticker.activeLearners)} learners · ${String.format(Locale.US, "%.1f%%", ticker.completionYieldPct)} yield",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Center: Real-time Sparkline Canvas
            SparklineCanvas(
                points = ticker.sparklinePoints,
                lineColor = trendColor,
                modifier = Modifier
                    .width(if (isExpanded) 104.dp else 64.dp)
                    .height(36.dp)
            )

            // Right: Index Score + % Change Pill
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%.2f", ticker.currentIndex),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = trendColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = String.format(Locale.US, "%s%.2f%% %s", sign, ticker.percentChange, arrow),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = trendColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SparklineCanvas(
    points: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.semantics {
            contentDescription = "Course engagement trend sparkline"
        }
    ) {
        val w = size.width
        val h = size.height
        if (points.size < 2 || w <= 0f || h <= 0f) return@Canvas

        val minVal = points.minOrNull() ?: 0f
        val maxVal = points.maxOrNull() ?: 1f
        val range = (maxVal - minVal).coerceAtLeast(0.5f)
        val padY = 4.dp.toPx()
        val usableH = (h - padY * 2f).coerceAtLeast(1f)
        val stepX = w / (points.size - 1).toFloat()

        val linePath = Path()
        val fillPath = Path()

        points.forEachIndexed { idx, value ->
            val x = idx * stepX
            val norm = ((value - minVal) / range).coerceIn(0f, 1f)
            val y = padY + usableH * (1f - norm)
            if (idx == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            if (idx == points.lastIndex) {
                fillPath.lineTo(x, h)
                fillPath.close()
            }
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.25f),
                    lineColor.copy(alpha = 0.0f)
                )
            )
        )

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
