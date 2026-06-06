package com.example.ui.screens

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.rounded.Group
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.data.Job
import com.example.ui.TalentViewModel
import com.example.ui.ConfigData
import com.example.ui.components.TopBarWithLanguage
import androidx.compose.material.icons.filled.Share
import com.example.LocalSharedTransitionScope
import com.example.LocalAnimatedVisibilityScope

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(jobId: String?, viewModel: TalentViewModel, navController: NavController, chatViewModel: com.example.ui.ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    val workers by viewModel.workers.collectAsStateWithLifecycle()
    val currentUserId = viewModel.getCurrentUserId()
    val myWorkerProfile = workers.firstOrNull { it.userId == currentUserId }
    val job = jobs.find { it.id == jobId }
    val localContext = LocalContext.current

    var showSignUpDialog by remember { mutableStateOf(false) }
    var showIncompleteProfileDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    if (showSignUpDialog) {
        AlertDialog(
            onDismissRequest = { showSignUpDialog = false },
            title = { Text("Create an account to use this feature") },
            text = { Text("Please register or log in to continue.") },
            confirmButton = {
                TextButton(onClick = {
                    showSignUpDialog = false
                    viewModel.openInSignUpMode = true
                    navController.navigate(com.example.ui.Screen.Login.route)
                }) {
                    Text("Sign Up")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSignUpDialog = false
                    viewModel.openInSignUpMode = false
                    navController.navigate(com.example.ui.Screen.Login.route)
                }) {
                    Text("Login")
                }
            }
        )
    }

    if (showIncompleteProfileDialog) {
        AlertDialog(
            onDismissRequest = { showIncompleteProfileDialog = false },
            title = { Text("Profile Incomplete") },
            text = { Text("Since your profile is incomplete, please update it to increase your chances of getting the job.") },
            confirmButton = {
                TextButton(onClick = {
                    showIncompleteProfileDialog = false
                    navController.navigate(com.example.ui.Screen.Post.route)
                }) {
                    Text("Update Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showIncompleteProfileDialog = false
                    job?.let {
                        viewModel.applyForJob(it)
                    }
                }) {
                    Text("Apply Anyway")
                }
            }
        )
    }

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    var hasApplied by remember { mutableStateOf(false) }

    if (job == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Job not found", color = MaterialTheme.colorScheme.onSurface)
        }
        return
    }

    LaunchedEffect(job.id) {
        try {
            val uid = viewModel.getCurrentUserId().ifEmpty { null }
            if (uid != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("applications")
                    .whereEqualTo("userId", uid)
                    .whereEqualTo("jobId", job.id)
                    .get()
                    .addOnSuccessListener { snap ->
                        hasApplied = !snap.isEmpty
                    }
                    .addOnFailureListener {
                        com.example.ui.FirestoreErrorHandler.handleError(it, "JobDetails-CheckApp")
                    }
            }
        } catch (e: Exception) {
            com.example.ui.FirestoreErrorHandler.handleError(e, "JobDetails-CheckApp")
        }
        viewModel.logJobView(job)
    }

    val appConfig by viewModel.appConfig.collectAsStateWithLifecycle()
    val admobEnabled = appConfig["admobEnabled"] as? Boolean ?: false
    val showBannerOnDetails = appConfig["showBannerOnDetails"] as? Boolean ?: true
    val showAdOnApply = appConfig["showAdOnApply"] as? Boolean ?: true
    val bannerAdUnitId = appConfig["bannerAdUnitId"] as? String ?: "ca-app-pub-3940256099942544/6300978111"
    val interstitialAdUnitId = appConfig["interstitialAdUnitId"] as? String ?: "ca-app-pub-3940256099942544/1033173712"

    val interstitialAdLoader = remember(admobEnabled, showAdOnApply) { 
        if (admobEnabled && showAdOnApply) {
            com.example.ui.SmartInterstitialAdLoader(localContext).apply {
                loadAd()
            }
        } else null
    }

    Scaffold(
        topBar = {
            val currentContext = androidx.compose.ui.platform.LocalContext.current
            TopAppBar(
                title = { Text("Job Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val jobUrl = "https://talentmarket.com/jobs/${job.id}"
                        val shareMessage = "Check out this job on TalentMarket:\n\n*${job.title}*\n🏢 Company: ${job.companyName.ifEmpty { "Employer" }}\n📍 Location: ${job.location}, ${job.country}\n💰 Salary/Budget: ${job.salary.ifEmpty { job.budget }}\n\nLink: $jobUrl"
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, job.title)
                            putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                        }
                        currentContext.startActivity(android.content.Intent.createChooser(shareIntent, "Share job via"))
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                    if (job.userId != currentUserId && currentUserId.isNotEmpty()) {
                        IconButton(onClick = { showReportDialog = true }) {
                            Icon(imageVector = Icons.Default.Flag, contentDescription = "Report", tint = Color(0xFFEF4444))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        var cardModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)

        if (sharedTransitionScope != null && animatedVisibilityScope != null) {
            with(sharedTransitionScope) {
                cardModifier = cardModifier.sharedBounds(
                    rememberSharedContentState(key = "job-${job.id}"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }
        }

        ElevatedCard(
            modifier = cardModifier,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (!job.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = job.imageUrl,
                        contentDescription = "${job.title} Cover Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ConfigData.getIconForCategory(job.category),
                                contentDescription = job.category,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = job.jobType.uppercase(),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${job.location}, ${job.country}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Posted ${com.example.utils.TimeUtils.getRelativeTime(job.timestamp)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    // Company Highlight Card
                    LaunchedEffect(job.userId) {
                        viewModel.loadCompanyProfile(job.userId)
                    }
                    val companyProfiles by viewModel.companyProfiles.collectAsStateWithLifecycle()
                    val companyProfile = companyProfiles[job.userId]

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("company_profile/${job.userId}") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Display preset logo preview or a fallback icon
                            val presetLogoGradients = listOf(
                                Brush.linearGradient(listOf(Color(0xFFF093FB), Color(0xFFF5576C))),
                                Brush.linearGradient(listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))),
                                Brush.linearGradient(listOf(Color(0xFF43E97B), Color(0xFF38F9D7))),
                                Brush.linearGradient(listOf(Color(0xFFFA709A), Color(0xFFFEE140))),
                                Brush.linearGradient(listOf(Color(0xFF30CFD0), Color(0xFF33086F))),
                                Brush.linearGradient(listOf(Color(0xFFA18CD1), Color(0xFFFBC2EB)))
                            )
                            val presetEmojis = listOf("🔥", "🚀", "🌐", "💡", "🤖", "📈")

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (companyProfile != null && companyProfile.logoUrl.startsWith("PRESET_")) {
                                            val index = companyProfile.logoUrl.removePrefix("PRESET_").toIntOrNull() ?: 0
                                            presetLogoGradients.getOrNull(index) ?: presetLogoGradients[0]
                                        } else {
                                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (companyProfile != null && companyProfile.logoUrl.startsWith("PRESET_")) {
                                    val index = companyProfile.logoUrl.removePrefix("PRESET_").toIntOrNull() ?: 0
                                    Text(
                                        text = presetEmojis.getOrNull(index) ?: "💼",
                                        fontSize = 22.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = companyProfile?.companyName ?: "Independent Recruiter",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (companyProfile?.isVerified == true) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Filled.Verified,
                                            contentDescription = "Verified Company",
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = companyProfile?.industry?.ifEmpty { "External Recruiter" } ?: "Tap to view corporate profile",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "View Profile",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = job.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if ((admobEnabled || viewModel.adPlacements.value.isNotEmpty()) && showBannerOnDetails) {
                        com.example.ui.SmartBannerAd(
                            adUnitId = bannerAdUnitId,
                            viewModel = viewModel,
                            navController = navController
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (job.budget.isNotEmpty() || job.deadline.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (job.budget.isNotEmpty()) {
                                Column {
                                    Text("Budget", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(job.budget, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            if (job.deadline.isNotEmpty()) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Deadline", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(job.deadline, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    // WhatsApp Share Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clickable {
                                val jobUrl = "https://talentmarket.com/jobs/${job.id}"
                                val shareMessage = "Check out this job on TalentMarket:\n\n*${job.title}*\n🏢 *Company:* ${job.companyName.ifEmpty { "Employer" }}\n📍 *Location:* ${job.location}, ${job.country}\n💰 *Salary/Budget:* ${job.salary.ifEmpty { job.budget }}\n\nApply here: $jobUrl"
                                
                                try {
                                    // Try native send intent directly targeting WhatsApp
                                    val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                                        setPackage("com.whatsapp")
                                    }
                                    localContext.startActivity(sendIntent)
                                } catch (e: Exception) {
                                    try {
                                        // Fallback 1: Use WhatsApp API URL
                                        val encodedMessage = java.net.URLEncoder.encode(shareMessage, "UTF-8")
                                        val whatsappUrl = "https://api.whatsapp.com/send?text=$encodedMessage"
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(whatsappUrl))
                                        localContext.startActivity(intent)
                                    } catch (ex: Exception) {
                                        // Fallback 2: General Share Chooser if WhatsApp is completely unavailable
                                        val generalIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_SUBJECT, job.title)
                                            putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                                        }
                                        localContext.startActivity(android.content.Intent.createChooser(generalIntent, "Share job via"))
                                    }
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF25D366).copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF25D366)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share on WhatsApp",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Share on WhatsApp",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Spread the word! Send this job opportunity to friends or groups",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val hasContact = job.contact.trim().isNotEmpty()

                    if (job.userId == currentUserId) {
                        Button(
                            onClick = {
                                navController.navigate(
                                    com.example.ui.Screen.JobApplicants.createRoute(
                                        job.id, job.title
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0F3460)
                            )
                        ) {
                            Icon(Icons.Rounded.Group, null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "View Applicants",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!viewModel.isGuest) {
                                        if (!hasApplied) {
                                            if (viewModel.currentUserType == "worker") {
                                                if (myWorkerProfile == null || myWorkerProfile.skills.isEmpty() || myWorkerProfile.experience.isBlank() || myWorkerProfile.resumeLink.isBlank()) {
                                                    showIncompleteProfileDialog = true
                                                    return@Button
                                                }
                                            }
                                            val currentActivity = localContext as? android.app.Activity
                                            if (interstitialAdLoader != null && currentActivity != null) {
                                                interstitialAdLoader.showAd(currentActivity) {
                                                    viewModel.applyForJob(job)
                                                    hasApplied = true
                                                }
                                            } else {
                                                viewModel.applyForJob(job)
                                                hasApplied = true
                                            }
                                        }
                                    } else {
                                        showSignUpDialog = true
                                    }
                                },
                                enabled = !hasApplied,
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasApplied) Color(0xFF374151) else MaterialTheme.colorScheme.secondary,
                                    disabledContainerColor = Color(0xFF374151)
                                )
                            ) {
                                Text(
                                    if (hasApplied) "Already Applied ✓" else "Apply Now",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasApplied) Color(0xFF9CA3AF) else MaterialTheme.colorScheme.onSecondary
                                )
                            }

                            if (hasContact) {
                                Button(
                                    onClick = {
                                        val runWhatsApp = {
                                            try {
                                                val cleanNumber = job.contact.replace("+", "").replace(" ", "").trim()
                                                val url = "https://api.whatsapp.com/send?phone=$cleanNumber"
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                    data = android.net.Uri.parse(url)
                                                    setPackage("com.whatsapp")
                                                }
                                                localContext.startActivity(intent)
                                            } catch (e: Exception) {
                                                val cleanNumber = job.contact.replace("+", "").replace(" ", "").trim()
                                                val url = "https://api.whatsapp.com/send?phone=$cleanNumber"
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                                localContext.startActivity(intent)
                                            }
                                        }

                                        val currentActivity = localContext as? android.app.Activity
                                        if (interstitialAdLoader != null && currentActivity != null) {
                                            interstitialAdLoader.showAd(currentActivity, runWhatsApp)
                                        } else {
                                            runWhatsApp()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                                ) {
                                    Text("WhatsApp", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Button(
                                onClick = {
                                    if (viewModel.isGuest) {
                                        showSignUpDialog = true
                                    } else {
                                        chatViewModel.startChat(
                                            otherUserId = job.userId,
                                            otherUserName = job.companyName.ifEmpty { "Employer" },
                                            otherUserPhoto = "" // could fetch employer profile picture if available
                                        ) { roomId ->
                                            navController.navigate(com.example.ui.Screen.ChatDetail.createRoute(roomId, job.userId, job.companyName.ifEmpty { "Employer" }))
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text("Chat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary)
                            }
                        }
                    }
                }
            }
        }

        if (showReportDialog) {
            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                containerColor = Color(0xFF1A1A2E),
                title = {
                    Text("Report This Post",
                        color = Color.White,
                        fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Why are you reporting this?",
                            color = Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.bodyMedium)
                        
                        val reasons = listOf(
                            "Fake or spam post",
                            "Inappropriate content",
                            "Wrong category",
                            "Scam or fraud",
                            "Already filled position",
                            "Other"
                        )
                        
                        reasons.forEach { reason ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedReason = reason }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedReason == reason,
                                    onClick = { selectedReason = reason },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFFD4AF37)
                                    )
                                )
                                Text(reason,
                                    color = if (selectedReason == reason) Color.White else Color(0xFF9CA3AF),
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (selectedReason.isNotEmpty()) {
                                viewModel.reportPost(
                                    postId = job.id,
                                    postType = "job",
                                    reason = selectedReason,
                                    reportedUserId = job.userId
                                )
                                showReportDialog = false
                            }
                        },
                        enabled = selectedReason.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Submit Report", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showReportDialog = false }
                    ) {
                        Text("Cancel", color = Color(0xFF9CA3AF))
                    }
                }
            )
        }
    }
}
