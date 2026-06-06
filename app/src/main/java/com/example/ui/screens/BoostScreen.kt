package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BoostPlan
import com.example.ui.TalentViewModel

@Composable
fun BoostScreen(
    viewModel: TalentViewModel,
    jobId: String,
    onBoost: (BoostPlan) -> Unit,
    onBack: () -> Unit
) {
    val boostPlans by viewModel.boostPlans.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, null, tint = Color.White)
            }
            Text("⚡ Boost Your Post",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold)
        }

        Text(
            "Appear at the top of search results",
            modifier = Modifier.padding(horizontal = 24.dp),
            color = Color(0xFF9CA3AF),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        if (boostPlans.isEmpty()) {
            // Loading state - simple
            repeat(3) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .background(Color(0xFF1A1A2E), RoundedCornerShape(16.dp))
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(boostPlans) { plan ->
                    DynamicBoostPlanCard(
                        plan = plan,
                        isPopular = plan.id == "boost_30",
                        onSelect = { onBoost(plan) }
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicBoostPlanCard(
    plan: BoostPlan,
    isPopular: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isPopular) 2.dp else 0.5.dp,
                color = if (isPopular) Color(0xFFD4AF37)
                else Color(0xFF374151),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (isPopular) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFD4AF37).copy(alpha = 0.15f),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text("⭐ Most Popular",
                        modifier = Modifier.padding(
                            horizontal = 12.dp, vertical = 4.dp),
                        color = Color(0xFFD4AF37),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("⚡ ${plan.duration}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(plan.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9CA3AF))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${plan.currency} ${plan.price}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFFD4AF37),
                        fontWeight = FontWeight.Bold)
                    Text("one-time",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6B7280))
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPopular) Color(0xFFD4AF37)
                    else Color(0xFF0F3460)
                )
            ) {
                Text("Boost for ${plan.duration}",
                    color = if (isPopular) Color(0xFF1A1A2E)
                    else Color.White,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}
