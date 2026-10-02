package com.example.data.model

data class BadgeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val percentageRequired: Int,
    val iconKey: String,
    val colorCategory: String, // e.g. "bronze", "silver", "gold", "emerald", "diamond", "ruby"
    val specificCourseId: String? = null
)

data class BadgeDisplayItem(
    val definition: BadgeDefinition,
    val isUnlocked: Boolean,
    val currentProgressPct: Int,
    val unlockedAt: Long? = null,
    val courseTitle: String? = null
)

object BadgeCatalog {
    val MILESTONE_BADGES = listOf(
        BadgeDefinition(
            id = "badge_10",
            title = "First Spark",
            description = "Begin your learning path and complete 10% of any course",
            percentageRequired = 10,
            iconKey = "spark",
            colorCategory = "bronze"
        ),
        BadgeDefinition(
            id = "badge_25",
            title = "Momentum Builder",
            description = "Solidify your study habits with 25% course completion",
            percentageRequired = 25,
            iconKey = "momentum",
            colorCategory = "silver"
        ),
        BadgeDefinition(
            id = "badge_50",
            title = "Knowledge Titan",
            description = "Cross the halfway line — reach 50% course mastery",
            percentageRequired = 50,
            iconKey = "titan",
            colorCategory = "gold"
        ),
        BadgeDefinition(
            id = "badge_75",
            title = "Mastery Adept",
            description = "Enter the final stretch — achieve 75% curriculum completion",
            percentageRequired = 75,
            iconKey = "adept",
            colorCategory = "emerald"
        ),
        BadgeDefinition(
            id = "badge_100",
            title = "Grand Scholar",
            description = "Graduate with honors — conquer 100% of an entire course",
            percentageRequired = 100,
            iconKey = "scholar",
            colorCategory = "diamond"
        ),
        BadgeDefinition(
            id = "badge_shmkt_mastery",
            title = "Wall Street Virtuoso",
            description = "Complete 100% of the Share Market & Equity Investing curriculum",
            percentageRequired = 100,
            iconKey = "market_bull",
            colorCategory = "ruby",
            specificCourseId = ShareMarketCatalog.SHARE_MARKET_COURSE_ID
        )
    )

    fun evaluateBadgesForCourse(course: Course): List<BadgeDefinition> {
        val pct = course.progressPercentage
        return MILESTONE_BADGES.filter { badge ->
            if (badge.specificCourseId != null) {
                course.id == badge.specificCourseId && pct >= badge.percentageRequired
            } else {
                pct >= badge.percentageRequired
            }
        }
    }
}
