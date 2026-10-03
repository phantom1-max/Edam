package com.example.data.model

data class BadgeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val percentageRequired: Int,
    val iconKey: String,
    val colorCategory: String, // e.g. "bronze", "silver", "gold", "emerald", "diamond", "ruby", "cyan", "purple"
    val category: String, // "STREAK", "COURSE", "MARKET", "CHESS", "CODING", "POLYMATH"
    val specificCourseId: String? = null
)

data class BadgeDisplayItem(
    val definition: BadgeDefinition,
    val isUnlocked: Boolean,
    val currentProgressPct: Int,
    val unlockedAt: Long? = null,
    val courseTitle: String? = null,
    val isCloudSynced: Boolean = true
)

object BadgeCatalog {
    val MILESTONE_BADGES = listOf(
        // Course Milestones
        BadgeDefinition(
            id = "badge_10",
            title = "First Spark",
            description = "Begin your learning path and complete 10% of any course",
            percentageRequired = 10,
            iconKey = "spark",
            colorCategory = "bronze",
            category = "COURSE"
        ),
        BadgeDefinition(
            id = "badge_25",
            title = "Momentum Builder",
            description = "Solidify your study habits with 25% course completion",
            percentageRequired = 25,
            iconKey = "momentum",
            colorCategory = "silver",
            category = "COURSE"
        ),
        BadgeDefinition(
            id = "badge_50",
            title = "Knowledge Titan",
            description = "Cross the halfway line — reach 50% course mastery",
            percentageRequired = 50,
            iconKey = "titan",
            colorCategory = "gold",
            category = "COURSE"
        ),
        BadgeDefinition(
            id = "badge_75",
            title = "Mastery Adept",
            description = "Enter the final stretch — achieve 75% curriculum completion",
            percentageRequired = 75,
            iconKey = "adept",
            colorCategory = "emerald",
            category = "COURSE"
        ),
        BadgeDefinition(
            id = "badge_100",
            title = "Grand Scholar",
            description = "Graduate with honors — conquer 100% of an entire course",
            percentageRequired = 100,
            iconKey = "scholar",
            colorCategory = "diamond",
            category = "COURSE"
        ),

        // Daily Streaks & Habits
        BadgeDefinition(
            id = "badge_streak_3",
            title = "Flame Awakened",
            description = "Ignite a 3-day consecutive study streak with daily flashcards",
            percentageRequired = 100,
            iconKey = "flame_3",
            colorCategory = "gold",
            category = "STREAK"
        ),
        BadgeDefinition(
            id = "badge_streak_7",
            title = "Unstoppable Week",
            description = "Achieve a full 7-day learning streak without missing a day",
            percentageRequired = 100,
            iconKey = "flame_7",
            colorCategory = "ruby",
            category = "STREAK"
        ),

        // Stock Market & Finance (Guided by RoboBroker)
        BadgeDefinition(
            id = "badge_shmkt_mastery",
            title = "Wall Street Virtuoso",
            description = "Complete 100% of the Share Market & Equity Investing curriculum",
            percentageRequired = 100,
            iconKey = "market_bull",
            colorCategory = "ruby",
            category = "MARKET",
            specificCourseId = ShareMarketCatalog.SHARE_MARKET_COURSE_ID
        ),
        BadgeDefinition(
            id = "badge_market_analyst",
            title = "Bull Market Analyst",
            description = "Explore stock market candlesticks, technical graphs, and company valuations",
            percentageRequired = 100,
            iconKey = "candlestick",
            colorCategory = "cyan",
            category = "MARKET"
        ),

        // Chess Mastery (Guided by Vex Grandmaster)
        BadgeDefinition(
            id = "badge_chess_tactician",
            title = "Tactical Prodigy",
            description = "Solve interactive chess puzzles and calculate multi-step combinations",
            percentageRequired = 100,
            iconKey = "chess_knight",
            colorCategory = "purple",
            category = "CHESS",
            specificCourseId = ChessAndFlashcardCatalog.CHESS_GM_COURSE_ID
        ),

        // Coding & Computer Virus Bit (Guided by Bit Virus)
        BadgeDefinition(
            id = "badge_coding_virus",
            title = "Byte Glitch Overlord",
            description = "Master coding algorithms, data structures, and computer virus simulations",
            percentageRequired = 100,
            iconKey = "code_virus",
            colorCategory = "emerald",
            category = "CODING"
        ),

        // Polymath: Any New Subject
        BadgeDefinition(
            id = "badge_polymath",
            title = "Universal Polymath",
            description = "Create and explore a brand new custom subject using the AI Course Architect",
            percentageRequired = 100,
            iconKey = "polymath",
            colorCategory = "diamond",
            category = "POLYMATH"
        )
    )

    fun evaluateBadgesForCourse(course: Course): List<BadgeDefinition> {
        val pct = course.progressPercentage
        return MILESTONE_BADGES.filter { badge ->
            if (badge.category == "COURSE") {
                pct >= badge.percentageRequired
            } else if (badge.specificCourseId != null) {
                course.id == badge.specificCourseId && pct >= badge.percentageRequired
            } else {
                false
            }
        }
    }
}
