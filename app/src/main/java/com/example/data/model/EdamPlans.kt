package com.example.data.model

enum class PlanTier(
    val id: String,
    val displayName: String,
    val standardPrice: String,
    val badgeLabel: String,
    val tagline: String,
    val features: List<String>
) {
    BASIC(
        id = "BASIC",
        displayName = "Basic",
        standardPrice = "Free",
        badgeLabel = "Starter",
        tagline = "Essential tools to build courses and practice fundamentals.",
        features = listOf(
            "Up to 3 active courses at a time",
            "Standard lesson explanations & 4-question quizzes",
            "Included Share Market & Investing starter curriculum",
            "On-device progress tracking"
        )
    ),
    PRO(
        id = "PRO",
        displayName = "Pro",
        standardPrice = "$9.99 / mo",
        badgeLabel = "Popular",
        tagline = "Full offline study packs and unlimited custom courses.",
        features = listOf(
            "Unlimited AI-generated courses across any topic",
            "Automatic 100% offline course & quiz downloading",
            "Cloud backup & multi-device sync with Gmail",
            "All 7 custom appearance & high-contrast themes"
        )
    ),
    MAX(
        id = "MAX",
        displayName = "Max",
        standardPrice = "$24.99 / mo",
        badgeLabel = "Top Tier",
        tagline = "Complete mastery suite with priority AI and full offline packs.",
        features = listOf(
            "Everything in Pro with priority Gemini curriculum depth",
            "Instant full-course offline pack bundling on creation",
            "Advanced Share Market, Technical & Fundamental modules",
            "Detailed quiz analytics & unlimited cloud storage",
            "Developer & Early-Access features unlocked"
        )
    );

    companion object {
        const val DEVELOPER_MAX_CODE = "X7PLD9Q2RM4JY1S8W"

        fun fromId(id: String?): PlanTier {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: BASIC
        }

        fun isValidDeveloperCode(input: String): Boolean {
            return input.trim() == DEVELOPER_MAX_CODE
        }
    }
}
