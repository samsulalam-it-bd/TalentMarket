package com.example.ui

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ADMIN_EMAIL
import com.example.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private fun isFirebaseAvailable(): Boolean {
        return try {
            com.google.firebase.FirebaseApp.getInstance()
            true
        } catch (e: Exception) {
            false
        }
    }

    private val _isLoadingStats = MutableStateFlow(false)
    val isLoadingStats = _isLoadingStats.asStateFlow()

    private val _securityLogs = MutableStateFlow<List<SecurityLog>>(emptyList())
    val securityLogs = _securityLogs.asStateFlow()

    private val _users = MutableStateFlow<List<UserAdmin>>(emptyList())
    val users = _users.asStateFlow()

    private val _allJobs = MutableStateFlow<List<Job>>(emptyList())
    val allJobs = _allJobs.asStateFlow()

    private val _allWorkers = MutableStateFlow<List<Worker>>(emptyList())
    val allWorkers = _allWorkers.asStateFlow()

    private val _revenue = MutableStateFlow<List<BoostPurchase>>(emptyList())
    val revenue = _revenue.asStateFlow()

    private val _tickets = MutableStateFlow<List<SupportRequest>>(emptyList())
    val tickets = _tickets.asStateFlow()

    // Stats moved below

    private val _boostPlansState = MutableStateFlow<List<BoostPlan>>(emptyList())
    val boostPlansState = _boostPlansState.asStateFlow()

    private val _categoriesState = MutableStateFlow<List<Category>>(emptyList())
    val categoriesState = _categoriesState.asStateFlow()

    private val _appConfig = MutableStateFlow<Map<String, Any>>(emptyMap())
    val appConfig = _appConfig.asStateFlow()

    private val _reportedPosts = MutableStateFlow<List<ReportedPost>>(emptyList())
    val reportedPosts = _reportedPosts.asStateFlow()

    private val _adminLogs = MutableStateFlow<List<AdminLog>>(emptyList())
    val adminLogs = _adminLogs.asStateFlow()

    private val _adPlacementsAdmin = MutableStateFlow<List<com.example.data.AdPlacement>>(emptyList())
    val adPlacementsAdmin = _adPlacementsAdmin.asStateFlow()

    val stats = combine(
        users, allJobs, allWorkers, combine(revenue, tickets, reportedPosts) { r, t, rp -> Triple(r, t, rp) }
    ) { usersList, jobsList, workersList, triple ->
        val (revenueList, ticketsList, reportsList) = triple
        AdminStats(
            totalUsers = usersList.size,
            totalWorkers = usersList.count { it.userType == "worker" },
            totalEmployers = usersList.count { it.userType == "employer" },
            verifiedUsers = usersList.count { it.isVerified },
            bannedUsers = usersList.count { it.isBanned },
            totalJobs = jobsList.size,
            activeCampaigns = jobsList.count { it.isBoosted } + workersList.count { it.isBoosted },
            pendingTickets = ticketsList.count { it.status == "open" || it.status == "pending" },
            pendingReports = reportsList.count { !it.isDeactivated },
            totalRevenue = revenueList.sumOf { it.amount }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = AdminStats()
    )

    init {
        if (isFirebaseAvailable()) {
            loadDashboardStats()
            loadAllUsers()
            loadAllJobs()
            loadAllWorkers()
            loadRevenue()
            loadSupportTickets()
            loadCategories()
            loadBoostPlans()
            loadAppConfig()
            loadSecurityLogs()
            loadReportedPosts()
            loadAdminLogs()
            loadAdPlacementsAdmin()
        }
    }

    fun loadDashboardStats() {
        // Stats are now dynamically calculated via state flow combine.
    }

    fun loadAllUsers() {
        db.collection("users")
            .limit(1000)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.map { doc ->
                    UserAdmin(
                        uid = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        email = doc.getString("email") ?: "",
                        userType = doc.getString("userType") ?: "worker",
                        isBanned = doc.getBoolean("isBanned") ?: false,
                        isVerified = doc.getBoolean("isVerified") ?: false,
                        plan = (doc.get("subscription") as? Map<*,*>)
                            ?.get("plan") as? String ?: "free",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        photoUrl = doc.getString("photoUrl") ?: "",
                        isAdmin = doc.getBoolean("isAdmin") ?: false
                    )
                } ?: emptyList()
                _users.value = list
            }
    }

    fun banUser(userId: String, ban: Boolean) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("users").document(userId)
            .update("isBanned", ban)
            .addOnSuccessListener {
                logAdminAction(if (ban) "BAN_USER" else "UNBAN_USER", userId)
            }
    }

    fun verifyUser(userId: String, verify: Boolean) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("users").document(userId)
            .update("isVerified", verify)
            .addOnSuccessListener {
                logAdminAction(if (verify) "VERIFY_USER" else "UNVERIFY_USER", userId)
            }
    }

    fun toggleAdmin(userId: String, makeAdmin: Boolean) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("users").document(userId)
            .update("isAdmin", makeAdmin)
            .addOnSuccessListener {
                logAdminAction(if (makeAdmin) "PROMOTE_ADMIN" else "DEMOTE_ADMIN", userId)
            }
    }

    fun deleteUser(userId: String) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("users").document(userId).delete()
            .addOnSuccessListener {
                logAdminAction("DELETE_USER", userId)
            }
    }

    fun setUserPlan(userId: String, plan: String) {
        if (userId.isBlank()) return
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        val endDate = System.currentTimeMillis() + 
            (30L * 24 * 60 * 60 * 1000)
        db.collection("users").document(userId)
            .update(mapOf(
                "subscription.plan" to plan,
                "subscription.isActive" to true,
                "subscription.startDate" to System.currentTimeMillis(),
                "subscription.endDate" to endDate,
                "subscription.grantedByAdmin" to true
            ))
            .addOnSuccessListener {
                logAdminAction("SET_PLAN_$plan", userId)
            }
    }

    fun setupAdminUser() {
        val user = auth.currentUser
        if (user?.email == ADMIN_EMAIL) {
            db.collection("users")
                .document(user.uid)
                .update("isAdmin", true)
        }
    }

    fun loadAllJobs() {
        db.collection("jobs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snap, _ ->
                _allJobs.value = snap?.toObjects(Job::class.java) 
                    ?: emptyList()
            }
    }

    fun deleteJob(jobId: String) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("jobs").document(jobId).delete()
    }

    fun pinJob(jobId: String, pin: Boolean) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        val boostEnd = if (pin) 
            System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
        else 0L
        db.collection("jobs").document(jobId)
            .update(mapOf(
                "isBoosted" to pin,
                "boostEndDate" to boostEnd,
                "pinnedByAdmin" to pin
            ))
    }

    fun approveJob(jobId: String, approve: Boolean) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("jobs").document(jobId)
            .update("isApproved", approve)
    }

    fun loadAllWorkers() {
        db.collection("workers")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snap, _ ->
                _allWorkers.value = snap?.toObjects(Worker::class.java)
                    ?: emptyList()
            }
    }

    fun deleteWorker(workerId: String) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("workers").document(workerId).delete()
    }

    fun pinWorker(workerId: String, pin: Boolean) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("workers").document(workerId)
            .update(mapOf(
                "isBoosted" to pin,
                "boostEndDate" to if (pin) 
                    System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000) 
                else 0L,
                "pinnedByAdmin" to pin
            ))
    }

    fun updateBoostPlan(planId: String, price: Int, 
        duration: String, durationDays: Int, 
        label: String, isActive: Boolean) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("settings").document("boost_plans")
            .get().addOnSuccessListener { doc ->
                val plans = (doc.get("plans") as? List<*>)
                    ?.filterIsInstance<Map<*, *>>()
                    ?.map { plan ->
                        if (plan["id"] == planId) {
                            hashMapOf(
                                "id" to planId,
                                "duration" to duration,
                                "durationDays" to durationDays,
                                "price" to price,
                                "currency" to "SAR",
                                "label" to label,
                                "isActive" to isActive,
                                "productId" to (plan["productId"] as? String ?: "")
                            )
                        } else plan
                    } ?: emptyList()
                
                db.collection("settings").document("boost_plans")
                    .update(mapOf(
                        "plans" to plans,
                        "lastUpdated" to System.currentTimeMillis()
                    ))
            }
    }

    fun addBoostPlan(plan: BoostPlan) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("settings").document("boost_plans")
            .get().addOnSuccessListener { doc ->
                val newPlan = hashMapOf(
                    "id" to plan.id,
                    "duration" to plan.duration,
                    "durationDays" to plan.durationDays,
                    "price" to plan.price,
                    "currency" to "SAR",
                    "label" to plan.label,
                    "isActive" to plan.isActive,
                    "productId" to plan.productId
                )
                db.collection("settings").document("boost_plans")
                    .update("plans", com.google.firebase.firestore.FieldValue.arrayUnion(newPlan))
            }
    }

    fun loadBoostPlans() {
        db.collection("settings").document("boost_plans")
            .addSnapshotListener { snap, _ ->
                val plansList = (snap?.get("plans") as? List<*>)
                    ?.filterIsInstance<Map<String, Any>>()
                    ?.map {
                        BoostPlan(
                            id = it["id"] as? String ?: "",
                            duration = it["duration"] as? String ?: "",
                            durationDays = (it["durationDays"] as? Long ?: 0L).toInt(),
                            price = (it["price"] as? Long ?: 0L).toInt(),
                            currency = it["currency"] as? String ?: "SAR",
                            label = it["label"] as? String ?: "",
                            isActive = it["isActive"] as? Boolean ?: true,
                            productId = it["productId"] as? String ?: ""
                        )
                    } ?: emptyList()
                _boostPlansState.value = plansList
            }
    }

    fun loadCategories() {
        db.collection("settings").document("categories")
            .addSnapshotListener { snap, _ ->
                val list = (snap?.get("list") as? List<*>)
                    ?.filterIsInstance<Map<String, Any>>()
                    ?.map {
                        Category(
                            id = it["id"] as? String ?: "",
                            nameEn = it["nameEn"] as? String ?: "",
                            nameAr = it["nameAr"] as? String ?: "",
                            icon = it["icon"] as? String ?: "work",
                            sector = it["sector"] as? String ?: "",
                            isActive = it["isActive"] as? Boolean ?: true
                        )
                    } ?: emptyList()
                _categoriesState.value = list
            }
    }

    fun addCategory(name: String, nameAr: String, 
        icon: String, sector: String) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("settings").document("categories")
            .update("list", com.google.firebase.firestore.FieldValue.arrayUnion(
                hashMapOf(
                    "id" to java.util.UUID.randomUUID().toString(),
                    "nameEn" to name,
                    "nameAr" to nameAr,
                    "icon" to icon,
                    "sector" to sector,
                    "isActive" to true
                )
            ))
    }

    fun toggleCategory(categoryId: String, active: Boolean) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("settings").document("categories")
            .get().addOnSuccessListener { doc ->
                val list = (doc.get("list") as? List<*>)
                    ?.filterIsInstance<Map<String, Any>>()
                    ?.map { category ->
                        if (category["id"] == categoryId) {
                            category.toMutableMap().apply { put("isActive", active) }
                        } else category
                    } ?: emptyList()
                
                db.collection("settings").document("categories")
                    .update("list", list)
            }
    }

    fun deleteCategory(categoryId: String) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("settings").document("categories")
            .get().addOnSuccessListener { doc ->
                val list = (doc.get("list") as? List<*>)
                    ?.filterIsInstance<Map<String, Any>>()
                    ?.filter { it["id"] != categoryId }
                    ?: emptyList()
                
                db.collection("settings").document("categories")
                    .update("list", list)
            }
    }

    fun editCategory(categoryId: String, nameEn: String, nameAr: String, icon: String, sector: String) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("settings").document("categories")
            .get().addOnSuccessListener { doc ->
                val list = (doc.get("list") as? List<*>)
                    ?.filterIsInstance<Map<String, Any>>()
                    ?.map { category ->
                        if (category["id"] == categoryId) {
                            category.toMutableMap().apply {
                                put("nameEn", nameEn)
                                put("nameAr", nameAr)
                                put("icon", icon)
                                put("sector", sector)
                            }
                        } else category
                    } ?: emptyList()
                
                db.collection("settings").document("categories")
                    .update("list", list)
            }
    }

    fun importDefaultCategories() {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        val saudiList = ConfigData.saudiCategories.map {
            hashMapOf(
                "id" to it.id.toString(),
                "nameEn" to it.nameEn,
                "nameAr" to it.nameAr,
                "icon" to it.nameEn.lowercase().replace(" & ", "_").replace(" ", "_"),
                "sector" to it.sector,
                "isActive" to true
            )
        }
        db.collection("settings").document("categories")
            .set(mapOf("list" to saudiList))
    }

    fun sendAdminNotification(
        target: String,
        targetUsers: List<UserAdmin>,
        specificUserId: String?,
        title: String,
        body: String,
        sendInApp: Boolean = true,
        sendPush: Boolean = false
    ) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        if (sendInApp) {
            if (target == "All Users") {
                sendBroadcastNotification(title, body)

                targetUsers.forEach { u ->
                    db.collection("notifications").document(u.uid).collection("items").add(
                        hashMapOf(
                            "title" to title,
                            "body" to body,
                            "isRead" to false,
                            "createdAt" to System.currentTimeMillis(),
                            "type" to "admin_message"
                        )
                    )
                }
            } else if (target == "Specific User" && specificUserId != null) {
                sendNotificationToUser(specificUserId, title, body)
            } else {
                targetUsers.forEach { u ->
                    sendNotificationToUser(u.uid, title, body)
                }
            }
        }
        
        if (sendPush) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                sendOneSignalPush(target, targetUsers, specificUserId, title, body)
            }
        }
    }
    
    private suspend fun sendOneSignalPush(
        target: String,
        targetUsers: List<UserAdmin>,
        specificUserId: String?,
        title: String,
        body: String
    ) {
        try {
            val appId = "85a9b91c-9b4b-4986-8331-83a20ee4a1f2"
            val restApiKey = com.example.BuildConfig.ONESIGNAL_REST_API_KEY
            if (restApiKey.isBlank()) {
                Log.e("AdminViewModel", "OneSignal REST API Key is missing.")
                return
            }
            
            val client = okhttp3.OkHttpClient()
            
            val jsonObj = org.json.JSONObject()
            jsonObj.put("app_id", appId)
            
            val headings = org.json.JSONObject()
            headings.put("en", title)
            jsonObj.put("headings", headings)
            
            val contents = org.json.JSONObject()
            contents.put("en", body)
            jsonObj.put("contents", contents)
            
            when (target) {
                "All Users" -> {
                    val segments = org.json.JSONArray()
                    segments.put("Total Subscriptions")
                    jsonObj.put("included_segments", segments)
                }
                "Specific User" -> {
                    val userIds = org.json.JSONArray()
                    specificUserId?.let { userIds.put(it) }
                    val aliases = org.json.JSONObject()
                    aliases.put("external_id", userIds)
                    jsonObj.put("include_aliases", aliases)
                    jsonObj.put("target_channel", "push")
                }
                else -> {
                    val userIds = org.json.JSONArray()
                    targetUsers.forEach { userIds.put(it.uid) }
                    val aliases = org.json.JSONObject()
                    aliases.put("external_id", userIds)
                    jsonObj.put("include_aliases", aliases)
                    jsonObj.put("target_channel", "push")
                }
            }
            
            val bodyReq = okhttp3.RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), jsonObj.toString())
            val request = okhttp3.Request.Builder()
                .url("https://onesignal.com/api/v1/notifications")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Basic $restApiKey")
                .post(bodyReq)
                .build()
                
            val response = client.newCall(request).execute()
            Log.d("AdminViewModel", "OneSignal Response: \${response.body?.string()}")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AdminViewModel", "Error sending OneSignal Push: \${e.message}")
        }
    }

    fun sendBroadcastNotification(title: String, body: String) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("announcements").add(
            hashMapOf(
                "title" to title,
                "body" to body,
                "createdAt" to System.currentTimeMillis(),
                "sentBy" to auth.currentUser?.uid,
                "type" to "broadcast"
            )
        )
    }

    fun sendNotificationToUser(
        userId: String, title: String, body: String) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("notifications")
            .document(userId)
            .collection("items")
            .add(hashMapOf(
                "title" to title,
                "body" to body,
                "isRead" to false,
                "createdAt" to System.currentTimeMillis(),
                "type" to "admin_message"
            ))

        // Trigger FCM push via Firebase FCM Extension pattern
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            val fcmToken = doc.getString("fcmToken")
            if (!fcmToken.isNullOrEmpty()) {
                val fcmMessage = hashMapOf(
                    "token" to fcmToken,
                    "notification" to hashMapOf(
                        "title" to title,
                        "body" to body
                    ),
                    "data" to hashMapOf(
                        "type" to "admin_message"
                    )
                )
                db.collection("fcm_messages").add(fcmMessage)
            }
        }
    }

    fun loadSupportTickets() {
        db.collection("support_requests")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snap, _ ->
                _tickets.value = snap?.toObjects(SupportRequest::class.java)
                    ?: emptyList()
            }
    }

    fun replyToTicket(ticketId: String, reply: String) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("support_requests").document(ticketId)
            .update(mapOf(
                "adminReply" to reply,
                "status" to "resolved",
                "repliedAt" to System.currentTimeMillis()
            ))
    }

    fun updateTicketStatus(ticketId: String, status: String) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("support_requests").document(ticketId)
            .update("status", status)
    }

    fun loadRevenue() {
        db.collection("boost_purchases")
            .orderBy("purchasedAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snap, _ ->
                _revenue.value = snap?.documents?.map { doc ->
                    BoostPurchase(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "",
                        planId = doc.getString("planId") ?: "",
                        amount = (doc.getLong("amount") ?: 0L).toInt(),
                        purchasedAt = doc.getLong("purchasedAt") ?: 0L
                    )
                } ?: emptyList()
            }
    }

    fun setMaintenanceMode(enabled: Boolean) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("settings").document("app_config")
            .set(hashMapOf(
                "maintenanceMode" to enabled,
                "maintenanceMessage" to 
                    "App is under maintenance. Please try again later.",
                "updatedAt" to System.currentTimeMillis()
            ), com.google.firebase.firestore.SetOptions.merge())
    }

    fun updateAppConfig(key: String, value: Any) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("settings").document("app_config")
            .set(mapOf(key to value), com.google.firebase.firestore.SetOptions.merge())
    }

    fun loadAppConfig() {
        db.collection("settings").document("app_config")
            .addSnapshotListener { snap, _ ->
                _appConfig.value = snap?.data ?: emptyMap()
            }
    }

    fun clearExpiredBoostPosts() {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        val now = System.currentTimeMillis()
        db.collection("jobs")
            .whereEqualTo("isBoosted", true)
            .get()
            .addOnSuccessListener { snap ->
                snap.documents.forEach { doc ->
                    val boostEndDate = doc.getLong("boostEndDate") ?: 0L
                    if (boostEndDate > 0L && boostEndDate < now) {
                        doc.reference.update(mapOf(
                            "isBoosted" to false,
                            "boostEndDate" to 0L
                        ))
                    }
                }
            }
        
        db.collection("workers")
            .whereEqualTo("isBoosted", true)
            .get()
            .addOnSuccessListener { snap ->
                snap.documents.forEach { doc ->
                    val boostEndDate = doc.getLong("boostEndDate") ?: 0L
                    if (boostEndDate > 0L && boostEndDate < now) {
                        doc.reference.update(mapOf(
                            "isBoosted" to false,
                            "boostEndDate" to 0L
                        ))
                    }
                }
            }
    }

    fun loadAdminLogs() {
        db.collection("admin_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snap, _ ->
                val logs = snap?.documents?.mapNotNull { doc ->
                    AdminLog(
                        id = doc.id,
                        adminId = doc.getString("adminId") ?: "",
                        action = doc.getString("action") ?: "",
                        targetId = doc.getString("targetId") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                } ?: emptyList()
                _adminLogs.value = logs
            }
    }

    fun logAdminAction(action: String, targetId: String) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        val adminEmail = auth.currentUser?.email ?: "admin@talentmarket.com"
        val log = hashMapOf(
            "adminEmail" to adminEmail,
            "action" to action,
            "targetId" to targetId,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("security_action_logs").add(log)
    }

    fun loadSecurityLogs() {
        db.collection("security_action_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snap, _ ->
                _securityLogs.value = snap?.documents?.map { doc ->
                    SecurityLog(
                        id = doc.id,
                        adminEmail = doc.getString("adminEmail") ?: "",
                        action = doc.getString("action") ?: "",
                        targetId = doc.getString("targetId") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                } ?: emptyList()
            }
    }

    fun loadReportedPosts() {
        db.collection("jobs")
            .whereGreaterThan("reportsCount", 0)
            .limit(50)
            .addSnapshotListener { jobsSnap, _ ->
                val jobsList = jobsSnap?.documents?.mapNotNull { doc ->
                    try {
                        ReportedPost(
                            id = doc.id,
                            title = doc.getString("title") ?: "Untitled Job",
                            postType = "job",
                            reportsCount = (doc.getLong("reportsCount") ?: 0L).toInt(),
                            isDeactivated = doc.getBoolean("isDeactivated") ?: false,
                            reports = (doc.get("reports") as? List<*>)?.filterIsInstance<Map<String, Any>>() ?: emptyList(),
                            userId = doc.getString("userId") ?: "",
                            details = doc.getString("description") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                db.collection("workers")
                    .whereGreaterThan("reportsCount", 0)
                    .limit(50)
                    .addSnapshotListener { workersSnap, _ ->
                        val workersList = workersSnap?.documents?.mapNotNull { doc ->
                            try {
                                ReportedPost(
                                    id = doc.id,
                                    title = doc.getString("name") ?: "Unnamed Worker",
                                    postType = "worker",
                                    reportsCount = (doc.getLong("reportsCount") ?: 0L).toInt(),
                                    isDeactivated = doc.getBoolean("isDeactivated") ?: false,
                                    reports = (doc.get("reports") as? List<*>)?.filterIsInstance<Map<String, Any>>() ?: emptyList(),
                                    userId = doc.getString("userId") ?: "",
                                    details = doc.getString("profession") ?: ""
                                )
                            } catch (e: Exception) {
                                null
                            }
                        } ?: emptyList()

                        _reportedPosts.value = (jobsList + workersList).sortedByDescending { it.reportsCount }
                    }
            }
    }

    fun deleteReportedPost(post: ReportedPost, onComplete: (Boolean) -> Unit = {}) {
        if (auth.currentUser?.email != ADMIN_EMAIL) {
            onComplete(false)
            return
        }
        val collection = if (post.postType == "job") "jobs" else "workers"
        db.collection(collection).document(post.id).delete()
            .addOnSuccessListener {
                logAdminAction("DELETE_REPORTED_${post.postType.uppercase()}", post.id)
                db.collection("notifications").document(post.userId).collection("items").add(
                    mapOf(
                        "title" to "Post Deleted",
                        "body" to "Your ${post.postType} post '${post.title}' was deleted by an admin due to community reports.",
                        "createdAt" to System.currentTimeMillis(),
                        "isRead" to false,
                        "type" to "post_status",
                        "relatedJobId" to post.id
                    )
                )

                // Trigger FCM push via Firebase FCM Extension pattern
                db.collection("users").document(post.userId).get().addOnSuccessListener { doc ->
                    val fcmToken = doc.getString("fcmToken")
                    if (!fcmToken.isNullOrEmpty()) {
                        val fcmMessage = hashMapOf(
                            "token" to fcmToken,
                            "notification" to hashMapOf(
                                "title" to "Post Deleted",
                                "body" to "Your ${post.postType} post '${post.title}' was deleted by an admin due to community reports."
                            ),
                            "data" to hashMapOf(
                                "type" to "post_status",
                                "relatedJobId" to post.id
                            )
                        )
                        db.collection("fcm_messages").add(fcmMessage)
                    }
                }
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun reactivatePost(post: ReportedPost, onComplete: (Boolean) -> Unit = {}) {
        if (auth.currentUser?.email != ADMIN_EMAIL) {
            onComplete(false)
            return
        }
        val collection = if (post.postType == "job") "jobs" else "workers"
        db.collection(collection).document(post.id)
            .update(mapOf(
                "reportsCount" to 0,
                "isDeactivated" to false,
                "reports" to emptyList<Any>()
            ))
            .addOnSuccessListener {
                logAdminAction("REACTIVATE_REPORTED_${post.postType.uppercase()}", post.id)
                db.collection("notifications").document(post.userId).collection("items").add(
                    mapOf(
                        "title" to "Post Reactivated",
                        "body" to "Your ${post.postType} post '${post.title}' has been reviewed and reactivated.",
                        "createdAt" to System.currentTimeMillis(),
                        "isRead" to false,
                        "type" to "post_status",
                        "relatedJobId" to post.id
                    )
                )

                // Trigger FCM push via Firebase FCM Extension pattern
                db.collection("users").document(post.userId).get().addOnSuccessListener { doc ->
                    val fcmToken = doc.getString("fcmToken")
                    if (!fcmToken.isNullOrEmpty()) {
                        val fcmMessage = hashMapOf(
                            "token" to fcmToken,
                            "notification" to hashMapOf(
                                "title" to "Post Reactivated",
                                "body" to "Your ${post.postType} post '${post.title}' has been reviewed and reactivated."
                            ),
                            "data" to hashMapOf(
                                "type" to "post_status",
                                "relatedJobId" to post.id
                            )
                        )
                        db.collection("fcm_messages").add(fcmMessage)
                    }
                }
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun loadAdPlacementsAdmin() {
        db.collection("ad_placements")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(com.example.data.AdPlacement::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                _adPlacementsAdmin.value = list.sortedBy { it.priority }
            }
    }

    fun saveAdPlacement(ad: com.example.data.AdPlacement) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        val adId = ad.id.ifEmpty { db.collection("ad_placements").document().id }
        val finalAd = ad.copy(id = adId)
        db.collection("ad_placements").document(adId).set(finalAd)
            .addOnSuccessListener {
                logAdminAction("SAVE_AD_PLACEMENT", adId)
            }
    }

    fun deleteAdPlacement(adId: String) {
        if (auth.currentUser?.email != ADMIN_EMAIL) return
        db.collection("ad_placements").document(adId).delete()
            .addOnSuccessListener {
                logAdminAction("DELETE_AD_PLACEMENT", adId)
            }
    }
}

data class AdminLog(
    val id: String = "",
    val adminId: String = "",
    val action: String = "",
    val targetId: String = "",
    val timestamp: Long = 0L
)

data class ReportedPost(
    val id: String = "",
    val title: String = "",
    val postType: String = "", // "job" or "worker"
    val reportsCount: Int = 0,
    val isDeactivated: Boolean = false,
    val reports: List<Map<String, Any>> = emptyList(),
    val userId: String = "",
    val details: String = ""
)

data class SecurityLog(
    val id: String = "",
    val adminEmail: String = "",
    val action: String = "",
    val targetId: String = "",
    val timestamp: Long = 0L
)

data class AdminStats(
    val totalUsers: Int = 0,
    val totalWorkers: Int = 0,
    val totalEmployers: Int = 0,
    val verifiedUsers: Int = 0,
    val bannedUsers: Int = 0,
    val totalJobs: Int = 0,
    val activeCampaigns: Int = 0,
    val totalRevenue: Int = 0,
    val pendingTickets: Int = 0,
    val pendingReports: Int = 0
)

data class UserAdmin(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val userType: String = "",
    val isBanned: Boolean = false,
    val isVerified: Boolean = false,
    val plan: String = "free",
    val createdAt: Long = 0L,
    val photoUrl: String = "",
    val isAdmin: Boolean = false
)

data class BoostPurchase(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val planId: String = "",
    val amount: Int = 0,
    val purchasedAt: Long = 0L
)

data class AdminSection(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val route: String
)

data class Category(
    val id: String = "",
    val nameEn: String = "",
    val nameAr: String = "",
    val icon: String = "work",
    val sector: String = "",
    val isActive: Boolean = true
)
