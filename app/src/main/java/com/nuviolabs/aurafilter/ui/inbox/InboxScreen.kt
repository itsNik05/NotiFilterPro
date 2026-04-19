package com.nuviolabs.aurafilter.ui.inbox

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nuviolabs.aurafilter.data.local.InterceptedNotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

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
    var showClearAllDialog by remember { mutableStateOf(false) }

    val isDark = savedThemePreference ?: isSystemInDarkTheme()

    val bgColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFF3F4F6)
    val cardColor = if (isDark) Color(0xFF151B29) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    val cyanAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF06B6D4)
    val redAccent = Color(0xFFEF4444)
    val orangeAccent = Color(0xFFF97316)
    val greenAccent = Color(0xFF22C55E)

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(end = 12.dp)) {
                        Text(
                            text = "Aura Filter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = cyanAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Peace of mind",
                            fontSize = 11.sp,
                            color = subTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToBlocked() },
                        title = "BLOCKED",
                        count = blockedCount.toString(),
                        iconColor = redAccent,
                        cardBg = cardColor,
                        textColor = textColor,
                        subTextColor = subTextColor
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "REVIEW",
                        count = notifications.size.toString(),
                        iconColor = orangeAccent,
                        cardBg = cardColor,
                        textColor = textColor,
                        subTextColor = subTextColor
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "ALLOWED",
                        count = allowedCount.toString(),
                        iconColor = greenAccent,
                        cardBg = cardColor,
                        textColor = textColor,
                        subTextColor = subTextColor
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(orangeAccent))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Needs Review", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = textColor)
                        }
                        Surface(
                            color = if (isDark) Color(0xFF1E2433) else Color(0xFFE9EEF5),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(orangeAccent)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${notifications.size} queued",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = subTextColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    AnimatedVisibility(visible = notifications.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearAllDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = orangeAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear all review items", color = orangeAccent, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (notifications.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
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
                        onDismiss = { viewModel.deleteNotification(notification.id) }
                    )
                }
            }
        }
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear review queue?") },
            text = { Text("This will remove all notifications from the review page.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAll()
                        showClearAllDialog = false
                    }
                ) {
                    Text("Clear all")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    title: String,
    count: String,
    iconColor: Color,
    cardBg: Color,
    textColor: Color,
    subTextColor: Color
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(1.dp, iconColor.copy(alpha = 0.3f), CircleShape),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuraNotificationCard(
    notification: InterceptedNotificationEntity,
    isDark: Boolean,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val timeString = DateFormat.format("hh:mm a", Date(notification.timestamp)).toString()

    val realAppName = remember(notification.packageName) {
        try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(notification.packageName, 0)
            ).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            notification.packageName
        }
    }

    val appIcon by produceState<ImageBitmap?>(initialValue = null, notification.packageName) {
        value = withContext(Dispatchers.IO) {
            getAppIconBitmap(context, notification.packageName)
        }
    }

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.3f },
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onDismiss()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val dismissColor = if (isDark) Color(0xFF132031) else Color(0xFFE0F2FE)
            val dismissText = if (isDark) Color(0xFF7DD3FC) else Color(0xFF0369A1)
            val alignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Alignment.CenterEnd
            } else {
                Alignment.CenterStart
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(dismissColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = dismissText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remove", color = dismissText, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val launchIntent = packageManager.getLaunchIntentForPackage(notification.packageName)
                    if (launchIntent != null) {
                        context.startActivity(launchIntent)
                    } else {
                        Toast.makeText(context, "Cannot open", Toast.LENGTH_SHORT).show()
                    }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (appIcon != null) {
                        Image(
                            bitmap = appIcon!!,
                            contentDescription = "App Icon",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color.White.copy(0.1f) else Color.Black.copy(0.05f))
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = realAppName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                        Text(text = timeString, fontSize = 12.sp, color = subTextColor)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = notification.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = textColor)
                if (notification.content.isNotBlank()) {
                    Text(
                        text = notification.content,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = subTextColor
                    )
                }
            }
        }
    }
}

fun getAppIconBitmap(context: Context, packageName: String): ImageBitmap? {
    return try {
        val drawable = context.packageManager.getApplicationIcon(packageName)
        val size = 96

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
