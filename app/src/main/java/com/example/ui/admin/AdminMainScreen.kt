package com.example.ui.admin

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.data.repository.FleearnRepository
import com.example.ui.components.Success3DDialog
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*

enum class AdminNavCategory(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    USERS("Users", Icons.Default.People),
    PUBLISHERS("Publishers", Icons.Default.Business),
    MODERATORS("Moderators", Icons.Default.Shield),
    JOBS("Jobs & Ads", Icons.Default.Assignment),
    PRODUCTS("Products", Icons.Default.LocalMall),
    FINANCE("Finance & Fees", Icons.Default.AccountBalanceWallet),
    PLANS("Plans & Partners", Icons.Default.WorkspacePremium),
    SUPPORT("Support & Social", Icons.Default.SupportAgent),
    SECURITY("Config & Security", Icons.Default.Tune)
}

@Composable
fun AdminMainScreen(
    admin: UserProfile,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val config by FleearnRepository.appConfig.collectAsState()
    var selectedCategory by remember { mutableStateOf(AdminNavCategory.DASHBOARD) }

    var showCreateModDialog by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showQuickConfigDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BentoSurfaceCard)
                    .statusBarsPadding()
            ) {
                // Main Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedCategory = AdminNavCategory.DASHBOARD }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandGold.copy(alpha = 0.2f))
                                .border(1.dp, BrandGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = BrandGold, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Fleministration",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = BrandGold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                VerifiedBadge(role = UserRole.ADMIN, size = 16)
                            }
                            Text(
                                text = "Root AX: ${admin.uid}",
                                fontSize = 11.sp,
                                color = BentoTextMuted
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showBroadcastDialog = true }, modifier = Modifier.size(38.dp)) {
                            Icon(Icons.Default.Campaign, contentDescription = "Broadcast", tint = BrandPurple, modifier = Modifier.size(20.dp))
                        }

                        IconButton(onClick = { showQuickConfigDialog = true }, modifier = Modifier.size(38.dp)) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = BrandCyan, modifier = Modifier.size(20.dp))
                        }

                        IconButton(
                            onClick = {
                                FleearnRepository.logout()
                                onLogout()
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = BrandRed, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Scrollable Category Navigation Bar
                ScrollableTabRow(
                    selectedTabIndex = selectedCategory.ordinal,
                    edgePadding = 12.dp,
                    containerColor = BentoSurface,
                    contentColor = BrandCyan,
                    divider = { HorizontalDivider(color = BentoBorderSubtle) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AdminNavCategory.values().forEach { category ->
                        val isSelected = selectedCategory == category
                        Tab(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        category.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) BrandCyan else BentoTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = category.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) BrandCyan else BentoTextMuted
                                    )
                                }
                            }
                        )
                    }
                }
            }
        },
        containerColor = BentoBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when (selectedCategory) {
                AdminNavCategory.DASHBOARD -> {
                    AdminDashboardTab(
                        onNavigateTab = { tabName ->
                            when (tabName) {
                                "Users" -> selectedCategory = AdminNavCategory.USERS
                                "Publishers" -> selectedCategory = AdminNavCategory.PUBLISHERS
                                "Moderators" -> selectedCategory = AdminNavCategory.MODERATORS
                                "Jobs" -> selectedCategory = AdminNavCategory.JOBS
                                "Ads" -> selectedCategory = AdminNavCategory.JOBS
                                "Products" -> selectedCategory = AdminNavCategory.PRODUCTS
                                "Finance" -> selectedCategory = AdminNavCategory.FINANCE
                                "Support" -> selectedCategory = AdminNavCategory.SUPPORT
                                "Security" -> selectedCategory = AdminNavCategory.SECURITY
                                else -> {}
                            }
                        },
                        onOpenCreateMod = { showCreateModDialog = true },
                        onOpenConfig = { selectedCategory = AdminNavCategory.SECURITY },
                        onOpenBroadcast = { showBroadcastDialog = true }
                    )
                }
                AdminNavCategory.USERS -> {
                    AdminUsersAndPublishersTab(isPublishersOnly = false)
                }
                AdminNavCategory.PUBLISHERS -> {
                    AdminUsersAndPublishersTab(isPublishersOnly = true)
                }
                AdminNavCategory.MODERATORS -> {
                    AdminModeratorTab(
                        onOpenCreateMod = { showCreateModDialog = true }
                    )
                }
                AdminNavCategory.JOBS -> {
                    AdminJobsAndContentTab()
                }
                AdminNavCategory.PRODUCTS -> {
                    AdminProductsAndServicesTab()
                }
                AdminNavCategory.FINANCE -> {
                    AdminFinanceAndFeesTab()
                }
                AdminNavCategory.PLANS -> {
                    AdminPlansAndPartnersTab()
                }
                AdminNavCategory.SUPPORT -> {
                    AdminSupportAndSocialTab()
                }
                AdminNavCategory.SECURITY -> {
                    AdminConfigAndSecurityTab(
                        onLogout = {
                            FleearnRepository.logout()
                            onLogout()
                        }
                    )
                }
            }
        }
    }

    // Create Moderator Dialog
    if (showCreateModDialog) {
        CreateModeratorDialog(
            onDismiss = { showCreateModDialog = false },
            onSuccess = { uid, pin ->
                showCreateModDialog = false
                showSuccessDialog = "Moderator Created" to "New Moderator Account Successfully Created!\n\nUID: $uid\nPIN: $pin\n\nPlease share these credentials securely with the officer."
            }
        )
    }

    // Broadcast Notification Dialog
    if (showBroadcastDialog) {
        AdminBroadcastDialog(
            onDismiss = { showBroadcastDialog = false },
            onSend = { title, msg, target ->
                FleearnRepository.sendBroadcastNotification(title, msg, target)
                showBroadcastDialog = false
                Toast.makeText(context, "Broadcast sent to $target", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Quick Config Dialog
    if (showQuickConfigDialog) {
        AdminConfigDialog(
            config = config,
            onDismiss = { showQuickConfigDialog = false },
            onSave = { bKash, nagad, pin ->
                val updated = config.copy(
                    bKashNumber = bKash,
                    nagadNumber = nagad,
                    courseDailyPin = pin
                )
                FleearnRepository.updateAppConfig(updated)
                showQuickConfigDialog = false
                showSuccessDialog = "Config Synchronized" to "Official gateway numbers and daily course PIN have been updated across the Fleearn."
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
}

@Composable
fun AdminBroadcastDialog(
    onDismiss: () -> Unit,
    onSend: (title: String, message: String, target: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedTarget by remember { mutableStateOf("ALL") }

    val targets = listOf("ALL", "USERS", "PUBLISHERS", "MODERATORS")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Broadcast Global Announcement",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Text("Send high priority instant notifications to Fleearn tiers.", fontSize = 11.sp, color = BentoTextMuted)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    targets.forEach { target ->
                        val isSelected = selectedTarget == target
                        Button(
                            onClick = { selectedTarget = target },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) BrandPurple else BentoSurface,
                                contentColor = if (isSelected) Color.White else BentoTextMuted
                            ),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                        ) {
                            Text(target, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Announcement Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message Body") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && message.isNotBlank()) {
                                onSend(title, message, selectedTarget)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Send Broadcast", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateModeratorDialog(
    onDismiss: () -> Unit,
    onSuccess: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("12345678") }
    var error by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Generate New Moderator Account (MX)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandRed
                )
                Text(
                    text = "Generates a unique MX prefixed UID with customized 8-digit PIN.",
                    fontSize = 11.sp,
                    color = BentoTextMuted
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Moderator Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Official Mobile Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 8) pin = it },
                    label = { Text("8-Digit Secure PIN") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (error.isNotBlank()) {
                    Text(error, color = BrandRed, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || mobile.isBlank() || pin.length != 8) {
                                error = "Please provide name, mobile, and 8-digit PIN"
                                return@Button
                            }
                            val newUid = FleearnRepository.generateModerator(name, mobile, pin)
                            onSuccess(newUid, pin)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                    ) {
                        Text("Generate MX Officer", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminConfigDialog(
    config: SystemAppConfig,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var bKash by remember { mutableStateOf(config.bKashNumber) }
    var nagad by remember { mutableStateOf(config.nagadNumber) }
    var coursePin by remember { mutableStateOf(config.courseDailyPin) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Fleearn Quick Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandCyan)

                OutlinedTextField(value = bKash, onValueChange = { bKash = it }, label = { Text("Official bKash Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = nagad, onValueChange = { nagad = it }, label = { Text("Official Nagad Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = coursePin, onValueChange = { coursePin = it }, label = { Text("Daily Course PIN") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(bKash, nagad, coursePin) }, colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)) {
                        Text("Save Settings", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
