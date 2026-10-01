package com.example.data.model

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

enum class MarketTimeframe(
    val label: String,
    val description: String,
    val candleCount: Int,
    val baselineLookback: Int
) {
    ONE_HOUR("1H", "Last 60m Live Ticks", 16, 6),
    ONE_DAY("24H", "24-Hour Intraday Session", 24, 14),
    SEVEN_DAYS("7D", "7-Day Weekly Momentum", 28, 22),
    THIRTY_DAYS("30D", "30-Day Macro Growth", 32, 30)
}

enum class MarketChartType(val label: String) {
    AREA_LINE("Growth Curve"),
    CANDLESTICK("OHLC Candles")
}

enum class MarketFilterTab(val label: String) {
    ALL("All Markets"),
    BOOKMARKED("Bookmarked ★"),
    TOP_GAINERS("Top Gainers ▲"),
    MOST_ACTIVE("High Volume"),
    MY_COURSES("My Courses")
}

data class CourseCandle(
    val label: String,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val learnerVolume: Int
) {
    val isBullish: Boolean
        get() = close >= open
}

data class TrendingCourseTicker(
    val id: String,
    val symbol: String,
    val title: String,
    val category: String,
    val level: String,
    val goal: String,
    val currentIndex: Float,
    val previousClose: Float,
    val high24h: Float,
    val low24h: Float,
    val activeLearners: Int,
    val completionYieldPct: Float,
    val quizVelocityPerHr: Int,
    val sparklinePoints: List<Float>,
    val candles: List<CourseCandle>,
    val isUserCourse: Boolean = false,
    val linkedCourseId: String? = null
) {
    val netChange: Float
        get() = currentIndex - previousClose

    val percentChange: Float
        get() = if (previousClose <= 0.01f) 0f else ((currentIndex - previousClose) / previousClose) * 100f

    val isPositive: Boolean
        get() = netChange >= 0f

    val momentumSignal: String
        get() = when {
            percentChange >= 8.0f -> "SURGING BREAKOUT ▲"
            percentChange >= 3.0f -> "STRONG DEMAND ▲"
            percentChange >= 0.0f -> "STEADY GROWTH ▲"
            percentChange >= -2.5f -> "CONSOLIDATING"
            else -> "DIP OPPORTUNITY ▼"
        }
}

data class MarketGlobalSummary(
    val compositeIndexValue: Float = 4892.40f,
    val compositeChangePct: Float = 5.42f,
    val totalActiveLearners: Int = 148_620,
    val avgQuizEngagementPct: Float = 92.4f,
    val advancingCount: Int = 5,
    val decliningCount: Int = 1
)

object TrendingMarketEngine {

