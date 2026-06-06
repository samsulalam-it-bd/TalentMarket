package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PremiumGateBottomSheet(
  title: String,
  description: String,
  requiredPlan: String,
  onUpgrade: () -> Unit,
  onDismiss: () -> Unit
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    color = Color(0xFF1A1A2E)
  ) {
    Column(
      modifier = Modifier.padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Drag handle
      Box(
        modifier = Modifier
          .width(40.dp).height(4.dp)
          .background(Color(0xFF374151), CircleShape)
      )
      Spacer(Modifier.height(16.dp))
      
      Text("👑", fontSize = 40.sp)
      Spacer(Modifier.height(8.dp))
      
      Text(title,
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center)
      Spacer(Modifier.height(8.dp))
      
      Text(description,
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF9CA3AF),
        textAlign = TextAlign.Center)
      Spacer(Modifier.height(8.dp))
      
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFD4AF37).copy(alpha = 0.1f)
      ) {
        Text(
          "Required: ${requiredPlan.uppercase()} plan",
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          color = Color(0xFFD4AF37),
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold)
      }
      
      Spacer(Modifier.height(20.dp))
      
      Button(
        onClick = onUpgrade,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color(0xFFD4AF37)
        )
      ) {
        Text("Upgrade Now",
          color = Color(0xFF1A1A2E),
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp)
      }
      Spacer(Modifier.height(8.dp))
      TextButton(onClick = onDismiss,
        modifier = Modifier.fillMaxWidth()) {
        Text("Maybe Later",
          color = Color(0xFF6B7280))
      }
    }
  }
}
