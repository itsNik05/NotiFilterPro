package com.nuviolabs.aurafilter

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.annotation.Keep
import javax.inject.Inject

//@Keep
@HiltAndroidApp
class AuraFilterApp : Application(), Configuration.Provider {

    // We inject the HiltWorkerFactory so WorkManager can use Hilt dependencies
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
