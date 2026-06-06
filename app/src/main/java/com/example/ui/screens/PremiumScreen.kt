package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp

@Composable
fun PremiumScreen(
  currentPlan: String,
  onSubscribe: (String) -> Unit,
  onBack: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) } // 0=Worker, 1=Company
  
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F0F1A))
  ) {
    // Header
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            listOf(Color(0xFF0F3460), Color(0xFF0F0F1A))
          )
        )
        .padding(24.dp)
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onBack,
          modifier = Modifier.align(Alignment.Start)) {
          Icon(Icons.Rounded.ArrowBack, null, tint = Color.White)
        }
        Text("👑", fontSize = 48.sp)
        Spacer(Modifier.height(8.dp))
        Text("Upgrade Your Plan",
          style = MaterialTheme.typography.headlineMedium,
          color = Color.White,
          fontWeight = FontWeight.Bold)
        Text("Unlock premium features",
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFF9CA3AF))
      }
    }
    
    // Tab selector
    Row(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
        .background(Color(0xFF1A1A2E), RoundedCornerShape(12.dp))
        .padding(4.dp)
    ) {
      listOf("For Workers", "For Companies").forEachIndexed { i, tab ->
        Box(
          modifier = Modifier
            .weight(1f)
            .background(
              if (selectedTab == i) Color(0xFFD4AF37) 
              else Color.Transparent,
              RoundedCornerShape(10.dp)
            )
            .clickable { selectedTab = i }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(tab,
            color = if (selectedTab == i) Color(0xFF1A1A2E) 
                    else Color(0xFF9CA3AF),
            fontWeight = if (selectedTab == i) FontWeight.Bold 
                         else FontWeight.Normal,
            fontSize = 14.sp)
        }
      }
    }
    
    // Plans
    LazyColumn(
      modifier = Modifier.padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      val plans = if (selectedTab == 0) workerPlans else companyPlans
      items(plans) { plan ->
        PremiumPlanCard(
          plan = plan,
          isCurrentPlan = (plan.id.contains(currentPlan ?: "none", ignoreCase=true)) || (currentPlan == "free" && plan.id == "free"),
          onSubscribe = { onSubscribe(plan.id) }
        )
      }
      item { Spacer(Modifier.height(16.dp)) }
      item {
        Text(
          "✓ Cancel anytime  ✓ Secure payment  ✓ Instant activation",
          style = MaterialTheme.typography.labelMedium,
          color = Color(0xFF6B7280),
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

data class PremiumPlan(
  val id: String,
  val name: String,
  val price: String,
  val period: String,
  val features: List<String>,
  val badge: String,
  val isPopular: Boolean = false,
  val color: Color
)

val workerPlans = listOf(
  PremiumPlan(
    id = "free",
    name = "Free",
    price = "SAR 0",
    period = "forever",
    features = listOf(
      "Apply to 5 jobs/month",
      "Basic profile",
      "Browse all jobs"
    ),
    badge = "",
    color = Color(0xFF374151)
  ),
  PremiumPlan(
    id = com.example.billing.BillingManager.PLAN_SILVER,
    name = "Silver",
    price = "SAR 19",
    period = "per month",
    features = listOf(
      "Unlimited job applications",
      "🥈 Silver badge on profile",
      "See who viewed your profile",
      "Priority in search results",
      "Advanced filters"
    ),
    badge = "🥈",
    color = Color(0xFF6B7280)
  ),
  PremiumPlan(
    id = com.example.billing.BillingManager.PLAN_GOLD,
    name = "Gold",
    price = "SAR 39",
    period = "per month",
    features = listOf(
      "Everything in Silver",
      "👑 Gold badge on profile",
      "Direct message companies",
      "Featured profile placement",
      "Urgent job alerts",
      "Resume highlighted"
    ),
    badge = "👑",
    isPopular = true,
    color = Color(0xFFD4AF37)
  )
)

val companyPlans = listOf(
  PremiumPlan(
    id = "free",
    name = "Free",
    price = "SAR 0",
    period = "forever",
    features = listOf(
      "Post 2 jobs/month",
      "Basic company profile",
      "Standard listing"
    ),
    badge = "",
    color = Color(0xFF374151)
  ),
  PremiumPlan(
    id = com.example.billing.BillingManager.PLAN_BUSINESS,
    name = "Business",
    price = "SAR 99",
    period = "per month",
    features = listOf(
      "Post 20 jobs/month",
      "✅ Verified company badge",
      "Boost 3 job posts to top",
      "See full applicant profiles",
      "Job analytics dashboard",
      "Advanced worker search"
    ),
    badge = "✅",
    isPopular = true,
    color = Color(0xFF0F3460)
  ),
  PremiumPlan(
    id = com.example.billing.BillingManager.PLAN_ENTERPRISE,
    name = "Enterprise",
    price = "SAR 299",
    period = "per month",
    features = listOf(
      "Unlimited job posts",
      "💎 Diamond badge",
      "Featured on homepage",
      "Bulk worker search",
      "Dedicated support",
      "Custom branding"
    ),
    badge = "💎",
    color = Color(0xFF4C1D95)
  )
)

@Composable
fun PremiumPlanCard(
  plan: PremiumPlan,
  isCurrentPlan: Boolean,
  onSubscribe: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(
        width = if (isCurrentPlan || plan.isPopular) 2.dp else 0.5.dp,
        color = if (isCurrentPlan) Color(0xFFD4AF37)
                else if (plan.isPopular) plan.color
                else Color(0xFF374151),
        shape = RoundedCornerShape(16.dp)
      ),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFF1A1A2E)
    )
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      
      // Popular badge
      if (plan.isPopular) {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = plan.color.copy(alpha = 0.2f),
          modifier = Modifier.padding(bottom = 8.dp)
        ) {
          Text("⭐ Most Popular",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = plan.color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold)
        }
      }
      
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          if (plan.badge.isNotEmpty()) {
            Text(plan.badge, fontSize = 24.sp)
          }
          Text(plan.name,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(plan.price,
            style = MaterialTheme.typography.titleMedium,
            color = plan.color,
            fontWeight = FontWeight.Bold)
          Text(plan.period,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF6B7280))
        }
      }
      
      Spacer(Modifier.height(16.dp))
      HorizontalDivider(color = Color(0xFF374151), thickness = 0.5.dp)
      Spacer(Modifier.height(12.dp))
      
      plan.features.forEach { feature ->
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.padding(vertical = 3.dp)
        ) {
          Text("✓", color = Color(0xFF10B981), fontSize = 14.sp)
          Text(feature,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFD1D5DB))
        }
      }
      
      Spacer(Modifier.height(16.dp))
      
      if (isCurrentPlan) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF374151)
        ) {
          Text("Current Plan",
            modifier = Modifier.padding(vertical = 14.dp),
            textAlign = TextAlign.Center,
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Medium)
        }
      } else if (plan.id != "free") {
        Button(
          onClick = onSubscribe,
          modifier = Modifier.fillMaxWidth().height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = plan.color
          )
        ) {
          Text("Subscribe Now",
            color = if (plan.color == Color(0xFFD4AF37)) 
                      Color(0xFF1A1A2E) 
                    else Color.White,
            fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
