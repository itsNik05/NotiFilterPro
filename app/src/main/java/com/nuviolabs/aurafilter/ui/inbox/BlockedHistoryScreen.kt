package com.nuviolabs.aurafilter.ui.inbox

import android.content.pm.PackageManager
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuviolabs.aurafilter.data.local.BlockedNotificationDao
import com.nuviolabs.aurafilter.data.local.BlockedNotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockedHistoryViewModel @Inject constructor(
    private val blockedDao: BlockedNotificationDao
) : ViewModel() {
    val blockedList = blockedDao.getAllBlockedNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) { blockedDao.clearAll() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: BlockedHistoryViewModel = hiltViewModel(),
    themeViewModel: InboxViewModel = hiltViewModel() // Syncing the theme!
) {
    val blockedList by viewModel.blockedList.collectAsState()
    val savedThemePreference by themeViewModel.isDarkMode.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    val isDark = savedThemePreference ?: isSystemInDarkTheme()

    // Aura Premium Palette
    val bgColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFF3F4F6)
    val cardColor = if (isDark) Color(0xFF151B29) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val redAccent = Color(0xFFEF4444)

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Blocked History", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Back", tint = textColor) }
                },
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Default.DeleteOutline, "Clear All", tint = redAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor,
                    scrolledContainerColor = bgColor
                )
            )
        }
    ) { padding ->
        if (blockedList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No blocked notifications yet!", color = subTextColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(blockedList, key = { it.id }) { item ->
                    BlockedNotificationCard(item, isDark, cardColor, textColor, subTextColor, redAccent)
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = cardColor,
            title = { Text("Clear blocked history?", fontWeight = FontWeight.Bold, color = textColor) },
            text = { Text("This will permanently delete all blocked notification logs.", color = subTextColor) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Delete", color = redAccent, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = textColor)
                }
            }
        )
    }
}

@Composable
fun BlockedNotificationCard(
    item: BlockedNotificationEntity,
    isDark: Boolean,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    redAccent: Color
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val borderColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    val realAppName = remember(item.packageName) {
        try {
            val appInfo = packageManager.getApplicationInfo(item.packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            item.packageName
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val launchIntent = packageManager.getLaunchIntentForPackage(item.packageName)
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                } else {
                    Toast.makeText(context, "Cannot open this app", Toast.LENGTH_SHORT).show()
                }
            }
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(realAppName, fontSize = 12.sp, color = redAccent, fontWeight = FontWeight.Bold)
                Text(DateFormat.format("hh:mm a", item.timestamp).toString(), fontSize = 12.sp, color = subTextColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = textColor)
            if (item.content.isNotBlank()) {
                Text(item.content, fontSize = 14.sp, color = subTextColor, maxLines = 2)
            }
        }
    }
}