    fun createInitialTickers(): List<TrendingCourseTicker> {
        return listOf(
            buildSeededTicker(
                id = "market_shmkt",
                symbol = "\$SHMKT",
                title = "Share Market & Equity Investing Mastery",
                category = "Equities & Technicals",
                level = "Intermediate",
                goal = "Master stock exchanges, fundamental valuation, candlestick price action, and risk management.",
                baseIndex = 242.80f,
                previousClose = 218.40f,
                activeLearners = 42_850,
                completionYieldPct = 96.4f,
                quizVelocityPerHr = 3_480,
                phaseSeed = 1.1f,
                trendSlope = 0.85f,
                isUserCourse = true,
                linkedCourseId = ShareMarketCatalog.SHARE_MARKET_COURSE_ID
            ),
            buildSeededTicker(
                id = "market_genai",
                symbol = "\$GENAI",
                title = "Applied Generative AI & LLM Systems",
                category = "AI Engineering",
                level = "Advanced",
                goal = "Build production RAG pipelines, multimodal agents, and structured prompt architectures.",
                baseIndex = 318.50f,
                previousClose = 289.10f,
                activeLearners = 38_190,
                completionYieldPct = 93.8f,
                quizVelocityPerHr = 3_120,
                phaseSeed = 2.4f,
                trendSlope = 1.05f
            ),
            buildSeededTicker(
                id = "market_pyqnt",
                symbol = "\$PYQNT",
                title = "Algorithmic Trading & Quantitative Python",
                category = "Quant Finance",
                level = "Advanced",
                goal = "Backtest statistical arbitrage, momentum factors, and Sharpe-optimal portfolios in Python.",
                baseIndex = 194.20f,
                previousClose = 181.60f,
                activeLearners = 24_640,
                completionYieldPct = 91.2f,
                quizVelocityPerHr = 1_980,
                phaseSeed = 3.7f,
                trendSlope = 0.55f
            ),
            buildSeededTicker(
                id = "market_optns",
                symbol = "\$OPTNS",
                title = "Options Greeks, Hedging & Volatility",
                category = "Derivatives",
                level = "Professional",
                goal = "Understand Delta, Gamma, Theta, implied volatility surfaces, and iron condor risk bounds.",
                baseIndex = 156.40f,
                previousClose = 160.10f,
                activeLearners = 19_310,
                completionYieldPct = 88.7f,
                quizVelocityPerHr = 1_540,
                phaseSeed = 4.9f,
                trendSlope = -0.18f
            ),
            buildSeededTicker(
                id = "market_sysds",
                symbol = "\$SYSDS",
                title = "High-Scale Distributed Systems Design",
                category = "Cloud Architecture",
                level = "University",
                goal = "Design low-latency consensus engines, sharded databases, and fault-tolerant event streams.",
                baseIndex = 212.90f,
                previousClose = 199.50f,
                activeLearners = 16_420,
                completionYieldPct = 92.1f,
                quizVelocityPerHr = 1_390,
                phaseSeed = 5.8f,
                trendSlope = 0.48f
            ),
            buildSeededTicker(
                id = "market_macro",
                symbol = "\$MACRO",
                title = "Global Macroeconomics & Central Banking",
                category = "Economics",
                level = "Beginner",
                goal = "Decode interest rate cycles, yield curves, inflation prints, and sovereign liquidity flows.",
                baseIndex = 128.75f,
                previousClose = 122.30f,
                activeLearners = 14_890,
                completionYieldPct = 94.0f,
                quizVelocityPerHr = 1_210,
                phaseSeed = 6.6f,
                trendSlope = 0.32f
            )
        )
    }

    private fun buildSeededTicker(
        id: String,
        symbol: String,
        title: String,
        category: String,
        level: String,
        goal: String,
        baseIndex: Float,
        previousClose: Float,
        activeLearners: Int,
        completionYieldPct: Float,
        quizVelocityPerHr: Int,
        phaseSeed: Float,
        trendSlope: Float,
        isUserCourse: Boolean = false,
        linkedCourseId: String? = null
    ): TrendingCourseTicker {
        val totalCandles = 32
        val candles = mutableListOf<CourseCandle>()
        val sparkline = mutableListOf<Float>()

        var runningClose = previousClose - (trendSlope * 14f)
        var high24 = baseIndex
        var low24 = baseIndex

        for (i in 0 until totalCandles) {
            val wave = sin(phaseSeed + i * 0.48f) * 2.4f + cos(phaseSeed * 0.7f + i * 0.27f) * 1.6f
            val open = runningClose
            val close = if (i == totalCandles - 1) {
                baseIndex
            } else {
                max(20f, open + trendSlope + wave * 0.55f)
            }
            val spread = abs(sin(phaseSeed + i * 0.9f)) * 3.2f + 1.2f
            val high = max(open, close) + spread
            val low = max(15f, min(open, close) - spread * 0.85f)
            val vol = (quizVelocityPerHr * (0.72f + 0.48f * abs(sin(phaseSeed + i * 0.35f)))).roundToInt()

            if (i >= totalCandles - 24) {
                high24 = max(high24, high)
                low24 = min(low24, low)
            }

            candles.add(
                CourseCandle(
                    label = "T-${totalCandles - 1 - i}",
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    learnerVolume = vol
                )
            )
            sparkline.add(close)
            runningClose = close
        }

        return TrendingCourseTicker(
            id = id,
            symbol = symbol,
            title = title,
            category = category,
            level = level,
            goal = goal,
            currentIndex = baseIndex,
            previousClose = previousClose,
            high24h = max(high24, baseIndex + 2.5f),
            low24h = min(low24, baseIndex - 2.5f),
            activeLearners = activeLearners,
            completionYieldPct = completionYieldPct,
            quizVelocityPerHr = quizVelocityPerHr,
            sparklinePoints = sparkline,
            candles = candles,
            isUserCourse = isUserCourse,
            linkedCourseId = linkedCourseId
        )
    }

