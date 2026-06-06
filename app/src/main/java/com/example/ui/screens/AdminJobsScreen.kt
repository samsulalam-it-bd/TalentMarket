package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Job
import com.example.ui.AdminViewModel
import com.example.ui.components.AdminTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminJobsScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val allJobs by adminViewModel.allJobs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("all") }

    val filtered = allJobs.filter { job ->
        val matchSearch = job.title.contains(searchQuery, ignoreCase = true) ||
                (job.companyName).contains(searchQuery, ignoreCase = true)
        val matchStatus = when (filterStatus) {
            "pinned" -> job.isBoosted
            "approved" -> job.isApproved
            "pending" -> !job.isApproved
            else -> true
        }
        matchSearch && matchStatus
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
    ) {
        AdminTopBar("💼 Jobs (${allJobs.size})", onBack = onBack)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search jobs...", color = Color(0xFF9CA3AF)) },
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
            listOf(
                "all" to "All",
                "pinned" to "Pinned",
                "approved" to "Approved",
                "pending" to "Pending"
            ).forEach { (key, label) ->
                val isSelected = filterStatus == key
                FilterChip(
                    selected = isSelected,
                    onClick = { filterStatus = key },
                    label = {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF3B82F6),
                        containerColor = Color(0xFF1A1A2E)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color(0xFF3B82F6) else Color(0xFF374151),
                        selectedBorderColor = Color(0xFF3B82F6)
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    emoji = "💼",
                    title = "No Jobs Found",
                    subtitle = "We couldn't find any jobs matching your criteria."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { job ->
                    AdminJobCard(
                        job = job,
                        onPin = { adminViewModel.pinJob(job.id, !job.isBoosted) },
                        onApprove = { adminViewModel.approveJob(job.id, !job.isApproved) },
                        onDelete = { adminViewModel.deleteJob(job.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminJobCard(
    job: Job,
    onPin: () -> Unit,
    onApprove: () -> Unit,
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
            if (job.isBoosted) Color(0xFFD4AF37).copy(0.6f)
            else Color(0xFF374151)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        job.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (job.companyName.isNotEmpty()) job.companyName else job.location,
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (job.isBoosted) {
                        val diff = job.boostEndDate - System.currentTimeMillis()
                        val remainingText = if (diff > 0) {
                            val days = diff / (24L * 60 * 60 * 1000)
                            val hours = (diff % (24L * 60 * 60 * 1000)) / (60 * 60 * 1000)
                            if (days > 0) "${days}d ${hours}h left" else "${hours}h left"
                        } else {
                            "Pinned"
                        }
                        Surface(
                          shape = RoundedCornerShape(6.dp),
                          color = Color(0xFFD4AF37).copy(0.15f)
                        ) {
                            Text(
                                "📌 PINNED ($remainingText)",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = Color(0xFFD4AF37),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (job.isApproved) {
                        Surface(
                          shape = RoundedCornerShape(6.dp),
                          color = Color(0xFF065F46).copy(0.3f)
                        ) {
                            Text(
                                "✅ APPROVED",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Surface(
                          shape = RoundedCornerShape(6.dp),
                          color = Color(0xFF78350F).copy(0.3f)
                        ) {
                            Text(
                                "⏳ PENDING",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = Color(0xFFF59E0B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
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
                        if (job.isBoosted) Color(0xFF9CA3AF)
                        else Color(0xFFD4AF37)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (job.isBoosted)
                            Color(0xFF9CA3AF) else Color(0xFFD4AF37)
                    )
                ) {
                    Text(
                        if (job.isBoosted) "Unpin" else "📌 Pin",
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        0.5.dp,
                        if (job.isApproved) Color(0xFF9CA3AF)
                        else Color(0xFF10B981)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (job.isApproved)
                            Color(0xFF9CA3AF) else Color(0xFF10B981)
                    )
                ) {
                    Text(
                        if (job.isApproved) "Unapprove" else "✅ Approve",
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
                Text("Delete Job?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will permanently delete \"${job.title}\". Cannot be undone.",
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
