package com.example.ui.moderator

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.FleearnRepository
import com.example.ui.components.Success3DDialog
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*

@Composable
fun ModeratorHomeTab(
    moderator: UserProfile,
    config: SystemAppConfig,
    tickets: List<SupportTicket>,
    reports: List<UserReport>
) {
    var activeSubPage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    val context = LocalContext.current

    if (activeSubPage != null) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeSubPage = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BentoTextPrimary)
                }
                Text(activeSubPage!!, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = BentoTextPrimary)
            }

            when (activeSubPage) {
                "Support" -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        if (tickets.isEmpty()) {
                            item { Text("No support tickets currently.", color = BentoTextSecondary, modifier = Modifier.padding(16.dp)) }
                        } else {
                            items(tickets) { ticket ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(ticket.problemTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(
                                                text = ticket.status,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (ticket.status == "RUNNING") BrandGreen else Color.Gray
                                            )
                                        }
                                        Text("User: ${ticket.creatorName} (UID: ${ticket.creatorUid}) • WhatsApp: ${ticket.whatsAppNumber}", fontSize = 12.sp, color = BentoTextSecondary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(ticket.problemDetails, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                val newStatus = if (ticket.status == "RUNNING") "CLOSED" else "RUNNING"
                                                FleearnRepository.updateTicketStatus(ticket.id, newStatus)
                                                showSuccessDialog = "Status Updated" to "Ticket #${ticket.id} marked as $newStatus."
                                            },
                                            modifier = Modifier.fillMaxWidth().height(36.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(if (ticket.status == "RUNNING") "Mark as Resolved (Close)" else "Reopen Ticket (Running)", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "Reports" -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                        if (reports.isEmpty()) {
                            item { Text("No reports currently.", color = BentoTextSecondary, modifier = Modifier.padding(16.dp)) }
                        } else {
                            items(reports) { r ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(r.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(r.status, fontSize = 11.sp, color = if (r.status == "PENDING") BrandRed else BrandGreen)
                                        }
                                        Text("Reporter UID: ${r.reporterUid}", fontSize = 11.sp, color = BentoTextSecondary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(r.description, fontSize = 12.sp)
                                        if (r.status == "PENDING") {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    FleearnRepository.resolveReport(r.id)
                                                    showSuccessDialog = "Report Resolved" to "Report #${r.id} marked resolved."
                                                },
                                                modifier = Modifier.fillMaxWidth().height(34.dp)
                                            ) {
                                                Text("Mark Report Resolved", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "Instructions" -> {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val inst = FleearnRepository.moderatorInstructions.collectAsState().value
                        Card(colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)) {
                            Text(inst, modifier = Modifier.padding(16.dp), color = BentoTextPrimary)
                        }
                    }
                }
                "Notices" -> {
                    val notifications by FleearnRepository.broadcastNotifications.collectAsState()
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        if (notifications.isEmpty()) {
                            item {
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)) {
                                    Text("No new notices at this time.", color = BentoTextSecondary, modifier = Modifier.padding(16.dp))
                                }
                            }
                        } else {
                            items(notifications) { notif ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(notif.title, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(notif.message, fontSize = 12.sp, color = BentoTextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
                "Approvals" -> {
                    val jobs by FleearnRepository.jobs.collectAsState()
                    val products by FleearnRepository.products.collectAsState()
                    val services by FleearnRepository.services.collectAsState()
                    
                    val pendingJobs = jobs.filter { !it.isApproved }
                    val pendingProducts = products.filter { !it.isApproved }
                    val pendingServices = services.filter { !it.isApproved }
                    
                    var approvalTab by remember { mutableStateOf(0) } // 0: Jobs, 1: Products, 2: Services
                    
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        TabRow(
                            selectedTabIndex = approvalTab,
                            containerColor = BentoSurfaceCard,
                            contentColor = BrandRed,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        ) {
                            Tab(selected = approvalTab == 0, onClick = { approvalTab = 0 }, text = { Text("Jobs (${pendingJobs.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                            Tab(selected = approvalTab == 1, onClick = { approvalTab = 1 }, text = { Text("Products (${pendingProducts.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                            Tab(selected = approvalTab == 2, onClick = { approvalTab = 2 }, text = { Text("Services (${pendingServices.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        when (approvalTab) {
                            0 -> {
                                if (pendingJobs.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("No pending jobs to approve.", color = BentoTextSecondary)
                                    }
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(pendingJobs) { job ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text(job.title, fontWeight = FontWeight.Bold, color = BentoTextPrimary, fontSize = 15.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("Category: ${job.category} • Reward: ${job.rewardPerSlot} FCT", fontSize = 12.sp, color = BrandRed, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(job.description, fontSize = 12.sp, color = BentoTextSecondary)
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Button(
                                                            onClick = {
                                                                FleearnRepository.approveJob(job.id)
                                                                showSuccessDialog = "Job Approved" to "Job '${job.title}' is now live!"
                                                            },
                                                            modifier = Modifier.weight(1f),
                                                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                                                        ) {
                                                            Text("Approve", color = Color.White)
                                                        }
                                                        Button(
                                                            onClick = {
                                                                FleearnRepository.rejectJob(job.id)
                                                                showSuccessDialog = "Job Rejected" to "Job '${job.title}' has been deleted."
                                                            },
                                                            modifier = Modifier.weight(1f),
                                                            colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                                                        ) {
                                                            Text("Reject", color = Color.White)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                if (pendingProducts.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("No pending products to approve.", color = BentoTextSecondary)
                                    }
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(pendingProducts) { prod ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text(prod.title, fontWeight = FontWeight.Bold, color = BentoTextPrimary, fontSize = 15.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("Price: ${prod.adminPrice} FCT • Stock: ${prod.stock}", fontSize = 12.sp, color = BrandRed, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(prod.description, fontSize = 12.sp, color = BentoTextSecondary)
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Button(
                                                            onClick = {
                                                                FleearnRepository.approveProduct(prod.id)
                                                                showSuccessDialog = "Product Approved" to "Product '${prod.title}' is now in marketplace!"
                                                            },
                                                            modifier = Modifier.weight(1f),
                                                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                                                        ) {
                                                            Text("Approve", color = Color.White)
                                                        }
                                                        Button(
                                                            onClick = {
                                                                FleearnRepository.rejectProduct(prod.id)
                                                                showSuccessDialog = "Product Rejected" to "Product '${prod.title}' has been deleted."
                                                            },
                                                            modifier = Modifier.weight(1f),
                                                            colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                                                        ) {
                                                            Text("Reject", color = Color.White)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                if (pendingServices.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("No pending services to approve.", color = BentoTextSecondary)
                                    }
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(pendingServices) { srv ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text(srv.title, fontWeight = FontWeight.Bold, color = BentoTextPrimary, fontSize = 15.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("Starting Price: ${srv.price} FCT", fontSize = 12.sp, color = BrandRed, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(srv.description, fontSize = 12.sp, color = BentoTextSecondary)
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Button(
                                                            onClick = {
                                                                FleearnRepository.approveService(srv.id)
                                                                showSuccessDialog = "Service Approved" to "Service '${srv.title}' is now approved!"
                                                            },
                                                            modifier = Modifier.weight(1f),
                                                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                                                        ) {
                                                            Text("Approve", color = Color.White)
                                                        }
                                                        Button(
                                                            onClick = {
                                                                FleearnRepository.rejectService(srv.id)
                                                                showSuccessDialog = "Service Rejected" to "Service '${srv.title}' has been deleted."
                                                            },
                                                            modifier = Modifier.weight(1f),
                                                            colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                                                        ) {
                                                            Text("Reject", color = Color.White)
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
                "Partner Program" -> {
                    LaunchedEffect(Unit) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fleearn.com/partner"))
                        context.startActivity(intent)
                        activeSubPage = null
                    }
                }
            }
        }
    } else {
        val pendingTickets = tickets.count { it.status == "PENDING" }
        val pendingReports = reports.count { it.status == "PENDING" }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
        ) {
            // Top Bar (Profile + Notifications)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(moderator.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BentoTextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            VerifiedBadge(role = UserRole.MODERATOR)
                        }
                        Text("UID: ${moderator.uid}", fontSize = 12.sp, color = BentoTextSecondary)
                    }
                    IconButton(onClick = { showSuccessDialog = "Notifications" to "No new notifications." }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = BentoTextPrimary)
                    }
                }
            }

            // Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandRed.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Salary Balance", fontSize = 14.sp, color = BentoTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${moderator.balance} FCT", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = BrandRed)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Active Tasks: ${pendingTickets + pendingReports}", fontSize = 12.sp, color = BentoTextPrimary)
                            Text("Pending Tickets: $pendingTickets", fontSize = 12.sp, color = BentoTextPrimary)
                        }
                    }
                }
            }

            // Quick Options Grid
            item {
                Text("Quick Options", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BentoTextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                
                val options = listOf(
                    Pair("Support", Icons.Default.SupportAgent),
                    Pair("Reports", Icons.Default.Report),
                    Pair("Instructions", Icons.Default.IntegrationInstructions),
                    Pair("Notices", Icons.Default.Campaign),
                    Pair("Approvals", Icons.Default.CheckCircle),
                    Pair("Partner Program", Icons.Default.Handshake)
                )

                // 2 Columns layout
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in options.indices step 2) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            QuickOptionItem(
                                title = options[i].first,
                                icon = options[i].second,
                                modifier = Modifier.weight(1f),
                                onClick = { activeSubPage = options[i].first }
                            )
                            if (i + 1 < options.size) {
                                QuickOptionItem(
                                    title = options[i + 1].first,
                                    icon = options[i + 1].second,
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeSubPage = options[i + 1].first }
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
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
fun QuickOptionItem(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().clickable(onClick = onClick).padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = BrandRed, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
        }
    }
}
