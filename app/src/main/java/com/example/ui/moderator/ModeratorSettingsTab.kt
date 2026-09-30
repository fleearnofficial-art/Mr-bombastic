package com.example.ui.moderator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SystemAppConfig
import com.example.data.model.UserProfile
import com.example.data.repository.FleearnRepository
import com.example.ui.components.Success3DDialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ModeratorSettingsTab(
    moderator: UserProfile,
    config: SystemAppConfig,
    onLogout: () -> Unit
) {
    var showProfileDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
    ) {
        item {
            Text("Settings", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = BentoTextPrimary)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SettingsSectionHeader(title = "Appearance", icon = Icons.Default.Palette)
            SettingsOptionCard(title = "Theme (Dark/Light)", icon = Icons.Default.BrightnessMedium, onClick = {})
            SettingsOptionCard(title = "Language", icon = Icons.Default.Translate, onClick = {})
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SettingsSectionHeader(title = "Moderator Profile", icon = Icons.Default.Person)
            SettingsOptionCard(title = "Profile Information", subtitle = "View details and creation date", icon = Icons.Default.AccountCircle, onClick = { showProfileDialog = true })
            SettingsOptionCard(title = "Security & Access", icon = Icons.Default.Security, onClick = {})
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SettingsSectionHeader(title = "Financial", icon = Icons.Default.AccountBalanceWallet)
            SettingsOptionCard(title = "Withdraw Salary", subtitle = "Withdraw to bKash or Nagad (50 FCT fee)", icon = Icons.Default.Money, onClick = { showWithdrawDialog = true })
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SettingsSectionHeader(title = "History & Logs", icon = Icons.Default.History)
            SettingsOptionCard(title = "Support Handover History", icon = Icons.Default.Assignment, onClick = {})
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SettingsSectionHeader(title = "Fleearn Info", icon = Icons.Default.Info)
            SettingsOptionCard(title = "Events", icon = Icons.Default.Event, onClick = {})
            SettingsOptionCard(title = "About Us", subtitle = "Version ${config.appVersion}", icon = Icons.Default.Info, onClick = {})
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SettingsSectionHeader(title = "Account", icon = Icons.Default.Logout)
            SettingsOptionCard(
                title = "Logout",
                icon = Icons.Default.ExitToApp,
                onClick = onLogout,
                isDestructive = true
            )
        }
    }

    if (showProfileDialog) {
        Dialog(onDismissRequest = { showProfileDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Moderator Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Name: ${moderator.name}", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("UID: ${moderator.uid}", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Account Created: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())}", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { showProfileDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close")
                    }
                }
            }
        }
    }

    if (showWithdrawDialog) {
        var withdrawMethod by remember { mutableStateOf("bKash") }
        var walletNumber by remember { mutableStateOf("") }
        var withdrawAmount by remember { mutableStateOf("") }
        var error by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showWithdrawDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Withdraw Salary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Available: ${moderator.balance} FCT", color = BentoTextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = withdrawMethod == "bKash",
                            onClick = { withdrawMethod = "bKash" },
                            label = { Text("bKash") }
                        )
                        FilterChip(
                            selected = withdrawMethod == "Nagad",
                            onClick = { withdrawMethod = "Nagad" },
                            label = { Text("Nagad") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = walletNumber,
                        onValueChange = { walletNumber = it },
                        label = { Text("Wallet Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it },
                        label = { Text("Amount (FCT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Flat 50 FCT withdrawal fee applies.", color = BrandRed, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    
                    if (error.isNotBlank()) {
                        Text(error, color = BrandRed, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showWithdrawDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            val amount = withdrawAmount.toDoubleOrNull() ?: 0.0
                            val totalNeeded = amount + 50.0
                            if (amount <= 0 || walletNumber.isBlank()) {
                                error = "Please enter valid amount and wallet number."
                            } else if (moderator.balance < totalNeeded) {
                                error = "Insufficient balance (need ${totalNeeded} FCT including 50 FCT fee)."
                            } else {
                                val (success, msg) = FleearnRepository.requestWithdraw(amount, withdrawMethod, walletNumber)
                                if (success) {
                                    showWithdrawDialog = false
                                    showSuccessDialog = "Withdrawal Requested" to msg
                                } else {
                                    error = msg
                                }
                            }
                        }) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }

    showSuccessDialog?.let { (t, m) ->
        Success3DDialog(title = t, message = m, onDismiss = { showSuccessDialog = null })
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = BrandRed, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandRed)
    }
}

@Composable
fun SettingsOptionCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = if (isDestructive) BrandRed else BentoTextPrimary
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = contentColor)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 12.sp, color = BentoTextSecondary)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BentoTextMuted)
        }
    }
}
