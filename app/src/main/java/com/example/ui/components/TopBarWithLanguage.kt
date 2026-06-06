package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.LocalLanguageChange
import com.example.LocalCurrentLanguage
import com.example.ui.TalentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithLanguage(
    navController: NavController? = null,
    viewModel: TalentViewModel? = null
) {
    val languageChange = LocalLanguageChange.current
    val currentLang = LocalCurrentLanguage.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("T", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                stringResource(R.string.talentmarket),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (viewModel != null && navController != null) {
                val notifications by viewModel.notifications.collectAsStateWithLifecycle(emptyList())
                val unreadCount = notifications.count { !it.isRead }
                
                IconButton(
                    onClick = { navController.navigate("notifications") },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text(unreadCount.toString(), style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        languageChange?.invoke(if (currentLang == "en") "ar" else "en")
                    }
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Language,
                        contentDescription = "Change Language",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (currentLang == "en") "عربي" else "Eng",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