    /**
     * Merges user-created saved courses into the live market ticker list so any custom course
     * has its own real-time market ticker and engagement chart.
     */
    fun syncWithUserCourses(
        currentTickers: List<TrendingCourseTicker>,
        userCourses: List<Course>
    ): List<TrendingCourseTicker> {
        val mutable = currentTickers.toMutableList()

        // Update Share Market ticker with user's actual completion boost if present
        val shareMarketCourse = userCourses.find { it.id == ShareMarketCatalog.SHARE_MARKET_COURSE_ID }
        if (shareMarketCourse != null) {
            val idx = mutable.indexOfFirst { it.linkedCourseId == ShareMarketCatalog.SHARE_MARKET_COURSE_ID }
            if (idx >= 0) {
                val existing = mutable[idx]
                val completionBoost = shareMarketCourse.completedLessonsCount * 3.25f
                val boostedClose = max(existing.currentIndex, 242.80f + completionBoost)
                val updatedSpark = existing.sparklinePoints.dropLast(1) + boostedClose
                val lastCandle = existing.candles.last()
                val updatedCandles = existing.candles.dropLast(1) + lastCandle.copy(
                    close = boostedClose,
                    high = max(lastCandle.high, boostedClose)
                )
                mutable[idx] = existing.copy(
                    currentIndex = boostedClose,
                    high24h = max(existing.high24h, boostedClose),
                    completionYieldPct = min(99.8f, 96.4f + shareMarketCourse.progressPercentage * 0.03f),
                    sparklinePoints = updatedSpark,
                    candles = updatedCandles,
                    isUserCourse = true
                )
            }
        }

        // Add any custom user-created courses as live market tickers
        userCourses
            .filter { it.id != ShareMarketCatalog.SHARE_MARKET_COURSE_ID }
            .forEachIndexed { index, course ->
                val existingIdx = mutable.indexOfFirst { it.linkedCourseId == course.id }
                val progressBoost = course.completedLessonsCount * 4.5f
                if (existingIdx >= 0) {
                    val existing = mutable[existingIdx]
                    val newCurrent = max(existing.currentIndex, existing.previousClose + 8.4f + progressBoost)
                    mutable[existingIdx] = existing.copy(
                        title = course.title,
                        level = course.level,
                        goal = course.goal,
                        currentIndex = newCurrent,
                        high24h = max(existing.high24h, newCurrent),
                        isUserCourse = true
                    )
                } else {
                    val cleanLetters = course.title
                        .uppercase()
                        .replace(Regex("[^A-Z]"), "")
                        .take(5)
                        .ifEmpty { "EDAM${index + 1}" }
                    val symbol = "\$$cleanLetters"
                    val baseVal = 140f + (index * 18f) + progressBoost
                    val prevVal = 128f + (index * 18f)
                    val customTicker = buildSeededTicker(
                        id = "user_course_${course.id}",
                        symbol = symbol,
                        title = course.title,
                        category = "Custom Curriculum",
                        level = course.level,
                        goal = course.goal,
                        baseIndex = baseVal,
                        previousClose = prevVal,
                        activeLearners = 4_200 + (course.totalLessons * 310) + (course.completedLessonsCount * 190),
                        completionYieldPct = min(99.5f, 89.0f + course.progressPercentage * 0.1f),
                        quizVelocityPerHr = 640 + (course.completedLessonsCount * 85),
                        phaseSeed = (index + 1) * 1.7f,
                        trendSlope = 0.65f,
                        isUserCourse = true,
                        linkedCourseId = course.id
                    )
                    mutable.add(1, customTicker)
                }
            }

        return mutable
    }

