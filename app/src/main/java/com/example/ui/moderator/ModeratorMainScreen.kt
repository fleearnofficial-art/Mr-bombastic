package com.example.ui.moderator

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.UserProfile
import com.example.data.repository.FleearnRepository
import com.example.ui.theme.*

@Composable
fun ModeratorMainScreen(
    moderator: UserProfile,
    onLogout: () -> Unit
) {
    var currentCategory by remember { mutableStateOf(ModeratorNavCategory.HOME) }
    
    val tickets by FleearnRepository.tickets.collectAsState()
    val reports by FleearnRepository.reports.collectAsState()
    val config by FleearnRepository.appConfig.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = BentoSurfaceCard,
                contentColor = BentoTextPrimary
            ) {
                ModeratorNavCategory.values().forEach { category ->
                    NavigationBarItem(
                        selected = currentCategory == category,
                        onClick = { currentCategory = category },
                        icon = { Icon(category.icon, contentDescription = category.title) },
                        label = { Text(category.title, fontWeight = if (currentCategory == category) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandRed,
                            selectedTextColor = BrandRed,
                            indicatorColor = BrandRed.copy(alpha = 0.2f),
                            unselectedIconColor = BentoTextSecondary,
                            unselectedTextColor = BentoTextSecondary
                        )
                    )
                }
            }
        },
        containerColor = BentoBackground
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentCategory) {
                ModeratorNavCategory.SETTINGS -> ModeratorSettingsTab(
                    moderator = moderator,
                    config = config,
                    onLogout = onLogout
                )
                ModeratorNavCategory.HOME -> ModeratorHomeTab(
                    moderator = moderator,
                    config = config,
                    tickets = tickets,
                    reports = reports
                )
                ModeratorNavCategory.INBOX -> ModeratorInboxTab(
                    moderator = moderator
                )
            }
        }
    }
}
