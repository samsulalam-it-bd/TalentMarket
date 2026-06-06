package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TalentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewBottomSheet(
    targetId: String,
    targetName: String,
    targetType: String,
    talentViewModel: TalentViewModel,
    onDismiss: () -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A2E),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Color(0xFF374151),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Rate $targetName",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )

            // Star selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { star ->
                    IconButton(
                        onClick = { selectedRating = star },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            if (star <= selectedRating)
                                Icons.Rounded.Star
                            else Icons.Rounded.StarOutline,
                            contentDescription = "$star stars",
                            modifier = Modifier.size(36.dp),
                            tint = if (star <= selectedRating)
                                Color(0xFFD4AF37)
                            else Color(0xFF374151)
                        )
                    }
                }
            }

            Text(
                when (selectedRating) {
                    1 -> "Poor ☹️"
                    2 -> "Fair 😐"
                    3 -> "Good 🙂"
                    4 -> "Very Good 😊"
                    5 -> "Excellent 🌟"
                    else -> ""
                },
                color = Color(0xFFD4AF37),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Write a review (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD4AF37),
                    focusedLabelColor = Color(0xFFD4AF37)
                )
            )

            if (isSubmitted) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF065F46).copy(0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "✅ Review submitted!",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF10B981),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = {
                    if (!isSubmitted) {
                        talentViewModel.submitReview(
                            targetId = targetId,
                            targetType = targetType,
                            rating = selectedRating,
                            comment = comment
                        )
                        isSubmitted = true
                        onDismiss() // Let's also dismiss after a short delay or right away
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSubmitted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD4AF37),
                    disabledContainerColor = Color(0xFF374151)
                )
            ) {
                Text(
                    if (isSubmitted) "Submitted ✓" else "Submit Review",
                    color = Color(0xFF1A1A2E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
