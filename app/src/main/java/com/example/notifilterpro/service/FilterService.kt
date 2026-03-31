package com.example.notifilterpro.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.notifilterpro.data.local.*
import com.example.notifilterpro.data.preferences.SettingsRepository
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
    @Inject lateinit var timeProfileDao: TimeProfileDao
    @Inject lateinit var settingsRepository: SettingsRepository // <-- ADDED BACK: The Stats Tracker

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var activeWhitelist: List<String> = emptyList()
    private var activeBlacklist: List<String> = emptyList()
    private var activeVipSenders: List<String> = emptyList()
    private var activeBlockedSenders: List<String> = emptyList()
    private var activeTimeProfiles: List<TimeProfileEntity> = emptyList()

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch { keywordDao.getWhitelist().collect { entities -> activeWhitelist = entities.map { it.keyword } } }
        serviceScope.launch { keywordDao.getBlacklist().collect { entities -> activeBlacklist = entities.map { it.keyword } } }
        serviceScope.launch { senderRuleDao.getWhitelistSenders().collect { entities -> activeVipSenders = entities.map { it.senderName } } }
        serviceScope.launch { senderRuleDao.getBlacklistSenders().collect { entities -> activeBlockedSenders = entities.map { it.senderName } } }
        serviceScope.launch { timeProfileDao.getAllProfiles().collect { profiles -> activeTimeProfiles = profiles.filter { it.isEnabled } } }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun isCurrentlyInFocusMode(): Boolean {
        if (activeTimeProfiles.isEmpty()) return true
        val calendar = Calendar.getInstance()
        val currentMins = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        for (profile in activeTimeProfiles) {
            val startMins = profile.startHour * 60 + profile.startMinute
            val endMins = profile.endHour * 60 + profile.endMinute
            if (startMins <= endMins) {
                if (currentMins in startMins..endMins) return true
            } else {
                if (currentMins >= startMins || currentMins <= endMins) return true
            }
        }
        return false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        // Safety guard: Don't let the app intercept its own alerts
        if (sbn.packageName == packageName) return

        val prefs = applicationContext.getSharedPreferences("noti_prefs", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("is_paused", false)) return

        val packageName = sbn.packageName

        // Using getCharSequence to prevent silent crashes if an app sends colored/bolded text
        val title = sbn.notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "No Title"
        val text = sbn.notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        val lowerTitle = title.lowercase()
        val fullContent = "$title $text".lowercase()

        serviceScope.launch {
            try {
                val hasPriorityKeyword = activeWhitelist.any { fullContent.contains(it) }
                val hasSpamKeyword = activeBlacklist.any { fullContent.contains(it) }
                val isVipSender = activeVipSenders.any { lowerTitle.contains(it) }
                val isBlockedSender = activeBlockedSenders.any { lowerTitle.contains(it) }

                val rule = appRuleDao.getRuleForApp(packageName)
                val category = rule?.category ?: "GREEN"

                // 1. VIPs and Priority
                if (hasPriorityKeyword || isVipSender) {
                    settingsRepository.incrementAllowedCount() // <-- ADDED: +1 Allowed
                    return@launch
                }

                // 2. Spam and Blocked Senders
                if (hasSpamKeyword || isBlockedSender) {
                    cancelNotification(sbn.key)
                    blockedNotificationDao.insertBlockedNotification(
                        BlockedNotificationEntity(packageName = packageName, title = title, content = text, timestamp = System.currentTimeMillis())
                    )
                    settingsRepository.incrementBlockedCount() // <-- ADDED: +1 Blocked
                    return@launch
                }

                // 3. Time Profile Check
                if (!isCurrentlyInFocusMode()) {
                    settingsRepository.incrementAllowedCount() // <-- ADDED: +1 Allowed
                    return@launch
                }

                // 4. App Rules Check
                if (category == "RED") {
                    cancelNotification(sbn.key)
                    blockedNotificationDao.insertBlockedNotification(
                        BlockedNotificationEntity(packageName = packageName, title = title, content = text, timestamp = System.currentTimeMillis())
                    )
                    settingsRepository.incrementBlockedCount() // <-- ADDED: +1 Blocked
                    return@launch
                }

                if (category == "ORANGE") {
                    cancelNotification(sbn.key)
                    notificationDao.insertNotification(
                        InterceptedNotificationEntity(packageName = packageName, title = title, content = text, timestamp = System.currentTimeMillis())
                    )
                    return@launch
                }

                // 5. If it reaches here, the app is set to GREEN
                settingsRepository.incrementAllowedCount() // <-- ADDED: +1 Allowed

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}