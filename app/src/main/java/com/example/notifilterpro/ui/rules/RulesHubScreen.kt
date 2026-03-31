package com.example.notifilterpro.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notifilterpro.ui.inbox.InboxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesHubScreen(
    onNavigateToKeywords: () -> Unit,
    onNavigateToSender: () -> Unit,
    onNavigateToTime: () -> Unit,
    themeViewModel: InboxViewModel = hiltViewModel()
) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val isDark = isDarkMode ?: isSystemInDarkTheme()

    val bgColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFF3F4F6)
    val cardColor = if (isDark) Color(0xFF151B29) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cyanAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF06B6D4)

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize()
        ) {
            // FIX: Blocked History has been completely removed
            RuleCard("Keyword Filters", "Block or allow based on text", Icons.Default.Search, Color(0xFF3B82F6), cardColor, textColor, subTextColor, onNavigateToKeywords)
            RuleCard("Sender Rules", "Filter by contact name", Icons.Default.Person, Color(0xFF9333EA), cardColor, textColor, subTextColor, onNavigateToSender)
            RuleCard("Time Profiles", "E.g., Focus Mode (9 AM - 5 PM)", Icons.Default.Schedule, Color(0xFFF97316), cardColor, textColor, subTextColor, onNavigateToTime)
        }
    }
}

@Composable
fun RuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Background box for the icon (matches screenshot)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                    Text(subtitle, fontSize = 12.sp, color = subTextColor)
                }
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = subTextColor)
        }
    }
}