package com.example.ui.publisher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.components.InAppBrowserManager
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*

@Composable
fun PublisherDashboardTab(
    publisher: UserProfile,
    config: SystemAppConfig,
    ads: List<AdItem>,
    jobs: List<JobItem>,
    products: List<ProductItem>,
    services: List<ServiceItem>,
    submissions: List<JobSubmission>,
    productOrders: List<ProductOrder>,
    serviceOrders: List<ServiceOrder>,
    onNavigateCategory: (PublisherNavCategory) -> Unit,
    onCreateAdClick: () -> Unit,
    onCreateJobClick: () -> Unit,
    onPostProductClick: () -> Unit,
    onPostServiceClick: () -> Unit,
    onDepositClick: () -> Unit
) {
    val myAds = ads.filter { it.publisherUid == publisher.uid }
    val activeAds = myAds.filter { it.isActive }
    val myJobs = jobs.filter { it.publisherUid == publisher.uid }
    val myProducts = products.filter { it.publisherUid == publisher.uid }
    val myServices = services.filter { it.publisherUid == publisher.uid }
    val pendingSubmissions = submissions.filter { it.publisherUid == publisher.uid && it.status == "PENDING" }
    val pendingProductOrders = productOrders.filter { it.publisherUid == publisher.uid && it.status == "PENDING" }
    val pendingServiceOrders = serviceOrders.filter { it.publisherUid == publisher.uid && it.status == "PENDING" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Publisher Profile & Wallet Balance Card
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
                                colors = listOf(
                                    BrandPurple.copy(alpha = 0.15f),
                                    BrandCyan.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        // Top Identity Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (publisher.organizationLogoUrl.isNotBlank() || publisher.profilePhotoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = publisher.organizationLogoUrl.ifEmpty { publisher.profilePhotoUrl },
                                        contentDescription = "Publisher Logo",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, BrandCyan, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(BrandCyan.copy(alpha = 0.2f))
                                            .border(1.5.dp, BrandCyan, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Business, contentDescription = null, tint = BrandCyan)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = publisher.organizationName.ifEmpty { publisher.name },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        VerifiedBadge(role = UserRole.PUBLISHER, size = 16)
                                    }
                                    Text(
                                        text = "Publisher UID: ${publisher.uid}",
                                        fontSize = 12.sp,
                                        color = BentoTextMuted
                                    )
                                }
                            }

                            // Plan Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (publisher.publisherPlan == PublisherPlan.BLAZE) BrandGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (publisher.publisherPlan == PublisherPlan.BLAZE) BrandGold else Color.Gray.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (publisher.publisherPlan == PublisherPlan.BLAZE) Icons.Default.LocalFireDepartment else Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = if (publisher.publisherPlan == PublisherPlan.BLAZE) BrandGold else BrandCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = publisher.publisherPlan.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (publisher.publisherPlan == PublisherPlan.BLAZE) BrandGold else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Balance & Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "PUBLISHER WALLET BALANCE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoTextMuted,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "%.2f".format(publisher.balance),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandCyan
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "FCT",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 3.dp)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onDepositClick,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.AddCard, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Deposit", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = BrandPurple.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandPurple.copy(alpha = 0.4f)),
                                    modifier = Modifier.clickable { onNavigateCategory(PublisherNavCategory.ACCOUNT) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${publisher.activeStreak}d Streak", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandPurple)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Publisher Actions Speed Bar
        item {
            Text(
                text = "Campaign Creation Hub",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionChip(
                    title = "Create Ad",
                    icon = Icons.Default.Campaign,
                    color = BrandCyan,
                    modifier = Modifier.weight(1f),
                    onClick = onCreateAdClick
                )
                QuickActionChip(
                    title = "Create Job",
                    icon = Icons.Default.PostAdd,
                    color = BrandGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onCreateJobClick
                )
                QuickActionChip(
                    title = "Post Product",
                    icon = Icons.Default.AddBusiness,
                    color = BrandPurple,
                    modifier = Modifier.weight(1f),
                    onClick = onPostProductClick
                )
                QuickActionChip(
                    title = "Post Service",
                    icon = Icons.Default.DesignServices,
                    color = BrandGold,
                    modifier = Modifier.weight(1f),
                    onClick = onPostServiceClick
                )
            }
        }

        // Real-Time Publisher Metrics Grid
        item {
            Text(
                text = "Publisher Analytics & Overview",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PublisherMetricCard(
                        title = "Total Campaigns",
                        value = "${myAds.size}",
                        subtext = "${activeAds.size} Active",
                        icon = Icons.Default.Campaign,
                        iconTint = BrandCyan,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateCategory(PublisherNavCategory.CAMPAIGNS) }
                    )
                    PublisherMetricCard(
                        title = "Active Jobs",
                        value = "${myJobs.size}",
                        subtext = "${myJobs.sumOf { it.remainingSlots }} Slots Open",
                        icon = Icons.Default.Work,
                        iconTint = BrandGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateCategory(PublisherNavCategory.JOBS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PublisherMetricCard(
                        title = "Active Products",
                        value = "${myProducts.size}",
                        subtext = "Drop Shipping",
                        icon = Icons.Default.Storefront,
                        iconTint = BrandPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateCategory(PublisherNavCategory.PRODUCTS) }
                    )
                    PublisherMetricCard(
                        title = "Active Services",
                        value = "${myServices.size}",
                        subtext = "Services Hub",
                        icon = Icons.Default.MiscellaneousServices,
                        iconTint = BrandGold,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateCategory(PublisherNavCategory.SERVICES) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PublisherMetricCard(
                        title = "Job Submissions",
                        value = "${submissions.filter { it.publisherUid == publisher.uid }.size}",
                        subtext = "${pendingSubmissions.size} Pending Review",
                        icon = Icons.Default.AssignmentTurnedIn,
                        iconTint = if (pendingSubmissions.isNotEmpty()) BrandRed else BrandCyan,
                        badge = if (pendingSubmissions.isNotEmpty()) "${pendingSubmissions.size} NEW" else null,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateCategory(PublisherNavCategory.SUBMISSIONS) }
                    )
                    PublisherMetricCard(
                        title = "Product Orders",
                        value = "${productOrders.filter { it.publisherUid == publisher.uid }.size}",
                        subtext = "${pendingProductOrders.size} Pending",
                        icon = Icons.Default.LocalShipping,
                        iconTint = if (pendingProductOrders.isNotEmpty()) BrandGold else BrandGreen,
                        badge = if (pendingProductOrders.isNotEmpty()) "${pendingProductOrders.size}" else null,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateCategory(PublisherNavCategory.PRODUCT_ORDERS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PublisherMetricCard(
                        title = "Service Orders",
                        value = "${serviceOrders.filter { it.publisherUid == publisher.uid }.size}",
                        subtext = "${pendingServiceOrders.size} In Progress",
                        icon = Icons.Default.ReceiptLong,
                        iconTint = BrandCyan,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateCategory(PublisherNavCategory.SERVICE_ORDERS) }
                    )
                    PublisherMetricCard(
                        title = "Ad Impressions",
                        value = "${myAds.sumOf { it.currentViews }}",
                        subtext = "${myAds.sumOf { it.clicks }} Clicks Recorded",
                        icon = Icons.Default.Visibility,
                        iconTint = BrandGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateCategory(PublisherNavCategory.CAMPAIGNS) }
                    )
                }
            }
        }

        // Publishers Association Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        InAppBrowserManager.openUrl(
                            config.publisherAssociationUrl,
                            "Publishers Association of Fleearn"
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandGold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = BrandGold, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Publishers Association", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "Join the official network of certified publishers, access escrow guarantees & publisher webinars.",
                            fontSize = 11.sp,
                            color = BentoTextMuted
                        )
                    }
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = BrandGold, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun QuickActionChip(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(14.dp),
        color = BentoSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun PublisherMetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    badge: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }

                if (badge != null) {
                    Surface(
                        color = BrandRed,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = subtext,
                fontSize = 10.sp,
                color = BentoTextMuted,
                maxLines = 1
            )
        }
    }
}
