package com.example.notifilterpro.ui.inbox

import android.content.pm.PackageManager
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifilterpro.data.local.BlockedNotificationDao
import com.example.notifilterpro.data.local.BlockedNotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- THE VIEWMODEL ---
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

// --- THE UI SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: BlockedHistoryViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val blockedList by viewModel.blockedList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blocked History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(Icons.Default.DeleteOutline, "Clear All", tint = Color(0xFFEF4444))
                    }
                }
            )
        }
    ) { padding ->
        if (blockedList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No blocked notifications yet!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(blockedList, key = { it.id }) { item ->
                    // WE NOW CALL THE NEW CARD COMPOSABLE HERE
                    BlockedNotificationCard(item = item)
                }
            }
        }
    }
}

// --- NEW EXTRACTED CARD COMPOSABLE ---
@Composable
fun BlockedNotificationCard(item: BlockedNotificationEntity) {
    val context = LocalContext.current
    val packageManager = context.packageManager

    // TRANSLATE PACKAGE NAME TO REAL APP NAME
    val realAppName = remember(item.packageName) {
        try {
            val appInfo = packageManager.getApplicationInfo(item.packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            item.packageName // Fallback to raw name
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // OPEN THE APP WHEN TAPPED
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
                // USING REAL APP NAME HERE
                Text(realAppName, fontSize = 12.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                Text(DateFormat.format("hh:mm a", item.timestamp).toString(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            if (item.content.isNotBlank()) {
                Text(item.content, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
        }
    }
}