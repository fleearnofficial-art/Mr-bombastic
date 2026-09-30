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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.data.repository.FleearnRepository
import com.example.ui.components.InAppBrowserManager
import com.example.ui.theme.*

@Composable
fun AdminProductsAndServicesTab() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val products by FleearnRepository.products.collectAsState()
    val productOrders by FleearnRepository.productOrders.collectAsState()
    val shippingBanners by FleearnRepository.shippingBanners.collectAsState()
    val services by FleearnRepository.services.collectAsState()
    val serviceOrders by FleearnRepository.serviceOrders.collectAsState()
    val config by FleearnRepository.appConfig.collectAsState()

    var selectedSubSection by remember { mutableStateOf(0) } // 0: Products, 1: Orders, 2: Shipping Banners, 3: Services
    val subSections = listOf("Products (${products.size})", "Orders (${productOrders.size})", "Banners (${shippingBanners.size})", "Services (${services.size})")

    var showAddBannerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tab Row
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
                // PRODUCTS CATALOG
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { prod ->
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AsyncImage(
                                        model = prod.thumbnails.firstOrNull() ?: "",
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(BentoSurface),
                                        contentScale = ContentScale.Crop
                                    )

                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary, maxLines = 1)
                                        Text("By: ${prod.publisherOrgName}", fontSize = 11.sp, color = BrandGold)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("Admin: ${prod.adminPrice} FCT", fontSize = 11.sp, color = BentoTextMuted)
                                            Text("Resell: ${prod.resellPrice} FCT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandEmerald)
                                        }
                                        Text("Profit Margin: ${prod.resellPrice - prod.adminPrice} FCT • Stock: ${prod.stock}", fontSize = 10.sp, color = BrandCyan)
                                    }

                                    IconButton(
                                        onClick = {
                                            FleearnRepository.rejectProduct(prod.id)
                                            Toast.makeText(context, "Product removed", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = BrandRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // PRODUCT ORDERS
                if (productOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No drop shipping orders placed yet.", color = BentoTextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(productOrders, key = { it.id }) { order ->
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
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(order.productTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary, modifier = Modifier.weight(1f))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(BrandCyan.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(order.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                                            }
                                        }

                                        Text("Receiver: ${order.receiverName} (${order.receiverMobile})", fontSize = 11.sp, color = BentoTextMuted)
                                        Text("Address: ${order.receiverAddress}", fontSize = 11.sp, color = BentoTextMuted)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Delivery: ${order.deliveryCharge} FCT", fontSize = 11.sp, color = BentoTextMuted)
                                            Text("User Profit: +${order.profit} FCT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandEmerald)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // SHIPPING BANNERS (Max 10 banners / 3-sec timer)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Shipping Carousel Banners", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                            Text("Max 10 banners • 3s automated rotation timer", fontSize = 11.sp, color = BentoTextMuted)
                        }

                        Button(
                            onClick = { showAddBannerDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Banner", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(shippingBanners, key = { it.id }) { banner ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, BentoBorderSubtle, RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = banner.imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(banner.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                                            Text(banner.destinationUrl, fontSize = 11.sp, color = BrandCyan, maxLines = 1)
                                        }

                                        IconButton(
                                            onClick = { InAppBrowserManager.openUrl(banner.destinationUrl, banner.title) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Language, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                FleearnRepository.removeShippingBanner(banner.id)
                                                Toast.makeText(context, "Banner removed", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = BrandRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // SERVICES LIST
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(services, key = { it.id }) { srv ->
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
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(srv.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoTextPrimary)
                                        Text("${srv.price} FCT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandEmerald)
                                    }
                                    Text(srv.description, fontSize = 11.sp, color = BentoTextMuted)
                                    Text("Offered by: ${srv.publisherName}", fontSize = 11.sp, color = BrandGold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Banner Dialog
    if (showAddBannerDialog) {
        var bannerTitle by remember { mutableStateOf("") }
        var bannerImageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=600") }
        var bannerUrl by remember { mutableStateOf("https://fleearn.example.com/shipping-deals") }

        Dialog(onDismissRequest = { showAddBannerDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Add Carousel Shipping Banner", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                    Text("Max 10 active banners (3s automated interval)", fontSize = 11.sp, color = BentoTextMuted)

                    OutlinedTextField(
                        value = bannerTitle,
                        onValueChange = { bannerTitle = it },
                        label = { Text("Banner Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = bannerImageUrl,
                        onValueChange = { bannerImageUrl = it },
                        label = { Text("Image URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = bannerUrl,
                        onValueChange = { bannerUrl = it },
                        label = { Text("Destination URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddBannerDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (bannerTitle.isNotBlank() && bannerImageUrl.isNotBlank()) {
                                    coroutineScope.launch {
                                        val res = FleearnRepository.addShippingBanner(bannerImageUrl, bannerUrl, bannerTitle)
                                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                                        if (res.first) showAddBannerDialog = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                        ) {
                            Text("Publish Banner", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
