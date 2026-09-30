package com.example.ui.publisher

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DepositRequest
import com.example.data.model.SystemAppConfig
import com.example.data.model.UserProfile
import com.example.data.repository.FleearnRepository
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PublisherDepositTab(
    publisher: UserProfile,
    config: SystemAppConfig,
    deposits: List<DepositRequest>,
    onSuccess: (String, String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())

    var selectedMethod by remember { mutableStateOf("bKash") }
    var amountText by remember { mutableStateOf("100") }
    var senderNumber by remember { mutableStateOf(publisher.mobile) }
    var txid by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    val myDeposits = deposits.filter { it.userUid == publisher.uid }

    val activeNumber = when (selectedMethod) {
        "bKash" -> config.bKashNumber
        "Nagad" -> config.nagadNumber
        else -> config.upayNumber
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Balance & Info Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Publisher Deposit Gateway", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Deposit funds to launch ad campaigns, create micro-jobs and list products.", fontSize = 11.sp, color = BentoTextMuted)

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("CURRENT BALANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoTextMuted)
                                Text("%.2f FCT".format(publisher.balance), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BrandGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "1 FCT = 1 BDT",
                                    color = BrandGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive Deposit Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Step 1: Select Payment Gateway", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("bKash" to BrandPink, "Nagad" to BrandOrange, "Upay" to BrandCyan).forEach { (method, color) ->
                            val isSelected = selectedMethod == method
                            Surface(
                                onClick = { selectedMethod = method },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) color else Color.Transparent
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = method,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Merchant / Agent Number Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Send Money / Cash In Number:", fontSize = 10.sp, color = BentoTextMuted)
                                Text(activeNumber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(activeNumber.substringBefore(" ")))
                                    Toast.makeText(context, "Number copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BrandCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Step 2: Payment Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Minimum Deposit: 100 FCT", fontSize = 11.sp, color = BrandGold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Deposit Amount (FCT)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = senderNumber,
                        onValueChange = { senderNumber = it },
                        label = { Text("Sender Mobile Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = txid,
                        onValueChange = { txid = it },
                        label = { Text("Transaction ID (TrxID)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (errorMsg.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(errorMsg, color = BrandRed, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount < 100.0) {
                                errorMsg = "Minimum deposit amount is 100 FCT."
                                return@Button
                            }
                            if (senderNumber.trim().length < 8) {
                                errorMsg = "Please provide a valid sender mobile number."
                                return@Button
                            }
                            if (txid.trim().length < 4) {
                                errorMsg = "Please enter valid Transaction ID."
                                return@Button
                            }
                            errorMsg = ""
                            val reqId = FleearnRepository.requestDeposit(
                                amount = amount,
                                method = selectedMethod,
                                senderNumber = senderNumber.trim(),
                                txid = txid.trim().uppercase()
                            )
                            txid = ""
                            onSuccess("Deposit Requested", "Request #$reqId submitted! Status: PENDING verification (usually 3-5 minutes).")
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Deposit Request", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Recent Deposits List
        item {
            Text("Deposit History & Status", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BentoTextMuted)
        }

        if (myDeposits.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No deposit records found.", fontSize = 12.sp, color = BentoTextMuted)
                    }
                }
            }
        } else {
            items(myDeposits, key = { it.id }) { dep ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(dep.method, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TxID: ${dep.transactionId}", fontSize = 11.sp, color = BrandCyan)
                            }
                            Text("Sender: ${dep.senderNumber}", fontSize = 11.sp, color = BentoTextMuted)
                            Text(dateFormat.format(Date(dep.timestamp)), fontSize = 10.sp, color = BentoTextMuted)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("+%.2f FCT".format(dep.amount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandGreen)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (dep.status) {
                                    "APPROVED" -> BrandGreen.copy(alpha = 0.2f)
                                    "REJECTED" -> BrandRed.copy(alpha = 0.2f)
                                    else -> BrandGold.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = dep.status,
                                    color = when (dep.status) {
                                        "APPROVED" -> BrandGreen
                                        "REJECTED" -> BrandRed
                                        else -> BrandGold
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
