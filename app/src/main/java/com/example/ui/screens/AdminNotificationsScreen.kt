package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AdminViewModel
import com.example.ui.components.AdminTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationsScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val users by adminViewModel.users.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedTarget by remember { mutableStateOf("All Users") } // "All Users", "Workers Only", "Employers Only", "Specific User"
    var specificUserQuery by remember { mutableStateOf("") }
    
    var titleText by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    
    var sendInApp by remember { mutableStateOf(true) }
    var sendPush by remember { mutableStateOf(false) }
    
    var isSending by remember { mutableStateOf(false) }
    var showSuccessAlert by remember { mutableStateOf(false) }
    var successAlertMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AdminTopBar(
                title = "🔔 Send Notifications",
                onBack = onBack
            )
        },
        containerColor = Color(0xFF0F0F1A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Target selector header
            Text(
                text = "1. Select Target Audience",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            // Selector Chips Group
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val targets = listOf("All Users", "Workers Only", "Employers Only", "Specific User")
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        targets.take(2).forEach { target ->
                            val isSelected = selectedTarget == target
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    selectedTarget = target
                                    errorMessage = null 
                                },
                                label = { Text(target, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3B82F6),
                                    containerColor = Color(0xFF1E1E30),
                                    selectedLabelColor = Color.White,
                                    labelColor = Color(0xFF9CA3AF)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0xFF374151),
                                    selectedBorderColor = Color(0xFF3B82F6)
                                )
                            )
                        }
                    }
                    // Row 2
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        targets.drop(2).forEach { target ->
                            val isSelected = selectedTarget == target
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    selectedTarget = target
                                    errorMessage = null 
                                },
                                label = { Text(target, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF3B82F6),
                                    containerColor = Color(0xFF1E1E30),
                                    selectedLabelColor = Color.White,
                                    labelColor = Color(0xFF9CA3AF)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0xFF374151),
                                    selectedBorderColor = Color(0xFF3B82F6)
                                )
                            )
                        }
                    }
                }
            }

            // Custom user lookup field if Specific User is selected
            AnimatedVisibility(
                visible = selectedTarget == "Specific User",
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter User Email Address or unique User ID",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF9CA3AF)
                    )
                    OutlinedTextField(
                        value = specificUserQuery,
                        onValueChange = { 
                            specificUserQuery = it
                            errorMessage = null 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. user@example.com or uid_12345", color = Color(0xFF6B7280)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF9CA3AF))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedContainerColor = Color(0xFF1E1E30),
                            unfocusedContainerColor = Color(0xFF1E1E30)
                        )
                    )
                }
            }

            Divider(color = Color(0xFF374151), modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = "2. Select Notification Type",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = sendInApp,
                    onCheckedChange = { sendInApp = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6), checkmarkColor = Color.White)
                )
                Text("In-App Notification", color = Color.White, fontSize = 14.sp)

                Spacer(modifier = Modifier.width(16.dp))

                Checkbox(
                    checked = sendPush,
                    onCheckedChange = { sendPush = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6), checkmarkColor = Color.White)
                )
                Text("Push Notification (OneSignal)", color = Color.White, fontSize = 14.sp)
            }

            Divider(color = Color(0xFF374151), modifier = Modifier.padding(vertical = 4.dp))

            // Notification Details Header
            Text(
                text = "3. Compose Message Details",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text("Notification Title") },
                placeholder = { Text("e.g. Maintenance Scheduled, Premium Active", color = Color(0xFF6B7280)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF374151)
                )
            )

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("Notification Message") },
                placeholder = { Text("Type full description here...", color = Color(0xFF6B7280)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF374151)
                )
            )

            // Live Preview Card
            Text(
                text = "Live Phone Push Notification Preview",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF9CA3AF)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E30)),
                border = BorderStroke(1.dp, Color(0xFF374151))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "Talent Portal",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "now",
                            color = Color(0xFF9CA3AF),
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (titleText.isBlank()) "Notification Title Title" else titleText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (messageText.isBlank()) "Type message contents above to preview real presentation." else messageText,
                        color = Color(0xFFD1D5DB),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Error Message Alert
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Action Buttons
            Button(
                onClick = {
                    if (!sendInApp && !sendPush) {
                        errorMessage = "Please select at least one Notification Type."
                        return@Button
                    }
                    if (titleText.isBlank() || messageText.isBlank()) {
                        errorMessage = "Title and Message fields cannot be blank."
                        return@Button
                    }
                    
                    isSending = true
                    errorMessage = null

                    scope.launch {
                        try {
                            when (selectedTarget) {
                                "All Users" -> {
                                    adminViewModel.sendAdminNotification(selectedTarget, users, null, titleText, messageText, sendInApp, sendPush)
                                    successAlertMessage = "Successfully broadcasted to all registered users!"
                                }
                                "Workers Only" -> {
                                    val targetUsers = users.filter { it.userType.equals("worker", ignoreCase = true) }
                                    adminViewModel.sendAdminNotification(selectedTarget, targetUsers, null, titleText, messageText, sendInApp, sendPush)
                                    successAlertMessage = "Successfully sent to ${targetUsers.size} workers."
                                }
                                "Employers Only" -> {
                                    val targetUsers = users.filter { it.userType.equals("employer", ignoreCase = true) }
                                    adminViewModel.sendAdminNotification(selectedTarget, targetUsers, null, titleText, messageText, sendInApp, sendPush)
                                    successAlertMessage = "Successfully sent to ${targetUsers.size} employers."
                                }
                                "Specific User" -> {
                                    val cleanedQuery = specificUserQuery.trim()
                                    if (cleanedQuery.isBlank()) {
                                        errorMessage = "Please enter email or User ID."
                                        isSending = false
                                        return@launch
                                    }
                                    val matched = users.find { 
                                        it.email.equals(cleanedQuery, ignoreCase = true) || 
                                        it.uid == cleanedQuery 
                                    }
                                    val targetId = matched?.uid ?: cleanedQuery
                                    adminViewModel.sendAdminNotification(selectedTarget, emptyList(), targetId, titleText, messageText, sendInApp, sendPush)
                                    successAlertMessage = "Delivered message safely to user target: $targetId"
                                }
                            }
                            
                            showSuccessAlert = true
                            // Reset inputs
                            titleText = ""
                            messageText = ""
                            specificUserQuery = ""
                        } catch (e: Exception) {
                            errorMessage = "Failed: ${e.message}"
                        } finally {
                            isSending = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSending && titleText.isNotBlank() && messageText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD4AF37), // beautiful gold
                    disabledContainerColor = Color(0xFF332D15),
                    disabledContentColor = Color(0xFF7C7149)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(imageVector = Icons.Rounded.Send, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Send Notification Invite",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // Success dialog
    if (showSuccessAlert) {
        AlertDialog(
            onDismissRequest = { showSuccessAlert = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981)
                    )
                    Text("Broadcast Success", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = successAlertMessage,
                    color = Color(0xFFD1D5DB)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessAlert = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1A1A2E)
        )
    }
}
