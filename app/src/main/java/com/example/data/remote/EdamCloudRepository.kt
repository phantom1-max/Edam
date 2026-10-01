package com.example.data.remote

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
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
}
