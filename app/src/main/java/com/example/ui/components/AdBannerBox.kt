package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AdItem
import com.example.data.repository.FleearnRepository
import com.example.ui.theme.BrandCyan
import kotlinx.coroutines.delay

@Composable
fun AdBannerBox(
    modifier: Modifier = Modifier,
    adList: List<AdItem> = emptyList()
) {
    val context = LocalContext.current
    val ads = if (adList.isNotEmpty()) adList else FleearnRepository.ads.collectAsState().value.filter { it.isActive && it.isApproved }
    
    var currentIndex by remember { mutableStateOf(0) }

    // 7-second rotation algorithm
    LaunchedEffect(ads.size) {
        if (ads.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(7000)
            if (ads.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % ads.size
            }
        }
    }

    if (ads.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sponsored Space (320x150)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val currentAd = ads[currentIndex.coerceIn(0, ads.size - 1)]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                FleearnRepository.recordAdClick(currentAd.id)
                InAppBrowserManager.openUrl(currentAd.destinationUrl, "Sponsored Ad")
            }
            .testTag("ad_banner_box_${currentAd.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        AnimatedContent(
            targetState = currentAd,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ad_transition"
        ) { targetAd ->
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = targetAd.imageUrl,
                    contentDescription = "Sponsored Ad",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                startY = 50f
                            )
                        )
                )

                // Sponsored pill
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AD • 7s Rotation",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandCyan
                    )
                }

                // Publisher info and CTA
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = targetAd.publisherName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = "Tap to view sponsored offer",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open Link",
                        tint = BrandCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
