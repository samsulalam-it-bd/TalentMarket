package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AdminViewModel
import com.example.ui.components.AdminTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val appConfig by adminViewModel.appConfig.collectAsState()

    // Query on Start
    LaunchedEffect(Unit) {
        adminViewModel.loadAppConfig()
    }

    // Safeguard values
    val maintenanceMode = appConfig["maintenanceMode"] as? Boolean ?: false
    val forceUpdate = appConfig["forceUpdate"] as? Boolean ?: false
    val minAppVersion = appConfig["minAppVersion"] as? String ?: "1.0.0"
    val welcomeMessage = appConfig["welcomeMessage"] as? String ?: "Welcome to Talent Portal"

    Scaffold(
        topBar = {
            AdminTopBar(
                title = "⚙️ System Configuration",
                onBack = onBack
            )
        },
        containerColor = Color(0xFF0F0F1A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "System Toggles",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            // Maintenance Toggle card
            AdminSettingToggle(
                title = "Maintenance Mode",
                subtitle = "Take the app offline except for administrators. Users will see a full-page maintenance shield.",
                checked = maintenanceMode,
                isDanger = true,
                onCheckedChange = { enabled ->
                    adminViewModel.setMaintenanceMode(enabled)
                }
            )

            // Force Update Toggle card
            AdminSettingToggle(
                title = "Force App Update",
                subtitle = "Prompt all users with outdated app builds to immediately update via the Google Play Store.",
                checked = forceUpdate,
                onCheckedChange = { value ->
                    adminViewModel.updateAppConfig("forceUpdate", value)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Platform Configurations",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            // AdMob Settings
            val admobEnabled = appConfig["admobEnabled"] as? Boolean ?: false
            val bannerAdUnitId = appConfig["bannerAdUnitId"] as? String ?: "ca-app-pub-3940256099942544/6300978111"
            val interstitialAdUnitId = appConfig["interstitialAdUnitId"] as? String ?: "ca-app-pub-3940256099942544/1033173712"
            
            val feedAdFrequency = (appConfig["feedAdFrequency"] as? Number)?.toInt() ?: 5
            val showBannerOnDetails = appConfig["showBannerOnDetails"] as? Boolean ?: true
            val showAdOnApply = appConfig["showAdOnApply"] as? Boolean ?: true
            val showBannerOnFeed = appConfig["showBannerOnFeed"] as? Boolean ?: true
            
            AdminSettingToggle(
                title = "Enable AdMob Ads",
                subtitle = "Show Google Ads across the app",
                checked = admobEnabled,
                onCheckedChange = { value ->
                    adminViewModel.updateAppConfig("admobEnabled", value)
                }
            )

            if (admobEnabled) {
                AdminSettingToggle(
                    title = "Show Web Banner in Home & Feed",
                    subtitle = "Display banner ad units intermixed within scrolling feed",
                    checked = showBannerOnFeed,
                    onCheckedChange = { value ->
                        adminViewModel.updateAppConfig("showBannerOnFeed", value)
                    }
                )
                if (showBannerOnFeed) {
                    AdminSettingTextField(
                        title = "Feed Ad Frequency",
                        subtitle = "Show ad every N items in lists (e.g. 5)",
                        currentValue = feedAdFrequency.toString(),
                        onSave = { Number ->
                            adminViewModel.updateAppConfig("feedAdFrequency", Number.toIntOrNull() ?: 5)
                        }
                    )
                }
                AdminSettingToggle(
                    title = "Show Banner on Details Screen",
                    subtitle = "Always show a banner at the bottom of a job or worker profile",
                    checked = showBannerOnDetails,
                    onCheckedChange = { value ->
                        adminViewModel.updateAppConfig("showBannerOnDetails", value)
                    }
                )
                AdminSettingToggle(
                    title = "Show Interstitial Ad on Apply/Connect",
                    subtitle = "Display a full-page ad when someone tries to apply to a job or contact a worker",
                    checked = showAdOnApply,
                    onCheckedChange = { value ->
                        adminViewModel.updateAppConfig("showAdOnApply", value)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                AdminSettingTextField(
                    title = "Banner Ad Unit ID",
                    currentValue = bannerAdUnitId,
                    onSave = { unitId ->
                        adminViewModel.updateAppConfig("bannerAdUnitId", unitId.trim())
                    }
                )
                
                AdminSettingTextField(
                    title = "Interstitial Ad Unit ID",
                    currentValue = interstitialAdUnitId,
                    onSave = { unitId ->
                        adminViewModel.updateAppConfig("interstitialAdUnitId", unitId.trim())
                    }
                )
            }

            // Minimum App Version input
            AdminSettingTextField(
                title = "Minimum App Version Requirement",
                currentValue = minAppVersion,
                onSave = { version ->
                    adminViewModel.updateAppConfig("minAppVersion", version.trim())
                }
            )

            // Welcome announcement input
            AdminSettingTextField(
                title = "Platform Welcome Announcement Banner",
                currentValue = welcomeMessage,
                onSave = { msg ->
                    adminViewModel.updateAppConfig("welcomeMessage", msg.trim())
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Danger Zone Block
            Text(
                text = "⚠️ Danger Zone",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF281115)),
                border = BorderStroke(1.dp, Color(0xFF7F1D1D))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "System Maintenance Actions",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Clean up the database by scanning expired boost contracts and unpinning completed premium postings automatically.",
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    
                    var isClearing by remember { mutableStateOf(false) }
                    var clearSuccess by remember { mutableStateOf(false) }

                    Button(
                        onClick = {
                            isClearing = true
                            adminViewModel.clearExpiredBoostPosts()
                            isClearing = false
                            clearSuccess = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Rounded.CleaningServices, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Clear All Expired Boost Posts",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (clearSuccess) {
                        Text(
                            text = "✅ Job & Worker boosts cleaned up successfully!",
                            color = Color(0xFF10B981),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    isDanger: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDanger && checked) Color(0xFF331C20) else Color(0xFF1E1E30)
        ),
        border = BorderStroke(
            1.dp,
            if (isDanger && checked) Color(0xFFEF4444) else Color(0xFF374151)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isDanger && checked) Color(0xFFFCA5A5) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = if (isDanger) Color(0xFFEF4444) else Color(0xFF3B82F6),
                    checkedTrackColor = if (isDanger) Color(0xFFEF4444).copy(alpha = 0.3f) else Color(0xFF3B82F6).copy(alpha = 0.3f),
                    uncheckedThumbColor = Color(0xFF9CA3AF),
                    uncheckedTrackColor = Color(0xFF374151)
                )
            )
        }
    }
}

@Composable
fun AdminSettingTextField(
    title: String,
    subtitle: String? = null,
    currentValue: String,
    onSave: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editValue by remember(currentValue) { mutableStateOf(currentValue) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E30)),
        border = BorderStroke(1.dp, Color(0xFF374151))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                if (!isEditing) {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit $title",
                            tint = Color(0xFF3B82F6)
                        )
                    }
                }
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF374151)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            onSave(editValue)
                            isEditing = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { 
                            editValue = currentValue
                            isEditing = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (currentValue.isBlank()) "(Not Configured)" else currentValue,
                    color = if (currentValue.isBlank()) Color(0xFF6B7280) else Color(0xFFD1D5DB),
                    fontSize = 14.sp
                )
            }
        }
    }
}
