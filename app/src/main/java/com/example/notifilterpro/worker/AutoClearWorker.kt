package com.example.notifilterpro.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.notifilterpro.data.local.NotificationDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AutoClearWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationDao: NotificationDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Calculate the time exactly 48 hours ago
            val fortyEightHoursAgo = System.currentTimeMillis() - (48 * 60 * 60 * 1000)

            // Delete everything older than that
            notificationDao.deleteNotificationsOlderThan(fortyEightHoursAgo)

            Log.d("AutoClearWorker", "Successfully cleared notifications older than 48 hours.")
            Result.success()
        } catch (e: Exception) {
            Log.e("AutoClearWorker", "Failed to clear old notifications", e)
            Result.retry()
        }
    }
}