package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LessonOutlineItem(
    val id: String,
    val title: String,
    val summary: String,
    val estimatedMinutes: Int = 12,
    val xpReward: Int = 50,
    val keyConcepts: List<String> = emptyList()
)

@Serializable
data class UnitOutlineItem(
    val id: String,
    val title: String,
    val description: String,
    val lessons: List<LessonOutlineItem>
)

@Serializable
data class CourseOutline(
    val id: String = "outline_${System.currentTimeMillis()}",
    val title: String,
    val level: String,
    val goal: String,
    val focusArea: String = "Comprehensive",
    val description: String,
    val outcomes: List<String>,
    val estimatedHours: Int = 8,
    val totalXp: Int = 600,
    val units: List<UnitOutlineItem>
) {
    fun toCourse(): Course {
        val courseUnits = units.map { u ->
            CourseUnit(
                id = u.id,
                title = u.title,
                description = u.description,
                lessons = u.lessons.map { l ->
                    LessonSummary(
                        id = l.id,
                        title = l.title,
                        summary = l.summary
                    )
                }
            )
        }
        return Course(
            id = id,
            title = title,
            level = level,
            goal = goal,
            description = description,
            outcomes = outcomes,
            units = courseUnits,
            completed = emptyList()
        )
    }
}

data class QuickTopicPreset(
    val title: String,
    val topic: String,
    val level: String,
    val goal: String,
    val focusArea: String,
    val category: String
)

object CourseOutlinePresets {
    val suggestedPresets = listOf(
        QuickTopicPreset(
            title = "Algorithmic Trading & Python",
            topic = "Algorithmic Trading Strategies with Python",
            level = "Intermediate",
            goal = "Build automated mean-reversion and momentum bots, backtest sharpe ratios, and manage market risk",
            focusArea = "Quantitative Finance",
            category = "Finance"
        ),
        QuickTopicPreset(
            title = "Generative AI & LLMs",
            topic = "Generative AI Architecture & Large Language Models",
            level = "Advanced",
            goal = "Master transformer self-attention, fine-tuning LoRA, and deploying retrieval-augmented generation (RAG)",
            focusArea = "Artificial Intelligence",
            category = "Tech"
        ),
        QuickTopicPreset(
            title = "Value Investing Masterclass",
            topic = "Value Investing & Fundamental Analysis",
            level = "Beginner",
            goal = "Analyze 10-K balance sheets, calculate intrinsic DCF value, and identify economic moats like Warren Buffett",
            focusArea = "Stock Market",
            category = "Finance"
        ),
        QuickTopicPreset(
            title = "DeFi & Blockchain Systems",
            topic = "Decentralized Finance & Smart Contract Architecture",
            level = "Intermediate",
            goal = "Understand automated market makers (AMMs), liquidity pools, staking yields, and protocol risk analysis",
            focusArea = "Crypto & Web3",
            category = "Crypto"
        ),
        QuickTopicPreset(
            title = "Behavioral Economics",
            topic = "Behavioral Economics & Investor Psychology",
            level = "Intermediate",
            goal = "Identify cognitive biases, herd behavior, loss aversion, and make rational financial decisions under uncertainty",
            focusArea = "Economics",
            category = "Economics"
        ),
        QuickTopicPreset(
            title = "Full-Stack Android Jetpack",
            topic = "Modern Android Development with Jetpack Compose & Kotlin",
            level = "Intermediate",
            goal = "Build high-performance, reactive Android apps using M3, Coroutines, StateFlow, and offline Room caching",
            focusArea = "Software Engineering",
            category = "Tech"
        )
    )
}
