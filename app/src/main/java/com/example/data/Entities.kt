package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "jobs")
data class Job(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "",
    val category: String = "",
    val categories: List<String> = emptyList(), // MULTIPLE
    val country: String = "",
    val city: String = "",
    val location: String = "",
    val salary: String = "",
    val experience: String = "No Experience",
    val description: String = "",
    val contact: String = "",
    val jobType: String = "", // Full Time, Part Time
    val budget: String = "",
    val deadline: String = "",
    var isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val expiryDate: Long = 0L,
    val imageUrl: String = "",
    val experienceLevel: String = "No Experience",
    val education: String = "Any",
    val nationality: String = "Any",
    val gender: String = "Any",
    val companyType: String = "Private",
    val viewsCount: Int = 0,
    val applicantsCount: Int = 0,
    val isBoosted: Boolean = false,
    val boostEndDate: Long = 0L,
    val isApproved: Boolean = true,
    val companyName: String = "",
    val isDeactivated: Boolean = false,
    val reportsCount: Int = 0,
    val reports: List<Map<String, Any>> = emptyList()
) : Serializable

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String = "",
    val categories: List<String> = emptyList(), // MULTIPLE
    val profession: String = "",
    val skills: List<String> = emptyList(),
    val country: String = "",
    val city: String = "",
    val location: String = "",
    val salary: String = "0",
    val contact: String = "",
    val availability: String = "", // Available Now, Full Time, Part Time, Contract
    
    // Extended Profile Info
    val experience: String = "",
    val resumeLink: String = "",
    val photoUrl: String = "",
    val gender: String = "Any",
    val education: String = "Any",
    val rating: Int = 0,

    // Portfolio Info
    val portfolioImages: String = "", // Could be comma separated URLs
    val portfolioWebsite: String = "",
    val portfolioGithub: String = "",
    val portfolioBehance: String = "",
    val portfolioDribbble: String = "",
    val portfolioYoutube: String = "",

    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isBoosted: Boolean = false,
    val boostEndDate: Long = 0L,
    val isApproved: Boolean = true,
    val isDeactivated: Boolean = false,
    val reportsCount: Int = 0,
    val reports: List<Map<String, Any>> = emptyList(),
    val averageRating: Double? = null,
    val totalReviews: Int? = null
) : Serializable

data class Review(
  val id: String = "",
  val reviewerId: String = "",
  val reviewerName: String = "",
  val reviewerType: String = "",
  val targetId: String = "",
  val targetType: String = "",
  val rating: Int = 5,
  val comment: String = "",
  val jobId: String = "",
  val createdAt: Long = 0L
)

data class JobApplication(
  val id: String = "",
  val jobId: String = "",
  val jobTitle: String = "",
  val employerId: String = "",
  val workerId: String = "",
  val workerName: String = "",
  val workerPhotoUrl: String = "",
  val status: String = "applied",
  val appliedAt: Long = 0L,
  val updatedAt: Long = 0L
)

data class Notification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val body: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val type: String = "", // "view", "bookmark", "category", "announcement"
    val relatedJobId: String = ""
) : Serializable

data class CompanyProfile(
    val companyId: String = "",
    val companyName: String = "",
    val logoUrl: String = "",
    val industry: String = "",
    val companySize: String = "",
    val location: String = "",
    val websiteUrl: String = "",
    val phoneNumber: String = "",
    val description: String = "",
    val foundedYear: String = "",
    val linkedinUrl: String = "",
    val instagramUrl: String = "",
    val twitterUrl: String = "",
    val isVerified: Boolean = false
) : Serializable

data class SupportRequest(
    val id: String = "",
    val userId: String = "",
    val email: String = "",
    val subject: String = "",
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "Open"
) : Serializable

data class Bookmark(
    val jobId: String = "",
    val userId: String = "",
    val savedAt: Long = System.currentTimeMillis()
) : Serializable

data class Subscription(
    val plan: String = "free",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = 0L,
    val isActive: Boolean = true,
    val transactionId: String = ""
) : Serializable

data class Application(
    val id: String = java.util.UUID.randomUUID().toString(),
    val jobId: String = "",
    val workerId: String = "",
    val companyId: String = "",
    val status: String = "pending",
    val appliedAt: Long = System.currentTimeMillis(),
    val coverNote: String = ""
) : Serializable

data class BoostPlan(
    val id: String = "",
    val duration: String = "",
    val durationDays: Int = 0,
    val price: Int = 0,
    val currency: String = "SAR",
    val label: String = "",
    val isActive: Boolean = true,
    val productId: String = ""
) : Serializable

data class ChatRoom(
    val id: String = "",
    val participants: List<String> = emptyList(), // User IDs
    val participantNames: Map<String, String> = emptyMap(), // User ID to Name
    val participantPhotos: Map<String, String> = emptyMap(), // User ID to Photo URL
    val lastMessage: String = "",
    val lastMessageAt: Long = 0L,
    val unreadCounts: Map<String, Long> = emptyMap() // User ID to Unread Count
) : Serializable

data class ChatMessage(
    val id: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isPending: Boolean = false
) : Serializable

data class AdPlacement(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val actionText: String = "",
    val targetType: String = "premium_subscription", // premium_subscription, job_boost, external_url
    val imageUrl: String = "",
    val targetUrl: String = "",
    val clicksCount: Int = 0,
    val viewsCount: Int = 0,
    val isActive: Boolean = true,
    val priority: Int = 0,
    val customGradientStart: String = "#8B5CF6",
    val customGradientEnd: String = "#3B82F6"
) : Serializable




