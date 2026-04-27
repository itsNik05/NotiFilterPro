package com.nuviolabs.aurafilter.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nuviolabs.aurafilter.ui.inbox.InboxViewModel
import com.nuviolabs.aurafilter.ui.theme.AuraStyle

private enum class LegalSheet { Privacy, Terms }
private enum class ThemeMode { System, Dark, Light }

private data class AccentOption(
    val id: Int,
    val name: String,
    val color: Color,
    val brush: Brush
)

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: InboxViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isActive by viewModel.isActive.collectAsState()
    val currentInterval by viewModel.currentInterval.collectAsState()
    val currentThreshold by viewModel.autoDeleteThreshold.collectAsState()
    val accentId by viewModel.accentColor.collectAsState()
    val savedThemePreference by themeViewModel.isDarkMode.collectAsState()
    val isDark = savedThemePreference ?: isSystemInDarkTheme()
    val palette = AuraStyle.palette(isDark)

    val accentOptions = remember {
        listOf(
            AccentOption(0, "Aura", AuraStyle.Cyan, Brush.linearGradient(listOf(AuraStyle.Cyan, Color(0xFF0096C7)))),
            AccentOption(1, "Sunset", AuraStyle.Orange, Brush.linearGradient(listOf(AuraStyle.Orange, Color(0xFFE85D04)))),
            AccentOption(2, "Mint", AuraStyle.Green, Brush.linearGradient(listOf(AuraStyle.Green, Color(0xFF00916E)))),
            AccentOption(3, "Rose", Color(0xFFFD79A8), Brush.linearGradient(listOf(Color(0xFFFD79A8), Color(0xFFE84393))))
        )
    }
    val selectedAccent = accentOptions.firstOrNull { it.id == accentId } ?: accentOptions.first()
    val themeMode = when (savedThemePreference) {
        true -> ThemeMode.Dark
        false -> ThemeMode.Light
        null -> ThemeMode.System
    }

    var showClearDialog by remember { mutableStateOf(false) }
    var showCustomIntervalDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
    var customIntervalInput by remember { mutableStateOf("") }
    var legalSheet by remember { mutableStateOf<LegalSheet?>(null) }
    var batteryProtected by remember { mutableStateOf(isBatteryOptimizationDisabled(context)) }

    androidx.compose.runtime.DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                batteryProtected = isBatteryOptimizationDisabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(containerColor = palette.bg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            SettingsProfile(isActive = isActive, accent = selectedAccent, palette = palette)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                SettingsGroup(label = "ENGINE", palette = palette) {
                    SettingsToggleRow(
                        icon = Icons.Default.PlayArrow,
                        iconBrush = Brush.linearGradient(listOf(AuraStyle.Cyan, Color(0xFF00A896))),
                        title = "Filtering Active",
                        description = "Intercept & sort notifications",
                        checked = isActive,
                        activeColor = AuraStyle.Cyan,
                        palette = palette,
                        onClick = { viewModel.toggleService(!isActive) }
                    )
                    SettingsDivider(palette)
                    SettingsActionRow(
                        icon = Icons.Default.NotificationsActive,
                        iconBrush = Brush.linearGradient(listOf(AuraStyle.Yellow, Color(0xFFF4A261))),
                        title = "Notification Access",
                        description = "Required for engine to work",
                        onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                    )
                    SettingsDivider(palette)
                    SettingsToggleRow(
                        icon = Icons.Default.Lock,
                        iconBrush = Brush.linearGradient(listOf(Color(0xFFB48FFF), Color(0xFF6C5CE7))),
                        title = "Prevent Sleep",
                        description = "Stop Android killing the engine",
                        checked = batteryProtected,
                        activeColor = Color(0xFF6C5CE7),
                        palette = palette,
                        onClick = { openBatterySettings(context) }
                    )
                }

                SettingsGroup(label = "DIGEST", palette = palette) {
                    SettingsChipRow(
                        icon = Icons.Default.Timer,
                        iconBrush = Brush.linearGradient(listOf(AuraStyle.Cyan, Color(0xFF0096C7))),
                        title = "Reminder Frequency",
                        description = "How often to ping about waiting reviews",
                        chips = listOf("1h", "2h", "4h", "Custom"),
                        selected = when (currentInterval) {
                            1 -> "1h"
                            2 -> "2h"
                            4 -> "4h"
                            else -> "Custom"
                        },
                        activeColor = selectedAccent.color,
                        palette = palette,
                        onChipClick = { label ->
                            when (label) {
                                "1h" -> viewModel.updateInterval(1)
                                "2h" -> viewModel.updateInterval(2)
                                "4h" -> viewModel.updateInterval(4)
                                else -> showCustomIntervalDialog = true
                            }
                        }
                    )
                    SettingsDivider(palette)
                    SettingsChipRow(
                        icon = Icons.Default.Delete,
                        iconBrush = Brush.linearGradient(listOf(AuraStyle.Red, Color(0xFFC0392B))),
                        title = "Auto-Delete Logs",
                        description = "How long to keep intercepted notifications",
                        chips = listOf("24h", "48h", "7 days", "Forever"),
                        selected = when (currentThreshold) {
                            24 -> "24h"
                            48 -> "48h"
                            168 -> "7 days"
                            -1 -> "Forever"
                            else -> "24h"
                        },
                        activeColor = selectedAccent.color,
                        palette = palette,
                        onChipClick = { label ->
                            when (label) {
                                "24h" -> viewModel.updateAutoDeleteThreshold(24)
                                "48h" -> viewModel.updateAutoDeleteThreshold(48)
                                "7 days" -> viewModel.updateAutoDeleteThreshold(168)
                                "Forever" -> viewModel.updateAutoDeleteThreshold(-1)
                            }
                        }
                    )
                }

                SettingsGroup(label = "APPEARANCE", palette = palette) {
                    SettingsActionRow(
                        icon = Icons.Default.DarkMode,
                        iconBrush = Brush.linearGradient(listOf(Color(0xFF2D3561), Color(0xFF1A1A2E))),
                        title = "Theme",
                        description = themeDescription(themeMode),
                        trailing = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = themeLabel(themeMode),
                                    color = palette.muted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = palette.muted)
                            }
                        },
                        onClick = { showThemeDialog = true }
                    )
                    SettingsDivider(palette)
                    SettingsActionRow(
                        icon = Icons.Default.Palette,
                        iconBrush = selectedAccent.brush,
                        title = "Accent Color",
                        description = "Personalize your interface",
                        trailing = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(selectedAccent.color)
                                )
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = palette.muted)
                            }
                        },
                        onClick = { showAccentDialog = true }
                    )
                }

                SettingsGroup(label = "PRIVACY & LEGAL", palette = palette) {
                    SettingsActionRow(
                        icon = Icons.Default.Shield,
                        iconBrush = Brush.linearGradient(listOf(AuraStyle.Green, Color(0xFF00916E))),
                        title = "Privacy Policy",
                        description = "How we handle your data",
                        onClick = { legalSheet = LegalSheet.Privacy }
                    )
                    SettingsDivider(palette)
                    SettingsActionRow(
                        icon = Icons.Default.Article,
                        iconBrush = Brush.linearGradient(listOf(Color(0xFF74B9FF), Color(0xFF0984E3))),
                        title = "Terms of Service",
                        description = "Usage terms & conditions",
                        onClick = { legalSheet = LegalSheet.Terms }
                    )
                    SettingsDivider(palette)
                    SettingsActionRow(
                        icon = Icons.Default.Star,
                        iconBrush = Brush.linearGradient(listOf(Color(0xFFFD79A8), Color(0xFFE84393))),
                        title = "Rate Aura Filter",
                        description = "Enjoying it? Leave a review",
                        onClick = { openStoreListing(context) }
                    )
                    SettingsDivider(palette)
                    SettingsActionRow(
                        icon = Icons.Default.Feedback,
                        iconBrush = Brush.linearGradient(listOf(Color(0xFF636E72), Color(0xFF2D3436))),
                        title = "Send Feedback",
                        description = "Report bugs or suggest features",
                        onClick = { sendFeedback(context) }
                    )
                }

                SettingsGroup(label = "DATA", palette = palette) {
                    SettingsActionRow(
                        icon = Icons.Default.DeleteForever,
                        iconBrush = Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFEE0979))),
                        title = "Clear All History",
                        description = "Wipe blocked & review data",
                        titleColor = AuraStyle.Red,
                        chevronColor = AuraStyle.Red,
                        onClick = { showClearDialog = true }
                    )
                }

                SettingsFooter(palette)
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = palette.surface,
            title = { Text("Clear All History?", fontWeight = FontWeight.Bold, color = palette.text) },
            text = {
                Text(
                    "This will permanently delete all blocked and review notification logs. Your rules and settings will stay untouched.",
                    color = palette.muted2
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDialog = false
                        Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AuraStyle.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = palette.text)
                }
            }
        )
    }

    if (showCustomIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showCustomIntervalDialog = false },
            containerColor = palette.surface,
            title = { Text("Custom Frequency", fontWeight = FontWeight.Bold, color = palette.text) },
            text = {
                OutlinedTextField(
                    value = customIntervalInput,
                    onValueChange = { value -> if (value.all { it.isDigit() }) customIntervalInput = value },
                    label = { Text("Hours", color = palette.muted2) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = palette.text,
                        unfocusedTextColor = palette.text,
                        focusedBorderColor = selectedAccent.color,
                        unfocusedBorderColor = palette.border
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hours = customIntervalInput.toIntOrNull()
                        if (hours != null && hours > 0) {
                            viewModel.updateInterval(hours)
                            customIntervalInput = ""
                            showCustomIntervalDialog = false
                        } else {
                            Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = selectedAccent.color)
                ) {
                    Text("Save", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomIntervalDialog = false }) {
                    Text("Cancel", color = palette.text)
                }
            }
        )
    }

    if (showThemeDialog) {
        SelectionDialog(
            title = "Theme",
            options = listOf("System", "Dark", "Light"),
            selected = themeLabel(themeMode),
            accentColor = selectedAccent.color,
            palette = palette,
            onSelect = { option ->
                when (option) {
                    "System" -> viewModel.setThemeMode(null)
                    "Dark" -> viewModel.setThemeMode(true)
                    "Light" -> viewModel.setThemeMode(false)
                }
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showAccentDialog) {
        AccentDialog(
            options = accentOptions,
            selectedId = selectedAccent.id,
            palette = palette,
            onSelect = { accent ->
                viewModel.setAccentColor(accent.id)
                showAccentDialog = false
            },
            onDismiss = { showAccentDialog = false }
        )
    }

    legalSheet?.let { sheet ->
        LegalDialog(sheet = sheet, accentColor = selectedAccent.color, palette = palette, onDismiss = { legalSheet = null })
    }
}

@Composable
private fun SettingsProfile(isActive: Boolean, accent: AccentOption, palette: AuraStyle.Palette) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accent.brush),
                contentAlignment = Alignment.Center
            ) {
                Text("AF", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Aura Filter", color = palette.text, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("v2.1.0 · Notification Guard", color = palette.muted2, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            StatusPill(isActive = isActive)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(palette.border)
        )
    }
}

