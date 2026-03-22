package com.example.notifilterpro.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object DigestScheduler {
    private const val DIGEST_WORK_NAME = "NotiFilterDigestWork"

    // This function can be called from your UI when the user selects their preferred frequency (e.g., 2 hours)
    fun schedule(context: Context, intervalHours: Long) {
        // Android enforces a strict minimum of 15 minutes for periodic background work
        val safeInterval = if (intervalHours < 1) 1 else intervalHours

        val workRequest = PeriodicWorkRequestBuilder<DigestWorker>(
            safeInterval, TimeUnit.HOURS
        ).build()

        // Enqueue the work, replacing any existing schedule if the user changes their settings
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DIGEST_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    // Call this if the user turns off the digest feature entirely
    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DIGEST_WORK_NAME)
    }
}