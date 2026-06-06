package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.R
import com.example.data.SupportRequest
import com.example.ui.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportAdminScreen(viewModel: AdminViewModel, navController: NavController) {
    val supportRequests by viewModel.tickets.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("All") }
    var showResolveDialog by remember { mutableStateOf(false) }
    var selectedRequest by remember { mutableStateOf<SupportRequest?>(null) }
    var notificationSubject by remember { mutableStateOf("") }
    var notificationMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadSupportTickets()
    }

    val filteredRequests = remember(supportRequests, selectedFilter) {
        if (selectedFilter == "All") {
            supportRequests
        } else {
            supportRequests.filter { it.status.equals(selectedFilter, ignoreCase = true) }
        }
    }

    if (showResolveDialog && selectedRequest != null) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = { Text("Resolve Ticket & Notify") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Send a notification to the user about resolving this issue.")
                    OutlinedTextField(
                        value = notificationSubject,
                        onValueChange = { notificationSubject = it },
                        label = { Text("Notification Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notificationMessage,
                        onValueChange = { notificationMessage = it },
                        label = { Text("Notification Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val request = selectedRequest!!
                    viewModel.replyToTicket(request.id, notificationMessage)
                    if (notificationSubject.isNotBlank() && notificationMessage.isNotBlank()) {
                        viewModel.sendNotificationToUser(
                            userId = request.userId,
                            title = notificationSubject,
                            body = notificationMessage
                        )
                    }
                    showResolveDialog = false
                }) {
                    Text("Send & Resolve")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.support_list_admin), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter Selector Tab Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Open", "Resolved").forEach { option ->
                    val isSelected = selectedFilter == option
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = option },
                        label = { Text(option) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            if (filteredRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        emoji = "💬",
                        title = "No Support Tickets",
                        subtitle = "Need help? Send us a message\nand we'll respond quickly",
                        actionLabel = null,
                        onAction = null
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredRequests, key = { it.id }) { request ->
                        SupportRequestCard(
                            request = request,
                            onStatusChange = { newStatus ->
                                if (newStatus == "Resolved") {
                                    selectedRequest = request
                                    notificationSubject = "Support Ticket Resolved: ${request.subject}"
                                    notificationMessage = "Your support request has been resolved. Let us know if you need any further help!"
                                    showResolveDialog = true
                                } else {
                                    viewModel.updateTicketStatus(request.id, newStatus)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SupportRequestCard(
    request: SupportRequest,
    onStatusChange: (String) -> Unit
) {
    val dateString = remember(request.createdAt) {
        try {
            val sdf = SimpleDateFormat("MMM d, yyyy - hh:mm a", Locale.getDefault())
            sdf.format(Date(request.createdAt))
        } catch (e: Exception) {
            "Just now"
        }
    }

    val isOpen = request.status.equals("Open", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Status Pills & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isOpen) Icons.Default.Schedule else Icons.Default.CheckCircle,
                        contentDescription = "Status Icon",
                        tint = if (isOpen) MaterialTheme.colorScheme.primary else Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = request.status.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOpen) MaterialTheme.colorScheme.primary else Color(0xFF4CAF50)
                    )
                }

                Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subject text
            Text(
                text = request.subject,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Sender Email Detail
            Text(
                text = "From: ${request.email}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Message multi-line representation
            Text(
                text = request.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(8.dp))

            // Actions Block: marking Resolved / reopen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isOpen) {
                    Button(
                        onClick = { onStatusChange("Resolved") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Resolve Icon",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark Resolved", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onStatusChange("Open") },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Reopen Icon",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reopen Issue", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
