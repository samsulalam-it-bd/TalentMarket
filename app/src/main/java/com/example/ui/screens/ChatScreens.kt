package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ui.ChatViewModel
import com.example.ui.TalentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(chatViewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(), navController: NavController) {
    val chatRooms by chatViewModel.chatRooms.collectAsStateWithLifecycle()
    val currentUserId = chatViewModel.currentUserId

    LaunchedEffect(Unit) {
        chatViewModel.loadChatRooms()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (chatRooms.isEmpty()) {
            EmptyStateView(
                emoji = "💬",
                title = "No Messages Yet",
                subtitle = "When you start a chat with employers or workers, they will appear here.",
                actionLabel = "Find Talent",
                onAction = { navController.navigate(com.example.ui.Screen.Workers.route) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatRooms) { room ->
                    val otherUserId = (room.participants ?: emptyList()).find { it != currentUserId } ?: ""
                    val otherUserName = (room.participantNames ?: emptyMap())[otherUserId] ?: "User"
                    val otherUserPhoto = (room.participantPhotos ?: emptyMap())[otherUserId] ?: ""

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(com.example.ui.Screen.ChatDetail.createRoute(room.id, otherUserId, otherUserName))
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (otherUserPhoto.isNotEmpty()) {
                                AsyncImage(
                                    model = otherUserPhoto,
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.size(50.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(otherUserName, fontWeight = FontWeight.Bold)
                                    if (room.lastMessageAt > 0) {
                                        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                                        Text(sdf.format(Date(room.lastMessageAt)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val unreadCount = ((room.unreadCounts ?: emptyMap())[currentUserId] ?: 0L).toInt()
                                    val isUnread = unreadCount > 0
                                    Text(
                                        text = room.lastMessage.ifEmpty { "Say hi!" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (unreadCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = unreadCount.toString(),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    roomId: String,
    fallbackOtherUserId: String = "",
    fallbackOtherUserName: String = "",
    chatViewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    navController: NavController
) {
    val messages by chatViewModel.currentMessages.collectAsStateWithLifecycle()
    val chatRooms by chatViewModel.chatRooms.collectAsStateWithLifecycle()
    val currentUserId = chatViewModel.currentUserId
    val room = chatRooms.find { it.id == roomId }
    val otherUserId = (room?.participants ?: emptyList()).find { it != currentUserId } ?: fallbackOtherUserId.ifEmpty { "" }
    val otherUserName = (room?.participantNames ?: emptyMap())[otherUserId] ?: fallbackOtherUserName.ifEmpty { "Chat" }

    var currentText by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(roomId) {
        chatViewModel.loadMessages(roomId)
        chatViewModel.markMessagesAsRead(roomId)
    }
    
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
            chatViewModel.markMessagesAsRead(roomId)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.leaveChatRoom()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(otherUserName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WindowInsets.ime.asPaddingValues())
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentText,
                        onValueChange = { currentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (currentText.isNotBlank()) {
                                chatViewModel.sendMessage(roomId, currentText.trim(), otherUserId)
                                currentText = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == currentUserId
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .wrapContentWidth(if (isMe) Alignment.End else Alignment.Start)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 0.dp,
                                    bottomEnd = if (isMe) 0.dp else 16.dp
                                )
                            )
                            .background(
                                if (isMe) {
                                    if (msg.isPending) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    else MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.text,
                                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                Text(
                                    text = sdf.format(Date(msg.timestamp)),
                                    color = (if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp
                                )
                                if (isMe) {
                                    val icon = when {
                                        msg.isPending -> Icons.Default.Done
                                        else -> Icons.Default.DoneAll
                                    }
                                    val iconColor = when {
                                        msg.isPending -> (if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.3f)
                                        msg.isRead -> androidx.compose.ui.graphics.Color(0xFF00E676) // Radiant green for read confirmation
                                        else -> (if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f) // Normal double-ticks for sent
                                    }
                                    val desc = when {
                                        msg.isPending -> "সার্ভারে পাঠানো হচ্ছে..."
                                        msg.isRead -> "পঠিত (আইকন পঠিত)"
                                        else -> "সার্ভারে পৌঁছেছে (সেন্ট)"
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = desc,
                                        tint = iconColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
