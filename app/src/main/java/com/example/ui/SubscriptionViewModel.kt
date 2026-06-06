package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubscriptionState(
  val plan: String = "free",
  val isActive: Boolean = false,
  val endDate: Long = 0,
  val jobApplicationsThisMonth: Int = 0,
  val jobPostsThisMonth: Int = 0
)

class SubscriptionViewModel : ViewModel() {
  
  private val _state = MutableStateFlow(SubscriptionState())
  val state = _state.asStateFlow()
  
  fun loadSubscription(userId: String) {
    try {
      FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .addSnapshotListener { snapshot, _ ->
          val data = snapshot?.data ?: return@addSnapshotListener
          val sub = data["subscription"] as? Map<*, *>
          _state.value = SubscriptionState(
            plan = sub?.get("plan") as? String ?: "free",
            isActive = sub?.get("isActive") as? Boolean ?: false,
            endDate = sub?.get("endDate") as? Long ?: 0
          )
        }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
  
  fun canApplyToJob(): Boolean {
    return when (_state.value.plan) {
      "free" -> _state.value.jobApplicationsThisMonth < 5
      else -> true
    }
  }
  
  fun canPostJob(): Boolean {
    return when (_state.value.plan) {
      "free" -> _state.value.jobPostsThisMonth < 2
      "business" -> _state.value.jobPostsThisMonth < 20
      else -> true
    }
  }
  
  fun isPremium(): Boolean = _state.value.plan != "free"
  
  fun getPlanBadge(): String = when (_state.value.plan) {
    "silver" -> "🥈"
    "gold" -> "👑"
    "business" -> "✅"
    "enterprise" -> "💎"
    else -> ""
  }
}
