package com.example.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.local.DailyStreakManager
import com.example.ui.EdamDailyGoalDataStoreKeys
import com.example.ui.edamDataStore
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * WorkManager CoroutineWorker that executes at the user's chosen daily reminder time,
 * inspects the user's daily lesson goal progress in DataStore and current streak in SharedPreferences,
 * and dispatches a push notification alerting them to complete their daily learning target.
 */
class DailyGoalReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = applicationContext.edamDataStore.data.first()
            val todayEpoch = DailyStreakManager.currentEpochDay()
            val storedEpoch = prefs[EdamDailyGoalDataStoreKeys.DAILY_LESSONS_EPOCH_DAY] ?: todayEpoch

            val dailyGoal = (prefs[EdamDailyGoalDataStoreKeys.DAILY_LESSON_GOAL]
                ?: inputData.getInt(KEY_FALLBACK_DAILY_GOAL, 3)).coerceIn(1, 10)

            val completedToday = if (storedEpoch == todayEpoch) {
                (prefs[EdamDailyGoalDataStoreKeys.DAILY_LESSONS_COMPLETED_TODAY] ?: 1).coerceAtLeast(0)
            } else {
                0
            }

            val reminderEnabled = prefs[EdamDailyGoalDataStoreKeys.DAILY_REMINDER_ENABLED] ?: true
            val isManualTrigger = inputData.getBoolean(KEY_IS_MANUAL_TRIGGER, false)

            if (!reminderEnabled && !isManualTrigger) {
                Log.d(TAG, "Daily goal reminder is disabled in DataStore; skipping alert.")
                return Result.success()
            }

            val hour24 = (prefs[EdamDailyGoalDataStoreKeys.DAILY_REMINDER_HOUR]
                ?: inputData.getInt(KEY_REMINDER_HOUR, 20)).coerceIn(0, 23)
            val minute = (prefs[EdamDailyGoalDataStoreKeys.DAILY_REMINDER_MINUTE]
                ?: inputData.getInt(KEY_REMINDER_MINUTE, 0)).coerceIn(0, 59)

            val streakManager = DailyStreakManager(applicationContext)
            val currentStreak = streakManager.streakState.value.currentStreak

            val formattedTime = DailyGoalReminderScheduler.formatTime12Hour(hour24, minute)

            EdamNotificationHelper.sendDailyGoalReminderNotification(
                context = applicationContext,
                completedToday = completedToday,
                dailyGoal = dailyGoal,
                currentStreak = currentStreak,
                scheduledTimeLabel = formattedTime
            )

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed executing DailyGoalReminderWorker", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "DailyGoalReminderWorker"
        const val KEY_REMINDER_HOUR = "reminder_hour_24"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
        const val KEY_FALLBACK_DAILY_GOAL = "fallback_daily_goal"
        const val KEY_IS_MANUAL_TRIGGER = "is_manual_trigger"
    }
}

/**
 * Helper object for scheduling, updating, and canceling daily learning goal push notifications
 * via Android Jetpack WorkManager.
 */
object DailyGoalReminderScheduler {

    const val UNIQUE_PERIODIC_WORK_NAME = "edam_daily_goal_reminder_periodic_work"
    const val UNIQUE_ONE_TIME_WORK_NAME = "edam_daily_goal_reminder_onetime_work"
    const val WORK_TAG_DAILY_GOAL = "edam_daily_goal_reminder_tag"

    /**
     * Computes the initial delay in milliseconds from [nowMillis] to the next occurrence of
     * [targetHour24]:[targetMinute] in the device's local timezone.
     */
    fun computeInitialDelayMillis(
        targetHour24: Int,
        targetMinute: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val nowCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
        }
        val targetCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, targetHour24.coerceIn(0, 23))
            set(Calendar.MINUTE, targetMinute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (targetCal.timeInMillis <= nowCal.timeInMillis) {
            targetCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return (targetCal.timeInMillis - nowCal.timeInMillis).coerceAtLeast(1_000L)
    }

    /**
     * Formats a 24-hour time (`0..23`, `0..59`) into a user-friendly 12-hour label (e.g., "08:00 PM").
     */
    fun formatTime12Hour(hour24: Int, minute: Int): String {
        val safeHour = hour24.coerceIn(0, 23)
        val safeMinute = minute.coerceIn(0, 59)
        val amPm = if (safeHour < 12) "AM" else "PM"
        val hour12 = when {
            safeHour == 0 -> 12
            safeHour > 12 -> safeHour - 12
            else -> safeHour
        }
        return String.format(Locale.US, "%02d:%02d %s", hour12, safeMinute, amPm)
    }

    /**
     * Formats a millisecond delay into a concise countdown label (e.g. "Next alert in 4h 32m").
     */
    fun formatDelayCountdown(delayMillis: Long): String {
        val totalMinutes = (delayMillis / 60_000L).coerceAtLeast(1L)
        val hours = totalMinutes / 60L
        val mins = totalMinutes % 60L
        return if (hours > 0L) {
            "Next WorkManager alert in ${hours}h ${mins}m"
        } else {
            "Next WorkManager alert in ${mins}m"
        }
    }

    /**
     * Schedules (or updates) a 24-hour periodic WorkManager job that fires at [hour24]:[minute].
     */
    fun scheduleDailyGoalReminder(
        context: Context,
        hour24: Int,
        minute: Int,
        dailyGoal: Int = 3
    ): Long {
        val safeHour = hour24.coerceIn(0, 23)
        val safeMinute = minute.coerceIn(0, 59)
        val initialDelayMillis = computeInitialDelayMillis(safeHour, safeMinute)

        val inputData = workDataOf(
            DailyGoalReminderWorker.KEY_REMINDER_HOUR to safeHour,
            DailyGoalReminderWorker.KEY_REMINDER_MINUTE to safeMinute,
            DailyGoalReminderWorker.KEY_FALLBACK_DAILY_GOAL to dailyGoal.coerceIn(1, 10),
            DailyGoalReminderWorker.KEY_IS_MANUAL_TRIGGER to false
        )

        val periodicWorkRequest = PeriodicWorkRequestBuilder<DailyGoalReminderWorker>(
            24,
            TimeUnit.HOURS
        )
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(WORK_TAG_DAILY_GOAL)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )

        return initialDelayMillis
    }

    /**
     * Enqueues an immediate one-time WorkManager request to test/dispatch the daily goal alert right now.
     */
    fun triggerImmediateGoalReminderWork(
        context: Context,
        hour24: Int,
        minute: Int,
        dailyGoal: Int = 3
    ) {
        val inputData = workDataOf(
            DailyGoalReminderWorker.KEY_REMINDER_HOUR to hour24.coerceIn(0, 23),
            DailyGoalReminderWorker.KEY_REMINDER_MINUTE to minute.coerceIn(0, 59),
            DailyGoalReminderWorker.KEY_FALLBACK_DAILY_GOAL to dailyGoal.coerceIn(1, 10),
            DailyGoalReminderWorker.KEY_IS_MANUAL_TRIGGER to true
        )

        val oneTimeRequest = OneTimeWorkRequestBuilder<DailyGoalReminderWorker>()
            .setInputData(inputData)
            .addTag(WORK_TAG_DAILY_GOAL)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )
    }

    /**
     * Cancels the scheduled periodic daily goal reminder in WorkManager.
     */
    fun cancelDailyGoalReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_PERIODIC_WORK_NAME)
    }
}
