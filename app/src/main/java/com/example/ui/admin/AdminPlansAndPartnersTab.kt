package com.example.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun AdminPlansAndPartnersTab() {
    val context = LocalContext.current
    val users by FleearnRepository.users.collectAsState()
    val config by FleearnRepository.appConfig.collectAsState()

    var selectedSubSection by remember { mutableStateOf(0) } // 0: User Plans, 1: Publisher Plans, 2: Partners & Referrals, 3: Leaderboard
    val subSections = listOf("User Plans", "Publisher Plans", "Partners & Referral", "Top Leaderboard")

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
                // USER PLANS SPECIFICATION
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(UserPlan.values()) { plan ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BentoBorderSubtle, RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(plan.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandCyan)
                                        Text("${plan.priceFct} FCT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandGold)
                                    }

                                    Text("Duration: ${plan.durationDays} Days • Daily Limit: ${plan.dailyUsageMinutes} mins", fontSize = 11.sp, color = BentoTextMuted)
                                    Text("Max Daily Withdraw: ${plan.maxDailyWithdraw} FCT • Monthly: ${plan.maxMonthlyWithdraw} FCT", fontSize = 11.sp, color = BrandEmerald)

                                    val count = users.count { it.plan == plan && it.role == UserRole.USER }
                                    Text("Enrolled Users: $count Active", fontSize = 10.sp, color = BentoTextMuted)
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // PUBLISHER PLANS
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(PublisherPlan.values()) { plan ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BentoBorderSubtle, RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(plan.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandGold)
                                        Text("${plan.priceFct} FCT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandEmerald)
                                    }

                                    Text(if (plan == PublisherPlan.BLAZE) "Unlimited Campaigns • Lifetime Verified Badge • Priority Moderation" else "Standard posting with standard moderation queue", fontSize = 11.sp, color = BentoTextMuted)

                                    val count = users.count { it.publisherPlan == plan && it.role == UserRole.PUBLISHER }
                                    Text("Active Publishers: $count", fontSize = 10.sp, color = BentoTextMuted)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // PARTNERS & REFERRALS
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
                            Text("Partner Program Architecture", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandGold)
                            Text("Partner Program Fee: ${config.partnerProgramFee} FCT", fontSize = 12.sp, color = BentoTextPrimary)
                            Text("Commission Structure: 10% Direct referral commission on all deposit upgrades & job commissions.", fontSize = 11.sp, color = BentoTextMuted)

                            Button(
                                onClick = { InAppBrowserManager.openUrl(config.partnerProgramUrl, "Partner Program") },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                            ) {
                                Text("Inspect Partner Portal", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            3 -> {
                // LEADERBOARD (Top Ranked Users)
                val topUsers = users.filter { it.role == UserRole.USER }.sortedByDescending { it.balance }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(topUsers.take(100)) { user ->
                        val index = topUsers.indexOf(user) + 1
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BentoBorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (index <= 3) BrandGold else BentoSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("#$index", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (index <= 3) Color.Black else BentoTextMuted)
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                                        Text("UID: ${user.uid} • ${user.plan.displayName}", fontSize = 10.sp, color = BentoTextMuted)
                                    }

                                    Text("${String.format("%,.2f", user.balance)} FCT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandEmerald)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
