package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.remote.CloudDailyStreakDoc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class DailyStreakState(
    val currentStreak: Int = 1,
    val longestStreak: Int = 1,
    val totalXp: Int = 1420,
    val studiedToday: Boolean = false,
    val lastStudyEpochDay: Long = 0L,
    val weeklyStudyDays: List<Boolean> = listOf(true, true, true, false, false, false, false),
    val flashcardsReviewedCount: Int = 0,
    val chessPuzzlesSolvedCount: Int = 0,
    val streakFreezeActive: Boolean = true,
    val streakFreezesAvailable: Int = 2,
    val isCloudSynced: Boolean = false
) {
    val weeklyStudyDaysCsv: String
        get() = weeklyStudyDays.joinToString(",") { if (it) "1" else "0" }

    val xpLevel: Int
        get() = (totalXp / 250) + 1

    val nextMilestoneDays: Int
        get() = when {
            currentStreak < 3 -> 3
            currentStreak < 7 -> 7
            currentStreak < 14 -> 14
            currentStreak < 30 -> 30
            currentStreak < 60 -> 60
            else -> ((currentStreak / 30) + 1) * 30
        }

    val previousMilestoneDays: Int
        get() = when {
            currentStreak < 3 -> 0
            currentStreak < 7 -> 3
            currentStreak < 14 -> 7
            currentStreak < 30 -> 14
            currentStreak < 60 -> 30
            else -> (currentStreak / 30) * 30
        }

    val milestoneProgress: Float
        get() {
            val span = (nextMilestoneDays - previousMilestoneDays).coerceAtLeast(1)
            val progressed = (currentStreak - previousMilestoneDays).coerceAtLeast(0)
            return (progressed.toFloat() / span.toFloat()).coerceIn(0.08f, 1f)
        }

    val streakTierTitle: String
        get() = when {
            currentStreak >= 30 -> "Grandmaster Flame"
            currentStreak >= 14 -> "Momentum Architect"
            currentStreak >= 7 -> "Consistent Scholar"
            currentStreak >= 3 -> "Rising Spark"
            currentStreak >= 1 -> "Kindled Learner"
            else -> "Ready to Ignite"
        }
}

class DailyStreakManager(context: Context) {

    companion object {
        const val PREFS_NAME = "edam_daily_streak_prefs"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_LONGEST_STREAK = "longest_streak"
        private const val KEY_LAST_STUDY_EPOCH_DAY = "last_study_epoch_day"
        private const val KEY_TOTAL_XP = "total_xp"
        private const val KEY_WEEKLY_DAYS_CSV = "weekly_days_csv"
        private const val KEY_FLASHCARDS_REVIEWED = "flashcards_reviewed_count"
        private const val KEY_CHESS_PUZZLES_SOLVED = "chess_puzzles_solved_count"
        private const val KEY_STREAK_FREEZE_ACTIVE = "streak_freeze_active"
        private const val KEY_STREAK_FREEZES_AVAILABLE = "streak_freezes_available"
        private const val KEY_CLOUD_SYNCED = "streak_cloud_synced"

        fun currentEpochDay(nowMillis: Long = System.currentTimeMillis()): Long {
            return TimeUnit.MILLISECONDS.toDays(nowMillis)
        }
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _streakState = MutableStateFlow(loadStateFromPrefs())
    val streakState: StateFlow<DailyStreakState> = _streakState.asStateFlow()

    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            _streakState.value = loadStateFromPrefs()
        }

    init {
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        evaluateStreakOnLaunch()
    }

