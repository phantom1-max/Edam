package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object EdamNotificationHelper {

    const val CHANNEL_ID = "edam_course_milestones"
    private const val CHANNEL_NAME = "Course Milestones & Badges"
    private const val CHANNEL_DESC = "Notifications for course completion milestones, newly earned badges, and study goals."

    const val DAILY_GOAL_CHANNEL_ID = "edam_daily_goal_reminders"
    private const val DAILY_GOAL_CHANNEL_NAME = "Daily Learning Goal Reminders"
    private const val DAILY_GOAL_CHANNEL_DESC = "Scheduled WorkManager reminders alerting you to complete your daily Edam learning target."
    private const val DAILY_GOAL_NOTIFICATION_ID = 7788

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val milestoneChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val dailyGoalChannel = NotificationChannel(
                DAILY_GOAL_CHANNEL_ID,
                DAILY_GOAL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = DAILY_GOAL_CHANNEL_DESC
                enableVibration(true)
            }
            manager?.createNotificationChannel(milestoneChannel)
            manager?.createNotificationChannel(dailyGoalChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendBadgeUnlockedNotification(
        context: Context,
        badgeTitle: String,
        courseTitle: String,
        percentage: Int
    ) {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            Log.w("EdamNotificationHelper", "POST_NOTIFICATIONS permission not granted; skipping badge alert.")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            badgeTitle.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notificationTitle = "🎖️ New Badge: $badgeTitle Unlocked!"
        val notificationText = "Outstanding! You reached $percentage% in \"$courseTitle\" and earned the \"$badgeTitle\" milestone badge!"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_badge)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(badgeTitle.hashCode(), notification)
        } catch (e: SecurityException) {
            Log.e("EdamNotificationHelper", "SecurityException posting notification", e)
        }
    }

    fun sendTestMilestoneNotification(context: Context) {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            Log.w("EdamNotificationHelper", "POST_NOTIFICATIONS permission not granted.")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val title = "🎯 Study Milestone Achieved!"
        val text = "Edam Notifications are active! Your course progress and badge unlocks will trigger real-time milestone alerts."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_badge)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(9999, notification)
        } catch (e: SecurityException) {
            Log.e("EdamNotificationHelper", "SecurityException posting notification", e)
        }
    }

    fun sendDailyGoalReminderNotification(
        context: Context,
        completedToday: Int,
        dailyGoal: Int,
        currentStreak: Int,
        scheduledTimeLabel: String
    ) {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            Log.w("EdamNotificationHelper", "POST_NOTIFICATIONS permission not granted; skipping daily goal reminder.")
            return
        }

        val safeGoal = dailyGoal.coerceAtLeast(1)
        val safeCompleted = completedToday.coerceAtLeast(0)
        val remaining = (safeGoal - safeCompleted).coerceAtLeast(0)
        val isGoalMet = safeCompleted >= safeGoal

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_daily_goal_from_notification", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            DAILY_GOAL_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val title = if (isGoalMet) {
            "🔥 Daily Goal Complete ($safeCompleted/$safeGoal Lessons)!"
        } else {
            "⏰ Edam Daily Goal Alert ($scheduledTimeLabel): $remaining ${if (remaining == 1) "Lesson" else "Lessons"} Left"
        }

        val bodyText = if (isGoalMet) {
            "Awesome job! You reached your $safeGoal-lesson daily goal and protected your $currentStreak-day learning streak."
        } else {
            "You've completed $safeCompleted of $safeGoal lessons today. Jump into Edam now to finish your remaining $remaining ${if (remaining == 1) "lesson" else "lessons"} and keep your $currentStreak-day streak burning!"
        }

        val notification = NotificationCompat.Builder(context, DAILY_GOAL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_badge)
            .setContentTitle(title)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setProgress(safeGoal, safeCompleted.coerceAtMost(safeGoal), false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(DAILY_GOAL_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.e("EdamNotificationHelper", "SecurityException posting daily goal reminder", e)
        }
    }
}
