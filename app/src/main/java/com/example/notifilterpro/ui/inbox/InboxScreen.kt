package com.example.notifilterpro.ui.inbox

import android.content.pm.PackageManager
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notifilterpro.data.local.InterceptedNotificationEntity
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onNavigateToBlocked: () -> Unit = {},
    viewModel: InboxViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val blockedCount by viewModel.blockedCount.collectAsState()
    val allowedCount by viewModel.allowedCount.collectAsState()
    val savedThemePreference by viewModel.isDarkMode.collectAsState()

    val isDark = savedThemePreference ?: isSystemInDarkTheme()

    // Color Palette matching the screenshots
    val bgColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFF3F4F6)
    val cardColor = if (isDark) Color(0xFF151B29) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    // Accents
    val cyanAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF06B6D4)
    val redAccent = Color(0xFFEF4444)
    val orangeAccent = Color(0xFFF97316)
    val greenAccent = Color(0xFF22C55E)

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Aura Filter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = cyanAccent // The cool cyan title!
                        )
                        Text(
                            text = "Peace of mind",
                            fontSize = 12.sp,
                            color = subTextColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleTheme(isDark) }) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDark) Color(0xFFEAB308) else subTextColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. STATS ROW (Matching the screenshot) ---
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f).clickable { onNavigateToBlocked() },
                        title = "BLOCKED", count = blockedCount.toString(),
                        iconColor = redAccent, cardBg = cardColor, textColor = textColor, subTextColor = subTextColor
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "REVIEW", count = notifications.size.toString(),
                        iconColor = orangeAccent, cardBg = cardColor, textColor = textColor, subTextColor = subTextColor
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "ALLOWED", count = allowedCount.toString(),
                        iconColor = greenAccent, cardBg = cardColor, textColor = textColor, subTextColor = subTextColor
                    )
                }
            }

            // --- 2. QUEUE HEADER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(orangeAccent))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Needs Review", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = textColor)
                    }
                    Surface(
                        color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${notifications.size} pending",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = subTextColor
                        )
                    }
                }
            }

            // --- 3. REVIEW CARDS ---
            if (notifications.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(48.dp), tint = greenAccent.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("You're all caught up!", color = subTextColor)
                    }
                }
            } else {
                items(notifications, key = { it.id }) { notification ->
                    AuraNotificationCard(
                        notification = notification,
                        isDark = isDark,
                        cardColor = cardColor,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        onBlock = { viewModel.deleteNotification(notification.id) },
                        onAllow = { viewModel.deleteNotification(notification.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, title: String, count: String, iconColor: Color, cardBg: Color, textColor: Color, subTextColor: Color) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).border(1.dp, iconColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = count, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = textColor)
            Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = subTextColor, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun AuraNotificationCard(
    notification: InterceptedNotificationEntity,
    isDark: Boolean,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    onBlock: () -> Unit,
    onAllow: () -> Unit
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val timeString = DateFormat.format("hh:mm a", Date(notification.timestamp)).toString()

    val realAppName = remember(notification.packageName) {
        try { packageManager.getApplicationLabel(packageManager.getApplicationInfo(notification.packageName, 0)).toString() }
        catch (e: PackageManager.NameNotFoundException) { notification.packageName }
    }

    // Button colors matching the mockup
    val blockBg = if (isDark) Color(0xFF2A1515) else Color(0xFFFFF0F0)
    val blockText = Color(0xFFEF4444)
    val allowBg = if (isDark) Color(0xFF14291E) else Color(0xFFEEFDF3)
    val allowText = Color(0xFF22C55E)

    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            val launchIntent = packageManager.getLaunchIntentForPackage(notification.packageName)
            if (launchIntent != null) context.startActivity(launchIntent) else Toast.makeText(context, "Cannot open", Toast.LENGTH_SHORT).show()
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Mock app icon box
                Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(if (isDark) Color.White.copy(0.1f) else Color.Black.copy(0.05f)))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = realAppName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                    Text(text = timeString, fontSize = 12.sp, color = subTextColor)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = notification.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = textColor)
            if (notification.content.isNotBlank()) {
                Text(text = notification.content, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = subTextColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onBlock, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = blockBg, contentColor = blockText),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Block", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onAllow, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = allowBg, contentColor = allowText),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Allow", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}