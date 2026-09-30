package com.example.ui.publisher

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class PublisherNavCategory(
    val title: String,
    val icon: ImageVector,
    val badgeCount: (pendingSubs: Int, pendingProdOrders: Int, pendingSrvOrders: Int) -> Int = { _, _, _ -> 0 }
) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    ACCOUNT("Account / Plan", Icons.Default.WorkspacePremium),
    CAMPAIGNS("Active Campaigns", Icons.Default.Campaign),
    JOBS("Active Jobs", Icons.Default.Work),
    PRODUCTS("Active Products", Icons.Default.Storefront),
    SERVICES("Active Services", Icons.Default.MiscellaneousServices),
    SUBMISSIONS("Job Submissions", Icons.Default.AssignmentTurnedIn, { subs, _, _ -> subs }),
    PRODUCT_ORDERS("Product Orders", Icons.Default.LocalShipping, { _, prodOrders, _ -> prodOrders }),
    SERVICE_ORDERS("Service Orders", Icons.Default.ReceiptLong, { _, _, srvOrders -> srvOrders }),
    DEPOSIT("Deposit", Icons.Default.AccountBalanceWallet),
    CREATE_CAMPAIGN("Campaign (Create)", Icons.Default.AddCircle),
    SETTINGS("Settings", Icons.Default.Settings)
}
