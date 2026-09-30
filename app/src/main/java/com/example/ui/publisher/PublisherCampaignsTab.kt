package com.example.ui.publisher

import androidx.compose.foundation.background
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
import com.example.data.model.AdItem
import com.example.data.model.UserProfile
import com.example.ui.components.InAppBrowserManager
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PublisherCampaignsTab(
    publisher: UserProfile,
    ads: List<AdItem>,
    onCreateAdClick: () -> Unit
) {
    val myAds = ads.filter { it.publisherUid == publisher.uid }
    val totalViews = myAds.sumOf { it.currentViews }
    val totalClicks = myAds.sumOf { it.clicks }
    val avgCtr = if (totalViews > 0) (totalClicks.toDouble() / totalViews * 100) else 0.0

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Analytics Summary Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Campaign Analytics", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Button(
                            onClick = onCreateAdClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Campaign", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalyticsMiniBox(
                            title = "Total Views",
                            value = "$totalViews",
                            icon = Icons.Default.Visibility,
                            color = BrandCyan,
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsMiniBox(
                            title = "Total Clicks",
                            value = "$totalClicks",
                            icon = Icons.Default.TouchApp,
                            color = BrandGreen,
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsMiniBox(
                            title = "Avg. CTR",
                            value = "%.2f%%".format(avgCtr),
                            icon = Icons.Default.TrendingUp,
                            color = BrandGold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "My Advertisements (${myAds.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextMuted
            )
        }

        if (myAds.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Active Ad Campaigns", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Launch high-conversion 320x150 banner ads to promote your products, channels or services across Fleearn.",
                                fontSize = 12.sp,
                                color = BentoTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onCreateAdClick,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                            ) {
                                Text("Launch 320x150 Ad (100 FCT)", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(myAds, key = { it.id }) { ad ->
                val startDate = Date(ad.createdAt)
                val cal = Calendar.getInstance().apply {
                    time = startDate
                    add(Calendar.DAY_OF_YEAR, ad.durationDays)
                }
                val endDate = cal.time
                val progress = if (ad.targetViews > 0) (ad.currentViews.toFloat() / ad.targetViews).coerceIn(0f, 1f) else 0f
                val ctr = if (ad.currentViews > 0) (ad.clicks.toDouble() / ad.currentViews * 100) else 0.0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Banner preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { InAppBrowserManager.openUrl(ad.destinationUrl, ad.publisherName) }
                        ) {
                            AsyncImage(
                                model = ad.imageUrl,
                                contentDescription = "Ad Banner",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Surface(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.TopEnd),
                                shape = RoundedCornerShape(6.dp),
                                color = if (ad.isActive) BrandGreen else BrandGold
                            ) {
                                Text(
                                    text = if (ad.isActive) "ACTIVE" else "ENDED",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Campaign #${ad.id}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "Duration: ${ad.durationDays} Days",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Destination: ${ad.destinationUrl}",
                            fontSize = 11.sp,
                            color = BrandCyan,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar for impressions
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Impressions: ${ad.currentViews} / ${ad.targetViews}", fontSize = 11.sp, color = BentoTextMuted)
                                Text("${(progress * 100).toInt()}% Delivered", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = BrandCyan,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Metrics row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Clicks: ${ad.clicks}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("CTR: %.2f%%".format(ctr), fontSize = 11.sp, color = BrandGreen)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Start: ${dateFormat.format(startDate)}", fontSize = 11.sp, color = BentoTextMuted)
                                Text("End: ${dateFormat.format(endDate)}", fontSize = 11.sp, color = BentoTextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsMiniBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(title, fontSize = 10.sp, color = BentoTextMuted)
        }
    }
}
