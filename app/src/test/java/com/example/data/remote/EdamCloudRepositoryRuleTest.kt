package com.example.data.remote

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class EdamCloudRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun upsertAndGetUserAccount_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = EdamCloudRepository(firestore, auth)

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertUserAccount(
                displayName = "Alice Investor",
                email = ALICE_EMAIL,
                planTier = "MAX",
                promoCodeUsed = "X7PLD9Q2RM4JY1S8W",
                themeMode = "DARK"
            )
        }
        assertTrue("Expected upsertUserAccount to succeed", saveResult.isSuccess)

        val accountResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.getUserAccount(uid)
        }
        assertTrue(accountResult.isSuccess)
        val account = accountResult.getOrNull()
        assertNotNull(account)
        assertEquals("MAX", account?.planTier)
        assertEquals("X7PLD9Q2RM4JY1S8W", account?.promoCodeUsed)
    }

    @Test
    fun saveAndObserveCourse_authenticatedOwner_emitsCourse() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = EdamCloudRepository(firestore, auth)
        val courseId = "course_${UUID.randomUUID().toString().replace("-", "_")}"

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveCloudCourse(
                courseId = courseId,
                title = "Share Market & Investing Mastery",
                level = "Intermediate",
                goal = "Master stock analysis and risk management",
                courseJson = "{\"title\":\"Share Market\"}",
                completedLessonsJson = "[\"lesson_1\"]",
                offlineLessonsJson = "{\"lesson_1\":{}}"
            )
        }
        assertTrue(saveResult.isSuccess)

        val courses = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserCourses(uid).first { list -> list.any { it.id == courseId } }
        }
        assertTrue(courses.any { it.id == courseId })
    }

    @Test
    fun getCourseById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = EdamCloudRepository(firestore, auth)
        val courseId = "course_${UUID.randomUUID().toString().replace("-", "_")}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveCloudCourse(
                courseId = courseId,
                title = "Alice Private Course",
                level = "Beginner",
                goal = "Goal",
                courseJson = "{}",
                completedLessonsJson = "[]",
                offlineLessonsJson = "{}"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = EdamCloudRepository(firestore, auth)
        val bobAttempt = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getCourseById(aliceUid, courseId)
        }
        assertTrue(bobAttempt.isFailure)
        val ex = bobAttempt.exceptionOrNull() as? FirebaseFirestoreException
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    @Test
    fun observeUserCourses_unauthenticated_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = EdamCloudRepository(firestore, auth)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeUserCourses("unauth_user_123").first()
            }
            fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
        } catch (e: Exception) {
            val firestoreEx = generateSequence<Throwable>(e) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            assertNotNull("Expected underlying FirebaseFirestoreException", firestoreEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
        }
    }

    @Test
    fun upsertAndRecordDailyStreak_tracksConsecutiveDaysAndEnforcesOwnership() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = EdamCloudRepository(firestore, auth)
        val day1Millis = 20700L * 86_400_000L
        val day2Millis = 20701L * 86_400_000L

        val firstDayResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.recordConsecutiveDayActivityInCloud(xpEarned = 20, nowMillis = day1Millis)
        }
        assertTrue(firstDayResult.isSuccess)
        val firstDoc = firstDayResult.getOrThrow()
        assertEquals(1, firstDoc.currentStreak)

        val secondDayResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.recordConsecutiveDayActivityInCloud(xpEarned = 30, nowMillis = day2Millis)
        }
        assertTrue(secondDayResult.isSuccess)
        val secondDoc = secondDayResult.getOrThrow()
        assertEquals(2, secondDoc.currentStreak)
        assertTrue(secondDoc.longestStreak >= 2)

        signInTestUser(BOB_EMAIL)
        val bobRepo = EdamCloudRepository(firestore, auth)
        val bobReadAttempt = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getDailyStreak(targetUserId = aliceUid)
        }
        assertTrue(bobReadAttempt.isFailure)
        val ex = bobReadAttempt.exceptionOrNull() as? FirebaseFirestoreException
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@edam.test"
        const val BOB_EMAIL = "bob@edam.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
