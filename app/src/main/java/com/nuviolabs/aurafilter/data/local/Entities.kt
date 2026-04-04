package com.nuviolabs.aurafilter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Table 1: Stores the user's rules for each app
@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val category: String, // "RED", "ORANGE", "GREEN"
    val isWhitelisted: Boolean = false
)

// Table 2: Stores the intercepted "Orange" notifications
@Entity(tableName = "intercepted_notifications")
data class InterceptedNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val title: String,
    val content: String,
    val timestamp: Long
)

@Entity(tableName = "blocked_notifications")
data class BlockedNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val title: String,
    val content: String,
    val timestamp: Long
)

// --- SMART KEYWORDS TABLE ---
@Entity(tableName = "smart_keywords")
data class KeywordEntity(
    @PrimaryKey val keyword: String,
    val isWhitelist: Boolean // TRUE for Priority (Green), FALSE for Spam (Red)
)

// --- SENDER RULES TABLE ---
@Entity(tableName = "sender_rules")
data class SenderRuleEntity(
    @PrimaryKey val senderName: String,
    val isWhitelist: Boolean // TRUE for VIPs (Green), FALSE for Blocked (Red)
)

// --- TIME PROFILES TABLE ---
@Entity(tableName = "time_profiles")
data class TimeProfileEntity(
    @PrimaryKey val profileName: String,
    val startHour: Int,   // 24-hour format (e.g., 9 for 9 AM)
    val startMinute: Int,
    val endHour: Int,     // 24-hour format (e.g., 17 for 5 PM)
    val endMinute: Int,
    val isEnabled: Boolean
)
