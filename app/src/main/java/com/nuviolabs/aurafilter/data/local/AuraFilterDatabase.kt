package com.nuviolabs.aurafilter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AppRuleEntity::class,
        InterceptedNotificationEntity::class,
        BlockedNotificationEntity::class,
        KeywordEntity::class,
        SenderRuleEntity::class,
        TimeProfileEntity::class// <--- 1. ADDED THIS
    ],
    version = 5, // <--- 2. BUMPED THIS UP BY 1
    exportSchema = false
)
abstract class AuraFilterDatabase : RoomDatabase() {
    abstract fun appRuleDao(): AppRuleDao
    abstract fun notificationDao(): NotificationDao
    abstract fun blockedNotificationDao(): BlockedNotificationDao // ADDED

    abstract fun keywordDao(): KeywordDao

    abstract fun senderRuleDao(): SenderRuleDao

    abstract fun timeProfileDao(): TimeProfileDao
}
