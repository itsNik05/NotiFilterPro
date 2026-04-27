package com.nuviolabs.aurafilter.ui

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nuviolabs.aurafilter.ui.categorizer.AppCategorizerScreen
import com.nuviolabs.aurafilter.ui.inbox.BlockedHistoryScreen
import com.nuviolabs.aurafilter.ui.inbox.InboxScreen
import com.nuviolabs.aurafilter.ui.inbox.InboxViewModel
import com.nuviolabs.aurafilter.ui.inbox.ReviewHistoryScreen
import com.nuviolabs.aurafilter.ui.rules.RulesHubScreen
import com.nuviolabs.aurafilter.ui.rules.RulesManagerScreen
import com.nuviolabs.aurafilter.ui.rules.SenderRulesScreen
import com.nuviolabs.aurafilter.ui.rules.TimeProfilesScreen
import com.nuviolabs.aurafilter.ui.settings.SettingsScreen
import com.nuviolabs.aurafilter.ui.theme.AuraStyle

@Composable
fun MainScreen(themeViewModel: InboxViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val isDark = isDarkMode ?: isSystemInDarkTheme()
    val palette = AuraStyle.palette(isDark)

    val bgColor = palette.bg
    val navColor = palette.bg.copy(alpha = 0.97f)

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
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.border))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(navColor)
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomBottomNavItem("Home", Icons.Default.Dashboard, currentRoute == "inbox" || currentRoute == "blocked_history" || currentRoute == "review_history", isDark) { navigateToTab("inbox") }

                    // FIX: Changed .contains("rules") to .startsWith("rules_").
                    // This stops the "Apps" tab (app_rules) from lighting this up!
                    CustomBottomNavItem("Rules", Icons.Default.FilterAlt, currentRoute?.startsWith("rules_") == true, isDark) { navigateToTab("rules_hub") }

                    CustomBottomNavItem("Apps", Icons.Default.Smartphone, currentRoute == "app_rules", isDark) { navigateToTab("app_rules") }
                    CustomBottomNavItem("Settings", Icons.Default.Settings, currentRoute == "settings", isDark) { navigateToTab("settings") }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "inbox", modifier = Modifier.padding(innerPadding)) {
            composable("inbox") {
                InboxScreen(
                    onNavigateToBlocked = { navController.navigate("blocked_history") },
                    onNavigateToReview = { navController.navigate("review_history") }
                )
            }
            composable("app_rules") { AppCategorizerScreen() }
            composable("settings") { SettingsScreen() }
            composable("blocked_history") { BlockedHistoryScreen(onBackClick = { navController.popBackStack() }) }
            composable("review_history") { ReviewHistoryScreen(onBackClick = { navController.popBackStack() }) }

            // FIX: Removed onNavigateToBlocked parameter
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

@Composable
fun CustomBottomNavItem(label: String, icon: ImageVector, isSelected: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val selectedColor = AuraStyle.Cyan
    val unselectedColor = AuraStyle.palette(isDark).muted

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (isSelected) selectedColor else unselectedColor, modifier = Modifier.size(21.dp))
        Spacer(modifier = Modifier.height(3.dp))
        Text(label.uppercase(), fontSize = 8.sp, color = if (isSelected) selectedColor else unselectedColor, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(5.dp))
        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) selectedColor else Color.Transparent))
    }
}
