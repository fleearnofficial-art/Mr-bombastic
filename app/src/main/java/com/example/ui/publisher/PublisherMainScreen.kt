package com.example.ui.publisher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.data.repository.FleearnRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublisherMainScreen(
    publisher: UserProfile,
    onLogout: () -> Unit
) {
    val config by FleearnRepository.appConfig.collectAsState()
    val submissions by FleearnRepository.jobSubmissions.collectAsState()
    val ads by FleearnRepository.ads.collectAsState()
    val jobs by FleearnRepository.jobs.collectAsState()
    val products by FleearnRepository.products.collectAsState()
    val services by FleearnRepository.services.collectAsState()
    val productOrders by FleearnRepository.productOrders.collectAsState()
    val serviceOrders by FleearnRepository.serviceOrders.collectAsState()
    val deposits by FleearnRepository.depositRequests.collectAsState()

    var currentCategory by remember { mutableStateOf(PublisherNavCategory.DASHBOARD) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showCreateAdDialog by remember { mutableStateOf(false) }
    var showCreateProductDialog by remember { mutableStateOf(false) }
    // Note: showCreateJobDialog and showCreateServiceDialog are mocked for this demo
    var showSuccessDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    
    val pendingSubmissions = submissions.count { it.status == "PENDING" && it.jobId in jobs.filter { j -> j.publisherUid == publisher.uid }.map { j -> j.id } }
    val pendingProductOrders = productOrders.count { it.status == "PENDING" && it.productId in products.filter { p -> p.publisherUid == publisher.uid }.map { p -> p.id } }
    val pendingServiceOrders = serviceOrders.count { it.status == "PENDING" && it.serviceId in services.filter { s -> s.publisherUid == publisher.uid }.map { s -> s.id } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = BentoSurfaceCard,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "FleConsole", tint = BrandCyan, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("FleConsole", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Publisher Panel", style = MaterialTheme.typography.bodySmall, color = BentoTextSecondary, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(24.dp))
                
                PublisherNavCategory.values().forEach { category ->
                    val isSelected = currentCategory == category
                    NavigationDrawerItem(
                        label = { Text(category.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        selected = isSelected,
                        onClick = {
                            currentCategory = category
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(category.icon, contentDescription = null) },
                        badge = {
                            val badgeCount = category.badgeCount(pendingSubmissions, pendingProductOrders, pendingServiceOrders)
                            if (badgeCount > 0) {
                                Badge(containerColor = BrandRed) { Text(badgeCount.toString(), color = Color.White) }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BrandCyan.copy(alpha = 0.15f),
                            selectedIconColor = BrandCyan,
                            selectedTextColor = BrandCyan,
                            unselectedIconColor = BentoTextSecondary,
                            unselectedTextColor = BentoTextPrimary
                        )
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentCategory.title, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BentoSurface,
                        titleContentColor = BentoTextPrimary,
                        navigationIconContentColor = BentoTextPrimary
                    )
                )
            },
            containerColor = BentoBackground
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentCategory) {
                    PublisherNavCategory.DASHBOARD -> PublisherDashboardTab(
                        publisher = publisher,
                        config = config,
                        ads = ads,
                        jobs = jobs,
                        products = products,
                        services = services,
                        submissions = submissions,
                        productOrders = productOrders,
                        serviceOrders = serviceOrders,
                        onNavigateCategory = { currentCategory = it }, onCreateAdClick = { showCreateAdDialog = true }, onCreateJobClick = { }, onPostProductClick = { showCreateProductDialog = true }, onPostServiceClick = { }, onDepositClick = { currentCategory = PublisherNavCategory.DEPOSIT }
                    )
                    PublisherNavCategory.ACCOUNT -> PublisherAccountTab(
                        publisher = publisher,
                        onSuccess = { t, m -> showSuccessDialog = t to m },
                        onError = { /* Handle error */ }
                    )
                    PublisherNavCategory.CAMPAIGNS -> PublisherCampaignsTab(
                        publisher = publisher,
                        ads = ads,
                        onCreateAdClick = { showCreateAdDialog = true }
                    )
                    PublisherNavCategory.JOBS -> PublisherJobsTab(
                        publisher = publisher,
                        jobs = jobs,
                        onCreateJobClick = { /* Handle job creation */ },
                        onViewSubmissionsClick = { currentCategory = PublisherNavCategory.SUBMISSIONS }
                    )
                    PublisherNavCategory.PRODUCTS -> PublisherProductsTab(
                        publisher = publisher,
                        products = products,
                        onPostProductClick = { showCreateProductDialog = true }
                    )
                    PublisherNavCategory.SERVICES -> PublisherServicesTab(
                        publisher = publisher,
                        services = services,
                        serviceOrders = serviceOrders,
                        onPostServiceClick = { /* Handle service creation */ }
                    )
                    PublisherNavCategory.SUBMISSIONS -> PublisherSubmissionsTab(
                        publisher = publisher,
                        submissions = submissions,
                        onSuccess = { t, m -> showSuccessDialog = t to m }
                    )
                    PublisherNavCategory.PRODUCT_ORDERS -> PublisherProductOrdersScreen(
                        publisher = publisher,
                        orders = productOrders,
                        onSuccess = { t, m -> showSuccessDialog = t to m }
                    )
                    PublisherNavCategory.SERVICE_ORDERS -> PublisherServiceOrdersScreen(
                        publisher = publisher,
                        orders = serviceOrders,
                        onSuccess = { t, m -> showSuccessDialog = t to m }
                    )
                    PublisherNavCategory.DEPOSIT -> PublisherDepositTab(
                        publisher = publisher,
                        config = config,
                        deposits = deposits,
                        onSuccess = { t, m -> showSuccessDialog = t to m }
                    )
                    PublisherNavCategory.CREATE_CAMPAIGN -> PublisherCreateCampaignTab(
                        publisher = publisher,
                        onCreateAdClick = { showCreateAdDialog = true },
                        onCreateJobClick = { /* Show Create Job Dialog */ },
                        onPostProductClick = { showCreateProductDialog = true },
                        onPostServiceClick = { /* Show Create Service Dialog */ }
                    )
                    PublisherNavCategory.SETTINGS -> PublisherSettingsTab(
                        publisher = publisher, config = config,
                        onLogout = onLogout,
                        onSuccess = { t, m -> showSuccessDialog = t to m },
                        onError = { /* Handle error */ }
                    )
                }
            }
        }
    }

    if (showCreateAdDialog) {
        CreateAdDialog(
            publisher = publisher,
            onDismiss = { showCreateAdDialog = false },
            onSuccess = { msg ->
                showCreateAdDialog = false
                showSuccessDialog = "Ad Created" to msg
            }
        )
    }

    if (showCreateProductDialog) {
        CreateProductDialog(
            publisher = publisher,
            onDismiss = { showCreateProductDialog = false },
            onSuccess = { msg ->
                showCreateProductDialog = false
                showSuccessDialog = "Product Added" to msg
            }
        )
    }

    showSuccessDialog?.let { (title, msg) ->
        Success3DDialog(title = title, message = msg, onDismiss = { showSuccessDialog = null })
    }
}

// Ensure the Dialog components are present below (CreateAdDialog, CreateProductDialog)

@Composable
fun CreateAdDialog(publisher: UserProfile, onDismiss: () -> Unit, onSuccess: (String) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var imgUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1551836022-d5d88e9218df?w=640") }
    var destUrl by remember { mutableStateOf("https://google.com") }
    var viewsText by remember { mutableStateOf("5000") }
    var daysText by remember { mutableStateOf("7") }
    var error by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(0.95f), shape = RoundedCornerShape(18.dp)) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Create 320x150 Ad Campaign", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = imgUrl, onValueChange = { imgUrl = it }, label = { Text("Banner Image URL (320x150)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = destUrl, onValueChange = { destUrl = it }, label = { Text("Destination URL") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = viewsText, onValueChange = { viewsText = it }, label = { Text("Target Views") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = daysText, onValueChange = { daysText = it }, label = { Text("Days") }, modifier = Modifier.weight(1f))
                }
                if (error.isNotBlank()) {
                    Text(error, color = BrandRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        val views = viewsText.toIntOrNull() ?: 5000
                        val days = daysText.toIntOrNull() ?: 7
                        coroutineScope.launch {
                            val (success, msg) = FleearnRepository.createAd(imgUrl, destUrl, views, days, 100.0)
                            if (success) {
                                onSuccess(msg)
                            } else {
                                error = msg
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Launch Campaign (100 FCT)")
                }
            }
        }
    }
}