@Composable
private fun StatusPill(isActive: Boolean) {
    val color = if (isActive) AuraStyle.Green else AuraStyle.Red
    Surface(
        color = if (isActive) AuraStyle.GreenDim else AuraStyle.RedDim,
        border = BorderStroke(1.dp, color),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(if (isActive) "ON" else "OFF", color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun SettingsGroup(label: String, palette: AuraStyle.Palette, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = label,
            color = palette.muted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, palette.border),
            colors = CardDefaults.cardColors(containerColor = palette.surface)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconBrush: Brush,
    title: String,
    description: String,
    checked: Boolean,
    activeColor: Color,
    palette: AuraStyle.Palette,
    onClick: () -> Unit
) {
    SettingsBaseRow(
        icon = icon,
        iconBrush = iconBrush,
        title = title,
        description = description,
        trailing = { AuraToggle(checked = checked, activeColor = activeColor, palette = palette) },
        onClick = onClick
    )
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    iconBrush: Brush,
    title: String,
    description: String,
    titleColor: Color = Color.Unspecified,
    chevronColor: Color = Color.Unspecified,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val resolvedTitleColor = if (titleColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else titleColor
    val resolvedChevronColor = if (chevronColor == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else chevronColor
    SettingsBaseRow(
        icon = icon,
        iconBrush = iconBrush,
        title = title,
        description = description,
        titleColor = resolvedTitleColor,
        trailing = {
            if (trailing != null) {
                trailing()
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = resolvedChevronColor)
            }
        },
        onClick = onClick
    )
}

@Composable
private fun SettingsChipRow(
    icon: ImageVector,
    iconBrush: Brush,
    title: String,
    description: String,
    chips: List<String>,
    selected: String,
    activeColor: Color,
    palette: AuraStyle.Palette,
    onChipClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(13.dp, 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            SettingsIcon(icon = icon, brush = iconBrush)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            chips.forEach { chip ->
                SettingsChip(
                    label = chip,
                    selected = chip == selected,
                    modifier = Modifier.weight(1f),
                    activeColor = activeColor,
                    palette = palette,
                    onClick = { onChipClick(chip) }
                )
            }
        }
    }
}

@Composable
private fun SettingsBaseRow(
    icon: ImageVector,
    iconBrush: Brush,
    title: String,
    description: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SettingsIcon(icon = icon, brush = iconBrush)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = titleColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        }
        trailing()
    }
}

