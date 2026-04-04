package com.nuviolabs.aurafilter.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nuviolabs.aurafilter.data.local.NotificationDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

// @HiltWorker allows us to inject our database directly into the background worker
@HiltWorker
class DigestWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationDao: NotificationDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // 1. Fetch the current intercepted notifications
        // We use .first() to grab the latest list from the Flow and cancel the stream
        val notifications = notificationDao.getAllOrangeNotifications().first()

        // 2. If the database is empty, we do nothing and tell Android the job succeeded
        if (notifications.isEmpty()) {
            return Result.success()
        }

        // 3. If we have notifications, trigger a summary alert for the user
        showSummaryNotification(notifications.size)

        return Result.success()
    }

    private fun showSummaryNotification(count: Int) {
        val channelId = "digest_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0+ requires a Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notification Digests",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to check your filtered notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build the actual notification
        val builder = NotificationCompat.Builder(context, channelId)
            // Note: Replace android.R.drawable.ic_dialog_info with your app's actual icon later
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Notification Digest")
            .setContentText("You have $count notifications waiting for review.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Show it! (We use a fixed ID so it updates the same notification instead of making multiples)
        notificationManager.notify(1001, builder.build())
    }
}
