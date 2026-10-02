package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val level: String,
    val goal: String,
    val courseJson: String,
    val completedLessonsJson: String,
    val isActive: Boolean,
    val createdAt: Long
)

@Entity(
    tableName = "cached_lessons",
    primaryKeys = ["courseId", "lessonId"]
)
data class CachedLessonEntity(
    val courseId: String,
    val lessonId: String,
    val lessonJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "earned_badges",
    primaryKeys = ["badgeId", "courseId"]
)
data class EarnedBadgeEntity(
    val badgeId: String,
    val courseId: String,
    val courseTitle: String,
    val badgeTitle: String,
    val description: String,
    val percentageRequired: Int,
    val iconKey: String,
    val colorCategory: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

@Dao
interface EdamDao {
    @Query("SELECT * FROM courses ORDER BY createdAt DESC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE isActive = 1 ORDER BY createdAt DESC LIMIT 1")
    fun getActiveCourse(): Flow<CourseEntity?>

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCourseCount(): Int

    @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
    suspend fun getCourseById(courseId: String): CourseEntity?

    @Query("UPDATE courses SET isActive = 0")
    suspend fun clearActiveCourses()

    @Query("UPDATE courses SET isActive = 1 WHERE id = :courseId")
    suspend fun markCourseActive(courseId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Query("UPDATE courses SET completedLessonsJson = :completedJson WHERE id = :courseId")
    suspend fun updateCompletedLessons(courseId: String, completedJson: String)

    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteCourse(courseId: String)

    @Query("DELETE FROM cached_lessons WHERE courseId = :courseId")
    suspend fun deleteCachedLessonsForCourse(courseId: String)

    @Query("SELECT * FROM courses ORDER BY createdAt DESC LIMIT 1")
    suspend fun getFirstCourse(): CourseEntity?

    @Query("SELECT * FROM cached_lessons WHERE courseId = :courseId AND lessonId = :lessonId LIMIT 1")
    suspend fun getCachedLesson(courseId: String, lessonId: String): CachedLessonEntity?

    @Query("SELECT * FROM cached_lessons WHERE courseId = :courseId")
    suspend fun getCachedLessonsForCourse(courseId: String): List<CachedLessonEntity>

    @Query("SELECT COUNT(*) FROM cached_lessons WHERE courseId = :courseId")
    fun observeCachedLessonCount(courseId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedLesson(cachedLesson: CachedLessonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedLessons(cachedLessons: List<CachedLessonEntity>)

    @Query("SELECT * FROM earned_badges ORDER BY unlockedAt DESC")
    fun getAllEarnedBadges(): Flow<List<EarnedBadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEarnedBadge(badge: EarnedBadgeEntity)

    @Query("SELECT * FROM earned_badges WHERE badgeId = :badgeId AND courseId = :courseId LIMIT 1")
    suspend fun getEarnedBadge(badgeId: String, courseId: String): EarnedBadgeEntity?

    @Query("SELECT COUNT(*) FROM earned_badges")
    fun observeEarnedBadgeCount(): Flow<Int>

    // Screen Entity Queries
    @Query("SELECT * FROM screens ORDER BY visitCount DESC")
    fun getAllScreens(): Flow<List<ScreenEntity>>

    @Query("SELECT * FROM screens WHERE screenId = :screenId LIMIT 1")
    suspend fun getScreenById(screenId: String): ScreenEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreen(screen: ScreenEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreens(screens: List<ScreenEntity>)

    @Query("UPDATE screens SET visitCount = visitCount + 1, lastVisitedAt = :timestamp WHERE screenId = :screenId")
    suspend fun recordScreenVisit(screenId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM screens")
    suspend fun getScreenCount(): Int

    // Competitive Leaderboard Queries
    @Query("SELECT * FROM leaderboard_entries WHERE leagueId = :leagueId ORDER BY weeklyXp DESC")
    fun getLeaderboardForLeague(leagueId: String): Flow<List<LeaderboardEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntries(entries: List<LeaderboardEntryEntity>)

    @Query("UPDATE leaderboard_entries SET weeklyXp = weeklyXp + :xpBonus, updatedAt = :timestamp WHERE isCurrentUser = 1")
    suspend fun addXpToCurrentUser(xpBonus: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE leaderboard_entries SET leagueId = :newLeagueId, updatedAt = :timestamp WHERE isCurrentUser = 1")
    suspend fun updateCurrentUserLeague(newLeagueId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM leaderboard_entries WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUserLeaderboardEntry(): LeaderboardEntryEntity?

    @Query("SELECT COUNT(*) FROM leaderboard_entries WHERE leagueId = :leagueId")
    suspend fun getLeaderboardCount(leagueId: String): Int
}

@Entity(tableName = "screens")
data class ScreenEntity(
    @PrimaryKey val screenId: String,
    val title: String,
    val route: String,
    val description: String = "",
    val iconKey: String = "home",
    val isEnabled: Boolean = true,
    val visitCount: Int = 0,
    val lastVisitedAt: Long = System.currentTimeMillis(),
    val metadataJson: String = "{}"
)

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntryEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val avatarColorHex: Long,
    val avatarInitial: String,
    val leagueId: String,
    val weeklyXp: Int,
    val streakDays: Int,
    val isCurrentUser: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Database(
    entities = [
        CourseEntity::class,
        CachedLessonEntity::class,
        EarnedBadgeEntity::class,
        ScreenEntity::class,
        LeaderboardEntryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class EdamDatabase : RoomDatabase() {
    abstract fun edamDao(): EdamDao

    companion object {
        @Volatile
        private var INSTANCE: EdamDatabase? = null

        fun getInstance(context: Context): EdamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EdamDatabase::class.java,
                    "edam_learning.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
