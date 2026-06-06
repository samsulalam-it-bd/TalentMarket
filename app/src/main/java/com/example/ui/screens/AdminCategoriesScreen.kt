package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.scale
import com.example.ui.AdminViewModel
import com.example.ui.Category
import com.example.ui.components.AdminTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriesScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val categories by adminViewModel.categoriesState.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedSectorFilter by remember { mutableStateOf("All") }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Category?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Category?>(null) }
    var showImportConfirm by remember { mutableStateOf(false) }
    
    // Derived sectors
    val sectors = remember(categories) {
        listOf("All") + categories.map { it.sector }.distinct().filter { it.isNotEmpty() }
    }
    
    // Filtered categories
    val filteredCategories = remember(categories, searchQuery, selectedSectorFilter) {
        categories.filter { cat ->
            val matchesSearch = cat.nameEn.contains(searchQuery, ignoreCase = true) ||
                    cat.nameAr.contains(searchQuery, ignoreCase = true) ||
                    cat.sector.contains(searchQuery, ignoreCase = true)
            val matchesSector = selectedSectorFilter == "All" || cat.sector == selectedSectorFilter
            matchesSearch && matchesSector
        }
    }

    Scaffold(
        topBar = {
            AdminTopBar(title = "📁 Manage Categories", onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF8B5CF6),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Category")
            }
        },
        containerColor = Color(0xFF0F0F1A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Stats & Tools Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Categories: ${categories.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Active: ${categories.count { it.isActive }} | Disabled: ${categories.count { !it.isActive }}",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Button(
                        onClick = { showImportConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset & Load 100 Defaults", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Search Bar & Filter Sector Scroll
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                placeholder = { Text("Search by name, Arabic name or sector...", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3B2E5C),
                    focusedContainerColor = Color(0xFF151525),
                    unfocusedContainerColor = Color(0xFF151525)
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // Sector filters
            if (sectors.size > 1) {
                ScrollableTabRow(
                    selectedTabIndex = sectors.indexOf(selectedSectorFilter).coerceAtLeast(0),
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    sectors.forEach { sector ->
                        val isSelected = selectedSectorFilter == sector
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFF8B5CF6) else Color(0xFF1E1E2E))
                                .clickable { selectedSectorFilter = sector }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (sector == "All") "All Sectors" else sector,
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Categories list
            if (filteredCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching categories found",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredCategories) { category ->
                        CategoryItemRow(
                            category = category,
                            onToggleActive = { active ->
                                adminViewModel.toggleCategory(category.id, active)
                            },
                            onEdit = {
                                showEditDialog = category
                            },
                            onDelete = {
                                showDeleteConfirm = category
                            }
                        )
                    }
                }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        CategoryFormDialog(
            title = "Add New Category",
            initialCategory = null,
            onDismiss = { showAddDialog = false },
            onSubmit = { nameEn, nameAr, sector, icon ->
                adminViewModel.addCategory(nameEn, nameAr, icon, sector)
                showAddDialog = false
            }
        )
    }

    // Edit dialog
    showEditDialog?.let { category ->
        CategoryFormDialog(
            title = "Edit Category",
            initialCategory = category,
            onDismiss = { showEditDialog = null },
            onSubmit = { nameEn, nameAr, sector, icon ->
                adminViewModel.editCategory(category.id, nameEn, nameAr, icon, sector)
                showEditDialog = null
            }
        )
    }

    // Delete confirm dialog
    showDeleteConfirm?.let { category ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Category", color = Color.White) },
            text = { Text("Are you sure you want to delete '${category.nameEn}'? This action cannot be undone.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.deleteCategory(category.id)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1E2E)
        )
    }

    // Reset Confirm Dialog
    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            title = { Text("Reset to Defaults?", color = Color.White) },
            text = { Text("This will overwrite your existing categories in Firestore and import all 100 pre-configured job categories, including Construction, IT, Healthcare, and Oil & Energy. Continue?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.importDefaultCategories()
                        showImportConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Import & Overwrite", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirm = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1E2E)
        )
    }
}

@Composable
fun CategoryItemRow(
    category: Category,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151525)),
        border = BorderStroke(1.dp, if (category.isActive) Color(0xFF2E2E3E) else Color(0xFF3E1E1E)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Category info
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle Accent Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (category.isActive) Color(0xFF2A2B4D) else Color(0xFF2D1823)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = if (category.isActive) Color(0xFF8B5CF6) else Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = category.nameEn,
                            color = if (category.isActive) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (category.isActive) Color(0x2210B981) else Color(0x22EF4444))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (category.isActive) "ACTIVE" else "DISABLED",
                                color = if (category.isActive) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = category.nameAr,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Sector: ${category.sector}",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }

            // Category Controls (Switch, Edit, Delete)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Switch(
                    checked = category.isActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF8B5CF6),
                        checkedTrackColor = Color(0x558B5CF6),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0x22FFFFFF)
                    ),
                    modifier = Modifier.scale(0.8f)
                )

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Category",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Category",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFormDialog(
    title: String,
    initialCategory: Category?,
    onDismiss: () -> Unit,
    onSubmit: (nameEn: String, nameAr: String, sector: String, icon: String) -> Unit
) {
    var nameEn by remember { mutableStateOf(initialCategory?.nameEn ?: "") }
    var nameAr by remember { mutableStateOf(initialCategory?.nameAr ?: "") }
    var sector by remember { mutableStateOf(initialCategory?.sector ?: "") }
    var icon by remember { mutableStateOf(initialCategory?.icon ?: "work") }
    
    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            border = BorderStroke(1.dp, Color(0xFF8B5CF6))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // En Name
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("English Name", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF3B2E5C)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Ar Name
                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text("Arabic Name", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF3B2E5C)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Sector
                OutlinedTextField(
                    value = sector,
                    onValueChange = { sector = it },
                    label = { Text("Sector (e.g. Construction, IT, Finance)", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF3B2E5C)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Icon (string key)
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text("Icon key (e.g. work, code, health, construction)", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color(0xFF3B2E5C)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Button(
                        onClick = {
                            if (nameEn.isBlank() || nameAr.isBlank() || sector.isBlank()) {
                                errorMsg = "All fields except Icon are required."
                            } else {
                                onSubmit(nameEn.trim(), nameAr.trim(), sector.trim(), icon.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}
