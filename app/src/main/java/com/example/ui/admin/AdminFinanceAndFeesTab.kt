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
import com.example.ui.theme.*

@Composable
fun AdminFinanceAndFeesTab() {
    val context = LocalContext.current
    val depositRequests by FleearnRepository.depositRequests.collectAsState()
    val withdrawRequests by FleearnRepository.withdrawRequests.collectAsState()
    val transactions by FleearnRepository.transactions.collectAsState()
    val config by FleearnRepository.appConfig.collectAsState()

    var selectedSubSection by remember { mutableStateOf(0) } // 0: Deposits, 1: Withdrawals, 2: Gateways & Fees, 3: Ledger
    val subSections = listOf(
        "Deposits (${depositRequests.count { it.status == "PENDING" }})",
        "Withdrawals (${withdrawRequests.count { it.status == "PENDING" }})",
        "Gateways & Fees",
        "Ledger"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tab row
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
                // DEPOSITS
                if (depositRequests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No deposit requests pending.", color = BentoTextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(depositRequests, key = { it.id }) { req ->
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(BrandCyan.copy(alpha = 0.2f))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(req.method, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("TXID: ${req.transactionId}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BentoTextPrimary)
                                            }

                                            Text("+${String.format("%,.2f", req.amount)} FCT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandEmerald)
                                        }

                                        Text("User: ${req.userName} • UID: ${req.userUid}", fontSize = 11.sp, color = BentoTextMuted)
                                        Text("Sender Phone: ${req.senderNumber}", fontSize = 11.sp, color = BrandGold)

                                        if (req.status == "PENDING") {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        FleearnRepository.approveDeposit(req.id)
                                                        Toast.makeText(context, "Deposit approved and balance credited!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Approve & Credit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Button(
                                                    onClick = {
                                                        FleearnRepository.rejectDeposit(req.id)
                                                        Toast.makeText(context, "Deposit rejected", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        } else {
                                            Text("Status: ${req.status}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (req.status == "APPROVED") BrandEmerald else BrandRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // WITHDRAWALS
                if (withdrawRequests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No withdrawal requests pending.", color = BentoTextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(withdrawRequests, key = { it.id }) { req ->
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
                                            Text(req.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                                            Text("Payout: ${req.netAmount} FCT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandRed)
                                        }

                                        Text("UID: ${req.userUid} (${req.userRole}) • Method: ${req.method}", fontSize = 11.sp, color = BentoTextMuted)
                                        Text("Wallet Number: ${req.walletNumber}", fontSize = 11.sp, color = BrandGold, fontWeight = FontWeight.SemiBold)
                                        Text("Gross: ${req.amount} FCT • Platform Fee: ${req.fee} FCT", fontSize = 10.sp, color = BrandCyan)

                                        if (req.status == "PENDING") {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        FleearnRepository.approveWithdraw(req.id)
                                                        Toast.makeText(context, "Withdrawal approved and paid out!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Approve Payout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Button(
                                                    onClick = {
                                                        FleearnRepository.rejectWithdraw(req.id)
                                                        Toast.makeText(context, "Withdrawal rejected & refunded", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Reject & Refund", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        } else {
                                            Text("Status: ${req.status}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (req.status == "APPROVED") BrandEmerald else BrandRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // GATEWAYS & FEES CONFIGURATION
                var bKashNum by remember { mutableStateOf(config.bKashNumber) }
                var nagadNum by remember { mutableStateOf(config.nagadNumber) }
                var upayNum by remember { mutableStateOf(config.upayNumber) }
                var sendFee by remember { mutableStateOf(config.sendMoneyFee.toString()) }
                var rechargeFee by remember { mutableStateOf(config.mobileRechargeFee.toString()) }
                var partnerFee by remember { mutableStateOf(config.partnerProgramFee.toString()) }
                var userWithdrawFee by remember { mutableStateOf(config.userWithdrawalFeePercent.toString()) }
                var modWithdrawFee by remember { mutableStateOf(config.moderatorWithdrawalFeePercent.toString()) }
                var jobFee by remember { mutableStateOf(config.jobPostingFee.toString()) }
                var prodFee by remember { mutableStateOf(config.productPostingFee.toString()) }
                var srvFee by remember { mutableStateOf(config.servicePostingFee.toString()) }
                var deliveryFee by remember { mutableStateOf(config.productDeliveryFee.toString()) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Official Deposit Gateways", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandCyan)

                            OutlinedTextField(
                                value = bKashNum,
                                onValueChange = { bKashNum = it },
                                label = { Text("bKash Merchant / Agent Number") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = nagadNum,
                                onValueChange = { nagadNum = it },
                                label = { Text("Nagad Merchant / Agent Number") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = upayNum,
                                onValueChange = { upayNum = it },
                                label = { Text("Upay Agent Number") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Fleearn Charges & Commission Schedule", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandGold)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = sendFee,
                                    onValueChange = { sendFee = it },
                                    label = { Text("Send Money Fee (FCT)") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = rechargeFee,
                                    onValueChange = { rechargeFee = it },
                                    label = { Text("Recharge Fee (FCT)") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = userWithdrawFee,
                                    onValueChange = { userWithdrawFee = it },
                                    label = { Text("User Withdraw %") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = modWithdrawFee,
                                    onValueChange = { modWithdrawFee = it },
                                    label = { Text("Mod Withdraw %") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = jobFee,
                                    onValueChange = { jobFee = it },
                                    label = { Text("Job Post Fee") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = prodFee,
                                    onValueChange = { prodFee = it },
                                    label = { Text("Product Post Fee") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = srvFee,
                                    onValueChange = { srvFee = it },
                                    label = { Text("Service Post Fee") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = deliveryFee,
                                    onValueChange = { deliveryFee = it },
                                    label = { Text("Delivery Charge") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            OutlinedTextField(
                                value = partnerFee,
                                onValueChange = { partnerFee = it },
                                label = { Text("Partner Program Fee (FCT)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val updatedConfig = config.copy(
                                bKashNumber = bKashNum,
                                nagadNumber = nagadNum,
                                upayNumber = upayNum,
                                sendMoneyFee = sendFee.toDoubleOrNull() ?: 5.0,
                                mobileRechargeFee = rechargeFee.toDoubleOrNull() ?: 9.0,
                                partnerProgramFee = partnerFee.toDoubleOrNull() ?: 500.0,
                                userWithdrawalFeePercent = userWithdrawFee.toDoubleOrNull() ?: 2.0,
                                moderatorWithdrawalFeePercent = modWithdrawFee.toDoubleOrNull() ?: 1.0,
                                jobPostingFee = jobFee.toDoubleOrNull() ?: 25.0,
                                productPostingFee = prodFee.toDoubleOrNull() ?: 15.0,
                                servicePostingFee = srvFee.toDoubleOrNull() ?: 20.0,
                                productDeliveryFee = deliveryFee.toDoubleOrNull() ?: 200.0
                            )
                            FleearnRepository.updateAppConfig(updatedConfig)
                            Toast.makeText(context, "Financial configuration updated globally", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                    ) {
                        Text("Save Gateway & Fee Settings", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            3 -> {
                // LEDGER TRANSACTIONS
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
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
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(tx.type, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandCyan)
                                        Text("User: ${tx.userUid} • ${tx.details}", fontSize = 10.sp, color = BentoTextMuted)
                                    }
                                    Text("${tx.amount} FCT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
