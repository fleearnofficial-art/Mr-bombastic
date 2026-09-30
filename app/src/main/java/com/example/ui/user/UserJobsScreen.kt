package com.example.ui.user

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.JobItem
import com.example.data.repository.FleearnRepository
import com.example.ui.components.*
import com.example.ui.dialogs.MobileRechargeDialog
import com.example.ui.dialogs.SendMoneyDialog
import com.example.ui.theme.*

@Composable
fun UserJobsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val jobs by FleearnRepository.jobs.collectAsState()
    val appConfig by FleearnRepository.appConfig.collectAsState()

    var showSendMoneyDialog by remember { mutableStateOf(false) }
    var showRechargeDialog by remember { mutableStateOf(false) }
    var showPostJobDialog by remember { mutableStateOf(false) }
    var selectedJobForDetails by remember { mutableStateOf<JobItem?>(null) }
    var showSuccessDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    var selectedCategory by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Featured") }

    val categories = listOf("All", "Social Media", "App Review", "Survey", "Signup", "YouTube")

    val filteredJobs = jobs.filter {
        selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)
    }.let { list ->
        when (sortBy) {
            "Highest" -> list.sortedByDescending { it.rewardPerSlot }
            "Lowest" -> list.sortedBy { it.rewardPerSlot }
            else -> list
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // Top 4 Micro Services Cards (Send Money, Recharge, Gaming, Subscription)
        item {
            Text(
                text = "Fast Micro-Services",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ServiceActionCard(
                    title = "Send Money",
                    icon = Icons.Default.Send,
                    color = BrandCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { showSendMoneyDialog = true }
                )
                ServiceActionCard(
                    title = "Recharge",
                    icon = Icons.Default.PhoneAndroid,
                    color = BrandGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { showRechargeDialog = true }
                )
                ServiceActionCard(
                    title = "Gaming",
                    icon = Icons.Default.SportsEsports,
                    color = BrandPurple,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        InAppBrowserManager.openUrl(appConfig.gamingTopupUrl, "Gaming Topup Portal")
                    }
                )
                ServiceActionCard(
                    title = "Subscribe",
                    icon = Icons.Default.Subscriptions,
                    color = BrandGold,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        InAppBrowserManager.openUrl(appConfig.subscriptionUrl, "Premium Subscription")
                    }
                )
            }
        }

        // Sponsored Ad Box (7s rotation)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            AdBannerBox()
        }

        // Categories & Post Job Button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Micro Jobs Marketplace",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showPostJobDialog = true },
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Post Job", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sorting bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredJobs.size} Available Jobs",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sort: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf("Featured", "Highest", "Lowest").forEach { s ->
                        Text(
                            text = s,
                            fontSize = 12.sp,
                            fontWeight = if (sortBy == s) FontWeight.Bold else FontWeight.Normal,
                            color = if (sortBy == s) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clickable { sortBy = s }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        // Job Items with Ad Box every 4 jobs
        items(filteredJobs.size) { index ->
            val job = filteredJobs[index]
            Spacer(modifier = Modifier.height(8.dp))
            JobCardItem(
                job = job,
                onClick = { selectedJobForDetails = job }
            )

            // Insert ad box after every 4 items
            if ((index + 1) % 4 == 0 && index != filteredJobs.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
                AdBannerBox()
            }
        }
    }

    if (showSendMoneyDialog) {
        SendMoneyDialog(
            onDismiss = { showSendMoneyDialog = false },
            onSuccess = { msg ->
                showSendMoneyDialog = false
                showSuccessDialog = "Transfer Complete" to msg
            }
        )
    }

    if (showRechargeDialog) {
        MobileRechargeDialog(
            onDismiss = { showRechargeDialog = false },
            onSuccess = { msg ->
                showRechargeDialog = false
                showSuccessDialog = "Recharge Submitted" to msg
            }
        )
    }

    if (showPostJobDialog) {
        PostJobDialog(
            onDismiss = { showPostJobDialog = false },
            onSuccess = { msg ->
                showPostJobDialog = false
                showSuccessDialog = "Job Created" to msg
            }
        )
    }

    selectedJobForDetails?.let { job ->
        JobDetailsDialog(
            job = job,
            onDismiss = { selectedJobForDetails = null },
            onSubmitProof = { note, count, url ->
                coroutineScope.launch {
                    val res = FleearnRepository.submitJobProof(job.id, note, count, url)
                    selectedJobForDetails = null
                    showSuccessDialog = if (res.first) "Proof Submitted" to res.second else "Submission Error" to res.second
                }
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
fun ServiceActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun JobCardItem(
    job: JobItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("job_item_${job.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = job.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = job.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandCyan
                    )
                    Text(
                        text = "${job.remainingSlots}/${job.slots} Slots",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = job.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "By ${job.publisherName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    FctCurrencyPill(amount = job.rewardPerSlot, isGain = true)
                }
            }
        }
    }
}

@Composable
fun JobDetailsDialog(
    job: JobItem,
    onDismiss: () -> Unit,
    onSubmitProof: (String, Int, String) -> Unit
) {
    val context = LocalContext.current
    var proofNote by remember { mutableStateOf("") }
    var proofUrl by remember { mutableStateOf("") }
    var proofCount by remember { mutableStateOf(1) }
    var error by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Job Details & Proof", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(job.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Reward: ${job.rewardPerSlot} FCT", color = BrandGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Remaining: ${job.remainingSlots} slots", fontSize = 13.sp)
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Instructions:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(job.description, fontSize = 12.sp)
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                InAppBrowserManager.openUrl(job.jobLink, job.title)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Task Link")
                        }
                    }

                    item {
                        Text("Submit Proof Requirements", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = proofNote,
                            onValueChange = { proofNote = it; error = "" },
                            label = { Text("Proof Note (Username, channel name, review text)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = proofUrl,
                            onValueChange = { proofUrl = it; error = "" },
                            label = { Text("Screenshot / Proof Image URL") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    if (error.isNotBlank()) {
                        item {
                            Text(error, color = BrandRed, fontSize = 12.sp)
                        }
                    }
                }

                Button(
                    onClick = {
                        if (proofNote.isBlank() && proofUrl.isBlank()) {
                            error = "Please provide proof notes or screenshot link"
                            return@Button
                        }
                        onSubmitProof(proofNote, proofCount, proofUrl)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text("Submit Completed Proof", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PostJobDialog(
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("https://") }
    var category by remember { mutableStateOf("Social Media") }
    var slotsText by remember { mutableStateOf("25") }
    var rewardText by remember { mutableStateOf("1.00") }
    var error by remember { mutableStateOf("") }

    val slots = slotsText.toIntOrNull() ?: 25
    val reward = rewardText.toDoubleOrNull() ?: 1.0
    val totalCost = (slots * reward) + 25.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.98f).fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Post New Micro Job", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Job Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Task Instructions & Proof Requirements") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = link,
                            onValueChange = { link = it },
                            label = { Text("Target Link") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = slotsText,
                                onValueChange = { slotsText = it },
                                label = { Text("Slots (Min 25)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = rewardText,
                                onValueChange = { rewardText = it },
                                label = { Text("Reward/Slot (Min 0.50)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total Cost: $totalCost FCT (Including 25 FCT posting fee)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    if (error.isNotBlank()) {
                        item { Text(error, color = BrandRed, fontSize = 12.sp) }
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val res = FleearnRepository.createJob(
                                title = title,
                                description = description,
                                link = link,
                                category = category,
                                thumbnailUrl = "",
                                slots = slots,
                                rewardPerSlot = reward
                            )
                            if (res.first) onSuccess(res.second) else error = res.second
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Confirm & Launch Job", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
