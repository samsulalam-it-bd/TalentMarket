package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PremiumBadge(plan: String, size: Dp = 20.dp) {
  if (plan == "free" || plan.isEmpty()) return
  
  val (emoji, bgColor) = when (plan) {
    "silver" -> Pair("🥈", Color(0xFF6B7280))
    "gold" -> Pair("👑", Color(0xFFD4AF37))
    "business" -> Pair("✅", Color(0xFF0F3460))
    "enterprise" -> Pair("💎", Color(0xFF4C1D95))
    else -> return
  }
  
  Surface(
    shape = CircleShape,
    color = bgColor.copy(alpha = 0.2f),
    modifier = Modifier.size(size + 8.dp)
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(emoji, fontSize = (size.value * 0.7f).sp)
    }
  }
}
