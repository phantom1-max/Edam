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
}

@Database(
    entities = [CourseEntity::class, CachedLessonEntity::class],
    version = 1,
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
