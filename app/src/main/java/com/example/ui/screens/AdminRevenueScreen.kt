package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AdminViewModel
import com.example.ui.BoostPurchase
import com.example.ui.components.AdminTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRevenueScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val purchases by adminViewModel.revenue.collectAsState()
    
    val totalRevenue = remember(purchases) {
        purchases.sumOf { it.amount }
    }
    
    val trailing30DaysRevenue = remember(purchases) {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        purchases.filter { it.purchasedAt >= thirtyDaysAgo }.sumOf { it.amount }
    }

    Scaffold(
        topBar = {
            AdminTopBar(
                title = "💰 Revenue",
                onBack = onBack
            )
        },
        containerColor = Color(0xFF0F0F1A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stat Cards Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Revenue Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E30)),
                    border = BorderStroke(1.dp, Color(0xFF374151))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Total Revenue",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF9CA3AF)
                        )
                        Text(
                            text = "$totalRevenue SAR",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // This Month Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E30)),
                    border = BorderStroke(1.dp, Color(0xFF374151))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "This Month",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF9CA3AF)
                        )
                        Text(
                            text = "$trailing30DaysRevenue SAR",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Section banner
            Text(
                text = "Transaction History (${purchases.size})",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (purchases.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFF374151),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No purchase records found",
                            color = Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = purchases,
                        key = { it.id }
                    ) { purchase ->
                        RevenueItemCard(purchase = purchase)
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueItemCard(purchase: BoostPurchase) {
    val context = LocalContext.current
    
    val timeAgo = remember(purchase.purchasedAt) {
        if (purchase.purchasedAt == 0L) "Unknown time"
        else DateUtils.getRelativeTimeSpanString(
            purchase.purchasedAt,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161626)),
        border = BorderStroke(0.5.dp, Color(0xFF2E2E42))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Orange circle avatar with ⚡ emoji
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚡",
                    fontSize = 18.sp
                )
            }

            // Buyer Name / Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (purchase.userName.isBlank()) "Anonymous User" else purchase.userName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF334155))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = purchase.planId.uppercase(),
                            color = Color(0xFFD1D5DB),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = timeAgo,
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Price tags
            Text(
                text = "+${purchase.amount} SAR",
                color = Color(0xFF10B981),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
