package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun MaintenanceScreen(
    message: String = "We are currently performing scheduled system updates to improve your experience. Please check back later!"
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F1A),
                        Color(0xFF0A0A12)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Large 🔧 emoji with custom visual spacing
            Text(
                text = "🔧",
                fontSize = 72.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Title
            Text(
                text = "Under Maintenance",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Subtitle text block with clean padding
            Surface(
                color = Color(0xFF1E1E30),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = message,
                    color = Color(0xFFD1D5DB),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(20.dp)
                )
            }

            // Bottom informational tagline
            Text(
                text = "Thank you for your patience",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
