package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.TalentViewModel
import com.example.data.Job
import com.example.utils.TimeUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun AppliedJobsScreen(
    viewModel: TalentViewModel,
    navController: NavController
) {
    // Load applications from Firestore
    val applications = remember { mutableStateListOf<Map<String, Any>>() }
    val jobs by viewModel.jobs.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val userId = viewModel.getCurrentUserId()
        try {
            FirebaseFirestore.getInstance()
                .collection("applications")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { snap ->
                    applications.clear()
                    snap.documents.forEach { doc ->
                        doc.data?.let { applications.add(it + ("id" to doc.id)) }
                    }
                    isLoading = false
                }
                .addOnFailureListener { 
                    com.example.ui.FirestoreErrorHandler.handleError(it, "AppliedJobs")
                    isLoading = false 
                }
        } catch (e: Exception) {
            com.example.ui.FirestoreErrorHandler.handleError(e, "AppliedJobs")
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
    ) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0F3460), Color(0xFF1A1A2E))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Rounded.ArrowBack, null, tint = Color.White)
                }
                Text("📋 Applied Jobs (${applications.size})",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold)
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFD4AF37))
            }
        } else if (applications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    emoji = "📝",
                    title = "No Applications",
                    subtitle = "Jobs you apply to will appear here",
                    actionLabel = "Find Jobs",
                    onAction = { navController.navigate("jobs") }
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(applications) { application ->
                    val jobId = application["jobId"] as? String ?: ""
                    val job = jobs.find { it.id == jobId }
                    val status = application["status"] as? String ?: "pending"
                    val appliedAt = application["timestamp"] as? Long ?: 0L

                    ApplicationCard(
                        job = job,
                        status = status,
                        appliedAt = appliedAt,
                        onClick = {
                            if (job != null) {
                                navController.navigate("job_details/${job.id}")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ApplicationCard(
    job: Job?,
    status: String,
    appliedAt: Long,
    onClick: () -> Unit
) {
    val (statusColor, statusText, statusEmoji) = when (status) {
        "pending" -> Triple(Color(0xFF6B7280), "Pending", "⏳")
        "viewed" -> Triple(Color(0xFF60A5FA), "Viewed", "👁️")
        "shortlisted" -> Triple(Color(0xFF10B981), "Shortlisted", "✅")
        "rejected" -> Triple(Color(0xFFEF4444), "Rejected", "❌")
        else -> Triple(Color(0xFF6B7280), "Applied", "📝")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        ),
        border = BorderStroke(0.5.dp, Color(0xFF374151))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    job?.title ?: "Job No Longer Available",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    job?.companyName ?: "",
                    color = Color(0xFF60A5FA),
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Applied ${TimeUtils.getRelativeTime(appliedAt)}",
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.labelSmall)
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    "$statusEmoji $statusText",
                    modifier = Modifier.padding(
                        horizontal = 10.dp, vertical = 6.dp),
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}
