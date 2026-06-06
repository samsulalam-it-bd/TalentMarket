package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChipWithCount(
    category: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
                if (count > 0) {
                    Surface(
                        shape = CircleShape,
                        color = if (selected)
                            Color.White.copy(0.3f)
                        else
                            Color(0xFF374151)
                    ) {
                        Text(
                            text = count.toString(),
                            modifier = Modifier.padding(
                                horizontal = 6.dp, vertical = 2.dp
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) Color.White
                            else Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }
    )
}
