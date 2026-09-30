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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceItem
import com.example.data.model.ServiceOrder
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun PublisherServicesTab(
    publisher: UserProfile,
    services: List<ServiceItem>,
    serviceOrders: List<ServiceOrder>,
    onPostServiceClick: () -> Unit
) {
    val myServices = services.filter { it.publisherUid == publisher.uid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                        Text("Active Professional Services", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "${myServices.size} Services Listed • Freelance Hub",
                            fontSize = 12.sp,
                            color = BentoTextMuted
                        )
                    }

                    Button(
                        onClick = onPostServiceClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Post Service", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        if (myServices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MiscellaneousServices, contentDescription = null, tint = BrandGold, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Services Offered Yet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Offer graphic design, video editing, development, or consultation services to users.", fontSize = 12.sp, color = BentoTextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onPostServiceClick, colors = ButtonDefaults.buttonColors(containerColor = BrandGold)) {
                                Text("Post First Service (20 FCT fee)", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(myServices, key = { it.id }) { service ->
                val ordersCount = serviceOrders.count { it.serviceId == service.id }

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
                            Text("Service ID: ${service.id}", fontSize = 11.sp, color = BentoTextMuted)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (service.isApproved) BrandGreen.copy(alpha = 0.2f) else BrandGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (service.isApproved) "APPROVED" else "UNDER REVIEW",
                                    color = if (service.isApproved) BrandGreen else BrandGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(service.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(service.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Price: %.2f FCT".format(service.price),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = BrandCyan
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandPurple.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$ordersCount Client Orders",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandPurple,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
