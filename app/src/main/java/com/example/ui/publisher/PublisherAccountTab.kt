package com.example.ui.publisher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PublisherPlan
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.FleearnRepository
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*

@Composable
fun PublisherAccountTab(
    publisher: UserProfile,
    onSuccess: (String, String) -> Unit,
    onError: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isBlaze = publisher.publisherPlan == PublisherPlan.BLAZE

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current Plan Status Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = if (isBlaze) {
                                    listOf(BrandGold.copy(alpha = 0.2f), BrandPurple.copy(alpha = 0.1f), Color.Transparent)
                                } else {
                                    listOf(BrandCyan.copy(alpha = 0.15f), Color.Transparent)
                                }
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CURRENT PUBLISHER MEMBERSHIP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoTextMuted,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isBlaze) "Blaze Plan" else "Spark Plan",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isBlaze) BrandGold else BrandCyan
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (isBlaze) {
                                        Surface(
                                            color = BrandGold.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandGold)
                                        ) {
                                            Text(
                                                text = "LIFETIME ACCESS",
                                                color = BrandGold,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(if (isBlaze) BrandGold.copy(alpha = 0.2f) else BrandCyan.copy(alpha = 0.2f))
                                    .border(1.5.dp, if (isBlaze) BrandGold else BrandCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isBlaze) Icons.Default.LocalFireDepartment else Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = if (isBlaze) BrandGold else BrandCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            VerifiedBadge(role = UserRole.PUBLISHER, size = 18)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBlaze) "Verified Blaze Publisher Status (Lifetime)" else "Standard Spark Publisher Status",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Plan Comparison Cards
        item {
            Text(
                text = "Publisher Plans Comparison",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Spark Plan Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (!isBlaze) BrandCyan.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Spark Plan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Text("Free (0 FCT)", fontWeight = FontWeight.Bold, color = BrandCyan, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        PlanFeatureItem(text = "Standard micro-job publishing", included = true)
                        PlanFeatureItem(text = "Drop shipping product cataloging", included = true)
                        PlanFeatureItem(text = "Standard 24-48h moderation queue", included = true)
                        PlanFeatureItem(text = "Zero lifetime priority listing", included = false)
                        PlanFeatureItem(text = "Dedicated Account Manager", included = false)
                    }
                }

                // Blaze Plan Card (Highlight)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isBlaze) BrandGold else BrandGold.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(BrandGold.copy(alpha = 0.12f), Color.Transparent)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = BrandGold, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Blaze Plan", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BrandGold)
                                        Text("Lifetime Access", fontSize = 11.sp, color = BentoTextMuted)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("99 FCT", fontWeight = FontWeight.ExtraBold, color = BrandGold, fontSize = 20.sp)
                                    Text("One-Time Only", fontSize = 10.sp, color = BentoTextMuted)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text("Blaze Plan Benefits:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            PlanFeatureItem(text = "Lifetime Verified Publisher Golden Badge", included = true, highlight = true)
                            PlanFeatureItem(text = "Zero Fee on Micro-Job Moderations", included = true, highlight = true)
                            PlanFeatureItem(text = "Instant 5-Minute Proof Auto-Verification Protocol", included = true, highlight = true)
                            PlanFeatureItem(text = "Top Placement on Resell & Shipping Marketplace", included = true, highlight = true)
                            PlanFeatureItem(text = "Unlimited Active 320x150 Ad Campaigns", included = true, highlight = true)
                            PlanFeatureItem(text = "Priority 24/7 AI Support & Dedicated Moderator Escalation", included = true, highlight = true)

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isBlaze) {
                                Button(
                                    onClick = {},
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(disabledContainerColor = BrandGold.copy(alpha = 0.25f))
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandGold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Currently Active (Lifetime Plan)", color = BrandGold, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val (success, msg) = FleearnRepository.upgradePublisherBlaze()
                                            if (success) {
                                                onSuccess("Upgraded to Blaze", msg)
                                            } else {
                                                onError(msg)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandGold)
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Upgrade to Blaze (99 FCT)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
fun PlanFeatureItem(text: String, included: Boolean, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (included) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (included) {
                if (highlight) BrandGold else BrandGreen
            } else Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (included) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = if (highlight) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
