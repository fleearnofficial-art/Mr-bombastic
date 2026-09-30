package com.example.ui.publisher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.JobSubmission
import com.example.data.model.UserProfile
import com.example.data.repository.FleearnRepository
import com.example.ui.components.InAppBrowserManager
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PublisherSubmissionsTab(
    publisher: UserProfile,
    submissions: List<JobSubmission>,
    onSuccess: (String, String) -> Unit,
    initialJobIdFilter: String? = null
) {
    var selectedFilter by remember { mutableStateOf("PENDING") } // "PENDING", "APPROVED", "REJECTED", "ALL"
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())

    val mySubmissions = submissions.filter {
        it.publisherUid == publisher.uid &&
                (initialJobIdFilter == null || it.jobId == initialJobIdFilter)
    }

    val filteredList = when (selectedFilter) {
        "PENDING" -> mySubmissions.filter { it.status == "PENDING" }
        "APPROVED" -> mySubmissions.filter { it.status == "APPROVED" }
        "REJECTED" -> mySubmissions.filter { it.status == "REJECTED" }
        else -> mySubmissions
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filter tabs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Proof Review & Verification", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("PENDING" to "Pending", "APPROVED" to "Approved", "REJECTED" to "Rejected", "ALL" to "All").forEach { (key, label) ->
                            val count = when (key) {
                                "PENDING" -> mySubmissions.count { it.status == "PENDING" }
                                "APPROVED" -> mySubmissions.count { it.status == "APPROVED" }
                                "REJECTED" -> mySubmissions.count { it.status == "REJECTED" }
                                else -> mySubmissions.size
                            }
                            FilterChip(
                                selected = selectedFilter == key,
                                onClick = { selectedFilter = key },
                                label = { Text("$label ($count)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Submissions Found", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("No micro-job proofs currently match this filter.", fontSize = 12.sp, color = BentoTextMuted)
                        }
                    }
                }
            }
        } else {
            items(filteredList, key = { it.id }) { sub ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Submission #${sub.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Job ID: ${sub.jobId}", fontSize = 11.sp, color = BentoTextMuted)
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (sub.status) {
                                    "APPROVED" -> BrandGreen.copy(alpha = 0.2f)
                                    "REJECTED" -> BrandRed.copy(alpha = 0.2f)
                                    else -> BrandCyan.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = sub.status,
                                    color = when (sub.status) {
                                        "APPROVED" -> BrandGreen
                                        "REJECTED" -> BrandRed
                                        else -> BrandCyan
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(sub.jobTitle, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("User: ${sub.userName} (UID: ${sub.userUid})", fontSize = 11.sp, color = BentoTextMuted)
                        Text("Submitted: ${dateFormat.format(Date(sub.timestamp))}", fontSize = 10.sp, color = BentoTextMuted)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Proof notes container
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Proof Note / Notes:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoTextMuted)
                                Text(sub.proofNote.ifEmpty { "No textual notes provided." }, fontSize = 12.sp)

                                if (sub.proofUrl.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { previewImageUrl = sub.proofUrl },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = sub.proofUrl,
                                            contentDescription = "Proof Screenshot",
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(6.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Screenshot Proof Attached", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                                            Text("Tap to view full image", fontSize = 10.sp, color = BentoTextMuted)
                                        }
                                    }
                                }
                            }
                        }

                        if (sub.status == "PENDING") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        FleearnRepository.approveJobSubmission(sub.id)
                                        onSuccess("Proof Approved", "Worker ${sub.userName} rewarded ${sub.rewardAmount} FCT")
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve & Reward (%.2f FCT)".format(sub.rewardAmount), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        FleearnRepository.rejectJobSubmission(sub.id)
                                        onSuccess("Proof Rejected", "Submission #${sub.id} marked as rejected.")
                                    },
                                    modifier = Modifier.weight(0.6f).height(40.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = BrandRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reject", color = BrandRed, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    previewImageUrl?.let { url ->
        Dialog(onDismissRequest = { previewImageUrl = null }) {
            Card(
                modifier = Modifier.fillMaxWidth(0.95f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Worker Proof Screenshot", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { previewImageUrl = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = url,
                        contentDescription = "Full Proof",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { InAppBrowserManager.openUrl(url, "Proof Image") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                    ) {
                        Text("Open Full Resolution in Browser", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
