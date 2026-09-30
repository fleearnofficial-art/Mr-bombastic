package com.example.ui.publisher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.JobItem
import com.example.data.model.UserProfile
import com.example.ui.components.InAppBrowserManager
import com.example.ui.theme.*

@Composable
fun PublisherJobsTab(
    publisher: UserProfile,
    jobs: List<JobItem>,
    onCreateJobClick: () -> Unit,
    onViewSubmissionsClick: (String) -> Unit
) {
    val myJobs = jobs.filter { it.publisherUid == publisher.uid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary & Action Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Micro-Jobs", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "${myJobs.size} Campaigns • ${myJobs.sumOf { it.slots - it.remainingSlots }} Completed Slots",
                            fontSize = 12.sp,
                            color = BentoTextMuted
                        )
                    }

                    Button(
                        onClick = onCreateJobClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Post Job", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        if (myJobs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Work, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Jobs Created Yet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Publish your tasks to 100,000+ active workers for fast engagement.", fontSize = 12.sp, color = BentoTextMuted)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onCreateJobClick, colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
                                Text("Create First Job", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(myJobs, key = { it.id }) { job ->
                val completedSlots = job.slots - job.remainingSlots
                val progress = if (job.slots > 0) completedSlots.toFloat() / job.slots else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (job.thumbnailUrl.isNotBlank()) {
                                AsyncImage(
                                    model = job.thumbnailUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Job ID: ${job.id}", fontSize = 11.sp, color = BentoTextMuted)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (job.remainingSlots > 0) BrandGreen.copy(alpha = 0.2f) else BrandGold.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (job.remainingSlots > 0) "ACTIVE" else "COMPLETED",
                                            color = if (job.remainingSlots > 0) BrandGreen else BrandGold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(job.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(job.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Slots: $completedSlots / ${job.slots} Completed", fontSize = 11.sp, color = BentoTextMuted)
                                Text("${job.remainingSlots} Remaining", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = BrandGreen,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Footer row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Category: ${job.category}", fontSize = 11.sp, color = BentoTextMuted)
                                Text("Reward: %.2f FCT / slot".format(job.rewardPerSlot), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (job.jobLink.isNotBlank()) {
                                    IconButton(
                                        onClick = { InAppBrowserManager.openUrl(job.jobLink, job.title) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.Link, contentDescription = "Job Link", tint = BrandCyan, modifier = Modifier.size(18.dp))
                                    }
                                }

                                OutlinedButton(
                                    onClick = { onViewSubmissionsClick(job.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Proofs", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
