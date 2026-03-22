package com.example.notifilterpro.di

import android.content.Context
import androidx.room.Room
import com.example.notifilterpro.data.local.AppRuleDao
import com.example.notifilterpro.data.local.NotiFilterDatabase
import com.example.notifilterpro.data.local.NotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.notifilterpro.data.local.BlockedNotificationDao
import com.example.notifilterpro.data.local.KeywordDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotiFilterDatabase {
        return Room.databaseBuilder(
            context,
            NotiFilterDatabase::class.java,
            "notifilter_db"
        )
            .fallbackToDestructiveMigration() // <--- THIS IS THE MAGIC LINE!
            .build()
    }

    @Provides
    fun provideAppRuleDao(database: NotiFilterDatabase): AppRuleDao {
        return database.appRuleDao()
    }

    @Provides
    fun provideNotificationDao(database: NotiFilterDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    fun provideBlockedNotificationDao(database: NotiFilterDatabase): BlockedNotificationDao {
        return database.blockedNotificationDao()
    }

    @Provides
    fun provideKeywordDao(database: NotiFilterDatabase): KeywordDao {
        return database.keywordDao()
    }

    @Provides
    fun provideSenderRuleDao(database: NotiFilterDatabase): com.example.notifilterpro.data.local.SenderRuleDao {
        return database.senderRuleDao()
    }

    @Provides
    fun provideTimeProfileDao(database: NotiFilterDatabase): com.example.notifilterpro.data.local.TimeProfileDao {
        return database.timeProfileDao()
    }
}