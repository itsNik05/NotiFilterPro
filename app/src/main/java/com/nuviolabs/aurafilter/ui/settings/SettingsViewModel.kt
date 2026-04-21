package com.nuviolabs.aurafilter.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuviolabs.aurafilter.data.local.BlockedNotificationDao
import com.nuviolabs.aurafilter.data.local.NotificationDao
import com.nuviolabs.aurafilter.data.preferences.SettingsRepository
import com.nuviolabs.aurafilter.worker.AutoClearScheduler
import com.nuviolabs.aurafilter.worker.DigestScheduler
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

    private val prefs = context.getSharedPreferences("noti_prefs", Context.MODE_PRIVATE)

    // --- 0. AUTO-DELETE THRESHOLD ---
    private val _autoDeleteThreshold = MutableStateFlow(prefs.getInt("auto_delete_threshold", 24))
    val autoDeleteThreshold: StateFlow<Int> = _autoDeleteThreshold.asStateFlow()

    fun updateAutoDeleteThreshold(newThreshold: Int) {
        _autoDeleteThreshold.value = newThreshold
        prefs.edit().putInt("auto_delete_threshold", newThreshold).apply()
        viewModelScope.launch {
            AutoClearScheduler.schedule(context)
        }
    }

    // --- 1. EXISTING DIGEST INTERVAL LOGIC ---
    val currentInterval: StateFlow<Int> = settingsRepository.digestIntervalFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 2
        )

    // Keep this above init so the initial digest sync does not read an uninitialized state.
    private val _isActive = MutableStateFlow(prefs.getBoolean("is_active", true))
    val isActive = _isActive.asStateFlow()

    init {
        viewModelScope.launch {
            syncDigestSchedule(currentInterval.value, _isActive.value)
            AutoClearScheduler.schedule(context)
        }
    }

    fun updateInterval(hours: Int) {
        viewModelScope.launch {
            settingsRepository.saveDigestInterval(hours)
            syncDigestSchedule(hours, _isActive.value)
        }
    }

    fun toggleService(active: Boolean) {
        // .commit() saves instantly so the FilterService reads it immediately
        prefs.edit().putBoolean("is_active", active).commit()
        _isActive.value = active
        viewModelScope.launch {
            syncDigestSchedule(currentInterval.value, active)
        }
    }

    // --- 3. CLEAR DATA LOGIC ---
    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            blockedDao.clearAllBlocked()
            notificationDao.clearAllIntercepted()
        }
    }

    private fun syncDigestSchedule(intervalHours: Int, active: Boolean) {
        if (active) {
            DigestScheduler.schedule(context, intervalHours.toLong())
        } else {
            DigestScheduler.cancel(context)
        }
    }
}
