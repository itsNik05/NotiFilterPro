package com.nuviolabs.aurafilter.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuviolabs.aurafilter.data.local.InterceptedNotificationEntity
import com.nuviolabs.aurafilter.data.local.NotificationDao
import com.nuviolabs.aurafilter.data.preferences.SettingsRepository
import com.nuviolabs.aurafilter.data.preferences.WeeklyTrendPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeeklyOverview(
    val blocked: Int = 0,
    val review: Int = 0,
    val allowed: Int = 0,
    val total: Int = 0,
    val hoursSaved: Float = 0f,
    val trend: List<WeeklyTrendPoint> = emptyList()
)

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val notificationDao: NotificationDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Automatically fetches and updates the UI whenever a new notification is intercepted
    val notifications: StateFlow<List<InterceptedNotificationEntity>> = notificationDao.getAllOrangeNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Tracks the total number of blocked (Red) notifications
    val blockedCount: StateFlow<Int> = settingsRepository.blockedCountFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Tracks the total number of allowed (Green) notifications
    val allowedCount: StateFlow<Int> = settingsRepository.allowedCountFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val weeklyOverview: StateFlow<WeeklyOverview> = combine(
        settingsRepository.weeklyBlockedCountFlow,
        settingsRepository.weeklyReviewCountFlow,
        settingsRepository.weeklyAllowedCountFlow,
        settingsRepository.weeklyTrendFlow
    ) { blocked, review, allowed, trend ->
        val total = blocked + review + allowed
        val secondsSaved = (blocked * 45f) + (review * 20f)
        WeeklyOverview(
            blocked = blocked,
            review = review,
            allowed = allowed,
            total = total,
            hoursSaved = secondsSaved / 3600f,
            trend = trend
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeeklyOverview()
    )

    fun deleteNotification(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            notificationDao.deleteNotificationById(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            notificationDao.clearAll()
        }
    }

    // Read the dark mode state
    val isDarkMode: StateFlow<Boolean?> = settingsRepository.isDarkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Function to trigger the flip
    fun toggleTheme(currentIsDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(!currentIsDark)
        }
    }
}
