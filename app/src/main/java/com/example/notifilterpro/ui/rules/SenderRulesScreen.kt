package com.example.notifilterpro.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.example.notifilterpro.ui.inbox.InboxViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SenderRulesViewModel @Inject constructor(
    private val senderRuleDao: SenderRuleDao
) : ViewModel() {
    val whitelist = senderRuleDao.getWhitelistSenders().map { list -> list.map { it.senderName } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val blacklist = senderRuleDao.getBlacklistSenders().map { list -> list.map { it.senderName } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWhitelist(name: String) {
        val cleanName = name.trim().lowercase()
        if (cleanName.isNotBlank()) viewModelScope.launch(Dispatchers.IO) { senderRuleDao.insertSender(SenderRuleEntity(cleanName, true)) }
    }
    fun removeWhitelist(name: String) { viewModelScope.launch(Dispatchers.IO) { senderRuleDao.deleteSender(SenderRuleEntity(name, true)) } }
    fun addBlacklist(name: String) {
        val cleanName = name.trim().lowercase()
        if (cleanName.isNotBlank()) viewModelScope.launch(Dispatchers.IO) { senderRuleDao.insertSender(SenderRuleEntity(cleanName, false)) }
    }
    fun removeBlacklist(name: String) { viewModelScope.launch(Dispatchers.IO) { senderRuleDao.deleteSender(SenderRuleEntity(name, false)) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenderRulesScreen(
    viewModel: SenderRulesViewModel = hiltViewModel(),
    themeViewModel: InboxViewModel = hiltViewModel()
) {
    val whitelist by viewModel.whitelist.collectAsState()
    val blacklist by viewModel.blacklist.collectAsState()
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
                title = { Text("Sender Rules", fontWeight = FontWeight.Bold, color = textColor) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
        ) {
            Text("Contact Overrides", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = textColor, modifier = Modifier.padding(bottom = 4.dp))
            Text("Add names exactly as they appear in your notifications.", fontSize = 14.sp, color = subTextColor, modifier = Modifier.padding(bottom = 16.dp))

            SenderCard("VIPs (Always Allow)", "Messages from these people will never be blocked or held.", whitelist, Color(0xFF22C55E), cardColor, textColor, subTextColor, isDark, { viewModel.addWhitelist(it) }, { viewModel.removeWhitelist(it) })
            Spacer(modifier = Modifier.height(16.dp))
            SenderCard("Blocked (Always Destroy)", "Messages from these people will be instantly blocked.", blacklist, Color(0xFFEF4444), cardColor, textColor, subTextColor, isDark, { viewModel.addBlacklist(it) }, { viewModel.removeBlacklist(it) })
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SenderCard(title: String, description: String, names: List<String>, iconColor: Color, cardColor: Color, textColor: Color, subTextColor: Color, isDark: Boolean, onAdd: (String) -> Unit, onRemove: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val borderColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(iconColor, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 13.sp, color = subTextColor)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = text, onValueChange = { text = it }, placeholder = { Text("Add sender name...", fontSize = 14.sp, color = subTextColor) }, singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { if (text.isNotBlank()) onAdd(text); text = "" }),
                trailingIcon = { IconButton(onClick = { if (text.isNotBlank()) onAdd(text); text = "" }) { Icon(Icons.Default.Add, contentDescription = "Add", tint = iconColor) } },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = iconColor, unfocusedBorderColor = borderColor, focusedTextColor = textColor, unfocusedTextColor = textColor, cursorColor = iconColor)
            )
            Spacer(modifier = Modifier.height(16.dp))
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                names.forEach { name ->
                    InputChip(
                        selected = false, onClick = { onRemove(name) }, label = { Text(name, fontWeight = FontWeight.Medium) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) },
                        colors = InputChipDefaults.inputChipColors(containerColor = iconColor.copy(alpha = 0.1f), labelColor = textColor, trailingIconColor = textColor),
                        border = InputChipDefaults.inputChipBorder(borderColor = iconColor.copy(alpha = 0.3f), enabled = true, selected = false), shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}