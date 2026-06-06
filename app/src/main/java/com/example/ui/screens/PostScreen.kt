package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import com.example.R
import com.example.ui.TalentViewModel
import com.example.ui.components.TopBarWithLanguage

import androidx.navigation.NavController

@Composable
fun PostScreen(viewModel: TalentViewModel, navController: NavController) {
    val localContext = androidx.compose.ui.platform.LocalContext.current
    var isPostingJob by remember { mutableStateOf(viewModel.currentUserType != "worker") }
    var showSignUpDialog by remember { mutableStateOf(false) }
    var proposedBoostId by remember { mutableStateOf<String?>(null) }

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

    proposedBoostId?.let { createdPostId ->
        AlertDialog(
            onDismissRequest = { proposedBoostId = null },
            title = { 
                Text(
                    text = if (com.example.LocalCurrentLanguage.current == "ar") "⚡ ترقية منشورك" else "⚡ Boost Your Post",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ) 
            },
            text = { 
                Text(
                    text = if (com.example.LocalCurrentLanguage.current == "ar") {
                        "اجعل منشورك مميزاً ويصل لجمهور أكبر! قم بتثبيت منشورك في أعلى نتائج البحث عبر اختيار إحدى خطط الترقية المخصصة والمدارة من قبل الإدارة."
                    } else {
                        "Make your post stand out and reach more people! Pin your post at the top of results by choosing one of our custom billing plans configured by the admin."
                    },
                    fontSize = 14.sp
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        proposedBoostId = null
                        navController.navigate("boost/$createdPostId")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (com.example.LocalCurrentLanguage.current == "ar") "ترقية الآن" else "Boost Now",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { proposedBoostId = null }
                ) {
                    Text(
                        text = if (com.example.LocalCurrentLanguage.current == "ar") "لاحقاً" else "Later"
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TopBarWithLanguage(navController, viewModel)

            Text(
                text = stringResource(R.string.post_and_connect),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = if (viewModel.isGuest || viewModel.currentUserType.isNullOrEmpty()) {
                    stringResource(R.string.looking_or_offering)
                } else if (viewModel.currentUserType == "employer") {
                    if (com.example.LocalCurrentLanguage.current == "ar") "هل تبحث عن المواهب؟ انشر وظيفة هنا." else "Looking for talent? Post a job here."
                } else {
                    if (com.example.LocalCurrentLanguage.current == "ar") "هل تقدم مهاراتك؟ قم بإنشاء ملفك الشخصي هنا." else "Offering your skills? Create your profile here."
                },
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            if (viewModel.isGuest || viewModel.currentUserType.isNullOrEmpty()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = isPostingJob,
                        onClick = { isPostingJob = true },
                        label = { Text(stringResource(R.string.post_a_job)) },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                    FilterChip(
                        selected = !isPostingJob,
                        onClick = { isPostingJob = false },
                        label = { Text(stringResource(R.string.offer_skills)) },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                }
            } else {
                // If the user has a specific role, fix the posting mode to their capabilities
                isPostingJob = viewModel.currentUserType == "employer"
                Text(
                    text = if (isPostingJob) stringResource(R.string.post_a_job) else stringResource(R.string.offer_skills),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        if (isPostingJob) {
            PostJobForm(viewModel, { showSignUpDialog = true }) { createdId ->
                proposedBoostId = createdId
            }
        } else {
            PostWorkerForm(viewModel, { showSignUpDialog = true }) { createdId ->
                proposedBoostId = createdId
            }
        }
    }
}

@Composable
fun PostJobForm(viewModel: TalentViewModel, onSignUpRequired: () -> Unit, onPostSuccess: (String) -> Unit) {
    val localContext = androidx.compose.ui.platform.LocalContext.current
    var title by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(listOf<String>()) }
    var country by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Full Time") }

    val categories = com.example.ui.ConfigData.getActiveCategoriesEn()
    var showSingleSelectDialog by remember { mutableStateOf(false) }

    val countries = com.example.ui.ConfigData.gulfCountriesWithCities.keys.toList()
    var countryExpanded by remember { mutableStateOf(false) }

    val jobTypes = stringArrayResource(R.array.job_types).toList()
    var typeExpanded by remember { mutableStateOf(false) }

    var expiryDuration by remember { mutableStateOf("1 Month") }
    var expiryExpanded by remember { mutableStateOf(false) }
    val expiryOptions = listOf("None", "1 Week", "2 Weeks", "1 Month", "3 Months")

    var budget by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.job_title)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                var showMultiSelectDialog by remember { mutableStateOf(false) }
                
                OutlinedTextField(
                    value = selectedCategories.joinToString(", "),
                    onValueChange = { },
                    label = { Text(stringResource(R.string.category)) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showMultiSelectDialog = true },
                    trailingIcon = {
                        IconButton(onClick = { showMultiSelectDialog = true }) {
                            Text("▼")
                        }
                    }
                )
                
                if (showMultiSelectDialog) {
                    AlertDialog(
                        onDismissRequest = { showMultiSelectDialog = false },
                        title = { Text("Select Categories (Max 3)") },
                        text = {
                            com.example.ui.components.CategoryMultiSelectGrid(
                                selectedCategories = selectedCategories,
                                onCategoriesChanged = { selectedCategories = it }
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = { showMultiSelectDialog = false }) {
                                Text("Done")
                            }
                        }
                    )
                }
                
                androidx.compose.foundation.layout.Box(modifier = Modifier.matchParentSize().clickable(onClick = { showMultiSelectDialog = true }), content = {} )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = type,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.type)) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { typeExpanded = !typeExpanded }) {
                            Text("▼")
                        }
                    }
                )
                DropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    jobTypes.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                type = selection
                                typeExpanded = false
                            }
                        )
                    }
                }
                androidx.compose.foundation.layout.Box(modifier = Modifier.matchParentSize().clickable(onClick = { typeExpanded = true }), content = {} )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = country,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.country)) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { countryExpanded = !countryExpanded }) {
                            Text("▼")
                        }
                    }
                )
                DropdownMenu(
                    expanded = countryExpanded,
                    onDismissRequest = { countryExpanded = false }
                ) {
                    countries.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                country = selection
                                countryExpanded = false
                            }
                        )
                    }
                }
                androidx.compose.foundation.layout.Box(modifier = Modifier.matchParentSize().clickable(onClick = { countryExpanded = true }), content = {} )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f)) {
                val cities = com.example.ui.ConfigData.gulfCountriesWithCities[country] ?: emptyList()
                var locationExpanded by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = location,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.location)) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { locationExpanded = !locationExpanded }) {
                            Text("▼")
                        }
                    }
                )
                DropdownMenu(
                    expanded = locationExpanded,
                    onDismissRequest = { locationExpanded = false }
                ) {
                    cities.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                location = selection
                                locationExpanded = false
                            }
                        )
                    }
                }
                androidx.compose.foundation.layout.Box(modifier = Modifier.matchParentSize().clickable(onClick = { if (country.isNotEmpty()) locationExpanded = true }), content = {} )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = contact,
            onValueChange = { contact = it },
            label = { Text(stringResource(R.string.whatsapp_contact)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = expiryDuration,
                onValueChange = { },
                label = { Text("Expiry Duration") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { expiryExpanded = !expiryExpanded }) {
                        Text("▼")
                    }
                }
            )
            DropdownMenu(
                expanded = expiryExpanded,
                onDismissRequest = { expiryExpanded = false }
            ) {
                expiryOptions.forEach { selection ->
                    DropdownMenuItem(
                        text = { Text(selection) },
                        onClick = {
                            expiryDuration = selection
                            expiryExpanded = false
                        }
                    )
                }
            }
            androidx.compose.foundation.layout.Box(modifier = Modifier.matchParentSize().clickable(onClick = { expiryExpanded = true }), content = {} )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text(stringResource(R.string.budget)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text(stringResource(R.string.deadline)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.job_desc)) },
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (viewModel.isGuest) {
                    onSignUpRequired()
                } else if (title.isNotEmpty() && selectedCategories.isNotEmpty() && country.isNotEmpty() && location.isNotEmpty()) {
                    val expTime = System.currentTimeMillis() + when (expiryDuration) {
                        "1 Week" -> 7L * 24 * 60 * 60 * 1000
                        "2 Weeks" -> 14L * 24 * 60 * 60 * 1000
                        "1 Month" -> 30L * 24 * 60 * 60 * 1000
                        "3 Months" -> 90L * 24 * 60 * 60 * 1000
                        else -> 0L
                    }

                    val doPublish = { imageUrl: String ->
                        viewModel.addJob(
                            title = title,
                            categories = selectedCategories,
                            country = country,
                            location = location,
                            description = description,
                            contact = contact,
                            type = type,
                            budget = budget,
                            deadline = deadline,
                            expiryDate = if (expiryDuration == "None") 0L else expTime,
                            imageUrl = imageUrl,
                            onSuccess = { createdId ->
                                onPostSuccess(createdId)
                            }
                        )
                        title = ""; selectedCategories = emptyList(); country = ""; location = ""; description = ""; contact = ""; budget = ""; deadline = ""
                    }

                    doPublish("")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.publish_job))
        }
        Spacer(modifier = Modifier.height(64.dp)) // Nav bar padding
    }
}

