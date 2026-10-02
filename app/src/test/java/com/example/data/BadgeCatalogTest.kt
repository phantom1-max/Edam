package com.example.data

import com.example.data.local.ScreenEntity
import com.example.data.model.BadgeCatalog
import com.example.data.model.Course
import com.example.data.model.CourseOutline
import com.example.data.model.CourseUnit
import com.example.data.model.LeaderboardDrillCatalog
import com.example.data.model.LeagueTier
import com.example.data.model.LessonOutlineItem
import com.example.data.model.LessonSummary
import com.example.data.model.ShareMarketCatalog
import com.example.data.model.UnitOutlineItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BadgeCatalogTest {

    private fun createMockCourse(
        id: String = "test_course",
        totalLessonsCount: Int = 10,
        completedCount: Int = 0
    ): Course {
        val lessons = (1..totalLessonsCount).map {
            LessonSummary(id = "lesson_$it", title = "Lesson $it", summary = "Summary $it")
        }
        val unit = CourseUnit(
            id = "unit_1",
            title = "Unit 1",
            description = "Unit 1 description",
            lessons = lessons
        )
        val completedIds = (1..completedCount).map { "lesson_$it" }
        return Course(
            id = id,
            title = "Test Course",
            level = "Beginner",
            goal = "Master Testing",
            description = "Test description",
            outcomes = listOf("Learn testing"),
            units = listOf(unit),
            completed = completedIds
        )
    }

    @Test
    fun `course with zero progress earns zero badges`() {
        val course = createMockCourse(totalLessonsCount = 10, completedCount = 0)
        val badges = BadgeCatalog.evaluateBadgesForCourse(course)
        assertTrue(badges.isEmpty())
    }

    @Test
    fun `course with 10 percent progress earns first spark badge`() {
        val course = createMockCourse(totalLessonsCount = 10, completedCount = 1) // 10%
        val badges = BadgeCatalog.evaluateBadgesForCourse(course)
        assertEquals(1, badges.size)
        assertEquals("badge_10", badges[0].id)
    }

    @Test
    fun `course with 50 percent progress earns 10, 25 and 50 percent badges`() {
        val course = createMockCourse(totalLessonsCount = 10, completedCount = 5) // 50%
        val badges = BadgeCatalog.evaluateBadgesForCourse(course)
        assertEquals(3, badges.size)
        val badgeIds = badges.map { it.id }.toSet()
        assertTrue(badgeIds.contains("badge_10"))
        assertTrue(badgeIds.contains("badge_25"))
        assertTrue(badgeIds.contains("badge_50"))
    }

    @Test
    fun `share market course at 100 percent unlocks Wall Street Virtuoso badge`() {
        val course = createMockCourse(
            id = ShareMarketCatalog.SHARE_MARKET_COURSE_ID,
            totalLessonsCount = 10,
            completedCount = 10
        )
        val badges = BadgeCatalog.evaluateBadgesForCourse(course)
        val badgeIds = badges.map { it.id }.toSet()
        assertTrue(badgeIds.contains("badge_shmkt_mastery"))
        assertTrue(badgeIds.contains("badge_100"))
    }

    @Test
    fun `course outline converts cleanly to domain course`() {
        val outline = CourseOutline(
            id = "outline_ai_1",
            title = "Generative AI Mastery",
            level = "Intermediate",
            goal = "Build LLM agents",
            focusArea = "Applied Engineering",
            description = "Comprehensive LLM syllabus",
            estimatedHours = 8,
            totalXp = 450,
            outcomes = listOf("Understand transformers", "Build RAG pipelines"),
            units = listOf(
                UnitOutlineItem(
                    id = "u_1",
                    title = "Unit 1: Transformers",
                    description = "Attention mechanisms",
                    lessons = listOf(
                        LessonOutlineItem(
                            id = "l_1",
                            title = "Attention Is All You Need",
                            summary = "Multi-head attention fundamentals",
                            keyConcepts = listOf("Self-Attention", "Tokens")
                        )
                    )
                )
            )
        )
        val domainCourse = outline.toCourse()
        assertEquals("outline_ai_1", domainCourse.id)
        assertEquals("Generative AI Mastery", domainCourse.title)
        assertEquals(1, domainCourse.units.size)
        assertEquals(1, domainCourse.totalLessons)
    }

    @Test
    fun `duolingo league tiers progress from bronze to diamond`() {
        assertEquals(LeagueTier.SILVER, LeagueTier.BRONZE.nextLeague())
        assertEquals(LeagueTier.RUBY, LeagueTier.SAPPHIRE.nextLeague())
        assertEquals(LeagueTier.GOLD, LeagueTier.SAPPHIRE.previousLeague())
        val drill = LeaderboardDrillCatalog.getRandomDrill()
        assertNotNull(drill)
        assertTrue(drill.options.isNotEmpty())
    }

    @Test
    fun `screen entity stores route and telemetry`() {
        val screen = ScreenEntity(
            screenId = "screen_course_studio",
            title = "AI Course Architect",
            route = "course_studio",
            visitCount = 5
        )
        assertEquals("course_studio", screen.route)
        assertEquals(5, screen.visitCount)
    }
}