@Composable
private fun SettingsIcon(icon: ImageVector, brush: Brush) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(brush),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AuraToggle(checked: Boolean, activeColor: Color, palette: AuraStyle.Palette) {
    Box(
        modifier = Modifier
            .width(42.dp)
            .height(22.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (checked) activeColor else palette.surface3)
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun SettingsDivider(palette: AuraStyle.Palette) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 16.dp)
            .background(palette.border)
    )
}

@Composable
private fun SettingsChip(
    label: String,
    selected: Boolean,
    modifier: Modifier,
    activeColor: Color,
    palette: AuraStyle.Palette,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) activeColor.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) activeColor else palette.border,
                shape = RoundedCornerShape(9.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) activeColor else palette.muted2,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun SettingsFooter(palette: AuraStyle.Palette) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("✦ Aura Filter", color = palette.muted, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        Text("Version 2.1.0 · Build 210", color = palette.muted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text("Made with care · Not affiliated with any ad network", color = palette.muted.copy(alpha = 0.5f), fontSize = 9.sp)
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selected: String,
    accentColor: Color,
    palette: AuraStyle.Palette,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        title = { Text(title, color = palette.text, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(option) },
                        color = if (option == selected) accentColor.copy(alpha = 0.12f) else palette.surface3,
                        border = BorderStroke(1.dp, if (option == selected) accentColor else palette.border)
                    ) {
                        Text(
                            text = option,
                            color = if (option == selected) accentColor else palette.text,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = accentColor) }
        }
    )
}

