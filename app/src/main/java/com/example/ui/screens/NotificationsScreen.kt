package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.R
import com.example.data.Notification
import com.example.ui.Screen
import com.example.ui.TalentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: TalentViewModel, navController: NavController) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    var showUnreadOnly = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val displayedNotifications = if (showUnreadOnly.value) {
        notifications.filter { !it.isRead }
    } else {
        notifications
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                            Text(
                                text = "Mark All Read",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Toggle for unread only
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Show unread only",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Switch(
                        checked = showUnreadOnly.value,
                        onCheckedChange = { showUnreadOnly.value = it }
                    )
                }

                if (displayedNotifications.isEmpty()) {
                    EmptyStateView(
                        emoji = "🔔",
                        title = "No Notifications Yet",
                        subtitle = "Apply for jobs or boost your profile\nto start getting updates",
                        actionLabel = "Explore Jobs",
                        onAction = { navController.navigate("jobs") }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(displayedNotifications, key = { it.id }) { notif ->
                            NotificationCard(
                                notification = notif,
                                onClick = {
                                    if (!notif.isRead) {
                                        viewModel.markNotificationAsRead(notif.id)
                                    }
                                    if (notif.relatedJobId.isNotEmpty()) {
                                        navController.navigate(Screen.JobDetails.createRoute(notif.relatedJobId))
                                    }
                                },
                                onToggleRead = {
                                    if (notif.isRead) {
                                        // If we had a markAsUnread, we would call it. 
                                        // For now, only mark as read is implemented.
                                        // Assuming markNotificationAsRead doesn't have a toggle.
                                        // Let's implement toggle in viewmodel later if missing.
                                        viewModel.toggleNotificationReadStatus(notif.id, !notif.isRead)
                                    } else {
                                        viewModel.markNotificationAsRead(notif.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    onToggleRead: () -> Unit
) {
    val isUnread = !notification.isRead && notification.type != "announcement"
    
    val (icon, tint) = when (notification.type) {
        "view" -> Icons.Default.RemoveRedEye to MaterialTheme.colorScheme.primary
        "bookmark" -> Icons.Default.Favorite to Color.Red
        "category" -> Icons.Default.Star to MaterialTheme.colorScheme.tertiary
        "announcement" -> Icons.Default.Campaign to MaterialTheme.colorScheme.error
        else -> Icons.Default.Notifications to MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnread) 2.dp else 0.dp
        ),
        border = if (isUnread) {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = formatRelativeTime(notification.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
            
            // Unread Indicator Dot and Toggle button
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onToggleRead) {
                Icon(
                    imageVector = if (isUnread) Icons.Default.Circle else Icons.Default.CheckCircle,
                    contentDescription = if (isUnread) "Mark as read" else "Mark as unread",
                    tint = if (isUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

fun formatRelativeTime(time: Long): String {
    val diff = System.currentTimeMillis() - time
    return when {
        diff < 60000L -> "Just now"
        diff < 3600000L -> "${diff / 60000L}m ago"
        diff < 86400000L -> "${diff / 3600000L}h ago"
        diff < 172800000L -> "Yesterday"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            sdf.format(java.util.Date(time))
        }
    }
}
