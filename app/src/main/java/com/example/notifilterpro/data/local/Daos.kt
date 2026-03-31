package com.example.notifilterpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Delete

@Dao
interface AppRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AppRuleEntity)

    @Query("SELECT * FROM app_rules WHERE packageName = :packageName LIMIT 1")
    suspend fun getRuleByPackage(packageName: String): AppRuleEntity?

    @Query("SELECT * FROM app_rules")
    fun getAllRules(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules WHERE packageName = :packageName LIMIT 1")
    suspend fun getRuleForApp(packageName: String): AppRuleEntity?
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: InterceptedNotificationEntity)

    @Query("SELECT * FROM intercepted_notifications ORDER BY timestamp DESC")
    fun getAllOrangeNotifications(): Flow<List<InterceptedNotificationEntity>>

    @Query("DELETE FROM intercepted_notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Long)

    @Query("DELETE FROM intercepted_notifications")
    suspend fun clearAll()

    @Query("DELETE FROM intercepted_notifications WHERE timestamp < :threshold")
    suspend fun deleteNotificationsOlderThan(threshold: Long)

    @Query("DELETE FROM intercepted_notifications")
    suspend fun clearAllIntercepted()
}

@Dao
interface BlockedNotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedNotification(notification: BlockedNotificationEntity)

    @Query("SELECT * FROM blocked_notifications ORDER BY timestamp DESC")
    fun getAllBlockedNotifications(): kotlinx.coroutines.flow.Flow<List<BlockedNotificationEntity>>

    @Query("DELETE FROM blocked_notifications")
    suspend fun clearAll()

    @Query("DELETE FROM blocked_notifications")
    suspend fun clearAllBlocked()
}

// --- SMART KEYWORDS DAO ---
@Dao
interface KeywordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyword(keyword: KeywordEntity)

    @Delete
    suspend fun deleteKeyword(keyword: KeywordEntity)

    @Query("SELECT * FROM smart_keywords WHERE isWhitelist = 1")
    fun getWhitelist(): kotlinx.coroutines.flow.Flow<List<KeywordEntity>>

    @Query("SELECT * FROM smart_keywords WHERE isWhitelist = 0")
    fun getBlacklist(): kotlinx.coroutines.flow.Flow<List<KeywordEntity>>

    @Query("DELETE FROM smart_keywords WHERE keyword = :word AND isWhitelist = :isWhitelist")
    suspend fun deleteKeywordByValue(word: String, isWhitelist: Boolean)
}

// --- SENDER RULES DAO ---
@Dao
interface SenderRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSender(rule: SenderRuleEntity)

    @Delete
    suspend fun deleteSender(rule: SenderRuleEntity)

    @Query("SELECT * FROM sender_rules WHERE isWhitelist = 1")
    fun getWhitelistSenders(): kotlinx.coroutines.flow.Flow<List<SenderRuleEntity>>

    @Query("SELECT * FROM sender_rules WHERE isWhitelist = 0")
    fun getBlacklistSenders(): kotlinx.coroutines.flow.Flow<List<SenderRuleEntity>>
}

// --- TIME PROFILES DAO ---
@Dao
interface TimeProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: TimeProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: TimeProfileEntity)

    @Query("SELECT * FROM time_profiles")
    fun getAllProfiles(): kotlinx.coroutines.flow.Flow<List<TimeProfileEntity>>
}