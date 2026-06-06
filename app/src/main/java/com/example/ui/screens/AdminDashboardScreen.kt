package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AdminSection
import com.example.ui.AdminViewModel
import com.example.ui.components.AdminStatCard
import com.example.ui.components.AdminSectionCard
import com.example.ui.components.AdminTopBar
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.SecurityLog
import com.example.utils.TimeUtils

@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
    ) {
        AdminTopBar("🛡️ Admin Panel")
        
        val stats by adminViewModel.stats.collectAsState()
        val isLoadingStats by adminViewModel.isLoadingStats.collectAsState()
        val securityLogs by adminViewModel.securityLogs.collectAsState()
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats header
            item(span = { GridItemSpan(6) }) {
                Text(
                    text = "System Overview",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (isLoadingStats) {
                // Shimmer loading stats widgets
                repeat(6) {
                    item(span = { GridItemSpan(2) }) {
                        ShimmerStatCard()
                    }
                }
            } else {
                // Row 1: Users Breakdown
                item(span = { GridItemSpan(2) }) {
                    AdminStatCard("Users", stats.totalUsers.toString(), Color(0xFF3B82F6))
                }
                item(span = { GridItemSpan(2) }) {
                    AdminStatCard("Workers", stats.totalWorkers.toString(), Color(0xFF8B5CF6))
                }
                item(span = { GridItemSpan(2) }) {
                    AdminStatCard("Employers", stats.totalEmployers.toString(), Color(0xFFEAB308))
                }
                
                // Row 2: User Status
                item(span = { GridItemSpan(2) }) {
                    AdminStatCard("Verified", stats.verifiedUsers.toString(), Color(0xFF10B981))
                }
                item(span = { GridItemSpan(2) }) {
                    AdminStatCard("Banned", stats.bannedUsers.toString(), Color(0xFFEF4444))
                }
                item(span = { GridItemSpan(2) }) {
                    AdminStatCard("Campaigns", stats.activeCampaigns.toString(), Color(0xFFEC4899))
                }
                
                // Row 3: Platform Metrics
                item(span = { GridItemSpan(2) }) {
                    AdminStatCard("Total Jobs", stats.totalJobs.toString(), Color(0xFF14B8A6))
                }
                item(span = { GridItemSpan(2) }) {
                    AdminStatCard("Tickets", stats.pendingTickets.toString(), Color(0xFFF97316)) // orange
                }
                item(span = { GridItemSpan(2) }) {
                    AdminStatCard("Reports", stats.pendingReports.toString(), Color(0xFFDC2626)) // red
                }

                // Row 4: Revenue (Full Width)
                item(span = { GridItemSpan(6) }) {
                    AdminStatCard("Total Revenue", "${stats.totalRevenue} SAR", Color(0xFFF59E0B))
                }
            }
            
            // Sections header
            item(span = { GridItemSpan(6) }) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Management",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 10 management sections
            val sections = listOf(
                AdminSection("👥", "Users", "Manage user database", "admin_users"),
                AdminSection("💼", "Jobs", "Moderate job posts", "admin_jobs"),
                AdminSection("👤", "Workers", "Manage worker profiles", "admin_workers"),
                AdminSection("🚩", "Reported Posts", "Manage flagged content", "admin_reports"),
                AdminSection("⚡", "Boost Plans", "Configure pricing & plans", "admin_boost_plans"),
                AdminSection("📁", "Categories", "Manage job categories", "admin_categories"),
                AdminSection("🔔", "Notifications", "Send broad notifications", "admin_notifications"),
                AdminSection("💬", "Support", "Respond to support chat", "support_admin"),
                AdminSection("💰", "Revenue", "View transaction history", "admin_revenue"),
                AdminSection("📝", "Action Logs", "Audit admin actions", "admin_logs"),
                AdminSection("🚀", "Campaigns", "Active boosted posts", "admin_campaigns"),
                AdminSection("⚙️", "App Settings", "System configuration", "admin_settings")
            )
            
            items(
                count = sections.size,
                span = { GridItemSpan(2) } // Each card spans 2 out of 6 columns (making 3 columns total per row)
            ) { index ->
                val section = sections[index]
                AdminSectionCard(
                    section = section,
                    onClick = { onNavigate(section.route) }
                )
            }

            // Security Logs Header
            item(span = { GridItemSpan(6) }) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "🛡️ Recent Security Action Logs",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            if (securityLogs.isEmpty()) {
                item(span = { GridItemSpan(6) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A1A2E), RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "No security events recorded.",
                            color = Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(
                    count = minOf(securityLogs.size, 10), // Limit to 10 logs
                    span = { GridItemSpan(6) }
                ) { index ->
                    val log = securityLogs[index]
                    SecurityLogItemCard(log)
                }
            }
        }
    }
}

@Composable
fun ShimmerStatCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2F).copy(alpha = alpha)
        ),
        border = BorderStroke(0.5.dp, Color(0xFF374151).copy(alpha = alpha))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(12.dp)
                    .background(Color(0xFF374151), RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(24.dp)
                    .background(Color(0xFF374151), RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun SecurityLogItemCard(log: SecurityLog) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
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
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.action,
                    color = when {
                        log.action.contains("BAN") -> Color(0xFFEF4444) // Red for Bans
                        log.action.contains("DELETE") -> Color(0xFFF59E0B) // Amber for Deletions
                        else -> Color(0xFF10B981) // Green for Verifications
                    },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "ID: ${log.targetId}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "By: ${log.adminEmail}",
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            Text(
                text = TimeUtils.getRelativeTime(log.timestamp),
                color = Color(0xFF6B7280),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
