package com.example.notifilterpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.notifilterpro.data.preferences.SettingsRepository
import com.example.notifilterpro.ui.MainScreen
import com.example.notifilterpro.ui.OnboardingScreen
import com.example.notifilterpro.ui.theme.NotiFilterProTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current

            // 1. Check if the app currently has notification access
            var hasPermission by remember {
                mutableStateOf(
                    NotificationManagerCompat.getEnabledListenerPackages(context)
                        .contains(context.packageName)
                )
            }

            // 2. Re-check the permission every time the app comes to the foreground
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        hasPermission = NotificationManagerCompat.getEnabledListenerPackages(context)
                            .contains(context.packageName)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            // 3. Theme setup
            val isSystemDark = isSystemInDarkTheme()
            val savedThemePreference by settingsRepository.isDarkModeFlow.collectAsState(initial = null)
            val useDarkTheme = savedThemePreference ?: isSystemDark

            NotiFilterProTheme(darkTheme = useDarkTheme) {
                // 4. The Traffic Light! 🚦
                if (hasPermission) {
                    MainScreen()
                } else {
                    OnboardingScreen(
                        onPermissionGranted = {
                            hasPermission = true
                        }
                    )
                }
            }
        }
    }
}