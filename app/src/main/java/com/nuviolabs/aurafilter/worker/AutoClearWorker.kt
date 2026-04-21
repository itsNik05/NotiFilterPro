package com.nuviolabs.aurafilter.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nuviolabs.aurafilter.data.local.BlockedNotificationDao
import com.nuviolabs.aurafilter.data.local.NotificationDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AutoClearWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationDao: NotificationDao,
    private val blockedNotificationDao: BlockedNotificationDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = applicationContext.getSharedPreferences("noti_prefs", Context.MODE_PRIVATE)
            val thresholdHours = prefs.getInt("auto_delete_threshold", 24)

            if (thresholdHours == -1) {
                Log.d("AutoClearWorker", "Auto-delete disabled. Skipping cleanup.")
                return Result.success()
            }

            val cutoffTime = System.currentTimeMillis() - (thresholdHours * 60L * 60L * 1000L)
            notificationDao.deleteNotificationsOlderThan(cutoffTime)
            blockedNotificationDao.deleteBlockedNotificationsOlderThan(cutoffTime)

            Log.d("AutoClearWorker", "Successfully cleared notifications older than $thresholdHours hours.")
            Result.success()
        } catch (e: Exception) {
            Log.e("AutoClearWorker", "Failed to clear old notifications", e)
            Result.retry()
        }
    }
}
