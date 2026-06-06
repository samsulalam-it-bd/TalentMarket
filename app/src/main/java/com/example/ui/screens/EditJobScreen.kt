package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TalentViewModel
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditJobScreen(
    jobId: String,
    talentViewModel: TalentViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(jobId) {
        try {
            FirebaseFirestore.getInstance()
                .collection("jobs")
                .document(jobId)
                .get()
                .addOnSuccessListener { doc ->
                    title = doc.getString("title") ?: ""
                    description = doc.getString("description") ?: ""
                    salary = doc.getString("salary") ?: (doc.getLong("salary") ?: 0L).toString()
                    location = doc.getString("location") ?: ""
                    category = doc.getString("category") ?: ""
                    val req = doc.get("requirements")
                    requirements = if (req is String) req else ""
                    isLoading = false
                }
                .addOnFailureListener {
                    com.example.ui.FirestoreErrorHandler.handleError(it, "EditJob")
                    isLoading = false
                }
        } catch (e: Exception) {
            com.example.ui.FirestoreErrorHandler.handleError(e, "EditJob")
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0F3460), Color(0xFF1A1A2E))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    "Edit Job Post",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFD4AF37))
            }
        } else {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Job Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD4AF37),
                        focusedLabelColor = Color(0xFFD4AF37),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    minLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD4AF37),
                        focusedLabelColor = Color(0xFFD4AF37),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("Salary (SAR)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD4AF37),
                        focusedLabelColor = Color(0xFFD4AF37),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD4AF37),
                        focusedLabelColor = Color(0xFFD4AF37),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = requirements,
                    onValueChange = { requirements = it },
                    label = { Text("Requirements") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFD4AF37),
                        focusedLabelColor = Color(0xFFD4AF37),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    )
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (title.isNotEmpty() && description.isNotEmpty()) {
                            isSaving = true
                            val updatedData = mapOf(
                                "title" to title,
                                "description" to description,
                                "salary" to salary,
                                "location" to location,
                                "category" to category,
                                "requirements" to requirements,
                                "updatedAt" to System.currentTimeMillis()
                            )
                            talentViewModel.updateJob(jobId, updatedData)
                            isSaving = false
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = title.isNotEmpty() && !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37),
                        disabledContainerColor = Color(0xFF374151)
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF1A1A2E),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Save Changes",
                            color = Color(0xFF1A1A2E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
