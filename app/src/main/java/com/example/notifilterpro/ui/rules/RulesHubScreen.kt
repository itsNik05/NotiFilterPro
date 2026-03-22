package com.example.notifilterpro.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RulesHubScreen(
    onNavigateToKeywords: () -> Unit,
    onNavigateToSender: () -> Unit,
    onNavigateToTime: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Premium Color Palette
    val bgColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFF3F4F6)
    val cardColor = if (isDark) Color(0xFF151B29) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cyanAccent = if (isDark) Color(0xFF00E5FF) else Color(0xFF06B6D4)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

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
                    IconButton(onClick = { /* Handle theme toggle if needed */ }) {
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Keyword Filters Card
            HubCard(
                title = "Keyword Filters",
                subtitle = "Block or allow based on text",
                icon = Icons.Default.Search,
                iconColor = Color(0xFF3B82F6), // Blue
                cardColor = cardColor,
                textColor = textColor,
                subTextColor = subTextColor,
                borderColor = borderColor,
                onClick = onNavigateToKeywords
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Allow:", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PreviewChip("OTP", Color(0xFF22C55E), isDark)
                            PreviewChip("Urgent", Color(0xFF22C55E), isDark)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Block:", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PreviewChip("Sale", Color(0xFFEF4444), isDark)
                            PreviewChip("Discount", Color(0xFFEF4444), isDark)
                        }
                    }
                }
            }

            // 2. Sender Rules Card
            HubCard(
                title = "Sender Rules", subtitle = "Filter by contact name",
                icon = Icons.Default.Person, iconColor = Color(0xFFA855F7), // Purple
                cardColor = cardColor, textColor = textColor, subTextColor = subTextColor, borderColor = borderColor,
                onClick = onNavigateToSender
            )

            // 3. Time Profiles Card
            HubCard(
                title = "Time Profiles", subtitle = "E.g., Focus Mode (9 AM - 5 PM)",
                icon = Icons.Default.Schedule, iconColor = Color(0xFFF97316), // Orange
                cardColor = cardColor, textColor = textColor, subTextColor = subTextColor, borderColor = borderColor,
                onClick = onNavigateToTime
            )
        }
    }
}

@Composable
fun HubCard(
    title: String, subtitle: String, icon: ImageVector, iconColor: Color,
    cardColor: Color, textColor: Color, subTextColor: Color, borderColor: Color,
    onClick: () -> Unit, content: @Composable () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                        Text(subtitle, fontSize = 13.sp, color = subTextColor)
                    }
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Go", tint = subTextColor)
            }
            content()
        }
    }
}

@Composable
fun PreviewChip(text: String, color: Color, isDark: Boolean) {
    val chipBg = if (isDark) color.copy(alpha = 0.1f) else color.copy(alpha = 0.05f)
    val chipBorder = if (isDark) color.copy(alpha = 0.2f) else color.copy(alpha = 0.1f)

    Box(
        modifier = Modifier
            .background(chipBg, RoundedCornerShape(8.dp))
            .border(1.dp, chipBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}