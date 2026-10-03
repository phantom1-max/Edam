package com.example.data.remote

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.snapshots
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(
    exception: Exception,
    operationType: OperationType,
    path: String?
): String {
    val currentUser = try {
        FirebaseAuth.getInstance().currentUser
    } catch (_: Exception) {
        null
    }

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

data class UserAccountDoc(
    val userId: String = "",
    val displayName: String = "",
    val email: String? = null,
    val planTier: String = "BASIC",
    val promoCodeUsed: String? = null,
    val themeMode: String = "SYSTEM_DEFAULT",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudCourseDoc(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val level: String = "",
    val goal: String = "",
    val courseJson: String = "{}",
    val completedLessonsJson: String = "[]",
    val offlineLessonsJson: String = "{}",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudDailyStreakDoc(
    val id: String = "daily",
    val userId: String = "",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastStudyEpochDay: Long = 0L,
    val totalXp: Int = 1420,
    val weeklyStudyDaysCsv: String = "1,1,1,0,0,0,0",
    val streakFreezeActive: Boolean = true,
    val streakFreezesAvailable: Int = 2,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudUserBadgeDoc(
    val id: String = "",
    val userId: String = "",
    val badgeKey: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "COURSE",
    val iconKey: String = "scholar",
    val progressPct: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

class EdamCloudRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun upsertUserAccount(
        displayName: String,
        email: String?,
        planTier: String,
        promoCodeUsed: String?,
        themeMode: String
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                val createPayload = mapOf(
                    "userId" to uid,
                    "displayName" to displayName.take(120).ifBlank { "Learner" },
                    "email" to email?.take(254),
                    "planTier" to planTier,
                    "promoCodeUsed" to promoCodeUsed?.take(64),
                    "themeMode" to themeMode.take(64),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(createPayload).await()
            } else {
                val updatePayload = mapOf(
                    "displayName" to displayName.take(120).ifBlank { "Learner" },
                    "email" to email?.take(254),
                    "planTier" to planTier,
                    "promoCodeUsed" to promoCodeUsed?.take(64),
                    "themeMode" to themeMode.take(64),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.update(updatePayload).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getUserAccount(userId: String = requireUserId()): Result<UserAccountDoc?> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                Result.success(null)
            } else {
                val doc = snapshot.toObject(
                    UserAccountDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                Result.success(doc)
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    fun observeUserAccount(userId: String): Flow<UserAccountDoc?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        UserAccountDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, docRef.path)
                }
                throw error
            }
    }

    suspend fun saveCloudCourse(
        courseId: String,
        title: String,
        level: String,
        goal: String,
        courseJson: String,
        completedLessonsJson: String,
        offlineLessonsJson: String
    ): Result<String> {
        val uid = requireUserId()
        val safeCourseId = courseId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128).ifBlank { "course_1" }
        val docRef = db.collection("users").document(uid).collection("courses").document(safeCourseId)
        return try {
            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                val payload = mapOf(
                    "id" to safeCourseId,
                    "userId" to uid,
                    "title" to title.take(200).ifBlank { "Course" },
                    "level" to level.take(64).ifBlank { "Beginner" },
                    "goal" to goal.take(1000).ifBlank { "Learn" },
                    "courseJson" to courseJson.take(200000).ifBlank { "{}" },
                    "completedLessonsJson" to completedLessonsJson.take(50000).ifBlank { "[]" },
                    "offlineLessonsJson" to offlineLessonsJson.take(700000).ifBlank { "{}" },
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updatePayload = mapOf(
                    "title" to title.take(200).ifBlank { "Course" },
                    "level" to level.take(64).ifBlank { "Beginner" },
                    "goal" to goal.take(1000).ifBlank { "Learn" },
                    "courseJson" to courseJson.take(200000).ifBlank { "{}" },
                    "completedLessonsJson" to completedLessonsJson.take(50000).ifBlank { "[]" },
                    "offlineLessonsJson" to offlineLessonsJson.take(700000).ifBlank { "{}" },
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
            Result.success(safeCourseId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getCourseById(targetUserId: String, courseId: String): Result<CloudCourseDoc?> {
        val docRef = db.collection("users").document(targetUserId).collection("courses").document(courseId)
        return try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                Result.success(null)
            } else {
                Result.success(
                    snap.toObject(
                        CloudCourseDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                )
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    fun observeUserCourses(userId: String): Flow<List<CloudCourseDoc>> {
        val collectionRef = db.collection("users").document(userId).collection("courses")
        return collectionRef
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { querySnap ->
                querySnap.toObjects(
                    CloudCourseDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, collectionRef.path)
                }
                throw error
            }
    }

    suspend fun upsertDailyStreak(
        currentStreak: Int,
        longestStreak: Int,
        lastStudyEpochDay: Long,
        totalXp: Int,
        weeklyStudyDaysCsv: String,
        streakFreezeActive: Boolean = true,
        streakFreezesAvailable: Int = 2,
        streakId: String = "daily"
    ): Result<CloudDailyStreakDoc> {
        val uid = requireUserId()
        val safeStreakId = streakId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128).ifBlank { "daily" }
        val docRef = db.collection("users").document(uid).collection("streaks").document(safeStreakId)
        return try {
            val safeCurrent = currentStreak.coerceIn(0, 100000)
            val safeLongest = longestStreak.coerceAtLeast(safeCurrent).coerceIn(0, 100000)
            val safeEpochDay = lastStudyEpochDay.coerceIn(0L, 10000000L)
            val safeXp = totalXp.coerceIn(0, 100000000)
            val safeCsv = weeklyStudyDaysCsv.take(32).ifBlank { "1,0,0,0,0,0,0" }
            val safeFreezes = streakFreezesAvailable.coerceIn(0, 100)

            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                val payload = mapOf(
                    "id" to safeStreakId,
                    "userId" to uid,
                    "currentStreak" to safeCurrent,
                    "longestStreak" to safeLongest,
                    "lastStudyEpochDay" to safeEpochDay,
                    "totalXp" to safeXp,
                    "weeklyStudyDaysCsv" to safeCsv,
                    "streakFreezeActive" to streakFreezeActive,
                    "streakFreezesAvailable" to safeFreezes,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updatePayload = mapOf(
                    "currentStreak" to safeCurrent,
                    "longestStreak" to safeLongest,
                    "lastStudyEpochDay" to safeEpochDay,
                    "totalXp" to safeXp,
                    "weeklyStudyDaysCsv" to safeCsv,
                    "streakFreezeActive" to streakFreezeActive,
                    "streakFreezesAvailable" to safeFreezes,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
            Result.success(
                CloudDailyStreakDoc(
                    id = safeStreakId,
                    userId = uid,
                    currentStreak = safeCurrent,
                    longestStreak = safeLongest,
                    lastStudyEpochDay = safeEpochDay,
                    totalXp = safeXp,
                    weeklyStudyDaysCsv = safeCsv,
                    streakFreezeActive = streakFreezeActive,
                    streakFreezesAvailable = safeFreezes
                )
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun recordConsecutiveDayActivityInCloud(
        xpEarned: Int = 20,
        nowMillis: Long = System.currentTimeMillis(),
        streakId: String = "daily"
    ): Result<CloudDailyStreakDoc> {
        val uid = requireUserId()
        val safeStreakId = streakId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128).ifBlank { "daily" }
        val docRef = db.collection("users").document(uid).collection("streaks").document(safeStreakId)
        val todayEpochDay = TimeUnit.MILLISECONDS.toDays(nowMillis).coerceIn(0L, 10000000L)
        return try {
            val snap = docRef.get().await()
            val existing = if (snap.exists()) {
                snap.toObject(
                    CloudDailyStreakDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else {
                null
            }

            val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val mondayIndex = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }

            val prevStreak = existing?.currentStreak ?: 0
            val prevLongest = existing?.longestStreak ?: 0
            val prevLastDay = existing?.lastStudyEpochDay ?: -1L
            var freezeActive = existing?.streakFreezeActive ?: true
            var freezesAvail = existing?.streakFreezesAvailable ?: 2

            val newCurrentStreak = when {
                prevLastDay == todayEpochDay -> prevStreak.coerceAtLeast(1)
                prevLastDay == todayEpochDay - 1L -> prevStreak + 1
                todayEpochDay - prevLastDay == 2L && freezeActive && freezesAvail > 0 -> {
                    freezesAvail = (freezesAvail - 1).coerceAtLeast(0)
                    freezeActive = freezesAvail > 0
                    prevStreak + 1
                }
                else -> 1
            }.coerceIn(1, 100000)

            val newLongestStreak = maxOf(prevLongest, newCurrentStreak).coerceIn(newCurrentStreak, 100000)
            val newTotalXp = ((existing?.totalXp ?: 1420) + xpEarned.coerceAtLeast(0)).coerceIn(0, 100000000)

            val daysList = (existing?.weeklyStudyDaysCsv ?: "0,0,0,0,0,0,0")
                .split(",")
                .map { it.trim() == "1" }
                .let { parsed -> if (parsed.size == 7) parsed.toMutableList() else MutableList(7) { false } }
            if (mondayIndex in 0..6) {
                daysList[mondayIndex] = true
            }
            val updatedCsv = daysList.joinToString(",") { if (it) "1" else "0" }

            upsertDailyStreak(
                currentStreak = newCurrentStreak,
                longestStreak = newLongestStreak,
                lastStudyEpochDay = todayEpochDay,
                totalXp = newTotalXp,
                weeklyStudyDaysCsv = updatedCsv,
                streakFreezeActive = freezeActive,
                streakFreezesAvailable = freezesAvail,
                streakId = safeStreakId
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getDailyStreak(
        targetUserId: String = requireUserId(),
        streakId: String = "daily"
    ): Result<CloudDailyStreakDoc?> {
        val docRef = db.collection("users").document(targetUserId).collection("streaks").document(streakId)
        return try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                Result.success(null)
            } else {
                Result.success(
                    snap.toObject(
                        CloudDailyStreakDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                )
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    fun observeUserDailyStreak(
        userId: String,
        streakId: String = "daily"
    ): Flow<CloudDailyStreakDoc?> {
        val docRef = db.collection("users").document(userId).collection("streaks").document(streakId)
        return docRef
            .snapshots()
            .map { snap ->
                if (!snap.exists()) {
                    null
                } else {
                    snap.toObject(
                        CloudDailyStreakDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, docRef.path)
                }
                throw error
            }
    }

    suspend fun upsertUserBadge(
        badgeId: String,
        badgeKey: String,
        title: String,
        description: String,
        category: String,
        iconKey: String,
        progressPct: Int,
        isUnlocked: Boolean,
        unlockedAt: Long? = null
    ): Result<CloudUserBadgeDoc> {
        val uid = requireUserId()
        val safeBadgeId = badgeId.filter { it.isLetterOrDigit() || it == '_' || it == '-' }.take(128).ifBlank { "badge" }
        val docRef = db.collection("users").document(uid).collection("badges").document(safeBadgeId)

        return try {
            val existingSnap = docRef.get().await()
            val safeProgress = progressPct.coerceIn(0, 100)
            if (!existingSnap.exists()) {
                val payload = mutableMapOf<String, Any>(
                    "id" to safeBadgeId,
                    "userId" to uid,
                    "badgeKey" to badgeKey.take(128),
                    "title" to title.take(120),
                    "description" to description.take(500),
                    "category" to category.take(64),
                    "iconKey" to iconKey.take(64),
                    "progressPct" to safeProgress,
                    "isUnlocked" to isUnlocked,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (unlockedAt != null) {
                    payload["unlockedAt"] = unlockedAt
                }
                docRef.set(payload).await()
            } else {
                val updates = mutableMapOf<String, Any>(
                    "title" to title.take(120),
                    "description" to description.take(500),
                    "category" to category.take(64),
                    "iconKey" to iconKey.take(64),
                    "progressPct" to safeProgress,
                    "isUnlocked" to isUnlocked,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (unlockedAt != null) {
                    updates["unlockedAt"] = unlockedAt
                }
                docRef.update(updates).await()
            }
            val updated = docRef.get().await().toObject(
                CloudUserBadgeDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: CloudUserBadgeDoc(
                id = safeBadgeId,
                userId = uid,
                badgeKey = badgeKey,
                title = title,
                description = description,
                category = category,
                iconKey = iconKey,
                progressPct = safeProgress,
                isUnlocked = isUnlocked,
                unlockedAt = unlockedAt
            )
            Result.success(updated)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getUserBadges(targetUserId: String = requireUserId()): Result<List<CloudUserBadgeDoc>> {
        val colRef = db.collection("users").document(targetUserId).collection("badges")
        return try {
            val snap = colRef.get().await()
            val list = snap.toObjects(
                CloudUserBadgeDoc::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(list)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, colRef.path)
            Result.failure(e)
        }
    }

    fun observeUserBadges(userId: String): Flow<List<CloudUserBadgeDoc>> {
        val colRef = db.collection("users").document(userId).collection("badges")
        return colRef
            .snapshots()
            .map { snap ->
                snap.toObjects(
                    CloudUserBadgeDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, colRef.path)
                }
                throw error
            }
    }
}
