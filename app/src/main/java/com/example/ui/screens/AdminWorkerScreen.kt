package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Worker
import com.example.ui.AdminViewModel
import com.example.ui.components.AdminTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWorkerScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val allWorkers by adminViewModel.allWorkers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filterPinned by remember { mutableStateOf(false) }

    val filtered = allWorkers.filter { worker ->
        val matchSearch = worker.name.contains(searchQuery, ignoreCase = true) ||
                worker.profession.contains(searchQuery, ignoreCase = true) ||
                worker.skills.any { it.contains(searchQuery, ignoreCase = true) }
        val matchPin = if (filterPinned) worker.isBoosted else true
        matchSearch && matchPin
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
    ) {
        AdminTopBar("👷 Workers (${allWorkers.size})", onBack = onBack)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search workers by name, profession, or skill...", color = Color(0xFF9CA3AF)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedBorderColor = Color(0xFF374151),
                focusedContainerColor = Color(0xFF1A1A2E),
                unfocusedContainerColor = Color(0xFF1A1A2E)
            ),
            leadingIcon = {
                Icon(Icons.Rounded.Search, null, tint = Color(0xFF9CA3AF))
            }
        )

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All Chip
            val isAllSelected = !filterPinned
            FilterChip(
                selected = isAllSelected,
                onClick = { filterPinned = false },
                label = {
                    Text(
                        text = "All (${allWorkers.size})",
                        color = if (isAllSelected) Color.White else Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF3B82F6),
                    containerColor = Color(0xFF1A1A2E)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isAllSelected,
                    borderColor = if (isAllSelected) Color(0xFF3B82F6) else Color(0xFF374151),
                    selectedBorderColor = Color(0xFF3B82F6)
                )
            )

            // Pinned Chip
            val isPinnedSelected = filterPinned
            val pinnedCount = allWorkers.count { it.isBoosted }
            FilterChip(
                selected = isPinnedSelected,
                onClick = { filterPinned = true },
                label = {
                    Text(
                        text = "Pinned ($pinnedCount)",
                        color = if (isPinnedSelected) Color.White else Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF3B82F6),
                    containerColor = Color(0xFF1A1A2E)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isPinnedSelected,
                    borderColor = if (isPinnedSelected) Color(0xFF3B82F6) else Color(0xFF374151),
                    selectedBorderColor = Color(0xFF3B82F6)
                )
            )
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { worker ->
                AdminWorkerCard(
                    worker = worker,
                    onPin = { adminViewModel.pinWorker(worker.id, !worker.isBoosted) },
                    onDelete = { adminViewModel.deleteWorker(worker.id) }
                )
            }
        }
    }
}

@Composable
fun AdminWorkerCard(
    worker: Worker,
    onPin: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        ),
        border = BorderStroke(
            0.5.dp,
            if (worker.isBoosted) Color(0xFFD4AF37).copy(0.6f)
            else Color(0xFF374151)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF0F3460), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        worker.name.firstOrNull()?.uppercase() ?: "W",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            worker.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (worker.isBoosted) {
                            val diff = worker.boostEndDate - System.currentTimeMillis()
                            val remainingText = if (diff > 0) {
                                val days = diff / (24L * 60 * 60 * 1000)
                                val hours = (diff % (24L * 60 * 60 * 1000)) / (60 * 60 * 1000)
                                if (days > 0) "${days}d ${hours}h left" else "${hours}h left"
                            } else {
                                "Pinned"
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFD4AF37).copy(0.15f)
                            ) {
                                Text(
                                    "📌 PINNED ($remainingText)",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = Color(0xFFD4AF37),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        worker.profession,
                        color = Color(0xFF60A5FA),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        worker.location,
                        color = Color(0xFF6B7280),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            if (worker.skills.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    worker.skills.joinToString(", "),
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF374151), thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPin,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        0.5.dp,
                        if (worker.isBoosted) Color(0xFF9CA3AF)
                        else Color(0xFFD4AF37)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (worker.isBoosted)
                            Color(0xFF9CA3AF) else Color(0xFFD4AF37)
                    )
                ) {
                    Text(
                        if (worker.isBoosted) "Unpin" else "📌 Pin",
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Icon(
                        Icons.Rounded.Delete, null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF1A1A2E),
            title = {
                Text("Delete Worker?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will permanently delete ${worker.name}'s profile. Cannot be undone.",
                    color = Color(0xFF9CA3AF)
                )
            },
            confirmButton = {
                Button(
                    onClick = { onDelete(); showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Color(0xFF9CA3AF))
                }
            }
        )
    }
}
