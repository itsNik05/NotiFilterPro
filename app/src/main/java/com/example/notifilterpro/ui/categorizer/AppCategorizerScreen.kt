package com.example.notifilterpro.ui.categorizer

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCategorizerScreen(
    viewModel: AppCategorizerViewModel = hiltViewModel()
) {
    val appList by viewModel.appList.collectAsState()

    // 1. Read the search query from the ViewModel
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Filters", fontWeight = FontWeight.Bold) },
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
        ) {



            // Header text matching your design
            Text(
                text = "Rule Management",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Set which apps go to the Block, Review, or Allow zones.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- NEW: THE SEARCH BAR ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search apps...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(20.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // The beautifully rounded main container card (Untouched!)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                LazyColumn {
                    itemsIndexed(appList, key = { _, app -> app.packageName }) { index, app ->
                        AuraAppRuleItem(
                            app = app,
                            isLastItem = index == appList.size - 1,
                            onRuleChanged = { newCat, isWhite ->
                                viewModel.updateRule(app.packageName, app.appName, newCat)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// EVERYTHING BELOW THIS LINE IS YOUR EXACT UNTOUCHED UI CODE
// ============================================================================

@Composable
fun AuraAppRuleItem(
    app: AppUiModel,
    isLastItem: Boolean,
    onRuleChanged: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val pm: PackageManager = context.packageManager

    // Safely load the app icon
    val iconBitmap = remember(app.packageName) {
        try { pm.getApplicationIcon(app.packageName).toBitmap().asImageBitmap() } catch (e: Exception) { null }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            if (iconBitmap != null) {
                Image(bitmap = iconBitmap, contentDescription = null, modifier = Modifier.size(40.dp))
            } else {
                Box(modifier = Modifier.size(40.dp).background(Color.Gray.copy(alpha = 0.2f), CircleShape))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // App Name & Package Name
            Column(modifier = Modifier.weight(1f)) {
                Text(text = app.appName, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                Text(text = app.packageName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Custom Segmented Control for Categories (Red / Orange / Green)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryButton(
                text = "Block",
                isSelected = app.category == "RED",
                selectedColor = Color(0xFFEF4444), // Tailwind Red 500
                onClick = { onRuleChanged("RED", false) },
                modifier = Modifier.weight(1f)
            )
            CategoryButton(
                text = "Review",
                isSelected = app.category == "ORANGE",
                selectedColor = Color(0xFFF97316), // Tailwind Orange 500
                onClick = { onRuleChanged("ORANGE", false) },
                modifier = Modifier.weight(1f)
            )
            CategoryButton(
                text = "Allow",
                isSelected = app.category == "GREEN" || app.isWhitelisted,
                selectedColor = Color(0xFF22C55E), // Tailwind Green 500
                onClick = { onRuleChanged("GREEN", true) },
                modifier = Modifier.weight(1f)
            )
        }

        // Divider between items (hidden on the very last item for a clean look)
        if (!isLastItem) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), thickness = 1.dp)
        }
    }
}

// Custom modern pill button
@Composable
fun CategoryButton(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // If selected, it uses a vibrant tinted background. If not, it's a subtle gray.
    val bgColor = if (isSelected) selectedColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val contentColor = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}