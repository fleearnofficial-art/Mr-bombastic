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
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.data.repository.FleearnRepository
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*

@Composable
fun AdminUsersAndPublishersTab(
    isPublishersOnly: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val users by FleearnRepository.users.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlanFilter by remember { mutableStateOf("ALL") }
    var selectedUserForDetails by remember { mutableStateOf<UserProfile?>(null) }
    var userToAdjustBalance by remember { mutableStateOf<UserProfile?>(null) }

    val filteredList = users.filter { user ->
        val roleMatch = if (isPublishersOnly) user.role == UserRole.PUBLISHER else user.role == UserRole.USER
        val searchMatch = searchQuery.isBlank() ||
                user.name.contains(searchQuery, ignoreCase = true) ||
                user.uid.contains(searchQuery, ignoreCase = true) ||
                user.mobile.contains(searchQuery, ignoreCase = true) ||
                user.organizationName.contains(searchQuery, ignoreCase = true)

        val planMatch = when (selectedPlanFilter) {
            "ALL" -> true
            "SUSPENDED" -> user.isSuspended
            "VERIFIED" -> user.isVerified
            else -> user.plan.name == selectedPlanFilter || user.publisherPlan.name == selectedPlanFilter
        }
        roleMatch && searchMatch && planMatch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (isPublishersOnly) "Search publisher name, org, UID, mobile..." else "Search user name, UID, mobile...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BentoTextMuted) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = BentoTextMuted)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BentoSurfaceCard,
                unfocusedContainerColor = BentoSurfaceCard,
                focusedBorderColor = BrandCyan,
                unfocusedBorderColor = BentoBorderSubtle
            ),
            singleLine = true
        )

        // Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filterOptions = if (isPublishersOnly) {
                listOf("ALL", "SPARK", "BLAZE", "VERIFIED", "SUSPENDED")
            } else {
                listOf("ALL", "PREMIUM", "STANDARD", "BASIC", "TRIAL", "SUSPENDED")
            }

            filterOptions.forEach { filter ->
                val isSelected = selectedPlanFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) BrandCyan else BentoSurfaceCard)
                        .border(1.dp, if (isSelected) BrandCyan else BentoBorderSubtle, RoundedCornerShape(20.dp))
                        .clickable { selectedPlanFilter = filter }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.Black else BentoTextMuted
                    )
                }
            }
        }

        Text(
            text = "${filteredList.size} ${if (isPublishersOnly) "Publishers" else "Users"} Found",
            fontSize = 12.sp,
            color = BentoTextMuted,
            fontWeight = FontWeight.SemiBold
        )

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList, key = { it.uid }) { user ->
                AdminUserCard(
                    user = user,
                    isPublisher = isPublishersOnly,
                    onInspect = { selectedUserForDetails = user },
                    onAdjustBalance = { userToAdjustBalance = user },
                    onToggleSuspend = {
                        if (user.isSuspended) {
                            FleearnRepository.reactivateUser(user.uid)
                            Toast.makeText(context, "Account ${user.uid} reactivated", Toast.LENGTH_SHORT).show()
                        } else {
                            FleearnRepository.suspendUser(user.uid)
                            Toast.makeText(context, "Account ${user.uid} suspended", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    // Details & Management Inspector Dialog
    selectedUserForDetails?.let { user ->
        AdminUserInspectorDialog(
            user = user,
            onDismiss = { selectedUserForDetails = null },
            onUpdatePlan = { plan ->
                FleearnRepository.setUserPlan(user.uid, plan)
                selectedUserForDetails = users.find { it.uid == user.uid }
                Toast.makeText(context, "Plan updated to ${plan.displayName}", Toast.LENGTH_SHORT).show()
            },
            onUpdatePublisherPlan = { plan ->
                FleearnRepository.setPublisherPlan(user.uid, plan)
                selectedUserForDetails = users.find { it.uid == user.uid }
                Toast.makeText(context, "Publisher Plan updated to ${plan.displayName}", Toast.LENGTH_SHORT).show()
            },
            onToggleVerification = {
                FleearnRepository.toggleUserVerification(user.uid)
                selectedUserForDetails = users.find { it.uid == user.uid }
                Toast.makeText(context, "Verification toggled", Toast.LENGTH_SHORT).show()
            },
            onAdjustBalance = {
                userToAdjustBalance = user
                selectedUserForDetails = null
            },
            onDeleteUser = {
                coroutineScope.launch {
                    val res = FleearnRepository.adminDeleteUser(user.uid)
                    Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                    if (res.first) selectedUserForDetails = null
                }
            }
        )
    }

    // Adjust Balance Modal
    userToAdjustBalance?.let { user ->
        AdminAdjustBalanceDialog(
            user = user,
            onDismiss = { userToAdjustBalance = null },
            onConfirm = { delta, reason ->
                coroutineScope.launch {
                    val res = FleearnRepository.adjustUserBalance(user.uid, delta, reason)
                    Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                    userToAdjustBalance = null
                }
            }
        )
    }
}

@Composable
fun AdminUserCard(
    user: UserProfile,
    isPublisher: Boolean,
    onInspect: () -> Unit,
    onAdjustBalance: () -> Unit,
    onToggleSuspend: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onInspect),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BentoBorderSubtle, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(BentoSurface)
                            .border(1.dp, BrandCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.profilePhotoUrl.isNotBlank() || user.organizationLogoUrl.isNotBlank()) {
                            AsyncImage(
                                model = user.organizationLogoUrl.ifEmpty { user.profilePhotoUrl },
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(user.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = BrandCyan)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isPublisher && user.organizationName.isNotBlank()) user.organizationName else user.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = BentoTextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            VerifiedBadge(role = user.role, size = 16)
                        }

                        Text(
                            text = "UID: ${user.uid} • ${user.mobile}",
                            fontSize = 11.sp,
                            color = BentoTextMuted
                        )
                    }

                    // Plan Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isPublisher) BrandGold.copy(alpha = 0.15f) else BrandCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isPublisher) user.publisherPlan.displayName else user.plan.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPublisher) BrandGold else BrandCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BentoBorderSubtle)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Balance", fontSize = 10.sp, color = BentoTextMuted)
                        Text("${String.format("%,.2f", user.balance)} FCT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandEmerald)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onAdjustBalance,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.PriceChange, contentDescription = "Adjust Balance", tint = BrandCyan, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = onToggleSuspend,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (user.isSuspended) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = "Suspend Toggle",
                                tint = if (user.isSuspended) BrandEmerald else BrandRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onInspect,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Inspect", tint = BentoTextMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserInspectorDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onUpdatePlan: (UserPlan) -> Unit,
    onUpdatePublisherPlan: (PublisherPlan) -> Unit,
    onToggleVerification: () -> Unit,
    onAdjustBalance: () -> Unit,
    onDeleteUser: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile Inspector",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = BentoTextMuted)
                    }
                }

                // User details card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BentoTextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            VerifiedBadge(role = user.role, size = 18)
                        }
                        Text("UID: ${user.uid}", fontSize = 12.sp, color = BrandCyan, fontWeight = FontWeight.SemiBold)
                        Text("Mobile: ${user.mobile}", fontSize = 12.sp, color = BentoTextMuted)
                        Text("Role: ${user.role.name} • Server: ${user.server}", fontSize = 12.sp, color = BentoTextMuted)
                        if (user.teamName.isNotBlank()) Text("Team: ${user.teamName}", fontSize = 12.sp, color = BentoTextMuted)
                        if (user.referralCode.isNotBlank()) Text("Referral Code: ${user.referralCode}", fontSize = 12.sp, color = BrandGold)
                        if (user.organizationName.isNotBlank()) Text("Org: ${user.organizationName}", fontSize = 12.sp, color = BrandGold)
                    }
                }

                // Balance & Financial Metrics
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = BentoSurface)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Current Balance", fontSize = 10.sp, color = BentoTextMuted)
                            Text("${String.format("%,.2f", user.balance)} FCT", fontWeight = FontWeight.Bold, color = BrandEmerald, fontSize = 14.sp)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = BentoSurface)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Total Earnings", fontSize = 10.sp, color = BentoTextMuted)
                            Text("${String.format("%,.2f", user.totalEarnings)} FCT", fontWeight = FontWeight.Bold, color = BrandGold, fontSize = 14.sp)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = BentoSurface)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Withdrawn", fontSize = 10.sp, color = BentoTextMuted)
                            Text("${String.format("%,.2f", user.totalWithdrawn)} FCT", fontWeight = FontWeight.Bold, color = BrandRed, fontSize = 14.sp)
                        }
                    }
                }

                // Plan selection
                Text("Change Subscription Plan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                if (user.role == UserRole.PUBLISHER) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PublisherPlan.values().forEach { pubPlan ->
                            val isSelected = user.publisherPlan == pubPlan
                            Button(
                                onClick = { onUpdatePublisherPlan(pubPlan) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) BrandGold else BentoSurface,
                                    contentColor = if (isSelected) Color.Black else BentoTextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(pubPlan.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        UserPlan.values().take(3).forEach { uPlan ->
                            val isSelected = user.plan == uPlan
                            Button(
                                onClick = { onUpdatePlan(uPlan) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) BrandCyan else BentoSurface,
                                    contentColor = if (isSelected) Color.Black else BentoTextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(uPlan.displayName.split(" ").first(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        UserPlan.values().drop(3).forEach { uPlan ->
                            val isSelected = user.plan == uPlan
                            Button(
                                onClick = { onUpdatePlan(uPlan) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) BrandCyan else BentoSurface,
                                    contentColor = if (isSelected) Color.Black else BentoTextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(uPlan.displayName.split(" ").first(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Actions row
                Button(
                    onClick = onAdjustBalance,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                ) {
                    Icon(Icons.Default.PriceChange, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Adjust Balance (Credit / Debit)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onToggleVerification,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = BrandGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (user.isVerified) "Remove Verified Badge" else "Grant Verified Badge", fontSize = 12.sp)
                }

                if (!showDeleteConfirm) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandRed)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete User Account", fontSize = 12.sp, color = BrandRed)
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BrandRed.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Are you sure? This cannot be undone.", fontSize = 11.sp, color = BrandRed, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onDeleteUser,
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Confirm Delete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                TextButton(onClick = { showDeleteConfirm = false }) {
                                    Text("Cancel", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAdjustBalanceDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onConfirm: (delta: Double, reason: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var isCredit by remember { mutableStateOf(true) }
    var reasonText by remember { mutableStateOf("Manual administrative credit") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Adjust Balance: ${user.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Text("UID: ${user.uid} • Current: ${user.balance} FCT", fontSize = 12.sp, color = BentoTextMuted)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isCredit = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCredit) BrandEmerald else BentoSurface,
                            contentColor = if (isCredit) Color.White else BentoTextMuted
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+ Credit", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { isCredit = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isCredit) BrandRed else BentoSurface,
                            contentColor = if (!isCredit) Color.White else BentoTextMuted
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("- Debit", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount in FCT") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Reason / Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsed = amountText.toDoubleOrNull() ?: 0.0
                            if (parsed > 0) {
                                val delta = if (isCredit) parsed else -parsed
                                onConfirm(delta, reasonText)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isCredit) BrandEmerald else BrandRed)
                    ) {
                        Text("Apply", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
