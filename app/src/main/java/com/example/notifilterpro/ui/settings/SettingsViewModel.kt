package com.example.notifilterpro.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifilterpro.data.local.BlockedNotificationDao
import com.example.notifilterpro.data.local.NotificationDao
import com.example.notifilterpro.data.preferences.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val blockedDao: BlockedNotificationDao,
    private val notificationDao: NotificationDao
) : ViewModel() {

    // --- 1. EXISTING DIGEST INTERVAL LOGIC ---
    val currentInterval: StateFlow<Int> = settingsRepository.digestIntervalFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 2
        )

    fun updateInterval(hours: Int) {
        viewModelScope.launch {
            settingsRepository.saveDigestInterval(hours)
        }
    }

    // --- 2. NEW MASTER TOGGLE LOGIC ---
    private val prefs = context.getSharedPreferences("noti_prefs", Context.MODE_PRIVATE)

    private val _isPaused = MutableStateFlow(prefs.getBoolean("is_paused", false))
    val isPaused = _isPaused.asStateFlow()

    fun toggleService(paused: Boolean) {
        prefs.edit().putBoolean("is_paused", paused).apply()
        _isPaused.value = paused
    }

    // --- 3. NEW CLEAR DATA LOGIC ---
    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            blockedDao.clearAllBlocked()
            notificationDao.clearAllIntercepted()
        }
    }
}