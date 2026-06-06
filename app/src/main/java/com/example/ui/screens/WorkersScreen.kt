package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import com.example.R
import com.example.data.Worker
import com.example.ui.TalentViewModel
import com.example.ui.components.TopBarWithLanguage
import com.example.ui.components.CategoryFilterWidget

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.pullrefresh.PullRefreshIndicator

import androidx.compose.material.icons.rounded.Star
import androidx.navigation.NavController
import androidx.compose.runtime.LaunchedEffect

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun WorkersScreen(viewModel: TalentViewModel, navController: NavController, initialWorkerId: String? = null, chatViewModel: com.example.ui.ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val workers by viewModel.workers.collectAsStateWithLifecycle()
    var showSignUpDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedProfession by remember { mutableStateOf("All") }
    var showCategoryFilterDialog by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf("All") }
    var selectedCity by remember { mutableStateOf("All") }
    var showProfessionMenu by remember { mutableStateOf(false) }
    var showCountryMenu by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf<Worker?>(null) }

    LaunchedEffect(initialWorkerId, workers) {
        if (!initialWorkerId.isNullOrBlank()) {
            val matchingWorker = workers.find { it.id == initialWorkerId }
            if (matchingWorker != null) {
                showProfileDialog = matchingWorker
            }
        }
    }

    val professions = listOf("All") + com.example.ui.ConfigData.getActiveCategoriesEn()
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

    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val appConfig by viewModel.appConfig.collectAsStateWithLifecycle()
    val admobEnabled = appConfig["admobEnabled"] as? Boolean ?: false
    val showBannerOnFeed = appConfig["showBannerOnFeed"] as? Boolean ?: true
    val feedAdFrequency = (appConfig["feedAdFrequency"] as? Number)?.toInt() ?: 5
    val bannerAdUnitId = appConfig["bannerAdUnitId"] as? String ?: "ca-app-pub-3940256099942544/6300978111"

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            viewModel.refreshWorkers()
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.search_talent), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                    leadingIcon = { Text("🔍") },
                    modifier = Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(26.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = { showProfessionMenu = !showProfessionMenu },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (showProfessionMenu) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (showProfessionMenu) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter by Category Button (Employer Side - Identical UI, layout, and behavior)
            Button(
                onClick = { showCategoryFilterDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentLang == "ar") "تصفية حسب الفئة" else "Filter by Category",
                    fontFamily = com.example.ui.theme.PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            if (showProfessionMenu) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Advanced Filter", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    var expandedCountry by remember { mutableStateOf(false) }
                    Column {
                        Text("Country", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box {
                            OutlinedButton(onClick = { expandedCountry = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (selectedCountry == "All") "All Countries" else selectedCountry)
                            }
                            DropdownMenu(expanded = expandedCountry, onDismissRequest = { expandedCountry = false }) {
                                countries.forEach { cou ->
                                    DropdownMenuItem(text = { Text(cou) }, onClick = { selectedCountry = cou; selectedCity = "All"; expandedCountry = false })
                                }
                            }
                        }
                    }
                    var expandedCity by remember { mutableStateOf(false) }
                    if (selectedCountry != "All") {
                        val cities = listOf("All") + (com.example.ui.ConfigData.gulfCountriesWithCities[selectedCountry] ?: emptyList())
                        Column {
                            Text("City", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                OutlinedButton(onClick = { expandedCity = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(if (selectedCity == "All") "All Cities" else selectedCity)
                                }
                                DropdownMenu(expanded = expandedCity, onDismissRequest = { expandedCity = false }) {
                                    cities.forEach { cit ->
                                        DropdownMenuItem(text = { Text(cit) }, onClick = { selectedCity = cit; expandedCity = false })
                                    }
                                }
                            }
                        }
                    }
                    var expandedProfession by remember { mutableStateOf(false) }
                    Column {
                        Text("Profession", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box {
                            val selectedLabel = if (selectedProfession == "All") (if (currentLang == "ar") "جميع المهن" else "All Professions") else com.example.ui.ConfigData.getCategoryName(selectedProfession, currentLang)
                            OutlinedButton(onClick = { expandedProfession = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(selectedLabel)
                            }
                            DropdownMenu(expanded = expandedProfession, onDismissRequest = { expandedProfession = false }) {
                                professions.forEach { proff ->
                                    val proffLabel = if (proff == "All") (if (currentLang == "ar") "جميع المهن" else "All Professions") else com.example.ui.ConfigData.getCategoryName(proff, currentLang)
                                    DropdownMenuItem(text = { Text(proffLabel) }, onClick = { selectedProfession = proff; expandedProfession = false })
                                }
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { 
                            selectedCountry = "All"
                            selectedCity = "All"
                            selectedProfession = "All"
                        }) {
                            Text("Reset Filters")
                        }
                    }
                }
            }
        }

        if (showCategoryFilterDialog) {
            CategoryFilterWidget(
                selectedCategory = selectedProfession,
                onDismissRequest = { showCategoryFilterDialog = false },
                onApply = { selectedProfession = it }
            )
        }

        val filteredWorkers = workers.filter { 
            val workerProfessions = it.profession.split(",").map { p -> p.trim() }
            val matchesProfession = selectedProfession == "All" || workerProfessions.any { p -> p.equals(selectedProfession, ignoreCase = true) }
            
            matchesProfession && 
            (selectedCountry == "All" || it.country == selectedCountry) && 
            (selectedCity == "All" || it.location == selectedCity) &&
            (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) || it.profession.contains(searchQuery, ignoreCase = true) || it.location.contains(searchQuery, ignoreCase = true) || it.country.contains(searchQuery, ignoreCase = true))
        }.sortedByDescending { it.isBoosted }

        val workerCounts = remember(workers) { viewModel.getCategoryWorkerCounts(workers) }
        // Horizontal Profession Quick Filter Row
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            items(professions) { proff ->
                val isSelected = (selectedProfession == proff)
                WorkerCategoryFilterChip(
                    category = proff,
                    count = workerCounts[proff] ?: 0,
                    isSelected = isSelected,
                    onClick = { selectedProfession = proff },
                    currentLang = currentLang,
                    totalWorkers = workers.size
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
            val titleText = if (selectedProfession == "All") {
                stringResource(R.string.workers)
            } else {
                com.example.ui.ConfigData.getCategoryName(selectedProfession, currentLang) + " " + stringResource(R.string.workers)
            }
            Text(
                text = titleText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedProfession != "All") {
                    val isPinned = viewModel.favoriteCategories.collectAsState().value.contains(selectedProfession)
                    val localContext = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = {
                        val authUserId = viewModel.auth?.currentUser?.uid
                        if (authUserId == null) {
                            // Can't show dialog maybe, but we can just toast
                            android.widget.Toast.makeText(localContext, "Please login first", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.toggleFavoriteCategory(selectedProfession)
                            if (isPinned) {
                                android.widget.Toast.makeText(localContext, "Category unpinned", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(localContext, "Category pinned for notifications", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isPinned) androidx.compose.material.icons.Icons.Default.NotificationsActive else androidx.compose.material.icons.Icons.Outlined.NotificationsNone,
                            contentDescription = "Pin Category",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            
                if (selectedProfession != "All" || searchQuery.isNotEmpty() || selectedCountry != "All" || selectedCity != "All") {
                    Text(
                        text = if (currentLang == "ar") "مسح التصفية" else "Clear Filter",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { 
                            selectedProfession = "All"
                            searchQuery = ""
                            selectedCountry = "All"
                            selectedCity = "All"
                        }.padding(start = 8.dp)
                    )
                }
            }
        }

        val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
        val hasMoreWorkers by viewModel.hasMoreWorkers.collectAsStateWithLifecycle()

        LaunchedEffect(gridState) {
            androidx.compose.runtime.snapshotFlow {
                gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            }.collect { lastIndex ->
                val total = gridState.layoutInfo.totalItemsCount
                if (lastIndex != null && lastIndex >= total - 3 && hasMoreWorkers) {
                    viewModel.loadMoreWorkers()
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (filteredWorkers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        emoji = "👷",
                        title = "No Workers Found",
                        subtitle = "Try different category or\ncheck back soon for new workers",
                        actionLabel = "Clear Filters",
                        onAction = { searchQuery = "" }
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 320.dp),
                    state = gridState,
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(filteredWorkers, key = { _, worker -> worker.id }) { index, worker ->
                        if ((admobEnabled || viewModel.adPlacements.value.isNotEmpty()) && showBannerOnFeed && index > 0 && index % feedAdFrequency == 0) {
                            com.example.ui.SmartBannerAd(
                                adUnitId = bannerAdUnitId,
                                viewModel = viewModel,
                                navController = navController,
                                modifier = Modifier.padding(bottom = 16.dp).clip(RoundedCornerShape(12.dp))
                            )
                        }
                        WorkerCard(
                            worker = worker, 
                            onCardClick = { showProfileDialog = worker },
                            onActionClick = {
                                if (viewModel.isGuest) {
                                    showSignUpDialog = true
                                } else {
                                    // Action handles inside WorkerProfileDialog
                                }
                            },
                            onFavoriteClick = {
                                if (viewModel.isGuest) {
                                    showSignUpDialog = true
                                } else {
                                    viewModel.toggleFavoriteWorker(worker)
                                }
                            }
                        )
                    }
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        if (hasMoreWorkers) {
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
                                "No more workers",
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
        
        showProfileDialog?.let { currentWorker ->
            val context = androidx.compose.ui.platform.LocalContext.current
            WorkerProfileDialog(
                worker = currentWorker,
                isGuest = viewModel.isGuest,
                viewModel = viewModel,
                onDismiss = { showProfileDialog = null },
                onActionClick = { 
                    if (viewModel.isGuest) showSignUpDialog = true 
                },
                onChatClick = {
                    if (viewModel.isGuest) {
                        showSignUpDialog = true
                    } else {
                        chatViewModel.startChat(
                            otherUserId = currentWorker.userId,
                            otherUserName = currentWorker.name,
                            otherUserPhoto = currentWorker.photoUrl
                        ) { roomId ->
                            showProfileDialog = null
                            navController.navigate(com.example.ui.Screen.ChatDetail.createRoute(roomId, currentWorker.userId, currentWorker.name))
                        }
                    }
                }
            )
        }
        }
    }
}

@Composable
fun WorkerCategoryFilterChip(
    category: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    currentLang: String,
    totalWorkers: Int
) {
    val displayName = if (category == "All") (if (currentLang == "ar") "الكل" else "All") else com.example.ui.ConfigData.getCategoryName(category, currentLang)
    val accentColor = if (category == "All") MaterialTheme.colorScheme.primary else com.example.ui.ConfigData.getCategoryColor(category)
    val icon = if (category == "All") androidx.compose.material.icons.Icons.Default.Person else com.example.ui.ConfigData.getIconForCategory(category)
    
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
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = displayName,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else accentColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$displayName (${if (category == "All") totalWorkers else count})",
                fontFamily = if (currentLang == "ar") com.example.ui.theme.CairoFontFamily else com.example.ui.theme.PoppinsFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}


@Composable
fun WorkerCard(worker: Worker, onCardClick: () -> Unit, onActionClick: () -> Unit, onFavoriteClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else if (isHovered) 1.03f else 1f,
        label = "scale"
    )

    ElevatedCard(
        onClick = onCardClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isHovered) 10.dp else 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (worker.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = worker.photoUrl,
                            contentDescription = "${com.example.ui.UserService.sanitizeName(worker.name)} Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = com.example.ui.ConfigData.getIconForCategory(worker.profession),
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = com.example.ui.UserService.sanitizeName(worker.name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "✓", 
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 2.dp, end = 6.dp),
                            fontWeight = FontWeight.Bold
                        )
                        if ((worker.averageRating ?: 0.0) > 0.0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Star, null,
                                    tint = Color(0xFFD4AF37),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    String.format("%.1f", worker.averageRating ?: 0.0),
                                    color = Color(0xFFD4AF37),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    "(${worker.totalReviews ?: 0})",
                                    color = Color(0xFF6B7280),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    if (worker.isDeactivated) {
                        Text(
                            "🚫 Deactivated",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else if (worker.isBoosted) {
                        val diff = worker.boostEndDate - System.currentTimeMillis()
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
                    val currentLang = com.example.LocalCurrentLanguage.current
                    val professionsList = worker.profession.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        professionsList.forEach { p ->
                            val accentColor = com.example.ui.ConfigData.getCategoryColor(p)
                            val displayName = com.example.ui.ConfigData.getCategoryName(p, currentLang)
                            val pIcon = com.example.ui.ConfigData.getIconForCategory(p)
                            
                            Row(
                                modifier = Modifier
                                    .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = pIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = accentColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${worker.location}, ${worker.country}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    IconButton(onClick = onFavoriteClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (worker.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (worker.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE6F4EA))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = worker.availability.uppercase(),
                            color = Color(0xFF1E8E3E),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = worker.skills.joinToString(", "),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Contact buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Text(stringResource(R.string.whatsapp), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.call), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun WorkerProfileDialog(worker: Worker, isGuest: Boolean, viewModel: TalentViewModel, onDismiss: () -> Unit, onActionClick: () -> Unit, onChatClick: (() -> Unit)? = null) {
    val currentUserId = viewModel.getCurrentUserId()
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    var showReviewSheet by remember { mutableStateOf(false) }
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val userType = currentUserProfile?.get("userType") as? String ?: ""

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (com.example.LocalCurrentLanguage.current == "ar") "تفاصيل ملف التعريفي" else "Worker Profile",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (worker.userId != currentUserId && currentUserId.isNotEmpty()) {
                        IconButton(onClick = { showReportDialog = true }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Flag,
                                contentDescription = "Report",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (worker.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = worker.photoUrl,
                                contentDescription = "${com.example.ui.UserService.sanitizeName(worker.name)} Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(com.example.ui.UserService.sanitizeName(worker.name), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        val currentLang = com.example.LocalCurrentLanguage.current
                        val professionsList = worker.profession.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            professionsList.forEach { p ->
                                val accentColor = com.example.ui.ConfigData.getCategoryColor(p)
                                val displayName = com.example.ui.ConfigData.getCategoryName(p, currentLang)
                                val pIcon = com.example.ui.ConfigData.getIconForCategory(p)
                                
                                Row(
                                    modifier = Modifier
                                        .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                        .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = pIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = accentColor
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(4.dp))
                            Text("${worker.location}, ${worker.country}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (worker.experience.isNotEmpty()) {
                    Text("Experience: ${worker.experience}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (worker.availability.isNotEmpty()) {
                    Text("Availability: ${worker.availability}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Skills", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(worker.skills.joinToString(", "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                if (worker.resumeLink.isNotEmpty() || worker.portfolioWebsite.isNotEmpty() || worker.portfolioGithub.isNotEmpty() || worker.portfolioBehance.isNotEmpty() || worker.portfolioDribbble.isNotEmpty() || worker.portfolioYoutube.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Portfolio & Links", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (worker.resumeLink.isNotEmpty()) Text("📄 Resume: ${worker.resumeLink}", color = MaterialTheme.colorScheme.primary)
                        if (worker.portfolioWebsite.isNotEmpty()) Text("🌐 Website: ${worker.portfolioWebsite}", color = MaterialTheme.colorScheme.primary)
                        if (worker.portfolioGithub.isNotEmpty()) Text("💻 GitHub: ${worker.portfolioGithub}", color = MaterialTheme.colorScheme.primary)
                        if (worker.portfolioBehance.isNotEmpty()) Text("🎨 Behance: ${worker.portfolioBehance}", color = MaterialTheme.colorScheme.primary)
                        if (worker.portfolioDribbble.isNotEmpty()) Text("🏀 Dribbble: ${worker.portfolioDribbble}", color = MaterialTheme.colorScheme.primary)
                        if (worker.portfolioYoutube.isNotEmpty()) Text("▶️ YouTube: ${worker.portfolioYoutube}", color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                com.example.ui.components.ReviewsSection(
                    targetId = worker.userId.ifEmpty { worker.id },
                    talentViewModel = viewModel
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rate button — only for employers
                if (userType == "employer") {
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
                            Icons.Rounded.Star, null,
                            tint = Color(0xFF1A1A2E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Rate This Worker",
                            color = Color(0xFF1A1A2E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                if (showReviewSheet) {
                    com.example.ui.components.ReviewBottomSheet(
                        targetId = worker.userId.ifEmpty { worker.id },
                        targetName = worker.name,
                        targetType = "worker",
                        talentViewModel = viewModel,
                        onDismiss = { showReviewSheet = false }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                val context = androidx.compose.ui.platform.LocalContext.current
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Close")
                    }
                    if (onChatClick != null) {
                        Button(
                            onClick = onChatClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("Chat", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary)
                        }
                    }
                    Button(
                        onClick = {
                            if (isGuest) {
                                onActionClick()
                            } else if (worker.contact.trim().isNotEmpty()) {
                                try {
                                    val cleanNumber = worker.contact.replace("+", "").replace(" ", "").trim()
                                    val url = "https://api.whatsapp.com/send?phone=$cleanNumber"
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        data = android.net.Uri.parse(url)
                                        setPackage("com.whatsapp")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val cleanNumber = worker.contact.replace("+", "").replace(" ", "").trim()
                                    val url = "https://api.whatsapp.com/send?phone=$cleanNumber"
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            } else {
                                onActionClick()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isGuest && worker.contact.trim().isNotEmpty()) Color(0xFF25D366) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (!isGuest && worker.contact.trim().isNotEmpty()) "WhatsApp" else "Contact",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        if (showReportDialog) {
            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                containerColor = Color(0xFF1A1A2E),
                title = {
                    Text("Report This worker",
                        color = Color.White,
                        fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Why are you reporting this profile?",
                            color = Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.bodyMedium)
                        
                        val reasons = listOf(
                            "Fake or spam profile",
                            "Inappropriate content",
                            "Wrong category",
                            "Scam or fraud",
                            "Unresponsive link or contact",
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
                                    postId = worker.id,
                                    postType = "worker",
                                    reason = selectedReason,
                                    reportedUserId = worker.userId
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
