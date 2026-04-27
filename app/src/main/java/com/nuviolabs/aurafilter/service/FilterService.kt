package com.nuviolabs.aurafilter.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.nuviolabs.aurafilter.data.local.*
import com.nuviolabs.aurafilter.data.preferences.SettingsRepository
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
    @Inject lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var activeWhitelist: List<String> = emptyList()
    private var activeBlacklist: List<String> = emptyList()
    private var activeVipSenders: List<String> = emptyList()
    private var activeBlockedSenders: List<String> = emptyList()
    private var activeTimeProfiles: List<TimeProfileEntity> = emptyList()

    override fun onCreate() {
        super.onCreate()
        Log.d("AuraFilter", "🟢 FilterService Created & Booting Up!")
        serviceScope.launch { keywordDao.getWhitelist().collect { entities -> activeWhitelist = entities.map { it.keyword } } }
        serviceScope.launch { keywordDao.getBlacklist().collect { entities -> activeBlacklist = entities.map { it.keyword } } }
        serviceScope.launch { senderRuleDao.getWhitelistSenders().collect { entities -> activeVipSenders = entities.map { it.senderName } } }
        serviceScope.launch { senderRuleDao.getBlacklistSenders().collect { entities -> activeBlockedSenders = entities.map { it.senderName } } }
        serviceScope.launch { timeProfileDao.getAllProfiles().collect { profiles -> activeTimeProfiles = profiles.filter { it.isEnabled } } }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AuraFilter", "🔴 FilterService Destroyed by Android!")
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

        val pkgName = sbn.packageName
        Log.d("AuraFilter", "🔔 Notification Detected from: $pkgName")

        if (pkgName == packageName) {
            Log.d("AuraFilter", "🚫 Ignored: Came from our own app")
            return
        }

        val prefs = applicationContext.getSharedPreferences("noti_prefs", android.content.Context.MODE_PRIVATE)

        // If is_active is FALSE, we return and ignore the notification
        if (!prefs.getBoolean("is_active", true)) {
            Log.d("AuraFilter", "⏸️ Ignored: Filtering is PAUSED in settings")
            return
        }

        val title = sbn.notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "No Title"
        val text = sbn.notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        Log.d("AuraFilter", "📝 Title: $title | Text: $text")

        val lowerTitle = title.lowercase()
        val fullContent = "$title $text".lowercase()

        serviceScope.launch {
            try {
                val hasPriorityKeyword = activeWhitelist.any { fullContent.contains(it.lowercase()) }
                val hasSpamKeyword = activeBlacklist.any { fullContent.contains(it.lowercase()) }
                val isVipSender = activeVipSenders.any { lowerTitle.contains(it.lowercase()) }
                val isBlockedSender = activeBlockedSenders.any { lowerTitle.contains(it.lowercase()) }

                val rule = appRuleDao.getRuleForApp(pkgName)
                val category = rule?.category ?: "GREEN"
                Log.d("AuraFilter", "📦 Searching DB for: $pkgName | Found Rule: $rule | Category applied: $category")

                Log.d("AuraFilter", "⚙️ App Category is: $category")

                if (hasPriorityKeyword || isVipSender) {
                    Log.d("AuraFilter", "✅ Allowed: VIP or Priority Keyword matched")
                    settingsRepository.incrementAllowedCount()
                    return@launch
                }

                if (hasSpamKeyword || isBlockedSender) {
                    Log.d("AuraFilter", "🛑 Blocked: Spam or Blocked Sender matched")
                    cancelNotification(sbn.key)
                    blockedNotificationDao.insertBlockedNotification(BlockedNotificationEntity(packageName = pkgName, title = title, content = text, timestamp = System.currentTimeMillis()))
                    settingsRepository.incrementBlockedCount()
                    return@launch
                }

                if (!isCurrentlyInFocusMode()) {
                    Log.d("AuraFilter", "✅ Allowed: Not currently in Focus Mode")
                    settingsRepository.incrementAllowedCount()
                    return@launch
                }

                if (category == "RED") {
                    Log.d("AuraFilter", "🛑 Blocked: App is categorized as RED")
                    cancelNotification(sbn.key)
                    blockedNotificationDao.insertBlockedNotification(BlockedNotificationEntity(packageName = pkgName, title = title, content = text, timestamp = System.currentTimeMillis()))
                    settingsRepository.incrementBlockedCount()
                    return@launch
                }

                if (category == "ORANGE") {
                    Log.d("AuraFilter", "⚠️ Review: App is categorized as ORANGE")
                    cancelNotification(sbn.key)
                    notificationDao.insertNotification(InterceptedNotificationEntity(packageName = pkgName, title = title, content = text, timestamp = System.currentTimeMillis()))
                    settingsRepository.incrementReviewCount()
                    return@launch
                }

                Log.d("AuraFilter", "✅ Allowed: Default Green Path")
                settingsRepository.incrementAllowedCount()

            } catch (e: Exception) {
                Log.e("AuraFilter", "❌ ERROR in Coroutine: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