    private fun evaluateStreakOnLaunch() {
        val today = currentEpochDay()
        val lastDay = prefs.getLong(KEY_LAST_STUDY_EPOCH_DAY, -1L)
        if (lastDay == -1L) {
            // Seed initial streak for a welcoming experience
            val yesterday = today - 1L
            prefs.edit()
                .putInt(KEY_CURRENT_STREAK, 3)
                .putInt(KEY_LONGEST_STREAK, 5)
                .putLong(KEY_LAST_STUDY_EPOCH_DAY, yesterday)
                .putInt(KEY_TOTAL_XP, 1420)
                .putString(KEY_WEEKLY_DAYS_CSV, "1,1,1,0,0,0,0")
                .putBoolean(KEY_STREAK_FREEZE_ACTIVE, true)
                .putInt(KEY_STREAK_FREEZES_AVAILABLE, 2)
                .apply()
        } else if (today - lastDay > 1L) {
            val freezeActive = prefs.getBoolean(KEY_STREAK_FREEZE_ACTIVE, true)
            val freezesAvail = prefs.getInt(KEY_STREAK_FREEZES_AVAILABLE, 2)
            if (today - lastDay == 2L && freezeActive && freezesAvail > 0) {
                // Streak freeze protects a 1-day miss
                prefs.edit()
                    .putInt(KEY_STREAK_FREEZES_AVAILABLE, freezesAvail - 1)
                    .putBoolean(KEY_STREAK_FREEZE_ACTIVE, (freezesAvail - 1) > 0)
                    .putLong(KEY_LAST_STUDY_EPOCH_DAY, today - 1L)
                    .apply()
            } else {
                // Streak broken if more than 1 full day elapsed without a Streak Freeze
                prefs.edit()
                    .putInt(KEY_CURRENT_STREAK, 0)
                    .apply()
            }
        }
        _streakState.value = loadStateFromPrefs()
    }

    fun loadStateFromPrefs(nowMillis: Long = System.currentTimeMillis()): DailyStreakState {
        val today = currentEpochDay(nowMillis)
        val lastDay = prefs.getLong(KEY_LAST_STUDY_EPOCH_DAY, today - 1L)
        val current = prefs.getInt(KEY_CURRENT_STREAK, 3)
        val longest = prefs.getInt(KEY_LONGEST_STREAK, 5).coerceAtLeast(current)
        val totalXp = prefs.getInt(KEY_TOTAL_XP, 1420)
        val flashcards = prefs.getInt(KEY_FLASHCARDS_REVIEWED, 0)
        val chessSolved = prefs.getInt(KEY_CHESS_PUZZLES_SOLVED, 0)
        val freezeActive = prefs.getBoolean(KEY_STREAK_FREEZE_ACTIVE, true)
        val freezesAvailable = prefs.getInt(KEY_STREAK_FREEZES_AVAILABLE, 2)
        val isCloudSynced = prefs.getBoolean(KEY_CLOUD_SYNCED, false)
        val studiedToday = (lastDay == today)

        val csv = prefs.getString(KEY_WEEKLY_DAYS_CSV, "1,1,1,0,0,0,0") ?: "1,1,1,0,0,0,0"
        val parsedDays = csv.split(",")
            .map { it.trim() == "1" }
            .let { list ->
                if (list.size == 7) list else List(7) { idx -> idx < current.coerceAtMost(7) }
            }

        return DailyStreakState(
            currentStreak = current,
            longestStreak = longest,
            totalXp = totalXp,
            studiedToday = studiedToday,
            lastStudyEpochDay = lastDay,
            weeklyStudyDays = parsedDays,
            flashcardsReviewedCount = flashcards,
            chessPuzzlesSolvedCount = chessSolved,
            streakFreezeActive = freezeActive,
            streakFreezesAvailable = freezesAvailable,
            isCloudSynced = isCloudSynced
        )
    }

