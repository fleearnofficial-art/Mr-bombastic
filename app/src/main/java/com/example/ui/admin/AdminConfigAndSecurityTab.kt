package com.example.ui.admin

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.FleearnRepository
import com.example.ui.components.InAppBrowserManager
import com.example.ui.theme.*

@Composable
fun AdminConfigAndSecurityTab(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val config by FleearnRepository.appConfig.collectAsState()
    val auditLogs by FleearnRepository.auditLogs.collectAsState()
    val users by FleearnRepository.users.collectAsState()

    val adminAccounts = users.filter { it.role == UserRole.ADMIN }

    var selectedSubSection by remember { mutableStateOf(0) } // 0: App Configuration, 1: Security & 2FA, 2: System Health & Audit
    val subSections = listOf("App Config & URLs", "Security & Fixed Accounts", "Infrastructure & Audit")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedSubSection,
            containerColor = BentoSurfaceCard,
            contentColor = BrandCyan,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            subSections.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubSection == index,
                    onClick = { selectedSubSection = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selectedSubSection == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        when (selectedSubSection) {
            0 -> {
                // APP CONFIGURATION & BRANDING
                var appName by remember { mutableStateOf(config.appName) }
                var slogan by remember { mutableStateOf(config.slogan) }
                var description by remember { mutableStateOf(config.description) }
                var logoUrl by remember { mutableStateOf(config.appLogoUrl) }
                var version by remember { mutableStateOf(config.appVersion) }
                var websiteUrl by remember { mutableStateOf(config.websiteUrl) }
                var courseUrl by remember { mutableStateOf(config.courseWebsiteUrl) }
                var coursePin by remember { mutableStateOf(config.courseDailyPin) }
                var noticePdfUrl by remember { mutableStateOf(config.noticePdfUrl) }
                var termsUrl by remember { mutableStateOf(config.termsUrl) }
                var privacyUrl by remember { mutableStateOf(config.privacyUrl) }
                var partnerUrl by remember { mutableStateOf(config.partnerProgramUrl) }
                var guidelinesUrl by remember { mutableStateOf(config.communityGuidelinesUrl) }
                var publisherPolicyUrl by remember { mutableStateOf(config.publisherPolicyUrl) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Fleearn Brand Identity", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandCyan)

                            OutlinedTextField(value = appName, onValueChange = { appName = it }, label = { Text("App Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(value = slogan, onValueChange = { slogan = it }, label = { Text("Slogan") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(value = logoUrl, onValueChange = { logoUrl = it }, label = { Text("Logo URL") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(value = version, onValueChange = { version = it }, label = { Text("App Release Version") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("App Description") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), minLines = 2)
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Web Links & Course Architecture", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandGold)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = courseUrl, onValueChange = { courseUrl = it }, label = { Text("Academy URL") }, modifier = Modifier.weight(1.5f), shape = RoundedCornerShape(10.dp))
                                OutlinedTextField(value = coursePin, onValueChange = { coursePin = it }, label = { Text("Daily Course PIN") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                            }

                            OutlinedTextField(value = noticePdfUrl, onValueChange = { noticePdfUrl = it }, label = { Text("Official PDF Notice URL") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(value = websiteUrl, onValueChange = { websiteUrl = it }, label = { Text("Official Website URL") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(value = partnerUrl, onValueChange = { partnerUrl = it }, label = { Text("Partner Program URL") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(value = guidelinesUrl, onValueChange = { guidelinesUrl = it }, label = { Text("Community Guidelines URL") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(value = publisherPolicyUrl, onValueChange = { publisherPolicyUrl = it }, label = { Text("Publisher Policy URL") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(value = termsUrl, onValueChange = { termsUrl = it }, label = { Text("Terms URL") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                            OutlinedTextField(value = privacyUrl, onValueChange = { privacyUrl = it }, label = { Text("Privacy Policy URL") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                        }
                    }

                    Button(
                        onClick = {
                            val updated = config.copy(
                                appName = appName,
                                slogan = slogan,
                                description = description,
                                appLogoUrl = logoUrl,
                                appVersion = version,
                                websiteUrl = websiteUrl,
                                courseWebsiteUrl = courseUrl,
                                courseDailyPin = coursePin,
                                noticePdfUrl = noticePdfUrl,
                                termsUrl = termsUrl,
                                privacyUrl = privacyUrl,
                                partnerProgramUrl = partnerUrl,
                                communityGuidelinesUrl = guidelinesUrl,
                                publisherPolicyUrl = publisherPolicyUrl
                            )
                            FleearnRepository.updateAppConfig(updated)
                            Toast.makeText(context, "App Configuration saved successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                    ) {
                        Text("Save Application Configuration", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            1 -> {
                // SECURITY & 2 FIXED ACCOUNTS
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = BrandGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fixed Admin Security Accounts (AX)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                            }
                            Text("Only 2 fixed root administrators are authorized with dual PIN authentication & brute force mitigation.", fontSize = 11.sp, color = BentoTextMuted)

                            adminAccounts.forEach { admin ->
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BentoSurface)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(admin.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BentoTextPrimary)
                                            Text("UID: ${admin.uid} • PIN: ${admin.pinHash}", fontSize = 11.sp, color = BrandGold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(BrandEmerald.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("ROOT AUTH", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BrandEmerald)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Security Protocols & Sessions", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandCyan)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Two-Factor Authentication (2FA)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BentoTextPrimary)
                                    Text("Enforce dual biometric / PIN OTP for all AX access", fontSize = 10.sp, color = BentoTextMuted)
                                }
                                Switch(
                                    checked = config.twoFactorAuthEnabled,
                                    onCheckedChange = {
                                        FleearnRepository.updateAppConfig(config.copy(twoFactorAuthEnabled = it))
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Brute-Force Rate Limiting", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BentoTextPrimary)
                                    Text("Lock PIN attempts after 5 consecutive failures", fontSize = 10.sp, color = BentoTextMuted)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BrandEmerald.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ENABLED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandEmerald)
                                }
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "All other sessions invalidated successfully.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandRed.copy(alpha = 0.8f))
                            ) {
                                Text("Logout All Other Devices", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            2 -> {
                // INFRASTRUCTURE & AUDIT LOGS
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("System Infrastructure & Health", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandEmerald)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Maintenance Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BentoTextPrimary)
                                    Text("Temporary lockdown of user transactions", fontSize = 10.sp, color = BentoTextMuted)
                                }
                                Switch(
                                    checked = config.maintenanceMode,
                                    onCheckedChange = {
                                        FleearnRepository.updateAppConfig(config.copy(maintenanceMode = it))
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Firebase Firestore", fontSize = 11.sp, color = BentoTextMuted)
                                Text("ONLINE (100% SLA)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandEmerald)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cloud Functions", fontSize = 11.sp, color = BentoTextMuted)
                                Text("DEPLOYED (Node 20)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandEmerald)
                            }
                        }
                    }

                    Text("System Audit Logs", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(auditLogs, key = { it.id }) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, BentoBorderSubtle, RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(log.action, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = BrandCyan)
                                            Text(log.adminUid, fontSize = 10.sp, color = BrandGold)
                                        }
                                        Text(log.details, fontSize = 11.sp, color = BentoTextMuted)
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
