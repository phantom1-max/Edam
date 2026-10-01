package com.example.data.repository

import com.example.data.local.CachedLessonEntity
import com.example.data.local.CourseEntity
import com.example.data.local.EdamDao
import com.example.data.model.Course
import com.example.data.model.CourseUnit
import com.example.data.model.EdamJsonParser
import com.example.data.model.LessonContent
import com.example.data.model.LessonSummary
import com.example.data.remote.EdamAiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EdamRepository(
    private val dao: EdamDao,
    private val aiService: EdamAiService = EdamAiService()
) {
    val activeCourseFlow: Flow<Course?> = dao.getActiveCourse().map { entity ->
        entity?.toDomainCourse()
    }

    val allCoursesFlow: Flow<List<Course>> = dao.getAllCourses().map { list ->
        list.map { it.toDomainCourse() }
    }

    suspend fun createAndSaveCourse(
        courseName: String,
        level: String,
        goal: String
    ): Course {
        val generatedCourse = aiService.generateCourse(
            courseName = courseName,
            level = level,
            goal = goal
        )
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
        return generatedCourse
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

        val generated = aiService.generateLesson(
            course = course,
            unit = unit,
            lesson = lesson
        )
        dao.insertCachedLesson(
            CachedLessonEntity(
                courseId = course.id,
                lessonId = lesson.id,
                lessonJson = EdamJsonParser.lessonToJson(generated)
            )
        )
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
        }
    }

    suspend fun selectCourse(courseId: String) {
        dao.clearActiveCourses()
        dao.markCourseActive(courseId)
    }

    suspend fun deleteCourse(courseId: String) {
        val wasActive = dao.getCourseById(courseId)?.isActive == true
        dao.deleteCourse(courseId)
        if (wasActive) {
            // Activate most recent remaining course if any
        }
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
