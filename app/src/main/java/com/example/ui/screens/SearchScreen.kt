package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.R
import com.example.data.Job
import com.example.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: TalentViewModel, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val searchPrefs = remember { SearchPrefs(context) }
    val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    val companyProfiles by viewModel.companyProfiles.collectAsStateWithLifecycle()

    var showSignUpDialog by remember { mutableStateOf(false) }
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
    
    // States
    var filter by remember { mutableStateOf(FilterModel()) }
    var searchQueryInput by remember { mutableStateOf("") }
    
    val appConfig by viewModel.appConfig.collectAsStateWithLifecycle()
    val admobEnabled = appConfig["admobEnabled"] as? Boolean ?: false
    val showBannerOnFeed = appConfig["showBannerOnFeed"] as? Boolean ?: true
    val feedAdFrequency = (appConfig["feedAdFrequency"] as? Number)?.toInt() ?: 5
    val bannerAdUnitId = appConfig["bannerAdUnitId"] as? String ?: "ca-app-pub-3940256099942544/6300978111"

    // Load initial deep linked category
    LaunchedEffect(Unit) {
        if (viewModel.initialSearchCategory != "All") {
            filter = filter.copy(jobCategory = viewModel.initialSearchCategory)
            viewModel.initialSearchCategory = "All"
        }
    }
    var recentSearches by remember { mutableStateOf(searchPrefs.getRecentSearches()) }
    var savedFilters by remember { mutableStateOf(searchPrefs.getSavedFilters()) }
    var userTier by remember { mutableStateOf(searchPrefs.getUserTier()) }
    
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSaveFilterDialog by remember { mutableStateOf(false) }
    var saveFilterName by remember { mutableStateOf("") }
    
    // Location (GPS) State
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    
    val currentLang = com.example.LocalCurrentLanguage.current
    val popularSearches = listOf("Software", "Driver", "AC & Cooling", "Riyadh", "Plumbing", "Verified Only", "Nurse")

    // GPS runtime permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            fetchGpsLocation(context) { lat, lng ->
                userLocation = Pair(lat, lng)
                Toast.makeText(context, "Location updated from GPS", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Permission denied simulation mapping
            userLocation = Pair(24.7136, 46.6753) // Fallback Riyadh
            Toast.makeText(context, "Using default Riyadh GPS simulation", Toast.LENGTH_SHORT).show()
        }
    }

    // Debounce search query
    LaunchedEffect(searchQueryInput) {
        delay(500)
        filter = filter.copy(searchText = searchQueryInput)
        if (searchQueryInput.trim().isNotEmpty()) {
            searchPrefs.saveSearch(searchQueryInput.trim())
            recentSearches = searchPrefs.getRecentSearches()
        }
    }

    // Speech to Text Launcher
    val speechIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val spokenText = matches[0]
                searchQueryInput = spokenText
                Toast.makeText(context, "Voice Search: $spokenText", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Filter implementation calculation
    val filteredJobs = remember(jobs, filter, userLocation) {
        val list = jobs.filter { job ->
            val matchesText = if (filter.searchText.isEmpty()) {
                true
            } else {
                job.title.contains(filter.searchText, ignoreCase = true) ||
                job.description.contains(filter.searchText, ignoreCase = true) ||
                job.category.contains(filter.searchText, ignoreCase = true) ||
                job.location.contains(filter.searchText, ignoreCase = true)
            }

            val matchesJobType = if (filter.jobTypes.isEmpty()) {
                true
            } else {
                filter.jobTypes.any { job.jobType.equals(it, ignoreCase = true) }
            }

            val matchesLocation = when {
                filter.location == "All" -> true
                filter.location == "Remote / Work from home" -> job.location.contains("Remote", ignoreCase = true) || job.description.contains("Remote", ignoreCase = true) || job.jobType.contains("Remote", ignoreCase = true)
                filter.location == "Near me" -> true // Handled primarily by sorting proximity
                else -> job.location.equals(filter.location, ignoreCase = true) || job.country.equals(filter.location, ignoreCase = true)
            }

            val matchesExperience = if (filter.experienceLevel == "Any") {
                true
            } else {
                job.experienceLevel.equals(filter.experienceLevel, ignoreCase = true)
            }

            val matchesEducation = if (filter.education == "Any") {
                true
            } else {
                job.education.equals(filter.education, ignoreCase = true)
            }

            val matchesNationality = when (filter.nationalityType) {
                "Any" -> true
                "Saudi Only" -> job.nationality.contains("Saudi", ignoreCase = true)
                "Open to Expats" -> job.nationality.contains("Expat", ignoreCase = true) || job.nationality.equals("Any", ignoreCase = true)
                else -> {
                    if (filter.specificNationalities.isNotEmpty()) {
                        filter.specificNationalities.any { job.nationality.contains(it, ignoreCase = true) }
                    } else {
                        true
                    }
                }
            }

            val matchesGender = if (filter.gender == "Any") {
                true
            } else {
                job.gender.equals(filter.gender, ignoreCase = true) || job.gender.equals("Any", ignoreCase = true)
            }

            val matchesPostingDate = when (filter.postingDate) {
                "Any time" -> true
                "Last 24 hours" -> job.timestamp >= (System.currentTimeMillis() - 24 * 60 * 60 * 1000L)
                "Last 3 days" -> job.timestamp >= (System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L)
                "Last week" -> job.timestamp >= (System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L)
                "Last month" -> job.timestamp >= (System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L)
                else -> true
            }

            val matchesCategory = if (filter.jobCategory == "All") {
                true
            } else {
                job.category.equals(filter.jobCategory, ignoreCase = true)
            }

            val matchesCompanyType = if (filter.companyType == "Any") {
                true
            } else {
                job.companyType.equals(filter.companyType, ignoreCase = true) || (filter.companyType == "Verified Only" && job.companyType == "Verified Only")
            }

            matchesText && matchesJobType && matchesLocation && matchesExperience &&
            matchesEducation && matchesNationality && matchesGender && matchesPostingDate &&
            matchesCategory && matchesCompanyType
        }

        // Sorting configuration
        when (filter.sortBy) {
            "Most Recent" -> list.sortedByDescending { it.timestamp }
            "Most Applicants" -> list.sortedByDescending { it.applicantsCount }
            "Most Viewed" -> list.sortedByDescending { it.viewsCount }
            "Nearest to me" -> {
                val userCoords = userLocation ?: Pair(24.7136, 46.6753) // Riyadh center
                list.sortedBy { job ->
                    val jobCoords = getCityCoords(job.location)
                    calculateDist(userCoords.first, userCoords.second, jobCoords.first, jobCoords.second)
                }
            }
            else -> list.sortedByDescending { it.timestamp }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = if (currentLang == "ar") "البحث المتقدم" else "Advanced Search",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Subscription Tier Toggle Visual for testing Saved Filters Limit (Silver=5, Gold=Unlimited)
                    TextButton(
                        onClick = {
                            val nextTier = if (userTier == "Silver") "Gold" else "Silver"
                            searchPrefs.setUserTier(nextTier)
                            userTier = nextTier
                            Toast.makeText(context, "Account tier changed to $nextTier plan!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Surface(
                            color = if (userTier == "Gold") Color(0xFFFFD700) else MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = userTier,
                                color = if (userTier == "Gold") Color.Black else MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Search Input Line
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQueryInput,
                        onValueChange = { searchQueryInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .testTag("search_field_input"),
                        placeholder = { Text(if (currentLang == "ar") "ابحث عن وظائف، مهارات، شركات..." else "Search jobs, skills, companies...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (searchQueryInput.isNotEmpty()) {
                                    IconButton(onClick = { searchQueryInput = "" }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now to search...")
                                        }
                                        try {
                                            speechIntentLauncher.launch(speechIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Speech recognition not installed.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Search")
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        singleLine = true
                    )

                    // Filter badged button
                    Box {
                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(if (filter.hasActiveFilters()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer)
                                .testTag("filter_trigger_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Open Filters",
                                tint = if (filter.hasActiveFilters()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        if (filter.hasActiveFilters()) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(2.dp, (-2).dp)
                            ) {
                                Text(filter.getActiveFiltersCount().toString(), fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Active Filter Removable Chips
                if (filter.hasActiveFilters()) {
                    ActiveFilterChipsRow(
                        filter = filter,
                        onRemoveFilter = { type ->
                            filter = removeSingleFilterSetting(filter, type)
                        },
                        onClearAll = {
                            filter = FilterModel(searchText = filter.searchText, sortBy = filter.sortBy)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Horizontal line division
            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Popular Searches section
                if (searchQueryInput.isEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = if (currentLang == "ar") "عمليات البحث الشائعة" else "Popular Searches",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(popularSearches) { search ->
                                    SuggestionChip(
                                        onClick = { searchQueryInput = search },
                                        label = { Text(search) },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Recent Searches Saved section
                if (recentSearches.isNotEmpty()) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (currentLang == "ar") "البحث الأخير" else "Recent Searches",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                TextButton(onClick = {
                                    recentSearches.forEach { searchPrefs.deleteRecentSearch(it) }
                                    recentSearches = emptyList()
                                }) {
                                    Text(if (currentLang == "ar") "مسح الكل" else "Clear All")
                                }
                            }
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                mainAxisSpacing = 8.dp,
                                crossAxisSpacing = 4.dp
                            ) {
                                recentSearches.forEach { search ->
                                    InputChip(
                                        selected = false,
                                        onClick = { searchQueryInput = search },
                                        label = { Text(search, fontSize = 12.sp) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Delete",
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable {
                                                        searchPrefs.deleteRecentSearch(search)
                                                        recentSearches = searchPrefs.getRecentSearches()
                                                    }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Saved combinations display section
                if (savedFilters.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = if (currentLang == "ar") "الفلاتر المحفوظة" else "Saved Filters (${savedFilters.size}/${if (userTier == "Silver") "5" else "∞"})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(savedFilters) { saved ->
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            filter = saved.filter
                                            searchQueryInput = saved.filter.searchText
                                            Toast.makeText(context, "Loaded: ${saved.name}", Toast.LENGTH_SHORT).show()
                                        },
                                        label = { Text(saved.name) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete filter",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable {
                                                        searchPrefs.deleteSavedFilter(saved.id)
                                                        savedFilters = searchPrefs.getSavedFilters()
                                                    }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Header Results + Sort selection
                item {
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentLang == "ar") "النتائج (${filteredJobs.size})" else "Results (${filteredJobs.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // Sort dropdown
                        var showSortMenu by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { showSortMenu = true }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(filter.sortBy)
                                }
                            }
                            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                                val sortingOptions = listOf(
                                    "Most Recent",
                                    "Most Applicants",
                                    "Most Viewed",
                                    "Nearest to me"
                                )
                                sortingOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = {
                                            if (opt == "Nearest to me") {
                                                // Trigger location fetching/permission
                                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                    locationPermissionLauncher.launch(
                                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                                    )
                                                } else {
                                                    fetchGpsLocation(context) { lat, lng ->
                                                        userLocation = Pair(lat, lng)
                                                    }
                                                }
                                            }
                                            filter = filter.copy(sortBy = opt)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Render Jobs Grid
                if (filteredJobs.isEmpty()) {
                    item {
                        com.example.ui.components.EmptyJobsState()
                    }
                } else {
                    itemsIndexed(filteredJobs, key = { _, job -> job.id }) { index, job ->
                        
                        if ((admobEnabled || viewModel.adPlacements.value.isNotEmpty()) && showBannerOnFeed && index > 0 && index % feedAdFrequency == 0) {
                            com.example.ui.SmartBannerAd(
                                adUnitId = bannerAdUnitId,
                                viewModel = viewModel,
                                navController = navController,
                                modifier = Modifier.padding(bottom = 16.dp).clip(RoundedCornerShape(12.dp))
                            )
                        }

                        val localContext = LocalContext.current
                        LaunchedEffect(job.userId) {
                            viewModel.loadCompanyProfile(job.userId)
                        }
                        val companyProfile = companyProfiles[job.userId]
                        val companyName = companyProfile?.companyName ?: "Independent Recruiter"
                        JobCard(
                            job = job,
                            companyName = companyName,
                            onActionClick = {
                                navController.navigate(Screen.JobDetails.createRoute(job.id))
                            },
                            onApplyClick = {
                                if (viewModel.isGuest) {
                                    showSignUpDialog = true
                                } else {
                                    viewModel.applyForJob(job)
                                }
                            },
                            onFavoriteClick = {
                                if (viewModel.isGuest) {
                                    showSignUpDialog = true
                                } else {
                                    viewModel.toggleFavoriteJob(job)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal / Draggable Filter Bottom Sheet Component
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            FilterBottomSheetContent(
                initialFilter = filter,
                resultsCount = filteredJobs.size,
                userLocation = userLocation,
                locationPermissionLauncher = locationPermissionLauncher,
                onSaveCombinationClick = {
                    saveFilterName = ""
                    showSaveFilterDialog = true
                },
                onApply = { newFilter ->
                    filter = newFilter
                    showFilterSheet = false
                },
                onClearAll = {
                    filter = FilterModel(searchText = filter.searchText, sortBy = filter.sortBy)
                    showFilterSheet = false
                }
            )
        }
    }

    // Save Filter combination Dialog
    if (showSaveFilterDialog) {
        AlertDialog(
            onDismissRequest = { showSaveFilterDialog = false },
            title = { Text(if (currentLang == "ar") "حفظ الفلتر الحالي" else "Save Filter Combination") },
            text = {
                Column {
                    Text(if (currentLang == "ar") "أدخل اسم الفلتر لحفظ المزيج الحالي لسهولة الوصول إليه." else "Enter a descriptive name to save this custom filter mix.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = saveFilterName,
                        onValueChange = { saveFilterName = it },
                        singleLine = true,
                        placeholder = { Text("e.g. Software in Riyadh") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nameToSave = saveFilterName.trim()
                        if (nameToSave.isNotEmpty()) {
                            val success = searchPrefs.saveFilter(nameToSave, filter)
                            if (success) {
                                savedFilters = searchPrefs.getSavedFilters()
                                showSaveFilterDialog = false
                                Toast.makeText(context, "Filter successfully saved!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Limit exceeded! Silver accounts are capped at only 5 saved filters. Click on the badge at top right to upgrade to unlimited Gold plan!", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text(if (currentLang == "ar") "حفظ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveFilterDialog = false }) {
                    Text(if (currentLang == "ar") "إلغاء الأمر" else "Cancel")
                }
            }
        )
    }
}

// Active chips list below the search inputs row
@Composable
fun ActiveFilterChipsRow(
    filter: FilterModel,
    onRemoveFilter: (String) -> Unit,
    onClearAll: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (filter.jobTypes.isNotEmpty()) {
            filter.jobTypes.forEach { type ->
                item {
                    FilterChipToRemove(label = "Type: $type", onRemove = { onRemoveFilter("jobType:$type") })
                }
            }
        }
        if (filter.location != "All") {
            item {
                FilterChipToRemove(label = "Location: ${filter.location}", onRemove = { onRemoveFilter("location") })
            }
        }
        if (filter.experienceLevel != "Any") {
            item {
                FilterChipToRemove(label = "Exp: ${filter.experienceLevel}", onRemove = { onRemoveFilter("experienceLevel") })
            }
        }
        if (filter.education != "Any") {
            item {
                FilterChipToRemove(label = "Edu: ${filter.education}", onRemove = { onRemoveFilter("education") })
            }
        }
        if (filter.nationalityType != "Any") {
            item {
                FilterChipToRemove(label = "Labor: ${filter.nationalityType}", onRemove = { onRemoveFilter("nationalityType") })
            }
        }
        if (filter.gender != "Any") {
            item {
                FilterChipToRemove(label = "Gender: ${filter.gender}", onRemove = { onRemoveFilter("gender") })
            }
        }
        if (filter.postingDate != "Any time") {
            item {
                FilterChipToRemove(label = "Posted: ${filter.postingDate}", onRemove = { onRemoveFilter("postingDate") })
            }
        }
        if (filter.jobCategory != "All") {
            item {
                FilterChipToRemove(label = "Cat: ${filter.jobCategory}", onRemove = { onRemoveFilter("jobCategory") })
            }
        }
        if (filter.companyType != "Any") {
            item {
                FilterChipToRemove(label = "Company: ${filter.companyType}", onRemove = { onRemoveFilter("companyType") })
            }
        }
        // Save filter combination
        item {
            SuggestionChip(
                onClick = onClearAll,
                label = { Text("Clear All", color = MaterialTheme.colorScheme.error) },
                icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp)) }
            )
        }
    }
}

@Composable
fun FilterChipToRemove(label: String, onRemove: () -> Unit) {
    InputChip(
        selected = true,
        onClick = {},
        label = { Text(label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier
                    .size(12.dp)
                    .clickable { onRemove() }
            )
        }
    )
}

// Logic helper for individual chip removal
fun removeSingleFilterSetting(filter: FilterModel, tag: String): FilterModel {
    return when {
        tag.startsWith("jobType:") -> {
            val typeToRemove = tag.substringAfter("jobType:")
            filter.copy(jobTypes = filter.jobTypes - typeToRemove)
        }
        tag == "location" -> filter.copy(location = "All")
        tag == "experienceLevel" -> filter.copy(experienceLevel = "Any")
        tag == "education" -> filter.copy(education = "Any")
        tag == "nationalityType" -> filter.copy(nationalityType = "Any")
        tag == "gender" -> filter.copy(gender = "Any")
        tag == "postingDate" -> filter.copy(postingDate = "Any time")
        tag == "jobCategory" -> filter.copy(jobCategory = "All")
        tag == "companyType" -> filter.copy(companyType = "Any")
        else -> filter
    }
}

// Search and filter bottom sheet configuration with scroll states
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheetContent(
    initialFilter: FilterModel,
    resultsCount: Int,
    userLocation: Pair<Double, Double>?,
    locationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    onSaveCombinationClick: () -> Unit,
    onApply: (FilterModel) -> Unit,
    onClearAll: () -> Unit
) {
    val context = LocalContext.current
    val currentLang = com.example.LocalCurrentLanguage.current
    var filterState by remember { mutableStateOf(initialFilter) }

    val saudiCities = listOf(
        "Riyadh", "Jeddah", "Mecca", "Medina", "Dammam", "Khobar", "Tabuk", "Abha",
        "Hail", "Najran", "Jizan", "Yanbu", "Qassim", "Al-Ahsa", "Jubail"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 680.dp)
            .padding(16.dp)
    ) {
        // Drag and Header Title Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentLang == "ar") "خيارات التصفية" else "Filter Options",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onSaveCombinationClick) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = "Save shortcut", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Save", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Scrollable content body using column with verticalScroll to allow proper nested sliding
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. JOB CATEGORIES
            Column {
                Text(
                    text = if (currentLang == "ar") "فئة الوظيفة" else "Job Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val allCats = listOf("All") + ConfigData.getActiveCategoriesEn()
                    items(allCats) { cat ->
                        FilterChip(
                            selected = filterState.jobCategory == cat,
                            onClick = { filterState = filterState.copy(jobCategory = cat) },
                            label = { Text(if (cat == "All") "All Categories" else cat) }
                        )
                    }
                }
            }

            // 2. JOB TYPE (multi-select)
            Column {
                Text(
                    text = if (currentLang == "ar") "نوع العمل" else "Job Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val jobTypesList = listOf("Full Time", "Part Time", "Contract", "Freelance", "Daily Work", "Seasonal")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp
                ) {
                    jobTypesList.forEach { type ->
                        val isSelected = filterState.jobTypes.contains(type)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val current = filterState.jobTypes
                                filterState = if (isSelected) {
                                    filterState.copy(jobTypes = current - type)
                                } else {
                                    filterState.copy(jobTypes = current + type)
                                }
                            },
                            label = { Text(type) }
                        )
                    }
                }
            }

            // 4. LOCATION (searchable dropdown)
            Column {
                Text(
                    text = if (currentLang == "ar") "الموقع" else "Location (Saudi Arabia)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                var locationSearchQuery by remember { mutableStateOf("") }
                var showDropdown by remember { mutableStateOf(false) }

                val locOptions = listOf("All", "Remote / Work from home", "Near me") + saudiCities
                val filteredOptions = locOptions.filter {
                    it.contains(locationSearchQuery, ignoreCase = true)
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (showDropdown) locationSearchQuery else filterState.location,
                        onValueChange = {
                            locationSearchQuery = it
                            showDropdown = true
                        },
                        placeholder = { Text("Search city/region, remote...") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDropdown = !showDropdown }) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Toggle list")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        filteredOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    if (opt == "Near me") {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                            locationPermissionLauncher.launch(
                                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                            )
                                        } else {
                                            fetchGpsLocation(context) { _, _ -> }
                                        }
                                    }
                                    filterState = filterState.copy(location = opt)
                                    locationSearchQuery = ""
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // 5. EXPERIENCE LEVEL (single select)
            Column {
                Text(
                    text = if (currentLang == "ar") "مستوى الخبرة" else "Experience Level",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val expList = listOf("Any", "No Experience", "Less than 1 year", "1-3 years", "3-5 years", "5-10 years", "10+ years")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp
                ) {
                    expList.forEach { exp ->
                        FilterChip(
                            selected = filterState.experienceLevel == exp,
                            onClick = { filterState = filterState.copy(experienceLevel = exp) },
                            label = { Text(exp) }
                        )
                    }
                }
            }

            // 6. EDUCATION (single select)
            Column {
                Text(
                    text = if (currentLang == "ar") "المستوى التعليمي" else "Education",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val eduList = listOf("Any", "High School", "Diploma", "Bachelor's", "Master's", "PhD")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp
                ) {
                    eduList.forEach { edu ->
                        FilterChip(
                            selected = filterState.education == edu,
                            onClick = { filterState = filterState.copy(education = edu) },
                            label = { Text(edu) }
                        )
                    }
                }
            }

            // 7. NATIONALITY
            Column {
                Text(
                    text = if (currentLang == "ar") "متطلبات الجنسية" else "Nationality (Saudi Labor Alignment)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val natList = listOf("Any", "Saudi Only", "Open to Expats")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp
                ) {
                    natList.forEach { nat ->
                        FilterChip(
                            selected = filterState.nationalityType == nat,
                            onClick = { filterState = filterState.copy(nationalityType = nat) },
                            label = { Text(nat) }
                        )
                    }
                }
            }

            // 8. GENDER
            Column {
                Text(
                    text = if (currentLang == "ar") "الجنس المحدد" else "Gender Specification",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val genList = listOf("Any", "Male", "Female")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp
                ) {
                    genList.forEach { gen ->
                        FilterChip(
                            selected = filterState.gender == gen,
                            onClick = { filterState = filterState.copy(gender = gen) },
                            label = { Text(gen) }
                        )
                    }
                }
            }

            // 9. POSTING DATE
            Column {
                Text(
                    text = if (currentLang == "ar") "تاريخ النشر" else "Posting Date",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val dateOptions = listOf("Any time", "Last 24 hours", "Last 3 days", "Last week", "Last month")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp
                ) {
                    dateOptions.forEach { dst ->
                        FilterChip(
                            selected = filterState.postingDate == dst,
                            onClick = { filterState = filterState.copy(postingDate = dst) },
                            label = { Text(dst) }
                        )
                    }
                }
            }

            // 10. COMPANY TYPE
            Column {
                Text(
                    text = if (currentLang == "ar") "نوع الشركة" else "Company Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val compList = listOf("Any", "Verified Only", "Premium Company", "Government", "Private", "Startup")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 4.dp
                ) {
                    compList.forEach { comp ->
                        FilterChip(
                            selected = filterState.companyType == comp,
                            onClick = { filterState = filterState.copy(companyType = comp) },
                            label = { Text(comp) }
                        )
                    }
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 12.dp))

        // Confirmation Actions bottom Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onClearAll,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Text(
                    text = if (currentLang == "ar") "مسح الكل" else "Clear All",
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(
                onClick = { onApply(filterState) },
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .testTag("submit_filter_button")
            ) {
                Text(
                    text = if (currentLang == "ar") "عرض $resultsCount نتيجة" else "Show $resultsCount Results",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// FlowRow layout helper for chip grids
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(childConstraints) }

        val layoutWidth = constraints.maxWidth
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0

        placeables.forEach { placeable ->
            if (currentRowWidth + placeable.width + mainAxisSpacing.roundToPx() > layoutWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width + mainAxisSpacing.roundToPx()
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val heights = rows.map { row -> row.maxOfOrNull { it.height } ?: 0 }
        val totalHeight = heights.sum() + (rows.size - 1) * crossAxisSpacing.roundToPx()

        layout(layoutWidth, totalHeight) {
            var currentY = 0
            rows.forEachIndexed { rowIndex, row ->
                var currentX = 0
                val rowHeight = heights[rowIndex]
                row.forEach { placeable ->
                    placeable.placeRelative(currentX, currentY + (rowHeight - placeable.height) / 2)
                    currentX += placeable.width + mainAxisSpacing.roundToPx()
                }
                currentY += rowHeight + crossAxisSpacing.roundToPx()
            }
        }
    }
}

// GPS coordinates helper mapping
fun getCityCoords(name: String): Pair<Double, Double> {
    return when (name.lowercase(Locale.getDefault()).trim()) {
        "riyadh" -> Pair(24.7136, 46.6753)
        "jeddah" -> Pair(21.4858, 39.1925)
        "mecca" -> Pair(21.3891, 39.8579)
        "medina" -> Pair(24.4673, 39.6111)
        "dammam" -> Pair(26.4207, 50.0888)
        "khobar" -> Pair(26.2172, 50.1971)
        "tabuk" -> Pair(28.3835, 36.5662)
        "abha" -> Pair(18.2164, 42.5053)
        "hail" -> Pair(27.5114, 41.7208)
        "najran" -> Pair(17.4938, 44.1311)
        "jizan" -> Pair(16.8894, 42.5511)
        "yanbu" -> Pair(24.0891, 38.0637)
        "qassim" -> Pair(26.3260, 43.9784)
        "al-ahsa" -> Pair(25.3833, 49.5833)
        "jubail" -> Pair(26.9600, 49.6644)
        else -> Pair(24.7136, 46.6753) // Fall Riyadh
    }
}

// Earth distance calculation (Haversine formula) in kilometers
fun calculateDist(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}

// Location Services GPS Fetching
fun fetchGpsLocation(context: Context, onLocationFound: (Double, Double) -> Unit) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val bestLocation = gpsLocation ?: netLocation
            if (bestLocation != null) {
                onLocationFound(bestLocation.latitude, bestLocation.longitude)
            } else {
                onLocationFound(24.7136, 46.6753) // Standard default
            }
        } catch (e: SecurityException) {
            onLocationFound(24.7136, 46.6753)
        }
    } else {
        onLocationFound(24.7136, 46.6753)
    }
}

// Support scale UI modifier
fun Modifier.scale(scale: Float) = this.graphicsLayer(scaleX = scale, scaleY = scale)