@Composable
fun CreateProductDialog(publisher: UserProfile, onDismiss: () -> Unit, onSuccess: (String) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Electronics") }
    var wholesaleText by remember { mutableStateOf("800") }
    var resellText by remember { mutableStateOf("1100") }
    var thumbUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500") }
    var error by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(0.95f), shape = RoundedCornerShape(18.dp)) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Add Drop Shipping Product", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Product Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = wholesaleText, onValueChange = { wholesaleText = it }, label = { Text("Wholesale (FCT)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = resellText, onValueChange = { resellText = it }, label = { Text("Resell Price") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = thumbUrl, onValueChange = { thumbUrl = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth())
                if (error.isNotBlank()) {
                    Text(error, color = BrandRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        val w = wholesaleText.toDoubleOrNull() ?: 800.0
                        val r = resellText.toDoubleOrNull() ?: 1100.0
                        coroutineScope.launch {
                            val (success, msg) = FleearnRepository.postProduct(
                                title = title,
                                description = desc,
                                category = category,
                                adminPrice = w,
                                resellPrice = r,
                                stock = 50,
                                thumbnails = listOf(thumbUrl)
                            )
                            if (success) {
                                onSuccess(msg)
                            } else {
                                error = msg
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Publish to Marketplace (15 FCT fee)")
                }
            }
        }
    }
}
