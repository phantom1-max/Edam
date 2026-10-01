package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.Course
import com.example.data.model.CourseUnit
import com.example.data.model.EdamJsonParser
import com.example.data.model.LessonContent
import com.example.data.model.LessonSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
