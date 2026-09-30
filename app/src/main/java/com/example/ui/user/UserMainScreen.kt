package com.example.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.*

enum class UserNavTab(val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    HOME("Home", Icons.Default.Home, Icons.Outlined.Home),
    JOBS("Job", Icons.Default.Work, Icons.Outlined.WorkOutline),
    SHIPPING("Shipping", Icons.Default.LocalShipping, Icons.Outlined.LocalShipping),
    CONNECTIONS("Connection", Icons.Default.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    SETTINGS("Setting", Icons.Default.Settings, Icons.Outlined.Settings)
}

@Composable
fun UserMainScreen(
    user: UserProfile,
    onLogout: () -> Unit
) {
    var currentTab by remember { mutableStateOf(UserNavTab.HOME) }

    Scaffold(
        bottomBar = {
            // Bento Grid Sleek Navigation Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_bottom_navigation"),
                color = BentoSurfaceNav,
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = BentoBorderSubtle,
                            shape = androidx.compose.ui.graphics.RectangleShape
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserNavTab.values().forEach { tab ->
                            val isSelected = currentTab == tab
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                    .clickable { currentTab = tab }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("nav_tab_${tab.name.lowercase()}")
                            ) {
                                // Active Indicator Top Dot
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) BrandEmerald else Color.Transparent)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Icon(
                                    imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) BrandEmerald else BentoTextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) BrandEmerald else BentoTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                UserNavTab.HOME -> UserHomeScreen(
                    user = user,
                    onNavigateToJobs = { currentTab = UserNavTab.JOBS },
                    onNavigateToShipping = { currentTab = UserNavTab.SHIPPING }
                )
                UserNavTab.JOBS -> UserJobsScreen()
                UserNavTab.SHIPPING -> UserShippingScreen()
                UserNavTab.CONNECTIONS -> UserConnectionsScreen(user = user)
                UserNavTab.SETTINGS -> UserSettingsScreen(user = user, onLogout = onLogout)
            }
        }
    }
}

