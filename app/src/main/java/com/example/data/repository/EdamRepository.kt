package com.example.data.repository

import android.content.Context
import com.example.data.local.CachedLessonEntity
import com.example.data.local.CourseEntity
import com.example.data.local.EarnedBadgeEntity
import com.example.data.local.EdamDao
import com.example.data.local.LeaderboardEntryEntity
import com.example.data.local.ScreenEntity
import com.example.data.model.BadgeCatalog
import com.example.data.model.ChessAndFlashcardCatalog
import com.example.data.model.Course
import com.example.data.model.CourseOutline
import com.example.data.model.CourseUnit
import com.example.data.model.EdamJsonParser
import com.example.data.model.LeagueTier
import com.example.data.model.LessonContent
import com.example.data.model.LessonSummary
import com.example.data.model.ShareMarketCatalog
import com.example.data.remote.EdamAiService
import com.example.data.remote.EdamCloudRepository
import com.example.notification.EdamNotificationHelper
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

    val earnedBadgesFlow: Flow<List<EarnedBadgeEntity>> = dao.getAllEarnedBadges()

    val screensFlow: Flow<List<ScreenEntity>> = dao.getAllScreens()

    suspend fun ensureDefaultScreensSeeded() {
        if (dao.getScreenCount() == 0) {
            val defaults = listOf(
                ScreenEntity(
                    screenId = "screen_home",
                    title = "Course Dashboard",
                    route = "home",
                    description = "Active learning path, course outlines, and offline lessons",
                    iconKey = "school",
                    visitCount = 1
                ),
                ScreenEntity(
                    screenId = "screen_course_studio",
                    title = "Edam Course Studio",
                    route = "course_studio",
                    description = "Generate structured course outlines with Edam",
                    iconKey = "auto_awesome",
                    visitCount = 0
                ),
                ScreenEntity(
                    screenId = "screen_leaderboard",
                    title = "Duolingo Leagues",
                    route = "leaderboard",
                    description = "Weekly competitive leagues, promotion zones, and rapid XP drills",
                    iconKey = "emoji_events",
                    visitCount = 0
                ),
                ScreenEntity(
                    screenId = "screen_market",
                    title = "Trending Market",
                    route = "market",
                    description = "Real-time course engagement candles and learner volume",
                    iconKey = "show_chart",
                    visitCount = 0
                ),
                ScreenEntity(
                    screenId = "screen_profile",
                    title = "Learner Profile",
                    route = "profile",
                    description = "Earned milestone badges and local storage status",
                    iconKey = "military_tech",
                    visitCount = 0
                )
            )
            dao.insertScreens(defaults)
        }
    }

    suspend fun recordScreenVisit(screenId: String) {
        dao.recordScreenVisit(screenId)
    }

    fun leaderboardFlow(leagueId: String): Flow<List<LeaderboardEntryEntity>> {
        return dao.getLeaderboardForLeague(leagueId)
    }

    suspend fun ensureLeaderboardSeeded(
        leagueId: String,
        currentUserName: String,
        initialUserXp: Int
    ) {
        if (dao.getLeaderboardCount(leagueId) == 0) {
            val competitors = createDefaultCompetitorsForLeague(
                leagueId = leagueId,
                currentUserName = currentUserName,
                userXp = initialUserXp
            )
            dao.insertLeaderboardEntries(competitors)
        }
    }

    suspend fun awardLeaderboardXp(xpBonus: Int) {
        dao.addXpToCurrentUser(xpBonus)
    }

    suspend fun updateCurrentUserLeague(newLeagueId: String) {
        dao.updateCurrentUserLeague(newLeagueId)
    }

    suspend fun generateCourseOutline(
        topic: String,
        level: String,
        goal: String,
        focusArea: String,
        unitCount: Int = 5
    ): CourseOutline {
        return aiService.generateCourseOutline(
            topic = topic,
            level = level,
            goal = goal,
            focusArea = focusArea,
            unitCount = unitCount
        )
    }

    suspend fun saveAndActivateCourseOutline(outline: CourseOutline): Course {
        val course = outline.toCourse()
        dao.clearActiveCourses()
        dao.insertCourse(
            CourseEntity(
                id = course.id,
                title = course.title,
                level = course.level,
                goal = course.goal,
                courseJson = EdamJsonParser.courseToJson(course),
                completedLessonsJson = EdamJsonParser.stringListToJson(course.completed),
                isActive = true,
                createdAt = course.createdAt
            )
        )
        bundleCourseForOfflineUse(course)
        syncCourseToCloudIfSignedIn(course.id)
        return course
    }

    private fun createDefaultCompetitorsForLeague(
        leagueId: String,
        currentUserName: String,
        userXp: Int
    ): List<LeaderboardEntryEntity> {
        val tier = LeagueTier.entries.firstOrNull { it.tierId == leagueId } ?: LeagueTier.BRONZE
        val baseTierXp = tier.minXpRequired
        val topScore = baseTierXp + 650

        val names = listOf(
            Triple("Alexandre Chen", 0xFF6366F1, "A"),
            Triple("Sophia Rodriguez", 0xFF10B981, "S"),
            Triple("Marcus Sterling", 0xFFF59E0B, "M"),
            Triple("Elena Rostova", 0xFFEC4899, "E"),
            Triple("Liam O'Connor", 0xFF06B6D4, "L"),
            Triple("Aria Nakamura", 0xFF8B5CF6, "A"),
            Triple("Vikram Patel", 0xFF3B82F6, "V"),
            Triple("Chloe Dubois", 0xFF14B8A6, "C"),
            Triple("Julian Alva", 0xFFF43F5E, "J"),
            Triple("Fatima Al-Mansoor", 0xFF84CC16, "F"),
            Triple("Ethan Huntley", 0xFFEAB308, "E"),
            Triple("Zoe Kravitz", 0xFF64748B, "Z"),
            Triple("Gabriel Santos", 0xFF0EA5E9, "G"),
            Triple("Maya Lin", 0xFFA855F7, "M"),
            Triple("David Kim", 0xFFD946EF, "D"),
            Triple("Isabella Morales", 0xFF22C55E, "I"),
            Triple("Noah Jensen", 0xFFF97316, "N"),
            Triple("Hannah Becker", 0xFF6B7280, "H"),
            Triple("Lucas Bennett", 0xFF0284C7, "L"),
            Triple("Amara Okafor", 0xFF9333EA, "A")
        )

        val entries = mutableListOf<LeaderboardEntryEntity>()
        var currentCompetitorScore = topScore

        for (i in names.indices) {
            val (name, color, initial) = names[i]
            val streak = 3 + (i * 2) % 25
            entries.add(
                LeaderboardEntryEntity(
                    userId = "comp_${leagueId}_$i",
                    displayName = name,
                    avatarColorHex = color,
                    avatarInitial = initial,
                    leagueId = leagueId,
                    weeklyXp = currentCompetitorScore,
                    streakDays = streak,
                    isCurrentUser = false
                )
            )
            currentCompetitorScore -= (25 + (i * 7) % 35)
        }

        val effectiveUserXp = if (userXp > 0) userXp else (baseTierXp + 380)
        entries.add(
            LeaderboardEntryEntity(
                userId = "current_user_id",
                displayName = if (currentUserName.isNotBlank()) currentUserName else "You",
                avatarColorHex = 0xFF7C3AED,
                avatarInitial = if (currentUserName.isNotBlank()) currentUserName.take(1).uppercase() else "Y",
                leagueId = leagueId,
                weeklyXp = effectiveUserXp,
                streakDays = 5,
                isCurrentUser = true
            )
        )

        return entries.sortedByDescending { it.weeklyXp }
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

    /**
     * Ensures the Novice to Grandmaster (GM) Chess Mastery course and all 15 lessons
     * are seeded locally in Room for instant offline study.
     */
    suspend fun ensureChessGmCourseSeeded(): Course {
        val existing = dao.getCourseById(ChessAndFlashcardCatalog.CHESS_GM_COURSE_ID)
        val chessCourse = ChessAndFlashcardCatalog.createNoviceToGmChessCourse().copy(
            completed = existing?.let { EdamJsonParser.parseStringList(it.completedLessonsJson) } ?: emptyList()
        )
        if (existing == null) {
            dao.insertCourse(
                CourseEntity(
                    id = chessCourse.id,
                    title = chessCourse.title,
                    level = chessCourse.level,
                    goal = chessCourse.goal,
                    courseJson = EdamJsonParser.courseToJson(chessCourse),
                    completedLessonsJson = EdamJsonParser.stringListToJson(chessCourse.completed),
                    isActive = false,
                    createdAt = chessCourse.createdAt
                )
            )
        }
        val lessonEntities = chessCourse.units.flatMap { unit ->
            unit.lessons.map { lesson ->
                val content = ChessAndFlashcardCatalog.getOfflineChessLessonContent(lesson.id, lesson.title)
                CachedLessonEntity(
                    courseId = chessCourse.id,
                    lessonId = lesson.id,
                    lessonJson = EdamJsonParser.lessonToJson(content)
                )
            }
        }
        dao.insertCachedLessons(lessonEntities)
        return chessCourse
    }

    suspend fun activateChessGmCourse(): Course {
        val course = ensureChessGmCourseSeeded()
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

    suspend fun markLessonComplete(
        courseId: String,
        lessonId: String,
        context: Context? = null,
        pushEnabled: Boolean = true
    ): List<EarnedBadgeEntity> {
        val entity = dao.getCourseById(courseId) ?: return emptyList()
        val currentCompleted = EdamJsonParser.parseStringList(entity.completedLessonsJson).toMutableList()
        if (!currentCompleted.contains(lessonId)) {
            currentCompleted.add(lessonId)
            dao.updateCompletedLessons(
                courseId = courseId,
                completedJson = EdamJsonParser.stringListToJson(currentCompleted)
            )
            syncCourseToCloudIfSignedIn(courseId)
        }
        val updated = dao.getCourseById(courseId)?.toDomainCourse() ?: return emptyList()
        return evaluateAndUnlockCourseBadges(updated, context, pushEnabled)
    }

    suspend fun evaluateAndUnlockCourseBadges(
        course: Course,
        context: Context? = null,
        pushEnabled: Boolean = true
    ): List<EarnedBadgeEntity> {
        val newlyEarned = mutableListOf<EarnedBadgeEntity>()
        val qualifying = BadgeCatalog.evaluateBadgesForCourse(course)
        for (def in qualifying) {
            val existing = dao.getEarnedBadge(def.id, course.id)
            if (existing == null) {
                val newBadge = EarnedBadgeEntity(
                    badgeId = def.id,
                    courseId = course.id,
                    courseTitle = course.title,
                    badgeTitle = def.title,
                    description = def.description,
                    percentageRequired = def.percentageRequired,
                    iconKey = def.iconKey,
                    colorCategory = def.colorCategory
                )
                dao.insertEarnedBadge(newBadge)
                newlyEarned.add(newBadge)
                if (context != null && pushEnabled) {
                    EdamNotificationHelper.sendBadgeUnlockedNotification(
                        context = context,
                        badgeTitle = def.title,
                        courseTitle = course.title,
                        percentage = course.progressPercentage
                    )
                }
            }
        }
        return newlyEarned
    }

    suspend fun recordBadgeUnlocked(
        badgeId: String,
        badgeTitle: String,
        percentageRequired: Int,
        courseId: String,
        courseTitle: String,
        description: String = "",
        iconKey: String = "military_tech",
        colorCategory: String = "gold"
    ): EarnedBadgeEntity {
        val existing = dao.getEarnedBadge(badgeId, courseId)
        if (existing != null) return existing
        val newBadge = EarnedBadgeEntity(
            badgeId = badgeId,
            courseId = courseId,
            courseTitle = courseTitle,
            badgeTitle = badgeTitle,
            description = description.ifBlank { "Earned milestone: $badgeTitle" },
            percentageRequired = percentageRequired,
            iconKey = iconKey,
            colorCategory = colorCategory
        )
        dao.insertEarnedBadge(newBadge)
        return newBadge
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
        val target = dao.getCourseById(courseId)
        val wasActive = target?.isActive == true
        dao.deleteCourse(courseId)
        dao.deleteCachedLessonsForCourse(courseId)
        if (wasActive) {
            val nextCourse = dao.getFirstCourse()
            if (nextCourse != null) {
                dao.markCourseActive(nextCourse.id)
            } else {
                ensureShareMarketCourseSeeded(makeActiveIfEmpty = true)
            }
        }
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
