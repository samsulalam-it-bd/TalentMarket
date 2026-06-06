package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.R
import com.example.data.CompanyProfile
import com.example.ui.TalentViewModel
import com.google.firebase.auth.FirebaseAuth

// Preset logo gradients & emojis to mock beautifully tailored company logos
val LogoPresets = listOf(
    "🔥" to Brush.linearGradient(listOf(Color(0xFFF093FB), Color(0xFFF5576C))),
    "🚀" to Brush.linearGradient(listOf(Color(0xFF4FACFE), Color(0xFF00F2FE))),
    "🌐" to Brush.linearGradient(listOf(Color(0xFF43E97B), Color(0xFF38F9D7))),
    "💡" to Brush.linearGradient(listOf(Color(0xFFFA709A), Color(0xFFFEE140))),
    "🤖" to Brush.linearGradient(listOf(Color(0xFF30CFD0), Color(0xFF33086F))),
    "📈" to Brush.linearGradient(listOf(Color(0xFFA18CD1), Color(0xFFFBC2EB)))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyProfileScreen(
    companyId: String?,
    viewModel: TalentViewModel,
    navController: NavController
) {
    val currentUserId = viewModel.getCurrentUserId()
    val targetCompanyId = companyId ?: currentUserId
    
    LaunchedEffect(targetCompanyId) {
        viewModel.loadCompanyProfile(targetCompanyId)
    }
    
    val companyProfiles by viewModel.companyProfiles.collectAsStateWithLifecycle()
    val companyProfile = companyProfiles[targetCompanyId]
    
    // Check if the current user owns this company profile
    val isOwner = (currentUserId == targetCompanyId) && (viewModel.currentUserType == "employer")
    
    var isEditing by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Inputs for edit
    var nameInput by remember { mutableStateOf("") }
    var logoUrlInput by remember { mutableStateOf("") }
    var industryInput by remember { mutableStateOf("") }
    var sizeInput by remember { mutableStateOf("11-50") }
    var locationInput by remember { mutableStateOf("") }
    var websiteInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var foundedInput by remember { mutableStateOf("") }
    var linkedinInput by remember { mutableStateOf("") }
    var instagramInput by remember { mutableStateOf("") }
    var twitterInput by remember { mutableStateOf("") }
    
    // Validation flags
    var nameError by remember { mutableStateOf(false) }
    var industryError by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }
    var foundedError by remember { mutableStateOf(false) }
    
    // Initialize editing values once companyProfile changes
    LaunchedEffect(companyProfile, isEditing) {
        if (companyProfile != null) {
            nameInput = companyProfile.companyName
            logoUrlInput = companyProfile.logoUrl
            industryInput = companyProfile.industry
            sizeInput = companyProfile.companySize.ifEmpty { "11-50" }
            locationInput = companyProfile.location
            websiteInput = companyProfile.websiteUrl
            phoneInput = companyProfile.phoneNumber
            descriptionInput = companyProfile.description
            foundedInput = companyProfile.foundedYear
            linkedinInput = companyProfile.linkedinUrl
            instagramInput = companyProfile.instagramUrl
            twitterInput = companyProfile.twitterUrl
        }
    }

    var showReviewSheet by remember { mutableStateOf(false) }
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUserProfile.collectAsStateWithLifecycle()

    LaunchedEffect(targetCompanyId) {
        viewModel.loadReviews(targetCompanyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isEditing) "Edit Company Profile" else "Company Profile", 
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (isEditing) {
                            isEditing = false
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isOwner && !isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile")
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isEditing) {
                // Edit profile form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Customize Corporate Presence",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Logo Picker / Logo URL
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Company Logo",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Display current logo or chosen gradient mock logo
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (logoUrlInput.startsWith("PRESET_")) {
                                            val index = logoUrlInput.removePrefix("PRESET_").toIntOrNull() ?: 0
                                            LogoPresets.getOrNull(index)?.second ?: LogoPresets[0].second
                                        } else {
                                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (logoUrlInput.startsWith("PRESET_")) {
                                    val index = logoUrlInput.removePrefix("PRESET_").toIntOrNull() ?: 0
                                    Text(
                                        text = LogoPresets.getOrNull(index)?.first ?: "💼",
                                        fontSize = 36.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = "Logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Tap a preset logo style, or type a custom logo URL below:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Presets Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LogoPresets.forEachIndexed { idx, (emoji, brush) ->
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(brush)
                                            .clickable { logoUrlInput = "PRESET_$idx" }
                                            .border(
                                                width = if (logoUrlInput == "PRESET_$idx") 3.dp else 0.dp,
                                                color = if (logoUrlInput == "PRESET_$idx") MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 20.sp)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = logoUrlInput,
                                onValueChange = { logoUrlInput = it },
                                label = { Text("Custom Logo Image URL") },
                                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Company Name
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { 
                            nameInput = it
                            nameError = it.isBlank()
                        },
                        label = { Text("Company Name *") },
                        isError = nameError,
                        supportingText = { if (nameError) Text("Company Name is required") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Industry
                    OutlinedTextField(
                        value = industryInput,
                        onValueChange = { 
                            industryInput = it
                            industryError = it.isBlank()
                        },
                        label = { Text("Industry / Business Type *") },
                        placeholder = { Text("e.g. Technology, Healthcare, Finance") },
                        isError = industryError,
                        supportingText = { if (industryError) Text("Industry is required") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Company Size Section
                    Column {
                        Text(
                            text = "Company Size *",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        val sizes = listOf("1-10", "11-50", "50-200", "200+")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            sizes.forEach { size ->
                                FilterChip(
                                    selected = sizeInput == size,
                                    onClick = { sizeInput = size },
                                    label = { Text(size) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Location / City
                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = { 
                            locationInput = it
                            locationError = it.isBlank()
                        },
                        label = { Text("Location / City *") },
                        placeholder = { Text("e.g. Riyadh, Dubai, Manama") },
                        isError = locationError,
                        supportingText = { if (locationError) Text("Location is required") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Website URL
                    OutlinedTextField(
                        value = websiteInput,
                        onValueChange = { websiteInput = it },
                        label = { Text("Website URL") },
                        placeholder = { Text("e.g. https://company.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Phone Number
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Founded Year
                    OutlinedTextField(
                        value = foundedInput,
                        onValueChange = { 
                            foundedInput = it
                            val yr = it.toIntOrNull()
                            foundedError = it.isNotEmpty() && (yr == null || yr < 1800 || yr > 2026)
                        },
                        label = { Text("Founded Year") },
                        placeholder = { Text("e.g. 2015") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = foundedError,
                        supportingText = { if (foundedError) Text("Enter a valid year (1800-2026)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Company Description
                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { 
                            descriptionInput = it
                            descriptionError = it.length < 10
                        },
                        label = { Text("Company Description *") },
                        isError = descriptionError,
                        supportingText = { if (descriptionError) Text("Must be at least 10 characters") else Text("Tell us about your company and business environment") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Social Links Title
                    Text(
                        text = "Social Links (LinkedIn, Instagram, Twitter)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // LinkedIn Link
                    OutlinedTextField(
                        value = linkedinInput,
                        onValueChange = { linkedinInput = it },
                        label = { Text("LinkedIn Profile Link") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Instagram Link
                    OutlinedTextField(
                        value = instagramInput,
                        onValueChange = { instagramInput = it },
                        label = { Text("Instagram Username/Link") },
                        leadingIcon = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Twitter Link
                    OutlinedTextField(
                        value = twitterInput,
                        onValueChange = { twitterInput = it },
                        label = { Text("Twitter Profile Link") },
                        leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Button(
                        onClick = {
                            // Validation checks
                            val nameValid = nameInput.isNotBlank()
                            val indValid = industryInput.isNotBlank()
                            val locValid = locationInput.isNotBlank()
                            val descValid = descriptionInput.length >= 10
                            val yr = foundedInput.toIntOrNull()
                            val fndValid = foundedInput.isEmpty() || (yr != null && yr in 1800..2026)

                            nameError = !nameValid
                            industryError = !indValid
                            locationError = !locValid
                            descriptionError = !descValid
                            foundedError = !fndValid

                            if (nameValid && indValid && locValid && descValid && fndValid) {
                                viewModel.updateCompanyProfile(
                                    companyId = targetCompanyId,
                                    companyName = nameInput.trim(),
                                    logoUrl = logoUrlInput.trim(),
                                    industry = industryInput.trim(),
                                    companySize = sizeInput,
                                    location = locationInput.trim(),
                                    websiteUrl = websiteInput.trim(),
                                    phoneNumber = phoneInput.trim(),
                                    description = descriptionInput.trim(),
                                    foundedYear = foundedInput.trim(),
                                    linkedinUrl = linkedinInput.trim(),
                                    instagramUrl = instagramInput.trim(),
                                    twitterUrl = twitterInput.trim()
                                ) { success ->
                                    if (success) {
                                        isEditing = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Corporate Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    TextButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                }
            } else {
                // Read-only/Public view
                if (companyProfile == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "No Corporate profile configured",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isOwner) {
                                "Tap below to initialize your company profile to let job seekers find you!"
                            } else {
                                "The publisher has not shared their company profile details."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        if (isOwner) {
                            Button(onClick = { isEditing = true }) {
                                Text("Initialize Profile")
                            }
                        }
                    }
                } else {
                    // Profile exists: full beauty view
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Header Box with circular Logo & Background Gradient
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.background
                                        )
                                    )
                                )
                        ) {
                            // Floating Logo In Header
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (companyProfile.logoUrl.startsWith("PRESET_")) {
                                            val index = companyProfile.logoUrl.removePrefix("PRESET_").toIntOrNull() ?: 0
                                            LogoPresets.getOrNull(index)?.second ?: LogoPresets[0].second
                                        } else {
                                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary))
                                        }
                                    )
                                    .border(4.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (companyProfile.logoUrl.startsWith("PRESET_")) {
                                    val index = companyProfile.logoUrl.removePrefix("PRESET_").toIntOrNull() ?: 0
                                    Text(
                                        text = LogoPresets.getOrNull(index)?.first ?: "💼",
                                        fontSize = 44.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = "Logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(50.dp)
                                    )
                                }
                            }
                        }

                        // Company Name + Verification Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = companyProfile.companyName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            if (companyProfile.isVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Verified Company",
                                    tint = Color(0xFFFFD700), // Elegant Gold Badge
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Average Rating
                        val employerRating = reviews.filter { it.targetId == targetCompanyId }
                        if (employerRating.isNotEmpty()) {
                            val avg = employerRating.map { it.rating }.average()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Star, null,
                                    tint = Color(0xFFD4AF37),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    String.format("%.1f", avg),
                                    color = Color(0xFFD4AF37),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "(${employerRating.size} reviews)",
                                    color = Color(0xFF6B7280),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        // Industry Subtext
                        Text(
                            text = companyProfile.industry,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Grid stats / quick facts: Size, Location, Founded
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FactCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.People,
                                title = "Size",
                                value = companyProfile.companySize
                            )
                            FactCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.LocationOn,
                                title = "Location",
                                value = companyProfile.location
                            )
                            if (companyProfile.foundedYear.isNotEmpty()) {
                                FactCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Outlined.Timeline,
                                    title = "Founded",
                                    value = companyProfile.foundedYear
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Business summary / Description
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = "About the Company",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = companyProfile.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Connect Links Section (Website, Phone, Socials)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = "Contact & Corporate Connections",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Website
                            if (companyProfile.websiteUrl.isNotEmpty()) {
                                ContactItem(
                                    icon = Icons.Default.Language,
                                    text = companyProfile.websiteUrl,
                                    label = "Website"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            
                            // Phone
                            if (companyProfile.phoneNumber.isNotEmpty()) {
                                ContactItem(
                                    icon = Icons.Default.Phone,
                                    text = companyProfile.phoneNumber,
                                    label = "Contact Number"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Social Links Row
                            if (companyProfile.linkedinUrl.isNotEmpty() || companyProfile.instagramUrl.isNotEmpty() || companyProfile.twitterUrl.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (companyProfile.linkedinUrl.isNotEmpty()) {
                                        SocialChip(icon = Icons.Default.Share, text = "LinkedIn")
                                    }
                                    if (companyProfile.instagramUrl.isNotEmpty()) {
                                        SocialChip(icon = Icons.Default.PhotoCamera, text = "Instagram")
                                    }
                                    if (companyProfile.twitterUrl.isNotEmpty()) {
                                        SocialChip(icon = Icons.Default.Tag, text = "Twitter")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Divider(color = Color(0xFF374151), thickness = 0.5.dp)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        com.example.ui.components.ReviewsSection(
                            targetId = targetCompanyId,
                            talentViewModel = viewModel
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Only show Rate button to workers
                        if ((currentUser?.get("userType") as? String) == "worker" && !isOwner) {
                            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                                Button(
                                    onClick = { showReviewSheet = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD4AF37)
                                    )
                                ) {
                                    Icon(
                                        Icons.Filled.Star, null,
                                        tint = Color(0xFF1A1A2E),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Rate This Employer",
                                        color = Color(0xFF1A1A2E),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
    
    if (showReviewSheet) {
        com.example.ui.components.ReviewBottomSheet(
            targetId = targetCompanyId,
            targetName = companyProfile?.companyName ?: "Employer",
            targetType = "employer",
            talentViewModel = viewModel,
            onDismiss = { showReviewSheet = false }
        )
    }
}

@Composable
fun FactCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ContactItem(
    icon: ImageVector,
    text: String,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SocialChip(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
