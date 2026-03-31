package com.example.notifilterpro.ui.categorizer

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.notifilterpro.ui.inbox.InboxViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCategorizerScreen(
    viewModel: AppCategorizerViewModel = hiltViewModel(),
    themeViewModel: InboxViewModel = hiltViewModel()
) {
    val appList by viewModel.appList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val savedThemePreference by themeViewModel.isDarkMode.collectAsState()

    val isDark = savedThemePreference ?: isSystemInDarkTheme()

    // Aura Premium Theme Colors
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text("Rule Management", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = textColor, modifier = Modifier.padding(bottom = 4.dp))
            Text("Set which apps go to the Block, Review, or Allow zones.", fontSize = 14.sp, color = subTextColor, modifier = Modifier.padding(bottom = 16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                placeholder = { Text("Search apps...", fontSize = 14.sp, color = subTextColor) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp), tint = subTextColor) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, "Clear", modifier = Modifier.size(20.dp), tint = subTextColor)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = borderColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = Color(0xFF3B82F6)
                )
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),

                // FIX: Replaced fillMaxSize() with weight(1f).fillMaxWidth()
                // This forces the list to respect the boundaries of the screen so scrolling works!
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(appList, key = { _, app -> app.packageName }) { index, app ->
                        AuraAppRuleItem(
                            app = app,
                            isLastItem = index == appList.size - 1,
                            isDark = isDark,
                            textColor = textColor,
                            subTextColor = subTextColor,
                            borderColor = borderColor,
                            onRuleChanged = { newCat, _ -> viewModel.updateRule(app.packageName, app.appName, newCat) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuraAppRuleItem(
    app: AppUiModel,
    isLastItem: Boolean,
    isDark: Boolean,
    textColor: Color,
    subTextColor: Color,
    borderColor: Color,
    onRuleChanged: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val pm: PackageManager = context.packageManager

    // THE CRASH FIX: State holding the icon, loaded asynchronously!
    var iconBitmap by remember(app.packageName) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(app.packageName) {
        withContext(Dispatchers.IO) {
            try {
                // Fetch the drawable on a background thread so Android 15 doesn't crash
                val drawable = pm.getApplicationIcon(app.packageName)
                val bitmap = drawable.toBitmap()
                iconBitmap = bitmap.asImageBitmap()
            } catch (e: Exception) {
                iconBitmap = null
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Icon
            if (iconBitmap != null) {
                Image(bitmap = iconBitmap!!, contentDescription = null, modifier = Modifier.size(40.dp))
            } else {
                Box(modifier = Modifier.size(40.dp).background(Color.Gray.copy(alpha = 0.2f), CircleShape))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // App Name & Package Name
            Column(modifier = Modifier.weight(1f)) {
                Text(text = app.appName, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = textColor)
                Text(text = app.packageName, fontSize = 11.sp, color = subTextColor)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryButton("Block", app.category == "RED", Color(0xFFEF4444), isDark, { onRuleChanged("RED", false) }, Modifier.weight(1f))
            CategoryButton("Review", app.category == "ORANGE", Color(0xFFF97316), isDark, { onRuleChanged("ORANGE", false) }, Modifier.weight(1f))
            CategoryButton("Allow", app.category == "GREEN" || app.isWhitelisted, Color(0xFF22C55E), isDark, { onRuleChanged("GREEN", true) }, Modifier.weight(1f))
        }

        if (!isLastItem) {
            HorizontalDivider(color = borderColor, thickness = 1.dp)
        }
    }
}

@Composable
fun CategoryButton(text: String, isSelected: Boolean, selectedColor: Color, isDark: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val unselectedBg = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
    val unselectedText = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    val bgColor = if (isSelected) selectedColor.copy(alpha = 0.15f) else unselectedBg
    val contentColor = if (isSelected) selectedColor else unselectedText

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = contentColor)
    }
}