package com.example.notifilterpro.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isPaused by viewModel.isPaused.collectAsState()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    // Premium Color Palette
    val bgColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFF3F4F6)
    val cardColor = if (isDark) Color(0xFF151B29) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cyanAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF06B6D4)
    val headerBlue = Color(0xFF3B82F6) // Matches the "Behavior" header in mockup

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Aura Filter", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = cyanAccent)
                        Text("Peace of mind", fontSize = 12.sp, color = subTextColor)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle theme toggle */ }) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Theme",
                            tint = if (isDark) Color(0xFFEAB308) else subTextColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- BEHAVIOR SECTION ---
            Text(
                text = "Behavior",
                color = headerBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Pause Filtering Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pause Filtering", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
                            Text("Temporarily allow all notifications", fontSize = 12.sp, color = subTextColor)
                        }
                        Switch(
                            checked = isPaused,
                            onCheckedChange = { viewModel.toggleService(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = headerBlue,
                                uncheckedThumbColor = subTextColor,
                                uncheckedTrackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- DATA MANAGEMENT SECTION ---
            Text(
                text = "Data Management",
                color = Color(0xFFA855F7), // Purple header
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Clear History", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
                            Text("Wipe all Blocked and Review data", fontSize = 12.sp, color = subTextColor)
                        }
                        Button(
                            onClick = {
                                viewModel.clearAllData()
                                Toast.makeText(context, "All history cleared!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF2A1515) else Color(0xFFFFF0F0), contentColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Clear", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}