package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
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

@Composable
fun SavedJobsScreen(
    viewModel: TalentViewModel,
    navController: NavController
) {
    val favoriteJobs by viewModel.favoriteJobs.collectAsState()
    val companyProfiles by viewModel.companyProfiles.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

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
                Text("❤️ Saved Jobs (${favoriteJobs.size})",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold)
            }
        }

        if (favoriteJobs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    emoji = "🔖",
                    title = "No Saved Jobs",
                    subtitle = "Tap the bookmark icon on any job\nto save it for later",
                    actionLabel = "Browse Jobs",
                    onAction = { navController.navigate("jobs") }
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriteJobs, key = { it.id }) { job ->
                    LaunchedEffect(job.userId) {
                        viewModel.loadCompanyProfile(job.userId)
                    }
                    val companyProfile = companyProfiles[job.userId]
                    val companyName = companyProfile?.companyName ?: "Independent Recruiter"
                    Box {
                        JobCard(
                            job = job,
                            companyName = companyName,
                            onActionClick = {
                                navController.navigate("job_details/${job.id}")
                            },
                            onApplyClick = {
                                viewModel.applyForJob(job)
                            },
                            onFavoriteClick = {
                                viewModel.toggleFavoriteJob(job)
                            }
                        )
                        // Remove bookmark button top-right
                        IconButton(
                            onClick = { viewModel.toggleFavoriteJob(job) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Bookmark,
                                contentDescription = "Remove",
                                tint = Color(0xFFD4AF37)
                            )
                        }
                    }
                }
            }
        }
    }
}
