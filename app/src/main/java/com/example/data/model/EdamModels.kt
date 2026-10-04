package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Course(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val level: String,
    val goal: String,
    val description: String,
    val outcomes: List<String>,
    val units: List<CourseUnit>,
    val completed: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalLessons: Int
        get() = units.sumOf { it.lessons.size }

    val completedLessonsCount: Int
        get() = units.sumOf { unit ->
            unit.lessons.count { lesson -> completed.contains(lesson.id) }
        }

    val progressPercentage: Int
        get() = if (totalLessons == 0) 0 else Math.round((completedLessonsCount.toFloat() / totalLessons.toFloat()) * 100f)
}

data class CourseUnit(
    val id: String,
    val title: String,
    val description: String,
    val lessons: List<LessonSummary>
)

data class LessonSummary(
    val id: String,
    val title: String,
    val summary: String
)

data class LessonContent(
    val title: String,
    val objective: String,
    val sections: List<LearningSection>,
    val practice: List<PracticeQuestion>,
    val summary: String
)

data class LearningSection(
    val heading: String,
    val text: String,
    val examples: List<String>
)

data class PracticeQuestion(
    val question: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanation: String
)

object EdamJsonParser {

    fun parseCourse(
        rawJson: String,
        fallbackTitle: String,
        fallbackLevel: String,
        fallbackGoal: String,
        completedIds: List<String> = emptyList(),
        courseId: String = UUID.randomUUID().toString(),
        createdAt: Long = System.currentTimeMillis()
    ): Course {
        val cleaned = stripMarkdownFences(rawJson)
        val obj = JSONObject(cleaned)

        val title = obj.optString("title").ifBlank { fallbackTitle }
        val level = obj.optString("level").ifBlank { fallbackLevel }
        val goal = obj.optString("goal").ifBlank { fallbackGoal }
        val description = obj.optString("description").ifBlank {
            "A structured $level learning path designed to help you achieve: $goal"
        }

        val outcomes = mutableListOf<String>()
        val outcomesArr = obj.optJSONArray("outcomes")
        if (outcomesArr != null) {
            for (i in 0 until outcomesArr.length()) {
                val item = outcomesArr.optString(i).trim()
                if (item.isNotEmpty()) outcomes.add(item)
            }
        }

        val units = mutableListOf<CourseUnit>()
        val unitsArr = obj.optJSONArray("units")
        if (unitsArr != null) {
            for (uIdx in 0 until unitsArr.length()) {
                val uObj = unitsArr.optJSONObject(uIdx) ?: continue
                val unitId = uObj.optString("id").ifBlank { "unit-${uIdx + 1}" }
                val unitTitle = uObj.optString("title").ifBlank { "Unit ${uIdx + 1}" }
                val unitDesc = uObj.optString("description")

                val lessons = mutableListOf<LessonSummary>()
                val lessonsArr = uObj.optJSONArray("lessons")
                if (lessonsArr != null) {
                    for (lIdx in 0 until lessonsArr.length()) {
                        val lObj = lessonsArr.optJSONObject(lIdx) ?: continue
                        val lessonId = lObj.optString("id").ifBlank { "$unitId-lesson-${lIdx + 1}" }
                        val lessonTitle = lObj.optString("title").ifBlank { "Lesson ${lIdx + 1}" }
                        val lessonSummary = lObj.optString("summary")
                        lessons.add(
                            LessonSummary(
                                id = lessonId,
                                title = lessonTitle,
                                summary = lessonSummary
                            )
                        )
                    }
                }

                units.add(
                    CourseUnit(
                        id = unitId,
                        title = unitTitle,
                        description = unitDesc,
                        lessons = lessons
                    )
                )
            }
        }

        return Course(
            id = courseId,
            title = title,
            level = level,
            goal = goal,
            description = description,
            outcomes = outcomes,
            units = units,
            completed = completedIds,
            createdAt = createdAt
        )
    }

    fun courseToJson(course: Course): String {
        val obj = JSONObject()
        obj.put("id", course.id)
        obj.put("title", course.title)
        obj.put("level", course.level)
        obj.put("goal", course.goal)
        obj.put("description", course.description)

        val outcomesArr = JSONArray()
        course.outcomes.forEach { outcomesArr.put(it) }
        obj.put("outcomes", outcomesArr)

        val unitsArr = JSONArray()
        course.units.forEach { unit ->
            val uObj = JSONObject()
            uObj.put("id", unit.id)
            uObj.put("title", unit.title)
            uObj.put("description", unit.description)

            val lessonsArr = JSONArray()
            unit.lessons.forEach { lesson ->
                val lObj = JSONObject()
                lObj.put("id", lesson.id)
                lObj.put("title", lesson.title)
                lObj.put("summary", lesson.summary)
                lessonsArr.put(lObj)
            }
            uObj.put("lessons", lessonsArr)
            unitsArr.put(uObj)
        }
        obj.put("units", unitsArr)
        return obj.toString()
    }

