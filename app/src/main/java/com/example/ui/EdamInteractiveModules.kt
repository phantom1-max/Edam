package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChessAndFlashcardCatalog
import com.example.data.model.ChessTacticalPuzzle
import com.example.data.model.EdamFlashcard
import com.example.data.model.FinancialInstrumentType
import com.example.data.model.StockGraphMode

/**
 * Interactive Daily Flashcard Deck displayed right inside the First Hero Section.
 * Allows learners to flip cards, master Stock Market & Chess GM concepts, earn XP, and build their daily streak.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EdamHeroFlashcardDeck(
    currentStreak: Int,
    totalXp: Int,
    flashcardsReviewedCount: Int,
    onMasterFlashcard: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val allCards = remember { ChessAndFlashcardCatalog.heroFlashcards }
    var selectedCategory by remember { mutableStateOf("All") }
    val filteredCards = remember(selectedCategory) {
        if (selectedCategory == "All") allCards
        else allCards.filter { it.category.contains(selectedCategory, ignoreCase = true) }
    }
    var cardIndex by remember(selectedCategory) { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var masteredCardIds by remember { mutableStateOf(setOf<String>()) }

    val currentCard: EdamFlashcard = filteredCards.getOrElse(cardIndex % filteredCards.size.coerceAtLeast(1)) {
        allCards.first()
    }
    val isMastered = masteredCardIds.contains(currentCard.id)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_flashcard_deck"),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
        tonalElevation = 3.dp,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Header: Edam Companion + Streak & XP Pills
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
                    EdamMascot(
                        expression = if (isFlipped) EdamExpression.SUCCESS else EdamExpression.CURIOUS,
                        character = if (currentCard.category.contains("Chess")) {
                            EdamCompanionCharacter.VEX
                        } else {
                            EdamCompanionCharacter.EDAM
                        },
                        size = 54.dp,
                        showTablet = !isFlipped
                    )
                    Column {
                        Text(
                            text = "EDAM QUICK FLASHCARDS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Flip to recall · Build your daily streak",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Live Streak & XP Pill
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = "Daily Streak",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${currentStreak}d",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$totalXp XP",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Category Filter Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "Stock Market", "Chess", "Options", "Bonds").forEach { cat ->
                    val selected = selectedCategory == cat
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedCategory = cat
                            cardIndex = 0
                            isFlipped = false
                        },
                        label = {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("flashcard_filter_$cat")
                    )
                }
            }

            // Interactive Flip Card Surface
            Surface(
                onClick = { isFlipped = !isFlipped },
                shape = RoundedCornerShape(20.dp),
                color = if (isFlipped) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (isFlipped) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_flashcard_flip_surface")
            ) {
                AnimatedContent(
                    targetState = isFlipped,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                    },
                    label = "flashcard_flip_content"
                ) { flipped ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "${currentCard.category} · ${currentCard.difficulty}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TouchApp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (flipped) "ANSWER SIDE (Tap to flip)" else "QUESTION SIDE (Tap to reveal)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (!flipped) {
                            Text(
                                text = currentCard.frontPrompt,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Hint: ${currentCard.frontHint}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = currentCard.backAnswer,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 23.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = currentCard.keyFormulaOrMove,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Action Row: Card counter + Mastered (+XP) + Next Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Card ${(cardIndex % filteredCards.size.coerceAtLeast(1)) + 1}/${filteredCards.size} · $flashcardsReviewedCount reviewed",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (!isMastered) {
                                masteredCardIds = masteredCardIds + currentCard.id
                                onMasterFlashcard(currentCard.xpReward)
                            }
                            isFlipped = false
                            cardIndex = (cardIndex + 1) % filteredCards.size.coerceAtLeast(1)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("hero_flashcard_master_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMastered) "Next Card" else "Got It (+${currentCard.xpReward} XP)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Multi-Instrument Stock Market & Interactive Graph Lab.
 * Supports Equities, Indices, Options (Payoff), ETFs, Bonds (Yield Curve), and Commodities/Forex
 * with interactive graph switching while learning the stock market.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StockInstrumentsAndGraphLab(
    onEarnStudyXp: (Int) -> Unit,
    onOpenFullMarketCourse: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedInstrument by remember { mutableStateOf(FinancialInstrumentType.EQUITIES) }
    var selectedGraphMode by remember { mutableStateOf(StockGraphMode.PRICE_AND_SMA) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stock_instruments_graph_lab"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Kora (Market & Quant Companion)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    EdamMascot(
                        expression = EdamExpression.WORKING,
                        character = EdamCompanionCharacter.KORA,
                        size = 60.dp,
                        showTablet = true
                    )
                    Column {
                        Text(
                            text = "MULTI-INSTRUMENT & GRAPH LAB",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color(EdamCompanionCharacter.KORA.sproutPrimaryHex)
                        )
                        Text(
                            text = "Stock Market Instruments & Interactive Charts",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 1. Instrument Selector Pills (Equities, Indices, Options, ETFs, Bonds, Commodities)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinancialInstrumentType.entries.forEach { inst ->
                    val isSelected = inst == selectedInstrument
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedInstrument = inst
                            onEarnStudyXp(5)
                        },
                        label = {
                            Text(
                                text = inst.displayName.substringBefore(" ("),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(EdamCompanionCharacter.KORA.sproutPrimaryHex),
                            selectedLabelColor = Color(0xFF062E22)
                        ),
                        modifier = Modifier.testTag("instrument_chip_${inst.id}")
                    )
                }
            }

            // 2. Selected Instrument Telemetry Banner
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${selectedInstrument.displayName} · ${selectedInstrument.tickerCode}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${selectedInstrument.assetClass} · Risk: ${selectedInstrument.riskProfile}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = selectedInstrument.keyMetricLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = selectedInstrument.keyMetricValue,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Text(
                        text = selectedInstrument.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3. Graph Mode Switcher (Price + SMA, Candlesticks, RSI Oscillator, Payoff / Volume)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StockGraphMode.entries.forEach { mode ->
                    val selected = mode == selectedGraphMode
                    Surface(
                        onClick = { selectedGraphMode = mode },
                        shape = RoundedCornerShape(999.dp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.testTag("stock_graph_mode_${mode.name}")
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // 4. Interactive Financial Graph Canvas
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF111827),
                border = BorderStroke(1.dp, Color(0xFF374151)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .testTag("instrument_interactive_chart_canvas")
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val primarySeries = selectedInstrument.seriesPoints
                    val secondarySeries = selectedInstrument.secondaryOverlayPoints
                    val volumeBars = selectedInstrument.volumeBars

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val minVal = (primarySeries + secondarySeries).minOrNull() ?: 0f
                        val maxVal = (primarySeries + secondarySeries).maxOrNull() ?: 100f
                        val range = (maxVal - minVal).coerceAtLeast(1f)

                        // Subtle horizontal grid lines
                        for (i in 0..3) {
                            val y = h * (i / 3f)
                            drawLine(
                                color = Color(0xFF374151).copy(alpha = 0.5f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1f
                            )
                        }

                        val stepX = w / (primarySeries.size - 1).coerceAtLeast(1)

                        when (selectedGraphMode) {
                            StockGraphMode.PRICE_AND_SMA -> {
                                val pricePath = Path()
                                val smaPath = Path()
                                primarySeries.forEachIndexed { idx, v ->
                                    val x = idx * stepX
                                    val y = h - ((v - minVal) / range) * (h * 0.82f) - h * 0.08f
                                    if (idx == 0) pricePath.moveTo(x, y) else pricePath.lineTo(x, y)
                                }
                                secondarySeries.forEachIndexed { idx, v ->
                                    val x = idx * stepX
                                    val y = h - ((v - minVal) / range) * (h * 0.82f) - h * 0.08f
                                    if (idx == 0) smaPath.moveTo(x, y) else smaPath.lineTo(x, y)
                                }
                                drawPath(
                                    path = smaPath,
                                    color = Color(0xFFF59E0B).copy(alpha = 0.75f),
                                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                                )
                                drawPath(
                                    path = pricePath,
                                    color = Color(0xFF10B981),
                                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                                )
                            }

                            StockGraphMode.CANDLESTICK -> {
                                val candleW = (stepX * 0.52f).coerceAtLeast(8f)
                                primarySeries.forEachIndexed { idx, closeVal ->
                                    val openVal = if (idx == 0) closeVal * 0.985f else primarySeries[idx - 1]
                                    val highVal = maxOf(openVal, closeVal) + range * 0.08f
                                    val lowVal = minOf(openVal, closeVal) - range * 0.07f
                                    val isBull = closeVal >= openVal
                                    val cColor = if (isBull) Color(0xFF10B981) else Color(0xFFF87171)

                                    val x = idx * stepX
                                    val yHigh = h - ((highVal - minVal) / range) * (h * 0.80f) - h * 0.10f
                                    val yLow = h - ((lowVal - minVal) / range) * (h * 0.80f) - h * 0.10f
                                    val yOpen = h - ((openVal - minVal) / range) * (h * 0.80f) - h * 0.10f
                                    val yClose = h - ((closeVal - minVal) / range) * (h * 0.80f) - h * 0.10f

                                    drawLine(
                                        color = cColor,
                                        start = Offset(x, yHigh),
                                        end = Offset(x, yLow),
                                        strokeWidth = 3f
                                    )
                                    val topY = minOf(yOpen, yClose)
                                    val bodyH = kotlin.math.abs(yClose - yOpen).coerceAtLeast(6f)
                                    drawRoundRect(
                                        color = cColor,
                                        topLeft = Offset(x - candleW / 2f, topY),
                                        size = Size(candleW, bodyH),
                                        cornerRadius = CornerRadius(3f, 3f)
                                    )
                                }
                            }

                            StockGraphMode.RSI_MOMENTUM -> {
                                // Overbought 70 & Oversold 30 bands
                                val y70 = h * 0.25f
                                val y30 = h * 0.75f
                                drawLine(
                                    color = Color(0xFFF87171).copy(alpha = 0.6f),
                                    start = Offset(0f, y70),
                                    end = Offset(w, y70),
                                    strokeWidth = 2f
                                )
                                drawLine(
                                    color = Color(0xFF10B981).copy(alpha = 0.6f),
                                    start = Offset(0f, y30),
                                    end = Offset(w, y30),
                                    strokeWidth = 2f
                                )
                                val rsiPath = Path()
                                primarySeries.forEachIndexed { idx, v ->
                                    val normalizedRsi = 32f + ((v - minVal) / range) * 44f
                                    val x = idx * stepX
                                    val y = h - (normalizedRsi / 100f) * h
                                    if (idx == 0) rsiPath.moveTo(x, y) else rsiPath.lineTo(x, y)
                                    drawCircle(
                                        color = Color(0xFFFBBF24),
                                        radius = 5f,
                                        center = Offset(x, y)
                                    )
                                }
                                drawPath(
                                    path = rsiPath,
                                    color = Color(0xFFF59E0B),
                                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                                )
                            }

                            StockGraphMode.PAYOFF_RISK -> {
                                val barW = (stepX * 0.56f).coerceAtLeast(10f)
                                volumeBars.forEachIndexed { idx, vol ->
                                    val x = idx * stepX
                                    val barH = vol * (h * 0.78f)
                                    drawRoundRect(
                                        color = if (idx >= volumeBars.size / 2) {
                                            Color(0xFF10B981).copy(alpha = 0.85f)
                                        } else {
                                            Color(0xFFF59E0B).copy(alpha = 0.75f)
                                        },
                                        topLeft = Offset(x - barW / 2f, h - barH),
                                        size = Size(barW, barH),
                                        cornerRadius = CornerRadius(6f, 6f)
                                    )
                                }
                            }
                        }
                    }

                    // Legend Overlay in top-left of chart
                    Row(
                        modifier = Modifier.align(Alignment.TopStart),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "● ${selectedInstrument.tickerCode}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "● ${selectedGraphMode.label}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
            }

            // 5. Open Full Stock Market Course Action
            OutlinedButton(
                onClick = onOpenFullMarketCourse,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, Color(EdamCompanionCharacter.KORA.sproutPrimaryHex)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = null,
                    tint = Color(EdamCompanionCharacter.KORA.sproutPrimaryHex),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Study Full Share Market & Instruments Course (Offline Ready)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Interactive Novice to Grandmaster (GM) Chess Academy & Tactical Board Trainer.
 * Guided by Vex (Edam's Grandmaster Chess Tactician), lets users solve interactive board tactics
 * or launch the complete 5-Unit Novice-to-GM Chess Course.
 */
