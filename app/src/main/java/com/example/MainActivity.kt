@file:OptIn(ExperimentalSharedTransitionApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.res.stringResource
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import android.content.res.Configuration
import java.util.Locale
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import com.example.data.TalentDatabase
import com.example.data.TalentRepository
import com.example.ui.Screen
import com.example.ui.TalentViewModel
import com.example.ui.TalentViewModelFactory
import com.example.ui.screens.JobsScreen
import com.example.ui.screens.PostScreen
import com.example.ui.screens.WorkersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.AdminViewModel
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminUsersScreen
import com.example.ui.screens.AdminBoostPlansScreen
import com.example.ui.screens.AdminNotificationsScreen
import com.example.ui.screens.AdminRevenueScreen
import com.example.ui.screens.AdminSettingsScreen
import com.example.ui.screens.AdminLogsScreen
import com.example.ui.screens.AdminJobsScreen
import com.example.ui.screens.AdminReportsScreen
import com.example.ui.screens.AdminWorkerScreen
import com.example.ui.screens.AdminCategoriesScreen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope

val LocalLanguageChange = staticCompositionLocalOf<((String) -> Unit)?> { null }
val LocalCurrentLanguage = staticCompositionLocalOf<String> { "en" }

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

class MainActivity : ComponentActivity() {

    private val db by lazy { TalentDatabase.getDatabase(this) }
    private val repository by lazy { TalentRepository(db) }
    private val viewModel: TalentViewModel by viewModels { TalentViewModelFactory(repository) }
    private val adminViewModel: AdminViewModel by viewModels()

    override fun attachBaseContext(newBase: android.content.Context) {
        val language = com.example.utils.LanguageManager.getSavedLanguage(newBase)
        val context = com.example.utils.LanguageManager.setLocale(newBase, language)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        com.example.ui.AdMediationManager.initialize(this)
        
        enableEdgeToEdge()
        com.example.ui.TalentViewModel.appContext = applicationContext
        com.example.ui.FirestoreErrorHandler.appContext = applicationContext

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
            }
        }

        // Exact Alarm permission check for Android 12+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            try {
                val alarmManager = getSystemService(android.content.Context.ALARM_SERVICE) as? android.app.AlarmManager
                if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                    val intent = android.content.Intent().apply {
                        action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        data = android.net.Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Initialize UI 
        setContent {
            var languageCode by remember { mutableStateOf(com.example.utils.LanguageManager.getSavedLanguage(this@MainActivity)) }

            CompositionLocalProvider(
                LocalLanguageChange provides { newLang ->
                    languageCode = newLang
                    com.example.utils.LanguageManager.saveLanguage(this@MainActivity, newLang)
                    val currentLocale = Locale.Builder().setLanguage(newLang).build()
                    Locale.setDefault(currentLocale)
                    val configuration = Configuration(resources.configuration)
                    configuration.setLocale(currentLocale)
                    @Suppress("DEPRECATION")
                    resources.updateConfiguration(configuration, resources.displayMetrics)
                    this@MainActivity.recreate()
                },
                LocalCurrentLanguage provides languageCode,
                LocalLayoutDirection provides if (languageCode == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                MyApplicationTheme {
                    MainAppScreen(viewModel, adminViewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        try {
            setIntent(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        com.example.utils.ChatConnectionManager.cleanUp()
    }
}

@Composable
fun MainAppScreen(viewModel: TalentViewModel, adminViewModel: AdminViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route?.let { r ->
        val baseR = r.substringBefore("?").substringBefore("/")
        baseR in listOf(Screen.Jobs.route, Screen.Workers.route, Screen.Post.route, Screen.ChatList.route, Screen.Profile.route)
    } ?: false

    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val auth = remember { try { FirebaseAuth.getInstance() } catch (e: Exception) { null } }
    val firebaseUser = auth?.currentUser
    
    val currentContext = LocalContext.current

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(firebaseUser) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = androidx.core.content.ContextCompat.checkSelfPermission(
                currentContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Fetch FCM token initially and save to Firestore if user logged in
        try {
            if (firebaseUser != null) {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("all_users")
                com.onesignal.OneSignal.login(firebaseUser.uid)
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            try {
                                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    .collection("users").document(firebaseUser.uid)
                                    .update("fcmToken", token)
                                    .addOnFailureListener { /* handle or ignore */ }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
            } else {
                com.onesignal.OneSignal.logout()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.newNotificationEvent.collect { notif ->
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                    currentContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    val channelId = "jobs_fcm_channel"
                    val notificationManager = currentContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val channel = android.app.NotificationChannel(
                            channelId,
                            "FCM Notifications",
                            android.app.NotificationManager.IMPORTANCE_HIGH
                        )
                        notificationManager.createNotificationChannel(channel)
                    }

                    val intent = android.content.Intent(currentContext, MainActivity::class.java).apply {
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent = android.app.PendingIntent.getActivity(
                        currentContext, 0, intent,
                        android.app.PendingIntent.FLAG_IMMUTABLE
                    )

                    val builder = androidx.core.app.NotificationCompat.Builder(currentContext, channelId)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(notif.title)
                        .setContentText(notif.body)
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)

                    notificationManager.notify(notif.id.hashCode(), builder.build())
                } else {
                    android.widget.Toast.makeText(currentContext, "${notif.title}\n${notif.body}", android.widget.Toast.LENGTH_LONG).show()
                }
        }
    }
    
    var showNamePromptSheet by remember { mutableStateOf(false) }
    var suggestedNameInput by remember { mutableStateOf("") }
    var isSavingPromptName by remember { mutableStateOf(false) }
    var hasPromptedThisSession by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserProfile, firebaseUser, hasPromptedThisSession) {
        if (firebaseUser != null && currentUserProfile != null && !hasPromptedThisSession) {
            if (com.example.ui.UserService.isNameUpdateRequired(currentUserProfile, firebaseUser)) {
                suggestedNameInput = com.example.ui.UserService.getSuggestedName(firebaseUser)
                hasPromptedThisSession = true
                showNamePromptSheet = true
            }
        }
    }

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    var activeIndexError by remember { mutableStateOf<com.example.ui.FirestoreErrorHandler.IndexErrorInfo?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(Unit) {
        com.example.ui.FirestoreErrorHandler.compositeIndexErrors.collect { errorInfo ->
            activeIndexError = errorInfo
        }
    }
    
    LaunchedEffect(viewModel) {
        viewModel.jobNotifications.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiMessage.collect { msg ->
            android.widget.Toast.makeText(currentContext, msg, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    var isInternetAvailable by remember { mutableStateOf(true) }
    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        
        fun updateConnectionState() {
            if (connectivityManager == null) {
                isInternetAvailable = true
                return
            }
            try {
                val activeNetwork = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                isInternetAvailable = capabilities != null && 
                        (capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) || 
                         capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                         capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET))
            } catch (e: Exception) {
                isInternetAvailable = true
            }
        }
        
        updateConnectionState()

        val callback = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                isInternetAvailable = true
            }
            override fun onLost(network: android.net.Network) {
                updateConnectionState()
            }
        }

        try {
            if (connectivityManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    connectivityManager.registerDefaultNetworkCallback(callback)
                } else {
                    val builder = android.net.NetworkRequest.Builder()
                        .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    connectivityManager.registerNetworkCallback(builder.build(), callback)
                }
            }
        } catch (e: Exception) {
            // fallback
        }

        onDispose {
            try {
                connectivityManager?.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    var isCheckingAuth by remember { mutableStateOf(true) }
    var currentUserType by remember { mutableStateOf<String?>(null) }
    var hasAuthInstance by remember { mutableStateOf(false) }
    var showMaintenanceScreen by remember { mutableStateOf(false) }
    var maintenanceMessageText by remember { mutableStateOf("App is under maintenance. Please try again later.") }

    LaunchedEffect(Unit) {
        try {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("settings").document("app_config")
                .addSnapshotListener { doc, err ->
                    if (err != null) {
                        com.example.ui.FirestoreErrorHandler.handleError(err, "MainActivity-AppConfig")
                    }
                    if (doc != null && doc.exists()) {
                        showMaintenanceScreen = doc.getBoolean("maintenanceMode") == true
                        doc.getString("maintenanceMessage")?.let { msg ->
                            maintenanceMessageText = msg
                        }
                    } else {
                        showMaintenanceScreen = false
                    }
                }
        } catch (e: Exception) {
            com.example.ui.FirestoreErrorHandler.handleError(e, "MainActivity-AppConfig")
        }
    }

    LaunchedEffect(Unit) {
        try {
            val auth = FirebaseAuth.getInstance()
            hasAuthInstance = true
            val user = auth.currentUser
            if (user != null) {
                val dbFetchJob = launch {
                    try {
                        FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                            .addOnSuccessListener { doc ->
                                viewModel.currentUserType = doc.getString("userType")
                                currentUserType = doc.getString("userType")
                                isCheckingAuth = false
                            }
                            .addOnFailureListener {
                                com.example.ui.FirestoreErrorHandler.handleError(it)
                                isCheckingAuth = false
                            }
                    } catch (e: Exception) {
                        isCheckingAuth = false
                    }
                }
                
                // Introduce safety timeout of 2.5 seconds to prevent app startup freeze
                kotlinx.coroutines.delay(2500)
                if (isCheckingAuth) {
                    dbFetchJob.cancel()
                    isCheckingAuth = false
                }
            } else {
                isCheckingAuth = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isCheckingAuth = false
        }
    }

    if (isCheckingAuth) {
        // Loading screen while checking auth
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
    }

    val appConfig by adminViewModel.appConfig.collectAsState()
    val isUserAdmin = firebaseUser?.email == com.example.ADMIN_EMAIL
    val maintenanceMode = (appConfig["maintenanceMode"] as? Boolean ?: false) || showMaintenanceScreen
    val maintenanceMessage = (appConfig["maintenanceMessage"] as? String) ?: maintenanceMessageText

    if (maintenanceMode && !isUserAdmin) {
        com.example.ui.screens.MaintenanceScreen(message = maintenanceMessage)
        return
    }

    val navItems = mutableListOf<Screen>()
    if (viewModel.isGuest) {
        navItems.add(Screen.Jobs)
        navItems.add(Screen.Workers)
    } else {
        navItems.add(Screen.Jobs)
        navItems.add(Screen.Workers)
        navItems.add(Screen.Post)
        navItems.add(Screen.ChatList)
        navItems.add(Screen.Profile)
    }

    val startDest = remember(viewModel.currentUserType) {
        val authUser = try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
        if (authUser != null) {
            if (viewModel.currentUserType == "employer") {
                Screen.Workers.route
            } else {
                Screen.Jobs.route
            }
        } else {
            Screen.Login.route
        }
    }

    if (activeIndexError != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { activeIndexError = null },
            icon = {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Firestore Index Required",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "এই ফিচারটি সঠিকভাবে কাজ করতে একটি ফায়ারবেস কম্পোজিট ইনডেক্স প্রয়োজন। নিচের বাটনে ক্লিক করে সরাসরি ফায়ারবেস কনসোল থেকে ইনডেক্স তৈরি করতে পারেন অথবা লিংকটি কপি করতে পারেন।",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "URL:",
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            text = activeIndexError!!.url,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            maxLines = 4,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        val url = activeIndexError?.url ?: ""
                        if (url.isNotEmpty()) {
                            com.example.ui.FirestoreErrorHandler.openIndexUrl(context, url)
                        }
                    }
                ) {
                    Text("অনলাইন ইনডেক্স তৈরি করুন")
                }
            },
            dismissButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            val url = activeIndexError?.url ?: ""
                            if (url.isNotEmpty()) {
                                com.example.ui.FirestoreErrorHandler.copyToClipboard(context, url)
                            }
                        }
                    ) {
                        Text("লিংক কপি করুন")
                    }
                    androidx.compose.material3.TextButton(
                        onClick = { activeIndexError = null }
                    ) {
                        Text("বন্ধ করুন")
                    }
                }
            }
        )
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Column {
                if (!isInternetAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                                contentDescription = "No Internet Warning",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "No Internet Connection Available",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }
                }
                if (showBottomBar) {
                    NavigationBar(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        navItems.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (currentDestination?.hierarchy?.any { it.route?.substringBefore("?")?.substringBefore("/") == screen.route?.substringBefore("?")?.substringBefore("/") } == true)
                                            screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = stringResource(screen.titleResId)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(screen.titleResId),
                                        fontSize = 12.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        fontFamily = if (com.example.LocalCurrentLanguage.current == "ar") com.example.ui.theme.CairoFontFamily else com.example.ui.theme.PoppinsFontFamily
                                    )
                                },
                                selected = currentDestination?.hierarchy?.any { it.route?.substringBefore("?")?.substringBefore("/") == screen.route?.substringBefore("?")?.substringBefore("/") } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        SharedTransitionLayout {
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this
            ) {
                val isRtl = com.example.LocalCurrentLanguage.current == "ar"

                NavHost(
                    navController = navController,
                    startDestination = startDest,
                    modifier = Modifier.padding(innerPadding),
                    enterTransition = {
                        androidx.compose.animation.slideInHorizontally(
                            initialOffsetX = { fullWidth -> if (isRtl) fullWidth else -fullWidth },
                            animationSpec = androidx.compose.animation.core.tween(300)
                        )
                    },
                    exitTransition = {
                        androidx.compose.animation.slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if (isRtl) -fullWidth else fullWidth },
                            animationSpec = androidx.compose.animation.core.tween(300)
                        )
                    },
                    popEnterTransition = {
                        androidx.compose.animation.slideInHorizontally(
                            initialOffsetX = { fullWidth -> if (isRtl) -fullWidth else fullWidth },
                            animationSpec = androidx.compose.animation.core.tween(300)
                        )
                    },
                    popExitTransition = {
                        androidx.compose.animation.slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if (isRtl) fullWidth else -fullWidth },
                            animationSpec = androidx.compose.animation.core.tween(300)
                        )
                    }
                ) {
                    composable(Screen.Search.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { com.example.ui.screens.SearchScreen(viewModel, navController) }
                    }
                    composable(Screen.Login.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { com.example.ui.screens.LoginScreen(navController, viewModel) }
                    }
                    composable(
                        route = Screen.Jobs.route + "?category={category}"
                    ) { backStackEntry ->
                        val category = backStackEntry.arguments?.getString("category")
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { 
                            JobsScreen(viewModel, navController, initialCategory = category) 
                        }
                    }
                    composable(
                        route = Screen.Workers.route + "?workerId={workerId}"
                    ) { backStackEntry ->
                        val workerId = backStackEntry.arguments?.getString("workerId")
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { 
                            WorkersScreen(viewModel, navController, initialWorkerId = workerId) 
                        }
                    }
                    composable(Screen.Post.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { PostScreen(viewModel, navController) }
                    }
                    composable(Screen.Profile.route) { 
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { com.example.ui.screens.ProfileScreen(viewModel, navController) }
                    }
                    composable(
                        route = Screen.JobDetails.route
                    ) { backStackEntry ->
                        val jobId = backStackEntry.arguments?.getString("jobId")
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { 
                            com.example.ui.screens.JobDetailsScreen(jobId, viewModel, navController)
                        }
                    }
                    composable(Screen.MyApplications.route) {
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            com.example.ui.screens.MyApplicationsScreen(
                                talentViewModel = viewModel,
                                navController = navController,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    composable(
                        route = Screen.JobApplicants.route,
                        arguments = listOf(
                            androidx.navigation.navArgument("jobId") { type = androidx.navigation.NavType.StringType },
                            androidx.navigation.navArgument("jobTitle") { type = androidx.navigation.NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                        val jobTitle = backStackEntry.arguments?.getString("jobTitle") ?: ""
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            com.example.ui.screens.JobApplicantsScreen(
                                jobId = jobId,
                                jobTitle = jobTitle,
                                talentViewModel = viewModel,
                                navController = navController,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    composable(Screen.Notifications.route) {
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) { 
                            com.example.ui.screens.NotificationsScreen(viewModel, navController)
                        }
                    }
                    composable(Screen.CompanyProfile.route) { backStackEntry ->
                        val companyId = backStackEntry.arguments?.getString("companyId")
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            com.example.ui.screens.CompanyProfileScreen(companyId, viewModel, navController)
                        }
                    }
                    composable(Screen.Support.route) {
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            com.example.ui.screens.SupportScreen(viewModel, navController)
                        }
                    }
                    composable(Screen.SupportAdmin.route) {
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            com.example.ui.screens.SupportAdminScreen(adminViewModel, navController)
                        }
                    }
                    composable(Screen.AdminDashboard.route) { AdminDashboardScreen(adminViewModel) { route -> navController.navigate(route) } }
                    composable(Screen.AdminUsers.route) { AdminUsersScreen(adminViewModel) { navController.popBackStack() } }
                    composable(Screen.AdminJobs.route) { AdminJobsScreen(adminViewModel) { navController.popBackStack() } }
                    composable(Screen.AdminWorkers.route) { AdminWorkerScreen(adminViewModel) { navController.popBackStack() } }
                    composable(Screen.AdminBoostPlans.route) { AdminBoostPlansScreen(adminViewModel) { navController.popBackStack() } }
                    composable(Screen.AdminCategories.route) { AdminCategoriesScreen(adminViewModel) { navController.popBackStack() } }
                    composable(Screen.AdminNotifications.route) { AdminNotificationsScreen(adminViewModel) { navController.popBackStack() } }
                    composable(Screen.AdminRevenue.route) { AdminRevenueScreen(adminViewModel) { navController.popBackStack() } }
                    composable(Screen.AdminSettings.route) { AdminSettingsScreen(adminViewModel) { navController.popBackStack() } }
                    composable(Screen.AdminLogs.route) { AdminLogsScreen(adminViewModel) { navController.popBackStack() } }
                    composable(Screen.AdminReports.route) { AdminReportsScreen(adminViewModel) { navController.popBackStack() } }
                    composable(Screen.AdminCampaigns.route) { 
                        com.example.ui.screens.AdminCampaignsScreen(adminViewModel) { navController.popBackStack() }
                    }
                    composable(Screen.Premium.route) {
                        val coroutineScope = rememberCoroutineScope()
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            com.example.ui.screens.PremiumScreen(
                                currentPlan = viewModel.subscriptionViewModel.state.collectAsState().value.plan,
                                onSubscribe = { planId ->
                                    val billingManager = com.example.billing.BillingManager(context)
                                    billingManager.startConnection {
                                        coroutineScope.launch {
                                            val products = billingManager.queryProducts(
                                                listOf(planId)
                                            )
                                            products.firstOrNull()?.let { productDetails ->
                                                val activity = findActivity(context)
                                                if (activity != null) {
                                                    billingManager.launchBillingFlow(
                                                        activity,
                                                        productDetails
                                                    )
                                                } else {
                                                    android.widget.Toast.makeText(context, "Billing error: Activity context not found", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    composable("boost/{jobId}") { backStackEntry ->
                        val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                        val coroutineScope = rememberCoroutineScope()
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            com.example.ui.screens.BoostScreen(
                                viewModel = viewModel,
                                jobId = jobId,
                                onBoost = { boostPlan ->
                                    try {
                                        val billingManager = com.example.billing.BillingManager(context)
                                        billingManager.startConnection {
                                            coroutineScope.launch {
                                                try {
                                                    val products = billingManager.queryProducts(
                                                        listOf(boostPlan.productId)
                                                    )
                                                    val productDetails = products.firstOrNull()
                                                    if (productDetails != null) {
                                                        val activity = findActivity(context)
                                                        if (activity != null) {
                                                            billingManager.launchBillingFlow(
                                                                activity,
                                                                productDetails
                                                             )
                                                        } else {
                                                            android.widget.Toast.makeText(context, "Billing error: Activity context not found", android.widget.Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else {
                                                        // Fallback: Mock success if Billing is not configured
                                                        val durationDays = boostPlan.durationDays
                                                        val computedBoostEndDate = System.currentTimeMillis() + (durationDays.toLong() * 24L * 60L * 60L * 1000L)
                                                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                            .collection("jobs").document(jobId)
                                                            .update(mapOf(
                                                                "isBoosted" to true,
                                                                "boostEndDate" to computedBoostEndDate,
                                                                "isApproved" to true
                                                            ))
                                                            .addOnSuccessListener {
                                                                android.widget.Toast.makeText(context, "Boost simulated (Duration: ${boostPlan.duration}).", android.widget.Toast.LENGTH_LONG).show()
                                                                navController.popBackStack()
                                                            }
                                                            .addOnFailureListener {
                                                                // What if it's a worker?
                                                                try {
                                                                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                                        .collection("workers").document(jobId)
                                                                        .update(mapOf(
                                                                            "isBoosted" to true,
                                                                            "boostEndDate" to computedBoostEndDate,
                                                                            "isApproved" to true
                                                                        ))
                                                                        .addOnSuccessListener {
                                                                            android.widget.Toast.makeText(context, "Profile Boost simulated (Duration: ${boostPlan.duration}).", android.widget.Toast.LENGTH_LONG).show()
                                                                            navController.popBackStack()
                                                                        }
                                                                } catch (ex: Exception) {
                                                                    ex.printStackTrace()
                                                                }
                                                            }
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    composable(
                        "edit_job/{jobId}",
                        arguments = listOf(androidx.navigation.navArgument("jobId") { 
                            type = androidx.navigation.NavType.StringType 
                        })
                    ) { backStackEntry ->
                        val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
                        com.example.ui.screens.EditJobScreen(
                            jobId = jobId,
                            talentViewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.ChatList.route) {
                        com.example.ui.screens.ChatListScreen(navController = navController)
                    }
                    composable(
                        route = Screen.ChatDetail.route,
                        arguments = listOf(
                            androidx.navigation.navArgument("roomId") { type = androidx.navigation.NavType.StringType },
                            androidx.navigation.navArgument("otherUserId") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                            androidx.navigation.navArgument("otherUserName") { type = androidx.navigation.NavType.StringType; defaultValue = "" }
                        )
                    ) { backStackEntry ->
                        val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                        val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
                        val otherUserName = backStackEntry.arguments?.getString("otherUserName") ?: ""
                        com.example.ui.screens.ChatDetailScreen(roomId, otherUserId, otherUserName, navController = navController)
                    }
                    composable(Screen.SavedJobs.route) {
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            com.example.ui.screens.SavedJobsScreen(viewModel, navController)
                        }
                    }
                    composable(Screen.AppliedJobs.route) {
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            com.example.ui.screens.AppliedJobsScreen(viewModel, navController)
                        }
                    }
                }
            }
        }
    }

    if (showNamePromptSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showNamePromptSheet = false 
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = "What should we call you?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please enter your name to display on your profile.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = suggestedNameInput,
                    onValueChange = { suggestedNameInput = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        val trimmed = suggestedNameInput.trim()
                        if (trimmed.isNotEmpty() && !trimmed.contains("@")) {
                            isSavingPromptName = true
                            viewModel.updateProfileName(trimmed) { success ->
                                isSavingPromptName = false
                                if (success) {
                                    showNamePromptSheet = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = suggestedNameInput.trim().isNotEmpty() && !suggestedNameInput.trim().contains("@") && !isSavingPromptName
                ) {
                    if (isSavingPromptName) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun findActivity(context: android.content.Context): android.app.Activity? {
    var cur = context
    while (cur is android.content.ContextWrapper) {
        if (cur is android.app.Activity) return cur
        cur = cur.baseContext
    }
    return null
}

