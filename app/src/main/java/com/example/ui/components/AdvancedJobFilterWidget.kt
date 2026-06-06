package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.ConfigData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedJobFilterWidget(
    selectedCategory: String,
    selectedLocation: String,
    selectedJobType: String,
    sortBy: String,
    onDismissRequest: () -> Unit,
    onApply: (category: String, location: String, jobType: String, sortBy: String) -> Unit
) {
    var tempCategory by remember { mutableStateOf(selectedCategory) }
    var tempLocation by remember { mutableStateOf(selectedLocation) }
    var tempJobType by remember { mutableStateOf(selectedJobType) }
    var tempSortBy by remember { mutableStateOf(sortBy) }

    val categories = listOf("All") + ConfigData.getActiveCategoriesEn()
    val locations = listOf("All") + ConfigData.gulfCountriesWithCities.keys.toList()
    val jobTypes = listOf("All", "Full-time", "Part-time", "Contract", "Freelance")
    val sortOptions = listOf("Recent")

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Advanced Search",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Divider()

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Category
                    item {
                        FilterSectionTitle("Category")
                        var categoryExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = tempCategory,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            tempCategory = cat
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Sort By
                    item {
                        FilterSectionTitle("Sort By")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            sortOptions.forEach { option ->
                                FilterChip(
                                    selected = tempSortBy == option,
                                    onClick = { tempSortBy = option },
                                    label = { Text(option) }
                                )
                            }
                        }
                    }

                    // Job Type
                    item {
                        FilterSectionTitle("Job Type")
                        // FlowRow or wrapped layout could be better but let's use scrolling row for simplicity
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(jobTypes) { option ->
                                FilterChip(
                                    selected = tempJobType == option,
                                    onClick = { tempJobType = option },
                                    label = { Text(option) }
                                )
                            }
                        }
                    }

                    // Location
                    item {
                        FilterSectionTitle("Location")
                        var locationExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = locationExpanded,
                            onExpandedChange = { locationExpanded = !locationExpanded }
                        ) {
                            OutlinedTextField(
                                value = tempLocation,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = locationExpanded,
                                onDismissRequest = { locationExpanded = false }
                            ) {
                                locations.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            tempLocation = selectionOption
                                            locationExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Category
                    item {
                        FilterSectionTitle("Category")
                        var categoryExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = tempCategory,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categories.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            tempCategory = selectionOption
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Bottom Buttons
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                tempCategory = "All"
                                tempLocation = "All"
                                tempJobType = "All"
                                tempSortBy = "Recent"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset")
                        }
                        Button(
                            onClick = {
                                onApply(tempCategory, tempLocation, tempJobType, tempSortBy)
                                onDismissRequest()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply Filters")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
