package com.example.notifilterpro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.notifilterpro.ui.categorizer.AppCategorizerScreen
import com.example.notifilterpro.ui.inbox.BlockedHistoryScreen
import com.example.notifilterpro.ui.inbox.InboxScreen
import com.example.notifilterpro.ui.rules.RulesHubScreen
import com.example.notifilterpro.ui.rules.RulesManagerScreen
import com.example.notifilterpro.ui.rules.SenderRulesScreen
import com.example.notifilterpro.ui.rules.TimeProfilesScreen
import com.example.notifilterpro.ui.settings.SettingsScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isDark = isSystemInDarkTheme() // You can tie this to your ViewModel if you want manual toggling globally

    val bgColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFF3F4F6)
    val navColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFFFFFFF)

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = bgColor,
        bottomBar = {
            Column {
                // Subtle top border for the nav bar
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(navColor)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomBottomNavItem("Home", Icons.Default.Dashboard, currentRoute == "inbox", isDark) { navigateToTab("inbox") }
                    CustomBottomNavItem("Rules", Icons.Default.FilterAlt, currentRoute?.startsWith("rules") == true, isDark) { navigateToTab("rules_hub") }
                    CustomBottomNavItem("Apps", Icons.Default.Smartphone, currentRoute == "app_rules", isDark) { navigateToTab("app_rules") }
                    CustomBottomNavItem("Settings", Icons.Default.Settings, currentRoute == "settings", isDark) { navigateToTab("settings") }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "inbox", modifier = Modifier.padding(innerPadding)) {
            composable("inbox") { InboxScreen(onNavigateToBlocked = { navController.navigate("blocked_history") }) }
            composable("app_rules") { AppCategorizerScreen() }
            composable("settings") { SettingsScreen() }
            composable("blocked_history") { BlockedHistoryScreen(onBackClick = { navController.popBackStack() }) }
            composable("rules_hub") {
                RulesHubScreen(
                    onNavigateToKeywords = { navController.navigate("rules_keywords") },
                    onNavigateToSender = { navController.navigate("rules_sender") },
                    onNavigateToTime = { navController.navigate("rules_time") }
                )
            }
            composable("rules_keywords") { RulesManagerScreen() }
            composable("rules_sender") { SenderRulesScreen() }
            composable("rules_time") { TimeProfilesScreen() }
        }
    }
}

// Custom UI to get that clean "Dot" indicator from your screenshot
@Composable
fun CustomBottomNavItem(label: String, icon: ImageVector, isSelected: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val selectedColor = Color(0xFF3B82F6) // Bright Blue
    val unselectedColor = if (isDark) Color(0xFF6B7280) else Color(0xFF9CA3AF)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
    ) {
        Icon(icon, contentDescription = label, tint = if (isSelected) selectedColor else unselectedColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = if (isSelected) selectedColor else unselectedColor)
        Spacer(modifier = Modifier.height(4.dp))
        // The Dot
        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) selectedColor else Color.Transparent))
    }
}