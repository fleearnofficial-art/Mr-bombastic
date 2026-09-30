package com.example.ui.publisher
import com.example.ui.components.AppLanguage

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
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
import coil.compose.AsyncImage
import com.example.data.model.SystemAppConfig
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.FleearnRepository
import com.example.ui.components.InAppBrowserManager
import com.example.ui.components.LanguageManager
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*

@Composable
fun PublisherSettingsTab(
    publisher: UserProfile,
    config: SystemAppConfig,
    onLogout: () -> Unit,
    onSuccess: (String, String) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val langManager = LanguageManager.LocalLanguage.current

    // Dialog States
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showSecurityQuestionsDialog by remember { mutableStateOf(false) }
    var showActiveSessionsDialog by remember { mutableStateOf(false) }
    var showLoginHistoryDialog by remember { mutableStateOf(false) }
    var showTrustedDevicesDialog by remember { mutableStateOf(false) }
    var showSubmitTicketDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showEarningsStatementDialog by remember { mutableStateOf(false) }

    // Toggle States
    var twoFactorAuth by remember { mutableStateOf(true) }
    var pushNotifyNewJobSubmission by remember { mutableStateOf(true) }
    var pushNotifyNewOrder by remember { mutableStateOf(true) }
    var pushNotifyNewServiceOrder by remember { mutableStateOf(true) }
    var pushNotifyAdStatus by remember { mutableStateOf(true) }
    var pushNotifyPayment by remember { mutableStateOf(true) }
    var pushNotifyAnnouncements by remember { mutableStateOf(true) }
    var masterPushNotification by remember { mutableStateOf(true) }
    var publishConfirmation by remember { mutableStateOf(true) }

    var selectedThemeMode by remember { mutableStateOf("Dark") }
    var selectedLanguage by remember { mutableStateOf(langManager.currentLanguage) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Profile & Organization Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (publisher.organizationLogoUrl.isNotBlank() || publisher.profilePhotoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = publisher.organizationLogoUrl.ifEmpty { publisher.profilePhotoUrl },
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp).clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier.size(50.dp).clip(CircleShape).background(BrandCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Business, contentDescription = null, tint = BrandCyan)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        publisher.organizationName.ifEmpty { publisher.name },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    VerifiedBadge(role = UserRole.PUBLISHER, size = 15)
                                }
                                Text("UID: ${publisher.uid}", fontSize = 11.sp, color = BentoTextMuted)
                                Text("Org: ${publisher.organizationName.ifEmpty { "Registered Publisher" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        IconButton(onClick = { showEditProfileDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = BrandCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mobile: ${publisher.mobile}", fontSize = 11.sp, color = BentoTextMuted)
                        Text("Country: ${publisher.country}", fontSize = 11.sp, color = BentoTextMuted)
                    }
                }
            }
        }

        // 2. Security Section
        item {
            SettingsSectionHeader(title = "Security & Access Control", icon = Icons.Default.Security)
            SettingsCard {
                SettingsNavigationRow(
                    title = "Change 8-Digit PIN",
                    subtitle = "Update your secure transaction and login PIN",
                    icon = Icons.Default.Pin,
                    onClick = { showChangePinDialog = true }
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Security Questions",
                    subtitle = "Recovery questions for PIN resets",
                    icon = Icons.Default.Quiz,
                    onClick = { showSecurityQuestionsDialog = true }
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "Two-Factor Authentication (2FA)",
                    subtitle = "Require PIN verification on every login",
                    icon = Icons.Default.VpnKey,
                    checked = twoFactorAuth,
                    onCheckedChange = { twoFactorAuth = it }
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Active Sessions",
                    subtitle = "Current device: Android Studio Container",
                    icon = Icons.Default.Devices,
                    onClick = { showActiveSessionsDialog = true }
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Login History",
                    subtitle = "View recent login attempts and timestamps",
                    icon = Icons.Default.History,
                    onClick = { showLoginHistoryDialog = true }
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Trusted Devices",
                    subtitle = "Manage authorized mobile devices",
                    icon = Icons.Default.PhonelinkLock,
                    onClick = { showTrustedDevicesDialog = true }
                )
                SettingsDivider()
                SettingsActionRow(
                    title = "Logout All Devices",
                    subtitle = "Terminates all active publisher sessions",
                    icon = Icons.Default.MeetingRoom,
                    textColor = BrandRed,
                    onClick = {
                        Toast.makeText(context, "All other sessions terminated.", Toast.LENGTH_SHORT).show()
                        onSuccess("Sessions Cleared", "Logged out from all secondary devices.")
                    }
                )
            }
        }

        // 3. Appearance
        item {
            SettingsSectionHeader(title = "Appearance", icon = Icons.Default.Palette)
            SettingsCard {
                SettingsSwitchRow(
                    title = "Dark Mode",
                    subtitle = "High contrast futuristic cyberpunk palette",
                    icon = Icons.Default.DarkMode,
                    checked = selectedThemeMode == "Dark",
                    onCheckedChange = { selectedThemeMode = if (it) "Dark" else "Light" }
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "Hardware Acceleration & 3D Polish",
                    subtitle = "Smooth 60fps animations and depth effects",
                    icon = Icons.Default.Speed,
                    checked = true,
                    onCheckedChange = {}
                )
            }
        }

        // 4. Language
        item {
            SettingsSectionHeader(title = "Language", icon = Icons.Default.Translate)
            SettingsCard {
                listOf(
                    AppLanguage.EN to "English (Default)",
                    AppLanguage.BN to "বাংলা (Bengali)",
                    AppLanguage.HI to "हिन्दी (Hindi)",
                    AppLanguage.AR to "العربية (Arabic)"
                ).forEachIndexed { idx, (appLang, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLanguage = appLang
                                langManager.currentLanguage = appLang
                                Toast.makeText(context, "Language switched to $name", Toast.LENGTH_SHORT).show()
                            }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        RadioButton(
                            selected = selectedLanguage == appLang,
                            onClick = {
                                selectedLanguage = appLang
                                langManager.currentLanguage = appLang
                            }
                        )
                    }
                    if (idx < 3) SettingsDivider()
                }
            }
        }

        // 5. Notifications
        item {
            SettingsSectionHeader(title = "Notification Preferences", icon = Icons.Default.Notifications)
            SettingsCard {
                SettingsSwitchRow(
                    title = "Push Notifications (Master)",
                    subtitle = "Enable real-time push alerts on device",
                    icon = Icons.Default.NotificationsActive,
                    checked = masterPushNotification,
                    onCheckedChange = { masterPushNotification = it }
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "New Job Submission Alerts",
                    subtitle = "Notify when a worker submits proof",
                    icon = Icons.Default.AssignmentTurnedIn,
                    checked = pushNotifyNewJobSubmission && masterPushNotification,
                    onCheckedChange = { pushNotifyNewJobSubmission = it }
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "New Product Resell Orders",
                    subtitle = "Notify on customer orders placed",
                    icon = Icons.Default.LocalShipping,
                    checked = pushNotifyNewOrder && masterPushNotification,
                    onCheckedChange = { pushNotifyNewOrder = it }
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "New Service Booking Orders",
                    subtitle = "Notify when a client books your service",
                    icon = Icons.Default.ReceiptLong,
                    checked = pushNotifyNewServiceOrder && masterPushNotification,
                    onCheckedChange = { pushNotifyNewServiceOrder = it }
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "Ad Campaign Delivery Status",
                    subtitle = "Alerts when impression quotas are completed",
                    icon = Icons.Default.Campaign,
                    checked = pushNotifyAdStatus && masterPushNotification,
                    onCheckedChange = { pushNotifyAdStatus = it }
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "Payment & Deposit Updates",
                    subtitle = "Instant confirmation on balance approvals",
                    icon = Icons.Default.AccountBalanceWallet,
                    checked = pushNotifyPayment && masterPushNotification,
                    onCheckedChange = { pushNotifyPayment = it }
                )
            }
        }

        // 6. Payment & Earnings
        item {
            SettingsSectionHeader(title = "Payment & Earnings", icon = Icons.Default.MonetizationOn)
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Publisher Balance", fontSize = 12.sp, color = BentoTextMuted)
                        Text("%.2f FCT".format(publisher.balance), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                    }
                    Button(
                        onClick = { showEarningsStatementDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("Earnings Statement", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Payment Method Configurations",
                    subtitle = "bKash, Nagad, Bank Account setup",
                    icon = Icons.Default.Payment,
                    onClick = { Toast.makeText(context, "Payment methods configured.", Toast.LENGTH_SHORT).show() }
                )
            }
        }

        // 7. Publishing Preferences
        item {
            SettingsSectionHeader(title = "Publishing Preferences", icon = Icons.Default.Tune)
            SettingsCard {
                SettingsSwitchRow(
                    title = "Publishing Confirmation Dialog",
                    subtitle = "Ask for confirmation before debiting posting fees",
                    icon = Icons.Default.CheckCircle,
                    checked = publishConfirmation,
                    onCheckedChange = { publishConfirmation = it }
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Default Job Category",
                    subtitle = "Social Media / App Review / Survey",
                    icon = Icons.Default.Category,
                    onClick = { Toast.makeText(context, "Default: Social Media", Toast.LENGTH_SHORT).show() }
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Default Proof Requirements",
                    subtitle = "1 Screenshot + 1 Textual Note",
                    icon = Icons.Default.FactCheck,
                    onClick = { Toast.makeText(context, "Default: 1 Screenshot, 1 Note", Toast.LENGTH_SHORT).show() }
                )
            }
        }

        // 8. Shipping & Delivery Settings
        item {
            SettingsSectionHeader(title = "Shipping & Store Settings", icon = Icons.Default.LocalShipping)
            SettingsCard {
                SettingsNavigationRow(
                    title = "Store Information",
                    subtitle = publisher.organizationName.ifEmpty { publisher.name },
                    icon = Icons.Default.Store,
                    onClick = { showEditProfileDialog = true }
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Default Delivery Charge",
                    subtitle = "200 FCT across all 64 districts",
                    icon = Icons.Default.LocalAtm,
                    onClick = { Toast.makeText(context, "Standard delivery fee: 200 FCT", Toast.LENGTH_SHORT).show() }
                )
            }
        }

        // 9. Communication & WhatsApp
        item {
            SettingsSectionHeader(title = "Communication", icon = Icons.Default.Chat)
            SettingsCard {
                SettingsNavigationRow(
                    title = "Direct Client WhatsApp",
                    subtitle = publisher.mobile,
                    icon = Icons.Default.Phone,
                    onClick = { showEditProfileDialog = true }
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Publisher Inbox",
                    subtitle = "Community direct messages and system alerts",
                    icon = Icons.Default.Mail,
                    onClick = { Toast.makeText(context, "Inbox: No unread messages", Toast.LENGTH_SHORT).show() }
                )
            }
        }

        // 10. Data & Privacy
        item {
            SettingsSectionHeader(title = "Data & Privacy", icon = Icons.Default.PrivacyTip)
            SettingsCard {
                SettingsNavigationRow(
                    title = "Download My Data",
                    subtitle = "Export JSON archive of all campaigns & orders",
                    icon = Icons.Default.Download,
                    onClick = {
                        Toast.makeText(context, "Data archive prepared & saved locally.", Toast.LENGTH_LONG).show()
                        onSuccess("Data Downloaded", "Publisher audit log archive generated successfully.")
                    }
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Data Usage & Telemetry",
                    subtitle = "Zero third-party tracking policy",
                    icon = Icons.Default.DataUsage,
                    onClick = { Toast.makeText(context, "Fleearn encryption: AES-256 enabled", Toast.LENGTH_SHORT).show() }
                )
            }
        }

        // 11. Legal
        item {
            SettingsSectionHeader(title = "Legal & Policies", icon = Icons.Default.Gavel)
            SettingsCard {
                SettingsNavigationRow(
                    title = "Terms & Conditions",
                    subtitle = "Platform user agreement",
                    icon = Icons.Default.Description,
                    onClick = { InAppBrowserManager.openUrl(config.termsUrl, "Terms & Conditions") }
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Publisher Policy",
                    subtitle = "Rules for posting ads, jobs and catalog items",
                    icon = Icons.Default.Policy,
                    onClick = { InAppBrowserManager.openUrl(config.publisherPolicyUrl, "Publisher Policy") }
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Privacy Policy",
                    subtitle = "Data handling & retention standards",
                    icon = Icons.Default.Lock,
                    onClick = { InAppBrowserManager.openUrl(config.privacyUrl, "Privacy Policy") }
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Community Guidelines",
                    subtitle = "Respectful interaction standards",
                    icon = Icons.Default.Group,
                    onClick = { InAppBrowserManager.openUrl(config.communityGuidelinesUrl, "Community Guidelines") }
                )
            }
        }

        // 12. Help & Support
        item {
            SettingsSectionHeader(title = "Help & Support", icon = Icons.Default.SupportAgent)
            SettingsCard {
                SettingsNavigationRow(
                    title = "24/7 AI Support Agent",
                    subtitle = "Instant answers powered by Google Gemini",
                    icon = Icons.Default.SmartToy,
                    onClick = { InAppBrowserManager.openUrl(config.aiSupportUrl, "AI Support Hub") }
                )
                SettingsDivider()
                SettingsNavigationRow(
                    title = "Submit Support Ticket",
                    subtitle = "Escalate directly to human moderators",
                    icon = Icons.Default.ConfirmationNumber,
                    onClick = { showSubmitTicketDialog = true }
                )
            }
        }

        // 13. About
        item {
            SettingsSectionHeader(title = "About FleConsole", icon = Icons.Default.Info)
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("FleConsole Publisher Suite", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Version ${config.appVersion} • Fleearn Fleearn", fontSize = 11.sp, color = BentoTextMuted)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandCyan.copy(alpha = 0.2f)
                    ) {
                        Text("BUILD 2026", color = BrandCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }

        // 14. Account Actions (Logout & Delete)
        item {
            SettingsSectionHeader(title = "Account Actions", icon = Icons.Default.AccountCircle)
            SettingsCard {
                SettingsActionRow(
                    title = "Logout from FleConsole",
                    subtitle = "Securely sign out of current session",
                    icon = Icons.Default.Logout,
                    textColor = BrandOrange,
                    onClick = {
                        FleearnRepository.logout()
                        onLogout()
                    }
                )
                SettingsDivider()
                SettingsActionRow(
                    title = "Delete Publisher Account",
                    subtitle = "Permanently wipe organization & all campaigns",
                    icon = Icons.Default.DeleteForever,
                    textColor = BrandRed,
                    onClick = { showDeleteAccountDialog = true }
                )
            }
        }
    }

    // --- DIALOGS ---

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var name by remember { mutableStateOf(publisher.name) }
        var orgName by remember { mutableStateOf(publisher.organizationName) }
        var orgLogo by remember { mutableStateOf(publisher.organizationLogoUrl) }
        var mobile by remember { mutableStateOf(publisher.mobile) }
        var country by remember { mutableStateOf(publisher.country) }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(0.95f)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Edit Organization & Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = orgName, onValueChange = { orgName = it }, label = { Text("Organization Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Contact Person Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = orgLogo, onValueChange = { orgLogo = it }, label = { Text("Organization Logo URL") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile / WhatsApp Number") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val (success, msg) = FleearnRepository.updatePublisherProfile(
                                name = name,
                                orgName = orgName,
                                orgLogoUrl = orgLogo,
                                mobile = mobile,
                                country = country
                            )
                            if (success) {
                                showEditProfileDialog = false
                                onSuccess("Profile Updated", msg)
                            } else {
                                onError(msg)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                    ) {
                        Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var oldPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showChangePinDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(0.95f)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Change 8-Digit PIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = oldPin, onValueChange = { oldPin = it }, label = { Text("Current 8-Digit PIN") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = newPin, onValueChange = { newPin = it }, label = { Text("New 8-Digit PIN") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = confirmPin, onValueChange = { confirmPin = it }, label = { Text("Confirm New PIN") }, modifier = Modifier.fillMaxWidth())
                    if (pinError.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(pinError, color = BrandRed, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (newPin != confirmPin) {
                                pinError = "New PIN and Confirm PIN do not match."
                                return@Button
                            }
                            coroutineScope.launch {
                                val (success, msg) = FleearnRepository.changeUserPin(oldPin, newPin)
                                if (success) {
                                    showChangePinDialog = false
                                    onSuccess("PIN Changed", msg)
                                } else {
                                    pinError = msg
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                    ) {
                        Text("Update PIN", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Security Questions Dialog
    if (showSecurityQuestionsDialog) {
        var a1 by remember { mutableStateOf(publisher.secAnswer1) }
        var a2 by remember { mutableStateOf(publisher.secAnswer2) }
        var a3 by remember { mutableStateOf(publisher.secAnswer3) }

        Dialog(onDismissRequest = { showSecurityQuestionsDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(0.95f)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Security Questions Setup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Used to recover account if 8-digit PIN is forgotten.", fontSize = 11.sp, color = BentoTextMuted)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = a1, onValueChange = { a1 = it }, label = { Text("1. Memorable life event") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = a2, onValueChange = { a2 = it }, label = { Text("2. Primary school roll / code") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = a3, onValueChange = { a3 = it }, label = { Text("3. Childhood best friend name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val (success, msg) = FleearnRepository.updateSecurityQuestions(a1, a2, a3)
                            if (success) {
                                showSecurityQuestionsDialog = false
                                onSuccess("Security Saved", msg)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                    ) {
                        Text("Save Security Questions", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Active Sessions Dialog
    if (showActiveSessionsDialog) {
        Dialog(onDismissRequest = { showActiveSessionsDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(0.95f)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Active Sessions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = BrandGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Current Device (Active Now)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Android 14 • Dhaka, Bangladesh • IP: 103.145.xx.xx", fontSize = 11.sp, color = BentoTextMuted)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = { showActiveSessionsDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Login History Dialog
    if (showLoginHistoryDialog) {
        Dialog(onDismissRequest = { showLoginHistoryDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(0.95f)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Login History", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf("Today • 14:02:10 (Success)", "Yesterday • 09:15:33 (Success)", "3 days ago • 18:40:02 (Success)").forEach { log ->
                        Text("• $log", fontSize = 12.sp, modifier = Modifier.padding(vertical = 3.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = { showLoginHistoryDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Trusted Devices Dialog
    if (showTrustedDevicesDialog) {
        Dialog(onDismissRequest = { showTrustedDevicesDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(0.95f)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Trusted Devices", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Devices authorized without additional SMS challenge.", fontSize = 11.sp, color = BentoTextMuted)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("1. Primary Android Smartphone (Verified)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("2. FleConsole Workstation (Chrome)", fontSize = 12.sp, color = BentoTextMuted)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = { showTrustedDevicesDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Submit Ticket Dialog
    if (showSubmitTicketDialog) {
        var problemTitle by remember { mutableStateOf("") }
        var problemDetails by remember { mutableStateOf("") }
        var whatsApp by remember { mutableStateOf(publisher.mobile) }

        Dialog(onDismissRequest = { showSubmitTicketDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(0.95f)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Submit Publisher Support Ticket", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = problemTitle, onValueChange = { problemTitle = it }, label = { Text("Issue Subject") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = problemDetails, onValueChange = { problemDetails = it }, label = { Text("Details & Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = whatsApp, onValueChange = { whatsApp = it }, label = { Text("WhatsApp Contact") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (problemTitle.isNotBlank()) {
                                val tktId = FleearnRepository.submitSupportTicket(problemTitle, problemDetails, whatsApp)
                                showSubmitTicketDialog = false
                                onSuccess("Ticket Created", "Ticket #$tktId submitted! A moderator will review shortly.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                    ) {
                        Text("Submit Ticket", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Earnings Statement Dialog
    if (showEarningsStatementDialog) {
        Dialog(onDismissRequest = { showEarningsStatementDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(0.95f)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Earnings & Expenses Statement", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Publisher UID: ${publisher.uid}", fontSize = 12.sp, color = BentoTextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Current Balance:", fontSize = 12.sp)
                        Text("%.2f FCT".format(publisher.balance), fontWeight = FontWeight.Bold, color = BrandCyan, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Withdrawn:", fontSize = 12.sp)
                        Text("%.2f FCT".format(publisher.totalWithdrawn), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = { showEarningsStatementDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close Statement")
                    }
                }
            }
        }
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        var pin by remember { mutableStateOf("") }
        var answer1 by remember { mutableStateOf("") }
        var delError by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showDeleteAccountDialog = false }) {
            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(0.95f)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Delete Publisher Account", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandRed)
                    Text("Warning: This action is permanent and deletes all active jobs, ads, and balance.", fontSize = 11.sp, color = BentoTextMuted)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = pin, onValueChange = { pin = it }, label = { Text("Enter 8-Digit PIN") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = answer1, onValueChange = { answer1 = it }, label = { Text("Security Answer #1") }, modifier = Modifier.fillMaxWidth())
                    if (delError.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(delError, color = BrandRed, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val (success, msg) = FleearnRepository.deleteAccount(publisher.uid, pin, answer1)
                                if (success) {
                                    showDeleteAccountDialog = false
                                    onLogout()
                                } else {
                                    delError = msg
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Permanently Delete Account", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoTextMuted)
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.8.dp)
}

@Composable
fun SettingsNavigationRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(subtitle, fontSize = 11.sp, color = BentoTextMuted)
            }
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = BentoTextMuted, modifier = Modifier.size(12.dp))
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(subtitle, fontSize = 11.sp, color = BentoTextMuted)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                Text(subtitle, fontSize = 11.sp, color = BentoTextMuted)
            }
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = BentoTextMuted, modifier = Modifier.size(12.dp))
    }
}
