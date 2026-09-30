package com.example.ui.user

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.UserPlan
import com.example.data.model.UserProfile
import com.example.data.repository.FleearnRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun UserSettingsScreen(
    user: UserProfile,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by FleearnRepository.currentUser.collectAsState()
    val appConfig by FleearnRepository.appConfig.collectAsState()
    val themeState = LocalThemeState.current
    val langManager = LanguageManager.LocalLanguage.current
    val u = currentUser ?: user

    var showLanguagePicker by remember { mutableStateOf(false) }
    var showProfileModal by remember { mutableStateOf(false) }
    var showEventsModal by remember { mutableStateOf(false) }
    var showSupportModal by remember { mutableStateOf(false) }
    var showPartnerModal by remember { mutableStateOf(false) }
    var showPlansModal by remember { mutableStateOf(false) }
    var showRateUsModal by remember { mutableStateOf(false) }
    var showReportModal by remember { mutableStateOf(false) }
    var showDeleteAccountModal by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // Top bar
        item {
            Text(
                text = "Fleearn Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Profile summary card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showProfileModal = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = u.profilePhotoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(u.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (u.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                VerifiedBadge(role = u.role)
                            }
                        }
                        Text("UID: ${u.uid} • ${u.country}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Active Plan: ${u.plan.displayName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BrandCyan)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Preferences Group
        item {
            Text("Preferences & Interface", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Dark/Light Theme Switch
        item {
            SettingsRow(
                title = if (themeState.isDarkTheme) "Dark Theme" else "Light Theme",
                subtitle = "Toggle Fleearn color palette",
                icon = if (themeState.isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                actionContent = {
                    Switch(
                        checked = themeState.isDarkTheme,
                        onCheckedChange = { themeState.isDarkTheme = it }
                    )
                }
            )
        }

        // Language Picker
        item {
            SettingsRow(
                title = "Language",
                subtitle = "Current: ${langManager.currentLanguage.displayName} (${langManager.currentLanguage.nativeName})",
                icon = Icons.Default.Translate,
                onClick = { showLanguagePicker = true }
            )
        }

        // Download Center
        item {
            SettingsRow(
                title = "Download Center",
                subtitle = "Official app media & offline resources",
                icon = Icons.Default.Download,
                onClick = { showSuccessDialog = "Download Center" to "All Fleearn offline resources are cached and up-to-date." }
            )
        }

        // Services & Events Group
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Fleearn Programs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
        }

        item {
            SettingsRow(
                title = "Plan Upgrade & Renew",
                subtitle = "45-Day Basic, Standard, Premium tiers",
                icon = Icons.Default.Stars,
                onClick = { showPlansModal = true }
            )
        }

        item {
            SettingsRow(
                title = "Partner Program",
                subtitle = "Eligibility checker & official application",
                icon = Icons.Default.Handshake,
                onClick = { showPartnerModal = true }
            )
        }

        item {
            SettingsRow(
                title = "Fleearn Events (2026-2050)",
                subtitle = "Calendar roadmaps and global webinars",
                icon = Icons.Default.Event,
                onClick = { showEventsModal = true }
            )
        }

        item {
            SettingsRow(
                title = "AI Support & Ticket Desk",
                subtitle = "24/7 AI chat and live moderator support",
                icon = Icons.Default.SupportAgent,
                onClick = { showSupportModal = true }
            )
        }

        item {
            SettingsRow(
                title = "More From Us",
                subtitle = "Explore Fleearn affiliate apps & tools",
                icon = Icons.Default.Apps,
                onClick = {
                    InAppBrowserManager.openUrl(appConfig.moreFromUsUrl, "More From Fleearn")
                }
            )
        }

        // Feedback & Security
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Feedback & Safety", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
        }

        item {
            SettingsRow(
                title = "Rate Us",
                subtitle = "Share feedback with administration",
                icon = Icons.Default.StarRate,
                onClick = { showRateUsModal = true }
            )
        }

        item {
            SettingsRow(
                title = "Report an Issue",
                subtitle = "Send direct report to FleUra moderators",
                icon = Icons.Default.ReportProblem,
                onClick = { showReportModal = true }
            )
        }

        item {
            SettingsRow(
                title = "Permanent Account Delete",
                subtitle = "Erase all personal data and UID",
                icon = Icons.Default.DeleteForever,
                isDanger = true,
                onClick = { showDeleteAccountModal = true }
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    FleearnRepository.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("settings_logout_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = BrandRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out of Session", fontWeight = FontWeight.Bold, color = BrandRed)
            }
        }
    }

    // Modals
    if (showLanguagePicker) {
        LanguagePickerDialog(
            onDismiss = { showLanguagePicker = false },
            onLanguageSelected = {
                langManager.currentLanguage = it
                showLanguagePicker = false
            }
        )
    }

    if (showPlansModal) {
        PlansUpgradeDialog(
            user = u,
            onDismiss = { showPlansModal = false },
            onSuccess = { msg ->
                showPlansModal = false
                showSuccessDialog = "Plan Activated" to msg
            }
        )
    }

    if (showPartnerModal) {
        PartnerProgramDialog(
            user = u,
            onDismiss = { showPartnerModal = false },
            onSuccess = { msg ->
                showPartnerModal = false
                showSuccessDialog = "Partner Program" to msg
            }
        )
    }

    if (showEventsModal) {
        EventsDialog(onDismiss = { showEventsModal = false })
    }

    if (showSupportModal) {
        SupportTicketDialog(
            user = u,
            onDismiss = { showSupportModal = false },
            onSuccess = { msg ->
                showSupportModal = false
                showSuccessDialog = "Support Ticket" to msg
            }
        )
    }

    if (showRateUsModal) {
        RateUsDialog(
            onDismiss = { showRateUsModal = false },
            onSuccess = {
                showRateUsModal = false
                showSuccessDialog = "Thank You" to "Your 5-star rating has been recorded by Administration."
            }
        )
    }

    if (showReportModal) {
        ReportDialog(
            onDismiss = { showReportModal = false },
            onSuccess = { msg ->
                showReportModal = false
                showSuccessDialog = "Report Received" to msg
            }
        )
    }

    if (showProfileModal) {
        UserProfileDetailsDialog(user = u, onDismiss = { showProfileModal = false })
    }

    if (showDeleteAccountModal) {
        DeleteAccountDialog(
            user = u,
            onDismiss = { showDeleteAccountModal = false },
            onDeleted = {
                showDeleteAccountModal = false
                onLogout()
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
fun SettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isDanger: Boolean = false,
    onClick: (() -> Unit)? = null,
    actionContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDanger) BrandRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDanger) BrandRed else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (isDanger) BrandRed else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (actionContent != null) {
                actionContent()
            } else if (onClick != null) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun LanguagePickerDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.8f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Language (20+)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AppLanguage.values().toList()) { lang ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onLanguageSelected(lang) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(lang.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(lang.nativeName, fontSize = 13.sp, color = BrandCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlansUpgradeDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val plans = listOf(UserPlan.BASIC, UserPlan.STANDARD, UserPlan.PREMIUM)
    var error by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.98f).fillMaxHeight(0.88f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("45-Day Plan Upgrade", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }
                Text("Your Balance: ${user.balance} FCT", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(plans) { plan ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (plan == UserPlan.PREMIUM) BrandPurple.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(plan.displayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("${plan.priceFct} FCT / 45 Days", fontWeight = FontWeight.Bold, color = BrandCyan, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("• Daily usage: ${plan.dailyUsageMinutes / 60}h ${plan.dailyUsageMinutes % 60}m", fontSize = 11.sp)
                                Text("• Withdraw limits: Daily ${plan.maxDailyWithdraw} FCT / Monthly ${plan.maxMonthlyWithdraw} FCT", fontSize = 11.sp)
                                Text("• Features: Inbox chat, Priority support, Verified badge", fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val res = FleearnRepository.upgradeUserPlan(plan)
                                            if (res.first) onSuccess(res.second) else error = res.second
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Activate ${plan.displayName}")
                                }
                            }
                        }
                    }
                    if (error.isNotBlank()) {
                        item { Text(error, color = BrandRed, fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}

@Composable
fun PartnerProgramDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var error by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Partner Program Eligibility", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text("• Minimum age: 15 years\n• Total withdrawal: 500+ FCT\n• Total earnings: 1,000+ FCT\n• Active streak: 30+ days continuous\n• Continuation Fee: 44.00 FCT", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(14.dp))
                if (error.isNotBlank()) {
                    Text(error, color = BrandRed, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val res = FleearnRepository.applyPartnerProgram()
                            if (res.first) onSuccess(res.second) else error = res.second
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verify Eligibility & Apply (44 FCT)")
                }
            }
        }
    }
}

@Composable
fun EventsDialog(onDismiss: () -> Unit) {
    val events by FleearnRepository.events.collectAsState()
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.75f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Calendar Events (2026-2050)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(events) { evt ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(evt.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Date: ${evt.dateString}", fontSize = 11.sp, color = BrandCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(evt.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupportTicketDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val appConfig by FleearnRepository.appConfig.collectAsState()
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var whatsApp by remember { mutableStateOf(user.mobile) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Support & Ticket Desk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        InAppBrowserManager.openUrl(appConfig.aiSupportUrl, "24/7 AI Support Agent")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Open 24/7 AI Support Agent")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Or Submit Moderator Ticket (FleUra):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Problem Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = details, onValueChange = { details = it }, label = { Text("Problem Details") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = whatsApp, onValueChange = { whatsApp = it }, label = { Text("WhatsApp Contact Number") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank() && details.isNotBlank()) {
                            val id = FleearnRepository.submitSupportTicket(title, details, whatsApp)
                            onSuccess("Support Ticket ($id) submitted with Running status.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Ticket to Moderators")
                }
            }
        }
    }
}

@Composable
fun RateUsDialog(onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var stars by remember { mutableStateOf(5) }
    var note by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Rate Fleearn Fleearn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { s ->
                        IconButton(onClick = { stars = s }) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = if (s <= stars) BrandGold else Color.Gray, modifier = Modifier.size(32.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Optional feedback note...") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(14.dp))
                Button(onClick = {
                    FleearnRepository.submitRating(stars, note)
                    onSuccess()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Submit 5-Star Rating")
                }
            }
        }
    }
}

@Composable
fun ReportDialog(onDismiss: () -> Unit, onSuccess: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Report an Issue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Issue Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Detailed Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                Spacer(modifier = Modifier.height(14.dp))
                Button(onClick = {
                    if (title.isNotBlank()) {
                        FleearnRepository.submitReport(title, desc)
                        onSuccess("Sorry. We have received your report and will try to resolve your issue as soon as possible.")
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Send Report to FleUra")
                }
            }
        }
    }
}

@Composable
fun UserProfileDetailsDialog(user: UserProfile, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("User Profile Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text("• Name: ${user.name}", fontSize = 13.sp)
                Text("• UID: ${user.uid}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                Text("• Mobile: ${user.mobile}", fontSize = 13.sp)
                Text("• Age / Gender: ${user.age} / ${user.gender}", fontSize = 13.sp)
                Text("• Country / Server: ${user.country} (${user.server})", fontSize = 13.sp)
                Text("• Team Name: ${user.teamName.ifEmpty { "None" }}", fontSize = 13.sp)
                Text("• Referral Code: ${user.uid}", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
            }
        }
    }
}

@Composable
fun DeleteAccountDialog(user: UserProfile, onDismiss: () -> Unit, onDeleted: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var pin by remember { mutableStateOf("") }
    var ans1 by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Permanent Account Deletion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandRed)
                Spacer(modifier = Modifier.height(8.dp))
                Text("This action is permanent and cannot be undone. Enter your 8-digit PIN and your 1st security answer to confirm.", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 8) pin = it },
                    label = { Text("8-Digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ans1,
                    onValueChange = { ans1 = it },
                    label = { Text("1st Security Answer (Memory you remember most)") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotBlank()) {
                    Text(error, color = BrandRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val res = FleearnRepository.deleteAccount(user.uid, pin, ans1)
                            if (res.first) onDeleted() else error = res.second
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("Permanently Delete My Account", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
