package com.example.ui.screens

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
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.JobApplication
import com.example.ui.TalentViewModel
import com.example.ui.components.getTimeAgo

import androidx.navigation.NavController

@Composable
fun MyApplicationsScreen(
  talentViewModel: TalentViewModel,
  navController: NavController,
  onBack: () -> Unit
) {
  val applications by talentViewModel
    .myApplications.collectAsState()

  LaunchedEffect(Unit) {
    talentViewModel.loadMyApplications()
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
          Icon(
            Icons.Rounded.ArrowBack, null,
            tint = Color.White
          )
        }
        Text(
          stringResource(R.string.my_applications),
          style = MaterialTheme.typography.headlineSmall,
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
      }
    }

    if (applications.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
            EmptyStateView(
                emoji = "📋",
                title = "No Applications Yet",
                subtitle = "Start applying for jobs\nto track them here",
                actionLabel = "Find Jobs",
                onAction = { navController.navigate("jobs") }
            )
      }
    } else {
      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(applications, key = { it.id }) { app ->
          ApplicationStatusCard(application = app)
        }
      }
    }
  }
}

@Composable
fun ApplicationStatusCard(application: JobApplication) {
  val statusColor = when(application.status) {
    "shortlisted" -> Color(0xFF60A5FA)
    "interviewing" -> Color(0xFFD4AF37)
    "hired" -> Color(0xFF10B981)
    "rejected" -> Color(0xFFEF4444)
    else -> Color(0xFF9CA3AF)
  }
  val statusLabel = when(application.status) {
    "applied" -> "Applied"
    "shortlisted" -> "Shortlisted ⭐"
    "interviewing" -> "Interview \uD83D\uDCC5"
    "hired" -> "Hired \uD83C\uDF89"
    "rejected" -> "Not Selected"
    else -> application.status
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFF1A1A2E)
    ),
    border = BorderStroke(0.5.dp, statusColor.copy(0.4f))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          application.jobTitle,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.titleSmall,
          modifier = Modifier.weight(1f)
        )
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = statusColor.copy(0.15f)
        ) {
          Text(
            statusLabel,
            modifier = Modifier.padding(
              horizontal = 10.dp, vertical = 4.dp
            ),
            color = statusColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
      Spacer(Modifier.height(8.dp))
      
      // Progress bar
      val steps = listOf(
        "applied", "shortlisted", 
        "interviewing", "hired"
      )
      val currentStep = steps.indexOf(application.status)
        .coerceAtLeast(0)
      
      if (application.status != "rejected") {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          steps.forEachIndexed { index, _ ->
            Box(
              modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .background(
                  if (index <= currentStep) statusColor
                  else Color(0xFF374151),
                  RoundedCornerShape(2.dp)
                )
            )
          }
        }
        Spacer(Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          listOf("Applied","Shortlisted",
            "Interview","Hired").forEach { label ->
            Text(
              label,
              color = Color(0xFF6B7280),
              fontSize = 9.sp
            )
          }
        }
      }
      
      Spacer(Modifier.height(8.dp))
      Text(
        getTimeAgo(application.appliedAt),
        color = Color(0xFF6B7280),
        style = MaterialTheme.typography.labelSmall
      )
    }
  }
}
