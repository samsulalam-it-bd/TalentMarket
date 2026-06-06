package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.R
import com.example.ui.TalentViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(viewModel: TalentViewModel, navController: NavController) {
    val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    val user = auth?.currentUser
    val localContext = LocalContext.current

    val initialEmail = user?.email ?: "guest@talentmarket.com"
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(initialEmail) }

    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.popBackStack()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Request Submitted",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Your message has been sent. We\'ll reply within 24 hours.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.support_screen_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Hero Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Support Email Icon",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "How can we help?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Send us a query and our team will support you as soon as possible.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Input Fields Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Email (Auto-filled)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { if (viewModel.isGuest) email = it },
                        label = { Text("Your Email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email Icon"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = viewModel.isGuest, // Only direct input if guests are writing support
                        colors = if (!viewModel.isGuest) {
                            OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            OutlinedTextFieldDefaults.colors()
                        }
                    )

                    // Subject Field
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { 
                            subject = it
                            errorMessage = null 
                        },
                        label = { Text(stringResource(R.string.subject)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Subject,
                                contentDescription = "Subject Icon"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("What is this query about?") }
                    )

                    // Message Field
                    OutlinedTextField(
                        value = message,
                        onValueChange = { 
                            message = it
                            errorMessage = null 
                        },
                        label = { Text(stringResource(R.string.message)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = "Message Icon"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        minLines = 4,
                        placeholder = { Text("Describe your support query or issue in detail...") }
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Submit Send Button
                    Button(
                        onClick = {
                            if (email.trim().isEmpty()) {
                                errorMessage = "Please enter your email address."
                            } else if (subject.trim().isEmpty()) {
                                errorMessage = "Please enter a subject."
                            } else if (message.trim().isEmpty()) {
                                errorMessage = "Please enter a message details."
                            } else {
                                isSubmitting = true
                                viewModel.sendSupportRequest(subject, message) { success ->
                                    isSubmitting = false
                                    if (success) {
                                        showSuccessDialog = true
                                    } else {
                                        errorMessage = "Internal connection failed. Please try again."
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp).padding(end = 8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sending...")
                        } else {
                            Text(stringResource(R.string.send), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
