package com.example.ui.user

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.UserPlan
import com.example.data.model.UserProfile
import com.example.data.repository.FleearnRepository
import com.example.ui.components.*
import com.example.ui.dialogs.DepositDialog
import com.example.ui.dialogs.WithdrawDialog
import com.example.ui.theme.*

data class QuickOption(val id: String, val title: String, val icon: ImageVector, val color: Color)

@Composable
fun UserHomeScreen(
    user: UserProfile,
    onNavigateToJobs: () -> Unit,
    onNavigateToShipping: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by FleearnRepository.currentUser.collectAsState()
    val appConfig by FleearnRepository.appConfig.collectAsState()
    val u = currentUser ?: user

    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var activeModal by remember { mutableStateOf<String?>(null) } // "FEED", "NOTICE", "LEADERBOARD", "SERVICES", "COURSES", "REFERRAL", "WEBSITE"

    val quickOptions = listOf(
        QuickOption("withdraw", "Withdraw", Icons.Default.AccountBalanceWallet, BrandCyan),
        QuickOption("feed", "Social Feed", Icons.Default.DynamicFeed, BrandPurple),
        QuickOption("notice", "Notice (PDF)", Icons.Default.Description, BrandGold),
        QuickOption("leaderboard", "Leaderboard", Icons.Default.EmojiEvents, BrandGreen),
        QuickOption("services", "Services", Icons.Default.DesignServices, BrandBlue),
        QuickOption("courses", "Courses", Icons.Default.School, BrandGold),
        QuickOption("post_job", "Post Job", Icons.Default.WorkOutline, BrandCyan),
        QuickOption("submissions", "Submissions", Icons.Default.FactCheck, BrandPurple),
        QuickOption("referral", "Referral", Icons.Default.Share, BrandGreen),
        QuickOption("website", "Website", Icons.Default.Public, BrandCyan)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // Top Bento Profile Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Profile Avatar with Emerald-to-Cyan Gradient Ring
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(BrandEmerald, BrandCyan)
                                )
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = u.profilePhotoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(BentoSurface),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = u.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = BentoTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Cyan check badge
                            Box(
                                modifier = Modifier
                                    .size(15.dp)
                                    .clip(CircleShape)
                                    .background(BrandCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Verified",
                                    tint = Color(0xFF0A0B0E),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                        Text(
                            text = "UID: ${u.uid}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.8.sp,
                            color = BentoTextMuted
                        )
                    }
                }

                // Bento Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Deposit Quick Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
                            .clickable { showDepositDialog = true }
                            .testTag("home_deposit_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Deposit",
                            tint = BrandEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Notification Quick Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
                            .clickable { activeModal = "NOTIFICATIONS" }
                            .testTag("home_notifications_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = BentoTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        // Rose unread indicator badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 9.dp, end = 9.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BrandRose)
                                .border(1.dp, BentoBackground, CircleShape)
                        )
                    }
                }
            }
        }

        // Trial Banner if active
        if (u.plan == UserPlan.TRIAL) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandEmerald.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = null, tint = BrandEmerald, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3-Day Premium Trial active • Unlimited micro-jobs and resell margins enabled.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = BentoTextPrimary
                        )
                    }
                }
            }
        }

        // Bento Hero Balance Card (Bento Grid Obsidian Gradient + 3-Column Metrics)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderSubtle)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0F141C), Color(0xFF161B22))
                            )
                        )
                ) {
                    // Subtle Geometric 3D Canvas
                    Box(modifier = Modifier.matchParentSize().alpha(0.35f)) {
                        Geometric3DCanvas(
                            modifier = Modifier.fillMaxSize(),
                            primaryColor = BrandEmerald,
                            secondaryColor = BrandCyan,
                            accentColor = BrandIndigo
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Card Header with Status Tag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CURRENT BALANCE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandEmerald,
                                letterSpacing = 1.6.sp
                            )

                            // Plan Pill Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(BrandEmerald.copy(alpha = 0.12f))
                                    .border(1.dp, BrandEmerald.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = u.plan.displayName.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandEmerald,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Large Balance Amount
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = String.format("%.2f", u.balance),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp,
                                color = Color.White
                            )
                            Text(
                                text = "FCT",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoTextSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Bento 3-Column Sub-Metrics Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Total Earn Bento Metric
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderSubtle)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "TOTAL EARN",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BentoTextMuted,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = String.format("%.0f", u.totalEarnings),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoTextPrimary
                                    )
                                }
                            }

                            // Withdraw Bento Metric
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderSubtle)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "WITHDRAW",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BentoTextMuted,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = String.format("%.0f", u.totalWithdrawn),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoTextPrimary
                                    )
                                }
                            }

                            // Streak Bento Metric
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderSubtle)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "STREAK",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BrandGold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${u.activeStreak} Days",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandGoldLight
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sponsored Ad Box (320x150 dp, 7s rotation)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            AdBannerBox()
        }

        // Bento Grid Fleearn Hub (3 Columns Bento Tile Layout)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            AppSectionHeader(title = "Fleearn Hub", subtitle = "Tools & verified services")
            Spacer(modifier = Modifier.height(10.dp))

            val hubItems = listOf(
                QuickOption("withdraw", "Withdraw", Icons.Default.AccountBalanceWallet, BrandIndigo),
                QuickOption("feed", "Feed", Icons.Default.DynamicFeed, BrandRose),
                QuickOption("notice", "Notice", Icons.Default.Description, BrandAmber),
                QuickOption("leaderboard", "Ranking", Icons.Default.EmojiEvents, BrandCyan),
                QuickOption("services", "Service", Icons.Default.DesignServices, BrandEmerald),
                QuickOption("courses", "Course", Icons.Default.School, BrandPurple),
                QuickOption("post_job", "Post Job", Icons.Default.WorkOutline, BrandBlue),
                QuickOption("submissions", "Review", Icons.Default.FactCheck, BrandGold),
                QuickOption("referral", "Referral", Icons.Default.Share, BrandIndigo),
                QuickOption("website", "Portal", Icons.Default.Public, BrandCyan)
            )

            // Bento Grid in rows of 3
            val rows = hubItems.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { option ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable {
                                        when (option.id) {
                                            "withdraw" -> showWithdrawDialog = true
                                            "post_job" -> onNavigateToJobs()
                                            "submissions" -> onNavigateToJobs()
                                            else -> activeModal = option.id.uppercase()
                                        }
                                    }
                                    .testTag("quick_option_${option.id}"),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = BentoSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderSubtle)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Bento Tinted Squircle Icon Container
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(13.dp))
                                            .background(option.color.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = option.icon,
                                            contentDescription = option.title,
                                            tint = option.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = option.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        color = BentoTextPrimary
                                    )
                                }
                            }
                        }
                        // Pad row if not full
                        if (rowItems.size < 3) {
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Live Feed Teaser
        item {
            Spacer(modifier = Modifier.height(20.dp))
            AppSectionHeader(
                title = "Live Community Feed",
                actionText = "View All Feed",
                onActionClick = { activeModal = "FEED" }
            )
            Spacer(modifier = Modifier.height(6.dp))
            val posts by FleearnRepository.posts.collectAsState()
            posts.take(2).forEach { post ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = post.authorPhoto,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(post.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // Modal dialogs
    if (showDepositDialog) {
        DepositDialog(
            onDismiss = { showDepositDialog = false },
            onSuccess = { msg ->
                showDepositDialog = false
                showSuccessDialog = "Deposit Request Submitted" to msg
            }
        )
    }

    if (showWithdrawDialog) {
        WithdrawDialog(
            onDismiss = { showWithdrawDialog = false },
            onSuccess = { msg ->
                showWithdrawDialog = false
                showSuccessDialog = "Withdrawal Submitted" to msg
            }
        )
    }

    showSuccessDialog?.let { (title, msg) ->
        Success3DDialog(
            title = title,
            message = msg,
            onDismiss = { showSuccessDialog = null }
        )
    }

    // Secondary Hub Modals
    when (activeModal) {
        "NOTIFICATIONS" -> NotificationsModal(onDismiss = { activeModal = null })
        "FEED" -> FeedModal(onDismiss = { activeModal = null })
        "NOTICE" -> NoticeModal(onDismiss = { activeModal = null })
        "LEADERBOARD" -> LeaderboardModal(onDismiss = { activeModal = null })
        "SERVICES" -> ServicesModal(onDismiss = { activeModal = null })
        "COURSES" -> CoursesModal(onDismiss = { activeModal = null })
        "REFERRAL" -> ReferralModal(user = u, onDismiss = { activeModal = null })
        "WEBSITE" -> WebsiteModal(url = appConfig.websiteUrl, onDismiss = { activeModal = null })
    }
}

@Composable
fun FeedModal(onDismiss: () -> Unit) {
    val posts by FleearnRepository.posts.collectAsState()
    var postText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Posts") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Community Feed", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Posts", "Long Videos", "Shorts", "Live").forEach { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text(tab, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Post Creator (Up to 700 words)
                OutlinedTextField(
                    value = postText,
                    onValueChange = { if (it.length <= 3500) postText = it },
                    label = { Text("Share update with Fleearn (up to 700 words)...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = {
                            if (postText.isNotBlank()) {
                                FleearnRepository.createPost(postText, type = selectedTab.uppercase())
                                postText = ""
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Post")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(posts.size) { idx ->
                        val post = posts[idx]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = post.authorPhoto,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp).clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(post.type, fontSize = 10.sp, color = BrandCyan)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(post.content, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = BrandRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${post.simulatedLikes} likes (viral algorithm)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoticeModal(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Official Platform Notices", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("📄 Fleearn Fleearn Policy & Guidelines v5.0", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("1. Multi-accounting or automated bot workers will lead to permanent forfeiture of balance.\n2. All withdrawals are processed via server-verified bKash and Nagad channels.\n3. Drop shipping deliveries take 24-72 hours across Bangladesh.", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close Notice") }
            }
        }
    }
}

@Composable
fun LeaderboardModal(onDismiss: () -> Unit) {
    val users by FleearnRepository.users.collectAsState()
    val sorted = users.sortedByDescending { it.balance }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.75f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Top 100 Earners Leaderboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(sorted.size) { idx ->
                        val u = sorted[idx]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("#${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (idx == 0) BrandGold else BrandCyan)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    AsyncImage(model = u.profilePhotoUrl, contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(u.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(u.teamName.ifEmpty { "Solo Earner" }, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                FctCurrencyPill(amount = u.balance)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServicesModal(onDismiss: () -> Unit) {
    val services by FleearnRepository.services.collectAsState()
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.75f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Publisher Services", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }
                Text("Order verified professional services with 50 FCT escrow guarantee", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(services.size) { idx ->
                        val s = services[idx]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(s.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(s.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${s.price} FCT", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Button(onClick = {}, shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                                        Text("Order (WhatsApp)", fontSize = 11.sp)
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

@Composable
fun CoursesModal(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val appConfig by FleearnRepository.appConfig.collectAsState()
    val currentUser by FleearnRepository.currentUser.collectAsState()
    var enteredPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val isPremium = currentUser?.plan == UserPlan.PREMIUM || currentUser?.plan == UserPlan.TRIAL

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fleearn Academy Courses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (!isPremium) {
                    Text("Courses are exclusive to Premium Plan members. Please upgrade your plan in Settings.", color = BrandRed, fontSize = 12.sp)
                } else {
                    Text("Enter the Daily Academy Access PIN provided by Administration (${appConfig.courseDailyPin}):", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { enteredPin = it; error = "" },
                        label = { Text("Daily PIN") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error.isNotBlank()) {
                        Text(error, color = BrandRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (enteredPin.trim() == appConfig.courseDailyPin) {
                                onDismiss()
                                InAppBrowserManager.openUrl(appConfig.courseWebsiteUrl, "Course Academy Portal")
                            } else {
                                error = "Invalid Daily Course PIN"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Unlock & Open Course Portal")
                    }
                }
            }
        }
    }
}

@Composable
fun ReferralModal(user: UserProfile, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Referral Commission Program", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Share your UID with friends. Earn 30.00 FCT instant bonus when they upgrade to Basic plan!", fontSize = 12.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(14.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text("Your Referral Code: ${user.uid}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Done") }
            }
        }
    }
}

@Composable
fun WebsiteModal(url: String, onDismiss: () -> Unit) {
    val appConfig by FleearnRepository.appConfig.collectAsState()
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Official Website & Fleearn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(appConfig.companyInfo, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        onDismiss()
                        InAppBrowserManager.openUrl(url, "Fleearn Official Portal")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Visit Official Website")
                }
            }
        }
    }
}

data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    val category: String,
    val icon: ImageVector,
    val iconColor: Color,
    val isUnread: Boolean = true
)

@Composable
fun NotificationsModal(onDismiss: () -> Unit) {
    var notifications by remember {
        mutableStateOf(
            listOf(
                NotificationItem(
                    id = "1",
                    title = "Premium Trial Unlocked",
                    description = "Your 3-day full Fleearn trial is active. Enjoy 0% withdrawal fees and unlimited micro-job submissions.",
                    timeAgo = "10m ago",
                    category = "System",
                    icon = Icons.Default.Celebration,
                    iconColor = BrandEmerald,
                    isUnread = true
                ),
                NotificationItem(
                    id = "2",
                    title = "Deposit Channels Verified",
                    description = "Automated bKash & Nagad instant merchant verification is running at 100% speed.",
                    timeAgo = "1h ago",
                    category = "Finance",
                    icon = Icons.Default.CheckCircle,
                    iconColor = BrandCyan,
                    isUnread = true
                ),
                NotificationItem(
                    id = "3",
                    title = "Daily Streak Reward",
                    description = "You have maintained your active daily login streak! Keep it going for the 365-day 100 FCT reward.",
                    timeAgo = "4h ago",
                    category = "Reward",
                    icon = Icons.Default.Bolt,
                    iconColor = BrandGold,
                    isUnread = false
                ),
                NotificationItem(
                    id = "4",
                    title = "Drop Shipping Marketplace",
                    description = "New trending gadgets & electronics have been added to the reseller catalog with up to 40% margin.",
                    timeAgo = "1d ago",
                    category = "Market",
                    icon = Icons.Default.LocalShipping,
                    iconColor = BrandIndigo,
                    isUnread = false
                )
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.82f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BrandRose.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = BrandRose,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Notifications",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                            val unreadCount = notifications.count { it.isUnread }
                            Text(
                                text = if (unreadCount > 0) "$unreadCount new updates" else "All caught up",
                                fontSize = 11.sp,
                                color = BentoTextMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BentoTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Bar (Mark all as read)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT ALERTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextMuted,
                        letterSpacing = 1.sp
                    )
                    if (notifications.any { it.isUnread }) {
                        TextButton(
                            onClick = {
                                notifications = notifications.map { it.copy(isUnread = false) }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Mark all read",
                                fontSize = 11.sp,
                                color = BrandEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notifications List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications.size) { index ->
                        val item = notifications[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    notifications = notifications.mapIndexed { idx, notif ->
                                        if (idx == index) notif.copy(isUnread = false) else notif
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.isUnread) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.02f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (item.isUnread) item.iconColor.copy(alpha = 0.3f) else BentoBorderSubtle
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(item.iconColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = item.iconColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = BentoTextPrimary
                                        )
                                        Text(
                                            text = item.timeAgo,
                                            fontSize = 10.sp,
                                            color = BentoTextMuted
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = item.description,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = BentoTextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(item.iconColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = item.category,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = item.iconColor
                                            )
                                        }

                                        if (item.isUnread) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(BrandRose)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                ) {
                    Text("Close", color = Color(0xFF0A0B0E), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
