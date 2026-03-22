package com.example.notifilterpro.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.notifilterpro.data.local.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class FilterService : NotificationListenerService() {

    @Inject lateinit var appRuleDao: AppRuleDao
    @Inject lateinit var notificationDao: NotificationDao
    @Inject lateinit var keywordDao: KeywordDao
    @Inject lateinit var blockedNotificationDao: BlockedNotificationDao
    @Inject lateinit var senderRuleDao: SenderRuleDao
    @Inject lateinit var timeProfileDao: TimeProfileDao // <--- 1. INJECTED TIME DAO

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Live memory for the engine
    private var activeWhitelist: List<String> = emptyList()
    private var activeBlacklist: List<String> = emptyList()
    private var activeVipSenders: List<String> = emptyList()
    private var activeBlockedSenders: List<String> = emptyList()
    private var activeTimeProfiles: List<TimeProfileEntity> = emptyList() // <--- 2. TIME MEMORY

    override fun onCreate() {
        super.onCreate()

        // Listen for all Database changes
        serviceScope.launch { keywordDao.getWhitelist().collect { entities -> activeWhitelist = entities.map { it.keyword } } }
        serviceScope.launch { keywordDao.getBlacklist().collect { entities -> activeBlacklist = entities.map { it.keyword } } }
        serviceScope.launch { senderRuleDao.getWhitelistSenders().collect { entities -> activeVipSenders = entities.map { it.senderName } } }
        serviceScope.launch { senderRuleDao.getBlacklistSenders().collect { entities -> activeBlockedSenders = entities.map { it.senderName } } }

        // Listen for Time Profiles (Only grab the ones toggled ON)
        serviceScope.launch {
            timeProfileDao.getAllProfiles().collect { profiles ->
                activeTimeProfiles = profiles.filter { it.isEnabled }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    // --- THE CLOCK LOGIC ---
    private fun isCurrentlyInFocusMode(): Boolean {
        // If no profiles are enabled, we filter 24/7 by default!
        if (activeTimeProfiles.isEmpty()) return true

        val calendar = Calendar.getInstance()
        val currentMins = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        for (profile in activeTimeProfiles) {
            val startMins = profile.startHour * 60 + profile.startMinute
            val endMins = profile.endHour * 60 + profile.endMinute

            if (startMins <= endMins) {
                // Normal day schedule (e.g., 9:00 AM to 5:00 PM)
                if (currentMins in startMins..endMins) return true
            } else {
                // Overnight schedule (e.g., 10:00 PM to 7:00 AM)
                if (currentMins >= startMins || currentMins <= endMins) return true
            }
        }
        return false // The clock is outside of all focus modes!
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val prefs = applicationContext.getSharedPreferences("noti_prefs", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("is_paused", false)) return

        val packageName = sbn.packageName
        val notification = sbn.notification
        val title = notification.extras.getString(Notification.EXTRA_TITLE) ?: "No Title"
        val text = notification.extras.getString(Notification.EXTRA_TEXT) ?: ""

        val lowerTitle = title.lowercase()
        val fullContent = "$title $text".lowercase()

        serviceScope.launch {
            val hasPriorityKeyword = activeWhitelist.any { fullContent.contains(it) }
            val hasSpamKeyword = activeBlacklist.any { fullContent.contains(it) }
            val isVipSender = activeVipSenders.any { lowerTitle.contains(it) }
            val isBlockedSender = activeBlockedSenders.any { lowerTitle.contains(it) }

            val rule = appRuleDao.getRuleForApp(packageName)
            val category = rule?.category ?: "GREEN"

            // 1. VIPs and Priority Keywords (Bypass EVERYTHING)
            if (hasPriorityKeyword || isVipSender) {
                return@launch
            }

            // 2. Spam and Blocked Senders (Blocked 24/7, even outside of focus modes)
            if (hasSpamKeyword || isBlockedSender) {
                cancelNotification(sbn.key)
                blockedNotificationDao.insertBlockedNotification(
                    // FIXED: Explicitly named the parameters
                    BlockedNotificationEntity(
                        packageName = packageName,
                        title = title,
                        content = text,
                        timestamp = System.currentTimeMillis()
                    )
                )
                return@launch
            }

            // 3. CLOCK CHECK FOR APPS!
            // If we are NOT in a focus mode, let the app through normally.
            if (!isCurrentlyInFocusMode()) {
                return@launch
            }

            // 4. FOCUS MODE IS ACTIVE: Apply Orange & Red App Rules
            if (category == "RED") {
                cancelNotification(sbn.key)
                blockedNotificationDao.insertBlockedNotification(
                    // FIXED: Explicitly named the parameters
                    BlockedNotificationEntity(
                        packageName = packageName,
                        title = title,
                        content = text,
                        timestamp = System.currentTimeMillis()
                    )
                )
                return@launch
            }

            if (category == "ORANGE") {
                cancelNotification(sbn.key)
                notificationDao.insertNotification(
                    // FIXED: Explicitly named the parameters
                    InterceptedNotificationEntity(
                        packageName = packageName,
                        title = title,
                        content = text,
                        timestamp = System.currentTimeMillis()
                    )
                )
                return@launch
            }
        }
    }
}