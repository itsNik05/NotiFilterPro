package com.example.notifilterpro.ui

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun OnboardingScreen(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // Aesthetic Colors
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFF3F4F6)
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cyanAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF06B6D4)
    val cardColor = if (isDark) Color(0xFF151B29) else Color.White

    // Swipeable Pager State
    val pagerState = rememberPagerState(pageCount = { 4 })

    // This launcher handles the Android 13+ POST_NOTIFICATIONS popup
    val postNotificationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Move to the Listener Settings after they answer the popup
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        context.startActivity(intent)
    }

    // Auto-Advance Observer
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

            // The Swipeable Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> OnboardingPage(
                        icon = Icons.Default.Shield,
                        iconTint = cyanAccent,
                        title = "Welcome to Aura Filter",
                        description = "Take back your peace of mind. Automatically block spam, mute distractions, and review notifications on your own schedule.",
                        textColor = textColor, subTextColor = subTextColor
                    )
                    1 -> MechanicPage(textColor, subTextColor, cardColor)
                    2 -> BatteryPage(textColor, subTextColor, cyanAccent, cardColor)
                    3 -> PermissionPage(textColor, subTextColor, cyanAccent)
                }
            }

            // Bottom Navigation Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) { index ->
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

                if (pagerState.currentPage < 3) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        containerColor = cyanAccent,
                        contentColor = Color.Black,
                        shape = CircleShape
                    ) {
                        // FIX: Updated to the AutoMirrored version to clear the warning
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                    }
                } else {
                    Button(
                        onClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
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
                        Text("Grant Access", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- STANDARD PAGE TEMPLATE ---
@Composable
fun OnboardingPage(icon: ImageVector, iconTint: Color, title: String, description: String, textColor: Color, subTextColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(100.dp).background(iconTint.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(50.dp), tint = iconTint)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = description, fontSize = 16.sp, color = subTextColor, textAlign = TextAlign.Center, lineHeight = 24.sp)
    }
}

// --- SLIDE 2: HOW IT WORKS ---
@Composable
fun MechanicPage(textColor: Color, subTextColor: Color, cardColor: Color) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("How it Works", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
        Spacer(modifier = Modifier.height(32.dp))

        MechanicRow(Icons.Default.CheckCircle, Color(0xFF22C55E), "Allow Zone", "VIPs and essential apps bypass the filter.", textColor, subTextColor, cardColor)
        Spacer(modifier = Modifier.height(16.dp))
        MechanicRow(Icons.Default.Warning, Color(0xFFF97316), "Review Zone", "Uncategorized alerts are held here for you.", textColor, subTextColor, cardColor)
        Spacer(modifier = Modifier.height(16.dp))
        MechanicRow(Icons.Default.Cancel, Color(0xFFEF4444), "Block Zone", "Spam and distractions are instantly destroyed.", textColor, subTextColor, cardColor)
    }
}

@Composable
fun MechanicRow(icon: ImageVector, iconTint: Color, title: String, desc: String, textColor: Color, subTextColor: Color, cardColor: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = cardColor), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = textColor)
                Text(desc, fontSize = 12.sp, color = subTextColor)
            }
        }
    }
}

// --- SLIDE 3: BATTERY ---
@Composable
fun BatteryPage(textColor: Color, subTextColor: Color, cyanAccent: Color, cardColor: Color) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(100.dp).background(Color(0xFFEAB308).copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.BatteryAlert, contentDescription = null, modifier = Modifier.size(50.dp), tint = Color(0xFFEAB308))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Keep the Engine Running", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Android phones like to kill background apps. Please exempt Aura from battery restrictions so we can keep catching spam.", fontSize = 16.sp, color = subTextColor, textAlign = TextAlign.Center, lineHeight = 24.sp)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // FIX: Removed the restricted intent to keep you safe from Play Store rejection.
                // This now safely opens the general Battery Optimization settings page instead.
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = cardColor, contentColor = cyanAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Open Battery Settings")
        }
    }
}

// --- SLIDE 4: PERMISSION ---
@Composable
fun PermissionPage(textColor: Color, subTextColor: Color, cyanAccent: Color) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(100.dp).background(cyanAccent.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(50.dp), tint = cyanAccent)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "The Final Step", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "To do its job, Aura Filter needs permission to read incoming notifications. We process everything locally—your data never leaves your device.", fontSize = 16.sp, color = subTextColor, textAlign = TextAlign.Center, lineHeight = 24.sp)
    }
}