@Composable
fun PostWorkerForm(viewModel: TalentViewModel, onSignUpRequired: () -> Unit, onPostSuccess: (String) -> Unit) {
    val localContext = androidx.compose.ui.platform.LocalContext.current
    val currentUserProfile by viewModel.currentUserProfile.collectAsState(null)
    val firebaseUser = remember { try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null } }
    
    var name by remember { mutableStateOf(com.example.ui.UserService.getDisplayName(currentUserProfile, firebaseUser).let { if (it == "User") "" else it }) }
    var profession by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("Available Now") }
    
    // Extended & Portfolio
    var experience by remember { mutableStateOf("") }
    var resumeLink by remember { mutableStateOf("") }
    var portfolioWebsite by remember { mutableStateOf("") }
    var portfolioGithub by remember { mutableStateOf("") }
    var portfolioBehance by remember { mutableStateOf("") }
    var portfolioDribbble by remember { mutableStateOf("") }
    var portfolioYoutube by remember { mutableStateOf("") }

    val categories = com.example.ui.ConfigData.getActiveCategoriesEn()
        var showMultiSelectDialog by remember { mutableStateOf(false) }

    val countries = com.example.ui.ConfigData.gulfCountriesWithCities.keys.toList()
    var countryExpanded by remember { mutableStateOf(false) }

    val availabilities = stringArrayResource(R.array.availabilities).toList()
    var availabilityExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.full_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = profession,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.profession)) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showMultiSelectDialog = true },
                    trailingIcon = {
                        IconButton(onClick = { showMultiSelectDialog = true }) {
                            Text("▼")
                        }
                    }
                )
                
                if (showMultiSelectDialog) {
                    var query by remember { mutableStateOf("") }
                    val currentLang = com.example.LocalCurrentLanguage.current
                    var selectedSet by remember { mutableStateOf(profession.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()) }
                    
                    AlertDialog(
                        onDismissRequest = { showMultiSelectDialog = false },
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
                                
                                val filteredCategories = com.example.ui.ConfigData.currentCategories.filter { cat ->
                                    val idStr = cat.id.toString()
                                    val isCategoryActive = com.example.ui.ConfigData.dynamicCategoriesList.find { it.id == idStr || it.nameEn == cat.nameEn }?.isActive != false
                                    isCategoryActive
                                }.filter {
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
                                showMultiSelectDialog = false
                            }) {
                                Text(if (currentLang == "ar") "موافق" else "Done")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showMultiSelectDialog = false }) {
                                Text(if (currentLang == "ar") "إلغاء" else "Cancel")
                            }
                        }
                    )
                }
                
                androidx.compose.foundation.layout.Box(modifier = Modifier.matchParentSize().clickable(onClick = { showMultiSelectDialog = true }), content = {} )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = availability,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.availability)) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { availabilityExpanded = !availabilityExpanded }) {
                            Text("▼")
                        }
                    }
                )
                DropdownMenu(
                    expanded = availabilityExpanded,
                    onDismissRequest = { availabilityExpanded = false }
                ) {
                    availabilities.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                availability = selection
                                availabilityExpanded = false
                            }
                        )
                    }
                }
                androidx.compose.foundation.layout.Box(modifier = Modifier.matchParentSize().clickable(onClick = { availabilityExpanded = true }), content = {} )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = country,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.country)) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { countryExpanded = !countryExpanded }) {
                            Text("▼")
                        }
                    }
                )
                DropdownMenu(
                    expanded = countryExpanded,
                    onDismissRequest = { countryExpanded = false }
                ) {
                    countries.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                country = selection
                                countryExpanded = false
                            }
                        )
                    }
                }
                androidx.compose.foundation.layout.Box(modifier = Modifier.matchParentSize().clickable(onClick = { countryExpanded = true }), content = {} )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f)) {
                val cities = com.example.ui.ConfigData.gulfCountriesWithCities[country] ?: emptyList()
                var locationExpanded by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = location,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.location)) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { locationExpanded = !locationExpanded }) {
                            Text("▼")
                        }
                    }
                )
                DropdownMenu(
                    expanded = locationExpanded,
                    onDismissRequest = { locationExpanded = false }
                ) {
                    cities.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                location = selection
                                locationExpanded = false
                            }
                        )
                    }
                }
                androidx.compose.foundation.layout.Box(modifier = Modifier.matchParentSize().clickable(onClick = { if (country.isNotEmpty()) locationExpanded = true }), content = {} )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = contact,
            onValueChange = { contact = it },
            label = { Text(stringResource(R.string.whatsapp_contact)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = skills,
            onValueChange = { skills = it },
            label = { Text(stringResource(R.string.keywords_skills)) },
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Extended Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = experience,
            onValueChange = { experience = it },
            label = { Text("Experience (e.g. 5 Years)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = resumeLink,
            onValueChange = { resumeLink = it },
            label = { Text("Resume Link") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Portfolio (Optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = portfolioWebsite,
            onValueChange = { portfolioWebsite = it },
            label = { Text("Website Link") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = portfolioGithub,
            onValueChange = { portfolioGithub = it },
            label = { Text("GitHub Link") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = portfolioBehance,
            onValueChange = { portfolioBehance = it },
            label = { Text("Behance Link") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = portfolioDribbble,
            onValueChange = { portfolioDribbble = it },
            label = { Text("Dribbble Link") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = portfolioYoutube,
            onValueChange = { portfolioYoutube = it },
            label = { Text("YouTube Link") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (viewModel.isGuest) {
                    onSignUpRequired()
                } else if (name.isNotEmpty() && profession.isNotEmpty() && country.isNotEmpty() && location.isNotEmpty()) {
                    val doPublish = { photoUrl: String ->
                        viewModel.addWorker(
                            name, profession, country, location, skills, contact, availability,
                            experience, resumeLink, photoUrl, "", portfolioWebsite, portfolioGithub, portfolioBehance, portfolioDribbble, portfolioYoutube,
                            onSuccess = { createdId ->
                                onPostSuccess(createdId)
                            }
                        )
                        name = ""; profession = ""; country = ""; location = ""; skills = ""; contact = ""
                        experience = ""; resumeLink = ""; portfolioWebsite = ""; portfolioGithub = ""
                        portfolioBehance = ""; portfolioDribbble = ""; portfolioYoutube = ""
                    }

                    doPublish("")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.publish_profile))
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}


