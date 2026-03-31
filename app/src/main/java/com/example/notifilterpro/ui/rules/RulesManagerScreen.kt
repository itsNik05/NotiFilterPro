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
import com.example.notifilterpro.data.local.KeywordDao
import com.example.notifilterpro.data.local.KeywordEntity
import com.example.notifilterpro.ui.inbox.InboxViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RulesManagerViewModel @Inject constructor(private val keywordDao: KeywordDao) : ViewModel() {
    val whitelist = keywordDao.getWhitelist()
        .map { list -> list.map { it.keyword } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blacklist = keywordDao.getBlacklist()
        .map { list -> list.map { it.keyword } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWhitelist(word: String) {
        val cleanWord = word.trim().lowercase()
        if (cleanWord.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                keywordDao.insertKeyword(KeywordEntity(keyword = cleanWord, isWhitelist = true))
            }
        }
    }

    // FIX: Using custom delete query to avoid Room ID mismatches
    fun removeWhitelist(word: String) {
        viewModelScope.launch(Dispatchers.IO) {
            keywordDao.deleteKeywordByValue(word, true)
        }
    }

    fun addBlacklist(word: String) {
        val cleanWord = word.trim().lowercase()
        if (cleanWord.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                keywordDao.insertKeyword(KeywordEntity(keyword = cleanWord, isWhitelist = false))
            }
        }
    }

    // FIX: Using custom delete query to avoid Room ID mismatches
    fun removeBlacklist(word: String) {
        viewModelScope.launch(Dispatchers.IO) {
            keywordDao.deleteKeywordByValue(word, false)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesManagerScreen(
    viewModel: RulesManagerViewModel = hiltViewModel(),
    themeViewModel: InboxViewModel = hiltViewModel()
) {
    val whitelist by viewModel.whitelist.collectAsState()
    val blacklist by viewModel.blacklist.collectAsState()
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    val isDark = isDarkMode ?: isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0B0F19) else Color(0xFFF3F4F6)
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val cardColor = if (isDark) Color(0xFF151B29) else Color.White

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Smart Rules", fontWeight = FontWeight.Bold, color = textColor) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
            Spacer(modifier = Modifier.height(8.dp))
            Text("Keyword Overrides", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = textColor)
            Text("Set specific words to automatically bypass or enforce blocks.", fontSize = 14.sp, color = subTextColor, modifier = Modifier.padding(bottom = 24.dp))

            KeywordCard(
                title = "Priority (Always Allow)",
                description = "Notifications with these words will never be blocked.",
                keywords = whitelist,
                iconColor = Color(0xFF22C55E),
                cardColor = cardColor,
                textColor = textColor,
                subTextColor = subTextColor,
                isDark = isDark,
                onAdd = { viewModel.addWhitelist(it) },
                onRemove = { viewModel.removeWhitelist(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            KeywordCard(
                title = "Spam (Always Block)",
                description = "Notifications with these words will be instantly filtered.",
                keywords = blacklist,
                iconColor = Color(0xFFEF4444),
                cardColor = cardColor,
                textColor = textColor,
                subTextColor = subTextColor,
                isDark = isDark,
                onAdd = { viewModel.addBlacklist(it) },
                onRemove = { viewModel.removeBlacklist(it) }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KeywordCard(
    title: String,
    description: String,
    keywords: List<String>,
    iconColor: Color,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    isDark: Boolean,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val borderColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
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
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Add keyword...", fontSize = 14.sp, color = subTextColor) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (text.isNotBlank()) { onAdd(text); text = "" }
                }),
                trailingIcon = {
                    IconButton(onClick = { if (text.isNotBlank()) { onAdd(text); text = "" } }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = iconColor)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = iconColor,
                    unfocusedBorderColor = borderColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = iconColor
                )
            )

            // FIX: Prevents weird empty spacing when there are no keywords
            if (keywords.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    keywords.forEach { word ->
                        InputChip(
                            selected = false,
                            onClick = { onRemove(word) },
                            label = { Text(word, fontWeight = FontWeight.Medium, fontSize = 12.sp) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp)) },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = iconColor.copy(alpha = 0.1f),
                                labelColor = iconColor,
                                trailingIconColor = iconColor
                            ),
                            border = InputChipDefaults.inputChipBorder(
                                borderColor = iconColor.copy(alpha = 0.2f),
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
}