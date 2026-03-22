package com.example.notifilterpro.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifilterpro.data.local.KeywordDao
import com.example.notifilterpro.data.local.KeywordEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import javax.inject.Inject

@HiltViewModel
class RulesManagerViewModel @Inject constructor(
    private val keywordDao: KeywordDao
) : ViewModel() {

    // 1. Read from the Database and convert entities to simple Strings for the UI
    val whitelist = keywordDao.getWhitelist()
        .map { list -> list.map { it.keyword } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blacklist = keywordDao.getBlacklist()
        .map { list -> list.map { it.keyword } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Insert into Database
    fun addWhitelist(word: String) {
        val cleanWord = word.trim().lowercase()
        if (cleanWord.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                keywordDao.insertKeyword(KeywordEntity(cleanWord, true))
            }
        }
    }

    // 3. Delete from Database
    fun removeWhitelist(word: String) {
        viewModelScope.launch(Dispatchers.IO) {
            keywordDao.deleteKeyword(KeywordEntity(word, true))
        }
    }

    fun addBlacklist(word: String) {
        val cleanWord = word.trim().lowercase()
        if (cleanWord.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                keywordDao.insertKeyword(KeywordEntity(cleanWord, false))
            }
        }
    }

    fun removeBlacklist(word: String) {
        viewModelScope.launch(Dispatchers.IO) {
            keywordDao.deleteKeyword(KeywordEntity(word, false))
        }
    }
}

// --- THE UI SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesManagerScreen(
    viewModel: RulesManagerViewModel = hiltViewModel()
) {
    val whitelist by viewModel.whitelist.collectAsState()
    val blacklist by viewModel.blacklist.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Rules", fontWeight = FontWeight.Bold) },
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
            // Header Text
            Text(
                text = "Keyword Overrides",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Set specific words to automatically bypass or enforce blocks.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 1. Whitelist Card (Green)
            KeywordCard(
                title = "Priority (Always Allow)",
                description = "Notifications with these words will never be blocked.",
                keywords = whitelist,
                iconColor = Color(0xFF22C55E), // Aura Green
                onAdd = { viewModel.addWhitelist(it) },
                onRemove = { viewModel.removeWhitelist(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Blacklist Card (Red)
            KeywordCard(
                title = "Spam (Always Block)",
                description = "Notifications with these words will be instantly destroyed.",
                keywords = blacklist,
                iconColor = Color(0xFFEF4444), // Aura Red
                onAdd = { viewModel.addBlacklist(it) },
                onRemove = { viewModel.removeBlacklist(it) }
            )
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
            // Title & Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(iconColor, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))

            // Text Input for new keywords
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Add keyword...", fontSize = 14.sp) },
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

            // The list of Keywords displayed as "Chips" that wrap automatically!
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                keywords.forEach { word ->
                    InputChip(
                        selected = false,
                        onClick = { onRemove(word) },
                        label = { Text(word, fontWeight = FontWeight.Medium) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                        },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = iconColor.copy(alpha = 0.1f),
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = InputChipDefaults.inputChipBorder(
                            borderColor = iconColor.copy(alpha = 0.3f),
                            enabled = true,   // <-- Added this!
                            selected = false  // <-- Added this!
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}