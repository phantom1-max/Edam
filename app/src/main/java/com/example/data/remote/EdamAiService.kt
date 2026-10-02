package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.Course
import com.example.data.model.CourseOutline
import com.example.data.model.CourseUnit
import com.example.data.model.EdamJsonParser
import com.example.data.model.LessonContent
import com.example.data.model.LessonOutlineItem
import com.example.data.model.LessonSummary
import com.example.data.model.UnitOutlineItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface GeminiRestApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body requestBody: RequestBody
    ): ResponseBody

    @POST
    suspend fun callWorker(
        @Url workerUrl: String,
        @Body requestBody: RequestBody
    ): ResponseBody
}

object EdamAiClient {
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
    const val WORKER_URL = "https://edam-ai.rup62012.workers.dev/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val apiService: GeminiRestApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .client(okHttpClient)
            .build()
            .create(GeminiRestApiService::class.java)
    }
}

class EdamAiService(
    private val service: GeminiRestApiService = EdamAiClient.apiService
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun hasValidGeminiApiKey(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY.trim()
        return key.isNotEmpty() &&
            key != "your_api_key_here" &&
            key != "GEMINI_API_KEY_DEFAULT_VALUE" &&
            !key.startsWith("YOUR_")
    }

    suspend fun generateCourse(
        courseName: String,
        level: String,
        goal: String
    ): Course = withContext(Dispatchers.IO) {
        val prompt = """
You are Edam, an educational AI teacher.

Create a structured learning course.

Course:
$courseName

Level:
$level

Goal:
$goal

Return ONLY data matching the requested JSON schema.

Create 5 to 8 units.

Each unit must contain 3 to 5 lessons.

The course should progress from easier ideas
to harder ideas.

For English and language courses, include:
vocabulary, grammar, reading, writing,
listening, pronunciation and communication
where appropriate.

Do not invent fake textbooks,
authors, institutions or citations.

The response will be rendered by an
interactive learning application.

Do NOT write markdown.
""".trimIndent()

        val rawResponseJson = executeAiRequest(
            action = "course",
            prompt = prompt,
            responseSchema = buildCourseSchema()
        )

        EdamJsonParser.parseCourse(
            rawJson = rawResponseJson,
            fallbackTitle = courseName,
            fallbackLevel = level,
            fallbackGoal = goal
        )
    }

    suspend fun generateCourseOutline(
        topic: String,
        level: String,
        goal: String,
        focusArea: String,
        unitCount: Int = 5
    ): CourseOutline = withContext(Dispatchers.IO) {
        val cleanTopic = topic.trim().ifBlank { "Modern Financial Intelligence" }
        val cleanLevel = level.trim().ifBlank { "Intermediate" }
        val cleanGoal = goal.trim().ifBlank { "Master practical concepts and hands-on decision making" }
        val cleanFocus = focusArea.trim().ifBlank { "Applied Strategy" }
        val units = unitCount.coerceIn(3, 8)

        val prompt = """
You are Edam, an educational curriculum architect powered by Gemini.

Create an exhaustive, structured course syllabus and learning outline for:
Topic: $cleanTopic
Level: $cleanLevel
Goal: $cleanGoal
Focus Specialization: $cleanFocus
Requested Module Units: $units

Return ONLY JSON conforming to schema.
Create $units distinct sequential units progressing from foundational to advanced mastery.
Each unit must contain 3 to 4 lessons with estimatedMinutes (10-25), xpReward (30-60), and summary.
Include 4 to 6 measurable outcome statements.
Do NOT write markdown.
""".trimIndent()

        try {
            val rawResponseJson = executeAiRequest(
                action = "course_outline",
                prompt = prompt,
                responseSchema = buildOutlineSchema()
            )
            parseCourseOutlineJson(
                rawJson = rawResponseJson,
                fallbackTopic = cleanTopic,
                fallbackLevel = cleanLevel,
                fallbackGoal = cleanGoal,
                fallbackFocus = cleanFocus,
                targetUnits = units
            )
        } catch (_: Exception) {
            buildStructuredFallbackOutline(
                topic = cleanTopic,
                level = cleanLevel,
                goal = cleanGoal,
                focusArea = cleanFocus,
                unitCount = units
            )
        }
    }

    private fun parseCourseOutlineJson(
        rawJson: String,
        fallbackTopic: String,
        fallbackLevel: String,
        fallbackGoal: String,
        fallbackFocus: String,
        targetUnits: Int
    ): CourseOutline {
        val cleaned = EdamJsonParser.stripMarkdownFences(rawJson)
        val obj = JSONObject(cleaned)

        val title = obj.optString("title").ifBlank { fallbackTopic }
        val level = obj.optString("level").ifBlank { fallbackLevel }
        val goal = obj.optString("goal").ifBlank { fallbackGoal }
        val focusArea = obj.optString("focusArea").ifBlank { fallbackFocus }
        val description = obj.optString("description").ifBlank {
            "A structured $level curriculum on $fallbackTopic focused on $fallbackFocus to achieve: $fallbackGoal."
        }
        val estimatedHours = obj.optInt("estimatedHours", targetUnits * 2)
        val totalXp = obj.optInt("totalXp", targetUnits * 150)

        val outcomes = mutableListOf<String>()
        val outcomesArr = obj.optJSONArray("outcomes")
        if (outcomesArr != null) {
            for (i in 0 until outcomesArr.length()) {
                val item = outcomesArr.optString(i).trim()
                if (item.isNotBlank()) outcomes.add(item)
            }
        }
        if (outcomes.isEmpty()) {
            outcomes.add("Understand the theoretical fundamentals of $fallbackTopic")
            outcomes.add("Apply core analytical methodologies in real-world scenarios")
            outcomes.add("Develop critical problem-solving and diagnostic skills")
            outcomes.add("Demonstrate competency through interactive quiz assessments")
        }

        val unitsList = mutableListOf<UnitOutlineItem>()
        val unitsArr = obj.optJSONArray("units")
        if (unitsArr != null) {
            for (i in 0 until unitsArr.length()) {
                val uObj = unitsArr.optJSONObject(i) ?: continue
                val uId = uObj.optString("id").ifBlank { "unit_${i + 1}" }
                val uTitle = uObj.optString("title").ifBlank { "Unit ${i + 1}: Core Modules" }
                val uDesc = uObj.optString("description").ifBlank { "Essential principles and practical lessons." }

                val lessonsList = mutableListOf<LessonOutlineItem>()
                val lessonsArr = uObj.optJSONArray("lessons")
                if (lessonsArr != null) {
                    for (j in 0 until lessonsArr.length()) {
                        val lObj = lessonsArr.optJSONObject(j) ?: continue
                        val lId = lObj.optString("id").ifBlank { "lesson_${i + 1}_${j + 1}" }
                        val lTitle = lObj.optString("title").ifBlank { "Lesson ${j + 1}" }
                        val lSummary = lObj.optString("summary").ifBlank { "Key concepts and quiz exercise." }
                        val estMin = lObj.optInt("estimatedMinutes", 15)
                        val xp = lObj.optInt("xpReward", 50)
                        lessonsList.add(
                            LessonOutlineItem(
                                id = lId,
                                title = lTitle,
                                summary = lSummary,
                                estimatedMinutes = estMin,
                                xpReward = xp
                            )
                        )
                    }
                }
                if (lessonsList.isNotEmpty()) {
                    unitsList.add(
                        UnitOutlineItem(
                            id = uId,
                            title = uTitle,
                            description = uDesc,
                            lessons = lessonsList
                        )
                    )
                }
            }
        }

        return if (unitsList.isNotEmpty()) {
            CourseOutline(
                title = title,
                level = level,
                goal = goal,
                focusArea = focusArea,
                description = description,
                outcomes = outcomes,
                estimatedHours = estimatedHours,
                totalXp = totalXp,
                units = unitsList
            )
        } else {
            buildStructuredFallbackOutline(
                topic = fallbackTopic,
                level = fallbackLevel,
                goal = fallbackGoal,
                focusArea = fallbackFocus,
                unitCount = targetUnits
            )
        }
    }

    private fun buildStructuredFallbackOutline(
        topic: String,
        level: String,
        goal: String,
        focusArea: String,
        unitCount: Int
    ): CourseOutline {
        val unitTitles = listOf(
            Pair("Foundations & Core Principles", "Groundwork concepts, terminology, and foundational mental models of $topic."),
            Pair("Mechanics & Analytical Frameworks", "Core mechanics, quantitative and qualitative analysis techniques."),
            Pair("Applied Strategies & Real-World Systems", "Practical workflows, execution strategies, and real-world system architecture."),
            Pair("Risk Mitigation & Diagnostic Problem Solving", "Identifying edge cases, systemic risks, and optimizing performance under stress."),
            Pair("Advanced Specialization & Edge Dynamics", "Cutting-edge paradigms, portfolio scale, and competitive positioning."),
            Pair("Mastery Capstone & Synthesis", "End-to-end practical execution, audit frameworks, and sustained competency.")
        )

        val actualUnitCount = unitCount.coerceIn(3, unitTitles.size)
        val units = mutableListOf<UnitOutlineItem>()
        var globalLessonCounter = 1

        for (uIdx in 0 until actualUnitCount) {
            val (uName, uDesc) = unitTitles[uIdx]
            val lessons = mutableListOf<LessonOutlineItem>()
            val lessonNames = listOf(
                "Introduction to $uName",
                "Deep Dive: Key Mechanics and Examples",
                "Hands-On Application & Practical Case Study",
                "Self-Assessment & Mastery Review"
            )
            for (lIdx in lessonNames.indices) {
                val lId = "l_outline_${uIdx + 1}_${lIdx + 1}"
                val title = "${uIdx + 1}.${lIdx + 1} ${lessonNames[lIdx]}"
                val summary = "Explore the primary components of ${lessonNames[lIdx].lowercase()} within $topic."
                lessons.add(
                    LessonOutlineItem(
                        id = lId,
                        title = title,
                        summary = summary,
                        estimatedMinutes = 12 + (lIdx * 3),
                        xpReward = 40 + (uIdx * 5),
                        keyConcepts = listOf("$topic Basics", "Applied Framework", "Quiz Checkpoint")
                    )
                )
                globalLessonCounter++
            }
            units.add(
                UnitOutlineItem(
                    id = "u_outline_${uIdx + 1}",
                    title = "Unit ${uIdx + 1}: $uName",
                    description = uDesc,
                    lessons = lessons
                )
            )
        }

        return CourseOutline(
            title = topic,
            level = level,
            goal = goal,
            focusArea = focusArea,
            description = "A comprehensive, structured $level curriculum designed to achieve: $goal.",
            outcomes = listOf(
                "Master foundational definitions, paradigms, and core principles of $topic",
                "Apply analytical models and practical decision-making frameworks",
                "Evaluate scenarios, troubleshoot risks, and synthesize strategic outcomes",
                "Demonstrate verified competence through interactive checkpoint quizzes"
            ),
            estimatedHours = actualUnitCount * 2,
            totalXp = units.sumOf { u -> u.lessons.sumOf { it.xpReward } },
            units = units
        )
    }

    private fun buildOutlineSchema(): JsonObject = buildJsonObject {
        put("type", "OBJECT")
        putJsonObject("properties") {
            putJsonObject("title") { put("type", "STRING") }
            putJsonObject("description") { put("type", "STRING") }
            putJsonObject("level") { put("type", "STRING") }
            putJsonObject("goal") { put("type", "STRING") }
            putJsonObject("focusArea") { put("type", "STRING") }
            putJsonObject("estimatedHours") { put("type", "INTEGER") }
            putJsonObject("totalXp") { put("type", "INTEGER") }
            putJsonObject("outcomes") {
                put("type", "ARRAY")
                putJsonObject("items") { put("type", "STRING") }
            }
            putJsonObject("units") {
                put("type", "ARRAY")
                putJsonObject("items") {
                    put("type", "OBJECT")
                    putJsonObject("properties") {
                        putJsonObject("id") { put("type", "STRING") }
                        putJsonObject("title") { put("type", "STRING") }
                        putJsonObject("description") { put("type", "STRING") }
                        putJsonObject("lessons") {
                            put("type", "ARRAY")
                            putJsonObject("items") {
                                put("type", "OBJECT")
                                putJsonObject("properties") {
                                    putJsonObject("id") { put("type", "STRING") }
                                    putJsonObject("title") { put("type", "STRING") }
                                    putJsonObject("summary") { put("type", "STRING") }
                                    putJsonObject("estimatedMinutes") { put("type", "INTEGER") }
                                    putJsonObject("xpReward") { put("type", "INTEGER") }
                                }
                                putJsonArray("required") {
                                    add("id")
                                    add("title")
                                    add("summary")
                                }
                            }
                        }
                    }
                    putJsonArray("required") {
                        add("id")
                        add("title")
                        add("description")
                        add("lessons")
                    }
                }
            }
        }
        putJsonArray("required") {
            add("title")
            add("description")
            add("outcomes")
            add("units")
        }
    }

    suspend fun generateLesson(
        course: Course,
        unit: CourseUnit,
        lesson: LessonSummary
    ): LessonContent = withContext(Dispatchers.IO) {
        val prompt = """
You are Edam, an educational AI teacher.

Teach this specific lesson.

Course:
${course.title}

Course level:
${course.level}

Course goal:
${course.goal}

Unit:
${unit.title}

Unit description:
${unit.description}

Lesson:
${lesson.title}

Lesson summary:
${lesson.summary}

Create a real teaching lesson, not an outline.

The lesson must contain:

- A clear objective
- Several explanation sections
- Useful examples
- A short practice activity
- 4 multiple-choice questions
- Explanations for every answer
- A concise lesson summary

The questions must actually test what
the lesson teaches.

For English learning, use accurate grammar,
vocabulary and natural examples.

Return ONLY the requested JSON.

Do not return markdown.
""".trimIndent()

        val rawResponseJson = executeAiRequest(
            action = "lesson",
            prompt = prompt,
            responseSchema = buildLessonSchema()
        )

        EdamJsonParser.parseLessonContent(
            rawJson = rawResponseJson,
            fallbackTitle = lesson.title
        )
    }

    private suspend fun executeAiRequest(
        action: String,
        prompt: String,
        responseSchema: JsonObject
    ): String {
        var lastError: Exception? = null

        // 1. Primary path: Direct Gemini REST API (when GEMINI_API_KEY is configured in Secrets)
        if (hasValidGeminiApiKey()) {
            try {
                return callDirectGeminiApi(prompt, responseSchema)
            } catch (e: Exception) {
                lastError = e
            }
        }

        // 2. Original Edam Worker endpoint from index.html
        try {
            return callEdamWorker(action, prompt)
        } catch (e: Exception) {
            if (lastError == null) {
                lastError = e
            }
        }

        throw IllegalStateException(
            lastError?.message ?: "AI request failed. Please configure GEMINI_API_KEY in the AI Studio Secrets panel."
        )
    }

    private suspend fun callDirectGeminiApi(
        prompt: String,
        responseSchema: JsonObject
    ): String {
        val payload = buildJsonObject {
            putJsonArray("contents") {
                add(
                    buildJsonObject {
                        putJsonArray("parts") {
                            add(
                                buildJsonObject {
                                    put("text", prompt)
                                }
                            )
                        }
                    }
                )
            }
            putJsonObject("generationConfig") {
                put("responseMimeType", "application/json")
                put("responseSchema", responseSchema)
                put("temperature", 0.6)
            }
        }

        val responseBody = service.generateContent(
            apiKey = BuildConfig.GEMINI_API_KEY.trim(),
            requestBody = payload.toString().toRequestBody(jsonMediaType)
        )
        val bodyStr = responseBody.string()
        val root = json.parseToJsonElement(bodyStr).jsonObject
        val text = root["candidates"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("content")?.jsonObject
            ?.get("parts")?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("text")?.jsonPrimitive?.content

        if (text.isNullOrBlank()) {
            throw IllegalStateException("Gemini returned no content.")
        }
        return text
    }

    private suspend fun callEdamWorker(
        action: String,
        prompt: String
    ): String {
        val payload = buildJsonObject {
            put("action", action)
            put("prompt", prompt)
        }

        val responseBody = service.callWorker(
            workerUrl = EdamAiClient.WORKER_URL,
            requestBody = payload.toString().toRequestBody(jsonMediaType)
        )
        val bodyStr = responseBody.string()
        val root = json.parseToJsonElement(bodyStr).jsonObject
        val errorMsg = root["error"]?.jsonPrimitive?.content
        if (!errorMsg.isNullOrBlank()) {
            throw IllegalStateException(errorMsg)
        }
        val text = root["text"]?.jsonPrimitive?.content
        if (text.isNullOrBlank()) {
            throw IllegalStateException("Gemini returned no content.")
        }
        return text
    }

    private fun buildCourseSchema(): JsonObject = buildJsonObject {
        put("type", "OBJECT")
        putJsonObject("properties") {
            putJsonObject("title") { put("type", "STRING") }
            putJsonObject("level") { put("type", "STRING") }
            putJsonObject("goal") { put("type", "STRING") }
            putJsonObject("description") { put("type", "STRING") }
            putJsonObject("outcomes") {
                put("type", "ARRAY")
                putJsonObject("items") { put("type", "STRING") }
            }
            putJsonObject("units") {
                put("type", "ARRAY")
                putJsonObject("items") {
                    put("type", "OBJECT")
                    putJsonObject("properties") {
                        putJsonObject("id") { put("type", "STRING") }
                        putJsonObject("title") { put("type", "STRING") }
                        putJsonObject("description") { put("type", "STRING") }
                        putJsonObject("lessons") {
                            put("type", "ARRAY")
                            putJsonObject("items") {
                                put("type", "OBJECT")
                                putJsonObject("properties") {
                                    putJsonObject("id") { put("type", "STRING") }
                                    putJsonObject("title") { put("type", "STRING") }
                                    putJsonObject("summary") { put("type", "STRING") }
                                }
                                putJsonArray("required") {
                                    add("id")
                                    add("title")
                                    add("summary")
                                }
                            }
                        }
                    }
                    putJsonArray("required") {
                        add("id")
                        add("title")
                        add("description")
                        add("lessons")
                    }
                }
            }
        }
        putJsonArray("required") {
            add("title")
            add("level")
            add("goal")
            add("description")
            add("outcomes")
            add("units")
        }
    }

    private fun buildLessonSchema(): JsonObject = buildJsonObject {
        put("type", "OBJECT")
        putJsonObject("properties") {
            putJsonObject("title") { put("type", "STRING") }
            putJsonObject("objective") { put("type", "STRING") }
            putJsonObject("sections") {
                put("type", "ARRAY")
                putJsonObject("items") {
                    put("type", "OBJECT")
                    putJsonObject("properties") {
                        putJsonObject("heading") { put("type", "STRING") }
                        putJsonObject("text") { put("type", "STRING") }
                        putJsonObject("examples") {
                            put("type", "ARRAY")
                            putJsonObject("items") { put("type", "STRING") }
                        }
                    }
                    putJsonArray("required") {
                        add("heading")
                        add("text")
                        add("examples")
                    }
                }
            }
            putJsonObject("practice") {
                put("type", "ARRAY")
                putJsonObject("items") {
                    put("type", "OBJECT")
                    putJsonObject("properties") {
                        putJsonObject("question") { put("type", "STRING") }
                        putJsonObject("options") {
                            put("type", "ARRAY")
                            putJsonObject("items") { put("type", "STRING") }
                        }
                        putJsonObject("answerIndex") { put("type", "INTEGER") }
                        putJsonObject("explanation") { put("type", "STRING") }
                    }
                    putJsonArray("required") {
                        add("question")
                        add("options")
                        add("answerIndex")
                        add("explanation")
                    }
                }
            }
            putJsonObject("summary") { put("type", "STRING") }
        }
        putJsonArray("required") {
            add("title")
            add("objective")
            add("sections")
            add("practice")
            add("summary")
        }
    }
}
