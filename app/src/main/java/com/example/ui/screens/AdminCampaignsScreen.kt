package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AdminViewModel
import com.example.ui.components.AdminTopBar
import com.example.utils.TimeUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCampaignsScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val allJobs by adminViewModel.allJobs.collectAsState()
    val allWorkers by adminViewModel.allWorkers.collectAsState()
    val adPlacements by adminViewModel.adPlacementsAdmin.collectAsState()

    val boostedJobs = allJobs.filter { it.isBoosted }
    val boostedWorkers = allWorkers.filter { it.isBoosted }

    var selectedTab by remember { mutableStateOf(0) } // 0 -> Jobs, 1 -> Workers, 2 -> Custom Sponsor Ads
    val tabs = listOf(
        "Boosted Jobs (${boostedJobs.size})",
        "Boosted Workers (${boostedWorkers.size})",
        "Custom Sponsor Ads (${adPlacements.size})"
    )
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AdminTopBar("🚀 Active Campaigns", onBack = onBack)
        },
        containerColor = Color(0xFF0F0F1A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E1E2F),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            if (selectedTab == 0) {
                if (boostedJobs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active job campaigns.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(boostedJobs) { job ->
                            AdminCampaignCard(
                                title = job.title,
                                details = "Company: ${job.companyName}\nCategory: ${job.category}",
                                boostEndDate = job.boostEndDate,
                                onRemoveBoost = {
                                    adminViewModel.pinJob(job.id, false)
                                    scope.launch { snackbarHostState.showSnackbar("Job campaign removed.") }
                                }
                            )
                        }
                    }
                }
            } else if (selectedTab == 1) {
                if (boostedWorkers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active worker campaigns.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(boostedWorkers) { worker ->
                            AdminCampaignCard(
                                title = worker.name,
                                details = "Profession: ${worker.profession}\nCountry: ${worker.country}",
                                boostEndDate = worker.boostEndDate,
                                onRemoveBoost = {
                                    adminViewModel.pinWorker(worker.id, false)
                                    scope.launch { snackbarHostState.showSnackbar("Worker campaign removed.") }
                                }
                            )
                        }
                    }
                }
            } else {
                // Tab 2: Custom Sponsor Fallback Ads
                var showAddAdDialog by remember { mutableStateOf(false) }
                var editingAd by remember { mutableStateOf<com.example.data.AdPlacement?>(null) }

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fallback Sponsorships", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Button(
                            onClick = { 
                                editingAd = null
                                showAddAdDialog = true 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Text("+ Create Placement", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (adPlacements.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No custom fallback ads configured yet.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1.0f)
                        ) {
                            items(adPlacements) { ad ->
                                AdminAdCard(
                                    ad = ad,
                                    onEdit = {
                                        editingAd = ad
                                        showAddAdDialog = true
                                    },
                                    onDelete = {
                                        adminViewModel.deleteAdPlacement(ad.id)
                                        scope.launch { snackbarHostState.showSnackbar("Ad Placement deleted successfully.") }
                                    }
                                )
                            }
                        }
                    }
                }

                if (showAddAdDialog) {
                    AdFormDialog(
                        ad = editingAd,
                        onDismiss = { showAddAdDialog = false },
                        onSave = { updatedAd ->
                            adminViewModel.saveAdPlacement(updatedAd)
                            showAddAdDialog = false
                            scope.launch { snackbarHostState.showSnackbar("Ad campaign configuration saved.") }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminCampaignCard(
    title: String,
    details: String,
    boostEndDate: Long,
    onRemoveBoost: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2F)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(details, color = Color(0xFF9CA3AF), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            val activeText = if (boostEndDate > System.currentTimeMillis()) {
                "Active until ${TimeUtils.formatDate(boostEndDate)}"
            } else if (boostEndDate <= 0L) {
                "Pinned by Admin (Indefinitely)"
            } else {
                "Expired on ${TimeUtils.formatDate(boostEndDate)}"
            }
            
            Text(
                activeText, 
                color = if (boostEndDate > System.currentTimeMillis() || boostEndDate == 0L) Color(0xFF10B981) else Color(0xFFEF4444),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = onRemoveBoost,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Stop Campaign", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AdminAdCard(
    ad: com.example.data.AdPlacement,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2F)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(ad.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1.0f))
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Priority: ${ad.priority}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(ad.subtitle, color = Color(0xFF9CA3AF), fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("👀 Views: ${ad.viewsCount}", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("🎯 Clicks: ${ad.clicksCount}", color = Color(0xFF60A5FA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("🔗 Destination: ${ad.targetType.replace("_", " ").uppercase()}", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Text("Edit Config", color = Color(0xFF60A5FA), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                ) {
                    Text("Delete", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AdFormDialog(
    ad: com.example.data.AdPlacement?,
    onDismiss: () -> Unit,
    onSave: (com.example.data.AdPlacement) -> Unit
) {
    var title by remember { mutableStateOf(ad?.title ?: "") }
    var subtitle by remember { mutableStateOf(ad?.subtitle ?: "") }
    var actionText by remember { mutableStateOf(ad?.actionText ?: "") }
    var targetType by remember { mutableStateOf(ad?.targetType ?: "premium_subscription") }
    var targetUrl by remember { mutableStateOf(ad?.targetUrl ?: "") }
    var priority by remember { mutableStateOf((ad?.priority ?: 10).toString()) }
    var customGradientStart by remember { mutableStateOf(ad?.customGradientStart ?: "#7C3AED") }
    var customGradientEnd by remember { mutableStateOf(ad?.customGradientEnd ?: "#DB2777") }
    var isActive by remember { mutableStateOf(ad?.isActive ?: true) }

    val scrollState = androidx.compose.foundation.rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ad == null) "🚀 Create Ad Placement" else "✏️ Edit Ad Placement", color = Color.White) },
        containerColor = Color(0xFF151522),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Ad Title (bold)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Ad Subtitle (description)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = actionText,
                    onValueChange = { actionText = it },
                    label = { Text("CTA Button Text (e.g. Learn More)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                // Target Type selection
                Text("Navigation Destination Action:", color = Color.Gray, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf("premium_subscription", "job_boost", "external_url")
                    types.forEach { type ->
                        val isSelected = targetType == type
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E1E2F),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { targetType = type }
                        ) {
                            Text(
                                text = type.substringBefore("_").uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                if (targetType == "external_url") {
                    OutlinedTextField(
                        value = targetUrl,
                        onValueChange = { targetUrl = it },
                        label = { Text("External URL (https://...)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = { Text("Priority Order (lower is higher, e.g. 10)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customGradientStart,
                        onValueChange = { customGradientStart = it },
                        label = { Text("Gradient Start") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = customGradientEnd,
                        onValueChange = { customGradientEnd = it },
                        label = { Text("Gradient End") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("Ad is currently Active & Visible", color = Color.White, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            com.example.data.AdPlacement(
                                id = ad?.id ?: "",
                                title = title,
                                subtitle = subtitle,
                                actionText = actionText,
                                targetType = targetType,
                                targetUrl = targetUrl,
                                priority = priority.toIntOrNull() ?: 10,
                                customGradientStart = customGradientStart,
                                customGradientEnd = customGradientEnd,
                                isActive = isActive,
                                clicksCount = ad?.clicksCount ?: 0,
                                viewsCount = ad?.viewsCount ?: 0
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
