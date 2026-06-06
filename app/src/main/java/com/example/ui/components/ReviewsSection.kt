package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Review
import com.example.ui.TalentViewModel

@Composable
fun ReviewsSection(
    targetId: String,
    talentViewModel: TalentViewModel
) {
    val reviews by talentViewModel.reviews.collectAsState()

    LaunchedEffect(targetId) {
        talentViewModel.loadReviews(targetId)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Reviews (${reviews.size})",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            if (reviews.isNotEmpty()) {
                val avg = reviews.map { it.rating }.average()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Rounded.Star, null,
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        String.format("%.1f", avg),
                        color = Color(0xFFD4AF37),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (reviews.isEmpty()) {
            com.example.ui.screens.EmptyStateView(
                emoji = "⭐",
                title = "No Reviews Yet",
                subtitle = "Be the first to leave a review",
                actionLabel = null,
                onAction = null
            )
        } else {
            reviews.forEach { review ->
                ReviewCard(review = review)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        ),
        border = BorderStroke(0.5.dp, Color(0xFF374151))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF0F3460), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            review.reviewerName.firstOrNull()?.uppercase() ?: "U",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Column {
                        Text(
                            review.reviewerName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            review.reviewerType,
                            color = Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            Icons.Rounded.Star, null,
                            modifier = Modifier.size(13.dp),
                            tint = if (star <= review.rating)
                                Color(0xFFD4AF37)
                            else Color(0xFF374151)
                        )
                    }
                }
            }
            if (review.comment.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    review.comment,
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                getTimeAgo(review.createdAt),
                color = Color(0xFF6B7280),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

fun getTimeAgo(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "$days days ago"
        hours > 0 -> "$hours hours ago"
        minutes > 0 -> "$minutes mins ago"
        else -> "Just now"
    }
}
