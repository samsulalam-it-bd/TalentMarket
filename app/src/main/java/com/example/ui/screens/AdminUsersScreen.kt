package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.ui.AdminViewModel
import com.example.ui.UserAdmin
import com.example.ui.components.AdminTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val users by adminViewModel.users.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "worker", "employer"
    
    val filteredUsers = users.filter { user ->
        val matchesSearch = user.name.contains(searchQuery, ignoreCase = true) || 
                            user.email.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter.lowercase()) {
            "all" -> true
            else -> user.userType.equals(selectedFilter, ignoreCase = true)
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            AdminTopBar(
                title = "👥 Users (${filteredUsers.size})",
                onBack = onBack
            )
        },
        containerColor = Color(0xFF0F0F1A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input Block
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by name or email...", color = Color(0xFF6B7280)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF9CA3AF)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Clear",
                                tint = Color(0xFF9CA3AF)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1A1A2E),
                    unfocusedContainerColor = Color(0xFF101026),
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF374151)
                )
            )

            // Filter Chips Node
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Worker", "Employer")
                filters.forEach { filter ->
                    val isSelected = selectedFilter.equals(filter, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter,
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
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Users list
            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        emoji = "👥",
                        title = "No Users Found",
                        subtitle = "We couldn't find any users matching your criteria."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredUsers,
                        key = { it.uid }
                    ) { userAdmin ->
                        AdminUserCard(
                            user = userAdmin,
                            onBan = { ban -> adminViewModel.banUser(userAdmin.uid, ban) },
                            onVerify = { verify -> adminViewModel.verifyUser(userAdmin.uid, verify) },
                            onToggleAdmin = { makeAdmin -> adminViewModel.toggleAdmin(userAdmin.uid, makeAdmin) },
                            onDelete = { adminViewModel.deleteUser(userAdmin.uid) },
                            onSetPlan = { plan -> adminViewModel.setUserPlan(userAdmin.uid, plan) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserCard(
    user: UserAdmin,
    onBan: (Boolean) -> Unit,
    onVerify: (Boolean) -> Unit,
    onToggleAdmin: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onSetPlan: (String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPlanDropdown by remember { mutableStateOf(false) }
    
    val planOptions = listOf("free", "silver", "gold", "business", "enterprise")
    
    val cardBackground = if (user.isBanned) {
        Color(0xFF3A1A22) // Red tint for banned
    } else {
        Color(0xFF1E1E30) // Regular high-contrast dark
    }

    val cardBorder = if (user.isBanned) {
        BorderStroke(1.dp, Color(0xFFEF4444))
    } else {
        BorderStroke(0.5.dp, Color(0xFF374151))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = cardBorder
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row (Avatar + Basic info)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar representation
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (user.isBanned) Color(0xFFEF4444).copy(alpha = 0.3f)
                            else Color(0xFF3B82F6).copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.firstOrNull()?.uppercase()?.toString() ?: "?",
                        color = if (user.isBanned) Color(0xFFEF4444) else Color(0xFF60A5FA),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Name & Email
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = user.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (user.isVerified) {
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = "Verified",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = user.email,
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Badges/Status column
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (user.userType.equals("worker", ignoreCase = true)) Color(0xFF065F46)
                                else Color(0xFF1E3A8A)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = user.userType.uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (user.isAdmin) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF8B5CF6))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ADMIN",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (user.isBanned) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF991B1B))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "BANNED",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Middle section (Plan display)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF101026))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Stars,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Subscription Plan: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9CA3AF)
                )
                Text(
                    text = user.plan.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lower Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Ban/Unban Button
                Button(
                    onClick = { onBan(!user.isBanned) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBanned) Color(0xFF059669) else Color(0xFFDC2626)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    val banText = if (user.isBanned) "Unban" else "Ban"
                    Text(banText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Verify / Unverify Button
                Button(
                    onClick = { onVerify(!user.isVerified) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isVerified) Color(0xFF4B5563) else Color(0xFF2563EB)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    val verifyText = if (user.isVerified) "Unverify" else "Verify"
                    Text(verifyText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Plan change dropdown button
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { showPlanDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Plan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showPlanDropdown,
                        onDismissRequest = { showPlanDropdown = false },
                        modifier = Modifier.background(Color(0xFF1E1E30))
                    ) {
                        planOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt.uppercase(), color = Color.White, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    onSetPlan(opt)
                                    showPlanDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Toggle admin status button
                val isAdminRole = user.isAdmin
                OutlinedButton(
                    onClick = { onToggleAdmin(!isAdminRole) },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, if (isAdminRole) Color(0xFFEF4444) else Color(0xFF8B5CF6)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isAdminRole) Color(0xFFEF4444) else Color(0xFFC084FC)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SupervisorAccount,
                        contentDescription = null,
                        tint = if (isAdminRole) Color(0xFFEF4444) else Color(0xFFC084FC),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAdminRole) "Remove Admin" else "Make Admin",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Delete Button
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Delete User",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // Delete Confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444)
                    )
                    Text("Delete User Account", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Are you absolutely sure you want to delete ${user.name}'s account? This action is permanent and cannot be undone.",
                    color = Color(0xFF9CA3AF)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete Permanently", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563))
                ) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF1A1A2E),
            textContentColor = Color.White
        )
    }
}