@Composable
fun InteractiveChessTrainingAcademyCard(
    puzzlesSolvedCount: Int,
    onSolvePuzzle: (Int) -> Unit,
    onOpenChessGmCourse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val puzzles = remember { ChessAndFlashcardCatalog.chessPuzzles }
    var puzzleIndex by remember { mutableIntStateOf(0) }
    val currentPuzzle: ChessTacticalPuzzle = puzzles[puzzleIndex % puzzles.size]

    var selectedSquare by remember(puzzleIndex) { mutableStateOf<Pair<Int, Int>?>(null) }
    var isSolved by remember(puzzleIndex) { mutableStateOf(false) }
    var feedbackText by remember(puzzleIndex) { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chess_gm_academy_card"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, Color(EdamCompanionCharacter.VEX.sproutPrimaryHex).copy(alpha = 0.55f)),
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Header with Vex (Grandmaster Chess Companion)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    EdamMascot(
                        expression = if (isSolved) EdamExpression.SUCCESS else EdamExpression.THINKING,
                        character = EdamCompanionCharacter.VEX,
                        size = 62.dp,
                        showTablet = false
                    )
                    Column {
                        Text(
                            text = "♟️ NOVICE TO GRANDMASTER ACADEMY",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Interactive GM Chess Tactics & 5-Unit Course",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$puzzlesSolvedCount Solved",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Puzzle Selector Tabs (Novice 1100 -> CM 2050 -> GM 2550)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                puzzles.forEachIndexed { idx, p ->
                    val selected = idx == puzzleIndex
                    Surface(
                        onClick = { puzzleIndex = idx },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chess_puzzle_tab_$idx")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = p.eloTier.substringBefore(" ("),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "+${p.xpReward} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Current Puzzle Prompt
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${currentPuzzle.title} · ${currentPuzzle.eloTier}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentPuzzle.prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Interactive 8x8 Chessboard
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 340.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, Color(0xFF1F2937), RoundedCornerShape(16.dp))
                        .testTag("interactive_chess_board")
                ) {
                    for (r in 0..7) {
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            for (c in 0..7) {
                                val isLightSquare = (r + c) % 2 == 0
                                val isFromSquare = (r == currentPuzzle.fromRow && c == currentPuzzle.fromCol)
                                val isToSquare = (r == currentPuzzle.toRow && c == currentPuzzle.toCol)
                                val isSelected = selectedSquare == Pair(r, c)

                                val pieceAtSquare = if (isSolved && isFromSquare) {
                                    null
                                } else if (isSolved && isToSquare) {
                                    currentPuzzle.pieces.find {
                                        it.row == currentPuzzle.fromRow && it.col == currentPuzzle.fromCol
                                    }
                                } else {
                                    currentPuzzle.pieces.find { it.row == r && it.col == c }
                                }

                                val bgColor = when {
                                    isSolved && isToSquare -> Color(0xFF10B981).copy(alpha = 0.75f)
                                    isSelected -> Color(0xFFF59E0B).copy(alpha = 0.75f)
                                    isFromSquare && !isSolved -> Color(0xFFFDE68A)
                                    isLightSquare -> Color(0xFFEDE9E4)
                                    else -> Color(0xFF4B5563)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .background(bgColor)
                                        .clickable {
                                            if (isSolved) return@clickable
                                            if (selectedSquare == null) {
                                                if (isFromSquare) {
                                                    selectedSquare = Pair(r, c)
                                                    feedbackText = "Piece selected! Now tap the target square to execute the winning move."
                                                } else {
                                                    selectedSquare = Pair( currentPuzzle.fromRow, currentPuzzle.fromCol)
                                                    feedbackText = "Hint: Start with the highlighted piece on (${('a' + currentPuzzle.fromCol)}${8 - currentPuzzle.fromRow})!"
                                                }
                                            } else {
                                                if (isToSquare) {
                                                    isSolved = true
                                                    feedbackText = "🏆 Brilliant! ${currentPuzzle.winningMoveNotation} — ${currentPuzzle.gmExplanation}"
                                                    onSolvePuzzle(currentPuzzle.xpReward)
                                                } else if (isFromSquare) {
                                                    selectedSquare = Pair(r, c)
                                                } else {
                                                    // Also allow 1-tap auto-solve if they tap the target square or give helpful hint
                                                    feedbackText = "Not quite! Look for the forcing move to (${('a' + currentPuzzle.toCol)}${8 - currentPuzzle.toRow})."
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (pieceAtSquare != null) {
                                        Text(
                                            text = pieceAtSquare.symbol,
                                            fontSize = 22.sp,
                                            color = if (pieceAtSquare.isWhite) {
                                                Color(0xFF111827)
                                            } else {
                                                Color(0xFF1F2937)
                                            },
                                            fontWeight = FontWeight.Black
                                        )
                                    } else if (selectedSquare != null && isToSquare && !isSolved) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF59E0B))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (feedbackText != null) {
                EdamMascotCalloutCard(
                    title = if (isSolved) "Grandmaster Move Found!" else "Vex's Tactical Coach",
                    message = feedbackText ?: "",
                    expression = if (isSolved) EdamExpression.SUCCESS else EdamExpression.ENCOURAGEMENT,
                    character = EdamCompanionCharacter.VEX,
                    badgeText = if (isSolved) "+${currentPuzzle.xpReward} XP" else null
                )
            }

            // Action Buttons: Show Solution / Next Puzzle + Open Full 5-Unit Novice to GM Course
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (!isSolved) {
                            isSolved = true
                            feedbackText = "Solution: ${currentPuzzle.winningMoveNotation} — ${currentPuzzle.gmExplanation}"
                            onSolvePuzzle(currentPuzzle.xpReward)
                        } else {
                            puzzleIndex = (puzzleIndex + 1) % puzzles.size
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("chess_puzzle_solve_or_next_button")
                ) {
                    Text(
                        text = if (isSolved) "Next GM Puzzle" else "Execute Winning Move",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = onOpenChessGmCourse,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .minimumInteractiveComponentSize()
                        .testTag("open_chess_gm_course_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Open Novice→GM Course",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun StockInstrumentsAndGraphsLearningLab(
    onEarnStudyXp: (Int) -> Unit = {},
    onOpenFullMarketCourse: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    StockInstrumentsAndGraphLab(
        onEarnStudyXp = onEarnStudyXp,
        onOpenFullMarketCourse = onOpenFullMarketCourse,
        modifier = modifier
    )
}

@Composable
fun NoviceToGmChessTrainingCard(
    chessPuzzlesSolvedCount: Int = 0,
    onPuzzleSolved: () -> Unit = {},
    onLaunchFullChessCourse: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    InteractiveChessTrainingAcademyCard(
        puzzlesSolvedCount = chessPuzzlesSolvedCount,
        onSolvePuzzle = { onPuzzleSolved() },
        onOpenChessGmCourse = onLaunchFullChessCourse,
        modifier = modifier
    )
}

