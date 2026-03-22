package com.example.notifilterpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.notifilterpro.data.preferences.SettingsRepository
import com.example.notifilterpro.ui.MainScreen
import com.example.notifilterpro.ui.theme.NotiFilterProTheme // <-- Updated to the likely correct name!
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isSystemDark = isSystemInDarkTheme()
            val savedThemePreference by settingsRepository.isDarkModeFlow.collectAsState(initial = null)
            val useDarkTheme = savedThemePreference ?: isSystemDark

            // Updated name here too!
            NotiFilterProTheme(darkTheme = useDarkTheme) {
                MainScreen()
            }
        }
    }
}