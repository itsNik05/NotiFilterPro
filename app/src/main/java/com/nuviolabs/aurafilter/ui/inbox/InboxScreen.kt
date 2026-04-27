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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nuviolabs.aurafilter.data.local.InterceptedNotificationEntity
import com.nuviolabs.aurafilter.data.preferences.WeeklyTrendPoint
import com.nuviolabs.aurafilter.ui.theme.AuraStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onNavigateToBlocked: () -> Unit = {},
    onNavigateToReview: () -> Unit = {},
    viewModel: InboxViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val blockedCount by viewModel.blockedCount.collectAsState()
    val allowedCount by viewModel.allowedCount.collectAsState()
    val weeklyOverview by viewModel.weeklyOverview.collectAsState()
    val savedThemePreference by viewModel.isDarkMode.collectAsState()
    val isDark = savedThemePreference ?: isSystemInDarkTheme()
    val palette = AuraStyle.palette(isDark)

    val bgColor = palette.bg
    val cardColor = palette.surface
    val textColor = palette.text
    val subTextColor = palette.muted2

    val cyanAccent = AuraStyle.Cyan
    val redAccent = AuraStyle.Red
    val orangeAccent = AuraStyle.Orange
    val greenAccent = AuraStyle.Green

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(end = 12.dp)) {
                        Text(
                            text = "Aura Filter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = cyanAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "// notification guard",
                            fontSize = 9.sp,
                            color = palette.muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    Surface(
                        color = AuraStyle.GreenDim,
                        border = BorderStroke(1.dp, AuraStyle.Green),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AuraStyle.Green)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ON", color = AuraStyle.Green, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 0.5.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor,
                    scrolledContainerColor = bgColor
                )
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
                HeroCard(
                    blockedCount = weeklyOverview.blocked,
                    reviewCount = weeklyOverview.review,
                    allowedCount = weeklyOverview.allowed,
                    totalCount = weeklyOverview.total,
                    hoursSaved = weeklyOverview.hoursSaved,
                    trend = weeklyOverview.trend,
                    palette = palette,
                    textColor = textColor,
                    subTextColor = subTextColor
                )
            }

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
                        subTextColor = subTextColor,
                        borderColor = palette.border
                    )
                    StatCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToReview() },
                        title = "REVIEW",
                        count = notifications.size.toString(),
                        iconColor = orangeAccent,
                        cardBg = cardColor,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        borderColor = palette.border
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "ALLOWED",
                        count = allowedCount.toString(),
                        iconColor = greenAccent,
                        cardBg = cardColor,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        borderColor = palette.border
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val savedThemePreference by viewModel.isDarkMode.collectAsState()
    var showClearAllDialog by remember { mutableStateOf(false) }

    val isDark = savedThemePreference ?: isSystemInDarkTheme()
    val palette = AuraStyle.palette(isDark)
    val bgColor = palette.bg
    val cardColor = palette.surface
    val textColor = palette.text
    val subTextColor = palette.muted2
    val orangeAccent = AuraStyle.Orange
    val greenAccent = AuraStyle.Green

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

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Needs Review", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = textColor)
                        Text("${notifications.size} queued", fontSize = 10.sp, color = palette.muted, letterSpacing = 1.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    AnimatedVisibility(visible = notifications.isNotEmpty()) {
                        TextButton(onClick = { showClearAllDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = orangeAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear", color = orangeAccent, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor,
                    scrolledContainerColor = bgColor
                )
            )
        }
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(48.dp), tint = greenAccent.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
                Text("You're all caught up!", color = subTextColor)
            }
        } else {
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
                    Surface(
                        color = AuraStyle.OrangeDim,
                        border = BorderStroke(1.dp, AuraStyle.Orange.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Swipe a card left or right to remove it from review.",
                            modifier = Modifier.padding(12.dp),
                            color = subTextColor,
                            fontSize = 12.sp
                        )
                    }
                }

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
}

@Composable
private fun StatCard(
    modifier: Modifier,
    title: String,
    count: String,
    iconColor: Color,
    cardBg: Color,
    textColor: Color,
    subTextColor: Color,
    borderColor: Color
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f))
                    .border(1.dp, iconColor.copy(alpha = 0.4f), CircleShape),
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
private fun HeroCard(
    blockedCount: Int,
    reviewCount: Int,
    allowedCount: Int,
    totalCount: Int,
    hoursSaved: Float,
    trend: List<WeeklyTrendPoint>,
    palette: AuraStyle.Palette,
    textColor: Color,
    subTextColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, AuraStyle.Cyan.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.heroGradient)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    "HOURS SAVED THIS WEEK",
                    color = AuraStyle.Cyan.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%.1f", hoursSaved),
                        color = AuraStyle.Cyan,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 52.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("hrs", color = AuraStyle.Cyan.copy(alpha = 0.65f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "Estimated from $totalCount notifications handled in the last 7 days",
                    color = subTextColor,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                WeeklyTrendRow(
                    trend = trend,
                    palette = palette
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroMiniStat("BLOCKED", blockedCount.toString(), AuraStyle.Red, Modifier.weight(1f), palette)
                    HeroMiniStat("REVIEW", reviewCount.toString(), AuraStyle.Orange, Modifier.weight(1f), palette)
                    HeroMiniStat("ALLOWED", allowedCount.toString(), AuraStyle.Green, Modifier.weight(1f), palette)
                }
            }
        }
    }
}

@Composable
private fun WeeklyTrendRow(trend: List<WeeklyTrendPoint>, palette: AuraStyle.Palette) {
    val maxTotal = (trend.maxOfOrNull { it.blocked + it.review + it.allowed } ?: 0).coerceAtLeast(1)
    Column {
        Text(
            text = "LAST 7 DAYS",
            color = palette.muted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            trend.forEach { point ->
                WeeklyTrendBar(
                    point = point,
                    maxTotal = maxTotal,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WeeklyTrendBar(
    point: WeeklyTrendPoint,
    maxTotal: Int,
    palette: AuraStyle.Palette,
    modifier: Modifier = Modifier
) {
    val total = point.blocked + point.review + point.allowed
    val totalHeight = if (total == 0) 4.dp else scaledHeight(total, maxTotal)
    val blockedHeight = fractionHeight(totalHeight, point.blocked, total)
    val reviewHeight = fractionHeight(totalHeight, point.review, total)
    val allowedHeight = fractionHeight(totalHeight, point.allowed, total)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(68.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                    .background(if (palette.bg == AuraStyle.Bg) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.05f))
                    .padding(horizontal = 3.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                if (allowedHeight > 0.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(allowedHeight)
                            .background(AuraStyle.Green, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    )
                }
                if (reviewHeight > 0.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(reviewHeight)
                            .background(AuraStyle.Orange)
                    )
                }
                if (blockedHeight > 0.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(blockedHeight)
                            .background(AuraStyle.Red, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(point.dayLabel, color = palette.muted, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun scaledHeight(value: Int, max: Int): Dp {
    val ratio = value.toFloat() / max.toFloat()
    return (8f + (52f * ratio)).dp
}

private fun fractionHeight(totalHeight: Dp, part: Int, total: Int): Dp {
    if (part <= 0 || total <= 0) return 0.dp
    return (totalHeight.value * (part.toFloat() / total.toFloat())).dp
}

@Composable
private fun HeroMiniStat(label: String, value: String, color: Color, modifier: Modifier, palette: AuraStyle.Palette) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (palette.bg == AuraStyle.Bg) Color.Black.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.65f))
            .border(1.dp, palette.border, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = palette.muted, fontSize = 9.sp, letterSpacing = 0.5.sp)
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
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = dismissText)
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
