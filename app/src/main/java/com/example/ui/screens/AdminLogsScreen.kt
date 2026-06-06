package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AdminViewModel
import com.example.ui.components.AdminTopBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLogsScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val logs by adminViewModel.adminLogs.collectAsState()

    Scaffold(
        topBar = {
            AdminTopBar(
                title = "📝 Action Logs",
                onBack = onBack
            )
        },
        containerColor = Color(0xFF0F0F1A)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E30)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Action: ${log.action}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            val df = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                            Text(
                                text = df.format(Date(log.timestamp)),
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Admin: ${log.adminId}",
                            color = Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Target ID: ${log.targetId}",
                            color = Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
