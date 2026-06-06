package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BoostPlan
import com.example.ui.AdminViewModel
import com.example.ui.components.AdminTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBoostPlansScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val boostPlans by adminViewModel.boostPlansState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AdminTopBar(
                title = "⚡ Boost Plans (${boostPlans.size})",
                onBack = onBack
            )
        },
        containerColor = Color(0xFF0F0F1A),
        bottomBar = {
            Surface(
                color = Color(0xFF1E1E30),
                tonalElevation = 8.dp,
                border = BorderStroke(0.5.dp, Color(0xFF374151)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add New Plan",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (boostPlans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Stars,
                        contentDescription = null,
                        tint = Color(0xFF1E1E30),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No plans found. Create one to start!",
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = boostPlans,
                    key = { it.id }
                ) { plan ->
                    EditableBoostPlanCard(
                        plan = plan,
                        onUpdate = { updatedPlan ->
                            adminViewModel.updateBoostPlan(
                                planId = updatedPlan.id,
                                price = updatedPlan.price,
                                duration = updatedPlan.duration,
                                durationDays = updatedPlan.durationDays,
                                label = updatedPlan.label,
                                isActive = updatedPlan.isActive
                            )
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddPlanDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newPlan ->
                adminViewModel.addBoostPlan(newPlan)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun EditableBoostPlanCard(
    plan: BoostPlan,
    onUpdate: (BoostPlan) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // Editable States
    var editPrice by remember(plan) { mutableStateOf(plan.price.toString()) }
    var editDuration by remember(plan) { mutableStateOf(plan.duration) }
    var editDurationDays by remember(plan) { mutableStateOf(plan.durationDays.toString()) }
    var editLabel by remember(plan) { mutableStateOf(plan.label) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (plan.isActive) Color(0xFF1E1E30) else Color(0xFF151525)
        ),
        border = BorderStroke(
            1.dp, 
            if (plan.isActive) Color(0xFF374151) else Color(0xFF2D2D3D)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Main Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = plan.duration,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (plan.label.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color(0xFFF59E0B).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = plan.label,
                                    color = Color(0xFFF59E0B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${plan.price} ${plan.currency} (${plan.durationDays} days)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFE2E8F0)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = plan.isActive,
                        onCheckedChange = { active ->
                            onUpdate(plan.copy(isActive = active))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF10B981),
                            checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.3f),
                            uncheckedThumbColor = Color(0xFF9CA3AF),
                            uncheckedTrackColor = Color(0xFF374151)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.Edit,
                            contentDescription = "Edit Boost Plan",
                            tint = if (isExpanded) Color(0xFF3B82F6) else Color(0xFF9CA3AF)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(color = Color(0xFF374151), modifier = Modifier.padding(bottom = 16.dp))

                    OutlinedTextField(
                        value = editDuration,
                        onValueChange = { editDuration = it },
                        label = { Text("Duration Label") },
                        placeholder = { Text("e.g., 30 Days Budget Plan") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF374151)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editPrice,
                            onValueChange = { editPrice = it },
                            label = { Text("Price (SAR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFF374151)
                            )
                        )

                        OutlinedTextField(
                            value = editDurationDays,
                            onValueChange = { editDurationDays = it },
                            label = { Text("Days Count") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFF374151)
                            )
                        )
                    }

                    OutlinedTextField(
                        value = editLabel,
                        onValueChange = { editLabel = it },
                        label = { Text("Label / Highlight Tag") },
                        placeholder = { Text("e.g. Best Value, Recommended") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF374151)
                        )
                    )

                    Button(
                        onClick = {
                            val priceVal = editPrice.toIntOrNull() ?: plan.price
                            val daysVal = editDurationDays.toIntOrNull() ?: plan.durationDays
                            onUpdate(
                                plan.copy(
                                    duration = editDuration,
                                    price = priceVal,
                                    durationDays = daysVal,
                                    label = editLabel
                                )
                            )
                            isExpanded = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Price Plan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlanDialog(
    onDismiss: () -> Unit,
    onConfirm: (BoostPlan) -> Unit
) {
    var id by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var durationDays by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var productId by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "🆕 Add New Plan",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        containerColor = Color(0xFF1A1A2E),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showError) {
                    Text(
                        text = "Please fill in all mandatory fields with valid values.",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    label = { Text("Unique ID (e.g. boost_super)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF374151)
                    )
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration Screen Label (e.g. 1 Month)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF374151)
                    )
                )

                OutlinedTextField(
                    value = durationDays,
                    onValueChange = { durationDays = it },
                    label = { Text("Days (e.g. 30)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF374151)
                    )
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (SAR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF374151)
                    )
                )

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Tag (e.g. Popular - optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF374151)
                    )
                )

                OutlinedTextField(
                    value = productId,
                    onValueChange = { productId = it },
                    label = { Text("Play Store Product ID (optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF374151)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toIntOrNull()
                    val d = durationDays.toIntOrNull()
                    if (id.isBlank() || duration.isBlank() || p == null || d == null) {
                        showError = true
                    } else {
                        onConfirm(
                            BoostPlan(
                                id = id.trim(),
                                duration = duration.trim(),
                                durationDays = d,
                                price = p,
                                label = label.trim(),
                                isActive = true,
                                productId = productId.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) {
                Text("Add Plan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563))
            ) {
                Text("Cancel")
            }
        }
    )
}
