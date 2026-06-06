package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Job
import com.example.data.TalentRepository
import com.example.data.Worker
import com.example.data.BoostPlan
import com.example.data.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.onesignal.OneSignal
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TalentViewModel(private val repository: TalentRepository) : ViewModel() {

    private val _uiMessage = kotlinx.coroutines.flow.MutableSharedFlow<String>(extraBufferCapacity = 1)
    val uiMessage: kotlinx.coroutines.flow.SharedFlow<String> = _uiMessage.asSharedFlow()

    fun getCurrentUserId(): String {
        return try {
            auth?.currentUser?.uid ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    val subscriptionViewModel = SubscriptionViewModel()
    
    private val _boostPlans = MutableStateFlow<List<BoostPlan>>(emptyList())
    val boostPlans = _boostPlans.asStateFlow()

    private val _dynamicCategories = MutableStateFlow<List<Category>>(emptyList())
    val dynamicCategories = _dynamicCategories.asStateFlow()

    private val _applicationsForCurrentJob = MutableStateFlow<List<Application>>(emptyList())
    val applicationsForCurrentJob: StateFlow<List<Application>> = _applicationsForCurrentJob.asStateFlow()

    private val _myApplications = MutableStateFlow<List<com.example.data.JobApplication>>(emptyList())
    val myApplications = _myApplications.asStateFlow()

    private val _jobApplications = MutableStateFlow<List<com.example.data.JobApplication>>(emptyList())
    val jobApplications = _jobApplications.asStateFlow()

    private var applicationsListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun loadMyApplications() {
        val uid = auth?.currentUser?.uid ?: return
        applicationsListener?.remove()
        applicationsListener = firestore
            ?.collection("applications")
            ?.whereEqualTo("workerId", uid)
            ?.addSnapshotListener { snap, err ->
                logFirestoreError(err)
                val list = snap?.toObjects(com.example.data.JobApplication::class.java) ?: emptyList()
                _myApplications.value = list.sortedByDescending { it.appliedAt }
            }
    }

    fun loadJobApplications(jobId: String) {
        val uid = auth?.currentUser?.uid ?: return
        firestore?.collection("applications")
            ?.whereEqualTo("jobId", jobId)
            ?.whereEqualTo("employerId", uid)
            ?.addSnapshotListener { snap, err ->
                logFirestoreError(err)
                val list = snap?.toObjects(com.example.data.JobApplication::class.java) ?: emptyList()
                _jobApplications.value = list.sortedByDescending { it.appliedAt }
            }
    }

    fun updateApplicationStatus(
        applicationId: String,
        newStatus: String,
        workerId: String,
        jobTitle: String
    ) {
        firestore?.collection("applications")
            ?.document(applicationId)
            ?.update(mapOf(
                "status" to newStatus,
                "updatedAt" to System.currentTimeMillis()
            ))
            ?.addOnSuccessListener {
                // Send notification to worker
                val statusText = when(newStatus) {
                    "shortlisted" -> "You have been shortlisted"
                    "interviewing" -> "Interview scheduled"
                    "hired" -> "Congratulations! You are hired"
                    "rejected" -> "Application not selected"
                    else -> "Application status updated"
                }
                val notifData = hashMapOf(
                    "title" to "Application Update: $jobTitle",
                    "body" to statusText,
                    "createdAt" to System.currentTimeMillis(),
                    "isRead" to false,
                    "type" to "application_status",
                    "status" to newStatus
                )
                firestore?.collection("notifications")
                    ?.document(workerId)
                    ?.collection("items")
                    ?.add(notifData)
            }
    }


    fun loadApplicationsForJob(jobId: String) {
        firestore?.collection("applications")
            ?.whereEqualTo("jobId", jobId)
            ?.addSnapshotListener { snapshot, error ->
                logFirestoreError(error)
                if (snapshot != null) {
                    val apps = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Application::class.java)?.copy(id = doc.id)
                    }.sortedByDescending { it.appliedAt }
                    _applicationsForCurrentJob.value = apps
                }
            }
    }

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin = _isAdmin.asStateFlow()

    var isGuest = false
    var openInSignUpMode by mutableStateOf(false)
    var authError by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var currentUserType by mutableStateOf<String?>(null)
    var initialSearchCategory by mutableStateOf("All")
    var jobFilter by mutableStateOf(JobFilter())

    private val _chatRooms = MutableStateFlow<List<com.example.data.ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<com.example.data.ChatRoom>> = _chatRooms.asStateFlow()

    private val _currentMessages = MutableStateFlow<List<com.example.data.ChatMessage>>(emptyList())
    val currentMessages: StateFlow<List<com.example.data.ChatMessage>> = _currentMessages.asStateFlow()

    var workerFilter by mutableStateOf(WorkerFilter())

    val auth by lazy { try { FirebaseAuth.getInstance() } catch (e: Exception) { null } }
    private val firestore by lazy { try { FirebaseFirestore.getInstance() } catch (e: Exception) { null } }

    fun loginUser(email: String, pass: String, userType: String, onResult: (Boolean) -> Unit) {
        isLoading = true
        authError = null
        val firebaseAuth = auth ?: run {
             isLoading = false
             authError = "Firebase not initialized. Please add google-services.json to the app."
             onResult(false)
             return
        }
        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val firebaseUser = user
                        val db = firestore
                        if (db != null) {
                            val userRef = db.collection("users").document(firebaseUser.uid)
                            userRef.get().addOnSuccessListener { doc ->
                                if (!doc.exists()) {
                                    val name = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "User"
                                    // Create user document for first time
                                    userRef.set(mapOf(
                                        "uid" to firebaseUser.uid,
                                        "email" to firebaseUser.email,
                                        "name" to (firebaseUser.displayName ?: name),
                                        "userType" to userType,
                                        "isAdmin" to (firebaseUser.email == com.example.ADMIN_EMAIL),
                                        "isBanned" to false,
                                        "isVerified" to false,
                                        "createdAt" to System.currentTimeMillis(),
                                        "subscription" to mapOf(
                                            "plan" to "free",
                                            "isActive" to false
                                        )
                                    )).addOnCompleteListener {
                                        // If admin email, ensure isAdmin is true
                                        if (firebaseUser.email == com.example.ADMIN_EMAIL) {
                                            userRef.update("isAdmin", true)
                                        }
                                        
                                        currentUserType = userType
                                        isLoading = false
                                        onResult(true)
                                    }
                                } else {
                                    // If admin email, ensure isAdmin is true
                                    if (firebaseUser.email == com.example.ADMIN_EMAIL) {
                                        userRef.update("isAdmin", true)
                                    }

                                    val isBanned = doc.getBoolean("isBanned") ?: false
                                    if (isBanned) {
                                        firebaseAuth.signOut()
                                        com.example.utils.ChatConnectionManager.cleanUp()
                                        authError = "Your account has been suspended. Contact support for help."
                                        isLoading = false
                                        onResult(false)
                                    } else {
                                        val actualType = doc.getString("userType")
                                        if (actualType != null) {
                                            currentUserType = actualType
                                            isLoading = false
                                            onResult(true)
                                        } else {
                                            currentUserType = userType
                                            isLoading = false
                                            onResult(true)
                                        }
                                    }
                                }
                                
                                com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                    userRef.update("fcmToken", token)
                                }
                            }.addOnFailureListener {
                                logFirestoreError(it)
                                currentUserType = userType
                                isLoading = false
                                onResult(true)
                            }
                        } else {
                            currentUserType = userType
                            isLoading = false
                            onResult(true)
                        }
                    } else {
                        isLoading = false
                        onResult(false)
                    }
                } else {
                    isLoading = false
                    authError = task.exception?.message ?: "Login failed"
                    onResult(false)
                }
            }
    }

    fun registerUser(email: String, pass: String, userType: String, name: String, onResult: (Boolean) -> Unit) {
        isLoading = true
        authError = null
        val firebaseAuth = auth ?: run {
             isLoading = false
             authError = "Firebase not initialized. Please add google-services.json to the app."
             onResult(false)
             return
        }
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener {
                                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                                val randomChars = (1..4).map { (('A'..'Z') + ('0'..'9')).random() }.joinToString("")
                                val uniqueId = "TM-$currentYear-$randomChars"

                                val firebaseUser = user
                                val db = firestore
                                if (db != null) {
                                    db.collection("users")
                                        .document(firebaseUser.uid)
                                        .set(mapOf(
                                            "uid" to firebaseUser.uid,
                                            "email" to email,
                                            "name" to name,
                                            "userType" to userType,
                                            "isAdmin" to (email == com.example.ADMIN_EMAIL),
                                            "isBanned" to false,
                                            "isVerified" to false,
                                            "createdAt" to System.currentTimeMillis(),
                                            "photoUrl" to "",
                                            "uniqueId" to uniqueId,
                                            "subscription" to mapOf(
                                                "plan" to "free",
                                                "isActive" to false,
                                                "startDate" to 0L,
                                                "endDate" to 0L
                                            )
                                        ))
                                        .addOnCompleteListener { fsTask ->
                                            currentUserType = userType
                                            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                                db.collection("users").document(firebaseUser.uid).update("fcmToken", token)
                                            }
                                            onResult(fsTask.isSuccessful)
                                        }
                                } else {
                                    currentUserType = userType
                                    onResult(true)
                                }
                            }
                    } else {
                        onResult(true)
                    }
                } else {
                    authError = task.exception?.message ?: "Registration failed"
                    onResult(false)
                }
            }
    }

    fun calculateProfileCompletion(userType: String): Int {
        val user = _currentUserProfile.value ?: return 0
        
        val workerFields = listOf(
            !(user["name"] as? String).isNullOrEmpty(),
            !(user["photoUrl"] as? String).isNullOrEmpty(),
            !(user["phone"] as? String).isNullOrEmpty(),
            !(user["location"] as? String).isNullOrEmpty(),
            !(user["bio"] as? String).isNullOrEmpty(),
            !(user["skills"] as? String).isNullOrEmpty(),
            !(user["experience"] as? String).isNullOrEmpty(),
            ((user["totalReviews"] as? Number)?.toInt() ?: 0) > 0
        )
        
        val employerFields = listOf(
            !(user["name"] as? String).isNullOrEmpty(),
            !(user["photoUrl"] as? String).isNullOrEmpty(),
            !(user["phone"] as? String).isNullOrEmpty(),
            !(user["location"] as? String).isNullOrEmpty(),
            !(user["companyName"] as? String).isNullOrEmpty(),
            !(user["bio"] as? String).isNullOrEmpty(),
            ((user["totalReviews"] as? Number)?.toInt() ?: 0) > 0
        )
        
        val fields = if (userType == "worker") workerFields else employerFields
        if (fields.isEmpty()) return 0
        val completed = fields.count { it }
        return ((completed.toFloat() / fields.size.toFloat()) * 100).toInt()
    }

    private val _currentUserProfile = MutableStateFlow<Map<String, Any>?>(null)
    val currentUserProfile: StateFlow<Map<String, Any>?> = _currentUserProfile

    fun updateProfileName(name: String, onDone: (Boolean) -> Unit) {
        val user = auth?.currentUser ?: return onDone(false)
        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(profileUpdates)
            .addOnCompleteListener { authTask ->
                val updates = mapOf("name" to name)
                firestore?.collection("users")?.document(user.uid)?.update("name", name)
                    ?.addOnSuccessListener {
                        onDone(true)
                    }
                    ?.addOnFailureListener {
                        firestore?.collection("users")?.document(user.uid)?.set(updates, com.google.firebase.firestore.SetOptions.merge())
                            ?.addOnSuccessListener {
                                onDone(true)
                            }
                            ?.addOnFailureListener {
                                onDone(false)
                            }
                    }
            }
    }

    private val _jobs = repository.allJobs.combine(isAdmin) { localJobs, admin ->
        localJobs.filter { (it.expiryDate == 0L || it.expiryDate > System.currentTimeMillis()) && (!it.isDeactivated || admin) }
                 .sortedWith(compareByDescending<Job> { it.isBoosted }.thenByDescending { it.timestamp })
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val allRawJobs = repository.allJobs.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())

    private val _workers = repository.allWorkers.combine(isAdmin) { localWorkers, admin ->
        localWorkers.filter { !it.isDeactivated || admin }
                    .sortedWith(compareByDescending<Worker> { it.isBoosted }.thenByDescending { it.timestamp })
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val allRawWorkers = repository.allWorkers.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())
    private val _companyProfiles = MutableStateFlow<Map<String, com.example.data.CompanyProfile>>(emptyMap())

    private val _appConfig = MutableStateFlow<Map<String, Any>>(emptyMap())
    val appConfig = _appConfig.asStateFlow()

    private var lastJobDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    private var isLoadingMoreJobs = false
    private val _hasMoreJobs = MutableStateFlow(true)
    val hasMoreJobs = _hasMoreJobs.asStateFlow()

    private var lastWorkerDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    private var isLoadingMoreWorkers = false
    private val _hasMoreWorkers = MutableStateFlow(true)
    val hasMoreWorkers = _hasMoreWorkers.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val companyProfiles: StateFlow<Map<String, com.example.data.CompanyProfile>> = _companyProfiles

    private val _newNotificationEvent = kotlinx.coroutines.flow.MutableSharedFlow<com.example.data.Notification>(extraBufferCapacity = 1)
    val newNotificationEvent: kotlinx.coroutines.flow.SharedFlow<com.example.data.Notification> = _newNotificationEvent

    private val _favoriteJobIds = MutableStateFlow<Set<String>>(emptySet())
    private val _favoriteWorkerIds = MutableStateFlow<Set<String>>(emptySet())

    private val _notifications = MutableStateFlow<List<com.example.data.Notification>>(emptyList())
    private val _announcements = MutableStateFlow<List<com.example.data.Notification>>(emptyList())
    private val _favoriteCategories = MutableStateFlow<Set<String>>(emptySet())
    
    private val _supportRequests = MutableStateFlow<List<com.example.data.SupportRequest>>(emptyList())
    val supportRequests: StateFlow<List<com.example.data.SupportRequest>> = _supportRequests

    private val _reviews = MutableStateFlow<List<com.example.data.Review>>(emptyList())
    val reviews = _reviews.asStateFlow()

    private var reviewsListener: com.google.firebase.firestore.ListenerRegistration? = null

    val favoriteCategories: StateFlow<Set<String>> = _favoriteCategories

    val notifications: StateFlow<List<com.example.data.Notification>> = combine(_notifications, _announcements) { userNotifs, globalAnnouncements ->
        (userNotifs + globalAnnouncements).sortedByDescending { it.createdAt }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val jobs: StateFlow<List<Job>> = combine(_jobs, _favoriteJobIds) { firestoreJobs, favIds ->
        firestoreJobs.map { fj -> 
            fj.copy(isFavorite = favIds.contains(fj.id))
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val workers: StateFlow<List<Worker>> = combine(_workers, _favoriteWorkerIds) { firestoreWorkers, favIds ->
        firestoreWorkers.map { fw -> 
            fw.copy(isFavorite = favIds.contains(fw.id))
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteJobs: StateFlow<List<Job>> = combine(_jobs, _favoriteJobIds) { firestoreJobs, favIds ->
        firestoreJobs.filter { favIds.contains(it.id) }.map { it.copy(isFavorite = true) }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteWorkers: StateFlow<List<Worker>> = combine(_workers, _favoriteWorkerIds) { firestoreWorkers, favIds ->
        firestoreWorkers.filter { favIds.contains(it.id) }.map { it.copy(isFavorite = true) }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _jobNotifications = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val jobNotifications = _jobNotifications.asSharedFlow()
    
    private val _adPlacements = MutableStateFlow<List<com.example.data.AdPlacement>>(emptyList())
    val adPlacements = _adPlacements.asStateFlow()

    fun recordAdClick(adId: String) {
        val db = firestore ?: return
        db.collection("ad_placements").document(adId)
            .update("clicksCount", com.google.firebase.firestore.FieldValue.increment(1))
    }

    fun recordAdView(adId: String) {
        val db = firestore ?: return
        db.collection("ad_placements").document(adId)
            .update("viewsCount", com.google.firebase.firestore.FieldValue.increment(1))
    }
    
    private var isJobsInitialLoad = true

    fun loadBoostPlans() {
        val db = firestore
        if (db != null) {
            db.collection("settings")
                .document("boost_plans")
                .addSnapshotListener { snapshot, err ->
                    logFirestoreError(err)
                    val data = snapshot?.data ?: return@addSnapshotListener
                    val plansList = (data["plans"] as? List<*>)
                        ?.filterIsInstance<Map<*, *>>()
                        ?.map { map ->
                            com.example.data.BoostPlan(
                                id = map["id"] as? String ?: "",
                                duration = map["duration"] as? String ?: "",
                                durationDays = (map["durationDays"] as? Long)?.toInt() ?: 0,
                                price = (map["price"] as? Long)?.toInt() ?: 0,
                                currency = map["currency"] as? String ?: "SAR",
                                label = map["label"] as? String ?: "",
                                isActive = map["isActive"] as? Boolean ?: true,
                                productId = map["productId"] as? String ?: ""
                            )
                        }
                        ?.filter { it.isActive }
                        ?: emptyList()
                    
                    _boostPlans.value = plansList
                }
        }
    }

    fun initializeAppSettings() {
        val db = firestore ?: return
        
        db.collection("settings").document("app_config")
            .addSnapshotListener { snap, _ ->
                _appConfig.value = snap?.data ?: emptyMap()
            }

        // Check and create app_config
        db.collection("settings")
            .document("app_config")
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    db.collection("settings")
                        .document("app_config")
                        .set(mapOf(
                            "maintenanceMode" to false,
                            "maintenanceMessage" to "App is under maintenance.",
                            "forceUpdate" to false,
                            "minAppVersion" to "1.0.0",
                            "welcomeMessage" to "Welcome to TalentMarket!",
                            "admobEnabled" to false,
                            "bannerAdUnitId" to "ca-app-pub-3940256099942544/6300978111",
                            "interstitialAdUnitId" to "ca-app-pub-3940256099942544/1033173712",
                            "feedAdFrequency" to 5,
                            "showBannerOnDetails" to true,
                            "showAdOnApply" to true,
                            "showBannerOnFeed" to true,
                            "updatedAt" to System.currentTimeMillis()
                        ))
                }
            }
        
        // Check and create boost_plans
        db.collection("settings")
            .document("boost_plans")
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    db.collection("settings")
                        .document("boost_plans")
                        .set(mapOf(
                            "plans" to listOf(
                                mapOf(
                                    "id" to "boost_7",
                                    "duration" to "7 Days",
                                    "durationDays" to 7,
                                    "price" to 29,
                                    "currency" to "SAR",
                                    "label" to "Great for quick hiring",
                                    "isActive" to true,
                                    "productId" to "talentmarket_boost_7days"
                                ),
                                mapOf(
                                    "id" to "boost_30",
                                    "duration" to "30 Days",
                                    "durationDays" to 30,
                                    "price" to 79,
                                    "currency" to "SAR",
                                    "label" to "Most popular choice",
                                    "isActive" to true,
                                    "productId" to "talentmarket_boost_30days"
                                ),
                                mapOf(
                                    "id" to "boost_90",
                                    "duration" to "90 Days",
                                    "durationDays" to 90,
                                    "price" to 149,
                                    "currency" to "SAR",
                                    "label" to "Best value for money",
                                    "isActive" to true,
                                    "productId" to "talentmarket_boost_90days"
                                )
                            ),
                            "lastUpdated" to System.currentTimeMillis()
                        ))
                }
            }
          
        // Check and create categories
        db.collection("settings")
            .document("categories")
            .get()
            .addOnSuccessListener { doc ->
                val currentList = doc.get("list") as? List<*>
                if (!doc.exists() || (currentList?.size ?: 0) < 50) {
                    val saudiList = com.example.ui.ConfigData.saudiCategories.map {
                        mapOf(
                            "id" to it.id.toString(),
                            "nameEn" to it.nameEn,
                            "nameAr" to it.nameAr,
                            "icon" to it.nameEn.lowercase().replace(" & ", "_").replace(" ", "_"),
                            "sector" to it.sector,
                            "isActive" to true
                        )
                    }
                    db.collection("settings")
                        .document("categories")
                        .set(mapOf("list" to saudiList))
                }
            }

        // Live category snapshot listener
        db.collection("settings")
            .document("categories")
            .addSnapshotListener { snapshot, error ->
                logFirestoreError(error)
                val data = snapshot?.data
                val listMap = data?.get("list") as? List<*>
                val list = listMap?.filterIsInstance<Map<String, Any>>()?.map { map ->
                    Category(
                        id = map["id"] as? String ?: "",
                        nameEn = map["nameEn"] as? String ?: "",
                        nameAr = map["nameAr"] as? String ?: "",
                        sector = map["sector"] as? String ?: "",
                        icon = map["icon"] as? String ?: "work",
                        isActive = map["isActive"] as? Boolean ?: true
                    )
                } ?: emptyList()
                _dynamicCategories.value = list
                com.example.ui.ConfigData.dynamicCategoriesList = list
            }

        // Live fallback campaign and dynamic sponsorship ads (ad_placements) snapshot listener
        db.collection("ad_placements")
            .addSnapshotListener { snapshot, error ->
                logFirestoreError(error)
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(com.example.data.AdPlacement::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _adPlacements.value = list.filter { it.isActive }.sortedBy { it.priority }
                }
            }

        // Auto-seed default high-converting campaigns if ad_placements is empty
        db.collection("ad_placements")
            .get()
            .addOnSuccessListener { snap ->
                if (snap == null || snap.isEmpty) {
                    val defaultAds = listOf(
                        com.example.data.AdPlacement(
                            id = "default_premium",
                            title = "Upgrade to Premium Membership 💎",
                            subtitle = "Unlock unlimited job applications & premium applicant details instantly!",
                            actionText = "Go Premium",
                            targetType = "premium_subscription",
                            priority = 10,
                            customGradientStart = "#7C3AED",
                            customGradientEnd = "#DB2777"
                        ),
                        com.example.data.AdPlacement(
                            id = "default_boost",
                            title = "Boost Your Job/Talent Post 🚀",
                            subtitle = "Get up to 10x more reach & views by pinning your profile/posting on top of feed!",
                            actionText = "Boost Now",
                            targetType = "job_boost",
                            priority = 20,
                            customGradientStart = "#2563EB",
                            customGradientEnd = "#06B6D4"
                        )
                    )
                    for (ad in defaultAds) {
                        db.collection("ad_placements").document(ad.id).set(ad)
                    }
                }
            }
    }

    private fun logFirestoreError(error: Throwable?) {
        if (error == null) return
        
        val msg = error.message ?: ""
        
        // Log to logcat for developer tracing
        android.util.Log.e("TalentViewModel", "Firestore Error occurred: $msg", error)

        // Ignore PERMISSION_DENIED / insufficient permissions errors for notifications/toasts
        // especially important on start screen when we are still unauthenticated.
        val isPermissionError = msg.contains("permission", ignoreCase = true) || 
                                 msg.contains("denied", ignoreCase = true)
        if (isPermissionError) {
            return
        }

        // Delegate to our custom centralized global index error handler service
        com.example.ui.FirestoreErrorHandler.handleError(error)
        
        val isIndexError = msg.contains("index", ignoreCase = true) || 
                           msg.contains("FAILED_PRECONDITION", ignoreCase = true) ||
                           msg.contains("failed-precondition", ignoreCase = true)
        
        viewModelScope.launch {
            try {
                if (isIndexError) {
                    _jobNotifications.emit("ইনডেক্স প্রয়োজন! তৈরি করার লিংকটি নিচে পপআপ আকারে এবং লগে ডেডিকেটেডলি প্রিন্ট করা হয়েছে।")
                } else {
                    _jobNotifications.emit("Firestore error: $msg")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun isFirebaseAvailable(): Boolean {
        return try {
            com.google.firebase.FirebaseApp.getInstance()
            true
        } catch (e: Exception) {
            false
        }
    }

    init {
        if (isFirebaseAvailable()) {
            checkExpiredBoosts()
            loadBoostPlans()
            initializeAppSettings()
        }
        auth?.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            val userId = user?.uid
            val userEmail = user?.email
            if (userId != null) {
                setupAdminUser()
                subscriptionViewModel.loadSubscription(userId)
                // 1. Favorites Listener
                firestore?.collection("users")?.document(userId)?.collection("favorites")
                    ?.addSnapshotListener { snapshot, error ->
                        logFirestoreError(error)
                        if (snapshot != null) {
                            _favoriteJobIds.value = snapshot.documents.map { it.id }.toSet()
                        }
                    }
                // 2. Favorite Categories Listener
                firestore?.collection("users")?.document(userId)?.collection("favorite_categories")
                    ?.addSnapshotListener { snapshot, error ->
                        logFirestoreError(error)
                        if (snapshot != null) {
                            val categories = snapshot.documents.map { it.id }.toSet()
                            _favoriteCategories.value = categories
                            
                            // Sync FCM subscriptions
                            categories.forEach { category ->
                                val sanitizedCategory = category.replace(Regex("[^a-zA-Z0-9_.~%\\-]"), "_")
                                try {
                                    com.google.firebase.messaging.FirebaseMessaging.getInstance()
                                        .subscribeToTopic("category_$sanitizedCategory")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                // 3. User Notifications Listener (both duplicates are in sync, items is primary)
                var isInitialNotifSnapshot = true
                firestore?.collection("notifications")?.document(userId)?.collection("items")
                    ?.addSnapshotListener { snapshot, error ->
                        logFirestoreError(error)
                        if (snapshot != null) {
                            if (!isInitialNotifSnapshot) {
                                for (dc in snapshot.documentChanges) {
                                    if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                        val doc = dc.document
                                        val isRead = doc.getBoolean("isRead") ?: false
                                        if (!isRead) {
                                            val notif = com.example.data.Notification(
                                                id = doc.id,
                                                title = doc.getString("title") ?: "",
                                                body = doc.getString("body") ?: "",
                                                isRead = false,
                                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                                type = doc.getString("type") ?: "",
                                                relatedJobId = doc.getString("relatedJobId") ?: ""
                                            )
                                            _newNotificationEvent.tryEmit(notif)
                                        }
                                    }
                                }
                            }
                            isInitialNotifSnapshot = false

                            val list = snapshot.documents.mapNotNull { doc ->
                                try {
                                    com.example.data.Notification(
                                        id = doc.id,
                                        title = doc.getString("title") ?: "",
                                        body = doc.getString("body") ?: "",
                                        isRead = doc.getBoolean("isRead") ?: false,
                                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                        type = doc.getString("type") ?: "",
                                        relatedJobId = doc.getString("relatedJobId") ?: ""
                                    )
                                } catch (e: Exception) { null }
                            }.sortedByDescending { it.createdAt }
                            _notifications.value = list
                        }
                    }
                // 4. Current User Profile Listener
                firestore?.collection("users")?.document(userId)
                    ?.addSnapshotListener { snapshot, error ->
                        logFirestoreError(error)
                        if (snapshot != null && snapshot.exists()) {
                            _currentUserProfile.value = snapshot.data
                            _isAdmin.value = (snapshot.data?.get("isAdmin") as? Boolean) == true
                            val favWorkers = snapshot.get("favoriteWorkers") as? List<*>
                            _favoriteWorkerIds.value = favWorkers?.filterIsInstance<String>()?.toSet() ?: emptySet()
                        } else {
                            _currentUserProfile.value = null
                            _favoriteWorkerIds.value = emptySet()
                        }
                    }
            } else {
                _favoriteJobIds.value = emptySet()
                _favoriteWorkerIds.value = emptySet()
                _favoriteCategories.value = emptySet()
                _notifications.value = emptyList()
                _currentUserProfile.value = null
                _isAdmin.value = false
            }
        }

        // Global Announcements Listener
        firestore?.collection("announcements")
            ?.orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            ?.limit(50)
            ?.addSnapshotListener { snapshot, error ->
                logFirestoreError(error)
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            com.example.data.Notification(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                body = doc.getString("body") ?: "",
                                isRead = false,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                type = "announcement",
                                relatedJobId = ""
                            )
                        } catch (e: Exception) { null }
                    }.sortedByDescending { it.createdAt }
                    _announcements.value = list
                }
            }

        loadJobs(refresh = true)
        loadWorkers(refresh = true)
    }

    fun loadJobs(refresh: Boolean = false, onComplete: (() -> Unit)? = null) {
        if (refresh) {
            lastJobDocument = null
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) { repository.clearAllJobs() }
            _hasMoreJobs.value = true
            isLoadingMoreJobs = false
        }
        if (isLoadingMoreJobs || !_hasMoreJobs.value) {
            onComplete?.invoke()
            return
        }
        isLoadingMoreJobs = true

        var query = firestore?.collection("jobs")
            ?.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            ?.limit(20)

        lastJobDocument?.let { query = query?.startAfter(it) }

        if (query == null) {
            isLoadingMoreJobs = false
            onComplete?.invoke()
            return
        }

        query.get()
            ?.addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    _hasMoreJobs.value = false
                } else {
                    lastJobDocument = snap.documents.last()
                    val newJobs = snap.documents.mapNotNull { doc ->
                        try {
                            val jobId = doc.id
                            
                            val experienceLevelValue = doc.getString("experienceLevel") ?: ""
                            val educationValue = doc.getString("education") ?: ""
                            val nationalityValue = doc.getString("nationality") ?: ""
                            val genderValue = doc.getString("gender") ?: ""
                            val companyTypeValue = doc.getString("companyType") ?: ""
                            val viewsCountValue = doc.getLong("viewsCount")?.toInt() ?: 0
                            val applicantsCountValue = doc.getLong("applicantsCount")?.toInt() ?: 0
    
                            Job(
                                id = jobId,
                                userId = doc.getString("userId") ?: "",
                                title = doc.getString("title") ?: "",
                                category = doc.getString("category") ?: "",
                                country = doc.getString("country") ?: "",
                                location = doc.getString("location") ?: "",
                                description = doc.getString("description") ?: "",
                                contact = doc.getString("contact") ?: "",
                                jobType = doc.getString("jobType") ?: "",
                                budget = doc.getString("budget") ?: "",
                                deadline = doc.getString("deadline") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                expiryDate = doc.getLong("expiryDate") ?: 0L,
                                imageUrl = doc.getString("imageUrl") ?: "",
                                experienceLevel = experienceLevelValue,
                                education = educationValue,
                                nationality = nationalityValue,
                                gender = genderValue,
                                companyType = companyTypeValue,
                                viewsCount = viewsCountValue,
                                applicantsCount = applicantsCountValue,
                                isBoosted = doc.getBoolean("isBoosted") ?: false,
                                boostEndDate = doc.getLong("boostEndDate") ?: 0L,
                                isApproved = doc.getBoolean("isApproved") ?: true,
                                companyName = doc.getString("companyName") ?: "",
                                isDeactivated = doc.getBoolean("isDeactivated") ?: false,
                                reportsCount = doc.getLong("reportsCount")?.toInt() ?: 0,
                                reports = (doc.get("reports") as? List<*>)?.filterIsInstance<Map<String, Any>>() ?: emptyList()
                            )
                        } catch (e: Exception) { null }
                    }
                    
                    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        newJobs.forEach { repository.insertJob(it) }
                    }
                    
                    if (snap.size() < 20) _hasMoreJobs.value = false
                }
                isLoadingMoreJobs = false
                onComplete?.invoke()
            }
            ?.addOnFailureListener {
                isLoadingMoreJobs = false
                onComplete?.invoke()
            }
    }

    fun loadMoreJobs() = loadJobs(refresh = false)

    fun refreshJobs() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadJobs(refresh = true) {
                _isRefreshing.value = false
            }
        }
    }

    fun loadWorkers(refresh: Boolean = false, onComplete: (() -> Unit)? = null) {
        if (refresh) {
            lastWorkerDocument = null
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) { repository.clearAllWorkers() }
            _hasMoreWorkers.value = true
            isLoadingMoreWorkers = false
        }
        if (isLoadingMoreWorkers || !_hasMoreWorkers.value) {
            onComplete?.invoke()
            return
        }
        isLoadingMoreWorkers = true

        var query = firestore?.collection("workers")
            ?.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            ?.limit(20)

        lastWorkerDocument?.let { query = query?.startAfter(it) }

        if (query == null) {
            isLoadingMoreWorkers = false
            onComplete?.invoke()
            return
        }

        query.get()
            ?.addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    _hasMoreWorkers.value = false
                } else {
                    lastWorkerDocument = snap.documents.last()
                    val newWorkers = snap.documents.mapNotNull { doc ->
                        try {
                            Worker(
                                id = doc.id,
                                userId = doc.getString("userId") ?: "",
                                name = doc.getString("name") ?: "",
                                profession = doc.getString("profession") ?: "",
                                country = doc.getString("country") ?: "",
                                location = doc.getString("location") ?: "",
                                skills = doc.getString("skills")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList(),
                                contact = doc.getString("contact") ?: "",
                                availability = doc.getString("availability") ?: "",
                                experience = doc.getString("experience") ?: "",
                                resumeLink = doc.getString("resumeLink") ?: "",
                                photoUrl = doc.getString("photoUrl") ?: "",
                                portfolioImages = doc.getString("portfolioImages") ?: "",
                                portfolioWebsite = doc.getString("portfolioWebsite") ?: "",
                                portfolioGithub = doc.getString("portfolioGithub") ?: "",
                                portfolioBehance = doc.getString("portfolioBehance") ?: "",
                                portfolioDribbble = doc.getString("portfolioDribbble") ?: "",
                                portfolioYoutube = doc.getString("portfolioYoutube") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                isBoosted = doc.getBoolean("isBoosted") ?: false,
                                boostEndDate = doc.getLong("boostEndDate") ?: 0L,
                                isApproved = doc.getBoolean("isApproved") ?: true,
                                isDeactivated = doc.getBoolean("isDeactivated") ?: false,
                                reportsCount = doc.getLong("reportsCount")?.toInt() ?: 0,
                                reports = (doc.get("reports") as? List<*>)?.filterIsInstance<Map<String, Any>>() ?: emptyList(),
                                averageRating = doc.getDouble("averageRating"),
                                totalReviews = doc.getLong("totalReviews")?.toInt()
                            )
                        } catch (e: Exception) { null }
                    }
                    
                    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        newWorkers.forEach { repository.insertWorker(it) }
                    }
                    
                    if (snap.size() < 20) _hasMoreWorkers.value = false
                }
                isLoadingMoreWorkers = false
                onComplete?.invoke()
            }
            ?.addOnFailureListener {
                isLoadingMoreWorkers = false
                onComplete?.invoke()
            }
    }

    fun loadMoreWorkers() = loadWorkers(refresh = false)

    fun refreshWorkers() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadWorkers(refresh = true) {
                _isRefreshing.value = false
            }
        }
    }

    fun checkExpiredBoosts() {
        val db = firestore ?: return
        val now = System.currentTimeMillis()
        listOf("jobs", "workers").forEach { collection ->
            db.collection(collection)
                .whereEqualTo("isBoosted", true)
                .get()
                .addOnSuccessListener { snap ->
                    snap.documents.forEach { doc ->
                        val boostEnd = doc.getLong("boostEndDate") ?: 0
                        if (boostEnd in 1 until now) {
                            doc.reference.update(
                                mapOf("isBoosted" to false, "boostEndDate" to 0)
                            )
                        }
                    }
                }
        }
    }

    fun setupAdminUser() {
        val user = auth?.currentUser
        if (user != null && user.email == com.example.ADMIN_EMAIL) {
            firestore?.collection("users")?.document(user.uid)
                ?.update("isAdmin", true)
        }
    }

    fun toggleFavoriteJob(job: Job) {
        val currentUserId = auth?.currentUser?.uid ?: return
        val jobRef = firestore?.collection("users")?.document(currentUserId)?.collection("favorites")?.document(job.id)
        
        if (job.isFavorite) {
            jobRef?.delete()
        } else {
            jobRef?.set(hashMapOf("timestamp" to System.currentTimeMillis()))
        }
    }

    fun applyJobFilters(jobs: List<Job>, filter: JobFilter): List<Job> {
        var result = jobs
        if (filter.timeRange != TimeRange.ANY) {
            val cutoff = System.currentTimeMillis() - filter.timeRange.milliseconds
            result = result.filter { it.timestamp >= cutoff }
        }
        if (filter.country.isNotEmpty()) {
            result = result.filter { it.country.equals(filter.country, ignoreCase = true) }
        }
        if (filter.city.isNotEmpty()) {
            result = result.filter { it.city.equals(filter.city, ignoreCase = true) }
        }
        if (filter.selectedCategories.isNotEmpty()) {
            result = result.filter { job ->
                filter.selectedCategories.any { cat ->
                    job.categories.contains(cat) || job.category.equals(cat, ignoreCase = true)
                }
            }
        }
        if (filter.jobTypes.isNotEmpty()) {
            result = result.filter { job -> filter.jobTypes.any { type -> job.jobType.equals(type, ignoreCase = true) } }
        }
        if (filter.experience.isNotEmpty()) {
            result = result.filter { it.experience.equals(filter.experience, ignoreCase = true) }
        }
        return result
    }

    fun applyWorkerFilters(workers: List<Worker>, filter: WorkerFilter): List<Worker> {
        var result = workers
        if (filter.timeRange != TimeRange.ANY) {
            val cutoff = System.currentTimeMillis() - filter.timeRange.milliseconds
            result = result.filter { it.timestamp >= cutoff }
        }
        if (filter.country.isNotEmpty()) {
            result = result.filter { it.country.equals(filter.country, ignoreCase = true) }
        }
        if (filter.city.isNotEmpty()) {
            result = result.filter { it.city.equals(filter.city, ignoreCase = true) }
        }
        if (filter.selectedCategories.isNotEmpty()) {
            result = result.filter { worker ->
                filter.selectedCategories.any { cat ->
                    worker.categories.contains(cat) || worker.profession.equals(cat, ignoreCase = true)
                }
            }
        }
        if (filter.profession.isNotEmpty()) {
            result = result.filter { it.profession.equals(filter.profession, ignoreCase = true) }
        }
        return result
    }

    fun toggleFavoriteWorker(worker: Worker) {
        val uid = auth?.currentUser?.uid ?: return
        val currentFavorites = _favoriteWorkerIds.value.toMutableSet()
        
        if (currentFavorites.contains(worker.id)) {
            // Remove from favorites
            currentFavorites.remove(worker.id)
            firestore?.collection("users")?.document(uid)
                ?.update("favoriteWorkers", com.google.firebase.firestore.FieldValue.arrayRemove(worker.id))
        } else {
            // Add to favorites
            currentFavorites.add(worker.id)
            firestore?.collection("users")?.document(uid)
                ?.update("favoriteWorkers", com.google.firebase.firestore.FieldValue.arrayUnion(worker.id))
        }
        
        _favoriteWorkerIds.value = currentFavorites
    }

    fun deleteJob(jobId: String) {
        val currentUserId = auth?.currentUser?.uid ?: return
        firestore?.collection("jobs")?.document(jobId)?.get()?.addOnSuccessListener { doc ->
            if (doc.getString("userId") == currentUserId) {
                firestore?.collection("jobs")?.document(jobId)?.delete()?.addOnSuccessListener {
                    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) { repository.deleteJobById(jobId) }
                    _uiMessage.tryEmit("Job deleted successfully")
                }?.addOnFailureListener { e ->
                    _uiMessage.tryEmit(e.message ?: "Delete failed")
                }
            }
        }
    }

    fun updateJob(jobId: String, updatedData: Map<String, Any>) {
        val currentUserId = auth?.currentUser?.uid ?: return
        firestore?.collection("jobs")?.document(jobId)?.get()?.addOnSuccessListener { doc ->
            if (doc.getString("userId") == currentUserId) {
                firestore?.collection("jobs")?.document(jobId)
                    ?.update(updatedData)
                    ?.addOnSuccessListener {
                        _uiMessage.tryEmit("Job updated successfully")
                    }
                    ?.addOnFailureListener {
                        _uiMessage.tryEmit("Failed to update job")
                    }
            }
        }
    }

    fun deleteWorker(workerId: String) {
        val currentUserId = auth?.currentUser?.uid ?: return
        firestore?.collection("workers")?.document(workerId)?.get()?.addOnSuccessListener { doc ->
            if (doc.getString("userId") == currentUserId) {
                firestore?.collection("workers")?.document(workerId)?.delete()?.addOnSuccessListener {
                    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) { repository.deleteWorkerById(workerId) }
                    _uiMessage.tryEmit("Profile deleted successfully")
                }?.addOnFailureListener { e ->
                    _uiMessage.tryEmit(e.message ?: "Delete failed")
                }
            }
        }
    }

    fun addJob(title: String, categories: List<String>, country: String, location: String, description: String, contact: String, type: String, budget: String = "", deadline: String = "", expiryDate: Long = 0L, imageUrl: String = "", onSuccess: ((String) -> Unit)? = null) {
        val currentUserId = auth?.currentUser?.uid ?: ""
        val jobData = hashMapOf(
            "userId" to currentUserId,
            "title" to title,
            "category" to if (categories.isNotEmpty()) categories[0] else "", // Backward compatibility
            "categories" to categories,
            "country" to country,
            "location" to location,
            "description" to description,
            "contact" to contact,
            "jobType" to type,
            "budget" to budget,
            "deadline" to deadline,
            "timestamp" to System.currentTimeMillis(),
            "expiryDate" to expiryDate,
            "imageUrl" to imageUrl,
            "isApproved" to true,
            "isBoosted" to false,
            "boostEndDate" to 0,
            "reportsCount" to 0,
            "reports" to listOf<Any>(),
            "isDeactivated" to false,
            "viewsCount" to 0,
            "applicantsCount" to 0
        )
        firestore?.collection("jobs")?.add(jobData)
            ?.addOnSuccessListener { docRef ->
                categories.forEach { category ->
                    val sanitizedCategory = category.replace(Regex("[^a-zA-Z0-9_.~%\\-]"), "_")
                    val fcmMessage = hashMapOf(
                        "topic" to "category_$sanitizedCategory",
                        "notification" to hashMapOf(
                            "title" to "New Job in $category",
                            "body" to title
                        ),
                        "data" to hashMapOf(
                            "type" to "new_job",
                            "jobId" to docRef.id
                        )
                    )
                    firestore?.collection("fcm_messages")?.add(fcmMessage)
                }
                _uiMessage.tryEmit("Job successfully posted!")
                onSuccess?.invoke(docRef.id)
            }
            ?.addOnFailureListener {
                logFirestoreError(it)
                _uiMessage.tryEmit("Failed to post job.")
            }
    }

    fun applyForJob(job: com.example.data.Job) {
        val currentUserId = auth?.currentUser?.uid ?: ""
        if (currentUserId.isEmpty()) return

        // First check if already applied
        firestore?.collection("applications")
            ?.whereEqualTo("workerId", currentUserId)
            ?.whereEqualTo("jobId", job.id)
            ?.get()
            ?.addOnSuccessListener { existing ->
                if (!existing.isEmpty) {
                    _uiMessage.tryEmit("You have already applied for this job")
                    return@addOnSuccessListener
                }

                val workerName = _currentUserProfile.value?.get("name") as? String 
                    ?: _currentUserProfile.value?.get("displayName") as? String 
                    ?: auth?.currentUser?.displayName 
                    ?: "Unknown Worker"
                val workerPhotoUrl = _currentUserProfile.value?.get("photoUrl") as? String ?: ""
                val appData = hashMapOf(
                    "workerId" to currentUserId,
                    "employerId" to job.userId,
                    "jobId" to job.id,
                    "jobTitle" to job.title,
                    "workerName" to workerName,
                    "workerPhotoUrl" to workerPhotoUrl,
                    "status" to "applied",
                    "appliedAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore?.collection("applications")?.add(appData)
                    ?.addOnSuccessListener {
                        if (job.userId.isNotEmpty()) {
                            val notifData = hashMapOf(
                                "title" to "New Application",
                                "body" to "Someone has applied to your job: ${job.title}",
                                "createdAt" to System.currentTimeMillis(),
                                "isRead" to false,
                                "type" to "job_application",
                                "relatedJobId" to job.id
                            )
                            firestore?.collection("notifications")?.document(job.userId)?.collection("items")?.add(notifData)

                            // Trigger FCM push via Firebase FCM Extension pattern if preferred
                            firestore?.collection("users")?.document(job.userId)?.get()?.addOnSuccessListener { doc ->
                                val prefs = doc.get("notificationPreferences") as? Map<String, Boolean>
                                val notifyNewApp = prefs?.get("new_application") ?: true
                                if (notifyNewApp) {
                                    val fcmToken = doc.getString("fcmToken")
                                    if (!fcmToken.isNullOrEmpty()) {
                                        val fcmMessage = hashMapOf(
                                            "token" to fcmToken,
                                            "notification" to hashMapOf(
                                                "title" to "New Application",
                                                "body" to "Someone has applied to your job: ${job.title}"
                                            ),
                                            "data" to hashMapOf(
                                                "type" to "job_application",
                                                "relatedJobId" to job.id
                                            )
                                        )
                                        firestore?.collection("fcm_messages")?.add(fcmMessage)
                                    }
                                }
                            }
                        }

                        firestore?.collection("jobs")?.document(job.id)?.update(
                            "applicantsCount", com.google.firebase.firestore.FieldValue.increment(1)
                        )

                        _uiMessage.tryEmit("Successfully applied for ${job.title}!")
                    }
                    ?.addOnFailureListener {
                        logFirestoreError(it)
                        _uiMessage.tryEmit("Failed to apply.")
                    }
            }
            ?.addOnFailureListener {
                logFirestoreError(it)
                _uiMessage.tryEmit("Error checking application status.")
            }
    }

    fun sendSupportRequest(subject: String, message: String, onResult: (Boolean) -> Unit) {
        val user = auth?.currentUser
        if (user == null && !isGuest) {
            onResult(false)
            return
        }
        val email = user?.email ?: "guest@talentmarket.com"
        val userId = user?.uid ?: "guest_user"
        val requestId = firestore?.collection("support_requests")?.document()?.id ?: java.util.UUID.randomUUID().toString()
        
        val createdAt = System.currentTimeMillis()
        val status = "Open"
        
        val supportData = hashMapOf(
            "id" to requestId,
            "userId" to userId,
            "email" to email,
            "subject" to subject,
            "message" to message,
            "createdAt" to createdAt,
            "status" to status
        )
        
        firestore?.collection("support_requests")?.document(requestId)?.set(supportData)
            ?.addOnSuccessListener {
                triggerEmailNotification(email, subject, message)
                onResult(true)
            }
            ?.addOnFailureListener {
                logFirestoreError(it)
                onResult(false)
            }
    }

    private fun triggerEmailNotification(email: String, subject: String, message: String) {
        // Left empty or write to a dedicated "mail" collection if a mail extension is configured
        // firestore?.collection("mail")?.add(
        //     hashMapOf(
        //         "to" to "support@yoursite.com",
        //         "message" to hashMapOf(
        //             "subject" to "New Support Request: $subject",
        //             "text" to "From: $email\n\n$message"
        //         )
        //     )
        // )
    }

    fun loadAllSupportRequests() {
        firestore?.collection("support_requests")
            ?.orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            ?.limit(100)
            ?.addSnapshotListener { snapshot, error ->
                logFirestoreError(error)
                if (snapshot != null) {
                    val requests = snapshot.documents.mapNotNull { doc ->
                        try {
                            com.example.data.SupportRequest(
                                id = doc.getString("id") ?: doc.id,
                                userId = doc.getString("userId") ?: "",
                                email = doc.getString("email") ?: "",
                                subject = doc.getString("subject") ?: "",
                                message = doc.getString("message") ?: "",
                                createdAt = doc.getLong("createdAt") ?: 0L,
                                status = doc.getString("status") ?: "Open"
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedByDescending { it.createdAt }
                    _supportRequests.value = requests
                }
            }
    }

    fun updateSupportRequestStatus(requestId: String, newStatus: String) {
        if (requestId.isEmpty()) return
        firestore?.collection("support_requests")?.document(requestId)
            ?.update("status", newStatus)
    }

    fun updateNotificationPreference(key: String, value: Boolean) {
        val currentUserId = auth?.currentUser?.uid ?: return
        firestore?.collection("users")?.document(currentUserId)?.update("notificationPreferences.$key", value)
    }

    fun updateWorkerProfile(workerId: String, name: String, profession: String, bio: String, skills: String) {
        if (workerId.isEmpty()) return
        val updates = mapOf(
            "name" to name,
            "profession" to profession,
            "experience" to bio,
            "skills" to skills
        )
        firestore?.collection("workers")?.document(workerId)?.update(updates)
    }

    fun addWorker(
        name: String, profession: String, country: String, location: String, 
        skills: String, contact: String, availability: String,
        experience: String = "", resumeLink: String = "", photoUrl: String = "",
        portfolioImages: String = "", portfolioWebsite: String = "",
        portfolioGithub: String = "", portfolioBehance: String = "",
        portfolioDribbble: String = "", portfolioYoutube: String = "",
        onSuccess: ((String) -> Unit)? = null
    ) {
        val currentUserId = auth?.currentUser?.uid ?: ""
        val workerData = hashMapOf(
            "userId" to currentUserId,
            "name" to name,
            "profession" to profession,
            "country" to country,
            "location" to location,
            "skills" to skills,
            "contact" to contact,
            "availability" to availability,
            "experience" to experience,
            "resumeLink" to resumeLink,
            "photoUrl" to photoUrl,
            "portfolioImages" to portfolioImages,
            "portfolioWebsite" to portfolioWebsite,
            "portfolioGithub" to portfolioGithub,
            "portfolioBehance" to portfolioBehance,
            "portfolioDribbble" to portfolioDribbble,
            "portfolioYoutube" to portfolioYoutube,
            "timestamp" to System.currentTimeMillis(),
            "isApproved" to true,
            "isDeactivated" to false,
            "reportsCount" to 0,
            "reports" to listOf<Any>()
        )
        firestore?.collection("workers")?.add(workerData)
            ?.addOnSuccessListener { docRef ->
                val sanitizedCategory = profession.replace(Regex("[^a-zA-Z0-9_.~%\\-]"), "_")
                val fcmMessage = hashMapOf(
                    "topic" to "category_$sanitizedCategory",
                    "notification" to hashMapOf(
                        "title" to "New Worker in $profession",
                        "body" to "$name has published a profile!"
                    ),
                    "data" to hashMapOf(
                        "type" to "new_worker",
                        "workerId" to docRef.id
                    )
                )
                firestore?.collection("fcm_messages")?.add(fcmMessage)
                
                _uiMessage.tryEmit("Profile successfully published!")
                onSuccess?.invoke(docRef.id)
            }
            ?.addOnFailureListener {
                logFirestoreError(it)
                _uiMessage.tryEmit("Failed to publish profile.")
            }
    }

    fun getCategoryJobCounts(jobs: List<Job>): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        jobs.forEach { job ->
            job.categories.forEach { category ->
                counts[category] = (counts[category] ?: 0) + 1
            }
            // Also count single category field
            if (job.category.isNotEmpty()) {
                counts[job.category] = (counts[job.category] ?: 0) + 1
            }
        }
        return counts
    }

    fun getCategoryWorkerCounts(workers: List<Worker>): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        workers.forEach { worker ->
            worker.categories.forEach { category ->
                counts[category] = (counts[category] ?: 0) + 1
            }
            // Also count single category field (if any stored in profession)
            if (worker.profession.isNotEmpty()) {
                counts[worker.profession] = (counts[worker.profession] ?: 0) + 1
            }
        }
        return counts
    }

    fun toggleFavoriteCategory(category: String) {
        val userId = auth?.currentUser?.uid ?: return
        val isFav = _favoriteCategories.value.contains(category)
        val catRef = firestore?.collection("users")?.document(userId)?.collection("favorite_categories")?.document(category)
        val sanitizedCategory = category.replace(Regex("[^a-zA-Z0-9_.~%\\-]"), "_")
        if (isFav) {
            catRef?.delete()
            try {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic("category_$sanitizedCategory")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            catRef?.set(hashMapOf("timestamp" to System.currentTimeMillis()))
            try {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("category_$sanitizedCategory")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendNotification(
        targetUserId: String,
        title: String,
        body: String,
        type: String,
        relatedJobId: String = ""
    ) {
        if (targetUserId.isEmpty()) return
        val notifData = hashMapOf(
            "title" to title,
            "body" to body,
            "isRead" to false,
            "createdAt" to System.currentTimeMillis(),
            "type" to type,
            "relatedJobId" to relatedJobId
        )
        // 1. Path: notifications/{userId}/items/
        firestore?.collection("notifications")?.document(targetUserId)?.collection("items")?.add(notifData)
    }

    private val viewedJobs = mutableSetOf<String>()

    fun logJobView(job: Job) {
        val currentUserId = auth?.currentUser?.uid ?: ""
        if (currentUserId.isNotEmpty() && currentUserId != job.userId) {
            if (!viewedJobs.contains(job.id)) {
                viewedJobs.add(job.id)
                firestore?.collection("jobs")?.document(job.id)?.update(
                    "viewsCount", com.google.firebase.firestore.FieldValue.increment(1)
                )
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        val userId = auth?.currentUser?.uid ?: return
        firestore?.collection("notifications")?.document(userId)?.collection("items")?.document(notificationId)
            ?.update("isRead", true)
    }

    fun toggleNotificationReadStatus(notificationId: String, isUnread: Boolean) {
        val userId = auth?.currentUser?.uid ?: return
        val newStatus = !isUnread
        firestore?.collection("notifications")?.document(userId)?.collection("items")?.document(notificationId)
            ?.update("isRead", newStatus)
    }

    fun markAllNotificationsAsRead() {
        val userId = auth?.currentUser?.uid ?: return
        val db = firestore ?: return
        db.collection("notifications").document(userId).collection("items").get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    doc.reference.update("isRead", true)
                }
            }
    }

    fun updateCompanyProfile(
        companyId: String,
        companyName: String,
        logoUrl: String,
        industry: String,
        companySize: String,
        location: String,
        websiteUrl: String,
        phoneNumber: String,
        description: String,
        foundedYear: String,
        linkedinUrl: String,
        instagramUrl: String,
        twitterUrl: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        if (companyId.isEmpty() || firestore == null) {
            onComplete(false)
            return
        }
        val profileRef = firestore?.collection("companies")?.document(companyId)?.collection("profile")?.document("info")
        val profileData = hashMapOf<String, Any>(
            "companyName" to companyName,
            "logoUrl" to logoUrl,
            "industry" to industry,
            "companySize" to companySize,
            "location" to location,
            "websiteUrl" to websiteUrl,
            "phoneNumber" to phoneNumber,
            "description" to description,
            "foundedYear" to foundedYear,
            "linkedinUrl" to linkedinUrl,
            "instagramUrl" to instagramUrl,
            "twitterUrl" to twitterUrl
        )

        profileRef?.get()?.addOnCompleteListener { task ->
            val isVerifiedValue = if (task.isSuccessful && task.result != null) {
                when (val raw = task.result.get("isVerified")) {
                    is Boolean -> raw
                    is String -> raw.toBoolean()
                    else -> false
                }
            } else {
                false
            }
            profileData["isVerified"] = isVerifiedValue

            profileRef.set(profileData)
                .addOnSuccessListener {
                    val updatedProfile = com.example.data.CompanyProfile(
                        companyId = companyId,
                        companyName = companyName,
                        logoUrl = logoUrl,
                        industry = industry,
                        companySize = companySize,
                        location = location,
                        websiteUrl = websiteUrl,
                        phoneNumber = phoneNumber,
                        description = description,
                        foundedYear = foundedYear,
                        linkedinUrl = linkedinUrl,
                        instagramUrl = instagramUrl,
                        twitterUrl = twitterUrl,
                        isVerified = isVerifiedValue
                    )
                    _companyProfiles.value = _companyProfiles.value + (companyId to updatedProfile)
                    onComplete(true)
                }
                .addOnFailureListener {
                    logFirestoreError(it)
                    onComplete(false)
                }
        } ?: onComplete(false)
    }

    fun loadCompanyProfile(companyId: String) {
        if (companyId.isBlank()) return
        if (_companyProfiles.value.containsKey(companyId)) return

        firestore?.collection("companies")?.document(companyId)?.collection("profile")?.document("info")?.get()
            ?.addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    try {
                        val isVerifiedValue = when (val raw = doc.get("isVerified")) {
                            is Boolean -> raw
                            is String -> raw.toBoolean()
                            else -> false
                        }
                        val profile = com.example.data.CompanyProfile(
                            companyId = companyId,
                            companyName = doc.getString("companyName") ?: "",
                            logoUrl = doc.getString("logoUrl") ?: "",
                            industry = doc.getString("industry") ?: "",
                            companySize = doc.getString("companySize") ?: "",
                            location = doc.getString("location") ?: "",
                            websiteUrl = doc.getString("websiteUrl") ?: "",
                            phoneNumber = doc.getString("phoneNumber") ?: "",
                            description = doc.getString("description") ?: "",
                            foundedYear = doc.getString("foundedYear") ?: "",
                            linkedinUrl = doc.getString("linkedinUrl") ?: "",
                            instagramUrl = doc.getString("instagramUrl") ?: "",
                            twitterUrl = doc.getString("twitterUrl") ?: "",
                            isVerified = isVerifiedValue
                        )
                        _companyProfiles.value = _companyProfiles.value + (companyId to profile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            ?.addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun showLocalNotification(context: android.content.Context, title: String, content: String) {
        try {
            val channelId = "jobs_notifications_channel"
            val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Job Alerts",
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alerts for new jobs and notifications"
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun reportPost(
        postId: String,
        postType: String, // "job" or "worker"
        reason: String,
        reportedUserId: String
    ) {
        val currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) return
        val db = firestore ?: return
        val collection = if (postType == "job") "jobs" else "workers"
        val docRef = db.collection(collection).document(postId)
        
        // Check if user already reported this post via transaction
        db.runTransaction { transaction ->
            val doc = transaction.get(docRef)
            val existingReports = (doc.get("reports") as? List<*>) ?: emptyList<Any>()
            
            // Prevent duplicate reports from same user
            val alreadyReported = existingReports
                .filterIsInstance<Map<*, *>>()
                .any { it["userId"] == currentUserId }
            
            if (alreadyReported) {
                return@runTransaction Pair(false, 0)
            }
            
            // Add report
            val reportData = mapOf(
                "userId" to currentUserId,
                "reason" to reason,
                "timestamp" to System.currentTimeMillis()
            )
            
            val currentCount = (doc.getLong("reportsCount") ?: 0L).toInt()
            val newCount = currentCount + 1
            
            // If 10+ reports, deactivate post automatically
            val shouldDeactivate = newCount >= 10
            
            val updates = mutableMapOf<String, Any>(
                "reports" to com.google.firebase.firestore.FieldValue.arrayUnion(reportData),
                "reportsCount" to newCount
            )
            
            if (shouldDeactivate) {
                updates["isDeactivated"] = true
            }
            
            transaction.update(docRef, updates)
            Pair(shouldDeactivate, newCount)
        }.addOnSuccessListener { result ->
            @Suppress("UNCHECKED_CAST")
            val p = result as? Pair<Boolean, Int> ?: return@addOnSuccessListener
            val shouldDeactivate = p.first
            val newCount = p.second
            if (newCount == 0) return@addOnSuccessListener // Already reported

            if (shouldDeactivate) {
                val notifData = hashMapOf(
                    "title" to "Post Deactivated",
                    "body" to "Your $postType post was automatically deactivated due to multiple reports.",
                    "createdAt" to System.currentTimeMillis(),
                    "isRead" to false,
                    "type" to "post_status",
                    "relatedJobId" to postId
                )
                db.collection("notifications").document(reportedUserId).collection("items").add(notifData)

                // Trigger FCM push via Firebase FCM Extension pattern if preferred
                db.collection("users").document(reportedUserId).get().addOnSuccessListener { doc ->
                    val prefs = doc.get("notificationPreferences") as? Map<String, Boolean>
                    val notifyReports = prefs?.get("report_updates") ?: true
                    if (notifyReports) {
                        val fcmToken = doc.getString("fcmToken")
                        if (!fcmToken.isNullOrEmpty()) {
                            val fcmMessage = hashMapOf(
                                "token" to fcmToken,
                                "notification" to hashMapOf(
                                    "title" to "Post Deactivated",
                                    "body" to "Your $postType post was automatically deactivated due to multiple reports."
                                ),
                                "data" to hashMapOf(
                                    "type" to "post_status",
                                    "relatedJobId" to postId
                                )
                            )
                            db.collection("fcm_messages").add(fcmMessage)
                        }
                    }
                }
            }

            // Save to reports collection for admin tracking
            db.collection("reports").add(
                mapOf(
                    "postId" to postId,
                    "postType" to postType,
                    "reportedUserId" to reportedUserId,
                    "reportedByUserId" to currentUserId,
                    "reason" to reason,
                    "timestamp" to System.currentTimeMillis(),
                    "totalReports" to newCount,
                    "isDeactivated" to shouldDeactivate
                )
            )
                
            // Notify admin if 10+ reports
            if (shouldDeactivate) {
                db.collection("notifications")
                    .document("admin")
                    .collection("items")
                    .add(mapOf(
                        "title" to "⚠️ Post Auto-Deactivated",
                        "body" to "A $postType post received $newCount reports and was deactivated.",
                        "type" to "report_alert",
                        "postId" to postId,
                        "postType" to postType,
                        "isRead" to false,
                        "createdAt" to System.currentTimeMillis()
                    ))
            }
        }.addOnFailureListener {
            logFirestoreError(it)
        }
    }

    fun loadReviews(targetId: String) {
        reviewsListener?.remove()
        reviewsListener = firestore?.collection("reviews")
            ?.whereEqualTo("targetId", targetId)
            ?.addSnapshotListener { snap, err ->
                logFirestoreError(err)
                val reviewList = snap?.toObjects(com.example.data.Review::class.java) ?: emptyList()
                _reviews.value = reviewList.sortedByDescending { it.createdAt }.take(20)
            }
    }

    fun submitReview(
        targetId: String,
        targetType: String,
        rating: Int,
        comment: String
    ) {
        val currentUser = auth?.currentUser ?: return
        viewModelScope.launch {
            val reviewData = hashMapOf(
                "reviewerId" to currentUser.uid,
                "reviewerName" to (currentUser.displayName ?: "User"),
                "reviewerType" to (_currentUserProfile.value?.get("userType") as? String ?: ""),
                "targetId" to targetId,
                "targetType" to targetType,
                "rating" to rating,
                "comment" to comment,
                "jobId" to "",
                "createdAt" to System.currentTimeMillis()
            )
            firestore?.collection("reviews")
                ?.add(reviewData)
                ?.addOnSuccessListener {
                    updateAverageRating(targetId, targetType, rating)
                }
        }
    }

    private fun updateAverageRating(targetId: String, targetType: String, newRating: Int) {
        val fs = firestore ?: return
        val userTask = fs.collection("users").document(targetId).get()
        val workerTask = fs.collection("workers").whereEqualTo("userId", targetId).limit(1).get()

        com.google.android.gms.tasks.Tasks.whenAllComplete(userTask, workerTask)
            .addOnSuccessListener {
                val userDoc = userTask.result
                val workerSnap = workerTask.result

                if (userDoc != null && userDoc.exists()) {
                    val currentAverage = userDoc.getDouble("averageRating") ?: 0.0
                    val currentTotal = userDoc.getLong("totalReviews") ?: 0L
                    val newTotal = currentTotal + 1
                    val newAverage = ((currentAverage * currentTotal) + newRating.toDouble()) / newTotal.toDouble()

                    val batch = fs.batch()
                    batch.update(fs.collection("users").document(targetId), mapOf(
                        "averageRating" to newAverage,
                        "totalReviews" to newTotal
                    ))

                    if (workerSnap != null && !workerSnap.isEmpty) {
                        val workerDocId = workerSnap.documents.first().id
                        batch.update(fs.collection("workers").document(workerDocId), mapOf(
                            "averageRating" to newAverage,
                            "totalReviews" to newTotal
                        ))
                    }

                    batch.commit().addOnFailureListener { err ->
                        logFirestoreError(err)
                    }
                }
            }
            .addOnFailureListener { err ->
                logFirestoreError(err)
            }
    }

    override fun onCleared() {
        super.onCleared()
        reviewsListener?.remove()
        applicationsListener?.remove()
    }

    companion object {
        var appContext: android.content.Context? = null
    }
}

class TalentViewModelFactory(private val repository: TalentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TalentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TalentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
