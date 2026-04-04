package com.nuviolabs.aurafilter.ui.rules

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuviolabs.aurafilter.data.local.TimeProfileDao
import com.nuviolabs.aurafilter.data.local.TimeProfileEntity
import com.nuviolabs.aurafilter.ui.inbox.InboxViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TimeProfilesViewModel @Inject constructor(private val timeProfileDao: TimeProfileDao) : ViewModel() {
    val profiles = timeProfileDao.getAllProfiles().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun addProfile(name: String, startH: Int, startM: Int, endH: Int, endM: Int) {
        if (name.isNotBlank()) viewModelScope.launch(Dispatchers.IO) { timeProfileDao.insertProfile(TimeProfileEntity(name, startH, startM, endH, endM, isEnabled = true)) }
    }
    fun toggleProfile(profile: TimeProfileEntity, isEnabled: Boolean) { viewModelScope.launch(Dispatchers.IO) { timeProfileDao.insertProfile(profile.copy(isEnabled = isEnabled)) } }
    fun deleteProfile(profile: TimeProfileEntity) { viewModelScope.launch(Dispatchers.IO) { timeProfileDao.deleteProfile(profile) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeProfilesScreen(
    viewModel: TimeProfilesViewModel = hiltViewModel(),
    themeViewModel: InboxViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val savedThemePreference by themeViewModel.isDarkMode.collectAsState()
    val isDark = savedThemePreference ?: isSystemInDarkTheme()

    val bgColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFF3F4F6)
    val cardColor = if (isDark) Color(0xFF151B29) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Time Profiles", fontWeight = FontWeight.Bold, color = textColor) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Color(0xFFF97316)) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
            Text("Focus Modes", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = textColor, modifier = Modifier.padding(bottom = 4.dp))
            Text("During these hours, background filtering is enforced.", fontSize = 14.sp, color = subTextColor, modifier = Modifier.padding(bottom = 16.dp))

            if (profiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No profiles yet.", color = subTextColor) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(profiles) { profile ->
                        ProfileCard(profile, cardColor, textColor, subTextColor, isDark, { viewModel.toggleProfile(profile, it) }, { viewModel.deleteProfile(profile) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddProfileDialog(isDark, { showAddDialog = false }, { n, sh, sm, eh, em -> viewModel.addProfile(n, sh, sm, eh, em); showAddDialog = false })
    }
}

@Composable
fun ProfileCard(profile: TimeProfileEntity, cardColor: Color, textColor: Color, subTextColor: Color, isDark: Boolean, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    val formatTime = { h: Int, m: Int -> val displayH = if (h % 12 == 0) 12 else h % 12; String.format("%02d:%02d %s", displayH, m, if (h >= 12) "PM" else "AM") }
    val borderColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(40.dp).background(Color(0xFFF97316).copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFF97316))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(profile.profileName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                    Text("${formatTime(profile.startHour, profile.startMinute)} - ${formatTime(profile.endHour, profile.endMinute)}", fontSize = 13.sp, color = subTextColor)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = profile.isEnabled, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFF97316), checkedTrackColor = Color(0xFFF97316).copy(alpha = 0.5f)))
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444)) }
            }
        }
    }
}

@Composable
fun AddProfileDialog(isDark: Boolean, onDismiss: () -> Unit, onSave: (String, Int, Int, Int, Int) -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var startH by remember { mutableStateOf(9) }; var startM by remember { mutableStateOf(0) }
    var endH by remember { mutableStateOf(17) }; var endM by remember { mutableStateOf(0) }
    val formatTime = { h: Int, m: Int -> val displayH = if (h % 12 == 0) 12 else h % 12; String.format("%02d:%02d %s", displayH, m, if (h >= 12) "PM" else "AM") }

    val dialogBg = if (isDark) Color(0xFF151B29) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val buttonBg = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    AlertDialog(
        onDismissRequest = onDismiss, containerColor = dialogBg,
        title = { Text("New Focus Mode", fontWeight = FontWeight.Bold, color = textColor) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Profile Name", color = textColor) }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = Color(0xFFF97316)))
                Button(onClick = { TimePickerDialog(context, { _, h, m -> startH = h; startM = m }, startH, startM, false).show() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = buttonBg, contentColor = textColor)) { Text("Start Time: ${formatTime(startH, startM)}") }
                Button(onClick = { TimePickerDialog(context, { _, h, m -> endH = h; endM = m }, endH, endM, false).show() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = buttonBg, contentColor = textColor)) { Text("End Time: ${formatTime(endH, endM)}") }
            }
        },
        confirmButton = { Button(onClick = { onSave(name, startH, startM, endH, endM) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))) { Text("Save", color = Color.White) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = textColor) } }
    )
}
