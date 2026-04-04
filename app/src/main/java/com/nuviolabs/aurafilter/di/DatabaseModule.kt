package com.nuviolabs.aurafilter.di

import android.content.Context
import androidx.room.Room
import com.nuviolabs.aurafilter.data.local.AppRuleDao
import com.nuviolabs.aurafilter.data.local.AuraFilterDatabase
import com.nuviolabs.aurafilter.data.local.BlockedNotificationDao
import com.nuviolabs.aurafilter.data.local.KeywordDao
import com.nuviolabs.aurafilter.data.local.NotificationDao
import com.nuviolabs.aurafilter.data.local.SenderRuleDao
import com.nuviolabs.aurafilter.data.local.TimeProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AuraFilterDatabase {
        return Room.databaseBuilder(
            context,
            AuraFilterDatabase::class.java,
            "notifilter_db"
        )
            .fallbackToDestructiveMigration() // <--- THIS IS THE MAGIC LINE!
            .build()
    }

    @Provides
    fun provideAppRuleDao(database: AuraFilterDatabase): AppRuleDao {
        return database.appRuleDao()
    }

    @Provides
    fun provideNotificationDao(database: AuraFilterDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    fun provideBlockedNotificationDao(database: AuraFilterDatabase): BlockedNotificationDao {
        return database.blockedNotificationDao()
    }

    @Provides
    fun provideKeywordDao(database: AuraFilterDatabase): KeywordDao {
        return database.keywordDao()
    }

    @Provides
    fun provideSenderRuleDao(database: AuraFilterDatabase): SenderRuleDao {
        return database.senderRuleDao()
    }

    @Provides
    fun provideTimeProfileDao(database: AuraFilterDatabase): TimeProfileDao {
        return database.timeProfileDao()
    }
}
