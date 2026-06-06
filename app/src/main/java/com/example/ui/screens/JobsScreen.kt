package com.example.ui.screens

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import com.example.LocalSharedTransitionScope
import com.example.LocalAnimatedVisibilityScope

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.Job
import com.example.ui.TalentViewModel
import com.example.ui.components.TopBarWithLanguage
import com.example.ui.components.CategoryFilterWidget

import androidx.navigation.NavController

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.pullrefresh.PullRefreshIndicator

import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun JobsScreen(viewModel: TalentViewModel, navController: NavController, initialCategory: String? = null) {
    val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    val companyProfiles by viewModel.companyProfiles.collectAsStateWithLifecycle()
    var showSignUpDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory ?: "All") }

    LaunchedEffect(initialCategory) {
        if (!initialCategory.isNullOrBlank()) {
            selectedCategory = initialCategory
        }
    }
    var showCategoryFilterDialog by remember { mutableStateOf(false) }
    var showCategoryPickerDialog by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf("All") }
    var selectedCity by remember { mutableStateOf("All") }
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedJobType by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Recent") }
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val appConfig by viewModel.appConfig.collectAsStateWithLifecycle()
    val admobEnabled = appConfig["admobEnabled"] as? Boolean ?: false
    val showBannerOnFeed = appConfig["showBannerOnFeed"] as? Boolean ?: true
    val bannerAdUnitId = appConfig["bannerAdUnitId"] as? String ?: "ca-app-pub-3940256099942544/6300978111"

    val categories = listOf("All") + com.example.ui.ConfigData.getActiveCategoriesEn()
    val countries = listOf("All") + com.example.ui.ConfigData.gulfCountriesWithCities.keys.toList()
    val currentLang = com.example.LocalCurrentLanguage.current

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

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { 
            viewModel.refreshJobs()
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TopBarWithLanguage(navController, viewModel)
            
            // Search and Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(
                            text = "Search jobs...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                IconButton(
                    onClick = { showCategoryFilterDialog = true },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FilterList,
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter by Category Button
            Button(
                onClick = { showCategoryPickerDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentLang == "ar") "تصفية حسب الفئة" else "Filter by Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showCategoryFilterDialog) {
            com.example.ui.components.AdvancedJobFilterWidget(
                selectedCategory = selectedCategory,
                selectedLocation = selectedCountry, // reusing Country selection for location filter
                selectedJobType = selectedJobType,
                sortBy = sortBy,
                onDismissRequest = { showCategoryFilterDialog = false },
                onApply = { category, location, jobType, sort ->
                    selectedCategory = category
                    selectedCountry = location
                    selectedJobType = jobType
                    sortBy = sort
                }
            )
        }

        if (showCategoryPickerDialog) {
            CategoryFilterWidget(
                selectedCategory = selectedCategory,
                onDismissRequest = { showCategoryPickerDialog = false },
                onApply = { selectedCategory = it }
            )
        }

        val filteredJobs = jobs.filter { job ->
            val matchesCategory = (selectedCategory == "All" || job.category.equals(selectedCategory, ignoreCase = true))
            val matchesCountry = (selectedCountry == "All" || job.country.equals(selectedCountry, ignoreCase = true) || job.location.equals(selectedCountry, ignoreCase = true))
            val matchesCity = (selectedCity == "All" || job.location.equals(selectedCity, ignoreCase = true))
            val matchesJobType = (selectedJobType == "All" || job.jobType.equals(selectedJobType, ignoreCase = true))

            val matchesQuery = (searchQuery.isEmpty() || 
                job.title.contains(searchQuery, ignoreCase = true) || 
                job.description.contains(searchQuery, ignoreCase = true) || 
                job.category.contains(searchQuery, ignoreCase = true) || 
                job.location.contains(searchQuery, ignoreCase = true))
                
            matchesCategory && matchesCountry && matchesCity && matchesJobType && matchesQuery
        }.sortedWith(
            if (sortBy == "Recent") {
                compareByDescending<com.example.data.Job> { it.isBoosted }.thenByDescending { it.timestamp }
            } else {
                compareByDescending<com.example.data.Job> { it.isBoosted }.thenByDescending { job ->
                    try { job.budget.replace(Regex("[^0-9]"), "").toFloat() } catch (e: Exception) { 0f }
                }
            }
        )

        val jobCounts = remember(jobs) { viewModel.getCategoryJobCounts(jobs) }
        
        // Horizontal Category Quick Filter Row
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = (selectedCategory == cat)
                CategoryFilterChip(
                    category = cat,
                    isSelected = isSelected,
                    onClick = { selectedCategory = cat },
                    currentLang = currentLang,
                    count = if (cat == "All") jobs.size else jobCounts[cat] ?: 0
                )
            }
        }

        // Section Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val titleText = if (selectedCategory == "All") {
                "${stringResource(R.string.latest_jobs)} (${filteredJobs.size} total)"
            } else {
                com.example.ui.ConfigData.getCategoryName(selectedCategory, currentLang) + " " + stringResource(R.string.latest_jobs) + " (${filteredJobs.size})"
            }
            Text(
                text = titleText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedCategory != "All") {
                    val isPinned = viewModel.favoriteCategories.collectAsState().value.contains(selectedCategory)
                    val localContext = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = {
                        val authUserId = viewModel.auth?.currentUser?.uid
                        if (authUserId == null) {
                            showSignUpDialog = true
                        } else {
                            viewModel.toggleFavoriteCategory(selectedCategory)
                            if (isPinned) {
                                android.widget.Toast.makeText(localContext, "Category unpinned", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(localContext, "Category pinned for notifications", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }, modifier = Modifier.size(32.dp)) {
                        androidx.compose.material3.Icon(
                            imageVector = if (isPinned) androidx.compose.material.icons.Icons.Default.NotificationsActive else androidx.compose.material.icons.Icons.Outlined.NotificationsNone,
                            contentDescription = "Pin Category",
                            tint = if (isPinned) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (selectedCategory != "All") {
                    Text(
                        text = if (currentLang == "ar") "عرض الكل" else "Clear",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { selectedCategory = "All" }
                            .padding(start = 8.dp)
                    )
                }
            }
        }

        val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
        val hasMoreJobs by viewModel.hasMoreJobs.collectAsStateWithLifecycle()

        LaunchedEffect(gridState) {
            androidx.compose.runtime.snapshotFlow {
                gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            }.collect { lastIndex ->
                val total = gridState.layoutInfo.totalItemsCount
                if (lastIndex != null && lastIndex >= total - 3 && hasMoreJobs) {
                    viewModel.loadMoreJobs()
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (filteredJobs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        emoji = "💼",
                        title = "No Jobs Found",
                        subtitle = "Try different keywords or\ncheck back later for new jobs",
                        actionLabel = "Clear Filters",
                        onAction = { 
                            searchQuery = ""
                        }
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    state = gridState,
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(filteredJobs, key = { _, job -> job.id }) { index, job ->
                        if ((admobEnabled || viewModel.adPlacements.value.isNotEmpty()) && showBannerOnFeed && index > 0 && index % (appConfig["feedAdFrequency"] as? Number ?: 5).toInt() == 0) {
                            com.example.ui.SmartBannerAd(
                                adUnitId = bannerAdUnitId,
                                viewModel = viewModel,
                                navController = navController,
                                modifier = Modifier.padding(bottom = 16.dp).clip(RoundedCornerShape(12.dp))
                            )
                        }
                        val localContext = androidx.compose.ui.platform.LocalContext.current
                        LaunchedEffect(job.userId) {
                            viewModel.loadCompanyProfile(job.userId)
                        }
                        val companyProfile = companyProfiles[job.userId]
                        val companyName = companyProfile?.companyName ?: "Independent Recruiter"
                        JobCard(
                            job = job,
                            companyName = companyName,
                            onActionClick = {
                                navController.navigate(com.example.ui.Screen.JobDetails.createRoute(job.id))
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
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        if (hasMoreJobs) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFFD4AF37),
                                    strokeWidth = 2.dp
                                )
                            }
                        } else {
                            Text(
                                "No more jobs",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color(0xFF6B7280),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun CategoryFilterChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    currentLang: String,
    count: Int = 0
) {
    val displayName = if (category == "All") (if (currentLang == "ar") "الكل" else "All") else com.example.ui.ConfigData.getCategoryName(category, currentLang)
    val accentColor = if (category == "All") MaterialTheme.colorScheme.primary else com.example.ui.ConfigData.getCategoryColor(category)
    val icon = if (category == "All") androidx.compose.material.icons.Icons.Rounded.Work else com.example.ui.ConfigData.getIconForCategory(category)
    
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = displayName,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else accentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$displayName ${if (count > 0) "($count)" else ""}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun JobCard(
    job: Job,
    companyName: String? = null,
    onActionClick: () -> Unit,
    onApplyClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else if (isHovered) 1.02f else 1f,
        label = "scale"
    )
    
    val elevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isHovered) 8.dp else 2.dp,
        label = "elevation"
    )

    ElevatedCard(
        onClick = onActionClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = com.example.ui.ConfigData.getIconForCategory(job.category),
                            contentDescription = job.category,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = job.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = companyName ?: "Independent Recruiter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (job.isDeactivated) {
                            Text(
                                "🚫 Deactivated (Reported/Violation)",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else if (job.isBoosted) {
                            val diff = job.boostEndDate - System.currentTimeMillis()
                            val remainingText = if (diff > 0) {
                                val days = diff / (24L * 60 * 60 * 1000)
                                val hours = (diff % (24L * 60 * 60 * 1000)) / (60 * 60 * 1000)
                                if (days > 0) "${days}d ${hours}h left" else "${hours}h left"
                            } else {
                                "Pinned"
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .background(Color(0xFFFFD700).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "📌 Featured ($remainingText)",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                IconButton(onClick = onFavoriteClick, modifier = Modifier.size(32.dp)) {
                    val scale = animateFloatAsState(if (job.isFavorite) 1.2f else 1f, label = "scale")
                    Icon(
                        imageVector = if (job.isFavorite) androidx.compose.material.icons.Icons.Filled.Favorite else androidx.compose.material.icons.Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save Job",
                        tint = if (job.isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${job.location}, ${job.country}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = com.example.utils.TimeUtils.getRelativeTime(job.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "❤️ ${job.applicantsCount} saved",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onApplyClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Apply Now",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
