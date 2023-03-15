package io.github.akiomik.seiun

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.akiomik.seiun.model.app.bsky.notification.Notification
import io.github.akiomik.seiun.ui.theme.NotificationColor

class NotificationWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val notificationChannelId = "io.github.akiomik.seiun"

    override suspend fun doWork(): Result {
        Log.d(SeiunApplication.TAG, "Fetch notifications")

        // TODO: Check first that notifications are available
        val notificationRepository = SeiunApplication.instance!!.notificationRepository
        val notifications =
            try {
                notificationRepository.listNotifications()
            } catch (e: Exception) {
                Log.d(SeiunApplication.TAG, "Failed to fetch notifications: $e")
                return Result.failure()
            }

        val unreadCount = notifications.notifications.fold(0) { acc, notification ->
            if (notification.isRead) {
                acc
            } else {
                acc + 1
            }
        }
        Log.d(SeiunApplication.TAG, "Unread notification count: $unreadCount")

        // TODO: Do not notify if there are no new notifications compared to the previous cursor
        if (unreadCount > 0) {
            sendNotification(unreadCount)
        }

        return Result.success()
    }

    private fun sendNotification(unreadCount: Int) {
        val channel = createNotificationChannel()
        notificationManager.createNotificationChannel(channel)

        with(NotificationManagerCompat.from(context)) {
            Log.d(SeiunApplication.TAG, "Notification status = ${areNotificationsEnabled()}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                )

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    Log.d(SeiunApplication.TAG, "Notification permission is not granted")

                    return
                }
            }

            if (areNotificationsEnabled()) {
                val notification = createNotification(unreadCount)
                notify(SeiunApplication.NOTIFICATION_ID, notification)
            } else {
                Log.d(SeiunApplication.TAG, "Notification is enabled")
            }
        }
    }

    private fun createNotification(unreadCount: Int): android.app.Notification {
        val intent = Intent(context, SeiunActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("from", "notification")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE + PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(R.mipmap.ic_launcher) // TODO: Set single color icon (128x128px)
            .setContentTitle(context.getString(R.string.notification_title, unreadCount))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setNumber(unreadCount)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setColor(NotificationColor.toArgb())
            .setColorized(true)
            .build()
    }

    private fun createNotificationChannel(): NotificationChannel {
        val name = context.getString(R.string.notification_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        return NotificationChannel(notificationChannelId, name, importance)
    }
}
