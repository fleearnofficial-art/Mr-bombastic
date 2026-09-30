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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.data.repository.FleearnRepository
import com.example.ui.components.InAppBrowserManager
import com.example.ui.theme.*

@Composable
fun AdminJobsAndContentTab() {
    val context = LocalContext.current
    val jobs by FleearnRepository.jobs.collectAsState()
    val submissions by FleearnRepository.jobSubmissions.collectAsState()
    val ads by FleearnRepository.ads.collectAsState()

    var selectedSubSection by remember { mutableStateOf(0) } // 0: Jobs, 1: Submissions, 2: Ads
    val subSections = listOf("Jobs (${jobs.size})", "Submissions (${submissions.size})", "Ads (${ads.size})")

    var selectedSubmissionForReview by remember { mutableStateOf<JobSubmission?>(null) }

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
                            fontSize = 12.sp,
                            fontWeight = if (selectedSubSection == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        when (selectedSubSection) {
            0 -> {
                // JOBS LIST
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(jobs, key = { it.id }) { job ->
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
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(BrandCyan.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(job.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("${job.remainingSlots}/${job.slots} Slots", fontSize = 11.sp, color = BentoTextMuted)
                                            Text("•", fontSize = 11.sp, color = BentoTextMuted)
                                            Text("${job.rewardPerSlot} FCT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandEmerald)
                                        }
                                    }

                                    Text(job.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BentoTextPrimary)
                                    Text(job.description, fontSize = 11.sp, color = BentoTextMuted, maxLines = 2)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("By: ${job.publisherName}", fontSize = 11.sp, color = BrandGold)

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            IconButton(
                                                onClick = { InAppBrowserManager.openUrl(job.jobLink, job.title) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Language, contentDescription = "Open Link", tint = BrandCyan, modifier = Modifier.size(16.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    FleearnRepository.rejectJob(job.id)
                                                    Toast.makeText(context, "Job rejected", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BrandRed, modifier = Modifier.size(16.dp))
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
                // SUBMISSIONS LIST
                if (submissions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No job submissions yet.", color = BentoTextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(submissions, key = { it.id }) { sub ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedSubmissionForReview = sub },
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
                                            Text(sub.jobTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary, modifier = Modifier.weight(1f))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        when (sub.status) {
                                                            "APPROVED" -> BrandEmerald.copy(alpha = 0.2f)
                                                            "REJECTED" -> BrandRed.copy(alpha = 0.2f)
                                                            else -> BrandGold.copy(alpha = 0.2f)
                                                        }
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    sub.status,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (sub.status) {
                                                        "APPROVED" -> BrandEmerald
                                                        "REJECTED" -> BrandRed
                                                        else -> BrandGold
                                                    }
                                                )
                                            }
                                        }

                                        Text("Submitted by: ${sub.userName} (UID: ${sub.userUid})", fontSize = 11.sp, color = BentoTextMuted)
                                        Text("Proof Note: ${sub.proofNote}", fontSize = 11.sp, color = BentoTextMuted, maxLines = 1)
                                        Text("Reward: ${sub.rewardAmount} FCT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandEmerald)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // ADS LIST
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(ads, key = { it.id }) { ad ->
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
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(ad.publisherName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (ad.isActive) BrandEmerald.copy(alpha = 0.2f) else BrandRed.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(if (ad.isActive) "ACTIVE (7s Timer)" else "SUSPENDED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (ad.isActive) BrandEmerald else BrandRed)
                                        }
                                    }

                                    AsyncImage(
                                        model = ad.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${ad.currentViews}/${ad.targetViews} Views • ${ad.clicks} Clicks", fontSize = 11.sp, color = BentoTextMuted)

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            IconButton(
                                                onClick = { InAppBrowserManager.openUrl(ad.destinationUrl, ad.publisherName) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Language, contentDescription = "Preview", tint = BrandCyan, modifier = Modifier.size(16.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    if (ad.isActive) {
                                                        FleearnRepository.suspendAd(ad.id)
                                                        Toast.makeText(context, "Ad suspended", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        FleearnRepository.approveAd(ad.id)
                                                        Toast.makeText(context, "Ad activated", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(if (ad.isActive) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = BrandGold, modifier = Modifier.size(16.dp))
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
    }

    // Submission Review Dialog
    selectedSubmissionForReview?.let { sub ->
        Dialog(onDismissRequest = { selectedSubmissionForReview = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Submission Review & Dispute", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                    Text("Job: ${sub.jobTitle}", fontSize = 13.sp, color = BrandCyan, fontWeight = FontWeight.SemiBold)
                    Text("Worker UID: ${sub.userUid} (${sub.userName})", fontSize = 11.sp, color = BentoTextMuted)
                    Text("Publisher UID: ${sub.publisherUid}", fontSize = 11.sp, color = BentoTextMuted)

                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BentoSurface)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Worker Proof Note:", fontSize = 11.sp, color = BentoTextMuted)
                            Text(sub.proofNote.ifEmpty { "No written notes attached." }, fontSize = 13.sp, color = BentoTextPrimary)
                        }
                    }

                    if (sub.proofUrl.isNotBlank()) {
                        Text("Screenshot Attachment:", fontSize = 11.sp, color = BentoTextMuted)
                        AsyncImage(
                            model = sub.proofUrl,
                            contentDescription = "Proof",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Text("Reward Amount: ${sub.rewardAmount} FCT", fontWeight = FontWeight.Bold, color = BrandEmerald, fontSize = 14.sp)

                    if (sub.status == "PENDING") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    FleearnRepository.approveJobSubmission(sub.id)
                                    Toast.makeText(context, "Submission approved & rewarded", Toast.LENGTH_SHORT).show()
                                    selectedSubmissionForReview = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                            ) {
                                Text("Approve & Pay", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    FleearnRepository.rejectJobSubmission(sub.id)
                                    Toast.makeText(context, "Submission rejected (50/50 refund)", Toast.LENGTH_SHORT).show()
                                    selectedSubmissionForReview = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                            ) {
                                Text("Reject", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BentoSurface)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Status: ${sub.status}", fontWeight = FontWeight.Bold, color = BentoTextMuted)
                        }
                    }
                }
            }
        }
    }
}