    /**
     * Advances all market tickers by one real-time tick, updating live engagement index,
     * active learners, sparkline points, and the active candlestick bar.
     */
    fun stepRealTimeTick(
        tickers: List<TrendingCourseTicker>,
        tickCounter: Long
    ): List<TrendingCourseTicker> {
        return tickers.mapIndexed { idx, ticker ->
            val phase = tickCounter * 0.63f + idx * 1.37f
            val microDelta = (sin(phase) * 0.95f) + (cos(phase * 0.5f) * 0.45f) +
                (if (ticker.isPositive) 0.14f else -0.04f)
            val updatedIndex = max(25f, ticker.currentIndex + microDelta)

            val learnerDelta = ((sin(phase * 0.8f) * 18f) + 6f).roundToInt()
            val updatedLearners = max(500, ticker.activeLearners + learnerDelta)

            val velocityDelta = (cos(phase) * 14f).roundToInt()
            val updatedVelocity = max(120, ticker.quizVelocityPerHr + velocityDelta)

            val updatedSparkline = (ticker.sparklinePoints.drop(1) + updatedIndex)

            val lastCandle = ticker.candles.lastOrNull()
            val updatedCandles = if (lastCandle != null) {
                if (tickCounter % 4L == 0L) {
                    // Roll a fresh live candle every 4 ticks for dynamic candlestick chart movement
                    val newOpen = lastCandle.close
                    val newClose = updatedIndex
                    val newHigh = max(newOpen, newClose) + abs(microDelta) * 0.9f + 0.6f
                    val newLow = min(newOpen, newClose) - abs(microDelta) * 0.8f - 0.5f
                    val newVol = max(200, lastCandle.learnerVolume + learnerDelta * 8)
                    ticker.candles.drop(1) + CourseCandle(
                        label = "LIVE",
                        open = newOpen,
                        high = newHigh,
                        low = newLow,
                        close = newClose,
                        learnerVolume = newVol
                    )
                } else {
                    val updatedCandle = lastCandle.copy(
                        close = updatedIndex,
                        high = max(lastCandle.high, updatedIndex + 0.35f),
                        low = min(lastCandle.low, updatedIndex - 0.35f),
                        learnerVolume = max(200, lastCandle.learnerVolume + abs(learnerDelta) * 3)
                    )
                    ticker.candles.dropLast(1) + updatedCandle
                }
            } else {
                ticker.candles
            }

            ticker.copy(
                currentIndex = updatedIndex,
                high24h = max(ticker.high24h, updatedIndex),
                low24h = min(ticker.low24h, updatedIndex),
                activeLearners = updatedLearners,
                quizVelocityPerHr = updatedVelocity,
                sparklinePoints = updatedSparkline,
                candles = updatedCandles
            )
        }
    }

    fun computeGlobalSummary(tickers: List<TrendingCourseTicker>): MarketGlobalSummary {
        if (tickers.isEmpty()) return MarketGlobalSummary()
        val totalIndex = tickers.sumOf { it.currentIndex.toDouble() }.toFloat() * 3.85f
        val avgChange = tickers.map { it.percentChange }.average().toFloat()
        val totalLearners = tickers.sumOf { it.activeLearners }
        val avgYield = tickers.map { it.completionYieldPct }.average().toFloat()
        val advancing = tickers.count { it.isPositive }
        val declining = tickers.size - advancing
        return MarketGlobalSummary(
            compositeIndexValue = totalIndex,
            compositeChangePct = avgChange,
            totalActiveLearners = totalLearners,
            avgQuizEngagementPct = avgYield,
            advancingCount = advancing,
            decliningCount = declining
        )
    }
}
