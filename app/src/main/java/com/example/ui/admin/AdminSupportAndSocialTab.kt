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
fun AdminSupportAndSocialTab() {
    val context = LocalContext.current
    val tickets by FleearnRepository.tickets.collectAsState()
    val reports by FleearnRepository.reports.collectAsState()
    val posts by FleearnRepository.posts.collectAsState()
    val channels by FleearnRepository.channels.collectAsState()
    val config by FleearnRepository.appConfig.collectAsState()

    var selectedSubSection by remember { mutableStateOf(0) } // 0: Tickets, 1: Reports, 2: Feed & Posts, 3: Channels
    val subSections = listOf(
        "Tickets (${tickets.count { it.status == "RUNNING" }})",
        "Reports (${reports.count { it.status == "PENDING" }})",
        "Social Feed",
        "Channels"
    )

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
                // TICKETS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Support Inquiry Desk", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                    Button(
                        onClick = { InAppBrowserManager.openUrl(config.aiSupportUrl, "FleUra AI Support Engine") },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                    ) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Support Portal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (tickets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active support tickets.", color = BentoTextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(tickets, key = { it.id }) { tkt ->
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
                                            Text(tkt.problemTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary, modifier = Modifier.weight(1f))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (tkt.status == "RUNNING") BrandGold.copy(alpha = 0.2f) else BrandEmerald.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(tkt.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (tkt.status == "RUNNING") BrandGold else BrandEmerald)
                                            }
                                        }

                                        Text(tkt.problemDetails, fontSize = 11.sp, color = BentoTextMuted)
                                        Text("User: ${tkt.creatorName} (${tkt.creatorUid}) • WhatsApp: ${tkt.whatsAppNumber}", fontSize = 11.sp, color = BrandCyan)

                                        if (tkt.status == "RUNNING") {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                                Button(
                                                    onClick = {
                                                        FleearnRepository.updateTicketStatus(tkt.id, "CLOSED")
                                                        Toast.makeText(context, "Ticket marked as resolved & closed", Toast.LENGTH_SHORT).show()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                                                ) {
                                                    Text("Mark as Resolved", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

            1 -> {
                // USER REPORTS
                if (reports.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No pending user reports.", color = BentoTextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(reports, key = { it.id }) { rep ->
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
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(rep.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (rep.status == "PENDING") BrandRed.copy(alpha = 0.2f) else BrandEmerald.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(rep.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (rep.status == "PENDING") BrandRed else BrandEmerald)
                                            }
                                        }

                                        Text(rep.description, fontSize = 11.sp, color = BentoTextMuted)
                                        Text("Reporter UID: ${rep.reporterUid}", fontSize = 10.sp, color = BrandCyan)

                                        if (rep.status == "PENDING") {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                                Button(
                                                    onClick = {
                                                        FleearnRepository.resolveReport(rep.id)
                                                        Toast.makeText(context, "Report resolved", Toast.LENGTH_SHORT).show()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                                                ) {
                                                    Text("Resolve Report", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

            2 -> {
                // SOCIAL FEED MODERATION
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(posts, key = { it.id }) { post ->
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
                                        Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                                        Text("${post.simulatedLikes} Likes", fontSize = 11.sp, color = BrandGold)
                                    }
                                    Text(post.content, fontSize = 11.sp, color = BentoTextMuted)
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // OFFICIAL CHANNELS
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(channels, key = { it.id }) { ch ->
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ch.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                                        Text(ch.description, fontSize = 11.sp, color = BentoTextMuted)
                                        Text("${ch.memberCount} Verified Subscribers", fontSize = 10.sp, color = BrandEmerald)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BrandCyan.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("PINNED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
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