@Composable
private fun AccentDialog(
    options: List<AccentOption>,
    selectedId: Int,
    palette: AuraStyle.Palette,
    onSelect: (AccentOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        title = { Text("Accent Color", color = palette.text, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelect(option) },
                        color = palette.surface3,
                        border = BorderStroke(1.dp, if (option.id == selectedId) option.color else palette.border)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(option.brush)
                            )
                            Text(option.name, color = palette.text, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            if (option.id == selectedId) {
                                Text("Active", color = option.color, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = AuraStyle.Cyan) }
        }
    )
}

@Composable
private fun LegalDialog(sheet: LegalSheet, accentColor: Color, palette: AuraStyle.Palette, onDismiss: () -> Unit) {
    val title = if (sheet == LegalSheet.Privacy) "Privacy Policy" else "Terms & Conditions"
    val body = if (sheet == LegalSheet.Privacy) privacyPolicyText() else termsText()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        title = { Text(title, color = palette.text, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(
                modifier = Modifier
                    .height(360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(body, color = palette.muted2, fontSize = 12.sp, lineHeight = 18.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = accentColor) }
        }
    )
}

private fun themeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.System -> "System"
    ThemeMode.Dark -> "Dark"
    ThemeMode.Light -> "Light"
}

private fun themeDescription(mode: ThemeMode): String = when (mode) {
    ThemeMode.System -> "Follow your phone's current appearance"
    ThemeMode.Dark -> "Dark mode · System default"
    ThemeMode.Light -> "Light mode · Manual override"
}

private fun privacyPolicyText(): String = """
Aura Filter Privacy Policy

Aura Filter processes notification information locally on your device so it can block, allow, or queue notifications for review.

What the app stores:
- Notification app name, title, text, timestamp, and filter result when needed for Review or Blocked History.
- Your rules, reminder frequency, theme preference, and auto-delete preference.
- Your selected accent preference for the app interface.

What the app does not do:
- We do not sell your data.
- We do not upload notification content to our servers.
- We do not share notification content with advertisers.

Permissions:
Notification Access is required so Aura Filter can inspect incoming notifications and apply your rules. Battery optimization access is optional, but helps the filtering engine stay reliable.

Data control:
You can clear blocked and review history from Settings at any time. Auto-delete can also remove old logs automatically.
""".trimIndent()

private fun termsText(): String = """
Aura Filter Terms & Conditions

By using Aura Filter, you agree to use it as a personal notification management tool.

Filtering behavior:
Aura Filter may cancel or hide notifications based on your rules and app-zone choices. Please review your rules carefully so important alerts are not blocked accidentally.

No guarantee:
Android device settings, battery restrictions, OEM behavior, and notification access permissions can affect reliability. Aura Filter is provided as-is without a guarantee that every notification will be filtered.

Your responsibility:
You are responsible for configuring rules, reviewing queued notifications, and keeping notification access enabled.

Updates:
These terms may be updated as the app changes. Continued use of the app means you accept the updated terms.
""".trimIndent()

private fun openStoreListing(context: Context) {
    val packageName = context.packageName
    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
    try {
        context.startActivity(marketIntent)
    } catch (_: Exception) {
        try {
            context.startActivity(webIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "No app store or browser found", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun sendFeedback(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:support@aurafilter.app")
        putExtra(Intent.EXTRA_SUBJECT, "Aura Filter feedback")
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
    }
}

private fun openBatterySettings(context: Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        )
    } catch (_: Exception) {
        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    }
}

private fun isBatteryOptimizationDisabled(context: Context): Boolean {
    val powerManager = context.getSystemService(PowerManager::class.java)
    return powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
}