    fun applyCloudStreakDoc(
        cloudDoc: CloudDailyStreakDoc,
        nowMillis: Long = System.currentTimeMillis()
    ): DailyStreakState {
        val local = loadStateFromPrefs(nowMillis)
        val mergedLastDay = maxOf(local.lastStudyEpochDay, cloudDoc.lastStudyEpochDay)
        val mergedCurrent = if (cloudDoc.lastStudyEpochDay >= local.lastStudyEpochDay) {
            maxOf(local.currentStreak, cloudDoc.currentStreak)
        } else {
            local.currentStreak
        }
        val mergedLongest = maxOf(local.longestStreak, cloudDoc.longestStreak, mergedCurrent)
        val mergedXp = maxOf(local.totalXp, cloudDoc.totalXp)
        val cloudDays = cloudDoc.weeklyStudyDaysCsv.split(",").map { it.trim() == "1" }
        val mergedDays = List(7) { idx ->
            (local.weeklyStudyDays.getOrElse(idx) { false }) || (cloudDays.getOrElse(idx) { false })
        }
        val mergedCsv = mergedDays.joinToString(",") { if (it) "1" else "0" }

        prefs.edit()
            .putInt(KEY_CURRENT_STREAK, mergedCurrent)
            .putInt(KEY_LONGEST_STREAK, mergedLongest)
            .putLong(KEY_LAST_STUDY_EPOCH_DAY, mergedLastDay)
            .putInt(KEY_TOTAL_XP, mergedXp)
            .putString(KEY_WEEKLY_DAYS_CSV, mergedCsv)
            .putBoolean(KEY_STREAK_FREEZE_ACTIVE, cloudDoc.streakFreezeActive)
            .putInt(KEY_STREAK_FREEZES_AVAILABLE, cloudDoc.streakFreezesAvailable)
            .putBoolean(KEY_CLOUD_SYNCED, true)
            .apply()

        val updated = loadStateFromPrefs(nowMillis)
        _streakState.value = updated
        return updated
    }

    fun markCloudSynced(synced: Boolean = true): DailyStreakState {
        prefs.edit().putBoolean(KEY_CLOUD_SYNCED, synced).apply()
        val updated = loadStateFromPrefs()
        _streakState.value = updated
        return updated
    }

    fun toggleStreakFreeze(): DailyStreakState {
        val currentFreeze = prefs.getBoolean(KEY_STREAK_FREEZE_ACTIVE, true)
        val available = prefs.getInt(KEY_STREAK_FREEZES_AVAILABLE, 2).coerceAtLeast(1)
        prefs.edit()
            .putBoolean(KEY_STREAK_FREEZE_ACTIVE, !currentFreeze)
            .putInt(KEY_STREAK_FREEZES_AVAILABLE, available)
            .apply()
        val updated = loadStateFromPrefs()
        _streakState.value = updated
        return updated
    }

    fun recordStudyActivity(
        xpEarned: Int,
        isFlashcard: Boolean = false,
        isChessPuzzle: Boolean = false,
        nowMillis: Long = System.currentTimeMillis()
    ): DailyStreakState {
        val today = currentEpochDay(nowMillis)
        val lastDay = prefs.getLong(KEY_LAST_STUDY_EPOCH_DAY, -1L)
        var currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        var longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0)
        val totalXp = (prefs.getInt(KEY_TOTAL_XP, 1420) + xpEarned.coerceAtLeast(0))
        val flashcards = prefs.getInt(KEY_FLASHCARDS_REVIEWED, 0) + if (isFlashcard) 1 else 0
        val chessSolved = prefs.getInt(KEY_CHESS_PUZZLES_SOLVED, 0) + if (isChessPuzzle) 1 else 0

        if (lastDay != today) {
            currentStreak = if (lastDay == today - 1L) {
                currentStreak + 1
            } else {
                1.coerceAtLeast(currentStreak + 1)
            }
        } else if (currentStreak == 0) {
            currentStreak = 1
        }

        if (currentStreak > longestStreak) {
            longestStreak = currentStreak
        }

        val dayOfWeekIndex = getDayOfWeekMondayIndex(nowMillis)
        val currentDays = loadStateFromPrefs(nowMillis).weeklyStudyDays.toMutableList()
        if (dayOfWeekIndex in 0..6) {
            currentDays[dayOfWeekIndex] = true
        }
        val newCsv = currentDays.joinToString(",") { if (it) "1" else "0" }

        prefs.edit()
            .putInt(KEY_CURRENT_STREAK, currentStreak)
            .putInt(KEY_LONGEST_STREAK, longestStreak)
            .putLong(KEY_LAST_STUDY_EPOCH_DAY, today)
            .putInt(KEY_TOTAL_XP, totalXp)
            .putInt(KEY_FLASHCARDS_REVIEWED, flashcards)
            .putInt(KEY_CHESS_PUZZLES_SOLVED, chessSolved)
            .putString(KEY_WEEKLY_DAYS_CSV, newCsv)
            .apply()

        val updated = loadStateFromPrefs(nowMillis)
        _streakState.value = updated
        return updated
    }

    private fun getDayOfWeekMondayIndex(nowMillis: Long): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }
}
