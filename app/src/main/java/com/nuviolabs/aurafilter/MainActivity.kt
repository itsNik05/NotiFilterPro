package com.nuviolabs.aurafilter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nuviolabs.aurafilter.data.preferences.SettingsRepository
import com.nuviolabs.aurafilter.ui.MainScreen
import com.nuviolabs.aurafilter.ui.OnboardingScreen
import com.nuviolabs.aurafilter.ui.theme.AuraFilterTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
            val coroutineScope = rememberCoroutineScope()

            var hasPermission by remember {
                mutableStateOf(
                    NotificationManagerCompat.getEnabledListenerPackages(context)
                        .contains(context.packageName)
                )
            }

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

            val isSystemDark = isSystemInDarkTheme()
            val savedThemePreference by settingsRepository.isDarkModeFlow.collectAsState(initial = null)
            val hasSeenOnboarding by settingsRepository.hasSeenOnboardingFlow.collectAsState(initial = false)
            val useDarkTheme = savedThemePreference ?: isSystemDark

            AuraFilterTheme(darkTheme = useDarkTheme) {
                if (hasSeenOnboarding && hasPermission) {
                    MainScreen()
                } else {
                    OnboardingScreen(
                        hasSeenOnboarding = hasSeenOnboarding,
                        hasPermission = hasPermission,
                        onIntroComplete = {
                            coroutineScope.launch {
                                settingsRepository.markOnboardingSeen()
                            }
                        },
                        onPermissionGranted = {
                            hasPermission = true
                            coroutineScope.launch {
                                settingsRepository.markOnboardingSeen()
                            }
                        }
                    )
                }
            }
        }
    }
}
