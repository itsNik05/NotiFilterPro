package com.example.notifilterpro.ui.rules

import android.app.TimePickerDialog
import androidx.compose.foundation.background
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
import com.example.notifilterpro.data.local.TimeProfileDao
import com.example.notifilterpro.data.local.TimeProfileEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

// --- THE VIEWMODEL ---
@HiltViewModel
class TimeProfilesViewModel @Inject constructor(
    private val timeProfileDao: TimeProfileDao
) : ViewModel() {

    val profiles = timeProfileDao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addProfile(name: String, startH: Int, startM: Int, endH: Int, endM: Int) {
        if (name.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                timeProfileDao.insertProfile(
                    TimeProfileEntity(name, startH, startM, endH, endM, isEnabled = true)
                )
            }
        }
    }

    fun toggleProfile(profile: TimeProfileEntity, isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            timeProfileDao.insertProfile(profile.copy(isEnabled = isEnabled))
        }
    }

    fun deleteProfile(profile: TimeProfileEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            timeProfileDao.deleteProfile(profile)
        }
    }
}

// --- THE UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeProfilesScreen(
    viewModel: TimeProfilesViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Time Profiles", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFF97316) // Aura Orange
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Profile", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Focus Modes",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "During these hours, background filtering is strictly enforced. Outside these hours, all notifications are let through.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (profiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No profiles yet. Tap + to create one!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(profiles) { profile ->
                        ProfileCard(
                            profile = profile,
                            onToggle = { isEnabled -> viewModel.toggleProfile(profile, isEnabled) },
                            onDelete = { viewModel.deleteProfile(profile) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddProfileDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, sh, sm, eh, em ->
                viewModel.addProfile(name, sh, sm, eh, em)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProfileCard(
    profile: TimeProfileEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    // Format the time nicely (e.g., "09:00 AM")
    val formatTime = { h: Int, m: Int ->
        val amPm = if (h >= 12) "PM" else "AM"
        val displayH = if (h % 12 == 0) 12 else h % 12
        String.format("%02d:%02d %s", displayH, m, amPm)
    }

    val timeString = "${formatTime(profile.startHour, profile.startMinute)} - ${formatTime(profile.endHour, profile.endMinute)}"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFFF97316).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFF97316))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(profile.profileName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(timeString, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = profile.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFF97316), checkedTrackColor = Color(0xFFF97316).copy(alpha = 0.5f))
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddProfileDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int, Int, Int, Int) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var startH by remember { mutableStateOf(9) }
    var startM by remember { mutableStateOf(0) }
    var endH by remember { mutableStateOf(17) }
    var endM by remember { mutableStateOf(0) }

    val formatTime = { h: Int, m: Int ->
        val amPm = if (h >= 12) "PM" else "AM"
        val displayH = if (h % 12 == 0) 12 else h % 12
        String.format("%02d:%02d %s", displayH, m, amPm)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Focus Mode", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name (e.g., Deep Work)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Start Time Button
                Button(
                    onClick = {
                        TimePickerDialog(context, { _, h, m -> startH = h; startM = m }, startH, startM, false).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Start Time: ${formatTime(startH, startM)}")
                }

                // End Time Button
                Button(
                    onClick = {
                        TimePickerDialog(context, { _, h, m -> endH = h; endM = m }, endH, endM, false).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("End Time: ${formatTime(endH, endM)}")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, startH, startM, endH, endM) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}