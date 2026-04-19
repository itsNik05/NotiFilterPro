package com.nuviolabs.aurafilter.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    hasSeenOnboarding: Boolean,
    hasPermission: Boolean,
    onIntroComplete: () -> Unit,
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFF3F4F6)
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cyanAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF06B6D4)
    val cardColor = if (isDark) Color(0xFF151B29) else Color.White

    val totalPages = if (hasSeenOnboarding) 1 else 2
    val pagerState = rememberPagerState(pageCount = { totalPages })

    val postNotificationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { _ ->
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        context.startActivity(intent)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val isGranted = NotificationManagerCompat.getEnabledListenerPackages(context)
                    .contains(context.packageName)
                if (isGranted) {
                    onPermissionGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(containerColor = bgColor) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                if (!hasSeenOnboarding && page == 0) {
                    IntroPage(
                        textColor = textColor,
                        subTextColor = subTextColor,
                        cyanAccent = cyanAccent,
                        cardColor = cardColor
                    )
                } else {
                    PermissionPage(
                        textColor = textColor,
                        subTextColor = subTextColor,
                        cyanAccent = cyanAccent,
                        hasPermission = hasPermission
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(totalPages) { index ->
                        val color = if (pagerState.currentPage == index) cyanAccent else subTextColor.copy(alpha = 0.3f)
                        val width = if (pagerState.currentPage == index) 24.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                if (!hasSeenOnboarding && pagerState.currentPage == 0) {
                    FloatingActionButton(
                        onClick = {
                            onIntroComplete()
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        containerColor = cyanAccent,
                        contentColor = Color.Black,
                        shape = CircleShape
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                    }
                } else {
                    Button(
                        onClick = {
                            if (hasPermission) {
                                onPermissionGranted()
                            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                postNotificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                context.startActivity(intent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = cyanAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(if (hasPermission) "Open Aura Filter" else "Grant Access", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroPage(
    textColor: Color,
    subTextColor: Color,
    cyanAccent: Color,
    cardColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(cyanAccent.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(50.dp), tint = cyanAccent)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Aura Filter", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Aura Filter quietly catches distracting notifications before they break your focus. Important alerts stay visible, and everything else waits for you in review.",
            fontSize = 16.sp,
            color = subTextColor,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        MechanicRow(Icons.Default.CheckCircle, Color(0xFF22C55E), "Allow", "Keep trusted people and important apps flowing normally.", textColor, subTextColor, cardColor)
        Spacer(modifier = Modifier.height(16.dp))
        MechanicRow(Icons.Default.Warning, Color(0xFFF97316), "Review", "Hold uncertain notifications until you are ready to look.", textColor, subTextColor, cardColor)
        Spacer(modifier = Modifier.height(16.dp))
        MechanicRow(Icons.Default.Cancel, Color(0xFFEF4444), "Block", "Push obvious spam and noise out of the way instantly.", textColor, subTextColor, cardColor)
    }
}

@Composable
private fun MechanicRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    desc: String,
    textColor: Color,
    subTextColor: Color,
    cardColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = textColor)
                Text(desc, fontSize = 12.sp, color = subTextColor)
            }
        }
    }
}

@Composable
private fun PermissionPage(
    textColor: Color,
    subTextColor: Color,
    cyanAccent: Color,
    hasPermission: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(cyanAccent.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(50.dp), tint = cyanAccent)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = if (hasPermission) "You're Ready" else "Turn On Notification Access",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasPermission) {
                "Aura Filter is set up. You can start reviewing notifications and tuning your rules right away."
            } else {
                "To do its job, Aura Filter needs permission to read incoming notifications. Everything stays on your device."
            },
            fontSize = 16.sp,
            color = subTextColor,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
