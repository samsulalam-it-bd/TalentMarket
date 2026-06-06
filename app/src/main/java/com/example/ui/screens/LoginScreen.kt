@file:Suppress("DEPRECATION")
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.Icons
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.navigation.NavController
import com.example.R
import com.example.ui.Screen
import com.example.ui.TalentViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: TalentViewModel) {
    var isLoginMode by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        if (viewModel.openInSignUpMode) {
            isLoginMode = false
            viewModel.openInSignUpMode = false
        }
    }
    var userType by remember { mutableStateOf("") } // "worker" or "employer"
    var showUserTypeDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var pendingGoogleUser by remember { mutableStateOf<com.google.firebase.auth.FirebaseUser?>(null) }
    var showGoogleOnboardingDialog by remember { mutableStateOf(false) }

    val createGoogleUserProfile = { user: com.google.firebase.auth.FirebaseUser, selectedUserType: String ->
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val randomChars = (1..4).map { (('A'..'Z') + ('0'..'9')).random() }.joinToString("")
        val uniqueId = "TM-$currentYear-$randomChars"
        val profile = hashMapOf(
            "uid" to user.uid,
            "email" to (user.email ?: ""),
            "userType" to selectedUserType,
            "name" to (user.displayName ?: ""),
            "isAdmin" to ((user.email ?: "") == com.example.ADMIN_EMAIL),
            "isBanned" to false,
            "isVerified" to false,
            "createdAt" to System.currentTimeMillis(),
            "photoUrl" to (user.photoUrl?.toString() ?: ""),
            "uniqueId" to uniqueId,
            "subscription" to mapOf(
                "plan" to "free",
                "isActive" to false,
                "startDate" to 0L,
                "endDate" to 0L
            )
        )
        db.collection("users").document(user.uid).set(profile)
            .addOnSuccessListener {
                viewModel.currentUserType = selectedUserType
                viewModel.isGuest = false
                if (selectedUserType == "worker") {
                    navController.navigate(Screen.Jobs.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                } else {
                    navController.navigate(Screen.Workers.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                }
            }
            .addOnFailureListener {
                com.example.ui.FirestoreErrorHandler.handleError(it, "Login/Profile Creation")
                android.widget.Toast.makeText(context, "Error saving user profile: ${it.message}", android.widget.Toast.LENGTH_LONG).show()
            }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            val user = authTask.result.user
                            if (user != null) {
                                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                db.collection("users").document(user.uid).get()
                                    .addOnSuccessListener { doc ->
                                        if (doc.exists() && doc.getString("userType") != null) {
                                            val currentFirestoreName = doc.getString("name")
                                            val googleName = user.displayName
                                            if ((currentFirestoreName == null || currentFirestoreName.trim().isEmpty()) && googleName != null && googleName.trim().isNotEmpty()) {
                                                db.collection("users").document(user.uid).update("name", googleName)
                                            }
                                            
                                            viewModel.currentUserType = doc.getString("userType")
                                            viewModel.isGuest = false
                                            if (viewModel.currentUserType == "worker") {
                                                navController.navigate(Screen.Jobs.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                                            } else {
                                                navController.navigate(Screen.Workers.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                                            }
                                        } else {
                                            if (userType.isNotEmpty()) {
                                                createGoogleUserProfile(user, userType)
                                            } else {
                                                pendingGoogleUser = user
                                                showGoogleOnboardingDialog = true
                                            }
                                        }
                                    }
                                    .addOnFailureListener {
                                        com.example.ui.FirestoreErrorHandler.handleError(it, "Login/Profile Fetch")
                                        android.widget.Toast.makeText(context, "Error fetching user profile: ${it.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                            }
                        } else {
                            android.widget.Toast.makeText(context, "Firebase auth failed: ${authTask.exception?.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                android.widget.Toast.makeText(context, "Sign-In failed, no ID token. Check Firebase SHA-1.", android.widget.Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Google Sign-In failed. Check Firebase SHA-1 configuration.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    if (showUserTypeDialog) {
        AlertDialog(
            onDismissRequest = { showUserTypeDialog = false },
            title = { Text("Select Account Type") },
            text = { Text("Are you registering as a Worker or Employer?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userType = "employer"
                        showUserTypeDialog = false
                        val clientIdResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                        if (clientIdResId != 0) {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(context.getString(clientIdResId))
                                .requestEmail()
                                .build()
                            val client = GoogleSignIn.getClient(context, gso)
                            googleSignInLauncher.launch(client.signInIntent)
                        } else {
                            android.widget.Toast.makeText(context, "Google Sign-In needs an OAuth client ID in Firebase. Enable Google Sign In provider in Firebase console and re-download google-services.json.", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text("Employer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        userType = "worker"
                        showUserTypeDialog = false
                        val clientIdResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                        if (clientIdResId != 0) {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(context.getString(clientIdResId))
                                .requestEmail()
                                .build()
                            val client = GoogleSignIn.getClient(context, gso)
                            googleSignInLauncher.launch(client.signInIntent)
                        } else {
                            android.widget.Toast.makeText(context, "Google Sign-In needs an OAuth client ID in Firebase. Enable Google Sign In provider in Firebase console and re-download google-services.json.", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text("Worker")
                }
            }
        )
    }

    if (showGoogleOnboardingDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleOnboardingDialog = false },
            title = { Text("Complete Your Profile") },
            text = { Text("To complete registration, please select if you are a Worker or an Employer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val user = pendingGoogleUser
                        if (user != null) {
                            showGoogleOnboardingDialog = false
                            createGoogleUserProfile(user, "employer")
                        }
                    }
                ) {
                    Text("Employer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val user = pendingGoogleUser
                        if (user != null) {
                            showGoogleOnboardingDialog = false
                            createGoogleUserProfile(user, "worker")
                        }
                    }
                ) {
                    Text("Worker")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            com.example.ui.components.TopBarWithLanguage(navController = null, viewModel = null)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        // App Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text("T", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isLoginMode) stringResource(R.string.welcome_to_talentmarket) else "Create an Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = if (isLoginMode) "Login to continue" else "Register to get started",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        if (!isLoginMode) {
            // User Type Selection
            Text(
                text = stringResource(R.string.i_am_a),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start)
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TypeCard(
                    title = stringResource(R.string.job_seeker),
                    description = stringResource(R.string.looking_for_job),
                    icon = "👤",
                    selected = userType == "worker",
                    onClick = { userType = "worker" },
                    modifier = Modifier.weight(1f)
                )
                TypeCard(
                    title = stringResource(R.string.employer),
                    description = stringResource(R.string.looking_for_workers),
                    icon = "🏢",
                    selected = userType == "employer",
                    onClick = { userType = "employer" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        var email by remember { mutableStateOf("") }

        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var name by remember { mutableStateOf("") }

        if (!isLoginMode) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.authError != null) {
            Text(
                text = viewModel.authError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        val buttonEnabled = if (isLoginMode) {
            email.isNotEmpty() && password.isNotEmpty() && !viewModel.isLoading
        } else {
            userType.isNotEmpty() && name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && !viewModel.isLoading
        }

        Button(
            onClick = {
                if (isLoginMode) {
                    viewModel.loginUser(email, password, "worker") { success -> // Fallback userType
                        if (success) {
                            viewModel.isGuest = false
                            if (viewModel.currentUserType == "worker") {
                                navController.navigate(Screen.Jobs.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.Workers.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                    }
                } else {
                    viewModel.registerUser(email, password, userType, name) { success ->
                         if (success) {
                            viewModel.isGuest = false
                            if (userType == "worker") {
                                navController.navigate(Screen.Jobs.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.Workers.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                    }
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = buttonEnabled
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(if (isLoginMode) stringResource(R.string.login) else "Register", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text("OR", modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                val clientIdResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                if (clientIdResId == 0) {
                     android.widget.Toast.makeText(context, "Google Sign-In needs an OAuth client ID in Firebase. Enable Google Sign In provider in Firebase console and re-download google-services.json.", android.widget.Toast.LENGTH_LONG).show()
                } else {
                     if (!isLoginMode && userType.isEmpty()) {
                         showUserTypeDialog = true
                     } else {
                         val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                             .requestIdToken(context.getString(clientIdResId))
                             .requestEmail()
                             .build()
                         val client = GoogleSignIn.getClient(context, gso)
                         googleSignInLauncher.launch(client.signInIntent)
                     }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = true // removed userType.isNotEmpty() requirement here to handle it inside
        ) {
             Text("Continue with Google", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isLoginMode) "Don't have an account?" else "Already have an account?",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = { isLoginMode = !isLoginMode }
            ) {
                Text(if (isLoginMode) "Register" else "Login", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
        
        TextButton(
            onClick = {
                viewModel.isGuest = true
                navController.navigate(Screen.Jobs.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        ) {
            Text(stringResource(R.string.continue_as_guest), color = MaterialTheme.colorScheme.primary)
        }
    }
    }
}

@Composable
fun TypeCard(title: String, description: String, icon: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

