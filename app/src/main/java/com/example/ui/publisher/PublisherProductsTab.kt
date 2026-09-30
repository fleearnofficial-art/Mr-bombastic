package com.example.ui.publisher

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
import com.example.data.model.ProductItem
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun PublisherProductsTab(
    publisher: UserProfile,
    products: List<ProductItem>,
    onPostProductClick: () -> Unit
) {
    val myProducts = products.filter { it.publisherUid == publisher.uid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Resell Products", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "${myProducts.size} Items Cataloged • Drop Shipping Hub",
                            fontSize = 12.sp,
                            color = BentoTextMuted
                        )
                    }

                    Button(
                        onClick = onPostProductClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Product", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (myProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Products Cataloged", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Upload products for community resellers to sell across Bangladesh with automated escrow.", fontSize = 12.sp, color = BentoTextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onPostProductClick, colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)) {
                                Text("Add First Product (15 FCT fee)")
                            }
                        }
                    }
                }
            }
        } else {
            items(myProducts, key = { it.id }) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = product.thumbnails.firstOrNull() ?: "",
                            contentDescription = product.title,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ID: ${product.id}", fontSize = 10.sp, color = BentoTextMuted)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (product.isApproved) BrandGreen.copy(alpha = 0.2f) else BrandGold.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (product.isApproved) "APPROVED" else "PENDING",
                                        color = if (product.isApproved) BrandGreen else BrandGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(product.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                            Text("Category: ${product.category}", fontSize = 11.sp, color = BentoTextMuted)

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Wholesale: %.0f FCT".format(product.adminPrice), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandCyan)
                                Text("Resale: %.0f FCT".format(product.resellPrice), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                                Text("Stock: ${product.stock}", fontSize = 11.sp, color = BentoTextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
