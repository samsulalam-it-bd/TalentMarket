package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AdminViewModel
import com.example.ui.ReportedPost
import com.example.ui.components.AdminTopBar

@Composable
fun AdminReportsScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val reportedPosts by adminViewModel.reportedPosts.collectAsState()
    
    // Toast state
    var toastMessage by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            AdminTopBar(
                title = "🚩 Reported Posts (${reportedPosts.size})",
                onBack = onBack
            )
        },
        containerColor = Color(0xFF0F0F1A)
    ) { paddingValues ->
        if (reportedPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "No reports",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Everything looks clean!",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "No job posts or worker profiles have been reported.",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(reportedPosts, key = { "${it.postType}-${it.id}" }) { post ->
                    ReportedPostItem(
                        post = post,
                        onReactivate = {
                            adminViewModel.reactivatePost(post) { success ->
                                toastMessage = if (success) "Post successfully reactivated" else "Failed to reactivate post"
                            }
                        },
                        onDelete = {
                            adminViewModel.deleteReportedPost(post) { success ->
                                toastMessage = if (success) "Post successfully deleted" else "Failed to delete post"
                            }
                        }
                    )
                }
            }
        }
        
        toastMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Snackbar(
                    action = {
                        TextButton(onClick = { toastMessage = null }) {
                            Text("Dismiss", color = Color(0xFFD4AF37))
                        }
                    }
                ) {
                    Text(msg)
                }
            }
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                toastMessage = null
            }
        }
    }
}

@Composable
fun ReportedPostItem(
    post: ReportedPost,
    onReactivate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2F)
        ),
        border = BorderStroke(
            width = if (post.isDeactivated) 2.dp else 0.5.dp,
            color = if (post.isDeactivated) Color(0xFFEF4444) else Color(0xFF374151)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Badge & Reports Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type badge
                val badgeColor = if (post.postType == "job") Color(0xFF3B82F6) else Color(0xFF10B981)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = post.postType.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }

                // Report count indicator
                val repColor = if (post.reportsCount >= 5) Color(0xFFEF4444) else Color(0xFFF59E0B)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Flag,
                        contentDescription = "Reports",
                        tint = repColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${post.reportsCount} Reports",
                        color = repColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Deactivation status warning badge
            if (post.isDeactivated) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEF4444).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                         imageVector = Icons.Rounded.Warning,
                         contentDescription = "Deactivated",
                         tint = Color(0xFFEF4444),
                         modifier = Modifier.size(16.dp)
                    )
                    Text(
                         text = "AUTO-DEACTIVATED (10+ Reports)",
                         fontSize = 11.sp,
                         fontWeight = FontWeight.Bold,
                         color = Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details/Snippet Text
            Text(
                text = post.details,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9CA3AF),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Reason/Reports section list
            if (post.reports.isNotEmpty()) {
                Text(
                    text = "Report Reasons:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF141423),
                    border = BorderStroke(0.5.dp, Color(0xFF374151))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        post.reports.take(5).forEach { report ->
                            val reason = report["reason"] as? String ?: "No reason given"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = reason,
                                    fontSize = 12.sp,
                                    color = Color(0xFFE5E7EB)
                                )
                            }
                        }
                        if (post.reports.size > 5) {
                            Text(
                                text = "+ ${post.reports.size - 5} more report reasons...",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF),
                                modifier = Modifier.padding(start = 14.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Buttons / Action layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReactivate,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF10B981)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Reactivate",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reactivate", fontSize = 12.sp)
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Post", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}
