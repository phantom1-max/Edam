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

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
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
}
