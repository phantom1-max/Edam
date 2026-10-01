package com.example.data.repository

import com.example.data.local.CachedLessonEntity
import com.example.data.local.CourseEntity
import com.example.data.local.EdamDao
import com.example.data.model.Course
import com.example.data.model.CourseUnit
import com.example.data.model.EdamJsonParser
import com.example.data.model.LessonContent
import com.example.data.model.LessonSummary
import com.example.data.model.ShareMarketCatalog
import com.example.data.remote.EdamAiService
import com.example.data.remote.EdamCloudRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

class EdamRepository(
    private val dao: EdamDao,
    private val aiService: EdamAiService = EdamAiService(),
    private val cloudRepository: EdamCloudRepository? = null
) {
    val activeCourseFlow: Flow<Course?> = dao.getActiveCourse().map { entity ->
        entity?.toDomainCourse()
    }

    val allCoursesFlow: Flow<List<Course>> = dao.getAllCourses().map { list ->
        list.map { it.toDomainCourse() }
    }

    fun observeCachedLessonCount(courseId: String): Flow<Int> {
        return dao.observeCachedLessonCount(courseId)
    }

    /**
     * Ensures the built-in Share Market & Equity Investing Mastery course and all of its
     * lessons are saved locally in Room for instant 100% offline study.
     */
    suspend fun ensureShareMarketCourseSeeded(makeActiveIfEmpty: Boolean = true): Course {
        val existing = dao.getCourseById(ShareMarketCatalog.SHARE_MARKET_COURSE_ID)
        val shareMarketCourse = ShareMarketCatalog.createShareMarketCourse(
            completedIds = existing?.let { EdamJsonParser.parseStringList(it.completedLessonsJson) } ?: emptyList()
        )
        val totalCourses = dao.getCourseCount()

        if (existing == null) {
            val shouldActivate = makeActiveIfEmpty && totalCourses == 0
            if (shouldActivate) {
                dao.clearActiveCourses()
            }
            dao.insertCourse(
                CourseEntity(
                    id = shareMarketCourse.id,
                    title = shareMarketCourse.title,
                    level = shareMarketCourse.level,
                    goal = shareMarketCourse.goal,
                    courseJson = EdamJsonParser.courseToJson(shareMarketCourse),
                    completedLessonsJson = EdamJsonParser.stringListToJson(shareMarketCourse.completed),
                    isActive = shouldActivate,
                    createdAt = shareMarketCourse.createdAt
                )
            )
        }

        // Pre-populate all Share Market lessons in Room so they work 100% offline immediately
        val prebuiltLessons = ShareMarketCatalog.getPrebuiltShareMarketLessons()
        val entities = prebuiltLessons.map { (lessonId, content) ->
            CachedLessonEntity(
                courseId = shareMarketCourse.id,
                lessonId = lessonId,
                lessonJson = EdamJsonParser.lessonToJson(content)
            )
        }
        dao.insertCachedLessons(entities)
        return shareMarketCourse
    }

    suspend fun activateShareMarketCourse(): Course {
        val course = ensureShareMarketCourseSeeded(makeActiveIfEmpty = false)
        dao.clearActiveCourses()
        dao.markCourseActive(course.id)
        return course
    }

    suspend fun createAndSaveCourse(
        courseName: String,
        level: String,
        goal: String
    ): Course {
        val isShareMarketTopic = courseName.contains("share market", ignoreCase = true) ||
            courseName.contains("stock market", ignoreCase = true) ||
            courseName.contains("investing", ignoreCase = true)

        val generatedCourse = try {
            aiService.generateCourse(
                courseName = courseName,
                level = level,
                goal = goal
            )
        } catch (e: Exception) {
            if (isShareMarketTopic) {
                ShareMarketCatalog.createShareMarketCourse().copy(
                    title = courseName,
                    level = level,
                    goal = goal
                )
            } else {
                throw e
            }
        }

        dao.clearActiveCourses()
        val entity = CourseEntity(
            id = generatedCourse.id,
            title = generatedCourse.title,
            level = generatedCourse.level,
            goal = generatedCourse.goal,
            courseJson = EdamJsonParser.courseToJson(generatedCourse),
            completedLessonsJson = EdamJsonParser.stringListToJson(generatedCourse.completed),
            isActive = true,
            createdAt = generatedCourse.createdAt
        )
        dao.insertCourse(entity)

        // Immediately bundle & save offline lessons for every unit/lesson in the course
        // so that after getting the course, the entire curriculum & quizzes work 100% offline!
        bundleCourseForOfflineUse(generatedCourse)
        syncCourseToCloudIfSignedIn(generatedCourse.id)

        return generatedCourse
    }

    /**
     * Ensures every lesson in [course] has an offline-ready LessonContent cached in Room.
     */
    suspend fun bundleCourseForOfflineUse(course: Course) {
        val existingIds = dao.getCachedLessonsForCourse(course.id).map { it.lessonId }.toSet()
        val newOfflineLessons = mutableListOf<CachedLessonEntity>()

        course.units.forEach { unit ->
            unit.lessons.forEach { lesson ->
                if (!existingIds.contains(lesson.id)) {
                    val offlineContent = ShareMarketCatalog.buildOfflineLessonFor(course, unit, lesson)
                    newOfflineLessons.add(
                        CachedLessonEntity(
                            courseId = course.id,
                            lessonId = lesson.id,
                            lessonJson = EdamJsonParser.lessonToJson(offlineContent)
                        )
                    )
                }
            }
        }

        if (newOfflineLessons.isNotEmpty()) {
            dao.insertCachedLessons(newOfflineLessons)
        }
    }

    suspend fun getOrGenerateLesson(
        course: Course,
        unit: CourseUnit,
        lesson: LessonSummary,
        forceRefresh: Boolean = false
    ): LessonContent {
        if (!forceRefresh) {
            val cached = dao.getCachedLesson(course.id, lesson.id)
            if (cached != null) {
                return EdamJsonParser.parseLessonContent(cached.lessonJson, lesson.title)
            }
        }

        val generated = try {
            aiService.generateLesson(
                course = course,
                unit = unit,
                lesson = lesson
            )
        } catch (_: Exception) {
            // Offline fallback: build structured offline lesson so the user can always study offline
            val cached = dao.getCachedLesson(course.id, lesson.id)
            if (cached != null) {
                EdamJsonParser.parseLessonContent(cached.lessonJson, lesson.title)
            } else {
                ShareMarketCatalog.buildOfflineLessonFor(course, unit, lesson)
            }
        }

        dao.insertCachedLesson(
            CachedLessonEntity(
                courseId = course.id,
                lessonId = lesson.id,
                lessonJson = EdamJsonParser.lessonToJson(generated)
            )
        )
        syncCourseToCloudIfSignedIn(course.id)
        return generated
    }

    suspend fun markLessonComplete(courseId: String, lessonId: String) {
        val entity = dao.getCourseById(courseId) ?: return
        val currentCompleted = EdamJsonParser.parseStringList(entity.completedLessonsJson).toMutableList()
        if (!currentCompleted.contains(lessonId)) {
            currentCompleted.add(lessonId)
            dao.updateCompletedLessons(
                courseId = courseId,
                completedJson = EdamJsonParser.stringListToJson(currentCompleted)
            )
            syncCourseToCloudIfSignedIn(courseId)
        }
    }

    suspend fun selectCourse(courseId: String) {
        dao.clearActiveCourses()
        dao.markCourseActive(courseId)
        val entity = dao.getCourseById(courseId)
        if (entity != null) {
            bundleCourseForOfflineUse(entity.toDomainCourse())
        }
    }

    suspend fun deleteCourse(courseId: String) {
        dao.deleteCourse(courseId)
    }

    suspend fun syncCourseToCloudIfSignedIn(courseId: String) {
        val cloud = cloudRepository ?: return
        val entity = dao.getCourseById(courseId) ?: return
        val cachedLessons = dao.getCachedLessonsForCourse(courseId)
        val offlineObj = JSONObject()
        cachedLessons.forEach { item ->
            offlineObj.put(item.lessonId, item.lessonJson)
        }
        cloud.saveCloudCourse(
            courseId = entity.id,
            title = entity.title,
            level = entity.level,
            goal = entity.goal,
            courseJson = entity.courseJson,
            completedLessonsJson = entity.completedLessonsJson,
            offlineLessonsJson = offlineObj.toString()
        )
    }

    private fun CourseEntity.toDomainCourse(): Course {
        val completedIds = EdamJsonParser.parseStringList(completedLessonsJson)
        return EdamJsonParser.parseCourse(
            rawJson = courseJson,
            fallbackTitle = title,
            fallbackLevel = level,
            fallbackGoal = goal,
            completedIds = completedIds,
            courseId = id,
            createdAt = createdAt
        )
    }
}