    fun parseLessonContent(rawJson: String, fallbackTitle: String): LessonContent {
        val cleaned = stripMarkdownFences(rawJson)
        val obj = JSONObject(cleaned)

        val title = obj.optString("title").ifBlank { fallbackTitle }
        val objective = obj.optString("objective")
        val summary = obj.optString("summary")

        val sections = mutableListOf<LearningSection>()
        val sectionsArr = obj.optJSONArray("sections")
        if (sectionsArr != null) {
            for (i in 0 until sectionsArr.length()) {
                val sObj = sectionsArr.optJSONObject(i) ?: continue
                val heading = sObj.optString("heading").ifBlank { "Part ${i + 1}" }
                val text = sObj.optString("text")
                val examples = mutableListOf<String>()
                val exArr = sObj.optJSONArray("examples")
                if (exArr != null) {
                    for (j in 0 until exArr.length()) {
                        val ex = exArr.optString(j).trim()
                        if (ex.isNotEmpty()) examples.add(ex)
                    }
                }
                sections.add(
                    LearningSection(
                        heading = heading,
                        text = text,
                        examples = examples
                    )
                )
            }
        }

        val practice = mutableListOf<PracticeQuestion>()
        val practiceArr = obj.optJSONArray("practice")
        if (practiceArr != null) {
            for (i in 0 until practiceArr.length()) {
                val qObj = practiceArr.optJSONObject(i) ?: continue
                val questionText = qObj.optString("question")
                val options = mutableListOf<String>()
                val optArr = qObj.optJSONArray("options")
                if (optArr != null) {
                    for (j in 0 until optArr.length()) {
                        options.add(optArr.optString(j))
                    }
                }
                val answerIndex = qObj.optInt("answerIndex", 0)
                    .coerceIn(0, (options.size - 1).coerceAtLeast(0))
                val explanation = qObj.optString("explanation")
                if (questionText.isNotBlank() && options.isNotEmpty()) {
                    practice.add(
                        PracticeQuestion(
                            question = questionText,
                            options = options,
                            answerIndex = answerIndex,
                            explanation = explanation
                        )
                    )
                }
            }
        }

        return LessonContent(
            title = title,
            objective = objective,
            sections = sections,
            practice = practice,
            summary = summary
        )
    }

    fun lessonToJson(lesson: LessonContent): String {
        val obj = JSONObject()
        obj.put("title", lesson.title)
        obj.put("objective", lesson.objective)
        obj.put("summary", lesson.summary)

        val sectionsArr = JSONArray()
        lesson.sections.forEach { sec ->
            val sObj = JSONObject()
            sObj.put("heading", sec.heading)
            sObj.put("text", sec.text)
            val exArr = JSONArray()
            sec.examples.forEach { exArr.put(it) }
            sObj.put("examples", exArr)
            sectionsArr.put(sObj)
        }
        obj.put("sections", sectionsArr)

        val practiceArr = JSONArray()
        lesson.practice.forEach { q ->
            val qObj = JSONObject()
            qObj.put("question", q.question)
            val optArr = JSONArray()
            q.options.forEach { optArr.put(it) }
            qObj.put("options", optArr)
            qObj.put("answerIndex", q.answerIndex)
            qObj.put("explanation", q.explanation)
            practiceArr.put(qObj)
        }
        obj.put("practice", practiceArr)
        return obj.toString()
    }

