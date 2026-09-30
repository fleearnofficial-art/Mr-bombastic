package com.example.ui.publisher

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductOrder
import com.example.data.model.ServiceOrder
import com.example.data.model.UserProfile
import com.example.data.repository.FleearnRepository
import com.example.ui.components.InAppBrowserManager
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PublisherProductOrdersScreen(
    publisher: UserProfile,
    orders: List<ProductOrder>,
    onSuccess: (String, String) -> Unit
) {
    var filter by remember { mutableStateOf("ALL") }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())

    val myOrders = orders.filter { it.publisherUid == publisher.uid }
    val filteredOrders = when (filter) {
        "PENDING" -> myOrders.filter { it.status == "PENDING" }
        "SHIPPED" -> myOrders.filter { it.status == "SHIPPED" }
        "DELIVERED" -> myOrders.filter { it.status == "DELIVERED" }
        else -> myOrders
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary & Filters
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Product Orders Management", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Manage customer fulfillment, delivery tracking and escrow releases.", fontSize = 11.sp, color = BentoTextMuted)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ALL" to "All", "PENDING" to "Pending", "SHIPPED" to "Shipped", "DELIVERED" to "Delivered").forEach { (key, label) ->
                            val count = when (key) {
                                "PENDING" -> myOrders.count { it.status == "PENDING" }
                                "SHIPPED" -> myOrders.count { it.status == "SHIPPED" }
                                "DELIVERED" -> myOrders.count { it.status == "DELIVERED" }
                                else -> myOrders.size
                            }
                            FilterChip(
                                selected = filter == key,
                                onClick = { filter = key },
                                label = { Text("$label ($count)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Product Orders Found", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Orders from drop shipping resellers will appear here automatically.", fontSize = 12.sp, color = BentoTextMuted)
                        }
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Order #${order.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Date: ${dateFormat.format(Date(order.timestamp))}", fontSize = 10.sp, color = BentoTextMuted)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (order.status) {
                                    "DELIVERED" -> BrandGreen.copy(alpha = 0.2f)
                                    "SHIPPED" -> BrandCyan.copy(alpha = 0.2f)
                                    else -> BrandGold.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = order.status,
                                    color = when (order.status) {
                                        "DELIVERED" -> BrandGreen
                                        "SHIPPED" -> BrandCyan
                                        else -> BrandGold
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(order.productTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Customer UID: ${order.buyerUid} • Product ID: ${order.productId}", fontSize = 11.sp, color = BentoTextMuted)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Customer & Delivery info
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Receiver: ${order.receiverName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Mobile: ${order.receiverMobile}", fontSize = 12.sp, color = BrandCyan)
                                Text("Delivery Address: ${order.receiverAddress}", fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Resell Amount: %.0f FCT • Profit: %.0f FCT • Delivery Charge: %.0f FCT"
                                        .format(order.resellPrice, order.profit, order.deliveryCharge),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Order actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (order.status == "PENDING") {
                                Button(
                                    onClick = {
                                        FleearnRepository.updateProductOrderStatus(order.id, "SHIPPED")
                                        onSuccess("Order Shipped", "Order #${order.id} marked as dispatched.")
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
                                ) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark Shipped", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (order.status == "SHIPPED" || order.status == "PENDING") {
                                Button(
                                    onClick = {
                                        FleearnRepository.updateProductOrderStatus(order.id, "DELIVERED")
                                        onSuccess("Order Delivered", "Order #${order.id} marked as successfully delivered.")
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark Delivered", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
fun PublisherServiceOrdersScreen(
    publisher: UserProfile,
    orders: List<ServiceOrder>,
    onSuccess: (String, String) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    val myOrders = orders.filter { it.publisherUid == publisher.uid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Service Orders & Client Bookings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Direct contact via WhatsApp with clients requesting your professional freelance services.", fontSize = 11.sp, color = BentoTextMuted)
                }
            }
        }

        if (myOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = BrandGold, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Service Orders Yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Client bookings for your design, development and consultation services will appear here.", fontSize = 12.sp, color = BentoTextMuted)
                        }
                    }
                }
            }
        } else {
            items(myOrders, key = { it.id }) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Order #${order.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Placed: ${dateFormat.format(Date(order.timestamp))}", fontSize = 10.sp, color = BentoTextMuted)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (order.status == "COMPLETED") BrandGreen.copy(alpha = 0.2f) else BrandCyan.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = order.status,
                                    color = if (order.status == "COMPLETED") BrandGreen else BrandCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(order.serviceTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Customer UID: ${order.buyerUid} • Fee Paid: %.2f FCT".format(order.feePaid), fontSize = 12.sp, color = BrandCyan)

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Client WhatsApp Contact:", fontSize = 10.sp, color = BentoTextMuted)
                                    Text(order.buyerWhatsApp, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        val cleanNum = order.buyerWhatsApp.replace(Regex("[^0-9]"), "")
                                        InAppBrowserManager.openUrl("https://wa.me/$cleanNum", "WhatsApp Client")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (order.status != "COMPLETED") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    FleearnRepository.updateServiceOrderStatus(order.id, "COMPLETED")
                                    onSuccess("Service Completed", "Order #${order.id} marked as completed.")
                                },
                                modifier = Modifier.fillMaxWidth().height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                            ) {
                                Text("Mark Service as Completed", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
