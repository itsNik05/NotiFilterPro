package com.nuviolabs.aurafilter.ui.settings

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nuviolabs.aurafilter.ui.inbox.InboxViewModel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: InboxViewModel = hiltViewModel()
) {
    val isActive by viewModel.isActive.collectAsState()
    val currentInterval by viewModel.currentInterval.collectAsState()
    val savedThemePreference by themeViewModel.isDarkMode.collectAsState()
    val context = LocalContext.current

    val isDark = savedThemePreference ?: isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFF3F4F6)
    val cardColor = if (isDark) Color(0xFF151B29) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
    val cyanAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF06B6D4)
    val redAccent = Color(0xFFEF4444)
    val currentThreshold by viewModel.autoDeleteThreshold.collectAsState()

    // Dialog States
    var showClearDialog by remember { mutableStateOf(false) }
    var showCustomIntervalDialog by remember { mutableStateOf(false) }
    var customIntervalInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = textColor) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. MASTER TOGGLE ---
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (isActive) Icons.Default.PlayCircle else Icons.Default.PauseCircle,
                            contentDescription = null,
                            tint = if (isActive) cyanAccent else subTextColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(if (isActive) "Filtering Active" else "Filtering Paused", fontWeight = FontWeight.Bold, color = textColor)
                            Text(if (isActive) "Engine is running" else "All alerts are allowed", fontSize = 12.sp, color = subTextColor)
                        }
                    }
                    // No more double negatives! If it's active, it's checked.
                    Switch(
                        checked = isActive,
                        onCheckedChange = { viewModel.toggleService(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = cyanAccent, checkedTrackColor = cyanAccent.copy(alpha = 0.5f))
                    )
                }
            }

            // --- 2. NOTIFICATION ACCESS PERMISSION ---
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.clickable { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("System Notification Access", fontWeight = FontWeight.Bold, color = textColor)
                        Text("Tap to fix if the engine stops working", fontSize = 12.sp, color = subTextColor)
                    }
                }
            }

            // --- 3. DIGEST FREQUENCY ---
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = cyanAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Digest Frequency", fontWeight = FontWeight.Bold, color = textColor)
                            Text("How often to remind you of waiting alerts", fontSize = 12.sp, color = subTextColor)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 4).forEach { hours ->
                            FrequencyChip("${hours}h", currentInterval == hours, cyanAccent, isDark) { viewModel.updateInterval(hours) }
                        }
                        FrequencyChip("Custom", currentInterval !in listOf(1, 2, 4), cyanAccent, isDark) { showCustomIntervalDialog = true }
                    }
                    if (currentInterval !in listOf(1, 2, 4)) {
                        Text("Currently set to every $currentInterval hours", fontSize = 12.sp, color = cyanAccent, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }

            // --- BATTERY OPTIMIZATION (KEEP ALIVE) ---
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.clickable {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = android.net.Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                        }
                    }.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Swapped SettingsPower for Lock
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Prevent Sleep (Recommended)", fontWeight = FontWeight.Bold, color = textColor)
                        Text("Stop Android from killing the engine", fontSize = 12.sp, color = subTextColor)
                    }
                }
            }

            // --- AUTO DELETE THRESHOLD ---
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Swapped AutoDelete for standard Delete
                        Icon(Icons.Default.Delete, contentDescription = null, tint = cyanAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Auto-Delete Logs", fontWeight = FontWeight.Bold, color = textColor)
                            Text("How long to keep intercepted notifications", fontSize = 12.sp, color = subTextColor)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FrequencyChip("24h", currentThreshold == 24, cyanAccent, isDark) { viewModel.updateAutoDeleteThreshold(24) }
                        FrequencyChip("48h", currentThreshold == 48, cyanAccent, isDark) { viewModel.updateAutoDeleteThreshold(48) }
                        FrequencyChip("7 Days", currentThreshold == 168, cyanAccent, isDark) { viewModel.updateAutoDeleteThreshold(168) }
                        FrequencyChip("Forever", currentThreshold == -1, cyanAccent, isDark) { viewModel.updateAutoDeleteThreshold(-1) }
                    }
                }
            }

            // --- 4. CLEAR DATA ---
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.clickable { showClearDialog = true }.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = redAccent, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Clear History", fontWeight = FontWeight.Bold, color = redAccent)
                        Text("Wipe all Blocked and Review data", fontSize = 12.sp, color = subTextColor)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // --- CLEAR DATA CONFIRMATION DIALOG ---
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = cardColor,
            title = { Text("Clear All History?", fontWeight = FontWeight.Bold, color = textColor) },
            text = { Text("This will permanently delete all logs of blocked and reviewed notifications. Your rules will not be affected.", color = subTextColor) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDialog = false
                        Toast.makeText(context, "History Cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = redAccent)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel", color = textColor) }
            }
        )
    }

    // --- CUSTOM INTERVAL DIALOG ---
    if (showCustomIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showCustomIntervalDialog = false },
            containerColor = cardColor,
            title = { Text("Custom Frequency", fontWeight = FontWeight.Bold, color = textColor) },
            text = {
                OutlinedTextField(
                    value = customIntervalInput,
                    onValueChange = { if (it.all { char -> char.isDigit() }) customIntervalInput = it },
                    label = { Text("Hours", color = subTextColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = cyanAccent)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hrs = customIntervalInput.toIntOrNull()
                        if (hrs != null && hrs > 0) {
                            viewModel.updateInterval(hrs)
                            showCustomIntervalDialog = false
                            customIntervalInput = ""
                        } else {
                            Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cyanAccent)
                ) { Text("Save", color = Color.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showCustomIntervalDialog = false }) { Text("Cancel", color = textColor) }
            }
        )
    }
}

@Composable
fun RowScope.FrequencyChip(label: String, isSelected: Boolean, activeColor: Color, isDark: Boolean, onClick: () -> Unit) {
    val unselectedBg = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
    val unselectedText = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.2f) else unselectedBg)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) activeColor else unselectedText)
    }
}
