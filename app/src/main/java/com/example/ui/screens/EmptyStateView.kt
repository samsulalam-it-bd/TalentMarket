package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyStateView(
    emoji: String,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(emoji, fontSize = 64.sp)

            Text(
                title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Text(
                subtitle,
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37)
                    )
                ) {
                    Text(
                        actionLabel,
                        color = Color(0xFF1A1A2E),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
