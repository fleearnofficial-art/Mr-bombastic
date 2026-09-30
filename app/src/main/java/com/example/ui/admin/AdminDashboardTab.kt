package com.example.ui.admin

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.FleearnRepository
import com.example.ui.theme.*

@Composable
fun AdminDashboardTab(
    onNavigateTab: (String) -> Unit,
    onOpenCreateMod: () -> Unit,
    onOpenConfig: () -> Unit,
    onOpenBroadcast: () -> Unit
) {
    val config by FleearnRepository.appConfig.collectAsState()
    val users by FleearnRepository.users.collectAsState()
    val depositRequests by FleearnRepository.depositRequests.collectAsState()
    val withdrawRequests by FleearnRepository.withdrawRequests.collectAsState()
    val jobs by FleearnRepository.jobs.collectAsState()
    val submissions by FleearnRepository.jobSubmissions.collectAsState()
    val products by FleearnRepository.products.collectAsState()
    val productOrders by FleearnRepository.productOrders.collectAsState()
    val services by FleearnRepository.services.collectAsState()
    val ads by FleearnRepository.ads.collectAsState()
    val tickets by FleearnRepository.tickets.collectAsState()
    val reports by FleearnRepository.reports.collectAsState()
    val auditLogs by FleearnRepository.auditLogs.collectAsState()

    val pendingDeposits = depositRequests.count { it.status == "PENDING" }
    val pendingWithdraws = withdrawRequests.count { it.status == "PENDING" }
    val pendingSubmissions = submissions.count { it.status == "PENDING" }
    val pendingTickets = tickets.count { it.status == "RUNNING" }
    val pendingReports = reports.count { it.status == "PENDING" }
    val pendingOrders = productOrders.count { it.status == "PENDING" }

    val totalUsersCount = users.count { it.role == UserRole.USER }
    val totalPublishersCount = users.count { it.role == UserRole.PUBLISHER }
    val totalModeratorsCount = users.count { it.role == UserRole.MODERATOR }
    val totalPlatformBalance = users.sumOf { it.balance }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Platform Financial Matrix Card (Bento Hero)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(BrandGold.copy(alpha = 0.12f), Color.Transparent),
                            radius = 600f
                        )
                    )
                    .border(1.dp, BentoBorderSubtle, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(BrandGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = BrandGold, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "FINANCIAL MATRIX & PLATFORM PROFIT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGold,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (config.maintenanceMode) BrandRed.copy(alpha = 0.2f) else BrandEmerald.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (config.maintenanceMode) "MAINTENANCE" else "ACTIVE 24/7",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (config.maintenanceMode) BrandRed else BrandEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "${String.format("%,.2f", config.platformProfit)} FCT",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BentoTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Net Platform Cumulative Profit • Reserve Ledger Active",
                        fontSize = 12.sp,
                        color = BentoTextMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BentoBorderSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Users Balance", fontSize = 10.sp, color = BentoTextMuted)
                            Text("${String.format("%,.0f", totalPlatformBalance)} FCT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandCyan)
                        }
                        Column {
                            Text("Active Streaks", fontSize = 10.sp, color = BentoTextMuted)
                            Text("100% Verified", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandEmerald)
                        }
                        Column {
                            Text("App Version", fontSize = 10.sp, color = BentoTextMuted)
                            Text(config.appVersion, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandGold)
                        }
                    }
                }
            }
        }

        // Quick Admin Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onOpenCreateMod,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Mod (MX)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onOpenBroadcast,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Broadcast", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onOpenConfig,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Config", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        // Section Title
        Text(
            text = "Fleearn Statistics & Queues",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = BentoTextPrimary
        )

        // Bento Grid KPI Matrix (2 columns)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricBentoCard(
                title = "Total Users",
                value = "$totalUsersCount",
                subtext = "Active in Fleearn",
                icon = Icons.Default.People,
                accentColor = BrandCyan,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab("Users") }
            )
            MetricBentoCard(
                title = "Publishers",
                value = "$totalPublishersCount",
                subtext = "Verified orgs",
                icon = Icons.Default.Business,
                accentColor = BrandGold,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab("Publishers") }
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricBentoCard(
                title = "Moderators",
                value = "$totalModeratorsCount",
                subtext = "MX Active staff",
                icon = Icons.Default.Shield,
                accentColor = BrandRed,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab("Moderators") }
            )
            MetricBentoCard(
                title = "Pending Deposits",
                value = "$pendingDeposits",
                subtext = "Gateway verification",
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = if (pendingDeposits > 0) BrandEmerald else BentoTextMuted,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab("Finance") }
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricBentoCard(
                title = "Pending Withdraws",
                value = "$pendingWithdraws",
                subtext = "Payout queue",
                icon = Icons.Default.Payments,
                accentColor = if (pendingWithdraws > 0) BrandRed else BentoTextMuted,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab("Finance") }
            )
            MetricBentoCard(
                title = "Job Submissions",
                value = "$pendingSubmissions",
                subtext = "Proof reviews",
                icon = Icons.Default.AssignmentTurnedIn,
                accentColor = if (pendingSubmissions > 0) BrandPurple else BentoTextMuted,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab("Jobs") }
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricBentoCard(
                title = "Support Tickets",
                value = "$pendingTickets",
                subtext = "Running inquiries",
                icon = Icons.Default.LiveHelp,
                accentColor = if (pendingTickets > 0) BrandGold else BentoTextMuted,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab("Support") }
            )
            MetricBentoCard(
                title = "User Reports",
                value = "$pendingReports",
                subtext = "Pending resolution",
                icon = Icons.Default.ReportProblem,
                accentColor = if (pendingReports > 0) BrandRed else BentoTextMuted,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab("Support") }
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricBentoCard(
                title = "Resell Orders",
                value = "$pendingOrders",
                subtext = "Shipping pipeline",
                icon = Icons.Default.LocalShipping,
                accentColor = if (pendingOrders > 0) BrandCyan else BentoTextMuted,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab("Products") }
            )
            MetricBentoCard(
                title = "Active Ads",
                value = "${ads.count { it.isActive }}",
                subtext = "${ads.size} Total campaigns",
                icon = Icons.Default.AdsClick,
                accentColor = BrandEmerald,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab("Ads") }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Real-Time Audit & Activity Stream
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BrandEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Real-Time Activity & Audit Logs",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }
                    TextButton(onClick = { onNavigateTab("Security") }) {
                        Text("View All", fontSize = 11.sp, color = BrandCyan)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                auditLogs.take(4).forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(BentoSurface)
                                .border(1.dp, BentoBorderSubtle, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = BrandGold, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${log.action} • ${log.target}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoTextPrimary
                            )
                            Text(
                                text = log.details,
                                fontSize = 10.sp,
                                color = BentoTextMuted,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBentoCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BentoBorderSubtle, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = BentoTextMuted
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(15.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Text(
                    text = subtext,
                    fontSize = 10.sp,
                    color = BentoTextMuted
                )
            }
        }
    }
}
