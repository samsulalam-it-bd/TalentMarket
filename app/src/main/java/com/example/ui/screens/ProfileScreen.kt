package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.R
import com.example.ui.TalentViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.components.AdminMenuCard

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.data.Worker
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: TalentViewModel, navController: NavController) {
    val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    val user = auth?.currentUser

    val favoriteJobs by viewModel.favoriteJobs.collectAsStateWithLifecycle()
    val favoriteWorkers by viewModel.favoriteWorkers.collectAsStateWithLifecycle()
    val workers by viewModel.workers.collectAsStateWithLifecycle()
    val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    val companyProfiles by viewModel.companyProfiles.collectAsStateWithLifecycle()
    val nameState by viewModel.currentUserProfile.collectAsStateWithLifecycle(null)
    
    val displayName = remember(nameState, user) {
        com.example.ui.UserService.getDisplayName(nameState, user)
    }
    
    val initials = remember(displayName) {
        val parts = displayName.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        if (parts.isEmpty()) "U"
        else if (parts.size == 1) parts[0].take(2).uppercase()
        else (parts[0].take(1) + parts[1].take(1)).uppercase()
    }

    val allRawWorkers by viewModel.allRawWorkers.collectAsStateWithLifecycle()
    val myWorkerProfiles = allRawWorkers.filter { it.userId == user?.uid }.sortedByDescending { it.timestamp }
    val myWorkerProfile = myWorkerProfiles.firstOrNull()
    val myCompanyProfile = companyProfiles[user?.uid ?: ""]
    val allRawJobs by viewModel.allRawJobs.collectAsStateWithLifecycle()
    val myJobs = allRawJobs.filter { it.userId == user?.uid }.sortedByDescending { it.timestamp }
    var showProfileDialog by remember { mutableStateOf<Worker?>(null) }
    var showEditProfileDialog by remember { mutableStateOf<Worker?>(null) }
    var showCreateWorkerDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    showProfileDialog?.let { currentWorker ->
        WorkerProfileDialog(
            worker = currentWorker,
            isGuest = viewModel.isGuest,
            viewModel = viewModel,
            onDismiss = { showProfileDialog = null },
            onActionClick = {}
        )
    }

    if (showEditProfileDialog != null) {
        val workerToEdit = showEditProfileDialog!!
        EditWorkerProfileDialog(
            worker = workerToEdit,
            onDismiss = { showEditProfileDialog = null },
            onSave = { name, profession, bio, skills ->
                viewModel.updateWorkerProfile(workerToEdit.id, name, profession, bio, skills)
                showEditProfileDialog = null
            }
        )
    }

    if (showCreateWorkerDialog) {
        val isAr = com.example.LocalCurrentLanguage.current == "ar"
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showCreateWorkerDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                topBar = {
                    @OptIn(ExperimentalMaterial3Api::class)
                    TopAppBar(
                        title = { Text(if (isAr) "إنشاء ملف التعريفي" else "Create Profile") },
                        navigationIcon = {
                            IconButton(onClick = { showCreateWorkerDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    PostWorkerForm(
                        viewModel = viewModel,
                        onSignUpRequired = { showCreateWorkerDialog = false },
                        onPostSuccess = { showCreateWorkerDialog = false }
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Logout", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    auth?.signOut()
                    com.example.utils.ChatConnectionManager.cleanUp()
                    viewModel.isGuest = false
                    viewModel.currentUserType = null
                    navController.navigate(com.example.ui.Screen.Login.route) {
                        popUpTo(0)
                    }
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (viewModel.isGuest || user == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.signup_required), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { 
                navController.navigate(com.example.ui.Screen.Login.route) {
                    popUpTo(com.example.ui.Screen.Profile.route) { inclusive = true }
                } 
            }) {
                Text(stringResource(R.string.signup_now))
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome, $displayName!", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val photoUrl = nameState?.get("photoUrl") as? String
                    if (!photoUrl.isNullOrEmpty()) {
                        coil.compose.AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                val typeText = if (viewModel.currentUserType == "worker") "Job Seeker" else if (viewModel.currentUserType == "employer") "Employer" else "User"
                Text(
                    text = typeText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                val userTypeStr = nameState?.get("userType") as? String ?: "worker"
                val completion = remember(myWorkerProfile, myCompanyProfile, viewModel.currentUserType) {
                    if (viewModel.currentUserType == "worker") {
                        if (myWorkerProfile == null) {
                            10
                        } else {
                            var score = 10
                            if (myWorkerProfile.name.isNotEmpty()) score += 15
                            if (myWorkerProfile.profession.isNotEmpty()) score += 15
                            if (myWorkerProfile.skills.isNotEmpty()) score += 20
                            if (myWorkerProfile.location.isNotEmpty()) score += 10
                            if (myWorkerProfile.experience.isNotEmpty()) score += 15
                            if (myWorkerProfile.contact.isNotEmpty()) score += 15
                            score.coerceAtMost(100)
                        }
                    } else if (viewModel.currentUserType == "employer") {
                        if (myCompanyProfile == null) {
                            15
                        } else {
                            var score = 15
                            if (myCompanyProfile.companyName.isNotEmpty()) score += 15
                            if (myCompanyProfile.logoUrl.isNotEmpty()) score += 15
                            if (myCompanyProfile.industry.isNotEmpty()) score += 15
                            if (myCompanyProfile.location.isNotEmpty()) score += 15
                            if (myCompanyProfile.companySize.isNotEmpty()) score += 10
                            if (myCompanyProfile.phoneNumber.isNotEmpty()) score += 15
                            score.coerceAtMost(100)
                        }
                    } else {
                        100
                    }
                }

                if (completion < 100) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFF1A1A2E)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (completion < 50) androidx.compose.ui.graphics.Color(0xFFEF4444).copy(0.4f)
                            else androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val isAr = com.example.LocalCurrentLanguage.current == "ar"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (isAr) "إكمال الملف الشخصي" else "Profile Completion",
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "${completion}%",
                                    color = when {
                                        completion < 40 -> androidx.compose.ui.graphics.Color(0xFFEF4444)
                                        completion < 70 -> androidx.compose.ui.graphics.Color(0xFFD4AF37)
                                        else -> androidx.compose.ui.graphics.Color(0xFF10B981)
                                    },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                    
                            Spacer(Modifier.height(10.dp))
                    
                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(
                                        androidx.compose.ui.graphics.Color(0xFF374151),
                                        RoundedCornerShape(3.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(completion / 100f)
                                        .height(6.dp)
                                        .background(
                                            when {
                                                completion < 40 -> androidx.compose.ui.graphics.Color(0xFFEF4444)
                                                completion < 70 -> androidx.compose.ui.graphics.Color(0xFFD4AF37)
                                                else -> androidx.compose.ui.graphics.Color(0xFF10B981)
                                            },
                                            RoundedCornerShape(3.dp)
                                        )
                                )
                            }
                    
                            Spacer(Modifier.height(10.dp))
                    
                            val missingText = when {
                                completion < 40 -> if (isAr) "أضف صورتك، مهاراتك ونبذتك التعريفية ليتم ملاحظتك" else "Add your photo, skills and bio to get noticed"
                                completion < 70 -> if (isAr) "اقتربت من الانتهاء! أضف المزيد من التفاصيل إلى ملفك الشخصي" else "Almost there! Add more details to your profile"
                                else -> if (isAr) "ملفك الشخصي مكتمل تقريباً! فقط بضع تفاصيل إضافية" else "You're almost complete! Just a few more details"
                            }
                    
                            Text(
                                missingText,
                                color = androidx.compose.ui.graphics.Color(0xFF9CA3AF),
                                style = MaterialTheme.typography.bodySmall
                            )
                    
                            Spacer(Modifier.height(10.dp))
                    
                            OutlinedButton(
                                onClick = { 
                                    if (viewModel.currentUserType == "worker") {
                                        if (myWorkerProfile != null) {
                                            showEditProfileDialog = myWorkerProfile
                                        } else {
                                            showCreateWorkerDialog = true
                                        }
                                    } else if (viewModel.currentUserType == "employer") {
                                        navController.navigate("company_profile/${user?.uid ?: ""}")
                                    } else {
                                        showCreateWorkerDialog = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.5.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = androidx.compose.ui.graphics.Color(0xFFD4AF37)
                                )
                            ) {
                                Text(
                                    if (isAr) "أكمل الملف الشخصي" else "Complete Profile",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                if (viewModel.currentUserType == "worker" && myWorkerProfile == null) {
                    val isAr = com.example.LocalCurrentLanguage.current == "ar"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = if (isAr) "أكمل ملفك الشخصي 🚀" else "Complete Your Profile 🚀",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isAr) {
                                    "تميز أمام كبار أصحاب العمل من خلال إضافة مهاراتك وخبراتك ونبذة تعريفية رائعة."
                                } else {
                                    "Stand out to top employers by adding your skills, experience, and an awesome bio."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showCreateWorkerDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isAr) "أكمل الملف الشخصي" else "Complete Profile")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileItem(label = "Email", value = user.email ?: "")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        val rawUidText = user.uid.filter { it.isDigit() }
                        val baseNum = if (rawUidText.length >= 9) rawUidText else {
                            kotlin.math.abs(user.uid.hashCode()).toString().padStart(9, '0')
                        }
                        val safeBase = baseNum.padEnd(9, '0')
                        val formattedNumericId = "${safeBase.substring(0, 3)}-${safeBase.substring(3, 6)}-${safeBase.substring(6, 9)}"
                        ProfileItem(label = "User ID", value = formattedNumericId)
                        
                        // Notification Settings
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Notification Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        
                        val notifPrefs = nameState?.get("notificationPreferences") as? Map<String, Boolean> ?: emptyMap()
                        
                        val newAppPref = notifPrefs["new_application"] ?: true
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("New Applications", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Switch(
                                checked = newAppPref,
                                onCheckedChange = { v -> viewModel.updateNotificationPreference("new_application", v) }
                            )
                        }
                        val reportPref = notifPrefs["report_updates"] ?: true
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Report Updates", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Switch(
                                checked = reportPref,
                                onCheckedChange = { v -> viewModel.updateNotificationPreference("report_updates", v) }
                            )
                        }

                        if (viewModel.currentUserType == "employer") {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "My Company Profile",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = { navController.navigate("company_profile/${user.uid}") }) {
                                    Icon(
                                        imageVector = if (myCompanyProfile != null) Icons.Default.Edit else Icons.Default.Add,
                                        contentDescription = "Manage Company Profile",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            if (myCompanyProfile != null) {
                                ProfileItem(label = "Company Name", value = myCompanyProfile.companyName)
                                Spacer(Modifier.height(8.dp))
                                ProfileItem(label = "Industry / Business", value = myCompanyProfile.industry)
                                Spacer(Modifier.height(8.dp))
                                ProfileItem(label = "Location", value = myCompanyProfile.location)
                                Spacer(Modifier.height(8.dp))
                                ProfileItem(label = "Company Size", value = myCompanyProfile.companySize)
                            } else {
                                Text(
                                    text = "No Company Profile configured yet. Tap the + icon to set up your corporate brand!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (viewModel.currentUserType == "employer") "My Posted Jobs" else "My Posts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Button(
                        onClick = {
                            if (viewModel.currentUserType == "employer") {
                                navController.navigate(com.example.ui.Screen.Post.route)
                            } else {
                                navController.navigate(com.example.ui.Screen.Post.route)
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Post", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        val isAr = com.example.LocalCurrentLanguage.current == "ar"
                        Text(if (viewModel.currentUserType == "employer") (if (isAr) "انشر وظيفة" else "Post Job") else (if (isAr) "إنشاء ملف" else "Post Profile"))
                    }
                }
            }
            if (viewModel.currentUserType == "employer") {
                if (myJobs.isNotEmpty()) {
                    items(myJobs) { job ->
                        val localContext = androidx.compose.ui.platform.LocalContext.current
                        LaunchedEffect(job.userId) {
                            viewModel.loadCompanyProfile(job.userId)
                        }
                        val companyProfile = companyProfiles[job.userId]
                        val companyName = companyProfile?.companyName ?: "Independent Recruiter"
                        Column {
                            if (job.isBoosted) {
                                val isArJob = com.example.LocalCurrentLanguage.current == "ar"
                                val diff = job.boostEndDate - System.currentTimeMillis()
                                val remainingText = if (diff > 0) {
                                    val days = diff / (24L * 60 * 60 * 1000)
                                    val hours = (diff % (24L * 60 * 60 * 1000)) / (60 * 60 * 1000)
                                    if (isArJob) {
                                        if (days > 0) "متبقي ${days} يوم و ${hours} ساعة" else "متبقي ${hours} ساعة"
                                    } else {
                                        if (days > 0) "${days}d ${hours}h left" else "${hours}h left"
                                    }
                                } else {
                                    if (isArJob) "منتهي" else "Expired"
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .background(Color(0xFFD4AF37).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isArJob) "⚡ حملة إعلانية مميزة ($remainingText)" else "⚡ FEATURED CAMPAIGN ($remainingText)",
                                        color = Color(0xFFD4AF37),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            
                            // Post Statistics Box
                            val timeAgo = android.text.format.DateUtils.getRelativeTimeSpanString(job.timestamp, System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS)
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Posted: $timeAgo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Person, contentDescription = "Views", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text("${job.viewsCount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            JobCard(
                                job = job,
                                companyName = companyName,
                                onActionClick = { navController.navigate("job_details/${job.id}") },
                                onApplyClick = { viewModel.applyForJob(job) },
                                onFavoriteClick = { viewModel.toggleFavoriteJob(job) }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { navController.navigate("edit_job/${job.id}") }
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Edit")
                                }
                                Button(
                                    onClick = { viewModel.deleteJob(job.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Delete")
                                }
                                Button(
                                    onClick = { navController.navigate("boost/${job.id}") }
                                ) {
                                    Icon(Icons.Filled.Star, contentDescription = "Boost", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Boost")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text("You haven't posted any jobs yet. Head over to the Post screen to hire talent!", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else if (viewModel.currentUserType == "worker") {
                if (myWorkerProfiles.isNotEmpty()) {
                    items(myWorkerProfiles) { worker ->
                        val localContext = androidx.compose.ui.platform.LocalContext.current
                        Column {
                            if (worker.isBoosted) {
                                val isArProfile = com.example.LocalCurrentLanguage.current == "ar"
                                val diff = worker.boostEndDate - System.currentTimeMillis()
                                val remainingText = if (diff > 0) {
                                    val days = diff / (24L * 60 * 60 * 1000)
                                    val hours = (diff % (24L * 60 * 60 * 1000)) / (60 * 60 * 1000)
                                    if (isArProfile) {
                                        if (days > 0) "متبقي ${days} يوم و ${hours} ساعة" else "متبقي ${hours} ساعة"
                                    } else {
                                        if (days > 0) "${days}d ${hours}h left" else "${hours}h left"
                                    }
                                } else {
                                    if (isArProfile) "منتهي" else "Expired"
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .background(Color(0xFFD4AF37).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isArProfile) "👑 ملف شخصي مميز" else "👑 Premium Featured Profile",
                                                color = Color(0xFFD4AF37),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isArProfile) {
                                                "تمت ترقية ملفك الشخصي وسيظهر في مقدمة نتائج البحث للعمال.\nالوقت المتبقي: $remainingText"
                                            } else {
                                                "Your profile is boosted and appears at the top of worker searches.\nTime remaining: $remainingText"
                                            },
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            
                            val timeAgo = android.text.format.DateUtils.getRelativeTimeSpanString(worker.timestamp, System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS)
                            Text("Posted: $timeAgo", modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            WorkerCard(
                                worker = worker,
                                onActionClick = { showProfileDialog = worker },
                                onCardClick = { showProfileDialog = worker },
                                onFavoriteClick = { viewModel.toggleFavoriteWorker(worker) }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { showEditProfileDialog = worker }
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Edit")
                                }
                                Button(
                                    onClick = { viewModel.deleteWorker(worker.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Delete")
                                }
                                Button(
                                    onClick = { navController.navigate("boost/${worker.id}") }
                                ) {
                                    Icon(Icons.Filled.Star, contentDescription = "Boost", modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Boost")
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text("You haven't posted your worker profile yet. Head over to the Post screen to increase your visibility!", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Favorite Jobs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 8.dp)
                )
            }
            if (favoriteJobs.isNotEmpty()) {
                items(favoriteJobs) { job ->
                    val localContext = androidx.compose.ui.platform.LocalContext.current
                    LaunchedEffect(job.userId) {
                        viewModel.loadCompanyProfile(job.userId)
                    }
                    val companyProfile = companyProfiles[job.userId]
                    val companyName = companyProfile?.companyName ?: "Independent Recruiter"
                    JobCard(
                        job = job,
                        companyName = companyName,
                        onActionClick = { navController.navigate("job_details/${job.id}") },
                        onApplyClick = { viewModel.applyForJob(job) },
                        onFavoriteClick = { viewModel.toggleFavoriteJob(job) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text("No favorite jobs yet.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                Text(
                    text = "Favorite Workers",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 8.dp)
                )
            }
            if (favoriteWorkers.isNotEmpty()) {
                items(favoriteWorkers) { worker ->
                    WorkerCard(
                        worker = worker,
                        onCardClick = { showProfileDialog = worker },
                        onActionClick = { showProfileDialog = worker },
                        onFavoriteClick = { viewModel.toggleFavoriteWorker(worker) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                item {
                    EmptyStateView(
                        emoji = "⭐",
                        title = "No Saved Workers",
                        subtitle = "Save worker profiles you like\nto contact them later",
                        actionLabel = "Browse Workers",
                        onAction = { navController.navigate("workers") }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        if (viewModel.currentUserType == "worker") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("my_applications") }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Work,
                                    contentDescription = stringResource(R.string.my_applications),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.my_applications),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "View and manage your job applications",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Navigate Applications",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("support") }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Support",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Support & Contact Us",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Have questions or need help? Talk to our support team.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Navigate Support",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val talentViewModel = viewModel
                        val isAdmin by talentViewModel.isAdmin.collectAsState()
                        
                        if (isAdmin) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            AdminMenuCard(onClick = { navController.navigate("admin_dashboard") })
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        showLogoutDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
fun EditWorkerProfileDialog(
    worker: Worker,
    onDismiss: () -> Unit,
    onSave: (name: String, profession: String, bio: String, skills: String) -> Unit
) {
    var name by remember { mutableStateOf(worker.name) }
    var profession by remember { mutableStateOf(worker.profession) }
    var bio by remember { mutableStateOf(worker.experience) }
    var skillsText by remember { mutableStateOf(worker.skills.joinToString(", ")) }
    var showEditMultiSelectDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = profession,
                        onValueChange = { },
                        label = { Text("Profession") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { showEditMultiSelectDialog = true },
                        trailingIcon = {
                            IconButton(onClick = { showEditMultiSelectDialog = true }) {
                                Text("▼")
                            }
                        }
                    )
                    
                    if (showEditMultiSelectDialog) {
                        var query by remember { mutableStateOf("") }
                        val currentLang = com.example.LocalCurrentLanguage.current
                        var selectedSet by remember { mutableStateOf(profession.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()) }
                        
                        AlertDialog(
                            onDismissRequest = { showEditMultiSelectDialog = false },
                            title = { Text(if (currentLang == "ar") "اختر المهن (متعدد)" else "Select Professions (Multi)") },
                            text = {
                                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)) {
                                    OutlinedTextField(
                                        value = query,
                                        onValueChange = { query = it },
                                        placeholder = { Text(if (currentLang == "ar") "ابحث عن مهنة..." else "Search professions...") },
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        singleLine = true
                                    )
                                    
                                    val filteredCategories = com.example.ui.ConfigData.saudiCategories.filter {
                                        val localizedName = if (currentLang == "ar") it.nameAr else it.nameEn
                                        localizedName.contains(query, ignoreCase = true) || it.nameEn.contains(query, ignoreCase = true)
                                    }
                                    
                                    androidx.compose.foundation.lazy.LazyColumn(
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(filteredCategories) { cat ->
                                            val isChecked = selectedSet.contains(cat.nameEn)
                                            val accentColor = com.example.ui.ConfigData.getCategoryColor(cat.nameEn)
                                            val localizedName = if (currentLang == "ar") cat.nameAr else cat.nameEn
                                            
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedSet = if (isChecked) selectedSet - cat.nameEn else selectedSet + cat.nameEn
                                                    }
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = { checked ->
                                                        selectedSet = if (checked) selectedSet + cat.nameEn else selectedSet - cat.nameEn
                                                    },
                                                    colors = CheckboxDefaults.colors(checkedColor = accentColor)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(accentColor.copy(alpha = 0.12f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = cat.icon,
                                                        contentDescription = null,
                                                        tint = accentColor,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(localizedName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    profession = selectedSet.joinToString(", ")
                                    showEditMultiSelectDialog = false
                                }) {
                                    Text(if (currentLang == "ar") "موافق" else "Done")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEditMultiSelectDialog = false }) {
                                    Text(if (currentLang == "ar") "إلغاء" else "Cancel")
                                }
                            }
                        )
                    }
                    
                    androidx.compose.foundation.layout.Box(modifier = Modifier.matchParentSize().clickable(onClick = { showEditMultiSelectDialog = true }), content = {} )
                }
                
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio (Experience)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                OutlinedTextField(
                    value = skillsText,
                    onValueChange = { skillsText = it },
                    label = { Text("Skills (Comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                val skillsList = skillsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                onSave(name, profession, bio, skillsList.joinToString(",")) 
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProfileItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
