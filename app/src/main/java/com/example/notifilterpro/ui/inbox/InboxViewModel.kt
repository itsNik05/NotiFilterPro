package com.example.notifilterpro.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifilterpro.data.local.InterceptedNotificationEntity
import com.example.notifilterpro.data.local.NotificationDao
import com.example.notifilterpro.data.preferences.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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