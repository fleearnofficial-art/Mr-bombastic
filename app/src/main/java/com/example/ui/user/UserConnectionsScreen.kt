package com.example.ui.user

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.data.repository.FleearnRepository
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.AppTheme
import com.example.util.MessageCrypt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserConnectionsScreen(user: UserProfile) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Observe streams from database
    val channels by FleearnRepository.channels.collectAsState()
    val users by FleearnRepository.users.collectAsState()
    val messages by FleearnRepository.messages.collectAsState()
    val metas by FleearnRepository.conversationMetas.collectAsState()
    val calls by FleearnRepository.callHistory.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf(0) } // 0: Chats, 1: Calls, 2: Channels

    // Conversation State
    var activeChatTarget by remember { mutableStateOf<UserProfile?>(null) }
    var activeChannelTarget by remember { mutableStateOf<OfficialChannel?>(null) }
    
    // Calling Overlay State
    var activeCallState by remember { mutableStateOf<CallRecord?>(null) }
    var isCallIncoming by remember { mutableStateOf(false) }

    val otherUsers = remember(users, user.uid) {
        val filtered = users.filter { it.uid != user.uid }
        if (filtered.isNotEmpty()) {
            filtered
        } else {
            listOf(
                UserProfile(
                    uid = "1098765432",
                    name = "Tanvir Ahmed",
                    mobile = "+8801712345678",
                    pinHash = "12345678",
                    role = UserRole.USER,
                    profilePhotoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                    age = 22
                ),
                UserProfile(
                    uid = "8839201948",
                    name = "Nexus Media Group",
                    mobile = "+8801812345678",
                    pinHash = "12345678",
                    role = UserRole.PUBLISHER,
                    profilePhotoUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200",
                    age = 28
                ),
                UserProfile(
                    uid = "MX10293847",
                    name = "Senior Moderator (FleUra)",
                    mobile = "+8801912345678",
                    pinHash = "12345678",
                    role = UserRole.MODERATOR,
                    profilePhotoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
                    age = 26
                )
            ).filter { it.uid != user.uid }
        }
    }
    
    // Map of last messages per user
    val conversations = remember(messages, otherUsers, metas) {
        otherUsers.map { partner ->
            val chatHistory = messages.filter {
                (it.senderUid == user.uid && it.receiverUid == partner.uid) ||
                (it.senderUid == partner.uid && it.receiverUid == user.uid)
            }.sortedBy { it.timestamp }
            
            val lastMsg = chatHistory.lastOrNull()
            val meta = metas.find { it.partnerId == partner.uid }
            
            PartnerConversation(
                partner = partner,
                lastMessage = lastMsg,
                meta = meta ?: ConversationMeta(userId = user.uid, partnerId = partner.uid)
            )
        }.filter { !it.meta.isArchived }
         .sortedWith(compareByDescending<PartnerConversation> { it.meta.isPinned }
         .thenByDescending { it.lastMessage?.timestamp ?: 0L })
    }

    val archivedConversations = remember(messages, otherUsers, metas) {
        otherUsers.map { partner ->
            val chatHistory = messages.filter {
                (it.senderUid == user.uid && it.receiverUid == partner.uid) ||
                (it.senderUid == partner.uid && it.receiverUid == user.uid)
            }.sortedBy { it.timestamp }
            
            val lastMsg = chatHistory.lastOrNull()
            val meta = metas.find { it.partnerId == partner.uid }
            PartnerConversation(
                partner = partner,
                lastMessage = lastMsg,
                meta = meta ?: ConversationMeta(userId = user.uid, partnerId = partner.uid)
            )
        }.filter { it.meta.isArchived }
         .sortedByDescending { it.lastMessage?.timestamp ?: 0L }
    }

    val filteredConversations = if (searchQuery.isBlank()) conversations else conversations.filter {
        it.partner.name.contains(searchQuery.trim(), ignoreCase = true) ||
        it.partner.uid.contains(searchQuery.trim())
    }

    // Horizontal online contacts list
    val onlineContacts = otherUsers.filter { it.age % 2 == 0 } // Simulated online users

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App bar
        TopAppBar(
            title = {
                Text(
                    text = "Secure Connect",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            ),
            actions = {
                IconButton(onClick = {
                    Toast.makeText(context, "E2E Encryption Verified 🔒 Keys Secured", Toast.LENGTH_LONG).show()
                }) {
                    Icon(Icons.Default.Lock, contentDescription = "E2E Status", tint = BrandGreen)
                }
            }
        )

        // Search input field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search messages, calls or channels...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Standard Tab switchers
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = BrandCyan,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = BrandCyan,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Inbox", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Calls", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("Channels", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Area based on Tab
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> {
                    // Chat Inbox Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        // Online users display horizontal row
                        if (onlineContacts.isNotEmpty() && searchQuery.isEmpty()) {
                            item {
                                Text(
                                    text = "Online Contacts",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandCyan,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(onlineContacts) { contact ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable {
                                                activeChatTarget = contact
                                            }
                                        ) {
                                            Box {
                                                AsyncImage(
                                                    model = contact.profilePhotoUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120" },
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(54.dp)
                                                        .clip(CircleShape)
                                                        .border(1.5.dp, BrandCyan, CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                                // Live Online Status Ring Indicator
                                                Box(
                                                    modifier = Modifier
                                                        .size(13.dp)
                                                        .clip(CircleShape)
                                                        .background(BrandGreen)
                                                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                                        .align(Alignment.BottomEnd)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = contact.name.split(" ").first(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.width(54.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }

                        // Archive count if exists
                        if (archivedConversations.isNotEmpty() && searchQuery.isEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            Toast.makeText(context, "Archive folder opened", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Archived Conversations", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = archivedConversations.size.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandCyan
                                    )
                                }
                                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            }
                        }

                        // Conversation Rows
                        if (filteredConversations.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(60.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("No conversations found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        } else {
                            items(filteredConversations) { convo ->
                                ConversationRowItem(
                                    convo = convo,
                                    currentUser = user,
                                    onOpenChat = { activeChatTarget = convo.partner },
                                    onPinToggle = { FleearnRepository.updateConversationPin(convo.partner.uid, !convo.meta.isPinned) },
                                    onMuteToggle = { FleearnRepository.updateConversationMute(convo.partner.uid, !convo.meta.isMuted) },
                                    onArchiveToggle = { FleearnRepository.updateConversationArchive(convo.partner.uid, !convo.meta.isArchived) },
                                    onDeleteConvo = { FleearnRepository.deleteConversation(convo.partner.uid) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Call History Tab
                    val sortedCalls = calls.sortedByDescending { it.timestamp }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        if (sortedCalls.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(60.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("No call records found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        } else {
                            items(sortedCalls) { call ->
                                CallHistoryRowItem(
                                    call = call,
                                    currentUser = user,
                                    onRedialVoice = {
                                        val record = CallRecord(
                                            id = "CALL_${System.currentTimeMillis()}",
                                            callerUid = user.uid,
                                            receiverUid = if (call.callerUid == user.uid) call.receiverUid else call.callerUid,
                                            callerName = user.name,
                                            receiverName = if (call.callerUid == user.uid) call.receiverName else call.callerName,
                                            isVideo = false,
                                            status = "ONGOING"
                                        )
                                        activeCallState = record
                                        isCallIncoming = false
                                        FleearnRepository.registerCall(record.callerUid, record.receiverUid, record.callerName, record.receiverName, record.isVideo, record.status)
                                    },
                                    onRedialVideo = {
                                        val record = CallRecord(
                                            id = "CALL_${System.currentTimeMillis()}",
                                            callerUid = user.uid,
                                            receiverUid = if (call.callerUid == user.uid) call.receiverUid else call.callerUid,
                                            callerName = user.name,
                                            receiverName = if (call.callerUid == user.uid) call.receiverName else call.callerName,
                                            isVideo = true,
                                            status = "ONGOING"
                                        )
                                        activeCallState = record
                                        isCallIncoming = false
                                        FleearnRepository.registerCall(record.callerUid, record.receiverUid, record.callerName, record.receiverName, record.isVideo, record.status)
                                    }
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Pinned Official Channels
                    val filteredChannels = if (searchQuery.isBlank()) channels else channels.filter {
                        it.name.contains(searchQuery.trim(), ignoreCase = true)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(filteredChannels) { channel ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { activeChannelTarget = channel },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = channel.avatarUrl.ifEmpty { "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=120" },
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(channel.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            VerifiedBadge(size = 14)
                                        }
                                        Text(channel.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${channel.memberCount} members", fontSize = 10.sp, color = BrandCyan, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Icon(Icons.Default.Star, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Active Chat dialog / screen overlay
    activeChatTarget?.let { target ->
        ChatConversationScreen(
            partner = target,
            currentUser = user,
            onDismiss = { activeChatTarget = null },
            onInitiateCall = { isVideo ->
                val call = CallRecord(
                    id = "CALL_${System.currentTimeMillis()}",
                    callerUid = user.uid,
                    receiverUid = target.uid,
                    callerName = user.name,
                    receiverName = target.name,
                    isVideo = isVideo,
                    status = "ONGOING"
                )
                activeCallState = call
                isCallIncoming = false
                FleearnRepository.registerCall(call.callerUid, call.receiverUid, call.callerName, call.receiverName, call.isVideo, call.status)
            }
        )
    }

    // Official Channel View Overlay
    activeChannelTarget?.let { channel ->
        Dialog(onDismissRequest = { activeChannelTarget = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = channel.avatarUrl, contentDescription = null, modifier = Modifier.size(50.dp).clip(CircleShape))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(channel.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                VerifiedBadge(size = 14)
                            }
                            Text("${channel.memberCount} subscribers", fontSize = 11.sp, color = BrandCyan)
                        }
                        IconButton(onClick = { activeChannelTarget = null }) { Icon(Icons.Default.Close, contentDescription = null) }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(channel.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { Toast.makeText(context, "Joined ${channel.name} Notifications enabled!", Toast.LENGTH_SHORT).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mute Channel Updates")
                    }
                }
            }
        }
    }

    // Call Screen Overlay (Pulse animated fullscreen voice/video calling experience)
    activeCallState?.let { call ->
        CallingScreen(
            call = call,
            isIncoming = isCallIncoming,
            onAnswer = {
                val updatedCall = call.copy(status = "ONGOING")
                activeCallState = updatedCall
                isCallIncoming = false
                FleearnRepository.updateCallStatus(call.id, "ONGOING", 0)
            },
            onReject = {
                FleearnRepository.updateCallStatus(call.id, "REJECTED", 0)
                activeCallState = null
            },
            onEndCall = { duration ->
                FleearnRepository.updateCallStatus(call.id, "COMPLETED", duration)
                activeCallState = null
            }
        )
    }
}

data class PartnerConversation(
    val partner: UserProfile,
    val lastMessage: ChatMessage?,
    val meta: ConversationMeta
)

@Composable
fun ConversationRowItem(
    convo: PartnerConversation,
    currentUser: UserProfile,
    onOpenChat: () -> Unit,
    onPinToggle: () -> Unit,
    onMuteToggle: () -> Unit,
    onArchiveToggle: () -> Unit,
    onDeleteConvo: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // On-the-fly local message decryption safely
    val decryptedText = remember(convo.lastMessage) {
        val msg = convo.lastMessage
        if (msg == null) ""
        else if (msg.deletedForEveryone) "Message deleted"
        else if (msg.messageText.isEmpty()) ""
        else if (msg.isEncrypted) {
            MessageCrypt.decrypt(msg.messageText, msg.senderUid, msg.receiverUid)
        } else {
            msg.messageText
        }
    }

    val displayTime = remember(convo.lastMessage) {
        if (convo.lastMessage == null) ""
        else {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(Date(convo.lastMessage.timestamp))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, AppTheme.colors.borderColor, RoundedCornerShape(16.dp))
            .clickable { onOpenChat() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (convo.meta.isPinned) AppTheme.colors.accentColor.copy(alpha = 0.12f) else AppTheme.colors.surfaceColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AsyncImage(
                    model = convo.partner.profilePhotoUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120" },
                    contentDescription = null,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                // Simulated Online State
                if (convo.partner.age % 2 == 0) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.successColor)
                            .border(1.5.dp, AppTheme.colors.backgroundColor, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        convo.partner.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AppTheme.colors.primaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (convo.partner.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadge(role = convo.partner.role, size = 13)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                
                // Show rich icons if audio, images, files
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val last = convo.lastMessage
                    if (last != null) {
                        if (last.isAudio) {
                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(12.dp).padding(end = 4.dp), tint = AppTheme.colors.accentColor)
                            Text("Voice message", fontSize = 12.sp, color = AppTheme.colors.accentColor, fontWeight = FontWeight.Bold)
                        } else if (last.isImage) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(12.dp).padding(end = 4.dp), tint = AppTheme.colors.accentColor)
                            Text("Photo attachment", fontSize = 12.sp, color = AppTheme.colors.accentColor)
                        } else if (last.isVideo) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(12.dp).padding(end = 4.dp), tint = AppTheme.colors.accentColor)
                            Text("Video clip", fontSize = 12.sp, color = AppTheme.colors.accentColor)
                        } else if (last.isFile) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(12.dp).padding(end = 4.dp), tint = AppTheme.colors.accentColor)
                            Text(last.fileName.ifEmpty { "Document" }, fontSize = 12.sp, color = AppTheme.colors.accentColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        } else {
                            Text(
                                decryptedText,
                                fontSize = 12.sp,
                                color = AppTheme.colors.secondaryTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Text("No messages yet", fontSize = 12.sp, color = AppTheme.colors.secondaryTextColor.copy(alpha = 0.6f))
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Dynamic Right Controls & Badges
            Column(horizontalAlignment = Alignment.End) {
                if (displayTime.isNotEmpty()) {
                    Text(displayTime, fontSize = 10.sp, color = AppTheme.colors.secondaryTextColor.copy(alpha = 0.7f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (convo.meta.isMuted) {
                        Icon(Icons.Default.Lock, contentDescription = "Muted", tint = AppTheme.colors.secondaryTextColor.copy(alpha = 0.4f), modifier = Modifier.size(14.dp).padding(end = 4.dp))
                    }
                    if (convo.meta.isPinned) {
                        Icon(Icons.Default.Star, contentDescription = "Pinned", tint = AppTheme.colors.accentColor, modifier = Modifier.size(14.dp).padding(end = 4.dp))
                    }
                    if (convo.meta.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppTheme.colors.accentColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(convo.meta.unreadCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.buttonTextColor)
                        }
                    }
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (convo.meta.isPinned) "Unpin Chat" else "Pin Chat") },
                            onClick = { onPinToggle(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (convo.meta.isMuted) "Unmute Chat" else "Mute Chat") },
                            onClick = { onMuteToggle(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Archive Chat") },
                            onClick = { onArchiveToggle(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Chat", color = Color.Red) },
                            onClick = { onDeleteConvo(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Red) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CallHistoryRowItem(
    call: CallRecord,
    currentUser: UserProfile,
    onRedialVoice: () -> Unit,
    onRedialVideo: () -> Unit
) {
    val isOutgoing = call.callerUid == currentUser.uid
    val context = LocalContext.current
    val callTime = remember(call.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        sdf.format(Date(call.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (call.status == "MISSED") Color.Red.copy(alpha = 0.15f)
                        else BrandGreen.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (call.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = null,
                    tint = if (call.status == "MISSED") Color.Red else BrandGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOutgoing) call.receiverName else call.callerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isOutgoing) Icons.Default.ArrowForward else Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (call.status == "MISSED") Color.Red else BrandCyan
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${call.status} • $callTime",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Call Redial Actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onRedialVoice) {
                    Icon(Icons.Default.Phone, contentDescription = "Voice Call", tint = BrandCyan, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onRedialVideo) {
                    Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = BrandCyan, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// Full Chat Conversation screen with comprehensive features
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    partner: UserProfile,
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onInitiateCall: (isVideo: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val messages by FleearnRepository.messages.collectAsState()
    val blockedUsers by FleearnRepository.blockedUsers.collectAsState()
    val isBlocked = blockedUsers.contains(partner.uid)
    
    // Sort and decrypt messages
    val chatMessages = remember(messages, partner.uid) {
        messages.filter {
            ((it.senderUid == currentUser.uid && it.receiverUid == partner.uid) ||
            (it.senderUid == partner.uid && it.receiverUid == currentUser.uid)) &&
            !it.deletedForUidsJson.contains(currentUser.uid)
        }.sortedBy { it.timestamp }
    }

    var messageText by remember { mutableStateOf("") }
    
    // Typing state simulator
    var isTypingSimulated by remember { mutableStateOf(false) }

    // Media attachment state
    var showAttachmentSheet by remember { mutableStateOf(false) }

    // Reply Message State
    var replyTargetMessage by remember { mutableStateOf<ChatMessage?>(null) }

    // Edit message state
    var editTargetMessage by remember { mutableStateOf<ChatMessage?>(null) }

    // Selected message action dialog
    var actionTargetMessage by remember { mutableStateOf<ChatMessage?>(null) }

    // Voice recording simulated states
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableStateOf(0) }
    var isVoiceLocked by remember { mutableStateOf(false) }
    var isRecordingCancelled by remember { mutableStateOf(false) }
    var voicePreviewPath by remember { mutableStateOf<String?>(null) }
    var isPlayingPreview by remember { mutableStateOf(false) }

    // Live wave simulator state
    var voiceWaveformBars by remember { mutableStateOf(List(15) { (4..24).random() }) }

    // Audio Playback states for message cells
    var currentlyPlayingMsgId by remember { mutableStateOf<String?>(null) }
    var msgPlaybackSpeed by remember { mutableStateOf(1.0f) }

    // Dropdown options menu state
    var showMenu by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showBlockConfirm by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    
    // Report fields
    var selectedReportCategory by remember { mutableStateOf("Spam") }
    var reportDescriptionText by remember { mutableStateOf("") }

    // Mark as read when entering
    LaunchedEffect(partner.uid, messages.size) {
        FleearnRepository.markConversationAsRead(partner.uid)
    }

    // Typing simulated flow
    LaunchedEffect(messageText) {
        if (messageText.isNotEmpty()) {
            isTypingSimulated = true
            delay(2000)
            isTypingSimulated = false
        }
    }

    // Voice record duration ticker
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingDurationSec = 0
            while (isRecordingVoice) {
                delay(1000)
                recordingDurationSec++
                voiceWaveformBars = List(15) { (4..24).random() }
            }
        }
    }

    BackHandler(onBack = onDismiss)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppTheme.colors.backgroundColor
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.surfaceColor)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = AppTheme.colors.primaryTextColor
                    )
                }
                Box {
                    AsyncImage(
                        model = partner.profilePhotoUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120" },
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    if (partner.age % 2 == 0) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.successColor)
                                .border(1.5.dp, AppTheme.colors.surfaceColor, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = partner.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AppTheme.colors.primaryTextColor
                        )
                        if (partner.isVerified) {
                            Spacer(modifier = Modifier.width(3.dp))
                            VerifiedBadge(role = partner.role, size = 12)
                        }
                    }
                    if (isTypingSimulated) {
                        Text("typing...", fontSize = 11.sp, color = AppTheme.colors.accentColor, fontWeight = FontWeight.SemiBold)
                    } else if (partner.age % 2 == 0) {
                        Text("Online", fontSize = 11.sp, color = AppTheme.colors.successColor)
                    } else {
                        Text("Offline", fontSize = 11.sp, color = AppTheme.colors.secondaryTextColor.copy(alpha = 0.6f))
                    }
                }

                // Header Action Buttons (Calling + Options menu)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (!isBlocked) onInitiateCall(false) },
                        enabled = !isBlocked
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Voice Call",
                            tint = if (isBlocked) AppTheme.colors.secondaryTextColor.copy(alpha = 0.4f) else AppTheme.colors.accentColor
                        )
                    }
                    IconButton(
                        onClick = { if (!isBlocked) onInitiateCall(true) },
                        enabled = !isBlocked
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = if (isBlocked) AppTheme.colors.secondaryTextColor.copy(alpha = 0.4f) else AppTheme.colors.accentColor
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = AppTheme.colors.primaryTextColor
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(AppTheme.colors.surfaceColor)
                        ) {
                            DropdownMenuItem(
                                text = { Text("View Profile", color = AppTheme.colors.primaryTextColor) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AppTheme.colors.accentColor) },
                                onClick = {
                                    showMenu = false
                                    showProfileDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isBlocked) "Unblock User" else "Block User", color = if (isBlocked) AppTheme.colors.successColor else AppTheme.colors.errorColor) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isBlocked) Icons.Default.CheckCircle else Icons.Default.Block,
                                        contentDescription = null,
                                        tint = if (isBlocked) AppTheme.colors.successColor else AppTheme.colors.errorColor
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    if (isBlocked) {
                                        FleearnRepository.unblockUser(partner.uid)
                                        Toast.makeText(context, "${partner.name} has been unblocked", Toast.LENGTH_SHORT).show()
                                    } else {
                                        showBlockConfirm = true
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Report User", color = AppTheme.colors.errorColor) },
                                leadingIcon = { Icon(Icons.Default.Report, contentDescription = null, tint = AppTheme.colors.errorColor) },
                                onClick = {
                                    showMenu = false
                                    showReportDialog = true
                                }
                            )
                        }
                    }
                }
            }
            Divider(color = AppTheme.colors.borderColor, thickness = 1.dp)

            // E2E Security notification
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.successColor.copy(alpha = 0.1f))
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = AppTheme.colors.successColor)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Messages are end-to-end encrypted. No keys stored on servers.", fontSize = 10.sp, color = AppTheme.colors.successColor, fontWeight = FontWeight.SemiBold)
                }
            }

            // Message History Panel
            val listState = rememberLazyListState()
            LaunchedEffect(chatMessages.size) {
                if (chatMessages.isNotEmpty()) {
                    listState.animateScrollToItem(chatMessages.size - 1)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
            ) {
                if (chatMessages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No messages yet. Send a secure greeting to start.",
                                fontSize = 12.sp,
                                color = AppTheme.colors.secondaryTextColor.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(chatMessages) { msg ->
                        MessageBubbleCell(
                            msg = msg,
                            currentUser = currentUser,
                            partner = partner,
                            isPlaying = currentlyPlayingMsgId == msg.id,
                            playbackSpeed = msgPlaybackSpeed,
                            onTogglePlayAudio = {
                                currentlyPlayingMsgId = if (currentlyPlayingMsgId == msg.id) null else msg.id
                            },
                            onToggleAudioSpeed = {
                                msgPlaybackSpeed = if (msgPlaybackSpeed == 1.0f) 1.5f else if (msgPlaybackSpeed == 1.5f) 2.0f else 1.0f
                            },
                            onLongClick = {
                                actionTargetMessage = msg
                            },
                            onRetry = {
                                val textToRetry = if (msg.messageText.isNotEmpty()) {
                                    if (msg.isEncrypted) MessageCrypt.decrypt(msg.messageText, msg.senderUid, msg.receiverUid) else msg.messageText
                                } else ""
                                FleearnRepository.sendMessage(
                                    receiverUid = partner.uid,
                                    text = textToRetry,
                                    isAudio = msg.isAudio,
                                    isImage = msg.isImage,
                                    isVideo = msg.isVideo,
                                    isFile = msg.isFile,
                                    mediaUrl = msg.mediaUrl,
                                    fileName = msg.fileName,
                                    fileSize = msg.fileSize,
                                    audioDurationSec = msg.audioDurationSec,
                                    replyToId = msg.replyToId,
                                    replyToText = msg.replyToText,
                                    replyToSenderName = msg.replyToSenderName,
                                    customMsgId = msg.id
                                )
                            }
                        )
                    }
                }
            }

            // Reply reference view preview
            replyTargetMessage?.let { refMsg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceColor)
                        .border(1.dp, AppTheme.colors.borderColor)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(36.dp)
                            .background(AppTheme.colors.accentColor, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (refMsg.senderUid == currentUser.uid) "Reply to Yourself" else "Reply to ${partner.name}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.accentColor
                        )
                        val decryptedRef = if (refMsg.isEncrypted) MessageCrypt.decrypt(refMsg.messageText, refMsg.senderUid, refMsg.receiverUid) else refMsg.messageText
                        Text(
                            text = if (refMsg.isAudio) "🎤 Voice Message" else if (refMsg.isImage) "📷 Photo" else decryptedRef,
                            fontSize = 12.sp,
                            color = AppTheme.colors.primaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { replyTargetMessage = null }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Reply",
                            modifier = Modifier.size(16.dp),
                            tint = AppTheme.colors.primaryTextColor
                        )
                    }
                }
            }

            // Edit reference view preview
            editTargetMessage?.let { editMsg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.accentColor.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = AppTheme.colors.accentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Editing message", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.accentColor)
                        val decryptedEdit = if (editMsg.isEncrypted) MessageCrypt.decrypt(editMsg.messageText, editMsg.senderUid, editMsg.receiverUid) else editMsg.messageText
                        Text(decryptedEdit, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = AppTheme.colors.primaryTextColor)
                    }
                    IconButton(onClick = {
                        editTargetMessage = null
                        messageText = ""
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Edit",
                            modifier = Modifier.size(16.dp),
                            tint = AppTheme.colors.primaryTextColor
                        )
                    }
                }
            }

            // Block Composer switch
            if (isBlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceColor)
                        .border(1.dp, AppTheme.colors.borderColor)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "You have blocked this contact. Unblock to secure connection.",
                            color = AppTheme.colors.secondaryTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                FleearnRepository.unblockUser(partner.uid)
                                Toast.makeText(context, "${partner.name} has been unblocked", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.buttonColor,
                                contentColor = AppTheme.colors.buttonTextColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Unblock User", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Chat Composer Input Area
                Column(modifier = Modifier.fillMaxWidth()) {
                    Divider(color = AppTheme.colors.borderColor, thickness = 1.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppTheme.colors.surfaceColor)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    // Attachments sheet button
                    IconButton(onClick = { showAttachmentSheet = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Attachment Options", tint = AppTheme.colors.accentColor, modifier = Modifier.size(28.dp))
                    }

                    // Input Box / Voice Recording State Panel
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(AppTheme.colors.backgroundColor, RoundedCornerShape(24.dp))
                            .border(1.dp, AppTheme.colors.borderColor, RoundedCornerShape(24.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (isRecordingVoice) {
                            // Voice Recording simulated panel
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = AppTheme.colors.errorColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = String.format("%02d:%02d", recordingDurationSec / 60, recordingDurationSec % 60),
                                    color = AppTheme.colors.errorColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                // Simple waveform bars animation
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    voiceWaveformBars.forEach { height ->
                                        Box(
                                            modifier = Modifier
                                                .width(2.5.dp)
                                                .height(height.dp)
                                                .background(AppTheme.colors.accentColor, RoundedCornerShape(1.dp))
                                        )
                                    }
                                }
                                Text(
                                    text = "Slide to cancel",
                                    fontSize = 11.sp,
                                    color = AppTheme.colors.secondaryTextColor.copy(alpha = 0.6f),
                                    modifier = Modifier.clickable {
                                        isRecordingVoice = false
                                        isRecordingCancelled = true
                                        Toast.makeText(context, "Recording cancelled", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        } else {
                            // Normal Text Field Composer
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        messageText += "😊"
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Outlined.EmojiEmotions, contentDescription = "Emoji picker", tint = AppTheme.colors.secondaryTextColor)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                BasicTextField(
                                    value = messageText,
                                    onValueChange = { messageText = it },
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = AppTheme.colors.primaryTextColor),
                                    decorationBox = { innerTextField ->
                                        if (messageText.isEmpty()) {
                                            Text("Type secured message...", fontSize = 14.sp, color = AppTheme.colors.secondaryTextColor.copy(alpha = 0.5f))
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Dynamic Voice / Send Action Button
                    Box {
                        if (messageText.isNotBlank() || editTargetMessage != null) {
                            IconButton(
                                onClick = {
                                    val currentText = messageText.trim()
                                    if (currentText.isNotBlank()) {
                                        if (editTargetMessage != null) {
                                            FleearnRepository.editMessage(editTargetMessage!!.id, currentText)
                                            editTargetMessage = null
                                        } else {
                                            val replyId = replyTargetMessage?.id
                                            val replyText = replyTargetMessage?.let {
                                                if (it.isAudio) "Voice message" else if (it.isImage) "Photo" else MessageCrypt.decrypt(it.messageText, it.senderUid, it.receiverUid)
                                            }
                                            val replyName = replyTargetMessage?.let { if (it.senderUid == currentUser.uid) "You" else partner.name }
                                            
                                            FleearnRepository.sendMessage(
                                                receiverUid = partner.uid,
                                                text = currentText,
                                                replyToId = replyId,
                                                replyToText = replyText,
                                                replyToSenderName = replyName
                                            )
                                            replyTargetMessage = null
                                        }
                                        messageText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(AppTheme.colors.buttonColor, CircleShape)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = AppTheme.colors.buttonTextColor)
                            }
                        } else {
                            // Voice Record Button (Hold to Record, click to start/stop simulation)
                            IconButton(
                                onClick = {
                                    if (isRecordingVoice) {
                                        // Stop and Send Voice recording
                                        isRecordingVoice = false
                                        FleearnRepository.sendMessage(
                                            receiverUid = partner.uid,
                                            text = "",
                                            isAudio = true,
                                            audioDurationSec = recordingDurationSec
                                        )
                                        Toast.makeText(context, "Voice message sent!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Start Recording Voice
                                        isRecordingVoice = true
                                        Toast.makeText(context, "Recording started. Click again to stop & send.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(if (isRecordingVoice) AppTheme.colors.errorColor else AppTheme.colors.buttonColor, CircleShape)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Record", tint = AppTheme.colors.buttonTextColor)
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // View Profile Dialog
    if (showProfileDialog) {
        Dialog(onDismissRequest = { showProfileDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, AppTheme.colors.borderColor, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        AsyncImage(
                            model = partner.profilePhotoUrl.ifEmpty { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200" },
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(2.dp, AppTheme.colors.accentColor, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = partner.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = AppTheme.colors.primaryTextColor
                        )
                        if (partner.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            VerifiedBadge(role = partner.role, size = 16)
                        }
                    }
                    Text(
                        text = "UID: ${partner.uid}",
                        fontSize = 12.sp,
                        color = AppTheme.colors.secondaryTextColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Divider(color = AppTheme.colors.borderColor, modifier = Modifier.padding(vertical = 16.dp))
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mobile Number", color = AppTheme.colors.secondaryTextColor, fontSize = 13.sp)
                            Text(partner.mobile.ifEmpty { "Hidden (E2E Encrypted)" }, color = AppTheme.colors.primaryTextColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Account Role", color = AppTheme.colors.secondaryTextColor, fontSize = 13.sp)
                            Text(partner.role.name, color = AppTheme.colors.accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Simulation Age", color = AppTheme.colors.secondaryTextColor, fontSize = 13.sp)
                            Text("${partner.age} years", color = AppTheme.colors.primaryTextColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showProfileDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.buttonColor,
                            contentColor = AppTheme.colors.buttonTextColor
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Block User Confirmation Dialog
    if (showBlockConfirm) {
        Dialog(onDismissRequest = { showBlockConfirm = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, AppTheme.colors.borderColor, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Block ${partner.name}?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppTheme.colors.primaryTextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Are you sure you want to block this user? Blocked contacts will not be able to initiate calls or send messages securely to you.",
                        fontSize = 13.sp,
                        color = AppTheme.colors.secondaryTextColor
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showBlockConfirm = false }) {
                            Text("Cancel", color = AppTheme.colors.secondaryTextColor)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                showBlockConfirm = false
                                FleearnRepository.blockUser(partner.uid)
                                Toast.makeText(context, "${partner.name} has been blocked", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.errorColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Block User", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Report User Dialog
    if (showReportDialog) {
        Dialog(onDismissRequest = { showReportDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, AppTheme.colors.borderColor, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Report User",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppTheme.colors.primaryTextColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Select a category for your report:",
                        fontSize = 12.sp,
                        color = AppTheme.colors.secondaryTextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val categories = listOf("Spam", "Harassment", "Inappropriate behavior", "Financial scam", "Other")
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReportCategory = cat }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReportCategory == cat,
                                onClick = { selectedReportCategory = cat },
                                colors = RadioButtonDefaults.colors(selectedColor = AppTheme.colors.accentColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cat, fontSize = 13.sp, color = AppTheme.colors.primaryTextColor)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reportDescriptionText,
                        onValueChange = { reportDescriptionText = it },
                        placeholder = { Text("Describe the issue (optional)", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = AppTheme.colors.primaryTextColor),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.colors.accentColor,
                            unfocusedBorderColor = AppTheme.colors.borderColor,
                            focusedContainerColor = AppTheme.colors.backgroundColor,
                            unfocusedContainerColor = AppTheme.colors.backgroundColor
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showReportDialog = false }) {
                            Text("Cancel", color = AppTheme.colors.secondaryTextColor)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (reportDescriptionText.isBlank() && selectedReportCategory == "Other") {
                                    Toast.makeText(context, "Please add description for 'Other'", Toast.LENGTH_SHORT).show()
                                } else {
                                    showReportDialog = false
                                    val title = "Report User: ${partner.name}"
                                    val desc = "Category: $selectedReportCategory\nReported UID: ${partner.uid}\nDetails: $reportDescriptionText"
                                    FleearnRepository.submitReport(title, desc)
                                    Toast.makeText(context, "Report submitted securely to moderators.", Toast.LENGTH_LONG).show()
                                    reportDescriptionText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.errorColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Submit Report", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Attachment Sheets / Options Dialog
    if (showAttachmentSheet) {
        Dialog(onDismissRequest = { showAttachmentSheet = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AppTheme.colors.borderColor, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Select Attachment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AppTheme.colors.primaryTextColor,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                showAttachmentSheet = false
                                FleearnRepository.sendMessage(partner.uid, "", isImage = true, mediaUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400")
                                Toast.makeText(context, "Photo uploaded & E2E encrypted", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Box(modifier = Modifier.size(50.dp).background(AppTheme.colors.accentColor.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = AppTheme.colors.accentColor)
                            }
                            Text("Gallery", fontSize = 12.sp, color = AppTheme.colors.primaryTextColor, modifier = Modifier.padding(top = 4.dp))
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                showAttachmentSheet = false
                                FleearnRepository.sendMessage(partner.uid, "", isVideo = true, mediaUrl = "https://www.w3schools.com/html/mov_bbb.mp4")
                                Toast.makeText(context, "Video clip uploaded & E2E encrypted", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Box(modifier = Modifier.size(50.dp).background(AppTheme.colors.accentColor.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = AppTheme.colors.accentColor)
                            }
                            Text("Video", fontSize = 12.sp, color = AppTheme.colors.primaryTextColor, modifier = Modifier.padding(top = 4.dp))
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                showAttachmentSheet = false
                                FleearnRepository.sendMessage(partner.uid, "", isFile = true, fileName = "secure_e2ee_ledger.pdf", fileSize = "2.4 MB")
                                Toast.makeText(context, "Document uploaded & E2E encrypted", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Box(modifier = Modifier.size(50.dp).background(AppTheme.colors.accentColor.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, tint = AppTheme.colors.accentColor)
                            }
                            Text("Document", fontSize = 12.sp, color = AppTheme.colors.primaryTextColor, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAttachmentSheet = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.buttonColor,
                            contentColor = AppTheme.colors.buttonTextColor
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    // Message context option dialogue
    actionTargetMessage?.let { actionMsg ->
        val decryptedText = if (actionMsg.isEncrypted) MessageCrypt.decrypt(actionMsg.messageText, actionMsg.senderUid, actionMsg.receiverUid) else actionMsg.messageText
        Dialog(onDismissRequest = { actionTargetMessage = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AppTheme.colors.borderColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Reactions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.primaryTextColor)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("❤️", "👍", "😂", "😮", "😢", "🙏").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .clickable {
                                        FleearnRepository.addReaction(actionMsg.id, emoji)
                                        actionTargetMessage = null
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                    
                    Divider(color = AppTheme.colors.borderColor)
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            replyTargetMessage = actionMsg
                            actionTargetMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Reply, contentDescription = null, tint = AppTheme.colors.accentColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Reply to message", color = AppTheme.colors.primaryTextColor)
                        }
                    }

                    if (actionMsg.senderUid == currentUser.uid) {
                        TextButton(
                            onClick = {
                                editTargetMessage = actionMsg
                                messageText = decryptedText
                                actionTargetMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = AppTheme.colors.accentColor)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Edit message", color = AppTheme.colors.primaryTextColor)
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(decryptedText))
                            Toast.makeText(context, "Message copied!", Toast.LENGTH_SHORT).show()
                            actionTargetMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CopyAll, contentDescription = null, tint = AppTheme.colors.accentColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Copy text content", color = AppTheme.colors.primaryTextColor)
                        }
                    }

                    TextButton(
                        onClick = {
                            Toast.makeText(context, "Select contact to forward message", Toast.LENGTH_LONG).show()
                            actionTargetMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AppTheme.colors.accentColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Forward message", color = AppTheme.colors.primaryTextColor)
                        }
                    }

                    TextButton(
                        onClick = {
                            FleearnRepository.deleteMessageForMe(actionMsg.id)
                            actionTargetMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = AppTheme.colors.errorColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete for Me", color = AppTheme.colors.errorColor)
                        }
                    }

                    if (actionMsg.senderUid == currentUser.uid) {
                        TextButton(
                            onClick = {
                                FleearnRepository.deleteMessageForEveryone(actionMsg.id)
                                actionTargetMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = AppTheme.colors.errorColor)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Delete for Everyone", color = AppTheme.colors.errorColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubbleCell(
    msg: ChatMessage,
    currentUser: UserProfile,
    partner: UserProfile,
    isPlaying: Boolean,
    playbackSpeed: Float,
    onTogglePlayAudio: () -> Unit,
    onToggleAudioSpeed: () -> Unit,
    onLongClick: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    val isMe = msg.senderUid == currentUser.uid
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Local Message decryption on the fly
    val decryptedText = remember(msg) {
        if (msg.deletedForEveryone) "Message deleted"
        else if (msg.messageText.isEmpty()) ""
        else if (msg.isEncrypted) {
            MessageCrypt.decrypt(msg.messageText, msg.senderUid, msg.receiverUid)
        } else {
            msg.messageText
        }
    }

    val displayTime = remember(msg.timestamp) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date(msg.timestamp))
    }

    // Reactions deserializer
    val reactions = remember(msg.reactionsJson) {
        try {
            val json = JSONObject(msg.reactionsJson)
            val list = mutableListOf<String>()
            json.keys().forEach { k ->
                list.add(json.getString(k))
            }
            list
        } catch (e: Exception) {
            emptyList<String>()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            
            // Show reply preview inside bubble if exists
            if (msg.replyToId != null) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .background(AppTheme.colors.surfaceColor, RoundedCornerShape(8.dp))
                        .border(1.dp, AppTheme.colors.borderColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column {
                        Text(
                            text = msg.replyToSenderName ?: "Reply",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.accentColor
                        )
                        Text(
                            text = msg.replyToText ?: "Attachment",
                            fontSize = 11.sp,
                            color = AppTheme.colors.secondaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Message Bubble Card
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.deletedForEveryone) {
                        AppTheme.colors.surfaceColor.copy(alpha = 0.5f)
                    } else if (isMe) {
                        AppTheme.colors.buttonColor
                    } else {
                        AppTheme.colors.surfaceColor
                    }
                ),
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .border(
                        width = 1.dp,
                        color = if (isMe) AppTheme.colors.buttonColor else AppTheme.colors.borderColor,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        )
                    )
                    .combinedClickable(
                        onClick = {
                            if (msg.isAudio) onTogglePlayAudio()
                            else if (msg.isImage || msg.isVideo) {
                                Toast.makeText(context, "Opening secure E2E attachment", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onLongClick = onLongClick
                    )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    if (msg.deletedForEveryone) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp), tint = AppTheme.colors.secondaryTextColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("This message was deleted.", fontSize = 13.sp, color = AppTheme.colors.secondaryTextColor)
                        }
                    } else if (msg.isAudio) {
                        // Voice message bubble rendering
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onTogglePlayAudio, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isMe) AppTheme.colors.buttonTextColor else AppTheme.colors.accentColor
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                // Live dynamic seek bar simulator
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(4.dp)
                                        .background(
                                            if (isMe) AppTheme.colors.buttonTextColor.copy(alpha = 0.3f) else AppTheme.colors.borderColor,
                                            RoundedCornerShape(2.dp)
                                        )
                                ) {
                                    if (isPlaying) {
                                        var progress by remember { mutableStateOf(0.0f) }
                                        LaunchedEffect(Unit) {
                                            while (progress < 1.0f) {
                                                delay(50)
                                                progress += 0.01f * playbackSpeed
                                            }
                                            progress = 0.0f
                                            onTogglePlayAudio() // finish playback
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(progress)
                                                .background(if (isMe) AppTheme.colors.buttonTextColor else AppTheme.colors.accentColor, RoundedCornerShape(2.dp))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${msg.audioDurationSec} sec",
                                        fontSize = 11.sp,
                                        color = if (isMe) AppTheme.colors.buttonTextColor.copy(alpha = 0.8f) else AppTheme.colors.secondaryTextColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Speed toggle
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isMe) AppTheme.colors.buttonTextColor.copy(alpha = 0.2f) else AppTheme.colors.accentColor.copy(alpha = 0.15f))
                                            .clickable { onToggleAudioSpeed() }
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("${playbackSpeed}x", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isMe) AppTheme.colors.buttonTextColor else AppTheme.colors.accentColor)
                                    }
                                }
                            }
                        }
                    } else if (msg.isImage) {
                        // Image rendering cell with E2E Lock Overlay
                        Box {
                            AsyncImage(
                                model = msg.mediaUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(6.dp)
                                    .align(Alignment.BottomStart)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(11.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Decrypted Image File", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (msg.isVideo) {
                        // Video rendering cell
                        Box {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1461151304267-38cd8917a94e?w=400",
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .align(Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(6.dp)
                                    .align(Alignment.BottomStart)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(11.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Decrypted Video clip", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (msg.isFile) {
                        // File Document block
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(if (isMe) AppTheme.colors.buttonTextColor.copy(alpha = 0.15f) else AppTheme.colors.accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = if (isMe) AppTheme.colors.buttonTextColor else AppTheme.colors.accentColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(msg.fileName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isMe) AppTheme.colors.buttonTextColor else AppTheme.colors.primaryTextColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(msg.fileSize, fontSize = 10.sp, color = if (isMe) AppTheme.colors.buttonTextColor.copy(alpha = 0.7f) else AppTheme.colors.secondaryTextColor)
                            }
                        }
                    } else {
                        // Normal text content with auto E2E tag
                        Text(
                            text = decryptedText,
                            fontSize = 14.sp,
                            color = if (isMe) AppTheme.colors.buttonTextColor else AppTheme.colors.primaryTextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Date Time + Delivery/Read Status
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (msg.isEdited && !msg.deletedForEveryone) {
                            Text(
                                "edited • ",
                                fontSize = 9.sp,
                                color = if (isMe) AppTheme.colors.buttonTextColor.copy(alpha = 0.6f) else AppTheme.colors.secondaryTextColor.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            displayTime,
                            fontSize = 9.sp,
                            color = if (isMe) AppTheme.colors.buttonTextColor.copy(alpha = 0.7f) else AppTheme.colors.secondaryTextColor.copy(alpha = 0.7f)
                        )
                        if (isMe && !msg.deletedForEveryone) {
                            Spacer(modifier = Modifier.width(4.dp))
                            when (msg.deliveryStatus) {
                                "SENDING" -> {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Sending Message",
                                        tint = AppTheme.colors.buttonTextColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                "FAILED" -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { onRetry?.invoke() }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = "Failed. Tap to retry.",
                                            tint = AppTheme.colors.errorColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Retry", fontSize = 9.sp, color = AppTheme.colors.errorColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Read Check",
                                        tint = if (msg.timestamp % 2 == 0L) AppTheme.colors.successColor else AppTheme.colors.buttonTextColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reactions overlay rendering
            if (reactions.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                ) {
                    reactions.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppTheme.colors.surfaceColor)
                                .border(1.dp, AppTheme.colors.borderColor, RoundedCornerShape(10.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(emoji, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// Complete Calling Simulation overlay UI
@Composable
fun CallingScreen(
    call: CallRecord,
    isIncoming: Boolean,
    onAnswer: () -> Unit,
    onReject: () -> Unit,
    onEndCall: (durationSec: Int) -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    var isCameraOn by remember { mutableStateOf(call.isVideo) }
    var isMicrophoneOn by remember { mutableStateOf(true) }

    // Call duration timer ticker
    var durationSec by remember { mutableStateOf(0) }
    val isOngoing = call.status == "ONGOING" && !isIncoming

    LaunchedEffect(isOngoing) {
        if (isOngoing) {
            durationSec = 0
            while (true) {
                delay(1000)
                durationSec++
            }
        }
    }

    val displayDuration = remember(durationSec) {
        String.format("%02d:%02d", durationSec / 60, durationSec % 60)
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E293B)
                        )
                    )
                )
        ) {
            // Pulse Ring Animation for Incoming/Outgoing Call
            if (isIncoming || call.status == "ONGOING") {
                val infiniteTransition = rememberInfiniteTransition()
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(scale)
                        .background(BrandCyan.copy(alpha = alpha), CircleShape)
                        .align(Alignment.Center)
                )
            }

            // Caller profile banner
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=240",
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(4.dp, BrandCyan, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = if (isIncoming) call.callerName else call.receiverName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isIncoming) "Incoming secure ${if (call.isVideo) "Video" else "Voice"} call..."
                           else if (isOngoing) "Active Call • $displayDuration"
                           else "Secure connection dial-up...",
                    color = BrandCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Video Feed Simulation Block
            if (call.isVideo && isCameraOn && isOngoing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, BrandCyan, RoundedCornerShape(16.dp))
                        .background(Color.DarkGray)
                        .align(Alignment.Center)
                ) {
                    // Full feed placeholder
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1461151304267-38cd8917a94e?w=600",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(6.dp)
                            .align(Alignment.BottomStart)
                    ) {
                        Text("Simulated E2E encrypted HD Video", color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            // Bottom control buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isIncoming) {
                    // Incoming actions
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconButton(
                            onClick = onReject,
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Red, CircleShape)
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "Decline call", tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                        IconButton(
                            onClick = onAnswer,
                            modifier = Modifier
                                .size(64.dp)
                                .background(BrandGreen, CircleShape)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Answer call", tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                    }
                } else {
                    // Ongoing Call Controls row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier
                                .size(50.dp)
                                .background(if (isMuted) Color.White else Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Speaker",
                                tint = if (isMuted) Color.Black else Color.White
                            )
                        }
                        
                        if (call.isVideo) {
                            IconButton(
                                onClick = { isCameraOn = !isCameraOn },
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(if (!isCameraOn) Color.White else Color.White.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                    contentDescription = "Camera toggle",
                                    tint = if (!isCameraOn) Color.Black else Color.White
                                )
                            }
                        }

                        IconButton(
                            onClick = { isMicrophoneOn = !isMicrophoneOn },
                            modifier = Modifier
                                .size(50.dp)
                                .background(if (!isMicrophoneOn) Color.White else Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isMicrophoneOn) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Microphone toggle",
                                tint = if (!isMicrophoneOn) Color.Black else Color.White
                            )
                        }
                    }

                    // End call button
                    IconButton(
                        onClick = { onEndCall(durationSec) },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.Red, CircleShape)
                        ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }
            }
        }
    }
}
