package com.example.notifilterpro.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifilterpro.data.local.SenderRuleDao
import com.example.notifilterpro.data.local.SenderRuleEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- THE VIEWMODEL ---
@HiltViewModel
class SenderRulesViewModel @Inject constructor(
    private val senderRuleDao: SenderRuleDao
) : ViewModel() {

    val whitelist = senderRuleDao.getWhitelistSenders()
        .map { list -> list.map { it.senderName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blacklist = senderRuleDao.getBlacklistSenders()
        .map { list -> list.map { it.senderName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWhitelist(name: String) {
        val cleanName = name.trim().lowercase() // Lowercase for exact engine matching
        if (cleanName.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                senderRuleDao.insertSender(SenderRuleEntity(cleanName, true))
            }
        }
    }

    fun removeWhitelist(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            senderRuleDao.deleteSender(SenderRuleEntity(name, true))
        }
    }

    fun addBlacklist(name: String) {
        val cleanName = name.trim().lowercase()
        if (cleanName.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                senderRuleDao.insertSender(SenderRuleEntity(cleanName, false))
            }
        }
    }

    fun removeBlacklist(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            senderRuleDao.deleteSender(SenderRuleEntity(name, false))
        }
    }
}

// --- THE UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenderRulesScreen(
    viewModel: SenderRulesViewModel = hiltViewModel()
) {
    val whitelist by viewModel.whitelist.collectAsState()
    val blacklist by viewModel.blacklist.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sender Rules", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Contact Overrides",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Add names exactly as they appear in your notifications.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // VIP Card (Green)
            SenderCard(
                title = "VIPs (Always Allow)",
                description = "Messages from these people will never be blocked or held.",
                names = whitelist,
                iconColor = Color(0xFF22C55E), // Aura Green
                onAdd = { viewModel.addWhitelist(it) },
                onRemove = { viewModel.removeWhitelist(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Blocked Card (Red)
            SenderCard(
                title = "Blocked (Always Destroy)",
                description = "Messages from these people will be instantly blocked.",
                names = blacklist,
                iconColor = Color(0xFFEF4444), // Aura Red
                onAdd = { viewModel.addBlacklist(it) },
                onRemove = { viewModel.removeBlacklist(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- REUSABLE CARD COMPONENT ---
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SenderCard(
    title: String,
    description: String,
    names: List<String>,
    iconColor: Color,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(iconColor, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Add sender name...", fontSize = 14.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (text.isNotBlank()) onAdd(text)
                    text = ""
                }),
                trailingIcon = {
                    IconButton(onClick = {
                        if (text.isNotBlank()) onAdd(text)
                        text = ""
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = iconColor)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = iconColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                names.forEach { name ->
                    InputChip(
                        selected = false,
                        onClick = { onRemove(name) },
                        label = { Text(name, fontWeight = FontWeight.Medium) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                        },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = iconColor.copy(alpha = 0.1f),
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = InputChipDefaults.inputChipBorder(
                            borderColor = iconColor.copy(alpha = 0.3f),
                            enabled = true,
                            selected = false
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}