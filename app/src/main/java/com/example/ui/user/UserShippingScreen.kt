package com.example.ui.user

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.ProductItem
import com.example.data.repository.FleearnRepository
import com.example.ui.components.AdBannerBox
import com.example.ui.components.InAppBrowserManager
import com.example.ui.components.Success3DDialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun UserShippingScreen() {
    val context = LocalContext.current
    val products by FleearnRepository.products.collectAsState()
    val banners by FleearnRepository.shippingBanners.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedProductForDetail by remember { mutableStateOf<ProductItem?>(null) }
    var showSuccessDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    val categories = listOf("All", "Electronics", "Wearables", "Fashion", "Home", "Saved")

    // 10-Banner Carousel 3-second auto-rotation
    var currentBannerIndex by remember { mutableStateOf(0) }
    LaunchedEffect(banners.size) {
        if (banners.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(3000)
            if (banners.isNotEmpty()) {
                currentBannerIndex = (currentBannerIndex + 1) % banners.size
            }
        }
    }

    val filteredProducts = products.filter {
        when (selectedCategory) {
            "All" -> true
            "Saved" -> it.isSaved
            else -> it.category.equals(selectedCategory, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 10-Banner Carousel (3-second auto-rotation)
        item {
            if (banners.isNotEmpty()) {
                val currentBanner = banners[currentBannerIndex.coerceIn(0, banners.size - 1)]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            InAppBrowserManager.openUrl(currentBanner.destinationUrl, currentBanner.title)
                        },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    AnimatedContent(
                        targetState = currentBanner,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "banner_transition"
                    ) { banner ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = banner.imageUrl,
                                contentDescription = banner.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "PROMO BANNER • 3s Auto-Carousel",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandCyan
                                )
                                Text(
                                    text = banner.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sponsored Ad Box (7s rotation)
        item {
            Spacer(modifier = Modifier.height(12.dp))
            AdBannerBox()
        }

        // Categories
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Drop Shipping & Resell Marketplace",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
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
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 2-Column Products Grid
        val chunkedProducts = filteredProducts.chunked(2)
        items(chunkedProducts.size) { rowIndex ->
            val rowItems = chunkedProducts[rowIndex]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (prod in rowItems) {
                    ProductGridCard(
                        product = prod,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedProductForDetail = prod },
                        onToggleSave = { FleearnRepository.toggleSaveProduct(prod.id) }
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    selectedProductForDetail?.let { prod ->
        ProductDetailDialog(
            product = prod,
            onDismiss = { selectedProductForDetail = null },
            onOrderPlaced = { msg ->
                selectedProductForDetail = null
                showSuccessDialog = "Order Placed" to msg
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
fun ProductGridCard(
    product: ProductItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onToggleSave: () -> Unit
) {
    val profit = product.resellPrice - product.adminPrice
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                AsyncImage(
                    model = product.thumbnails.firstOrNull() ?: "",
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Profit tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(BrandGreen)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "+$profit FCT Profit",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Save button
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (product.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (product.isSaved) BrandGold else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Wholesale", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${product.adminPrice} FCT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Resell Price", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${product.resellPrice} FCT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailDialog(
    product: ProductItem,
    onDismiss: () -> Unit,
    onOrderPlaced: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var receiverName by remember { mutableStateOf("") }
    var receiverMobile by remember { mutableStateOf("") }
    var receiverAddress by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val profit = product.resellPrice - product.adminPrice
    val deliveryCharge = 200.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Drop Shipping Order", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        AsyncImage(
                            model = product.thumbnails.firstOrNull() ?: "",
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    item {
                        Text(product.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(product.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = product.publisherOrgLogo,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Supplier: ${product.publisherOrgName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Phone: ${product.publisherMobile} • In Stock: ${product.stock}", fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = BrandGreen.copy(alpha = 0.15f))) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Wholesale Base:", fontSize = 12.sp)
                                    Text("${product.adminPrice} FCT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Customer Selling Price:", fontSize = 12.sp)
                                    Text("${product.resellPrice} FCT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Your Profit Margin:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("+$profit FCT", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = BrandGreen)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Delivery Charge (All Bangladesh):", fontSize = 11.sp)
                                    Text("200.00 FCT", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    item {
                        Text("Customer Delivery Information", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = receiverName,
                            onValueChange = { receiverName = it },
                            label = { Text("Customer Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = receiverMobile,
                            onValueChange = { receiverMobile = it },
                            label = { Text("Customer Mobile Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = receiverAddress,
                            onValueChange = { receiverAddress = it },
                            label = { Text("Complete Delivery Address (City, District)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                    }

                    if (error.isNotBlank()) {
                        item { Text(error, color = BrandRed, fontSize = 12.sp) }
                    }
                }

                Button(
                    onClick = {
                        if (receiverName.isBlank() || receiverMobile.length < 10 || receiverAddress.isBlank()) {
                            error = "Please fill in all customer delivery details"
                            return@Button
                        }
                        coroutineScope.launch {
                            val res = FleearnRepository.placeResellOrder(
                                productId = product.id,
                                receiverName = receiverName,
                                receiverMobile = receiverMobile,
                                receiverAddress = receiverAddress
                            )
                            if (res.first) onOrderPlaced(res.second) else error = res.second
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Place Resell Order Now", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
