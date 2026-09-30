package com.example.ui.moderator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ModeratorNavCategory(
    val title: String,
    val icon: ImageVector
) {
    SETTINGS("Settings", Icons.Default.Settings),
    HOME("Home", Icons.Default.Home),
    INBOX("Inbox", Icons.Default.Chat)
}