    fun parseStringList(jsonArrayStr: String): List<String> {
        return try {
            val arr = JSONArray(jsonArrayStr)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val item = arr.optString(i)
                if (item.isNotBlank()) list.add(item)
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun stringListToJson(list: List<String>): String {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    fun stripMarkdownFences(raw: String): String {
        var text = raw.trim()
        val firstBrace = text.indexOf('{')
        val lastBrace = text.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return text.substring(firstBrace, lastBrace + 1).trim()
        }
        if (text.startsWith("```")) {
            val firstNewline = text.indexOf('\n')
            if (firstNewline != -1) {
                text = text.substring(firstNewline + 1)
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length - 3)
            }
        }
        return text.trim()
    }
}

/**
 * Represents a learning subject available to be discovered, searched, and added in the Discover section.
 * Can be either a built-in curriculum or a user-added subject synced with Firestore.
 */
data class DiscoverSubject(
    val id: String,
    val title: String,
    val category: String,
    val level: String,
    val description: String,
    val unitsCount: Int = 4,
    val xpReward: Int = 50,
    val isUserAdded: Boolean = false,
    val isCloudSynced: Boolean = false,
    val iconKey: String = "science",
    val tagList: List<String> = emptyList()
)

object DiscoverCatalog {
    val PRESET_SUBJECTS = listOf(
        DiscoverSubject(
            id = "sub_autonomous_ai",
            title = "Autonomous AI Agents & LLMs",
            category = "Technology & AI",
            level = "Intermediate",
            description = "Explore memory architectures, tool calling, multi-agent debates, and reinforcement learning fine-tuning.",
            unitsCount = 5,
            xpReward = 80,
            iconKey = "psychology",
            tagList = listOf("AI", "LLM", "Agents", "Python")
        ),
        DiscoverSubject(
            id = "sub_algo_trading",
            title = "Algorithmic Trading & Quant Finance",
            category = "Finance & Markets",
            level = "Advanced",
            description = "Backtesting strategies, statistical arbitrage, risk budgeting, and high-frequency orderbook mechanics.",
            unitsCount = 6,
            xpReward = 90,
            iconKey = "trending_up",
            tagList = listOf("Quant", "Trading", "Risk", "Math")
        ),
        DiscoverSubject(
            id = "sub_neuro_learning",
            title = "Neuroscience of High-Performance Learning",
            category = "Science & Math",
            level = "Beginner",
            description = "Synaptic plasticity, deliberate practice, circadian memory consolidation, and flow-state activation.",
            unitsCount = 4,
            xpReward = 65,
            iconKey = "neurology",
            tagList = listOf("Brain", "Memory", "Focus", "Biology")
        ),
        DiscoverSubject(
            id = "sub_cyber_defense",
            title = "Cybersecurity Defense & Ethical Hacking",
            category = "Technology & AI",
            level = "Intermediate",
            description = "Network forensics, vulnerability scanning, threat modeling, and zero-trust perimeter architecture.",
            unitsCount = 5,
            xpReward = 75,
            iconKey = "shield",
            tagList = listOf("Security", "Networks", "Crypto", "Linux")
        ),
        DiscoverSubject(
            id = "sub_astrophysics",
            title = "Astrophysics & Space Exploration Systems",
            category = "Science & Math",
            level = "Intermediate",
            description = "Orbital mechanics, stellar nucleosynthesis, black hole physics, and planetary terraforming engineering.",
            unitsCount = 4,
            xpReward = 70,
            iconKey = "rocket",
            tagList = listOf("Space", "Gravity", "Physics", "Cosmos")
        ),
        DiscoverSubject(
            id = "sub_game_theory",
            title = "Game Theory & Competitive Strategy",
            category = "Strategy & Logic",
            level = "Intermediate",
            description = "Nash equilibrium, zero-sum matrices, Pareto optimality, and dynamic bargaining simulations.",
            unitsCount = 4,
            xpReward = 70,
            iconKey = "military_tech",
            tagList = listOf("Strategy", "Logic", "Decisions", "Math")
        ),
        DiscoverSubject(
            id = "sub_behavioral_econ",
            title = "Behavioral Economics & Nudge Architecture",
            category = "Finance & Markets",
            level = "Beginner",
            description = "Cognitive heuristics, prospect theory, incentive mechanisms, and social choice theory.",
            unitsCount = 4,
            xpReward = 60,
            iconKey = "account_balance",
            tagList = listOf("Psychology", "Economics", "Markets")
        ),
        DiscoverSubject(
            id = "sub_fullstack_apps",
            title = "Modern Full-Stack Cloud Architecture",
            category = "Technology & AI",
            level = "Advanced",
            description = "Distributed microservices, event streaming, Kubernetes orchestration, and reactive frontends.",
            unitsCount = 5,
            xpReward = 85,
            iconKey = "cloud",
            tagList = listOf("Cloud", "Fullstack", "DevOps", "Scale")
        ),
        DiscoverSubject(
            id = "sub_longevity_biohack",
            title = "Metabolic Longevity & Biohacking",
            category = "Science & Math",
            level = "Intermediate",
            description = "Cellular autophagy, mitochondrial resilience, biomarker tracking, and epigenetic rejuvenation.",
            unitsCount = 4,
            xpReward = 65,
            iconKey = "favorite",
            tagList = listOf("Health", "Biomarkers", "Longevity")
        ),
        DiscoverSubject(
            id = "sub_philosophy_mind",
            title = "Philosophy of Mind & Cognitive Epistemology",
            category = "Humanities",
            level = "Beginner",
            description = "The hard problem of consciousness, Turing tests, phenomenological intentionality, and moral agency.",
            unitsCount = 4,
            xpReward = 60,
            iconKey = "menu_book",
            tagList = listOf("Philosophy", "Ethics", "Logic", "Mind")
        )
    )

    val CATEGORIES = listOf(
        "All",
        "Technology & AI",
        "Finance & Markets",
        "Strategy & Logic",
        "Science & Math",
        "Humanities"
    )
}
