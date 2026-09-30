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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.data.repository.FleearnRepository
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminModeratorTab(
    onOpenCreateMod: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val users by FleearnRepository.users.collectAsState()
    val salaryRecords by FleearnRepository.salaryRecords.collectAsState()
    val config by FleearnRepository.appConfig.collectAsState()

    val moderators = users.filter { it.role == UserRole.MODERATOR }
    var selectedModForSalary by remember { mutableStateOf<UserProfile?>(null) }
    var selectedModDetails by remember { mutableStateOf<UserProfile?>(null) }
    var instructionsText by remember { mutableStateOf("") }
    val repoInstructions by FleearnRepository.moderatorInstructions.collectAsState()

    LaunchedEffect(repoInstructions) {
        instructionsText = repoInstructions
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BentoBorderSubtle, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = BrandRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MODERATOR FLEET (MX)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BentoTextPrimary)
                        }
                        Text("${moderators.size} Active Officers • 24/7 SLA", fontSize = 11.sp, color = BentoTextMuted)
                    }

                    Button(
                        onClick = onOpenCreateMod,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create MX", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Moderator Operational Directives
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Moderator Operational Guidelines & Protocol", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandGold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = instructionsText,
                    onValueChange = { instructionsText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BentoSurface,
                        unfocusedContainerColor = BentoSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = {
                            FleearnRepository.updateModeratorInstructions(instructionsText)
                            Toast.makeText(context, "Directives updated for all MX staff", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                    ) {
                        Text("Save Directives", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        Text("Active Moderator Officers", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)

        // Moderator Officers List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(moderators, key = { it.uid }) { mod ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedModDetails = mod },
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
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(BrandRed.copy(alpha = 0.15f))
                                        .border(1.dp, BrandRed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = BrandRed, modifier = Modifier.size(20.dp))
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(mod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BentoTextPrimary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        VerifiedBadge(role = mod.role, size = 16)
                                    }
                                    Text("UID: ${mod.uid} • PIN: ${mod.pinHash}", fontSize = 11.sp, color = BrandGold, fontWeight = FontWeight.SemiBold)
                                    Text("Mobile: ${mod.mobile}", fontSize = 11.sp, color = BentoTextMuted)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (mod.isSuspended) BrandRed.copy(alpha = 0.2f) else BrandEmerald.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        if (mod.isSuspended) "SUSPENDED" else "ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (mod.isSuspended) BrandRed else BrandEmerald
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
                                    Text("Salary Balance", fontSize = 10.sp, color = BentoTextMuted)
                                    Text("${String.format("%,.2f", mod.balance)} FCT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandEmerald)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { selectedModForSalary = mod },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Send Salary", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(
                                        onClick = {
                                            if (mod.isSuspended) {
                                                FleearnRepository.reactivateUser(mod.uid)
                                                Toast.makeText(context, "Moderator reactivated", Toast.LENGTH_SHORT).show()
                                            } else {
                                                FleearnRepository.suspendUser(mod.uid)
                                                Toast.makeText(context, "Moderator suspended", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            if (mod.isSuspended) Icons.Default.LockOpen else Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = if (mod.isSuspended) BrandEmerald else BrandRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Salary Dispatch History Section
        if (salaryRecords.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Recent Salary Dispatches", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BentoTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    salaryRecords.take(3).forEach { rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(rec.moderatorName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BentoTextPrimary)
                                Text("UID: ${rec.moderatorUid}", fontSize = 10.sp, color = BentoTextMuted)
                            }
                            Text("+${String.format("%,.0f", rec.amount)} FCT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandEmerald)
                        }
                    }
                }
            }
        }
    }

    // Send Salary Dialog
    selectedModForSalary?.let { mod ->
        AdminSendSalaryDialog(
            moderator = mod,
            onDismiss = { selectedModForSalary = null },
            onConfirm = { amount ->
                coroutineScope.launch {
                    val res = FleearnRepository.sendModeratorSalary(mod.uid, amount)
                    Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                    if (res.first) selectedModForSalary = null
                }
            }
        )
    }
}

@Composable
fun AdminSendSalaryDialog(
    moderator: UserProfile,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var salaryAmountText by remember { mutableStateOf("5000") }
    val config by FleearnRepository.appConfig.collectAsState()

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
                    text = "Dispatch Salary to Moderator",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Text("Officer: ${moderator.name} (${moderator.uid})", fontSize = 12.sp, color = BentoTextMuted)
                Text("Available Platform Profit: ${String.format("%,.2f", config.platformProfit)} FCT", fontSize = 11.sp, color = BrandGold)

                OutlinedTextField(
                    value = salaryAmountText,
                    onValueChange = { salaryAmountText = it },
                    label = { Text("Salary Amount (FCT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            val amount = salaryAmountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0) onConfirm(amount)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                    ) {
                        Text("Confirm Transfer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
