package com.example.ui.publisher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun PublisherCreateCampaignTab(
    publisher: UserProfile,
    onCreateAdClick: () -> Unit,
    onCreateJobClick: () -> Unit,
    onPostProductClick: () -> Unit,
    onPostServiceClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Create New Campaign", style = MaterialTheme.typography.headlineSmall, color = BentoTextPrimary)
            Text("Launch ads, post jobs, or sell items on Fleearn.", color = BentoTextSecondary, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            CreateOptionCard(
                title = "Create Advertisement",
                subtitle = "Boost your visibility across the network",
                icon = Icons.Default.Campaign,
                color = BrandCyan,
                onClick = onCreateAdClick
            )
        }

        item {
            CreateOptionCard(
                title = "Create Job",
                subtitle = "Post micro-tasks and grow your community",
                icon = Icons.Default.Work,
                color = BrandEmerald,
                onClick = onCreateJobClick
            )
        }

        item {
            CreateOptionCard(
                title = "Post Product",
                subtitle = "List physical or digital items for resell",
                icon = Icons.Default.Storefront,
                color = BrandGold,
                onClick = onPostProductClick
            )
        }

        item {
            CreateOptionCard(
                title = "Post Service",
                subtitle = "Offer professional services or courses",
                icon = Icons.Default.MiscellaneousServices,
                color = BrandPurple,
                onClick = onPostServiceClick
            )
        }
    }
}

@Composable
private fun CreateOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurfaceCard)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.padding(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = BentoTextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = BentoTextSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = BentoTextMuted)
        }
    }
}
