package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.JobApplication
import com.example.ui.TalentViewModel
import com.example.ui.components.getTimeAgo

@Composable
fun JobApplicantsScreen(
  jobId: String,
  jobTitle: String,
  talentViewModel: TalentViewModel,
  chatViewModel: com.example.ui.ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
  navController: NavController,
  onBack: () -> Unit
) {
  val applications by talentViewModel
    .jobApplications.collectAsState()
  val context = androidx.compose.ui.platform.LocalContext.current

  LaunchedEffect(jobId) {
    talentViewModel.loadJobApplications(jobId)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F0F1A))
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
          Icon(Icons.Rounded.ArrowBack, null,
            tint = Color.White)
        }
        Column {
          Text(
            "Applicants",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
          Text(
            Uri.decode(jobTitle),
            color = Color(0xFF9CA3AF),
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    }

    if (applications.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text("👥", fontSize = 56.sp)
          Text(
            "No applicants yet",
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      }
    } else {
      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(applications, key = { it.id }) { app ->
          ApplicantCard(
            application = app,
            onUpdateStatus = { newStatus ->
              talentViewModel.updateApplicationStatus(
                applicationId = app.id,
                newStatus = newStatus,
                workerId = app.workerId,
                jobTitle = Uri.decode(app.jobTitle)
              )
            },
            onChatClick = {
              chatViewModel.startChat(
                otherUserId = app.workerId,
                otherUserName = app.workerName,
                otherUserPhoto = app.workerPhotoUrl
              ) { roomId ->
                navController.navigate(com.example.ui.Screen.ChatDetail.createRoute(roomId, app.workerId, app.workerName))
              }
            }
          )
        }
      }
    }
  }
}

@Composable
fun ApplicantCard(
  application: JobApplication,
  onUpdateStatus: (String) -> Unit,
  onChatClick: () -> Unit
) {
  var showStatusMenu by remember { mutableStateOf(false) }
  val statusColor = when(application.status) {
    "shortlisted" -> Color(0xFF60A5FA)
    "interviewing" -> Color(0xFFD4AF37)
    "hired" -> Color(0xFF10B981)
    "rejected" -> Color(0xFFEF4444)
    else -> Color(0xFF9CA3AF)
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFF1A1A2E)
    ),
    border = BorderStroke(0.5.dp, Color(0xFF374151))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .background(Color(0xFF0F3460), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              application.workerName
                .firstOrNull()?.uppercase() ?: "W",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
          }
          Column {
            Text(
              application.workerName,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleSmall
            )
            Text(
              getTimeAgo(application.appliedAt),
              color = Color(0xFF6B7280),
              style = MaterialTheme.typography.labelSmall
            )
          }
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          IconButton(
            onClick = onChatClick,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.iconButtonColors(
              containerColor = Color(0xFF10B981).copy(alpha = 0.15f),
              contentColor = Color(0xFF10B981)
            )
          ) {
            Icon(
              imageVector = Icons.Default.Send,
              contentDescription = "Chat",
              modifier = Modifier.size(16.dp)
            )
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = statusColor.copy(0.15f)
          ) {
            Text(
              application.status.replaceFirstChar { 
                it.uppercase() 
              },
              modifier = Modifier.padding(
                horizontal = 10.dp, vertical = 4.dp
              ),
              color = statusColor,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(Modifier.height(12.dp))
      HorizontalDivider(color = Color(0xFF374151), thickness = 0.5.dp)
      Spacer(Modifier.height(12.dp))

      Text(
        "Update Status",
        color = Color(0xFF9CA3AF),
        style = MaterialTheme.typography.labelSmall
      )
      Spacer(Modifier.height(8.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(
          "shortlisted" to "Shortlist",
          "interviewing" to "Interview",
          "hired" to "Hire ✓",
          "rejected" to "Reject"
        ).forEach { (status, label) ->
          val isSelected = application.status == status
          val btnColor = when(status) {
            "hired" -> Color(0xFF10B981)
            "rejected" -> Color(0xFFEF4444)
            "interviewing" -> Color(0xFFD4AF37)
            else -> Color(0xFF60A5FA)
          }
          OutlinedButton(
            onClick = { onUpdateStatus(status) },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(
              if (isSelected) 1.5.dp else 0.5.dp,
              btnColor
            ),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = if (isSelected)
                btnColor.copy(0.15f)
              else Color.Transparent,
              contentColor = btnColor
            ),
            contentPadding = PaddingValues(
              horizontal = 12.dp, vertical = 6.dp
            )
          ) {
            Text(label, fontSize = 11.sp,
              fontWeight = if (isSelected)
                FontWeight.Bold else FontWeight.Normal)
          }
        }
      }
    }
  }
}
