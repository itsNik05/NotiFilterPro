package com.example.notifilterpro.ui.rules

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
// THESE ARE THE MISSING IMPORTS:
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notifilterpro.ui.inbox.InboxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesHubScreen(
    onNavigateToKeywords: () -> Unit,
    onNavigateToSender: () -> Unit,
    onNavigateToTime: () -> Unit,
    onNavigateToBlocked: () -> Unit,
    themeViewModel: InboxViewModel = hiltViewModel()
) {
    // FIX: Actively using the themeViewModel so it syncs with the Home screen
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val isDark = isDarkMode ?: isSystemInDarkTheme()

    val bgColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFF3F4F6)
    val cardColor = if (isDark) Color(0xFF151B29) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    Scaffold(containerColor = bgColor) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize()
        ) {
            Text(
                text = "Control Center",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(20.dp))

            RuleCard("Smart Keywords", "Filter by text", Icons.Default.Search, Color(0xFF3B82F6), cardColor, textColor, subTextColor, onNavigateToKeywords)
            RuleCard("Sender Rules", "VIPs & Blacklist", Icons.Default.Person, Color(0xFF22C55E), cardColor, textColor, subTextColor, onNavigateToSender)
            RuleCard("Time Profiles", "Schedule focus", Icons.Default.Schedule, Color(0xFFF97316), cardColor, textColor, subTextColor, onNavigateToTime)

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = subTextColor.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // FIX: Actively using onNavigateToBlocked here for the button
            RuleCard("Blocked History", "View caught alerts", Icons.Default.History, Color(0xFFEF4444), cardColor, textColor, subTextColor, onNavigateToBlocked)
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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = textColor)
                Text(subtitle, fontSize = 12.sp, color = subTextColor)
            }
        }
    }
}